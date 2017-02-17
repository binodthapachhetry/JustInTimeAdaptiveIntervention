package edu.neu.android.wocketslib.csvparser;

import java.util.Date;
import java.util.HashMap;

/**
 * Represents a single row of data in a global content CSV. Cell values are
 * retrieved by their column name using one of the get methods. Note that you
 * must know the data format of the column that you are trying to retrieve - if
 * you attempt to read a column using the wrong get function the result will
 * always be null, regardless of whether that column actually has a value
 * stored.
 * 
 * @author pixel@media.mit.edu
 * 
 */
public class GlobalRow {

	/** The original line number in the CSV file */
	public int line;

	private HashMap<String, String[]> keywordColumns = new HashMap<String, String[]>();
	private HashMap<String, String> stringColumns = new HashMap<String, String>();
	private HashMap<String, Integer> intColumns = new HashMap<String, Integer>();
	private HashMap<String, Double> doubleColumns = new HashMap<String, Double>();
	private HashMap<String, Date> dateColumns = new HashMap<String, Date>();

	/**
	 * Constructor
	 * 
	 * @param line
	 *            The original line number in the CSV file
	 */
	public GlobalRow(int line) {
		this.line = line;
	}

	public void storeKeywords(String key, String[] keywords) {
		keywordColumns.put(key, keywords);
	}

	public void storeString(String key, String str) {
		stringColumns.put(key, str);
	}

	public void storeInt(String key, int i) {
		intColumns.put(key, i);
	}

	public void storeDouble(String key, double d) {
		doubleColumns.put(key, d);
	}

	public void storeDate(String key, Date d) {
		dateColumns.put(key, d);
	}

	/**
	 * Retrieve the value of a keyword cell.
	 * 
	 * @param columnName
	 *            The name of the column
	 * @return An array of keywords, or null if that column doesn't exist
	 */
	public String[] getKeywords(String columnName) {
		return keywordColumns.get(columnName);
	}

	/**
	 * Retrieve the value of a string cell.
	 * 
	 * @param columnName
	 *            The name of the column
	 * @return A string, or null if that column doesn't exist
	 */
	public String getString(String columnName) {
		return stringColumns.get(columnName);
	}

	/**
	 * Retrieve the value of an int cell.
	 * 
	 * @param columnName
	 *            The name of the column
	 * @return An integer, or null if that column doesn't exist
	 */
	public int getInt(String columnName) {
		return intColumns.get(columnName);
	}

	/**
	 * Retrieve the value of a double cell.
	 * 
	 * @param columnName
	 *            The name of the column
	 * @return A double, or null if that column doesn't exist
	 */
	public double getDouble(String columnName) {
		return doubleColumns.get(columnName);
	}

	/**
	 * Retrieve the value of a date cell.
	 * 
	 * @param columnName
	 *            The name of the column
	 * @return A Date, or null if that column doesn't exist
	 */
	public Date getDate(String columnName) {
		return dateColumns.get(columnName);
	}
}
