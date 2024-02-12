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
public class MakeList extends Workflow {
	int minimumNumberOfInputs = -1;

	public MakeList(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
//		System.out.println("running workflow " + instanceId);
//		Utility.reportToLoggerNode(Utility.myIP + "rr__rrunning workflow " + instanceId);
		if (!readyToExecute())
			return;

		ArrayList<DataProduct> inputDPs_i2 = inputPortID_to_DP.get("i1");

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			String dataName = instanceId + ".o1." + runID;
			outputPortID_to_DP.put("o1", new ListDP(dataName, inputDPs_i2));
//			System.out.println(instanceId + " producing result (files): " + dataName);
//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>>>>>>>>>>>>>>>> " + instanceId + " producing result (file): " + dataName);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public boolean readyToExecute() {
		
		try {
//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>> " + instanceId + " ready to execute call, inputPortID_to_DP keyset: " + inputPortID_to_DP.keySet());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") == null || inputPortID_to_DP.get("i2") == null || !(inputPortID_to_DP.get("i2").get(0) instanceof IntDP))
			return false;

		int requiredSize = ((IntDP) inputPortID_to_DP.get("i2").get(0)).data;

		if (inputPortID_to_DP.get("i1").size() != requiredSize)
			return false;

		try {
//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>> " + instanceId + " eady to execute call TRUEEEE");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
