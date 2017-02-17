package edu.neu.android.wocketslib.emasurvey.model;

import java.util.ArrayList;

public abstract class QuestionSet {
	public static final String TAG = "QuestionSet";
	public static final String mainActivity = "$$$";

	protected abstract void setQuestions();

	public abstract int getQuestionNum();

	public abstract ArrayList<SurveyQuestion> getDefaultQuestionSet();

	public abstract String getReadableQuestionSetName();

	public abstract String[] getAllQuestionIDs();

	public QuestionSet() {
	}

	public QuestionSet(QuestionSetParamHandler param) {
	}

	public static SurveyQuestion getQuestionByID(ArrayList<SurveyQuestion> defaultQuestionSet, String ID) {
		if (defaultQuestionSet != null && defaultQuestionSet.size() > 0) {
			for (SurveyQuestion question : defaultQuestionSet) {
				if (question.getQuestionId().equals(ID))
					return question;
			}
		}
		return null;
	}

	/**
	 * @return the ID of the first question
	 */
	public static String findFirstQuestionID(ArrayList<SurveyQuestion> defaultQuestionSet) {
		if (defaultQuestionSet == null)
			return SurveyQuestion.NO_DATA;
		for (SurveyQuestion question : defaultQuestionSet) {
			if (question.getDefaultPrevQuestionID().equals(SurveyQuestion.NO_DATA))
				return question.getQuestionId();
		}
		return SurveyQuestion.NO_DATA;
	}

}
