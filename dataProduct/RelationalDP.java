package dataProduct;

import java.util.ArrayList;
import java.util.HashMap;

import translator.Pair;

public class RelationalDP extends DataProduct {
	public String dbName = null;
	public ArrayList<Pair> schema = new ArrayList<Pair>(); //maps column name to type
	public String tableName = null;
	
	public RelationalDP(String dbName, String dataName,  ArrayList<Pair> columnNameToType){
		this.dbName = dbName;
		this.dataName = dataName;
		this.tableName = dataName;
		this.schema = columnNameToType;
		this.description = tableName;
	}
	
	public RelationalDP(String dbName, String dataName, String tableName,  ArrayList<Pair> columnNameToType){
		this.dbName = dbName;
		this.dataName = dataName;
		this.tableName = tableName;
		this.schema = columnNameToType;
		this.description = tableName;
	}

}
