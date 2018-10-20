
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

//import Domain.Program;
import Domain.AssembleyGenVisitor;
public class TestMiniC {
	public static void main(String[] args) throws Exception {
		/*�Է��� ������ �־��� ����(MiniC)�� �°� ���ֺм��� �ϰ� ������ ��ū���� �Ľ�Ʈ���� �ڵ����� �������ش�.*/
		MiniCLexer lexer = new MiniCLexer( new ANTLRFileStream("test"));
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		MiniCParser parser = new MiniCParser( tokens );
		ParseTree tree = parser.program();
	
		/*AST�� �����ϰ� ��ȯ�� ��ü Program�� accept �޼��带 ȣ���Ѵ�.*/
		MiniCAstVisitor visitor = new MiniCAstVisitor();
		visitor.visit(tree).accept(new AssembleyGenVisitor());
	}
}
