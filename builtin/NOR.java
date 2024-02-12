package builtin;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.BooleanDP;
import dataProduct.DataProduct;
import dataProduct.IntDP;

public class NOR extends Workflow {

	public NOR(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 2) {
			return false;
		}
		return true;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		
		boolean a = false;
		boolean b = false;
		if (inputPortID_to_DP.get("i1") instanceof BooleanDP)
			a = ((BooleanDP) inputPortID_to_DP.get("i1")).data;
		if (inputPortID_to_DP.get("i2") instanceof BooleanDP)
			b = ((BooleanDP) inputPortID_to_DP.get("i2")).data;

		executionStatus = ExecutionStatus.inProcessOfExecution;
		boolean c = false;

		try {
			if(a == false && b == false)
				c = true;
			else
				c = false;

			DataProduct resultDP = new BooleanDP(c, instanceId + ".o1." + runID);
			outputPortID_to_DP.put("o1", resultDP);
			if(registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
			System.out.println(instanceId + " producing result: " + c);
		} catch (Exception e) {
		}

	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof BooleanDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;

	}

}
