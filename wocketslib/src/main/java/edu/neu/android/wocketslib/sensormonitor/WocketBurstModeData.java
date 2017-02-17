package edu.neu.android.wocketslib.sensormonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class WocketBurstModeData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String INTENT_ACTION_WOCKET_DATA = "_INTENT_WOCKET_DATA";
	private double[][] data;
	private String MacID;
	private Date lastConnectionTime;
	private long missingData;

	public Date getLastConnectionTime() {
		return lastConnectionTime;
	}

	public void setLastConnectionTime(Date lastConnectionTime) {
		this.lastConnectionTime = lastConnectionTime;
	}

	public long getMissingData() {
		return missingData;
	}

	public void setMissingData(long iS_missingData) {
		missingData = iS_missingData;
	}

	public WocketBurstModeData(String macID, ArrayList<AccelPoint> accelPoints) {
		super();
		MacID = macID;
		setData(accelPoints);
	}

	public WocketBurstModeData(String macID, ArrayList<AccelPoint> accelPoints,
			long missingPackets, Date lastConnectionTime) {
		super();
		MacID = macID;
		setDataAddRecordTime(accelPoints);
		missingData = missingPackets;
		this.lastConnectionTime = lastConnectionTime;
	}

	private void setDataAddRecordTime(ArrayList<AccelPoint> accelPoints) {
		data = new double[4][accelPoints.size()];
		for (int i = 0; i < accelPoints.size(); i++) {
			AccelPoint accelData = accelPoints.get(i);
			data[0][i] = accelData.getmX();
			data[1][i] = accelData.getmY();
			data[2][i] = accelData.getmZ();
			data[3][i] = accelData.mWocketRecordedTime.getTimeInMillis();
		}
	}

	private void setData(ArrayList<AccelPoint> accelPoints) {
		data = new double[4][accelPoints.size()];
		for (int i = 0; i < accelPoints.size(); i++) {
			AccelPoint accelData = accelPoints.get(i);
			data[0][i] = accelData.getmX();
			data[1][i] = accelData.getmY();
			data[2][i] = accelData.getmZ();
		}
	}

	public String getMacID() {
		return MacID;
	}

	public double[][] getData() {
		return data;
	}
}
