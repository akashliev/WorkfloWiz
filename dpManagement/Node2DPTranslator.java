package dpManagement;

import java.util.Calendar;

import lambda.calculus.types.XSDType;

import org.w3c.dom.Element;

import utility.Inst2Xsd;
import utility.Utility;
import dataProduct.BooleanDP;
import dataProduct.ByteDP;
import dataProduct.DataProduct;
import dataProduct.DecimalDP;
import dataProduct.DoubleDP;
import dataProduct.FloatDP;
import dataProduct.IntDP;
import dataProduct.IntegerDP;
import dataProduct.LongDP;
import dataProduct.NegativeIntegerDP;
import dataProduct.NonNegativeIntegerDP;
import dataProduct.NonPositiveIntegerDP;
import dataProduct.PositiveIntegerDP;
import dataProduct.ShortDP;
import dataProduct.StringDP;
import dataProduct.UnsignedByteDP;
import dataProduct.UnsignedIntDP;
import dataProduct.UnsignedLongDP;
import dataProduct.UnsignedShortDP;

public class Node2DPTranslator {

	public static DataProduct node2dp(String nodeAsString) throws Exception {
		DataProduct  result = null;
		// first, create unique dp id:
		Calendar now = Calendar.getInstance();
		String milliseconds = new Long(now.getTimeInMillis()).toString();
		
		if (nodeAsString.indexOf("<") == -1) {
			String tmpNodeStr = "<tmp>" + nodeAsString + "</tmp>";
			Element schema = Inst2Xsd.getSchemaElement(tmpNodeStr);
			String schemaStr = Utility.nodeToString(schema);
			String type = schemaStr.substring(schemaStr.indexOf("type=\"xs:") + 8, schemaStr.indexOf("\"/>"));
			type = XSDType.xsdTypeToVIEWType(type);
			if (type.trim().equals("String")) 
				result = new StringDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("Decimal")) 
				result = new DecimalDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("Double")) 
				result = new DoubleDP(new Double(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Float")) 
				result = new FloatDP(new Float(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Long")) 
				result = new LongDP(new Long(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Int")) 
				result = new IntDP(new Integer(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Short")) 
				result = new ShortDP(new Short(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Byte")) 
				result = new ByteDP(new Byte(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Boolean")) 
				result = new BooleanDP(new Boolean(nodeAsString), "nodeAsString_" + milliseconds);
			if (type.trim().equals("Integer")) 
				result = new IntegerDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("NonNegativeInteger")) 
				result = new NonNegativeIntegerDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("PositiveInteger")) 
				result = new PositiveIntegerDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("UnsignedLong")) 
				result = new UnsignedLongDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("UnsignedInt")) 
				result = new UnsignedIntDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("UnsignedShort")) 
				result = new UnsignedShortDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("UnsignedByte")) 
				result = new UnsignedByteDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("NonPositiveInteger")) 
				result = new NonPositiveIntegerDP(nodeAsString, "nodeAsString_" + milliseconds);
			if (type.trim().equals("NegatigveInteger")) 
				result = new NegativeIntegerDP(nodeAsString, "nodeAsString_" + milliseconds);
			return result;
		}
		return null;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String xmlStr = "33333";
		DataProduct dp = node2dp(xmlStr);
		System.out.println(dp);
		// Element schema = Inst2Xsd.getSchemaElement(xmlStr);
		// System.out.println(Utility.nodeToString(schema));
		// DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		// long l = 10000000L;
		// System.out.println("Long value: " + l);
		// Calendar c = new GregorianCalendar();
		// System.out.println("MILLISECOND: " + c.get(Calendar.MILLISECOND));
		// System.out.println("MILLISECOND: " + c.get(Calendar.MILLISECOND));
		Calendar now = Calendar.getInstance();

		/*
		 * To get time in milliseconds, use long getTimeInMillis() method of Java Calendar class.
		 * 
		 * It returns millseconds from Jan 1, 1970.
		 */
		System.out.println("Current milliseconds since Jan 1, 1970 are :" + now.getTimeInMillis());
	}

}
