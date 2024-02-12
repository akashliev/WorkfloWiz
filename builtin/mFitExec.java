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
public class mFitExec extends Workflow {
	int minimumNumberOfInputs = -1;

	public mFitExec(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		FileDP input_diffs_file = null;
		ArrayList<DataProduct> inputDPs_i2 = null;
		ArrayList<FileDP> inputFileDPs_i2 = new ArrayList<FileDP>();
		FileDP header_file = null;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP)
			input_diffs_file = (FileDP) inputPortID_to_DP.get("i1").get(0);

		if (inputPortID_to_DP.get("i2").get(0) instanceof ListDP)
			inputDPs_i2 = ((ListDP) inputPortID_to_DP.get("i2").get(0)).data;

		if (inputDPs_i2 == null)
			inputDPs_i2 = new ArrayList<DataProduct>();

		if (inputPortID_to_DP.get("i2").get(0) instanceof FileDP)
			inputDPs_i2.addAll(inputPortID_to_DP.get("i2"));

		for (DataProduct dp : inputDPs_i2)
			if (dp instanceof FileDP)
				inputFileDPs_i2.add((FileDP) dp);

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {

			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; mkdir tmp");
			for (FileDP fileDP : inputFileDPs_i2) {
				String command = "cd " + Utility.pathToFileDPsFolder + "; cp " + fileDP.getFileName() + " tmp";// + "; mFitExec "
																												// +
																												// input_diffs_file.getFileName()
																												// + " " +
																												// instanceId +
																												// ".o1." + runID;
//				System.out.println("aboutttt to run:\n" + command);
				Utility.executeShellCommand(command);
			}

			String cmd = "cd " + Utility.pathToFileDPsFolder + "; mFitExec " + input_diffs_file.getFileName() + " " + instanceId + ".o1." + runID
					+ " tmp";
//			System.out.println("about to execute " + cmd);
			Utility.executeShellCommand(cmd);
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; rm -r -f tmp");

			FileDP outputFitsTblFileDP = new FileDP(instanceId + ".o1." + runID);
			outputFitsTblFileDP.setFileName(instanceId + ".o1." + runID);

			outputPortID_to_DP.put("o1", outputFitsTblFileDP);
			System.out.println(instanceId + " producing result (files): " + outputFitsTblFileDP.getFileName());

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public boolean readyToExecute() {

		if (inputPortID_to_DP == null)
			return false;

		if (inputPortID_to_DP.get("i1") == null)
			return false;

		if(!portConsumesASingleListDP("i2"))
			return false;

		// if (inputPortID_to_DP.get("i2").get(0) instanceof ListDP
		// && ((ListDP) inputPortID_to_DP.get("i2").get(0)).data.size() - 1 < minimumNumberOfInputs)
		// return false;

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
