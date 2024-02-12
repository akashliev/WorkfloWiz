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
public class mAdd extends Workflow {
	int minimumNumberOfInputs = -1;

	public mAdd(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		FileDP images_tbl_file = (FileDP) inputPortID_to_DP.get("i1").get(0);
		ListDP listDP = (ListDP) inputPortID_to_DP.get("i2").get(0);
		FileDP template_header = (FileDP) inputPortID_to_DP.get("i3").get(0);

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; mkdir tmp;");
			for (DataProduct dp : listDP.data) {
				FileDP fileDP = null;
				if (dp instanceof FileDP) {
					fileDP = (FileDP) dp;
					String mvCommand = "cd " + Utility.pathToFileDPsFolder + "; mv " + fileDP.getFileName() + " tmp";
//					System.out.println("about to execute command: " + mvCommand);
					Utility.executeShellCommand(mvCommand);
				}
			}
			String dataName_o1 = instanceId + ".o1." + runID;
			String dataName_o2 = instanceId + ".o2." + runID;
			
			String cmd = "cd " + Utility.pathToFileDPsFolder + "; mAdd -p tmp " + images_tbl_file.getFileName() + " "
					+ template_header.getFileName() + " " + dataName_o1;
//			System.out.println("about to execute command:\n" + cmd);
			String stdoutput = Utility.executeShellCommand(cmd);
//			 System.out.println("stdoutput:\n" + stdoutput);

			FileDP outputFitsFile = new FileDP(dataName_o1);
			outputFitsFile.setFileName(dataName_o1 + ".fits");
			
			FileDP outputFitsAreaFile = new FileDP(dataName_o2);
			outputFitsAreaFile.setFileName(dataName_o2 + "_area.fits");
			
			outputPortID_to_DP.put("o1", outputFitsFile);
			outputPortID_to_DP.put("o2", outputFitsAreaFile);
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; rm -r -f tmp");

//			System.out.println(instanceId + " produced file: " + dataName_o1);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// //////////////////////////////////////i3:

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") == null || !(inputPortID_to_DP.get("i1").get(0) instanceof FileDP))
			return false;

		if (inputPortID_to_DP.get("i2") == null || !(inputPortID_to_DP.get("i2").get(0) instanceof ListDP))
			return false;

		if (inputPortID_to_DP.get("i3") == null || !(inputPortID_to_DP.get("i3").get(0) instanceof FileDP))
			return false;

		if (!portConsumesASingleListDP("i2"))
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
