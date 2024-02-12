package translator;

/**
 * This class represents pair data structure.
 * 
 * @author Andrey Kashlev
 *
 */
public class Pair {
	public String left;
	public String right;

	
	public Pair(String left, String right){
		this.left = left;
		this.right = right;
	}
	
	public boolean equals(Pair anotherPair){
		if(anotherPair.left.equals(left) && anotherPair.right.endsWith(right))
			return true;
		return false;
	}
	
}
