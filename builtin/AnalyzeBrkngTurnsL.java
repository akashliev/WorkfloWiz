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
import utility.CPUintensity;
import utility.Pair;
import utility.Utility;
import utility.textAnalyzer;
import dataProduct.DataProduct;
import dataProduct.DoubleDP;
import dataProduct.FileDP;
import dataProduct.IntegerDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class AnalyzeBrkngTurnsL extends Workflow {
	public AnalyzeBrkngTurnsL(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		try {
			Utility.reportToLoggerNode("AnalyzeBrkngTurns starts.................................");
		} catch (Exception e1) {
		}
		long startTime = System.currentTimeMillis();
		String fileName = null;
		String pathToInFile = null;

		if (inputPortID_to_DP.get("i1") instanceof FileDP) {
			fileName = ((FileDP) inputPortID_to_DP.get("i1")).data;
			pathToInFile = Utility.pathToFileDPsFolder + fileName;
		}

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {
			String result = textAnalyzer.getAvgBrakingAbruptnessBeforeTurnsL(pathToInFile);

			String outFileName = instanceId + ".o1." + runID;
			String pathToOutFile = Utility.pathToFileDPsFolder + outFileName;
			Utility.writeToFile(result, pathToOutFile);
			FileDP resultDPFile = new FileDP(outFileName, outFileName);
			outputPortID_to_DP.put("o1", resultDPFile);
//			CPUintensity.runCPUIntensiveTask();
			long endTime = System.currentTimeMillis();
			Utility.reportToLoggerNode("____" + instanceId + " duration: " + new Long((endTime - startTime) / 1000).toString() + " s");
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null) {
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		try {
			Utility.reportToLoggerNode("AnalyzeBrkngTurns finished!");
		} catch (Exception e1) {
		}

		if (!outputPortID_to_DP.isEmpty())
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;

	}

	public static boolean listOfPairsContains(String key, ArrayList<Pair> listOfPairs) {
		for (Pair currPair : listOfPairs) {
			if (currPair.left.trim().equals(key.trim()))
				return true;
		}

		return false;
	}

	public static void incrementCount(String key, ArrayList<Pair> listOfPairs) {
		for (Pair currPair : listOfPairs) {
			if (currPair.left.trim().equals(key.trim()))
				currPair.right++;

		}
	}

	public static void printListOfPairs(ArrayList<Pair> listOfPairs) {
		for (Pair currPair : listOfPairs)
			System.out.println(currPair.left + " " + currPair.right);
	}

	public static void writeListOfPairsIntoAFile(ArrayList<Pair> listOfPairs, String outputFileName) throws Exception {
		File file = new File(Utility.pathToFileDPsFolder + outputFileName);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		for (Pair currPair : listOfPairs)
			output.write(currPair.left + " " + currPair.right + "\n");
		output.close();
	}

	public static boolean isAWordCountReport(String fileName) throws Exception {
		// returns true if the file contains word count result
		String pathToFile = Utility.pathToFileDPsFolder + fileName;
		File file = new File(pathToFile);
		Scanner sc = new Scanner(new FileInputStream(file));
		sc.next();
		String currWord = sc.next();
		if (currWord.trim().matches("\\d")) {
			return true;
		}
		return false;
	}

}
