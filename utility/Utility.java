package utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import toyDPM.DataProductManager;
import translator.Pair;
import translator.SWLAnalyzer;
import webbench.WebbenchUtility;

import com.mysql.jdbc.PreparedStatement;

import crm2.CRM;
import crm2.VMProvisioner;
import dataProduct.DataProduct;
import dataProduct.FileDP;
import dpManagement.DPLTranslator;

/**
 * This class provides a number of static methods used by various classes in workflow engine. It hides low-level technical
 * details, such as JDBC connection to databases, converting org.w3c.dom objects to strings, etc.
 * 
 * @author Andrey Kashlev
 * 
 */
public class Utility {
	// all constants used in Engine:
	public static String swlSchemaURL = "";
	public static String experimentSchemaURL = "";
	public static String pathToVIEW = "";
	public static String pathToLog = "";
	public static String pathToConfigFile = "";
	public static String pathToFileDPsFolder = "";
	public static String passwdToVMs = "";
	public static final String extensionForSWLs = ".swl";

	public static String dbURL = "";
	public static String engineRepoDBname = "";
	public static final String driver = "com.mysql.jdbc.Driver";
	public static String login = "";
	public static String password = "";
	public static String tableNameForSWLSpecs = "";
	public static String dbNameForRelationalDPs = "relationaldps";

	// data product management-related constants:
	public static String dplSchemaURL = "";
	public static String dplSpecsTableName = "";

	public static LoggingLevels loggingLevel = LoggingLevels.onlySystemOutput;
	public static boolean executeInCloud = false;
	public static String myIP = null;
	public static HashMap<String, String> runID_to_wfName;
	public static boolean disseminateDPs = true;
	public static boolean disseminateSpecs = true;

	private static Connection conn = null;

	public static String readFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	public static Document readFileAsDocument(String filePath) throws Exception {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return XMLParser.getDocument(fileData.toString());
	}

	public static void storeSpecInDB(String wfName, Element spec, Element diagram, boolean isCorrect, boolean isExperiment) {
		String xmlString = nodeToString(spec);
		String diagramString = "";
		if (diagram != null)
			diagramString = nodeToString(diagram);
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);

			Statement st = conn.createStatement();
			String q = "INSERT INTO " + tableNameForSWLSpecs + " VALUES (\'" + wfName + "\', \'" + xmlString + "\', \'" + diagramString + "\', "
					+ isCorrect + ", " + isExperiment + ")";
			// System.out.println("Query: " + q);
			// System.out.println("-----------------------------------");
			st.executeUpdate(q);
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void storeDPLSpecInDB(String dataName, Document dplSpec, String description) {
		String xmlString = nodeToString(dplSpec);
		xmlString = removeEscapeSequences(xmlString);
		String type = DPLTranslator.getDPType(dplSpec);
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);

			Statement st = conn.createStatement();
			String q = "INSERT INTO " + dplSpecsTableName + " VALUES (\'" + dataName + "\', \'" + xmlString + "\', \'" + type + "\', \'"
					+ description + "\')";
			// System.out.println("Query: " + q);
			st.executeUpdate(q);
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String removeEscapeSequences(String input) {
		String result = input.replaceAll("&lt;", "<");
		result = result.replaceAll("&gt;", ">");

		return result;
	}

	public static void updateSpecInDB(String wfName, Element spec, Element diagram, boolean isCorrect, boolean isExperiment) {
		String specString = nodeToString(spec);
		String diagramString = nodeToString(diagram);
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);

			Statement st = conn.createStatement();
			String q = "UPDATE specs SET spec=\'" + specString + "\', diagram=\'" + diagramString + "\', isCorrect=" + isCorrect
					+ ", isExperiment=" + isExperiment + " WHERE name=\'" + wfName + "\'";
			// System.out.println("update Query: " + q);
			st.executeUpdate(q);
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean deleteWorkflowFromDatabase(String wfName) {
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);

			Statement st = conn.createStatement();
			String q = "DELETE FROM " + tableNameForSWLSpecs + " WHERE name=\'" + wfName + "\'";
			st.executeUpdate(q);
			conn.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean deleteDPFromDatabase(String dataName) {
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);

			Statement st = conn.createStatement();
			String q = "DELETE FROM " + dplSpecsTableName + " WHERE name=\'" + dataName + "\'";
			st.executeUpdate(q);
			conn.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean deleteTable(String dbName, String tableName) {
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbName + "?characterEncoding=utf8", login, password);

			Statement st = conn.createStatement();
			String q = "DROP table " + tableName;
			st.executeUpdate(q);
			conn.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static HashSet<String> getAllWorkflowNamesFromDB() throws Exception {
		HashSet<String> allwfNames = new HashSet<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT name FROM " + tableNameForSWLSpecs;
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				allwfNames.add(rs.getString("name"));
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return allwfNames;
	}

	public static boolean workflowIsInTheDatabase(String wfName) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT * FROM " + tableNameForSWLSpecs + " WHERE name=\'" + wfName + "\'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				return true;
			}
			// appendToLog("no such workflow in the repository: " + wfName);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return false;
	}

	public static void appendToLog(String record) {
		if (loggingLevel.equals(LoggingLevels.none))
			return;
		if (loggingLevel.equals(LoggingLevels.detailed)) {
			// writing to log file (disabled to improve performance):
			/*
			 * BufferedWriter bw = null; try { String prefix = System.getenv("CATALINA_HOME"); prefix = prefix + "/webapps/VIEW/";
			 * String fullPathToLog = prefix + pathToLog; bw = new BufferedWriter(new FileWriter(fullPathToLog, true));
			 * bw.write(record); bw.newLine(); bw.flush();
			 * 
			 * } catch (IOException ioe) { ioe.printStackTrace(); } finally { if (bw != null) try { bw.close(); } catch
			 * (IOException ioe2) { } }
			 */
			System.out.println(record);
		}
		if (loggingLevel.equals(LoggingLevels.onlySystemOutput))
			System.out.println(record);
	}

	public static String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static String domToString(Document dom) {
		String domString = "";
		DOMImplementationLS domImplLS = (DOMImplementationLS) dom.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		domString = serializer.writeToString(dom.getDocumentElement());
		return domString;
	}

	public static String nodeToString(Node node) {
		String resultStr = "";
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(node);
			trans.transform(source, result);
			resultStr = sw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return resultStr;
	}

	public static Document getDocument(Element element) {
		Document doc = null;
		while ((element.getParentNode() instanceof Element))
			element = (Element) element.getParentNode();
		doc = (Document) element.getParentNode();
		return doc;
	}

	public static HashSet<String> getAllBuiltInClasses() throws ClassNotFoundException, IOException {
		HashSet<String> builtinClasses = new HashSet<String>();
		// Reflections reflections = new Reflections("org.your.package");
		// Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
		List myList = getClasses("builtin");
		ListIterator li = myList.listIterator();
		while (li.hasNext()) {
			Class cl = (Class) li.next();
			// System.out.println("current class: " + cl.getName().replaceFirst("builtin.", ""));
			builtinClasses.add(cl.getName().replaceFirst("builtin.", ""));
		}
		return builtinClasses;
	}

	private static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			String fileName = resource.getFile();
			String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
			dirs.add(new File(fileNameDecoded));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (file.isDirectory()) {
				assert !fileName.contains(".");
				classes.addAll(findClasses(file, packageName + "." + fileName));
			} else if (fileName.endsWith(".class") && !fileName.contains("$")) {
				Class _class;
				try {
					_class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
				} catch (ExceptionInInitializerError e) {
					// happen, for example, in classes, which depend on
					// Spring to inject some beans, and which fail,
					// if dependency is not fulfilled
					_class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6), false, Thread.currentThread()
							.getContextClassLoader());
				}
				classes.add(_class);
			}
		}
		return classes;
	}

	public static String getWorkflowFromDB(String wfName) throws Exception {
		String result = "";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT spec FROM " + tableNameForSWLSpecs + " WHERE name=\'" + wfName + "\'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				return rs.getString("spec");
			}
			// appendToLog("no such workflow in the repository: " + wfName);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return result;
	}

	public static String getDPLFromDB(String dataName) throws Exception {

		String result = "";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT dpl FROM " + dplSpecsTableName + " WHERE name=\'" + dataName + "\'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				return rs.getString("dpl");
			}
			// appendToLog("no such workflow in the repository: " + wfName);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return result;
	}

	public static String getDiagramFromDB(String wfName) throws Exception {
		String result = "";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT diagram FROM " + tableNameForSWLSpecs + " WHERE name=\'" + wfName + "\'";
//			 System.out.println("query: " + query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				return rs.getString("diagram");
			}
			// appendToLog("no such workflow in the repository: " + wfName);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return result;
	}

	public static JSONObject getWorkflowMetadata(String wfName) throws SQLException {
		JSONObject currMetadata = null;

		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT * FROM " + tableNameForSWLSpecs + " WHERE name=\'" + wfName + "\'";
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				currMetadata = new JSONObject();

				String specStr = rs.getString("spec");
				Document spec = null;
				try {
					spec = XMLParser.getDocument(specStr);
				} catch (Exception e) {
				}

				currMetadata.put("name", rs.getString("name"));

				if (rs.getString("diagram") != "")
					currMetadata.put("hasDiagram", true);
				else
					currMetadata.put("hasDiagram", false);
				currMetadata.put("isCorrect", rs.getString("isCorrect"));
				currMetadata.put("isExperiment", rs.getString("isExperiment"));
				if (rs.getString("isExperiment").contains("0"))
					currMetadata.put("interface", SWLAnalyzer.getPortsMetadata(spec.getDocumentElement()));
				// metadata.put(currMetadata);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return currMetadata;
	}

	public static void removeAll(Node node, short nodeType, String name) {
		if (node.getNodeType() == nodeType && (name == null || node.getNodeName().equals(name))) {
			node.getParentNode().removeChild(node);
		} else {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				removeAll(list.item(i), nodeType, name);
			}
		}
	}

	public static JSONArray getWorkflowsMetadataFromDB() throws SQLException {
//		System.out.println("111");
		JSONArray metadata = new JSONArray();

		Statement stmt = null;
		
		ResultSet rs = null;
		try {
//			System.out.println("222");
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		System.out.println("333");
		try {
//			System.out.println("trying to create conn: |" + dbURL + engineRepoDBname + "|" + login + "|" + password + "|");
//			url="jdbc:mysql://localhost:3306/dbname?characterEncoding=utf8"
//			     jdbc:mysql://localhost:3306/enginerepo1d0|engine|engine|
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
//			System.out.println("conn: |"  + conn + "|");
			stmt = conn.createStatement();
			String query = "SELECT * FROM " + tableNameForSWLSpecs;
//			System.out.println("about to execute query");
			rs = stmt.executeQuery(query);
//			System.out.println("rs is " + rs.toString());
			JSONObject currMetadata = null;
			while (rs.next()) {
//				System.out.println("next rs");
//				System.out.println("1'th string:");
//				System.out.println(rs.getString(1));
//				System.out.println("name");
//				System.out.println(rs.getString("name"));
				currMetadata = new JSONObject();

				String specStr = rs.getString("spec");
				Document spec = null;
				boolean isWebService = false;
				try {
					 System.out.println(rs.getString("name"));
					spec = XMLParser.getDocument(specStr);
					if (spec != null)
						isWebService = SWLAnalyzer.isWebservice(spec.getDocumentElement());
				} catch (Exception e) {
				}

				if (!isWebService) {
					// only add it to JSON array if it's not a web service
					currMetadata.put("name", rs.getString("name"));

					if (rs.getString("diagram") != "")
						currMetadata.put("hasDiagram", true);
					else
						currMetadata.put("hasDiagram", false);
					currMetadata.put("isCorrect", rs.getString("isCorrect"));
					currMetadata.put("isExperiment", rs.getString("isExperiment"));
					if (rs.getString("isExperiment").contains("0"))
						currMetadata.put("interface", SWLAnalyzer.getPortsMetadata(spec.getDocumentElement()));
					metadata.put(currMetadata);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return metadata;
	}

	public static JSONArray getDPsMetadata() throws Exception {
		JSONArray metadata = new JSONArray();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT name, type, description FROM " + dplSpecsTableName;
			rs = stmt.executeQuery(query);
			JSONObject currMetadata = null;
			while (rs.next()) {
				currMetadata = new JSONObject();

				currMetadata.put("dataName", rs.getString("name"));

				currMetadata.put("dataType", rs.getString("type"));
				currMetadata.put("dataDescription", rs.getString("description"));
				metadata.put(currMetadata);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		getFilesMetadata(metadata);
		return metadata;
	}

	public static void getFilesMetadata(JSONArray metadata) throws Exception {
		String path = Utility.pathToFileDPsFolder;
		String fileName;
		File folder = new File(path);

		if (folder == null)
			return;

		if (folder.listFiles() == null)
			return;

		File[] listOfFiles = folder.listFiles();
		JSONObject currMetadata = null;
		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {
				fileName = listOfFiles[i].getName();

				currMetadata = new JSONObject();
				currMetadata.put("dataName", fileName);
				currMetadata.put("dataType", "File");
				currMetadata.put("dataDescription", fileName);
				metadata.put(currMetadata);

				// if (files.endsWith(".txt") || files.endsWith(".TXT")) {
				// System.out.println(files);
				// }
			}

		}
	}

	public static boolean fullPathToFileIsCorrect(String fullPathToAFile) {
		String dirPath = fullPathToAFile.substring(0, fullPathToAFile.lastIndexOf("/"));
		String fileName = fullPathToAFile.substring(fullPathToAFile.lastIndexOf("/") + 1);

		// System.out.println(dirPath);
		// System.out.println(fileName);

		File folder = new File(dirPath);
		if (folder == null || folder.listFiles() == null)
			return false;
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile())
				if (listOfFiles[i].getName().trim().equals(fileName))
					return true;
		}

		return false;
	}

	public static boolean dirExists(String dirName) {
		String path = Utility.pathToFileDPsFolder;

		File folder = new File(path);
		if (folder == null || folder.listFiles() == null)
			return false;
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isDirectory())
				return true;
		}
		return false;
	}

	public static boolean isFile(String dataName) {

		String path = Utility.pathToFileDPsFolder;

		File folder = new File(path);
		if (folder == null || folder.listFiles() == null)
			return false;
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile())
				if (listOfFiles[i].getName().trim().equals(dataName) || listOfFiles[i].getName().trim().equals(dataName + ".fits")
						|| listOfFiles[i].getName().trim().equals(dataName + "_area.fits"))
					return true;
		}

		File f = new File(pathToFileDPsFolder.substring(0, pathToFileDPsFolder.length() - 1));
		if (f.exists()) {
			// path = "/home/view/FileDPs/";
			path = pathToFileDPsFolder.substring(0, pathToFileDPsFolder.length() - 1);

			File folder2 = new File(path);
			if (folder2 == null || folder2.listFiles() == null)
				return false;
			File[] listOfFiles2 = folder2.listFiles();

			for (int i = 0; i < listOfFiles2.length; i++) {

				if (listOfFiles2[i].isFile())
					if (listOfFiles2[i].getName().trim().equals(dataName) || listOfFiles2[i].getName().trim().equals(dataName + ".fits")
							|| listOfFiles2[i].getName().trim().equals(dataName + "_area.fits"))
						return true;
			}
		}

		f = new File("/mnt/mntEBSDevice/FileDPs");
		if (f.exists()) {
			path = "/mnt/mntEBSDevice/FileDPs/";

			File folder2 = new File(path);
			if (folder2 == null || folder2.listFiles() == null)
				return false;
			File[] listOfFiles2 = folder2.listFiles();

			for (int i = 0; i < listOfFiles2.length; i++) {

				if (listOfFiles2[i].isFile())
					if (listOfFiles2[i].getName().trim().equals(dataName))
						return true;
			}
		}
		return false;
	}

	public static boolean fileExists(String fileName) {
		if (fileName.contains("/"))
			return fullPathToFileIsCorrect(fileName);
		return isFile(fileName);
	}

	public static JSONArray getWSsMetadataFromDB() throws SQLException {
		JSONArray metadata = new JSONArray();

		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT * FROM " + tableNameForSWLSpecs;
			rs = stmt.executeQuery(query);
			JSONObject currMetadata = null;
			while (rs.next()) {
				currMetadata = new JSONObject();

				String specStr = rs.getString("spec");
				Document spec = null;
				boolean isWebService = false;
				try {
					spec = XMLParser.getDocument(specStr);
					if (spec != null)
						isWebService = SWLAnalyzer.isWebservice(spec.getDocumentElement());
				} catch (Exception e) {
				}

				if (isWebService) {
					// only add it to JSON array if it's not a web service
					currMetadata.put("name", SWLAnalyzer.getServiceName(spec.getDocumentElement()));

					currMetadata.put("hasDiagram", false);
					currMetadata.put("isCorrect", 1);
					currMetadata.put("isExperiment", "0");
					currMetadata.put("wsOpName", SWLAnalyzer.getWSOperationName(spec.getDocumentElement()));
					currMetadata.put("interface", SWLAnalyzer.getPortsMetadata(spec.getDocumentElement()));

					metadata.put(currMetadata);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return metadata;

	}

	public static JSONObject getDPMetadata(String dataName) throws SQLException {
		JSONObject currMetadata = null;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String query = "SELECT name, type, description FROM " + dplSpecsTableName + " WHERE name=\'" + dataName + "\'";
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				currMetadata = new JSONObject();

				currMetadata.put("dataName", rs.getString("name"));

				currMetadata.put("dataType", rs.getString("type"));
				currMetadata.put("dataDescription", rs.getString("description"));
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return currMetadata;
	}

	public static JSONArray getDPMetadata(ArrayList<String> dataNames) throws Exception {
		JSONArray dpsMetadata = new JSONArray();
		JSONObject currMetadata = null;
		// hack:
		if (dataNames.get(0).equals("grade")) {
			currMetadata = new JSONObject();

			currMetadata.put("dataName", "grade");

			currMetadata.put("dataType", "File");
			String grade = readFromAFile(pathToFileDPsFolder + "grade");
			grade = grade.substring(0, 4);
			currMetadata.put("dataDescription", grade);
			dpsMetadata.put(currMetadata);
		}

		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		try {
			conn = DriverManager.getConnection(dbURL + engineRepoDBname + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			String whereCondition = "";
			for (String dataName : dataNames)
				whereCondition += " OR name=\'" + dataName + "\'";
			whereCondition = whereCondition.replaceFirst(" OR", "");

			String query = "SELECT name, type, description FROM " + dplSpecsTableName + " WHERE " + whereCondition;
			// System.out.println("query for multiple::::\n" + query);
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				currMetadata = new JSONObject();

				currMetadata.put("dataName", rs.getString("name"));

				currMetadata.put("dataType", rs.getString("type"));
				if (rs.getString("type").trim().equals("Double") || rs.getString("type").trim().equals("Float"))
					currMetadata.put("dataDescription", rs.getString("description").substring(0, 4) + "...");
				else
					currMetadata.put("dataDescription", rs.getString("description"));
				dpsMetadata.put(currMetadata);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
			rs.close();
		}
		return dpsMetadata;
	}

	public static DataProduct getDataProduct(String dataName) throws Exception {
		// System.out.println("calling Utility.getDataProduct(): " + dataName);
		String dplSpecStr = "";
		if (Utility.isFile(dataName)) {
			FileDP fileDP = new FileDP(dataName);
			return fileDP;
		}
		if (WebbenchUtility.useLocalDPM)
			dplSpecStr = getDPLFromDB(dataName);
		if (dplSpecStr.trim().equals(""))
			return null;
		DataProduct dp = DPLTranslator.translateDPLtoDP(XMLParser.getDocument(dplSpecStr));
		return dp;
	}

	public static void serialize(Document doc, OutputStream out) throws Exception {

		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer serializer;

		try {
			serializer = tfactory.newTransformer();
			// Setup indenting to "pretty print"
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			serializer.transform(new DOMSource(doc), new StreamResult(out));
		} catch (TransformerException e) {
			// this is fatal, just dump the stack and throw a runtime exception
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	public static Document buildDocumentFromSingleNode(Node node) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);

		DocumentBuilder builder = factory.newDocumentBuilder();

		Document newDocument = builder.newDocument();
		Node newNode = newDocument.importNode(node, true);
		newDocument.appendChild(newNode);
		return newDocument;
	}

	public static String formatXML(Document document) throws Exception {
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};

		serialize(document, output);

		String result = output.toString();
		return result;

	}

	public static String formatXML(Node node) throws Exception {
		return formatXML(buildDocumentFromSingleNode(node));
	}

	public static String translateInUserFriendlyLanguage(String msg) {
		String result = msg;
		// System.out.println("cvc-complex-type.2.4.b: The content of element 'inputPorts' is not complete. One of '{inputPort}' is expected.");
		if (msg.trim().contains("cvc-complex-type.2.4.b: The content of element 'inputPorts' is not complete. One of '{inputPort}' is expected."))
			return msg.replaceAll("cvc-complex-type.2.4.b: The content of element 'inputPorts' is not complete. One of '{inputPort}' is expected.",
					"no input ports found in the workflow");
		return result;
	}

	public static String getMeaningfulStatusMessage(String statusMsg) {
		String result = statusMsg;
		if (statusMsg.contains("ERROR:")) {
			String[] chunks = statusMsg.split("ERROR:");
			for (int i = 0; i < chunks.length; i++)
				translateInUserFriendlyLanguage(chunks[i]);
		}
		return result;
	}

	public static String readURLIntoString(String url) throws Exception {
		String result = "";
		URL oracle = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null)
			result += inputLine;
		// System.out.println(inputLine);
		in.close();
		return result;
	}

	public static String getDate() {
		String result = "";
		Calendar calender = Calendar.getInstance();

		result = result + calender.get(Calendar.YEAR);
		result = result + "-" + (calender.get(Calendar.MONTH) + 1);
		result = result + "-" + calender.get(Calendar.DAY_OF_MONTH);
		result = result + "-" + calender.get(Calendar.HOUR);
		result = result + "-" + calender.get(Calendar.MINUTE);
		result = result + "-" + calender.get(Calendar.SECOND);

		return result;
	}

	public static void registerDataProduct(DataProduct dp) throws Exception {
		if (WebbenchUtility.useLocalDPM)
			DataProductManager.registerDataProduct(dp);
	}

	public static void executeSelectIntoStatement(String dbName, String tableName, String whereClause, String resultTableName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		ArrayList<Pair> schema = getTableSchema(dbName, tableName);

		for (Pair colName2ColType : schema) {
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}
		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String selectQuery = "INSERT INTO " + resultTableName + " SELECT * FROM " + tableName + " WHERE " + whereClause;
		System.out.println("sel: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static void executeNaturalJoin(String dbName, String tableName1, String tableName2, String resultTableName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		ArrayList<Pair> schemaFromTable1 = getTableSchema(dbName, tableName1);

		for (Pair colName2ColType : schemaFromTable1) {
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}

		ArrayList<Pair> schemaFromTable2 = getTableSchema(dbName, tableName2);

		boolean atLeastOneColumnIsCommon = false;
		for (Pair colName2ColType : schemaFromTable2) {

			boolean thisColumnIsCommon = false;
			for (Pair pairFromSchema1 : schemaFromTable1) {
				if (!thisColumnIsCommon && pairFromSchema1.left.trim().equals(colName2ColType.left.trim())
						&& pairFromSchema1.right.trim().equals(colName2ColType.right.trim())) {
					thisColumnIsCommon = true;
					atLeastOneColumnIsCommon = true;
				}
			}

			if (!thisColumnIsCommon)
				createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}

		if (!atLeastOneColumnIsCommon) {
			System.out.println("ERROR: cannot perform natural join on relations with no common attributes: " + tableName1 + " and " + tableName2);
			return;
		}

		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String selectQuery = "INSERT INTO " + resultTableName + " SELECT * FROM " + tableName1 + " NATURAL JOIN " + tableName2;
		System.out.println("sel: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static void executeUnion(String dbName, String tableName1, String tableName2, String resultTableName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		ArrayList<Pair> schemaFromTable1 = getTableSchema(dbName, tableName1);
		ArrayList<Pair> schemaFromTable2 = getTableSchema(dbName, tableName2);

		boolean schemasAreUnionCompatible = true;
		if (schemaFromTable1.size() != schemaFromTable2.size())
			schemasAreUnionCompatible = false;

		for (Pair colName2ColType1 : schemaFromTable1) {
			boolean thisColumnHasCounterPartInSchema2 = false;
			for (Pair colName2ColType2 : schemaFromTable2) {
				if (colName2ColType2.left.trim().equals(colName2ColType1.left.trim())) {
					thisColumnHasCounterPartInSchema2 = true;
					if (!colName2ColType2.right.trim().equals(colName2ColType1.right.trim()))
						schemasAreUnionCompatible = false;
				}
			}
			if (!thisColumnHasCounterPartInSchema2)
				schemasAreUnionCompatible = false;
		}

		if (!schemasAreUnionCompatible) {
			System.out.println("ERROR: cannot perform union: schemas are not union-compatible " + tableName1 + " " + tableName2);
			return;
		}

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		for (Pair colName2ColType : schemaFromTable1) {
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}
		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String selectQuery = "INSERT INTO " + resultTableName + " SELECT * FROM " + tableName1 + " UNION SELECT * FROM " + tableName2;
		System.out.println("sel: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static void executeIntersect(String dbName, String tableName1, String tableName2, String resultTableName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		ArrayList<Pair> schemaFromTable1 = getTableSchema(dbName, tableName1);
		ArrayList<Pair> schemaFromTable2 = getTableSchema(dbName, tableName2);

		boolean schemasAreUnionCompatible = true;
		if (schemaFromTable1.size() != schemaFromTable2.size())
			schemasAreUnionCompatible = false;

		for (Pair colName2ColType1 : schemaFromTable1) {
			boolean thisColumnHasCounterPartInSchema2 = false;
			for (Pair colName2ColType2 : schemaFromTable2) {
				if (colName2ColType2.left.trim().equals(colName2ColType1.left.trim())) {
					thisColumnHasCounterPartInSchema2 = true;
					if (!colName2ColType2.right.trim().equals(colName2ColType1.right.trim()))
						schemasAreUnionCompatible = false;
				}
			}
			if (!thisColumnHasCounterPartInSchema2)
				schemasAreUnionCompatible = false;
		}

		if (!schemasAreUnionCompatible) {
			System.out.println("ERROR: cannot perform intersect: schemas are not union-compatible " + tableName1 + " " + tableName2);
			return;
		}
		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		for (Pair colName2ColType : schemaFromTable1) {
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}
		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String intersectQuery = "SELECT * FROM (SELECT * FROM " + tableName1 + " UNION ALL SELECT * FROM " + tableName2;
		intersectQuery = intersectQuery + ") as tmp GROUP BY ";
		for (Pair colName2ColType : schemaFromTable1) {
			intersectQuery = intersectQuery + "tmp." + colName2ColType.left + ", ";
		}
		intersectQuery = intersectQuery + "end";
		intersectQuery = intersectQuery.replaceAll(", end", "");
		intersectQuery = intersectQuery + " HAVING COUNT(*)>1";
		String selectQuery = "INSERT INTO " + resultTableName + " " + intersectQuery;
		System.out.println("sel: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static void executeSetDiff(String dbName, String tableName1, String tableName2, String resultTableName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		ArrayList<Pair> schemaFromTable1 = getTableSchema(dbName, tableName1);
		ArrayList<Pair> schemaFromTable2 = getTableSchema(dbName, tableName2);

		boolean schemasAreUnionCompatible = true;
		if (schemaFromTable1.size() != schemaFromTable2.size())
			schemasAreUnionCompatible = false;

		for (Pair colName2ColType1 : schemaFromTable1) {
			boolean thisColumnHasCounterPartInSchema2 = false;
			for (Pair colName2ColType2 : schemaFromTable2) {
				if (colName2ColType2.left.trim().equals(colName2ColType1.left.trim())) {
					thisColumnHasCounterPartInSchema2 = true;
					if (!colName2ColType2.right.trim().equals(colName2ColType1.right.trim()))
						schemasAreUnionCompatible = false;
				}
			}
			if (!thisColumnHasCounterPartInSchema2)
				schemasAreUnionCompatible = false;
		}

		if (!schemasAreUnionCompatible) {
			System.out.println("ERROR: cannot perform intersect: schemas are not union-compatible " + tableName1 + " " + tableName2);
			return;
		}
		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		for (Pair colName2ColType : schemaFromTable1) {
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}
		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String setDiffQuery = "SELECT DISTINCT * FROM " + tableName1 + " WHERE(";
		for (Pair colName2ColType : schemaFromTable1) {
			setDiffQuery = setDiffQuery + colName2ColType.left + ", ";
		}
		setDiffQuery = setDiffQuery + "end";
		setDiffQuery = setDiffQuery.replaceAll(", end", "");
		setDiffQuery = setDiffQuery + ") NOT IN (SELECT * FROM " + tableName2 + ")";
		String selectQuery = "INSERT INTO " + resultTableName + " " + setDiffQuery;
		System.out.println("sel: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static void executeCartProduct(String dbName, String tableName1, String tableName2, String resultTableName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		ArrayList<Pair> schemaFromTable1 = getTableSchema(dbName, tableName1);
		ArrayList<Pair> schemaFromTable2 = getTableSchema(dbName, tableName2);

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		for (Pair colName2ColType : schemaFromTable1)
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";

		for (Pair colName2ColType2 : schemaFromTable2) {
			boolean thisColumnIsCommon = false;
			for (Pair colName2ColType1 : schemaFromTable1) {
				if (colName2ColType1.left.trim().equals(colName2ColType2.left.trim()))
					thisColumnIsCommon = true;
			}
			if (thisColumnIsCommon)
				createTableQuery = createTableQuery + colName2ColType2.left + "Prime " + colName2ColType2.right + ", ";
			else
				createTableQuery = createTableQuery + colName2ColType2.left + " " + colName2ColType2.right + ", ";
		}

		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String selectQuery = "INSERT INTO " + resultTableName + " SELECT * FROM " + tableName1 + " JOIN " + tableName2;
		System.out.println("sel: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static ArrayList<Pair> getTableSchema(String dbName, String tableName) throws Exception {
		ArrayList<Pair> schema = new ArrayList<Pair>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbName + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE false");
			ResultSetMetaData rsm = rs.getMetaData();

			for (int i = 1; i <= rsm.getColumnCount(); i++) {
				String type = rsm.getColumnTypeName(i).trim();
				if (rsm.getColumnTypeName(i).trim().equals("VARCHAR"))
					type = "TEXT";
				else if (rsm.getColumnTypeName(i).trim().equals("TINYINT"))
					type = "TINYINT(1)";

				schema.add(new Pair(rsm.getColumnLabel(i).trim(), type));
			}
			return schema;

		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
		return null;
	}

	public static void executeSelectStatementWithColumnList(String dbName, String tableName, String listOfColumns, String resultTableName)
			throws Exception {

		ArrayList<String> targetColumnList = new ArrayList<String>();
		String[] columnsFromInputString = listOfColumns.split(",");
		for (String col : columnsFromInputString)
			targetColumnList.add(col.trim());

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		ArrayList<Pair> schema = getTableSchema(dbName, tableName);

		ArrayList<Pair> schemaForResultTable = new ArrayList<Pair>();

		for (Pair columnNameToType : schema) {
			if (targetColumnList.contains(columnNameToType.left.trim()))
				schemaForResultTable.add(columnNameToType);
		}

		if (schemaForResultTable.size() == 0)
			schemaForResultTable = schema;

		for (Pair colName2ColType : schemaForResultTable) {
			createTableQuery = createTableQuery + colName2ColType.left + " " + colName2ColType.right + ", ";
		}
		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String selectQuery = "INSERT INTO " + resultTableName + " (" + listOfColumns + ") " + " SELECT " + listOfColumns + " FROM " + tableName;
		System.out.println("insert: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	public static void executeRename(String dbName, String tableName, String listOfColumns, String resultTableName) throws Exception {

		ArrayList<String> targetColumnList = new ArrayList<String>();
		String[] columnsFromInputString = listOfColumns.split(",");
		for (String col : columnsFromInputString)
			targetColumnList.add(col.trim());

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(";

		ArrayList<Pair> schema = getTableSchema(dbName, tableName);

		if (schema.size() != targetColumnList.size()) {
			System.out.println("ERROR: cannot rename attributes - their number differes from the number of attributes in the actual relation");
			return;
		}

		int i = 0;
		for (Pair colName2ColType : schema) {
			createTableQuery = createTableQuery + targetColumnList.get(i) + " " + colName2ColType.right + ", ";
			i++;
		}
		createTableQuery = createTableQuery.trim() + "end";
		createTableQuery = createTableQuery.replace(",end", "");
		createTableQuery = createTableQuery + ")";
		System.out.println("create query: " + createTableQuery);

		String colXAsYPart = "";
		int j = 0;
		for (Pair colName2ColType : schema) {
			colXAsYPart = colXAsYPart + colName2ColType.left + " AS " + targetColumnList.get(j) + ", ";
			j++;
		}
		colXAsYPart = colXAsYPart.trim() + "end";
		colXAsYPart = colXAsYPart.replace(",end", "");

		String selectQuery = "INSERT INTO " + resultTableName + " (" + listOfColumns + ") " + " SELECT " + colXAsYPart + " FROM " + tableName;
		System.out.println("insert: " + selectQuery);

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL + dbNameForRelationalDPs + "?characterEncoding=utf8", login, password);
			stmt = conn.createStatement();
			// conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs, userName, password);

			stmt = (Statement) conn.createStatement();
			stmt.executeUpdate(createTableQuery);
			stmt.executeUpdate(selectQuery);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			conn.close();
			stmt.close();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Mahdi's code:

	public static void RegisterRelationDataProduct(String tableName, String dataType, String dataValue) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement preparedStmt = null;
		PreparedStatement preparedStmt1 = null;
		PreparedStatement prest = null;
		ResultSet keyResultSet = null;
		String url = "jdbc:mysql://localhost:3306/";
		String driverName = "com.mysql.jdbc.Driver";
		String userName = "engine";
		String password = "engine";
		try {
			Class.forName(driverName);
			conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs + "?characterEncoding=utf8", userName, password);

			// Create a new Table for the Relational Data Product
			// Create a new table in mysql
			Vector<Vector<String>> dataTable;
			dataTable = string2vector2d(dataValue);
			System.out.println("table name is: " + tableName);

			String table = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
			String insertsql = "(";
			for (int i = 0; i < dataTable.get(0).size(); i++) {
				String cell = (String) ((Vector) dataTable.get(0)).get(i);
				if (!cell.equals("")) {
					table += " " + cell + " ";
					table += (String) ((Vector) dataTable.get(1)).get(i);
					table += ", ";
					insertsql += "?,";
				}
			}
			table = table.substring(0, table.length() - 2) + ")";
			insertsql = insertsql.substring(0, insertsql.length() - 1) + ")";

			stmt = (Statement) conn.createStatement();
			// System.out.println("table is: \n" + table + "\nend");
			stmt.executeUpdate(table);

			// Insert Data into the new table

			String sql = "INSERT " + tableName + " VALUES" + insertsql;
			// System.out.println("insert sql: \n" + sql);
			prest = (PreparedStatement) conn.prepareStatement(sql);
			for (int j = 2; j < dataTable.size(); j++) {
				for (int i = 0; i < dataTable.get(j).size(); i++) {
					String cell = (String) ((Vector) dataTable.get(j)).get(i);
					if (!cell.equals("")) {
						prest.setObject(i + 1, cell);
					}
				}
				prest.addBatch();
			}
			prest.executeBatch();
		} catch (RuntimeException ex) {
			System.out.println("SQL Exception: " + ex.toString());
			// }
			System.out.println("RuntimeException: " + ex.toString());
		} catch (SQLException ex) {
			System.out.println("SQL Exception: " + ex.toString());
		} catch (ClassNotFoundException cE) {
			System.out.println("ClassNotFoundException: " + cE.toString());
		} finally {
			if (conn != null)
				conn.close();
			if (stmt != null)
				stmt.close();
			if (preparedStmt != null)
				preparedStmt.close();
			if (preparedStmt1 != null)
				preparedStmt1.close();
			if (prest != null)
				prest.close();
			if (keyResultSet != null)
				keyResultSet.close();
		}
	}

	public static String generateUniqueTableName(String dbName, String originalName) throws Exception {
		ArrayList<String> allTableNamesInDB = getAllTableNamesFromDB(dbName);
		Calendar calendar = Calendar.getInstance();

		String baseName;
		if (originalName == null)
			baseName = "t" + new Integer(calendar.get(Calendar.YEAR)).toString() + new Integer(calendar.get(Calendar.MONTH) + 1).toString()
					+ new Integer(calendar.get(Calendar.DATE)).toString();
		else
			baseName = originalName;

		int indexAtTheEnd = allTableNamesInDB.size();
		while (allTableNamesInDB.contains(baseName + +indexAtTheEnd)) {
			indexAtTheEnd++;
		}
		return baseName + +indexAtTheEnd;
	}

	public static ArrayList<String> getAllTableNamesFromDB(String dbName) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		Connection conn;
		String url = "jdbc:mysql://localhost:3306/";
		String driverName = "com.mysql.jdbc.Driver";
		String userName = "engine";
		String password = "engine";

		try {
			Class.forName(driverName);
			conn = (Connection) DriverManager.getConnection(url + dbNameForRelationalDPs + "?characterEncoding=utf8", userName, password);

			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
				// System.out.println(rs.getString(3));
				result.add(rs.getString(3));
			}
		} catch (RuntimeException ex) {
			System.out.println("SQL Exception: " + ex.toString());
			// }
		}

		return result;

	}

	private static Vector<Vector<String>> string2vector2d(String dataValue) {
		// String value = "col1,col2,col3,col4,!INTEGER,TEXT,FLOAT,DATE,!1,2,3,4,!11,22,33,44,!";
		String value = dataValue;
		String[] rows;
		String[] cells;

		/* delimiter */
		String delimiter = "!";
		/* given string will be split by the argument delimiter provided. */
		rows = value.split(delimiter);
		/* print substrings */
		Vector outter = new Vector();
		for (int i = 0; i < rows.length; i++) {
			cells = rows[i].split(",");
			Vector v = new Vector();
			for (int j = 0; j < cells.length; j++)
				v.add(cells[j]);
			outter.add(v);
		}
		return outter;
	}

	public static String Table2String(String tableName) throws SQLException {
		if (tableName.contains("."))
			tableName = tableName.replaceAll("\\.", "ddott");

		Statement stmt = null;
		Statement nodestmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSetMetaData rsMetaData = null;
		Connection con = null;
		Connection con1 = null;

		String data = "";
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "jdpmManager";
		String dbNodeName = dbNameForRelationalDPs;
		String driverName = "com.mysql.jdbc.Driver";
		String userName = "engine";
		String password = "engine";
		try {
			Class.forName(driverName);
			con1 = (Connection) DriverManager.getConnection(url + dbNodeName + "?characterEncoding=utf8", userName, password);
			nodestmt = (Statement) con1.createStatement();
			String query3 = "SELECT COUNT(*) FROM " + tableName;
			rs2 = nodestmt.executeQuery(query3);
			rs2.next();
			int rowCount = rs2.getInt(1);

			String query4 = "SELECT * FROM " + tableName;
			rs3 = nodestmt.executeQuery(query4);
			// *********************************Get MetaData*****************************
			rsMetaData = rs3.getMetaData();
			int columnCount = rsMetaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				data += rsMetaData.getColumnName(i) + ",";
			}

			// ******************************Get Data************************************************
			for (int j = 1; j <= rowCount + 1; j++) {
				if (rs3.next()) {
					data += "!";
					for (int i = 1; i <= columnCount; i++) {
						if (rs3 != null && rs3.getObject(i) != null)
							data += rs3.getObject(i).toString() + ",";
					}
				}
			}
			data += "!";

		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if (con != null)
				con.close();
			if (con1 != null)
				con1.close();
			if (stmt != null)
				stmt.close();
			if (nodestmt != null)
				nodestmt.close();
			if (rs != null)
				rs.close();
			if (rs1 != null)
				rs1.close();
			if (rs2 != null)
				rs2.close();
			if (rs3 != null)
				rs3.close();

		}
		return data;
	}

	public static Node prettifyNode(Node node) throws Exception {
		Document docFromNode = Utility.buildDocumentFromSingleNode(node);
		String docStrFromNode = Utility.nodeToString(docFromNode);
		docStrFromNode = docStrFromNode.replaceAll(">\\s+", ">");
		Document docUnformatted = XMLParser.getDocument(docStrFromNode);
		String docFormatted = Utility.formatXML(docUnformatted);
		Document resultDoc = XMLParser.getDocument(docFormatted);
		Node result = resultDoc.getDocumentElement();
		return result;
	}

	public static Document createWellFormedDocument(String xmlStr) throws Exception {
		String openingTagName = xmlStr.substring(xmlStr.indexOf("<") + 1, xmlStr.indexOf(">")).trim();
		if (openingTagName.indexOf(" ") != -1)
			openingTagName = openingTagName.substring(0, openingTagName.indexOf(" "));
		String theVeryLastClosingTagName = xmlStr.substring(xmlStr.lastIndexOf("</") + 2, xmlStr.lastIndexOf(">")).trim();

		if (!openingTagName.equals(theVeryLastClosingTagName))
			xmlStr = "<wrapperElement>" + xmlStr + "</wrapperElement>";

		Document doc = XMLParser.getDocument(xmlStr);
		if (doc == null)
			System.out.println("ERROR: cannot create document from :\n" + xmlStr);
		return doc;

	}

	public static int getNumberOfFiles() {
		String path = Utility.pathToFileDPsFolder;

		String files;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		return listOfFiles.length;
	}

	public static String createUniqueRunID() throws Exception {
		int numberOfWorkflowsInDB = Utility.getWorkflowsMetadataFromDB().length();
		// the following includes regular DPs and files:
		int numberOfDPsInDB = getDPsMetadata().length();
		// int numOfFiles = getNumberOfFiles();
		return new Integer(numberOfWorkflowsInDB + numberOfDPsInDB).toString();
	}

	public static String json2String(JSONObject jo, int indentation) throws Exception {
		JSONTokener tokener = new JSONTokener(jo.toString());
		JSONObject finalResult = new JSONObject(tokener);
		return finalResult.toString(indentation);
	}

	public static String json2FlatString(JSONObject jo) throws Exception {
		JSONTokener tokener = new JSONTokener(jo.toString());
		JSONObject finalResult = new JSONObject(tokener);
		return finalResult.toString();
	}

	public static String executeShellCommand(String command) throws Exception {
//		System.out.println("EEExecuting command:\n" + command);
		if (command.contains("./mahout")) {
			// writeToFile(command, "/home/view/log");
		}
		Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		StringBuffer sb = new StringBuffer();

		String line = reader.readLine();
		sb.append(line);
		while (line != null) {
			line = reader.readLine();
			if (line != null)
				sb.append(line);
		}

		String result = sb.toString();
		return result;
	}

	public static String getIpGlobal() throws Exception {
		URL whatismyip = new URL("http://checkip.amazonaws.com");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine();
			return ip;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getIpLocal() {
		String ipAddress = null;
		Enumeration<NetworkInterface> net = null;
		try {
			net = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		while (net.hasMoreElements()) {
			NetworkInterface element = net.nextElement();
			Enumeration<InetAddress> addresses = element.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress ip = addresses.nextElement();
				if (ip instanceof Inet4Address) {

					if (ip.isSiteLocalAddress()) {

						ipAddress = ip.getHostAddress();
					}

				}

			}
		}
		return ipAddress.trim();
	}

	public static void createDummyFile(String fileName) throws Exception {
		String pathToFile = pathToFileDPsFolder + fileName;
		File outputFile = new File(pathToFile);
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
		output.write(" ");
		output.close();
	}

	public static void reportToNode(String destNodeIP, String msg) throws Exception {
		String command = "sshpass -p" + passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + destNodeIP + " 'echo '" + msg + "' >> log'";
		executeShellCommand(command);
	}

	public static void reportToDmsg1(String msg) throws Exception {
		String command = "sshpass -pv1ew\'$y$tem\' ssh  -o StrictHostKeyChecking=no viewlog@dmsg1.cs.wayne.edu 'echo '" + msg + "' >> log'";
		executeShellCommand(command);
	}

	public static void reportToMasterNode(String msg, JSONObject schedule) throws Exception {
		reportToLoggerNode(msg);
		// String masterNodeIP = null;
		// if (getIpGlobal().trim().equals(schedule.getString("masterNodeIPGlobal")))
		// masterNodeIP = schedule.getString("masterNodeIPlocal");
		// else
		// masterNodeIP = schedule.getString("masterNodeIPGlobal");
		// reportToNode(masterNodeIP, msg);
	}

	// public static void reportToMasterNode(String msg) throws Exception {
	// reportToLoggerNode(msg);
	// // if (Experiment.schedule != null)
	// // reportToMasterNode(msg, Experiment.schedule);
	// }

	public static void reportToLoggerNode(String msg) throws Exception {
		if (loggingLevel.equals(LoggingLevels.none))
			return;
		// if it's my desktop machine - syso and exit:
		File folder = new File("/home/andrey/");
		if (folder.exists()) {
			System.out.println(msg);
			// return;
		}
		File loggingLocal = new File("/home/view/loggingLocal");
		if (loggingLocal == null)
			writeToFile("it's nulllllllllllllllllllllllllllllllllllllllllllllllll " + getIpGlobal(), "/home/view/log");
		// writeToFile(new Boolean(loggingLocal.exists()).toString(), "/home/view/log");
		// writeToFile(loggingLocal, "/home/view/loggingLoca");
		if (loggingLocal.exists()) {
			writeToFile(msg, "/home/view/log");
			return;
		}
		if (CRM.logNodeIP != null) {
			if (CRM.logNodeIP.trim().equals(getIpGlobal())) {
				writeToFile(msg + "\n", "/home/view/log");
				return;
			}
			String command = "sshpass -p \'system\' ssh  -o StrictHostKeyChecking=no view@" + CRM.logNodeIP + " 'echo '" + msg + "' >> log'";
			String res = executeShellCommand(command);
		}
	}

	public static void reportToLoggerNodeUnconditionally(String msg) throws Exception {
		String command = "sshpass -p \'system\' ssh  -o StrictHostKeyChecking=no view@" + CRM.logNodeIP + " 'echo '" + msg + "' >> log'";
		String res = executeShellCommand(command);
	}

	public static void writeToFile(String text, String pathToFile) {
		try {
			FileWriter fw = new FileWriter(pathToFile, true); // the true will append the new data
			fw.write(text + "\n");// appends the string to the file
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static String readFromAFile(String pathToFile) throws Exception {
		String result = "";
		BufferedReader br = new BufferedReader(new FileReader(pathToFile));
		String line;
		while ((line = br.readLine()) != null) {
			result += line + "\n";
		}
		return result;
	}

	public static String runPieceOfWorkflowLocally(String wfName, String vmIP, ArrayList<String> componentsToRunOnThisVM, JSONObject schedule,
			String runID) throws Exception {

		String commaSeparatedList = componentsToRunOnThisVM.toString().replace("[", "").replace("]", "").replace(" ", "");
		String runWFinVM = null;

		runWFinVM = "cd $CATALINA_HOME/webapps/VIEW/WEB-INF; java" + " -cp classes:lib/* translator.Experiment \"" + wfName + "\" \"" + runID
				+ "\" \"" + vmIP + "\" \"" + Utility.json2FlatString(schedule) + "\" \"" + commaSeparatedList + "\" ";
		System.out.println("local execution " + componentsToRunOnThisVM.toString() + " on " + vmIP);
		// reportToLoggerNode("local execution " + componentsToRunOnThisVM.toString() + " on " + vmIP);
		String result = Utility.executeShellCommand(runWFinVM);
		return result;
	}

	public static String runPieceOfWorkflowOnVM(String wfName, String vmIP, ArrayList<String> componentsToRunOnThisVM, JSONObject schedule,
			String runID) throws Exception {

		String commaSeparatedList = componentsToRunOnThisVM.toString().replace("[", "").replace("]", "").replace(" ", "");
		String runWFinVM = null;

		runWFinVM = "sshpass -p" + Utility.passwdToVMs + " ssh  -o StrictHostKeyChecking=no view@" + vmIP
				+ " 'cd $CATALINA_HOME/webapps/VIEW/WEB-INF; java" + " -cp classes:lib/* translator.Experiment \"" + wfName + "\" \"" + runID
				+ "\" \"" + vmIP + "\" \"" + Utility.json2FlatString(schedule) + "\" \"" + commaSeparatedList + "\"' ";
		// System.out.println("nnnnnnetwork: execute" + componentsToRunOnThisVM.toString() + " on " + vmIP);
		// reportToLoggerNode("nnnnnnetwork: execute" + componentsToRunOnThisVM.toString() + " on " + vmIP);
		// Utility.appendToLog(runWFinVM);
		String result = Utility.executeShellCommand(runWFinVM);
		return result;
	}

	public static void sendFileToVM(String fileName, String srcIP, String destVMip) throws Exception {
		reportToLoggerNode("nnnnnnetwork: send " + fileName + " from " + srcIP + " to " + destVMip);
		// Utility.reportToLoggerNode("ssssending " + fileName + " from " + srcIP + " to " + destVMip);
		String pathToFileDPs = "";
		File f = new File(Utility.pathToFileDPsFolder + fileName);
		if (f.exists())
			pathToFileDPs = Utility.pathToFileDPsFolder;
		else
			pathToFileDPs = "/home/view/FileDPs/";
		String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + pathToFileDPs + fileName + " view@"
				+ destVMip + ":" + pathToFileDPsFolder;
		// String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + pathToFileDPs +
		// fileName
		// + " view@" + destVMip + ":/mnt/mntEBSDevice/FileDPs";
		System.out.println("scp command to send DP: \n" + scpCommand);
		Utility.executeShellCommand(scpCommand);
	}
	
	public static void sendFileXInFolderYToVM(String fileLocation, String fileName, String srcIP, String destVMip) throws Exception {
		reportToLoggerNode("nnnnnnetwork: send " + fileName + " from " + srcIP + " to " + destVMip);
		// Utility.reportToLoggerNode("ssssending " + fileName + " from " + srcIP + " to " + destVMip);
		File f = new File(fileLocation + fileName);

		String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + fileLocation + fileName + " view@"
				+ destVMip + ":" + pathToFileDPsFolder;
		// String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + pathToFileDPs +
		// fileName
		// + " view@" + destVMip + ":/mnt/mntEBSDevice/FileDPs";
		System.out.println("scp command to send DP: \n" + scpCommand);
		Utility.executeShellCommand(scpCommand);
	}
	
	public static void sendFilesXInFolderYToVM(String fileLocation, ArrayList<String> fileNames, String srcIP, String destVMip) throws Exception {
		reportToLoggerNode("nnnnnnetwork: send " + fileNames + " from " + srcIP + " to " + destVMip);
		// Utility.reportToLoggerNode("ssssending " + fileName + " from " + srcIP + " to " + destVMip);

		String listOfFiles = "";
		for (String fileName : fileNames)
			listOfFiles += fileLocation + fileName + " ";
		listOfFiles.trim();
		
		String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + listOfFiles + " view@"
				+ destVMip + ":" + pathToFileDPsFolder;
		// String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + pathToFileDPs +
		// fileName
		// + " view@" + destVMip + ":/mnt/mntEBSDevice/FileDPs";
		System.out.println("scp command to send DP: \n" + scpCommand);
		Utility.executeShellCommand(scpCommand);
	}

	public static void sendFilesToVM(ArrayList<String> fileNames, String srcIP, String destIP) throws Exception {
		System.out.println("sending files " + fileNames + "\nfrom " + srcIP + "\nto " + destIP);
		String prefix = "/var/FileDPs/";

		String listOfFiles = "";
		for (String fileName : fileNames)
			listOfFiles += prefix + fileName + " ";
		listOfFiles.trim();
		String scpCommand = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no " + listOfFiles + " view@" + destIP + ":"
				+ pathToFileDPsFolder;
		System.out.println("scp command to send DP: \n" + scpCommand);
		Utility.executeShellCommand(scpCommand);
	}

	public static void copyFileFromVM(String srcFileName, String targetFileName, String VMIP) throws Exception {
		String command = "sshpass -p '" + Utility.passwdToVMs + "' scp -o StrictHostKeyChecking=no view@" + VMIP + ":" + pathToFileDPsFolder
				+ srcFileName + " " + pathToFileDPsFolder + targetFileName;
		System.out.println("copy from: \n" + command);
		Utility.executeShellCommand(command);
	}

	public static boolean checkIfFileExists(String vmIP) throws Exception {
		String command = "sshpass -p '" + Utility.passwdToVMs + "' ssh view@" + vmIP + " 'cd " + Utility.pathToFileDPsFolder + "; ls -l'";

		String resultOfLS = Utility.executeShellCommand(command);

		if (resultOfLS.contains("ComputeGradeL"))
			return true;
		return false;
	}

	public static boolean checkIfFileExists(String fileName, String path) throws Exception {
		String command = "cd " + path + "; ls -l";
		String result = Utility.executeShellCommand(command);
		if (result.contains(fileName))
			return true;
		return false;
	}

	public static void drawALineOnEachNodesLog() throws Exception {
		ArrayList<String> workerVMs = VMProvisioner.getAvailableVMIPs();
		for (String ip : workerVMs) {
			String command = "sshpass -p '" + Utility.passwdToVMs + "' ssh view@" + ip + " 'echo ---------------------------------------- >> log'";
			executeShellCommand(command);
		}
	}

	public static void main(String[] args) throws Exception {
		// WebbenchUtility.initializeWebbenchConfig();
		// reportToNode("172.30.12.199", "report msg to specific ip (172.30.12.199)");
		// String scheduleStr =
		// "{\"ShuffleYellow10\": \"192.168.29.132\", \"ShuffleOrange6\": \"192.168.29.132\", \"MergeTwo51\": "
		// + "\"192.168.29.132\", \"ShuffleRed2\": \"192.168.29.131\", \"dummy\": \"192.168.29.131\", \"masterNodeIPGlobal\": "
		// + "\"141.217.203.151\", \"masterNodeIPlocal\": \"172.30.12.199\"}";
		// CRM.logNodeIP = "54.234.86.238";
		// reportToLoggerNode("report msg to MASTER");

		System.out.println("global ip: \n" + getIpGlobal());
		System.out.println("global ip: \n" + getIpLocal());
	}
}