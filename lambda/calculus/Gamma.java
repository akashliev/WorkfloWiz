package lambda.calculus;

import java.util.HashMap;

import lambda.calculus.types.Type;

public class Gamma {
	private HashMap<LambdaExpression, Type> context = new HashMap<LambdaExpression, Type>();

	public void addBinding(LambdaExpression expr, Type type) {
		context.put(expr, type);
	}

	public boolean contains(LambdaExpression expr) {
		for (LambdaExpression currExpr : context.keySet()) {
			if (expr instanceof PrimOp) {
				if (currExpr.toString().trim().equals(((PrimOp) expr).opName.trim()))
					return true;
			} else if (currExpr.toString().trim().equals(expr.toString().trim())) {
				return true;
			}
		}
		return false;
	}

	public Type getBinding(LambdaExpression expr) {
		for (LambdaExpression currExpr : context.keySet()) {
			if (expr instanceof PrimOp) {
				if (currExpr instanceof PrimOp) {
					String currExpr_opName = ((PrimOp) currExpr).opName.trim();
					if (currExpr_opName.equals(((PrimOp) expr).opName.trim()))
						return context.get(currExpr);
				}

			} else if (currExpr.toString().trim().equals(expr.toString().trim()))
				return context.get(currExpr);
		}
		return null;
	}

	public String toString() {
		String result = "";
		// System.out.println("pppppppppppringting context: " + context);
		for (LambdaExpression key : context.keySet()) {
			result = result + key + " : " + context.get(key) + ", ";
		}
		result = result + "end";
		result = result.replaceAll(", end", "");
		if (result.trim().equals("end"))
			return "";
		return result;
		// return context.toString();
	}

	public HashMap<LambdaExpression, Type> getAllBindings() {
		return context;
	}

	public Gamma clone() {
		Gamma newGamma = new Gamma();
		for (LambdaExpression le : context.keySet())
			newGamma.addBinding(le, context.get(le));
		return newGamma;
	}
}
