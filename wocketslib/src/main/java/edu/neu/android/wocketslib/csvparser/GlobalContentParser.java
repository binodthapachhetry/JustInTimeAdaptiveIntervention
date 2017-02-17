package edu.neu.android.wocketslib.csvparser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import edu.neu.android.wocketslib.utils.Log;

/**
 * A parser for reading GlobalContent CSV files.
 * 
 * <p>
 * For an example format, see
 * http://healthinterfaces.wikispaces.com/CITY-CSV-GlobalContentFormat
 * </p>
 * 
 * <p>
 * This class reads rows from the CSV and parses them into GlobalRow objects.
 * The data from the cells are parsed into String[], String, int, double, and
 * Date datatypes depending on column formats. These formats are defined in the
 * columnSpecifications parameter to the constructor. See the GlobalCSVFormat
 * class for an example.
 * </p>
 * 
 * <p>
 * If the parser encounters a row which does not match the specified format, it
 * ignores it and logs a warning.
 * </p>
 * 
 * @author pixel@media.mit.edu
 * 
 */
public class GlobalContentParser {

	private String dataPath;
	private ColumnDefinition[] columnDefinitions;

	private ArrayList<GlobalRow> parsedRows;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * Constructor
	 * 
	 * @param path
	 *            The absolute path to the .csv file
	 * @param columnSpecification
	 *            A list of ColumnDefinitions that specify the formats for
	 *            individual columns in the file. See the ColumnDefinition class
	 *            for more information.
	 */
	public GlobalContentParser(String path,
			ColumnDefinition[] columnSpecification) {
		dataPath = path;
		columnDefinitions = columnSpecification;
	}

	/**
	 * Getter for all successfully parsed rows.
	 * 
	 * @return All rows that have been successfully parsed.
	 */
	public List<GlobalRow> rows() {
		return this.parsedRows;
	}

	/**
	 * Loads and parses the CSV file. Rows that violate the format specification
	 * are logged and ignored.
	 * 
	 * @return A list of parsed rows.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public List<GlobalRow> load() throws IOException, FileNotFoundException {
		Log.i("CSVParser", "Reading file...");
		CSVReader reader = new CSVReader(new FileReader(dataPath));
		List<String[]> rows = reader.readAll();

		Log.i("CSVParser", "Parsing file...");
		parseLines(rows);

		return parsedRows;
	}

	/**
	 * Returns all rows whose keywords in a specific column match the desired
	 * set of keywords.
	 * 
	 * @param columnName
	 *            The name of the column, as specified in its ColumnDefinition
	 *            class
	 * @param keywords
	 *            The keywords to match. Only rows whose keywords are a SUBSET
	 *            of the filter keywords will be returned. For example, a filter
	 *            of [A, B, C] will return rows with {A}, {A,B}, {B,C}, {A,B,C},
	 *            but NOT {C, D}
	 * @return A list of matching rows
	 */
	public List<GlobalRow> filterByKeyword(String columnName, String[] keywords) {
		HashSet<String> searchKeywords = new HashSet<String>();
		for (String skw : keywords)
			searchKeywords.add(skw);

		ArrayList<GlobalRow> filteredRows = new ArrayList<GlobalRow>();

		// TODO: Find a way to do this in not O(n) time
		for (GlobalRow row : this.parsedRows) {
			String[] rowKeywords = row.getKeywords(columnName);
			if (rowKeywords == null) {
				filteredRows.add(row);
			} else {
				boolean matches = true;
				for (String kw : rowKeywords) {
					if (!searchKeywords.contains(kw)) {
						matches = false;
						break;
					}
				}
				if (matches)
					filteredRows.add(row);
			}

		}

		return filteredRows;
	}

	private void parseError(int row, int column, String message, boolean fatal) {
		Log.w("CSVParser", "PARSE ERROR (row " + (row + 1) + ", col "
				+ Character.toString((char) (65 + column)) + "): " + message);

		if (fatal) {
			Log.e("CSVParser",
					">>> Fatal parsing error encountered, abandoning parse.");
		}
	}

	private void parseLines(List<String[]> rows) {

		int ignoredRows = 0;
		this.parsedRows = new ArrayList<GlobalRow>();

		for (int n = 0; n < rows.size(); n++) {
			boolean fatal = false;
			boolean validRow = true;
			String[] row = rows.get(n);

			GlobalRow gRow = new GlobalRow(n);

			for (int a = 0; a < columnDefinitions.length; a++) {
				ColumnDefinition colDef = columnDefinitions[a];

				String cell = a < row.length ? row[a].trim() : "";

				if (n == 0) {
					// match header
					if (!cell.matches(colDef.columnName + ".*")) {
						parseError(n, a,
								"Column title does not match specification: '"
										+ cell + "' (should start with '"
										+ colDef.columnName + "')", true);
						fatal = true;
						break;
					}

					if (colDef.bodyType == ColumnDefinition.BodyType.KEYWORD_LIST) {
						int colonIndex = cell.indexOf(':');
						if (colonIndex == -1 || colonIndex == cell.length() - 1) {
							parseError(
									n,
									a,
									"Cannot parse keywords in column title (are you missing a colon?)",
									true);
							fatal = true;
							break;
						}
						String[] keywords = cell.substring(colonIndex + 1)
								.split(";"); // +1 ensures ':' not included
						colDef.setKeywords(keywords);
					}
				} else {
					if (cell.equals("") && !colDef.allowEmpty) {
						parseError(
								n,
								a,
								"Empty cell in a column that does not allow empty cells",
								false);
						validRow = false;
						break;
					}

					// match body
					if (!cell.equals("") && colDef.bodyPattern != null
							&& !cell.matches(colDef.bodyPattern)) {
						parseError(n, a, "Invalid value for column '"
								+ colDef.columnName + "': " + cell, false);
					} else {
						switch (colDef.bodyType) {
						case KEYWORD_LIST:
							String[] keywords;
							if (cell.equals(""))
								keywords = new String[0];
							else
								keywords = cell.split(" *; *");

							if (colDef.areValidKeywords(keywords)) {
								gRow.storeKeywords(colDef.columnName, keywords);
							} else {
								parseError(
										n,
										a,
										"One or more invalid keywords: " + cell,
										false);
								validRow = false;
							}
							break;
						case STRING:
							gRow.storeString(colDef.columnName, cell);
							break;
						case INT:
							if (cell.equals("")) {
								gRow.storeInt(colDef.columnName, 0);
							} else {
								try {
									int i = Integer.parseInt(cell);
									gRow.storeInt(colDef.columnName, i);
								} catch (NumberFormatException e) {
									parseError(n, a, "Not a valid integer: "
											+ cell, false);
									validRow = false;
								}
							}
							break;
						case DOUBLE:
							if (cell.equals("")) {
								gRow.storeDouble(colDef.columnName, 0);
							} else {
								try {
									double f = Double.parseDouble(cell);
									gRow.storeDouble(colDef.columnName, f);
								} catch (NumberFormatException e) {
									parseError(n, a, "Not a valid double: "
											+ cell, false);
									validRow = false;
								}
							}
							break;
						case DATE:
							if (cell.equals("")) {
								gRow.storeDate(colDef.columnName, null);
							} else {
								try {
									Date d = dateFormat.parse(cell);
									gRow.storeDate(colDef.columnName, d);
								} catch (ParseException e) {
									parseError(n, a, "Not a valid date: "
											+ cell, false);
									validRow = false;
								}
							}
							break;
						}
					}
				}
			}

			if (fatal)
				break;

			if (validRow)
				this.parsedRows.add(gRow);
			else
				ignoredRows++;
		}

		Log.i("CSVParser", "Parse complete");
		Log.i("CSVParser", "Original rows: " + rows.size());
		Log.i("CSVParser", "Invalid rows: " + ignoredRows);
		Log.i("CSVParser", "Valid rows: " + parsedRows.size());
	}
}
