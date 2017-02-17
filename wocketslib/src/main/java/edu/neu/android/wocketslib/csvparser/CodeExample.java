package edu.neu.android.wocketslib.csvparser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import edu.neu.android.wocketslib.utils.Log;


public class CodeExample {

	public void exampleBasicUsage() {

		AppStringParser parser = new AppStringParser("/sdcard/test.csv");

		try {
			parser.load();
		} catch (FileNotFoundException e) {
			Log.e("CSVParser", "File not found");
			return;
		} catch (IOException e) {
			Log.e("CSVParser", "IO EXCEPTION!");
			return;
		}

		Log.i("CSVParser", "Main");
		printStrArr(parser.getSection("Main title"));

		Log.i("CSVParser", "Other");
		printStrArr(parser.getSection("Other title"));

		Log.i("CSVParser", "Match");
		printStrArr(parser.matchKeywords("Other title stuff", Arrays
				.asList("A"), true));
		Log.i("CSVParser", "Match");
		printStrArr(parser.matchKeywords("Other title stuff", Arrays.asList(
				"A", "B", "D"), true));
		Log.i("CSVParser", "Match");
		printStrArr(parser.matchKeywords("Other title stuff", Arrays.asList(
				"A", "B", "C"), true));
		Log.i("CSVParser", "Match");
		printStrArr(parser.matchKeywords("Other title stuff", Arrays.asList(
				"B", "C"), true));
	}

	public void globalParserExample() {

		// The global content parser takes in a format parameter
		// You can specify your own, or use the built-in versions defined in
		// GlobalCSVFormat
		GlobalContentParser parser = new GlobalContentParser(
				"/sdcard/test.csv", GlobalCSVFormat.v0);

		try {
			List<GlobalRow> parsedRows = parser.load();

			// we can do something with parsedRows manually, or we can do some
			// filtered searches
			// NOTE: this method is currently unoptimized and thus kinda slow
			List<GlobalRow> filterResults = parser.filterByKeyword(
					"SomeColumn", new String[] { "keyword1", "keyword2" });

			// If we want to actuall examine the contents of the row, we look
			// them up by column name
			GlobalRow row = filterResults.get(0);
			// Note that we have to remember the type of data stored in that
			// particular row:
			String[] someKeywords = row.getKeywords("SomeKeywordsColumn");
			Date myDate = row.getDate("SomeDateColumn");
		} catch (FileNotFoundException e) {
			Log.e("CSVParser", "File not found");
			return;
		} catch (IOException e) {
			Log.e("CSVParser", "IO EXCEPTION!");
			return;
		}

	}

	private void printStrArr(String[] arr) {
		if (arr == null) {
			Log.i("CSVParser", "NULL");
			return;
		}
		Log.i("CSVParser", "Count: " + arr.length);
		for (String str : arr) {
			Log.i("CSVParser", str);
		}
	}
}
