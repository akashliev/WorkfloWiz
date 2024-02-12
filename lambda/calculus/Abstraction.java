package lambda.calculus;

import java.util.ArrayList;

public class Abstraction extends LambdaExpression {
	protected Name name = null;
	protected LambdaExpression expr;

	public Abstraction(Name n, LambdaExpression expr) {
		name = n;
		this.expr = expr;
	}
	
	public Abstraction(LambdaExpression n, LambdaExpression expr) {
		if(n instanceof Name)
			name = (Name) n;
		else
			return;
		this.expr = expr;
	}
	
	public LambdaExpression lambda(LambdaExpression l) {
		return new Abstraction(new Name(), new Application(this, l));
	};

	public LambdaExpression apply(LambdaExpression l) {
		return new Application(this, l);
	};
	
	public LambdaExpression beta(LambdaExpression argument) {
		
		LambdaExpression result = expr;
			if(expr.isBound(name)){
//				System.out.println("A-A-A replace :" + result);
				result = result.replace(name, argument);
			}
		return result;
	};

	public boolean isBound(Name n) {
		return expr.isBound(n);
	};

	public LambdaExpression replace(Name n, LambdaExpression l) {
		if(isBound(n)){
//			ArrayList<Name> listOfNames = new ArrayList<Name>();
//			listOfNames.addAll(names);
//			System.out.println("AAAAAbstr replace call : " + expr);
			return new Abstraction(name, expr.replace(n, l));
		}
		else
			return this;
	};

	public String toString() {
		String result = "λ" + name + "." + expr.toString();
		result = result.trim();
		return result;
	}
	
	public String toStringWithTyping() {
		String result = "λ" + name.toStringWithTyping();
		result = result + "." + expr.toStringWithTyping();
		return result;
	}

}