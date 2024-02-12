package lambda.calculus.types;

import java.util.ArrayList;

public class ArrowType extends Type {

	public ArrayList<Type> types = null;

	public ArrowType() {

	}

	public ArrowType(Type from, Type to) {
		if (types == null)
			types = new ArrayList<Type>();
		if ((from instanceof ArrowType) && ((ArrowType) from).types.size() > 0)
			types.addAll(((ArrowType) from).types);
		else
			types.add(from);

		if ((to instanceof ArrowType) && ((ArrowType) to).types.size() > 0)
			types.addAll((((ArrowType) to).types));
		else
			types.add(to);
	}

	public ArrowType(Type from0, Type from1, Type to) {
		if (types == null)
			types = new ArrayList<Type>();
		types.add(from0);
		types.add(from1);
		types.add(to);
	}

	public ArrowType(ArrayList<Type> listOfTypes) {
		if (listOfTypes.size() > 1) {
			if (types == null)
				types = new ArrayList<Type>();
			types.addAll(listOfTypes);
		} else
			id = listOfTypes.get(0).id;
	}

	@Override
	public boolean equals(Type t2) {
		if (!(t2 instanceof ArrowType)) {
			return false;
		}

		for (int i = 0; i < types.size(); i++)
			if (types.get(i) != ((ArrowType) t2).types.get(i))
				return false;

		return true;
	}

	public String toString() {
		String fromTypeList = "";
		for (Type currType : types)
			fromTypeList = fromTypeList + currType + "→";
		fromTypeList = fromTypeList + "end";
		if (fromTypeList.indexOf("→end") != -1)
			fromTypeList = fromTypeList.replace("→end", "");
		else
			fromTypeList = fromTypeList.replace("end", "");

		return fromTypeList;
	}

}
