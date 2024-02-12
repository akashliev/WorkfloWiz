package utility;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.inst2xsd.RussianDollStrategy;
import org.apache.xmlbeans.impl.inst2xsd.SalamiSliceStrategy;
import org.apache.xmlbeans.impl.inst2xsd.VenetianBlindStrategy;
import org.apache.xmlbeans.impl.inst2xsd.XsdGenStrategy;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;
import org.apache.xmlbeans.impl.tool.CommandLine;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 16, 2004
 * 
 *         This class generates a set of XMLSchemas from an instance XML document.
 * 
 *         How it works: - first: pass through all the instances, building a TypeSystemHolder structure - second: serialize the
 *         TypeSystemHolder structure into SchemaDocuments
 */
public class Inst2Xsd {
	public static Element getSchemaElement(String xmlInstance) throws Exception {
		String schemaStr = getSchema(xmlInstance);
		//cast byte and short up to int:
		schemaStr = schemaStr.replaceAll("byte", "int");
		schemaStr = schemaStr.replaceAll("short", "int");
		
		Document xsdDoc = XMLParser.getDocument(schemaStr);
		if (xsdDoc == null)
			System.out.println("ERROR: getSchema returned null");
		return xsdDoc.getDocumentElement();

	}

	public static String getSchema(String xmlInstance) throws Exception {
		String[] args = new String[1];
		args[0] = "test.xml";
		if (args == null || args.length == 0) {
			printHelp();
			System.exit(0);
			return null;
		}

		Set flags = new HashSet();
		flags.add("h");
		flags.add("help");
		flags.add("usage");
		flags.add("license");
		flags.add("version");
		flags.add("verbose");
		flags.add("validate");

		Set opts = new HashSet();
		opts.add("design");
		opts.add("simple-content-types");
		opts.add("enumerations");
		opts.add("outDir");
		opts.add("outPrefix");

		CommandLine cl = new CommandLine(args, flags, opts);
		Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();

		if (cl.getOpt("license") != null) {
			CommandLine.printLicense();
			System.exit(0);
			return null;
		}

		if (cl.getOpt("version") != null) {
			CommandLine.printVersion();
			System.exit(0);
			return null;
		}

		if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null) {
			printHelp();
			System.exit(0);
			return null;
		}

		String[] badopts = cl.getBadOpts();
		if (badopts.length > 0) {
			for (int i = 0; i < badopts.length; i++)
				System.out.println("Unrecognized option: " + badopts[i]);
			printHelp();
			System.exit(0);
			return null;
		}

		String design = cl.getOpt("design");
		if (design == null) {
			// default
		} else if (design.equals("vb")) {
			inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
		} else if (design.equals("rd")) {
			inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_RUSSIAN_DOLL);
		} else if (design.equals("ss")) {
			inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
		} else {
			printHelp();
			System.exit(0);
			return null;
		}

		String simpleContent = cl.getOpt("simple-content-types");
		if (simpleContent == null) {
			// default
		} else if (simpleContent.equals("smart")) {
			inst2XsdOptions.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
		} else if (simpleContent.equals("string")) {
			inst2XsdOptions.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
		} else {
			printHelp();
			System.exit(0);
			return null;
		}

		String enumerations = cl.getOpt("enumerations");
		if (enumerations == null) {
			// default
		} else if (enumerations.equals("never")) {
			inst2XsdOptions.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
		} else {
			try {
				int intVal = Integer.parseInt(enumerations);
				inst2XsdOptions.setUseEnumerations(intVal);
			} catch (NumberFormatException e) {
				printHelp();
				System.exit(0);
				return null;
			}
		}

		File outDir = new File(cl.getOpt("outDir") == null ? "." : cl.getOpt("outDir"));

		String outPrefix = cl.getOpt("outPrefix");
		if (outPrefix == null)
			outPrefix = "schema";

		inst2XsdOptions.setVerbose((cl.getOpt("verbose") != null));
		boolean validate = cl.getOpt("validate") != null;

		File[] xmlFiles = cl.filesEndingWith(".xml");

		XmlObject[] xmlInstances = new XmlObject[xmlFiles.length];

		if (xmlInstances.length == 0) {
			printHelp();
			System.exit(0);
			return null;
		}

		int i = 0;
		try {
			for (i = 0; i < xmlFiles.length; i++) {

				xmlInstances[i] = XmlObject.Factory.parse(xmlInstance);
			}
		} catch (XmlException e) {
			System.err.println("Invalid xml file: '" + xmlFiles[i].getName() + "'. " + e.getMessage());
			return null;
		}
		// catch (IOException e)
		// {
		// System.err.println("Could not read file: '" + xmlFiles[i].getName() + "'. " + e.getMessage());
		// return null;
		// }

		SchemaDocument[] schemaDocs = inst2xsd(xmlInstances, inst2XsdOptions);

		try {
			for (i = 0; i < schemaDocs.length; i++) {
				SchemaDocument schema = schemaDocs[i];

				if (inst2XsdOptions.isVerbose())
					System.out.println("----------------------\n\n" + schema);

				// schema.save(new File("testFiles/JUnit/tmpSchema", outPrefix + i + ".xsd"), new
				// XmlOptions().setSavePrettyPrint());

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				schema.save(baos, new XmlOptions().setSavePrettyPrint());
				// System.out.println("baos.toString():");
				// System.out.println(baos.toString());
				return baos.toString();
				// Document xsdFile = Utility.readFileAsDocument("testFiles/JUnit/tmpSchema/schema0.xsd");
				// System.out.println("************************ schema: :)");
				// System.out.println(Utility.nodeToString(xsdFile));
			}
		} catch (IOException e) {
			System.err.println("Could not write file: '" + outDir + File.pathSeparator + outPrefix + i + ".xsd" + "'. " + e.getMessage());
			return null;
		}

		if (validate) {
			validateInstances(schemaDocs, xmlInstances);
		}
		return null;
	}

	private static void printHelp() {
		System.out.println("Generates XMLSchema from instance xml documents.");
		System.out.println("Usage: inst2xsd [opts] [instance.xml]*");
		System.out.println("Options include:");
		System.out.println("    -design [rd|ss|vb] - XMLSchema design type");
		System.out.println("             rd  - Russian Doll Design - local elements and local types");
		System.out.println("             ss  - Salami Slice Design - global elements and local types");
		System.out.println("             vb  - Venetian Blind Design (default) - local elements and global complex types");
		System.out.println("    -simple-content-types [smart|string] - Simple content types detection (leaf text). Smart is the default");
		System.out.println("    -enumerations [never|NUMBER] - Use enumerations. Default value is "
				+ Inst2XsdOptions.ENUMERATION_NOT_MORE_THAN_DEFAULT + ".");
		System.out.println("    -outDir [dir] - Directory for output files. Default is '.'");
		System.out.println("    -outPrefix [file_name_prefix] - Prefix for output file names. Default is 'schema'");
		System.out.println("    -validate - Validates input instances agaist generated schemas.");
		System.out.println("    -verbose - print more informational messages");
		System.out.println("    -license - print license information");
		System.out.println("    -help - help imformation");
	}

	private Inst2Xsd() {
	}

	// public entry points

	public static SchemaDocument[] inst2xsd(Reader[] instReaders, Inst2XsdOptions options) throws IOException, XmlException {
		XmlObject[] instances = new XmlObject[instReaders.length];
		for (int i = 0; i < instReaders.length; i++) {
			instances[i] = XmlObject.Factory.parse(instReaders[i]);
		}
		return inst2xsd(instances, options);
	}

	public static SchemaDocument[] inst2xsd(XmlObject[] instances, Inst2XsdOptions options) {
		if (options == null)
			options = new Inst2XsdOptions();

		// create structure
		TypeSystemHolder typeSystemHolder = new TypeSystemHolder();

		XsdGenStrategy strategy;
		switch (options.getDesign()) {
		case Inst2XsdOptions.DESIGN_RUSSIAN_DOLL:
			strategy = new RussianDollStrategy();
			break;

		case Inst2XsdOptions.DESIGN_SALAMI_SLICE:
			strategy = new SalamiSliceStrategy();
			break;

		case Inst2XsdOptions.DESIGN_VENETIAN_BLIND:
			strategy = new VenetianBlindStrategy();
			break;

		default:
			throw new IllegalArgumentException("Unknown design.");
		}
		// processDoc the instance
		strategy.processDoc(instances, options, typeSystemHolder);

		if (options.isVerbose())
			System.out.println("typeSystemHolder.toString(): " + typeSystemHolder);

		SchemaDocument[] sDocs = typeSystemHolder.getSchemaDocuments();

		return sDocs;
	}

	private static boolean validateInstances(SchemaDocument[] sDocs, XmlObject[] instances) {
		SchemaTypeLoader sLoader;
		Collection compErrors = new ArrayList();
		XmlOptions schemaOptions = new XmlOptions();
		schemaOptions.setErrorListener(compErrors);
		try {
			sLoader = XmlBeans.loadXsd(sDocs, schemaOptions);
		} catch (Exception e) {
			if (compErrors.isEmpty() || !(e instanceof XmlException)) {
				e.printStackTrace(System.out);
			}
			System.out.println("\n-------------------\n\nInvalid schemas.");
			for (Iterator errors = compErrors.iterator(); errors.hasNext();) {
				XmlError xe = (XmlError) errors.next();
				System.out.println(xe.getLine() + ":" + xe.getColumn() + " " + xe.getMessage());
			}
			return false;
		}

		System.out.println("\n-------------------");
		boolean result = true;

		for (int i = 0; i < instances.length; i++) {
			XmlObject xobj;

			try {
				xobj = sLoader.parse(instances[i].newXMLStreamReader(), null, new XmlOptions().setLoadLineNumbers());
			} catch (XmlException e) {
				System.out.println("Error:\n" + instances[i].documentProperties().getSourceName() + " not loadable: " + e);
				e.printStackTrace(System.out);
				result = false;
				continue;
			}

			Collection errors = new ArrayList();

			if (xobj.schemaType() == XmlObject.type) {
				System.out.println(instances[i].documentProperties().getSourceName() + " NOT valid.  ");
				System.out.println("  Document type not found.");
				result = false;
			} else if (xobj.validate(new XmlOptions().setErrorListener(errors)))
				System.out.println("Instance[" + i + "] valid - " + instances[i].documentProperties().getSourceName());
			else {
				System.out.println("Instance[" + i + "] NOT valid - " + instances[i].documentProperties().getSourceName());
				for (Iterator it = errors.iterator(); it.hasNext();) {
					XmlError xe = (XmlError) it.next();
					System.out.println(xe.getLine() + ":" + xe.getColumn() + " " + xe.getMessage());
				}
				result = false;
			}
		}

		return result;
	}
}