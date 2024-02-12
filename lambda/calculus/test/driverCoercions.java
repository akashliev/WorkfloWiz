package lambda.calculus.test;

import lambda.calculus.AlphaConverter;
import lambda.calculus.CoercionInserter;
import lambda.calculus.LambdaExpression;
import lambda.calculus.SWLToLambdaTranslator;
import lambda.calculus.SubTyping;
import lambda.calculus.Typing;
import lambda.calculus.types.Type;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import repository.Repository;
import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;

public class driverCoercions {

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

		// filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/coercions/AddFloatTakesInt.swl";
		// experimentSpec = Utility.readFileAsString(filePath);
		// spec = XMLParser.getDocument(experimentSpec);
		// expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		// System.out.println(expr.toStringWithTyping());
		// typeOfExpr = Typing.typeOf(spec);
		// System.out.println("Typing.typeOf : " + typeOfExpr);
		//
		// typeOfExpr = SubTyping.typeOf(spec);
		// System.out.println("SubTyping.typeOf : " + typeOfExpr + "\n");
		// LambdaExpression exprWithCoercionsInserted = Coercion.pennTranslation(Typing.getContext(spec.getDocumentElement()),
		// expr, SubTyping.typeOf(spec));
		// System.out.println("expr w/0  coercions: " + expr.toStringWithTyping());
		// System.out.println("expr with coercions: " + exprWithCoercionsInserted.toStringWithTyping());

		filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/coercions/AddFloatTakesInt.swl";
		// filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/coercions/testInserter";
		experimentSpec = Utility.readFileAsString(filePath);
		spec = XMLParser.getDocument(experimentSpec);
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		System.out.println(expr.toStringWithTyping());
		typeOfExpr = Typing.typeOf(spec);
		System.out.println("Typing.typeOf : " + typeOfExpr);

		typeOfExpr = SubTyping.typeOf(spec);
		System.out.println("SubTyping.typeOf : " + typeOfExpr);
		LambdaExpression exprWithCoercionsInserted = CoercionInserter.pennTranslation(Typing.getContext(spec.getDocumentElement()), expr);
		System.out.println("expr w/0  coercions: " + expr.toStringWithTyping());
		System.out.println("expr with coercions: " + exprWithCoercionsInserted.toStringWithTyping());

		// ///// from original SWL to SWL with shims injected:	
		System.out.println("");
		Document originalSWL = Repository.getWorkflowSpecification("AddExecInt").getOwnerDocument();
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(originalSWL);
		exprWithCoercionsInserted = CoercionInserter.pennTranslation(Typing.getContext(originalSWL.getDocumentElement()), expr);
		System.out.println("expr w/0  coercions: " + expr.toStringWithTyping());
		System.out.println("expr with coercions: " + exprWithCoercionsInserted.toStringWithTyping());
		Document specWithCoercions = CoercionInserter.insertCoercionsInSWL(originalSWL);

		if (specWithCoercions != null)
			System.out.println(Utility.nodeToString(specWithCoercions));
		else
			System.out.println("it's null");

	}

}
