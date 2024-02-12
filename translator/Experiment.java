package translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;
import org.w3c.dom.Element;

import repository.Repository;
import utility.LoggingLevels;
import utility.Utility;
import webbench.WebbenchUtility;
import crm2.CRM;
import dataProduct.DataProduct;

/**
 * This class represents executable workflows, i.e. those workflows that have input data specified in them. Input data is
 * specified through mapping that has the following form: someDataName : componentInstanceId.portID where someDataName is a name
 * of the data product, which is used as a unique identified of each data product in the toyDPM.
 * 
 * @author Andrey Kashlev
 * 
 */
public class Experiment {

	public static String myIP = null;
	public static JSONObject schedule = null;

	public String name;
	public String rootWorkflowName = null;

	public Element experimentSpec;

	public Element workflowSpec;
	public Workflow rootWorkflow = null;

	public HashMap<String, ArrayList<String>> portID_to_dpID = new HashMap<String, ArrayList<String>>();

	public Experiment(Element experimentSpec) throws Exception {

		this.name = ((Element) experimentSpec.getElementsByTagName("experiment").item(0)).getAttribute("name").trim();
		this.workflowSpec = SWLAnalyzer.translateExperimentSpecIntoWorkflowSpec(experimentSpec);
		// System.out.println(Utility.nodeToString(this.workflowSpec));
		this.portID_to_dpID = SWLAnalyzer.getExperiment_portID_to_dpID(experimentSpec);
		rootWorkflowName = "wfFromExperiment" + name;
		rootWorkflow = Translator.createExecutableWorkflow(rootWorkflowName, workflowSpec);
	}

	public JSONObject run(String runID, boolean registerIntermediateDPs) throws Exception {

		JSONObject outputStubNamesToDataNames = new JSONObject();
		Utility.appendToLog("running experiment " + name);
		// rootWorkflow = Translator.createExecutableWorkflow(rootWorkflowName, workflowSpec);
		if (rootWorkflow == null) {
			return null;
		}

		for (String port_id : SWLAnalyzer.getWorkflowInputPorts(workflowSpec)) {
			ArrayList<String> dataNames = portID_to_dpID.get(port_id);

			ArrayList<DataProduct> dps_corresp_to_dataNames = new ArrayList<DataProduct>();
			for (String dataName : dataNames) {

				if (Utility.getDataProduct(dataName) == null) {
					Utility.appendToLog("ERROR: cannot retrieve the data product from DPM, id: " + dataName);
				}
				dps_corresp_to_dataNames.add(Utility.getDataProduct(dataName));

			}
			rootWorkflow.inputPortID_to_DP.put(port_id, dps_corresp_to_dataNames);
		}
		rootWorkflow.run(runID, registerIntermediateDPs);

		for (String port_id : SWLAnalyzer.getWorkflowOutputPorts(workflowSpec)) {
			// System.out.println("ddddddddddddddddddp id is supposed to be:" + portID_to_dpID.get(port_id));
			if (registerIntermediateDPs)
				Utility.deleteDPFromDatabase(rootWorkflow.outputPortID_to_DP.get(port_id).dataName);
			// next line assumes that an output port cannot be connected to more than one output stub:
			String stubName = portID_to_dpID.get(port_id).get(0);
			String dataName = stubName + "." + runID;
			rootWorkflow.outputPortID_to_DP.get(port_id).dataName = dataName;
			outputStubNamesToDataNames.put(stubName, dataName);

			Utility.registerDataProduct(rootWorkflow.outputPortID_to_DP.get(port_id));
		}
		return outputStubNamesToDataNames;
	}

	public JSONObject runInTheCloud(String runID, String myIP, JSONObject schedule, ArrayList<String> componentsToBeRunOnThisVM) throws Exception {
		JSONObject outputStubNamesToDataNames = new JSONObject();
		// Utility.appendToLog("running experiment in cloud: " + name);
		// rootWorkflow = Translator.createExecutableWorkflow(rootWorkflowName, workflowSpec);
		if (rootWorkflow == null) {
			return null;
		}
		((GraphBasedWorkflow) rootWorkflow).executeInCloud = true;
		((GraphBasedWorkflow) rootWorkflow).schedule = schedule;
		((GraphBasedWorkflow) rootWorkflow).myIP = myIP;
		((GraphBasedWorkflow) rootWorkflow).componentsToBeRunOnThisVM = componentsToBeRunOnThisVM;

		// for (String port_id : SWLAnalyzer.getWorkflowInputPorts(workflowSpec)) {
		// String dataName = portID_to_dpID.get(port_id);
		// System.out.println("portId: " + port_id);
		// System.out.println("dataName: " + dataName);
		// }
		System.out.println(myIP + " calls rootWorkflow.run");
//		Utility.reportToLoggerNode(myIP + " calls rootWorkflow.run");
		rootWorkflow.run(runID, false);
		return outputStubNamesToDataNames;
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;
		Utility.executeInCloud = true;

		String name = null;
		String runID = null;
		// String myIP = null;
		// JSONObject schedule = null;
		String componentsToRun = null;

		if (args.length == 5) {
			name = args[0].trim();
			runID = args[1].trim();
			myIP = args[2].trim();
			Utility.myIP = myIP;
			// System.out.println("myIP: " + myIP);
			componentsToRun = args[4];
			System.out.println("name: " + name + ",  runID: " + runID + ", components to run: " + componentsToRun);
			if(Utility.runID_to_wfName == null)
				Utility.runID_to_wfName = new HashMap<String, String>();
			Utility.runID_to_wfName.put(runID, name);

			schedule = new JSONObject(args[3].trim());

			CRM.logNodeIP = schedule.getString("logNodeIP").trim();

			Iterator it = schedule.keys();
			while (it.hasNext()) {
				String ip = (String) schedule.get((String) it.next());
				if (!CRM.listOfVMs.contains(ip))
					CRM.listOfVMs.add(ip);
			}
			// System.out.println("schedule: \n" + Utility.json2String(schedule, 4));

		}
		Utility.reportToLoggerNode(myIP + ": Experiment.main method to execute " + componentsToRun);
		Utility.reportToLoggerNode(myIP + ": 11");
		// String jsonStr =
		// "{\"ShuffleYellow10\":\"192.168.29.132\",\"ShuffleOrange6\":\"192.168.29.132\",\"MergeTwo51\":\"192.168.29.132\","
		// + "\"ShuffleRed2\":\"192.168.29.131\",\"dummy\":\"192.168.29.131\"}";
		// JSONObject jo = new JSONObject(jsonStr);
		//
		// System.out.println(Utility.json2String(jo, 4));

		Element experimentSpec = Repository.getWorkflowSpecification(name);
		Experiment experiment = new Experiment(experimentSpec);
		Utility.reportToLoggerNode(myIP + ": 22");

		ArrayList<String> componentsToBeRunOnThisVM = new ArrayList<String>();
		Utility.reportToLoggerNode(myIP + ": 33");
		for (String currComp : componentsToRun.split(",")) {
			componentsToBeRunOnThisVM.add(currComp);
		}
		Utility.reportToLoggerNode(myIP + ": 44: " + componentsToBeRunOnThisVM);

		JSONObject outputStubNamesToDataNames = null;
		outputStubNamesToDataNames = experiment.runInTheCloud(runID, myIP, schedule, componentsToBeRunOnThisVM);
	}

}
