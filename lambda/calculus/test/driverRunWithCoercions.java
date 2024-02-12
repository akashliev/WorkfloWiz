package lambda.calculus.test;

import lambda.calculus.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import dataProduct.IntDP;

import repository.Repository;
import utility.LoggingLevels;
import utility.Utility;
import webbench.WebbenchUtility;

public class driverRunWithCoercions {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WebbenchUtility.initializeWebbenchConfig();
		Utility.loggingLevel = LoggingLevels.onlySystemOutput;

		int numberOfDPsInTheDP = ((JSONArray) WebbenchUtility.getWFandDPMetadata().get("dataProducts")).length();
		String runID = new Integer(numberOfDPsInTheDP).toString();

		JSONObject outputStub2DPNames = WebbenchUtility.runWorkflow("complexSubtyping", runID, false, true);

		System.out.println("map: " + outputStub2DPNames);
		
//		Document originalSWL = Repository.getWorkflowSpecification("complexSubtyping3").getOwnerDocument();
//		
//		LambdaExpression expr = SWLToLambdaTranslator.translateExperiment(originalSWL);
////		LambdaEvaluator.reducePrimOps = false;
////		System.out.println(LambdaEvaluator.evaluate(expr));
//		
//		Document swlWithCoercions = CoercionInserter.insertCoercionsInSWL(originalSWL);
//		System.out.println(Utility.nodeToString(swlWithCoercions));
//		
//		PrimOp add = new PrimOp("Add");
//		DataProductRepres twoInt = new DataProductRepres(new IntDP(2, "twoInt"));
//		DataProductRepres threeIng = new DataProductRepres(new IntDP(2, "threeIng"));
//		
		
		

	}

}
