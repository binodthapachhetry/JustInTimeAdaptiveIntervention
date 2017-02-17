package edu.neu.android.wocketslib.activities.paema.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import au.com.bytecode.opencsv.CSVReader;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class StaticDataStore {
	private static final String TAG = "StaticDataStore";
	
	// a handle to the application's resources
	private static Resources resources;

	public static String fileName = "activityinfo.csv";

	private static String BLANK = "";
	private static StaticDataStore staticDataStoreInst;

	private ArrayList<PhysicalActivity> physicalActivities;
	private ArrayList<String> titles;
	private ArrayList<String> phrases;

	private boolean storeIntialized = false;
	private int randomNumber;

	private StaticDataStore() {
	}

	public void setStoreToUnintialized() {
		storeIntialized = false;
	}

	public String getTitle() {
		if (titles != null)
			return titles.get(randomNumber % titles.size());

		titles = new ArrayList<String>();
		try {
			ArrayList<String[]> rows = getRows("Title", "Title information");
			if (rows != null && rows.size() > 0) {
				for (int i = 0; i < rows.size(); i++) {
					String title;
					if (rows.get(i).length >= 2)
						title = rows.get(i)[1];
					else
						title = null;
					if (title == null)
						continue;
					title = title.trim();
					if (!BLANK.equals(title))
						titles.add(title);
				}
			} else
				titles.add("Activity Tracker");
		} catch (WOCKETSException e) {
			titles.add("Activity Tracker"); // Add default title
		}

		return titles.get(randomNumber % titles.size());
	}

	public String getPhrase() {
		if (phrases != null)
			return phrases.get(randomNumber % phrases.size());

		phrases = new ArrayList<String>();
		try {
			ArrayList<String[]> rows = getRows("Phrase", "Phrase information");
			if (rows != null && rows.size() > 0) {
				for (int i = 0; i < rows.size(); i++) {
					String phrase;
					if (rows.get(i).length >= 2)
						phrase = rows.get(i)[1];
					else
						phrase = null;
					if (phrase == null)
						continue;
					phrase = phrase.trim();
					if (!BLANK.equals(phrase))
						phrases.add(rows.get(i)[1]);
				}
			} else
				phrases.add("were you (check all)...");
		} catch (WOCKETSException e) {
			phrases.add("did you do any of this?"); // Add default title
		}

		String phrase = phrases.get(randomNumber % phrases.size());
		return phrase.replaceAll("[^\\p{ASCII}]", "");
	}

	public ArrayList<PhysicalActivity> getAvailablePhysicalActivities() throws WOCKETSException {
		if (physicalActivities != null)
			return physicalActivities;

		physicalActivities = new ArrayList<PhysicalActivity>();
		ArrayList<String[]> rows = getRows("Activity", "Activity information");
		if (rows == null || rows.size() == 0)
			throw new WOCKETSException(TAG, "Activity information not found");

		for (int i = 0; i < rows.size(); i++) {
			String[] columns = rows.get(i);
			if (columns.length < 3)
				continue;
			if (BLANK.equals(columns[1]) && BLANK.equals(columns[2]) && ((columns.length < 4) || (BLANK.equals(columns[3]))))
				continue;

			String name = columns[2];
			if (name == null)
				continue;

			// sometime name has trailing blanks
			name = name.trim();
			if (BLANK.equals(name))
				continue;

			PhysicalActivity physicalActivity = new PhysicalActivity();
			physicalActivity.setKeyWord(columns[1]);
			physicalActivity.setName(name);
			if (columns.length > 3) {
				physicalActivity.setFollowupQuestion(columns[3]);
				ArrayList<String> followupAnswers = new ArrayList<String>();
				int index = 4;
				while (true) {
					if (columns.length <= index || columns[index] == null || BLANK.equals(columns[index]))
						break;
					followupAnswers.add(columns[index]);
					index++;
				}
				physicalActivity.setFollowupAnswers(followupAnswers);
			}
			physicalActivities.add(physicalActivity);
		}

		return physicalActivities;
	}

	public String getCongratulationMsg(String keyWord, int time) {
		ArrayList<String> congragulationMsgs = new ArrayList<String>();
		try {
			ArrayList<String[]> rows = getRows("Notes", "");
			if (rows != null && rows.size() > 0) {
				for (int i = 0; i < rows.size(); i++) {
					String[] columns = rows.get(i);
					if (BLANK.equals(columns[4]))
						continue;
					if (columns[1] == null || columns[1].equals("") || columns[1].contains(keyWord)) {
						int minMinutes = 0;
						int maxMinutes = 100000;
						if (columns[2] != null && !BLANK.equals(columns[2]))
							minMinutes = Integer.parseInt(columns[2]);
						if (columns[3] != null && !BLANK.equals(columns[3]))
							maxMinutes = Integer.parseInt(columns[3]);
						if (time >= minMinutes && time <= maxMinutes)
							congragulationMsgs.add(columns[4]);
					}
				}
			} else
				congragulationMsgs.add("Congratulations");
		} catch (WOCKETSException e) {
			congragulationMsgs.add("Congratulations"); // Add default message
		}

		if ((congragulationMsgs != null) && (congragulationMsgs.size() != 0) && (randomNumber != 0))
			return congragulationMsgs.get(randomNumber % congragulationMsgs.size());
		else
			return ""; // TODO
	}

	private String[] trimRow(String[] someRows) {
		String[] newRow = new String[someRows.length];
		for (int i = 0; i < someRows.length; i++)
			newRow[i] = someRows[i].trim();
		return newRow;
	}

	// private ArrayList<String[]> getRows(String dataType, String dataTypeDesc)
	// throws WOCKETSException {
	// InputStream is = null;
	// try {
	// is = FileHelper.openFileForRead(pathToFile, dataTypeDesc, true);
	// CSVReader reader = new CSVReader(new InputStreamReader(is));
	//
	// ArrayList<String[]> rows = new ArrayList<String[]>(10);
	// String[] row;
	// while ((row = reader.readNext()) != null) {
	//
	// row = trimRow(row);
	// if (row[0] == null || BLANK.equals(row[0])
	// || !row[0].startsWith(dataType))
	// continue;
	// // Start of data that is of type as in datatype variable
	// while ((row = reader.readNext()) != null) {
	// if ((row[0] != null) && !(BLANK.equals(row[0])))
	// break;
	// if (row.length > 1)
	// rows.add(row);
	// }
	// break;
	// }
	// return rows;
	// } catch (IOException ex) {
	// throw new WOCKETSException("Error in reading");
	// } finally {
	// if (is != null)
	// try {
	// is.close();
	// } catch (IOException e) {
	// }
	// }
	// }

	private ArrayList<String[]> getRows(String dataType, String dataTypeDesc) throws WOCKETSException {
		InputStream is = null;
		try {
			is = resources.getAssets().open(fileName);
			CSVReader reader = new CSVReader(new InputStreamReader(is));

			ArrayList<String[]> rows = new ArrayList<String[]>(10);
			String[] row;
			while ((row = reader.readNext()) != null) {

				row = trimRow(row);
				if (row[0] == null || BLANK.equals(row[0]) || !row[0].startsWith(dataType))
					continue;
				// Start of data that is of type as in datatype variable
				while ((row = reader.readNext()) != null) {
					if ((row[0] != null) && !(BLANK.equals(row[0])))
						break;
					if (row.length > 1)
						rows.add(row);
				}
				break;
			}
			return rows;
		} catch (IOException ex) {
			throw new WOCKETSException(TAG, "Error in reading");
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	public boolean init(Context aContext) {

		// get the application's resources
		resources = aContext.getResources();

		if (storeIntialized)
			return true;

		// if (!FileHelper.fileExists(pathToFile))
		// return false;

		physicalActivities = null;
		phrases = titles = null;

		randomNumber = ((int) (Math.random() * 100));
		storeIntialized = true;
		return true;
	}

	public static synchronized StaticDataStore getInst() {
		if (staticDataStoreInst == null)
			staticDataStoreInst = new StaticDataStore();
		return staticDataStoreInst;
	}

}
