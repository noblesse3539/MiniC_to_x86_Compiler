# MiniC_to_x86_Compiler

간단한 C언어로 이루어진 코드를 어셈블리어 x86으로 번역하는 컴파일러입니다.

Antlr-4.7 버전입니다.

MiniC 문법은 MiniC.g4를 통해 확인 가능합니다.

test에 원시코드가 있습니다.

src/TestMiniC.java에 main 메서드가 있습니다.

src/Domain/AssembleyGenVisitor.java

src/MiniCAstVisitor.java

src/symboltable/SymbolTable.java 
이 세가지 코드가 핵심이고 주석을 달았습니다.

감사합니다.

(콘솔창 출력결과를 result.txt에 옮겼습니다.)
