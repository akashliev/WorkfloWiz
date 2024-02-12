package translator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import repository.Repository;
import utility.LoggingLevels;
import utility.Utility;
import webbench.WebbenchUtility;

public class CloudWFRunner {

	private static String wfName = null;
	private static String runID = null;

	private static void prepareInputs(Experiment experiment) {
		System.out.println(experiment.name);
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;

		if (args.length != 0) {
			wfName = args[0];
			runID = args[1];
		} else {
			wfName = "analyzeText10_3comps";
			runID = Utility.createUniqueRunID();
		}

		System.out.println("running Cloud workflow " + wfName + " " + runID);

		Element experimentSpec = Repository.getWorkflowSpecification(wfName);

		Experiment experiment = new Experiment(experimentSpec);

		//prepareInputs(experiment);
		System.out.println(Utility.createUniqueRunID());
//		JSONObject outputStub2DPNames = experiment.run(runID, false);

//		System.out.println("map: " + outputStub2DPNames);

	}

}
