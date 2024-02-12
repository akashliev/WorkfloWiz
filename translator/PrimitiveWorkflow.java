package translator;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import utility.SSHClient;
import utility.Utility;
import utility.WSClient5;
import dataProduct.DataProduct;
import dataProduct.IntDP;
import dataProduct.XmlDP;

/**
 * This class represents primitive workflow (i.e. ws-based or script-based).
 * 
 * @author Andrey Kashlev
 * 
 */
public class PrimitiveWorkflow extends Workflow {
	// String taskType = null;

	public PrimitiveWorkflow(String instanceId, Element workflowSpec) {
		thisWorkflowSpec = workflowSpec;
		this.instanceId = instanceId;
		executionStatus = null;
		// taskType = SWLAnalyzer.getTaskType(workflowSpec);
	}

	@Override
	public boolean readyToExecute() {
		if (SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec).size() != inputPortID_to_DP.size()) {
			Utility.appendToLog("ERROR: not all inputs are available for workflow: " + SWLAnalyzer.getWorkflowName(thisWorkflowSpec));
			System.out.println("# input ports: " + SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec).size());
			System.out.println("inputPortID_to_DP.size(): " + inputPortID_to_DP.size());
			return false;
		}
		return true;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		if (SWLAnalyzer.getTaskType(thisWorkflowSpec).equals("LinuxApplication")) {
			System.out.println("running script-based workflow " + instanceId);
			String executableFullName = SWLAnalyzer.getExecutableFullName(thisWorkflowSpec);
			String hostname = SWLAnalyzer.getHostName(thisWorkflowSpec);
			String username = SWLAnalyzer.getUserName(thisWorkflowSpec);
			String password = SWLAnalyzer.getPassword(thisWorkflowSpec);
			String argument = "";
			for (String portID : SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec)) {
				DataProduct currDP = inputPortID_to_DP.get(portID).get(0);
				if (currDP instanceof IntDP) {
					argument = argument + dataProduct2String(currDP) + " ";
				}
			}
			String command = (executableFullName + " " + argument).trim();
			// System.out.println("command: " + command);
			// System.out.println("hostname: |" + hostname + "|");
			// System.out.println("username: |" + username + "|");
			// System.out.println("password: |" + password + "|");

			String result = SSHClient.executeCommandOnRemoteHost(hostname, username, password, command);
			System.out.println("script-based pworkflow p" + instanceId + " producing result: " + result);

			String outputPortID = SWLAnalyzer.getWorkflowOutputPorts(thisWorkflowSpec).iterator().next();
			DataProduct resultDP = string2DataProduct(result, instanceId, outputPortID, runID);
			outputPortID_to_DP.put(outputPortID, resultDP);

			if (registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
		}
		if (SWLAnalyzer.getTaskType(thisWorkflowSpec).equals("WebService")) {
			System.out.println("running ws-based workflow " + instanceId);
			String wsdl = SWLAnalyzer.getWSDL(thisWorkflowSpec);
			// String serviceName = SWLAnalyzer.getServiceName(thisWorkflowSpec);
			String opName = SWLAnalyzer.getOperationName(thisWorkflowSpec);
			// ArrayList<Object> wsArguments = getArgumentsForWebService();
			// ArrayList<Class> returnTypes = getReturnTypesForWebService();

			DataProduct inputDP = inputPortID_to_DP.get(SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec).get(0).trim()).get(0);

			if (!(inputDP instanceof XmlDP)) {
				System.out.println("ERROR: Web Service attempting to process non-xml data");
				return;
			}

			Element inputXML = ((XmlDP) inputDP).data;
			// System.out.println("inputXML: \n" + inputXML);
			Document result = WSClient5.callWebService(wsdl, opName, inputXML);
			System.out.println("WS-based pworkflow " + instanceId + " producing result: " + Utility.nodeToString(result));

			String outputPortID = SWLAnalyzer.getWorkflowOutputPorts(thisWorkflowSpec).iterator().next();
			DataProduct resultDP = new XmlDP(result.getDocumentElement(), instanceId + "." + outputPortID + "." + runID);
			// string2DataProduct(result.toString(), instanceId, outputPortID, runID);
			outputPortID_to_DP.put(outputPortID, resultDP);

			if (registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
		}
	}

	private String dataProduct2String(DataProduct dp) {
		String result = "";
		if (dp instanceof IntDP)
			result = new Integer(((IntDP) dp).data).toString();
		return result;
	}

	@Override
	public void setFinishedStatus() {
		boolean allOutputDataProductsSuccessfullyComputed = true;
		for (String portID : SWLAnalyzer.getWorkflowOutputPorts(thisWorkflowSpec)) {
			if (outputPortID_to_DP.get(portID) == null)
				allOutputDataProductsSuccessfullyComputed = false;
		}
		if (allOutputDataProductsSuccessfullyComputed)
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

	public DataProduct string2DataProduct(String dpString, String instanceId, String portID, String runID) throws Exception {
		DataProduct result = null;
		String outputPortID = SWLAnalyzer.getWorkflowOutputPorts(thisWorkflowSpec).iterator().next();
		String type = SWLAnalyzer.getPortType(thisWorkflowSpec, outputPortID);
		// System.out.println("type: " + type);
		// System.out.println("dpString: |" + dpString + "|");
		if (type.equals("Int")) {
			result = new IntDP(new Integer(dpString), instanceId + "." + portID + "." + runID);
			return result;
		}
		return result;
	}

	private ArrayList<Object> getArgumentsForWebService() {
		ArrayList<Object> wsArguments = new ArrayList<Object>();
		for (String portID : SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec)) {
			wsArguments.add(dataProduct2Object(inputPortID_to_DP.get(portID).get(0)));
		}
		return wsArguments;
	}

	private Object dataProduct2Object(DataProduct dp) {
		Object result = null;
		if (dp instanceof IntDP)
			result = new Integer(((IntDP) dp).data);
		if (dp instanceof XmlDP)
			return translateXmlIntoABean(((XmlDP) dp).data);
		return result;
	}

	private static Object translateXmlIntoABean(Element data) {
		return null;
	}

	private ArrayList<Class> getReturnTypesForWebService() throws Exception {
		ArrayList<Class> returnTypes = new ArrayList<Class>();
		for (String portID : SWLAnalyzer.getWorkflowOutputPorts(thisWorkflowSpec)) {
			returnTypes.add(outputPortSWLType2WSOutputType(SWLAnalyzer.getPortType(thisWorkflowSpec, portID)));
		}
		return returnTypes;
	}

	private Class outputPortSWLType2WSOutputType(String type) {
		Class result = null;
		if (type.equals("Int"))
			result = Integer.class;
		return result;
	}

	private ArrayList<DataProduct> WSOutput2DataProduct(Object result, String instanceId, String portID, String runID) {
		ArrayList<DataProduct> resultDPs = new ArrayList<DataProduct>();
		DataProduct resultDP = null;
		if (result instanceof Integer)
			resultDPs.add(new IntDP((Integer) result, instanceId + "." + portID + "." + runID));

		return resultDPs;
	}
}
