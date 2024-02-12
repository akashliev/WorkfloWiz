package scheduler2;

import java.util.ArrayList;

public class TaskGraph {
	private int size;
	public double[][] C_bar;

	public TaskGraph(int newSize) {
		size = newSize;
		C_bar = new double[size][size];

		for (int i = 0; i < size; i++)
			C_bar[i] = new double[size];

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				C_bar[i][j] = -1;
	}

	public int getSize() {
		return size;
	}

	public void addEdge(int i, int k, double c_i_k) {
		C_bar[i][k] = c_i_k;
	}

	public double get_c_i_k(int i, int k) {
		return C_bar[i][k];
	}

	public ArrayList<Integer> getSuccessors(int i) {
		ArrayList<Integer> succ = new ArrayList<Integer>();
		for (int j = 0; j < size; j++)
			if (C_bar[i][j] != -1)
				succ.add(j);
		return succ;
	}

	public ArrayList<Integer> getPredecessors(int j) {
		ArrayList<Integer> pred = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
			if (C_bar[i][j] != -1)
				pred.add(i);
		return pred;
	}

	public int getEntryTask() {
		for (int i = 0; i < size; i++)
			if (getPredecessors(i).size() == 0)
				return i;
		return -1;
	}

	public boolean isEntryTask(int i) {
		return getPredecessors(i).size() == 0;
	}

	public int getExitTask() {
		for (int i = 0; i < size; i++)
			if (getSuccessors(i).size() == 0)
				return i;
		return -1;
	}

	public boolean isExitTask(int i) {
		return getSuccessors(i).size() == 0;
	}

}
