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
 **** AST�� ���������� �湮�Ͽ� x86�������� ��ȯ�ϰ� ����ϴ� ���α׷� ****
 ********************************************************
 * MiniCAstVisitor�� ���� AST�� ���������, �� ������ ���������� �湮
 * �Ͽ� Assembley�ڵ�� ��ȯ�ϰ� ����Ѵ�.
 * �� ��尴ü�� accept��� �޼��带 ���� ������, ȣ�� �� �Ʒ��� visit 
 * �޼��带 �湮�Ѵ�.
 * 
 * --�ٽ� �������-- 
 * - symTab	: ������, ������ �ɺ��� �ϳ��� ���ڵ� ���·� 
 *			    ����, ����, �˻������ �����ϴ� �ڷᱸ��.
 ********************************************************/
public class AssembleyGenVisitor implements ASTVisitor {
	private SymbolTable symTab = new SymbolTable();
	private int label = 0; 		/*�����̳� �ݺ��� ��� �� jmp�ϱ� ���� ���*/
	private int offset = 0; 	/*���� ������ �ּҸ� ã�ư��� ���� ����	*/
	private int paramIndex=0; 	/*�Լ� ȣ���� �̷���� ������ 0���� �ʱ�ȭ ��.*/
	

	/*******************************************************
	 * ������ .data�� .text�� �з��Ͽ� ����Ѵ�.
	 * .data �������� ���������� ���� �� �ʱ�ȭ�� �̷������. 
	 * .text �������� ������ �Լ����� ����� �ڵ�� ��µȴ�.
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
	 * �Լ��� �̸��� Ÿ��, �Ķ������ ������ �ľ��Ͽ� �ɺ����̺� �����Ѵ�.
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
	 * .section ��� �޼���
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
	 *�������������� �����Ͽ� ������ڵ�� ��ȯ�Ͽ� ����Ѵ�.
	 *����� �ش� ������ ���� �پ��� ������ �ϳ��� ���ڵ忡 ��� �ɺ����̺�
	 *�����Ѵ�.
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
	 * �Լ������.
	 * �������� ������ ���� Compound_Statement�� �ڽĳ��������, Compound_Statement��
	 * while���̳� if�������� ���ȴ�. ���������� ���� ��ġ�� �Լ� ���� �ٷ� �������� �� �� �ֵ���
	 * �ϱ� ���� �̰����� ���������� ������ ����ϵ��� �����ߴ�. 
	 * ���� ���������� ������ ���������� �ɺ����̺��� ���ԵǸ�, �Լ��� ����Ǹ� �ɺ����̺��� ��� ����
	 * �ϵ��� �����ߴ�.
	 **************************************************/
	@Override
	public void visitFunction_Declaration(Function_Declaration node) {
		// TODO Auto-generated method stub
		String varName = node.t_node.getText();
		int returnType = this.typeCheck(node.type);		// int : 1, void : 0
		int numOfArguments;
		int stackSize = this.symTab.symbolTable.size(); //�Լ� ����� �ɺ����̺� ������ ���������� �����ϱ� ���� �ʱ� ����� ����.
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
		this.emit("sub	$"+(local_Decl_Size) + ", %esp"); /* ���ÿ� ���ú��� ũ�� ����*/
		iterator = node.compount_stmt.local_decls.iterator();
		while(iterator.hasNext()) {
			child = iterator.next();
			child.accept(this);
		}
		
		node.compount_stmt.accept(this);
		this.emitFunc_Decl_End();
		/*�ɺ����̺� ���� �ʱ�ȭ - �ش� �Լ����� ���Ե� ��� �ɺ��� ����*/
		for (int i = this.symTab.symbolTable.size(); i > stackSize; i--) {
			this.symTab.delete();
		}
	}


	/**************************************************
	 * �ڽĳ���� �Ķ������ accept()�� ���������� ȣ���Ѵ�.
	 * child.accept()�� ȣ��� ������ this.paramIndex�� 1�� ��
	 * ���ϱ� ������, ��� child�� �湮�� ���� this.paramIndex�� 0
	 * ���� �ʱ�ȭ���ش�.
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
	 * visitParameter�� �Լ�����ο��� ȣ��ȴ�. �Լ� ������ � 
	 * ���忡�� �Ű������� ����� ��, �� ��ġ�� �˱� ���� �Ű������� ������
	 * �ɺ����̺� �����Ѵ�.
	 * �ɺ��� �Ķ������ ��� value�� �Ķ���� �ε����� ����ִ´�. 
	 * x86���� �� �ε����� �ش��ϴ� �������ʹ� 8(%ebp)���� �����Ͽ� ���ʷ� �ö󰣴�.
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
	 * �ڽĳ���� expr�� ���Ͽ� visitExpression()�� ȣ���Ѵ�.
	 **************************************************/
	@Override
	public void visitExpression_Statement(Expression_Statement node) {
		// TODO Auto-generated method stub
		// �� ������� ó���κ�
		node.expr.accept(this);
	}

	
	/**************************************************
	 * while���� x86���� �ΰ��� ���̺��� ����Ѵ�. 
	 * ���̺��� ����� ������ �ʵ庯���� this.label�� 1�� �����Ͽ� 
	 * ���̺� ���� ��������ش�.
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
	 * �߰�ȣ{} �� �̷���� ������ �����̴�.
	 * C����� Ư¡�� �������� ������ �Լ��� ���� �������� �̷������ ����
	 * �����ϱ� ���� ���չ� ������ ���������� ��ȿ�� �ϰ� Statement�� 
	 * ó���Ѵ�.
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
	 * �������� ������ 3����(������ �迭, ����, ���� ���� �� �Ҵ�)�� �����Ѵ�.
	 * ������ ���� ���������� ���������� ������ ���� ������ �ɹ����̺� �����Ѵ�.
	 *******************************************************/
	@Override
	public void visitLocal_Declaration(Local_Declaration node) {
		/*���������� ���� offset �����Ͽ� �ɺ����̺� ����.*/
		// TODO Auto-generated method stub
		String varName = node.lhs.getText();
		int type = this.typeCheck(node.type);	// int : 1, void : 0
		Record r, preR;
		preR = this.symTab.symbolTable.peek();
		int pre_Local_Offset = preR.getOffset();
		int local_Offset = pre_Local_Offset + preR.getSize();
		
		if (node instanceof Local_Variable_Declaration_Array) { // ���� �迭 ����
			int arrSize = Integer.parseInt(((Local_Variable_Declaration_Array) node).rhs.getText());
			int size = arrSize * this.typeSize(type);
			r = new Record(varName , "LOCVARARR", type, 1, local_Offset, size, 0);		
			this.symTab.insert(varName, r);
		}
		else if (node instanceof Local_Variable_Declaration_Assign) { // �������� ���� �� �ʱ�ȭ
			// MiniC �������� ���� �� �Ҵ翡�� rhs �� �� �� �ִ°��� ���ͷ� ���̴�.
			int initialValue = Integer.parseInt(((Local_Variable_Declaration_Assign) node).rhs.getText());
			int size = this.typeSize(type);
			r = new Record(varName , "LOCVAR", type, 1, local_Offset, size, initialValue);
			this.emitLocVarInit(local_Offset, initialValue);
			this.symTab.insert(varName, r);
		}
		else { 	// �������� ����
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
	 * if���� else�� ������ ���� ���̺��� ������ �޶�����.
	 * while���� ���������� ���̺��� ����� ������ this.label�� 1�� ������Ų��.
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
	 * x86���� ���ϰ��� �׻� eax��� �������Ϳ� ����ȴ�. ��ȯ Ÿ���� �ִ� 
	 * �Լ��� ȣ���� ����, eax�� ���� �� �Լ��� ��ȯ���� ����ְ� �ȴ�.
	 * �Լ� ����� return�� ��Ű�� �ʾƵ� ret�� ��µǵ��� �ϱ�����
	 * this.emitFunc_Decl_End()���� ret ����� �ϰ�, �Լ� ����ο��� ȣ���Ѵ�.
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
						System.out.println("Return_Statement. �������� �ʴ� ������");
						this.error(1);
					}
				}
			} else { 
				/**expr�� nonterminal �� ��, accept ���� ���� %edx�� �����Ų��.
				 **���� %edx�� ���� %eax�� �ű��. */
				node.expr.accept(this);
				System.out.println("	mov	%edx, %eax");
			}
		} 
	}


	
	
	/*******************************************************
	 * MiniC���� ����ϴ� Expr��  �迭 ����, �迭 ���� �Ҵ�, ������ �� �Ҵ�,
	 * �Ǻ���, ���׿���, �Լ� ȣ��, ��ȣ ���� �ִ�.
	 * �� �۰Դ� �͹̳� ���� �� �������̳� ���ͷ��� ��Ÿ�� ������ ���������, 
	 * �͹̳� ����� ��� �ش� ����� �̸��� �ʿ��ϹǷ� accept���� �ʰ� 
	 * toString()�� ���� �����´�.
	 * Expr�� ������ ���� x86���� �����ϴ� ������� �� �޶����Ƿ� �����Ͽ� ó���Ѵ�.
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
				if (this.isNum(tnode)) { // �캯�� �͹̳��� �� ���ͷ����� ��������.
					rAddr = "$"+tnode;
				} else {
					rAddr = this.discTerminal(tnode); // rhs �ּ�
				}
				System.out.println("	mov	"+rAddr+", %edx");
			} else { 
				/************************************************************
				 * rhs �� non terminal �̹Ƿ� �ѹ� �� accept ���ش�.
				 * x[1] = a+b; �� ���� ������ ��� a+b�� ���� exprVisitor�� �湮�� ���̰�, 
				 * �� ����� %edx�� ����ȴ�. 
				 ************************************************************/
				rhs.accept(this); 
			}
			if (stIndex >= 0) {
				Expression lhs = ((ArefAssignNode) node).lhs;
				if(lhs instanceof TerminalExpression) {
					String tnode = ((TerminalExpression) lhs).t_node.toString();
					if (this.isNum(tnode)) { // �캯�� �͹̳��� �� ���ͷ����� ��������.
						int arrayIndex = Integer.parseInt(tnode);
						lAddr = this.discTerminal(name, arrayIndex);
						System.out.println("	mov	%edx, " + lAddr);
					} else {
						// �̱���. x[y] = 1 ����
						System.out.println("ArefAssignNode �̱���. x[y] = 1 ����");
						this.error(1);
					}
				} else {
					// �̱���. x[1+2] = 1 ����
					//((ArefAssignNode) node).lhs.accept(this);
					System.out.println("ArefAssignNode �̱���. x[1+2] = 1 ����");
					this.error(1);
				}
				
			}
			else {
				System.out.println("ArefAssign������ �������� ����.");
				this.error(1);
			}
		}
		else if ( node instanceof ArefNode) {
			String name = ((ArefNode) node).t_node.toString();
			Expression expr = ((ArefNode) node).expr;
			if(expr instanceof TerminalExpression) {
				int arrayIndex;
				String tnode = ((TerminalExpression) expr).t_node.toString();
				if(this.isNum(tnode)) { // x[1] ����
					String arrayAddr;
					arrayIndex = Integer.parseInt(tnode);
					arrayAddr = this.discTerminal(name, arrayIndex);
					System.out.println("	mov	"+arrayAddr+", %edx");
				} else {
					// �̱���. x[y] ����.
					System.out.println("ArefNode �̱���. x[y] ����.");
					this.error(1);
				}
			} else {
				// �̿ϼ�. x[1+2] �� ���� ����
				System.out.println("ArefNode �̱���. x[1+2] �� ���� ����.");
				this.error(1);
			}
		}
		else if ( node instanceof AssignNode) {
			String name = ((AssignNode) node).t_node.toString();
			lAddr = this.discTerminal(name);
			Expression expr = ((AssignNode) node).expr;
			if(lAddr != null) {
				if (expr instanceof TerminalExpression) { // �캯�� �͹̳��϶�
					String tnode = ((TerminalExpression) expr).t_node.toString();
					if(this.isNum(tnode)) {
						rAddr = "$"+tnode;
					} else {
						rAddr = this.discTerminal(tnode);
					}
					System.out.println("	mov	"+rAddr+ ", %edx" +"\n" +
									   "	mov	%edx, "+lAddr);
				} else { // �캯�� ���͹̳��� ��
					expr.accept(this);
					System.out.println("	mov	%edx"+", "+lAddr);
				}
			}else {
				System.out.println(" AssignNode : �������� �ʴ� ����");
			}
		}
		else if ( node instanceof BinaryOpNode) { // lhs ������ eax��, rhs ������ ecx�� �����Ѵ�.
			Expression lhs = ((BinaryOpNode) node).lhs;
			Expression rhs = ((BinaryOpNode) node).rhs;
			String terminal1, terminal2;
			int val1, val2;
			
			// lhs �� %eax �� �Ű� ����.
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
						System.out.println("BinaryOpNode lhs : �������� �ʴ� ������");
						this.error(1);
					}
				}
			} else {
				lhs.accept(this);
				System.out.println("	mov	%edx, %eax");
			}
			
			// rhs �� %ecx �� �Ű� ����.
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
						System.out.println("BinaryOpNode rhs : �������� �ʴ� ������");
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
	            System.out.println("�Լ��� ���ǵ��� �ʾҽ��ϴ�.");
	            this.error(1);
	         }
	         else {
	            String argument = ((FuncallNode) node).args.toString();
	            String[] arguments = argument.split(", ");
	            int numberOfArgument = arguments.length;
	            if(numberOfArgument > 4) {
	            	System.out.println("�ش� �����Ϸ��� ���ڸ� 4�������� �޽��ϴ�.");
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
							System.out.println("FuncallNode : �������� �ʴ� ������");
						}
					}
	            }
	            System.out.println("\t"+ "call" + "\t" + funName );            
	         }
		}
		else if ( node instanceof ParenExpression) {	// ��ȣ () �̱���
	
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
					System.out.println("UnaryOpNode expr : ���ʿ��� ����.");
					this.error(1);
				} else {
					lAddr = this.discTerminal(terminal);
					if(lAddr == null) {
						System.out.println("UnaryOpNode expr : �������� �ʴ� ������");
						this.error(1);
					}
				}
				this.emitUnary(op, lAddr);
			} else { // %edx�� �Ű� ������ ���� �� %edx���� �޸𸮿� �ű��.
				expr.accept(this);
				this.emitUnary(op, "%edx");
				if(expr instanceof ArefNode) {
					lAddr = ((ArefNode) expr).t_node.toString();
					Expression expr_expr = ((ArefNode) expr).expr;
					int arrayIndex;
					if (expr_expr instanceof TerminalExpression) {
						String expr_expr_term = ((TerminalExpression) expr_expr).t_node.toString();
						if(this.isNum(expr_expr_term)) { // ++y[1] ����.
							arrayIndex = Integer.parseInt(expr_expr_term);
							lAddr = this.discTerminal(lAddr, arrayIndex);
							System.out.println("	mov	%edx, "+ lAddr);
						} else { // �̱���. ++y[x] ����.
							System.out.println("UnaryOpNode �̱���. ++y[x] ����.");
							this.error(1);
						}
					} else {  // �̱���. ++y[1+2] ����.
						System.out.println("UnaryOpNode �̱���. ++y[1+2] ����.");
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
	
	
	/* ���� �޼���*/
	
	/*************************************
	 * Expression���� ���׿���Ŀ� ���� ��� �޼���.
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
	 * Expression���� ���׿���Ŀ� ���� ��� �޼���.
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
			System.out.println("UnaryOpNode Error: �������� ���� ���̽�");
			this.error(1);

		}
	}


	/*************************************
	 * ���ǽĿ��� ����ϴ� ���۷��̼ǿ� ���� 
	 * ������ ������ɾ ����Ѵ�.
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
	 * add, sub�� ���� ����� ���
	 *************************************/
	private void emit1(String op) {
		// TODO Auto-generated method stub
		System.out.println("	"+op+"	%ecx, %eax\n" +
						   "	mov	%eax, %edx");
	}

	/*************************************
	 * �Լ� ����� �ʿ��� ����� ����� ���
	 *************************************/
	private void emitFunc_Decl_Start(String varName) {
		// TODO Auto-generated method stub
		System.out.println(	"	.globl	" + varName + "\n" +
							varName+":" + "\n" +
							"	push	%ebp" + "\n" +
							"	mov	%esp, %ebp");
	}

	/*************************************
	 * �Լ� ����� �ʿ��� ����� ����� ���
	 *************************************/
	private void emitFunc_Decl_End() {
		// TODO Auto-generated method stub
		System.out.println(	"	mov	%ebp, %esp" +"\n" +
							"	pop	%ebp" +"\n"	+
							"	ret\n");
	}


	/*************************************
	 * �͹̳� ��尡 �������� ���������� �Ǻ��Ѵ�.
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
	 * ���Ŀ� ��߳� �Է��� ������ ��� erroró��.
	 *************************************/
	private void error(int errNum) {
		// TODO Auto-generated method stub
		System.out.println("���� : " + errNum);
		System.exit(0);
	}

	/*************************************
	 * type�� ���� ũ�⸦ ��ȯ�Ѵ�.
	 *************************************/
	private int typeSize(int type) {
		// TODO Auto-generated method stub
		if (type == SymbolTable.TYPE_INT) {
			return 4;
		}
		else return 0;
	}
	
	/************************************************************
	 * �ɹ����̺� Ÿ���� ������ ��, ����ϸ�, int���� 1, void���� 0�� ��ȯ�Ѵ�.
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
	 * ������ ����� ��, �ش� ������ ���������� �����ؾ��ϴ� �������ͳ� �ּҰ� �޶����� �ȴ�.
	 * �������, ���������� ��� esp�� 0��°���� �����ϸ�, �Ű������� ��� 8(%ebp)���� 
	 * �����Ѵ�.
	 * discTerminal()�� �־��� �̸����� �����ϴ� ������ �ɹ����̺��� ã�� ������ �ľ��ϰ�,
	 * �׿� �´� ������ �ּҸ� ��ȯ���ִ� ������ �Ѵ�.
	 * �迭�� ��� index�� �ʿ��ϹǷ� �����ε��Ͽ� �� �޼���� ������ �ۼ��ߴ�.
	 * �ɺ��� �Ķ������ ��� value���� �Ķ���� �ε���(1���� ����)�� ����ִ�.
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
	 * ������ ����� ��, �ش� ������ ���������� �����ؾ��ϴ� �������ͳ� �ּҰ� �޶����� �ȴ�.
	 * �������, ���������� ��� esp�� 0��°���� �����ϸ�, �Ű������� ��� 8(%ebp)���� 
	 * �����Ѵ�.
	 * discTerminal()�� �־��� �̸����� �����ϴ� ������ �ɹ����̺��� ã�� ������ �ľ��ϰ�,
	 * �׿� �´� ������ �ּҸ� ��ȯ���ִ� ������ �Ѵ�.
	 * �迭�� ��� index�� �ʿ��ϹǷ� �����ε��Ͽ� �� �޼���� ������ �ۼ��ߴ�.
	 * �ɺ��� �Ķ������ ��� value���� �Ķ���� �ε���(1���� ����)�� ����ִ�.
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
			case "VARARR" : // ���ڷ� �����迭�� ���� ��� �ּҸ� ����
				result = "$"+r.getName();
				System.out.println("	lea	$"+r.getName()+", %edx");
				result = "%edx"; 
				break;
			case "LOCVARARR" : //// ���ڷ� �����迭�� ���� ��� �ּҸ� ����
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
