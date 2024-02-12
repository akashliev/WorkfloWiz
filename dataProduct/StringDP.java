package dataProduct;

public class StringDP extends DataProduct {
	
	public String data;
	public StringDP(String value, String dataName){
		data = value;
		this.dataName = dataName;
		this.description = value;
	}

}
