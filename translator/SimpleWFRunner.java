package translator;

import utility.Utility;

public class SimpleWFRunner extends Thread {

	private String ip;
	private int numberOfCopiesPerVM = -1;
	boolean properlyCopyData = true;
	int id;
	
	public SimpleWFRunner(int id, String ip, int numberOfCopiesPerVM, boolean properlyCopyData) {
		this.ip = ip;
		this.numberOfCopiesPerVM = numberOfCopiesPerVM;
		this.properlyCopyData = properlyCopyData;
		this.id = id;
	}

	public void run() {
		try {
			System.out.println("starting " + id);
//			System.out.println("triggering execution on " + ip);
			String destFolder = "/home/view/FileDPs";
			String cmd = "";
			
			if (properlyCopyData)
				cmd += "sshpass -p" + Utility.passwdToVMs + 
				" scp  -o StrictHostKeyChecking=no /home/andrey/2016_04_16_montage/files.tar.gz view@"
						+ ip + ":/home/view/FileDPs;\n";
			else {
				cmd += "cp /home/view/2016_data_for_debugging/* /home/view/FileDPs/";
				SimpleEngine3_scalingUp.executeRemotely(cmd, ip);
			}

			Utility.executeShellCommand(cmd);
			cmd = "";

			for (int i = 0; i < numberOfCopiesPerVM; i++) {
				cmd += "mkdir " + destFolder + i + ";\n cd " + destFolder + i + ";\n";
				cmd += "cp /home/view/FileDPs/files.tar.gz .;\n";
				cmd += "tar xvzf files.tar.gz;\n";
				cmd += "./script.sh";

			}

			SimpleEngine3_scalingUp.executeRemotely(cmd, ip);
			System.out.println("finished " + id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
