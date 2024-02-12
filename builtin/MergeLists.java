package builtin;

import java.util.ArrayList;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
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
public class MergeLists extends Workflow {
	int minimumNumberOfInputs = -1;

	public MergeLists(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
//		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		ArrayList<DataProduct> inputDPs_i2 = inputPortID_to_DP.get("i1");

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			ArrayList<DataProduct> outputList = new ArrayList<DataProduct>();
			for (DataProduct dp : inputPortID_to_DP.get("i1"))
				if (dp instanceof ListDP)
					outputList.addAll(((ListDP) dp).data);

			String dataName = instanceId + ".o1." + runID;
			outputPortID_to_DP.put("o1", new ListDP(dataName, outputList));
//			System.out.println(instanceId + " producing result (files): " + dataName);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public boolean readyToExecute() {

		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") == null || inputPortID_to_DP.get("i2") == null || !(inputPortID_to_DP.get("i2").get(0) instanceof IntDP))
			return false;

		int requiredSize = ((IntDP) inputPortID_to_DP.get("i2").get(0)).data;

		if (inputPortID_to_DP.get("i1").size() != requiredSize)
			return false;

		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && outputPortID_to_DP.get("o1") instanceof ListDP)
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
