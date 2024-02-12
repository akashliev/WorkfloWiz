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
public class MergeThree extends Workflow {
	public MergeThree(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		String fileName1 = null;
		String pathToFile1 = null;
		String fileName2 = null;
		String pathToFile2 = null;
		String fileName3 = null;
		String pathToFile3 = null;
		String outputFileName = instanceId + ".o1." + runID;

		if (inputPortID_to_DP.get("i1").get(0) instanceof FileDP) {
			fileName1 = ((FileDP) inputPortID_to_DP.get("i1").get(0)).getFileName();
			pathToFile1 = Utility.pathToFileDPsFolder + fileName1;
		}

		if (inputPortID_to_DP.get("i2").get(0) instanceof FileDP) {
			fileName2 = ((FileDP) inputPortID_to_DP.get("i2").get(0)).getFileName();
			pathToFile2 = Utility.pathToFileDPsFolder + fileName2;
		}

		if (inputPortID_to_DP.get("i3").get(0) instanceof FileDP) {
			fileName3 = ((FileDP) inputPortID_to_DP.get("i3").get(0)).getFileName();
			pathToFile3 = Utility.pathToFileDPsFolder + fileName3;
		}

		executionStatus = ExecutionStatus.inProcessOfExecution;

		try {

			File file1 = new File(pathToFile1);
			Scanner sc1 = new Scanner(new FileInputStream(file1));

			File file2 = new File(pathToFile2);
			Scanner sc2 = new Scanner(new FileInputStream(file2));

			File file3 = new File(pathToFile3);
			Scanner sc3 = new Scanner(new FileInputStream(file3));

			File outputFile = new File(Utility.pathToFileDPsFolder + outputFileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));

			while (sc1.hasNext())
				output.write(sc1.next() + " ");

			while (sc2.hasNext())
				output.write(sc2.next() + " ");

			while (sc3.hasNext())
				output.write(sc3.next() + " ");

			output.close();

			FileDP resultDP = new FileDP(outputFileName);
			outputPortID_to_DP.put("o1", resultDP);
			System.out.println(instanceId + " producing result (file): " + instanceId + ".o1." + runID);
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.size() != 3) {
			// Utility.appendToLog("cannot run Add workflow (" + instanceId + "), number of inputs != 2");
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (outputPortID_to_DP.size() == 1 && outputPortID_to_DP.get("o1") instanceof FileDP)
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;

		if (Utility.executeInCloud)
			try {
				Utility.reportToLoggerNode(Utility.myIP + ":>>>>>>>>>>>>>>> OK: " + instanceId + " finished execution, and has set its status to "
						+ executionStatus);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
