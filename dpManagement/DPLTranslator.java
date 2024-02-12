package dpManagement;

import java.math.BigInteger;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import toyDPM.DataProductManager;
import translator.Pair;
import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;
import dataProduct.BooleanDP;
import dataProduct.ByteDP;
import dataProduct.DataProduct;
import dataProduct.DecimalDP;
import dataProduct.DoubleDP;
import dataProduct.FloatDP;
import dataProduct.IntDP;
import dataProduct.IntegerDP;
import dataProduct.ListDP;
import dataProduct.LongDP;
import dataProduct.NegativeIntegerDP;
import dataProduct.NonNegativeIntegerDP;
import dataProduct.NonPositiveIntegerDP;
import dataProduct.PositiveIntegerDP;
import dataProduct.RelationalDP;
import dataProduct.ShortDP;
import dataProduct.StringDP;
import dataProduct.UnsignedByteDP;
import dataProduct.UnsignedIntDP;
import dataProduct.UnsignedLongDP;
import dataProduct.UnsignedShortDP;
import dataProduct.XmlDP;

/**
 * This class translates DPL into executable representation of data products used inside workflow engine. It also does translate
 * in the reverse direction.
 * 
 * @author Andrey Kashlev
 * 
 */
public class DPLTranslator {

	public static String getDPType(Document dpl) {

		String type = ((Element) ((Document) dpl).getElementsByTagName("type").item(0)).getTextContent().trim();
		if (type.equals("ScalarValue")) {
			type = ((Element) ((Document) dpl).getElementsByTagName("scalarType").item(0)).getTextContent().trim();
		}
		if (type.equals("XmlElement"))
			return "xml";

		return type;
	}

	public static Document translateToDPL(DataProduct dp) throws Exception {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document resultDPL = parser.newDocument();
		Node dataProduct = resultDPL.createElement("dataProduct");

		((Element) dataProduct).setAttribute("name", dp.dataName);
		resultDPL.appendChild(dataProduct);

		Node description = resultDPL.createElement("description");
		description.setTextContent(dp.description);
		dataProduct.appendChild(description);

		Node type = resultDPL.createElement("type");
		Node data = resultDPL.createElement("data");

		if (dp instanceof StringDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("String");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((StringDP) dp).data);
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof DecimalDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Decimal");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((DecimalDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof IntegerDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Integer");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((IntegerDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof NonPositiveIntegerDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("NonPositiveInteger");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((NonPositiveIntegerDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof NegativeIntegerDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("NegativeInteger");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((NegativeIntegerDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof NonNegativeIntegerDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("NonNegativeInteger");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((NonNegativeIntegerDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof UnsignedLongDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("UnsignedLong");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((UnsignedLongDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof UnsignedIntDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("UnsignedInt");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((UnsignedIntDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof UnsignedShortDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("UnsignedShort");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((UnsignedShortDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof UnsignedByteDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("UnsignedByte");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((UnsignedByteDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof PositiveIntegerDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("PositiveInteger");
			Node value = resultDPL.createElement("value");
			value.setTextContent(((PositiveIntegerDP) dp).data.toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof DoubleDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Double");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Double(((DoubleDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof FloatDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Float");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Float(((FloatDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof LongDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Long");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Long(((LongDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof IntDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Int");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Integer(((IntDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof ShortDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Short");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Short(((ShortDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof ByteDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Byte");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Byte(((ByteDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof BooleanDP) {
			type.setTextContent("ScalarValue");
			Node scalarValue = resultDPL.createElement("scalarValue");
			Node scalarType = resultDPL.createElement("scalarType");
			scalarType.setTextContent("Boolean");
			Node value = resultDPL.createElement("value");
			value.setTextContent(new Boolean(((BooleanDP) dp).data).toString());
			scalarValue.appendChild(scalarType);
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}
		if (dp instanceof RelationalDP) {
			RelationalDP dp2 = (RelationalDP) dp;
			type.setTextContent("Relation");
			Node relation = resultDPL.createElement("relation");
			Node schema = resultDPL.createElement("schema");

			for (Pair currPair : ((RelationalDP) dp).schema) {
				Node currColumn = resultDPL.createElement("column");

				Node currColumnName = resultDPL.createElement("columnName");
				currColumnName.setTextContent(currPair.left);

				Node currColumnType = resultDPL.createElement("columnType");
				currColumnType.setTextContent(currPair.right);

				currColumn.appendChild(currColumnName);
				currColumn.appendChild(currColumnType);
				schema.appendChild(currColumn);
			}

			Node DBEntry = resultDPL.createElement("DBEntry");
			Node dbName = resultDPL.createElement("dbName");
			dbName.setTextContent(Utility.dbNameForRelationalDPs);
			Node tableName = resultDPL.createElement("tableName");
			tableName.setTextContent(((RelationalDP) dp).tableName);
			DBEntry.appendChild(dbName);
			DBEntry.appendChild(tableName);
			relation.appendChild(schema);
			relation.appendChild(DBEntry);

			data.appendChild(relation);
		}

		if (dp instanceof XmlDP) {
			type.setTextContent("XmlElement");
			Node scalarValue = resultDPL.createElement("xmlElement");
			Node value = resultDPL.createElement("value");
			String xmlElementStr = Utility.nodeToString(((XmlDP) dp).data);
			value.setTextContent("<![CDATA[" + xmlElementStr + "]]>");
			scalarValue.appendChild(value);
			data.appendChild(scalarValue);
		}

		if (dp instanceof ListDP) {
			type.setTextContent("List");
			Node list = resultDPL.createElement("list");
			String textContent = "";
			for (DataProduct currDP : ((ListDP) dp).data)
				textContent += currDP.dataName + ",";
			if (textContent.endsWith(","))
				textContent = textContent.substring(0, textContent.lastIndexOf(","));

			list.setTextContent(textContent);
			data.appendChild(list);

		}

		dataProduct.appendChild(type);
		dataProduct.appendChild(data);
		return resultDPL;
	}

	public static DataProduct translateDPLtoDP(Document dpl) throws Exception {
		DataProduct dp = null;
		NodeList dataProducts = dpl.getElementsByTagName("dataProduct");
		String dataName = ((Element) dpl.getElementsByTagName("dataProduct").item(0)).getAttribute("name").trim();
		String description = ((Element) dpl.getElementsByTagName("description").item(0)).getTextContent().trim();
		String type = ((Element) dpl.getElementsByTagName("type").item(0)).getTextContent().trim();

		if (type.trim().equals("ScalarValue")) {
			String scalarType = ((Element) dpl.getElementsByTagName("scalarType").item(0)).getTextContent().trim();
			if (scalarType.equals("String")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				String data = new String(valueStr);
				dp = new StringDP(data, dataName);
			} else if (scalarType.equals("Decimal")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				String data = new String(valueStr);
				dp = new DecimalDP(data, dataName);
			} else if (scalarType.equals("Integer")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				// Integer data = new Integer(valueStr);
				dp = new IntegerDP(valueStr, dataName);
				dp.description = description;
			} else if (scalarType.equals("NonPositiveInteger")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new NonPositiveIntegerDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("NegativeInteger")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new NegativeIntegerDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("NonNegativeInteger")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new NonNegativeIntegerDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("UnsignedLong")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new UnsignedLongDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("UnsignedInt")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new UnsignedIntDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("UnsignedShort")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new UnsignedShortDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("UnsignedByte")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new UnsignedByteDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("PositiveInteger")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				BigInteger data = new BigInteger(valueStr);
				dp = new PositiveIntegerDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Double")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				double data = new Double(valueStr);
				dp = new DoubleDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Float")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				Float data = new Float(valueStr);
				dp = new FloatDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Long")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				long data = new Long(valueStr);
				dp = new LongDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Int")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				Integer data = new Integer(valueStr);
				dp = new IntDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Short")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				short data = new Short(valueStr);
				dp = new ShortDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Byte")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				byte data = new Byte(valueStr);
				dp = new ByteDP(data, dataName);
				dp.description = description;
			} else if (scalarType.equals("Boolean")) {
				String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
				Boolean data = new Boolean(valueStr);
				dp = new BooleanDP(data, dataName);
			}
		}

		if (type.trim().equals("Relation")) {
			ArrayList<Pair> schema = new ArrayList<Pair>();
			NodeList columns = dpl.getElementsByTagName("column");

			for (int i = 0; i < columns.getLength(); i++) {

				String columnName = ((Element) ((Element) columns.item(i)).getElementsByTagName("columnName").item(0)).getTextContent().trim();
				String columnType = ((Element) ((Element) columns.item(i)).getElementsByTagName("columnType").item(0)).getTextContent().trim();
				schema.add(new Pair(columnName, columnType));
			}
			String tableName = dpl.getElementsByTagName("tableName").item(0).getTextContent().trim();
			dp = new RelationalDP(Utility.dbNameForRelationalDPs, dataName, tableName, schema);
		}

		if (type.trim().equals("XmlElement")) {
			String valueStr = ((Element) dpl.getElementsByTagName("value").item(0)).getTextContent().trim();
			Document xmlValue = XMLParser.getDocument(valueStr);
			dp = new XmlDP(xmlValue.getDocumentElement(), dataName);

		}

		return dp;
	}

	public static void main(String[] args) throws Exception {
		// Document four = translateToDPL(new IntegerDP(4, "four"));
		//
		// System.out.println("result of translation:");
		// System.out.println(Utility.nodeToString(four));
		WebbenchUtility.initializeWebbenchConfig();
		String dplStr = DataProductManager.getDataProductDPL("addInputXml");
		Document dpl = XMLParser.getDocument(dplStr);
		translateDPLtoDP(dpl);
	}

}
