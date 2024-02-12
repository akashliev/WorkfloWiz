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
public class mOverlaps extends Workflow {

	public mOverlaps(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;
		
		FileDP inputFile = null;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP)
			inputFile = (FileDP) inputPortID_to_DP.get("i1").get(0);


		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + ";"); // " mImgtbl rawdir images-rawdir.tbl runlogistic --input aaa --model bbb --scores";

			String command = "cd " + Utility.pathToFileDPsFolder + "; mOverlaps " + inputFile.getFileName() + " " + instanceId + ".o1." + runID ;

			System.out.println("about to execute command:\n" + command);
			Utility.executeShellCommand(command);

			FileDP outputFitsFileDP = new FileDP(instanceId + ".o1." + runID);
			outputFitsFileDP.setFileName(instanceId + ".o1." + runID);
			

			outputPortID_to_DP.put("o1", outputFitsFileDP);
			System.out.println(instanceId + " producing result (files): " + outputFitsFileDP.getFileName());
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 1) {
			return false;
		}
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
