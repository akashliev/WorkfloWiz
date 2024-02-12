package scheduler2;

import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.Element;

import repository.Repository;
import translator.SWLAnalyzer;
import utility.LoggingLevels;
import utility.Utility;
import webbench.WebbenchUtility;

public class Synchronizer {

	public static void synchronizeWorkflow(String wfName, ArrayList<String> listOfVMs) throws Exception {
		Utility.appendToLog("ssssssssssssssssssssssssynchronizing " + wfName);
		String dumpWFToFile = "cd " + Utility.pathToVIEW + "; " + "mysqldump --user=engine --password=engine --no-create-info "
				+ Utility.engineRepoDBname + " specs --where=\"name='" + wfName + "'\" > tmp.sql";
		Utility.executeShellCommand(dumpWFToFile);
		String scpFileOverToEachVM = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + Utility.pathToVIEW
				+ "tmp.sql view@IPADDRESS:/home/view";
		for (String ip : listOfVMs) {
			String scpToVM = scpFileOverToEachVM.replace("IPADDRESS", ip);
			System.out.println("scp commandddddddddddddddddddddddddddddddd:\n" + scpToVM);
			Utility.executeShellCommand(scpToVM);
		}

		String insertWFInDP = "sshpass -p '" + Utility.passwdToVMs
				+ "' ssh -o StrictHostKeyChecking=no view@IPADDRESS 'cp tmp.sql $CATALINA_HOME/webapps/VIEW/; cd $CATALINA_HOME/webapps/VIEW/; mysql -u engine -pengine "
				+ Utility.engineRepoDBname + " < tmp.sql; rm tmp.sql'";

		for (String ip : listOfVMs) {
			String insertWF = insertWFInDP.replace("IPADDRESS", ip);
			System.out.println("insertttttttttttttttttttttttttttttttt:\n" + insertWF);
			Utility.executeShellCommand(insertWF);
		}

	}

	public static void synchronizeRecursively(String wfName, ArrayList<String> listOfVMs) throws Exception {
		synchronizeWorkflow(wfName, listOfVMs);
		Element spec = Repository.getWorkflowSpecification(wfName);
		HashSet<String> allComponents = SWLAnalyzer.getAllReferencedComponents(spec);
		for (String currComp : allComponents) {
			synchronizeRecursively(currComp, listOfVMs);
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;

		ArrayList<String> listOfVMs = new ArrayList<String>();
		listOfVMs.add("192.168.29.131");
		listOfVMs.add("192.168.29.132");
		String wfName = "analyzeText10_3compsF";

		synchronizeRecursively(wfName, listOfVMs);


	}

}
