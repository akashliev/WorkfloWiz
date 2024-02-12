package builtin;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import webbench.WebbenchUtility;
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
public class mProjectPP extends Workflow {

	public mProjectPP(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		FileDP headerFile = null;

		ArrayList<DataProduct> all_dps_i1 = new ArrayList<DataProduct>();
		ArrayList<FileDP> fileDPs_i1 = new ArrayList<FileDP>();

		if (portConsumesASingleListDP("i1"))
			all_dps_i1.addAll(((ListDP) inputPortID_to_DP.get("i1").get(0)).data);
		else if (portConsumesMultipleDPs("i1"))
			all_dps_i1.addAll(inputPortID_to_DP.get("i1"));

		for (DataProduct dp : all_dps_i1)
			if (dp instanceof FileDP)
				fileDPs_i1.add((FileDP) dp);

		if (inputPortID_to_DP.get("i2").get(0) instanceof FileDP)
			headerFile = (FileDP) inputPortID_to_DP.get("i2").get(0);

		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			ArrayList<DataProduct> allOutputFiles = new ArrayList<DataProduct>();
			for (int i = 0; i < fileDPs_i1.size(); i++) {
				String dataName = instanceId + ".o1." + runID + "." + i;

				String command = "cd " + Utility.pathToFileDPsFolder + "; mProjectPP " + fileDPs_i1.get(i).getFileName() + " " + dataName + " "
						+ headerFile.dataName;

				System.out.println("about to execute command:\n" + command);
				Utility.executeShellCommand(command);

				FileDP outputFitsFileDP = new FileDP(dataName);
				outputFitsFileDP.setFileName(dataName + ".fits");

				FileDP outputAreaFileDP = new FileDP(dataName);
				outputAreaFileDP.setFileName(dataName + "_area.fits");

				allOutputFiles.add(outputFitsFileDP);
				allOutputFiles.add(outputAreaFileDP);
			}

			outputPortID_to_DP.put("o1", new ListDP(instanceId + ".o1." + runID, allOutputFiles));
			outputPortID_to_DP.put("o2", new IntDP(allOutputFiles.size(), instanceId + ".o2." + runID));

			// outputPortID_to_DP.put("o1", outputFitsFileDP);
			// outputPortID_to_DP.put("o2", outputAreaFileDP);
			// System.out.println(instanceId + " producing result (files): " + outputFitsFileDP.getFileName() + " and "
			// + outputAreaFileDP.getFileName());
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null)
			return false;

		if (!(inputPortID_to_DP.get("i1").get(0) instanceof ListDP))
			return false;

		if (!(inputPortID_to_DP.get("i2").get(0) instanceof FileDP))
			return false;

		if (!portConsumesASingleListDP("i1"))
			return false;

		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof ListDP) && (outputPortID_to_DP.get("o2") instanceof IntDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}
	
}
