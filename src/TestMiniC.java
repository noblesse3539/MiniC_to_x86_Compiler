
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

//import Domain.Program;
import Domain.AssembleyGenVisitor;
public class TestMiniC {
	public static void main(String[] args) throws Exception {
		/*입력한 문장을 주어진 문법(MiniC)에 맞게 어휘분석을 하고 생성된 토큰으로 파싱트리를 자동으로 생성해준다.*/
		MiniCLexer lexer = new MiniCLexer( new ANTLRFileStream("test"));
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		MiniCParser parser = new MiniCParser( tokens );
		ParseTree tree = parser.program();
	
		/*AST를 생성하고 반환된 객체 Program의 accept 메서드를 호출한다.*/
		MiniCAstVisitor visitor = new MiniCAstVisitor();
		visitor.visit(tree).accept(new AssembleyGenVisitor());
	}
}
