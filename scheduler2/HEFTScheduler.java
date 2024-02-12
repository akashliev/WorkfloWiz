package scheduler2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import translator.Pair;

public class HEFTScheduler {
	int v;
	int q;

	// q x q:
	public double[][] B;

	// vector of size q:
	public double[] L;

	public double L_bar;

	// v x q:
	public double[][] W;

	// v x v:
	public double[][] C;
	public double[][] data;

	public TaskGraph graph = null;

	public ArrayList<PairUrank2TaskId> uranks2TaskIds = new ArrayList<PairUrank2TaskId>();

	public ArrayList<Task2Worker2AFT> schedule = new ArrayList<Task2Worker2AFT>();

	public HEFTScheduler(TaskGraph newGraph, double[][] newW) {
		v = newGraph.getSize();
		q = newW[0].length;
		W = newW;
		graph = newGraph;
	}

	public HEFTScheduler(double[][] newW, double[][] newData, double[][] newB, double[] newL) {
		v = newW.length;
		q = newW[0].length;
		graph = new TaskGraph(v);
		W = newW;
		data = newData;
		B = newB;
		setInfinitiesInB();
		L = newL;
		computeC_bar();
	}

	public void setInfinitiesInB() {
		// since rate of transferring data within the same processor is infinite:
		for (int i = 0; i < q; i++)
			for (int j = 0; j < q; j++)
				if (i == j)
					B[i][j] = Double.POSITIVE_INFINITY;
	}

	public void computeC_bar() {
		double B_bar = 0;
		for (int i = 0; i < q; i++)
			for (int j = 0; j < q; j++)
				if (i != j)
					B_bar += B[i][j];
		B_bar = B_bar / (q * q - q);

		double L_bar = 0;
		for (int i = 0; i < q; i++)
			L_bar += L[i];
		L_bar = L_bar / q;

		for (int i = 0; i < v; i++)
			for (int j = 0; j < v; j++)
				if (data[i][j] != 0)
					graph.C_bar[i][j] = L_bar + data[i][j] / B_bar;
	}

	public boolean isEntryTask(int i) {
		return graph.isEntryTask(i);
	}

	public boolean isExitTask(int i) {
		return graph.isExitTask(i);
	}

	public ArrayList<Integer> pred(int i) {
		return graph.getPredecessors(i);
	}

	public ArrayList<Integer> succ(int i) {
		return graph.getSuccessors(i);
	}

	public static double max(ArrayList<Double> list) {
		double max = list.get(0);
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) > max)
				max = list.get(i);
		return max;

	}

	public double avail(int j) {
		double result = 0;
		for (int i = 0; i < schedule.size(); i++)
			if (schedule.get(i).getWorkerId() == j)
				result = schedule.get(i).getAFT();
		return result;
	}

	public double AFT(int m) {
		for (Task2Worker2AFT currSched : schedule)
			if (currSched.getTaskId() == m)
				return currSched.getAFT();
		System.out.println("ERROR: Scheduler calling AFT on task " + m + ", which is has not been scheduled yet");
		return -1;
	}

	public double EST(int i, int j) {
		double est = -1;
		if (isEntryTask(i))
			return 0;
		double avail_j = avail(j);

		ArrayList<Double> innerMaxArg = new ArrayList<Double>();
		for (int m : pred(i)) {
			double c_m_i = 0;
			if (isScheduled(m) && B != null) {
				int procWhereMisScheduled = -1;
				for (Task2Worker2AFT currTriple : schedule)
					if (currTriple.getTaskId() == m)
						procWhereMisScheduled = currTriple.getWorkerId();
				double B_m_n = B[procWhereMisScheduled][j];
				c_m_i = L[procWhereMisScheduled] + data[m][i] / B_m_n;
			} else
				c_m_i = graph.C_bar[m][i];

			innerMaxArg.add(AFT(m) + c_m_i);
		}
		double innerMaxResult = max(innerMaxArg);
		est = Math.max(avail_j, innerMaxResult);
		return est;
	}

	public double EFT(int i, int j) {
		double est_i_j = EST(i, j);
		return W[i][j] + est_i_j;
	}

	public boolean isScheduled(int i) {
		for (Task2Worker2AFT currTriple : schedule)
			if (currTriple.getTaskId() == i)
				return true;
		return false;
	}

	public double w_i_bar(int i) {
		double currSum = 0;
		for (int j = 0; j < q; j++)
			currSum += W[i][j];
		return currSum / q;
	}

	public double ranku(int i) {
		if (isExitTask(i))
			return w_i_bar(i);

		ArrayList<Double> maxArg = new ArrayList<Double>();
		for (int j : succ(i)) {
			maxArg.add(graph.C_bar[i][j] + ranku(j));
		}

		return w_i_bar(i) + max(maxArg);
	}

	public void computeAllRanks() {
		for (int i = 0; i < v; i++)
			uranks2TaskIds.add(new PairUrank2TaskId(ranku(i), i));
	}

	public void sortURanks() {
		ArrayList<PairUrank2TaskId> tmp = new ArrayList<PairUrank2TaskId>();
		for (int i = 0; i < v; i++)
			tmp.add(uranks2TaskIds.get(i));
		uranks2TaskIds = new ArrayList<PairUrank2TaskId>();
		while (!tmp.isEmpty())
			uranks2TaskIds.add(removeMaxRankPair(tmp));

	}

	public PairUrank2TaskId removeMaxRankPair(ArrayList<PairUrank2TaskId> tmp) {
		double maxRank = Double.NEGATIVE_INFINITY;
		int maxRankPairId = -1;

		for (int i = 0; i < tmp.size(); i++)
			if (tmp.get(i).urank > maxRank) {
				maxRank = tmp.get(i).urank;
				maxRankPairId = i;
			}
		return tmp.remove(maxRankPairId);
	}

	public void printSchedule() {
		DecimalFormat df = new DecimalFormat("#.##");
		System.out.println("ni \tworker \tstart \tfinish");
		for (Task2Worker2AFT currTriple : schedule) {
			System.out.println(currTriple.getTaskId() + "\t" + currTriple.getWorkerId() + "\t"
					+ df.format((currTriple.getAFT() - W[currTriple.getTaskId()][currTriple.getWorkerId()])) + "\t"
					+ df.format(currTriple.getAFT()));
		}
	}

	public void schedule() {
		// Topcuoglu's paper, line 2:
		computeAllRanks();
		
//		System.out.println("uuuuuuuuuuuuranks to task Ids:");
//		for(PairUrank2TaskId currPair : uranks2TaskIds)
//			System.out.println(currPair.taskId + " : " + currPair.urank);
		// line 3:
		sortURanks();

		// lines 4-9
		while (!uranks2TaskIds.isEmpty()) {

			int i = uranks2TaskIds.get(0).taskId;

			double minEFT = Double.POSITIVE_INFINITY;
			int procWithMinEFT = -1;
			for (int k = 0; k < q; k++) {
				double currEFT = EFT(i, k);
				if (currEFT < minEFT) {
					minEFT = currEFT;
					procWithMinEFT = k;
				}
			}
			schedule.add(new Task2Worker2AFT(i, procWithMinEFT, minEFT));
			uranks2TaskIds.remove(0);
		}
	}

}
