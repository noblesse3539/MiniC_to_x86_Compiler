package Domain;

import java.util.Iterator;
import Domain.MiniCNode;
import Domain.Program;
import Domain.Args.Arguments;
import Domain.Decl.Declaration;
import Domain.Decl.Function_Declaration;
import Domain.Decl.Local_Declaration;
import Domain.Decl.Local_Variable_Declaration_Array;
import Domain.Decl.Local_Variable_Declaration_Assign;
import Domain.Decl.Variable_Declaration;
import Domain.Decl.Variable_Declaration_Array;
import Domain.Decl.Variable_Declaration_Assign;
import Domain.Expr.ArefAssignNode;
import Domain.Expr.ArefNode;
import Domain.Expr.AssignNode;
import Domain.Expr.BinaryOpNode;
import Domain.Expr.Expression;
import Domain.Expr.FuncallNode;
import Domain.Expr.ParenExpression;
import Domain.Expr.TerminalExpression;
import Domain.Expr.UnaryOpNode;
import Domain.Param.ArrayParameter;
import Domain.Param.Parameter;
import Domain.Param.Parameters;
import Domain.Stmt.Compound_Statement;
import Domain.Stmt.Expression_Statement;
import Domain.Stmt.If_Statement;
import Domain.Stmt.Return_Statement;
import Domain.Stmt.Statement;
import Domain.Stmt.While_Statement;
import Domain.Type_spec.TypeSpecification;
import symboltable.Record;
import symboltable.SymbolTable;


/********************************************************
 **** AST를 순차적으로 방문하여 x86문법으로 변환하고 출력하는 프로그램 ****
 ********************************************************
 * MiniCAstVisitor에 의해 AST가 만들어지면, 각 노드들을 순차적으로 방문
 * 하여 Assembley코드로 변환하고 출력한다.
 * 각 노드객체는 accept라는 메서드를 갖고 있으며, 호출 시 아래의 visit 
 * 메서드를 방문한다.
 * 
 * --핵심 멤버변수-- 
 * - symTab	: 변수명, 값등의 심볼을 하나의 레코드 형태로 
 *			    저장, 삭제, 검색기능을 수행하는 자료구조.
 ********************************************************/
public class AssembleyGenVisitor implements ASTVisitor {
	private SymbolTable symTab = new SymbolTable();
	private int label = 0; 		/*조건이나 반복문 사용 시 jmp하기 위해 사용*/
	private int offset = 0; 	/*전역 변수의 주소를 찾아가기 위한 변수	*/
	private int paramIndex=0; 	/*함수 호출이 이루어질 때마다 0으로 초기화 됨.*/
	

	/*******************************************************
	 * 섹션을 .data와 .text로 분류하여 출력한다.
	 * .data 영역에서 전역변수의 선언 및 초기화가 이루어진다. 
	 * .text 영역에서 각각의 함수들이 어셈블리 코드로 출력된다.
	 *******************************************************/
	@Override
	public void visitProgram(Program node) {
		// TODO Auto-generated method stub
		Iterator<Declaration> iterator = node.decls.listIterator();
		MiniCNode child = null;
		
		this.emitSection(".data");
		while (iterator.hasNext()) {
			child = iterator.next();
			if (child instanceof Variable_Declaration) {
				child.accept(this);
			}
			else if (child instanceof Function_Declaration) {
				this.FunctionHeader((Function_Declaration)child);
			}
			else {
				this.error(1);
			}
		}
		
		this.emitSection(".text");
		// function part
		iterator = node.decls.listIterator();
		while (iterator.hasNext()) {
			child = iterator.next();
			if (child instanceof Function_Declaration) {
				child.accept(this);
			}
		}
	}

	/**************************************************
	 * 함수의 이름과 타입, 파라미터의 개수를 파악하여 심볼테이블에 삽입한다.
	 **************************************************/
	private void FunctionHeader(Function_Declaration child) {
		// TODO Auto-generated method stub
		String funcName = child.t_node.getText();
		int type = this.typeCheck(child.type);
		int width;
		if (child.params.params != null) {
			width = child.params.params.size();
		}
		else {
			width = 0;
		}
		Record record = new Record(funcName, "FUNC", type, 0, 0, width, 0);
		this.symTab.insert(funcName, record);
		
	}

	/*********************
	 * .section 출력 메서드
	 *********************/
	private void emitSection(String string) {
		// TODO Auto-generated method stub
		System.out.println(".section	"+ string);
	}


	@Override
	public void visitDeclaration(Declaration node) {
		// TODO Auto-generated method stub

	}

	/**************************************************
	 *전역변수선언을 구분하여 어셈블리코드로 변환하여 출력한다.
	 *선언시 해당 변수에 대한 다양한 정보를 하나의 레코드에 담고 심볼테이블에
	 *삽입한다.
	 **************************************************/
	@Override
	public void visitVariable_Declaration(Variable_Declaration node) {
		// TODO Auto-generated method stub
		String varName = node.lhs.getText();
		int type = this.typeCheck(node.type);	// int : 1, void : 0
		Record r;
		
		if (node instanceof Variable_Declaration_Array) {
			int arrSize = Integer.parseInt(((Variable_Declaration_Array) node).rhs.getText());
			int size = arrSize * this.typeSize(type);
			r = new Record(varName , "VARARR", type, SymbolTable.INBLOCK, offset, size, 0);
			System.out.println("	.comm	"+ varName +", "+ size);
			offset += size;
		}
		else if (node instanceof Variable_Declaration_Assign) {
			int initialValue = Integer.parseInt(((Variable_Declaration_Assign) node).rhs.getText());
			int size = this.typeSize(type);
			r = new Record(varName , "VAR", type, SymbolTable.INBLOCK, offset, size, initialValue);
			System.out.println(varName + ":\n" +
								"	.int	"+ initialValue);
			offset += this.typeSize(type);
			
		}
		else {
			int size = this.typeSize(type);
			r = new Record(varName , "VAR", type, SymbolTable.INBLOCK, offset, size, 0);	
			System.out.println("	.comm	"+varName+", "+size);
			offset += this.typeSize(type);
		}
		this.symTab.insert(varName, r);
		
	}



	@Override
	public void visitTypeSpecification(TypeSpecification node) {
		// TODO Auto-generated method stub
		
	}

	/**************************************************
	 * 함수선언부.
	 * 지역변수 선언은 원래 Compound_Statement의 자식노드이지만, Compound_Statement는
	 * while문이나 if문에서도 사용된다. 지역변수의 선언 위치를 함수 선언 바로 다음에만 올 수 있도록
	 * 하기 위해 이곳에서 지역변수의 선언을 담당하도록 설계했다. 
	 * 지역 변수에대한 정보는 마찬가지로 심볼테이블이 삽입되며, 함수가 종료되면 심볼테이블에서 모두 삭제
	 * 하도록 설계했다.
	 **************************************************/
	@Override
	public void visitFunction_Declaration(Function_Declaration node) {
		// TODO Auto-generated method stub
		String varName = node.t_node.getText();
		int returnType = this.typeCheck(node.type);		// int : 1, void : 0
		int numOfArguments;
		int stackSize = this.symTab.symbolTable.size(); //함수 종료시 심볼테이블에 저장한 지역변수를 삭제하기 위해 초기 사이즈를 저장.
		if(node.params.params == null) {
			numOfArguments = 0;
		} else {
			numOfArguments = node.params.params.size();			
		}
		int local_Decl_Size = 0;
		Local_Declaration child;
		Iterator<Local_Declaration> iterator;
		Record r;
		r = new Record(varName , "FUNC", returnType, 0, 0, numOfArguments, 0);
		this.symTab.insert(varName, r);
		this.emitFunc_Decl_Start(varName);
		node.params.accept(this);
		
		iterator = node.compount_stmt.local_decls.iterator();
		while (iterator.hasNext()) {
			child = iterator.next();
			if (child instanceof Local_Variable_Declaration_Array) {
				local_Decl_Size += Integer.parseInt(((Local_Variable_Declaration_Array) child).rhs.getText()) * 4;
			} else {
				local_Decl_Size += 4;
			}
		}
		this.emit("sub	$"+(local_Decl_Size) + ", %esp"); /* 스택에 로컬변수 크기 지정*/
		iterator = node.compount_stmt.local_decls.iterator();
		while(iterator.hasNext()) {
			child = iterator.next();
			child.accept(this);
		}
		
		node.compount_stmt.accept(this);
		this.emitFunc_Decl_End();
		/*심볼테이블 스택 초기화 - 해당 함수에서 삽입된 모든 심볼들 삭제*/
		for (int i = this.symTab.symbolTable.size(); i > stackSize; i--) {
			this.symTab.delete();
		}
	}


	/**************************************************
	 * 자식노드인 파라미터의 accept()를 순차적으로 호출한다.
	 * child.accept()가 호출될 때마다 this.paramIndex는 1씩 증
	 * 가하기 때문에, 모든 child를 방문한 이후 this.paramIndex를 0
	 * 으로 초기화해준다.
	 **************************************************/
	@Override
	public void visitParameters(Parameters node) {
		// TODO Auto-generated method stub
		if (node.params != null) {
			Iterator<Parameter> iterator = node.params.listIterator();
			while (iterator.hasNext()) {
				MiniCNode child = iterator.next();
				child.accept(this);		
			}
			this.paramIndex = 0;
		}
	}

	/**************************************************
	 * visitParameter는 함수선언부에서 호출된다. 함수 내부의 어떤 
	 * 문장에서 매개변수를 사용할 때, 그 위치를 알기 위해 매개변수의 정보도
	 * 심볼테이블에 삽입한다.
	 * 심볼이 파라미터인 경우 value에 파라미터 인덱스를 집어넣는다. 
	 * x86에서 각 인덱스에 해당하는 레지스터는 8(%ebp)부터 시작하여 차례로 올라간다.
	 **************************************************/
	@Override
	public void visitParameter(Parameter node) {
		// TODO Auto-generated method stub
		String varName = node.t_node.toString();
		int type = this.typeCheck(node.type);	// int : 1, void : 0
		Record r;
		if(node instanceof ArrayParameter) {
			r = new Record(varName , "ARRAYPARAMETER", type, SymbolTable.INBLOCK, 0, 0, ++this.paramIndex);
		} else {
			
			r = new Record(varName , "PARAMETER", type, SymbolTable.INBLOCK, 0, 0, ++this.paramIndex);			
		}
		this.symTab.insert(varName, r);
	}

	@Override
	public void visitStatement(Statement node) {
	}

	
	/**************************************************
	 * 자식노드인 expr에 대하여 visitExpression()을 호출한다.
	 **************************************************/
	@Override
	public void visitExpression_Statement(Expression_Statement node) {
		// TODO Auto-generated method stub
		// 각 문장들의 처리부분
		node.expr.accept(this);
	}

	
	/**************************************************
	 * while문은 x86에서 두개의 레이블을 사용한다. 
	 * 레이블은 사용할 때마다 필드변수인 this.label을 1씩 증가하여 
	 * 레이블 명을 변경시켜준다.
	 **************************************************/
	@Override
	public void visitWhile_Statement(While_Statement node) {
		// TODO Auto-generated method stub
		int label1 = ++this.label;
		int label2 = ++this.label;
		System.out.println(".L"+label1+":");
		node.expr.accept(this);
		this.emitConditionJmp(node.expr);
		System.out.println(".L"+label2);
		node.stmt.accept(this);
		System.out.println("	jmp	.L"+label1);
		System.out.println(".L"+label2+":");
	}

	
	/**************************************************
	 * 중괄호{} 로 이루어진 블럭단위 문장이다.
	 * C언어의 특징인 지역변수 선언이 함수의 가장 위에서만 이루어지는 것을
	 * 구현하기 위해 복합문 내에선 변수선언을 무효로 하고 Statement만 
	 * 처리한다.
	 **************************************************/
	@Override
	public void visitCompound_Statement(Compound_Statement node) {
		// TODO Auto-generated method stub
		Iterator<Statement> iterator2 = node.stmts.listIterator();

		while (iterator2.hasNext()) {
			Statement child = iterator2.next();
			child.accept(this);		
		}
	}

	private void emit(String string) {
		// TODO Auto-generated method stub
		System.out.println("	"+ string);
	}


	/*******************************************************
	 * 지역변수 선언은 3가지(정수형 배열, 정수, 정수 선언 및 할당)로 구분한다.
	 * 구분한 이후 전역변수와 마찬가지로 변수에 대한 정보를 심벌테이블에 삽입한다.
	 *******************************************************/
	@Override
	public void visitLocal_Declaration(Local_Declaration node) {
		/*지역변수에 대한 offset 설정하여 심볼테이블에 삽입.*/
		// TODO Auto-generated method stub
		String varName = node.lhs.getText();
		int type = this.typeCheck(node.type);	// int : 1, void : 0
		Record r, preR;
		preR = this.symTab.symbolTable.peek();
		int pre_Local_Offset = preR.getOffset();
		int local_Offset = pre_Local_Offset + preR.getSize();
		
		if (node instanceof Local_Variable_Declaration_Array) { // 지역 배열 선언
			int arrSize = Integer.parseInt(((Local_Variable_Declaration_Array) node).rhs.getText());
			int size = arrSize * this.typeSize(type);
			r = new Record(varName , "LOCVARARR", type, 1, local_Offset, size, 0);		
			this.symTab.insert(varName, r);
		}
		else if (node instanceof Local_Variable_Declaration_Assign) { // 지역변수 선언 및 초기화
			// MiniC 지역변수 선언 및 할당에서 rhs 에 올 수 있는것은 리터럴 뿐이다.
			int initialValue = Integer.parseInt(((Local_Variable_Declaration_Assign) node).rhs.getText());
			int size = this.typeSize(type);
			r = new Record(varName , "LOCVAR", type, 1, local_Offset, size, initialValue);
			this.emitLocVarInit(local_Offset, initialValue);
			this.symTab.insert(varName, r);
		}
		else { 	// 지역변수 선언만
			int size = this.typeSize(type);
			r = new Record(varName , "LOCVAR", type, 1, local_Offset, size, 0);	
			this.symTab.insert(varName, r);
		}
	}

	private void emitLocVarInit(int offset, int value) {
		// TODO Auto-generated method stub
		System.out.println("	mov	$"+ value+", "+ offset+"(%esp)");
	}


	/**********************************************************
	 * if문은 else의 유무에 따라 레이블의 갯수가 달라진다.
	 * while문과 마찬가지로 레이블은 사용할 때마다 this.label을 1씩 증가시킨다.
	 **********************************************************/
	@Override
	public void visitIf_Statement(If_Statement node) {
		// TODO Auto-generated method stub
		int label1 = ++this.label , label2;
		if (node.else_stmt == null) {
			node.expr.accept(this);
			this.emitConditionJmp(node.expr);
			System.out.println(".L"+label1);
			node.if_stmt.accept(this);
			System.out.println(".L"+label1+":");
		}
		else {
			label2 = ++this.label;
			node.expr.accept(this);
			this.emitConditionJmp(node.expr);
			System.out.println(".L"+label1);
			node.if_stmt.accept(this);
			System.out.println("	jmp	.L"+label2);
			System.out.println(".L"+label1+":");
			node.else_stmt.accept(this);
			System.out.println(".L"+label2+":");
		}
	}


	/**************************************************************
	 * x86에서 리턴값은 항상 eax라는 레지스터에 저장된다. 반환 타입이 있는 
	 * 함수를 호출한 이후, eax의 값엔 그 함수의 반환값이 들어있게 된다.
	 * 함수 종료시 return을 시키지 않아도 ret이 출력되도록 하기위해
	 * this.emitFunc_Decl_End()에서 ret 출력을 하고, 함수 선언부에서 호출한다.
	 **************************************************************/
	@Override
	public void visitReturn_Statement(Return_Statement node) {
		// TODO Auto-generated method stub
		int value;
		String arr;
		String terminal;
		if (node.expr != null) {
			Expression retExpr = node.expr;
			if (retExpr instanceof TerminalExpression) {
				terminal = ((TerminalExpression) retExpr).t_node.toString();
				if (this.isNum(terminal)) {
					value = Integer.parseInt(terminal);
					System.out.println("	mov	$"+value+", %eax");
				} else {
					arr = this.discTerminal(terminal);
					if(arr != null) {
						System.out.println("	mov	"+arr+", %eax");						
					} else {
						System.out.println("Return_Statement. 존재하지 않는 변수명");
						this.error(1);
					}
				}
			} else { 
				/**expr이 nonterminal 일 때, accept 이후 값은 %edx에 저장시킨다.
				 **따라서 %edx의 값을 %eax로 옮긴다. */
				node.expr.accept(this);
				System.out.println("	mov	%edx, %eax");
			}
		} 
	}


	
	
	/*******************************************************
	 * MiniC에서 허용하는 Expr은  배열 참조, 배열 참조 할당, 변수에 값 할당,
	 * 판별식, 단항연산, 함수 호출, 괄호 등이 있다.
	 * 더 작게는 터미널 노드로 각 변수명이나 리터럴을 나타낼 때에도 사용하지만, 
	 * 터미널 노드의 경우 해당 노드의 이름만 필요하므로 accept하지 않고 
	 * toString()을 통해 가져온다.
	 * Expr의 종류에 따라 x86에서 수행하는 문장들이 다 달라지므로 구분하여 처리한다.
	 *******************************************************/
	@Override
	public void visitExpression(Expression node) {
		// TODO Auto-generated method stub
		int stIndex;
		String lAddr="",rAddr ="";
		if ( node instanceof ArefAssignNode) {
			String name = ((ArefAssignNode) node).t_node.toString();
			stIndex = this.symTab.lookUp(name);
			Expression rhs = ((ArefAssignNode) node).rhs;
			if(rhs instanceof TerminalExpression) { 
				String tnode = ((TerminalExpression) rhs).t_node.toString();
				if (this.isNum(tnode)) { // 우변이 터미널일 때 리터럴인지 변수인지.
					rAddr = "$"+tnode;
				} else {
					rAddr = this.discTerminal(tnode); // rhs 주소
				}
				System.out.println("	mov	"+rAddr+", %edx");
			} else { 
				/************************************************************
				 * rhs 는 non terminal 이므로 한번 더 accept 해준다.
				 * x[1] = a+b; 와 같은 형식일 경우 a+b에 대한 exprVisitor를 방문할 것이고, 
				 * 그 결과가 %edx에 저장된다. 
				 ************************************************************/
				rhs.accept(this); 
			}
			if (stIndex >= 0) {
				Expression lhs = ((ArefAssignNode) node).lhs;
				if(lhs instanceof TerminalExpression) {
					String tnode = ((TerminalExpression) lhs).t_node.toString();
					if (this.isNum(tnode)) { // 우변이 터미널일 때 리터럴인지 변수인지.
						int arrayIndex = Integer.parseInt(tnode);
						lAddr = this.discTerminal(name, arrayIndex);
						System.out.println("	mov	%edx, " + lAddr);
					} else {
						// 미구현. x[y] = 1 형식
						System.out.println("ArefAssignNode 미구현. x[y] = 1 형식");
						this.error(1);
					}
				} else {
					// 미구현. x[1+2] = 1 형식
					//((ArefAssignNode) node).lhs.accept(this);
					System.out.println("ArefAssignNode 미구현. x[1+2] = 1 형식");
					this.error(1);
				}
				
			}
			else {
				System.out.println("ArefAssign변수가 존재하지 않음.");
				this.error(1);
			}
		}
		else if ( node instanceof ArefNode) {
			String name = ((ArefNode) node).t_node.toString();
			Expression expr = ((ArefNode) node).expr;
			if(expr instanceof TerminalExpression) {
				int arrayIndex;
				String tnode = ((TerminalExpression) expr).t_node.toString();
				if(this.isNum(tnode)) { // x[1] 형식
					String arrayAddr;
					arrayIndex = Integer.parseInt(tnode);
					arrayAddr = this.discTerminal(name, arrayIndex);
					System.out.println("	mov	"+arrayAddr+", %edx");
				} else {
					// 미구현. x[y] 형식.
					System.out.println("ArefNode 미구현. x[y] 형식.");
					this.error(1);
				}
			} else {
				// 미완성. x[1+2] 와 같은 형식
				System.out.println("ArefNode 미구현. x[1+2] 와 같은 형식.");
				this.error(1);
			}
		}
		else if ( node instanceof AssignNode) {
			String name = ((AssignNode) node).t_node.toString();
			lAddr = this.discTerminal(name);
			Expression expr = ((AssignNode) node).expr;
			if(lAddr != null) {
				if (expr instanceof TerminalExpression) { // 우변이 터미널일때
					String tnode = ((TerminalExpression) expr).t_node.toString();
					if(this.isNum(tnode)) {
						rAddr = "$"+tnode;
					} else {
						rAddr = this.discTerminal(tnode);
					}
					System.out.println("	mov	"+rAddr+ ", %edx" +"\n" +
									   "	mov	%edx, "+lAddr);
				} else { // 우변이 논터미널일 때
					expr.accept(this);
					System.out.println("	mov	%edx"+", "+lAddr);
				}
			}else {
				System.out.println(" AssignNode : 존재하지 않는 변수");
			}
		}
		else if ( node instanceof BinaryOpNode) { // lhs 변수는 eax에, rhs 변수는 ecx에 저장한다.
			Expression lhs = ((BinaryOpNode) node).lhs;
			Expression rhs = ((BinaryOpNode) node).rhs;
			String terminal1, terminal2;
			int val1, val2;
			
			// lhs 를 %eax 로 옮겨 담음.
			if (lhs instanceof TerminalExpression) {
				terminal1 = ((TerminalExpression) lhs).t_node.toString();
				if (this.isNum(terminal1)) {
					val1 = Integer.parseInt(terminal1);
					System.out.println("	mov	"+ val1 + ", %eax");
				} else {
					lAddr = this.discTerminal(terminal1);
					if(lAddr != null) {
						System.out.println("	mov	"+ lAddr + ", %eax");						
					} else {
						System.out.println("BinaryOpNode lhs : 존재하지 않는 변수명");
						this.error(1);
					}
				}
			} else {
				lhs.accept(this);
				System.out.println("	mov	%edx, %eax");
			}
			
			// rhs 를 %ecx 로 옮겨 담음.
			if (rhs instanceof TerminalExpression) {
				terminal2 = ((TerminalExpression) rhs).t_node.toString();
				if (this.isNum(terminal2)) {
					val2 = Integer.parseInt(terminal2);
					System.out.println("	mov	"+ val2 + ", %ecx");
				}
				else {
					rAddr = this.discTerminal(terminal2);
					if(rAddr != null) {
						System.out.println("	mov	"+ rAddr + ", %ecx");						
					} else {
						System.out.println("BinaryOpNode rhs : 존재하지 않는 변수명");
						this.error(1);
					}
				}
			} else {
				rhs.accept(this);	
				System.out.println("	mov	%edx, %ecx");
			}
			this.emitBinaryOp(((BinaryOpNode) node).op);
		}
		else if ( node instanceof FuncallNode) {
			String funName = ((FuncallNode) node).t_node.toString();
	         stIndex = this.symTab.lookUp(funName);
	         if ( stIndex < 0) {
	            System.out.println("함수가 정의되지 않았습니다.");
	            this.error(1);
	         }
	         else {
	            String argument = ((FuncallNode) node).args.toString();
	            String[] arguments = argument.split(", ");
	            int numberOfArgument = arguments.length;
	            if(numberOfArgument > 4) {
	            	System.out.println("해당 컴파일러는 인자를 4개까지만 받습니다.");
	            	this.error(1);
	            }
	            int val;
	            for(int i=arguments.length-1; i >= 0; i--) {
	            	if (this.isNum(arguments[i])) {
						val = Integer.parseInt(arguments[i]);
						System.out.println("	push	$"+val);
					}
					else {
						
						String arg = this.discTerminal(arguments[i]);
						if (arg != null) {
							System.out.println("	push	"+arg);
						} else {
							System.out.println("FuncallNode : 존재하지 않는 변수명");
						}
					}
	            }
	            System.out.println("\t"+ "call" + "\t" + funName );            
	         }
		}
		else if ( node instanceof ParenExpression) {	// 괄호 () 미구현
	
		}
		else if ( node instanceof TerminalExpression) {
			
		}
		else if ( node instanceof UnaryOpNode) { // UnaryOpNode
			String op = ((UnaryOpNode) node).op;
			Expression expr = ((UnaryOpNode) node).expr;
			String terminal;
			if (expr instanceof TerminalExpression) {
				terminal = ((TerminalExpression) expr).t_node.toString();
				if (this.isNum(terminal)) {
					System.out.println("UnaryOpNode expr : 불필요한 연산.");
					this.error(1);
				} else {
					lAddr = this.discTerminal(terminal);
					if(lAddr == null) {
						System.out.println("UnaryOpNode expr : 존재하지 않는 변수명");
						this.error(1);
					}
				}
				this.emitUnary(op, lAddr);
			} else { // %edx에 옮겨 연산을 수행 후 %edx값을 메모리에 옮긴다.
				expr.accept(this);
				this.emitUnary(op, "%edx");
				if(expr instanceof ArefNode) {
					lAddr = ((ArefNode) expr).t_node.toString();
					Expression expr_expr = ((ArefNode) expr).expr;
					int arrayIndex;
					if (expr_expr instanceof TerminalExpression) {
						String expr_expr_term = ((TerminalExpression) expr_expr).t_node.toString();
						if(this.isNum(expr_expr_term)) { // ++y[1] 형식.
							arrayIndex = Integer.parseInt(expr_expr_term);
							lAddr = this.discTerminal(lAddr, arrayIndex);
							System.out.println("	mov	%edx, "+ lAddr);
						} else { // 미구현. ++y[x] 형식.
							System.out.println("UnaryOpNode 미구현. ++y[x] 형식.");
							this.error(1);
						}
					} else {  // 미구현. ++y[1+2] 형식.
						System.out.println("UnaryOpNode 미구현. ++y[1+2] 형식.");
						this.error(1);
					}	
				}
			}
			
		}
	}

	@Override
	public void visitArguments(Arguments node) {
		// TODO Auto-generated method stub
		
	}
	
	
	/* 보조 메서드*/
	
	/*************************************
	 * Expression에서 이항연산식에 대한 출력 메서드.
	 *************************************/
	private void emitBinaryOp(String op) {
		// TODO Auto-generated method stub
		switch(op) {
		case "*" :	System.out.println(	"	mul	%cx");	break;
		case "/" :	System.out.println(	"	div	%ecx");	break;
		case "%" :	System.out.println(	"	div	%ecx" +"\n" +
										"	mov	%edx, $eax");	break;
		case "+" :	this.emit1("add");	break;
		case "-" :	this.emit1("sub");	break;
		case "==" :	System.out.println(	"	cmp	%ecx, %eax");	break;
		case "!=" :	System.out.println(	"	cmp	%ecx, %eax");	break;
		case "<=" :	System.out.println(	"	cmp	%ecx, %eax");	break;
		case "<" :	System.out.println(	"	cmp	%ecx, %eax");	break;
		case ">=" :	System.out.println(	"	cmp	%ecx, %eax");	break;
		case ">" :	System.out.println(	"	cmp	%ecx, %eax");	break;
		case "and":	this.emit1("and");	break;
		case "or" :	this.emit1("or");	break;
		}
	}


	/*************************************
	 * Expression에서 단항연산식에 대한 출력 메서드.
	 *************************************/
	private void emitUnary(String op, String addr) {
		// TODO Auto-generated method stub
		switch (op) {
		case "--":
			System.out.println("	sub	$1, "+addr);
			break;
		case "++":
			System.out.println("	add	$1, "+addr);
			break;
		case "-":
		case "+":
		case "!":
		default :
			System.out.println("UnaryOpNode Error: 설정하지 않은 케이스");
			this.error(1);

		}
	}


	/*************************************
	 * 조건식에서 사용하는 오퍼레이션에 따라 
	 * 각각의 점프명령어를 출력한다.
	 *************************************/
	private void emitConditionJmp(Expression expr) {
		// TODO Auto-generated method stub
		if(expr instanceof BinaryOpNode) {
			
			switch(((BinaryOpNode)expr).op) {
			case "==" :
				System.out.print("	jne	");
				break;
			case "!=" :
				System.out.print("	je	");
				break;
			case "<=" :
				System.out.print("	jg	");
				break;
			case "<" :
				System.out.print("	jge	");
				break;
			case ">=" :
				System.out.print("	jl	");
				break;
			case ">" :
				System.out.print("	jle	");
				break;
			default :
				System.out.print("	jz	");
				break;
			
			}
		}
	}
	
	/*************************************
	 * add, sub와 같은 명령을 출력
	 *************************************/
	private void emit1(String op) {
		// TODO Auto-generated method stub
		System.out.println("	"+op+"	%ecx, %eax\n" +
						   "	mov	%eax, %edx");
	}

	/*************************************
	 * 함수 선언시 필요한 어셈블리 명령을 출력
	 *************************************/
	private void emitFunc_Decl_Start(String varName) {
		// TODO Auto-generated method stub
		System.out.println(	"	.globl	" + varName + "\n" +
							varName+":" + "\n" +
							"	push	%ebp" + "\n" +
							"	mov	%esp, %ebp");
	}

	/*************************************
	 * 함수 종료시 필요한 어셈블리 명령을 출력
	 *************************************/
	private void emitFunc_Decl_End() {
		// TODO Auto-generated method stub
		System.out.println(	"	mov	%ebp, %esp" +"\n" +
							"	pop	%ebp" +"\n"	+
							"	ret\n");
	}


	/*************************************
	 * 터미널 노드가 숫자인지 문자인지를 판별한다.
	 *************************************/
	private boolean isNum(String terminal) {
		// TODO Auto-generated method stub
		if (terminal == null || terminal =="") {
			return false;
		}
		for (int i =0; i< terminal.length(); i++) {
			if (terminal.charAt(i) < '0' || terminal.charAt(i) >'9') {
				return false;
			}
		}
		return true;
	}

	/*************************************
	 * 형식에 어긋난 입력이 들어왔을 경우 error처리.
	 *************************************/
	private void error(int errNum) {
		// TODO Auto-generated method stub
		System.out.println("에러 : " + errNum);
		System.exit(0);
	}

	/*************************************
	 * type에 따라 크기를 반환한다.
	 *************************************/
	private int typeSize(int type) {
		// TODO Auto-generated method stub
		if (type == SymbolTable.TYPE_INT) {
			return 4;
		}
		else return 0;
	}
	
	/************************************************************
	 * 심벌테이블에 타입을 기입할 때, 사용하며, int형은 1, void형은 0을 반환한다.
	 ************************************************************/
	private int typeCheck(TypeSpecification type) {
		// TODO Auto-generated method stub
		if (type.toString().equals("int")) {
			return SymbolTable.TYPE_INT;
		}
		else if (type.toString().equals("void")) {
			return SymbolTable.TYPE_VOID;
		}
		else {
			return -1;
		}
	}
	
	
	/*******************************************************************
	 * 변수를 사용할 때, 해당 변수의 정보에따라 참조해야하는 레지스터나 주소가 달라지게 된다.
	 * 예를들면, 지역변수의 경우 esp의 0번째부터 시작하며, 매개변수의 경우 8(%ebp)부터 
	 * 시작한다.
	 * discTerminal()는 주어진 이름으로 시작하는 변수를 심벌테이블에서 찾아 종류를 파악하고,
	 * 그에 맞는 적절한 주소를 반환해주는 역할을 한다.
	 * 배열일 경우 index도 필요하므로 오버로딩하여 두 메서드로 나누어 작성했다.
	 * 심볼이 파라미터인 경우 value에는 파라미터 인덱스(1부터 시작)가 들어있다.
	 *******************************************************************/
	public String discTerminal(String name, int index) {
		int symIndex = this.symTab.lookUp(name);
		String kind;
		Record r;
		String result;
		int varOffset;
		if( symIndex >= 0) {
			r = this.symTab.symbolTable.elementAt(symIndex);
			kind = r.getKind();
			switch(kind) {
			
			case "VARARR" :
				result = "$"+r.getName()+"+"+(4*index);
				break;
			case "LOCVARARR" :
				varOffset = r.getOffset();
				result = (varOffset + index*4)+"(%esp)"; 
				break;
			case "ARRAYPARAMETER" :
				System.out.println("	mov	"+(4+4*r.getValue())+"(%ebp), %edx");
				result = (4*index)+"(%edx)";
				break;
			default :
				result = null;
			}
			return result;
		} else {
			return null;
		}
	}
	
	/*******************************************************************
	 * 변수를 사용할 때, 해당 변수의 정보에따라 참조해야하는 레지스터나 주소가 달라지게 된다.
	 * 예를들면, 지역변수의 경우 esp의 0번째부터 시작하며, 매개변수의 경우 8(%ebp)부터 
	 * 시작한다.
	 * discTerminal()는 주어진 이름으로 시작하는 변수를 심벌테이블에서 찾아 종류를 파악하고,
	 * 그에 맞는 적절한 주소를 반환해주는 역할을 한다.
	 * 배열일 경우 index도 필요하므로 오버로딩하여 두 메서드로 나누어 작성했다.
	 * 심볼이 파라미터인 경우 value에는 파라미터 인덱스(1부터 시작)가 들어있다.
	 *******************************************************************/
	public String discTerminal(String name) {
		int symIndex = this.symTab.lookUp(name);
		String kind;
		Record r;
		String result;
		int varOffset;
		if( symIndex >= 0) {
			r = this.symTab.symbolTable.elementAt(symIndex);
			kind = r.getKind();
			switch(kind) {
			case "VAR" :
				result = "$"+r.getName();
				break;
			case "LOCVAR" :
				varOffset = r.getOffset();
				result = (varOffset)+"(%esp)"; 
				break;
			case "PARAMETER" :
				result = (4+4*r.getValue())+"(%ebp)";
				break;
			case "VARARR" : // 인자로 전역배열을 넣을 경우 주소를 전달
				result = "$"+r.getName();
				System.out.println("	lea	$"+r.getName()+", %edx");
				result = "%edx"; 
				break;
			case "LOCVARARR" : //// 인자로 지역배열을 넣을 경우 주소를 전달
				varOffset = r.getOffset();
				System.out.println("	lea	"+(varOffset)+"(%esp), %edx");
				result = "%edx"; 
				break;
			default :
				result = null;
			}
			return result;
		} else {
			return null;
		}
	}
}
