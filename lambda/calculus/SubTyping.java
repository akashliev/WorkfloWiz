package lambda.calculus;

import java.util.ArrayList;

import lambda.calculus.types.ArrowType;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;
import lambda.calculus.types.XSDType;

import org.w3c.dom.Document;

import utility.Utility;

public class SubTyping {
	public static String errorMsg = "";
	public static Gamma gamma = null;

	public static boolean subtype(Type tyS, Type tyT) {
		// S-Refl:
		if (tyS.equals(tyT))
			return true;

		if (tyS instanceof PrimitiveType && ((PrimitiveType) tyT).isString())
			return true;

		if (tyS instanceof XSDType) {
			if (!(tyT instanceof XSDType))
				return false;

			boolean tmp = subtypeXSD(((XSDType) tyS), ((XSDType) tyT));
			if (!tmp)
				System.out.println("ffffffffffffffffffffffffffffffffffffffffffalse returned for : " + tyS + " ||| " + tyT);
			return tmp;

		}

		if ((((PrimitiveType) tyS).isInteger() || ((PrimitiveType) tyS).isNonPositiveInteger() || ((PrimitiveType) tyS).isNegativeInteger()
				|| ((PrimitiveType) tyS).isNonNegativeInteger() || ((PrimitiveType) tyS).isUnsignedLong() || ((PrimitiveType) tyS).isUnsignedInt()
				|| ((PrimitiveType) tyS).isUnsignedShort() || ((PrimitiveType) tyS).isUnsignedByte() || ((PrimitiveType) tyS).isPositiveInteger()
				|| ((PrimitiveType) tyS).isDouble() || ((PrimitiveType) tyS).isFloat() || ((PrimitiveType) tyS).isLong()
				|| ((PrimitiveType) tyS).isInt() || ((PrimitiveType) tyS).isShort() || ((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS)
				.isBoolean()) && ((PrimitiveType) tyT).isDecimal())
			return true;
		if ((((PrimitiveType) tyS).isNonPositiveInteger() || ((PrimitiveType) tyS).isNegativeInteger()
				|| ((PrimitiveType) tyS).isNonNegativeInteger() || ((PrimitiveType) tyS).isUnsignedLong() || ((PrimitiveType) tyS).isUnsignedInt()
				|| ((PrimitiveType) tyS).isUnsignedShort() || ((PrimitiveType) tyS).isUnsignedByte() || ((PrimitiveType) tyS).isPositiveInteger()
				|| ((PrimitiveType) tyS).isLong() || ((PrimitiveType) tyS).isInt() || ((PrimitiveType) tyS).isShort()
				|| ((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS).isBoolean())
				&& ((PrimitiveType) tyT).isInteger())
			return true;
		if (((PrimitiveType) tyS).isNegativeInteger() && ((PrimitiveType) tyT).isNonPositiveInteger())
			return true;
		if ((((PrimitiveType) tyS).isUnsignedLong() || ((PrimitiveType) tyS).isUnsignedInt() || ((PrimitiveType) tyS).isUnsignedShort()
				|| ((PrimitiveType) tyS).isUnsignedByte() || ((PrimitiveType) tyS).isPositiveInteger())
				&& ((PrimitiveType) tyT).isNonNegativeInteger())
			return true;
		if ((((PrimitiveType) tyS).isUnsignedInt() || ((PrimitiveType) tyS).isUnsignedShort() || ((PrimitiveType) tyS).isUnsignedByte())
				&& ((PrimitiveType) tyT).isUnsignedLong())
			return true;
		if ((((PrimitiveType) tyS).isUnsignedShort() || ((PrimitiveType) tyS).isUnsignedByte()) && ((PrimitiveType) tyT).isUnsignedInt())
			return true;
		if ((((PrimitiveType) tyS).isUnsignedByte()) && ((PrimitiveType) tyT).isUnsignedShort())
			return true;
		if ((((PrimitiveType) tyS).isInt() || ((PrimitiveType) tyS).isShort() || ((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS)
				.isBoolean()) && ((PrimitiveType) tyT).isLong())
			return true;
		if ((((PrimitiveType) tyS).isShort() || ((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS).isBoolean())
				&& ((PrimitiveType) tyT).isInt())
			return true;
		if ((((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS).isBoolean()) && ((PrimitiveType) tyT).isShort())
			return true;
		if ((((PrimitiveType) tyS).isBoolean()) && ((PrimitiveType) tyT).isByte())
			return true;
		if ((((PrimitiveType) tyS).isFloat() || ((PrimitiveType) tyS).isLong() || ((PrimitiveType) tyS).isInt() || ((PrimitiveType) tyS).isShort()
				|| ((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS).isBoolean())
				&& ((PrimitiveType) tyT).isDouble())
			return true;
		if ((((PrimitiveType) tyS).isLong() || ((PrimitiveType) tyS).isInt() || ((PrimitiveType) tyS).isShort() || ((PrimitiveType) tyS).isByte() || ((PrimitiveType) tyS)
				.isBoolean()) && ((PrimitiveType) tyT).isFloat())
			return true;

		// TODO: arrowType
		return false;
	}

	public static boolean subtypeXSD(XSDType tyS, XSDType tyT) {
//		System.out.println("subtype XSD: " + tyS + "\n" + tyT);
		if (tyS.child != null) {
			return subtype(tyS.child, tyT.child);
		}

		if (sameContentAlthoughDifferentWrappers(tyT, tyS)) {
			tyS = getContentRemoveWrapper(tyS);
			tyT = getContentRemoveWrapper(tyT);
		}

		if (!tyS.elementName.equals(tyT.elementName)) {
			return false;
		}

		if (tyS.children.size() < tyT.children.size())
			return false;

		if (tyS.children.size() == 0)
			return true;

		for (Type currChildInS : tyS.children) {
			if (currChildInS instanceof XSDType) {
				if (correspondingChildExists(((XSDType) currChildInS).elementName, tyT.children)) {
					XSDType correspondingChild = findCorrespondingChild(((XSDType) currChildInS).elementName, tyT.children);
					if (!subtypeXSD((XSDType) currChildInS, correspondingChild))
						return false;
				}
			}
		}

		for (Type currChildInS : tyS.children) {
			if (currChildInS instanceof PrimitiveType) {
				if (tyT.children.size() > 1)
					return false;
				if (!subtype(currChildInS, tyT.children.get(0)))
					return false;
			}
		}

		return true;
	}

	public static boolean sameContentAlthoughDifferentWrappers(XSDType tyS, XSDType tyT) {
//		System.out.println("sameContent: \n" + tyS + "\n" + tyT);
		// IMPORTANT: tyT is the one with return element
		if (tyS.elementName.equals(tyT.elementName))
			return false;

		Type childOfSType = tyS.children.get(0);
		String childOfSName = null;

		if (childOfSType instanceof XSDType)
			childOfSName = ((XSDType) childOfSType).elementName;

		Type childOfT = tyT.children.get(0);
		if (childOfT instanceof XSDType) {
			Type grandChildOfT = ((XSDType) childOfT).children.get(0);
			// System.out.println("childOfT: " + childOfT);
			if (grandChildOfT instanceof XSDType)
				return (childOfSName.equals(((XSDType) grandChildOfT).elementName));
		}

		return false;
	}

	public static XSDType getContentRemoveWrapper(XSDType T) {
		if (T.children.get(0) instanceof XSDType && ((XSDType) T.children.get(0)).elementName.equals("return"))
			return (XSDType) ((XSDType) T.children.get(0)).children.get(0);
		else if (T.children.get(0) instanceof XSDType)
			return (XSDType) ((XSDType) T.children.get(0));
		return null;
	}

	public static boolean correspondingChildExists(String name, ArrayList<XSDType> childrenInT) {
		for (XSDType currChild : childrenInT)
			if (currChild.elementName.equals(name))
				return true;
		return false;
	}

	public static XSDType findCorrespondingChild(String name, ArrayList<XSDType> childrenInT) {
		for (XSDType currChild : childrenInT)
			if (currChild.elementName.equals(name))
				return currChild;
		return null;
	}

	public static boolean isWellTyped(Document spec) throws Exception {
		return typeOf(spec) != null;
	}

	public static Type typeOf(Document spec) throws Exception {
		errorMsg = "";
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(spec);
		// System.out.println("expr: " + expr);
		if (expr == null)
			return null;

		gamma = Typing.getContext(spec.getDocumentElement());
		// System.out.println("ggggggamma: " + gamma);
		Type result = typeOf(expr, gamma);
		// if (result != null)
		// errorMsg = "workflow " + SWLAnalyzer.getWorkflowName(spec.getDocumentElement()) + " is well-typed";
		// else
		// errorMsg = "workflow " + SWLAnalyzer.getWorkflowName(spec.getDocumentElement()) + " is not well-typed: " + errorMsg;

		return result;
	}

	public static Type typeOf(LambdaExpression expr, Gamma gamma) {
		// if (expr instanceof DataProductRepres)
		// return ((DataProductRepres) expr).type;
		// else if
		if (expr instanceof Name || expr instanceof DataProductRepres) {
			return gamma.getBinding(expr);
		} else if (expr instanceof PrimOp) {
			return gamma.getBinding(expr);
		} else if (expr instanceof Abstraction) {
			Gamma gammaTmp = gamma.clone();

			gammaTmp.addBinding(((Abstraction) expr).name, ((Abstraction) expr).name.type);
			Type tyT2 = typeOf(((Abstraction) expr).expr, gammaTmp);
			if (tyT2 == null)
				return null;
			return new ArrowType(((Abstraction) expr).name.type, tyT2);
		} else if (expr instanceof Application) {
			Type typeOfF = typeOf(((Application) expr).f, gamma);
			if (typeOfF == null)
				return null;

			Type typeOfN = typeOf(((Application) expr).n, gamma);
			if (typeOfN == null)
				return null;

			if (typeOfF instanceof ArrowType) {
				if (subtype(typeOfN, ((ArrowType) typeOfF).types.get(0))) {
					ArrayList<Type> allTypesButFirstFromF = new ArrayList<Type>();
					for (int i = 1; i < ((ArrowType) typeOfF).types.size(); i++)
						allTypesButFirstFromF.add(((ArrowType) typeOfF).types.get(i));
					if (allTypesButFirstFromF.size() > 1)
						return new ArrowType(allTypesButFirstFromF);
					else
						return allTypesButFirstFromF.get(0);
				} else {
					System.out.println("typeOfN: " + typeOfN);
					System.out.println("typeOfF: " + typeOfF);
					System.out.println("application is wrong: " + expr.toStringWithTyping());
					System.out.println("parameter type mismatch: " + ((Application) expr).f + ":" + typeOfF + "\nwith \n" + ((Application) expr).n
							+ ":" + typeOfN);
					errorMsg = "parameter type mismatch: " + ((Application) expr).f + ":" + typeOfF + " with " + ((Application) expr).n + ":"
							+ typeOfN;
				}

			} else {
				System.out.println("arrow type expected in " + ((Application) expr).f);
				errorMsg = "arrow type expected in " + ((Application) expr).f;
			}
		}
		return null;
	}

}
