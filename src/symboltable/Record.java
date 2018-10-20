package symboltable;

/********************************************
 * �ɹ����̺� ����Ǵ� ���ڵ� ��ü 
 * �̸�, ����, Ÿ��, ����, �ּ�, ũ��, �� ���� ����Ѵ�.
 * referenceValue�� �ɹ����̺������� ����ϴ� ���̴�.
 ********************************************/
public class Record {
	private String name;
	private String kind;
	private int type;
	private int scope;
	private int offset;
	private int size;
	private int value = 0;
	private int referenceValue;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getReferenceValue() {
		return referenceValue;
	}

	public void setReferenceValue(int referenceValue) {
		this.referenceValue = referenceValue;
	}

	// Constructor
	public Record(String name, String kind, int type, int scope, int offset, int size, int value) {
		this.name = name;
		this.kind = kind;
		this.type = type;
		this.scope = scope;
		this.offset = offset;
		this.size = size;
		this.value = value;
		this.referenceValue = -1;
		
	}


}
