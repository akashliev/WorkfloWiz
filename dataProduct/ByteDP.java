package dataProduct;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class ByteDP extends DataProduct {
	public byte data;

	public ByteDP(byte value, String dataName) {
		data = value;
		this.dataName = dataName;
		this.description = new Byte(value).toString();
	}
	
	public ByteDP(String value, String dataName) {
		data = new Byte(value);
		this.dataName = dataName;
		this.description = new Byte(value).toString();
	}

	// public IntDP(int value, String dataName, String description) {
	// data = value;
	// this.dataName = dataName;
	// this.description = new Integer(value).toString();
	// }

}
