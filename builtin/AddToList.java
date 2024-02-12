package builtin;

import java.util.ArrayList;

import translator.ExecutionStatus;
import translator.Workflow;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dataProduct.IntDP;
import dataProduct.ListDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class AddToList extends Workflow {
	int minimumNumberOfInputs = -1;

	public AddToList(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		ListDP listDP = (ListDP) inputPortID_to_DP.get("i1").get(0);
		IntDP intDP = (IntDP) inputPortID_to_DP.get("i2").get(0);

		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			ArrayList<DataProduct> outputList = new ArrayList<DataProduct>();
			outputList.addAll(listDP.data);
			outputList.add(intDP);
			ListDP outputListDP = new ListDP(instanceId + ".o1." + runID, outputList);

			outputPortID_to_DP.put("o1", outputListDP);
			outputPortID_to_DP.put("o2", new IntDP(outputListDP.data.size(), instanceId + ".o2." + runID));
			System.out.println(instanceId + " finished, produced list of size " + outputListDP.data.size());

		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {

		if (inputPortID_to_DP == null)
			return false;

		if (!(inputPortID_to_DP.get("i1").get(0) instanceof ListDP))
			return false;

		if (inputPortID_to_DP.get("i2") == null)
			return false;

		if (!(inputPortID_to_DP.get("i2").get(0) instanceof IntDP))
			return false;

		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof ListDP) && (outputPortID_to_DP.get("o2") instanceof IntDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}
}
