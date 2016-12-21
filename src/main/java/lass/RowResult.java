package lass;

import java.util.Map;

public class RowResult {
	//Map from column name to column contents
	private final Map<String, Object> colResults;

	//TODO: This could be immutable or something...
	//Works out best if we have a builder, to filter the names of
	//objects
	public RowResult(Map<String, Object> colResults) {
		this.colResults = colResults;
	}

	public int getInt(String colName) {
		Object result = colResults.get(colName);
		if (result instanceof Integer) {
			return (Integer) result;
		} else {
			throw new IllegalStateException("We were expecting an int in " + colName + ", but the contents were not an int: " + result);
		}
	}

	public long getLong(String colName) {
		Object result = colResults.get(colName);
		if (result instanceof Integer) {
			return (Integer) result;
		} else {
			return (Long) result;
		}
	}

	public String getString(String colName) {
		return (String) colResults.get(colName);
	}

	public byte[] getBlob(String colName) {
		Object result = colResults.get(colName);
		return (byte[]) result;
	}

	@Override
	public String toString() {
		return "RowResult [colResults=" + colResults + "]";
	}

}
