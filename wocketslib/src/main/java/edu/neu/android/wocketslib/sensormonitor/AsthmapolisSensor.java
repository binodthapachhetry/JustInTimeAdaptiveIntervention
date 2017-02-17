/******************************************************************************
 * 
 * @author Stephen Intille
 * @date  6/1/11
 * @brief Abstract class that models data and functions common to all Asthma sensors
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import edu.neu.android.wocketslib.utils.Log;

public class AsthmapolisSensor extends Sensor {
	private static final String TAG = "AsthmapolisSensor";
	
//	private boolean isRescue = false;	
	
	/**
	 * Obscure a MAC address so that when this function runs, the Asthmapolis detection hack does not trigger itself.
	 * @param anAddress
	 * @return
	 */
	private String obscureAddress(String anAddress)
	{	
		StringBuffer sb = new StringBuffer(anAddress);
		sb.insert(2, '=');
		sb.insert(2, '=');
		return sb.toString(); 
	}
	
	
	/**
	 * Constructor
	 * @param type - The type of sensor
	 * @param name - The Bluetooth device name
	 * @param address - The Bluetooth device MAC address
	 */
	public AsthmapolisSensor(String name, String address)
	{
		super(Sensor.ASTHMA ,name, address);
		Log.d(TAG, "Adding Ashmapolis sensor:" + " Name: " + name + " Address: " + obscureAddress(address));
	}

	public double getBatteryPercentage() 
	{
		// We have no way to get battery percentage information at this time
		return 0; 
	}

	public String getDetailedInfo(){
		// We have no way to get detailed infor at this time
		return null; 
	}
	
	/**
	 * Decodes the raw bytes to find the heart rate
	 * 
	 * @param data - the raw bytes read from the sensor
	 * @param size - the number of bytes read
	 */
	public void parsePacket(byte[] data, int size)
	{
		return; //Todo process logcat here? 
	}	
	
	public String[] getHeader() {
    	String[] header = {"TIME_STAMP","VALUE", "SEQUENCE_NUMBER"};
    	return header;
	}   
}
