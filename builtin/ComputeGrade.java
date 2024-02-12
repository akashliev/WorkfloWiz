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
import dataProduct.StringDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class ComputeGrade extends Workflow {
	public ComputeGrade(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;
		String outputFileName = instanceId + ".o1." + runID;

		String pathToDrivingSummary = null;
		String drivSummaryFileName = null;
		String modelFileName = null;
		String pathToModelFile = null;
		if (inputPortID_to_DP.get("i1") instanceof FileDP) {
			modelFileName = ((FileDP) inputPortID_to_DP.get("i1")).data;
			pathToModelFile = Utility.pathToFileDPsFolder + modelFileName;
		}
		if (inputPortID_to_DP.get("i2") instanceof FileDP) {
			drivSummaryFileName = ((FileDP) inputPortID_to_DP.get("i2")).data;
			pathToDrivingSummary = Utility.pathToFileDPsFolder + drivSummaryFileName;
		}
		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			String command = "cd /usr/local/mahout-0.9/bin; ./mahout runlogistic --input " + pathToDrivingSummary + " --model " + pathToModelFile
					+ " --scores";
			Utility.writeToFile(command, "/home/view/log");
			String shellOutput = Utility.executeShellCommand(command);
			shellOutput = shellOutput.substring(shellOutput.indexOf("likelihood") + 11);
			Utility.appendToLog("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
			Utility.appendToLog(shellOutput);
			Utility.appendToLog("bbbbbbbbbbbbbbb");
			System.out.println(shellOutput);
			String target = shellOutput.substring(0, shellOutput.indexOf(","));
			String modelOutput = shellOutput.substring(shellOutput.indexOf(",") + 1, shellOutput.lastIndexOf(","));
			String likelihood = shellOutput.substring(shellOutput.lastIndexOf(",") + 1);
			String grade = "undefined";
			String result = null;

			if (target.trim().equals("0"))
				grade = "PASS";
			else if (target.trim().equals("1"))
				grade = "FAIL";

			String drivingSummary = Utility.readFromAFile(pathToDrivingSummary);
			String gasbrk = drivingSummary.substring(drivingSummary.indexOf("grade") + 7);
			gasbrk = gasbrk.substring(0, gasbrk.indexOf(","));
			String allFourNums = drivingSummary.substring(drivingSummary.indexOf("grade") + 7);
			String speedup = allFourNums.substring(allFourNums.indexOf(",")+1);
			speedup = speedup.substring(0, speedup.indexOf(","));
			String brakingonturns = allFourNums.substring(allFourNums.indexOf(",") + 1);
			brakingonturns = brakingonturns.substring(brakingonturns.indexOf(",") + 1, brakingonturns.lastIndexOf(","));
			
			result = "Confidence Level:" + likelihood + "\n";
			result += "Frequency of acceleration and sudden braking : " + gasbrk.substring(0, gasbrk.indexOf(".") + 4) + "\n";
			result += "Acceleration smoothness : " + speedup.substring(0, speedup.indexOf(".") + 4) + "\n";
			result += "Gradual braking on turns : " + brakingonturns.substring(0, brakingonturns.indexOf(".") + 4) + "\n";;
			result += grade;
			Utility.reportToLoggerNode(grade);
			String outFileName = instanceId + ".o1." + runID;
			String pathToOutFile = Utility.pathToFileDPsFolder + outFileName;
			Utility.writeToFile(result, pathToOutFile);
			Utility.reportToLoggerNode("wwwwwwwwwwwwwwwwwwrote to file: " + pathToOutFile);
			FileDP resultDPFile = new FileDP(outFileName, outFileName);

			// ArrayList<String> allWords = new ArrayList<String>();
			//
			// File file = new File(pathToDrivingSummary);
			// Scanner sc = new Scanner(new FileInputStream(file));
			// while (sc.hasNext())
			// allWords.add(sc.next());
			//
			// File outputFile = new File(Utility.pathToFileDPsFolder + outputFileName);
			// BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
			//
			// while (!allWords.isEmpty()) {
			// int i = (int) (Math.random() * allWords.size());
			// output.write(allWords.remove(i) + " ");
			// }
			//
			// output.close();

			// FileDP resultDP = new FileDP(outputFileName, outputFileName);
			outputPortID_to_DP.put("o1", resultDPFile);
//			Utility.reportToLoggerNode(instanceId + " producing result (string): ");
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
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!outputPortID_to_DP.isEmpty()) {
			executionStatus = ExecutionStatus.finishedOK;
		} else {
			executionStatus = ExecutionStatus.finishedError;
		}
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
