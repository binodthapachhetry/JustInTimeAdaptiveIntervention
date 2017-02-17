package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class WocketsDecodingHandler {
	private final static String TAG = "WocketsDecoder";
	private final static int diff = 25;
	private SensorDataInfo sensor;
	private String ID;
	private Context c;
	
	/** The time for the raw data, which is used to find the path of the input files*/
	private Calendar time;
	/** The full raw time stamp for the last data point. */
	private long lastRawTimeStamp;
	
	/** The raw data in x, y and z for the last data point. */
	private int[] lastRawData;
	/**
	 * Unused
	 */
	public WocketsDecodingHandler(SensorDataInfo sensor, Calendar time, Context c) {
		super();
		this.sensor = sensor;
//		ID = String.format("%02d",sensor.getID());
		this.time = time;
		this.c = c;
		this.lastRawTimeStamp = -1;
		this.lastRawData = new int[3];
	}

	private void writeMeanDataToFile (String data) {	
	      try {
	    	  String aPathStr = getExternalPath();
	    	  String rawMeanFileName = getExternalPath() + File.separator + getRawMeanFileName();
	    	  FileHelper.createDir(aPathStr);
			String content = "";
	  		if (!FileHelper.isFileExists(rawMeanFileName))
	  			content += "UnixTimeStamp,TimeStamp,X,Y,Z\r\n";
	  		content += data;
	    	FileHelper.appendToFile(content, rawMeanFileName);
	      } 
	      catch (WOCKETSException e) 
	      {
		    Log.e(TAG, "Can not write raw mean to file --- "+ e.toString());
	      } 
	}
	
	private String getExternalPath(){
  	  SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
  	  Date date = time.getTime();
  	  String path = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY + File.separator + dayFormat.format(date)+"/merged/";
  	  return path;
	}

	private String getRawMeanFileName(){
		String fileName = "Wocket_"+ID+"_1s-RawMean_";
		fileName += sensor.getBodyLocation().replace("_", "-")+".csv";
		return fileName;
	}
	
	private class RawMeanDataHandler{
		private long timeInSec;
		private int[] xs = new int[40];
		private int[] ys = new int[40];
		private int[] zs = new int[40];
		private long unixTimeStamp;
		private int x;
		private int y;
		private int z;
		private int xMean;
		private int yMean;
		private int zMean;
		private SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		private String[] rawDatas;

		public RawMeanDataHandler() {
			super();
			day.setTimeZone(TimeZone.getTimeZone("GMT"));

		}
		public String parseRawDataAndGetMean(String data){
			rawDatas = data.replace("\r\n", ",").split(",");
			String rawMean = "";
			if(rawDatas.length > 3){
				int dataSize = rawDatas.length/4;
				for (int i = 0; i < dataSize; i++) {
					try{
					unixTimeStamp = Long.parseLong(rawDatas[4*i]);
					x = 0;
					if(rawDatas[4*i+1].length()>0)
						x = Integer.parseInt(rawDatas[4*i+1]);
					y = 0;
					if(rawDatas[4*i+2].length()>0)
						y = Integer.parseInt(rawDatas[4*i+2]);
					z = 0;
					if(rawDatas[4*i+3].length()>0)
						z = Integer.parseInt(rawDatas[4*i+3]);
					if(unixTimeStamp%1000 < diff){
						rawMean += getRawMean(unixTimeStamp);
					}
					setData(unixTimeStamp,x,y,z);
					} catch(NumberFormatException e){
						Log.e(TAG, "Failed to parse String: "+ data+"\n Error: "+e.toString());
					}
				}
			}
			return rawMean;
		}
		private void setData(long timeInMillis, int x, int y, int z){
			if(timeInSec != timeInMillis/1000){
				timeInSec = timeInMillis/1000;
				xs = new int[40];
				ys = new int[40];
				zs = new int[40];	
			}
			int i = (int) (timeInMillis%1000/diff);
			xs[i] = x;
			ys[i] = y;
			zs[i] = z;
		}
		private String getRawMean(long unixTimeStamp){
			xMean = getMean(xs);
			yMean = getMean(ys);
			zMean = getMean(zs);
			String timeStamp = day.format(new Date(unixTimeStamp));
			if(xMean != 0 && yMean !=0 && zMean !=0)
				return (unixTimeStamp-unixTimeStamp%1000) +","+timeStamp+","+xMean+","+yMean+","+zMean+"\r\n";
			else
				return (unixTimeStamp-unixTimeStamp%1000) +","+timeStamp+",,,,\r\n";
		}
		private int getMean(int[] nums){
			long temp = 0;
			int ticks = 0;
			for (int i : nums) {
				if(i != 0){
				temp+=i;
				ticks++;
				}
			}
			if(ticks != 0)
				return (int) (temp/ticks);
			else
				return 0;
		}
		
	}
	private String replenishMissingData(String rawDataByLine){
		String[] rawData = rawDataByLine.replace("\r\n", "").split(",");
		long unixTimeStamp = Long.parseLong(rawData[0]);
		if(lastRawTimeStamp == -1){
			lastRawTimeStamp = unixTimeStamp;
			return rawDataByLine;
		}else{
			int missingDataSize = (int) ((unixTimeStamp - lastRawTimeStamp)/25);
			String fullData = "";
			for (int i = 1; i < missingDataSize; i++) {
				fullData += (lastRawTimeStamp+i*25)+",,,\r\n";
			}
			fullData += rawDataByLine;
			lastRawTimeStamp = unixTimeStamp;
			return fullData;
		}
	}
}
