package translator;

import java.util.ArrayList;

import crm2.VMProvisioner;
import utility.Utility;
import webbench.WebbenchUtility;

public class SimpleEngine {

	public static void resetVM(String ip) throws Exception {
		// removes temporary files from FileDPs folder

		String cleanFileDPs = "sshpass -p" + Utility.passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + ip
				+ " 'rm -r -f /home/view/FileDPs/*; cp /home/view/FileDPs_minimum/* /home/view/FileDPs'";
		Utility.executeShellCommand(cleanFileDPs);

	}

	public static void executeRemotely(String command, String ip) throws Exception {
		command = "cd " + Utility.pathToFileDPsFolder + "; " + command;
		String cleanFileDPs = "sshpass -p" + Utility.passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + ip + " '" + command + "'";
		Utility.executeShellCommand(cleanFileDPs);
	}

	public static String commandToSend(ArrayList<String> fileNames, String destIP) {
		// sshpass -psystem scp -o StrictHostKeyChecking=no something view@52.35.155.8:/home/view
		String listOfFiles = "";
		for (String fileName : fileNames)
			listOfFiles += fileName + " ";
		listOfFiles.trim();

		String cmd = "sshpass -p" + Utility.passwdToVMs + " scp  -o StrictHostKeyChecking=no " + listOfFiles + "view@" + destIP + ":"
				+ Utility.pathToFileDPsFolder + ";";
		return cmd;
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();

		ArrayList<String> vmIPs = VMProvisioner.getAvailableVMIPs();
		VMProvisioner.waitOutPendingVMs();

		if (vmIPs == null || vmIPs.size() == 0) {
			System.out.println("no VMs in the pool!");
			return;
		}

		ArrayList<String> fileNames = new ArrayList();
		fileNames.add("template.hdr");

		String srcIP = "123";
		Utility.pathToFileDPsFolder = "/home/view/FileDPs/";

		fileNames.add("img0.fits");
		Utility.sendFilesXInFolderYToVM("/var/FileDPs_minimum/", fileNames, srcIP, vmIPs.get(0));

		fileNames.remove(1);
		fileNames.add("img1.fits");
		Utility.sendFilesXInFolderYToVM("/var/FileDPs_minimum/", fileNames, srcIP, vmIPs.get(1));

		fileNames.remove(1);
		fileNames.add("img2.fits");
		Utility.sendFilesXInFolderYToVM("/var/FileDPs_minimum/", fileNames, srcIP, vmIPs.get(2));

		fileNames.remove(1);
		fileNames.add("img3.fits");
		Utility.sendFilesXInFolderYToVM("/var/FileDPs_minimum/", fileNames, srcIP, vmIPs.get(3));

		String mProjectPPPmImgtblCmd = "mProjectPP img0.fits mProjectPPmImgtbl50.o2.79.0 template.hdr;"
				+ "mkdir tmp; cp mProjectPPmImgtbl50.o2.79.0.fits tmp; " + "cp mProjectPPmImgtbl50.o2.79.0_area.fits tmp;"
				+ "mImgtbl tmp mProjectPPmImgtbl50.o1.79; rm -r -f tmp;";
		ArrayList<String> outputFiles = new ArrayList<String>();
		outputFiles.add("mProjectPPmImgtbl50.o1.79.fits");
		outputFiles.add("mProjectPPmImgtbl50.o1.79_area.fits");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(2));
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(1));
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(0));
		executeRemotely(mProjectPPPmImgtblCmd, vmIPs.get(0));

		mProjectPPPmImgtblCmd = "mProjectPP img1.fits mProjectPPmImgtbl56.o2.79.0 template.hdr;"
				+ "mkdir tmp; cp mProjectPPmImgtbl56.o2.79.0.fits tmp; " + "cp mProjectPPmImgtbl56.o2.79.0_area.fits tmp;"
				+ "mImgtbl tmp mProjectPPmImgtbl56.o1.79; rm -r -f tmp;";

		outputFiles.clear();
		outputFiles.add("mProjectPPmImgtbl56.o1.79");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(0));

		outputFiles.clear();
		outputFiles.add("mProjectPPmImgtbl56.o2.79.0.fits");
		outputFiles.add("mProjectPPmImgtbl56.o2.79.0_area.fits");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(2));
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(4));

		executeRemotely(mProjectPPPmImgtblCmd, vmIPs.get(1));

		mProjectPPPmImgtblCmd = "mProjectPP img2.fits mProjectPPmImgtbl62.o2.79.0 template.hdr;"
				+ "mkdir tmp; cp mProjectPPmImgtbl62.o2.79.0.fits tmp; " + "cp mProjectPPmImgtbl62.o2.79.0_area.fits tmp;"
				+ "mImgtbl tmp mProjectPPmImgtbl62.o1.79; rm -r -f tmp;";
		outputFiles.clear();
		outputFiles.add("mProjectPPmImgtbl62.o1.79");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(0));

		outputFiles.clear();
		outputFiles.add("mProjectPPmImgtbl62.o2.79.0.fits");
		outputFiles.add("mProjectPPmImgtbl62.o2.79.0_area.fits");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(3));

		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(1));
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(4));

		executeRemotely(mProjectPPPmImgtblCmd, vmIPs.get(2));

		mProjectPPPmImgtblCmd = "mProjectPP img3.fits mProjectPPmImgtbl68.o2.79.0 template.hdr;"
				+ "mkdir tmp; cp mProjectPPmImgtbl68.o2.79.0.fits tmp; " + "cp mProjectPPmImgtbl68.o2.79.0_area.fits tmp;"
				+ "mImgtbl tmp mProjectPPmImgtbl68.o1.79; rm -r -f tmp;";
		outputFiles.clear();
		outputFiles.add("mProjectPPmImgtbl68.o1.79");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(0));

		outputFiles.clear();
		outputFiles.add("mProjectPPmImgtbl68.o2.79.0.fits");
		outputFiles.add("mProjectPPmImgtbl68.o2.79.0_area.fits");
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(3));
		mProjectPPPmImgtblCmd += commandToSend(outputFiles, vmIPs.get(0));

		executeRemotely(mProjectPPPmImgtblCmd, vmIPs.get(3));

		// /////////////////////////////////////// mMergeImgs:
		String mMergeImgs = "java mergeTblFiles mProjectPPmImgtbl62.o1.79 mProjectPPmImgtbl68.o1.79 "
				+ "mProjectPPmImgtbl56.o1.79 mProjectPPmImgtbl50.o1.79 mMergeImgs156.o1.79; " + "mOverlaps mMergeImgs156.o1.79 mOverlaps161.o1.79";
		executeRemotely(mMergeImgs, vmIPs.get(0));

		// //////////////////////////////////////////// mDiffFit and mFitExec:

		String mDiffFit = "mDiffFit mProjectPPmImgtbl62.o2.79.0.fits mProjectPPmImgtbl68.o2.79.0.fits diff.000000.000001.fits template.hdr; ";
		String mFitExec = "mkdir tmp; cp diff.000000.000001.fits tmp; cp diff.000000.000001_area.fits tmp; "
				+ "mFitExec mOverlaps161.o1.79 mFitExec248.o1.79 tmp; rm -r -f tmp;";

		String cmd = mDiffFit + mFitExec;
		outputFiles.clear();
		outputFiles.add("mFitExec248.o1.79");
		cmd += commandToSend(outputFiles, vmIPs.get(0));

		executeRemotely(cmd, vmIPs.get(3));

		mDiffFit = "mDiffFit mProjectPPmImgtbl56.o2.79.0.fits mProjectPPmImgtbl50.o2.79.0.fits diff.000002.000003.fits template.hdr; ";
		mFitExec = "mkdir tmp; cp diff.000002.000003.fits tmp; cp diff.000002.000003_area.fits tmp; "
				+ "mFitExec mOverlaps161.o1.79 mFitExec226.o1.79 tmp; rm -r -f tmp;";

		cmd = mDiffFit + mFitExec;
		outputFiles.clear();
		outputFiles.add("mFitExec226.o1.79");
		cmd += commandToSend(outputFiles, vmIPs.get(0));

		executeRemotely(cmd, vmIPs.get(2));

		mDiffFit = "mDiffFit mProjectPPmImgtbl62.o2.79.0.fits mProjectPPmImgtbl50.o2.79.0.fits diff.000000.000003.fits template.hdr; ";
		mFitExec = "mkdir tmp; cp diff.000000.000003.fits tmp; cp diff.000000.000003_area.fits tmp; "
				+ "mFitExec mOverlaps161.o1.79 mFitExec233.o1.79 tmp; rm -r -f tmp;";

		cmd = mDiffFit + mFitExec;
		outputFiles.clear();
		outputFiles.add("mFitExec233.o1.79");
		cmd += commandToSend(outputFiles, vmIPs.get(0));

		executeRemotely(cmd, vmIPs.get(1));

		mDiffFit = "mDiffFit mProjectPPmImgtbl68.o2.79.0.fits mProjectPPmImgtbl50.o2.79.0.fits diff.000001.000003.fits template.hdr; ";
		mFitExec = "mkdir tmp; cp diff.000001.000003.fits tmp; cp diff.000001.000003_area.fits tmp; "
				+ "mFitExec mOverlaps161.o1.79 mFitExec238.o1.79 tmp; rm -r -f tmp;";

		cmd = mDiffFit + mFitExec;
		outputFiles.clear();
		outputFiles.add("mFitExec238.o1.79");
		// cmd += commandToSend(outputFiles, vmIPs.get(0));

		executeRemotely(cmd, vmIPs.get(0));

		mDiffFit = "mDiffFit mProjectPPmImgtbl62.o2.79.0.fits mProjectPPmImgtbl56.o2.79.0.fits diff.000000.000002.fits template.hdr; ";
		mFitExec = "mkdir tmp; cp diff.000000.000002.fits tmp; cp diff.000000.000002_area.fits tmp; "
				+ "mFitExec mOverlaps161.o1.79 mFitExec243.o1.79 tmp; rm -r -f tmp;";

		cmd = mDiffFit + mFitExec;
		outputFiles.clear();
		outputFiles.add("mFitExec243.o1.79");
		cmd += commandToSend(outputFiles, vmIPs.get(0));

		executeRemotely(cmd, vmIPs.get(4));

		// ///////////////////////////////// mMergeFits:

		String mMergeFits = "java mMergeFits mFitExec233.o1.79 mFitExec243.o1.79 mFitExec248.o1.79 mFitExec226.o1.79 mFitExec238.o1.79 mMergeFits278.o1.79; ";
		String mBgModel = "mBgModel mMergeImgs156.o1.79 mMergeFits278.o1.79 mBgModel284.o1.79; ";
		String mBgExec = "mkdir tmp; mv mProjectPPmImgtbl50.o2.79.0.fits tmp; mv mProjectPPmImgtbl50.o2.79.0_area.fits tmp; "
				+ "mBgExec -p tmp mMergeImgs156.o1.79 mBgModel284.o1.79 " + Utility.pathToFileDPsFolder + "; rm -r -f tmp;";
		
		System.out.println("never got to run:\n" + mBgExec);
		mBgExec = "";
		cmd = mMergeFits + mBgModel + mBgExec;

		outputFiles.clear();
		outputFiles.add("mBgModel284.o1.79");
		cmd += commandToSend(outputFiles, vmIPs.get(1));
		cmd += commandToSend(outputFiles, vmIPs.get(2));
		cmd += commandToSend(outputFiles, vmIPs.get(3));
		
		executeRemotely(cmd, vmIPs.get(0));
		
	}

}
