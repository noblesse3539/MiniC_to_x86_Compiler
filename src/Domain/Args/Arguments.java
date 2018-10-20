package Domain.Args;

import java.util.List;

import Domain.ASTVisitor;
import Domain.MiniCNode;
import Domain.Expr.Expression;

public class Arguments extends MiniCNode{
	List<Expression> exprs;
	
	public Arguments(){}
	
	public Arguments(List<Expression> exprs){
		this.exprs = exprs;
	}

	@Override
	public String toString(){
		return exprs == null? "" 
				:exprs.stream().map(t -> ", " + t.toString())
							   .skip(1)
							   .reduce(exprs.get(0).toString(), (acc, param) -> acc + param);
	}

	@Override
	public void accept(ASTVisitor v) {
		// TODO Auto-generated method stub
		
	}
}
