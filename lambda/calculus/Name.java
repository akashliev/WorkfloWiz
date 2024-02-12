package lambda.calculus;

import java.util.ArrayList;

import lambda.calculus.types.Type;


public class Name extends LambdaExpression {
	protected String name;
	private static int counter = 0;
	public Type type = null;

	public Name() {
		name = "x" + counter;
		counter++;
	}

	public Name(String s) {
		name = s;
	};
	
	public Name(Type type) {
		name = "x" + counter;
		counter++;
		this.type = type;
	}
	
	public Name(String s, Type type) {
		name = s;
		this.type = type;
	};
	
	public LambdaExpression lambda(LambdaExpression l) {
		return new Abstraction(this, (LambdaExpression) l);
	};

	public LambdaExpression apply(LambdaExpression l) {
		return new Application(this, l);
	};
	
	public LambdaExpression beta(LambdaExpression l) {
		return new Application(this, l);
	};
	
	public boolean isBound(Name n) {
		return (n.name == name);
	};
	
	public static void clearCounter(){
		counter = 0;
	}

	public LambdaExpression replace(Name n, LambdaExpression l) {
		if (n.name == name) {
			return l;
		} else {
			return this;
		}
	};
	
	public String toString(){
		return name;
	}
	
	@Override
	public String toStringWithTyping(){
		if(type != null)
			return name + ":" + type;
		else
			return name;
	}

};