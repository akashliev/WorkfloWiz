package lambda.calculus.test;

import lambda.calculus.*;
import lambda.calculus.types.Type;

import org.w3c.dom.Document;

import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;

public class driverTyping3 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		String filePath = null;
		String experimentSpec = null;
		Document spec = null;
		LambdaExpression expr = null;
		AlphaConverter ac = new AlphaConverter();
		Type typeOfExpr = null;

		filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/experiments/test16Types";
		experimentSpec = Utility.readFileAsString(filePath);
		spec = XMLParser.getDocument(experimentSpec);
//		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
//		Gamma ctx = Typing.getContext(spec.getDocumentElement());
		System.out.println("subtyping: " + SubTyping.isWellTyped(spec));
		//System.out.println(expr.toStringWithTyping());
		typeOfExpr = SubTyping.typeOf(spec);
		System.out.println("Typing.typeOf : " + typeOfExpr);

	}

}
