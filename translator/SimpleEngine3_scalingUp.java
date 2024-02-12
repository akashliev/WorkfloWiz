package translator;

import java.util.ArrayList;
import java.util.List;

import test.tscbdw2015.cloudManyVMs.verifyJPEGwasCreated;
import utility.Utility;
import webbench.WebbenchUtility;
import crm2.VMProvisioner;

public class SimpleEngine3_scalingUp {

	public static void resetVM(String ip) throws Exception {
		// removes temporary files from FileDPs folder

		String cleanFileDPs = "sshpass -p" + Utility.passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + ip
				+ " 'rm -r -f /home/view/FileDPs/*; cp /home/view/FileDPs_minimum/* /home/view/FileDPs'";
		Utility.executeShellCommand(cleanFileDPs);

	}

	public static void cleanAllVMs(ArrayList<String> vmIPs) throws Exception {
		for (String ip : vmIPs) {
			String cmd = "cd /home/view/;\n";
			cmd += "rm -r -f FileDP*;\n";
			cmd += "mkdir FileDPs";
			executeRemotely(cmd, ip);
		}

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
		long startTime = System.currentTimeMillis();

		// int numberOfVMs = 2;
		int copiesPerVM = 20;
		boolean properlyCopyData = false;

		String vmType = "t2.micro";

		WebbenchUtility.initializeWebbenchConfig();

		ArrayList<String> vmIPs = VMProvisioner.getAvailableVMIPs();
		VMProvisioner.waitOutPendingVMs();
		System.out.println("found " + vmIPs.size() + " VMs");

		if (vmIPs == null || vmIPs.size() == 0) {
			System.out.println("ERROR: VM pool is empty!");
			return;
		}

		// **************** create threads: *****************

		List threads = new ArrayList();
		for (String ip : vmIPs) {
			SimpleWFRunner currentRunner = new SimpleWFRunner(vmIPs.indexOf(ip), ip, copiesPerVM, properlyCopyData);

			currentRunner.start();
			threads.add(currentRunner);
		}

		for (int i = 0; i < threads.size(); i++)
			((Thread) threads.get(i)).join();

		long endTime = System.currentTimeMillis();

		// size of montage's rawdir is 21.1 Mb.
		// Total size of processed data is:
		if(!verifyJPEGwasCreated.verify(vmIPs.get(0))){
			System.out.println("ERROR: VERIFICATION OF JPEG FAILED!!!");
			return;
		}
		System.out.println("***************************SUMMARY*****************************");
		System.out.println("properlyCopyData: " + properlyCopyData);
		System.out.println("number of VMs:" + vmIPs.size());
		System.out.println("copies per VM: " + copiesPerVM);
		System.out.println("total data size: " + 21 * vmIPs.size() * copiesPerVM + " Mb");
		System.out.println("Total time: " + (endTime - startTime) / 1000 + " s");

	}

}
