package lambda.calculus;

import java.util.ArrayList;

import lambda.calculus.types.ArrowType;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;
import lambda.calculus.types.XSDType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import toyDPM.DataProductManager;
import translator.SWLAnalyzer;
import utility.Inst2Xsd;
import utility.Utility;
import utility.XMLParser;
import webbench.WebbenchUtility;
import dataProduct.StringDP;
import dataProduct.XmlDP;

public class CoercionInserter {

	public static Document insertCoercionsInSWL(Document originalSWL) throws Exception {
		if (!SWLAnalyzer.workflowAndEachOfItsComponentsHasASingleOutputPort(originalSWL.getDocumentElement())) {
			System.out.println("workflow or one of its components have more than one output port");
			return originalSWL;
		}

		String outputDPid = SWLAnalyzer.getOutputDPIds(originalSWL.getDocumentElement()).get(0);
		LambdaExpression expr = SWLToLambdaTranslator.translateWorkflowOrExp(originalSWL);
		expr = LambdaFlattener.flattenExpression(expr);
		// System.out.println("flattened expression : " + expr);
		LambdaExpression exprWithCoercionsInserted = pennTranslation(Typing.getContext(originalSWL.getDocumentElement()), expr);
		exprWithCoercionsInserted = LambdaFlattener.flattenExpression(exprWithCoercionsInserted);
		exprWithCoercionsInserted = LambdaToSWLTranslator.simplifyExprByRemovingIdentityFs(exprWithCoercionsInserted);
		// System.out.println("exprWC:\n" + exprWithCoercionsInserted);
		System.out.println();
		return LambdaToSWLTranslator
				.translate(exprWithCoercionsInserted, SWLAnalyzer.getWorkflowName(originalSWL.getDocumentElement()), outputDPid);
	}

	public static LambdaExpression pennTranslation(Gamma gamma, LambdaExpression expr) throws Exception {
		Type T = SubTyping.typeOf(expr, gamma);
		if (T == null || !T.isValidType())
			return null;
		LambdaExpression result = pennTranslation(gamma, expr, T);
		System.out.println("result:\n" + result);
		result = LambdaToSWLTranslator.simplifyExprByRemovingIdentityFs(result);
		return result;
	}

	// public static LambdaExpression pennTranslationOld(Type S, Type T) throws Exception {
	// if (SubTyping.subtype(S, T)) {
	// if (S instanceof PrimitiveType)
	// return new PrimOp(S.id + "2" + T.id, new ArrowType(S, T));
	// else if (S instanceof XSDType) {
	// XSDType Sxsd = (XSDType) S;
	// XSDType Txsd = (XSDType) T;
	// if (SubTyping.sameContentAlthoughDifferentWrappers(Txsd, Sxsd)) {
	// Sxsd = SubTyping.getContentRemoveWrapper(Sxsd);
	// Txsd = SubTyping.getContentRemoveWrapper(Txsd);
	// }
	// Name r = new Name();
	// StringDP l_dp = new StringDP(((XSDType) Sxsd).elementName, "l_" + ((XSDType) Sxsd).elementName);
	// ArrayList<StringDP> kj_dps = new ArrayList<StringDP>();
	// DataProductRepres l = new DataProductRepres(l_dp);
	// PrimOp compose = new PrimOp("compose" + Txsd.children.size());
	// compose.numOfArgs = Txsd.children.size();
	//
	// LambdaExpression compose_l_apply_wrap_kj_cj_extract_kj_r = compose.apply(l);
	//
	// // if T is a simple type (e.g. <a>25</a>, but not <ab><a>22</a><b>33</b></ab>)
	// if (Txsd.child != null) {
	// if (Txsd.child.equals(Sxsd.child)) {
	// Name x = new Name("x");
	// return x.lambda(x);
	// } else if (Sxsd.child != null) {
	// Name x = new Name();
	// return x.lambda(pennTranslationOld(Sxsd.child, Txsd.child).apply(new PrimOp("getSimpleContent").apply(x)));
	// // return new PrimOp(Sxsd.child.id + "2" + Txsd.child.id, new ArrowType(Sxsd.child, Txsd.child));
	// }
	// }
	//
	// for (int i = 0; i < Txsd.children.size(); i++) {
	// XSDType currChildOfT = Txsd.children.get(i);
	// Type currChildOfS = Sxsd.children.get(i);
	// currChildOfS = SubTyping.findCorrespondingChild(((XSDType) currChildOfT).elementName, Sxsd.children);
	// String kj = ((XSDType) currChildOfT).elementName;
	// StringDP kj_dp = new StringDP(kj, "kj_" + kj);
	// kj_dps.add(kj_dp);
	// DataProductRepres kjDpr = new DataProductRepres(kj_dp);
	// LambdaExpression extract_kj_r = (new PrimOp("extract")).apply(kjDpr).apply(r);
	//
	// LambdaExpression cj = pennTranslationOld(currChildOfS, currChildOfT);
	// LambdaExpression cj_extract_kj_r = cj.apply(extract_kj_r);
	// PrimOp wrap = new PrimOp("wrap");
	// LambdaExpression wrap_kj_cj_extract_kj_r = wrap.apply(kjDpr).apply(cj_extract_kj_r);
	// compose_l_apply_wrap_kj_cj_extract_kj_r = compose_l_apply_wrap_kj_cj_extract_kj_r.apply(wrap_kj_cj_extract_kj_r);
	// }
	//
	// if (DataProductManager.getDataProductDPL(l_dp.dataName).trim() == "")
	// DataProductManager.registerDataProduct(l_dp);
	// for (StringDP curr_kj_dp : kj_dps) {
	// if (DataProductManager.getDataProductDPL(curr_kj_dp.dataName).trim() == "")
	// DataProductManager.registerDataProduct(curr_kj_dp);
	// }
	//
	// return r.lambda(compose_l_apply_wrap_kj_cj_extract_kj_r);
	// }
	// }
	// System.out.println("CoercionInserter.pennTranslation(S, T) returning null since S is not a subtype of T " + S +
	// " not subtype of " + T);
	// return null;
	// }

	public static LambdaExpression pennTranslation(Type S, Type T) throws Exception {
		if (SubTyping.subtype(S, T)) {
			if (S.equals(T)) {
				// S-REFL:
				Name x = new Name();
				return x.lambda(x);
			}
			if (S instanceof PrimitiveType) {
				// S-PRIM:
				return new PrimOp(S.id + "2" + T.id, new ArrowType(S, T));
			} else if (S instanceof XSDType) {
				// S-XSD1 and X-XSD2:
				XSDType Sxsd = (XSDType) S;
				XSDType Txsd = (XSDType) T;
				if (SubTyping.sameContentAlthoughDifferentWrappers(Txsd, Sxsd)) {
					Sxsd = SubTyping.getContentRemoveWrapper(Sxsd);
					Txsd = SubTyping.getContentRemoveWrapper(Txsd);
				}

				String e = Txsd.elementName;
				StringDP e_dp = new StringDP(e, "e_" + e);
				DataProductRepres e_dpr = new DataProductRepres(e_dp);

				if (DataProductManager.getDataProductDPL(e_dp.dataName).trim() == "")
					DataProductManager.registerDataProduct(e_dp);

				// S-XSD2:
				if (Txsd.child != null) {
					Name x = new Name();
					PrimOp wrap = new PrimOp("wrap");
					return x.lambda(wrap.apply(e_dpr).apply(pennTranslation(Sxsd.child, Txsd.child).apply(new PrimOp("getSimpleContent").apply(x))));
				}

				Name x = new Name();
				PrimOp compose = new PrimOp("compose" + Txsd.children.size());

				// LambdaExpression compose_e_cj_x = compose.apply(e_dpr);
				LambdaExpression compose_e_cj_extr_kj_x = compose.apply(e_dpr);
				
				compose.numOfArgs = Txsd.children.size();

				// LambdaExpression cj_x = null;

				for (int i = 0; i < Txsd.children.size(); i++) {
					XSDType currChildOfT = Txsd.children.get(i);
					Type currChildOfS = SubTyping.findCorrespondingChild(((XSDType) currChildOfT).elementName, Sxsd.children);
					LambdaExpression cj = pennTranslation(currChildOfS, currChildOfT);

					String kj = ((XSDType) currChildOfT).elementName;
					StringDP kj_dp = new StringDP(kj, "kj_" + kj);
					DataProductRepres kj_dpr = new DataProductRepres(kj_dp);
					LambdaExpression cj_extract_kj_x = cj.apply((new PrimOp("extract")).apply(kj_dpr).apply(x));

					if (DataProductManager.getDataProductDPL(kj_dp.dataName).trim() == "")
						DataProductManager.registerDataProduct(kj_dp);

					compose_e_cj_extr_kj_x = compose_e_cj_extr_kj_x.apply(cj_extract_kj_x);
					// cj_x = cj.apply(x);
					// compose_e_cj_x = compose_e_cj_x.apply(cj.apply(x));
				}

				return x.lambda(compose_e_cj_extr_kj_x);

			}
		}
		System.out.println("CoercionInserter.pennTranslation(S, T) returning null since S is not a subtype of T " + S + " not subtype of " + T);
		return null;
	}

	private static LambdaExpression pennTranslation(Gamma gamma, LambdaExpression expr, Type T) throws Exception {
		// T-Sub:
		// System.out.println("gamma: " + gamma);
		// System.out.println("expr: " + expr);
		// System.out.println("Type: " + T);
		// if (!SubTyping.typeOf(expr, gamma).equals(T) && SubTyping.subtype(SubTyping.typeOf(expr, gamma), T))
		// return fSubtypeDer(SubTyping.typeOf(expr, gamma), T).apply(pennTranslation(gamma, expr, SubTyping.typeOf(expr,
		// gamma)));

		// T-Var:
		if (expr instanceof DataProductRepres)
			return expr;
		else if (expr instanceof Name)
			return expr;
		else if (expr instanceof PrimOp) {
			return expr;
		}
		// T-Abs:
		else if (expr instanceof Abstraction) {
			Gamma gammaTmp = gamma.clone();

			gammaTmp.addBinding(((Abstraction) expr).name, ((Abstraction) expr).name.type);
			Type T2 = SubTyping.typeOf(((Abstraction) expr).expr, gammaTmp);

			LambdaExpression translatedD2 = pennTranslation(gammaTmp, ((Abstraction) expr).expr, T2);

			return ((Abstraction) expr).name.lambda(translatedD2);
		}

		// T-App:
		else if (expr instanceof Application) {
			Type typeOfF = SubTyping.typeOf(((Application) expr).f, gamma);
			Type T1 = ((ArrowType) typeOfF).types.get(0);
			Type T3 = SubTyping.typeOf(((Application) expr).n, gamma);

			LambdaExpression translatedD1 = pennTranslation(gamma, ((Application) expr).f, typeOfF);

			LambdaExpression translatedD2 = pennTranslation(gamma, ((Application) expr).n, T3);

			// System.out.println("subsumption took place: " + T3 + " <: " + T1);

			if (T3.equals(T1))
				return translatedD1.apply(translatedD2);

			LambdaExpression translatedC = pennTranslation(T3, T1);

			return translatedD1.apply(translatedC.apply(translatedD2));

			// return translatedC.apply(translatedD1.apply(translatedD2));
		}
		return null;
	}

	public static void main(String[] args) throws Exception {

		String abStr = "<ab><a>211111</a><b>311111</b></ab>";
		// String abStr = "<a>200000</a>";
		Element abSchema = XMLParser.getDocument(Inst2Xsd.getSchema(abStr)).getDocumentElement();
		XSDType abType = new XSDType(abSchema);
		Document docAb = XMLParser.getDocument(abStr);
		XmlDP xmlDpAb = new XmlDP(docAb.getDocumentElement(), "dataname_a");
		DataProductRepres xmlDprAb = new DataProductRepres(xmlDpAb);

		String abcStr = "<ab><a>222222</a><b>333333</b><c>444444</c></ab>";
		// String abcStr = "<a>20</a>";
		Element abcSchema = XMLParser.getDocument(Inst2Xsd.getSchema(abcStr)).getDocumentElement();
		XSDType abcType = new XSDType(abcSchema);
		Document docAbc = XMLParser.getDocument(abcStr);
		XmlDP xmlDpAbc = new XmlDP(docAbc.getDocumentElement(), "dataname_a");
		DataProductRepres xmlDprAbc = new DataProductRepres(xmlDpAbc);

		LambdaExpression coercion = pennTranslation(abcType, abType);
		System.out.println("overall result of translation: \n" + coercion);

		// System.out.println(LambdaToSWLTranslator.getShimNameForXMLCoercion((Abstraction) coercion));
		// Document shimSpec = LambdaToSWLTranslator.generateShim(coercion);
		// System.out.println(Utility.nodeToString(shimSpec));

		// DataProductRepres resultDPr = (DataProductRepres) LambdaEvaluator.evaluate(coercion.apply(xmlDprAbc));
		// System.out.println("**************");
		// System.out.println(Utility.nodeToString(((Element) resultDPr.data)));
		//
		// System.out.println("is coercion? " + LambdaToSWLTranslator.isCoercion(coercion));
	}

}
