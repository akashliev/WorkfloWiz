package utility;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class hides low-level details of working with org.w3c.dom objects, and obtaining them from String objects.
 * 
 * @author Andrey Kashlev
 * 
 */
public class XMLParser {

	public static Element parseXmlFile(String swl) throws Exception {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		Element docElement = null;

		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource inStream = new org.xml.sax.InputSource();
			// System.out.println("here's the swl: " + swl);
			inStream.setCharacterStream(new java.io.StringReader(swl));

			// parse using builder to get DOM representation of the XML file
			dom = db.parse(inStream);
			docElement = dom.getDocumentElement();

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return docElement;
	}

	public static Document getDocument(String swl) throws Exception {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		Document dom = null;

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource inStream = new org.xml.sax.InputSource();
			inStream.setCharacterStream(new java.io.StringReader(swl));
			dom = db.parse(inStream);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return dom;
	}

	public static Document copyDocument(Document originalDoc) throws Exception {
		String originalDocAsStr = Utility.nodeToString(originalDoc);
		return getDocument(originalDocAsStr);
	}

	public static Document getDocumentNSAware(String docStr) throws Exception {
		// get the factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource inStream = new org.xml.sax.InputSource();
		inStream.setCharacterStream(new java.io.StringReader(docStr));
		Document doc = builder.parse(inStream);
		return doc;
	}

	public static Element getElement(String element) throws Exception {
		String swl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + element;

		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource inStream = new org.xml.sax.InputSource();
			inStream.setCharacterStream(new java.io.StringReader(swl));
			dom = db.parse(inStream);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return dom.getDocumentElement();
	}
}
