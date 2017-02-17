package edu.neu.mhealth.android.wockets.library.database.entities.study;

/**
 * @author Dharam Maniar
 */
public class Answer {

	public String key;

	public Text text;

	public boolean isPostpone;

    public int postponeTime;

	public Answer() {
	}

	public Answer(String key, Text text, boolean isPostpone, int postponeTime) {
		this.key = key;
		this.text = text;
        this.isPostpone = isPostpone;
        this.postponeTime = postponeTime;
	}

	public Answer setKey(String key) {
		this.key = key;
		return this;
	}

	public Answer setText(Text text) {
		this.text = text;
		return this;
	}

	public Answer setIsPostpone(boolean isPostpone) {
		this.isPostpone = isPostpone;
		return this;
	}

	public Answer setPostponeTime(int postponeTime) {
		this.postponeTime = postponeTime;
		return this;
	}

	public Answer createAnswer() {
		return new Answer(key, text, isPostpone, postponeTime);
	}
}
