package lambda.calculus.test;

import lambda.calculus.DataProductRepres;
import lambda.calculus.LambdaEvaluator;
import lambda.calculus.LambdaExpression;
import lambda.calculus.Name;
import lambda.calculus.PrimOp;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;

public class driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Name x = new Name("x");
		Name x0 = new Name("x0");
		Name x1 = new Name("x1");
		Name x2 = new Name("x2");
		Name z = new Name("z");
		Name a = new Name("a");
		
		PrimOp Increment = new PrimOp("Increment");
		PrimOp Square = new PrimOp("Square");
		PrimOp Add = new PrimOp("Add");
		
		DataProductRepres two = new DataProductRepres("two", 2, new PrimitiveType("Int"));
		DataProductRepres three = new DataProductRepres("three", 3, new PrimitiveType("Int"));
		
		LambdaExpression expr = null;
		// λx.x
		expr = x.lambda(x);
		System.out.println(expr + "\n");
		
		// λa.a a
		expr = a.lambda(a.apply(a));
		System.out.println(expr + "\n");
		
		expr = a.lambda(a.apply(a)).apply(x.lambda(x));
		//System.out.println(expr);
		
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		// λx0.x0 (λa.(a a) λx1.x1):
		expr = x0.lambda(x0).apply(a.lambda(a.apply(a)).apply(x1.lambda(x1)));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		// λx0.x0 (λx1.x1 λz.(λx2.x2 z))
		expr = x0.lambda(x0).apply(x1.lambda(x1).apply(z.lambda(x2.lambda(x2).apply(z))));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		expr = Increment.apply(Increment.apply(two));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		expr = Increment.apply(x0.lambda(Increment.apply(x0)).apply(two));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		expr = Increment.apply(Square.apply(two));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		expr = x0.lambda(Increment.apply(x0)).apply(Square.apply(three));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		expr = Add.apply(two).apply(three);
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
		expr = x0.lambda(x1.lambda(x2.lambda(Add.apply(x0).apply(x1).apply(x2))));
		LambdaEvaluator.evaluate(expr);
		System.out.println();
		
	}
}
