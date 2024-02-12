package translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utility.Utility;
import utility.XMLParser;

import com.ibm.wsdl.OperationImpl;
import com.ibm.wsdl.PortTypeImpl;

public class WSDLAnalyzer {

	public static ArrayList<String> getOperations(String wsdl) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		WSDLFactory sf = WSDLFactory.newInstance();
		WSDLReader s = sf.newWSDLReader();
		Definition wsdlDefinition = s.readWSDL(null, wsdl);

		Object[] ob = wsdlDefinition.getPortTypes().values().toArray();
		PortTypeImpl pti = (PortTypeImpl) ob[0];

		pti.getQName().getNamespaceURI();
		List<OperationImpl> list = pti.getOperations();

		for (OperationImpl currOp : list) {
			String opName = currOp.getName();
			result.add(opName);
		}

		return result;
	}

	public static ArrayList<String> getOperationNames(String wsdl) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		WSDLFactory sf = WSDLFactory.newInstance();
		WSDLReader s = sf.newWSDLReader();
		Definition wsdlDefinition = s.readWSDL(null, wsdl);

		Object[] ob = wsdlDefinition.getPortTypes().values().toArray();
		PortTypeImpl pti = (PortTypeImpl) ob[0];

		pti.getQName().getNamespaceURI();
		List<OperationImpl> list = pti.getOperations();

		for (OperationImpl currOp : list)
			result.add(currOp.getName());
		return result;
	}

	public static Node getOperationInputType(String wsdl, String opName) throws Exception {
		// System.out.println("getOperationInputType call:\n" + wsdl + "\nopName:\n" + opName);
		ArrayList<Node> result = new ArrayList<Node>();
		WSDLFactory sf = WSDLFactory.newInstance();
		WSDLReader s = sf.newWSDLReader();
		Definition wsdlDefinition = s.readWSDL(null, wsdl);

		Object[] ob = wsdlDefinition.getPortTypes().values().toArray();
		PortTypeImpl pti = (PortTypeImpl) ob[0];

		Operation op = pti.getOperation(opName, null, null);

		QName elemNameForInputComplexType = null;

		Message inputMsg = op.getInput().getMessage();
		Map parts = inputMsg.getParts();
		for (Object cPart : parts.values()) {
			Part currP = ((Part) cPart);
			elemNameForInputComplexType = currP.getElementName();
		}

		String wsdlDocStr = Utility.readURLIntoString(wsdlDefinition.getDocumentBaseURI());

		Document wsdlDoc = XMLParser.getDocumentNSAware(wsdlDocStr);

		NodeList elementNodes = wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
		for (int i = 0; i < elementNodes.getLength(); i++) {
			Element currElem = (Element) elementNodes.item(i);
			if (currElem.getAttribute("name").trim().equals(elemNameForInputComplexType.getLocalPart())) {
				// System.out.println("**********");
				// System.out.println(Utility.nodeToString(currElem));
				NodeList childNodes = currElem.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node currN = childNodes.item(j);
					if (currN.getNodeType() == Node.ELEMENT_NODE) {
						// result.add(prettifyNode(currN));
						result.add(Utility.prettifyNode(currElem));

						NodeList elementsInsideCurrN = ((Element) currN).getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");

						for (int k = 0; k < elementsInsideCurrN.getLength(); k++) {
							Element currElement = (Element) elementsInsideCurrN.item(k);
							if (currElement.getAttribute("type") != null) {
								String typeAttribute = currElement.getAttribute("type").trim();
								if (typeIsCustom(wsdl, typeAttribute)) {
									ArrayList<Node> customTypes = findAllCustomTypesDefinedInThisWSDL(wsdl);//, new QName(getNamespace(typeAttribute),
											//getLocalPart(typeAttribute)));
									result.addAll(customTypes);
									// System.out.println("full list:");
									// for(Node currCustType: customTypes)
									// System.out.println(Utility.nodeToString(currCustType));
								}
							}
						}
					}
				}
			}
		}
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();
		Node schemaWrapperNode = doc.createElement("xs:schema");
		((Element) schemaWrapperNode).setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		doc.appendChild(schemaWrapperNode);
		for (Node currN : result) {
			Node copyOfCurrN = doc.importNode(currN, true);
			schemaWrapperNode.appendChild(copyOfCurrN);
		}

		return schemaWrapperNode;
	}

	public static Node getOperationOutputType(String wsdl, String opName) throws Exception {
		ArrayList<Node> result = new ArrayList<Node>();
		WSDLFactory sf = WSDLFactory.newInstance();
		WSDLReader s = sf.newWSDLReader();
		Definition wsdlDefinition = s.readWSDL(null, wsdl);

		Object[] ob = wsdlDefinition.getPortTypes().values().toArray();
		PortTypeImpl pti = (PortTypeImpl) ob[0];

		Operation op = pti.getOperation(opName, null, null);

		QName elemNameForInputComplexType = null;

		Message outputMsg = op.getOutput().getMessage();
		Map parts = outputMsg.getParts();
		for (Object cPart : parts.values()) {
			Part currP = ((Part) cPart);
			elemNameForInputComplexType = currP.getElementName();
		}

		String wsdlDocStr = Utility.readURLIntoString(wsdlDefinition.getDocumentBaseURI());

		Document wsdlDoc = XMLParser.getDocumentNSAware(wsdlDocStr);

		NodeList elementNodes = wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
		for (int i = 0; i < elementNodes.getLength(); i++) {
			Element currElem = (Element) elementNodes.item(i);
			if (currElem.getAttribute("name").trim().equals(elemNameForInputComplexType.getLocalPart())) {
				NodeList childNodes = currElem.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node currN = childNodes.item(j);
					if (currN.getNodeType() == Node.ELEMENT_NODE) {
						// result.add(prettifyNode(currN));
						result.add(Utility.prettifyNode(currElem));

						NodeList elementsInsideCurrN = ((Element) currN).getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");

						for (int k = 0; k < elementsInsideCurrN.getLength(); k++) {
							Element currElement = (Element) elementsInsideCurrN.item(k);
							if (currElement.getAttribute("type") != null) {
								String typeAttribute = currElement.getAttribute("type").trim();
								if (typeIsCustom(wsdl, typeAttribute)) {
									ArrayList<Node> customTypes = findAllCustomTypesDefinedInThisWSDL(wsdl);//, new QName(getNamespace(typeAttribute),
											//getLocalPart(typeAttribute)));
									result.addAll(customTypes);
								}
							}
						}
					}
				}
			}
		}

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();
		Node schemaWrapperNode = doc.createElement("xs:schema");
		((Element) schemaWrapperNode).setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		doc.appendChild(schemaWrapperNode);
		for (Node currN : result) {
			Node copyOfCurrN = doc.importNode(currN, true);
			schemaWrapperNode.appendChild(copyOfCurrN);
		}

		return schemaWrapperNode;
	}

	public static String getNamespace(String fullyQualifName) {
		return fullyQualifName.substring(0, fullyQualifName.indexOf(":"));
	}

	public static String getLocalPart(String fullyQualifName) {
		return fullyQualifName.substring(fullyQualifName.indexOf(":") + 1);
	}

	public static boolean typeIsCustom(String wsdl, String fullyQualifiedTypeName) throws Exception {
		String prefix = fullyQualifiedTypeName.substring(0, fullyQualifiedTypeName.indexOf(":"));
		String wsdlStr = Utility.readURLIntoString(wsdl);
		Document wsdlDoc = XMLParser.getDocumentNSAware(wsdlStr);

		Node wsdlDefinitions = wsdlDoc.getElementsByTagName("wsdl:definitions").item(0);

		NamedNodeMap map = wsdlDefinitions.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			Node currAttr = map.item(i);
			if (currAttr.getLocalName().trim().equals(prefix)) {
				String namespace = ((Element) wsdlDefinitions).getAttribute(currAttr.getNodeName().trim());
				if (namespace.equals("http://www.w3.org/2001/XMLSchema"))
					return false;
			}
		}
		return true;
	}

	public static ArrayList<Node> findAllCustomTypesDefinedInThisWSDL(String wsdl, QName type) throws Exception {
		ArrayList<Node> result = new ArrayList<Node>();
		String prefix = type.getNamespaceURI();
		System.out.println("pppprefix: " + prefix);

		String wsdlStr = Utility.readURLIntoString(wsdl);
		Document wsdlDoc = XMLParser.getDocumentNSAware(wsdlStr);

		Node wsdlDefinitions = wsdlDoc.getElementsByTagName("wsdl:definitions").item(0);

		NamedNodeMap map = wsdlDefinitions.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			Node currAttr = map.item(i);
			if (currAttr.getLocalName().trim().equals(prefix)) {
				String namespace = ((Element) wsdlDefinitions).getAttribute(currAttr.getNodeName().trim());
				NodeList schemas = wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
				Element theRightSchema = null;
				for (int j = 0; j < schemas.getLength(); j++) {
					Element currSchema = (Element) schemas.item(j);
					if (currSchema.getAttribute("targetNamespace").trim().equals(namespace))
						theRightSchema = currSchema;
				}
				NodeList complexTypes = theRightSchema.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "complexType");
				for (int j = 0; j < complexTypes.getLength(); j++)
					result.add(Utility.prettifyNode(complexTypes.item(j)));
			}
		}

		/*
		 * // for (int i = 0; i < map.getLength(); i++) { // Node currAttr = map.item(i); // if
		 * (currAttr.getLocalName().trim().equals(prefix)) { // String namespace = ((Element)
		 * wsdlDefinitions).getAttribute(currAttr.getNodeName().trim()); // NodeList schemas =
		 * wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema"); NodeList complexTypes =
		 * wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "complexType"); for (int j = 0; j <
		 * complexTypes.getLength(); j++) result.add(Utility.prettifyNode(complexTypes.item(j))); // } // }
		 */

		System.out.println("ccccccccccustom types:");
		for (Node currN : result) {
			System.out.println(Utility.nodeToString(currN));
		}

		return result;
	}

	public static ArrayList<Node> findAllCustomTypesDefinedInThisWSDL(String wsdl) throws Exception {
		ArrayList<Node> result = new ArrayList<Node>();
		String wsdlStr = Utility.readURLIntoString(wsdl);
		Document wsdlDoc = XMLParser.getDocumentNSAware(wsdlStr);

		NodeList schemas = wsdlDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
		for (int j = 0; j < schemas.getLength(); j++) {
			Element currSchema = (Element) schemas.item(j);
			NodeList complexTypes = currSchema.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "complexType");
			for (int k = 0; k < complexTypes.getLength(); k++) {
				if (((Element) complexTypes.item(k)).hasAttribute("name"))
					result.add(Utility.prettifyNode(complexTypes.item(k)));
			}
		}
		return result;
	}

	public static String formatXML(Node node) throws Exception {
		Document doc = Utility.buildDocumentFromSingleNode(node);
		String docStr = Utility.nodeToString(doc);
		docStr = docStr.replaceAll(">\\s+", ">");
		return Utility.formatXML(XMLParser.getDocument(docStr));
	}

	public static void main(String[] args) throws Exception {
		String wsdl = null;
		// wsdl = "http://localhost:8080/axis2/services/serviceMultipleOps2?wsdl";
		// wsdl = "http://mrs.cmbi.ru.nl/mrsws/search/wsdl";
		wsdl = "http://dmsg1.cs.wayne.edu:8080/axis2/services/AddressBook?wsdl";
		// wsdl = "http://dmsg1.cs.wayne.edu:8080/axis2/services/Math?wsdl";
		// WSDLFactory sf = WSDLFactory.newInstance();
		// WSDLReader s = sf.newWSDLReader();
		// Definition wsdlDefinition = s.readWSDL(null, wsdl);

		// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%% fixing formatting:");
		// String xmlStr = Utility.readFileAsString("testFiles/JUnit/TSC/schema");
		// System.out.println("unformatted:");
		// System.out.println(xmlStr);
		// System.out.println("ffformatted");
		// Document doc3 = XMLParser.getDocument(xmlStr);
		// String doc3Str = Utility.nodeToString(doc3);
		// System.out.println(doc3Str);
		// System.out.println("1111");
		// doc3Str = doc3Str.replaceAll(">\\s+", ">");
		// System.out.println(doc3Str);
		// System.out.println("2222");
		// System.out.println(Utility.formatXML(XMLParser.getDocument(doc3Str)));

		// System.out.println(Utility.nodeToString(doc2));
		// System.out.println(doc2.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema").getLength());
		// Node schemaNode = doc2.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema").item(0);

		// ArrayList<Node> inputTypesOfAdd = getOperationInputType(wsdl, "saveAddress");
		// for (Node currN : inputTypesOfAdd)
		// System.out.println(Utility.nodeToString(currN));
		//
		// System.out.println("addInt: ");
		// wsdl = "http://dmsg1.cs.wayne.edu:8080/axis2/services/Math?wsdl";
		//
		// ArrayList<Node> inputTypesOfAddInt = getOperationInputType(wsdl, "addInt");
		// for (Node currN : inputTypesOfAddInt)
		// System.out.println(Utility.nodeToString(currN));

		// System.out.println(Utility.nodeToString(inputTypeOfAdd));

		// wsdl = "http://dmsg1.cs.wayne.edu:8080/axis2/services/serviceMultipleOps?wsdl";
		// wsdl = "http://mrs.cmbi.ru.nl/mrsws/search/wsdl";
		// System.out.println(getOperations(wsdl));

	}

}
