package translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import repository.Repository;
import toyDPM.DataProductManager;
import utility.LoggingLevels;
import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;
import dataProduct.DataProduct;
import dataProduct.ListDP;

/**
 * This is a crucial class for the entire workflow engine. It provides a set of static methods to analyze SWL specification of a
 * workflow. Most other classes inside workflow engine rely on these methods.
 * 
 * @author Andrey Kashlev
 * 
 */
public class SWLAnalyzer {
	// this class provides methods to work with SWL, e.g. extract name of the workflow etc.
	// it uses DOM API.

	ArrayList<String> builtInWorkflows = new ArrayList<String>();
	HashMap<String, Workflow> workflowInstances = new HashMap<String, Workflow>();

	public SWLAnalyzer() throws Exception {
		// builtInWorkflows.addAll(UtilityGetBuiltInWorkflows.getBuiltInWorkflowNames());
	}

	public static String getWorkflowName(Element el) {
		if (el.hasAttribute("name"))
			return el.getAttribute("name");
		else {
			NodeList workflowElements = el.getElementsByTagName("workflow");
			for (int i = 0; i < workflowElements.getLength(); i++) {
				if (((Element) workflowElements.item(i)).hasAttribute("name"))
					return ((Element) workflowElements.item(i)).getAttribute("name");
			}
		}
		return null;
	}

	public static String getWorkflowOrExperimentName(Element el) {
		String result = "undefined";
		if (isExperimentOrWorkflow(el).equals("experiment")) {
			NodeList experiments = el.getElementsByTagName("experiment");
			for (int i = 0; i < experiments.getLength(); i++) {
				Element currWorkflow = (Element) experiments.item(i);
				if (currWorkflow.hasAttributes()) {
					result = currWorkflow.getAttribute("name");
				}
			}

		} else {
			NodeList workflows = el.getElementsByTagName("workflow");
			for (int i = 0; i < workflows.getLength(); i++) {
				Element currWorkflow = (Element) workflows.item(i);
				if (currWorkflow.hasAttributes()) {
					result = currWorkflow.getAttribute("name");
				}
			}

		}
		return result;
	}

	public static HashSet<Element> getAllDeclaredWorkflowsFromSpec(Document swlSpec) {
		HashSet<Element> allWorkflows = new HashSet<Element>();
		Element swlRootElement = swlSpec.getDocumentElement();
		NodeList workflowElements = swlRootElement.getElementsByTagName("workflow");
		for (int i = 0; i < workflowElements.getLength(); i++) {

			Element currentWorkflow = (Element) workflowElements.item(i);
			if (currentWorkflow.hasAttribute("name"))
				allWorkflows.add(currentWorkflow);
		}
		return allWorkflows;
	}

	// for a given workflow, return all components used by it (i.e. mentioned in its spec)
	public static HashSet<String> getAllReferencedComponents(Element workflow) {
		HashSet<String> referencedComponents = new HashSet<String>();
		String mode = getWorkflowMode(workflow);

		if (mode.equals("graph-based")) {
			NodeList listOfAllWFInstances = workflow.getElementsByTagName("workflowInstance");
			Element currentWFInstance = null;
			String instanceId = null;
			for (int i = 0; i < listOfAllWFInstances.getLength(); i++) {
				currentWFInstance = (Element) listOfAllWFInstances.item(i);
				NodeList workflows = currentWFInstance.getElementsByTagName("workflow");
				instanceId = (currentWFInstance.getAttribute("id"));
				referencedComponents.add(workflows.item(0).getTextContent().trim());
			}
		}
		if (mode.equals("unary-construct-based")) {
			NodeList baseWorkflows = workflow.getElementsByTagName("baseWorkflow");
			for (int i = 0; i < baseWorkflows.getLength(); i++) {
				Element currBaseWorkflow = (Element) baseWorkflows.item(i);
				if (currBaseWorkflow.getChildNodes().getLength() == 1) {
					referencedComponents.add(currBaseWorkflow.getChildNodes().item(0).getNodeValue().trim());
				}
			}
		}
		return referencedComponents;
	}

	public static HashSet<String> getAllReferencedComponentIDs(Element workflow) {
		HashSet<String> referencedComponentIDs = new HashSet<String>();
		String mode = getWorkflowMode(workflow);

		if (mode.equals("graph-based")) {
			NodeList listOfAllWFInstances = workflow.getElementsByTagName("workflowInstance");
			Element currentWFInstance = null;
			String instanceId = null;
			for (int i = 0; i < listOfAllWFInstances.getLength(); i++) {
				currentWFInstance = (Element) listOfAllWFInstances.item(i);
				instanceId = (currentWFInstance.getAttribute("id"));
				referencedComponentIDs.add(instanceId);
			}
		}

		return referencedComponentIDs;
	}

	public static HashSet<String> getAllUnaryConstructBasedComponents(Document swl) throws Exception {
		Element swlRootElement = swl.getDocumentElement();
		HashSet<String> ucbWorkflows = new HashSet<String>();
		NodeList nl = swlRootElement.getElementsByTagName("baseworkflow");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getChildNodes().getLength() == 1)
				ucbWorkflows.add(el.getChildNodes().item(0).getNodeValue().trim());
		}
		return ucbWorkflows;
	}

	public static String getBuiltinComponent(Element workflow) throws Exception {
		// returns a set of all non-builtin components used in this specification

		HashSet<String> allWorkflows = new HashSet<String>();
		// for graph-based:
		NodeList builtInElements = workflow.getElementsByTagName("builtin");
		for (int i = 0; i < builtInElements.getLength(); i++) {
			Element builtInElement = (Element) builtInElements.item(i);
			if (builtInElement.getChildNodes().getLength() == 1)
				return builtInElement.getChildNodes().item(0).getNodeValue().trim();
		}

		return "";

	}

	public static boolean singleRootWorkflow(Document swl) {
		Element rootWorkflow = null;
		HashSet<Element> rootWorkflows = new HashSet<Element>();
		NodeList workflows = swl.getDocumentElement().getElementsByTagName("workflow");
		for (int i = 0; i < workflows.getLength(); i++) {
			Element currentWorkflow = (Element) workflows.item(i);
			if (currentWorkflow.hasAttributes()) {
				String isRoot = currentWorkflow.getAttribute("root");

				if (isRoot.equals("true"))
					rootWorkflows.add(currentWorkflow);
			}
		}
		if (rootWorkflows.size() == 1)
			return true;
		else
			return false;
	}

	public static Element getRootWorkflow(Document swl) {
		Element rootWorkflow = null;
		NodeList workflows = swl.getDocumentElement().getElementsByTagName("workflow");
		for (int i = 0; i < workflows.getLength(); i++) {
			Element currentWorkflow = (Element) workflows.item(i);
			if (currentWorkflow.hasAttributes()) {
				String isRoot = currentWorkflow.getAttribute("root");

				if (isRoot.equals("true"))
					return currentWorkflow;
			}
		}
		return rootWorkflow;
	}

	public static String getWorkflowMode(Element workflow) {
		String mode = "undefined";
		NodeList workflowBodies = workflow.getElementsByTagName("workflowBody");
		NodeList childrenOfThisBody = ((Element) workflowBodies.item(0)).getChildNodes();
		for (int j = 0; j < childrenOfThisBody.getLength(); j++) {
			if (childrenOfThisBody.item(j).getNodeName().contains("unary-construct"))
				return "unary-construct-based";
			if (childrenOfThisBody.item(j).getNodeName().contains("taskComponent"))
				return "primitive";
			if (childrenOfThisBody.item(j).getNodeName().contains("workflowGraph"))
				return "graph-based";
			if (childrenOfThisBody.item(j).getNodeName().contains("builtin"))
				return "builtin";
		}
		return mode;
	}

	public static String getDeclaredWorkflowMode(Element workflow) {
		String mode = "undeclared";
		NodeList workflowBodies = workflow.getElementsByTagName("workflowBody");
		mode = ((Element) workflowBodies.item(0)).getAttribute("mode");

		return mode;
	}

	public static boolean verifyWorkflowMode(Element workflow) {
		boolean result = false;
		return getDeclaredWorkflowMode(workflow).equals(getWorkflowMode(workflow));
	}

	public static ArrayList<String> getWorkflowInputPorts(Element workflow) {
		ArrayList<String> result = new ArrayList();
		Element inputPorts = (Element) workflow.getElementsByTagName("inputPorts").item(0);
		NodeList inputPortIDs = inputPorts.getElementsByTagName("portID");
		for (int i = 0; i < inputPortIDs.getLength(); i++) {
			Element portID = (Element) inputPortIDs.item(i);
			result.add(portID.getTextContent().trim());
		}
		return result;

	}

	public static ArrayList<String> getWorkflowOutputPorts(Element workflow) {
		ArrayList<String> result = new ArrayList();
		Element inputPorts = (Element) workflow.getElementsByTagName("outputPorts").item(0);
		NodeList inputPortIDs = inputPorts.getElementsByTagName("portID");
		for (int i = 0; i < inputPortIDs.getLength(); i++) {
			Element portID = (Element) inputPortIDs.item(i);
			result.add(portID.getTextContent().trim());
		}
		return result;

	}

	public static HashSet<String> getInputMappingToAttribute(Element workflow, String fromPortID) {
		HashSet<String> result = new HashSet<String>();
		NodeList inputMappings = workflow.getElementsByTagName("inputMapping");
		for (int i = 0; i < inputMappings.getLength(); i++) {
			Element currentMapping = (Element) inputMappings.item(i);
			if (currentMapping.getAttribute("from").equals("this." + fromPortID)) {
				// System.out.println("||" + currentMapping.getAttribute("to") + "||");
				result.add(currentMapping.getAttribute("to"));
			}
		}
		return result;
	}

	public static HashSet<String> getDataChannelsToAttribute(Element workflow, String fromComponentId, String fromPortID) {
		HashSet<String> result = new HashSet<String>();
		NodeList dataChannels = workflow.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Element currentDataChannel = (Element) dataChannels.item(i);
			if (currentDataChannel.getAttribute("from").trim().equals(fromComponentId + "." + fromPortID)) {
				result.add(currentDataChannel.getAttribute("to"));
			}
		}
		return result;
	}

	public static ArrayList<String> getDataChannelsFromAttribute(Element workflow, String toComponentId, String toPortID) {
		ArrayList<String> result = new ArrayList<String>();
		NodeList dataChannels = workflow.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Element currentDataChannel = (Element) dataChannels.item(i);
			if (currentDataChannel.getAttribute("to").trim().equals(toComponentId + "." + toPortID))
				result.add(currentDataChannel.getAttribute("from"));
		}
		// System.out.println("ERROR: SWLAnalyzer cannot find from attribute " + toComponentId + " . " + toPortID);
		if (result.size() > 0)
			return result;
		return null;
	}

	public static String getComponentName(String instanceId, Element workflowSpec) {

		String result = "";
		NodeList workflowInstances = workflowSpec.getElementsByTagName("workflowInstance");
		Element rightWorkflow;
		for (int i = 0; i < workflowInstances.getLength(); i++) {
			Element currentInstance = (Element) workflowInstances.item(i);
			if (currentInstance.getAttribute("id").equals(instanceId)) {
				rightWorkflow = (Element) currentInstance.getElementsByTagName("workflow").item(0);
				return rightWorkflow.getTextContent().trim();
			}
		}
		return result;
	}

	// For a workflow A, get a set of all components such that for each component in the set every input port
	// is part of the A's interface. In other words, return all components who don't have any inputs connected to datachannels
	// (data products? 2015 comment).
	public static HashSet<String> getComponentsWithAllInputDataAvailable(Element workflowSpec) throws Exception {
		HashSet<String> workflows = new HashSet<String>();

		if (getWorkflowMode(workflowSpec).equals("graph-based")) {

			HashSet<String> allWorkflowsWithAtLeastOneInputMapping = getComponentsWithAtLeastOneInputMapping(workflowSpec);

			for (String componentId : allWorkflowsWithAtLeastOneInputMapping) {
				ArrayList<String> allPortsOfThisComponent = getWorkflowInputPorts(Repository.getWorkflowSpecification(getComponentName(componentId,
						workflowSpec)));
				if (getOnlyThosePortsThatArePartOfExternalInterface(workflowSpec, componentId).containsAll(allPortsOfThisComponent)) {
					// System.out.println("true, so adding");
					workflows.add(componentId);
				}
			}
		}
		return workflows;
	}

	// get a set of all components for which at least one port of a component is part of A's interface:
	public static HashSet<String> getComponentsWithAtLeastOneInputMapping(Element workflowSpec) {
		HashSet<String> allWorkflowsWithAtLeastOneInputMapping = new HashSet<String>();
		for (String portID : getWorkflowInputPorts(workflowSpec)) {
			for (String mapping : SWLAnalyzer.getInputMappingToAttribute(workflowSpec, portID)) {
				String componentInstanceId = mapping.substring(0, mapping.lastIndexOf("."));
				String portOfComponent = mapping.substring(mapping.lastIndexOf(".") + 1);
				allWorkflowsWithAtLeastOneInputMapping.add(componentInstanceId);
			}
		}
		return allWorkflowsWithAtLeastOneInputMapping;
	}

	private static HashSet<String> getOnlyThosePortsThatArePartOfExternalInterface(Element workflowSpec, String componentId) {
		// if(componentId.contains("AddAndMultiply2"))
		// {
		// //System.out.println(componentId);
		// //System.out.println(Utility.nodeToString(workflowSpec));
		//
		// }
		HashSet<String> portsThatArePartOfExternalInterface = new HashSet<String>();

		for (String portID : getWorkflowInputPorts(workflowSpec)) {
			for (String mapping : SWLAnalyzer.getInputMappingToAttribute(workflowSpec, portID)) {
				// System.out.println("mapping: " + mapping);
				String componentInstanceId = mapping.substring(0, mapping.lastIndexOf("."));
				String portOfComponent = mapping.substring(mapping.lastIndexOf(".") + 1);
				if (componentInstanceId.equals(componentId))
					portsThatArePartOfExternalInterface.add(portOfComponent);
			}
		}
		return portsThatArePartOfExternalInterface;
	}

	public static HashSet<String> getAllConsumers(String componentId, Element workflowSpec) {
		HashSet<String> consumers = new HashSet<String>();
		NodeList dataChannels = workflowSpec.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Element dataChannel = (Element) dataChannels.item(i);
			String fromAttribute = dataChannel.getAttribute("from").trim();
			if (fromAttribute.substring(0, fromAttribute.lastIndexOf(".")).equals(componentId)) {
				String toAttribute = dataChannel.getAttribute("to").trim();
				consumers.add(toAttribute.substring(0, toAttribute.lastIndexOf(".")));
			}
		}

		return consumers;
	}

	// for a graph-based workflow, given a portID, get all components and port ids that are connected to this portID.
	// in other words ports that are consuming the same data product as portID is consuming
	public static ArrayList<Pair> getConsumersOfThisInputPort(String portID, Element workflowSpec) {
		ArrayList<Pair> instanceIDtoPortID = new ArrayList<Pair>();

		for (String mapping : SWLAnalyzer.getInputMappingToAttribute(workflowSpec, portID)) {
			String componentInstanceId = mapping.substring(0, mapping.lastIndexOf("."));
			String portOfComponent = mapping.substring(mapping.lastIndexOf(".") + 1);
			instanceIDtoPortID.add(new Pair(componentInstanceId, portOfComponent));
		}

		return instanceIDtoPortID;
	}

	public static ArrayList<Pair> getAllPortsOnTheOtherEndOfEachDataChannel(String fromComponentId, String fromPortID, Element workflowSpec) {
		ArrayList<Pair> instanceIDtoPortID = new ArrayList<Pair>();

		for (String toAttribute : SWLAnalyzer.getDataChannelsToAttribute(workflowSpec, fromComponentId, fromPortID)) {
			String toComponentInstanceId = toAttribute.substring(0, toAttribute.lastIndexOf("."));
			String toPortOfComponent = toAttribute.substring(toAttribute.lastIndexOf(".") + 1);
			instanceIDtoPortID.add(new Pair(toComponentInstanceId, toPortOfComponent));
		}

		return instanceIDtoPortID;
	}

	public static ArrayList<Pair> getPortOnTheLeftEndOfTheDataChannel(String toComponentId, String toPortID, Element workflowSpec) {
		ArrayList<Pair> result = new ArrayList<Pair>();
		ArrayList<String> fromAttributes = getDataChannelsFromAttribute(workflowSpec, toComponentId, toPortID);
		for (String fromAttribute : fromAttributes)
			result.add(new Pair(fromAttribute.substring(0, fromAttribute.indexOf(".")), fromAttribute.substring(fromAttribute.indexOf(".") + 1)));

		return result;
	}

	public static HashSet<String> getConsumersOfThisOutputPort(String componentId, String fromPortID, Element workflowSpec) {
		HashSet<String> portIDs = new HashSet<String>();

		NodeList outputMappings = workflowSpec.getElementsByTagName("outputMapping");
		for (int i = 0; i < outputMappings.getLength(); i++) {
			Element currentMapping = (Element) outputMappings.item(i);
			if (currentMapping.getAttribute("from").equals(componentId + "." + fromPortID)) {
				portIDs.add(currentMapping.getAttribute("to").trim().replace("this.", ""));
			}
		}

		return portIDs;
	}

	public static String getTaskType(Element primitiveWorkflowSpec) {
		String taskType = "undefined";
		Element taskComponent = (Element) primitiveWorkflowSpec.getElementsByTagName("taskComponent").item(0);
		taskType = taskComponent.getAttribute("taskType").trim();
		return taskType;
	}

	public static String getExecutableFullName(Element primitiveWorkflowSpec) {
		String fullname = "undefined";
		Element executable = (Element) primitiveWorkflowSpec.getElementsByTagName("executable").item(0);
		fullname = executable.getTextContent().trim();
		return fullname;
	}

	public static String getHostName(Element primitiveWorkflowSpec) {
		String result = "undefined";
		Element hostname = (Element) primitiveWorkflowSpec.getElementsByTagName("hostName").item(0);
		result = hostname.getTextContent().trim();
		return result;
	}

	public static String getUserName(Element primitiveWorkflowSpec) {
		String result = "undefined";
		Element username = (Element) primitiveWorkflowSpec.getElementsByTagName("userName").item(0);
		result = username.getTextContent().trim();
		return result;
	}

	public static String getPassword(Element primitiveWorkflowSpec) {
		String result = "undefined";
		Element password = (Element) primitiveWorkflowSpec.getElementsByTagName("password").item(0);
		result = password.getTextContent().trim();
		return result;
	}

	public static String getPortType(String wfName, String portID) throws Exception {
		Element wfSpec = Repository.getWorkflowSpecification(wfName);
		return getPortType(wfSpec, portID);
	}

	public static String getPortType(Element workflowSpec, String portID) throws Exception {
		String result = "undefined";
		if (isWebservice(workflowSpec)) {
			result = "";
			String opName = getWSOperationName(workflowSpec);
			if (getWorkflowInputPorts(workflowSpec).contains(portID)) {
				Node type = WSDLAnalyzer.getOperationInputType(getWSDL(workflowSpec), opName);
				return Utility.nodeToString(type);
			}

			else if (getWorkflowOutputPorts(workflowSpec).contains(portID)) {
				Node type = WSDLAnalyzer.getOperationOutputType(getWSDL(workflowSpec), opName);
				return Utility.nodeToString(type);
			} else
				System.out.println("ERROR: cannot find such port " + portID);
		}

		Element ports = (Element) workflowSpec.getElementsByTagName("inputPorts").item(0);
		NodeList portsList = ports.getElementsByTagName("inputPort");
		for (int i = 0; i < portsList.getLength(); i++) {
			Element inputPort = (Element) portsList.item(i);
			String currentPortID = inputPort.getElementsByTagName("portID").item(0).getTextContent().trim();
			String currentPortType = inputPort.getElementsByTagName("portType").item(0).getTextContent().trim();
			if (currentPortID.equals(portID))
				return currentPortType;
		}

		ports = (Element) workflowSpec.getElementsByTagName("outputPorts").item(0);
		portsList = ports.getElementsByTagName("outputPort");
		for (int i = 0; i < portsList.getLength(); i++) {
			Element inputPort = (Element) portsList.item(i);
			String currentPortID = inputPort.getElementsByTagName("portID").item(0).getTextContent().trim();
			String currentPortType = inputPort.getElementsByTagName("portType").item(0).getTextContent().trim();
			if (currentPortID.equals(portID))
				return currentPortType;
		}
		return result;
	}

	public static ArrayList<String> getPortNameAndType(Element workflowSpec, String portID) {
		ArrayList<String> result = null;
		Element ports = (Element) workflowSpec.getElementsByTagName("inputPorts").item(0);
		NodeList portsList = ports.getElementsByTagName("inputPort");
		for (int i = 0; i < portsList.getLength(); i++) {
			Element inputPort = (Element) portsList.item(i);
			String currentPortID = inputPort.getElementsByTagName("portID").item(0).getTextContent().trim();
			String currentPortName = inputPort.getElementsByTagName("portName").item(0).getTextContent().trim();
			String currentPortType = inputPort.getElementsByTagName("portType").item(0).getTextContent().trim();
			if (currentPortID.equals(portID)) {
				result = new ArrayList<String>();
				result.add(currentPortName);
				result.add(currentPortType);
			}
		}

		ports = (Element) workflowSpec.getElementsByTagName("outputPorts").item(0);
		portsList = ports.getElementsByTagName("outputPort");
		for (int i = 0; i < portsList.getLength(); i++) {
			Element outputPort = (Element) portsList.item(i);
			String currentPortID = outputPort.getElementsByTagName("portID").item(0).getTextContent().trim();
			String currentPortName = outputPort.getElementsByTagName("portName").item(0).getTextContent().trim();
			String currentPortType = outputPort.getElementsByTagName("portType").item(0).getTextContent().trim();
			if (currentPortID.equals(portID)) {
				result = new ArrayList<String>();
				result.add(currentPortName);
				result.add(currentPortType);
			}
		}
		return result;
	}

	public static String getWSDL(Element primitiveWorkflowSpec) {
		String wsdluri = "undefined";
		Element wsdluriEl = (Element) primitiveWorkflowSpec.getElementsByTagName("wsdlURI").item(0);
		wsdluri = wsdluriEl.getTextContent().trim();
		return wsdluri;
	}

	public static String getServiceName(Element primitiveWorkflowSpec) {
		String result = "undefined";
		Element serviceName = (Element) primitiveWorkflowSpec.getElementsByTagName("serviceName").item(0);
		result = serviceName.getTextContent().trim();
		return result;
	}

	public static String getOperationName(Element primitiveWorkflowSpec) {
		String result = "undefined";
		Element opName = (Element) primitiveWorkflowSpec.getElementsByTagName("operationName").item(0);
		result = opName.getTextContent().trim();
		return result;
	}

	public static String getTypeOfConstruct(Element ucBasedWorkflowSpec) {
		String typeOfConstruct = "undefined";
		Element unaryConstructEl = (Element) ucBasedWorkflowSpec.getElementsByTagName("unary-construct").item(0);

		NodeList childrenList = unaryConstructEl.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++) {
			Node currChild = childrenList.item(i);
			if (currChild.getNodeType() == Node.ELEMENT_NODE) {
				typeOfConstruct = currChild.getNodeName();
			}
		}

		return typeOfConstruct;
	}

	public static Element ucBased2GraphBased(Element ucBasedworkflowSpec) throws Exception {
		Element resultSpec = null;

		System.out.println("uc based call");
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document resultSpecDoc = parser.newDocument();
		Node workflowSpec = resultSpecDoc.createElement("workflowSpec");
		resultSpecDoc.appendChild(workflowSpec);

		Node workflow = resultSpecDoc.createElement("workflow");
		workflowSpec.appendChild(workflow);

		Node workflowInterface = resultSpecDoc.createElement("workflowInterface");
		workflow.appendChild(workflowInterface);

		Node workflowBody = resultSpecDoc.createElement("workflowBody");
		workflow.appendChild(workflowBody);

		String ucType = getTypeOfConstruct(ucBasedworkflowSpec);
		if (ucType.equals("tree")) {

		}
		System.out.println("result of conversion:------------\n");
		System.out.println(Utility.formatXML(resultSpecDoc));
		return resultSpec;
	}

	public static JSONObject getWorkflowNamesAndPortInfos() throws Exception {
		JSONObject allWorkflowsInfos = new JSONObject();
		for (String name : Repository.getAllWorkflowNames()) {
			Element currWorkflowSpec = Repository.getWorkflowSpecification(name);

			JSONObject currWorkflowInfo = new JSONObject();
			ArrayList<String> inputPorts = SWLAnalyzer.getWorkflowInputPorts(currWorkflowSpec);
			ArrayList<String> outputPorts = SWLAnalyzer.getWorkflowOutputPorts(currWorkflowSpec);

			for (String portID : inputPorts) {
				JSONObject inputPortID2type = new JSONObject();
				inputPortID2type.put("id", portID);
				ArrayList<String> currNameAndType = getPortNameAndType(currWorkflowSpec, portID);
				Iterator<String> it1 = currNameAndType.iterator();
				String portName1 = it1.next();
				String portType1 = it1.next();
				inputPortID2type.put("name", portName1);
				inputPortID2type.put("type", portType1);
				currWorkflowInfo.put(portID, inputPortID2type);
			}

			for (String portID : outputPorts) {
				JSONObject outputPortID2type = new JSONObject();
				outputPortID2type.put("id", portID);
				ArrayList<String> currNameAndType = getPortNameAndType(currWorkflowSpec, portID);
				Iterator<String> it1 = currNameAndType.iterator();
				String portName1 = it1.next();
				String portType1 = it1.next();
				outputPortID2type.put("name", portName1);
				outputPortID2type.put("type", portType1);
				currWorkflowInfo.put(portID, outputPortID2type);
			}

			currWorkflowInfo.put("name", name);
			JSONObject metadata = Repository.getWorkflowMetadata(name);
			currWorkflowInfo.put("isCorrect", metadata.get("isCorrect"));
			currWorkflowInfo.put("isExperiment", metadata.get("isExperiment"));
			currWorkflowInfo.put("orderedListOfInputPorts", inputPorts);
			currWorkflowInfo.put("orderedListOfOutputPorts", outputPorts);

			allWorkflowsInfos.put(name, currWorkflowInfo);
		}
		return allWorkflowsInfos;
	}

	public static JSONObject getPortsMetadata(Element workflowSpec) throws Exception {
		JSONObject result = new JSONObject();

		if (isWebservice(workflowSpec))
			return getPortsMetadataForWS(workflowSpec);

		NodeList inputPortList = workflowSpec.getElementsByTagName("inputPort");
		ArrayList<String> orderedListOfInputPorts = new ArrayList<String>();
		for (int i = 0; i < inputPortList.getLength(); i++) {
			Element currInputPort = (Element) inputPortList.item(i);
			String portID = currInputPort.getElementsByTagName("portID").item(0).getTextContent().trim();
			orderedListOfInputPorts.add(portID);
			String portName = currInputPort.getElementsByTagName("portName").item(0).getTextContent().trim();
			String portType = currInputPort.getElementsByTagName("portType").item(0).getTextContent().trim();
			JSONObject currentPortInfo = new JSONObject();
			currentPortInfo.put("portName", portName);
			currentPortInfo.put("portType", portType);
			result.put(portID, currentPortInfo);
		}

		NodeList outputPortList = workflowSpec.getElementsByTagName("outputPort");
		ArrayList<String> orderedListOfOutputPorts = new ArrayList<String>();
		for (int i = 0; i < outputPortList.getLength(); i++) {
			Element currOutputPort = (Element) outputPortList.item(i);
			String portID = currOutputPort.getElementsByTagName("portID").item(0).getTextContent().trim();
			orderedListOfOutputPorts.add(portID);
			String portName = currOutputPort.getElementsByTagName("portName").item(0).getTextContent().trim();
			String portType = currOutputPort.getElementsByTagName("portType").item(0).getTextContent().trim();
			JSONObject currentPortInfo = new JSONObject();
			currentPortInfo.put("portName", portName);
			currentPortInfo.put("portType", portType);
			result.put(portID, currentPortInfo);
		}

		result.put("orderedListOfInputPorts", orderedListOfInputPorts);
		result.put("orderedListOfOutputPorts", orderedListOfOutputPorts);
		return result;
	}

	public static JSONObject getPortsMetadataForWS(Element workflowSpec) throws Exception {
		JSONObject result = new JSONObject();

		JSONObject currentPortInfo = new JSONObject();
		currentPortInfo.put("portName", "i1");
		currentPortInfo.put("portType", "Int");
		result.put("i1", currentPortInfo);

		currentPortInfo.put("portName", "o1");
		currentPortInfo.put("portType", "Int");
		result.put("o1", currentPortInfo);

		ArrayList<String> orderedListOfInputPorts = new ArrayList<String>();
		ArrayList<String> orderedListOfOutputPorts = new ArrayList<String>();

		orderedListOfInputPorts.add("i1");
		orderedListOfOutputPorts.add("o1");

		result.put("orderedListOfInputPorts", orderedListOfInputPorts);
		result.put("orderedListOfOutputPorts", orderedListOfOutputPorts);
		return result;

	}

	public static String isExperimentOrWorkflow(Element el) {
		String result = "undefined";
		if (el.getNodeName().trim().equals("workflowSpec"))
			result = "workflow";
		else if (el.getNodeName().trim().equals("experimentSpec"))
			result = "experiment";
		return result;
	}

	public static Element translateExperimentSpecIntoWorkflowSpec(Element expSpec) throws Exception {

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document wfSpecDoc = parser.newDocument();
		Node workflowSpec = wfSpecDoc.createElement("workflowSpec");
		wfSpecDoc.appendChild(workflowSpec);

		Node workflow = wfSpecDoc.createElement("workflow");

		String wfName = ((Element) expSpec.getElementsByTagName("experiment").item(0)).getAttribute("name").trim();

		((Element) workflow).setAttribute("name", "wfFromExperiment" + wfName);
		((Element) workflow).setAttribute("root", "true");

		workflowSpec.appendChild(workflow);

		Node workflowInterface = wfSpecDoc.createElement("workflowInterface");
		workflow.appendChild(workflowInterface);

		Node workflowBody = wfSpecDoc.createElement("workflowBody");
		((Element) workflowBody).setAttribute("mode", "graph-based");

		Element workflowGraph = (Element) expSpec.getElementsByTagName("workflowGraph").item(0);
		workflowBody.appendChild(wfSpecDoc.adoptNode(workflowGraph.cloneNode(true)));

		workflow.appendChild(workflowBody);

		// now let's build workflowInterface based on dataProductsToPorts element of experiment spec:
		Node inputPorts = wfSpecDoc.createElement("inputPorts");
		Element G2W = wfSpecDoc.createElement("G2W");

		NodeList inputDP2PortMappings = expSpec.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) inputDP2PortMappings.item(i);
			String toAttribute = currMapping.getAttribute("to").trim();
			String portNameStr = currMapping.getAttribute("from").trim() + "_TO_" + toAttribute.replace(".", "_dot_");

			Node inputPort = wfSpecDoc.createElement("inputPort");

			Node portName = wfSpecDoc.createElement("portName");
			Node portID = wfSpecDoc.createElement("portID");

			portName.setTextContent(portNameStr);
			portID.setTextContent(portNameStr);

			// find port type of component:
			String componentPortType = "";
			String componentInstanceId = toAttribute.substring(0, toAttribute.indexOf("."));
			String componentPortID = toAttribute.substring(toAttribute.indexOf(".") + 1, toAttribute.length());
			String componentWorkflowName = "";
			NodeList workflowInstances = expSpec.getElementsByTagName("workflowInstance");
			for (int j = 0; j < workflowInstances.getLength(); j++) {
				if (((Element) workflowInstances.item(j)).getAttribute("id").trim().equals(componentInstanceId)) {
					componentWorkflowName = ((Element) workflowInstances.item(j)).getElementsByTagName("workflow").item(0).getTextContent().trim();
					Element componentSpec = Repository.getWorkflowSpecification(componentWorkflowName.trim());
//					System.out.println("componentWorkflowName: " + componentWorkflowName);
//					System.out.println("componetnSpec: " + Utility.nodeToString(componentSpec));
					componentPortType = SWLAnalyzer.getPortType(componentSpec, componentPortID);
				}
			}
			Node portType = wfSpecDoc.createElement("portType");
			portType.setTextContent(componentPortType);

			inputPort.appendChild(portID);
			inputPort.appendChild(portName);
			inputPort.appendChild(portType);

			inputPorts.appendChild(inputPort);

			Element inputMapping = wfSpecDoc.createElement("inputMapping");

			String inputMappingFrom = "this." + portNameStr;
			String inputMappingTo = portNameStr.substring(portNameStr.indexOf("TO_") + 3, portNameStr.length()).replace("_dot_", ".");

			inputMapping.setAttribute("from", inputMappingFrom);
			inputMapping.setAttribute("to", inputMappingTo);

			G2W.appendChild(inputMapping);
		}

		// /////////////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////////////////////////////////
		Node outputPorts = wfSpecDoc.createElement("outputPorts");

		NodeList outputDP2PortMappings = expSpec.getElementsByTagName("outputDP2PortMapping");
		for (int i = 0; i < outputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) outputDP2PortMappings.item(i);
			String fromAttribute = currMapping.getAttribute("from").trim();
			String portNameStr = fromAttribute.replace(".", "_dot_") + "_TO_" + currMapping.getAttribute("to").trim();

			Node outputPort = wfSpecDoc.createElement("outputPort");

			Node portName = wfSpecDoc.createElement("portName");
			Node portID = wfSpecDoc.createElement("portID");

			portName.setTextContent(portNameStr);
			portID.setTextContent(portNameStr);

			// find port type of component:
			String componentPortType = "";
			String componentInstanceId = fromAttribute.substring(0, fromAttribute.indexOf("."));
			String componentPortID = fromAttribute.substring(fromAttribute.indexOf(".") + 1, fromAttribute.length());
			String componentWorkflowName = "";
			NodeList workflowInstances = expSpec.getElementsByTagName("workflowInstance");
			for (int j = 0; j < workflowInstances.getLength(); j++) {
				if (((Element) workflowInstances.item(j)).getAttribute("id").trim().equals(componentInstanceId)) {
					componentWorkflowName = ((Element) workflowInstances.item(j)).getElementsByTagName("workflow").item(0).getTextContent().trim();
					Element componentSpec = Repository.getWorkflowSpecification(componentWorkflowName);
					componentPortType = SWLAnalyzer.getPortType(componentSpec, componentPortID);
				}
			}
			Node portType = wfSpecDoc.createElement("portType");
			portType.setTextContent(componentPortType);

			outputPort.appendChild(portID);
			outputPort.appendChild(portName);
			outputPort.appendChild(portType);

			outputPorts.appendChild(outputPort);

			Element outputMapping = wfSpecDoc.createElement("outputMapping");
			outputMapping.setAttribute("from", portNameStr.substring(0, portNameStr.indexOf("TO_") - 1).replace("_dot_", "."));
			outputMapping.setAttribute("to", "this." + portNameStr);
			G2W.appendChild(outputMapping);
		}

		workflowInterface.appendChild(inputPorts);
		workflowInterface.appendChild(outputPorts);
		workflowBody.appendChild(G2W);
		return (Element) workflowSpec;
	}

	public static HashMap<String, ArrayList<String>> getExperiment_portID_to_dpID(Element experimentSpec) {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();

		NodeList inputDP2PortMappings = experimentSpec.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element currMapping = ((Element) inputDP2PortMappings.item(i));
			String DPID = currMapping.getAttribute("from").trim();
			String portID = currMapping.getAttribute("from").trim() + "_TO_" + currMapping.getAttribute("to").trim().replace(".", "_dot_");
			ArrayList<String> currDP_list = result.get(portID);
			if (currDP_list == null) {
				currDP_list = new ArrayList<String>();
				result.put(portID, currDP_list);
			}
			currDP_list.add(DPID);
			// result.put(portID, DPID);
		}

		NodeList outputDP2PortMappings = experimentSpec.getElementsByTagName("outputDP2PortMapping");
		for (int i = 0; i < outputDP2PortMappings.getLength(); i++) {
			Element currMapping = ((Element) outputDP2PortMappings.item(i));
			String DPID = currMapping.getAttribute("to").trim();
			String portID = currMapping.getAttribute("from").trim().replace(".", "_dot_") + "_TO_" + currMapping.getAttribute("to").trim();
			ArrayList<String> currDP_list = result.get(portID);
			if (currDP_list == null) {
				currDP_list = new ArrayList<String>();
				result.put(portID, currDP_list);
			}
			currDP_list.add(DPID);
			// result.put(portID, DPID);
		}

		return result;
	}

	public static boolean workflowAndEachOfItsComponentsHasASingleOutputPort(Element workflowOrExperSpec) throws Exception {
		if (isExperimentOrWorkflow(workflowOrExperSpec).trim().equals("experiment")) {
			NodeList outputDP2PortMappings = workflowOrExperSpec.getElementsByTagName("outputDP2PortMapping");
			int numberOfOutputDPs = outputDP2PortMappings.getLength();
			if (numberOfOutputDPs > 1)
				return false;

			boolean allComponentsHaveASingleOutput = true;
			/*
			 * 
			 * <workflowInstance id="IncrementLong2"> <workflow> IncrementLong</workflow> </workflowInstance>
			 */
			NodeList workflowInstanceElements = workflowOrExperSpec.getElementsByTagName("workflowInstance");
			for (int i = 0; i < workflowInstanceElements.getLength(); i++) {
				Element currComponentInstance = (Element) workflowInstanceElements.item(i);
				String currComponentName = currComponentInstance.getElementsByTagName("workflow").item(0).getTextContent().trim();
				Element currComponentSpec = Repository.getWorkflowSpecification(currComponentName);
				boolean currComponentHasASingleOutput = workflowAndEachOfItsComponentsHasASingleOutputPort(currComponentSpec);
				allComponentsHaveASingleOutput = allComponentsHaveASingleOutput && currComponentHasASingleOutput;
			}
			return allComponentsHaveASingleOutput;
		} else {
			int NoOfoutputPortsOfThisWorkflow = getWorkflowOutputPorts(workflowOrExperSpec).size();
			if (NoOfoutputPortsOfThisWorkflow > 1)
				return false;

			if (getWorkflowMode(workflowOrExperSpec).equals("primitive")
					|| (getWorkflowMode(workflowOrExperSpec).equals("builtin") && !Translator.isCoercion(getWorkflowName(workflowOrExperSpec))))
				return true;
			else {
				boolean allComponentsHaveASingleOutput = true;
				HashSet<String> allReferencedComponentNames = getAllReferencedComponents(workflowOrExperSpec);
				for (String currComponentName : allReferencedComponentNames) {
					Element currComponentSpec = Repository.getWorkflowSpecification(currComponentName);
					boolean currComponentHasASingleOutput = workflowAndEachOfItsComponentsHasASingleOutputPort(currComponentSpec);
					allComponentsHaveASingleOutput = allComponentsHaveASingleOutput && currComponentHasASingleOutput;
				}
				return allComponentsHaveASingleOutput;
			}

		}

	}

	public static boolean involvesRelationalAlgebra(Element workflowOrExperSpec) throws Exception {
		// System.out.println("involves call for " + getWorkflowOrExperimentName(workflowOrExperSpec));
		if (isExperimentOrWorkflow(workflowOrExperSpec).trim().equals("experiment")) {
			NodeList inputDP2PortMappings = workflowOrExperSpec.getElementsByTagName("inputDP2PortMapping");
			for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
				Element currMapping = (Element) inputDP2PortMappings.item(i);
				String inputDP = currMapping.getAttribute("from");
				// if (getWorkflowOrExperimentName(workflowOrExperSpec).trim().contains("kevin1"))
				// System.out.println("kev dp: \n" + DataProductManager.getDataProductDPL(inputDP) + "\nend");
				// if (getWorkflowOrExperimentName(workflowOrExperSpec).trim().contains("mahdi111"))
				// System.out.println("mahdi dp: \n" + DataProductManager.getDataProductDPL(inputDP) + "\nend");
				if (!DataProductManager.getDataProductDPL(inputDP).trim().equals("")) {
					Document dpl = XMLParser.getDocument(DataProductManager.getDataProductDPL(inputDP));
					Element type = (Element) dpl.getElementsByTagName("type").item(0);
					if (type.getTextContent().trim().equals("Relation"))
						return true;
				}
			}
			boolean atLeastOneComponentInvolvesRelationalAlgebra = false;
			for (String componentName : getAllReferencedComponents(workflowOrExperSpec)) {
				Element componentSpec = Repository.getWorkflowSpecification(componentName);
				atLeastOneComponentInvolvesRelationalAlgebra = atLeastOneComponentInvolvesRelationalAlgebra
						|| involvesRelationalAlgebra(componentSpec);
			}
			return atLeastOneComponentInvolvesRelationalAlgebra;
		} else {
			NodeList portTypes = workflowOrExperSpec.getElementsByTagName("portType");
			for (int i = 0; i < portTypes.getLength(); i++) {
				Element currType = (Element) portTypes.item(i);
				if (currType.getTextContent().trim().equals("RelationBase"))
					return true;
			}
			return false;
		}

	}

	public static ArrayList<String> getOutputDPIds(Element workflowOrExperSpec) throws Exception {
		if (!isExperimentOrWorkflow(workflowOrExperSpec).trim().equals("experiment"))
			return null;

		ArrayList<String> outputDPIds = new ArrayList<String>();

		NodeList outputDP2PortMappings = workflowOrExperSpec.getElementsByTagName("outputDP2PortMapping");
		for (int i = 0; i < outputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) outputDP2PortMappings.item(i);
			String toAttribute = currMapping.getAttribute("to").trim();
			outputDPIds.add(toAttribute);
		}
		return outputDPIds;
	}

	public static ArrayList<String> getOutputFileNames(Element workflowOrExperSpec) throws Exception {
		if (!isExperimentOrWorkflow(workflowOrExperSpec).trim().equals("experiment"))
			return null;

		ArrayList<String> outputDPIds = new ArrayList<String>();

		NodeList outputDP2PortMappings = workflowOrExperSpec.getElementsByTagName("outputDP2PortMapping");
		for (int i = 0; i < outputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) outputDP2PortMappings.item(i);
			String toAttribute = currMapping.getAttribute("from").trim();
			outputDPIds.add(toAttribute);
		}
		return outputDPIds;
	}

	public static boolean isWebservice(Element workflowSpec) {
		return (workflowSpec.getElementsByTagName("taskComponent").item(0) != null && ((Element) workflowSpec.getElementsByTagName("taskComponent")
				.item(0)).getAttribute("taskType").trim().equals("WebService"));
	}

	public static String getWSOperationName(Element workflowSpec) {
		ArrayList<String> result = new ArrayList<String>();
		Element operationName = (Element) workflowSpec.getElementsByTagName("operationName").item(0);
		return operationName.getTextContent().trim();
	}

	public static Document createWSBasedPWorkflowSpec(String wsdl, String opName) throws Exception {
		String pathToSampleWS = Utility.pathToConfigFile.replace("systemFiles/config", "testFiles/JUnit/primitive/samplews.swl");
		String newServiceName = wsdl.substring(wsdl.lastIndexOf("/") + 1, wsdl.indexOf("?"));

		String samplewsSpec = Utility.readFileAsString(pathToSampleWS);
		samplewsSpec = samplewsSpec.replace("http://dmsg1.cs.wayne.edu:8080/axis2/services/weatherVariations?wsdl", wsdl);

		samplewsSpec = samplewsSpec.replaceAll("analyzeWeatherData", opName);
		samplewsSpec = samplewsSpec.replaceAll("weatherVariations", newServiceName);
		Document result = XMLParser.getDocument(samplewsSpec);

		return result;
	}

	public static Node extractNode(Element data, String nodeName) {
		// System.out.println("extracting a child " + nodeName + " from \n" + Utility.nodeToString(data));
		NodeList children = data.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Node currC = children.item(i);
				if (currC.getNodeName().trim().equals(nodeName)) {
					// if (currC.getChildNodes().getLength() == 1)
					// return currC.getChildNodes().item(0);
					// else
					// System.out.println("calculus.engine: more than one child found for node " + Utility.nodeToString(currC));
					// return null;
					return currC;
				}
			}
		}
		System.out.println("extractNode returns null");
		return null;
	}

	public static Node extractText(Element data) {
		NodeList children = data.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.TEXT_NODE) {

				Node currC = children.item(i);
				return currC;
			}
		}
		return null;
	}

	public static String getInputParameter(Element spec, String parameterName) {
		NodeList inputParameters = spec.getElementsByTagName("inputParameter");
		for (int i = 0; i < inputParameters.getLength(); i++) {
			Node currN = inputParameters.item(i);
			String nameAttr = ((Element) currN).getAttribute("name").trim();
			if (nameAttr.equals(parameterName))
				return currN.getTextContent().trim();
		}
		System.out.println("getInputParameter returning null");
		return null;
	}

	public static Element getReturnElement(Element input) throws Exception {
		String inputStr = Utility.nodeToString(input);
		inputStr = removeXmlStringNamespaceAndPreamble(inputStr);
		input = XMLParser.getDocument(inputStr).getDocumentElement();
		if (input.getNodeName().trim().equals("return"))
			return input;

		if (!input.hasChildNodes())
			return null;

		Element returnEl = null;
		NodeList childrenOfCurrEl = input.getChildNodes();
		for (int i = 0; i < childrenOfCurrEl.getLength(); i++) {
			Node currN = childrenOfCurrEl.item(i);
			if (currN.getNodeType() == Node.ELEMENT_NODE) {
				returnEl = getReturnElement(((Element) currN));
				if (returnEl != null)
					return returnEl;
			}
		}

		return null;
	}

	public static Element getChildOfReturnIfSingleElement(Element input) throws Exception {
		Element returnEl = getReturnElement(input);
		if (returnEl == null)
			return null;
		NodeList childrenOfReturn = returnEl.getChildNodes();
		ArrayList<Node> childrenOfReturnList = new ArrayList<Node>();
		for (int i = 0; i < childrenOfReturn.getLength(); i++) {
			Node currChild = childrenOfReturn.item(i);

			if (currChild.getNodeType() == Node.ELEMENT_NODE) {
				childrenOfReturnList.add(currChild);
			}
		}
		if (childrenOfReturnList.size() == 1)
			return (Element) childrenOfReturnList.get(0);
		else
			return null;
	}

	public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
		xmlString = xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
		replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
		.replaceAll("xsi.*?(\"|\').*?(\"|\')", "") /* remove xsi declaration */
		.replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
		.replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
		while (xmlString.contains(" >"))
			xmlString = xmlString.replace(" >", ">");
		return xmlString;
	}

	public static boolean twoComponentsAreConnected(Element spec, String compAid, String compBid) {
		NodeList dataChannels = spec.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Node currN = dataChannels.item(i);
			if (twoCompIdsAreFromAndTwoAttributes(currN, compAid, compBid))
				return true;
		}

		NodeList inputDP2PortMappings = spec.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Node currN = inputDP2PortMappings.item(i);
			if (twoCompIdsAreFromAndTwoAttributes(currN, compAid, compBid))
				return true;
		}

		NodeList outputDP2PortMappings = spec.getElementsByTagName("outputDP2PortMapping");
		for (int i = 0; i < outputDP2PortMappings.getLength(); i++) {
			Node currN = outputDP2PortMappings.item(i);
			if (twoCompIdsAreFromAndTwoAttributes(currN, compAid, compBid))
				return true;
		}

		return false;
	}

	public static boolean twoCompIdsAreFromAndTwoAttributes(Node element, String compAid, String compBid) {
		Element input = ((Element) element);
		String fromCompId = input.getAttribute("from");
		if (fromCompId.indexOf(".") != -1)
			fromCompId = fromCompId.substring(0, fromCompId.indexOf("."));
		String toCompId = input.getAttribute("to");
		if (toCompId.indexOf(".") != -1)
			toCompId = toCompId.substring(0, toCompId.indexOf("."));

		if (fromCompId.trim().equals(compAid.trim()) && toCompId.trim().equals(compBid.trim()))
			return true;
		if (toCompId.trim().equals(compAid.trim()) && fromCompId.trim().equals(compBid.trim()))
			return true;
		return false;
	}

	public static ArrayList<String> getAllInstanceIds(Element spec) {
		ArrayList<String> result = new ArrayList<String>();
		NodeList workflowInstanceElems = spec.getElementsByTagName("workflowInstance");
		for (int i = 0; i < workflowInstanceElems.getLength(); i++) {
			Node currN = workflowInstanceElems.item(i);
			result.add(((Element) currN).getAttribute("id").trim());
		}
		return result;
	}

	public static boolean aIsConnectedToB(Element spec, String Aid, String Bid) {
		NodeList dataChannels = spec.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Node currN = dataChannels.item(i);

			String fromAttr = ((Element) currN).getAttribute("from");
			String fromAttrBeforeDot = fromAttr.substring(0, fromAttr.lastIndexOf(".")).trim();

			String toAttr = ((Element) currN).getAttribute("to");
			String toAttrBeforeDot = toAttr.substring(0, toAttr.lastIndexOf("."));

			if (fromAttrBeforeDot.equals(Aid) && toAttrBeforeDot.equals(Bid))
				return true;
		}
		return false;
	}

	public static void addDummyComponent(Element spec) {
		Node workflowInstances = spec.getElementsByTagName("workflowInstances").item(0);

		Document doc = spec.getOwnerDocument();

		Element workflowInstance = doc.createElement("workflowInstance");

		workflowInstances.appendChild(workflowInstance);
		workflowInstance.setAttribute("id", "dummy");

		Node dataProductsToPorts = spec.getElementsByTagName("dataProductsToPorts").item(0);
		NodeList inputDP2PortMappings = spec.getElementsByTagName("inputDP2PortMapping");

		Node dataChannels = spec.getElementsByTagName("dataChannels").item(0);

		ArrayList<Node> nodesToBeRemoved = new ArrayList<Node>();
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element dataChannel = doc.createElement("dataChannel");
			dataChannel.setAttribute("from", "dummy.o1");
			dataChannel.setAttribute("to", ((Element) inputDP2PortMappings.item(i)).getAttribute("to"));
			dataChannels.appendChild(dataChannel);
			nodesToBeRemoved.add(inputDP2PortMappings.item(i));
		}
		for (Node currN : nodesToBeRemoved)
			dataProductsToPorts.removeChild(currN);

		Element inputDP2DummyPortmapping = doc.createElement("inputDP2PortMapping");
		inputDP2DummyPortmapping.setAttribute("from", "dummyDP");
		inputDP2DummyPortmapping.setAttribute("to", "dummy.i1");

		dataProductsToPorts.appendChild(inputDP2DummyPortmapping);

	}

	public static ArrayList<String> getAllInputComponentIds(Element spec) {
		ArrayList<String> result = new ArrayList<String>();
		NodeList inputDP2PortMappings = spec.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) inputDP2PortMappings.item(i);
			String toAttr = currMapping.getAttribute("to");
			String compId = toAttr.substring(0, toAttr.lastIndexOf(".")).trim();
			if (!result.contains(compId))
				result.add(compId);

		}
		return result;
	}

	public static ArrayList<String> getAllDPNamesForThisInputComponent(String componentId, Element spec) {
		ArrayList<String> result = new ArrayList<String>();

		NodeList inputDP2PortMappings = spec.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) inputDP2PortMappings.item(i);
			String toAttr = currMapping.getAttribute("to");
			String toCompId = toAttr.substring(0, toAttr.lastIndexOf(".")).trim();
			if (toCompId.equals(componentId.trim()))
				result.add(currMapping.getAttribute("from").trim());

		}

		return result;
	}

	// public static ArrayList<String> findComponentChain(Element spec, JSONObject schedule, String VMip, ArrayList<String>
	// componentsAlreadyExecuted) {
	// ArrayList<String> chain = new ArrayList<String>();
	// ArrayList<String> inputComponentIds = SWLAnalyzer.getAllInputComponentIds(spec);
	//
	// return chain;
	// }

	public static ArrayList<String> findAllSuccessorsScheduledOnThisVM(Element spec, String componId, String thisVMip, JSONObject schedule)
			throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		HashSet<String> immediateSuccessors = getAllConsumers(componId, spec);

		for (String currSuccessorId : immediateSuccessors) {
			if (schedule.getString(currSuccessorId).trim().equals(thisVMip)) {
				result.add(currSuccessorId);
				result.addAll(findAllSuccessorsScheduledOnThisVM(spec, currSuccessorId, thisVMip, schedule));
			}

		}

		return result;

	}

	public static ArrayList<String> getAllInputCompntsScheduledOnThisVM(HashSet<String> allInputCompnts, String thisVMip, JSONObject schedule)
			throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		for (String inputComp : allInputCompnts)
			if (schedule.getString(inputComp).trim().equals(thisVMip))
				result.add(inputComp);
		return result;
	}

	public static ArrayList<Pair> getInputDPIdsForThisComponent(String componentId, Element experimentSpec, String runID) throws Exception {
		// returns a list of pairs input portID to dataName
		ArrayList<Pair> result = new ArrayList<Pair>();

		Element componentSpec = Repository.getWorkflowSpecification(getComponentName(componentId, experimentSpec));
		for (String currPortID : getWorkflowInputPorts(componentSpec)) {
			// Pair pair = null;

			if (getDataChannelsFromAttribute(experimentSpec, componentId, currPortID) != null) {
				// i.e. the componentId.currPortID is the right end of the data channel:
				// pair =
				for (Pair pair : getPortOnTheLeftEndOfTheDataChannel(componentId, currPortID, experimentSpec))
					result.add(new Pair(currPortID, pair.left + "." + pair.right + "." + runID));

			} else {
				// i.e. the componentId.currPortID is in the inputDP2PortMapping:
				NodeList inputDP2PortMappings = experimentSpec.getElementsByTagName("inputDP2PortMapping");
				for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
					Element currMapping = (Element) inputDP2PortMappings.item(i);
					String toAttr = currMapping.getAttribute("to");
					if (toAttr.contains(componentId + ".") && toAttr.substring(toAttr.indexOf(".") + 1).trim().equals(currPortID)) {
						String fromAttr = currMapping.getAttribute("from");
						result.add(new Pair(currPortID, fromAttr));
					}
				}
			}
		}

		return result;
	}

	public static Document parallelizeComponent(String componentId, String portToParallelizeOn, String wfName,
			HashMap<String, ArrayList<DataProduct>> inputPortID_to_DP) throws Exception {

		Element originalSpec = Repository.getWorkflowSpecification(wfName);

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();
		Node experimentSpec = doc.createElement("experimentSpec");
		doc.appendChild(experimentSpec);

		Element originalExperiment = (Element) originalSpec.getElementsByTagName("experiment").item(0);
		experimentSpec.appendChild(doc.adoptNode(originalExperiment.cloneNode(true)));

		// introduce a list of components:
		DataProduct inputListDP = inputPortID_to_DP.get(portToParallelizeOn).get(0);
		if (!(inputListDP instanceof ListDP)) {
			System.out.println("ERROR: input dp is of type " + inputListDP.getClass().getName() + " instead of ListDP!!!");
			if (Utility.executeInCloud)
				Utility.reportToLoggerNode("ERROR: input dp is of type " + inputListDP.getClass().getName() + " instead of ListDP!!!");
			return null;
		}

		String componentName = getComponentName(componentId, originalSpec);
		int numberOfParallelComponentsToBeCreated = ((ListDP) inputListDP).data.size();
		ArrayList<String> listOfParallelComps = createAListOfUniqueInstanceIds(numberOfParallelComponentsToBeCreated, componentName, originalSpec);
		// System.out.println(listOfParallelComps);

		// register input data products:
		Iterator it = inputPortID_to_DP.keySet().iterator();
		while (it.hasNext()) {
			String inputPortID = (String) it.next();
			if (!inputPortID.equals(portToParallelizeOn)) {
				DataProduct inputDP = inputPortID_to_DP.get(inputPortID).get(0);
				for (String currParallelComponent : listOfParallelComps)
					addInputDP2PortMapping(inputDP.dataName, currParallelComponent, inputPortID, (Element) experimentSpec);
				try {
					Utility.registerDataProduct(inputDP);
				} catch (Exception e) {

				}
			}
			// else {
			// // String dataName =
			// // ListDP singletonList = new ListDP(dataName, list);
			//
			// for (DataProduct currFileDP : ((ListDP) inputPortID_to_DP.get(portToParallelizeOn).get(0)).data) {
			// Element inputDP2PortMapping = doc.createElement("inputDP2PortMapping");
			// inputDP2PortMapping.setAttribute("from", currFileDP.dataName);
			// inputDP2PortMapping.setAttribute("to", value);
			// }
			// }
		}

		// remove data channels connecting to the input ports of the component to parallelize:
		NodeList dataChannels = doc.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			Element dataChannel = (Element) dataChannels.item(i);
			String toAttr = dataChannel.getAttribute("to");
			String to = toAttr.substring(0, toAttr.indexOf("."));
			if (to.trim().equals(componentId))
				dataChannels.item(i).getParentNode().removeChild(dataChannels.item(i));
		}

		// remove inputDP2PortMappings from DPs mapped to input ports of the component to parallelize:
		NodeList inputDP2PortMappings = doc.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element inputDP2PortMapping = (Element) inputDP2PortMappings.item(i);
			String toAttr = inputDP2PortMapping.getAttribute("to");
			String to = toAttr.substring(0, toAttr.indexOf("."));
			if (to.trim().equals(componentId))
				inputDP2PortMappings.item(i).getParentNode().removeChild(inputDP2PortMappings.item(i));
		}

		// create workflowInstance elements:
		// Node workflowGraph = doc.getElementsByTagName("workflowGraph").item(0);
		ArrayList<DataProduct> listOfFileDPs = ((ListDP) inputPortID_to_DP.get(portToParallelizeOn).get(0)).data;
		ListDP listDP = ((ListDP) inputPortID_to_DP.get(portToParallelizeOn).get(0));
		Node dataProductsToPorts = doc.getElementsByTagName("dataProductsToPorts").item(0);
		for (int i = 0; i < listOfParallelComps.size(); i++) {
			String newComponentId = listOfParallelComps.get(i);
			Node workflowInstance = doc.createElement("workflowInstance");
			((Element) workflowInstance).setAttribute("id", newComponentId);
			Node workflow = doc.createElement("workflow");
			workflow.setTextContent(componentName);
			workflowInstance.appendChild(workflow);
			doc.getElementsByTagName("workflowInstances").item(0).appendChild(workflowInstance);

			// add inputDP2PortMapping:
			Element inputDP2PortMapping = doc.createElement("inputDP2PortMapping");
			inputDP2PortMapping.setAttribute("from", listDP.dataName + "." + i);
			inputDP2PortMapping.setAttribute("to", listOfParallelComps.get(i) + "." + portToParallelizeOn);
			dataProductsToPorts.appendChild(inputDP2PortMapping);
		}

		System.out.println(Utility.nodeToString(doc));
		return null;
	}

	private static void addInputDP2PortMapping(String dataName, String componentID, String portID, Element spec) {
		Document doc = spec.getOwnerDocument();
		Node dataProductsToPorts = doc.getElementsByTagName("dataProductsToPorts").item(0);
		Element inputDP2PortMapping = doc.createElement("inputDP2PortMapping");
		inputDP2PortMapping.setAttribute("from", dataName);
		inputDP2PortMapping.setAttribute("to", componentID + "." + portID);
		dataProductsToPorts.appendChild(inputDP2PortMapping);
	}

	private static ArrayList<String> createAListOfUniqueInstanceIds(int howManyToCreate, String componentName, Element originalSpec) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> forbiddenNames = new ArrayList<String>();
		for (int i = 0; i < howManyToCreate; i++) {
			String uniqueName = createUniqueIdForComponent(componentName, originalSpec, forbiddenNames);
			forbiddenNames.add(uniqueName);
			result.add(uniqueName);
		}
		return result;
	}

	private static String createUniqueIdForComponent(String componentName, Element spec, ArrayList<String> idsAlreadyTaken) {
		ArrayList<String> forbiddenNames = new ArrayList<String>();
		if (idsAlreadyTaken != null)
			forbiddenNames.addAll(idsAlreadyTaken);

		int i = 0;
		String newId = componentName + new Integer(i).toString();
		while (forbiddenNames.contains(newId) || getComponentName(newId, spec) != "") {
			i++;
			newId = componentName + new Integer(i).toString();
		}

		return newId;
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.none;
		/*
		 * Document expAddSpec = Utility.readFileAsDocument("testFiles/JUnit/experiments/finalTest1");
		 * 
		 * // Document expAddSpec = Utility.readFileAsDocument("testFiles/JUnit/experiments/Add.swl");
		 * 
		 * //Document squareOfSumSpec = Utility.readFileAsDocument("testFiles/JUnit/experiments/squareOfSumExp");
		 * 
		 * // Utility.registerDataProduct(new IntegerDP(1, "one")); // Utility.registerDataProduct(new IntegerDP(2, "two")); //
		 * Utility.registerDataProduct(new IntegerDP(8, "eight")); // Utility.registerDataProduct(new IntegerDP(3, "three")); //
		 * Utility.registerDataProduct(new IntegerDP(10, "ten"));
		 * 
		 * int numberOfDPsInTheDP = ((JSONArray) WebbenchUtility.getWFandDPMetadata().get("dataProducts")).length(); String runID
		 * = new Integer(numberOfDPsInTheDP).toString();
		 * 
		 * Experiment experimentAdd = new Experiment(expAddSpec.getDocumentElement()); experimentAdd.run(runID, true);
		 * 
		 * // System.out.println("=================="); // for (String outputPortID :
		 * experimentAdd.rootWorkflow.outputPortID_to_DP.keySet()) { // System.out.println(outputPortID + ":" + ((IntegerDP) //
		 * experimentAdd.rootWorkflow.outputPortID_to_DP.get(outputPortID)).data); // }
		 */
		Document spec = Utility.readFileAsDocument("testFiles/JUnit/experiments/test16Types");
		System.out.println(isWebservice(spec.getDocumentElement()));

	}

}
