package dataProduct;

import java.math.BigInteger;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class NonPositiveIntegerDP extends DataProduct {
	public BigInteger data;

	public NonPositiveIntegerDP(String data, String dataName) {
		this.data = new BigInteger(data);
		this.dataName = dataName;
		this.description = data;
	}

	public NonPositiveIntegerDP(BigInteger data, String dataName) {
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
