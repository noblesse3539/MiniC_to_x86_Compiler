package symboltable;

import java.util.HashMap;
import java.util.Stack;


/********************************************************
 **** 		레코드의 삽입, 삭제, 검색을 수행하는 자료구조		 ****
 ********************************************************
 * 스택 구현 해시 구조의 심벌 테이블이며 실질적인 저장은 스택에서 이루어지지만, 
 * 찾기 위해 이름을 입력할 땐 해쉬맵을 통해 빠르게 주소를 찾아간다.
 * 같은 이름의 심벌이 들어올 경우, 레코드의 참조값을 설정하여 검색시 가장 마지막
 * 에 들어온 레코드의 인덱스를 반환한다.
 * levelTable의 역할은 블럭문 시작의 구분이다.
 * 블럭문이 종료되면 블럭문 내부에서 생성된 레코드를 모두 삭제해야하므로 
 * 이를 위해 필요하다.
 ********************************************************/
public class SymbolTable{
	public static final int BLOCKSTART = 1, INBLOCK = 0,
							TYPE_INT = 1, TYPE_VOID = 0;
							
	public HashMap<String, Integer> hashBucket;
	public Stack<Record> symbolTable;
	public Stack<Integer> levelTable;

	/*Constructor*/
	public SymbolTable() {
		this.hashBucket = new HashMap<String, Integer>();
		this.symbolTable = new Stack<Record>();
		this.levelTable = new Stack<Integer>();
	}
	
	
	/*************************************************************
	 * 스택에 레코드를 저장하고 이름과, 해당 인덱스를 해쉬맵의 key와 value로 설정한다.
	 * 해쉬맵에 key가 존재할 경우, 해당 value를 레코드의 참조값에 저장하고, 
	 * 새로운 레코드의 인덱스를 해쉬맵의 새로운 value로 설정한다.
	 * 블럭이 시작될 때, 해당 인덱스를 levelTable에 삽입한다.
	 *************************************************************/
	public boolean insert(String key, Record record) {
		if(this.hashBucket.containsKey(key)) {
			int newRValue = this.hashBucket.get(key);
		record.setReferenceValue(newRValue);
		this.hashBucket.put(key, this.symbolTable.size());
		this.symbolTable.push(record);
		}
		else {
			this.hashBucket.put(key, this.symbolTable.size());
			this.symbolTable.push(record);
		}
		if (record.getScope() == BLOCKSTART) {
			this.levelTable.push(this.symbolTable.size());
		}
		return true;
	}
	
	/******************************************************
	 * 스택의 마지막 레코드를 삭제한다. 만약 레코드에 참조값이 존재할 경우,
	 * 해쉬맵의 value를 참조값으로 설정하고,참조값이 없다면 꺼낼 때 해쉬버켓의
	 * 키와 값을 비워준다. 레벨테이블과 현재 스택 사이즈를 비교하여 같을경우
	 * 레벨테이블의 마지막 값을 삭제한다.
	 ******************************************************/
	public Record delete() {
		if(this.symbolTable.isEmpty()) {
			return null;
		} else {
			Record retSymbolTable = this.symbolTable.peek();
			int link = retSymbolTable.getReferenceValue();
			if( link >= 0) {
				this.hashBucket.put(retSymbolTable.getName(), (int)link);
			}
			else {
				this.hashBucket.remove(retSymbolTable.getName());
			}
			int tableSize = this.symbolTable.size();
			if (this.levelTable.size() > 0) {
				if(tableSize == this.levelTable.peek()) {
					this.levelTable.pop();
				}
			}
			this.symbolTable.pop();
			return retSymbolTable;
		}
	}
	
	/******************************************************
	 * 심벌테이블에 해당 이름의 레코드가 존재하는지를 검색한다. 존재할 경우 
	 * 스택에서의 해당 인덱스를 반환하고, 없을 경우 -1을 반환한다.
	 ******************************************************/
	public int lookUp(String x) {
		if (this.hashBucket.containsKey(x)) {
			int y = this.hashBucket.get(x);
			Record f = this.symbolTable.elementAt(y);
			while ( !x.equals(f.getName()) ) {
				y = f.getReferenceValue();
				f = this.symbolTable.elementAt(y);
			}
			return y;
		}
		else {
			return -1;
		}
	}
}
