package dataProduct;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class LongDP extends DataProduct {
	public long data;

	public LongDP(long value, String dataName) {
		data = value;
		this.dataName = dataName;
		this.description = new Long(value).toString();
	}
	
	public LongDP(String value, String dataName) {
		data = new Long(value);
		this.dataName = dataName;
		this.description = new Long(value).toString();
	}

//	public IntDP(int value, String dataName, String description) {
//		data = value;
//		this.dataName = dataName;
//		this.description = new Integer(value).toString();
//	}

}
