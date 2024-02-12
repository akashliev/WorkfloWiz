package dataProduct;

import java.util.ArrayList;

public class ListDP extends DataProduct {
	public ArrayList<DataProduct> data;

	public ListDP(String dataName, ArrayList<DataProduct> list) {
		this.dataName = dataName;
		this.data = new ArrayList<>();
		this.data.addAll(list);
	}
}
