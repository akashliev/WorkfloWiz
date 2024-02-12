package utility;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONTokener;

import crm2.CRM;

/**
 * This class provides a number of static methods used by various classes in workflow engine. It hides low-level technical
 * details, such as JDBC connection to databases, converting org.w3c.dom objects to strings, etc.
 * 
 * @author Andrey Kashlev
 * 
 */
public class ParallelUtility extends Thread {

	private String threadWfName;
	private String threadVmIP;
	private ArrayList<String> threadComponentsToRunOnThisVM = null;
	private JSONObject threadSchedule;
	private String threadRunID;

	public ParallelUtility(String wfName, String vmIP, ArrayList<String> componentsToRunOnThisVM, JSONObject schedule, String runID) {
		threadWfName = wfName;
		threadVmIP = vmIP;
		threadComponentsToRunOnThisVM = componentsToRunOnThisVM;
		threadSchedule = schedule;
		threadRunID = runID;
	}

	public static String json2FlatString(JSONObject jo) throws Exception {
		JSONTokener tokener = new JSONTokener(jo.toString());
		JSONObject finalResult = new JSONObject(tokener);
		return finalResult.toString();
	}

	public static String executeShellCommand(String command) throws Exception {
		// System.out.println("executing command:\n" + command);
		if (command.contains("./mahout")) {
			// writeToFile(command, "/home/view/log");
		}
		Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		StringBuffer sb = new StringBuffer();

		String line = reader.readLine();
		sb.append(line);
		while (line != null) {
			line = reader.readLine();
			if (line != null)
				sb.append(line);
		}

		String result = sb.toString();
		return result;
	}

	public static void reportToLoggerNode(String msg) throws Exception {
		if (CRM.logNodeIP != null) {
			if (CRM.logNodeIP.trim().equals(getIpGlobal())) {
				writeToFile(msg + "\n", "/home/view/log");
				return;
			}
			String command = "sshpass -p \'system\' ssh  -o StrictHostKeyChecking=no view@" + CRM.logNodeIP + " 'echo '" + msg + "' >> log'";
			String res = executeShellCommand(command);
		}
	}

	public static String getIpGlobal() throws Exception {
		URL whatismyip = new URL("http://checkip.amazonaws.com");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine();
			return ip;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeToFile(String text, String pathToFile) {
		try {
			FileWriter fw = new FileWriter(pathToFile, true); // the true will append the new data
			fw.write(text);// appends the string to the file
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}

	// public String runPieceOfWorkflowLocally(String wfName, String vmIP, ArrayList<String> componentsToRunOnThisVM, JSONObject
	// schedule,
	// String runID) throws Exception {
	//
	// String commaSeparatedList = componentsToRunOnThisVM.toString().replace("[", "").replace("]", "").replace(" ", "");
	// String runWFinVM = null;
	//
	// runWFinVM = "cd $CATALINA_HOME/webapps/VIEW/WEB-INF; java" + " -cp classes:lib/* translator.Experiment \"" + wfName +
	// "\" \"" + runID
	// + "\" \"" + vmIP + "\" \"" + ParallelUtility.json2FlatString(schedule) + "\" \"" + commaSeparatedList + "\" ";
	// System.out.println("local execution " + componentsToRunOnThisVM.toString() + " on " + vmIP);
	// reportToLoggerNode("local execution " + componentsToRunOnThisVM.toString() + " on " + vmIP);
	// String result = ParallelUtility.executeShellCommand(runWFinVM);
	// return result;
	// }

	public String runPieceOfWorkflowOnVM(String wfName, String vmIP, ArrayList<String> componentsToRunOnThisVM, JSONObject schedule, String runID)
			throws Exception {

		String commaSeparatedList = componentsToRunOnThisVM.toString().replace("[", "").replace("]", "").replace(" ", "");
		String runWFinVM = null;

		// runWFinVM = "sshpass -p" + Utility.passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + vmIP
		// + " 'cd $CATALINA_HOME/webapps/VIEW/WEB-INF; java" + " -cp classes:lib/* translator.Experiment \"" + wfName + "\" \"" +
		// runID
		// + "\" \"" + vmIP + "\" \"" + ParallelUtility.json2FlatString(schedule) + "\" \"" + commaSeparatedList + "\"' ";
		runWFinVM = "cd $CATALINA_HOME/webapps/VIEW/WEB-INF; java" + " -cp classes:lib/* translator.Experiment \"" + wfName + "\" \"" + runID
				+ "\" \"" + vmIP + "\" \"" + ParallelUtility.json2FlatString(schedule) + "\" \"" + commaSeparatedList + "\"";

		if (!vmIP.equals(Utility.myIP)) {
			reportToLoggerNode(Utility.myIP + ": remote run request on " + vmIP + " componentsToRun:" + componentsToRunOnThisVM);
			runWFinVM = "sshpass -p" + Utility.passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + vmIP + " '" + runWFinVM + "'";
		} else
			reportToLoggerNode(Utility.myIP + ": local run request to run " + componentsToRunOnThisVM);

		// System.out.println("nnnnnnetwork: execute" + componentsToRunOnThisVM.toString() + " on " + vmIP);
		// reportToLoggerNode("nnnnnnetwork: execute" + componentsToRunOnThisVM.toString() + " on " + vmIP);
//		ParallelUtility.appendToLog(runWFinVM);
		String result = ParallelUtility.executeShellCommand(runWFinVM);
		return result;
	}

	public void run() {
		try {
			runPieceOfWorkflowOnVM(threadWfName, threadVmIP, threadComponentsToRunOnThisVM, threadSchedule, threadRunID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void appendToLog(String record) {
		if (Utility.loggingLevel.equals(LoggingLevels.none))
			return;
		if (Utility.loggingLevel.equals(LoggingLevels.detailed)) {
			// writing to log file (disabled to improve performance):
			/*
			 * BufferedWriter bw = null; try { String prefix = System.getenv("CATALINA_HOME"); prefix = prefix + "/webapps/VIEW/";
			 * String fullPathToLog = prefix + pathToLog; bw = new BufferedWriter(new FileWriter(fullPathToLog, true));
			 * bw.write(record); bw.newLine(); bw.flush();
			 * 
			 * } catch (IOException ioe) { ioe.printStackTrace(); } finally { if (bw != null) try { bw.close(); } catch
			 * (IOException ioe2) { } }
			 */
			System.out.println(record);
		}
		if (Utility.loggingLevel.equals(LoggingLevels.onlySystemOutput))
			System.out.println(record);
	}

}