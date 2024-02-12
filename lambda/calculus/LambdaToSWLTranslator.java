package lambda.calculus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataProduct.IntDP;
import lambda.calculus.types.ArrowType;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;
import repository.Repository;
import translator.SWLAnalyzer;
import utility.Utility;
import webbench.WebbenchUtility;
import lambda.calculus.types.Type;

public class LambdaToSWLTranslator {
	private static ArrayList<String> translatedExpressions = new ArrayList<String>();

	public static Gamma getContextFromLambdaExpr(LambdaExpression expr) throws Exception {
		Gamma gamma = new Gamma();
		getContextFromLambdaExpr(expr, gamma);
		return gamma;
	}

	public static void getContextFromLambdaExpr(LambdaExpression expr, Gamma gamma) throws Exception {
		if (expr instanceof PrimOp) {
			String wfName = ((PrimOp) expr).opName;
			Element spec = Repository.getWorkflowSpecification(wfName);
			ArrayList<Type> types = new ArrayList<Type>();
			for (String portID : SWLAnalyzer.getWorkflowInputPorts(spec)) {
				String typeReturnedBySWLAnalyzer = SWLAnalyzer.getPortType(spec, portID);
				if (!typeReturnedBySWLAnalyzer.contains("<"))
					types.add(new PrimitiveType(SWLAnalyzer.getPortType(spec, portID)));
				else
					System.out.println("LambdaToSWLTranslator ERRORRRRRRRRRRR - input type is xml");
			}
			for (String portID : SWLAnalyzer.getWorkflowOutputPorts(spec)) {
				String typeReturnedBySWLAnalyzer = SWLAnalyzer.getPortType(spec, portID);
				if (!typeReturnedBySWLAnalyzer.contains("<"))
					types.add(new PrimitiveType(SWLAnalyzer.getPortType(spec, portID)));
				else
					System.out.println("LambdaToSWLTranslator ERRORRRRRRRRRRR - output type is xml");
			}
			gamma.addBinding(expr, new ArrowType(types));
		} else if (expr instanceof Abstraction) {
			getContextFromLambdaExpr(((Abstraction) expr).expr, gamma);
		} else if (expr instanceof Application) {
			getContextFromLambdaExpr(((Application) expr).f, gamma);
			getContextFromLambdaExpr(((Application) expr).n, gamma);
		}
	}

	public static Document translate(LambdaExpression expr, String name, String outputDPId) throws Exception {
		// 2016/03/30: replace translate with translateNew to support swl6.0:
		//Document doc = translate(expr, name);
		Document doc = translateNew(expr, name);

		// forming outputDP2PortMapping:
		String fromAttribute = findDanglingOutputPort(doc);
		Node outputDp2PortMapping = doc.createElement("outputDP2PortMapping");

		((Element) outputDp2PortMapping).setAttribute("from", fromAttribute);
		((Element) outputDp2PortMapping).setAttribute("to", outputDPId);

		Node dataProductsToPorts = doc.getElementsByTagName("dataProductsToPorts").item(0);
		dataProductsToPorts.appendChild(outputDp2PortMapping);

		return doc;
	}

	private static Document translate(LambdaExpression expr, String name) throws Exception {
		Document doc = null;
		translatedExpressions.clear();
		if (expr instanceof Application) {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = fact.newDocumentBuilder();
			doc = parser.newDocument();

			Node experimentSpec = doc.createElement("experimentSpec");
			doc.appendChild(experimentSpec);

			Node experiment = doc.createElement("experiment");
			experimentSpec.appendChild(experiment);

			((Element) experiment).setAttribute("name", name);

			Node workflowBody = doc.createElement("workflowBody");
			experiment.appendChild(workflowBody);

			Node workflowGraph = doc.createElement("workflowGraph");
			workflowBody.appendChild(workflowGraph);

			Node workflowInstances = doc.createElement("workflowInstances");
			workflowGraph.appendChild(workflowInstances);

			Node dataChannels = doc.createElement("dataChannels");
			workflowGraph.appendChild(dataChannels);

			Element dataProductsToPorts = doc.createElement("dataProductsToPorts");
			workflowBody.appendChild(dataProductsToPorts);

			translate(expr, doc);

		}
		return doc;
	}
	
	private static Document translateNew(LambdaExpression expr, String name) throws Exception {
		Document doc = null;
		translatedExpressions.clear();
		if (expr instanceof Application) {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = fact.newDocumentBuilder();
			doc = parser.newDocument();

			Node experimentSpec = doc.createElement("workflowSpec");
			doc.appendChild(experimentSpec);

			Node experiment = doc.createElement("workflow");
			experimentSpec.appendChild(experiment);

			if(name == null || name.trim().equals(""))
				name = "NotIncrement";
			((Element) experiment).setAttribute("name", name);

			Node workflowBody = doc.createElement("workflowBody");
			((Element) workflowBody).setAttribute("mode", "graph-based");
			experiment.appendChild(workflowBody);
			

			Node workflowGraph = doc.createElement("workflowGraph");
			workflowBody.appendChild(workflowGraph);

			Node workflowInstances = doc.createElement("workflowInstances");
			workflowGraph.appendChild(workflowInstances);

			Node dataChannels = doc.createElement("dataChannels");
			workflowGraph.appendChild(dataChannels);

			Element dataProductsToPorts = doc.createElement("dataProductsToPorts");
			workflowBody.appendChild(dataProductsToPorts);

			translate(expr, doc);

		}
		return doc;
	}

	private static void translate(LambdaExpression expr, Document doc) throws Exception {
		if (translatedExpressions.contains(expr.toString().trim()))
			return;

		if (expr instanceof PrimOp) {
			Node wfInstances = doc.getElementsByTagName("workflowInstances").item(0);

			Node workflowEl = doc.createElement("workflow");
			workflowEl.setTextContent(((PrimOp) expr).opName);

			Node workflowInstance = doc.createElement("workflowInstance");
			((Element) workflowInstance).setAttribute("id", ((PrimOp) expr).instanceId);

			workflowInstance.appendChild(workflowEl);
			wfInstances.appendChild(workflowInstance);
		}

		else if (expr instanceof Application) {
			LambdaExpression f = ((Application) expr).f;
			LambdaExpression n = ((Application) expr).n;

			translate(f, doc);
			translate(n, doc);

			String connectingElF = findConnectingElementId(f);
			String connectingElN = findConnectingElementId(n);

			addConnectionInSWL(connectingElF, connectingElN, doc);
		}
		translatedExpressions.add(expr.toString());
	}

	public static String findConnectingElementId(LambdaExpression expr) {
		// System.out.println("findConnectingEl call for " + expr);
		if (expr instanceof DataProductRepres) {
			return ((DataProductRepres) expr).dataName;
		} else if (expr instanceof PrimOp) {
			return ((PrimOp) expr).instanceId;
		} else if (expr instanceof Abstraction) {
			return findConnectingElementId(((Abstraction) expr).expr);
		} else if (expr instanceof Application) {
			return findConnectingElementId(((Application) expr).f);
		}
		return null;
	}

	public static void printMap(HashMap<LambdaExpression, String> map) {
		for (LambdaExpression expr : map.keySet())
			System.out.println(expr);
	}

	public static void addConnectionInSWL(String componentFId, String componentNId, Document doc) throws Exception {
		// System.out.println("add connection call for " + componentFId + " " + componentNId);

		// assume componentFId is wf id.

		if (isWorkflow(componentNId, doc)) {
			Node dataChannels = doc.getElementsByTagName("dataChannels").item(0);

			String componentNName = SWLAnalyzer.getComponentName(componentNId, doc.getDocumentElement());
			Element componentNSpec = Repository.getWorkflowSpecification(componentNName);
			String componentNOutputPortId = SWLAnalyzer.getWorkflowOutputPorts(componentNSpec).get(0);

			Node dataChannel = doc.createElement("dataChannel");
			((Element) dataChannel).setAttribute("from", componentNId + "." + componentNOutputPortId);
			((Element) dataChannel).setAttribute("to", componentFId + "." + findNextAvailableInputPort(componentFId, doc));
			dataChannels.appendChild(dataChannel);
		} else {
			// componentNId is data product id
			Node dataProductsToPorts = doc.getElementsByTagName("dataProductsToPorts").item(0);

			Node inputDP2PortMapping = doc.createElement("inputDP2PortMapping");
			((Element) inputDP2PortMapping).setAttribute("from", componentNId);
			((Element) inputDP2PortMapping).setAttribute("to", componentFId + "." + findNextAvailableInputPort(componentFId, doc));
			dataProductsToPorts.appendChild(inputDP2PortMapping);

		}

	}

	public static boolean isWorkflow(String componentId, Document doc) {
		NodeList workflowInstances = doc.getElementsByTagName("workflowInstance");
		for (int i = 0; i < workflowInstances.getLength(); i++) {
			Element currentInstance = (Element) workflowInstances.item(i);
			if (currentInstance.getAttribute("id").equals(componentId)) {
				return true;
			}
		}
		return false;
	}

	public static String findNextAvailableInputPort(String componentId, Document doc) throws Exception {
		// System.out.println("looking for next available port for " + componentId);
//		System.out.println(Utility.nodeToString(doc));
		String name = SWLAnalyzer.getComponentName(componentId, doc.getDocumentElement());
		ArrayList<String> inputPortIds = SWLAnalyzer.getWorkflowInputPorts(Repository.getWorkflowSpecification(name));
		NodeList dataChannels = doc.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Element currDataChannel = (Element) dataChannels.item(i);
			String toAttribute = currDataChannel.getAttribute("to");
			String currCompId = toAttribute.substring(0, toAttribute.lastIndexOf("."));
			String currPortId = toAttribute.substring(toAttribute.lastIndexOf(".") + 1);
			if (currCompId.equals(componentId)) {
				inputPortIds.remove(currPortId);
			}
		}
		NodeList inputDP2PortMappings = doc.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element currInputDP2PortMappings = (Element) inputDP2PortMappings.item(i);
			String toAttribute = currInputDP2PortMappings.getAttribute("to");
			String currCompId = toAttribute.substring(0, toAttribute.lastIndexOf("."));
			String currPortId = toAttribute.substring(toAttribute.lastIndexOf(".") + 1);
			if (currCompId.equals(componentId)) {
				inputPortIds.remove(currPortId);
			}
		}
		return inputPortIds.get(0);
	}

	public static String findDanglingOutputPort(Document doc) throws Exception {
		HashSet<String> componentIds = SWLAnalyzer.getAllReferencedComponentIDs(doc.getDocumentElement());

		for (String currComponentId : componentIds) {
			String currComponentName = SWLAnalyzer.getComponentName(currComponentId, doc.getDocumentElement());
			ArrayList<String> outputPortIds = SWLAnalyzer.getWorkflowOutputPorts(Repository.getWorkflowSpecification(currComponentName));

			NodeList dataChannels = doc.getElementsByTagName("dataChannel");
			for (int i = 0; i < dataChannels.getLength(); i++) {
				Element currDataChannel = (Element) dataChannels.item(i);
				String fromAttribute = currDataChannel.getAttribute("from");
				String currCompId = fromAttribute.substring(0, fromAttribute.lastIndexOf("."));
				String currPortId = fromAttribute.substring(fromAttribute.lastIndexOf(".") + 1);
				if (currCompId.equals(currComponentId)) {
					outputPortIds.remove(currPortId);
				}
			}
			if (!outputPortIds.isEmpty())
				return currComponentId + "." + outputPortIds.get(0);

		}

		return null;
	}

	public static LambdaExpression simplifyExprByRemovingIdentityFs(LambdaExpression expr) {
		if (expr instanceof Abstraction)
			((Abstraction) expr).expr = simplifyExprByRemovingIdentityFs(((Abstraction) expr).expr);
		else if (expr instanceof Application) {
			if (((Application) expr).f instanceof Abstraction) {
				Abstraction f = (Abstraction) ((Application) expr).f;
				if (f.name.name.trim().equals(f.expr.toString().trim()))
					return ((Application) expr).n;
				else {
					((Application) expr).f = simplifyExprByRemovingIdentityFs(((Application) expr).f);
					((Application) expr).n = simplifyExprByRemovingIdentityFs(((Application) expr).n);
				}
			} else {
				((Application) expr).f = simplifyExprByRemovingIdentityFs(((Application) expr).f);
				((Application) expr).n = simplifyExprByRemovingIdentityFs(((Application) expr).n);
			}
		}
		return expr;
	}

	/**
	 * @param args
	 * @throws Exception
	 */

	public static boolean listContains(ArrayList<String> list, String str) {
		for (String currStr : list)
			if (currStr.trim().equals(str))
				return true;
		return false;
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();

		PrimOp incr = new PrimOp("Increment");
		PrimOp square = new PrimOp("Square");
		DataProductRepres two = new DataProductRepres(new IntDP(2, "twoInt"));
		DataProductRepres five = new DataProductRepres(new IntDP(5, "fiveInt"));

		LambdaExpression IncrSqTwo = incr.apply(square.apply(two));
		System.out.println("expr: " + IncrSqTwo);
		Gamma gamma = getContextFromLambdaExpr(IncrSqTwo);
		System.out.println("context: " + gamma);
		System.out.println();

		HashMap<LambdaExpression, String> expr2instanceId2 = new HashMap<LambdaExpression, String>();

		PrimOp add1 = new PrimOp("Add");
		String name1 = "add1";
		PrimOp add2 = new PrimOp("Add");
		String name2 = "add2";

		expr2instanceId2.put(add1, name1);
		expr2instanceId2.put(add2, name2);

		System.out.println(expr2instanceId2.get(add2));

		// ArrayList<String> portIds = new ArrayList<String>();
		// portIds.add("ab");
		// portIds.add("ff");
		// portIds.add("dd");
		//
		// System.out.println(portIds);
		// portIds.remove("ff");
		// System.out.println(portIds);
		//
		// System.out.println(portIds.get(0));
		// System.out.println(portIds.get(1));

		System.out.println("Square two:");
		LambdaExpression squareTwo = square.apply(two);
		Document squareTwoSpec = translate(squareTwo, "squareTwo");
		// System.out.println(Utility.domToString(squareTwoSpec));
		System.out.println(Utility.nodeToString(squareTwoSpec));

		System.out.println("\nIncrSqTwo:");
		System.out.println(IncrSqTwo);
		Document IncrSqTwoSpec = translate(IncrSqTwo, "squareTwo");
		System.out.println(Utility.nodeToString(IncrSqTwoSpec));

		System.out.println("*******************************************\n");
		Element spec = Repository.getWorkflowSpecification("AddNestedEx");
		// System.out.println("owner doc\n" + Utility.nodeToString(spec.getOwnerDocument()));
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec.getOwnerDocument());
		System.out.println("expression: \n" + expr);

		 PrimOp increment = new PrimOp("Increment");
		 PrimOp add = new PrimOp("Add");
		
		 Name x0 = new Name("x0");
		 Name x1 = new Name("x1");
		
		
		 LambdaExpression expr2 = increment.apply(x0.lambda(x1.lambda(add.apply(x0).apply(x1))).apply(two).apply(five));
		
		 System.out.println("expr: \n" + expr2);
		 LambdaToSWLTranslator.translate(expr, "AddNestedEx", "outputDP0");

	}

}
