package edu.neu.android.wocketslib.algorithm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.DateHelper;


/**
 * The class is used to detect the different types of motion 
 * based on the chunks information (position, mean, length of each chunk).
 * 
 * @author bigbug
 * 
 */
public class MotionDetectAlgorithm {
	public static final String TAG = "MotionDetectAlgorithm";

	protected final static int NO_SENSOR_DATA = -1;
	protected final static int TOTAL_SECONDS_IN_ONE_DAY = 86400;
	
	public static int NO_DATA_DURATION_THRESHOLD       = 10 * 60;  // in second
	public static int MOTION_DURATION_THRESHOLD        = 30 * 60;  // in second
	public static int MOTION_TOLERATION_THRESHOLD      = 10 * 60;  // in second
	public static int NO_MOTION_DURATION_THRESHOLD     = 60 * 60;  // in second
	public static int NO_MOTION_TOLERATION_THRESHOLD   =  1 * 45;  // in second
	protected static float MOTION_INTENSITY_THRESHOLD = Globals.MAX_ACTIVITY_DATA_SCALE * 0.2f;
	
	private final static String KEY_DETECTING_START_TIME = "KEY_DETECTING_START_TIME";
	
	// singleton
	private static MotionDetectAlgorithm sAlgorithm;
	
	public static MotionDetectAlgorithm getInstance() {
		if (sAlgorithm == null) {
			sAlgorithm = new MotionDetectAlgorithm();
		}
		
		// Update start time if necessary
		long startTime   = sAlgorithm.getStartTime();
		long defaultTime = DateHelper.getDailyTime(Globals.DEFAULT_START_HOUR, 0);
		if (startTime < defaultTime || startTime > System.currentTimeMillis()) {
			sAlgorithm.setStartTime(defaultTime);
		}
		
		return sAlgorithm;
	}
	
	private MotionDetectAlgorithm() {}

	public long getStartTime() {
		Context context = ApplicationManager.getAppContext();
		long defaultTime = DateHelper.getDailyTime(8, 0);
		long startTime = DataStorage.GetValueLong(context, KEY_DETECTING_START_TIME, defaultTime);		
		return startTime;
	}
	
	protected void setStartTime(long startTime) {
		Context context = ApplicationManager.getAppContext();
		DataStorage.SetValue(context, KEY_DETECTING_START_TIME, startTime);
		Log.d(TAG, "update start time: " + new Date(startTime));
	}
	
	/**
	 * Analyze data based on chunks and duration to get the motion detection result
	 * @param sensorData	The accelerometer between from and to
	 * @param chunkPos    	All possible chunk positions between from and to
	 * @param from			Time in milliseconds since January 1, 1970 00:00:00 UTC 	
 	 * @param to			Time in milliseconds since January 1, 1970 00:00:00 UTC
	 * @return the array stores all chunks' positions from start second to stop second
	 */
	@SuppressLint("UseSparseArrays")
	public MotionInfo doMotionDetection(int[] sensorData, ArrayList<Integer> chunkPos, long from, long to) {
		Date startTime = new Date(from);
		Date stopTime  = new Date(to);		
		HashMap<MotionInfo, Integer> stateMap = new HashMap<MotionInfo, Integer>();
		
		// Convert Date to second
		long midnight = DateHelper.getDailyTime(0, 0);
		int secFrom = startTime.getHours() * 3600 + startTime.getMinutes() * 60 + startTime.getSeconds();
		int secTo   = stopTime.getHours() * 3600 + stopTime.getMinutes() * 60 + stopTime.getSeconds();
		int secNow  = (int) ((System.currentTimeMillis() - midnight) / 1000);
				
		// Create the position-to-mean hash and position-to-duration hash
		HashMap<Integer, Float>   ptmHash = new HashMap<Integer, Float>();
		HashMap<Integer, Integer> ptdHash = new HashMap<Integer, Integer>();
		for (int i = 0; i < chunkPos.size() - 1; ++i) {
			int curPos = chunkPos.get(i);
			int nxtPos = chunkPos.get(i + 1);
			// Get mean value from the current chunk position to the next
			float sum = 0;
			for (int j = curPos; j < nxtPos; ++j) {
				sum += sensorData[j];
			}
			int duration = Math.abs(nxtPos - curPos);
			if (duration != 0) {
				ptmHash.put(curPos, sum / duration);
				ptdHash.put(curPos, duration);
			}
		}
		
		// Merge some chunks if their means are relatively the same
		ArrayList<Integer> merged = new ArrayList<Integer>();
		for (int i = chunkPos.size() - 2; i > 0; --i) {
			int curPos = chunkPos.get(i);
			int prvPos = chunkPos.get(i - 1);
			
			Float curMean = ptmHash.get(curPos);
			Float prvMean = ptmHash.get(prvPos);						
			if (curMean == null || prvMean == null) { continue; }
			
			// Skip chunks without any data at all
			if (Math.abs(curMean - NO_SENSOR_DATA) < 0.1f || Math.abs(prvMean - NO_SENSOR_DATA) < 0.1f) {
				continue;
			}
			
			// Merge the current chunk with the next one if both of them have high or low mean value
			if ((curMean >= MOTION_INTENSITY_THRESHOLD && prvMean >= MOTION_INTENSITY_THRESHOLD) ||
				Math.abs(curMean - prvMean) <= Globals.MAX_ACTIVITY_DATA_SCALE * 0.1f) {					
				// Calculate and update the mean of the chunk after merging
				Integer prvDuration = ptdHash.get(prvPos);
				Integer curDuration = ptdHash.get(curPos);
				if (prvDuration == null || curDuration == null) { continue; }
				
				int sumDuration = prvDuration + curDuration;
				if (sumDuration > 0) {
					ptmHash.put(prvPos, (curMean * curDuration + prvMean * prvDuration) / sumDuration);
					ptdHash.put(prvPos, sumDuration);
				}
				// Clear the chunk that has been merged					
				ptmHash.put(curPos, null);
				ptdHash.put(curPos, null);
				merged.add(curPos);
			}
		}
		chunkPos.removeAll(merged);
		
		// Check whether there is some periods of time without any motion data
		for (int i = 0; i < chunkPos.size() - 1; ++i) {
			int curPos = chunkPos.get(i);
			int nxtPos = chunkPos.get(i + 1);
			
			Float curMean = ptmHash.get(curPos);
			if (curMean == null) { continue; }
			
			// It's better not to compare two float values with "=="
			if (Math.abs(curMean - NO_SENSOR_DATA) < 0.1f) {
				if (nxtPos - curPos >= NO_DATA_DURATION_THRESHOLD) {					
					Date dateFrom = new Date(midnight + curPos * 1000);
					Date dateTo   = new Date(midnight + nxtPos * 1000);
					stateMap.put(new MotionInfo(MotionInfo.NO_DATA, dateFrom, dateTo), nxtPos);
				}
				break;
			}
		}
		
		// Analyze chunks with motion data from secFrom to secTo based on their mean value and duration
		for (int i = 0; i < chunkPos.size() - 2; ++i) {			
			int curPos = chunkPos.get(i);
			int nxtPos = chunkPos.get(i + 1);
			int lstPos = chunkPos.get(i + 2);
			
			// Jump the period before the starting time 
			if (secFrom > curPos) { continue; }
			
			Integer curDuration = ptdHash.get(curPos);
			Integer nxtDuration = ptdHash.get(nxtPos);			
			Float curMean = ptmHash.get(curPos);
			Float nxtMean = ptmHash.get(nxtPos);		
			
			if (curDuration == null || nxtDuration == null || curMean == null || nxtMean == null) {
				continue;
			}
							
			// First figure out the chunk with small mean followed by another one with big mean
			if (curMean < MOTION_INTENSITY_THRESHOLD && nxtMean >= MOTION_INTENSITY_THRESHOLD) {
				if (curDuration >= NO_MOTION_DURATION_THRESHOLD && nxtDuration >= NO_MOTION_TOLERATION_THRESHOLD) {
					Date dateFrom = new Date(midnight + curPos * 1000);
					Date dateTo   = new Date(midnight + nxtPos * 1000);
					stateMap.put(new MotionInfo(MotionInfo.LOW_MOTION, dateFrom, dateTo), nxtPos);
					break;
				}
			} else if (curMean >= MOTION_INTENSITY_THRESHOLD && nxtMean < MOTION_INTENSITY_THRESHOLD) {				
				if (curDuration >= MOTION_DURATION_THRESHOLD && nxtDuration >= MOTION_TOLERATION_THRESHOLD) {
					Date dateFrom = new Date(midnight + curPos * 1000);
					Date dateTo   = new Date(midnight + nxtPos * 1000);
					stateMap.put(new MotionInfo(MotionInfo.HIGH_MOTION, dateFrom, dateTo), lstPos);
					break;
				}
			}
		}
		
		// Look for the earliest state by iterating each state in the hash map 
		Iterator<Entry<MotionInfo, Integer>> iterator = stateMap.entrySet().iterator();
		MotionInfo targetExtraInfo = null;
		Integer minPosition = TOTAL_SECONDS_IN_ONE_DAY;
		Entry<MotionInfo, Integer> entry = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry.getValue() < minPosition) {
            	targetExtraInfo = entry.getKey();
            	minPosition = entry.getValue();
            }
        }
        
        if (Globals.IS_DEBUG) {
        	StringBuffer sb = new StringBuffer();     
        	sb.append("chunks: \n");
        	for (int i = 0; i < chunkPos.size(); ++i) {
        		sb.append(new Date(chunkPos.get(i) * 1000 + midnight) + "\n");
        	}        	        	
        	Log.d(TAG, sb.toString());        	
        }
        
        // If the satisfied position is found, update the start time to this position and return the state
        if (targetExtraInfo != null) {
        	Log.d(TAG, "start: " + targetExtraInfo.getStartTime());
        	Log.d(TAG, "stop:  " + targetExtraInfo.getStopTime());
        	setStartTime(midnight + minPosition * 1000);
        	return targetExtraInfo;
        }

		// Update start time for the next check
		if (chunkPos.size() >= 3) {
			setStartTime(midnight + chunkPos.get(chunkPos.size() - 3) * 1000);
		}
				
		return new MotionInfo(MotionInfo.NO_INTEREST, startTime, stopTime);
	}
}
