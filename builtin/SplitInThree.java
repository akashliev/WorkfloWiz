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
public class SplitInThree extends Workflow {
	public SplitInThree(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		String fileName = null;
		String pathToFile = null;
		String outputFileName1 = instanceId + ".o1." + runID;
		String outputFileName2 = instanceId + ".o2." + runID;
		String outputFileName3 = instanceId + ".o3." + runID;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP) {
			fileName = ((FileDP) inputPortID_to_DP.get("i1").get(0)).getFileName();
			pathToFile = Utility.pathToFileDPsFolder + fileName;
		}

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {

			File file = new File(pathToFile);
			Scanner sc = new Scanner(new FileInputStream(file));
			int totalNoOfWords = 0;
			while (sc.hasNext()) {
				sc.next();
				totalNoOfWords++;
			}
			System.out.println("Number of words: " + totalNoOfWords);

			sc = new Scanner(new FileInputStream(file));

			File outputFile1 = new File(Utility.pathToFileDPsFolder + outputFileName1);
			BufferedWriter output1 = new BufferedWriter(new FileWriter(outputFile1));

			File outputFile2 = new File(Utility.pathToFileDPsFolder + outputFileName2);
			BufferedWriter output2 = new BufferedWriter(new FileWriter(outputFile2));

			File outputFile3 = new File(Utility.pathToFileDPsFolder + outputFileName3);
			BufferedWriter output3 = new BufferedWriter(new FileWriter(outputFile3));

			int counter = 0;
			while (sc.hasNext()) {
				if (counter < totalNoOfWords * 0.333)
					output1.write(sc.next() + " ");
				else if (counter < totalNoOfWords * 0.667)
					output2.write(sc.next() + " ");
				else
					output3.write(sc.next() + " ");
				counter++;
			}
			output1.close();
			output2.close();
			output3.close();

			FileDP resultDP1 = new FileDP(outputFileName1);
			FileDP resultDP2 = new FileDP(outputFileName2);
			FileDP resultDP3 = new FileDP(outputFileName3);
			outputPortID_to_DP.put("o1", resultDP1);
			outputPortID_to_DP.put("o2", resultDP2);
			outputPortID_to_DP.put("o3", resultDP3);
			System.out.println(instanceId + " producing result (file): " + instanceId + ".o1." + runID + "\nand" + instanceId + ".o2." + runID
					+ "\nand" + instanceId + ".o3." + runID);
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
