package edu.neu.android.wocketslib.mhealthformat;

import java.util.Date;

public class WocketSensorData extends SensorData {
//	private static final String TAG = "WocketSensorData";
	
	public int mX;
	public int mY;
	public int mZ; 
	
//	//The time the phone read the data from the sensor
//	public Time mReadTime;
//	//The approximate time that the sensor recorded the data
//	public Time mWocketRecordedTime;
	
	public WocketSensorData(SensorData.TYPE aType, Date aDateTime, int x, int y, int z) {
		super(aType, aDateTime);
		
		mX = x;
		mY = y;
		mZ = z;
//		mReadTime = readTime;
//		mWocketRecordedTime = recordedTime; 
	}

	@Override
	public void reset() {
		mX = 0;
		mY = 0;
		mZ = 0;
//		mReadTime = null;
//		mWocketRecordedTime = null; 
	}

}
