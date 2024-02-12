package lambda.calculus;

import java.util.ArrayList;

import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.XSDType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import translator.SWLAnalyzer;
import utility.Inst2Xsd;
import utility.Utility;
import utility.XMLParser;
import dataProduct.DataProduct;
import dataProduct.DoubleDP;
import dataProduct.FloatDP;
import dataProduct.IntDP;
import dataProduct.LongDP;
import dataProduct.XmlDP;
import dpManagement.Node2DPTranslator;

public class Engine {

	public static LambdaExpression execute(PrimOp op, LambdaExpression arg) throws Exception {
		String outputDPid = op.opName + ".outdp";
		if (op.opName.trim().equals("Increment")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				input = ((DataProductRepres) arg).data;
				if (input instanceof Integer)
					return new DataProductRepres(outputDPid, ((Integer) input) + 1, new PrimitiveType("Int"));
			}
			return null;
		} else if (op.opName.trim().equals("Decrement")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				input = ((DataProductRepres) arg).data;
				if (input instanceof Integer)
					return new DataProductRepres(outputDPid, ((Integer) input) - 1, new PrimitiveType("Int"));
			}
			return null;
		} else if (op.opName.trim().equals("Square")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				input = ((DataProductRepres) arg).data;
				if (input instanceof Integer)
					return new DataProductRepres(outputDPid, ((Integer) input) * ((Integer) input), new PrimitiveType("Int"));
			}
			return null;
		} else if (op.opName.trim().equals("extract")) {
			if (arg instanceof DataProductRepres) {
				if (op.argsAlreadyPassedAsInputs != null) {
					Node extractedNode = (Node) SWLAnalyzer.extractNode(((Element) ((DataProductRepres) arg).data),
							(String) ((DataProductRepres) op.argsAlreadyPassedAsInputs.get(0)).data);
					// System.out.println("extracteddddddddddd Node is \n" + Utility.nodeToString(extractedNode));
					// DataProduct resultDP = new XmlDP((Element) extractedNode, "someDataName");
					DataProductRepres result = new DataProductRepres(extractedNode.getNodeName(), extractedNode, new XSDType(
							Inst2Xsd.getSchemaElement(Utility.nodeToString(extractedNode))));
					return result;
				} else {
					PrimOp extractWithOneArgumentInIt = new PrimOp("extract");
					extractWithOneArgumentInIt.argsAlreadyPassedAsInputs = new ArrayList<Object>();
					extractWithOneArgumentInIt.argsAlreadyPassedAsInputs.add(arg);
					return extractWithOneArgumentInIt;
				}
			}
			return null;
		} else if (op.opName.trim().equals("getSimpleContent")) {
			if (arg instanceof DataProductRepres) {
				Node extractedNode = (Node) SWLAnalyzer.extractText(((Element) ((DataProductRepres) arg).data));
				DataProduct resultDP = Node2DPTranslator.node2dp(Utility.nodeToString(extractedNode));
				DataProductRepres result = new DataProductRepres(resultDP);
				return result;
			}
			return null;
		} else if (op.opName.trim().equals("AddAB")) {
			if (arg instanceof DataProductRepres) {
				if (!(((DataProductRepres) arg).data instanceof Element)) {
					System.out.println("addAB expects dom Element as input");
					return null;
				}
				Element input = (Element) ((DataProductRepres) arg).data;
				Node nodeA = SWLAnalyzer.extractNode(input, "a");
				Node nodeB = SWLAnalyzer.extractNode(input, "b");
				int sum = new Integer(Utility.nodeToString(nodeA)) + (new Integer(Utility.nodeToString(nodeB)));
				String aPlusB = new Integer(sum).toString();
				Element resultData = XMLParser.getDocument(("<sum>" + aPlusB + "</sum>")).getDocumentElement();
				Element outputSchema = Inst2Xsd.getSchemaElement(Utility.nodeToString(resultData));
				XSDType outputType = new XSDType(outputSchema);
				return new DataProductRepres(outputDPid, resultData, outputType);
			}
		} else if (op.opName.trim().startsWith("compose")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				if (op.argsAlreadyPassedAsInputs == null) {
					op.argsAlreadyPassedAsInputs = new ArrayList<Object>();
					op.argsAlreadyPassedAsInputs.add(arg);
					return op;
				} else if (op.argsAlreadyPassedAsInputs.size() < op.numOfArgs) {
					op.argsAlreadyPassedAsInputs.add(arg);
					return op;
				} else {
					String l = (String) ((DataProductRepres) op.argsAlreadyPassedAsInputs.get(0)).data;
					String resultXML = "<" + l + ">";
					for (int i = 1; i < op.argsAlreadyPassedAsInputs.size(); i++) {
						resultXML += Utility.nodeToString((Node) ((DataProductRepres) op.argsAlreadyPassedAsInputs.get(i)).data);
					}
					resultXML += Utility.nodeToString((Node) ((DataProductRepres) arg).data);
					resultXML += "</" + l + ">";
					Document doc = XMLParser.getDocument(resultXML);
					XmlDP xmlDp = new XmlDP(doc.getDocumentElement(), outputDPid);
					DataProductRepres xmlDpr = new DataProductRepres(xmlDp);
					return xmlDpr;
				}

			}
		} else if (op.opName.trim().equals("wrap")) {
			if (arg instanceof DataProductRepres) {
				if (op.argsAlreadyPassedAsInputs == null) {
					op.argsAlreadyPassedAsInputs = new ArrayList<Object>();
					op.argsAlreadyPassedAsInputs.add(arg);

					System.out.println("wrap added arg " + ((DataProductRepres) arg).data);
					return op;
				} else {
					String xmlStr = "<" + ((DataProductRepres) op.argsAlreadyPassedAsInputs.get(0)).data + ">";
					if (((DataProductRepres) arg).data instanceof Node || ((DataProductRepres) arg).data instanceof Element)
						xmlStr += Utility.nodeToString((Node) ((DataProductRepres) arg).data);
					else
						xmlStr += ((DataProductRepres) arg).data;
					xmlStr += "</" + ((DataProductRepres) op.argsAlreadyPassedAsInputs.get(0)).data + ">";
					System.out.println("wrap with " + ((DataProductRepres) op.argsAlreadyPassedAsInputs.get(0)).data + "returning document: \n"
							+ xmlStr);
					XmlDP xmlDp = new XmlDP(XMLParser.getDocument(xmlStr).getDocumentElement(), outputDPid);
					return new DataProductRepres(xmlDp);
				}
			}
		} else if (op.opName.trim().equals("Add")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				input = ((DataProductRepres) arg).data;
				if (input instanceof Integer) {
					if (op.argsAlreadyPassedAsInputs != null)
						return new DataProductRepres(outputDPid, ((Integer) op.argsAlreadyPassedAsInputs.get(0)) + (Integer) input,
								new PrimitiveType("Int"));
					else {
						PrimOp addWithOneArgumentInIt = new PrimOp("Add");
						addWithOneArgumentInIt.argsAlreadyPassedAsInputs = new ArrayList<Object>();
						addWithOneArgumentInIt.argsAlreadyPassedAsInputs.add(input);
						return addWithOneArgumentInIt;
					}
				}
			}
			return null;
		} else if (op.opName.trim().equals("Subtract")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				input = ((DataProductRepres) arg).data;
				if (input instanceof Integer) {
					if (op.argsAlreadyPassedAsInputs != null)
						return new DataProductRepres(outputDPid, ((Integer) op.argsAlreadyPassedAsInputs.get(0)) - (Integer) input,
								new PrimitiveType("Int"));
					else {
						PrimOp addWithOneArgumentInIt = new PrimOp("Subtract");
						addWithOneArgumentInIt.argsAlreadyPassedAsInputs = new ArrayList<Object>();
						addWithOneArgumentInIt.argsAlreadyPassedAsInputs.add(input);
						return addWithOneArgumentInIt;
					}
				}
			}
			return null;
		} else if (op.opName.trim().equals("Multiply")) {
			Object input;
			if (arg instanceof DataProductRepres) {
				input = ((DataProductRepres) arg).data;
				if (input instanceof Integer) {
					if (op.argsAlreadyPassedAsInputs != null)
						return new DataProductRepres(outputDPid, ((Integer) op.argsAlreadyPassedAsInputs.get(0)) * (Integer) input,
								new PrimitiveType("Int"));
					else {
						PrimOp addWithOneArgumentInIt = new PrimOp("Multiply");
						addWithOneArgumentInIt.argsAlreadyPassedAsInputs = new ArrayList<Object>();
						addWithOneArgumentInIt.argsAlreadyPassedAsInputs.add(input);
						return addWithOneArgumentInIt;
					}
				}
			}
			return null;
		} else if (op.opName.trim().equals("sOne")) {
			Node input;
			if (arg instanceof DataProductRepres) {
				input = (Node) ((DataProductRepres) arg).data;
				Node inputElement = ((Element) input).getElementsByTagName("input").item(0);
				String contentOfInput = inputElement.getTextContent();
				int inputNumber = new Integer(contentOfInput);
				String outputXmlStr = "<data><a>" + inputNumber + "</a><b>" + inputNumber * 2 + "</b><c>" + inputNumber * 3 + "</c></data>";
				Document outputDoc = XMLParser.getDocument(outputXmlStr);
				XmlDP outputDp = new XmlDP(outputDoc.getDocumentElement(), "sOneOutput");
				DataProductRepres outputDpr = new DataProductRepres(outputDp);
				return outputDpr;
			}
			return null;
		} else if (op.opName.trim().equals("sTwo")) {
			Node input;
			if (arg instanceof DataProductRepres) {
				input = (Node) ((DataProductRepres) arg).data;
				Node aNode = ((Element) input).getElementsByTagName("a").item(0);
				int a = new Integer(aNode.getTextContent());
				Node bNode = ((Element) input).getElementsByTagName("b").item(0);
				int b = new Integer(bNode.getTextContent().trim());

				IntDP outputDp = new IntDP((a + b), "randomid");
				DataProductRepres outputDpr = new DataProductRepres(outputDp);
				return outputDpr;
			}
			return null;
		} else if (op.opName.trim().equals("Int2Long")) {
			LongDP dp = new LongDP(new Long((new Integer((Integer) ((DataProductRepres) arg).data).toString())), "dataName");
			return new DataProductRepres(dp);

		} else if (op.opName.trim().equals("Float2Double")) {
			DoubleDP dp = new DoubleDP(new Double((new Float((Float) ((DataProductRepres) arg).data).toString())), "dataName");
			return new DataProductRepres(dp);

		} else
			System.out.println("functional.Engine: unknown operation: " + op.opName);
		return null;
	}
}