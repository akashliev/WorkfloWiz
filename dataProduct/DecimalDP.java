package dataProduct;

import java.math.BigDecimal;

/**
 * Class representing integer data products inside engine
 * 
 * @author Andrey Kashlev
 * 
 */

public class DecimalDP extends DataProduct {
	public BigDecimal data;

	public DecimalDP(String data, String dataName) {
		this.data = new BigDecimal(data);
		this.dataName = dataName;
		this.description = data;
	}

	public DecimalDP(BigDecimal data, String dataName) {
		this.data = data;
		this.dataName = dataName;
		this.description = data.toString();
	}

	// public DecimalDP(String value, String dataName, String description){
	// data = new BigDecimal(value);
	// this.dataName = dataName;
	// this.description = value;
	// }
	//
	// public DecimalDP(BigDecimal value, String dataName, String description){
	// data = value;
	// this.dataName = dataName;
	// this.description = value.toString();
	// }

}
