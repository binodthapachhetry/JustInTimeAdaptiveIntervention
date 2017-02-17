package edu.neu.android.wocketslib.emasurvey.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import edu.neu.android.wocketslib.emasurvey.model.SurveyQuestion;

public class QuesFromAns extends Rule {
	private int[] branchingAns;
	private ArrayList<SurveyQuestion> possibleNextQuestions;

	public QuesFromAns(int[] branchingAns, ArrayList<SurveyQuestion> possibleNextQuestions) {
		this(branchingAns, possibleNextQuestions, 1);
	}

	public QuesFromAns(int[] branchingAns, ArrayList<SurveyQuestion> nextQuestions, double probability) {
		super(RULE_TYPE.QUES_FROM_ANS, probability);
		this.branchingAns = branchingAns;
		this.possibleNextQuestions = nextQuestions;
		this.probability = probability;
	}

	public QuesFromAns(QuesFromAns sourceRule) {
		super(sourceRule);
		this.branchingAns = sourceRule.branchingAns;
		this.possibleNextQuestions = new ArrayList<SurveyQuestion>();
		for (Iterator<SurveyQuestion> iterator = sourceRule.possibleNextQuestions.iterator(); iterator.hasNext();) {
			SurveyQuestion oldQues = (SurveyQuestion) iterator.next();
			SurveyQuestion clonedQues = new SurveyQuestion(oldQues, oldQues.getQuestionId());
			this.possibleNextQuestions.add(clonedQues);
		}
	}

	@Override
	public String toString() {
		String tostring = "QuesFromAns [branchingAns=" + Arrays.toString(branchingAns) + ", possibleNextQuestions= (";
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

	public int[] getBranchingAns() {
		return branchingAns;
	}

	public void setBranchingAns(int[] branchingAns) {
		this.branchingAns = branchingAns;
	}

}
