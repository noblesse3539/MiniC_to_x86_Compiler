package Domain.Type_spec;

import Domain.ASTVisitor;
import Domain.MiniCNode;

public class TypeSpecification extends MiniCNode {
	public final Type type;

	public enum Type {
		VOID, INT
	}

	public TypeSpecification(Type type) {
		this.type = type;
	}
	
	@Override
	public String toString(){
		return type.toString().toLowerCase();
	}

	@Override
	public void accept(ASTVisitor v) {
		// TODO Auto-generated method stub
		
	}
}
