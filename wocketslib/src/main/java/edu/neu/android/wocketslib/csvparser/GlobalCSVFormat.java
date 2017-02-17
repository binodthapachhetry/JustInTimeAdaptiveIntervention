package edu.neu.android.wocketslib.csvparser;

import edu.neu.android.wocketslib.csvparser.ColumnDefinition.BodyType;

/**
 * This static class contains format specifications for use with the
 * GlobalContentParser. You can create your own, or use the ones provided here.
 * 
 * @author pixel@media.mit.edu
 * 
 */
public class GlobalCSVFormat {

	/** Test version of the official global content format */
	public static ColumnDefinition[] v0 = {
			new ColumnDefinition("Type", null, "", BodyType.KEYWORD_LIST, false),
			new ColumnDefinition("Categories", null, "", BodyType.KEYWORD_LIST,
					false),
			new ColumnDefinition("ApplicationName", null, "",
					BodyType.KEYWORD_LIST, true),
			new ColumnDefinition("Icon1", null, "", BodyType.KEYWORD_LIST, true),
			new ColumnDefinition("Icon2", null, "", BodyType.KEYWORD_LIST, true),
			new ColumnDefinition("Icon3", null, "", BodyType.KEYWORD_LIST, true),
			new ColumnDefinition("Icon4", null, "", BodyType.KEYWORD_LIST, true),
			new ColumnDefinition("StartDate", "\\d+/\\d+/[1-9][0-9]{3}", "",
					BodyType.DATE, true),
			new ColumnDefinition("EndDate", "\\d+/\\d+/[1-9][0-9]{3}", "",
					BodyType.DATE, true),
			new ColumnDefinition("Teaser", null, "", BodyType.STRING, true),
			new ColumnDefinition("Text", null, "", BodyType.STRING, true),
			new ColumnDefinition("Chars", null, "", BodyType.STRING, true),
			new ColumnDefinition("Graphic", null, "", BodyType.STRING, true),
			new ColumnDefinition("Link", "http://.*", "", BodyType.STRING, true),
			new ColumnDefinition("Sources", null, "", BodyType.STRING, true) };
}
