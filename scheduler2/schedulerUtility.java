package scheduler2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class schedulerUtility {

	public static List<List<Double>> readFileIntoTwoDimList(String pathToFile) throws FileNotFoundException {
		// pathToFile example: "src/scheduler/array2.txt"
		List<List<Double>> a = new ArrayList<List<Double>>();

		Scanner input = new Scanner(new File(pathToFile));
		while (input.hasNextLine()) {
			Scanner colReader = new Scanner(input.nextLine());
			ArrayList col = new ArrayList();
			while (colReader.hasNextDouble()) {
				col.add(colReader.nextDouble());
			}
			a.add(col);
		}
		return a;
	}

	public static double[][] readFileInto2DArray(String pathToFile) throws Exception {
		List<List<Double>> twoDimmList = readFileIntoTwoDimList(pathToFile);
//		System.out.println("two dim list: " + twoDimmList.size() + " x " + twoDimmList.get(0).size());
		double[][] twoDimmArray = new double[twoDimmList.size()][twoDimmList.get(0).size()];
		for (int i = 0; i < twoDimmList.size(); i++)
			for (int j = 0; j < twoDimmList.get(0).size(); j++)
				twoDimmArray[i][j] = twoDimmList.get(i).get(j);
		return twoDimmArray;
	}

	public static void print2DArray(double[][] arr) {
		DecimalFormat df = new DecimalFormat("#.##");
		
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[0].length; j++)
				System.out.print(df.format(arr[i][j]) + "\t");
			System.out.println();
		}
	}

	public static void main(String[] args) throws Exception {
		List<List<Double>> twoDimmList = readFileIntoTwoDimList("src/scheduler2/W");

		double[][] twoDimmArray = new double[twoDimmList.size()][twoDimmList.get(0).size()];
		for (int i = 0; i < twoDimmList.size(); i++)
			for (int j = 0; j < twoDimmList.get(0).size(); j++)
				twoDimmArray[i][j] = twoDimmList.get(i).get(j);

		for (int i = 0; i < twoDimmList.size(); i++) {
			for (int j = 0; j < twoDimmList.get(0).size(); j++)
				System.out.print(twoDimmArray[i][j] + "\t");
			System.out.println();
		}

	}
}
