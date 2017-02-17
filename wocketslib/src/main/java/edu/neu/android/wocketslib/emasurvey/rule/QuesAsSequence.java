package edu.neu.android.wocketslib.emasurvey.rule;

import java.util.ArrayList;
import java.util.Iterator;

import edu.neu.android.wocketslib.emasurvey.model.SurveyQuestion;

public class QuesAsSequence extends Rule {
	private ArrayList<SurveyQuestion> possibleNextQuestions;

	public QuesAsSequence(ArrayList<SurveyQuestion> possibleNextQuestions) {
		super(RULE_TYPE.QUES_AS_SEQUENTIAL, 1);
		this.possibleNextQuestions = possibleNextQuestions;
	}

	public QuesAsSequence(QuesAsSequence sourceRule) {
		super(sourceRule);
		this.possibleNextQuestions = new ArrayList<SurveyQuestion>();
		for (Iterator<SurveyQuestion> iterator = sourceRule.possibleNextQuestions.iterator(); iterator.hasNext();) {
			SurveyQuestion oldQues = (SurveyQuestion) iterator.next();
			SurveyQuestion clonedQues = new SurveyQuestion(oldQues, oldQues.getQuestionId());
			this.possibleNextQuestions.add(clonedQues);
		}
	}

	@Override
	public String toString() {
		String tostring = "QuesAsSequence [possibleNextQuestions= (";
		for (SurveyQuestion ques : possibleNextQuestions) {
			tostring += ques.getQuestionId() + " ";
		}
		tostring += "), probability threshold=" + probability + ", random number=" + randomNum + "]";
		return tostring;
	}

	public ArrayList<SurveyQuestion> getPossibleNextQuestions() {
		return possibleNextQuestions;
	}

	public void setPossibleNextQuestions(ArrayList<SurveyQuestion> possibleNextQuestions) {
		this.possibleNextQuestions = possibleNextQuestions;
	}

}
