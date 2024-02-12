package builtin;

import translator.ExecutionStatus;
import translator.Workflow;
import utility.Utility;
import dataProduct.ByteDP;
import dataProduct.DataProduct;
import dataProduct.DecimalDP;
import dataProduct.DoubleDP;
import dataProduct.FloatDP;
import dataProduct.IntDP;
import dataProduct.IntegerDP;
import dataProduct.LongDP;
import dataProduct.NonNegativeIntegerDP;
import dataProduct.NonPositiveIntegerDP;
import dataProduct.ShortDP;
import dataProduct.StringDP;
import dataProduct.UnsignedIntDP;
import dataProduct.UnsignedLongDP;
import dataProduct.UnsignedShortDP;

public class Coercion extends Workflow {
	public String sourceType;
	public String targetType;

	public Coercion(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	public Coercion(String instanceid, String sourceType, String targetType) {
		this.instanceId = instanceid;
		this.sourceType = sourceType;
		this.targetType = targetType;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public boolean readyToExecute() {
		// if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 1
		// || !inputPortID_to_DP.get("i1").getClass().getSimpleName().equals(sourceType)){
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 1) {
			return false;
		}
		return true;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) throws Exception {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		executionStatus = ExecutionStatus.inProcessOfExecution;

		DataProduct input = null;
		DataProduct resultDP = null;

		// to StringDP:
		if (targetType.equals(StringDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));
			resultDP = input;
		}

		// to DecimalDP:
		if (targetType.equals(DecimalDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new DecimalDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new DecimalDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new DecimalDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to IntegerDP:
		if (targetType.equals(IntegerDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new IntegerDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new IntegerDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new IntegerDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to NonPositiveIntegerDP:
		if (targetType.equals(NonPositiveIntegerDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new NonPositiveIntegerDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new NonPositiveIntegerDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new NonPositiveIntegerDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to NonNegativeIntegerDP:
		if (targetType.equals(NonNegativeIntegerDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new NonNegativeIntegerDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new NonNegativeIntegerDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new NonNegativeIntegerDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to UnsignedLongDP:
		if (targetType.equals(UnsignedLongDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new UnsignedLongDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new UnsignedLongDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new UnsignedLongDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to UnsignedIntDP:
		if (targetType.equals(UnsignedIntDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new UnsignedIntDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new UnsignedIntDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new UnsignedIntDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to UnsignedShortDP:
		if (targetType.equals(UnsignedShortDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new UnsignedShortDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new UnsignedShortDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new UnsignedShortDP(((StringDP) input).data.toString(), instanceId + ".o1." + runID);
		}

		// to DoubleDP:
		if (targetType.equals(DoubleDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new DoubleDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new DoubleDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new DoubleDP(((StringDP) input).data, instanceId + ".o1." + runID);
		}

		// to FloatDP:
		if (targetType.equals(FloatDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new FloatDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new FloatDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new FloatDP(((StringDP) input).data, instanceId + ".o1." + runID);
		}

		// to LongDP:
		if (targetType.equals(LongDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new LongDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new LongDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new LongDP(((StringDP) input).data, instanceId + ".o1." + runID);
		}

		// to IntDP:
		if (targetType.equals(IntDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new IntDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new IntDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new IntDP(((StringDP) input).data, instanceId + ".o1." + runID);
		}

		// to ShortDP:
		if (targetType.equals(ShortDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new ShortDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new ShortDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new ShortDP(((StringDP) input).data, instanceId + ".o1." + runID);
		}

		// to ByteDP:
		if (targetType.equals(ByteDP.class.getSimpleName())) {
			input = ((StringDP) inputPortID_to_DP.get("i1"));

			if (((StringDP) input).data.trim().equals("true")) {
				resultDP = new ByteDP("1", instanceId + ".o1." + runID);
				return;
			}
			if (((StringDP) input).data.trim().equals("false")) {
				resultDP = new ByteDP("0", instanceId + ".o1." + runID);
				return;
			}

			resultDP = new ByteDP(((StringDP) input).data, instanceId + ".o1." + runID);
		}

		// // to StringDP:
		// if (inputPortID_to_DP.get("i1") instanceof DecimalDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((DecimalDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((DecimalDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof IntegerDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((IntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((IntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NonPositiveIntegerDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((NonPositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((NonPositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NegativeIntegerDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((NegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((NegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NonNegativeIntegerDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((NonNegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((NonNegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedLongDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((UnsignedLongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((UnsignedLongDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedIntDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((UnsignedIntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((UnsignedIntDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedShortDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((UnsignedShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((UnsignedShortDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof PositiveIntegerDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((PositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(((PositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof DoubleDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((DoubleDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Double(((DoubleDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof FloatDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((FloatDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Float(((FloatDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof LongDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((LongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Long(((LongDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof IntDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((IntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Integer(((IntDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Short(((ShortDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Byte(((ByteDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(StringDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// resultDP = new StringDP(new Boolean(((BooleanDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		//
		// // to DecimalDP:
		// if (inputPortID_to_DP.get("i1") instanceof IntegerDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((IntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((IntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NonPositiveIntegerDP && targetType.equals(DecimalDP.class.getSimpleName()))
		// {
		// input = ((NonPositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((NonPositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NegativeIntegerDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((NegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((NegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NonNegativeIntegerDP && targetType.equals(DecimalDP.class.getSimpleName()))
		// {
		// input = ((NonNegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((NonNegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedLongDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((UnsignedLongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((UnsignedLongDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedIntDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((UnsignedIntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((UnsignedIntDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedShortDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((UnsignedShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((UnsignedShortDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof PositiveIntegerDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((PositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(((PositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof DoubleDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((DoubleDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(new Double(((DoubleDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof FloatDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((FloatDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(new Float(((FloatDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof LongDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((LongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(new Long(((LongDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof IntDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((IntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(new Integer(((IntDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(new Short(((ShortDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DecimalDP(new Byte(((ByteDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(DecimalDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new DecimalDP("1", instanceId + ".o1." + runID);
		// else
		// resultDP = new DecimalDP("0", instanceId + ".o1." + runID);
		// }
		//
		// // to IntegerDP:
		// if (inputPortID_to_DP.get("i1") instanceof NonPositiveIntegerDP && targetType.equals(IntegerDP.class.getSimpleName()))
		// {
		// input = ((NonPositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((NonPositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NegativeIntegerDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((NegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((NegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof NonNegativeIntegerDP && targetType.equals(IntegerDP.class.getSimpleName()))
		// {
		// input = ((NonNegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((NonNegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedLongDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((UnsignedLongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((UnsignedLongDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedIntDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((UnsignedIntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((UnsignedIntDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedShortDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((UnsignedShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((UnsignedShortDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof PositiveIntegerDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((PositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(((PositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof LongDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((LongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(new Long(((LongDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof IntDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((IntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(new Integer(((IntDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(new Short(((ShortDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntegerDP(new Byte(((ByteDP) input).data).toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(IntegerDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new IntegerDP("1", instanceId + ".o1." + runID);
		// else
		// resultDP = new IntegerDP("0", instanceId + ".o1." + runID);
		// }
		//
		// // to NonPositiveIntegerDP:
		// if (inputPortID_to_DP.get("i1") instanceof NegativeIntegerDP &&
		// targetType.equals(NonPositiveIntegerDP.class.getSimpleName())) {
		// input = ((NegativeIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new NonPositiveIntegerDP(((NegativeIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		//
		// // to NonNegativeIntegerDP:
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedLongDP &&
		// targetType.equals(NonNegativeIntegerDP.class.getSimpleName())) {
		// input = ((UnsignedLongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new NonNegativeIntegerDP(((UnsignedLongDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedIntDP &&
		// targetType.equals(NonNegativeIntegerDP.class.getSimpleName())) {
		// input = ((UnsignedIntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new NonNegativeIntegerDP(((UnsignedIntDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedShortDP &&
		// targetType.equals(NonNegativeIntegerDP.class.getSimpleName())) {
		// input = ((UnsignedShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new NonNegativeIntegerDP(((UnsignedShortDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP &&
		// targetType.equals(NonNegativeIntegerDP.class.getSimpleName())) {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new NonNegativeIntegerDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof PositiveIntegerDP &&
		// targetType.equals(NonNegativeIntegerDP.class.getSimpleName())) {
		// input = ((PositiveIntegerDP) inputPortID_to_DP.get("i1"));
		// resultDP = new NonNegativeIntegerDP(((PositiveIntegerDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		//
		// // to UnsignedLongDP:
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedIntDP && targetType.equals(UnsignedLongDP.class.getSimpleName())) {
		// input = ((UnsignedIntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new UnsignedLongDP(((UnsignedIntDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedShortDP && targetType.equals(UnsignedLongDP.class.getSimpleName()))
		// {
		// input = ((UnsignedShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new UnsignedLongDP(((UnsignedShortDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP && targetType.equals(UnsignedLongDP.class.getSimpleName())) {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new UnsignedLongDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		//
		// // to UnsignedIntDP:
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedShortDP && targetType.equals(UnsignedIntDP.class.getSimpleName())) {
		// input = ((UnsignedShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new UnsignedIntDP(((UnsignedShortDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP && targetType.equals(UnsignedIntDP.class.getSimpleName())) {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new UnsignedIntDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		//
		// // to UnsignedShortDP:
		// if (inputPortID_to_DP.get("i1") instanceof UnsignedByteDP && targetType.equals(UnsignedShortDP.class.getSimpleName()))
		// {
		// input = ((UnsignedByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new UnsignedShortDP(((UnsignedByteDP) input).data.toString(), instanceId + ".o1." + runID);
		// }
		//
		// // to DoubleDP:
		// if (inputPortID_to_DP.get("i1") instanceof FloatDP && targetType.equals(DoubleDP.class.getSimpleName())) {
		// input = ((FloatDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DoubleDP(((FloatDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof LongDP && targetType.equals(DoubleDP.class.getSimpleName())) {
		// input = ((LongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DoubleDP(((LongDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof IntDP && targetType.equals(DoubleDP.class.getSimpleName())) {
		// input = ((IntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DoubleDP(((IntDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(DoubleDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DoubleDP(((ShortDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(DoubleDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new DoubleDP(((ByteDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(DoubleDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new DoubleDP(1, instanceId + ".o1." + runID);
		// else
		// resultDP = new DoubleDP(0, instanceId + ".o1." + runID);
		// }
		//
		// // to FloatDP:
		// if (inputPortID_to_DP.get("i1") instanceof LongDP && targetType.equals(FloatDP.class.getSimpleName())) {
		// input = ((LongDP) inputPortID_to_DP.get("i1"));
		// resultDP = new FloatDP(((LongDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof IntDP && targetType.equals(FloatDP.class.getSimpleName())) {
		// input = ((IntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new FloatDP(((IntDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(FloatDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new FloatDP(((ShortDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(FloatDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new FloatDP(((ByteDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(FloatDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new FloatDP(1, instanceId + ".o1." + runID);
		// else
		// resultDP = new FloatDP(0, instanceId + ".o1." + runID);
		// }
		//
		// // to LongDP:
		// if (inputPortID_to_DP.get("i1") instanceof IntDP && targetType.equals(LongDP.class.getSimpleName())) {
		// input = ((IntDP) inputPortID_to_DP.get("i1"));
		// resultDP = new LongDP(((IntDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(LongDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new LongDP(((ShortDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(LongDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new LongDP(((ByteDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(LongDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new LongDP(1, instanceId + ".o1." + runID);
		// else
		// resultDP = new LongDP(0, instanceId + ".o1." + runID);
		// }
		//
		// // to IntDP:
		// if (inputPortID_to_DP.get("i1") instanceof ShortDP && targetType.equals(IntDP.class.getSimpleName())) {
		// input = ((ShortDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntDP(((ShortDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(IntDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new IntDP(((ByteDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(IntDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new IntDP(1, instanceId + ".o1." + runID);
		// else
		// resultDP = new IntDP(0, instanceId + ".o1." + runID);
		// }
		//
		// // to ShortDP:
		// if (inputPortID_to_DP.get("i1") instanceof ByteDP && targetType.equals(ShortDP.class.getSimpleName())) {
		// input = ((ByteDP) inputPortID_to_DP.get("i1"));
		// resultDP = new ShortDP(((ByteDP) input).data, instanceId + ".o1." + runID);
		// }
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(ShortDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new ShortDP((short) 1, instanceId + ".o1." + runID);
		// else
		// resultDP = new ShortDP((short) 0, instanceId + ".o1." + runID);
		// }
		//
		// // to ByteDP:
		// if (inputPortID_to_DP.get("i1") instanceof BooleanDP && targetType.equals(ByteDP.class.getSimpleName())) {
		// input = ((BooleanDP) inputPortID_to_DP.get("i1"));
		// if (((BooleanDP) input).data)
		// resultDP = new ByteDP((byte) 1, instanceId + ".o1." + runID);
		// else
		// resultDP = new ByteDP((byte) 0, instanceId + ".o1." + runID);
		// }

		outputPortID_to_DP.put("o1", resultDP);
		if (registerIntermediateDPs)
			Utility.registerDataProduct(resultDP);
		System.out.println(instanceId + " successfully converted " + sourceType + " to " + targetType);
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty())
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;

	}

}
