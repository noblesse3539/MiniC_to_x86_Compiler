package Domain;
import Domain.Program;
import Domain.Args.Arguments;
import Domain.Decl.Declaration;
import Domain.Decl.Function_Declaration;
import Domain.Decl.Local_Declaration;
import Domain.Decl.Variable_Declaration;
import Domain.Expr.Expression;
import Domain.Param.Parameter;
import Domain.Param.Parameters;
import Domain.Stmt.Compound_Statement;
import Domain.Stmt.Expression_Statement;
import Domain.Stmt.If_Statement;
import Domain.Stmt.Return_Statement;
import Domain.Stmt.Statement;
import Domain.Stmt.While_Statement;
import Domain.Type_spec.TypeSpecification;

public interface ASTVisitor {
	public void visitProgram(Program node);
	public void visitDeclaration(Declaration node);
	public void visitVariable_Declaration(Variable_Declaration node);
	public void visitTypeSpecification(TypeSpecification node);
	public void visitFunction_Declaration(Function_Declaration node);
	public void visitParameters(Parameters node);
	public void visitParameter(Parameter node);
	public void visitStatement(Statement node);
	public void visitExpression_Statement(Expression_Statement node);
	public void visitWhile_Statement(While_Statement node);
	public void visitCompound_Statement(Compound_Statement node);
	public void visitLocal_Declaration(Local_Declaration node);
	public void visitIf_Statement(If_Statement node);
	public void visitReturn_Statement(Return_Statement node);
	public void visitExpression(Expression node);
	public void visitArguments(Arguments node);
}
