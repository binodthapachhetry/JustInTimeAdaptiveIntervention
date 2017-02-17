package edu.neu.android.wocketslib;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.broadcastreceivers.BroadcastReceiverProcessor;
import edu.neu.android.wocketslib.sensormonitor.Arbitrater;

public class LibGlobals {
//	private static final String TAG = "LibGlobals";

	// Globals that are internal to the library and never need to be changed 
	public static final Date REASONABLE_DATE = new Date(2011-1900,0,1); // Year 0 is 1900

	//	public static final String GLOBALS_SHARED_PREF_NAME = "WocketsGlobalsSharedPreferences"; // TODO needed? 

	// Globals for Wockets
	public static final String WOCKETS_REFRESH_DATA_ACTION = "REFRESH_DATA_ACTION";

	// Globals for Wockets swap 
	public static final String SWAP_TAG = "Swap";
	public static final String[] locations = {"Right Wrist", "Right Ankle", "Left Wrist", "Left Ankle", "Right Pocket", "Left Pocket"};

	// Key Wocket file names 
	public static String sensorInfoFile = "WocketsInfo.json";
	public static String swappedInfoFile = "SwapEvent.json";
	public static String swappedHRFile = "ZephyrInfo.json";

	// All main apps must have a name. This is used in the shared preference and when determining if apps are active 
	public static final String TUTORIALPLAYER = "Tutorial";
	public static final String LEVEL3PA = "Level3PA";
	public static final String SWAP = "Swap";
	public static final String GUIDELINES = "Guidelines";
	public static final String STATUS = "Status";
	public static final String THISWEEK = "ThisWeek";
	public static final String SENDCOMMENTS = "SendComments";
	public static final String GETHELP = "GetHelp";
	public static final String SURVEY = "Survey";

	// Time constants
	public static final int MINUTES_1_IN_MS   =   1 * 60 * 1000;
	public static final int MINUTES_1POINT5_IN_MS   =   1 * 90 * 1000;
	public static final int MINUTES_2_IN_MS   =   2 * 90 * 1000;
	public static final int MINUTES_3_IN_MS   =   3 * 60 * 1000;
	public static final int MINUTES_4_IN_MS   =   4 * 60 * 1000;
	public static final int MINUTES_5_IN_MS   =   5 * 60 * 1000;
	public static final int MINUTES_6_IN_MS   =   6 * 60 * 1000;
	public static final int MINUTES_7_IN_MS   =   7 * 60 * 1000;
	public static final int MINUTES_10_IN_MS  =  10 * 60 * 1000;
	public static final int MINUTES_11_IN_MS  =  11 * 60 * 1000;
	public static final int MINUTES_15_IN_MS  =  15 * 60 * 1000;
	public static final int MINUTES_30_IN_MS  =  30 * 60 * 1000;
	public static final int MINUTES_60_IN_MS  =  60 * 60 * 1000;
	public static final int MINUTES_120_IN_MS = 120 * 60 * 1000;

	public static final int HOURS1_MS  =  1 * 60 * 60 * 1000;
	public static final int HOURS3_MS  =  3 * 60 * 60 * 1000;
	public static final int HOURS4_MS  =  4 * 60 * 60 * 1000;
	public static final int HOURS8_MS  =  8 * 60 * 60 * 1000;
	public static final int HOURS24_MS = 24 * 60 * 60 * 1000;
	public static final int HOURS36_MS = 36 * HOURS1_MS;

	public static final boolean NO_PLOT = false;
	public static final boolean PLOT = true;

	public static final int WOCKET = 0;
	public static final int ZEPHYR = 1;
	public static final int ALIVE  = 2;
	public static final int POLAR  = 3;
	public static final int ERROR1 = 4;
	public static final int ERROR2 = 5;
	public static final int ERROR3 = 6;
	public static final int ERROR4 = 7;
	public static final int ERROR5 = 8;
	public static final int ERROR6 = 9;
	public static final int ERROR7 = 10;

	public static final String NEWLINE = "\n";
	public static final String SKIPLINE = "\n\n";


	//date formats
	public static final SimpleDateFormat mHealthTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static final SimpleDateFormat mHealthFileNameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
	public static final SimpleDateFormat mHealthDateDirFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat mHealthHourDirFormat = new SimpleDateFormat("HH-z");

	// Example of how to call a method defined outside of the library from the library
	public static Arbitrater myArbitrater =  null;

	public static void setArbitrater(Arbitrater anArbitrater) {
		myArbitrater = anArbitrater;
	}

	public static BroadcastReceiverProcessor myBroadcastReceiverProcessor = null;
	public static void setBroadcastReceiverProcessor(BroadcastReceiverProcessor aBroadcastReceiverProcessor) {
		myBroadcastReceiverProcessor = aBroadcastReceiverProcessor;
	}

	//sleep detection constants
	public static final String SLEEP_START_TIME = "SLEEP_START_TIME";
	public static final String WAKE_START_TIME = "WAKE_START_TIME";
	public static final String ALL_MOTION_WAKE_DATA = "ALL_MOTION_WAKE_DATA";
	public static final String PREV_LABEL = "PREV_LABEL";
	public static final String CURRENT_LABEL = "CURRENT_LABEL";
	public static final String SLEEP_LABEL = "Sleep";
	public static final String WAKE_LABEL = "Wake";
	public static final String WOCKET_WORN = "WOCKET_WORN";
	public static final String NUM_ACTIVE_WOCKET = "NUM_ACTIVE_WOCKET";
	public static final String LAST_WOCKET_CONNCTION_TIME = "LAST_WOCKET_CONNCTION_TIME";
	public static final String WS_STATUS_VALUE = "WS_STATUS_VALUE_";
	public static final String WOCKET_ACTIVITY_VALUE = "WOCKET_ACTIVITY_VALUE_";
	public static final String AUDIO_MINUTE = "AUDIO_MINUTE_";
	public static final String NUMBER_OF_SUMMARY_DATA = "NUMBER_OF_SUMMARY_DATA";
	public static final String RESET_TIME = "RESET_TIME";
	public static final String WOCKET_STATUS = "wocket_status";

//	DataStore.init(MyWockets.this);
//
//
//
//
//	public static String NEWS_URL = ""
//    public static final String DEFAULT_NEWS_URL = "http://web.mit.edu/wockets/";
//
//
//
//
//	public static void init(Context context)
//	{
//		SharedPreferences prefs = context.getSharedPreferences(GLOBALS_SHARED_PREF_NAME, Context.MODE_PRIVATE);
//
//		IS_DEBUG =  prefs.getBoolean(IS_DEBUG_KEY, DEFAULT_IS_DEBUG);
//	}
//

}