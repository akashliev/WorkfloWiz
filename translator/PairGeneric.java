package translator;

/**
 * This class represents pair data structure.
 * 
 * @author Andrey Kashlev
 * 
 */
public class PairGeneric {
	// not used anywhere so far
	public Object left;
	public Object right;

	public PairGeneric(Object left, Object right) {
		this.left = left;
		this.right = right;
	}

	public boolean equals(PairGeneric anotherPair) {
		if (anotherPair.left.equals(left) && anotherPair.right.equals(right))
			return true;
		return false;
	}

}
