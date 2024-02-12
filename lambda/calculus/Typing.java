package lambda.calculus;

import java.util.ArrayList;
import java.util.HashSet;

import lambda.calculus.types.ArrowType;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;
import lambda.calculus.types.XSDType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import repository.Repository;
import translator.SWLAnalyzer;
import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;

public class Typing {
	public static String errorMsg = "";
	public static Gamma gamma = null;

	public static boolean isWellTyped(Document spec) throws Exception {
		return typeOf(spec) != null;
	}

	public static Type typeOf(Document spec) throws Exception {
		errorMsg = "";
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		gamma = getContext(spec.getDocumentElement());
		// System.out.println("ggggggamma: " + gamma);
		// Type result = typeOf(expr, gamma);

		return typeOf(expr, gamma);
	}

	public static Gamma getContext(Element spec) throws Exception {
		Gamma gamma = new Gamma();
		getContext(spec, gamma);
		return gamma;
	}

	private static void getContext(Element spec, Gamma gamma) throws Exception {
		if (SWLAnalyzer.isExperimentOrWorkflow(spec).contains("experiment")) {
			NodeList inputDP2PortMappings = spec.getElementsByTagName("inputDP2PortMapping");
			for (int i = 0; i < inputDP2PortMappings.getLength(); i++) {
				Element currMapping = (Element) inputDP2PortMappings.item(i);
				String fromAttr = currMapping.getAttribute("from").trim();
				DataProductRepres dpr = new DataProductRepres(Utility.getDataProduct(fromAttr));
				gamma.addBinding(dpr, dpr.type);
			}
		}
		String mode = SWLAnalyzer.getWorkflowMode(spec).trim();
		if (mode.equals("builtin") || mode.equals("primitive")) {
			ArrowType PrimOpType = new ArrowType();
			PrimOpType.types = new ArrayList<Type>();
			ArrayList<String> inputPorts = SWLAnalyzer.getWorkflowInputPorts(spec);
			for (String currentInputPortID : inputPorts) {
				String currType = SWLAnalyzer.getPortType(spec, currentInputPortID).trim();
				if (SWLAnalyzer.isWebservice(spec))
					PrimOpType.types.add(new XSDType(XMLParser.getDocument(currType).getDocumentElement()));
				else
					PrimOpType.types.add(new PrimitiveType(currType));
				// System.out.println("adding new type " + currType);

			}
			ArrayList<String> outputPorts = SWLAnalyzer.getWorkflowOutputPorts(spec);
			String currOutputPortID = outputPorts.get(0);
			String currType = SWLAnalyzer.getPortType(spec, currOutputPortID);
			if (SWLAnalyzer.isWebservice(spec))
				PrimOpType.types.add(new XSDType(XMLParser.getDocument(currType).getDocumentElement()));
			else
				PrimOpType.types.add(new PrimitiveType(currType));
			if (!gamma.contains((LambdaExpression) new PrimOp(SWLAnalyzer.getWorkflowName(spec))))
				gamma.addBinding((LambdaExpression) new PrimOp(SWLAnalyzer.getWorkflowName(spec)), PrimOpType);
		} else if (mode.equals("graph-based")) {
			HashSet<String> allReferencedComponents = SWLAnalyzer.getAllReferencedComponents(spec);
			for (String componentName : allReferencedComponents) {
				Element currComponentSpec = Repository.getWorkflowSpecification(componentName);
				
				if (currComponentSpec != null)
					getContext(currComponentSpec, gamma);
				else {
					System.out.println("cannot find spec for workflow : " + componentName);
					errorMsg = "cannot find spec for workflow : " + componentName;
				}
			}
		}
	}

	public static Type typeOf(LambdaExpression expr, Gamma gamma) {
		// if (expr instanceof DataProductRepres)
		// return ((DataProductRepres) expr).type;
		// else
		if (expr instanceof Name || expr instanceof DataProductRepres) {
			return gamma.getBinding(expr);
		} else if (expr instanceof PrimOp) {
			return gamma.getBinding(expr);
		} else if (expr instanceof Abstraction) {
			Gamma gammaTmp = gamma.clone();

			gammaTmp.addBinding(((Abstraction) expr).name, ((Abstraction) expr).name.type);
			Type tyT2 = typeOf(((Abstraction) expr).expr, gammaTmp);
			if (tyT2 == null)
				return null;
			return new ArrowType(((Abstraction) expr).name.type, tyT2);
		} else if (expr instanceof Application) {
			Type typeOfF = typeOf(((Application) expr).f, gamma);
			if (typeOfF == null)
				return null;

			Type typeOfN = typeOf(((Application) expr).n, gamma);
			if (typeOfN == null)
				return null;

			if (typeOfF instanceof ArrowType) {
				if (((Type) ((ArrowType) typeOfF).types.get(0)).equals(typeOfN)) {
					ArrayList<Type> allTypesButFirstFromF = new ArrayList<Type>();
					for (int i = 1; i < ((ArrowType) typeOfF).types.size(); i++)
						allTypesButFirstFromF.add(((ArrowType) typeOfF).types.get(i));
					if (allTypesButFirstFromF.size() > 1)
						return new ArrowType(allTypesButFirstFromF);
					else
						return allTypesButFirstFromF.get(0);
				} else {
					System.out.println("application is wrong: " + expr.toStringWithTyping());
					System.out.println("parameter type mismatch: " + ((Application) expr).f + ":" + typeOfF + "\nwith \n" + ((Application) expr).n
							+ ":" + typeOfN);
					errorMsg = "parameter type mismatch: " + ((Application) expr).f + ":" + typeOfF + " with " + ((Application) expr).n + ":"
							+ typeOfN;
				}

			} else {
				System.out.println("arrow type expected in " + ((Application) expr).f);
				errorMsg = "arrow type expected in " + ((Application) expr).f;
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		String filePath = null;
		String experimentSpec = null;
		Document spec = null;
		LambdaExpression expr = null;
		AlphaConverter ac = new AlphaConverter();

		filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/typing/SquareReus.swl";
		experimentSpec = Utility.readFileAsString(filePath);
		spec = XMLParser.getDocument(experimentSpec);
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		System.out.println(expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);

		System.out.println("type = " + typeOf(spec));
		LambdaEvaluator.evaluate(expr);
		ac.alphaConvertBack(expr);
	}

}
