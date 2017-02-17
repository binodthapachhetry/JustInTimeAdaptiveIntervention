package edu.neu.android.wocketslib.emasurvey.model;

import java.util.ArrayList;
import java.util.Random;

import edu.neu.android.wocketslib.emasurvey.rule.ChanceBeChosen;
import edu.neu.android.wocketslib.emasurvey.rule.QuesAsGroup;
import edu.neu.android.wocketslib.emasurvey.rule.QuesAsSequence;
import edu.neu.android.wocketslib.emasurvey.rule.QuesFromAns;
import edu.neu.android.wocketslib.emasurvey.rule.Rule;
import edu.neu.android.wocketslib.emasurvey.rule.Rule.RULE_TYPE;

public class SurveyQuestion {
	public static final String NO_DATA = "No_Data";
	private static final int NOT_INIT = -1;
	private String questionText;
	private String questionId;
	private String aliasID;
	private String defaultPrevQuestionID;
	private String defaultNextQuestionID;
	private String prevQuestionID;
	private String nextQuestionID;
	private SurveyAnswer[] answers;
	private ArrayList<Rule> rules;
	private TYPE questionTYPE;
	private boolean isMainActivity;
	private boolean isSkip;
	private long randomSeed;

	public enum TYPE {
		SINGLE_CHOICE, MULTI_CHOICE, NUMBER_RANGE_SELECTER, FREE_FORM_TEXT, MINUTES_PICKER, TIME_PICKER,
        MESSAGE
	}

	public SurveyQuestion(String questionId, String questionText, TYPE questionTYPE) {
		super();
		this.questionText = questionText;
		this.questionId = questionId;
		this.questionTYPE = questionTYPE;
		this.aliasID = questionId;
		reset();
	}
	public SurveyQuestion(String questionId, String alias,String questionText, TYPE questionTYPE) {
		super();
		this.questionText = questionText;
		this.questionId = questionId;
		this.questionTYPE = questionTYPE;
		this.aliasID = alias;
		reset();
	}

	public SurveyQuestion(SurveyQuestion sourceQues, String id){
		this(id,sourceQues.aliasID, sourceQues.questionText, sourceQues.questionTYPE);
		this.answers = new SurveyAnswer[sourceQues.answers.length];
		for (int i = 0; i < sourceQues.answers.length; i++) {
			if (sourceQues.answers[i] != null) {
				if (sourceQues.answers[i] instanceof FreeFormNumAns)
					this.answers[i] = new FreeFormNumAns((FreeFormNumAns) sourceQues.answers[i]);
				else
					this.answers[i] = new SurveyAnswer(sourceQues.answers[i]);
			}
		}
		this.isMainActivity = sourceQues.isMainActivity;
		for (Rule rule : sourceQues.rules) {
			switch (rule.getType()) {
			case QUES_AS_SEQUENTIAL:
				this.rules.add(new QuesAsSequence((QuesAsSequence) rule));
				break;
			case QUES_FROM_ANS:
				this.rules.add(new QuesFromAns((QuesFromAns) rule));
				break;
			case CHANCE_CHOSEN:
				this.rules.add(new ChanceBeChosen((ChanceBeChosen) rule));
				break;
			case QUES_AS_GROUP:
				this.rules.add(new QuesAsGroup((QuesAsGroup) rule));
				break;
			}
		}
	}

	public void setDefault(String defaultprevQuestionID, SurveyAnswer[] answers) {
		this.answers = answers;
		this.defaultPrevQuestionID = defaultprevQuestionID;
	}

	public void reset() {
		rules = new ArrayList<Rule>();
		this.prevQuestionID = NO_DATA;
		this.nextQuestionID = NO_DATA;
		this.isSkip = false;
		this.randomSeed = NOT_INIT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((defaultPrevQuestionID == null) ? 0 : defaultPrevQuestionID.hashCode());
		result = prime * result + (isMainActivity ? 1231 : 1237);
		result = prime * result + ((questionId == null) ? 0 : questionId.hashCode());
		result = prime * result + ((questionTYPE == null) ? 0 : questionTYPE.hashCode());
		result = prime * result + ((questionText == null) ? 0 : questionText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyQuestion other = (SurveyQuestion) obj;
		if (defaultPrevQuestionID == null) {
			if (other.defaultPrevQuestionID != null)
				return false;
		} else if (!defaultPrevQuestionID.equals(other.defaultPrevQuestionID))
			return false;
		if (isMainActivity != other.isMainActivity)
			return false;
		if (questionId == null) {
			if (other.questionId != null)
				return false;
		} else if (!questionId.equals(other.questionId))
			return false;
		if (questionTYPE != other.questionTYPE)
			return false;
		if (questionText == null) {
			if (other.questionText != null)
				return false;
		} else if (!questionText.equals(other.questionText))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SurveyQuestion [questionId=" + questionId + "]";
	}

	public String toDetailedString() {
		SurveyAnswer selectedAns = null;
		String questionDetail = "[Question: \"" + questionText + "\", QuestionID: " + questionId + ", QuestionTYPE: " + questionTYPE + ", ";
		for (int i = 0; i < answers.length; i++) {
			if (answers[i] != null && answers[i].isSelected())
				selectedAns = answers[i];
		}
		for (int i = 0; i < rules.size(); i++) {
			questionDetail += rules.get(i).toString();
		}
		if (selectedAns == null)
			questionDetail += "Question skipped.";
		else
			questionDetail += "Answers: " + selectedAns.toString();
		questionDetail += "]";
		return questionDetail;
	}

	// Getters and Setters
	public double getRandomProb() {
		if (randomSeed == NOT_INIT)
			return 1;
		else
			return new Random(randomSeed).nextDouble();
	}

	public void setRandomSeed() {
		this.randomSeed = System.currentTimeMillis() + (questionId.charAt(1) + questionId.charAt(2)) * 3 * 5 * 7 * 11;
	}

	public boolean isSeedSet() {
		return this.randomSeed != NOT_INIT;
	}

	public ArrayList<Rule> getRuleByType(RULE_TYPE type) {
		ArrayList<Rule> targetRule = new ArrayList<Rule>();
		if (!rules.isEmpty()) {
			for (Rule rule : rules) {
				if (rule.getType() == type)
					targetRule.add(rule);
			}
		}
		return targetRule;
	}

	public boolean isSkip() {
		return isSkip;
	}

	public String getDefaultNextQuestionID() {
		return defaultNextQuestionID;
	}

	public void setDefaultNextQuestionID(String defaultNextQuestionID) {
		this.defaultNextQuestionID = defaultNextQuestionID;
	}

	public String getDefaultPrevQuestionID() {
		return defaultPrevQuestionID;
	}

	public void setDefaultPrevQuestionID(String defaultprevQuestionID) {
		this.defaultPrevQuestionID = defaultprevQuestionID;
	}

	public void setSkip(boolean isSkip) {
		this.isSkip = isSkip;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public boolean isMainActivity() {
		return isMainActivity;
	}

	public void setMainActivity(boolean isMainActivity) {
		this.isMainActivity = isMainActivity;
	}

	public String getQuestionText() {
		return questionText;
	}

	public String getQuestionId() {
		return questionId;
	}

	public TYPE getQuestionTYPE() {
		return questionTYPE;
	}

	public String getPrevQuestionID() {
		return prevQuestionID;
	}

	public void setPrevQuestionID(String prevQuestionID) {
		this.prevQuestionID = prevQuestionID;
	}

	public String getNextQuestionID() {
		return nextQuestionID;
	}

	public void setNextQuestionID(String nextQuestionID) {
		this.nextQuestionID = nextQuestionID;
	}

	public SurveyAnswer[] getAnswers() {
		return answers;
	}

	public void setAnswers(SurveyAnswer[] answers) {
		this.answers = answers;
	}

	public ArrayList<Rule> getRules() {
		return rules;
	}

	public void addRules(Rule rule) {
		this.rules.add(rule);
	}
	public String getAliasID() {
		return aliasID;
	}
	public void setAliasID(String aliasID) {
		this.aliasID = aliasID;
	}
}
