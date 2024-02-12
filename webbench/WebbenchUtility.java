package webbench;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lambda.calculus.CoercionInserter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import repository.Repository;
import scheduler2.Synchronizer;
import scheduler2.VIEWScheduler;
import toyDPM.DataProductManager;
import translator.Experiment;
import translator.SWLAnalyzer;
import utility.LoggingLevels;
import utility.ParallelUtility;
import utility.Utility;
import crm2.CRM;
import dataProduct.FileDP;

/**
 * 
 * @author Andrey Kashlev
 * 
 */
public class WebbenchUtility {
	public static LoggingLevels loggingLevel = LoggingLevels.onlySystemOutput;

	public static boolean useLocalDPM = true;
	public static boolean useLocalEngine = true;

	public static String initializeWebbenchConfig() throws Exception {
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;
		String prefix = System.getenv("CATALINA_HOME");
		Utility.pathToVIEW = prefix + "webapps/VIEW/";
		Utility.pathToLog = prefix + "webapps/VIEW/" + "systemFiles/log";
		// Utility.pathToFileDPsFolder = prefix + "/webapps/VIEW/" + "FileDPs/";
		Utility.pathToFileDPsFolder = "/var/FileDPs/";
		// Utility.pathToFileDPsFolder = "/mnt/mntEBSDevice/FileDPs/";
		Utility.passwdToVMs = "system";
		
		// 2023/07/19 temporarily added this shortcut line becuase 
		// getenv doesn't see CATALINA_HOME since I didn't restart the computer yet
		// this lead to prefix being null:
		prefix = "/Users/andrii/software/apache-tomcat-9.0.78/";
		Utility.pathToConfigFile = prefix + "/webapps/VIEW/" + "systemFiles/config";
		Document config = Utility.readFileAsDocument(Utility.pathToConfigFile);
//		Utility.swlSchemaURL = ((Element) config.getDocumentElement().getElementsByTagName("swlSchemaURL").item(0)).getTextContent().trim();
		Utility.experimentSchemaURL = ((Element) config.getDocumentElement().getElementsByTagName("experimentSchemaURL").item(0)).getTextContent()
				.trim();
		Utility.dbURL = ((Element) config.getDocumentElement().getElementsByTagName("engineRepoDBURL").item(0)).getTextContent().trim();
		Utility.engineRepoDBname = ((Element) config.getDocumentElement().getElementsByTagName("engineRepoDBName").item(0)).getTextContent().trim();
		Utility.login = ((Element) config.getDocumentElement().getElementsByTagName("engineRepoLogin").item(0)).getTextContent().trim();
		Utility.password = ((Element) config.getDocumentElement().getElementsByTagName("engineRepoPasswd").item(0)).getTextContent().trim();
		Utility.tableNameForSWLSpecs = ((Element) config.getDocumentElement().getElementsByTagName("engineRepoTableName").item(0)).getTextContent()
				.trim();

		if (((Element) config.getDocumentElement().getElementsByTagName("useLocalDPM").item(0)).getTextContent().trim().equals("false"))
			useLocalDPM = false;

		if (((Element) config.getDocumentElement().getElementsByTagName("useLocalEngine").item(0)).getTextContent().trim().equals("false"))
			useLocalEngine = false;

//		Utility.dplSchemaURL = ((Element) config.getDocumentElement().getElementsByTagName("dplSchemaURL").item(0)).getTextContent().trim();
		Utility.dplSpecsTableName = ((Element) config.getDocumentElement().getElementsByTagName("dplSpecsTableName").item(0)).getTextContent()
				.trim();
		System.out.println("initialized successfully: useBuiltinDPM? " + useLocalDPM + " useLocalEngine? " + useLocalEngine);

		DataProductManager.viewTypesToMySQLTypes.put("String", "TEXT");
		DataProductManager.viewTypesToMySQLTypes.put("Boolean", "TINYINT(1)");
		// DataProductManager.viewTypesToMySQLTypes.put("Boolean", "TINYINT");
		DataProductManager.viewTypesToMySQLTypes.put("Long", "BIGINT");
		DataProductManager.viewTypesToMySQLTypes.put("Integer", "INTEGER");
		// DataProductManager.viewTypesToMySQLTypes.put("Integer", "INT");
		DataProductManager.viewTypesToMySQLTypes.put("Float", "FLOAT");
		DataProductManager.viewTypesToMySQLTypes.put("Double", "DOUBLE");
		DataProductManager.viewTypesToMySQLTypes.put("Decimal", "DECIMAL");
		DataProductManager.viewTypesToMySQLTypes.put("Date", "DATE");

		return "success";
	}

	public static String nodeToString(Node node) {
		String resultStr = "";
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(node);
			trans.transform(source, result);
			resultStr = sw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return resultStr;
	}

	public static JSONObject getWFandDPMetadata() throws Exception {
		JSONObject result = new JSONObject();

		JSONArray wfMetadata = null;
		JSONArray dpMetadata = null;
		JSONArray wsMetadata = null;
		System.out.println("WBUtility1");
		if (useLocalEngine)
			wfMetadata = Utility.getWorkflowsMetadataFromDB();
		System.out.println("WBUtility2");
		result.put("workflows", wfMetadata);
		System.out.println("WBUtility3");
		if (useLocalDPM)
			dpMetadata = DataProductManager.getAllDataProductInfos();
		// System.out.println("all DP metadata: \n" + dpMetadata);
		result.put("dataProducts", dpMetadata);

		System.out.println("WBUtility4");
		wsMetadata = Utility.getWSsMetadataFromDB();
		System.out.println("WBUtility5");
		result.put("webservices", wsMetadata);
		return result;
	}

	public static JSONArray getAllDPsMetadata() throws Exception {
		JSONArray dpMetadata = null;

		if (useLocalDPM)
			dpMetadata = DataProductManager.getAllDataProductInfos();

		return dpMetadata;
	}

	public static String registerWorkflow(Document spec, Document diagram, boolean overwriteAllowed) throws Exception {
		String result = "undefined";

		if (useLocalEngine)
			result = Repository.registerWorkflow(spec, diagram, overwriteAllowed);

		return result;
	}

	public static String getWorkflowDiagram(String name) throws Exception {
		if (useLocalEngine)
			return Repository.getWorkflowDiagram(name);

		return null;
	}

	public static JSONObject getWorkflowMetadata(String name) throws SQLException {
		if (useLocalEngine)
			return Repository.getWorkflowMetadata(name);

		return null;
	}

	public static void registerDataProduct(String name, String type, String value) throws Exception {
		if (useLocalDPM)
			DataProductManager.createAndRegisterDataProduct(name, type, value);
	}

	public static void deleteDataProduct(String dataName) {
		if (useLocalDPM)
			Utility.deleteDPFromDatabase(dataName);
	}

	public static JSONObject runWorkflow(String name, String runID, boolean saveIntermediateResults, boolean insertCoercions) throws Exception {
		System.out.println("WebbenchUtility running workflow " + name + " runID: " + runID + " insCoercions? " + insertCoercions);

		JSONObject outputStubNamesToDataNames = new JSONObject();
		Element experimentSpec = null;
		if (useLocalEngine) {
			experimentSpec = Repository.getWorkflowSpecification(name);
			if (insertCoercions) {
				Element experimentSpecWithCoercions = null;
				Document doc = CoercionInserter.insertCoercionsInSWL(experimentSpec.getOwnerDocument());
				System.out.println("docWC:");
				System.out.println(Utility.nodeToString(doc));

				if (doc != null)
					experimentSpecWithCoercions = doc.getDocumentElement();
				if (experimentSpecWithCoercions != null)
					experimentSpec = experimentSpecWithCoercions;
			}
			Experiment experiment = new Experiment(experimentSpec);
			outputStubNamesToDataNames = experiment.run(runID, saveIntermediateResults);
			return outputStubNamesToDataNames;
		}
		return null;
	}

	public static JSONObject runWorkflowInTheCloud(String name, String runID, boolean saveIntermediateResults, String config) throws Exception {
		// config contains a string of ips, separated by | and logger being the first ip in the list. The string should start with
		// vmips:
		// preparing to run WF in the cloud:

		Utility.executeInCloud = true;
		Element experimentSpec = Repository.getWorkflowSpecification(name);

		if (config.contains("default")) {
			CRM.listOfVMs = new ArrayList<String>();
			CRM.listOfVMs.add("54.197.7.78");
			CRM.logNodeIP = "54.197.7.78";
			// listOfVMs.add("172.30.12.199");
			// listOfVMs.add("192.168.29.132");
		} else if (config.contains(".")) {
			CRM.listOfVMs = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(config);
			// iterate through st object to get more tokens from it
			while (st.hasMoreElements()) {
				String currIP = st.nextElement().toString();
				CRM.listOfVMs.add(currIP);
			}
			CRM.logNodeIP = CRM.listOfVMs.get(0);
		}
		// first, ensure that each VM has the most recent version of this workflow:

		JSONObject schedule = null;

		ArrayList<String> allInstanceIds = SWLAnalyzer.getAllInstanceIds(experimentSpec);
		if (allInstanceIds.size() > 1)
			schedule = VIEWScheduler.schedule(experimentSpec, CRM.listOfVMs);
		else {
			schedule = new JSONObject();
			schedule.put(allInstanceIds.get(0), CRM.listOfVMs.get(0));
		}
		// ////////////////let's customize schedule (for testing purposes:):
		// schedule.remove("ShuffleYellow10");
		// schedule.put("ShuffleYellow10", "192.168.29.131");

		// ////////////////
		if (CRM.logNodeIP != null)
			schedule.put("logNodeIP", CRM.logNodeIP);
		else {
			schedule.put("masterNodeIPGlobal", Utility.getIpGlobal());
			schedule.put("masterNodeIPlocal", Utility.getIpLocal());
		}

		System.out.println(Utility.json2String(schedule, 4));
		Utility.reportToLoggerNode(Utility.json2String(schedule, 4));
		long startTime = System.currentTimeMillis();
		disseminateWFSpecsInTheCloud(name, CRM.listOfVMs);
		long afterSpecsDissem = System.currentTimeMillis();
		disseminateDPsInTheCloud(name, schedule);
		long endTime = System.currentTimeMillis();

		System.out.println("disseminating specs took: " + (afterSpecsDissem - startTime) / 1000);
		System.out.println("dissem. dps took: " + (endTime - afterSpecsDissem) / 1000);

		// running workflow in the cloud (action starts here):
		System.out.println("about to run all workflows in cloud");
		Utility.reportToLoggerNode("*********************************************STARTING EXECUTION************************");
		// components "fully connected" to input data products (do not depend on output of other components):
		Element specTranslated = SWLAnalyzer.translateExperimentSpecIntoWorkflowSpec(experimentSpec);
		HashSet<String> inputComponentIds = SWLAnalyzer.getComponentsWithAllInputDataAvailable(specTranslated);
		List threads = new ArrayList();
		for (String vmIP : CRM.listOfVMs) {
			ArrayList<String> inputComponentsScheduledOnThisVM = SWLAnalyzer.getAllInputCompntsScheduledOnThisVM(inputComponentIds, vmIP, schedule);

			if (vmIP.trim().equals(Utility.getIpGlobal()))
				Utility.runPieceOfWorkflowLocally(name, vmIP, inputComponentsScheduledOnThisVM, schedule, runID);
			else {
				// Utility.runPieceOfWorkflowOnVM(name, vmIP, inputComponentsScheduledOnThisVM, schedule, runID);
				if (inputComponentsScheduledOnThisVM.size() > 0) {
					// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>vmIP " + vmIP + "\n>>>inputComponentsScheduledOnThisVM: "
					// + inputComponentsScheduledOnThisVM);
					ParallelUtility pu = new ParallelUtility(name, vmIP, inputComponentsScheduledOnThisVM, schedule, runID);
					pu.start();
					threads.add(pu);
				}
			}
		}
		for (int i = 0; i < threads.size(); i++)
			((Thread) threads.get(i)).join();

		Utility.createDummyFile(runID);

		String passFailFlag = "PASS";
		String computeGradeCompId = null;

		for (String instId : allInstanceIds)
			if (instId.contains("ComputeGrade"))
				computeGradeCompId = instId;

		if (computeGradeCompId != null) {
			String computeGradeVMip = schedule.getString(computeGradeCompId);
			if (!Utility.checkIfFileExists(computeGradeVMip))
				passFailFlag = "FAILLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL";
		}

		System.out.println("execution ended " + runID);
		System.out.println(passFailFlag);
		Utility.reportToLoggerNode("----------execution ended, runID: " + runID);
		Utility.reportToLoggerNode(passFailFlag);

		String componentProducingFinalRes = SWLAnalyzer.getOutputFileNames(experimentSpec).get(0);
		System.out.println(componentProducingFinalRes);
		String vmIPwhereGradeResides = schedule.getString(componentProducingFinalRes.substring(0, componentProducingFinalRes.indexOf(".")));

		Utility.copyFileFromVM(componentProducingFinalRes + "." + runID, "grade", vmIPwhereGradeResides);
		Utility.reportToLoggerNode("----------file has been copied");
		JSONObject outputStubNamesToDataNames = new JSONObject();
		outputStubNamesToDataNames.put("outputDP0", "grade");
		// outputStubNamesToDataNames = experiment.run(runID, saveIntermediateResults);
		return outputStubNamesToDataNames;
	}

	public static void disseminateWFSpecsInTheCloud(String name, ArrayList<String> listOfVMs) throws Exception {
		if (!Utility.disseminateSpecs)
			return;
		Element experimentSpec = Repository.getWorkflowSpecification(name);
		Synchronizer.synchronizeRecursively(name, listOfVMs);
	}

	public static void disseminateDPsInTheCloud(String name, JSONObject schedule) throws Exception {
		if (!Utility.disseminateDPs)
			return;

		Element experimentSpec = Repository.getWorkflowSpecification(name);
		ArrayList<String> inputComponentIds = SWLAnalyzer.getAllInputComponentIds(experimentSpec);
		HashMap<String, ArrayList<String>> vmIPsToDPsResidingThere = new HashMap();
		for (String inputComponId : inputComponentIds) {
			// send data prod to inputComponId:
			ArrayList<String> dpNames = SWLAnalyzer.getAllDPNamesForThisInputComponent(inputComponId, experimentSpec);
			for (String dpName : dpNames) {
				String ip = schedule.getString(inputComponId);

				// String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp " + Utility.pathToFileDPsFolder + dpName +
				// " view@" + ip
				// + ":software/apache-tomcat-7.0.47/webapps/VIEW/FileDPs";
				if (!vmIPsToDPsResidingThere.keySet().contains(ip) || !vmIPsToDPsResidingThere.get(ip).contains(dpName)) {

					if (vmIPsToDPsResidingThere.keySet().contains(ip)) {
						ArrayList<String> dpNamesInThisVM = vmIPsToDPsResidingThere.get(ip);
						dpNamesInThisVM.add(dpName);
					} else {
						ArrayList<String> dpNamesInThisIP = new ArrayList<String>();
						dpNamesInThisIP.add(dpName);
						vmIPsToDPsResidingThere.put(ip, dpNamesInThisIP);
					}

					// vmIPsToDPsResidingThere.put(ip, value)
					if (!ip.trim().equals(Utility.getIpGlobal()) && Utility.getDataProduct(dpName) instanceof FileDP)
						Utility.sendFileToVM(dpName, Utility.getIpGlobal(), ip);
				}
			}
			// invoke run method with schedule as input

		}
	}

	public static String getDataProductValueAsPrettyHTML(String dataName) throws Exception {
		String dplAsString = null;
		if (Utility.isFile(dataName)) {
			if (dataName.trim().equals("grade") || dataName.trim().equals("grade50")) {
				String gradeStr = Utility.readFromAFile(Utility.pathToFileDPsFolder + dataName);
				return gradeStr;
			}
			return "this data product can be found at " + Utility.pathToFileDPsFolder + dataName;
		}
		if (useLocalDPM)
			dplAsString = DataProductManager.getDataProductDPL(dataName);

		if (dplAsString == null)
			return "";

		Document dpl = XMLParser.getDocument(dplAsString);
		// if it's a scalar type:
		if (dpl.getDocumentElement().getElementsByTagName("scalarType").getLength() > 0) {
			String typeDeclaredinDPL = ((Element) dpl.getDocumentElement().getElementsByTagName("scalarType").item(0)).getTextContent().trim();

			if (typeDeclaredinDPL.equals("String") || typeDeclaredinDPL.equals("Decimal") || typeDeclaredinDPL.equals("Integer")
					|| typeDeclaredinDPL.equals("NonPositiveInteger") || typeDeclaredinDPL.equals("NegativeInteger")
					|| typeDeclaredinDPL.equals("NonNegativeInteger") || typeDeclaredinDPL.equals("UnsignedLong")
					|| typeDeclaredinDPL.equals("UnsignedInt") || typeDeclaredinDPL.equals("UnsignedShort")
					|| typeDeclaredinDPL.equals("UnsignedByte") || typeDeclaredinDPL.equals("PositiveInteger")
					|| typeDeclaredinDPL.equals("Double") || typeDeclaredinDPL.equals("Float") || typeDeclaredinDPL.equals("Long")
					|| typeDeclaredinDPL.equals("Int") || typeDeclaredinDPL.equals("Short") || typeDeclaredinDPL.equals("Byte")
					|| typeDeclaredinDPL.equals("Boolean"))
				return ((Element) dpl.getDocumentElement().getElementsByTagName("value").item(0)).getTextContent().trim();

			// if (((Element)
			// dpl.getDocumentElement().getElementsByTagName("scalarType").item(0)).getTextContent().trim().equals("Decimal"))
			// return ((Element) dpl.getDocumentElement().getElementsByTagName("value").item(0)).getTextContent().trim();
			// if (((Element)
			// dpl.getDocumentElement().getElementsByTagName("scalarType").item(0)).getTextContent().trim().equals("Integer"))
			// return ((Element) dpl.getDocumentElement().getElementsByTagName("value").item(0)).getTextContent().trim();
			// if (((Element)
			// dpl.getDocumentElement().getElementsByTagName("scalarType").item(0)).getTextContent().trim().equals("Int"))
			// return ((Element) dpl.getDocumentElement().getElementsByTagName("value").item(0)).getTextContent().trim();
			// if (((Element)
			// dpl.getDocumentElement().getElementsByTagName("scalarType").item(0)).getTextContent().trim().equals("Float"))
			// return ((Element) dpl.getDocumentElement().getElementsByTagName("value").item(0)).getTextContent().trim();
			// if (((Element)
			// dpl.getDocumentElement().getElementsByTagName("scalarType").item(0)).getTextContent().trim().equals("Boolean"))
			// return ((Element) dpl.getDocumentElement().getElementsByTagName("value").item(0)).getTextContent().trim();

		}

		// if it's a relation:
		if (dpl.getDocumentElement().getElementsByTagName("relation").getLength() > 0) {
			String tableName = dpl.getDocumentElement().getElementsByTagName("tableName").item(0).getTextContent();
			return Utility.Table2String(tableName);
		}

		// if it's an XML DP:
		if (dpl.getElementsByTagName("xmlElement").getLength() > 0) {
			System.out.println("ddddddddddddddddpl:\n" + Utility.nodeToString(dpl));
			String valueStr = dpl.getElementsByTagName("value").item(0).getTextContent();
			// valueStr = valueStr.replace("<wrapperElement>", "").replace("</wrapperElement>", "");
			System.out.println("vvvvvvvvvvvvvvvvvvvalueStr:\n" + valueStr);
			Document valueDoc = XMLParser.getDocument(valueStr);
			Node valuePrettified = Utility.prettifyNode(valueDoc.getDocumentElement());
			String result = Utility.nodeToString(valuePrettified);
			if (result.contains("<wrapperElement>")) {
				result = result.replace("<wrapperElement>", "").replace("</wrapperElement>", "");
			}
			return "xmlDPvalue" + result;
		}
		return null;
	}

	public static JSONObject getDPMetadata(String dataName) throws Exception {
		if (useLocalEngine)
			return Utility.getDPMetadata(dataName);

		return null;

	}

	public static JSONArray getDPsMetadata(ArrayList<String> dataNames) throws Exception {
		if (useLocalEngine)
			return Utility.getDPMetadata(dataNames);

		return null;
	}

	// Mahdi's code:
	public static void registerStructuredDP(String name, String type, String value) throws Exception {
		DataProductManager.registerStructuredDP(name, type, value);
	}

	// public static int getUniqueRunId() throws Exception {
	// int numberOfDPsInTheDP = ((JSONArray) WebbenchUtility.getWFandDPMetadata().get("dataProducts")).length();
	// int runID = new Integer(numberOfDPsInTheDP);
	// return runID;
	// }
}
