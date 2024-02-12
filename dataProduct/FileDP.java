package dataProduct;

public class FileDP extends DataProduct {
	String fileName = null;

	public String data;

	public FileDP(String dataName) {
		// DP id in the database, primary key:
		this.dataName = dataName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		if (fileName == null)
			return dataName;
		return fileName;
	}

}
