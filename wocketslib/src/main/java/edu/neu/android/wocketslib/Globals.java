package edu.neu.android.wocketslib;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.utils.Log;

/**
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.apache.http.util.EntityUtils;
 import android.content.SharedPreferences;
 import android.telephony.TelephonyManager;
 import edu.neu.android.wocketslib.activities.sensorswap.MyWockets;
 import edu.neu.android.wocketslib.sensormonitor.Arbitrater;
 import edu.neu.android.wocketslib.sensormonitor.ArbitraterInterface;
 import edu.neu.android.wocketslib.sensormonitor.DataStore;
 import edu.neu.android.wocketslib.support.AppInfo;
 import edu.neu.android.wocketslib.utils.Log;
 */


/**
 * Globals that can and should be set by code calling the library (in the Application Manager onCreate)
 * @author SSI
 *
 */
public class Globals extends LibGlobals {

	//	private static final String TAG = "Globals";
	public static String KMLStartDate;
	public static String KMLEndDate;
	// Print debugging information defaults to off
	public static boolean IS_DEBUG = false;
	// These MUST be set in calling code
	public static String PACKAGE_NAME = "edu.neu.android.wocketslib";
	public static String STUDY_NAME = "TEMPLE";
	public static String STUDY_SERVER_NAME = "WocketsLib";
//	public static final String SURVEY = "USCStudy";

	// By default save logs to internal memory
	public static Boolean IS_LOG_EXTERNAL = true;

	// By default save mhealth data to external memory
	public static Boolean IS_MHEALTH_EXTERNAL = true;

	// Directory location information that should be changed by the main app using the library
	public static String APP_DIRECTORY = "." + Globals.STUDY_NAME;

	public static String PARTICIPANT_ID = "PARTICIPANT_ID";

	// These variables are most easily set by calling initDataDirectories(aContext) from the main app after setting APP_DIRECTORY
	public static String LOG_DIRECTORY = APP_DIRECTORY + File.separator + "logs";
	public static String DATA_DIRECTORY = APP_DIRECTORY + File.separator + "data";

	public static String DATA_MHEALTH_DIRECTORY = DATA_DIRECTORY + File.separator + "mhealth";
	public static String DATA_MHEALTH_SENSORS_DIRECTORY = DATA_MHEALTH_DIRECTORY + File.separator + "sensors";
	public static String APP_DATA_DIRECTORY = APP_DIRECTORY + File.separator + "appdata";
	public static String SURVEY_LOG_DIRECTORY = APP_DIRECTORY + File.separator + "survey";
	public static String UPLOADS_DIRECTORY = APP_DIRECTORY + File.separator + "uploads";
	public static String BACKUP_DIRECTORY = APP_DIRECTORY + File.separator + "backup";
	public static String INTERNAL_DIRECTORY_PATH = "";
	public static String EXTERNAL_DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String APP_INTERNAL_DIRECTORY_PATH = INTERNAL_DIRECTORY_PATH + File.separator + APP_DIRECTORY;
	public static String APP_EXTERNAL_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH + File.separator + APP_DIRECTORY;
	public static final String KEY_LAST_UPTIME = "KEY_LAST_UPTIME";
	public static final String KEY_LAST_DOWNTIME = "KEY_LAST_DOWNTIME";

	public static final String KEY_LAST_SURVEY_INTERACTION_TIME = "KEY_LAST_SURVEY_INTERACTION_TIME";

	// New mHealthFormat variables
	public static String ORIGINAL_RAW_DIRECTORY = DATA_DIRECTORY + File.separator + "OriginalRaw";
	public static String MASTER_SYNCED_DIRECTORY = DATA_DIRECTORY + File.separator + "MasterSynced";
	public static String METADATA_DIRECTORY = DATA_DIRECTORY + File.separator + "Metadata";
	public static boolean UPLOAD_MHEALTH_MASTER_DIRECTORY = true;
	public static boolean UPLOAD_MHEALTH_RAW_DIRECTORY = true;
	public static boolean UPLOAD_MHEALTH_METADATA_DIRECTORY = true;

	public static boolean IS_ANNOTATION_EXTERNAL = false;
	public static boolean IS_SENSOR_DATA_EXTERNAL = false;

	public static String SWAPPED_WOCKETS_JSON_FILE_PATH = EXTERNAL_DIRECTORY_PATH + File.separator
			+ DATA_DIRECTORY + File.separator + swappedInfoFile;
	public static String WOCKETS_INFO_JSON_FILE_PATH = EXTERNAL_DIRECTORY_PATH + File.separator
			+ DATA_DIRECTORY + File.separator + sensorInfoFile;
	public static boolean BACKUP_UPLOADS_EXTERNAL = false;
	public static boolean IS_COPY_TO_UPLOAD_DIRECTORY = true;
	public static boolean IS_SEND_WOCKET_DATA = false;
	public static String UNIQUE_LOG_STRING = "";

	// Upload interval for status info defaults to 1 hour
	public static long JSON_DATA_UPLOAD_INTERVAL = LibGlobals.MINUTES_60_IN_MS;
	public static long MHEALTH_DATA_UPLOAD_INTERVAL = LibGlobals.HOURS24_MS;
	public static long MHEALTH_DATA_UPLOAD_MAX_INTERVAL = LibGlobals.HOURS36_MS;
	public static long WAKEFUL_SERVICE_INTERVAL = LibGlobals.MINUTES_15_IN_MS;

	public static boolean IS_UPLOADING_SERVICE_RUNNING = false;


	public static int MIN_ZIP_OF_ZIP_JSON_FILES = 20;
	public static int MAX_ZIP_OF_ZIP_JSON_FILES = 100;

	// Percentage of files that must be uploading successfully to keep trying to send more at any one time.
	public static float UPLOAD_SUCCESS_PERCENTAGE = .85f;

	// URI information that must be set by the main app using the library. No defaults used to be sure errors are created if these are not set
	public static Uri WOCKETS_SP_URI = null; //Uri.parse("content://edu.neu.android.wocketslib");
	public static Uri CONTENT_URI = null; //Uri.parse(uri+".support");

	// Unique IDs used by the program for notifications (these could use better names)
	public static int SURVEY_NOTIFICATION_ID = 12345;
	public static int WOCKETS_NOTIFICATION_ID = 23456;

	// The default message presented when library service runs to read data
	public static String MSG_READING_SENSOR_DATA_NOTIFICATION = "Reading sensor data";
	public static String MSG_UPLOADING_DATA_NOTIFICATION = "Uploading sensor data files";

	// How often service runs (could use a better name)
	public static int BLUETOOTH_SENSOR_SERVICE_RUNNING_PERIOD_MS = 1000*60; // 1 minute default

	// Start the service automatically when the user successfully logs in to the server. Default
	// to false because not sure if this will impact CITY yet.
	public static boolean IS_AUTO_START_SERVICE_AT_AUTHORIZATION = false;

	public static int MIN_HEIGHT_SURVEY_ITEM = 40;

	// Key contact information
	public static String FAQ_URL = "http://mhealth.ccs.neu.edu";
	public static String NEWS_URL = "http://mhealth.ccs.neu.edu";
	public static String DEFAULT_MAIL_USERNAME = null; //"wocketssmtp@gmail.com";
	public static String DEFAULT_MAIL_PASSWORD = null; //"W0CKET$mtp";
	public static String DEFAULT_CC = null; //"wocketsresearchstudy@gmail.com";
	public static String DEFAULT_PING_ADDRESS = null; //"http://cityproject.media.mit.edu/ping.php";
	public static String HOTLINE_NUMBER = null; //"6172753933";

	// Determine what the WocketsLib code runs. Default everything to off.
	public static boolean IS_WOCKETS_ENABLED = false;
	public static boolean IS_WOCKETS_SUMMARY_NOTIFICATION_ENABLED = true;
	public static boolean IS_BLUETOOTH_ENABLED = false;
	public static boolean IS_AUTO_ENABLE_BT_SENSORS = false;
	public static boolean IS_ASTHMAPOLIS_ENABLED = false;
	public static boolean IS_POLAR_ENABLED = false;
	public static boolean IS_ZEPHYR_ENABLED = false;
	public static boolean IS_LOCATION_ENABLED = false;
	public static boolean IS_READING_SENSOR_ENABLED = true; // Show notification when reading sensor
	public static boolean IS_AUDIO_AMPLITUDE_MONITORING_ENABLED = false;
	public static boolean IS_SOUND_CLIP_RECORDING_ENABLED = false;
	public static boolean IS_RECORDING_PHONE_ACCEL_ENABLED = false;
	public static boolean IS_RECORDING_PHONE_ACCEL_MAGNITUDE_PER_SECOND_ENABLED = false;
	public static boolean IS_RECORDING_RAW_PHONE_ACCEL_DATA_ENABLED = false;
	public static boolean IS_LOG_PHONE_NUMS_ENABLED = true; //TODO turn this off in CITY-M
	public static boolean IS_RECORDING_PHONE_CALLS = false;
	public static boolean IS_LOG_PHONE_BATTERY_ENABLED = false;
	public static boolean IS_LOG_PHONE_RINGER_MODE_ENABLE = false;
	public static boolean IS_SLEEP_DETECTION_ENABLED = false;
	public static boolean IS_VERSION_CHECK_AVAILABLE = false;
	public static boolean IS_RECORDING_LIGHT_SENSOR_ENABALED = false;
	public static boolean IS_MAX_AUDIO_SAVING_ENABALED = false;
	public static boolean IS_LOG_SHARED_PREFERENCES_SIZE_ENABLED = false;
	public static boolean IS_SERVICE_DOWN = false;
	public static boolean IS_TIME_CHANGED = false;
	public static boolean IS_BACK_UP_DATABASE_ENABLED = false;
	public static boolean IS_BLE_ENABLED = false; //Bluetooth Low Energy
	public static boolean IS_AUTHORIZATION_NEEDED = true;

	// Globals related to Android Wear Integration
	public static boolean IS_WEAR_APP_ENABLED = false;
	public static boolean IS_MICRO_EMA_ENABLED = false;
	public static boolean IS_MINUTE_TRIGGER_REQUIRED = false;
	public static boolean IS_LOST_CONNECTION_NOTIFICATION_ENABLED = false;
	public static final String CAPABILITY_NAME = "wocketslib";
	public static final String LAST_WATCH_IN_CONNECTION_TIME = "LAST_WATCH_IN_CONNECTION_TIME";
	public static final int WATCH_LOST_CONNECTION_THRESHOLD = 1; //hours

	public static boolean IS_GPS_ENCRYPTION_ENABLED = false;
	public static boolean IS_PHONE_ID_ENCRYPTION_ENABLED = false;

	public static int DEFAULT_SAMPLING_RATE = 40;
	public static long BLE_SCAN_DURATION = 2 * 1000L; //Bluetooth Low Energy
	public static long BLE_NEXT_SCAN_TIME = 10 * 1000L; //Bluetooth Low Energy

	//Sensor Types
	public static String SENSOR_TYPE_PHONE_ACCELEROMETER = "PhoneAccelerometer";
	public static String SENSOR_TYPE_WOCKET = "Wocket";
	public static String SENSOR_TYPE_ACTIGRAPH_GT3XPLUS = "ActigraphGT3XPLUS";
	public static String SENSOR_TYPE_ACTIVPAL_TM = "ActivPALTM";
	public static String SENSOR_TYPE_GENEACTIV = "GENEActiv";
	public static String SENSOR_TYPE_OMRON = "Omron";
	public static String SENSOR_TYPE_GPS = "GPS";
	public static String SENSOR_TYPE_ZEPHYR_HxMBT = "ZephyrHxMBT";
	public static String SENSOR_TYPE_MICROPHONE = "MicAudio";
	public static String SENSOR_TYPE_PHONE_LIGHT = "PhoneLight";
	public static String SENSOR_TYPE_BLE = "BlueToothLowEnergy";
	public static String SENSOR_TYPE_OUS_STATUS = "OUS_STATUS" ;
	public static String SENSOR_TYPE_OUS_MOTION_EVENTS = "OUS_MOTION_EVENTS";

	// TODO
	// We need to check if this is only active in the BTSensorSerivice if only plugged in!
	public static int MIN_MS_FOR_SENSING_WHEN_PHONE_PLUGGED_IN = 0;

	// Server addresses for data exchange. Set in code
	public static String DEFAULT_SERVER_ADDR = "http://wockets.ccs.neu.edu:8080/";
	public static String PHP_DEFAULT_SERVER_ADDR = "http://wockets.ccs.neu.edu/";

	// These variable are most easily set by calling initServerWebCalls() from the main app after setting DEFAULT_SERVER_ADDR and PHP_DEFAULT_SERVER_ADDR
	public static String SERVER_ADDRESS_PID = DEFAULT_SERVER_ADDR + "Wockets/android/getParticipantId.html";
	public static String SERVER_REGISTER_PARTICIPANT = DEFAULT_SERVER_ADDR + "Wockets/android/registerParticipantPhone.html";
	public static String SERVER_GET_PID = DEFAULT_SERVER_ADDR + "Wockets/android/getParticipantIdFromName.html";
	public static String SERVER_GET_PHONE_ID = DEFAULT_SERVER_ADDR + "Wockets/android/getPhoneIdFromName.html";

	//public static String PHP_SERVER_ADDRESS_PID = PHP_DEFAULT_SERVER_ADDR + "getParticipantId.php";
	public static String PHP_SERVER_REGISTER_PARTICIPANT = PHP_DEFAULT_SERVER_ADDR + "registerParticipantPhone.php";
	public static String PHP_SERVER_GET_PID = PHP_DEFAULT_SERVER_ADDR + "getParticipantIdFromName2.php";
	public static String PHP_SERVER_GET_PHONE_ID = PHP_DEFAULT_SERVER_ADDR + "getPhoneIdFromName2.php";
	public static String PHP_SERVER_UPLOAD_FILE_ZIPPED = PHP_DEFAULT_SERVER_ADDR + "uploadFileZipped2.php";
	public static String UPLOAD_SERVER_DIR_NAME = "srv";

	public static String GET_WOCKETS_DETAIL_URL = DEFAULT_SERVER_ADDR + "Wockets/android/getWocketsDetail.html";
	public static String POST_ANDROID_DATA_LOG_URL = DEFAULT_SERVER_ADDR + "Wockets/AndroidDataLog.html";
	public static String URL_FILE_UPLOAD_SERVLET = DEFAULT_SERVER_ADDR + "FileUploader/Commonsfileuploadservlet";
	public static String URL_GET_WOCKETS_DETAIL = DEFAULT_SERVER_ADDR + "Wockets/android/getWocketsDetail.html?pId=6809";

	// These variables are used by SftpFileGrabber for SFTP login information
	public static String SFTP_SERVER_USER_NAME = "sftpdownload";
	public static String SFTP_SERVER_PASSWORD  = "$parRow1ark";
	public static String SFTP_SERVER_URL       = "wockets.ccs.neu.edu";

	// Passwords to unlock the setup screens
	public static String PW_STAFF_PASSWORD = "staff";
	public static String PW_SUBJECT_PASSWORD = "setup";
	public static String PW_QUIT_PASSWORD = "quit";

	// Prompting system globals (TODO some could use better names)
	public static int AUDIO_PROMPT_START_HOUR = 7;
	public static int AUDIO_PROMPT_END_HOUR = 23;
	public static int DEFAULT_PROMPTS_PER_DAY = 0;
	public static int DEFAULT_START_HOUR = 8;
	public static int DEFAULT_END_HOUR = 22;
	public static int MIN_MS_BETWEEN_SCHEDULED_PROMPTS = 15 * 60 * 1000; // 15 minutes
	public static int REPROMPT_TIMES = 2;
	public static long REPROMPT_DELAY_MS = 6*60*1000;
	public static int MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS = 4*60*1000; // 8min
	public static int MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS = 2*60*1000; // 4 min
	public static int MIN_TIME_BETWEEN_ALARMS_MS = 15 * 60 * 1000; //TODO
	public static long TARGET_PROMPT_WINDOW_INTERVAL_MS = 90 * 60 * 1000;
	public static boolean IS_POPPING_SURVEY_BACK_ENABLED = false;
	public static long TIMING_FOR_POPPING_SURVEY_BACK = 30 * 1000; // 30 seconds

	public static String LAST_TIME_VERSION_CHECK = "LAST_TIME_VERSION_CHECK";   //The last time that the app checks its version on Google Play Store

	public static String IS_SENDING_OBFUSCATED_ID = "IS_SENDING_OBFUSCATED_ID";

	public static boolean IS_DEMO = false;

	public static boolean IS_GZIP_INDIVIDUAL_CSV = false;

	public static boolean IS_UPLOAD_ONLY_WIFI = false;

	// Algorithm parameters
	public static int MAX_ACTIVITY_DATA_SCALE = 3500;

	// TODO what is this?
	public static boolean isTrackPrompts = false;

	// Default available WocketLib apps and order of presentation
	public static String[] ALL_APP_KEYS = { THISWEEK, SWAP, LEVEL3PA, GUIDELINES, STATUS, GETHELP, SURVEY };

	// Only apps that are prompted (TODO Not clear what this is)
	public static String[] ALL_APP_KEYS_PROMPTED = { LEVEL3PA };

	public static HashMap<String, Class<?>> classFromKey = new HashMap<String, Class<?>>();

	static {
		classFromKey.put(THISWEEK, edu.neu.android.wocketslib.activities.thisweek.ThisWeekActivity.class);
		classFromKey.put(SWAP, edu.neu.android.wocketslib.activities.sensorswap.SwapMenuActivity.class);
		classFromKey.put(LEVEL3PA, edu.neu.android.wocketslib.activities.paema.Level3PAActivity.class);
		classFromKey.put(GUIDELINES, edu.neu.android.wocketslib.activities.guidelines.GuidelinesActivity.class);
		classFromKey.put(STATUS, edu.neu.android.wocketslib.activities.sensorstatus.SimpleStatusScreenActivity.class);
		classFromKey.put(GETHELP, edu.neu.android.wocketslib.activities.helpcomment.GetHelpActivity.class);
		//classFromKey.put(SURVEY, edu.neu.android.wocketslib.USCMobileteens.StartSurveyActivity.class);
	}

	public static Class<?> getClassFromKey(String key) {
		return classFromKey.get(key);
	}

//	public static void InitAppInfo(Context aContext) {
////		AppInfo.Save(aContext, THISWEEK, "Right Now in Study", PACKAGE_NAME, ".thisweek.ThisWeekActivity");
//		AppInfo.Save(aContext, Globals.THISWEEK, "Right Now in Study", Globals.PACKAGE_NAME, ".activities.wocketsnews.NewsViewerActivity");
//
//		AppInfo.Save(aContext, Globals.SWAP, "Swap/Change Locations", Globals.PACKAGE_NAME, ".activities.sensorswap.SwapMenuActivity");
//		AppInfo.Save(aContext, Globals.LEVEL3PA, "Track Your Activity", Globals.PACKAGE_NAME, ".activities.paema.Level3PAActivity");
//		AppInfo.Save(aContext, Globals.GUIDELINES, "Key Actions/Tips", Globals.PACKAGE_NAME, ".activities.guidelines.GuidelinesActivity");
//		AppInfo.Save(aContext, Globals.STATUS, "Wockets Status", Globals.PACKAGE_NAME, ".activities.sensorstatus.SimpleStatusScreenActivity");
////		AppInfo.Save(aContext, SENDCOMMENTS, "Send Comments", PACKAGE_NAME, ".activities.helpcomment.SendCommentsActivity");
//		AppInfo.Save(aContext, Globals.GETHELP, "Question or Problem?", Globals.PACKAGE_NAME, ".activities.helpcomment.GetHelpActivity");
//		AppInfo.Save(aContext, Globals.SURVEY, "Mobile Teens", Globals.PACKAGE_NAME, ".emasurvey.SurveyActivity");
//	}


	//TODO Change this so these are part of shared preference
//	public final static String dominantSide = "Right";
//	public final static String nonDominantSide = "Left";


//	public static String serverAddress = "http://wockets.ccs.neu.edu:8080/Wockets/android/getWocketsDetail.html";
//	public static String serverAddress = "http://wockets.ccs.neu.edu:9080/Wockets/android/getWocketsDetail.html";

	public static void initDataDirectories(Context aContext)
	{
		LOG_DIRECTORY = APP_DIRECTORY + File.separator + "logs";
		DATA_DIRECTORY = APP_DIRECTORY + File.separator + "data";
		DATA_MHEALTH_DIRECTORY = DATA_DIRECTORY + File.separator + "mhealth";
		DATA_MHEALTH_SENSORS_DIRECTORY = DATA_MHEALTH_DIRECTORY + File.separator + "sensors";
		APP_DATA_DIRECTORY = APP_DIRECTORY + File.separator + "appdata";
		SURVEY_LOG_DIRECTORY = APP_DIRECTORY + File.separator + "survey";
		UPLOADS_DIRECTORY = APP_DIRECTORY + File.separator + "uploads";
		BACKUP_DIRECTORY = APP_DIRECTORY + File.separator + "backup";
		INTERNAL_DIRECTORY_PATH = aContext.getFilesDir().getAbsolutePath();
		EXTERNAL_DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
		APP_INTERNAL_DIRECTORY_PATH = INTERNAL_DIRECTORY_PATH + File.separator + APP_DIRECTORY;
		APP_EXTERNAL_DIRECTORY_PATH = EXTERNAL_DIRECTORY_PATH + File.separator + APP_DIRECTORY;
		ORIGINAL_RAW_DIRECTORY = DATA_DIRECTORY + File.separator + "OriginalRaw";
		MASTER_SYNCED_DIRECTORY = DATA_DIRECTORY + File.separator + "MasterSynced";
		METADATA_DIRECTORY = DATA_DIRECTORY + File.separator + "Metadata";
		mHealthFormat.init(STUDY_NAME, IS_MHEALTH_EXTERNAL);
	}

//	public static void initServerWebCalls()
//	{
//		SERVER_ADDRESS_PID = DEFAULT_SERVER_ADDR + "Wockets/android/getParticipantId.html";
//		SERVER_REGISTER_PARTICIPANT = DEFAULT_SERVER_ADDR + "Wockets/android/registerParticipantPhone.html";
//		SERVER_GET_PID = DEFAULT_SERVER_ADDR + "Wockets/android/getParticipantIdFromName.html";
//		SERVER_GET_PHONE_ID = DEFAULT_SERVER_ADDR + "Wockets/android/getPhoneIdFromName.html";
//
//		//PHP_SERVER_ADDRESS_PID = PHP_DEFAULT_SERVER_ADDR + "getParticipantId.php";
//		PHP_SERVER_REGISTER_PARTICIPANT = PHP_DEFAULT_SERVER_ADDR + "registerParticipantPhone.php";
//		PHP_SERVER_GET_PID = PHP_DEFAULT_SERVER_ADDR + "getParticipantIdFromName.php";
//		PHP_SERVER_GET_PHONE_ID = PHP_DEFAULT_SERVER_ADDR + "getPhoneIdFromName.php";
//
//		GET_WOCKETS_DETAIL_URL = DEFAULT_SERVER_ADDR + "Wockets/android/getWocketsDetail.html";
//		POST_ANDROID_DATA_LOG_URL = DEFAULT_SERVER_ADDR + "Wockets/AndroidDataLog.html";
//		URL_FILE_UPLOAD_SERVLET = DEFAULT_SERVER_ADDR + "FileUploader/Commonsfileuploadservlet";
//		URL_GET_WOCKETS_DETAIL = DEFAULT_SERVER_ADDR + "Wockets/android/getWocketsDetail.html?pId=6809";
//	}

	public static void logKeyVariables(String aModuleName)
	{
		Log.o(aModuleName, "IS_DEBUG",Boolean.toString(Globals.IS_DEBUG));

		Log.o(aModuleName, "PACKAGE_NAME",Globals.PACKAGE_NAME);
		Log.o(aModuleName, "STUDY_NAME",Globals.STUDY_NAME);
		Log.o(aModuleName, "STUDY_SERVER_NAME",Globals.STUDY_SERVER_NAME);
		Log.o(aModuleName, "IS_LOG_EXTERNAL", Boolean.toString(Globals.IS_LOG_EXTERNAL));
		Log.o(aModuleName, "APP_DIRECTORY",Globals.APP_DIRECTORY);
		Log.o(aModuleName, "APP_EXTERNAL_DIRECTORY_PATH",Globals.APP_EXTERNAL_DIRECTORY_PATH);
		Log.o(aModuleName, "BACKUP_UPLOADS_EXTERNAL", Boolean.toString(Globals.BACKUP_UPLOADS_EXTERNAL));
		Log.o(aModuleName, "UNIQUE_LOG_STRING",Globals.UNIQUE_LOG_STRING);
		Log.o(aModuleName, "BLUETOOTH_SENSOR_SERVICE_RUNNING_PERIOD_MS", Integer.toString(Globals.BLUETOOTH_SENSOR_SERVICE_RUNNING_PERIOD_MS));

		Log.o(aModuleName, "IS_WOCKETS_ENABLED", Boolean.toString(Globals.IS_WOCKETS_ENABLED));
		Log.o(aModuleName, "IS_WOCKETS_SUMMARY_NOTIFICATION_ENABLED", Boolean.toString(Globals.IS_WOCKETS_SUMMARY_NOTIFICATION_ENABLED));
		Log.o(aModuleName, "IS_BLUETOOTH_ENABLED", Boolean.toString(Globals.IS_BLUETOOTH_ENABLED));
		Log.o(aModuleName, "IS_ASTHMAPOLIS_ENABLED", Boolean.toString(Globals.IS_ASTHMAPOLIS_ENABLED));
		Log.o(aModuleName, "IS_POLAR_ENABLED", Boolean.toString(Globals.IS_POLAR_ENABLED));
		Log.o(aModuleName, "IS_ZEPHYR_ENABLED", Boolean.toString(Globals.IS_ZEPHYR_ENABLED));
		Log.o(aModuleName, "IS_LOCATION_ENABLED", Boolean.toString(Globals.IS_LOCATION_ENABLED));
		Log.o(aModuleName, "IS_AUDIO_AMPLITUDE_MONITORING_ENABLED", Boolean.toString(Globals.IS_AUDIO_AMPLITUDE_MONITORING_ENABLED));
		Log.o(aModuleName, "IS_SOUND_CLIP_RECORDING_ENABLED", Boolean.toString(Globals.IS_SOUND_CLIP_RECORDING_ENABLED));
		Log.o(aModuleName, "IS_LOG_PHONE_NUMS_ENABLED", Boolean.toString(Globals.IS_LOG_PHONE_NUMS_ENABLED));
		Log.o(aModuleName, "MIN_MS_FOR_SENSING_WHEN_PHONE_PLUGGED_IN",Integer.toString(Globals.MIN_MS_FOR_SENSING_WHEN_PHONE_PLUGGED_IN));

		Log.o(aModuleName, "AUDIO_PROMPT_START_HOUR",Integer.toString(Globals.AUDIO_PROMPT_START_HOUR));
		Log.o(aModuleName, "AUDIO_PROMPT_END_HOUR",Integer.toString(Globals.AUDIO_PROMPT_END_HOUR));
		Log.o(aModuleName, "DEFAULT_PROMPTS_PER_DAY",Integer.toString(Globals.DEFAULT_PROMPTS_PER_DAY));
		Log.o(aModuleName, "DEFAULT_START_HOUR",Integer.toString(Globals.DEFAULT_START_HOUR));
		Log.o(aModuleName, "DEFAULT_END_HOUR",Integer.toString(Globals.DEFAULT_END_HOUR));
		Log.o(aModuleName, "MIN_MS_BETWEEN_SCHEDULED_PROMPTS",Integer.toString(Globals.MIN_MS_BETWEEN_SCHEDULED_PROMPTS));
		Log.o(aModuleName, "REPROMPT_TIMES",Integer.toString(Globals.REPROMPT_TIMES));
		Log.o(aModuleName, "REPROMPT_DELAY_MS",Long.toString(Globals.REPROMPT_DELAY_MS));
		Log.o(aModuleName, "MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS",Integer.toString(Globals.MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS));
		Log.o(aModuleName, "MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS",Integer.toString(Globals.MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS));
		Log.o(aModuleName, "MIN_TIME_BETWEEN_ALARMS_MS",Integer.toString(Globals.MIN_TIME_BETWEEN_ALARMS_MS));
		Log.o(aModuleName, "TARGET_PROMPT_WINDOW_INTERVAL_MS",Long.toString(Globals.TARGET_PROMPT_WINDOW_INTERVAL_MS));
	}

	public static void initSwapEventDirectories(Context aContext) {
		if (!IS_SENSOR_DATA_EXTERNAL) {
			SWAPPED_WOCKETS_JSON_FILE_PATH = INTERNAL_DIRECTORY_PATH
					+ File.separator + DATA_DIRECTORY + File.separator
					+ swappedInfoFile;

			WOCKETS_INFO_JSON_FILE_PATH = INTERNAL_DIRECTORY_PATH
					+ File.separator + DATA_DIRECTORY + File.separator
					+ sensorInfoFile;
		}
	}
}