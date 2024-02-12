package builtin;

import java.util.ArrayList;
import java.util.Iterator;

import toyDPM.DataProductManager;
import translator.ExecutionStatus;
import translator.Pair;
import translator.Workflow;
import utility.Utility;
import dataProduct.RelationalDP;
import dataProduct.StringDP;

/**
 * Builtin workflow
 * 
 * @author Andrey Kashlev
 * 
 */
public class Projection extends Workflow {
	public Projection(String instanceid) {
		this.instanceId = instanceid;
		executionStatus = ExecutionStatus.initialized;
	}

	@Override
	public void execute(String runID, boolean registerIntermediateDPs) {
		System.out.println("running workflow " + instanceId);
		if (!readyToExecute())
			return;

		Iterator it = inputPortID_to_DP.values().iterator();
		// database name for input relation
		String dbName = null;
		// data product id, or data product name, which is the same as physical table name for the input relation:
		String tableName = null;
		String listOfColumns = null;

		if (inputPortID_to_DP.get("i1") instanceof RelationalDP) {
			dbName = ((RelationalDP) inputPortID_to_DP.get("i1")).dbName;
			tableName = ((RelationalDP) inputPortID_to_DP.get("i1")).tableName;
		}
		if (inputPortID_to_DP.get("i2") instanceof StringDP)
			listOfColumns = ((StringDP) inputPortID_to_DP.get("i2")).data;
		// add quotes, e.g. change Hobby=sampts to Hobby='stamps':

		executionStatus = ExecutionStatus.inProcessOfExecution;
		RelationalDP result = null;

		try {
			String resultTableName = instanceId + ".o1." + runID;
			resultTableName = resultTableName.replaceAll("\\.", "ddott");
			Utility.executeSelectStatementWithColumnList(dbName, tableName, listOfColumns, resultTableName);
			if (Utility.getAllTableNamesFromDB(dbName).contains(resultTableName)) {
				System.out.println("before outputSch calculation");
				ArrayList<Pair> outputSch = DataProductManager.convertSchemaFromMySQLTypesToViewTypes(Utility
						.getTableSchema(dbName, resultTableName));
				RelationalDP resultDP = new RelationalDP(dbName, resultTableName, DataProductManager.convertSchemaFromMySQLTypesToViewTypes(Utility
						.getTableSchema(dbName, resultTableName)));
				outputPortID_to_DP.put("o1", resultDP);
				if (registerIntermediateDPs)
					Utility.registerDataProduct(resultDP);
			}
		} catch (Exception e) {
		}

	}

	public boolean readyToExecute() {
		if (inputPortID_to_DP == null || inputPortID_to_DP.keySet().size() != 2) {
			// Utility.appendToLog("cannot run Add workflow (" + instanceId + "), number of inputs != 2");
			return false;
		}
		return true;
	}

	@Override
	public void setFinishedStatus() {
		if (!outputPortID_to_DP.isEmpty() && (outputPortID_to_DP.get("o1") instanceof RelationalDP))
			executionStatus = ExecutionStatus.finishedOK;
		else
			executionStatus = ExecutionStatus.finishedError;
	}

}
