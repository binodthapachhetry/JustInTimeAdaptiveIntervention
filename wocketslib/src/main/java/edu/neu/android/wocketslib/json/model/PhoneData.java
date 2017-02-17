package edu.neu.android.wocketslib.json.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class PhoneData {

	public static final int UNDEFINED_INT = -1;

	/**
	 * The serialized names in this class are short to minimize the length of
	 * the json string. We sacrifice some readability but this may improve
	 * performance on the phone.
	 */

	@SerializedName("time")
	public Date createTime;

	@SerializedName("bat")
	public int phoneBattery;

	@SerializedName("mem")
	public int mainMemory;

	@SerializedName("sdmem")
	public int sDMemory;

	// TODO enable this with server
	// @SerializedName("ram")
	// public int ram;

	public PhoneData() {
		createTime = null;
		phoneBattery = UNDEFINED_INT;
		mainMemory = UNDEFINED_INT;
		sDMemory = UNDEFINED_INT;
		// ram = UNDEFINED_INT;
	}
}
