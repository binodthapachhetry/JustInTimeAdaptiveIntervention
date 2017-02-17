/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Class used to create global constants that are used through out the app
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;


public class Defines {
	
	public final static String DEFAULT_RAW_DATA_DIR = "/sdcard/Protocol-Subject-Session/SensorFolder/";

	//in Minutes.  Number of minutes to use for calculating the trailing average HR
	public final static int HEART_RATE_AVERAGE_TIME = 5;
	//in bytes.  Max number of bytes to read from a Wocket
	public final static int MAX_WOCKET_PACKET_SIZE = 2400*5+960*2 + 1000; //TODO  //   5000; // TODO 2048;  
	
	//in bytes.  Max number of bytes to read from a Zepyhr sensor
	public final static int MAX_ZEPHYR_PACKET_SIZE = 60;
	
	//in bytes.  Max number of bytes to read from a Polar sensor
	public final static int MAX_POLAR_PACKET_SIZE = 16;
	
	//in x,y,z triplets.  Number of data points to store in memory before moving to flash
	public final static int MAX_WOCKET_POINTS = 500;
	//Number of data points to store in memory before moving to flash
	public final static int MAX_WOCKET_SUMMARIES = 500;
	
	public final static String SHARED_PREF_NAME = "ActivityMonitor";
	
	//strings used to read/store shared preferences
	public final static String SHARED_PREF_NUM_SENSORS = "numSensors";
	public final static String SHARED_PREF_EMOTION_THRESHOLD = "emotion";
	public final static String SHARED_PREF_FIRST_RUN = "firstRun";
	public final static String SHARED_PREF_VIBRATE = "vibrate";
	public final static String SHARED_PREF_INACTIVITY_TIME = "stillness";
	public final static String SHARED_PREF_SENSOR = "sensor";
	public final static String SHARED_PREF_ACTIVITY_SCORE = "score";
	public final static String SHARED_PREF_RUNNNING = "running";
	public final static String SHARED_PREF_START_TIME = "start";
	public final static String SHARED_PREF_PREV_SCORE= "previousScore";
	public final static String SHARED_PREF_SCORE_DATE = "scoreDate";
	public final static String SHARED_PREF_THREAD_LAST_RUN_TIME = "threadLastRunTime";

	public final static String SHARED_PREF_SENSOR_CONNECTION_ERRORS = "sensorConnectionErrors";
	public final static String SHARED_PREF_SENSOR_LAST_CONNECTION_TIME = "sensorLastConnectionTime";
	public final static String SHARED_PREF_SENSOR_BATTERY = "sensorBattery";
	public final static String SHARED_PREF_SENSOR_BYTES_RECEIVED = "sensorBytesReceived";
	public final static String SHARED_PREF_SENSOR_PACKETS_RECEIVED = "sensorPacketsReceived";

	public final static String SHARED_PREF_ZEPHRSENSOR_AVG_HR = "zephyrSensorAvgHR";
	public final static String SHARED_PREF_ZEPHRSENSOR_CURRENT_HR = "zephyrSensorCurrentHR";
	
	public final static String SHARED_PREF_RAW_DATA_DIR = "sp_raw_data_dir";
	public final static String SHARED_PREF_NUM_DATA_FILES = "sp_num_data_files";
	public final static String SHARED_PREF_DATA_FILE = "sp_data_file";

	public final static String SHARED_PREF_LAST_RUN_DATE = "lastTimeRun";
	public final static String SHARED_PREF_IS_PROMPT_TODAY = "isPromptToday";
	public final static String SHARED_PREF_USED_SENSORS_BY_DAY = "usedSensorsByDay";
	public final static String SHARED_PREF_IS_TRANSMITTED_TODAY = "isTransmittedToday";

	//Strings and values for the possible user settings of inactivity time
	///NOTE: the size of these 2 arrays, must match and items need to be in the same order!!!
	public final static CharSequence[] INACTIVITY_OPTION_STRINGS = {"Disable", "5 minutes", "30 minutes", "1 hour", "1.5 hours", "2 hours"};
	public final static int[] INACTIVTIY_OPTION_VALUES = {0,5,30,60,90,120};
	
	//Strings and values for the possible user settings of emotion threshold
	///NOTE: the size of these 2 arrays, must match and items need to be in the same order!!!
	public final static CharSequence[] EMOTION_OPTION_STRINGS = {"Disable", "5%", "10%", "15%", "20%", "25%"};
	public final static int[] EMOTION_OPTION_VALUES = {0,5,10,15,20,25};
	
	
	//String used to publish/receive a broadcast Intent
	public final static String NEW_DATA_READY_BROADCAST_STRING = "SensorDataUpdated";
	public final static String BOOT_COMPLETED_BROADCAST_STRING = "BootDone";
	
	//Number of times to try connecting to a device before resetting the BT adapter
	// Will be tried once a minute
	public final static int CONNECTION_ERRORS_BEFORE_RESET = 10;
	
	//Number of times BT device can fail a connection before being marked "No connection"
	public final static int NO_CONNECTION_LIMIT = 5;
	
	//in minutes.  Time without movement from Wockets required before alerting user
	public final static int DEFAULT_STILLNESS_WARNING_TIME = 2;
	//in percentage.  Amount above the trailing average that will trigger an emotional event
	public final static int DEFAULT_EMOTIONAL_EVENT_TRIGGER_PERCENT = 10;
	//Wocket summary value minimum that is used to measure no movement.  Below this value
	// is considered to be a "sitting" or idle position
	public final static int WOCKET_STILLNESS_MIN = 700;
	
	//This is the minimum heart rate value that will trigger a point increase
	// in the daily activity score.
	public final static int MINIMUM_HEART_RATE_DURING_EXERCISE = 100;
	
	//in minutes.  Minimum time between alerts to the user that there hasn't been any movement.
	public final static int MINUTES_BETWEEN_STILLNESS_ALERT = 30;
	//in minutes Minimum time between alerts to user that there has been an emotional event
	public final static int MINUTES_BETWEEN_EMOTIONAL_ALERT = 5;
	
	//Number of days worth of old activity scores to save
	public final static int NUM_DAYS_SCORE_TO_SAVE = 7;
	
	//value for a fully charged Wocket battery
	public final static int WOCKET_LOW_BATTERY_LEVEL = 600;

	//Strings used to identify a sensor based on its name.  These strings will
	// always be contained in the sensor's name in some form
	public final static String ASTHMAPOLIS_DEVICE_NAME = "Asthmapolis";
	public final static String ZEPHYR_DEVICE_NAME = "HXM";
	public final static String POLAR_DEVICE_NAME = "Polar";
	public final static String WOCKET_DEVICE_NAME = "Wocket";
	public final static String ASTHMOPOLIS_DEVICE_NAME = "Asthmpolis";
}
