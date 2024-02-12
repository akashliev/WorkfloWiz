package builtin;

import java.util.HashMap;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.IntDP;

/**
 * Builtin workflow
 * @author Andrey Kashlev
 *
 */
public class Increment extends Workflow {
	public Increment(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		Iterator it = inputPortID_to_DP.values().iterator();
		Integer a = null;

		if (inputPortID_to_DP.get("i1") instanceof IntDP)
			a = ((IntDP) inputPortID_to_DP.get("i1")).data;

		executionStatus = ExecutionStatus.inProcessOfExecution;
		Integer c = null;

		try {
			c = a + 1;
			DataProduct resultDP = new IntDP(c, instanceId + ".o1." + runID);
			outputPortID_to_DP.put("o1", resultDP);
			if(registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
			System.out.println(instanceId + " producing result: " + c);
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 1) {
			//Utility.appendToLog("cannot run Add workflow (" + instanceId + "), number of inputs != 2");
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof IntDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
