package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * This class is used to work with XML.
 * 
 * @author Andrey Kashlev
 *
 */
public class XMLFixer {

	public static HashSet<String> allFiles = new HashSet<String>(); 

	private static void getFiles(File f) {
		File files[];
		if (f.isFile()) {
			allFiles.add(f.getAbsolutePath());
		} else {
			files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				getFiles(files[i]);
			}
		}
	}

	private static void getFiles() {
		File mainFolder = new File("testFiles");
		getFiles(mainFolder);
		HashSet<String> filesToBeRemoved = new HashSet<String>();
		for (String fileName : allFiles) {
			if (fileName.contains("~") || fileName.contains("/mxGraph/")) {
				filesToBeRemoved.add(fileName);
			}
		}
		allFiles.removeAll(filesToBeRemoved);
	}

	private static void fixFirstLine(String file) {

		try {

			File inFile = new File(file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			// Construct the new file that will later be renamed to the original filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			int i = 0;
			while ((line = br.readLine()) != null) {

				if (i == 0) {
					i++;
					pw.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
					pw.flush();
				} else {
					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
//		System.out.println("reached the end");
	}

	public static void fixAllSWLs(){
		allFiles = new HashSet<String>();
		getFiles();
		for (String fileName : allFiles) {
			fixFirstLine(fileName);
		}
	}
	public static void main(String[] args) {
//		getFiles();
//		for (String str : allFiles) {
//			//System.out.println(str);
//		}
		
		//fixFirstLine("testFiles/sampleTestSWLs/unary-construct-based/test.swl");

		fixAllSWLs();
		
	}

}
