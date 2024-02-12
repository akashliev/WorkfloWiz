package dataProduct;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class ShortDP extends DataProduct {
	public short data;

	public ShortDP(short value, String dataName) {
		data = value;
		this.dataName = dataName;
		this.description = new Short(value).toString();
	}
	
	public ShortDP(String value, String dataName) {
		data = new Short(value);
		this.dataName = dataName;
		this.description = new Short(value).toString();
	}

//	public IntDP(int value, String dataName, String description) {
//		data = value;
//		this.dataName = dataName;
//		this.description = new Integer(value).toString();
//	}

}
