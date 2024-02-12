package dataProduct;

import org.w3c.dom.Element;

public class XmlDP extends DataProduct {
	public Element data;

	public XmlDP(Element data, String dataName) {
		this.data = data;
		this.dataName = dataName;
		this.description = dataName;
	}

}
