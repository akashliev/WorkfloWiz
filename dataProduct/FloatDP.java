package dataProduct;

/**
 * Class representing float data products inside engine
 * 
 * @author Andrey Kashlev
 *
 */
public class FloatDP extends DataProduct {
	public float data;
	public FloatDP(float value, String dataName){
		data = value;
		this.dataName = dataName;
		this.description = new Float(value).toString();
	}
	
	public FloatDP(String value, String dataName){
		data = new Float(value);
		this.dataName = dataName;
		this.description = new Float(value).toString();
	}
	
//	public FloatDP(float value, String dataName, String description){
//		data = value;
//		this.dataName = dataName;
//		this.description = new Float(value).toString();
//	}

}
