package edu.neu.android.wocketslib.activities.paema.model;

import java.util.ArrayList;

//This activity is not android activity. It stores data related to physical work
public class PhysicalActivity {
	private String keyWord;
	private String name;
	private String followupQuestion;
	private ArrayList<String> followupAnswers;
	private boolean selected = false;

	private int hours;
	private int minutes;
	private String answer;

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFollowupQuestion() {
		return followupQuestion;
	}

	public void setFollowupQuestion(String followupQuestion) {
		this.followupQuestion = followupQuestion;
	}

	public ArrayList<String> getFollowupAnswers() {
		return followupAnswers;
	}

	public void setFollowupAnswers(ArrayList<String> followupAnswers) {
		this.followupAnswers = followupAnswers;
	}

	public String toString() {
		return getName();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
