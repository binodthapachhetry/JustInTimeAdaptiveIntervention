/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Class used to define global variables that are used through the app.
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.Time;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Util;

public class DataStore {
	private static final String TAG = "DataStore";

	// File management

	// The directory where data is to be stored
	private static String mRawDataDir = Defines.DEFAULT_RAW_DATA_DIR;

	// The number of active data files being saved
	private static int mNumDataFiles = 0;

	// List of all the active data files being saved
	public static ArrayList<String> mDataFiles = new ArrayList<String>();

	// Data files

	public static void setNumDataFiles(int num) {
		mNumDataFiles = num;
		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
			edit.putInt(Defines.SHARED_PREF_NUM_DATA_FILES, num);
			edit.commit();
		}
		Log.d(TAG, "SET NUM DATA FILE: " + num);
	}

	public static void setAddDataFile(String aFileName) {
		// Don't add if already in the list
		for (int i = 0; i < mNumDataFiles; i++)
			if (mDataFiles.get(i).compareTo(aFileName) == 0)
				return;

		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
			edit.putString(Defines.SHARED_PREF_DATA_FILE + "_" + mNumDataFiles, aFileName);
			mNumDataFiles++;
			edit.putInt(Defines.SHARED_PREF_NUM_DATA_FILES, mNumDataFiles);
			edit.commit();
		}
	}

	/**
	 * Reset active raw data files
	 */
	public static void resetActiveDataFiles() {
		mDataFiles = new ArrayList<String>();
		mNumDataFiles = 0;
		setNumDataFiles(mNumDataFiles);
	}

	// flag to indicate that the DataStore has loaded all its saved values
	private static boolean mInitialized = false;

	// flag to indicate that the help message has been displayed at the very
	// first run
	private static boolean mFirstRunShown = false;

	// flag to indicate that the phone should vibrate on connections
	private static boolean mVibrate = true;

	// Flag to record if the service is currently recording and running
	private static boolean mRunning = false;

	// List of all the available paired Bluetooth sensors that are supported by
	// the app
	public static ArrayList<Sensor> mSensors = new ArrayList<Sensor>();

	// Context used by the main activity. Used for getting handles to write
	// files
	private static Context mContext = null;

	// Time in seconds to wait before alarming for inactivity
	public static int mStillnessDuration = Defines.DEFAULT_STILLNESS_WARNING_TIME;

	// Threshold in percent (ie 0-100) that heart rate must rise above the
	// trailing averager
	// before a stress event is triggered
	public static int mEmotionalEventThreshold = Defines.DEFAULT_EMOTIONAL_EVENT_TRIGGER_PERCENT;

	// Duration in minutes that the user must not have had any activity to
	// trigger
	// an emotional event
	public static int mEmotionalEventStillnessDuration = Defines.HEART_RATE_AVERAGE_TIME;

	// Most recent time when data recording started reading from sensors
	private static Time mStartRecordingTime = null;

	// Current score for daily activity
	private static int mActivityScore = 0;

	// Date that the activity score was last updated
	public static Time mActivityScoreDate = null;

	// SystemTime that the 1min thread last run (used for debugging to ensure
	// phone really waking up)
	public static long mThreadLastRunSystemTime = 0;

	// Previous week's activity scores
	// Position 0 is yesterday's score, position 6 is a week ago's score
	public static int[] mPreviousActivityScores = new int[Defines.NUM_DAYS_SCORE_TO_SAVE];

	/**
	 * Returns the current activity score
	 */
	public static int getActivityScore() {
		return mActivityScore;
	}

	/**
	 * Sets the current score to the given value
	 * 
	 * @param score
	 */
	public static void setActivityScore(int score) {

		mActivityScore = score;
	}

	/**
	 * Checks if the last time the activity score was updated was on a previous
	 * day. If so, will update all the previous scores, and reset the current
	 * day's score to 0 as a new day of recoding has started
	 */
	public static void checkForScoreReset() {
		if (mActivityScoreDate != null) {
			Time now = new Time();
			now.setToNow();

			// this is the easiest way to check for a day being different, there
			// is a downfall
			// in that when a new year starts, the previous week's data will be
			// cleared.
			if (now.yearDay != mActivityScoreDate.yearDay) {
				shiftScores(now.yearDay - mActivityScoreDate.yearDay);
			}
		} else {
			mActivityScoreDate = new Time();
		}
		mActivityScoreDate.setToNow();
		saveScore();

	}

	/**
	 * This function will shift all the saved values for previous day's activity
	 * scores to a new position in the array. When it is detected that a day or
	 * recoding has completed the data will be shifted appropriately. Handles
	 * skipping entire days or recording without issue
	 * 
	 * @param numDays
	 *            - the number of days to shift the data in the array
	 */
	private static void shiftScores(int numDays) {
		// If the number of days to shift is larger than the array size,
		// just clear out the entire array
		if (numDays >= Defines.NUM_DAYS_SCORE_TO_SAVE || numDays < 0) {
			for (int x = 0; x < Defines.NUM_DAYS_SCORE_TO_SAVE; x++) {
				mPreviousActivityScores[x] = 0;
			}
		} else {
			for (int x = Defines.NUM_DAYS_SCORE_TO_SAVE - 1; x >= numDays; x--) {
				mPreviousActivityScores[x] = mPreviousActivityScores[x - numDays];
			}
			for (int x = 0; x < numDays; x++) {
				mPreviousActivityScores[x] = 0;
			}
		}

		// set the previous day's score to the current score and reset
		// the current day's score to 0
		mPreviousActivityScores[0] = mActivityScore;
		mActivityScore = 0;
	}

	/**
	 * Increments the current score by 1. Checks if previous days need to be
	 * shifted over or the current day needs to be reset, then saves the data.
	 */
	public static void incrementActivityScore() {
		mActivityScore++;
		saveScore();
	}

	/**
	 * Decrements the current score by 1, then saves the changes
	 */
	public static void decrementActivityScore() {
		mActivityScore--;
		saveScore();
	}

	/**
	 * Saves the current and previous scores to the Shared Preferences
	 */
	private static void saveScore() {
		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
			edit.putInt(Defines.SHARED_PREF_ACTIVITY_SCORE, DataStore.mActivityScore);

			for (int x = 0; x < Defines.NUM_DAYS_SCORE_TO_SAVE; x++) {
				edit.putInt(Defines.SHARED_PREF_PREV_SCORE + x, DataStore.mPreviousActivityScores[x]);
			}

			if (mActivityScoreDate != null) {
				edit.putString(Defines.SHARED_PREF_SCORE_DATE, mActivityScoreDate.format2445());
			}

			edit.commit();
		}
	}

	/**
	 * Saves the thread runTime to Shared Preferences
	 */
	private static void saveThreadLastRunTime() {
		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
			edit.putLong(Defines.SHARED_PREF_THREAD_LAST_RUN_TIME, DataStore.mThreadLastRunSystemTime);

			edit.commit();
		}
	}

	public static void setThreadLastRunTime(long aSystemTime) {
		mThreadLastRunSystemTime = aSystemTime;
		saveThreadLastRunTime();
	}

	/**
	 * getStartRecordingTime
	 * 
	 * @return - The time that recording was started. May return null if
	 *         recording has not ever been started
	 */
	public static Time getStartRecordingTime() {
		return mStartRecordingTime;
	}

	/**
	 * Set the start of recording time to the current system time
	 */
	public static void setStartRecordingTime() {
		if (mStartRecordingTime == null) {
			mStartRecordingTime = new Time();
		}
		mStartRecordingTime.setToNow();

		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
			edit.putString(Defines.SHARED_PREF_START_TIME, mStartRecordingTime.format2445());
			edit.commit();
		}

	}

	public static Context getContext() {
		return mContext;
	}

	/**
	 * Reset all sensors to their default values
	 */
	public static void resetAll() {
		for (int x = 0; x < mSensors.size(); x++) {
			mSensors.get(x).reset();
		}
	}

	/**
	 * Reset all sensors to their default values
	 */
	public static void removeAllSensors() {
		mSensors.clear();
	}

	/**
	 * Reset all sensors to their default values
	 */
	public static void unsetPairedFlagAllSensors() {
		for (Sensor s : mSensors) {
			s.mPaired = false;
		}
	}

	/**
	 * Reset all sensors to their default values
	 */
	public static void removeNonPairedSensors() {
		ArrayList<Sensor> mSensorsTemp = new ArrayList<Sensor>();

		for (Sensor s : mSensors) {
			if (s.mPaired)
				mSensorsTemp.add(s);
		}

		mSensors = mSensorsTemp;
	}

	/**
	 * Takes a Bluetooth device name string and determines what type of sensor
	 * it is, then creates an object of that type of sensor and adds it to the
	 * list of available sensors.
	 * 
	 * @param name
	 *            - The Bluetooth sensor device name to be checked
	 * @param address
	 *            - the Bluetooth sensor MAC address
	 */
	public static Sensor checkAndAddSensor(Context aContext, String name, String address) {
		Sensor s = getSensor(name, address);
		
		if (s != null) {
			return s;
		}

		if (name.contains(Defines.ZEPHYR_DEVICE_NAME)) {
			if (Globals.IS_ZEPHYR_ENABLED) {
				s = new ZephyrSensor(name, address);
				mSensors.add(s);
			}
		} else if (name.contains(Defines.POLAR_DEVICE_NAME)) {
			if (Globals.IS_POLAR_ENABLED) {
				s = new PolarSensor(name, address);
				mSensors.add(s);
			}
		} else if (name.contains(Defines.WOCKET_DEVICE_NAME)) {
			if (Globals.IS_WOCKETS_ENABLED) {
				s = new WocketSensor(aContext, name, address);
				mSensors.add(s);
			}
		} else if (name.contains(Defines.ASTHMAPOLIS_DEVICE_NAME)) {
			if (Globals.IS_ASTHMAPOLIS_ENABLED) {
				s = new AsthmapolisSensor(name, address);
				mSensors.add(s);

			}
		}

		return s;
	}

	/**
	 * Returns the list of either all available sensor names, or the list of all
	 * enabled sensor names, depending on the param
	 * 
	 * @param onlyEnabled
	 *            - true to return all enabled sensor names, false to return all
	 *            sensor names
	 * @return - The list of sensor names, according to the given param
	 */
	public static ArrayList<CharSequence> getSensorNames(boolean onlyEnabled) {
		ArrayList<CharSequence> retVal = new ArrayList<CharSequence>();

		for (int x = 0; x < mSensors.size(); x++) {
			if (!onlyEnabled || mSensors.get(x).mEnabled) {
				retVal.add(mSensors.get(x).mName);
			}
		}
		return retVal;
	}

	/**
	 * A boolean array indicating the enabled state of all available sensors.
	 * True in the array indicated enabled, false indicated disabled.
	 * 
	 * @return the boolean array
	 */
	public static boolean[] getSensorStates() {
		boolean[] retVal = new boolean[mSensors.size()];

		for (int x = 0; x < mSensors.size(); x++) {
			retVal[x] = mSensors.get(x).mEnabled;
		}

		return retVal;
	}

	/**
	 * Returns the first sensor in the list of available sensors that is enabled
	 * 
	 * @return a Sensor object that is enabled
	 */
	public static Sensor getFirstSensor() {
		Sensor retVal = null;
		for (int x = 0; x < mSensors.size(); x++) {
			if (mSensors.get(x).mEnabled) {
				retVal = mSensors.get(x);
				break;
			}
		}

		return retVal;
	}

	/**
	 * Returns the Sensor object with the given Bluetooth name if a match is
	 * found in the list of available sensors. If no match, returns null.
	 * 
	 * @param name
	 *            - The Bluetooth name of the Sensor to find
	 * @return - The Sensor object that matches the name, or null
	 */
	public static Sensor getSensor(String name, String address) {
		for (int x = 0; x < mSensors.size(); x++) {
			if (mSensors.get(x).mName.equals(name)) {
				if (mSensors.get(x).mAddress.equals(address))
					return mSensors.get(x);
			}
		}
		return null;
	}

	/**
	 * Returns the Sensor object with the given Bluetooth name if a match is
	 * found in the list of available sensors. If no match, returns null.
	 * 
	 * @param name
	 *            - The Bluetooth name of the Sensor to find
	 * @return - The Sensor object that matches the name, or null
	 */
	public static Sensor getSensor(String name) {
		for (int x = 0; x < mSensors.size(); x++) {
			if (mSensors.get(x).mName.equals(name)) {
				return mSensors.get(x);
			}
		}
		return null;
	}

	/**
	 * Clear all the saved data from the saved preference file for a particular
	 * sensor (excluding enabled flag)
	 * 
	 * Assumes: Sensors exist and enabled.
	 * 
	 * @param id
	 *            The unique BT ID of the sensor
	 */
	public static void restoreSensorStats(Context aContext, Sensor s) {
		if (s != null && s.mAddress != null)
			s.restoreSensorStat(aContext);
	}

	public static void saveSensorStats(Context aContext, Sensor s) {
		s.saveSensorStats(aContext);
	}

	/**
	 * Loads all the data from Shared Preferences into member variables
	 * 
	 * @param context
	 *            The context to be used for writing files
	 */
	public static void init(Context context) {
		mContext = context;

		DataStore.removeAllSensors();

		// Look for various Bluetooth sensors and if they are found add them to
		// the list
		if ((Globals.IS_WOCKETS_ENABLED) || (Globals.IS_BLUETOOTH_ENABLED)) {
			if (BluetoothAdapter.getDefaultAdapter() != null) {
				BluetoothAdapter.getDefaultAdapter().enable();
				Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
				Iterator<BluetoothDevice> itr = devices.iterator();
				while (itr.hasNext()) {
					BluetoothDevice dev = itr.next();
					DataStore.checkAndAddSensor(context, dev.getName(), Util.removeColons(dev.getAddress()));
				}
			}
		}
		
		
		SharedPreferences prefs = context.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		int numEnabledSensors = prefs.getInt(Defines.SHARED_PREF_NUM_SENSORS, 0);
		Log.d(TAG, "got sensor number "+numEnabledSensors+" from shared prefs");
		for (int x = 0; x < numEnabledSensors; x++) {
			String sensorName = prefs.getString(Defines.SHARED_PREF_SENSOR + x, "");
			Sensor s = DataStore.getSensor(sensorName);
			DataStore.restoreSensorStats(context, s);
			if (s != null) {
				s.mEnabled = true;
			}
			Log.d(TAG, "Sensor set to enabled: " + sensorName);
		}

//		DataStore.mEmotionalEventThreshold = prefs.getInt(Defines.SHARED_PREF_EMOTION_THRESHOLD, Defines.DEFAULT_EMOTIONAL_EVENT_TRIGGER_PERCENT);

//		DataStore.mStillnessDuration = prefs.getInt(Defines.SHARED_PREF_INACTIVITY_TIME, Defines.DEFAULT_STILLNESS_WARNING_TIME);

		// Data files
		DataStore.mRawDataDir = prefs.getString(Defines.SHARED_PREF_RAW_DATA_DIR, Defines.DEFAULT_RAW_DATA_DIR);
		DataStore.mNumDataFiles = prefs.getInt(Defines.SHARED_PREF_NUM_DATA_FILES, 0);
		for (int i = 0; i < mNumDataFiles; i++) {
			mDataFiles = new ArrayList<String>();
			mDataFiles.add(prefs.getString(Defines.SHARED_PREF_DATA_FILE + "_" + i, ""));
		}
		for (int i = 0; i < mNumDataFiles; i++) {
			Log.d(TAG, "DATA FILE: " + mDataFiles.get(i));
		}

		DataStore.setActivityScore(prefs.getInt(Defines.SHARED_PREF_ACTIVITY_SCORE, 0));

		DataStore.mThreadLastRunSystemTime = prefs.getLong(Defines.SHARED_PREF_THREAD_LAST_RUN_TIME, -1);

		for (int x = 0; x < Defines.NUM_DAYS_SCORE_TO_SAVE; x++) {
			DataStore.mPreviousActivityScores[x] = prefs.getInt(Defines.SHARED_PREF_PREV_SCORE + x, 0);
		}

		String scoreDate = prefs.getString(Defines.SHARED_PREF_SCORE_DATE, "");

		if (!scoreDate.equals("")) {
			mActivityScoreDate = new Time();
			mActivityScoreDate.parse(scoreDate);
			// You MUST call normalize after parsing so that yearDay field will
			// be set correctly
			mActivityScoreDate.normalize(false);
		}

		DataStore.mFirstRunShown = prefs.getBoolean(Defines.SHARED_PREF_FIRST_RUN, false);

		DataStore.mVibrate = prefs.getBoolean(Defines.SHARED_PREF_VIBRATE, false);

		DataStore.mRunning = prefs.getBoolean(Defines.SHARED_PREF_RUNNNING, false);

		if (mRunning) {
			String startTime = prefs.getString(Defines.SHARED_PREF_START_TIME, "");
			if (!startTime.equals("")) {
				mStartRecordingTime = new Time();
				mStartRecordingTime.parse(startTime);
				mStartRecordingTime.normalize(false);
			}
		}

		// Check if the last update to the activity score was on a previous day
		checkForScoreReset();
		saveScore();

		mInitialized = true;
	}

	public static boolean getFirstRunShown() {
		return mFirstRunShown;
	}

	public static void setFirstRunShown() {
		mFirstRunShown = true;
		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

			edit.putBoolean(Defines.SHARED_PREF_FIRST_RUN, mFirstRunShown);
			edit.commit();
		}
	}

	public static boolean isVibrate() {
		return mVibrate;
	}

	public static void setIsVibrate(boolean value) {
		mVibrate = value;
		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

			edit.putBoolean(Defines.SHARED_PREF_VIBRATE, value);
			edit.commit();
		}
	}

	public static String getRawDataDir() {
		return mRawDataDir;
	}

	public static void setRawDataDir() {
		mRawDataDir = Defines.DEFAULT_RAW_DATA_DIR;
		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

			edit.putString(Defines.SHARED_PREF_RAW_DATA_DIR, mRawDataDir);
			edit.commit();
		}
	}

	public static boolean getInitialized() {
		return mInitialized;
	}

	public static boolean getRunning() {
		return mRunning;
	}

	/**
	 * Sets the flag that the service is running, and records the time the
	 * service was started
	 * 
	 * @param run
	 */
	public static void setRunning(boolean run) {
		mRunning = run;
		if (mRunning) {
			setStartRecordingTime();
		}

		if (mContext != null) {
			Editor edit = mContext.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
			edit.putBoolean(Defines.SHARED_PREF_RUNNNING, mRunning);
			edit.commit();
		}
	}

	/**
	 * set current time as the last time that runs the app
	 * 
	 * @param c
	 */
	public static void setLastTimeRun(Context c) {
		setLastTimeRun(c, new Date());
	}

	/**
	 * set date as the last time when runs the app
	 * 
	 * @param c
	 * @param date
	 */
	public static void setLastTimeRun(Context c, Date date) {
		SharedPreferences sp = c.getSharedPreferences(Defines.SHARED_PREF_LAST_RUN_DATE, Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		long currentDateMill = date.getTime();
		edit.putLong(Defines.SHARED_PREF_LAST_RUN_DATE, currentDateMill);
		edit.commit();
	}

	/**
	 * get the last time when runs the app in millis
	 * 
	 * @param c
	 * @return
	 */
	public static long getLastTimeRunInMills(Context c) {
		SharedPreferences sp = c.getSharedPreferences(Defines.SHARED_PREF_LAST_RUN_DATE, Context.MODE_PRIVATE);
		return sp.getLong(Defines.SHARED_PREF_LAST_RUN_DATE, -1);
	}

	/**
	 * get the last time when runs the app in Date
	 * 
	 * @param c
	 * @return
	 */
	public static Date getLastTimeRunInDate(Context c) {
		long lastTimeRuninMillis = getLastTimeRunInMills(c);
		if (lastTimeRuninMillis != -1)
			return new Date(getLastTimeRunInMills(c));
		return null;
	}

	/**
	 * get the minutes between now and the last time runs the app
	 * 
	 * @param c
	 * @return
	 */
	public final static int millisInOneMinute = 60 * 1000;

	public static int getMissingMinutesFromDate(Context c, Date date) {
		long timeInMillis = date.getTime();
		int missingMins = (int) ((timeInMillis - getLastTimeRunInMills(c)) / millisInOneMinute);
		Log.i(TAG, "Time gap is " + missingMins + " minutes");
		return missingMins;
	}

	// public static SensorDataInfo[] getSensorDataInfo(Context c){
	// String info =
	// c.getSharedPreferences(Defines.SHARED_PREF_USED_SENSORS_BY_DAY,
	// Context.MODE_PRIVATE)
	// .getString(Defines.SHARED_PREF_USED_SENSORS_BY_DAY, null);
	// if(info == null || info.equalsIgnoreCase("null"))
	// return null;
	// String[] details = info.split(",");
	// SensorDataInfo[] sensors = new SensorDataInfo[details.length/2];
	// for (int i = 0; i < sensors.length; i++) {
	// sensors[i] = new SensorDataInfo();
	// sensors[i].setMacID(details[2*i]);
	// sensors[i].setBodyLocation(details[2*i+1]);
	// sensors[i].setID(i);
	// }
	// return sensors;
	// }
	// public static void setSensorfDataInfo(Context c, SensorDataInfo sensor){
	// String info =
	// c.getSharedPreferences(Defines.SHARED_PREF_USED_SENSORS_BY_DAY,
	// Context.MODE_PRIVATE)
	// .getString(Defines.SHARED_PREF_USED_SENSORS_BY_DAY, null);
	// if(sensor != null){
	// if(!isSensorInfoExist(c,sensor)){
	// if(info != null && !info.equalsIgnoreCase("null"))
	// info += sensor.getMacID()+","+sensor.getBodyLocation()+",";
	// else
	// info = sensor.getMacID()+","+sensor.getBodyLocation()+",";
	// }
	// }
	// else
	// info = null;
	// c.getSharedPreferences(Defines.SHARED_PREF_USED_SENSORS_BY_DAY,
	// Context.MODE_PRIVATE)
	// .edit()
	// .putString(Defines.SHARED_PREF_USED_SENSORS_BY_DAY, info)
	// .commit();
	// SensorDataFileWriter sensorDataWriter = new SensorDataFileWriter(c);
	// sensorDataWriter.writeConfigInternal();
	// Log.i(TAG, "Saved sensor info --- "+ info);
	// }
	// public static void resetSwappedSensorsInfo(Context c){
	// setSensorDataInfo(c,null);
	// List<SwappedSensor> sensors = WocketInfoGrabber.getSwappedSensors(c);
	// for (SwappedSensor swappedSensor : sensors) {
	// SensorDataInfo sensorData = new SensorDataInfo();
	// sensorData.setSwappedData(swappedSensor);
	// DataStore.setSensorDataInfo(c, sensorData);
	// }
	// }
	// public static boolean isSensorInfoExist(Context c, SensorDataInfo
	// sensor){
	// if(sensor == null)
	// return false;
	// SensorDataInfo[] sensors = getSensorDataInfo(c);
	// if(sensors == null)
	// return false;
	// for (SensorDataInfo sensorDataInfo : sensors) {
	// if(sensorDataInfo.equals(sensor))
	// return true;
	// }
	// return false;
	// }
	// public static int getSensorInfoID(Context c, SensorDataInfo sensorInfo){
	// if(sensorInfo == null)
	// return -1;
	// SensorDataInfo[] sensors = getSensorDataInfo(c);
	// if(sensors == null)
	// return -1;
	// for (SensorDataInfo sensorDataInfo : sensors) {
	// if(sensorDataInfo.equals(sensorInfo))
	// return sensorDataInfo.getID();
	// }
	// return -1;
	// }
}
