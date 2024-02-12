package dataProduct;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class IntDP extends DataProduct {
	public int data;

	public IntDP(int value, String dataName) {
		data = value;
		this.dataName = dataName;
		this.description = new Integer(value).toString();
	}
	
	public IntDP(String value, String dataName) {
		data = new Integer(value);
		this.dataName = dataName;
		this.description = new Integer(value).toString();
	}

//	public IntDP(int value, String dataName, String description) {
//		data = value;
//		this.dataName = dataName;
//		this.description = new Integer(value).toString();
//	}

}
