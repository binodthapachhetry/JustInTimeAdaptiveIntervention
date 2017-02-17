package edu.neu.mhealth.android.wockets.library.support;

import java.util.List;

import edu.neu.mhealth.android.wockets.library.database.entities.study.Text;

/**
 * @author Dharam Maniar
 */

public class WocketsUtil {

	private static final String TAG = "WocketsUtil";

    /**
     * Utility function to convert a list of Strings to a String array
     *
     * @param listOfStrings     A list of String values
     * @return  A string array with the values
     */
    public static String[] listOfStringsToStringArray(List<String> listOfStrings) {
        // http://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
        return listOfStrings.toArray(new String[0]);
    }

	public static String listOfStringsToString(List<String> listOfStrings) {
		StringBuilder sb = new StringBuilder();

		for(String s: listOfStrings) {
			sb.append(s).append(',');
		}

		sb.deleteCharAt(sb.length()-1); //delete last comma

		return sb.toString();
	}

	public static String getStringFromListofTextForLanguage(Text text, String language) {
		switch (language) {
			case "english":
				return text.english;
			case "spanish":
				if (text.spanish != null) {
					return text.spanish;
				}
			default:
				return text.english;
		}
	}
}
