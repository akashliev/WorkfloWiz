package dataProduct;

public class BooleanDP extends DataProduct {
	
	public boolean data;
	public BooleanDP(boolean value, String dataName){
		data = value;
		this.dataName = dataName;
		this.description = new Boolean(value).toString();
	}

}
