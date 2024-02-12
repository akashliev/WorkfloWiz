package lambda.calculus.test;

import java.math.BigInteger;

public class driverBigInteger {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int bad = 2000000000; // Close to int max value.
		System.out.println("bad     = " + bad);
		System.out.println("bad + 1 = " + (bad + 1));
		System.out.println("bad * 3 = " + (bad * 3));
		System.out.println("bad * 4 = " + (bad * 4));

		BigInteger good = new BigInteger("8000000000");
		System.out.println();
		System.out.println("good     = " + good);
		System.out.println("good.add(BigInteger.ONE) = " + good.add(BigInteger.ONE));
		System.out.println("good.add(BigInteger.valueOf(1)) = " + good.add(BigInteger.valueOf(1)));
		System.out.println("good.multiply(BigInteger.valueOf(3)) = " + good.multiply(BigInteger.valueOf(3)));
		System.out.println("good.multiply(BigInteger.valueOf(4)) = " + good.multiply(BigInteger.valueOf(4)));

	}

}
