package scheduler2;

public class Task2Worker2AFT {
	private int taskId;
	private int workerId;
	private double AFT;

	public Task2Worker2AFT(int newTaskId, int newWorkerId, double newAFT) {
		taskId = newTaskId;
		workerId = newWorkerId;
		AFT = newAFT;
	}

	public int getTaskId() {
		return taskId;
	}

	public int getWorkerId() {
		return workerId;
	}

	public double getAFT() {
		return AFT;
	}

}
