package edu.neu.android.wocketslib.emasurvey.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SurveyAnswer {
	protected final static long NOT_INIT = -1;
	protected int id;
	protected String text;
	protected boolean isSelected;
	protected ArrayList<SurveyQuestion> nextQues;
	protected long randomSeed;

	public SurveyAnswer(int id, String answerText) {
		super();
		this.id = id;
		this.text = answerText;
		this.isSelected = false;
//		QuestionComparator comparator = new QuestionComparator();
		this.nextQues = new ArrayList<SurveyQuestion>();
		this.randomSeed = NOT_INIT;
	}

	public SurveyAnswer(SurveyAnswer sourceAns) {
		this(sourceAns.id, sourceAns.text);
		this.nextQues = new ArrayList<SurveyQuestion>();
		if (!sourceAns.nextQues.isEmpty()) {
			for (Iterator<SurveyQuestion> iterator = sourceAns.nextQues.iterator(); iterator.hasNext();) {
				SurveyQuestion oldQues = (SurveyQuestion) iterator.next();
				SurveyQuestion clonedQues = new SurveyQuestion(oldQues, oldQues.getQuestionId());
				this.nextQues.add(clonedQues);
			}
		}
	}

	public ArrayList<SurveyQuestion> getNextQues() {
		return nextQues;
	}

	public void setNextQues(ArrayList<SurveyQuestion> nextQues) {
		this.nextQues = nextQues;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		if (isSelected && !isSeedSet()) {
			setRandomSeed();
		}
	}

	public int getId() {
		return id;
	}

	public String getAnswerText() {
		return text;
	}

	public double getRandomProb(int id) {
		if (randomSeed == NOT_INIT)
			return 1;
		else
			return new Random(randomSeed + id * 13 * 13 * 13).nextDouble();
	}

	public void setRandomSeed() {
		this.randomSeed = System.currentTimeMillis();
	}

	public boolean isSeedSet() {
		return this.randomSeed != NOT_INIT;
	}

	@Override
	public String toString() {
		return "[SurveyAnswer id: " + id + ", Text: \"" + text + "\"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + id;
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
		SurveyAnswer other = (SurveyAnswer) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	public void setText(String text) {
		this.text = text;
	}

}
