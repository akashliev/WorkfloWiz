package utility;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CPUintensity extends Thread {
	private String myName;
	int n = 0;

	public CPUintensity(String name, int theN) {
		myName = name;
		n = theN;
	}

	public void run() {
		int status = 1, num = 3;

		for (int count = 2; count <= n;) {
			for (int j = 2; j <= Math.sqrt(num); j++) {
				if (num % j == 0) {
					status = 0;
					break;
				}
			}
			if (status != 0) {
				// System.out.println(num);
				count++;
			}
			status = 1;
			num++;
		}

	}

	private static int getN() {
		int theN = 0;
		File f = new File("/home/view/cpuIntensity");
		if (f.exists()) {
			try {
				theN = new Integer(Utility.readFileAsString("/home/view/cpuIntensity").trim());
				Utility.writeToFile("running CPU intensive task, n = " + theN, "/home/view/log");
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			theN = 400000;
		return theN;
	}

	public static void runCPUIntensiveTask() throws InterruptedException {

		List threads = new ArrayList();
		int nFromFile = getN();
		for (int j = 0; j < 10; j++) {
			CPUintensity thread = new CPUintensity(new String().valueOf(j), nFromFile);
			thread.start();
			threads.add(thread);
		}
		for (int i = 0; i < threads.size(); i++)
			((Thread) threads.get(i)).join();
	}

	public static void main(String[] args) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		List threads = new ArrayList();
		int nFromFile = getN();
		for (int j = 0; j < 10; j++) {
			CPUintensity thread = new CPUintensity(new String().valueOf(j), nFromFile);
			thread.start();
			threads.add(thread);
		}
		for (int i = 0; i < threads.size(); i++)
			((Thread) threads.get(i)).join();
		System.out.println("endddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
		long endTime = System.currentTimeMillis();
		System.out.println("task took " + (endTime - startTime) / 1000 + " s");
	}
}
