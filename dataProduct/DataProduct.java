package dataProduct;

/**
 * This is an abstract class representing data products inside workflow engine. All other classes in this package
 * are children of this class.
 *  
 * @author Andrey Kashlev
 *
 */
public abstract class DataProduct {
	public String dataName;
	public String description = "data product";
}
