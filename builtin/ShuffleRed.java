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
public class ShuffleRed extends Workflow {
	public ShuffleRed(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		System.out.println("running workflow " + instanceId);
		Utility.reportToLoggerNode("running workflow " + instanceId + ", runID: " + runID);
		if (!readyToExecute())
			return;

		String fileName = null;
		String outputFileName = instanceId + ".o1." + runID;
		String pathToFile = null;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP) {
			fileName = ((FileDP) inputPortID_to_DP.get("i1").get(0)).getFileName();
			pathToFile = Utility.pathToFileDPsFolder + fileName;
		}
		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {

			ArrayList<String> allWords = new ArrayList<String>();

			File file = new File(pathToFile);
			Scanner sc = new Scanner(new FileInputStream(file));
			while (sc.hasNext())
				allWords.add(sc.next());

			File outputFile = new File(Utility.pathToFileDPsFolder + outputFileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));

			while (!allWords.isEmpty()) {
				int i = (int) (Math.random() * allWords.size());
				output.write(allWords.remove(i) + " ");
			}
			output.close();
			FileDP resultDP = new FileDP(outputFileName);
			outputPortID_to_DP.put("o1", resultDP);
			System.out.println(instanceId + " producing result (file): " + instanceId + ".o1." + runID);
			Utility.reportToLoggerNode(instanceId + " producing result (file): " + instanceId + ".o1." + runID);
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null) {
			return false;
		}
		if (inputPortID_to_DP.get("i1").size() != 1 || !(inputPortID_to_DP.get("i1").get(0) instanceof FileDP))
			return false;
		return true;
	}

	@Override
	public void setFinishedStatus() {
		System.out.println("ShuffleYellow: setting finished status: ");
		if (!outputPortID_to_DP.isEmpty())
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
		System.out.println("executionStatus: " + executionStatus);
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
