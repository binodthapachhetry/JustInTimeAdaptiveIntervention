/******************************************************************************
 * 
 * @author SSI  
 * @date  10/15/11
 * @brief Abstract base class for any SensorData. The assumption is that when data are put into this,
 * they have been appropriately decoded and timestamped.
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.mhealthformat;

import java.util.Date;

public abstract class SensorData {
//	private static final String TAG = "SensorData";
	
	public static enum TYPE
	{
		ZEPHYR,
		POLAR,
		WOCKET12BITRAW
	}

	public String getSensorDataTypeName()
	{
		switch (mType)
		{	
		case ZEPHYR: 
			return "Zephyr";
		case POLAR:
			return "Polar";
		case WOCKET12BITRAW:
			return "Wocket12BitRaw";
		default:
			return "Unknown";
		}
	}
		
	//The type of this sensor
	public TYPE mType;

	//The datetime of the sensordata 
	public Date mDateTime; 

	/**
	 * Reset
	 * 
	 * Resets all necessary fields in this sensor to their default values
	 */
	public abstract void reset();

	/**
	 * Constructor
	 * 
	 * @param aType - The type of this sensor
	 */
	public SensorData(TYPE aType, Date aDateTime)
	{
		mType = aType;
		mDateTime = aDateTime; 
	}
}
