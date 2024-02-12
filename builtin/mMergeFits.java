package builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.w3c.dom.Document;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import webbench.WebbenchUtility;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dataProduct.IntDP;
import dataProduct.ListDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class mMergeFits extends Workflow {

	public mMergeFits(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		ArrayList<DataProduct> all_dps_i1 = new ArrayList<DataProduct>();
		ArrayList<FileDP> fileDPs_i1 = new ArrayList<FileDP>();

		all_dps_i1.addAll(((ListDP) inputPortID_to_DP.get("i1").get(0)).data);

		for (DataProduct dp : all_dps_i1)
			if (dp instanceof FileDP)
				fileDPs_i1.add((FileDP) dp);

		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			String dataName = instanceId + ".o1." + runID;
			ArrayList<String> fileNames = new ArrayList<String>();
			for (int i = 0; i < fileDPs_i1.size(); i++)
				fileNames.add(fileDPs_i1.get(i).getFileName());

			String result = mergeFiles(fileNames);
			Utility.writeToFile(result, Utility.pathToFileDPsFolder + dataName);

			outputPortID_to_DP.put("o1", new FileDP(dataName));
			System.out.println(instanceId + " finished, produced output file: " + dataName);

		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null)
			return false;

		if (!(inputPortID_to_DP.get("i1").get(0) instanceof ListDP))
			return false;

		if (!portConsumesASingleListDP("i1"))
			return false;

		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof FileDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

	public static String mergeFiles(ArrayList<String> fileNames) throws Exception {
		String header = extractHeaderFromFirstFileInTheList(fileNames);

		String result = header + "\n";

		for (String fileName : fileNames)
			result += extractEverythingBelowHeader(fileName);
		// for(int i=0; i<fileNames.size(); i++)
		// result += extractEverythingBelowHeader(fileNames.get(i)).replaceFirst("0", new Integer(i).toString());

		return result.trim();
	}

	public static String extractHeaderFromFirstFileInTheList(ArrayList<String> fileNames) throws IOException {
		String fileName = fileNames.get(0);
		String content = Utility.readFileAsString(Utility.pathToFileDPsFolder + fileName);
		return extractHeader(content);
	}

	public static String extractEverythingBelowHeader(String fileName) throws Exception {
		String content = Utility.readFileAsString(Utility.pathToFileDPsFolder + fileName);
		String[] linesArr = content.split("\n");
		ArrayList<String> lines = new ArrayList<String>(Arrays.asList(linesArr));
		String result = "";
		for (int i = 1; i < lines.size(); i++)
			result += lines.get(i) + "\n";

		result.trim();

		return result;
	}

	public static String extractHeader(String content) {
		String[] linesArr = content.split("\n");
		ArrayList<String> lines = new ArrayList<String>(Arrays.asList(linesArr));

		return lines.get(0);
	}

}
