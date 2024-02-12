package lambda.calculus;

import java.util.ArrayList;

//class Name;
public abstract class LambdaExpression {
	
	public abstract LambdaExpression lambda(LambdaExpression l);

	public abstract LambdaExpression apply(LambdaExpression l);
	
	public abstract LambdaExpression beta(LambdaExpression argument) throws Exception;

	public abstract boolean isBound(Name n);

	public abstract LambdaExpression replace(Name n, LambdaExpression e);
	
	public String toStringWithTyping(){
		return this.toString();
	}
};