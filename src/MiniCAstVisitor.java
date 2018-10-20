
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import Domain.*;
import Domain.Args.Arguments;
import Domain.Decl.*;
import Domain.Expr.*;
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

/*******************************************************
 **** C파일을 AST(Abstract Syntax Tree)로 정의하는 프로그램  ****
 *******************************************************
 *실행시 정상적인 코드를 트리형태로 만들고 Program이라는 객체로 반환한다.
 *Program은 인스턴스로 Decls를 갖고 있으며 Decl의 리스트타입이다. 
 *Decl의 인스턴스로는 함수선언과 전역변수 선언이 있으며, 함수선언의 인스턴스
 *로 타입과 파라미터, Compound_Stmt등이 있다.
 *이와 같은 방식으로 객체들을 생성하고 연결하여 하나의 구문트리를 구성한다.
 *******************************************************/
public class MiniCAstVisitor extends MiniCBaseVisitor<MiniCNode> {

	
	/************************************
	 * Program의 child인 decl을 차례로 방문하고,
	 * Program 객체를 만들어 반환한다.
	 * **********************************/
	@Override
	public Program visitProgram(MiniCParser.ProgramContext ctx) {
		// TODO Auto-generated method stub
		List<Declaration> decls = new LinkedList<Declaration>();
		Iterator iterator = ctx.decl().iterator();
		while(iterator.hasNext()) {
			decls.add((Declaration)visit((MiniCParser.DeclContext)iterator.next()));
		}
		return new Program(decls);
	}

	@Override
	public Declaration visitDecl(MiniCParser.DeclContext ctx) {
		// TODO Auto-generated method stub
		return (Declaration)visit(ctx.getChild(0));
	}

	
	/***************************************************
	 * 전역변수선언을 3가지(배열, 정수, 정수 선언 및 초기화)로 구분하고,
	 * 해당하는 객체를 생성하여 반환한다.
	 * *************************************************/
	@Override
	public Variable_Declaration visitVar_decl(MiniCParser.Var_declContext ctx) {
		// TODO Auto-generated method stub
		TypeSpecification type = (TypeSpecification)visit(ctx.type_spec());
		TerminalNode lhs = (TerminalNode)ctx.IDENT();
		String comp = ctx.getChild(2).getText();
		if(comp.equals(";")) {
			return new Variable_Declaration(type, lhs);
		}
		else {
			TerminalNode rhs = (TerminalNode)ctx.LITERAL();
			if(comp.equals("=")) {
				return new Variable_Declaration_Assign(type, lhs, rhs);
			}
			else {
				return new Variable_Declaration_Array(type, lhs, rhs);
			}
		}
	}

	
	/**********************************************************
	 * 함수와 변수의 타입을 의미하며 TypeSpecification객체를 생성하여 반환한다.
	 * int 인지 void 인지를 판별하여 멤버변수를 초기화한다.
	 * ********************************************************/
	@Override
	public TypeSpecification visitType_spec(MiniCParser.Type_specContext ctx) {
		// TODO Auto-generated method stub
		String s = ctx.getChild(0).getText().toUpperCase();
		return new TypeSpecification(TypeSpecification.Type.valueOf(s));
	}

	
	/**************************************************
	 * Function_Declaration 객체를 생성하여 반환한다.
	 * 자식 노드인 Parameters와 Compound_Statement를 방문한다.
	 **************************************************/
	@Override
	public Function_Declaration visitFun_decl(MiniCParser.Fun_declContext ctx) {
		// TODO Auto-generated method stub
		TypeSpecification type = (TypeSpecification)visit(ctx.type_spec());
		TerminalNode t_node = (TerminalNode)ctx.IDENT();
		Parameters params = (Parameters)visit(ctx.params());
		Compound_Statement compound_stmt = (Compound_Statement)visit(ctx.compound_stmt());
		return new Function_Declaration(type, t_node, params, compound_stmt);
	}

	
	/****************************************************************
	 * 함수 선언시 파라미터부분을 담당한다. 필요한 경유 자식노드인 Parameter를 방문한다.
	 * C에서 파라미터의 예는 main() main(void) main(int,...) 등이 올 수 있으므로,
	 * 각각을 구분하여 Parameters객체를 생성하여 반환한다.
	 ****************************************************************/
	@Override
	public Parameters visitParams(MiniCParser.ParamsContext ctx) {
		// TODO Auto-generated method stub
		int childNum = ctx.getChildCount();
		if ( childNum == 0) {
			return new Parameters();
		}
		else if(ctx.getChild(0) == ctx.VOID()) {
			return new Parameters(new TypeSpecification(TypeSpecification.Type.VOID));
		}
		else {
			List<Parameter> list = new LinkedList<Parameter>();
			Iterator iterator = ctx.param().iterator();
			while(iterator.hasNext()) {
				list.add((Parameter)visit((MiniCParser.ParamContext)iterator.next()));
			}
			return new Parameters(list);
		}
	}

	
	/************************************
	 * Parameter 객체를 생성하여 반환한다.
	 * 정수형과 정수형배열로 구분할 수 있다.
	 ************************************/
	@Override
	public Parameter visitParam(MiniCParser.ParamContext ctx) {
		// TODO Auto-generated method stub
		TypeSpecification type = (TypeSpecification)visit(ctx.type_spec());
		TerminalNode t_node = (TerminalNode)ctx.IDENT();
		if(ctx.getChildCount() == 2) {
			return new Parameter(type,t_node);
		}
		else {
			return new ArrayParameter(type,t_node);
		}
	}

	/************************************************
	 * 문장으로 해석되나 블록으로 구분하는것이 더 이해하기 수월하다.
	 * Stmt에는 {} 형태도 올 수 있고, 하나의 문장단위도 올 수 있다.
	 ************************************************/
	@Override
	public Statement visitStmt(MiniCParser.StmtContext ctx) {
		// TODO Auto-generated method stub
		return (Statement)visit(ctx.getChild(0));
	}

	
	/*****************************************
	 * 자식노드인 Expression을 방문하고,
	 * Expression_Statement객체를 생성하여 반환한다.
	 *****************************************/
	@Override
	public Expression_Statement visitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		// TODO Auto-generated method stub
		Expression expr = (Expression)visit(ctx.expr());
		return new Expression_Statement(expr);
	}

	
	/********************************************************
	 * while문의 구조는 while (expr) stmt 이다. 
	 * stmt에는 단순히 한 문장이 올 수 있고, {} 형식이 올 수도 있다.
	 * 각각의 자식 노드를 방문한 뒤 While_Statement객체를 생성하여 반환한다. 
	 ********************************************************/
	@Override
	public While_Statement visitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		// TODO Auto-generated method stub
		TerminalNode while_node = (TerminalNode)ctx.WHILE();
		Expression expr =(Expression)visit(ctx.expr());
		Statement stmt = (Statement)visit(ctx.stmt());
		return new While_Statement(while_node, expr, stmt);
	}

	
	/**********************************************
	 * {} 구조를 가진 블럭이다. 
	 * 함수 선언이나 while, if stmt 등의 자식노드로 호출된다.
	 * 지역변수 선언이나 여러 문장들을 순차적으로 방문한다.
	 * Compound_Statement객체를 생성하여 반환한다.
	 **********************************************/
	@Override
	public Compound_Statement visitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// TODO Auto-generated method stub
		List<Local_Declaration> local_decls = new LinkedList<Local_Declaration>();
		List<Statement> stmts = new LinkedList<Statement>();
		Iterator iterator;
		iterator=ctx.local_decl().iterator();
		while (iterator.hasNext()) {
			local_decls.add((Local_Declaration)visit((MiniCParser.Local_declContext)iterator.next()));
		}
		iterator=ctx.stmt().iterator();
		while (iterator.hasNext()) {
			stmts.add((Statement)visit((MiniCParser.StmtContext)iterator.next()));
		}
		return new Compound_Statement(local_decls, stmts);
	}

	
	/***********************************************************
	 * 지역변수 선언은 3가지(정수, 정수형 배열, 정수 선언 및 할당)로 구분할 수 있다.
	 * 각각을 구분하여 객체를 생성하여 반환한다.
	 ***********************************************************/
	@Override
	public Local_Declaration visitLocal_decl(MiniCParser.Local_declContext ctx) {
		// TODO Auto-generated method stub
		TypeSpecification type = (TypeSpecification)visit(ctx.type_spec());
		TerminalNode lhs = (TerminalNode)ctx.IDENT();
		String comp = ctx.getChild(2).getText();
		if(comp.equals(";")) {
			return new Local_Declaration(type, lhs);
		}
		else {
			TerminalNode rhs = (TerminalNode)ctx.LITERAL();
			if(comp.equals("=")) {
				return new Local_Variable_Declaration_Assign(type, lhs, rhs);
			}
			else {
				return new Local_Variable_Declaration_Array(type, lhs, rhs);
			}
		}
	}

	
	/***************************************************
	 * if문의 구조는 if (expr) stmt else stmt이다.
	 * stmt는 while문과 마찬가지로 하나의 문장 또는 블럭문이 올 수 있다.
	 * else는 생략될 수 있으므로 생성자를 오버로딩하여 객체를 생성한다.
	 * 각각의 자식 노드를 방문한 뒤,
	 * If_Statement 객체를 생성하여 반환한다.
	 ***************************************************/
	@Override
	public If_Statement visitIf_stmt(MiniCParser.If_stmtContext ctx) {
		// TODO Auto-generated method stub
		TerminalNode ifnode = (TerminalNode)ctx.IF();
		Expression expr = (Expression)visit(ctx.expr());
		Statement if_stmt = (Statement)visit(ctx.stmt(0));
		if(ctx.getChildCount() == 5) {
			return new If_Statement(ifnode, expr, if_stmt);
		}
		else {
			TerminalNode elsenode = (TerminalNode)ctx.ELSE();
			Statement else_stmt = (Statement)visit(ctx.stmt(1)) ;
			return new If_Statement(ifnode, expr, if_stmt, elsenode, else_stmt);
		}
	}

	
	/************************************************
	 * 반환문은 리턴값이 없는 반환과, 있는 반환으로 구분할 수 있다.
	 * 반환값이 있는 경우, 자식노드인 expr을 방문한다. 
	 * Return_Statement 객체를 생성하여 반환한다.
	 ************************************************/
	@Override
	public Return_Statement visitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		// TODO Auto-generated method stub
		TerminalNode return_node = (TerminalNode)ctx.RETURN();
		if(ctx.getChildCount() == 2) {
			return new Return_Statement(return_node);
		}
		else {
			Expression expr = (Expression)visit(ctx.expr());
			return new Return_Statement(return_node, expr);
		}
	}

	
	/*******************************************************
	 * MiniC에서 허용하는 Expr은  배열 참조, 배열 참조 할당, 변수에 값 할당,
	 * 판별식, 단항연산, 함수 호출, 괄호 등이 있다.
	 * 더 작게는 터미널 노드로 각 변수명이나 리터럴을 나타낼 때에도 사용한다.
	 * Expr은 자식노드로 Expr이 올 수 있는 재귀적 구문이므로,
	 * 필요한 경우 자식노드를 방문한다.
	 * 이들 각각을 구분하여 객체를 생성하고 반환한다.
	 *******************************************************/
	@Override
	public Expression visitExpr(MiniCParser.ExprContext ctx) {
		// TODO Auto-generated method stub
		Expression expr1 = null, expr2 = null;
		String op = null;
		
		if( ctx.getChildCount() == 1) {		// 1
			TerminalNode t_node = (TerminalNode)ctx.getChild(0);
			return new TerminalExpression(t_node);
		}
		if (isBinaryOperation(ctx)) {
			expr1 = (Expression)visit(ctx.expr(0));
			expr2 = (Expression)visit(ctx.expr(1));
			op = ctx.getChild(1).getText();
			return new BinaryOpNode(expr1, op, expr2);
		}
		else if	(ctx.getChild(0) == ctx.IDENT() ){ 
			TerminalNode t_node =(TerminalNode)ctx.IDENT();
			if( ctx.getChild(1).getText().equals("[")) {
				Expression lhs = (Expression)visit(ctx.expr(0));
				if (ctx.getChildCount() == 4) {
					return new ArefNode(t_node, lhs);
				}
				else {
					Expression rhs = (Expression)visit(ctx.expr(1));
					return new ArefAssignNode(t_node, lhs, rhs);
				}
			}
			else if (ctx.getChild(1).getText().equals("(")) {
				Arguments args = (Arguments)visit(ctx.args());
				return new FuncallNode(t_node, args);
			}
			else {
				Expression expr = (Expression)visit(ctx.expr(0));
				return new AssignNode(t_node, expr);
			}
		}
		else if ( ctx.getChild(0).getText().equals("(")) {
			Expression expr = (Expression)visit(ctx.expr(0));
			return new ParenExpression(expr);
		}
		else {
			op = ctx.getChild(0).getText();
			Expression expr = (Expression)visit(ctx.expr(0));
			return new UnaryOpNode(op, expr);
		}
	}

	
	/**************************************************
	 * 식에서 함수호출이 일어날 때 방문된다.
	 * 자식노드로 Expression이 있으며 변수명이나 리터럴이 올 수 있다.
	 * 차례로 방문하여 반환된 객체들을 리스트에 담고, 
	 * Arguments객체를 생성하여 반환한다.
	 **************************************************/
	@Override
	public Arguments visitArgs(MiniCParser.ArgsContext ctx) {
		// TODO Auto-generated method stub
		List<Expression> exprs = new LinkedList<Expression>();
		Iterator iterator = ctx.expr().iterator();
		while (iterator.hasNext()) {
			exprs.add((Expression)visit((MiniCParser.ExprContext)iterator.next()));
		}
		return new Arguments(exprs);
	}
	
	/* 보조 메서드 */
	
	/**이항연산식을 판별하여 참 거짓을 반환 **/
	boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 3
				&& ctx.getChild(1) != ctx.expr(0) && ctx.getChild(0) != ctx.IDENT();
	}
}
