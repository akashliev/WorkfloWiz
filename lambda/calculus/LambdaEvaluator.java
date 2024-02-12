package lambda.calculus;

import java.util.ArrayList;
import java.util.Arrays;

public class LambdaEvaluator {
	public static String evaluationStepsRecord = "";
	
	public static LambdaExpression evaluate(LambdaExpression expr) throws Exception {
		evaluationStepsRecord = "";
		//AlphaConverter ac = new AlphaConverter();
		//ac.alphaConvert(expr);
		LambdaExpression result = evaluateExpression(expr);
		//ac.alphaConvertBack(expr);

		return result;
	}

	public static boolean termIsReducible(LambdaExpression expr) {
			if (expr instanceof Application && ((Application) expr).isReducible()) 
				return true;
		return false;
	}

	public static LambdaExpression reduceOneTime(LambdaExpression expr) throws Exception {
//		System.out.println("reduce on etime call for " + expr);
		if (expr instanceof Name)
			return expr;
		else if (expr instanceof Abstraction)
			return expr;
		else if (expr instanceof Application) {
			LambdaExpression argumentAfterReduction = null;
			if (termIsReducible(((Application) expr).n)) {
					if (((Application) expr).n instanceof Application && ((Application) ((Application) expr).n).isReducible()) {
						LambdaExpression reducedTerm = reduceOneTime(((Application) expr).n);
						argumentAfterReduction = reducedTerm;
					} else
						argumentAfterReduction = ((Application) expr).n;
				return new Application(((Application) expr).f, argumentAfterReduction);
			}
			if (((Application) expr).f instanceof Application && ((Application) ((Application) expr).f).isReducible()) {
				LambdaExpression reducedTerm = reduceOneTime(((Application) expr).f);
				return new Application(reducedTerm, ((Application) expr).n);
			} else{
				return (LambdaExpression) ((Application) expr).f.beta(((Application) expr).n);
			}
		}
		return null;
	}

	public static LambdaExpression evaluateExpression(LambdaExpression expr) throws Exception {
		System.out.println(expr);
		addLineToEvaluationRecord(expr);
		
		if (expr instanceof Name)
			return expr;
		else if (expr instanceof Abstraction)
			return expr;
		else if (expr instanceof Application) {
			if (!((Application) expr).isReducible() && !(((Application) expr).f instanceof PrimOp)) {
				return expr;
			} else {
				LambdaExpression reducedTerm = reduceOneTime(expr);
				System.out.println(reducedTerm);
				addLineToEvaluationRecord(reducedTerm);

				while (reducedTerm instanceof Application && ((Application) reducedTerm).isReducible()) {
					reducedTerm = reduceOneTime(reducedTerm);
					System.out.println(reducedTerm);
					addLineToEvaluationRecord(reducedTerm);
				}
				return reducedTerm;
			}
		}
		return null;
	}
	
	private static void addLineToEvaluationRecord(LambdaExpression le){
		if(le == null){
			evaluationStepsRecord = evaluationStepsRecord + "\n" + le;
			return;
		}
		if (le.toString().substring(0, 1).trim().equals("("))
			evaluationStepsRecord = evaluationStepsRecord + "\n" + le.toString().substring(1,  le.toString().length() - 1);
		else
			evaluationStepsRecord = evaluationStepsRecord + "\n" + le;
		
	}

	public static void main(String[] args) {

	}

}
