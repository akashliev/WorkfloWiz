package translator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import repository.Repository;
import utility.LoggingLevels;
import utility.Utility;
import webbench.WebbenchUtility;

public class XmlSchemaAnalyzer {
	
//	public static boolean twoSchemasAreEqual(Element schemaA, Element schemaB){
//		System.out.println("A:");
//		System.out.println(Utility.nodeToString(schemaA));
//		System.out.println("B:");
//		System.out.println(Utility.nodeToString(schemaB));
//		NodeList nodesA = schemaA.getChildNodes();
//		NodeList nodesB = schemaB.getChildNodes();
//		System.out.println("1");
//		if(nodesA.getLength() != nodesB.getLength())
//			return false;
//		System.out.println("2");
//		for(int i=0; i<nodesA.getLength(); i++){
//			Node currNodeFromA = nodesA.item(i);
//			Node currNodeFromB = nodesB.item(i);
//			if(!twoNodesAreEqual(currNodeFromA, currNodeFromB))
//				return false;
//		}
//		System.out.println("3");
//		return true;
//	}
	
	public static boolean twoNodesAreEqual(Node a, Node b){
		if(a.getNodeType() != b.getNodeType())
			return false;
//		System.out.println("comparing nodes of types " + a.getNodeType() + " and " + b.getNodeType());
//		System.out.println("a:");
//		System.out.println(Utility.nodeToString(a).trim());
//		System.out.println("b:");
//		System.out.println(Utility.nodeToString(b).trim());
		if(Utility.nodeToString(a).trim().equals(Utility.nodeToString(b).trim())){
			return true;
		}
		
		return false;
	}
	
	public static String getOuterElementName(Element el){
		return null;
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;
		Element spec = Repository.getWorkflowSpecification("testXml");
		System.out.println(Utility.nodeToString(spec));
		System.out.println(spec.getNodeName());
	}

}
