package lambda.calculus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lambda.calculus.types.PrimitiveType;


public class AlphaConverter {
	// this map stores a set of tempName : originalName pairs
	private HashMap<String, String> alphaConversionMap = new HashMap<String, String>();

	public void alphaConvert(LambdaExpression expr) {
		int counter = 0;

		String nameClash = findFirstNameClash(expr);
		String tempName = null;

		while (nameClash != null) {
			tempName = nameClash + "" + counter;
			alphaConversionMap.put(tempName, nameClash);
			Abstraction abstrToBeRenamed = findFirstAbstrWithName(expr, nameClash);

			if (abstrToBeRenamed.name.name.trim().equals(nameClash.trim()))
				abstrToBeRenamed.name.name = tempName;
			renameAllFreeOccurrencesOfVar(abstrToBeRenamed.expr, nameClash, tempName);
			counter++;
			nameClash = findFirstNameClash(expr);
		}
	}

	private void renameAllFreeOccurrencesOfVar(LambdaExpression expr, String oldName, String tempName) {
		if (expr instanceof Name) {
			if (((Name) expr).name.equals(oldName))
				((Name) expr).name = tempName;
		} else if (expr instanceof Abstraction) {
			if (!((Abstraction) expr).name.name.equals(oldName))
				renameAllFreeOccurrencesOfVar(((Abstraction) expr).expr, oldName, tempName);

		} else if (expr instanceof Application) {
			renameAllFreeOccurrencesOfVar(((Application) expr).f, oldName, tempName);
			renameAllFreeOccurrencesOfVar(((Application) expr).n, oldName, tempName);
		}
	}

	private void renameAllOccurrencesOfVar(LambdaExpression expr, String tempName, String originalName) {
		if (expr instanceof Name) {
			if (((Name) expr).name.equals(tempName))
				((Name) expr).name = originalName;
		} else if (expr instanceof Abstraction) {

			if (((Abstraction) expr).name.name.equals(tempName))
				((Abstraction) expr).name.name = originalName;

			renameAllOccurrencesOfVar(((Abstraction) expr).expr, tempName, originalName);

		} else if (expr instanceof Application) {
			renameAllOccurrencesOfVar(((Application) expr).f, tempName, originalName);

			renameAllOccurrencesOfVar(((Application) expr).n, tempName, originalName);
		}
	}

	public void alphaConvertBack(LambdaExpression expr) {
		for (String tempName : alphaConversionMap.keySet())
			renameAllOccurrencesOfVar(expr, tempName, alphaConversionMap.get(tempName));
		alphaConversionMap.clear();
	}

	private String findFirstNameClash(LambdaExpression expr) {
		return findFirstNameClash(expr, new ArrayList<String>());
	}

	private String findFirstNameClash(LambdaExpression expr, ArrayList<String> alreadyUsedAbstractionNames) {
		String clashedName = null;
		if (expr instanceof Name)
			return null;

		else if (expr instanceof Abstraction) {

			if (alreadyUsedAbstractionNames.contains(((Abstraction) expr).name.name))
				return ((Abstraction) expr).name.name;
			alreadyUsedAbstractionNames.add(((Abstraction) expr).name.name);

			clashedName = findFirstNameClash(((Abstraction) expr).expr, alreadyUsedAbstractionNames);
		}

		else if (expr instanceof Application) {
			clashedName = findFirstNameClash(((Application) expr).f, alreadyUsedAbstractionNames);
			if (clashedName != null)
				return clashedName;

			clashedName = findFirstNameClash(((Application) expr).n, alreadyUsedAbstractionNames);
			if (clashedName != null)
				return clashedName;

			if (clashedName == null) {
				HashSet<String> allAbstractionNamesInF = findAllAbstractionNames(((Application) expr).f);
				HashSet<String> allAbstractionNamesInN = new HashSet<String>();
				HashSet<String> allAbstrNames = findAllAbstractionNames(((Application) expr).n);
				if (allAbstrNames != null)
					allAbstractionNamesInN.addAll(allAbstrNames);

				HashSet<String> allFreeVarNamesInF = findAllFreeVarNames(((Application) expr).f);
				HashSet<String> allFreeVarNamesInN = new HashSet<String>();
				HashSet<String> allFreeVars = findAllFreeVarNames(((Application) expr).n);
				if (allFreeVars != null)
					allFreeVarNamesInN.addAll(allFreeVars);

				if (allAbstractionNamesInF != null && allFreeVarNamesInN != null) {

					HashSet<String> abstrNamesInF_freeVarNamesInN = intersection(allAbstractionNamesInF, allFreeVarNamesInN);
					if (abstrNamesInF_freeVarNamesInN.size() > 0)
						return abstrNamesInF_freeVarNamesInN.iterator().next();
				}

				if (allFreeVarNamesInF != null && allAbstractionNamesInN != null) {
					HashSet<String> freeVarNamesInF_AbstrNamesInN = intersection(allFreeVarNamesInF, allAbstractionNamesInN);
					if (freeVarNamesInF_AbstrNamesInN.size() > 0)
						return freeVarNamesInF_AbstrNamesInN.iterator().next();
				}

			}

			return clashedName;
		}
		return clashedName;
	}

	private HashSet<String> findAllVarNames(LambdaExpression expr) {
		return findAllVarNames(expr, new HashSet<String>());
	}

	private HashSet<String> findAllVarNames(LambdaExpression expr, HashSet<String> allVarNames) {

		if (expr instanceof Name) {
			allVarNames.add(((Name) expr).name);
			return allVarNames;
		}

		else if (expr instanceof Abstraction) {
			allVarNames.add(((Abstraction) expr).name.name);
			return findAllVarNames(((Abstraction) expr).expr, allVarNames);
		}

		else if (expr instanceof Application) {
			HashSet<String> allVarNamesInF = findAllVarNames(((Application) expr).f, allVarNames);
			if (allVarNamesInF != null)
				allVarNames.addAll(allVarNamesInF);
			HashSet<String> allVarNamesInN = new HashSet<String>();
			HashSet<String> allVarNamesInCurrArg = findAllVarNames(((Application) expr).n, allVarNames);
			if (allVarNamesInCurrArg != null)
				allVarNamesInN.addAll(allVarNamesInCurrArg);
			allVarNames.addAll(allVarNamesInN);
			return allVarNames;
		}
		return null;
	}

	private HashSet<String> findAllAbstractionNames(LambdaExpression expr) {
		return findAllAbstractionNames(expr, new HashSet<String>());
	}

	private HashSet<String> findAllAbstractionNames(LambdaExpression expr, HashSet<String> allAbstrNames) {

		if (expr instanceof Name) {
			return allAbstrNames;
		}

		else if (expr instanceof Abstraction) {
			allAbstrNames.add(((Abstraction) expr).name.name);
			return findAllAbstractionNames(((Abstraction) expr).expr, allAbstrNames);
		}

		else if (expr instanceof Application) {
			HashSet<String> allAbstrNamesFromF = findAllAbstractionNames(((Application) expr).f, allAbstrNames);
			if (allAbstrNamesFromF != null)
				allAbstrNames.addAll(allAbstrNamesFromF);
			HashSet<String> allAbstrnames = findAllAbstractionNames(((Application) expr).n, allAbstrNames);
			if (allAbstrNames != null)
				allAbstrNames.addAll(allAbstrNames);
			return allAbstrNames;
		}
		return null;
	}

	public HashSet<String> findAllFreeVarNames(LambdaExpression expr) {
		HashSet<String> allVarNames = findAllVarNames(expr);
		HashSet<String> allAbstNames = findAllAbstractionNames(expr);

		if (allAbstNames != null)
			allVarNames.removeAll(allAbstNames);
		return allVarNames;
	}

	private HashSet<String> intersection(HashSet<String> set1, HashSet<String> set2) {
		HashSet<String> result = new HashSet<String>(set1);
		result.retainAll(set2);
		return result;
	}

	private Abstraction findFirstAbstrWithName(LambdaExpression expr, String name) {
		if (expr instanceof Name) {
			return null;
		} else if (expr instanceof Abstraction) {

			if (((Abstraction) expr).name.name.equals(name))
				return (Abstraction) expr;

			return findFirstAbstrWithName(((Abstraction) expr).expr, name);
		} else if (expr instanceof Application) {
			Abstraction resultFromF = findFirstAbstrWithName(((Application) expr).f, name);
			if (resultFromF == null) {
				Abstraction resultFromCurrentArg = findFirstAbstrWithName(((Application) expr).n, name);
				if (resultFromCurrentArg != null)
					return resultFromCurrentArg;
			}
			return resultFromF;
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		Name a0 = new Name("a");
		Name a1 = new Name("a");
		Name a2 = new Name("a");
		Name a3 = new Name("a");
		
		Name b = new Name("b");
		Name x0 = new Name("x");
		Name x1 = new Name("x");
		Name x2 = new Name("x");
		Name x3 = new Name("x");
		Name x4 = new Name("x");
		Name y0 = new Name("y");
		Name y1 = new Name("y");
		Name y2 = new Name("y");
		Name z0 = new Name("z");
		Name z1 = new Name("z");
		Name z2 = new Name("z");
		Name z3 = new Name("z");
		Name z4 = new Name("z");

		AlphaConverter ac = new AlphaConverter();

		// λy.λx.λy.(x y)
		LambdaExpression expr = y0.lambda(x0.lambda(y1.lambda(x1.apply(y2))));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash(expr));

		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		expr = y0.lambda(x0.lambda(x1.apply(y1.lambda(y2))));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash(expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		expr = x3.lambda(x0).apply(x1.lambda(x2.apply(y0)));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash(expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		expr = z0.lambda(x0.lambda(x1).apply(x2.lambda(x3.apply(y0))));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash(expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		// λx.(a x)
		System.out.println("expression:" + (LambdaExpression) x0.lambda(a0.apply(x1)));
		System.out.println("all variable names: " + ac.findAllVarNames((LambdaExpression) x0.lambda(a0.apply(x1))));
		System.out.println("all free variable names: " + ac.findAllFreeVarNames((LambdaExpression) x0.lambda(a0.apply(x1))));

		// λz.(λa.(z a) λx.(b x))
		expr = z0.lambda(a0.lambda(z1.apply(a1)).apply(x0.lambda(b.apply(x1))));
		System.out.println("expression: " + expr);
		System.out.println("all variable names:" + ac.findAllVarNames(expr));
		System.out.println("all abstraction names: "
				+ ac.findAllAbstractionNames((LambdaExpression) z0.lambda(a0.lambda(z1.apply(a1)).apply(x0.lambda(b.apply(x1))))));
		System.out.println("free vars:" + ac.findAllFreeVarNames(expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		
		expr = z0.lambda(a0.lambda(z1.apply(a1)).apply(x0.lambda(a2.apply(x1))));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash((LambdaExpression) expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		expr = z0.lambda(x0.lambda(a0.apply(x1)).apply(a1.lambda(z1.apply(a2))));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash(expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		
		expr = z0.lambda(x0.lambda(a0.apply(x1))).apply(a1.lambda(b.apply(a2)));
		System.out.println("expression: " + expr);
		System.out.println("first clash:" + ac.findFirstNameClash(expr));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		
		expr = y0.lambda(x0.lambda(x1.apply(x2.lambda(x3))));
		System.out.println("expression: " + expr);
		System.out.println("find Abstr with x: " + ac.findFirstAbstrWithName(expr, "x"));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		
//		System.out.println(((LambdaExpression) (x.lambda(x.apply(x.lambda(x))))).isBound(new Name("x")));
//		System.out.println(((LambdaExpression) x.apply(x)).isBound(new Name("x")));
//		System.out.println(((LambdaExpression) (x.lambda(x.apply(z.lambda(z))))).isBound(new Name("z")));
//
		Name x5 = new Name("x");
//
		expr = (LambdaExpression) x0.lambda(x1.apply(z0.lambda(x2.apply(x3.lambda(x4.apply(x5))))));
		System.out.println("expression: " + expr);
		// renameAllOccurrencesOfVar(((Abstraction) abstr).expr, "x", "i0");
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
//
		Name x10 = new Name("x");
		Name x11 = new Name("x");
		Name x12 = new Name("x");
		Name x13 = new Name("x");
		Name x14 = new Name("x");
		Name x15 = new Name("x");
		Name x16 = new Name("x");
		
		expr = (LambdaExpression) x10.lambda(x11.apply(z0.lambda(x12.apply(x13.lambda(x14.apply(x15.lambda(x16)))))));
		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		Name x20 = new Name("x");
		Name x21 = new Name("x");
		Name x22 = new Name("x");
		Name x23 = new Name("x");
		Name x24 = new Name("x");
		Name x25 = new Name("x");
		Name x26 = new Name("x");
		Name x27 = new Name("x");
		Name x28 = new Name("x");

		expr = (LambdaExpression) x20.lambda(x21.apply(z0.lambda(x22.apply(x23.lambda(x24.apply(x25.lambda(x26.apply(x27.lambda(x28)))))))));
		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);

		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		System.out.println("alpha-converted-back expression: " + expr);
		

		
		// find first abstraction with name x in expression: λx.(x λx.(x x)) u
		Name x30 = new Name("x");
		Name x31 = new Name("x");
		Name u = new Name("u");
		Name r = new Name("r");
//		x.name = "x";
		x0.name = "x";
		x1.name = "x";

		LambdaExpression identifyX1 = (LambdaExpression) x31.lambda(x31.apply(x31));
		expr = (LambdaExpression) x30.lambda(x30.apply(identifyX1)).apply(u);
		System.out.println("\nexpression: " + expr);
		System.out.println(ac.findFirstAbstrWithName(expr, "x"));
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		System.out.println("alpha-converted-back expression: " + expr);
		
		
		// λx.(x λx.x) (u r)
		expr = (LambdaExpression) x0.lambda(x0.apply(x1.lambda(x2))).apply(u.apply(r));
		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		
		// λx.(x λx.x)
		expr = (LambdaExpression) x0.lambda(x0.apply(x1.lambda(x2)));
		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);
		System.out.println("alpha-converted-back expression: " + expr);

		System.out.println("================================================");

		expr = (LambdaExpression) x3.lambda(x0.apply(x1.lambda(x2))).apply(u.apply(r));

		String firstNameClash = ac.findFirstNameClash(expr);
		System.out.println(firstNameClash);
		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);
		System.out.println("alpha-converted expression: " + expr);
		ac.alphaConvertBack(expr);

		System.out.println("================================================");
		
		
		System.out.println("-------------next---------------");

		PrimOp Multiply = new PrimOp("Multiply");
		PrimOp Add = new PrimOp("Add");
		DataProductRepres three = new DataProductRepres("three", 3, new PrimitiveType("Int"));
		DataProductRepres four = new DataProductRepres("four", 4, new PrimitiveType("Int"));
		DataProductRepres two = new DataProductRepres("two", 2, new PrimitiveType("Int"));

		Name n13 = new Name("x3");
		Name n11 = new Name("x1");
		Name n10 = new Name("x0");

		Name n20 = new Name("x0");
		Name n21 = new Name("x1");
		Name n22 = new Name("x2");

		// expr = innerLambd;
		System.out.println("\nexpression: " + expr);
		ac.alphaConvert(expr);
//
//		System.out.println("alpha-converted expression: " + expr);
//		ac.alphaConvertBack(expr);
//		System.out.println("alpha-converted-back expression: " + expr);

		System.out.println("evaluate: " + LambdaEvaluator.evaluate(expr));

		System.out.println("++++++++++++++++++++++++++");
		PrimOp incr = new PrimOp("Increment");
		PrimOp square = new PrimOp("Square");
		expr = (LambdaExpression) incr.apply(square.apply(three));
		System.out.println("\nexpression: " + expr);
		LambdaExpression result = LambdaEvaluator.evaluate(expr);
		System.out.println("evaluate: " + result);
	}
}
