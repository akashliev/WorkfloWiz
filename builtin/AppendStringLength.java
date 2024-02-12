package builtin;

import java.util.HashMap;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.*;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class AppendStringLength extends Workflow {
	public AppendStringLength(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		Iterator it = inputPortID_to_DP.values().iterator();
		String a = null;

		if (inputPortID_to_DP.get("i1") instanceof StringDP)
			a = ((StringDP) inputPortID_to_DP.get("i1")).data;

		executionStatus = ExecutionStatus.inProcessOfExecution;
		String c = null;

		try {
			c = a + new Integer(a.length() + 1).toString();

			DataProduct resultDP = new StringDP(c, instanceId + ".o1." + runID);
			outputPortID_to_DP.put("o1", resultDP);
			if (registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
			System.out.println(instanceId + " producing result: " + c);
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 1) {
			// Utility.appendToLog("cannot run Add workflow (" + instanceId + "), number of inputs != 2");
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof StringDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
