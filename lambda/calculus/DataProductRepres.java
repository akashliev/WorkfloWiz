package lambda.calculus;

import java.util.ArrayList;

import lambda.calculus.types.PrimitiveType;
import lambda.calculus.types.Type;
import lambda.calculus.types.XSDType;

import org.w3c.dom.Element;

import utility.Inst2Xsd;
import utility.Utility;
import dataProduct.BooleanDP;
import dataProduct.ByteDP;
import dataProduct.DataProduct;
import dataProduct.DecimalDP;
import dataProduct.DoubleDP;
import dataProduct.FloatDP;
import dataProduct.IntDP;
import dataProduct.IntegerDP;
import dataProduct.LongDP;
import dataProduct.NegativeIntegerDP;
import dataProduct.NonNegativeIntegerDP;
import dataProduct.NonPositiveIntegerDP;
import dataProduct.PositiveIntegerDP;
import dataProduct.ShortDP;
import dataProduct.StringDP;
import dataProduct.UnsignedByteDP;
import dataProduct.UnsignedIntDP;
import dataProduct.UnsignedLongDP;
import dataProduct.UnsignedShortDP;
import dataProduct.XmlDP;

public class DataProductRepres extends LambdaExpression {
	// class that serves to represent data products in functional representation of workflows

	public Object data = null;
	public Type type = null;
	public String dataName;
	
	// public DataProductRepres(Object data) {
	// this.data = data;
	// if (data instanceof Integer)
	// type = new Type("Integer");
	// if (data instanceof Float)
	// type = new Type("Float");
	// if (data instanceof String)
	// type = new Type("String");
	// }

	public DataProductRepres(String dataName, Object data, Type type) {
		this.dataName = dataName;
		this.data = data;
		this.type = type;
	}

	public DataProductRepres(DataProduct dp) throws Exception {
		this.dataName = dp.dataName;
		if (dp instanceof StringDP) {
			this.data = ((StringDP) dp).data;
			this.type = new PrimitiveType("String");
		}
		if (dp instanceof DecimalDP) {
			this.data = ((DecimalDP) dp).data;
			this.type = new PrimitiveType("Decimal");
		}
		if (dp instanceof IntegerDP) {
			this.data = ((IntegerDP) dp).data;
			this.type = new PrimitiveType("Integer");
		}
		if (dp instanceof NonPositiveIntegerDP) {
			this.data = ((NonPositiveIntegerDP) dp).data;
			this.type = new PrimitiveType("NonPositiveInteger");
		}
		if (dp instanceof NegativeIntegerDP) {
			this.data = ((NegativeIntegerDP) dp).data;
			this.type = new PrimitiveType("NegativeInteger");
		}
		if (dp instanceof NonNegativeIntegerDP) {
			this.data = ((NonNegativeIntegerDP) dp).data;
			this.type = new PrimitiveType("NonNegativeInteger");
		}
		if (dp instanceof UnsignedLongDP) {
			this.data = ((UnsignedLongDP) dp).data;
			this.type = new PrimitiveType("UnsignedLong");
		}
		if (dp instanceof UnsignedIntDP) {
			this.data = ((UnsignedIntDP) dp).data;
			this.type = new PrimitiveType("UnsignedInt");
		}
		if (dp instanceof UnsignedShortDP) {
			this.data = ((UnsignedShortDP) dp).data;
			this.type = new PrimitiveType("UnsignedShort");
		}
		if (dp instanceof UnsignedByteDP) {
			this.data = ((UnsignedByteDP) dp).data;
			this.type = new PrimitiveType("UnsignedByte");
		}
		if (dp instanceof PositiveIntegerDP) {
			this.data = ((PositiveIntegerDP) dp).data;
			this.type = new PrimitiveType("PositiveInteger");
		}
		if (dp instanceof DoubleDP) {
			this.data = ((DoubleDP) dp).data;
			this.type = new PrimitiveType("Double");
		}
		if (dp instanceof FloatDP) {
			this.data = ((FloatDP) dp).data;
			this.type = new PrimitiveType("Float");
		}
		if (dp instanceof LongDP) {
			this.data = ((LongDP) dp).data;
			this.type = new PrimitiveType("Long");
		}
		if (dp instanceof IntDP) {
			this.data = ((IntDP) dp).data;
			this.type = new PrimitiveType("Int");
		}
		if (dp instanceof ShortDP) {
			this.data = ((ShortDP) dp).data;
			this.type = new PrimitiveType("Short");
		}
		if (dp instanceof ByteDP) {
			this.data = ((ByteDP) dp).data;
			this.type = new PrimitiveType("Byte");
		}
		if (dp instanceof BooleanDP) {
			this.data = ((BooleanDP) dp).data;
			this.type = new PrimitiveType("Boolean");
		}
		if (dp instanceof XmlDP) {
			this.data = ((XmlDP) dp).data;
			this.type = new XSDType(Inst2Xsd.getSchemaElement(Utility.nodeToString(((XmlDP) dp).data)));
		}
	}

	@Override
	public LambdaExpression lambda(LambdaExpression l) {
		return null;
	}

	@Override
	public LambdaExpression apply(LambdaExpression l) {
		return null;

	}

	@Override
	public LambdaExpression beta(LambdaExpression argument) {
		return null;
	}

	@Override
	public boolean isBound(Name n) {
		return false;
	}

	@Override
	public LambdaExpression replace(Name n, LambdaExpression e) {
		return this;
	}

	public String toString() {
		if (type instanceof XSDType) {
			return "<" + ((Element) data).getNodeName().trim() + ">...</>";
		}

		return data.toString();
	}

	public String toStringWithTyping() {
		if (type == null)
			return data.toString();
		else if (type instanceof XSDType) {
			return "<" + ((Element) data).getNodeName().trim() + ">...</> : xs:schema";
		} else
			return data.toString() + ":" + type;
	}

}