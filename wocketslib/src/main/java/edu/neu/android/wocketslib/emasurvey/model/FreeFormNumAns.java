package edu.neu.android.wocketslib.emasurvey.model;

public class FreeFormNumAns extends SurveyAnswer {
	private String freeFormDescription;
	private int[] ansRange;

	public FreeFormNumAns(int id, String answerText) {
		super(id, answerText);
		this.freeFormDescription = null;
		this.ansRange = null;
	}

	public FreeFormNumAns(FreeFormNumAns sourceAns) {
		super(sourceAns);
		if (((FreeFormNumAns) sourceAns).freeFormDescription != null)
			this.freeFormDescription = ((FreeFormNumAns) sourceAns).freeFormDescription;
		if (((FreeFormNumAns) sourceAns).ansRange != null) {
			this.ansRange = new int[((FreeFormNumAns) sourceAns).ansRange.length];
			this.ansRange[0] = ((FreeFormNumAns) sourceAns).ansRange[0];
			this.ansRange[1] = ((FreeFormNumAns) sourceAns).ansRange[1];
		}
	}

	@Override
	public String toString() {
		String ans = "[SurveyAnswer id: " + id + ", Text: \"";
		if (this.freeFormDescription != null)
			ans += freeFormDescription + " - ";
		ans += text + "\"]";
		return ans;
	}

	public String getFreeFormDescription() {
		return freeFormDescription;
	}

	public void setFreeFormDescription(String freeFormDescription) {
		this.freeFormDescription = freeFormDescription;
	}

	public int[] getAnsRange() {
		return ansRange;
	}

	public void setAnsRange(int[] ansRange) {
		this.ansRange = ansRange;
	}

}
