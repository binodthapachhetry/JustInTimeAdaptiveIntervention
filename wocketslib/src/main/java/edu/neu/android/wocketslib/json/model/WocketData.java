package edu.neu.android.wocketslib.json.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class WocketData {

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

	@SerializedName("ac")
	public int activityCount;

	@SerializedName("bat")
	public int wocketBattery;

	@SerializedName("tbyte")
	public int transmittedBytes;

	@SerializedName("rbyte")
	public int receivedBytes;

	public WocketData() {
		macID = null;
		createTime = null;
		activityCount = UNDEFINED_INT;
		wocketBattery = UNDEFINED_INT;
		transmittedBytes = UNDEFINED_INT;
		receivedBytes = UNDEFINED_INT;
	}
}
