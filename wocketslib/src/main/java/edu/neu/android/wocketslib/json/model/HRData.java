package edu.neu.android.wocketslib.json.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class HRData implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int UNDEFINED_INT = -1;

	/**
	 * The serialized names in this class are short to minimize the length of
	 * the json string. We sacrifice some readability but this may improve
	 * performance on the phone.
	 */

	@SerializedName("hid")
	public String hardwareID;

	@SerializedName("time")
	public Date createTime;

	@SerializedName("hr")
	public int heartRate;

	@SerializedName("hbnum")
	public int heartBeatNumber;

	@SerializedName("bat")
	public int battery;

	public HRData() {
		hardwareID = null;
		createTime = null;
		heartRate = UNDEFINED_INT;
		heartBeatNumber = UNDEFINED_INT;
		battery = UNDEFINED_INT;
	}
}
