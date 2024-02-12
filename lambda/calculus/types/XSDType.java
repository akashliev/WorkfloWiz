package lambda.calculus.types;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import translator.XmlSchemaAnalyzer;
import utility.Utility;

public class XSDType extends Type {

	public String elementName = null;
	public ArrayList<XSDType> children = new ArrayList<XSDType>();
	public PrimitiveType child = null;
	public int minOccurs = 1;
	public int maxOccurs = 1;

	public Element xmlSchema = null;

	public XSDType(Element xmlSchema) {
		// System.out.println("ccccccccreating:");
		// System.out.println(xmlSchema.getNodeName().trim());
		// System.out.println(Utility.nodeToString(xmlSchema));

		id = "Xml";
		this.xmlSchema = xmlSchema;

		// If it's a complete xsd document:
		if (xmlSchema.getNodeName().trim().endsWith(":schema")) {
			// it's an entire xsd schema document rather than separate element type definition
			NodeList childrenOfSchemaElement = xmlSchema.getChildNodes();
			for (int i = 0; i < childrenOfSchemaElement.getLength(); i++) {
				Node currChild = childrenOfSchemaElement.item(i);
				if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().endsWith(":element")) {
					elementName = ((Element) currChild).getAttribute("name");
					if (((Element) currChild).hasAttribute("minOccurs"))
						minOccurs = new Integer(((Element) currChild).getAttribute("minOccurs"));
					if (((Element) currChild).hasAttribute("maxOccurs")) {
						if (xmlSchema.getAttribute("maxOccurs").trim().equals("unbounded"))
							maxOccurs = -1;
						else
							maxOccurs = new Integer(((Element) currChild).getAttribute("maxOccurs"));
					}

					NodeList childNodesOfElement = currChild.getChildNodes();

					// if type definition for this element is written in separate element, find that separate element
					// (externalType)
					if (childNodesOfElement.getLength() == 0) {
						String typeName = ((Element) currChild).getAttribute("type").trim();
						if (xsdTypeToVIEWType(typeName) != null) {
							// means this type is just simple xsd type such as int
							child = new PrimitiveType(xsdTypeToVIEWType(typeName));
							return;
						}

						NodeList childNodes = xmlSchema.getChildNodes();
						Node externalType = null;
						for (int j = 0; j < childNodes.getLength(); j++) {
							currChild = childNodes.item(j);
							if (typeName.contains(":"))
								typeName = typeName.substring(typeName.indexOf(":") + 1);
							if (!Utility.nodeToString(currChild).trim().equals("")
									&& currChild.getNodeType() == Node.ELEMENT_NODE
									&& (currChild.getNodeName().trim().endsWith(":complexType") || currChild.getNodeName().trim()
											.endsWith(":simpleType"))
									&& ((Element) currChild).hasAttribute("name")
									&& (((Element) currChild).getAttribute("name").trim().equals(typeName) || ((Element) currChild)
											.getAttribute("name").trim().equals(typeName))) {
								externalType = currChild;
							}
						}
						childNodes = externalType.getChildNodes();
						Node sequence = null;
						for (int j = 0; j < childNodes.getLength(); j++) {
							currChild = childNodes.item(j);
							if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains("sequence"))
								sequence = currChild;
						}
						childNodes = sequence.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) {
							currChild = childNodes.item(j);
							if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains(":element")) {
								children.add(new XSDType(((Element) currChild)));
							}
						}
					} else {
						// if type is defined inside element (i.e. it does not end with />):
						Node complexOrSimpleType = null;
						for (int j = 0; j < childNodesOfElement.getLength(); j++) {
							currChild = childNodesOfElement.item(j);
							if (currChild.getNodeType() == Node.ELEMENT_NODE
									&& (currChild.getNodeName().trim().endsWith(":complexType") || currChild.getNodeName().trim()
											.endsWith(":simpleType"))) {
								complexOrSimpleType = currChild;
							}
						}

						NodeList childNodes = complexOrSimpleType.getChildNodes();
						Node sequence = null;
						for (int j = 0; j < childNodes.getLength(); j++) {
							currChild = childNodes.item(j);
							if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains("sequence"))
								sequence = currChild;
						}
						childNodes = sequence.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) {
							currChild = childNodes.item(j);
							if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains(":element")) {
								children.add(new XSDType(((Element) currChild)));
							}
						}

					}

					// System.out.println("childNodesOfElement.getLength(): " + childNodesOfElement.getLength());

				}
			}
			return;
		}

		elementName = xmlSchema.getAttribute("name").trim();

		NodeList childNodes = xmlSchema.getChildNodes();

		// System.out.println("childNodes.getLength(): " + childNodes.getLength());
		if (childNodes.getLength() == 0) {
			if (xmlSchema.hasAttribute("minOccurs")) {
				minOccurs = new Integer(xmlSchema.getAttribute("minOccurs").trim());
			}
			if (xmlSchema.hasAttribute("maxOccurs")) {
				if (xmlSchema.getAttribute("maxOccurs").trim().equals("unbounded"))
					maxOccurs = -1;
				else
					maxOccurs = new Integer(xmlSchema.getAttribute("maxOccurs").trim());
			}
			String type = xmlSchema.getAttribute("type").trim();
			if (isBuiltinType(type)) {
				child = new PrimitiveType(xsdTypeToVIEWType(type));
			} else {
				// System.out.println("type: " + type);
				if (type.indexOf(":") != -1)
					type = type.substring(type.indexOf(":") + 1, type.length());
				// System.out.println("XSDType.java - type defined externallyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
				// find the externally defined complexType:
				Element complexOrSimpleType = findExternalTypeDefinition(xmlSchema, type);
				childNodes = complexOrSimpleType.getChildNodes();
				Node sequence = null;
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node currChild = childNodes.item(i);
					if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains("sequence"))
						sequence = currChild;
				}
				childNodes = sequence.getChildNodes();

				for (int i = 0; i < childNodes.getLength(); i++) {
					Node currChild = childNodes.item(i);
					if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains(":element")) {
						children.add(new XSDType(((Element) currChild)));
					}
				}
			}

		}

		Node complexType = null;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node currChild = childNodes.item(i);
			if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains("complexType"))
				complexType = currChild;
		}
		if (complexType != null) {
			childNodes = complexType.getChildNodes();
			Node sequence = null;
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node currChild = childNodes.item(i);
				if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains("sequence"))
					sequence = currChild;
			}
			childNodes = sequence.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node currChild = childNodes.item(i);
				if (currChild.getNodeType() == Node.ELEMENT_NODE && currChild.getNodeName().trim().contains(":element")) {
					children.add(new XSDType(((Element) currChild)));
				}
			}

		}

	}

	@Override
	public boolean equals(Type t2) {
		if (!(t2 instanceof XSDType))
			return false;

		if (child != null && ((XSDType) t2).child == null)
			return false;

		if (child != null && child.equals(((XSDType) t2).child)) {
			return true;
		}

		if (child != null && !child.equals(((XSDType) t2).child))
			return false;

		if (elementName.trim().equals(((XSDType) t2).elementName.trim())) {
			if (children.size() == ((XSDType) t2).children.size()) {
				for (int i = 0; i < children.size(); i++) {
					if (!children.get(i).equals(((XSDType) t2).children.get(i)))
						return false;
				}
			} else
				return false;

			return true;
		}
		return false;
	}

	// public String toString() {
	// String element = xmlSchema.getNodeName();
	// if (xmlSchema.getNodeName().trim().equals("xs:element") || xmlSchema.getNodeName().trim().equals("xs:schema"))
	// element = "xs:schema";
	//
	// return element;
	// }

	public static String xsdTypeToVIEWType(String xsdType) {
		if (xsdType.trim().endsWith(":boolean"))
			return "Boolean";
		if (xsdType.trim().endsWith(":byte"))
			return "Byte";
		if (xsdType.trim().endsWith(":short"))
			return "Short";
		if (xsdType.trim().endsWith(":int"))
			return "Int";
		if (xsdType.trim().endsWith(":long"))
			return "Long";
		if (xsdType.trim().endsWith(":float"))
			return "Float";
		if (xsdType.trim().endsWith(":double"))
			return "Double";
		if (xsdType.trim().endsWith(":decimal"))
			return "Decimal";
		if (xsdType.trim().endsWith(":string"))
			return "String";
		if (xsdType.trim().endsWith(":positiveInteger"))
			return "PositiveInteger";
		if (xsdType.trim().endsWith(":nonNegativeInteger"))
			return "NonNegativeInteger";
		if (xsdType.trim().endsWith(":integer"))
			return "Integer";
		if (xsdType.trim().endsWith(":unsignedByte"))
			return "UnsignedByte";
		if (xsdType.trim().endsWith(":unsignedShort"))
			return "UnsignedShort";
		if (xsdType.trim().endsWith(":unsignedInt"))
			return "UnsignedInt";
		if (xsdType.trim().endsWith(":unsignedLong"))
			return "UnsignedLong";
		if (xsdType.trim().endsWith(":negativeInteger"))
			return "NegativeInteger";
		if (xsdType.trim().endsWith(":nonPositiveInteger"))
			return "NonPositiveInteger";
		if (xsdType.trim().endsWith(":dateTime"))
			return "DateTime";

		return null;
	}

	private boolean isBuiltinType(String xsdType) {
		if (xsdType.trim().endsWith(":boolean") || xsdType.trim().endsWith(":byte") || xsdType.trim().endsWith(":short")
				|| xsdType.trim().endsWith(":int") || xsdType.trim().endsWith(":long") || xsdType.trim().endsWith(":float")
				|| xsdType.trim().endsWith(":double") || xsdType.trim().endsWith(":decimal") || xsdType.trim().endsWith(":string")
				|| xsdType.trim().endsWith(":positiveInteger") || xsdType.trim().endsWith(":nonNegativeInteger")
				|| xsdType.trim().endsWith(":integer") || xsdType.trim().endsWith(":unsignedByte") || xsdType.trim().endsWith(":unsignedShort")
				|| xsdType.trim().endsWith(":unsignedInt") || xsdType.trim().endsWith(":unsignedLong")
				|| xsdType.trim().endsWith(":negativeInteger") || xsdType.trim().endsWith(":nonPositiveInteger")
				|| xsdType.trim().endsWith(":dateTime"))
			return true;
		return false;
	}

	private Element findAncestorWithNodeNameSchema(Element descendant) {
		Element currAncestor = descendant;
		while (currAncestor != null) {
			if (currAncestor.getNodeName().trim().endsWith("schema"))
				return currAncestor;
			currAncestor = (Element) currAncestor.getParentNode();
		}
		return null;
	}

	private Element findComplexOrSimpleType(Element schemaEl, String typeName) {
		NodeList children = schemaEl.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& (child.getNodeName().trim().endsWith(":complexType") || child.getNodeName().trim().endsWith(":simpleType"))
					&& ((Element) child).hasAttribute("name") && ((Element) child).getAttribute("name").equals(typeName)) {
				return ((Element) child);
			}
		}
		return null;
	}

	private Element findExternalTypeDefinition(Element descendant, String typeName) {
		Element entireSchemaEl = findAncestorWithNodeNameSchema(descendant);
		return findComplexOrSimpleType(entireSchemaEl, typeName);
	}

	public String toString() {
		String childrenStr = "";
		if (children.size() > 0)
			for (Type childType : children)
				childrenStr += childType.toString() + ", ";

		if (childrenStr.length() > 2)
			childrenStr = childrenStr.substring(0, childrenStr.length() - 2);
		if (child != null)
			return elementName + " : " + child.toString();
		return "{ " + elementName + " : { " + childrenStr + " } }";
	}

}
