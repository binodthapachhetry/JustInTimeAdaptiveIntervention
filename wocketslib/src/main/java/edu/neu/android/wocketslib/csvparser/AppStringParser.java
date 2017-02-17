package edu.neu.android.wocketslib.csvparser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Parses CITY .CSV data files and provides a series of convenient selector
 * functions.
 * <p>
 * See: http://healthinterfaces.wikispaces.com/CITY-CSV-DefinitionFormat
 * </p>
 * 
 * <p>
 * Use getSection() to retrieve simple sections (Format 1). Use matchKeywords()
 * to retrieve values from keyword sections (Format 2).
 * </p>
 * 
 * @author pixel@media.mit.edu
 * 
 */
public class AppStringParser {

	private String dataPath;

	private HashMap<String, ArrayList<String>> simpleSections;
	private HashMap<String, ArrayList<KeywordLine>> keywordSections;
	private HashMap<String, HashMap<String, ArrayList<KeywordLine>>> reverseKeywordLookups;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            The absolute path to the CSV file
	 */
	public AppStringParser(String path) {
		dataPath = path;
		simpleSections = new HashMap<String, ArrayList<String>>();
		keywordSections = new HashMap<String, ArrayList<KeywordLine>>();
		reverseKeywordLookups = new HashMap<String, HashMap<String, ArrayList<KeywordLine>>>();
	}

	/**
	 * Loads and parses the target CSV file. Call this before using any other
	 * methods.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void load() throws IOException, FileNotFoundException {
		CSVReader reader = new CSVReader(new FileReader(dataPath));
		List<String[]> rows = reader.readAll();
		parseLines(rows);
	}

	/**
	 * Returns a list of all the values in a section See: Format 1 in
	 * http://healthinterfaces.wikispaces.com/CITY-CSV-DefinitionFormat
	 * 
	 * @param sectionTitle
	 *            The name of the section to retrieve, e.g. "Title" or
	 *            "Congratulatory message"
	 * @return An array of the strings contained in that section, or null if no
	 *         such section exists
	 */
	public String[] getSection(String sectionTitle) {
		if (simpleSections.containsKey(sectionTitle))
			return simpleSections.get(sectionTitle).toArray(new String[0]);
		else
			return null;
	}

	/**
	 * Retrieves matching values from a keyword section. See: Format 2 in
	 * http://healthinterfaces.wikispaces.com/CITY-CSV-DefinitionFormat Given a
	 * list of keywords, returns all values whose keywords are a subset of those
	 * passed. For example, [A, B] matches {A;B}, {A}, and {B}, but NOT {A;B;C}
	 * or {B;C}. If a value has no keywords associated with it (an empty
	 * column), then it always matches.
	 * <p>
	 * The "All" keyword is special. If present, all rows are returned.
	 * </p>
	 * 
	 * @param sectionTitle
	 *            The name of the section to match against, e.g. "Title" or
	 *            "Tip"
	 * @param keywords
	 *            A set of keywords.
	 * @param matchEmpty
	 *            If true, then rows with no keywords are also returned.
	 *            Otherwise they are excluded.
	 * @return All values that match the keywords, or null if the section
	 *         doesn't exist.
	 * 
	 */
	public String[] matchKeywords(String sectionTitle,
			Iterable<String> keywords, boolean matchEmpty) {
		// TODO: Write a more efficient version? At the moment, we have to test
		// every entry in the section...

		if (!keywordSections.containsKey(sectionTitle))
			return null;

		HashSet<String> keywordSet = new HashSet<String>();
		for (String keyword : keywords)
			keywordSet.add(keyword);

		ArrayList<String> matchingLines = new ArrayList<String>();
		for (KeywordLine line : keywordSections.get(sectionTitle)) {
			if (line.matches(keywordSet, matchEmpty))
				matchingLines.add(line.value);
		}

		String[] arrayVer = new String[matchingLines.size()];
		return matchingLines.toArray(arrayVer);
	}

	/**
	 * <code>matchEmpty</code> defaults to <code>true</code>
	 * 
	 * @see AppStringParser#matchKeywords(String, Iterable, boolean)
	 */
	public String[] matchKeywords(String sectionTitle, Iterable<String> keywords) {
		return matchKeywords(sectionTitle, keywords, true);
	}

	/**
	 * Given a section, returns the keywords associated with a particular value.
	 * Multiple arrays of keywords may be returned if the value appears multiple
	 * times in the section. If the value is not found, an empty List is
	 * returned.
	 * 
	 * @param sectionTitle
	 *            The name of the section to search
	 * @param value
	 *            The value to search for (3rd column)
	 * @return A List of arrays of keywords. One array of keywords for each
	 *         occurrence of <code>value</code> in the section.
	 */
	public List<String[]> lookupKeywords(String sectionTitle, String value) {
		ArrayList<String[]> hits = new ArrayList<String[]>();

		HashMap<String, ArrayList<KeywordLine>> sectionLookup = reverseKeywordLookups
				.get(sectionTitle);
		if (sectionLookup == null) {
			// Log.i("CSVParser", "Null section");
			return hits;
		}

		ArrayList<KeywordLine> matches = sectionLookup.get(value);
		if (matches == null) {
			// Log.i("CSVParser", "Null hits");
			return hits;
		}

		for (KeywordLine keyLine : matches)
			hits.add(keyLine.keywords);
		return hits;
	}

	private void parseLines(List<String[]> rows) {
		simpleSections.clear();
		keywordSections.clear();
		reverseKeywordLookups.clear();

		boolean keywordSection = false;
		boolean searchingForType = true;
		String currentSectionTitle = "";

		for (String[] row : rows) {
			if (row.length == 0)
				continue;

			// trim all values
			for (int a = 0; a < row.length; a++)
				row[a] = row[a].trim();

			// skip empty rows
			if (row[0].equals("") && (row.length < 2 || row[1].equals(""))
					&& (row.length < 3 || row[2].equals("")))
				continue;

			if (!row[0].equals("")) {
				// new section!
				// Excises all parentheticals and trims the result
				// E.g. removes anything between parentheses
				// Breaks with nested parens
				currentSectionTitle = row[0].replaceAll(" *\\([^)]*\\) *", " ")
						.trim();
				// Log.i("CITYParser","New title: " + currentSectionTitle);
				searchingForType = true;
			} else {
				if (searchingForType)
					keywordSection = (row.length >= 3 && !row[2].equals(""));

				if (row.length < 2)
					continue;

				if (keywordSection) {
					if (row.length < 3)
						continue;

					// Store keywords
					if (!keywordSections.containsKey(currentSectionTitle))
						keywordSections.put(currentSectionTitle,
								new ArrayList<KeywordLine>());
					KeywordLine keyLine = new KeywordLine(row[1], row[2]);
					keywordSections.get(currentSectionTitle).add(keyLine);
					// keywordSections.get(currentSectionTitle).add(new
					// KeywordLine(row[1], row[2]));

					// Store reverse lookup
					HashMap<String, ArrayList<KeywordLine>> reverseLookupSection = reverseKeywordLookups
							.get(currentSectionTitle);
					if (reverseLookupSection == null) {
						reverseLookupSection = new HashMap<String, ArrayList<KeywordLine>>();
						reverseKeywordLookups.put(currentSectionTitle,
								reverseLookupSection);
					}

					ArrayList<KeywordLine> revKeyLines = reverseLookupSection
							.get(keyLine.value);
					if (revKeyLines == null) {
						revKeyLines = new ArrayList<KeywordLine>();
						reverseLookupSection.put(keyLine.value, revKeyLines);
					}
					revKeyLines.add(keyLine);
				} else {
					if (!simpleSections.containsKey(currentSectionTitle))
						simpleSections.put(currentSectionTitle,
								new ArrayList<String>());
					simpleSections.get(currentSectionTitle).add(row[1].trim());
					// Log.i("CITYParser","Data: " + row[1].trim());
				}
			}
		}
	}

	private class KeywordLine {
		public String value;
		public String[] keywords;

		private HashSet<String> keywordSet = new HashSet<String>();

		public KeywordLine(String keywordStr, String value) {
			this.value = value.trim();
			// Log.i("CITYParser","Value: " + this.value);
			// Log.i("CITYParser", "Keywords: " + keywordStr);

			if (!value.equals("")) {
				if (!keywordStr.equals("")) {
					this.keywords = keywordStr.split(";");
					for (String keyword : this.keywords) {
						keyword = keyword.trim();
						if (!keyword.equals(""))
							this.keywordSet.add(keyword);
						// Log.i("CITYParser","   Keyword: " + keyword);
					}
				} else {
					this.keywords = new String[0];
				}
			} else {
				this.keywords = new String[0];
			}
		}

		public boolean matches(HashSet<String> matchWords, boolean matchEmpty) {
			// Log.i("CITYParser", "Check: " + this.value);

			if (matchWords.contains("All")) {
				// Log.i("CITYParser", "  ALL");
				return true;
			}

			if (keywordSet.size() == 0 && !matchEmpty) {
				// Log.i("CITYParser", "  EMPTY & NOT MATCHED");
				return false;
			}

			for (String keyword : keywordSet) {
				if (!matchWords.contains(keyword)) {
					// Log.i("CITYParser", "BAD KEYWORD: " + keyword);
					return false;
				}
			}

			return true;
		}
	}
}
