/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Abstract base class for a Bluetooth sensor.  Maintains variables and functions
 * 			that are common across all types of sensors
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import edu.neu.android.wocketslib.utils.DateHelper;

public abstract class Sensor {

	public static final int WOCKET = 1;
	public static final int ZEPHYR = 2;
	public static final int POLAR  = 3;
	public static final int ASTHMA = 4;
		
	public final static int UNKNOWN = -1; 
	
	//The current battery percentage remaining in the sensor (0-100)
	public int mBattery = 0;

	//The sensor's bluetooth device name
	public String mName = "";

	//Flag indicating if the user has chosen to enable and use this sensor.
	// If true, the sensor will be polled each time the Bluetooth service runs.
	public boolean mEnabled = false;

	//Flag indicating if the sensor is paired. 
	public boolean mPaired = false;

	//The type of this sensor
	public int mType;

	//The bluetooth device address of this sensor
	public String mAddress;

	//The number of attempts to read data from this sensor that have failed since
	//the last successful read
	public int mConnectionErrors;

	public Date mLastConnectionTime; 
	
	public boolean mDataToProcess = false; 
	public byte[] mData = null; 
	
	public int mBytesReceived = 0; 
	public int mPacketsReceived = 0; 

	protected boolean isResetDetected = false; 
	
	public boolean isResetDetected()
	{
		if (isResetDetected)
		{
			isResetDetected = false;
			return true;
		}
		else
			return false; 
	}
	
	/**
	 * Reset
	 * 
	 * Resets all necessary fields in this sensor to their default values. Sensor specific values in each subclass 
	 */
	public void reset()
	{
		mBytesReceived = 0;
		mPacketsReceived = 0;
		mDataToProcess = false;
		mBattery = UNKNOWN; 
	}	
	
	/**
	 * Parse a raw packet read from a sensor and decode its data
	 * 
	 * @param data - the raw bytes read from the Bluetooth device
	 * @param size - the number of raw bytes read
	 */
	public abstract void parsePacket(byte[] data, int size);

	public abstract double getBatteryPercentage(); 
	
	public void restoreSensorStat(Context aContext){
		SharedPreferences prefs = aContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE );

		String id = mAddress;
		mConnectionErrors = prefs.getInt(Defines.SHARED_PREF_SENSOR_CONNECTION_ERRORS+id, -1); //TODO fix -1
		mLastConnectionTime = DateHelper.getDate(prefs.getLong(Defines.SHARED_PREF_SENSOR_LAST_CONNECTION_TIME+id, 0)); //TODO fix 0
		mBattery = prefs.getInt(Defines.SHARED_PREF_SENSOR_BATTERY+id, -1); //TODO fix -1
		mBytesReceived = prefs.getInt(Defines.SHARED_PREF_SENSOR_BYTES_RECEIVED+id, -1); //TODO fix -1
		mPacketsReceived = prefs.getInt(Defines.SHARED_PREF_SENSOR_PACKETS_RECEIVED+id, -1); //TODO fix -1
	}
    public void saveSensorStats(Context aContext)
    {
		Editor edit = aContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

		String id = mAddress;
		
		edit.putInt(Defines.SHARED_PREF_SENSOR_CONNECTION_ERRORS+id, mConnectionErrors);
        if (mLastConnectionTime == null)
    		edit.putLong(Defines.SHARED_PREF_SENSOR_LAST_CONNECTION_TIME+id, 0);
        else
            edit.putLong(Defines.SHARED_PREF_SENSOR_LAST_CONNECTION_TIME+id, mLastConnectionTime.getTime());
		edit.putInt(Defines.SHARED_PREF_SENSOR_BATTERY+id, mBattery);
		edit.putInt(Defines.SHARED_PREF_SENSOR_BYTES_RECEIVED+id, mBytesReceived); 
		edit.putInt(Defines.SHARED_PREF_SENSOR_PACKETS_RECEIVED+id, mPacketsReceived);
		
		edit.commit();
    }
    
    public abstract String[] getHeader();
    
    public void saveLowSamplingRateData() {
		
	}
   

	public abstract String getDetailedInfo();
	
	/**
	 * Constructor
	 * 
	 * @param type - The type of this sensor
	 * @param name - The BT name of this device
	 * @param address - The BT MAC address of this device
	 */
	public Sensor(int type, String name, String address)
	{
		mLastConnectionTime = null; 
		mType = type;
    	mName = name;
    	mEnabled = false;
    	mAddress = address;
    	mConnectionErrors = Defines.NO_CONNECTION_LIMIT;
    	mBytesReceived = 0; 
    	mPacketsReceived = 0; 
		mDataToProcess = false; 		
	}
	
		
}
