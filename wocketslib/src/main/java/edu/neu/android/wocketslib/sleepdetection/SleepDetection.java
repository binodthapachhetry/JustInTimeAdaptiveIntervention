package edu.neu.android.wocketslib.sleepdetection;

import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;

public class SleepDetection {
	
	private final static String TAG = "SleepDetection";
	private static String algorithm = "sazonov";//"kripke"
	private static int ACC_ARRAY_SIZE = 9;//(algorithm.equalsIgnoreCase("sazonov") ? 9: 13);
	private static int asleepEnough = 15;
	private static int awakeEnough = 30;
	private static int assumedGoToBedStartTime = 21;
	private static int assumedGoToBedEndTime = 3;
	private static int assumedGetUpStartTime = 5;
	private static int assumedGetUpEndTime = 12;
	private static String ANNOT_START_TIME = "ANNOT_START_TIME";
	static int[] motionArr = new int[1440];
	
	public static SleepAnnotation detect(Context mContext, Date timeStamp) {
		String[] motionStrArr = DataStorage.GetValueString(mContext, Globals.WOCKET_ACTIVITY_VALUE, "").split(",");		
		for (int i = 0; i < motionStrArr.length; i++) {
    		int timeKey = Integer.parseInt(motionStrArr[i].split("_")[1]);
    		if (timeKey < 1440) {
    			motionArr[timeKey] = Integer.parseInt(motionStrArr[i].split("_")[0]);
    		} else {
    			Log.e(TAG, "wrong minute value for motion data: " + timeKey);
    		}
    	}
		
		long num = DataStorage.GetValueLong(mContext, Globals.NUMBER_OF_SUMMARY_DATA, 0);
		DataStorage.SetValue(mContext, Globals.NUMBER_OF_SUMMARY_DATA, num + 1);
		
		SleepAnnotation annotation = null;
		String[] msg = null;
		
		Date startTime = new Date();		
		String sTime = DataStorage.GetValueString(mContext, ANNOT_START_TIME, "");
		if (sTime.equals("")) {
			startTime.setTime(timeStamp.getTime() - Globals.MINUTES_1_IN_MS);
		} else {
			try {
				startTime = Globals.mHealthTimestampFormat.parse(sTime);
			} catch (ParseException e) {
				Log.e(TAG, "error in parsing the sTime: "+ sTime);
			}
			if (timeStamp.getTime() - startTime.getTime() > 2 * Globals.MINUTES_1_IN_MS) 
				startTime.setTime(timeStamp.getTime() - Globals.MINUTES_1_IN_MS);
		}
		DataStorage.SetValue(mContext, ANNOT_START_TIME, Globals.mHealthTimestampFormat.format(timeStamp));
		
		int thisMin = timeStamp.getHours() * 60 + timeStamp.getMinutes();
		int[] acc = new int[ACC_ARRAY_SIZE];
		int minute = thisMin - ACC_ARRAY_SIZE + 1;
		if (minute < 0) minute += 1440;
		for (int i = 0; i < ACC_ARRAY_SIZE; i++) {
			//acc[i] = (int)DataStorage.GetValueLong(mContext, Globals.WOCKET_ACTIVITY_VALUE + minute, 0);
			acc[i] = motionArr[minute];
			minute++;
			if (minute == 1440) minute = 0;
		}
		
		double statusValue = ClassifierOnline.classify(algorithm, acc);
		double	psi = 1 / (1 + Math.exp(-statusValue));
		String status = (psi < 0.5) ? Globals.WAKE_LABEL : Globals.SLEEP_LABEL;
		DataStorage.SetValue(mContext, Globals.WS_STATUS_VALUE + thisMin, (float)statusValue);
		msg = new String[] {Globals.mHealthTimestampFormat.format(startTime),
				Globals.mHealthTimestampFormat.format(timeStamp), status, "" + (Math.abs(psi - 0.5) * 2)};
	    Log.o("sleep_wake_annotation", msg);		
		
	    int data = (psi < 0.5) ? 1000: 0;
	    ServerLogger.addSleepWakeData(TAG, mContext, data, startTime, "1111");	    
	           
        String prevLabel = DataStorage.GetValueString(mContext, Globals.PREV_LABEL, "");
        String[] pLabelSplit = null;
        int currentLength = (int)DataStorage.GetValueLong(mContext, Globals.CURRENT_LABEL, 0);
        
        if (prevLabel.equals("")) {
        	prevLabel = Globals.mHealthTimestampFormat.format(startTime) + "," + status + "," + 1;
            DataStorage.SetValue(mContext, Globals.CURRENT_LABEL, 0);
        } else {
        	pLabelSplit = prevLabel.split(","); // StartTime, sleep/wake Label, Length
        	int labelLength = Integer.parseInt(pLabelSplit[2]);
        	Date labelTime = null;
        	try {
        		labelTime = Globals.mHealthTimestampFormat.parse(pLabelSplit[0]);
			} catch (ParseException e) {
				Log.e(TAG, "Parse Exception of label time: " + pLabelSplit[0]);
			}
        	
        	// if prevLabel is too old
        	if ((startTime.getTime() - labelTime.getTime()) / Globals.MINUTES_1_IN_MS > labelLength + 5) {           		
        		Date annotStopTime = new Date();
            	annotStopTime.setTime(labelTime.getTime() + labelLength * Globals.MINUTES_1_IN_MS);
            	
        		writeLabel(mContext, pLabelSplit, annotStopTime);
	        	
	        	//create sleep annotation            	
            	annotation = new SleepAnnotation(labelTime, annotStopTime, pLabelSplit[1]);    
	        	
        		prevLabel = Globals.mHealthTimestampFormat.format(startTime) + "," + status + "," + 1;
        		currentLength = 0;
                
        	} else {
        		
		        if (status.equals(pLabelSplit[1])) { 
		        	if (currentLength == 0) {
		        		prevLabel = pLabelSplit[0] + "," + pLabelSplit[1] + "," + (labelLength + 1); 
		        	} else {
		        		prevLabel = pLabelSplit[0] + "," + pLabelSplit[1] + "," + (labelLength + currentLength + 1);
		        		currentLength = 0;
		        	}
		        } else {
		        	currentLength++;        		
		        	if  (currentLength > 1) {
		        		Date prevEndTime = new Date();
			        	prevEndTime.setTime(timeStamp.getTime() - currentLength * Globals.MINUTES_1_IN_MS);
			        	
		        		writeLabel(mContext, pLabelSplit, prevEndTime);
			        	
		            	//create sleep annotation
		            	annotation = new SleepAnnotation(labelTime, prevEndTime, pLabelSplit[1]);	
			        	
			        	prevLabel = Globals.mHealthTimestampFormat.format(prevEndTime) + "," + status + "," + currentLength;
			        	currentLength = 0;
		        	}
		        	
		        }
		        checkSleepStart(mContext, pLabelSplit);
		        checkWakeStart(mContext, pLabelSplit);
		        checkWocketWorn(mContext, pLabelSplit);
        	}

        }
        
        DataStorage.SetValue(mContext, Globals.PREV_LABEL, prevLabel);
        DataStorage.SetValue(mContext, Globals.CURRENT_LABEL, currentLength);
                
        return annotation;
	}
	
	private static void writeLabel(Context aContext, String[] pLabelSplit, Date endTime) {
		int labelLength = Integer.parseInt(pLabelSplit[2]);
    	Date labelTime = null;
    	try {
    		labelTime = Globals.mHealthTimestampFormat.parse(pLabelSplit[0]);
		} catch (ParseException e) {
			Log.e(TAG, "Parse Exception of label time: " + pLabelSplit[0]);
		}
		String[] msg = new String[] {pLabelSplit[0], pLabelSplit[1], pLabelSplit[2]};
    	Log.o("sleep_wake_data", msg);
    	int data = (pLabelSplit[1].equals(Globals.WAKE_LABEL)) ? 1000 : 0;
    	
    	//ServerLogger.addSleepWakeData(TAG, aContext, data, labelTime, labelLength, "2222");
    	for (int i = 0; i <= labelLength; i++) {    	
			Date tStamp = new Date();
			tStamp.setTime(labelTime.getTime() + (i * Globals.MINUTES_1_IN_MS));
			if (tStamp.before(endTime))
				ServerLogger.addSleepWakeData(TAG, aContext, data, tStamp, "2222");
			else
				break;
		}	    	
        
    	//save the wake times during the night
    	if (pLabelSplit[1].equals(Globals.WAKE_LABEL)) {
    		String temp = DataStorage.GetValueString(aContext, Globals.ALL_MOTION_WAKE_DATA, "") 
    				+ pLabelSplit[0] + "," + pLabelSplit[2] + "_";
    		DataStorage.SetValue(aContext, Globals.ALL_MOTION_WAKE_DATA, temp);
    	}    	
	}
	
	
	protected static void checkSleepStart(Context aContext, String[] label) {
		int hour = new Date().getHours();
		if (hour >= assumedGoToBedStartTime || hour < assumedGoToBedEndTime) { 
			String sleepStartTime = DataStorage.GetValueString(aContext, Globals.SLEEP_START_TIME, "");
			Date prevTime = null;
			if (!sleepStartTime.equals("")) {			
				try {
					prevTime = Globals.mHealthTimestampFormat.parse(sleepStartTime);
				} catch (ParseException e) {
					Log.e(TAG, "error in parsing the time: "+ prevTime);
				}
			}
			Date labelTime = null;
			try {
				labelTime = Globals.mHealthTimestampFormat.parse(label[0]);
			} catch (ParseException e) {
				Log.e(TAG, "error in parsing the time: "+ labelTime);
			}
	        if (sleepStartTime.equals("") || (labelTime.getTime() - prevTime.getTime() > 2 * Globals.HOURS8_MS)) {
		        if (Integer.parseInt(label[2]) >= asleepEnough && label[1].equals(Globals.SLEEP_LABEL)) {
		        	sleepStartTime = label[0];
	        		DataStorage.SetValue(aContext, Globals.SLEEP_START_TIME, sleepStartTime);
		        	String[] msg = new String[] {sleepStartTime, "Go to sleep detected time."};
		        	Log.o("sleep_wake_data", msg);
		        	ServerLogger.addNote(aContext, "Go to sleep detected time." , Globals.PLOT);
	
		        	DataStorage.SetValue(aContext, Globals.WAKE_START_TIME, "");
		        } 
	        } 
		}
	}
	
	protected static void checkWakeStart(Context aContext, String[] label) {		
		int hour = new Date().getHours();
		if (hour >= assumedGetUpStartTime && hour < assumedGetUpEndTime) {
			String wakeStartTime = DataStorage.GetValueString(aContext, Globals.WAKE_START_TIME, "");			
			Date prevTime = null;
			if (!wakeStartTime.equals("")) {			
				try {
					prevTime = Globals.mHealthTimestampFormat.parse(wakeStartTime);
				} catch (ParseException e) {
					Log.e(TAG, "error in parsing the time: "+ prevTime);
				}
			}
			Date labelTime = null;
			try {
				labelTime = Globals.mHealthTimestampFormat.parse(label[0]);
			} catch (ParseException e) {
				Log.e(TAG, "error in parsing the time: "+ labelTime);
			}
			if (wakeStartTime.equals("") || (labelTime.getTime() - prevTime.getTime() > 2 * Globals.HOURS8_MS)) {
		        if (Integer.parseInt(label[2]) >= awakeEnough && label[1].equals(Globals.WAKE_LABEL)//) {
		        		 && detectSignificantMotion(aContext)) {
		        	wakeStartTime = label[0];
		        	DataStorage.SetValue(aContext, Globals.WAKE_START_TIME, wakeStartTime);	
		        	String[] msg = new String[] {wakeStartTime, "Wake up detected time."};
		        	Log.o("sleep_wake_data", msg);
		        	ServerLogger.addNote(aContext, "Wake up detected time." , Globals.PLOT);
		        }
	        }
		}
	}
	
	protected static void checkWocketWorn(Context aContext, String[] label) {
		boolean worn = DataStorage.GetValueBoolean(aContext, Globals.WOCKET_WORN, false); 		 
		
		if (worn) {
			if ((Integer.parseInt(label[2]) >= 90) && (label[1].equals(Globals.SLEEP_LABEL))) {
	        	DataStorage.SetValue(aContext, Globals.WOCKET_WORN, false); 
	        	String[] msg = new String[] {"Wocket is not worn probably scince: " + label[0]};        	
	        	Log.o(Globals.WOCKET_STATUS, msg);
			}
		} else {
			if ((Integer.parseInt(label[2]) >= 4) && (label[1].equals(Globals.WAKE_LABEL))) {
	        	DataStorage.SetValue(aContext, Globals.WOCKET_WORN, true);  
	        	String[] msg = new String[] {"Wocket is worn scince: " + label[0]};        	
	        	Log.o(Globals.WOCKET_STATUS, msg);
			}
		}
	}
	
	public static void resetSleepParametersIfNeeded(Context aContext) {
		String lastRestTime = DataStorage.GetValueString(aContext, Globals.RESET_TIME, "");
		if (!lastRestTime.equals("")) {
			Date resetTime = null;
			try {
				resetTime = Globals.mHealthTimestampFormat.parse(lastRestTime);
			} catch (ParseException e) {
				Log.e(TAG + "reset", e.toString()); 
			}
			Date now = new Date();
			if (now.getDate() <= resetTime.getDate() || now.getHours() < 21) {	
				return;				
			}
		}
		DataStorage.SetValue(aContext, Globals.SLEEP_START_TIME, "");
		DataStorage.SetValue(aContext, Globals.WAKE_START_TIME, "");    
    	DataStorage.SetValue(aContext, Globals.NUMBER_OF_SUMMARY_DATA, 0);
    	DataStorage.SetValue(aContext, Globals.ALL_MOTION_WAKE_DATA, "");
    	DataStorage.SetValue(aContext, Globals.WOCKET_ACTIVITY_VALUE, "");
    	DataStorage.SetValue(aContext, Globals.AUDIO_MINUTE, "");
    	/*for (int i = 0; i < 1440; i++ ) {
    		DataStorage.SetValue(aContext, Globals.WS_STATUS_VALUE + i, 0.0f);
    		DataStorage.SetValue(aContext, Globals.WOCKET_ACTIVITY_VALUE + i, 0);
    	}*/
    	DataStorage.SetValue(aContext, Globals.RESET_TIME, Globals.mHealthTimestampFormat.format(new Date()));
    	String[] msg = new String[] {Globals.mHealthTimestampFormat.format(new Date()), "Reset sleep wake parameters."};
    	Log.o(TAG, msg);
	}
	
	protected static boolean isAwake(Context aContext) {		
		return !DataStorage.GetValueString(aContext, Globals.WAKE_START_TIME, "").equals("");		
	}
	
	protected static boolean isAsleep(Context aContext) {
		return !DataStorage.GetValueString(aContext, Globals.SLEEP_START_TIME, "").equals("");
	}
	
	public static boolean isThereWocketData(Context aContext, int enoughWocketData) {
		long num = DataStorage.GetValueLong(aContext, Globals.NUMBER_OF_SUMMARY_DATA, 0);
		String last = DataStorage.GetValueString(aContext, Globals.LAST_WOCKET_CONNCTION_TIME, "");
		if (last.equals(""))
			return false; 
		Date lastTime = null;
		try {
			lastTime = Globals.mHealthTimestampFormat.parse(last);
		} catch (ParseException e) {
			Log.e(TAG, "isThereWocketData" + e.toString()); 
		}
		if (lastTime.equals(""))
			return false;
		Date now = new Date();
		if ((now.getTime() - lastTime.getTime() < Globals.MINUTES_60_IN_MS) && (num >= enoughWocketData)) {
			return true;
		} else
			return false;
	}	
	
	public static boolean detectSignificantMotion(Context aContext) {
		int ave = 0;
		int minute = new Date().getHours() * 60 + new Date().getMinutes();
		for (int j = minute; j > minute - awakeEnough; j--) {
			//int acc = (int)DataStorage.GetValueLong(aContext, Globals.WOCKET_ACTIVITY_VALUE + j, 0);
			int acc = motionArr[j];
			if (acc > 2500)
				return true;
			ave += acc;
		}
		ave /= awakeEnough;
		if (ave >= 750)
				return true;
		
		return false;		
	}
	
	
	
}
