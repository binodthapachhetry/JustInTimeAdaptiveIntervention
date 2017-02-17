package edu.neu.android.wocketslib.json.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class PromptEvent {

	public static final int UNDEFINED_INT = -1;

	/**
	 * The serialized names in this class are short to minimize the length of
	 * the json string. We sacrifice some readability but this may improve
	 * performance on the phone.
	 */

	@SerializedName("type")
	public String promptType;

	@SerializedName("ptime")
	public Date promptTime;

	@SerializedName("rtime")
	public Date responseTime;

	@SerializedName("int")
	public int activityInterval;

	@SerializedName("pact")
	public String primaryActivity;

	@SerializedName("aact")
	public String alternateActivity;

	public PromptEvent() {
		promptType = null;
		promptTime = null;
		responseTime = null;
		activityInterval = UNDEFINED_INT;
		primaryActivity = null;
		alternateActivity = null;
	}

}
