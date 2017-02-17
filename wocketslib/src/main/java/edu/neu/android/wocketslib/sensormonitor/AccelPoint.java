/******************************************************************************
 * 
 * @brief Class used to store a single decoded point of data from an accelerometer 
 * sensor.
 * @author Kyle Bechtel
 * @date  6/1/11
 * 
 * 
 *****************************************************************************/
package edu.neu.android.wocketslib.sensormonitor;

import java.util.Calendar;

public class AccelPoint {

	int mX;
	int mY;
	int mZ;
	byte[] mRawData;
	
	//true indicated the raw data is in compressed format, false uncompressed
	boolean mCompressed;
	
	//The time the phone read the data from the sensor
	public Calendar mPhoneReadTime;
	//The approximate time that the sensor recorded the data
	public Calendar mWocketRecordedTime;
	
	/*
	 * Constructor
	 * 
	 *  @param x - the decoded x value read from the sensor
	 *  @param y - the decoded y value read from the sensor
	 *  @param z - the decoded z value read from the sensor
	 *  @param compressed - flag indicating if the raw bytes for this point were
	 *  		compressed when read from the sensor
	 *  @param raw - an array of the raw bytes read from the sensor, un-decoded
	 * 
	 */
	public AccelPoint( int x,int y,int z, boolean compressed, byte[] raw)
	{
		mPhoneReadTime = Calendar.getInstance();
		
		mX = x;
		mY = y;
		mZ = z;
		mCompressed = compressed;
		
		mRawData = raw;
	}
	public AccelPoint(int x,int y,int z, boolean compressed, long timeInMilliseconds, byte[] raw){
		mPhoneReadTime = Calendar.getInstance();
		mPhoneReadTime.setTimeInMillis(timeInMilliseconds);		
		mX = x;
		mY = y;
		mZ = z;
		mCompressed = compressed;
		
		mRawData = raw;
		
	}
	public AccelPoint(int x, int y, int z, boolean compressed)
	{
		mPhoneReadTime = Calendar.getInstance();
		mX = x;
		mY = y;
		mZ = z;
		mCompressed = compressed;
	}

	public int getmX() {
		return mX;
	}

	public int getmY() {
		return mY;
	}

	public int getmZ() {
		return mZ;
	}
	
}
