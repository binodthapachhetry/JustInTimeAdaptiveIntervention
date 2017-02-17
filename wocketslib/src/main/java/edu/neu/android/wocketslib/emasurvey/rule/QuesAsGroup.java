package edu.neu.android.wocketslib.emasurvey.rule;

import java.util.ArrayList;
import java.util.Iterator;

import edu.neu.android.wocketslib.emasurvey.model.SurveyQuestion;

public class QuesAsGroup extends Rule {
	private ArrayList<SurveyQuestion> ingroupQuestions;

	public QuesAsGroup(ArrayList<SurveyQuestion> ingroupQuestions) {
		super(RULE_TYPE.QUES_AS_GROUP, 1);
		this.ingroupQuestions = ingroupQuestions;
	}

	public QuesAsGroup(QuesAsGroup sourceRule) {
		super(sourceRule);
		this.ingroupQuestions = new ArrayList<SurveyQuestion>();
		for (Iterator<SurveyQuestion> iterator = sourceRule.ingroupQuestions.iterator(); iterator.hasNext();) {
			SurveyQuestion oldQues = (SurveyQuestion) iterator.next();
			SurveyQuestion clonedQues = new SurveyQuestion(oldQues, oldQues.getQuestionId());
			this.ingroupQuestions.add(clonedQues);
		}
	}

	@Override
	public String toString() {
		String tostring = "QuesAsGroup [ingroupQuestions= (";
		for (SurveyQuestion ques : ingroupQuestions) {
			tostring += ques.getQuestionId() + " ";
		}
		tostring += "), probability threshold=" + probability + ", random number=" + randomNum + "]";
		return tostring;
	}

	public ArrayList<SurveyQuestion> getIngroupQuestions() {
		return ingroupQuestions;
	}

	public void setIngroupQuestions(ArrayList<SurveyQuestion> ingroupQuestions) {
		this.ingroupQuestions = ingroupQuestions;
	}

}
