package translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONObject;
import org.w3c.dom.Element;

import repository.Repository;
import utility.ParallelUtility;
import utility.Utility;
import crm2.CRM;
import dataProduct.DataProduct;
import dataProduct.FileDP;

/**
 * This is a key class in the workflow engine. It provides the functionality to run graph-based workflow. It supports fully
 * composable graph-based workflows - i.e. each component of the graph-based workflow can itself be any kind of workflow, even
 * another graph-based workflow.
 * 
 * @author Andrey Kashlev
 * 
 */
public class GraphBasedWorkflow extends Workflow {
	// maps instance IDs to executable workflow objects:
	public HashMap<String, Workflow> children = new HashMap<String, Workflow>();
	public HashSet<String> componentsReadyToBeExecuted = null;
	private boolean passedInputDataToComponents = false;

	private boolean isDerivedFromExperiment = false;
	public boolean executeInCloud = false;
	public JSONObject schedule = null;
	ArrayList<String> componentsToBeRunOnThisVM = null;
	String myIP = null;

	// to avoid sending data products to same virtual machines multiple times, we keep track of what we had sent and where:
	public HashMap<String, ArrayList<String>> DP_to_VMip = null;

	public GraphBasedWorkflow(String instanceId, Element workflowSpec) {
		thisWorkflowSpec = workflowSpec;
		this.instanceId = instanceId;
		executionStatus = null;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		// Utility.reportToLoggerNode(myIP + " calling execute - from sshhhhhhhhhhh");
		System.out.println(myIP + " calling execute");

		if (executeInCloud) {
			executeInTheCloud(runID, componentsToBeRunOnThisVM);
			return;
		}
		// Utility.appendToLog("executing graph-based workflow " + instanceId);

		giveAvailabledDataProductsToConsumers(registerIntermediateDPs);
		updateListOfComponentsThatAreReady();

		while (!componentsReadyToBeExecuted.isEmpty()) {
			runComponentsThatAreReady(runID, registerIntermediateDPs);
			giveAvailabledDataProductsToConsumers(registerIntermediateDPs);
			updateListOfComponentsThatAreReady();
		}
	}

	public void executeInTheCloud(String runID, ArrayList<String> componentsToExecute) throws Exception {
		Utility.reportToLoggerNode(myIP + ": (before giveDPs) executeInTheCloud call for " + componentsToExecute);
		giveDPsToConsumersInTheCloud(componentsToExecute, runID);
		Utility.reportToLoggerNode(myIP + ": (aaaaaaaaaaaaaaaaaafter giveDPs) executeInTheCloud call for " + componentsToExecute);
		for (String componentId : componentsToExecute) {
			runComponentInCloud(runID, componentId);
			Utility.reportToLoggerNode(myIP + ": finished runComponentInCloud for " + componentId);
		}
	}

	private void runComponentInCloud(String runID, String componentId) throws Exception {
		Utility.reportToLoggerNode(myIP + ": runComponentInCloud call for " + componentId);
		System.out.println("calling run on this component: " + children.get(componentId).instanceId);
		children.get(componentId).run(runID, false);

		String name = SWLAnalyzer.getWorkflowName(thisWorkflowSpec);
		if (name.contains("wfFromExperiment"))
			name = name.replace("wfFromExperiment", "").trim();

		if (!name.equals(Utility.runID_to_wfName.get(runID)))
			return;

		// find all consumers of this component scheduled on the same VM or on remote VMs:
		ArrayList<String> allConsumers = new ArrayList<String>(SWLAnalyzer.getAllConsumers(componentId, thisWorkflowSpec));

		for (String currConsumerId : allConsumers)
			if (!schedule.getString(currConsumerId).trim().equals(myIP))
				giveDPToLocalOrRemoteConsumer(currConsumerId, runID);

		List threads = new ArrayList();
		for (String currVMip : CRM.listOfVMs) {
			ArrayList<String> consumersScheduledOnCurrVM = findConsumersScheduledOnAGivenDestVM(componentId, currVMip, allConsumers);

			if (consumersScheduledOnCurrVM.size() > 0) {
				// if (!currVMip.equals(myIP)) {
				Utility.reportToLoggerNode(myIP + ": " + componentId + " is requesting to run the following consumers on the VMs " + currVMip
						+ ":\n" + consumersScheduledOnCurrVM.toString());
				// send request to another VM to run component
				ParallelUtility pu = new ParallelUtility(name, currVMip, consumersScheduledOnCurrVM, schedule, runID);
				pu.start();
				threads.add(pu);
			}
		}

		for (int i = 0; i < threads.size(); i++)
			((Thread) threads.get(i)).join();

	}

	public ArrayList<String> findConsumersScheduledOnAGivenDestVM(String producerId, String destVM,
			ArrayList<String> allConsumersScheduledOnOtherVMs) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		for (String consumerScheduledOnOtherVM : allConsumersScheduledOnOtherVMs)
			if (schedule.get(consumerScheduledOnOtherVM).equals(destVM))
				result.add(consumerScheduledOnOtherVM);
		return result;
	}

	public void giveDPsToConsumersInTheCloud(ArrayList<String> componentIdsToBeExecuted, String runID) throws Exception {
		for (String componentId : componentIdsToBeExecuted)
			giveDPToLocalOrRemoteConsumer(componentId, runID);
	}

	public void giveDPToLocalOrRemoteConsumer2(String consumerComponentId, String runID) throws Exception {
		Element spec = thisWorkflowSpec;
		String destVMip = schedule.getString(consumerComponentId).trim();
		if (SWLAnalyzer.getWorkflowName(thisWorkflowSpec).contains("wfFromExperiment"))
			spec = Repository.getWorkflowSpecification(SWLAnalyzer.getWorkflowName(thisWorkflowSpec).replace("wfFromExperiment", "").trim());
		for (Pair inputPortId_dataName : SWLAnalyzer.getInputDPIdsForThisComponent(consumerComponentId, spec, runID)) {
			if (destVMip.equals(myIP)) {
				DataProduct dp = null;
				ArrayList<String> producer_fromAttrs = SWLAnalyzer.getDataChannelsFromAttribute(spec, consumerComponentId,
						inputPortId_dataName.left);
				for (String producer_fromAttr : producer_fromAttrs) {
					String producerComponId = producer_fromAttr.substring(0, producer_fromAttr.indexOf("."));
					String producerOutputPortId = producer_fromAttr.substring(producer_fromAttr.indexOf(".") + 1, producer_fromAttr.length());
					dp = children.get(producerComponId).outputPortID_to_DP.get(producerOutputPortId);

					ArrayList<DataProduct> dps_mapped_to_this_port = children.get(consumerComponentId).inputPortID_to_DP
							.get(inputPortId_dataName.left);
					if (dps_mapped_to_this_port == null) {
						dps_mapped_to_this_port = new ArrayList<DataProduct>();
						children.get(consumerComponentId).inputPortID_to_DP.put(inputPortId_dataName.left, dps_mapped_to_this_port);
					}
					dps_mapped_to_this_port.add(dp);
				}
			} else{
				
			}
		}
	}

	public void giveDPToLocalOrRemoteConsumer(String consumerComponentId, String runID) throws Exception {
		Element spec = thisWorkflowSpec;
		String destVMip = schedule.getString(consumerComponentId).trim();
		if (SWLAnalyzer.getWorkflowName(thisWorkflowSpec).contains("wfFromExperiment"))
			spec = Repository.getWorkflowSpecification(SWLAnalyzer.getWorkflowName(thisWorkflowSpec).replace("wfFromExperiment", "").trim());
		for (Pair inputPortId_dataName : SWLAnalyzer.getInputDPIdsForThisComponent(consumerComponentId, spec, runID)) {
			// Utility.reportToLoggerNode("checking if file exists: " + inputPortId_dataName.right);
			DataProduct dp = Utility.getDataProduct(inputPortId_dataName.right);
			// Utility.reportToLoggerNode("true");

			// if it is scheduled on the same VM, put it in the input map for componentId, else, send it over to the right
			// VM
			// if component has input ports that are on the right side of inputDP2PortMappings:
			if (destVMip.equals(myIP)) {
				Utility.reportToLoggerNode("giving dp named " + inputPortId_dataName.right + " locally to : " + consumerComponentId + "."
						+ inputPortId_dataName.left);

				// if producer component ran on another VM, put it in the inputPortID_to_DP map of consumer component:

				ArrayList<DataProduct> dps_mapped_to_this_port = children.get(consumerComponentId).inputPortID_to_DP.get(inputPortId_dataName.left);
				if (dps_mapped_to_this_port == null) {
					dps_mapped_to_this_port = new ArrayList<DataProduct>();
					children.get(consumerComponentId).inputPortID_to_DP.put(inputPortId_dataName.left, dps_mapped_to_this_port);
				}
				dps_mapped_to_this_port.add(dp);
				// children.get(componentId).inputPortID_to_DP.put(inputPortId_dataName.left, dp);
			} else {
				// Utility.reportToLoggerNode("trying to send " + inputPortId_dataName.right + " to " + destVMip +
				// ", DP_to_VMip: " + DP_to_VMip);
				// Utility.reportToLoggerNode("DP_to_VMip == null: " + (DP_to_VMip == null)
				// + "\n!DP_to_VMip.containsKey(inputPortId_dataName.right): " +
				// (!DP_to_VMip.containsKey(inputPortId_dataName.right)));
				// if () {
				// Utility.reportToLoggerNode("calling Utility.sendFileToVM");
				if (Utility.getDataProduct(inputPortId_dataName.right) instanceof FileDP) {
					if (Utility.fileExists(inputPortId_dataName.right)
							&& (DP_to_VMip == null || !DP_to_VMip.containsKey(inputPortId_dataName.right) || !DP_to_VMip.get(
									inputPortId_dataName.right).contains(destVMip)))
						Utility.sendFileToVM(inputPortId_dataName.right, myIP, destVMip);
					// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					else if (!Utility.fileExists(inputPortId_dataName.right))
						Utility.reportToLoggerNode("file does not exist!!!!!!!!!!!: " + inputPortId_dataName.right);
				} else {
					// right code for sending DPL-based dp to another VM
				}

				if (DP_to_VMip == null)
					DP_to_VMip = new HashMap<String, ArrayList<String>>();
				ArrayList<String> VMsWhereThisDPsits = DP_to_VMip.get(inputPortId_dataName.right);
				if (VMsWhereThisDPsits == null) {
					VMsWhereThisDPsits = new ArrayList<String>();
					DP_to_VMip.put(inputPortId_dataName.right, VMsWhereThisDPsits);
				}
				VMsWhereThisDPsits.add(destVMip);
				// DP_to_VMip.put(inputPortId_dataName.right, destVMip);
			}

		}
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

	private void updateListOfComponentsThatAreReady() throws Exception {
		if (componentsReadyToBeExecuted == null) {
			componentsReadyToBeExecuted = getInitialComponents();
		} else {
			HashSet<String> oldListOfComponentsReadyToExecute = componentsReadyToBeExecuted;
			componentsReadyToBeExecuted = new HashSet<String>();
			HashSet<String> newListOfComponentsReadyToExecute = new HashSet<String>();
			for (String componentAlreadyExecuted : oldListOfComponentsReadyToExecute) {
				if (children.get(componentAlreadyExecuted).executionStatus.equals(ExecutionStatus.finishedOK))
					for (String componentConsumer : SWLAnalyzer.getAllConsumers(componentAlreadyExecuted, thisWorkflowSpec)) {
						if (children.get(componentConsumer).readyToExecute()) {
							newListOfComponentsReadyToExecute.add(componentConsumer);
						}
					}
			}
			componentsReadyToBeExecuted = newListOfComponentsReadyToExecute;
		}

	}

	// returns workflows whose inputs are part of the interface, that is who already have all input data available:
	private HashSet<String> getInitialComponents() throws Exception {
		HashSet<String> initialComponents = new HashSet<String>();
		initialComponents = SWLAnalyzer.getComponentsWithAllInputDataAvailable(thisWorkflowSpec);
		HashSet<String> componentsToRemove = new HashSet<String>();
		for (String componentId : initialComponents) {
			if (!children.get(componentId).readyToExecute()) {
				// initialComponents.remove(componentId);
				componentsToRemove.add(componentId);
			}
		}
		initialComponents.removeAll(componentsToRemove);
		return initialComponents;
	}

	private void giveAvailabledDataProductsToConsumers(boolean registerIntermediateDPs) throws Exception {
		if (!passedInputDataToComponents) {
			passedInputDataToComponents = true;
			// assign input data products to components' inputs:
			for (String portID : SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec)) {
				ArrayList<Pair> instanceIDtoPortID = SWLAnalyzer.getConsumersOfThisInputPort(portID, thisWorkflowSpec);
				for (Pair pair : instanceIDtoPortID) {

					String instanceIdInPair = pair.left;
					String portIDInPair = pair.right;

					Workflow componentWF = children.get(instanceIdInPair.trim());

					// componentWF.inputPortID_to_DP.put(portIDInPair, inputPortID_to_DP.get(portID));

					ArrayList<DataProduct> dps_mapped_to_this_port = null;

					if (componentWF.inputPortID_to_DP.get(portIDInPair) == null) {
						dps_mapped_to_this_port = new ArrayList<DataProduct>();
						dps_mapped_to_this_port.addAll(inputPortID_to_DP.get(portID));
						componentWF.inputPortID_to_DP.put(portIDInPair, dps_mapped_to_this_port);
					} else {
						dps_mapped_to_this_port = componentWF.inputPortID_to_DP.get(portIDInPair);
						dps_mapped_to_this_port.addAll(inputPortID_to_DP.get(portID));
					}

					// dps_mapped_to_this_port.addAll(inputPortID_to_DP.get(portID));

					// System.out.println("now it is : " + componentWF.inputPortID_to_DP.get(portIDInPair).size());
				}
			}
		} else {
			HashSet<String> componentsAlreadyExecuted = componentsReadyToBeExecuted;
			for (String componentProducerId : componentsAlreadyExecuted) {
				for (String outputPortID : SWLAnalyzer.getWorkflowOutputPorts(children.get(componentProducerId).thisWorkflowSpec)) {
					DataProduct newlyComputedDP = children.get(componentProducerId).outputPortID_to_DP.get(outputPortID);
					ArrayList<Pair> instanceIDtoPortID = SWLAnalyzer.getAllPortsOnTheOtherEndOfEachDataChannel(componentProducerId, outputPortID,
							thisWorkflowSpec);
					for (Pair consumerInstanceIdPortIdPair : instanceIDtoPortID) {
						Workflow consumerWF = children.get(consumerInstanceIdPortIdPair.left);
						String consumerWF_portID = consumerInstanceIdPortIdPair.right;

						ArrayList<DataProduct> dps_mapped_to_this_port = null;
						if (consumerWF.inputPortID_to_DP.get(consumerWF_portID) == null) {
							dps_mapped_to_this_port = new ArrayList<DataProduct>();
							dps_mapped_to_this_port.add(newlyComputedDP);
							consumerWF.inputPortID_to_DP.put(consumerWF_portID, dps_mapped_to_this_port);
						} else {
							dps_mapped_to_this_port = consumerWF.inputPortID_to_DP.get(consumerWF_portID);
							dps_mapped_to_this_port.add(newlyComputedDP);
						}

						// ArrayList<DataProduct> dps_mapped_to_this_port =
						// children.get(consumerInstanceIdPortIdPair.left).inputPortID_to_DP
						// .get(consumerInstanceIdPortIdPair.right);
						// if (dps_mapped_to_this_port == null)
						// dps_mapped_to_this_port = new ArrayList<DataProduct>();
						// dps_mapped_to_this_port.add(newlyComputedDP);
						// children.get(consumerInstanceIdPortIdPair.left).inputPortID_to_DP.put(consumerInstanceIdPortIdPair.right,
						// newlyComputedDP);
					}

					for (String portID : SWLAnalyzer.getConsumersOfThisOutputPort(componentProducerId, outputPortID, thisWorkflowSpec)) {
						outputPortID_to_DP.put(portID, newlyComputedDP);
					}
				}
			}
		}
	}

	private void runComponentsThatAreReady(String runID, boolean registerIntermediateDPs) throws Exception {
		// System.out.println("ccccccccccccccomponents that are ready: " + componentsReadyToBeExecuted);
		for (String componentId : componentsReadyToBeExecuted) {
			children.get(componentId).run(runID, registerIntermediateDPs);
		}
	}

	@Override
	public boolean readyToExecute() {
		if (executeInCloud)
			return true;
		if (SWLAnalyzer.getWorkflowInputPorts(thisWorkflowSpec).size() != inputPortID_to_DP.size()) {
			// Utility.appendToLog("ERROR: not all inputs are available for workflow: " +
			// SWLAnalyzer.getWorkflowName(thisWorkflowSpec));
			return false;
		}
		return true;
	}

}
