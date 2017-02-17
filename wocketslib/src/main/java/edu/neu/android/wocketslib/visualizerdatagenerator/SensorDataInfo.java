package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.util.List;

import android.content.Context;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.utils.Log;

public class SensorDataInfo {
//	private final static int NO_DATA = -1;
	private final static String TAG = "SensorDataInfo";
	private String MacID;
	private String bodyLocation;
//	private int ID;
	public SensorDataInfo() {
		super();
		MacID = null;
		this.bodyLocation = null;
//		ID = NO_DATA;
	}
	// didn't override hashcode(), pay attention!!
	@Override
	public boolean equals(Object other) {
	    if (other == null) 
	    	return false;
	    if (other == this) 
	    	return true;
	    if (other.getClass() != getClass())
	    	return false;
	    SensorDataInfo otherDataInfo = (SensorDataInfo)other;
	    return (this.MacID.equals(otherDataInfo.MacID))
	    		&& (this.bodyLocation.equals(otherDataInfo.bodyLocation));
	}

	@Override
	public String toString() {
		return "sensor: MacID("+this.MacID+") BodyLocation("+this.bodyLocation+")";
	}
	public void setSwappedData(SwappedSensor sensor){
		this.MacID = sensor.macID;
		this.bodyLocation = sensor.bodyLocation.replace(" ", "_");
	}
	public static SensorDataInfo getDataInfoFromMacID(Context c, String MacID){
		List<SwappedSensor> sensors = WocketInfoGrabber.getSwappedSensors(c);
		for (SwappedSensor swappedSensor : sensors) {
			if(swappedSensor.macID.equals(MacID)){
				SensorDataInfo dataInfo = new SensorDataInfo();
				dataInfo.setSwappedData(swappedSensor);
				return dataInfo;
			}
		}
		Log.e(TAG, "Can not find sensor id("+MacID+") from SwappedSensor list.");
		return null;
	}
//	public static String getIDFromMacID(Context c, String MacID){
//		return String.format("%02d", DataStore.getSensorInfoID(c, SensorDataInfo.getDataInfoFromMacID(c, MacID)));
//	}
	public String getMacID() {
		return MacID;
	}
	public void setMacID(String macID) {
		MacID = macID;
	}
	public String getBodyLocation() {
		return bodyLocation;
	}
	public void setBodyLocation(String bodyLocation) {
		this.bodyLocation = bodyLocation;
	}
//	public int getID() {
//		return ID;
//	}
//	public void setID(int iD) {
//		ID = iD;
//	}	
}
