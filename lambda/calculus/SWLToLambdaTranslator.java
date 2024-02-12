package lambda.calculus;

import java.util.ArrayList;
import java.util.HashMap;

import lambda.calculus.types.PrimitiveType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import repository.Repository;
import translator.SWLAnalyzer;
import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;

public class SWLToLambdaTranslator {

	private static HashMap<String, Name> inputPortsToNames = new HashMap<String, Name>();

	public static LambdaExpression translateWorkflowOrExp(Document spec) throws Exception {
		LambdaExpression result = null;
		NodeList experimentElements = spec.getElementsByTagName("experimentSpec");
		if (spec.getElementsByTagName("experimentSpec").getLength() > 0)
			result = translateExperiment(spec);
		else if (spec.getElementsByTagName("workflowSpec").getLength() > 0)
			result = translatResusableWorkflow(spec, "1111");
		else
			System.out.println("cannot translate to lambda - it's neither workflow nor experiment");

		inputPortsToNames.clear();
		Name.clearCounter();
		return result;
	}

	private static LambdaExpression translateExperiment(Document spec) throws Exception {
		NodeList outputDP2PortMappings = spec.getElementsByTagName("outputDP2PortMapping");
		Element firstMapping = (Element) outputDP2PortMappings.item(0);
		String fromStr = firstMapping.getAttribute("from").trim();
		String componentId = fromStr.substring(0, fromStr.lastIndexOf(".")).trim();
		String primOpName = getWorklowNameById(spec, componentId);

		ArrayList<LambdaExpression> listOfArgs = new ArrayList<LambdaExpression>();

		Element componentWorkflowSpec = Repository.getWorkflowSpecification(SWLAnalyzer.getComponentName(componentId, spec.getDocumentElement()));

		ArrayList<String> inputPorts = SWLAnalyzer.getWorkflowInputPorts(componentWorkflowSpec);
		for (String portId : inputPorts) {
			listOfArgs.add(getInputExpression(spec, componentId, portId));
		}

		LambdaExpression lastComponent = null;

		if (SWLAnalyzer.getWorkflowMode(componentWorkflowSpec).trim().equals("graph-based"))
			lastComponent = translatResusableWorkflow(componentWorkflowSpec.getOwnerDocument(), "2222");
		else{
//			System.out.println("creating new primop3 " + componentId);
			lastComponent = new PrimOp(primOpName, componentId);
		}

		for (int i = 0; i < listOfArgs.size(); i++)
			lastComponent = lastComponent.apply(listOfArgs.get(i));

		return lastComponent;
	}

	public static LambdaExpression getInputExpression(Document spec, String componentId, String portId) throws Exception {
		// if input for componentId is data product:
		NodeList inputDP2PortMappings = spec.getElementsByTagName("inputDP2PortMapping");
		for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
			Element currMapping = (Element) inputDP2PortMappings.item(i);
			String toAttr = currMapping.getAttribute("to").trim();
			String toAttrWithoutPort = toAttr.substring(0, toAttr.lastIndexOf(".")).trim();
			String currInputPortId = toAttr.substring(toAttr.lastIndexOf(".") + 1).trim();

			if (toAttrWithoutPort.equals(componentId) && currInputPortId.equals(portId)) {
				String fromAttr = currMapping.getAttribute("from").trim();
				return new DataProductRepres(Utility.getDataProduct(fromAttr));
			}
		}
		// /////////////////////// to accommodate reusable workflow:
		// if input is an input port (for reusable workflow):
		// if input for componentId is input port of the external workflow:
		NodeList inputMappings = spec.getElementsByTagName("inputMapping");
		for (int i = 0; i < inputMappings.getLength(); i++) {
			Element currMapping = (Element) inputMappings.item(i);
			String toAttr = currMapping.getAttribute("to").trim();
			String toAttrWithoutPort = toAttr.substring(0, toAttr.lastIndexOf(".")).trim();
			String currInputPortId = toAttr.substring(toAttr.lastIndexOf(".") + 1).trim();

			if (toAttrWithoutPort.equals(componentId) && currInputPortId.equals(portId)) {
				String fromAttr = currMapping.getAttribute("from").trim();
				String inputPortId = fromAttr.substring(fromAttr.lastIndexOf(".") + 1);
				String type = SWLAnalyzer.getPortType(spec.getDocumentElement(), inputPortId);
				if (inputPortsToNames.containsKey(inputPortId))
					return new Name(inputPortsToNames.get(inputPortId).name);
				// return new Name(inputPortsToNames.get(inputPortId).name, new Type(type));

				// Name name = new Name(new Type(type));
				Name name = new Name();
				inputPortsToNames.put(inputPortId, name);
				return name;
			}

		}
		// ////////////////////////

		// if it's result of other workflow:

		NodeList dataChannels = spec.getElementsByTagName("dataChannel");
		for (int i = 0; i < dataChannels.getLength(); i++) {
			String toAttr = ((Element) dataChannels.item(i)).getAttribute("to").trim();
			String toAttrWithoutPort = toAttr.substring(0, toAttr.lastIndexOf(".")).trim();
			String currInputPortId = toAttr.substring(toAttr.lastIndexOf(".") + 1).trim();
			if (toAttrWithoutPort.equals(componentId) && currInputPortId.equals(portId)) {
				String fromAttr = ((Element) dataChannels.item(i)).getAttribute("from").trim();
				String fromAttrWithoutPort = fromAttr.substring(0, fromAttr.lastIndexOf(".")).trim();
				ArrayList<LambdaExpression> inputExpressions = new ArrayList<LambdaExpression>();
				ArrayList<String> inputPorts = SWLAnalyzer.getWorkflowInputPorts(Repository.getWorkflowSpecification(SWLAnalyzer.getComponentName(
						fromAttrWithoutPort, spec.getDocumentElement())));
				for (String inputPortId : inputPorts) {
					inputExpressions.add(getInputExpression(spec, fromAttrWithoutPort, inputPortId));
				}
				Element componentWorkflowSpec = Repository.getWorkflowSpecification(SWLAnalyzer.getComponentName(fromAttrWithoutPort,
						spec.getDocumentElement()));

				LambdaExpression lastComponent = null;

				if (SWLAnalyzer.getWorkflowMode(componentWorkflowSpec).trim().equals("graph-based"))
					lastComponent = translatResusableWorkflow(componentWorkflowSpec.getOwnerDocument(), fromAttrWithoutPort);
				else{
//					System.out.println("creating new primop4 " + fromAttrWithoutPort);
					lastComponent = new PrimOp(getWorklowNameById(spec, fromAttrWithoutPort), fromAttrWithoutPort);
				}

				for (int j = 0; j < inputExpressions.size(); j++)
					lastComponent = lastComponent.apply(inputExpressions.get(j));

				return lastComponent;
			}
		}
		return null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String getWorklowNameById(Document spec, String componentId) {
		NodeList workflowInstances = spec.getElementsByTagName("workflowInstance");
		for (int i = 0; i < workflowInstances.getLength(); i++) {
			Element currentInstance = (Element) workflowInstances.item(i);
			if (currentInstance.getAttribute("id").trim().equals(componentId)) {
				Element workflow = (Element) currentInstance.getElementsByTagName("workflow").item(0);
				return workflow.getTextContent().trim();
			}
		}
		return null;
	}

	private static LambdaExpression translatResusableWorkflow(Document spec, String instanceId) throws Exception {
		if(SWLAnalyzer.getWorkflowMode(spec.getDocumentElement()).trim().equals("primitive")){
//			System.out.println("creating new primop " + instanceId);
			return new PrimOp(SWLAnalyzer.getWorkflowName(spec.getDocumentElement()), instanceId);
		}
		NodeList outputMapping = spec.getElementsByTagName("outputMapping");
		Element firstMapping = (Element) outputMapping.item(0);
		String fromStr = firstMapping.getAttribute("from").trim();
		String componentId = fromStr.substring(0, fromStr.lastIndexOf(".")).trim();
		String primOpName = getWorklowNameById(spec, componentId);

		ArrayList<LambdaExpression> listOfArgs = new ArrayList<LambdaExpression>();

		ArrayList<String> inputPorts = SWLAnalyzer.getWorkflowInputPorts(Repository.getWorkflowSpecification(SWLAnalyzer.getComponentName(
				componentId, spec.getDocumentElement())));
		for (String portId : inputPorts) {
			listOfArgs.add(getInputExpression(spec, componentId, portId));
		}

		ArrayList<Name> listOfNames = new ArrayList<Name>();
		ArrayList<String> inputPortsOfEntireWorkflow = SWLAnalyzer.getWorkflowInputPorts(spec.getDocumentElement());
		for (String portId : inputPortsOfEntireWorkflow) {
			String type = SWLAnalyzer.getPortType(spec.getDocumentElement(), portId);
			if(!type.contains("<"))
				listOfNames.add(new Name(inputPortsToNames.get(portId).name, new PrimitiveType(type)));
			else
				System.out.println("SWLToLambdaTranslator ERRORRRRRRRRRRR - input type is xml");
		}

		Element componentWorkflowSpec = Repository.getWorkflowSpecification(SWLAnalyzer.getComponentName(componentId, spec.getDocumentElement()));

		LambdaExpression lastComponent = null;

		if (SWLAnalyzer.getWorkflowMode(componentWorkflowSpec).trim().equals("graph-based"))
			lastComponent = translatResusableWorkflow(componentWorkflowSpec.getOwnerDocument(), "3333");
		else{
//			System.out.println("creating new primop2 " + componentId);
			lastComponent = new PrimOp(primOpName, componentId);
		}

		for (LambdaExpression currExpr : listOfArgs)
			lastComponent = lastComponent.apply(currExpr);

		LambdaExpression result = listOfNames.get(listOfNames.size() - 1).lambda(lastComponent);
		for (int i = listOfNames.size() - 2; i >= 0; i--) {
			result = listOfNames.get(i).lambda(result);
		}

		return result;

	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		String filePath = null;
		String experimentSpec = null;
		Document spec = null;
		LambdaExpression expr = null;
		AlphaConverter ac = new AlphaConverter();

		filePath = "/Users/andrii/gdrive/workspace2/VIEW/testFiles/JUnit/lambdaTranslation/sampleOneOutputEx27.swl";
		experimentSpec = Utility.readFileAsString(filePath);
		System.out.println(experimentSpec);
		spec = XMLParser.getDocument(experimentSpec);
		System.out.println("spec: " + spec);
		expr = translateWorkflowOrExp(spec);
		System.out.println(expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		LambdaEvaluator.evaluate(expr);
		ac.alphaConvertBack(expr);
	}
}
