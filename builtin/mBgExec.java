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
public class mBgExec extends Workflow {
	int minimumNumberOfInputs = -1;

	public mBgExec(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		FileDP images_tbl_file = null;
		ArrayList<DataProduct> inputDPs_i2 = null;
		ArrayList<FileDP> inputFileDPs_i2 = new ArrayList<FileDP>();
		FileDP corrections_tbl_file = null;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP)
			images_tbl_file = (FileDP) inputPortID_to_DP.get("i1").get(0);

		if (portConsumesASingleListDP("i2"))
			inputDPs_i2 = ((ListDP) inputPortID_to_DP.get("i2").get(0)).data;

		if (inputDPs_i2 == null)
			inputDPs_i2 = new ArrayList<DataProduct>();

		if (portConsumesMultipleDPs("i2"))
			inputDPs_i2.addAll(inputPortID_to_DP.get("i2"));

		for (DataProduct dp : inputDPs_i2)
			if (dp instanceof FileDP)
				inputFileDPs_i2.add((FileDP) dp);

		if (inputPortID_to_DP.get("i3").get(0) instanceof FileDP)
			corrections_tbl_file = (FileDP) inputPortID_to_DP.get("i3").get(0);

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			ArrayList<DataProduct> list_of_output_fileDPs = new ArrayList<DataProduct>();
			String outputFileNames = "";
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; mkdir tmp;");
			for (int i = 0; i < inputFileDPs_i2.size(); i++) {
				String input_fileName = inputFileDPs_i2.get(i).getFileName();
				String output_dataName = instanceId + ".o1." + runID + "." + i;
				String mvCmd = "cd " + Utility.pathToFileDPsFolder + "; mv " + input_fileName + " tmp";
//				System.out.println("about to run:\n" + mvCmd);
				Utility.executeShellCommand(mvCmd);
				FileDP outputFileDP = new FileDP(output_dataName);
				String outputFileName = input_fileName;
				outputFileDP.setFileName(outputFileName);
				list_of_output_fileDPs.add(outputFileDP);
				outputFileNames += outputFileName + "\n";
			}

			String command = "cd " + Utility.pathToFileDPsFolder + "; mBgExec -p tmp " + images_tbl_file.getFileName() + " "
					+ corrections_tbl_file.getFileName() + " .";
			// mBgExec -p projdir/img0 images.tbl corrections.tbl corrdir
//			System.out.println("about to execute command:\n" + command);
			Utility.executeShellCommand(command);
			 Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; rm -r -f tmp");

			outputPortID_to_DP.put("o1", new ListDP(instanceId + ".o1." + runID, list_of_output_fileDPs));
			outputPortID_to_DP.put("o2", new IntDP(list_of_output_fileDPs.size(), instanceId + ".o2." + runID));
//			System.out.println(instanceId + " producing result (files): " + outputFileNames);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public boolean readyToExecute() {

		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") == null || !(inputPortID_to_DP.get("i1").get(0) instanceof FileDP) || inputPortID_to_DP.get("i2") == null
				|| inputPortID_to_DP.get("i3") == null || !(inputPortID_to_DP.get("i3").get(0) instanceof FileDP))
			return false;

		// if many data products (fits files) are connected to i2:
		if(!portConsumesASingleListDP("i2"))
			return false;
		// if(!portPosessesRequiredNumberOfInputFiles("i2"))
		// return false;

		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && outputPortID_to_DP.get("o1") instanceof ListDP && outputPortID_to_DP.get("o2") instanceof IntDP)
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
