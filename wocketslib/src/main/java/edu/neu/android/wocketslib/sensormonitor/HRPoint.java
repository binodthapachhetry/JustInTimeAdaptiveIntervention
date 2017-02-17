/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Class used to store a single decoded heart rate measurement
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.util.Date;

public class HRPoint {

	//The time the phone read this heart rate value from the sensor
	// Currently matches the same time the sensor recorded the data
	public Date mPhoneReadTime;

	//The actual heart rate
	public int mRate;

	//The sensor battery percentage
	public int mBatteryPercent; 

	//The heart beat number
	public int mHeartBeatNumber;  
	
	//Flag indicating if this point has been sent to the server yet. 
	public boolean mJsonQueued;
	
	private static final int UNKNOWN = -1;
	
	/**
	 * Constructor
	 * 
	 * @param rate - the decoded heart rate value
	 */
	public HRPoint( int rate)
	{
		mPhoneReadTime = new Date();
		mRate = rate;
		mBatteryPercent = UNKNOWN; 
		mHeartBeatNumber = UNKNOWN;
		mJsonQueued = false; 
	}

	public HRPoint( int rate, int batteryPercent, int hbNum)
	{
		mPhoneReadTime = new Date();
		mRate = rate;
		mBatteryPercent = batteryPercent;
		mHeartBeatNumber = hbNum; 
		mJsonQueued = false; 
	}
}
