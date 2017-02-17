package edu.neu.android.wocketslib.emasurvey.model;

import java.util.Comparator;
import java.util.StringTokenizer;

public class QuestionComparator implements Comparator<SurveyQuestion> {
	private static final int ID_LEVELS = 4;
	private static final int MAX_RANGE = 20;

	@Override
	public int compare(SurveyQuestion lhs, SurveyQuestion rhs) {
		int[] lhsIDs = parseID(lhs.getQuestionId());
		int[] rhsIDs = parseID(rhs.getQuestionId());
		int lhs_idIndicator = calculateID(lhsIDs);
		int rhs_idIndicator = calculateID(rhsIDs);
		if (lhs_idIndicator != rhs_idIndicator)
			return lhs_idIndicator > rhs_idIndicator ? 1 : -1;
		else
			return 0;
	}

	public static int[] parseID(String id) {
		int[] ids = new int[ID_LEVELS];
		id = id.substring(1);
		StringTokenizer tokenizer = new StringTokenizer(id, "_");
		ids[0] = Integer.parseInt(tokenizer.nextToken());
		String idtoken;
		int index = 1;
		while ((idtoken = tokenizer.nextToken()).length() == 1) {
			ids[index] = idtoken.charAt(0) - 'a' + 1;
			index++;
		}
		if (tokenizer.hasMoreTokens())
			ids[ID_LEVELS - 1] = Integer.parseInt(tokenizer.nextToken());
		return ids;
	}

	public static int calculateID(int[] ids) {
		if (ids.length != ID_LEVELS)
			return 0;
		int id = 0;
		if (ids[ID_LEVELS - 1] != 0)
			id += ids[ID_LEVELS - 1] * Math.pow(MAX_RANGE, ID_LEVELS - 1);
		else
			id += Math.pow(MAX_RANGE, ID_LEVELS);
		for (int i = 0; i < ID_LEVELS - 1; i++) {
			id += ids[i] * Math.pow(MAX_RANGE, ID_LEVELS - i - 1);
		}
		return id;
	}

}
