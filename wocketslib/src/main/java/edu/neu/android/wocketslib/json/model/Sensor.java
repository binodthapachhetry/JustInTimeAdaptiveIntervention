package edu.neu.android.wocketslib.json.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class Sensor implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int UNDEFINED_INT = -1;

	/**
	 * The serialized names in this class are short to minimize the length of
	 * the json string. We sacrifice some readability but this may improve
	 * performance on the phone.
	 */

	@SerializedName("mac")
	public String macID;

	@SerializedName("col")
	public String color;

	@SerializedName("hver")
	public double hardwareVersion;

	@SerializedName("fver")
	public double firmwareVersion;

	@SerializedName("lab")
	public String label;

	public Sensor() {
		macID = null;
		color = null;
		hardwareVersion = 0.0;
		firmwareVersion = 0.0;
		label = null;
	}
}
