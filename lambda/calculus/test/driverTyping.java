package lambda.calculus.test;

import java.util.ArrayList;

import lambda.calculus.AlphaConverter;
import lambda.calculus.Gamma;
import lambda.calculus.LambdaExpression;
import lambda.calculus.Name;
import lambda.calculus.PrimOp;
import lambda.calculus.SWLToLambdaTranslator;
import lambda.calculus.SubTyping;
import lambda.calculus.Typing;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;

import org.w3c.dom.Document;

import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;

public class driverTyping {

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

		filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/typing/testSubTyping.swl";
		experimentSpec = Utility.readFileAsString(filePath);
		spec = XMLParser.getDocument(experimentSpec);
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		System.out.println(expr.toStringWithTyping());
		typeOfExpr = Typing.typeOf(spec);
		System.out.println("Typing.typeOf : " + typeOfExpr);

		typeOfExpr = SubTyping.typeOf(spec);
		System.out.println("SubTyping.typeOf : " + typeOfExpr);

		// ac.alphaConvert(expr);
		// System.out.println("alpha-converted expression: " + expr);

		filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/typing/SubTypingIntFloatExec.swl";
		experimentSpec = Utility.readFileAsString(filePath);
		spec = XMLParser.getDocument(experimentSpec);
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		System.out.println(expr.toStringWithTyping());
		typeOfExpr = Typing.typeOf(spec);
		System.out.println("Typing.typeOf : " + typeOfExpr);

		typeOfExpr = SubTyping.typeOf(spec);
		System.out.println("SubTyping.typeOf : " + typeOfExpr);

		filePath = "/home/andrey/Dropbox/workspace2/VIEW/testFiles/JUnit/typing/SubTypingFloatIntExec.swl";
		experimentSpec = Utility.readFileAsString(filePath);
		spec = XMLParser.getDocument(experimentSpec);
		expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		System.out.println(expr.toStringWithTyping());
		typeOfExpr = Typing.typeOf(spec);
		System.out.println("Typing.typeOf : " + typeOfExpr);

		typeOfExpr = SubTyping.typeOf(spec);
		System.out.println("SubTyping.typeOf : " + typeOfExpr);

		Name x0 = new Name("x0", new PrimitiveType("Boolean"));
		PrimOp incr = new PrimOp("Increment");
		expr = x0.lambda(incr.apply(x0));
		System.out.println("finding type of expression " + expr.toStringWithTyping());
		Gamma context = new Gamma();
		context.addBinding((LambdaExpression) x0, new PrimitiveType("Boolean"));
//		Type incrType = new PrimitiveType();
//		incrType.types = new ArrayList<Type>();
//		incrType.types.add(new PrimitiveType("Int"));
//		incrType.types.add(new PrimitiveType("Int"));
//		
//		context.addBinding(incr, incrType);
//		System.out.println(SubTyping.typeOf(expr, context));

	}

}
