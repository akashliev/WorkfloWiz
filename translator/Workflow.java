package translator;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;

import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dataProduct.IntDP;
import dataProduct.ListDP;

/**
 * This abstract class at the top of inheritance hierarchy of workflows. All types of worklflows (e.g. graph-based, primitive etc)
 * are its children, i.e. extend this class.
 * 
 * @author Andrey Kashlev
 * 
 */
public abstract class Workflow {
	public Element thisWorkflowSpec = null;
	public String instanceId = "undefinedInstanceID";
	public ExecutionStatus executionStatus = null;
	public HashMap<String, ArrayList<DataProduct>> inputPortID_to_DP = new HashMap<String, ArrayList<DataProduct>>();
	public HashMap<String, DataProduct> outputPortID_to_DP = new HashMap<String, DataProduct>();

	public void run(String runID, boolean registerIntermediateDPs) throws Exception {
		if (!readyToExecute()) {
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " not ready!!!!!!!!!!!!!!!!!");
			return;
		}
		executionStatus = ExecutionStatus.inProcessOfExecution;
		execute(runID, registerIntermediateDPs);
		setFinishedStatus();
		// if (executionStatus == ExecutionStatus.finishedOK && registerIntermediateDPs) {
		// for (DataProduct dp : outputPortID_to_DP.values())
		// Utility.registerDataProduct(dp);
		//
		// }
	}

	public abstract boolean readyToExecute();

	public abstract void execute(String runID, boolean registerIntermediateDPs) throws Exception;

	public abstract void setFinishedStatus();

	protected boolean portConsumesASingleListDP(String inputPortID) {
		if (inputPortID_to_DP == null || inputPortID_to_DP.get(inputPortID) == null)
			return false;

		if (inputPortID_to_DP.get(inputPortID).get(0) instanceof ListDP && inputPortID_to_DP.get(inputPortID).size() == 1)
			return true;
		return false;
	}

	protected boolean portConsumesMultipleDPs(String inputPortID) {
		if (inputPortID_to_DP.get(inputPortID).size() > 1)
			return true;
		return false;
	}

	protected boolean portPosessesRequiredNumberOfInputFiles(String inputPortID) {
		ArrayList<FileDP> fileDPS = new ArrayList<FileDP>();
		int minimumNumberOfFiles = -1;
		// if (portConsumesMultipleDPs(inputPortID)) {
		// for (DataProduct dp : inputPortID_to_DP.get(inputPortID))
		// if (dp instanceof FileDP)
		// fileDPS.add((FileDP) dp);
		// else if (dp instanceof IntDP)
		// minimumNumberOfFiles = ((IntDP) dp).data;
		// } else
		if (portConsumesASingleListDP(inputPortID)) {
			for (DataProduct dp : ((ListDP) inputPortID_to_DP.get(inputPortID).get(0)).data)
				if (dp instanceof FileDP)
					fileDPS.add((FileDP) dp);
				else if (dp instanceof IntDP)
					minimumNumberOfFiles = ((IntDP) dp).data;
		}

		if (minimumNumberOfFiles != -1 && fileDPS.size() == minimumNumberOfFiles)
			return true;

		return false;
	}
}
