package Domain.Stmt;

import Domain.ASTVisitor;
import Domain.Expr.Expression;

public class Expression_Statement extends Statement{
	public Expression expr;

	public Expression_Statement(Expression expr) {
		super();
		this.expr = expr;
	}
	
	@Override
	public String toString(){
		return expr.toString() + ";";
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitExpression_Statement(this);
		// TODO Auto-generated method stub
		
	}
}
