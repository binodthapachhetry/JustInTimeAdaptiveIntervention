package edu.neu.android.wearwocketslib;

import java.io.File;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WakefulServiceArbitrator;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by Dharam on 5/1/2015.
 */
public class Globals {

    public static final String DEBUG_SENSOR_ISSUE = "DebugSensorIssue";

    public static String STUDY_NAME = "TEMPLE";

    public static String CAPABILITY_NAME = "phire";

    public static String TRANSFER_FOLDER = null;

    public static final String SENSOR_MANAGER_SERVICE_STATUS = "SENSOR_MANAGER_SERVICE_STATUS";
    public static final String LAST_PHONE_IN_CONNECTION_TIME = "LAST_PHONE_IN_CONNECTION_TIME";
    public static final String LAST_BOOT_UP_TIME = "LAST_BOOT_UP_TIME";
    public static long BOOT_UP_TIME_HOLDER = 0;
    public static final int PHONE_CONNECTION_NOTIFICATION_THRESHOLD = 1; //hours

    public static boolean IS_ACCELEROMETER_LOGGING_ENABLED = false;
    public static boolean IS_MAGNETIC_FIELD_LOGGING_ENABLED = false;
    public static boolean IS_HEART_RATE_LOGGING_ENABLED = false;
    public static boolean IS_GYROSCOPE_LOGGING_ENABLED = false;
    public static boolean IS_BATTERY_LOGGING_ENABLED = false;
    public static boolean IS_MHEALTH_EXTERNAL = true;

    public static final String LAST_HEART_RATE_TIMESTAMP = "LAST_HEART_RATE_TIMESTAMP";
    public static int HEART_RATE_DELAY_MS = 5 * 60 * 1000;

    public static void init(){
        mHealthFormat.init(STUDY_NAME, IS_MHEALTH_EXTERNAL);
        TRANSFER_FOLDER  = "/sdcard/." + STUDY_NAME + "/transfer/";
        new File(TRANSFER_FOLDER).mkdirs();
    }
}
