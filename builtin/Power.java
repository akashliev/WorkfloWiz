package builtin;

import java.util.HashMap;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.IntDP;

/**
 * Builtin workflow that raises data at port i1 to the power equal to number supplied at port i1
 * 
 * @author Andrey Kashlev
 *
 */
public class Power extends Workflow {
	public Power(String instanceId) {
		this.instanceId = instanceId;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		
		Iterator it = inputPortID_to_DP.values().iterator();
		Integer a = null;
		Integer b = null;
		if (inputPortID_to_DP.get("i1") instanceof IntDP)
			a = ((IntDP) inputPortID_to_DP.get("i1")).data;
		if (inputPortID_to_DP.get("i2") instanceof IntDP)
			b = ((IntDP) inputPortID_to_DP.get("i2")).data;

		Integer c = null;
		try {
			c = (int) Math.pow(a, b);
			DataProduct resultDP = new IntDP(c, instanceId + ".o1." + runID);
			outputPortID_to_DP.put("o1", resultDP);
			if(registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
			System.out.println(instanceId + " producing result: " + c);
		} catch (Exception e) {
		}

	}

	@Override
	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 2) {
//			Utility.appendToLog("ERROR, readyToExecute: cannot run Power workflow (" 
//					+ instanceId + "),  number of inputs != 2");
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
