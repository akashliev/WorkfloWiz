package builtin;

import java.util.ArrayList;
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
public class mProjectPPmImgtbl extends Workflow {

	public mProjectPPmImgtbl(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
//		System.out.println("running workflow " + instanceId);
//		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 11aa");
		if (!readyToExecute())
			return;
//		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 22aa");
		FileDP headerFile = null;

		ArrayList<DataProduct> all_dps_i1 = new ArrayList<DataProduct>();
		ArrayList<FileDP> fileDPs_i1 = new ArrayList<FileDP>();

//		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 33aa");

		if (portConsumesASingleListDP("i1"))
			all_dps_i1.addAll(((ListDP) inputPortID_to_DP.get("i1").get(0)).data);
		else if (portConsumesMultipleDPs("i1"))
			all_dps_i1.addAll(inputPortID_to_DP.get("i1"));
//		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 44aa");
		for (DataProduct dp : all_dps_i1)
			if (dp instanceof FileDP)
				fileDPs_i1.add((FileDP) dp);
//		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 55aa");
		if (inputPortID_to_DP.get("i2").get(0) instanceof FileDP)
			headerFile = (FileDP) inputPortID_to_DP.get("i2").get(0);
//		Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 66aa");
		executionStatus = ExecutionStatus.inProcessOfExecution;
		try {
			ArrayList<DataProduct> allOutputFileDPs = new ArrayList<DataProduct>();
			for (int i = 0; i < fileDPs_i1.size(); i++) {
				String dataName = instanceId + ".o2." + runID + "." + i;

				String command = "cd " + Utility.pathToFileDPsFolder + "; mProjectPP " + fileDPs_i1.get(i).getFileName() + " " + dataName + " "
						+ headerFile.dataName;

//				System.out.println("about to execute command:\n" + command);
//				Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " about to execute command:\n" + command);
				Utility.executeShellCommand(command);

				FileDP outputFitsFileDP = new FileDP(dataName);
				outputFitsFileDP.setFileName(dataName + ".fits");

				FileDP outputAreaFileDP = new FileDP(dataName);
				outputAreaFileDP.setFileName(dataName + "_area.fits");

				allOutputFileDPs.add(outputFitsFileDP);
				allOutputFileDPs.add(outputAreaFileDP);
			}
			Utility.reportToLoggerNode(Utility.myIP + ": " + instanceId + " 77");
			outputPortID_to_DP.put("o2", new ListDP(instanceId + ".o2." + runID, allOutputFileDPs));
			
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; mkdir tmp;");
			for (int i = 0; i < allOutputFileDPs.size(); i++) {
				FileDP projectedFileDP = (FileDP) allOutputFileDPs.get(i);
				Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; cp " + projectedFileDP.getFileName() + " tmp;");
			}

			String outFileTblFileName = instanceId + ".o1." + runID;
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; mImgtbl tmp " + outFileTblFileName);
			Utility.executeShellCommand("cd " + Utility.pathToFileDPsFolder + "; rm -r -f tmp;");

			FileDP outputImgFile = new FileDP(outFileTblFileName);

			outputPortID_to_DP.put("o1", outputImgFile);
//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>>>>>>>>>>>>>>>> " + instanceId + " producing result (file): " + outputImgFile);

		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		try {
//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>> " + instanceId + " ready to execute call, inputPortID_to_DP keysettttttttt: "
//					+ inputPortID_to_DP.keySet());

//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>> just object:" + inputPortID_to_DP.get("i1").get(0));

//			Utility.reportToLoggerNode("(inputPortID_to_DP.get(\"i1\").get(0) instanceof ListDP): "
//					+ ((inputPortID_to_DP.get("i1").get(0) instanceof ListDP)));

//			if (inputPortID_to_DP.get("i1").get(0) != null)
//				Utility.reportToLoggerNode("what are the two inputs? " + inputPortID_to_DP.get("i1").get(0));
//
//			if (inputPortID_to_DP.get("i1").get(0).getClass() != null)
//				Utility.reportToLoggerNode("what are the two inputs? " + inputPortID_to_DP.get("i1").get(0).getClass());
//
//			if (inputPortID_to_DP.get("i1").get(0).getClass().getName() != null)
//				Utility.reportToLoggerNode("what are the two inputs? " + inputPortID_to_DP.get("i1").get(0).getClass().getName());

//			if (inputPortID_to_DP.get("i2").get(0) != null)
//				Utility.reportToLoggerNode("what are the two inputs? " + inputPortID_to_DP.get("i2").get(0));
//
//			if (inputPortID_to_DP.get("i2").get(0).getClass() != null)
//				Utility.reportToLoggerNode("what are the two inputs? " + inputPortID_to_DP.get("i2").get(0).getClass());
//
//			if (inputPortID_to_DP.get("i2").get(0).getClass().getName() != null)
//				Utility.reportToLoggerNode("what are the two inputs? " + inputPortID_to_DP.get("i2").get(0).getClass().getName());
//
//			Utility.reportToLoggerNode("(inputPortID_to_DP.get(\"i2\").get(0) instanceof FileDP): "
//					+ (inputPortID_to_DP.get("i2").get(0) instanceof FileDP));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (inputPortID_to_DP == null)
			return false;

		if (!(inputPortID_to_DP.get("i1").get(0) instanceof ListDP))
			return false;

		if (!(inputPortID_to_DP.get("i2").get(0) instanceof FileDP))
			return false;

		if (!portConsumesASingleListDP("i1"))
			return false;

		try {
//			Utility.reportToLoggerNode(Utility.myIP + ":>>>>>> " + instanceId + " ready to execute call, TRUEEE");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof FileDP) && (outputPortID_to_DP.get("o2") instanceof ListDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
