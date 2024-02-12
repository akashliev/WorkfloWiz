package lambda.calculus.test;

import lambda.calculus.DataProductRepres;
import lambda.calculus.LambdaExpression;
import lambda.calculus.LambdaFlattener;
import lambda.calculus.Name;
import lambda.calculus.PrimOp;
import lambda.calculus.types.PrimitiveType;
import dataProduct.IntDP;

public class driverEvaluatorButPrimOps {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PrimOp increment = new PrimOp("Increment");
		PrimOp add = new PrimOp("Add");
		
		Name x0 = new Name("x0", new PrimitiveType("Int"));
		Name x1 = new Name("x1", new PrimitiveType("Int"));
		
		DataProductRepres two = new DataProductRepres(new IntDP(2, "twoInt"));
		DataProductRepres five = new DataProductRepres(new IntDP(5, "fiveInt"));
		
		LambdaExpression expr = increment.apply(x0.lambda(x1.lambda(add.apply(x0).apply(x1))).apply(two).apply(five));
		
		System.out.println("expr: \n" + expr);
		expr = LambdaFlattener.flattenExpression(expr);
		System.out.println("flattened expr: \n" + expr);
		
		
	}

}
