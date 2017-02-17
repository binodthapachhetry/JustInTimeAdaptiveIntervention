package edu.neu.android.wocketslib.mhealthformat;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.utils.Log;


public class LowSamplingRateDataSaver {
	
	private String localPath;
	private String sensorType;
	private String sensorID;
	private String[] header;
	private SimpleDateFormat DateDirFormat = new SimpleDateFormat("yyyy-MM-dd");	 
	private SimpleDateFormat HourDirFormat = new SimpleDateFormat("HH");
	private static final String TAG = "LowSamplingRateDataSaver";
	
    //TODO define in the globals all sensor types. Globals.SensorType.WocketsV3
	
	public LowSamplingRateDataSaver(boolean isExternal, String sensorType, String sensorID, String[] header ) {
    	this.sensorType = sensorType;
    	this.sensorID = sensorID;
    	this.header = header;
    	
    	//Set the default path for phone applications
		if (isExternal)
			localPath = Globals.EXTERNAL_DIRECTORY_PATH; 
		else
			localPath = Globals.INTERNAL_DIRECTORY_PATH;
		localPath += File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY + File.separator; 
    }
    
    // To change the default path for other applications if needed 
    public void setPath(String path) {
    	localPath = path;
    }

    public void saveData(String[] values) {
         saveData(new Date(), values);
    }
    
    public void saveData(Date aDate, String[] values) {
    	String finalPath = localPath + DateDirFormat.format(aDate) + File.separator + HourDirFormat.format(aDate) + File.separator;
    	String fileName = finalPath + getFileNameForCurrentHour(finalPath, aDate, sensorType, sensorID);
    	writeTomHealth(finalPath, fileName, aDate, values);
    }

    public void saveData(Date aDate, String[] values, Boolean isEncrypt, Context mContext) {
        String finalPath = localPath + DateDirFormat.format(aDate) + File.separator + HourDirFormat.format(aDate) + File.separator;
        String fileName = finalPath + getFileNameForCurrentHour(finalPath, aDate, sensorType, sensorID);
        writeTomHealth(finalPath, fileName, aDate, values, isEncrypt, mContext);
    }
    
    public void saveDatadaily(Date aDate, String[] values, String fileName) { 
    	String finalPath = localPath + DateDirFormat.format(aDate) + File.separator;
    	writeTomHealth(finalPath, finalPath + fileName, aDate, values);    	 
    }
    
    private void writeTomHealth(String path, String fileName, Date timeStamp, String[] values) {
    	
    	if (values.length > 0) {
        	File outputDir = new File(path);
	    	if(!outputDir.isDirectory()){
	    		outputDir.mkdirs();
			}    	
	        File f = new File(fileName);
	        CSVWriter writer;
	        try {
	            if (!(f.exists())) {
                    f.createNewFile(); 
                    writer = new CSVWriter(new FileWriter(fileName), ',', CSVWriter.NO_QUOTE_CHARACTER);
                    if (header != null) {
                    	writer.writeNext(header);
                    }
	            } else {
	            	writer = new CSVWriter(new FileWriter(fileName, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
	            }
    			String[] outArray = new String[values.length + 1];
    			System.arraycopy(values, 0, outArray, 1, values.length);
    			outArray[0] = Globals.mHealthTimestampFormat.format(timeStamp);
    			writer.writeNext(outArray);
    			writer.close();
	            
            } catch (IOException e1) {
		    	Log.e(TAG, e1.toString() + " in " + fileName);              
		    }  
	        
    	}
			
    }

    private void writeTomHealth(String path, String fileName, Date timeStamp, String[] values, Boolean isEncrypt, Context mContext) {

        if (values.length > 0) {
            File outputDir = new File(path);
            if(!outputDir.isDirectory()){
                outputDir.mkdirs();
            }
            File f = new File(fileName);
            CSVWriter writer;
            try {
                if (!(f.exists())) {
                    f.createNewFile();
                    writer = new CSVWriter(new FileWriter(fileName), ',', CSVWriter.NO_QUOTE_CHARACTER);
                    if (header != null) {
                        writer.writeNext(header);
                    }
                } else {
                    writer = new CSVWriter(new FileWriter(fileName, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
                }

                String message = "";

                for (int i = 0 ; i < values.length ; i++) {
                    if (i == 0) {
                        message = values[i];
                    } else {
                        message = message + "," + values[i];
                    }
                }

                String[] outArray = new String[2];
                outArray[0] = Globals.mHealthTimestampFormat.format(timeStamp);
                if(isEncrypt) {
                    outArray[1] = RSACipher.encrypt(message, mContext);
                } else {
                    outArray[1] = message;
                }
                writer.writeNext(outArray);
                writer.close();

            } catch (IOException | GeneralSecurityException e1) {
                Log.e(TAG, e1.toString() + " in " + fileName);
            }

        }

    }
   
    private String getFileNameForCurrentHour(String path, Date time, final String sensor, final String ID){
		File dir = new File(path);
		String[] files = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".csv") && filename.contains(sensor) && filename.contains(ID))
					return true;
				else 
					return false;
			}
		});
		if(files == null || files.length == 0) {			
	        String filename = sensor + "." + ID + "." + Globals.mHealthFileNameFormat.format(time) + ".csv";
			return filename;
		}
		else 
			return files[0];
	}    

    
}
