package edu.neu.android.wocketslib.json.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SwapEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	public int swappingId;

	@SerializedName("uTime")
	public Date uploadTime;

	@SerializedName("swap")
	public boolean isSwap;

	@SerializedName("restart")
	public boolean isRestarted;

	@SerializedName("loc")
	public boolean isLocationChange;

	@SerializedName("sTime")
	public Date swapTime;

	@SerializedName("someSwappedSensors")
	public List<SwappedSensor> swappedSensor;

	public SwapEvent() {
		super();
		this.uploadTime = null;
		this.swappingId = 0;
		this.isSwap = false;
		this.isRestarted = false;
		this.isLocationChange = false;
		this.swapTime = null;
		this.swappedSensor = new ArrayList<SwappedSensor>();
	}

}
