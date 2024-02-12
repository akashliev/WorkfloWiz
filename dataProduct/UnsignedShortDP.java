package dataProduct;

import java.math.BigInteger;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class UnsignedShortDP extends DataProduct {
	public BigInteger data;

	public UnsignedShortDP(String data, String dataName) {
		this.data = new BigInteger(data);
		this.dataName = dataName;
		this.description = data;
	}

	public UnsignedShortDP(BigInteger data, String dataName) {
		this.data = data;
		this.dataName = dataName;
		this.description = data.toString();
	}

	// public IntegerDP(String value, String dataName, String description){
	// data = new BigInteger(value);
	// this.dataName = dataName;
	// this.description = new Integer(value).toString();
	// }

}
