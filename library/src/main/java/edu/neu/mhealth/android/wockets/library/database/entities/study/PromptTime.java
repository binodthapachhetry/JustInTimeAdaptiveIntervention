package edu.neu.mhealth.android.wockets.library.database.entities.study;

/**
 * @author Dharam Maniar
 */
public class PromptTime {

	public String key;

	public String type;

	public String startTime;

	public String endTime;

	public PromptTime() {}

	public PromptTime(String key, String type, String startTime, String endTime) {
		this.key = key;
		this.type = type;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public PromptTime setKey(String key) {
		this.key = key;
		return this;
	}

	public PromptTime setType(String type) {
		this.type = type;
		return this;
	}

	public PromptTime setStartTime(String startTime) {
		this.startTime = startTime;
		return this;
	}

	public PromptTime setEndTime(String endTime) {
		this.endTime = endTime;
		return this;
	}

	public PromptTime createPromptTime() {
		return new PromptTime(key, type, startTime, endTime);
	}
}
