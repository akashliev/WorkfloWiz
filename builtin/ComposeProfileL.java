package builtin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import utility.textAnalyzer;
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
public class ComposeProfileL extends Workflow {
	public ComposeProfileL(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		Utility.reportToLoggerNode(instanceId + " will now see if it's ready");
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;
		
		try {
			Utility.reportToLoggerNode(instanceId + " starts....................................");
		} catch (Exception e1) {
		}
		long startTime = System.currentTimeMillis();
		
		String gasbrk = null;
		String speedup = null;
		String brkngturns = null;
		if (inputPortID_to_DP.get("i1") instanceof FileDP) {
			gasbrk = Utility.readFromAFile(Utility.pathToFileDPsFolder + inputPortID_to_DP.get("i1").dataName);
		}
		if (inputPortID_to_DP.get("i2") instanceof FileDP) {
			speedup = Utility.readFromAFile(Utility.pathToFileDPsFolder + inputPortID_to_DP.get("i2").dataName);
		}
		if (inputPortID_to_DP.get("i3") instanceof FileDP) {
			brkngturns = Utility.readFromAFile(Utility.pathToFileDPsFolder + inputPortID_to_DP.get("i3").dataName);
		}
		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			String resultString = textAnalyzer.composeProfile(gasbrk, speedup, brkngturns);

			String outputFileName = instanceId + ".o1." + runID;
			File outputFile = new File(Utility.pathToFileDPsFolder + outputFileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
			output.write(resultString);
			output.close();
			FileDP resultDP = new FileDP(outputFileName, outputFileName);
			outputPortID_to_DP.put("o1", resultDP);
			if (registerIntermediateDPs)
				Utility.registerDataProduct(resultDP);
			System.out.println(instanceId + " producing result: " + resultString);
			long endTime = System.currentTimeMillis();
			Utility.reportToLoggerNode("____" + instanceId + "duration: " + new Long((endTime - startTime) / 1000).toString() + " s");
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		try {
			Utility.reportToLoggerNode("number of input DPs: " + inputPortID_to_DP.keySet().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 3) {
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		try {
			Utility.reportToLoggerNode(instanceId + " finished!");
		} catch (Exception e1) {
		}
		
		if (!outputPortID_to_DP.isEmpty())
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
		Utility.appendToLog("aaa set finished status: " + executionStatus);
	}

}
