/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Class that represents a Zephyr HXM Heart Rate sensor, extends HeartRateSensor
 * and Sensor
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import edu.neu.android.wocketslib.mhealthformat.HeartRateMonitorDataSaver;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;

public class ZephyrSensor extends HeartRateSensor {

	private static final String TAG = "ZephyrSensor"; 
	
	/**
	 * Constructor
	 * 
	 * @param name - The Bluetooth device name
	 * @param address - The Bluetooth device MAC address
	 */
	public ZephyrSensor( String name, String address) {
		super(Sensor.ZEPHYR, name, address);
	}

	public double getBatteryPercentage() 
	{
		int batteryValue = this.mBattery; 
		return (double)batteryValue/100; 
	}	
	
	private boolean checkCRC(byte[] someData, byte crc)
	{
		int runningCRC = 0; 
		for (int i = 3; i <= 57; i++)
		{
			runningCRC = pushCRC(someData[i], runningCRC); 
		}
		
		if (runningCRC == (crc & 0xFF))
			return true;
		else
			return false; 		
	}
	
	private int pushCRC(byte aByte, int runningCRC)
	{
		runningCRC = runningCRC ^ (aByte & 0xFF);
		
		for (int i=0; i<8; i++)
		{
			if ((runningCRC & 0x01) == 1)
				runningCRC = (runningCRC >> 1) ^ 0x8C; 
			else
				runningCRC = (runningCRC >> 1);  
		}
		return runningCRC; 
	}
	
	public String[] getHeader() {
    	String[] header = {"TIME_STAMP","VALUE", "SEQUENCE_NUMBER"};
    	return header;
	}   
	
	/**
	 * Decodes the raw bytes from the Zephyr sensor and pulls out the
	 * heart rate and battery values, then saves them.
	 * 
	 * @param data - the raw bytes read from the Zepyhr sensor
	 * @param size - the number of raw bytes read from the sensor
	 */
	public void parsePacket(byte[] data, int size) 
	{		
		byte crc = data[58];
		
		boolean isCheck = checkCRC(data, crc);

		if (!isCheck)
		{
			Log.e(TAG, "ZephyrSensor FAILED CRC check!"); 
			return; 
		}
		
		if (data[0] != 0x02)
		{
			Log.e(TAG, "Invalid Zephyr data STX");
			return; 
		}
		
		if (data[1] != 0x26)
		{
			Log.e(TAG, "Invalid Zephyr data 0x26");
			return; 
		}

		if (data[2] != 0x37)
		{
			Log.e(TAG, "Invalid Zephyr data 0x37");
			return; 
		}
		
		int battery = (int) data[11] & 0xFF;		
		int rate = (int) data[12] & 0xFF; 
		int hbNum = (int) data[13] & 0xFF; 

		if ((rate<30) || (rate > 240))
		{
			Log.e(TAG, "Warning: HR is out of range so HR ignored: " + rate);
			return;
		}
		
		if( battery > 0 && battery <= 100)
		{
			mBattery = battery;
		}
		else
		{
			Log.e(TAG, "Warning: battery value is out of range so set to: " + battery);
		}
		
		Log.i(TAG, "HR: " + rate + " battery: " + battery + " number: " + hbNum);
		// write heart rate monitor data here in mHealth format. 
		HeartRateMonitorDataSaver hrDataSaver = new HeartRateMonitorDataSaver(Calendar.getInstance(),
				new HRPoint(rate,battery, hbNum),
				"ZephyrHxMBT",
				this.mName);
		hrDataSaver.saveData();
		addPoint( new HRPoint(rate, battery, hbNum));

		///TODO figure out if we can use more data from the Zepyhr
		/*int v = merge(data[24], data[25]);
			out+= " battery: " + String.valueOf(((double) v / (double) 1000));

			int p = merge(data[18], data[19]);
			out+= " posture: " + String.valueOf(((double) p / (double) 10));

			int r = merge(data[14], data[15]);
			out+= " respiration: " + String.valueOf(Math.abs(((double) r / (double) 10)));

			int t = merge(data[16], data[17]);
			out+= "temp: " + String.valueOf(((double) t / (double) 10));
		 */

	}
	private static final Date REASONABLE_DATE = new Date(2011-1900,0,1); 
	public String getDetailedInfo(){
		String infoContent = mName+":\n";
		if(mLastConnectionTime == null||mLastConnectionTime.before(REASONABLE_DATE)){
			infoContent += "Last time connected:\nUnknown.";
		}
		else{
			SimpleDateFormat smf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			infoContent += "Last time connected:\n"+ smf.format(mLastConnectionTime) + ".\n"
						+"Battery was at "+getBatteryPercentage()*100+"%.\n" 
						+"Average Heart Rate was "+ mAvgRate+".";
		}
		return infoContent;
	}
	public void restoreSensorStat(Context aContext){
		SharedPreferences prefs = aContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE );

		String id = mAddress;
		mConnectionErrors = prefs.getInt(Defines.SHARED_PREF_SENSOR_CONNECTION_ERRORS+id, -1); //TODO fix -1
		mLastConnectionTime = DateHelper.getDate(prefs.getLong(Defines.SHARED_PREF_SENSOR_LAST_CONNECTION_TIME+id, 0)); //TODO fix 0
		mBattery = prefs.getInt(Defines.SHARED_PREF_SENSOR_BATTERY+id, -1); //TODO fix -1
		mBytesReceived = prefs.getInt(Defines.SHARED_PREF_SENSOR_BYTES_RECEIVED+id, -1); //TODO fix -1
		mPacketsReceived = prefs.getInt(Defines.SHARED_PREF_SENSOR_PACKETS_RECEIVED+id, -1); //TODO fix -1
		
		mAvgRate = prefs.getInt(Defines.SHARED_PREF_ZEPHRSENSOR_AVG_HR+id, -1); //TODO fix -1
		mCurrentRate = prefs.getInt(Defines.SHARED_PREF_ZEPHRSENSOR_CURRENT_HR+id, -1); //TODO fix -1    

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
		
		edit.putInt(Defines.SHARED_PREF_ZEPHRSENSOR_AVG_HR+id, mAvgRate);
		edit.putInt(Defines.SHARED_PREF_ZEPHRSENSOR_CURRENT_HR+id, mCurrentRate);

		edit.commit();

    }

}
