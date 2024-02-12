package builtin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.DoubleDP;
import dataProduct.FileDP;
import dataProduct.FloatDP;
import dataProduct.StringDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class ComposeProfile extends Workflow {
	public ComposeProfile(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		System.out.println("running workflow " + instanceId);
//		Utility.reportToLoggerNode("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		Double gasbrk = null;
		Double speedup = null;
		Double brkngturns = null;
		if (inputPortID_to_DP.get("i1") instanceof FileDP) {
			// gasbrk = ((DoubleDP) inputPortID_to_DP.get("i1")).data;
			gasbrk = new Double(Utility.readFromAFile(Utility.pathToFileDPsFolder + inputPortID_to_DP.get("i1").dataName));
		}
		if (inputPortID_to_DP.get("i2") instanceof FileDP) {
			// speedup = ((DoubleDP) inputPortID_to_DP.get("i2")).data;
			speedup = new Double(Utility.readFromAFile(Utility.pathToFileDPsFolder + inputPortID_to_DP.get("i2").dataName));
		}
		if (inputPortID_to_DP.get("i3") instanceof FileDP) {
			// brkngturns = ((DoubleDP) inputPortID_to_DP.get("i3")).data;
			brkngturns = new Double(Utility.readFromAFile(Utility.pathToFileDPsFolder + inputPortID_to_DP.get("i3").dataName));
		}

		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			String resultString = "\"gasbrake\",\"speedup\",\"brakingonturns\",\"grade\"\n" + gasbrk + "," + speedup + "," + brkngturns + ",1";

			String outputFileName = instanceId + ".o1." + runID;
			File outputFile = new File(Utility.pathToFileDPsFolder + outputFileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
			output.write(resultString);

			output.close();
			FileDP resultDP = new FileDP(outputFileName, outputFileName);
			outputPortID_to_DP.put("o1", resultDP);
			if (registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
//			System.out.println(instanceId + " producing result: " + resultString);
//			Utility.reportToLoggerNode(instanceId + " producing result: " + resultString);
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 3) {
			// Utility.appendToLog("cannot run Add workflow (" + instanceId + "), number of inputs != 2");
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty())
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
