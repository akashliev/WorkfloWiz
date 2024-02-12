package lambda.calculus;

import org.w3c.dom.Document;

import repository.Repository;
import utility.LoggingLevels;
import utility.Utility;
import webbench.WebbenchUtility;

public class LambdaFlattener {

	public static LambdaExpression flattenExpression(LambdaExpression expr) throws Exception {
		LambdaExpression exprTmp = expr;
		while (termIsReducible(exprTmp))
			exprTmp = reduceTerm(exprTmp);
		return exprTmp;
	}

	public static LambdaExpression reduceTerm(LambdaExpression expr) throws Exception {
		if (expr instanceof Abstraction) {
			LambdaExpression reducedExpr = ((Abstraction) expr).expr;
			if(termIsReducible(reducedExpr))
				reducedExpr = reduceTerm(reducedExpr);
			return ((Abstraction) expr).name.lambda(reducedExpr);
		} else if (expr instanceof Application) {
			LambdaExpression reducedF = ((Application) expr).f;
			LambdaExpression reducedN = ((Application) expr).n;
			
			if(termIsReducible(reducedF))
				reducedF = reduceTerm(reducedF);
			
			if(termIsReducible(reducedN))
				reducedN = reduceTerm(reducedN);
			
			if(reducedF instanceof Abstraction)
				return reducedF.beta(reducedN);
			
			return new Application(reducedF, reducedN);
		}
		return null;
	}

	public static boolean termIsReducible(LambdaExpression expr) {
		if (expr instanceof Abstraction)
			return termIsReducible(((Abstraction) expr).expr);
		else if (expr instanceof Application)
			return (((Application) expr).f instanceof Abstraction) || termIsReducible(((Application) expr).f)
					|| termIsReducible(((Application) expr).n);
		return false;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;

		Document spec = Repository.getWorkflowSpecification("complexSubtyping").getOwnerDocument();
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		System.out.println(expr);
		System.out.println(flattenExpression(expr));
	}

}
