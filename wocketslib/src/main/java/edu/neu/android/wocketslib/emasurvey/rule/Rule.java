package edu.neu.android.wocketslib.emasurvey.rule;

public class Rule {
	protected RULE_TYPE type;
	protected double probability;
	protected double randomNum;

	public enum RULE_TYPE {
		QUES_AS_SEQUENTIAL, QUES_FROM_ANS, CHANCE_CHOSEN, QUES_AS_GROUP, SWAP_QUES_CONTEXT
	}

	public Rule(RULE_TYPE type, double probability) {
		this.type = type;
		this.probability = probability;
		this.randomNum = -1;
	}

	public Rule(Rule sourceRule) {
		this(sourceRule.type, sourceRule.probability);
	}

	public RULE_TYPE getType() {
		return type;
	}

	public void setType(RULE_TYPE type) {
		this.type = type;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public boolean isChosen(double randomN) {
		this.randomNum = randomN;
		// Log.d("SurveyRule", "Apply rule: "+this);
		return randomNum < probability;
	}
}
