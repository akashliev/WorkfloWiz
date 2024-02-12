package builtin;

import java.util.ArrayList;
import java.util.Arrays;

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
public class mBgModel extends Workflow {
	int minimumNumberOfInputs = -1;

	public mBgModel(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		FileDP input_images_tbl_file = null;
		FileDP inputFitsFile = null;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP)
			input_images_tbl_file = (FileDP) inputPortID_to_DP.get("i1").get(0);

		if (inputPortID_to_DP.get("i2").get(0) instanceof FileDP)
			inputFitsFile = (FileDP) inputPortID_to_DP.get("i2").get(0);

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {

			String cmd = "cd " + Utility.pathToFileDPsFolder + "; mBgModel " + input_images_tbl_file.getFileName() + " "
					+ inputFitsFile.getFileName() + " " + instanceId + ".o1." + runID;
			// ".o1." + runID;
//			System.out.println("aboutttt to run:\n" + cmd);
			Utility.executeShellCommand(cmd);

			FileDP outputCorrectionsTblFileDP = new FileDP(instanceId + ".o1." + runID);
			outputCorrectionsTblFileDP.setFileName(instanceId + ".o1." + runID);

			outputPortID_to_DP.put("o1", outputCorrectionsTblFileDP);
//			System.out.println(instanceId + " producing result (files): " + outputCorrectionsTblFileDP.getFileName());

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public boolean readyToExecute() {

		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") != null && inputPortID_to_DP.get("i1").get(0) instanceof FileDP && inputPortID_to_DP.get("i2") != null
				&& inputPortID_to_DP.get("i2").get(0) instanceof FileDP)
			return true;

		return false;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof FileDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
