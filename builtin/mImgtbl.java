package builtin;

import java.util.ArrayList;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.SWLAnalyzer;
import translator.Workflow;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dataProduct.IntDP;
import dataProduct.ListDP;
import dataProduct.StringDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class mImgtbl extends Workflow {
	int minimumNumberOfInputs = -1;

	public mImgtbl(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;
		// Iterator it = inputPortID_to_DP.values().iterator();
		ArrayList<DataProduct> inputDPs = new ArrayList<DataProduct>();
		ArrayList<FileDP> inputFileDPs = new ArrayList<FileDP>();
		// String outputFileName = null;

		if (portConsumesASingleListDP("i1"))
			inputDPs.addAll(((ListDP) inputPortID_to_DP.get("i1").get(0)).data);
		else if (portConsumesMultipleDPs("i1")) {
			inputDPs.addAll(inputPortID_to_DP.get("i1"));
		}

		for (DataProduct dp : inputDPs)
			if (dp instanceof FileDP)
				inputFileDPs.add((FileDP) dp);

		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			String outFileName = instanceId + ".o1." + runID;
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; mkdir tmp;");
			for (FileDP fileDP : inputFileDPs) {
				String fileName = fileDP.getFileName();
				Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; cp " + fileName + " tmp");
			}

			String command = "cd " + Utility.pathToFileDPsFolder + "; mImgtbl tmp " + outFileName;
			System.out.println("about to execute command:\n" + command);
			Utility.executeShellCommand(command);

			FileDP resultDPFile = new FileDP(outFileName);

			outputPortID_to_DP.put("o1", resultDPFile);
			System.out.println(instanceId + " producing result (file): " + instanceId + ".o1." + runID);
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; rm -r -f tmp");
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {

		if (inputPortID_to_DP == null)
			return false;

		if (!(inputPortID_to_DP.get("i1").get(0) instanceof ListDP))
			return false;

		if (!portConsumesASingleListDP("i1"))
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
