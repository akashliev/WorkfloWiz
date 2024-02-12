package lambda.calculus;

import java.util.ArrayList;

public class Application extends LambdaExpression {
	protected LambdaExpression f = null;
	protected LambdaExpression n = null;

	public Application(LambdaExpression f, LambdaExpression n) {
		this.f = f;
		this.n = n;
	}

	// public Application(LambdaExpression f, ArrayList<LambdaExpression> listOfLs) {
	// this.f = f;
	// for (LambdaExpression l : listOfLs)
	// this.listOfArgs.add((LambdaExpression) l);
	// }

	public LambdaExpression lambda(LambdaExpression l) {
		return new Abstraction(new Name(), new Application(this, l));
	};

	public LambdaExpression apply(LambdaExpression l) {
		return new Application(this, l);
	};

	@Override
	public LambdaExpression beta(LambdaExpression argument) {
		return this;
	}

	public boolean isBound(Name name) {
		// boolean boundInAtLeastOneArgument = false;
		// for (LambdaExpression le : listOfArgs)
		// boundInAtLeastOneArgument = boundInAtLeastOneArgument || le.isBound(name);
		// System.out.println("curr applic: " + f + " _ " + n);
		return f.isBound(name) || n.isBound(name);
	};

	public LambdaExpression replace(Name name, LambdaExpression l) {
		// ArrayList<LambdaExpression> listOfArgsAfterReplacement = new ArrayList<LambdaExpression>();
		// for(LambdaExpression le : listOfArgs)
		// listOfArgsAfterReplacement.add(le.replace(name, l));
		// System.out.println("%%%%% replace: " + f);
		return new Application(f.replace(name, l), n.replace(name, l));
	};

	public String toString() {
		// String listOfArgsStr = "";
		// for(LambdaExpression le : listOfArgs)
		// listOfArgsStr = listOfArgsStr + le + " ";
		// listOfArgsStr = listOfArgsStr.trim();

		// return "(" + f + " " + listOfArgsStr + ")";
		return "(" + f + " " + n + ")";
	}

	public String toStringWithTyping() {
		// String listOfArgsStr = "";
		// for(LambdaExpression le : listOfArgs)
		// listOfArgsStr = listOfArgsStr + le.toStringWithTyping() + " ";
		// listOfArgsStr = listOfArgsStr.trim();
		//
		// return "(" + f.toStringWithTyping() + " " + listOfArgsStr + ")";
		return "(" + f.toStringWithTyping() + " " + n.toStringWithTyping() + ")";
	}

	public boolean isReducible() {
		if (f instanceof Name)
			return false;
		else if (f instanceof Abstraction || f instanceof PrimOp)
			return true;
		else if (f instanceof Application)
			return ((Application) f).isReducible();

		return false;
	}
}