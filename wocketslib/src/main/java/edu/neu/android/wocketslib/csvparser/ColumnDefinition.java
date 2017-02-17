package edu.neu.android.wocketslib.csvparser;

import java.util.HashSet;

public class ColumnDefinition {
	public String columnName = null;
	public String bodyPattern = null;
	public String bodyDescription;
	public BodyType bodyType;

	public boolean allowEmpty = false;

	private HashSet<String> keywordSet;

	public enum BodyType {
		KEYWORD_LIST, DATE, STRING, INT, DOUBLE
	}

	/**
	 * Constructor.
	 * 
	 * @param columnName
	 *            The name of the column. In a particular file, the value of the
	 *            first cell in the column must begin with this string, or the
	 *            parser will die dramatically. This is the name you will use to
	 *            retrieve information from this column.
	 * @param bodyPattern
	 *            A regular expression. If not null, all body cell values must
	 *            match the expression or the row is ignored.
	 * @param bodyDescription
	 *            A human-readable description of the allowable contents of the
	 *            column. This is printed out when a column cell fails to match
	 *            against the bodyPattern.
	 * @param bodyType
	 *            The type of data stored in the cell. The cell value is
	 *            automatically parsed to this data type.
	 *            <p>
	 *            If KEYWORD_LIST is specified, this triggers extra behavior in
	 *            the parser. First, a list of valid keywords will be scraped
	 *            from the title row (which should look something like
	 *            "ColumnName: keyword;keyword;..."). Second, all other cell
	 *            values are automatically split into arrays of keywords based
	 *            on semicolon delimiters. If the array contains keywords not
	 *            listed in the title column, the row is ignored.
	 * @param allowEmpty
	 *            Whether this column can contain empty cells.
	 */
	public ColumnDefinition(String columnName, String bodyPattern,
			String bodyDescription, BodyType bodyType, boolean allowEmpty) {
		this.columnName = columnName;
		this.bodyPattern = bodyPattern;
		this.bodyDescription = bodyDescription;
		this.bodyType = bodyType;
		this.allowEmpty = allowEmpty;
	}

	public void setKeywords(String[] keywords) {
		keywordSet = new HashSet<String>();
		for (String kw : keywords)
			keywordSet.add(kw.trim());
	}

	public boolean areValidKeywords(String[] keywords) {
		if (keywords == null)
			return true;

		for (String kw : keywords) {
			if (!keywordSet.contains(kw))
				return false;
		}
		return true;
	}
}
