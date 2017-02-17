package edu.neu.android.wocketslib.mhealthformat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Zipper;


public class DataSaver 
{	
	private static final String TAG = "DataSaver";
	private static final int HOUR_NOT_ASSIGNED = -1;
//	private static final int DEFAULT_TIMESTAMP_AFTER_NUM_SAMPLES = 200; 
	private static final int MS_1_SEC = 60000; 
	private static final int MS_DIFF_ENCODING_THRESHOLD = 255; 
	private static final SimpleDateFormat _DateFormat_dayDirectory = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat _DateFormat_filename = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
	private static final boolean IS_GZIP = true; 
	
	private static final int DEFAULT_BYTE_ARRAY_SIZE = 1000; 
	
	private SensorData.TYPE sensorType;
	private int sensorId;
	private String fileExtBinary;

	// Variables that keep track of the state of the data saving 
	private String storagePath;
	private String currentDataFile;
	private String currentDataDir;
	private int presentHour = HOUR_NOT_ASSIGNED;	
//	private int timeSaveCount = 0;

	// Timestamp data saving info 
	private boolean isForceTimeStamp = true;
	private long lastDataPointTimeStampMS = 0;
	private int diffMS = 0;
	private long lastFullTimeStampMS = 0; 

	// Binary file stream 
	private FileOutputStream bw;	
	private RawDataPacker rdp = null; 	

	public DataSaver(String aProtocolName, String aSubjectName, String aSessionName, SensorData.TYPE aSensorType, 
				     int aSensorID, String aStoragePath)
	{
		sensorType = aSensorType;
		sensorId = aSensorID;
		storagePath = aStoragePath + aProtocolName + "-" + aSubjectName + "-" + aSessionName + "/SensorFolder/";

		createAllSubdirectories();
	
		setupSensorTypeInfo(aSensorType);		
	}
	
	private void setupSensorTypeInfo(SensorData.TYPE aSensorType)
	{
		switch (aSensorType)
		{
//			case ZEPHYR:
//				rdp = new ZephyrRawDataPacker(sensorType, 300);
//				break;
//			case POLAR:
//				rdp = new PolarRawDataPacker(sensorType, 300);
//				break;
			case WOCKET12BITRAW:
				rdp = new WocketRawDataPacker(aSensorType, DEFAULT_BYTE_ARRAY_SIZE);
				fileExtBinary = WocketRawDataPacker.FILE_EXT; 
				break;				
			default: 
				rdp = null; 
		}				
	}

	public void SaveRawData(SensorData[] someSensorData)
	{
		for (SensorData sd : someSensorData) {  
			
			// Setup the directory based on the time of the particular data point
			setupOrChangeSensorDataDirectory(sd.mDateTime); 
	
			// Write the timestamp information, if required for this data point
			writeTimeStamp(sd.mDateTime.getTime());
			
			rdp.packRaw(sd, false, bw); //isForceNoDifferentialCompression)
		}

		flushRawBuffer(); 
	}

	private void flushRawBuffer()
	{		
	try
	{
		if(bw != null)
			bw.flush();
	}
	catch(Exception ex)
	{
		Log.e(TAG,"Unable to flush output stream :" + currentDataFile);
	}
	}
	
	private void writeTimeStamp(long aTimeStamp)
	{		
		if(bw == null)
		{
			Log.e(TAG,"Error: Object NULL. Can't write data to :" + currentDataFile);
			return; 
		}

		diffMS = (int) (aTimeStamp - lastDataPointTimeStampMS);   			

		if (isForceTimeStamp ||                                  // Write full timestamp no matter what 
				(diffMS >= MS_DIFF_ENCODING_THRESHOLD) ||        // or write if 255 or more ms between samples
				((aTimeStamp - lastFullTimeStampMS) > MS_1_SEC)) // or if more than 1s has elapsed without a full timestamp			
		{				
			// Write the full timestamp 
			int sec = (int)(aTimeStamp/1000);
			short ms = (short)(aTimeStamp%1000);

			try
			{
				bw.write((byte)255); // Mark as a full timestamp

				//TODO This is going to create a lot of garbage. Fix once working.
				bw.write(new byte[] {
						(byte)(sec&0xFF),
						(byte)((sec >>> 8)&0xFF),
						(byte)((sec >>> 16)&0xFF),
						(byte)((sec >>> 24)&0xFF) },0,4);
				bw.write(new byte[] {
						(byte)(ms&0xFF),
						(byte)((ms >>> 8)&0xFF)},0,2);

				isForceTimeStamp = false; 
				lastDataPointTimeStampMS = aTimeStamp;
				lastFullTimeStampMS = aTimeStamp; 
			}
			catch(IOException ex)
			{
				Log.e(TAG,"Exception while writing raw data to :" + currentDataFile);
			}
		}
		else
		{
			//Write the differential timestamp			
			try
			{
				bw.write((byte)diffMS);
				lastDataPointTimeStampMS = aTimeStamp;
			}
			catch(IOException ex)
			{
				Log.e(TAG,"Exception while writing raw data to :" + currentDataFile);
			}

		}
	}
	
	private void createAllSubdirectories()
	{
		//TODO If subdirectories don't exist, create
		
	}
	
	
	private void compressFile(String aFile)
	{
		Zipper.zipFile(aFile, true); //Replace
	}
	
	/**
	 * Create the directory where the data will be saved in the mhealth data format, where data is stored by the hour.
	 * @param aDateTime The time of the data to be saved
	 */
	private void setupOrChangeSensorDataDirectory(Date aDateTime)
	{
		//now.get(Calendar.HOUR_OF_DAY);
    	int nowHour=aDateTime.getHours();    			
    	
		if(presentHour != nowHour) // Has the hour changed, requiring a new directory?  
		{
			closeThenOpenBinaryFile(aDateTime, nowHour, IS_GZIP);
			
			// Ensure that the first data point in the new file will start
            // with the full, rather than differential, timecode info. 
			isForceTimeStamp = true;
		}		
	}

	private void closeThenOpenBinaryFile(Date aDateTime, int aNowHour, boolean isCompress)
	{
		// A raw file is open, so close the prior file before creating a new one in new directory 
		if(bw != null)
		{
			try
			{
				bw.flush();
				bw.close();
				
				if (isCompress)
					compressFile(currentDataFile);
			}
			catch(IOException ex)
			{
				Log.e(TAG,"Unable to flush outputstream");
			}
		}
		 
		presentHour = aNowHour;
		currentDataDir = storagePath +  _DateFormat_dayDirectory.format(aDateTime) + "/" + presentHour + "/"; 
		currentDataFile = currentDataDir + sensorType + "." + sensorId + "." + _DateFormat_filename.format(aDateTime) + fileExtBinary;
		
		File directory = new File(currentDataDir);
		if (!directory.exists())
			directory.mkdirs();

		Log.d(TAG, "Binary file: " + currentDataFile);
		try
		{
			bw = new FileOutputStream(currentDataFile, true);
		}
		catch(FileNotFoundException e)
		{
			presentHour = HOUR_NOT_ASSIGNED;
			Log.e(TAG,"Unable to open output file:" + currentDataFile);
		}
	}	
}
