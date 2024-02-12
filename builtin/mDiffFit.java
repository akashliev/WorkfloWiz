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
public class mDiffFit extends Workflow {
	int minimumNumberOfInputs = -1;

	public mDiffFit(String instanceid) {
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

		input_diffs_file = (FileDP) inputPortID_to_DP.get("i1").get(0);

		inputDPs_i2 = ((ListDP) inputPortID_to_DP.get("i2").get(0)).data;

		header_file = (FileDP) inputPortID_to_DP.get("i3").get(0);

		for (DataProduct dp : inputDPs_i2)
			if (dp instanceof FileDP)
				inputFileDPs_i2.add((FileDP) dp);

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			ArrayList<DataProduct> list = new ArrayList<DataProduct>();

			String diffsFileStr = Utility.readFileAsString(Utility.pathToFileDPsFolder + input_diffs_file.getFileName());
			ArrayList<String> cmds = generatemDiffFitCmd(diffsFileStr, header_file.getFileName(), inputFileDPs_i2);
			for (int i = 0; i < cmds.size(); i++) {
				String cmd = cmds.get(i);
				String completeCommand = "cd " + Utility.pathToFileDPsFolder + "; " + cmd;
				// System.out.println("about to execute:");
				// System.out.println(completeCommand);
				Utility.executeShellCommand(completeCommand);

				FileDP outputFitsFileDP = new FileDP(instanceId + ".o1." + runID + "." + i);
				outputFitsFileDP.setFileName(getOutputFileName(cmd));
				list.add(outputFitsFileDP);
				FileDP outputAreaFitsFileDP = new FileDP(instanceId + ".o1." + runID + "." + i);
				outputAreaFitsFileDP.setFileName(getOutputFileName(cmd).replace(".fits", "_area.fits"));
				list.add(outputAreaFitsFileDP);
//				System.out.println(instanceId + " producing result (files): " + outputFitsFileDP.getFileName() + "\n"
//						+ outputAreaFitsFileDP.getFileName());

			}
			ListDP listDP = new ListDP(instanceId + ".o1." + runID, list);
			outputPortID_to_DP.put("o1", listDP);

			outputPortID_to_DP.put("o2", new IntDP(listDP.data.size(), instanceId + ".o2." + runID));

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
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof ListDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

	public static ArrayList<String> generatemDiffFitCmd(String diffFile, String headerFileName, ArrayList<FileDP> inputFileDPs_i2) {
		ArrayList<String> cmds = new ArrayList<String>();
		ArrayList<String> filesThatWereActuallySupplied = new ArrayList<String>();
		for (FileDP fileDP : inputFileDPs_i2)
			filesThatWereActuallySupplied.add(fileDP.getFileName());

		String[] linesArr = diffFile.split("\n");
		ArrayList<String> lines = new ArrayList<String>(Arrays.asList(linesArr));
		lines.remove(0);
		lines.remove(0);

		for (String line : lines) {
			ArrayList<String> fieldsInLine = new ArrayList<String>(Arrays.asList(line.split(" ")));
			ArrayList<String> nonEmptyFieldsInLine = new ArrayList<String>();
			for (String field : fieldsInLine)
				if (!field.trim().equals(""))
					nonEmptyFieldsInLine.add(field);
			String inputFile1_name = nonEmptyFieldsInLine.get(2);
			String inputFile2_name = nonEmptyFieldsInLine.get(3);
			String outputFile_name = nonEmptyFieldsInLine.get(4);
			if (filesThatWereActuallySupplied.contains(inputFile1_name) && filesThatWereActuallySupplied.contains(inputFile2_name))
				cmds.add("mDiffFit " + inputFile1_name + " " + inputFile2_name + " " + outputFile_name + " " + headerFileName);
		}

		return cmds;
	}

	public static String getOutputFileName(String cmd) {
		String[] componentsArr = cmd.split(" ");
		ArrayList<String> components = new ArrayList<String>(Arrays.asList(componentsArr));
		return components.get(3);
	}
}
