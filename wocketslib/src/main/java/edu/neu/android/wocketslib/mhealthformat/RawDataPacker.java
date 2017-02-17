/******************************************************************************
 * 
 * @author SSI  
 * @date  10/15/11
 * @brief Abstract base class for any RawDataPacker. A RawDataPacker object must
 * be created for any sensor type. It is called sequentially with each data point
 * and creates the byte stream that is ultimately saved, handing whatever 
 * compression is appropriate for the given format.  
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.mhealthformat;

import java.io.FileOutputStream;

public abstract class RawDataPacker {
//	private static final String TAG = "RawDataPacker";

	public String getRawDataPackerTypeName()
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
		
	//The type of this rawdatapacker
	public SensorData.TYPE mType;

	//The rawdata
	public byte[] mRawDataBytes;

	//The index of the last valid rawDataBytes value
	public int mRawDataBytesIndex;

	//The size of the rawDataBytes array
	public int mRawDataBytesSize;
	
	//The number of individual SensorData points that have been packed
	public int mNumEntries = 0; 

	/**
	 * Reset
	 * 
	 * Resets the rawdatapacker to the default state (i.e., first data packet)
 	 */
	public abstract void reset();

	/**
	 * PackRaw
	 * 
	 * Pack data into raw byte string that will be saved. This is specific to the type of sensor data, 
	 * but must be provided for all data.
	 */
	public abstract void packRaw(SensorData aSensorDataPoint, boolean isForceNoDifferentialCompression, FileOutputStream bw);
				
	/**
	 * Constructor
	 * 
	 * @param aType - The type of this sensor
	 */
	public RawDataPacker(SensorData.TYPE aType, int maxSizeByteArray)
	{
		mType = aType;
		if (maxSizeByteArray < 100)
			mRawDataBytes = new byte[100]; 
		else 
			mRawDataBytes = new byte[maxSizeByteArray];
		
		mRawDataBytesSize = mRawDataBytes.length;
	}
}
