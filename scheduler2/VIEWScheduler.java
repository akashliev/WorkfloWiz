package scheduler2;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import translator.SWLAnalyzer;
import utility.Utility;
import utility.XMLParser;

public class VIEWScheduler {

	public static double[][] getDefaultWForHEFT(int v, int q) {
		double[][] W = new double[v][q];
		for (int i = 0; i < v; i++)
			for (int j = 0; j < q; j++)
				W[i][j] = 1;
		return W;
	}

	public static double[][] getDefaultBForHEFT(int q) {
		double[][] B = new double[q][q];
		for (int i = 0; i < q; i++)
			for (int j = 0; j < q; j++)
				B[i][j] = 10;
		return B;
	}

	public static double[][] getDefaultDataForHEFT(Element spec, ArrayList<String> allInstanceIds, int v) {
		double[][] data = new double[v][v];
		for (int i = 0; i < v; i++)
			for (int j = 0; j < v; j++)
				if (SWLAnalyzer.aIsConnectedToB(spec, allInstanceIds.get(i), allInstanceIds.get(j)))
					data[i][j] = 1;
				else
					data[i][j] = 0;

		return data;
	}

	public static double[] getDefaultLForHEFT(int q) {
		double[] L = new double[q];
		for (int i = 0; i < q; i++)
			L[i] = 0;
		return L;
	}

	public static JSONObject heftSchedule2VIEWschedule(ArrayList<String> allInstanceIds, ArrayList<String> listOfVMs,
			ArrayList<Task2Worker2AFT> heftSchedule) throws Exception {
		JSONObject schedule = new JSONObject();

		for (Task2Worker2AFT currTriple : heftSchedule)
			schedule.put(allInstanceIds.get(currTriple.getTaskId()), listOfVMs.get(currTriple.getWorkerId()));

		return schedule;

	}

	public static JSONObject schedule(Element spec, ArrayList<String> listOfVMs) throws Exception {
		if (listOfVMs.size() == 1) {
			JSONObject result = new JSONObject();
			for (String compID : SWLAnalyzer.getAllReferencedComponentIDs(spec))
				result.put(compID, listOfVMs.get(0));
			return result;
		}
		Element specWDummy = XMLParser.copyDocument(spec.getOwnerDocument()).getDocumentElement();
		if (SWLAnalyzer.getAllInputComponentIds(spec).size() > 1)
			SWLAnalyzer.addDummyComponent(specWDummy);
		// System.out.println(Utility.nodeToString(specWDummy));
		ArrayList<String> allPWorkflowIds = SWLAnalyzer.getAllInstanceIds(specWDummy);
		int q = listOfVMs.size();
		int v = allPWorkflowIds.size();

		double[][] W;

		double[][] data;
		double[][] B;
		double[] L;

		W = getDefaultWForHEFT(v, q);
		data = getDefaultDataForHEFT(specWDummy, allPWorkflowIds, v);
		B = getDefaultBForHEFT(q);
		L = getDefaultLForHEFT(q);

		// System.out.println("//////////////////////////////");
		// System.out.println(allPWorkflowIds);
		// schedulerUtility.print2DArray(B);

		HEFTScheduler heftScheduler = new HEFTScheduler(W, data, B, L);

		heftScheduler.schedule();

		JSONObject schedule = heftSchedule2VIEWschedule(allPWorkflowIds, listOfVMs, heftScheduler.schedule);

		// printSchedule(schedule);

		return schedule;
	}

	public static void printSchedule(JSONObject schedule) throws Exception {
		System.out.println(Utility.json2String(schedule, 4));
	}

}
