package edu.neu.android.wocketslib.utils;

import java.io.Serializable;

public class LocationData implements Serializable {	
	private static final long serialVersionUID = -4270878621326979375L;
	
	protected int   mHour;
	protected int   mMinute;
	protected int   mSecond;
	protected int   mMillisecond;
	protected float mLatitude;
	protected float mLongitude;	
	protected float mAccuracy;
	
	public LocationData() {}
	
	public LocationData(int hour, int minute, int second, int millisecond, 
			float latitude, float longitude, float accuracy) {
		mHour        = hour;
		mMinute      = minute;
		mSecond      = second;
		mMillisecond = millisecond;
		mLatitude    = latitude;
		mLongitude   = longitude;		
		mAccuracy    = accuracy;
	}
	
	public LocationData(String dateTime, float latitude, float longitude, float accuracy) {
		// Hack here to speed up, assume mHealth type won't be changed at least now
		mHour        = Integer.parseInt(dateTime.substring(11, 13)); 
		mMinute      = Integer.parseInt(dateTime.substring(14, 16));
		mSecond      = Integer.parseInt(dateTime.substring(17, 19));
		mMillisecond = Integer.parseInt(dateTime.substring(20, 23));
		mLatitude    = latitude;
		mLongitude   = longitude;		
		mAccuracy    = accuracy;
	}
	
	public LocationData clone() {

        LocationData data = new LocationData();
        data.mHour        = this.mHour;
        data.mMinute      = this.mMinute;
        data.mSecond      = this.mSecond;
        data.mMillisecond = this.mMillisecond;
        data.mLatitude    = this.mLatitude;
        data.mLongitude   = this.mLongitude;        
        data.mAccuracy    = this.mAccuracy;
		
		return data;
    }
	
	public int getHour() {
		return mHour;
	}
	
	public int getMinute() {
		return mMinute;
	}
	
	public int getSecond() {
		return mSecond;
	}
	
	public int getMillisecond() {
		return mMillisecond;
	}
	
	public float getLatitude() {
		return mLatitude;
	}
	
	public float getLongitude() {
		return mLongitude;
	}		
	
	public float getAccuracy() {
		return mAccuracy;	
	}		
}
