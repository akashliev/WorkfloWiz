package lambda.calculus.types;

public class PrimitiveType extends Type {

	final static String STRING = "String";
	final static String DECIMAL = "Decimal";
	final static String INTEGER = "Integer";
	final static String NONPOSITIVEINTEGER = "NonPositiveInteger";
	final static String NEGATIVEINTEGER = "NegativeInteger";
	final static String NONNEGATIVEINTEGER = "NonNegativeInteger";
	final static String UNSIGNEDLONG = "UnsignedLong";
	final static String UNSIGNEDINT = "UnsignedInt";
	final static String UNSIGNEDSHORT = "UnsignedShort";
	final static String UNSIGNEDBYTE = "UnsignedByte";
	final static String POSITIVEINTEGER = "PositiveInteger";
	final static String DOUBLE = "Double";
	final static String FLOAT = "Float";
	final static String LONG = "Long";
	final static String INT = "Int";
	final static String SHORT = "Short";
	final static String BYTE = "Byte";
	final static String BOOLEAN = "Boolean";
	final static String DATETIME = "DateTime";
	final static String XML = "Xml";

	public PrimitiveType(String t) {
		if (t.trim().equals(STRING) || t.trim().equals(DECIMAL) || t.trim().equals(INTEGER) || t.trim().equals(NONPOSITIVEINTEGER)
				|| t.trim().equals(NEGATIVEINTEGER) || t.trim().equals(NONNEGATIVEINTEGER) || t.trim().equals(UNSIGNEDLONG)
				|| t.trim().equals(UNSIGNEDINT) || t.trim().equals(UNSIGNEDSHORT) || t.trim().equals(UNSIGNEDBYTE)
				|| t.trim().equals(POSITIVEINTEGER) || t.trim().equals(DOUBLE) || t.trim().equals(FLOAT) || t.trim().equals(LONG)
				|| t.trim().equals(INT) || t.trim().equals(SHORT) || t.trim().equals(BYTE) || t.trim().equals(BOOLEAN) || t.trim().equals(DATETIME)
				|| t.trim().equals(XML)) {
			id = t.trim();
		}
	}

	public boolean isString() {
		return id.equals(STRING);
	}

	public boolean isDecimal() {
		return id.equals(DECIMAL);
	}

	public boolean isInteger() {
		return id.equals(INTEGER);
	}

	public boolean isNonPositiveInteger() {
		return id.equals(NONPOSITIVEINTEGER);
	}

	public boolean isNegativeInteger() {
		return id.equals(NEGATIVEINTEGER);
	}

	public boolean isNonNegativeInteger() {
		return id.equals(NONNEGATIVEINTEGER);
	}

	public boolean isUnsignedLong() {
		return id.equals(UNSIGNEDLONG);
	}

	public boolean isUnsignedInt() {
		return id.equals(UNSIGNEDINT);
	}

	public boolean isUnsignedShort() {
		return id.equals(UNSIGNEDSHORT);
	}

	public boolean isUnsignedByte() {
		return id.equals(UNSIGNEDBYTE);
	}

	public boolean isPositiveInteger() {
		return id.equals(POSITIVEINTEGER);
	}

	public boolean isDouble() {
		return id.equals(DOUBLE);
	}

	public boolean isFloat() {
		return id.equals(FLOAT);
	}

	public boolean isLong() {
		return id.equals(LONG);
	}

	public boolean isInt() {
		return id.equals(INT);
	}

	public boolean isShort() {
		return id.equals(SHORT);
	}

	public boolean isByte() {
		return id.equals(BYTE);
	}

	public boolean isBoolean() {
		return id.equals(BOOLEAN);
	}

	@Override
	public boolean equals(Type t2) {
		if (t2 == null)
			return false;
		
		if (id.equals(t2.id)){
			return true;
		}
		return false;
	}

	public String toString() {
		return id;
	}

}
