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
import utility.textAnalyzer;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dataProduct.IntegerDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class ExtrSpeedup extends Workflow {
	public ExtrSpeedup(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		try {
			Utility.reportToLoggerNode(instanceId + " starts....................................");
		} catch (Exception e1) {
		}
		long startTime = System.currentTimeMillis();
		String fileName = null;
		String outputFileName = instanceId + ".o1." + runID;
		String pathToInFile = null;
		String pathToOutFile = null;
		Utility.writeToFile("111", "/home/view/log");
		if (inputPortID_to_DP.get("i1") instanceof FileDP) {
			Utility.writeToFile("222", "/home/view/log");
			fileName = ((FileDP) inputPortID_to_DP.get("i1")).data;
			pathToInFile = Utility.pathToFileDPsFolder + fileName;
			File f = new File(pathToInFile);
			if(!f.exists())
				pathToInFile = "/home/view/FileDPs/" + fileName;
			f = new File(pathToInFile);
			if(!f.exists()){
				pathToInFile = "/mnt/mntEBSDevice/FileDPs/" + fileName;
				Utility.writeToFile("333", "/home/view/log");
			}
		}

		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			pathToOutFile = Utility.pathToFileDPsFolder + outputFileName;
			textAnalyzer.extractSpeedInfo(pathToInFile, pathToOutFile);
			FileDP resultDP = new FileDP(outputFileName, outputFileName);
			outputPortID_to_DP.put("o1", resultDP);
			System.out.println(instanceId + " producing result (file): " + instanceId + ".o1." + runID);
			long endTime = System.currentTimeMillis();
			Utility.reportToLoggerNode("____ExtrSpeedup duration: " + new Long((endTime - startTime) / 1000).toString() + " s");
		} catch (Exception e) {
			System.out.println("ERROR: in workflow inst id: " + instanceId);
			System.out.println(e.toString());
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
		try {
			Utility.reportToLoggerNode(instanceId + " finished!");
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
