package symboltable;

import java.util.HashMap;
import java.util.Stack;


/********************************************************
 **** 		���ڵ��� ����, ����, �˻��� �����ϴ� �ڷᱸ��		 ****
 ********************************************************
 * ���� ���� �ؽ� ������ �ɹ� ���̺��̸� �������� ������ ���ÿ��� �̷��������, 
 * ã�� ���� �̸��� �Է��� �� �ؽ����� ���� ������ �ּҸ� ã�ư���.
 * ���� �̸��� �ɹ��� ���� ���, ���ڵ��� �������� �����Ͽ� �˻��� ���� ������
 * �� ���� ���ڵ��� �ε����� ��ȯ�Ѵ�.
 * levelTable�� ������ ���� ������ �����̴�.
 * ������ ����Ǹ� ���� ���ο��� ������ ���ڵ带 ��� �����ؾ��ϹǷ� 
 * �̸� ���� �ʿ��ϴ�.
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
	 * ���ÿ� ���ڵ带 �����ϰ� �̸���, �ش� �ε����� �ؽ����� key�� value�� �����Ѵ�.
	 * �ؽ��ʿ� key�� ������ ���, �ش� value�� ���ڵ��� �������� �����ϰ�, 
	 * ���ο� ���ڵ��� �ε����� �ؽ����� ���ο� value�� �����Ѵ�.
	 * ���� ���۵� ��, �ش� �ε����� levelTable�� �����Ѵ�.
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
	 * ������ ������ ���ڵ带 �����Ѵ�. ���� ���ڵ忡 �������� ������ ���,
	 * �ؽ����� value�� ���������� �����ϰ�,�������� ���ٸ� ���� �� �ؽ�������
	 * Ű�� ���� ����ش�. �������̺�� ���� ���� ����� ���Ͽ� �������
	 * �������̺��� ������ ���� �����Ѵ�.
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
	 * �ɹ����̺� �ش� �̸��� ���ڵ尡 �����ϴ����� �˻��Ѵ�. ������ ��� 
	 * ���ÿ����� �ش� �ε����� ��ȯ�ϰ�, ���� ��� -1�� ��ȯ�Ѵ�.
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
