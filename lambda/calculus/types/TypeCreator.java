package lambda.calculus.types;

import java.util.ArrayList;

public class TypeCreator {

	public static Type createType(Type t) {
		if (t instanceof PrimitiveType)
			return new PrimitiveType(t.id);
		else if (t instanceof ArrowType) {
			ArrowType arrType = new ArrowType(new ArrayList<Type>());
			for (int i = 0; i < ((ArrowType) t).types.size(); i++) {
				Type currType = createType(((ArrowType) t).types.get(i));
				arrType.types.add(currType);
			}
			return arrType;
		} else if (t instanceof XSDType)
			return new XSDType(((XSDType) t).xmlSchema);

		System.out.println("type creator returning null");
		return null;
	}

}
