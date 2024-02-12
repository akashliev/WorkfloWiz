package builtin;

import java.util.HashMap;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.DoubleDP;

/**
 * Builtin workflow
 * @author Andrey Kashlev
 *
 */
public class DivideDouble extends Workflow {
	public DivideDouble(String instanceId) {
		this.instanceId = instanceId;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);

		Iterator it = inputPortID_to_DP.values().iterator();
		Double a = null;
		Double b = null;
		if (inputPortID_to_DP.get("i1") instanceof DoubleDP)
			a = ((DoubleDP) inputPortID_to_DP.get("i1")).data;
		if (inputPortID_to_DP.get("i2") instanceof DoubleDP)
			b = ((DoubleDP) inputPortID_to_DP.get("i2")).data;

		Double c = null;

		try {
			c = a / b;
			DataProduct resultDP = new DoubleDP(c, instanceId + ".o1." + runID);
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
//			Utility.appendToLog("ERROR, readyToExecute: cannot run Divide workflow (" 
//					+ instanceId + "),  number of inputs != 2");
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof DoubleDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;

	}

}
