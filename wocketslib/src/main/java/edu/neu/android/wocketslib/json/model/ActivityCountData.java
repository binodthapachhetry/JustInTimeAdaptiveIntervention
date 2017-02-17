package edu.neu.android.wocketslib.json.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ActivityCountData {

	public static final int UNDEFINED_INT = -1;

	/**
	 * The serialized names in this class are short to minimize the length of
	 * the json string. We sacrifice some readability but this may improve
	 * performance on the phone.
	 */

	@SerializedName("mac")
	public String macID;

	@SerializedName("time")
	public Date createTime;

	@SerializedName("otime")
	public Date originalTime;

	@SerializedName("ac")
	public int activityCount;

	public ActivityCountData() {
		macID = null;
		createTime = null;
		originalTime = null;
		activityCount = UNDEFINED_INT;
	}
}
