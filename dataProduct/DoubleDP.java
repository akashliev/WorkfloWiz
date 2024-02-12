package dataProduct;

/**
 * Class representing float data products inside engine
 * 
 * @author Andrey Kashlev
 *
 */
public class DoubleDP extends DataProduct {
	public double data;
	public DoubleDP(double value, String dataName){
		data = value;
		this.dataName = dataName;
		this.description = new Double(value).toString();
	}
	
	public DoubleDP(String value, String dataName){
		data = new Double(value);
		this.dataName = dataName;
		this.description = new Double(value).toString();
	}
	
//	public FloatDP(float value, String dataName, String description){
//		data = value;
//		this.dataName = dataName;
//		this.description = new Float(value).toString();
//	}

}
