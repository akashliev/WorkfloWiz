package translator;

import java.util.HashMap;

import lambda.calculus.SubTyping;
import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;

import org.w3c.dom.Element;

import repository.Repository;
import utility.Utility;
import builtin.AND;
import builtin.Add;
import builtin.AddToList;
import builtin.AnalyzeBrkngTurns;
import builtin.AnalyzeBrkngTurnsL;
import builtin.AnalyzeGasBrk;
import builtin.AnalyzeGasBrkL;
import builtin.AnalyzeSpeedup;
import builtin.AnalyzeSpeedupL;
import builtin.AnalyzeText;
import builtin.AppendStringLength;
import builtin.Boolean2Int;
import builtin.Boolean2String;
import builtin.CartesianProduct;
import builtin.Coercion;
import builtin.ComposeProfile;
import builtin.ComposeProfileL;
import builtin.ComputeGrade;
import builtin.ComputeGradeL;
import builtin.Decrement;
import builtin.Divide;
import builtin.DivideDouble;
import builtin.DivideEx;
import builtin.EQUIV;
import builtin.ExtrBrkngTurns;
import builtin.ExtrGasBrk;
import builtin.ExtrSpeedup;
import builtin.IMPLY;
import builtin.Increment;
import builtin.Intersect;
import builtin.MakeList;
import builtin.Mean;
import builtin.MergeLists;
import builtin.MergeThree;
import builtin.MergeTwo;
import builtin.Multiply;
import builtin.NAND;
import builtin.NOR;
import builtin.NOT;
import builtin.NXOR;
import builtin.NaturalJoin;
import builtin.OR;
import builtin.Power;
import builtin.Projection;
import builtin.Rename;
import builtin.Selection;
import builtin.SetDifference;
import builtin.Shuffle;
import builtin.ShuffleBlue;
import builtin.ShuffleGreen;
import builtin.ShuffleNavy;
import builtin.ShuffleOrange;
import builtin.ShufflePurple;
import builtin.ShuffleRed;
import builtin.ShuffleSleep;
import builtin.ShuffleYellow;
import builtin.SplitInThree;
import builtin.SplitInTwo;
import builtin.Sqrt;
import builtin.Square;
import builtin.Subtract;
import builtin.ToUpperCase;
import builtin.Union;
import builtin.XOR;
import builtin.mAdd;
import builtin.mBgExec;
import builtin.mBgModel;
import builtin.mDiffFit;
import builtin.mFitExec;
import builtin.mImgtbl;
import builtin.mJPEG;
import builtin.mMergeFits;
import builtin.mMergeImgs;
import builtin.mOverlaps;
import builtin.mProjectPP;
import builtin.mProjectPPmImgtbl;

/**
 * This class provides methods to translate workflow SWL specification into an executable form used by workflow engine to run
 * workflows.
 * 
 * @author Andrey Kashlev
 * 
 */
public class Translator {

	public static HashMap<String, Workflow> workflowInstances = new HashMap<String, Workflow>();

	public static Workflow createExecutableWorkflow(String instanceId, Element workflowSpec) throws Exception {
		Workflow resultWF = null;
		String mode = SWLAnalyzer.getWorkflowMode(workflowSpec);
		if (mode.equals("graph-based")) {
			// System.out.println("it's graph-based");
			resultWF = new GraphBasedWorkflow(instanceId, workflowSpec);
			for (String componentID : SWLAnalyzer.getAllReferencedComponentIDs(workflowSpec)) {
				Element currentComponentSpec = Repository.getWorkflowSpecification(SWLAnalyzer.getComponentName(componentID, workflowSpec));
				Workflow componentWorkflow = createExecutableWorkflow(componentID, currentComponentSpec);
				((GraphBasedWorkflow) resultWF).children.put(componentID.trim(), componentWorkflow);
			}
		} else if (mode.equals("builtin")) {
			// System.out.println("it's builtin, name: " + Utility.nodeToString(workflowSpec));
			if (isCoercion(SWLAnalyzer.getWorkflowName(workflowSpec)))
				resultWF = createExecutableCoercion(SWLAnalyzer.getWorkflowName(workflowSpec), instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Add"))
				resultWF = new Add(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Subtract"))
				resultWF = new Subtract(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Multiply"))
				resultWF = new Multiply(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Divide"))
				resultWF = new Divide(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("DivideDouble"))
				resultWF = new DivideDouble(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Power"))
				resultWF = new Power(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AND"))
				resultWF = new AND(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("OR"))
				resultWF = new OR(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("XOR"))
				resultWF = new XOR(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("NAND"))
				resultWF = new NAND(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("NOR"))
				resultWF = new NOR(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("NXOR"))
				resultWF = new NXOR(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("EQUIV"))
				resultWF = new EQUIV(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("IMPLY"))
				resultWF = new IMPLY(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("NOT"))
				resultWF = new NOT(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Increment"))
				resultWF = new Increment(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AppendStringLength"))
				resultWF = new AppendStringLength(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Decrement"))
				resultWF = new Decrement(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Square"))
				resultWF = new Square(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Boolean2Int"))
				resultWF = new Boolean2Int(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Boolean2String"))
				resultWF = new Boolean2String(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ToUpperCase"))
				resultWF = new ToUpperCase(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Selection"))
				resultWF = new Selection(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Projection"))
				resultWF = new Projection(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("NaturalJoin"))
				resultWF = new NaturalJoin(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Union"))
				resultWF = new Union(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Intersect"))
				resultWF = new Intersect(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("SetDifference"))
				resultWF = new SetDifference(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("CartesianProduct"))
				resultWF = new CartesianProduct(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Rename"))
				resultWF = new Rename(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Mean"))
				resultWF = new Mean(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Sqrt"))
				resultWF = new Sqrt(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeText")) {
				resultWF = new AnalyzeText(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("SplitInTwo")) {
				resultWF = new SplitInTwo(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("SplitInThree")) {
				resultWF = new SplitInThree(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("MergeTwo")) {
				resultWF = new MergeTwo(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("MergeThree")) {
				resultWF = new MergeThree(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("Shuffle")) {
				resultWF = new Shuffle(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleRed")) {
				resultWF = new ShuffleRed(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleOrange")) {
				resultWF = new ShuffleOrange(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleYellow")) {
				resultWF = new ShuffleYellow(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleGreen")) {
				resultWF = new ShuffleGreen(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleBlue")) {
				resultWF = new ShuffleBlue(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleNavy")) {
				resultWF = new ShuffleNavy(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShufflePurple")) {
				resultWF = new ShufflePurple(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ExtrGasBrk")) {
				resultWF = new ExtrGasBrk(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ExtrSpeedup")) {
				resultWF = new ExtrSpeedup(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ExtrBrkngTurns")) {
				resultWF = new ExtrBrkngTurns(instanceId);
			}
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeGasBrk")) 
				resultWF = new AnalyzeGasBrk(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeSpeedup")) 
				resultWF = new AnalyzeSpeedup(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeBrkngTurns")) 
				resultWF = new AnalyzeBrkngTurns(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ComposeProfile")) 
				resultWF = new ComposeProfile(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeGasBrkL")) 
				resultWF = new AnalyzeGasBrkL(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeSpeedupL")) 
				resultWF = new AnalyzeSpeedupL(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AnalyzeBrkngTurnsL")) 
				resultWF = new AnalyzeBrkngTurnsL(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ComposeProfileL")) 
				resultWF = new ComposeProfileL(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ComputeGrade")) 
				resultWF = new ComputeGrade(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ComputeGradeL")) 
				resultWF = new ComputeGradeL(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mImgtbl")) 
				resultWF = new mImgtbl(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mProjectPP")) 
				resultWF = new mProjectPP(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mOverlaps")) 
				resultWF = new mOverlaps(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mDiffFit")) 
				resultWF = new mDiffFit(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mFitExec")) 
				resultWF = new mFitExec(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mBgModel"))
				resultWF = new mBgModel(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mBgExec"))
				resultWF = new mBgExec(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("AddToList"))
				resultWF = new AddToList(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("MakeList"))
				resultWF = new MakeList(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mAdd"))
				resultWF = new mAdd(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mJPEG"))
				resultWF = new mJPEG(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("ShuffleSleep"))
				resultWF = new ShuffleSleep(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mProjectPPmImgtbl"))
				resultWF = new mProjectPPmImgtbl(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mMergeImgs"))
				resultWF = new mMergeImgs(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("MergeLists"))
				resultWF = new MergeLists(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("mMergeFits"))
				resultWF = new mMergeFits(instanceId);
			if (SWLAnalyzer.getWorkflowName(workflowSpec).equals("DivideEx"))
				resultWF = new DivideEx(instanceId);
		} else if (mode.equals("primitive")) {
			resultWF = new PrimitiveWorkflow(instanceId, workflowSpec);
		} else if (mode.equals("unary-construct-based")) {
			Element generatedGraphBasedWF = SWLAnalyzer.ucBased2GraphBased(workflowSpec);
		}

		// resultWF.name = SWLAnalyzer.getWorkflowName(workflowSpec);
		if (resultWF == null) {
			Utility.appendToLog("ERROR: could not create executable workflow : " + instanceId);
			return null;
		}

		resultWF.thisWorkflowSpec = workflowSpec;

		return resultWF;
	}

	public static boolean isCoercion(String wfName) {
		if (wfName.indexOf("2") == -1)
			return false;

		Type sourceType = new PrimitiveType(wfName.substring(0, wfName.indexOf("2")));
		Type targetType = new PrimitiveType(wfName.substring(wfName.indexOf("2") + 1, wfName.length()));
		if (sourceType.isValidType() && targetType.isValidType() && SubTyping.subtype(sourceType, targetType))
			return true;
		return false;
	}

	public static Coercion createExecutableCoercion(String wfName, String instanceId) throws ClassNotFoundException {
		String sourceType = wfName.substring(0, wfName.indexOf("2")) + "DP";
		String targetType = wfName.substring(wfName.indexOf("2") + 1, wfName.length()) + "DP";
		return new Coercion(instanceId, sourceType, targetType);
	}

}
