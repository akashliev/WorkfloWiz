package lambda.calculus.types;

import java.util.ArrayList;

import org.w3c.dom.Element;

import translator.XmlSchemaAnalyzer;

public abstract class Type {

	public String id = null;

	public boolean isValidType() {
		return id != null;
	}

//	public boolean isPrimitive() {
//		return isString() || isDecimal() || isInteger() || isNonPositiveInteger() || isNegativeInteger() || isNonNegativeInteger()
//				|| isUnsignedLong() || isUnsignedInt() || isUnsignedShort() || isUnsignedByte() || isPositiveInteger() || isDouble() || isFloat()
//				|| isLong() || isInt() || isShort() || isByte() || isBoolean();
//	}


//	{
////		if (isArrowType()) {
////			String fromTypeList = "";
////			for (Type currType : types)
////				fromTypeList = fromTypeList + currType + "→";
////			fromTypeList = fromTypeList + "end";
////			if (fromTypeList.indexOf("→end") != -1)
////				fromTypeList = fromTypeList.replace("→end", "");
////			else
////				fromTypeList = fromTypeList.replace("end", "");
////
////			return fromTypeList;
////		} else if (isXml()) {
////			String element = xmlSchema.getNodeName();
////			if(xmlSchema.getNodeName().trim().equals("xs:element") || xmlSchema.getNodeName().trim().equals("xs:schema"))
////				element = "xs:schema";
////			
////			return element;
////		} else
//			
//			return id;
//	}

	public abstract boolean equals(Type t2); 
//	{
		
		// System.out.println("equals call for " + id + " with " + t2);
//		if (!isArrowType() && !isXml() && id.equals(t2.id))
//			return true;
//		else if (isArrowType() && t2.isArrowType()) {
//
//			for (int i = 0; i < types.size(); i++)
//				if (types.get(i) != t2.types.get(i))
//					return false;
//
//			return true;
//		} else if (isXml() && t2.isXml())
//			return XmlSchemaAnalyzer.twoSchemasAreEqual(xmlSchema, t2.xmlSchema);
//		return false;
//	}

//	public void printId() {
//		System.out.println(id);
//	}

	// public boolean allFromTypesEqual(ArrayList<Type> t2) {
	// if (!(from.size() == t2.size()))
	// return false;
	// for (int i = 0; i < from.size(); i++) {
	// if (!from.get(i).equals(t2.get(i)))
	// return false;
	// }
	// return true;
	// }

}