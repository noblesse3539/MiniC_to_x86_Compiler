
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
 **** C������ AST(Abstract Syntax Tree)�� �����ϴ� ���α׷�  ****
 *******************************************************
 *����� �������� �ڵ带 Ʈ�����·� ����� Program�̶�� ��ü�� ��ȯ�Ѵ�.
 *Program�� �ν��Ͻ��� Decls�� ���� ������ Decl�� ����ƮŸ���̴�. 
 *Decl�� �ν��Ͻ��δ� �Լ������ �������� ������ ������, �Լ������� �ν��Ͻ�
 *�� Ÿ�԰� �Ķ����, Compound_Stmt���� �ִ�.
 *�̿� ���� ������� ��ü���� �����ϰ� �����Ͽ� �ϳ��� ����Ʈ���� �����Ѵ�.
 *******************************************************/
public class MiniCAstVisitor extends MiniCBaseVisitor<MiniCNode> {

	
	/************************************
	 * Program�� child�� decl�� ���ʷ� �湮�ϰ�,
	 * Program ��ü�� ����� ��ȯ�Ѵ�.
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
	 * �������������� 3����(�迭, ����, ���� ���� �� �ʱ�ȭ)�� �����ϰ�,
	 * �ش��ϴ� ��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	 * �Լ��� ������ Ÿ���� �ǹ��ϸ� TypeSpecification��ü�� �����Ͽ� ��ȯ�Ѵ�.
	 * int ���� void ������ �Ǻ��Ͽ� ��������� �ʱ�ȭ�Ѵ�.
	 * ********************************************************/
	@Override
	public TypeSpecification visitType_spec(MiniCParser.Type_specContext ctx) {
		// TODO Auto-generated method stub
		String s = ctx.getChild(0).getText().toUpperCase();
		return new TypeSpecification(TypeSpecification.Type.valueOf(s));
	}

	
	/**************************************************
	 * Function_Declaration ��ü�� �����Ͽ� ��ȯ�Ѵ�.
	 * �ڽ� ����� Parameters�� Compound_Statement�� �湮�Ѵ�.
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
	 * �Լ� ����� �Ķ���ͺκ��� ����Ѵ�. �ʿ��� ���� �ڽĳ���� Parameter�� �湮�Ѵ�.
	 * C���� �Ķ������ ���� main() main(void) main(int,...) ���� �� �� �����Ƿ�,
	 * ������ �����Ͽ� Parameters��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	 * Parameter ��ü�� �����Ͽ� ��ȯ�Ѵ�.
	 * �������� �������迭�� ������ �� �ִ�.
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
	 * �������� �ؼ��ǳ� ������� �����ϴ°��� �� �����ϱ� �����ϴ�.
	 * Stmt���� {} ���µ� �� �� �ְ�, �ϳ��� ��������� �� �� �ִ�.
	 ************************************************/
	@Override
	public Statement visitStmt(MiniCParser.StmtContext ctx) {
		// TODO Auto-generated method stub
		return (Statement)visit(ctx.getChild(0));
	}

	
	/*****************************************
	 * �ڽĳ���� Expression�� �湮�ϰ�,
	 * Expression_Statement��ü�� �����Ͽ� ��ȯ�Ѵ�.
	 *****************************************/
	@Override
	public Expression_Statement visitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		// TODO Auto-generated method stub
		Expression expr = (Expression)visit(ctx.expr());
		return new Expression_Statement(expr);
	}

	
	/********************************************************
	 * while���� ������ while (expr) stmt �̴�. 
	 * stmt���� �ܼ��� �� ������ �� �� �ְ�, {} ������ �� ���� �ִ�.
	 * ������ �ڽ� ��带 �湮�� �� While_Statement��ü�� �����Ͽ� ��ȯ�Ѵ�. 
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
	 * {} ������ ���� ���̴�. 
	 * �Լ� �����̳� while, if stmt ���� �ڽĳ��� ȣ��ȴ�.
	 * �������� �����̳� ���� ������� ���������� �湮�Ѵ�.
	 * Compound_Statement��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	 * �������� ������ 3����(����, ������ �迭, ���� ���� �� �Ҵ�)�� ������ �� �ִ�.
	 * ������ �����Ͽ� ��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	 * if���� ������ if (expr) stmt else stmt�̴�.
	 * stmt�� while���� ���������� �ϳ��� ���� �Ǵ� ������ �� �� �ִ�.
	 * else�� ������ �� �����Ƿ� �����ڸ� �����ε��Ͽ� ��ü�� �����Ѵ�.
	 * ������ �ڽ� ��带 �湮�� ��,
	 * If_Statement ��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	 * ��ȯ���� ���ϰ��� ���� ��ȯ��, �ִ� ��ȯ���� ������ �� �ִ�.
	 * ��ȯ���� �ִ� ���, �ڽĳ���� expr�� �湮�Ѵ�. 
	 * Return_Statement ��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	 * MiniC���� ����ϴ� Expr��  �迭 ����, �迭 ���� �Ҵ�, ������ �� �Ҵ�,
	 * �Ǻ���, ���׿���, �Լ� ȣ��, ��ȣ ���� �ִ�.
	 * �� �۰Դ� �͹̳� ���� �� �������̳� ���ͷ��� ��Ÿ�� ������ ����Ѵ�.
	 * Expr�� �ڽĳ��� Expr�� �� �� �ִ� ����� �����̹Ƿ�,
	 * �ʿ��� ��� �ڽĳ�带 �湮�Ѵ�.
	 * �̵� ������ �����Ͽ� ��ü�� �����ϰ� ��ȯ�Ѵ�.
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
	 * �Ŀ��� �Լ�ȣ���� �Ͼ �� �湮�ȴ�.
	 * �ڽĳ��� Expression�� ������ �������̳� ���ͷ��� �� �� �ִ�.
	 * ���ʷ� �湮�Ͽ� ��ȯ�� ��ü���� ����Ʈ�� ���, 
	 * Arguments��ü�� �����Ͽ� ��ȯ�Ѵ�.
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
	
	/* ���� �޼��� */
	
	/**���׿������ �Ǻ��Ͽ� �� ������ ��ȯ **/
	boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 3
				&& ctx.getChild(1) != ctx.expr(0) && ctx.getChild(0) != ctx.IDENT();
	}
}
