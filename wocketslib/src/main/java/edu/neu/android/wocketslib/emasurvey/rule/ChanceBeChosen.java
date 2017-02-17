package edu.neu.android.wocketslib.emasurvey.rule;

public class ChanceBeChosen extends Rule {
	public ChanceBeChosen(double probability) {
		super(RULE_TYPE.CHANCE_CHOSEN, probability);
		// TODO Auto-generated constructor stub
	}

	public ChanceBeChosen(ChanceBeChosen sourceRule) {
		super(sourceRule);
	}

	@Override
	public String toString() {
		return "ChanceBeChosen [probability threshold=" + probability + ", random number=" + randomNum + "]";
	}

}
