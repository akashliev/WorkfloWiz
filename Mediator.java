import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lambda.calculus.CoercionInserter;
import lambda.calculus.LambdaEvaluator;
import lambda.calculus.LambdaExpression;
import lambda.calculus.LambdaToSWLTranslator;
import lambda.calculus.SWLToLambdaTranslator;
import lambda.calculus.SubTyping;
import lambda.calculus.Typing;
import lambda.calculus.types.Type;
import lambda.calculus.types.XSDType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import crm2.CRM;
import crm2.VMProvisioner;

import repository.Repository;
import translator.SWLAnalyzer;
import translator.WSDLAnalyzer;
import utility.LoggingLevels;
import utility.Utility;
import utility.XMLParser;
import webbench.MXGraphToSWLTranslator;
import webbench.WebbenchUtility;

/**
 * This is the main servlet that connects webbench frontend and backend via ajax calls.
 * 
 * @author Andrey Kashlev
 * 
 */
public class Mediator extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		String prefix = request.getRealPath(request.getServletPath()).replace("Mediator", "");
		String action = request.getParameter("action");
		System.out.println("action: " + action);
		if (action.equals("getWFsAndDPsMetadata")) {
			getWFsAndDPsMetadata(response);
		} else if (action.equals("getMetadataForWorkflow")) {
			getMetadataForWorkflow(request.getParameter("wfName"), response);
		} else if (action.equals("initializeDataProductsAndWorkflows")) {
			initializeDataProductsAndWorkflows(response, prefix);
		} else if (action.equals("saveAs")) {
			System.out.println("save as recognized");
			saveAs(request.getParameter("name"), request.getParameter("diagram"), response);
		} else if (action.equals("overwriteAndSave")) {
			overwriteAndSave(request.getParameter("name"), request.getParameter("diagram"), response);
		} else if (action.equals("getWorkflowDiagram")) {
			getWorkflowDiagram(request.getParameter("wfName"), response);
		} else if (action.equals("translateDiagramIntoSpec")) {
			translateDiagram(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("runWorkflowIfWellTyped")) {
			runWorkflowIfWellTyped(request.getParameter("name"), request.getParameter("runID"), request.getParameter("saveIntermediateResults"),
					"true", response);
		} else if (action.equals("runWorkflowPlain")) {
			runWorkflow(request.getParameter("name"), request.getParameter("runID"), request.getParameter("saveIntermediateResults"), response);
		} else if (action.equals("getDataProduct")) {
			getDataProduct(request.getParameter("dataName"), response);
		} else if (action.equals("registerDataProduct")) {
			registerDataProduct(request.getParameter("name"), request.getParameter("type"), request.getParameter("value"), response);
		} else if (action.equals("getDPMetadata")) {
			getDPMetadata(request.getParameter("name"), response);
		} else if (action.equals("getDPMetadataForGroup")) {
			getDPMetadataForGroup(request.getParameter("dataNames"), response);
		} else if (action.equals("translateDiagramIntoLambda")) {
			translateDiagramIntoLambda(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("getLambdaEvaluationSteps")) {
			getLambdaEvaluationSteps(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("typeCheckAndEvaluateLambda")) {
			typeCheckAndEvaluateLambda(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("getAllDPsMetadata")) {
			getAllDPsMetadata(response);
		} else if (action.equals("checkWellTypedness")) {
			checkWellTypedness(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("getSWLWithCoercionsInserted")) {
			getSWLWithCoercionsInserted(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("translateDiagramIntoLambdaWCoercions")) {
			translateDiagramIntoLambdaWCoercions(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("getComparisonTable")) {
			getComparisonTable(request.getParameter("wfName"), request.getParameter("diagram"), response);
		} else if (action.equals("getPortTypes")) {
			getPortTypes(request.getParameter("wfNames"), request.getParameter("portIDs"), response);
		} else if (action.equals("parseWSDL")) {
			parseWSDL(request.getParameter("wsdl"), response);
		} else if (action.equals("createWSBasedPWorkflow")) {
			createAndRegisterWSBasedPWorkflow(request.getParameter("wsdl"), request.getParameter("opName"), response);
		} else if (action.equals("runInCloud")) {
			runInCloud(request.getParameter("name"), request.getParameter("runID"), request.getParameter("saveIntermediateResults"),
					request.getParameter("config"), response);
		} else if (action.equals("provision")) {
			provisionVMs(request.getParameter("types"), response);
		} else if (action.equals("provisionAndRun")) {
			provisionAndRun(request.getParameter("types"), request.getParameter("name"), response);
		} else if (action.equals("testByAndrey")) {
			testByAndrey(response);
		}

		// else if (action.equals("registerStructuredDP")) {
		// registerStructuredDP(request.getParameter("name"), request.getParameter("type"), request.getParameter("value"),
		// response);
		// }

		else {
			System.out.println("undefined operation!!!!!!!!!!!!!");
		}

	}

	public void saveAs(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		// System.out.println("diagramStr:\n" + diagramStr);
		// System.out.println("diagram as xml:" + Utility.nodeToString(diagram));
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);
		// System.out.println("swl spec translated: " + Utility.nodeToString(spec));
		String resultOfSaveAs = WebbenchUtility.registerWorkflow(spec, diagram, false);
		PrintWriter out = response.getWriter();
		out.println(resultOfSaveAs);
	}

	public void overwriteAndSave(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document swlSpec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);
		String resultOfSaveAs = WebbenchUtility.registerWorkflow(swlSpec, diagram, true);
		PrintWriter out = response.getWriter();
		out.println(resultOfSaveAs);
	}

	public void getWorkflowDiagram(String name, HttpServletResponse response) throws Exception {
		String diagram = WebbenchUtility.getWorkflowDiagram(name);
		PrintWriter out = response.getWriter();
		out.println(diagram);
	}

	public void translateDiagram(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);

		PrintWriter out = response.getWriter();
		out.println(WebbenchUtility.nodeToString(spec));
	}

	public void initializeDataProductsAndWorkflows(HttpServletResponse response, String prefix) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		WebbenchUtility.loggingLevel = LoggingLevels.onlySystemOutput;

		PrintWriter out = response.getWriter();
		out.println("success");
	}

	public static void getWFsAndDPsMetadata(HttpServletResponse response) throws Exception {
		JSONObject jo = WebbenchUtility.getWFandDPMetadata();
		System.out.println("Mediator, after getWFandDBMetadata");
		JSONTokener tokener = new JSONTokener(jo.toString()); // tokenize the ugly JSON string
		JSONObject finalResult = new JSONObject(tokener); // convert it to JSON object
		 System.out.println("____________________about to send:\n");
		 System.out.println(finalResult.toString(4));
		PrintWriter out = response.getWriter();
		out.println(finalResult.toString(4));
	}

	public static void getMetadataForWorkflow(String wfName, HttpServletResponse response) throws Exception {
		JSONObject jo = WebbenchUtility.getWorkflowMetadata(wfName);
		JSONTokener tokener = new JSONTokener(jo.toString());
		JSONObject finalResult = new JSONObject(tokener);

		PrintWriter out = response.getWriter();
		out.println(finalResult.toString(4));
	}

	public static void runWorkflow(String wfName, String runID, String saveIntermediateResults, HttpServletResponse response) throws Exception {
		boolean overwritePrevious = false;
		boolean saveIntermediate = false;
		String resultOfExecution = null;
		if (saveIntermediateResults.trim().equals("true"))
			saveIntermediate = true;

		JSONObject outputStubNamesToDataNames = WebbenchUtility.runWorkflow(wfName, Utility.createUniqueRunID(), saveIntermediate, false);

		JSONTokener tokener = new JSONTokener(outputStubNamesToDataNames.toString());
		JSONObject finalResult = new JSONObject(tokener);
		resultOfExecution = finalResult.toString(4);

		PrintWriter out = response.getWriter();
		out.println(resultOfExecution);
	}

	public static void runWorkflowIfWellTyped(String wfName, String runID, String saveIntermediateResults, String insertCoercionsIfPossible,
			HttpServletResponse response) throws Exception {
		boolean overwritePrevious = false;
		boolean saveIntermediate = false;
		String resultOfExecution = null;
		if (saveIntermediateResults.trim().equals("true"))
			saveIntermediate = true;
		// try typechecking:
		try {
			// if workflow and each of its components have only one output port and it does not contain relation algebra,
			// typecheck it. If it is not well-typed - don't run it.
			Element workflowSpec = Repository.getWorkflowSpecification(wfName);
			if (SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(workflowSpec)
					&& !SWLAnalyzer.involvesRelationalAlgebra(workflowSpec)) {
				if (!SubTyping.isWellTyped(workflowSpec.getOwnerDocument())) {
					resultOfExecution = "ERROR: the workflow " + wfName + " is not well-typed";
					PrintWriter out = response.getWriter();
					out.println(resultOfExecution);
					return;
				}
				System.out.println("workflow " + wfName + " is well-typed");

			} else
				System.out.println("at least one workflow in " + wfName + " has multiple outputs");
		} catch (Exception e) {
		}
		try {
			boolean insertCoercions = false;
			Element workflowSpec = Repository.getWorkflowSpecification(wfName);
			if (insertCoercionsIfPossible.contains("true") && SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(workflowSpec)
					&& !SWLAnalyzer.involvesRelationalAlgebra(workflowSpec) && SubTyping.isWellTyped(workflowSpec.getOwnerDocument()))
				insertCoercions = true;
			JSONObject outputStubNamesToDataNames = WebbenchUtility.runWorkflow(wfName, Utility.createUniqueRunID(), saveIntermediate,
					insertCoercions);

			JSONTokener tokener = new JSONTokener(outputStubNamesToDataNames.toString());
			JSONObject finalResult = new JSONObject(tokener);
			resultOfExecution = finalResult.toString(4);
		} catch (Exception e) {
			resultOfExecution = "ERROR: could not execute the workflow " + wfName;
		}

		PrintWriter out = response.getWriter();
		out.println(resultOfExecution);
	}

	public static void getDataProduct(String dataName, HttpServletResponse response) throws Exception {
		String dataProductValuePrettyHTML = WebbenchUtility.getDataProductValueAsPrettyHTML(dataName);
		PrintWriter out = response.getWriter();
		out.println(dataProductValuePrettyHTML);
	}

	public static void registerDataProduct(String name, String type, String value, HttpServletResponse response) throws Exception {
		value = value.replaceAll("\'", "\\\\'");
		PrintWriter out = response.getWriter();

		if (type.trim().equals("String") || type.trim().equals("Decimal") || type.trim().equals("Integer")
				|| type.trim().equals("NonPositiveInteger") || type.trim().equals("NegativeInteger") || type.trim().equals("NonNegativeInteger")
				|| type.trim().equals("UnsignedLong") || type.trim().equals("UnsignedInt") || type.trim().equals("UnsignedShort")
				|| type.trim().equals("UnsignedByte") || type.trim().equals("PositiveInteger") || type.trim().equals("Double")
				|| type.trim().equals("Float") || type.trim().equals("Long") || type.trim().equals("Int") || type.trim().equals("Short")
				|| type.trim().equals("Byte") || type.trim().equals("Boolean") || type.trim().equals("Relation")
				|| type.trim().equals("xmlElement")) {
			WebbenchUtility.registerDataProduct(name.trim(), type.trim(), value.trim());
			out.println("success");
		} else
			out.println("ERROR: failed to register data product (wrong type specified): " + name);

	}

	public static void createAndRegisterWSBasedPWorkflow(String wsdl, String opName, HttpServletResponse response) throws Exception {
		opName = opName.replaceAll(" ", "");
		Document newSpec = SWLAnalyzer.createWSBasedPWorkflowSpec(wsdl, opName);
		Repository.registerWorkflow(newSpec, null, true);
		PrintWriter out = response.getWriter();
		out.println("success");
	}

	public static void parseWSDL(String wsdl, HttpServletResponse response) throws Exception {
		ArrayList<String> operations = WSDLAnalyzer.getOperations(wsdl);
		PrintWriter out = response.getWriter();
		if (operations == null || operations.size() == 0)
			System.out.println("ERROR: no operations found in this wsdl: " + wsdl);

		String result = operations.toString();
		result = result.substring(1, result.length() - 1);
		out.println(result);

	}

	public static void getDPMetadata(String dataName, HttpServletResponse response) throws Exception {
		JSONObject jo = WebbenchUtility.getDPMetadata(dataName);
		JSONTokener tokener = new JSONTokener(jo.toString());
		JSONObject finalResult = new JSONObject(tokener);

		PrintWriter out = response.getWriter();
		out.println(finalResult.toString(4));
	}

	public static void getDPMetadataForGroup(String dataNamesFromClient, HttpServletResponse response) throws Exception {

		System.out.println("requested getDPMetadataForGroup : " + dataNamesFromClient);
		String[] dataNames1 = dataNamesFromClient.split(",");
		ArrayList<String> dataNames = new ArrayList<String>();
		Collections.addAll(dataNames, dataNames1);

		JSONArray ja = WebbenchUtility.getDPsMetadata(dataNames);

		PrintWriter out = response.getWriter();

		// System.out.println("getDPMetadataForGroup rrrrrrrrrrrrrrreturning: ");
		// System.out.println(ja.toString(4));
		out.println(ja.toString(4));
	}

	public static void getAllDPsMetadata(HttpServletResponse response) throws Exception {
		JSONArray ja = WebbenchUtility.getAllDPsMetadata();

		PrintWriter out = response.getWriter();
		out.println(ja.toString(4));
	}

	public void translateDiagramIntoLambda(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);
		// String result = "some &lambda;-expression from server side";
		// result = SWLToLambdaTranslator.translateWorkflowOrExp(spec).toString();
		// result = result.replaceAll("λ", "&lambda;");

		String result = "";

		if (!SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(spec.getDocumentElement())
				|| SWLAnalyzer.involvesRelationalAlgebra(spec.getDocumentElement())) {
			result = "not supported";
			PrintWriter out = response.getWriter();
			out.println(result);
			return;
		}
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);

		if (expr != null)
			result += expr.toString();
		else
			result = "ERROR: cannot translate workflow " + name;

		result = result.replaceAll("λ", "&lambda;");
		PrintWriter out = response.getWriter();
		out.println(result);
	}

	public void translateDiagramIntoLambdaWCoercions(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);
		// String result = "some &lambda;-expression from server side";
		// result = SWLToLambdaTranslator.translateWorkflowOrExp(spec).toString();
		// result = result.replaceAll("λ", "&lambda;");

		String result = "";

		if (!SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(spec.getDocumentElement())
				|| SWLAnalyzer.involvesRelationalAlgebra(spec.getDocumentElement())) {
			result = "not supported";
			PrintWriter out = response.getWriter();
			out.println(result);
			return;
		}
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		LambdaExpression exprWithCoercions = CoercionInserter.pennTranslation(Typing.getContext(spec.getDocumentElement()), expr);

		if (exprWithCoercions != null)
			result += exprWithCoercions.toString();
		else
			result = "ERROR: cannot translate workflow " + name;

		result = result.replaceAll("λ", "&lambda;");
		PrintWriter out = response.getWriter();
		out.println(result);
	}

	public void checkWellTypedness(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);

		String result = "";

		if (!SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(spec.getDocumentElement())
				|| SWLAnalyzer.involvesRelationalAlgebra(spec.getDocumentElement())) {
			result = "not supported";
			PrintWriter out = response.getWriter();
			out.println(result);
			return;
		}

		if (SubTyping.isWellTyped(spec)) {
			result += "Workflow " + name + " is well-typed";

		} else {
			LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
			if (expr != null)
				result += expr.toString() + "\n";
			result = "ERROR: Workflow " + name + " is not well-typed.";
			String errorMsg = SubTyping.errorMsg.replaceAll("→", "->");
			result += "\n" + errorMsg;
		}

		PrintWriter out = response.getWriter();
		out.println(result);
	}

	public void getLambdaEvaluationSteps(String name, String diagramStr, HttpServletResponse response) throws Exception {
		System.out.println("getlambda eval steps call");
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);
		LambdaExpression expr = null;
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		LambdaEvaluator.evaluate(expr);

		PrintWriter out = response.getWriter();
		out.println(LambdaEvaluator.evaluationStepsRecord.replaceAll("λ", "&lambda;"));
	}

	public void typeCheckAndEvaluateLambda(String name, String diagramStr, HttpServletResponse response) throws Exception {
		System.out.println("type check and eval call");
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document spec = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);
		LambdaExpression expr = null;
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		String resultToBeSentToTheClientSide = "<b>λ-epxression: </b>\n" + expr.toStringWithTyping();
		resultToBeSentToTheClientSide = resultToBeSentToTheClientSide + "\n\n<b>&Gamma;:</b>\n" + Typing.getContext(spec.getDocumentElement());

		String obtainedType = null; // Typing.typeOf(spec).toString();
		Typing.errorMsg = "";
		Type type = Typing.typeOf(spec);
		if (type == null)
			obtainedType = "error: " + Typing.errorMsg + "\n(not well-typed)";
		else
			obtainedType = type.toString() + "\n(well-typed)";

		resultToBeSentToTheClientSide = resultToBeSentToTheClientSide + "\n\n<b>Type of this λ-expression:</b>\n " + obtainedType;

		if (type != null) {
			LambdaEvaluator.evaluate(expr);
			resultToBeSentToTheClientSide = resultToBeSentToTheClientSide + "\n\n<b>Expression evaluation:</b>"
					+ LambdaEvaluator.evaluationStepsRecord;
		} else
			resultToBeSentToTheClientSide = resultToBeSentToTheClientSide + "\n\n<b>Expression evaluation:</b>"
					+ "\ncannot evaluate expression because it is not well-typed";

		resultToBeSentToTheClientSide = resultToBeSentToTheClientSide.replaceAll("→", "&rarr;");
		resultToBeSentToTheClientSide = resultToBeSentToTheClientSide.replaceAll("λ", "&lambda;");
		PrintWriter out = response.getWriter();
		out.println(resultToBeSentToTheClientSide);
	}

	public void getSWLWithCoercionsInserted(String name, String diagramStr, HttpServletResponse response) throws Exception {
		Document diagram = webbench.XMLParser.getDocument(diagramStr);
		Document originalSWL = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);

		Document specWithCoercions = CoercionInserter.insertCoercionsInSWL(originalSWL);

		PrintWriter out = response.getWriter();
		out.println(WebbenchUtility.nodeToString(specWithCoercions));
	}

	public void getComparisonTable(String name, String diagramStr, HttpServletResponse response) throws Exception {
		String result = "";
		Document diagram = webbench.XMLParser.getDocument(diagramStr);

		Document originalSWL = MXGraphToSWLTranslator.translateWorkflowOrExp(name, diagram);

		Document SWLWCoercions = null;

		LambdaExpression originalExpr = null;
		LambdaExpression exprWithCoercions = null;

		boolean isLambdaCompatible = false;
		boolean isWellTyped = true;

		if (SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(originalSWL.getDocumentElement())
				&& !SWLAnalyzer.involvesRelationalAlgebra(originalSWL.getDocumentElement())) {
			isLambdaCompatible = true;
			if (!SubTyping.isWellTyped(originalSWL))
				isWellTyped = false;
		}

		originalExpr = SWLToLambdaTranslator.translateWorkflowOrExp(originalSWL);
		result += originalExpr.toString().replaceAll("λ", "&lambda;");
		result += "endOfOriginalExpr\n";
		result += Utility.nodeToString(originalSWL) + "endOfOrigSWL\n";

		if (isWellTyped && Typing.typeOf(originalSWL) == null) {
			SWLWCoercions = CoercionInserter.insertCoercionsInSWL(originalSWL);
			exprWithCoercions = CoercionInserter.pennTranslation(Typing.getContext(originalSWL.getDocumentElement()), originalExpr);
			exprWithCoercions = LambdaToSWLTranslator.simplifyExprByRemovingIdentityFs(exprWithCoercions);
			result += Utility.nodeToString(SWLWCoercions) + "endOfSWLWC\n";
			result += exprWithCoercions.toString().replaceAll("λ", "&lambda;") + "endOfExprWC\n";
		}

		System.out.println("getComparisonTable returns: " + result);
		PrintWriter out = response.getWriter();
		out.println(result);
	}

	public void getPortTypes(String wfNames, String portIDs, HttpServletResponse response) throws Exception {
		String result = "";

		String[] wfNamesArr = wfNames.split(",");
		String[] portIDsArr = portIDs.split(",");

		for (int i = 0; i < wfNamesArr.length; i++) {
			String currType = SWLAnalyzer.getPortType(wfNamesArr[i], portIDsArr[i]);
			XSDType xsdType = new XSDType(XMLParser.getDocument(currType).getDocumentElement());
			String header = "primitive type";
			if (currType.contains("<"))
				header = "XML complex type";

			result += ("hheader:" + header + "ttype:" + currType + ",,," + xsdType.toString() + ",,,");
		}
		PrintWriter out = response.getWriter();
		out.println(result);
	}

	public void runInCloud(String wfName, String runID, String saveIntermediateRes, String config, HttpServletResponse response) throws Exception {
		System.out.println("REPLACED FileDPs location with attached EBS volume !!!!!!!!!!!!!!!!!!!!!!!!!!");
		String logFileName = System.getenv("CATALINA_HOME") + "webapps/" + "log_run_" + new Long(System.currentTimeMillis()).toString();
		JSONObject outputStubNamesToDataNames = null;
		String resultOfExecution = null;
		System.out.println("before creating integer");
		int numberOfRuns = new Integer(Utility.readFileAsString("/home/view/numberOfRuns").trim());
//		numberOfRuns = 1;
		for (int i = 0; i < numberOfRuns; i++) {
			long startTime = System.currentTimeMillis();

			boolean saveIntermediateResults = false;
			if (saveIntermediateRes.contains("true"))
				saveIntermediateResults = true;
			CRM.listOfVMs = VMProvisioner.getAvailableVMIPs();
			CRM.logNodeIP = CRM.listOfVMs.get(0);

			// JSONObject outputStubNamesToDataNames = WebbenchUtility.runWorkflowInTheCloud(wfName, Utility.createUniqueRunID(),
			// saveIntermediateResults, config);
			outputStubNamesToDataNames = WebbenchUtility
					.runWorkflowInTheCloud(wfName, Utility.createUniqueRunID(), saveIntermediateResults, config);
			long endTime = System.currentTimeMillis();
			String report = "RUNNING TOOK " + new Long((endTime - startTime) / 1000).toString() + " s";
			report += "\nor\n" + new Long((endTime - startTime) / 60000).toString() + " m\n";
			Utility.writeToFile(report, logFileName);
			System.out.println(report);
		}

		JSONTokener tokener = new JSONTokener(outputStubNamesToDataNames.toString());
		JSONObject finalResult = new JSONObject(tokener);
		resultOfExecution = finalResult.toString(4);
		PrintWriter out = response.getWriter();
		out.println(resultOfExecution);
		Utility.drawALineOnEachNodesLog();
	}

	public void provisionVMs(String types, HttpServletResponse response) throws Exception {
		System.out.println("provisioning request, types:" + types);
		String logFileName = System.getenv("CATALINA_HOME") + "webapps/" + "log_provis_" + new Long(System.currentTimeMillis()).toString();
		long startTime = System.currentTimeMillis();
		HashMap<String, Integer> typeToNoOfInstances = new HashMap<String, Integer>();
		String[] instanceTypes = types.split(",");
		for (String currType : instanceTypes) {
			if (!typeToNoOfInstances.keySet().contains(currType))
				typeToNoOfInstances.put(currType, 1);
			else {
				int currValue = typeToNoOfInstances.get(currType);
				typeToNoOfInstances.remove(currType);
				typeToNoOfInstances.put(currType, ++currValue);
			}
		}
		System.out.println(typeToNoOfInstances);
		System.out.println("about to provision: " + types);
		String result = "provisioned VMs: \n";

		for (String currType : typeToNoOfInstances.keySet()) {
			ArrayList<String> currIPs = VMProvisioner.provisionVMs(currType, typeToNoOfInstances.get(currType));
			CRM.listOfVMs.addAll(currIPs);
			result += currIPs + "\n";
		}
		CRM.logNodeIP = CRM.listOfVMs.get(0);

		result += "logger:" + CRM.logNodeIP + "\n";

		System.out.println(result);
		long endTime = System.currentTimeMillis();
		String report = "PROVISIONING TOOK " + new Long((endTime - startTime) / 1000).toString() + " s";
		report += "\nor\n" + new Long((endTime - startTime) / 60000).toString() + " m\n";
		//Utility.writeToFile(report, logFileName);
		System.out.println(report);

		PrintWriter out = response.getWriter();
		out.println(result);
	}

	public void provisionAndRun(String types, String name, HttpServletResponse response) throws Exception {
		CRM.listOfVMs.clear();
		CRM.logNodeIP = null;
		String logFileName = System.getenv("CATALINA_HOME") + "webapps/" + "log_" + name + "_" + new Long(System.currentTimeMillis()).toString();

		long time_0 = System.currentTimeMillis();
		HashMap<String, Integer> typeToNoOfInstances = new HashMap<String, Integer>();
		String[] instanceTypes = types.split(",");
		for (String currType : instanceTypes) {
			if (!typeToNoOfInstances.keySet().contains(currType))
				typeToNoOfInstances.put(currType, 1);
			else {
				int currValue = typeToNoOfInstances.get(currType);
				typeToNoOfInstances.remove(currType);
				typeToNoOfInstances.put(currType, ++currValue);
			}
		}
		System.out.println(types);
		String result = name + "\nprovisioned VMs: \n";

		for (String currType : typeToNoOfInstances.keySet()) {
			ArrayList<String> currIPs = VMProvisioner.provisionVMs(currType, typeToNoOfInstances.get(currType));
			CRM.listOfVMs.addAll(currIPs);
			result += currIPs + "\n";
		}
		CRM.logNodeIP = CRM.listOfVMs.get(0);

		result += "logger:" + CRM.logNodeIP + "\n";

		long time_1_afterProvisioning = System.currentTimeMillis();
		long provisioningTookMs = time_1_afterProvisioning - time_0;
		System.out.println(result);
		String runID = Utility.createUniqueRunID();
		String report = name + "\n" + types + "\nrun id: " + runID + "\nPROVISIONING TOOK: " + new Long(provisioningTookMs).toString() + " ms";
		report += "\nor\n" + new Long(provisioningTookMs / 1000).toString() + " s";
		report += "\nor\n" + new Long(provisioningTookMs / 60000).toString() + " m";

		System.out.println(report);

		long time_2_beforeRunning = System.currentTimeMillis();
		JSONObject outputStubNamesToDataNames = WebbenchUtility.runWorkflowInTheCloud(name, runID, false, "available");
		long time_3_afterRunning = System.currentTimeMillis();
		long time_4_runningTook = time_3_afterRunning - time_2_beforeRunning;
		report += "\nRUNNING TOOK: " + new Long(time_4_runningTook).toString() + " ms";
		report += "\nor\n" + new Long(time_4_runningTook / 1000).toString() + " s";
		report += "\nor\n" + new Long(time_4_runningTook / 60000).toString() + " m";
		report += "\nor\n" + new Long(time_4_runningTook / 3600000).toString() + " h";
		System.out.println(report);

		if (Utility.checkIfFileExists("grade", Utility.pathToFileDPsFolder))
			report += "PASSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS";
		else
			report += "FAILLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL";

		File grade = new File(Utility.pathToFileDPsFolder + "grade");
		grade.delete();

		Utility.writeToFile(report, logFileName);

		PrintWriter out = response.getWriter();
		out.println(report);
	}

	public void testByAndrey(HttpServletResponse response) throws Exception {
		System.out.println("test by andrey: ");
		File loggingLocal = new File("/home/view/loggingLocal");
		System.out.println(loggingLocal.exists());
		PrintWriter out = response.getWriter();
		out.println("something");
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Mediator() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("doGet call");
		try {
			processRequest(request, response);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

}
