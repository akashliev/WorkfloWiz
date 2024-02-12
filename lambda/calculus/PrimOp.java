package lambda.calculus;

import java.util.ArrayList;

import lambda.calculus.types.Type;

public class PrimOp extends LambdaExpression {
	String opName;
	public String instanceId = null;
	Type type;
	public static int instCounter = 0;

	ArrayList<Object> argsAlreadyPassedAsInputs = null;
	public int numOfArgs = -1;

	public PrimOp(String operationName) {
		opName = operationName;
		instanceId = operationName + instCounter++;
	}

	public PrimOp(String operationName, String instanceId) {
		opName = operationName;
		this.instanceId = instanceId;
	}

	public PrimOp(String operationName, Type type) {
		opName = operationName;
		if(opName.contains("Boolean"))
			opName = opName.replace("Boolean", "Bool");
		this.type = type;
		instanceId = operationName + instCounter++;
	}

	@Override
	public LambdaExpression lambda(LambdaExpression l) {
		return new Abstraction(new Name(), new Application(this, l));
	}

	@Override
	public LambdaExpression apply(LambdaExpression l) {
		return new Application(this, l);
	}

	public LambdaExpression beta(LambdaExpression arg) throws Exception {
		return Engine.execute(this, arg);
	}

	@Override
	public boolean isBound(Name n) {
		return false;
	}

	@Override
	public LambdaExpression replace(Name n, LambdaExpression e) {
		return this;
	}

	public String toString() {
		if (instanceId == null)
			return opName + instCounter++;
		return instanceId;
		// return opName;
	}

	public String toStringWithTyping() {
		return opName + " : " + type;
	}

}
