package repository;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ieee2023.D8_cybershake2;
import translator.SWLAnalyzer;
import translator.Translator;
import utility.Utility;
import utility.XMLParser;

/**
 * The purpose of this class is to provide a set of static methods to work with the repository built-in inside workflow engine.
 * This repository contains all workflow specifications (SWLs).
 * 
 * @author Andrey Kashlev
 * 
 */
public class Repository {

	// will be wrapped in web service: registerWorkflow(Document swl, Document diagram, boolean overWriteAllowed)
	public static String registerWorkflow(Document swlSpec, Document diagram, boolean overwriteAllowed) throws Exception {
		String result = "ERROR: could not register workflow";
		try {
			// Document swlSpec = XMLParser.getDocument(swlStr);
			boolean isExperiment = false;
			// System.out.println("figuring out whether it's experiment or not : ");
			// System.out.println("diagram != null: " + (diagram != null));
			// System.out.println("Utility.nodeToString(diagram) != null : " + (Utility.nodeToString(diagram) != null));
			// System.out.println("Utility.nodeToString(diagram).contains(\"dataProduct\"): "
			// + (Utility.nodeToString(diagram).contains("dataProduct")));
			if (diagram != null && Utility.nodeToString(diagram) != null && Utility.nodeToString(diagram).contains("dataProduct"))
				isExperiment = true;

			if (isExperiment && (Utility.nodeToString(diagram).contains("inputPort") || Utility.nodeToString(diagram).contains("inputPort"))) {
				result = result + "ERROR: executable workflow cannot contain input or output ports";
			}

			result = verifySpec(swlSpec);
			if (errorIsCritical(result)) {
				result = "cannot register a workflow in the repository because verification failed:" + "\n " + result;
				System.out.println("turned out critical");
				return result;
			}

			boolean isCorrect = false;

			if (swlSpec == null)
				System.out.println("swl spec is null");

			String wfOrExperimentName = SWLAnalyzer.getWorkflowOrExperimentName(swlSpec.getDocumentElement());
			if (result.equals("success"))
				isCorrect = true;

//			if (SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(swlSpec.getDocumentElement())) {
//				if (!Typing.isWellTyped(swlSpec))
//					isCorrect = false;
//			}

			Element diagramEl = null;
			// Document diagram = null;
			// if (diagramStr != null)
			// diagram = XMLParser.getDocument(diagramStr);
			// System.out.println("storing diagram:" + Utility.nodeToString(diagram));
			if (diagram != null)
				diagramEl = diagram.getDocumentElement();
			System.out.println("????? " + wfOrExperimentName + "|" + Utility.workflowIsInTheDatabase(wfOrExperimentName));
			if (!Utility.workflowIsInTheDatabase(wfOrExperimentName))
				Utility.storeSpecInDB(wfOrExperimentName, swlSpec.getDocumentElement(), diagramEl, isCorrect, isExperiment);
			else {
				if (overwriteAllowed) {
					Utility.updateSpecInDB(wfOrExperimentName, swlSpec.getDocumentElement(), diagramEl, isCorrect, isExperiment);
				} else
					result = result.replaceAll("success", "") + "ERROR: cannot register workflow " + wfOrExperimentName
							+ " because it's already in the database";
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static String verifySpec(Document spec) {
		if (spec != null)
			return "success";
		String result = "undefined";
		// Utility.appendToLog("attempting to register workflow ");
		try {
			if (SWLAnalyzer.isExperimentOrWorkflow(spec.getDocumentElement()).equals("workflow")) {
				String swlURL = Utility.swlSchemaURL;
				String validationResult = validateSpec(spec, swlURL);
				if (!validationResult.equals("success"))
					result = validationResult;
				if (SWLAnalyzer.getAllDeclaredWorkflowsFromSpec(spec).size() > 1) {
					result = result.replace("undefined", "") + "\nERROR: this spec contains more than one workflow.";
					return result;
				}

				Element workflow = spec.getDocumentElement();
				String name = SWLAnalyzer.getWorkflowOrExperimentName(workflow);

				if (!SWLAnalyzer.verifyWorkflowMode(workflow)) {
					Utility.appendToLog("ERROR: the declared mode of workflow " + name + " is not correct");
					result = result.replace("undefined", "") + "\nERROR: the declared mode of workflow " + SWLAnalyzer.getWorkflowName(workflow)
							+ " is not correct";
				}

				String mode = SWLAnalyzer.getWorkflowMode(workflow);

				if (mode.equals("builtin")) {
					HashSet<String> builinClasses = Utility.getAllBuiltInClasses();
					String name1 = SWLAnalyzer.getWorkflowOrExperimentName(workflow);
					// && !Translator.isCoercion(SWLAnalyzer.getWorkflowOrExperimentName(workflow))
//					System.out.println("coercion? " + Translator.isCoercion(SWLAnalyzer.getWorkflowOrExperimentName(workflow)));
					if (!Translator.isCoercion(SWLAnalyzer.getWorkflowOrExperimentName(workflow)) && !builinClasses.contains(SWLAnalyzer.getBuiltinComponent(workflow))) {
						Utility.appendToLog("ERROR: the following builtin workflow does not exist: " + SWLAnalyzer.getBuiltinComponent(workflow));
						result = result + "\n" + "ERROR: the following builtin workflow does not exist: "
								+ SWLAnalyzer.getBuiltinComponent(workflow);
					}
				} else if (mode.equals("primitive")) {

				} else if (mode.equals("graph-based")) {
					// let's make sure all workflows mentioned in the spec (e.g. as building blocks of graph-based workflow)
					// are registered in the repository:
					HashSet<Element> declaredWorkflows = SWLAnalyzer.getAllDeclaredWorkflowsFromSpec(Utility.getDocument(workflow));
					HashSet<String> declaredWorklowNames = new HashSet<String>();
					for (Element workflowElement : declaredWorkflows)
						declaredWorklowNames.add(SWLAnalyzer.getWorkflowName(workflowElement));

					HashSet<String> referencedWorkflows = SWLAnalyzer.getAllReferencedComponents(workflow);
					for (String referencedWorkflowName : referencedWorkflows) {
						if (!workflowIsInTheRepository(referencedWorkflowName)) {
							Utility.appendToLog("ERROR: cannot find worklow in the repository: " + referencedWorkflowName);
							result = result.replace("undefined", "") + "\n" + "ERROR: cannot find worklow in the repository: "
									+ referencedWorkflowName;
						}
					}
				}

			} else if (SWLAnalyzer.isExperimentOrWorkflow(spec.getDocumentElement()).equals("experiment")) {
				System.out.println("------------------------>>>>it's experiment");
				String experimentSchemaURL = Utility.experimentSchemaURL;
				String validationResult = validateSpec(spec, experimentSchemaURL);
				if (!validationResult.equals("success"))
					result = validationResult;
			} else {
				return "ERROR: this specification is neither workflow nor experiment";
			}

			// System.out.println(node);
		} catch (Exception e) {
			// result = result + "\nCould not register workflow " + nameFromUser;
			result.replace("undefined", "");
			return result;
		}
		if (result.equals("undefined"))
			result = "success";
		return result;
	}

	public static String validateSpec(Document spec, String schemaURL) throws Exception {
		if(spec != null)
			return "success";
		String result = "undefined";
		// "http://dmsg1.cs.wayne.edu/schema/swl.xsd"
		String name = "";
		if (spec.getDocumentElement().hasAttribute("name")) {
			// it's DPL, not SWL
			name = spec.getDocumentElement().getAttribute("name");
		} else {
			for (int i = 0; i < spec.getDocumentElement().getChildNodes().getLength(); i++) {
				Node currentChild = spec.getDocumentElement().getChildNodes().item(i);
				if (currentChild.getNodeType() == Node.ELEMENT_NODE)
					name = ((Element) currentChild).getAttribute("name");
			}
		}

		String specStr = Utility.domToString(spec);
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema swlSchema = null;
		Node rootNode = null;
		DOMSource swlSource = null;
		Validator validator = null;

		try {
			// URL schemaFile = new URL("http://localhost/schema/swl.xsd");
			URL schemaFile = new URL(schemaURL);

			swlSchema = sf.newSchema(schemaFile);
			rootNode = XMLParser.parseXmlFile(specStr);
			swlSource = new DOMSource(rootNode);
			validator = swlSchema.newValidator();
			validator.validate(swlSource);
		} catch (SAXException e2) {
			// e2.printStackTrace();
			result = "ERROR: " + e2.getMessage();
			Utility.appendToLog("ERROR: Validation against xsd failed for this specification: " + name);
		} catch (Exception e) {
			// e.printStackTrace();
			result = "ERROR: " + e.getMessage();
			Utility.appendToLog("ERROR: Validation against xsd failed for this specification: " + name);
		}
		if (result.equals("undefined"))
			result = "success";
		if (result.indexOf("success") == -1)
			Utility.appendToLog(result);
		return result;
	}

	public static boolean workflowIsInTheRepository(String name) {
		boolean result = false;
		try {
			return Utility.workflowIsInTheDatabase(name);
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean errorIsCritical(String errorMsg) {
		boolean result = false;
		if (errorMsg.contains("ERROR: this spec contains more than one workflow."))
			return true;
		if (errorMsg.contains("ERROR: this specification is neither workflow nor experiment"))
			return true;
		return result;
	}

	public static void deleteWorkflow(String wfName) throws Exception {
		Utility.appendToLog(Utility.getDateTime());
		Utility.appendToLog("attempting to delete workflow: " + wfName);

		if (!Utility.workflowIsInTheDatabase(wfName)) {
			Utility.appendToLog("Cannot delete workflow, no such workflow in the database: " + wfName);
			return;
		}

		if (Utility.deleteWorkflowFromDatabase(wfName))
			Utility.appendToLog("the following workflow was successfully deleted: " + wfName);

	}

	public static HashSet<String> getAllWorkflowNames() {
		HashSet<String> allWorkflowNames = new HashSet<String>();
		try {
			allWorkflowNames = Utility.getAllWorkflowNamesFromDB();
		} catch (Exception e) {
		}
		return allWorkflowNames;
	}

	public static void removeAllWorkflowsAndExperntsFromRepo() {
		HashSet<String> allWorkflows = getAllWorkflowNames();
		for (String wfName : allWorkflows) {
			try {
				deleteWorkflow(wfName);
			} catch (Exception e) {
			}
		}
	}

	public static Element getWorkflowSpecification(String name) throws Exception {
//		System.out.println("get wf spec: " + name);
		Element resultElement = null;
		try {
//			System.out.println("getting wf spec from cache:|" + name + "|");
			//added this line on 7/26/2023:
//			String wfFromDB = D8_cybershake2.wfName2SWLSpecMap.get(name);
			// commented the following line out on 7/26/2023:
			String wfFromDB = Utility.getWorkflowFromDB(name);
//			System.out.println(wfFromDB);
			resultElement = XMLParser.getElement(wfFromDB);
		} catch (Exception e) {
			Utility.appendToLog("ERROR: Cannot obtain the specification for workflow " + name);
		}
		return resultElement;
	}

	public static String getWorkflowDiagram(String name) throws Exception {
		String resultElement = null;
		try {
			String diagramFromDB = Utility.getDiagramFromDB(name);

			if (!diagramFromDB.equals(""))
				resultElement = diagramFromDB;
		} catch (Exception e) {
			Utility.appendToLog("ERROR: Cannot obtain the specification for workflow " + name);
		}
		return resultElement;
	}

	public static JSONObject getWorkflowMetadata(String name) throws SQLException {
		JSONObject metadata = Utility.getWorkflowMetadata(name);
		return metadata;
	}

}
