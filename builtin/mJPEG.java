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
public class mJPEG extends Workflow {
	int minimumNumberOfInputs = -1;

	public mJPEG(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		FileDP fits_file = (FileDP) inputPortID_to_DP.get("i1").get(0);

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			String dataName = instanceId + ".o1." + runID;
			String cmd = "cd " + Utility.pathToFileDPsFolder + "; mJPEG -gray " + fits_file.getFileName() + " 0s max gaussian-log -out " + dataName
					+ ".jpeg";
			System.out.println("about to execute " + cmd);
			Utility.executeShellCommand(cmd);

			FileDP jpegFile = new FileDP(dataName);
			jpegFile.setFileName(dataName + ".jpeg");
			outputPortID_to_DP.put("o1", jpegFile);

			System.out.println(instanceId + " produced file: " + dataName + ".jpeg");

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") == null || !(inputPortID_to_DP.get("i1").get(0) instanceof FileDP))
			return false;

		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof FileDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
