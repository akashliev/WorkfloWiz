package builtin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Pair;
import utility.Utility;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dataProduct.IntegerDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class SplitInTwo extends Workflow {
	public SplitInTwo(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		System.out.println("running workflow " + instanceId);
		Utility.reportToLoggerNode(Utility.myIP + ": aaaaaaaaaaaaaaaaaaaaaaactually running workflow (execute method) " + instanceId + ", runID: "
				+ runID + ", right before readyToExecute call");
		if (!readyToExecute())
			return;

		Utility.reportToLoggerNode(Utility.myIP + ":passed readyToExecute! " + instanceId + ", runID: " + runID);

		String fileName = null;
		String pathToFile = null;
		String outputFileName1 = instanceId + ".o1." + runID;
		String outputFileName2 = instanceId + ".o2." + runID;
		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 00");
		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP) {
			fileName = ((FileDP) inputPortID_to_DP.get("i1").get(0)).getFileName();
			pathToFile = Utility.pathToFileDPsFolder + fileName;
		}
		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 11");
		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 22");
			File file = new File(pathToFile);
			Scanner sc = new Scanner(new FileInputStream(file));
			int totalNoOfWords = 0;
			while (sc.hasNext()) {
				sc.next();
				totalNoOfWords++;
			}
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 33");
			System.out.println("Number of words: " + totalNoOfWords);

			sc = new Scanner(new FileInputStream(file));

			File outputFile1 = new File(Utility.pathToFileDPsFolder + outputFileName1);
			BufferedWriter output1 = new BufferedWriter(new FileWriter(outputFile1));

			File outputFile2 = new File(Utility.pathToFileDPsFolder + outputFileName2);
			BufferedWriter output2 = new BufferedWriter(new FileWriter(outputFile2));
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 44");
			int counter = 0;
			while (sc.hasNext()) {
				if (counter < totalNoOfWords / 2)
					output1.write(sc.next() + " ");
				else
					output2.write(sc.next() + " ");
				counter++;
			}
			output1.close();
			output2.close();
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 55");
			FileDP resultDP1 = new FileDP(outputFileName1);
			FileDP resultDP2 = new FileDP(outputFileName2);
			outputPortID_to_DP.put("o1", resultDP1);
			outputPortID_to_DP.put("o2", resultDP2);
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 66");
			System.out.println(instanceId + " producing result (file): " + instanceId + ".o1." + runID + "\nand" + instanceId + ".o2." + runID);
			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>>>>>>>>>>>>>>>> " + instanceId + " producing result (file): " + instanceId + ".o1."
					+ runID + "\nand" + instanceId + ".o2." + runID);
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null) {
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
