package edu.neu.android.wocketslib.mhealthformat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.sensormonitor.HRPoint;
import edu.neu.android.wocketslib.utils.Log;

public class HeartRateMonitorDataSaver {
	private final static String TAG = "HR DataSaver";
	private static String localPath;
	private HRPoint point;
	private String sensorType="ZephyrHxMBT";
	private String sensorID="";
	private Calendar sampleTimeStamp = Calendar.getInstance();
	
	//Save rate, battery, hbNum
	public HeartRateMonitorDataSaver (Calendar timeStamp, HRPoint hrPoint, String sensorID){
		this.point = hrPoint;
		this.sensorID = sensorID;
		this.sampleTimeStamp = timeStamp;
	}
	public HeartRateMonitorDataSaver (Calendar timeStamp, HRPoint hrPoint, String sensorType,String sensorID){
		this.point = hrPoint;
		this.sensorType=sensorType;
		this.sensorID = sensorID;
		this.sampleTimeStamp = timeStamp;
		
	}
	public void saveData(){
		SimpleDateFormat detailedTime = Globals.mHealthTimestampFormat;
		FileWriter fileWriter = null;
		int rate = this.point.mRate;
		int battery = this.point.mBatteryPercent;
		int hbNum = this.point.mHeartBeatNumber;
		Calendar time = Calendar.getInstance();
		time.setTime(this.point.mPhoneReadTime);
		
		if (Globals.IS_SENSOR_DATA_EXTERNAL) {
			localPath = Globals.EXTERNAL_DIRECTORY_PATH;
		}
		else {
			localPath = Globals.INTERNAL_DIRECTORY_PATH;
		}
		localPath += File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY + File.separator;
		
		String outputFilePath = getFilePathByTime(sampleTimeStamp);
		String outputFileName = getFileNameForCurrentHour(outputFilePath);
		try {
			File outputDir = new File(outputFilePath);
			if(!outputDir.isDirectory()){
				if(!outputDir.mkdirs())
					Log.e(TAG, "Error: can not make output directory in memory.");
			}
			File outputFile = new File(outputFilePath+outputFileName);
			if(!outputFile.exists()){
				if(!outputFile.createNewFile())
					Log.e(TAG, "Error: can not create output file in memory.");
				else {
					fileWriter =new FileWriter(outputFile,true);
					String headerToWrite="TIME_STAMP,Heart Rate, Heart Beat Number, Battery Percent"+"\r\n";
					fileWriter.write(headerToWrite);
					fileWriter.flush();
					fileWriter.close();
				}
			}
			fileWriter =new FileWriter(outputFile,true);
			
			// write data in output now. output.write
			String dataToWrite =detailedTime.format(this.sampleTimeStamp.getTime())+","
					+point.mRate+","
					+point.mHeartBeatNumber+","
					+point.mBatteryPercent+"\r\n";
			fileWriter.write(dataToWrite);
			
			
			
			
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Error: can not find file. File path: "+outputFilePath+outputFileName+". System error: "+e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Error: can not write file. File path: "+outputFilePath+outputFileName+". System error: "+e.toString());
		} finally{
			try {
				if (fileWriter != null){
					fileWriter.flush();
					fileWriter.close();
					
				}
			}catch (IOException e){
				e.printStackTrace();
			}
			
		}
		
		
	}
	/**
	 * Gets the path for the raw data file by day and hour.
	 *
	 * @return the path by time
	 */
	private String getFilePathByTime(Calendar time)
	{
		Date timeStamp = time.getTime();
		SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hour = new SimpleDateFormat("HH");
		String path = localPath +day.format(timeStamp)+"/"+hour.format(timeStamp)+"/";
		return path;
	}
	/**
	 * Checks the file name for current hour. If there is a file exists, return it; if not, generate the file name by the time. 
	 *
	 * @param path the path
	 * @return the file name for current hour
	 */
	private String getFileNameForCurrentHour(String path){
		
		Date timeStamp = sampleTimeStamp.getTime();
		SimpleDateFormat detailedTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS"); //TODO SSI hardcoded. Should use global
		
		
			File dir = new File(path);
			//final String bodyLocation = sensorInfo.getBodyLocation();
			String[] files = dir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.startsWith(sensorType+"."+sensorID);
				}
			});
			if(files == null || files.length == 0){
				return sensorType+"."+sensorID+"."+detailedTime.format(timeStamp)+".csv";
			}
			else
				return files[0];
		
	}

}
