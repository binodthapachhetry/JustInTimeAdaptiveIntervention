package mhealth.neu.edu.phire;


import android.content.Context;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.WocketsConstants;
import edu.neu.mhealth.android.wockets.library.data.DataManager;

/**
 * Created by jarvis on 2/2/17.
 */

public class TEMPLEConstants extends WocketsConstants {

    public static final String STUDY_NAME = "TEMPLE";
    private static final String DIRECTORY_EXTERNAL_STORAGE_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String DIRECTORY_LOGS = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/logs";
    private static final String DIRECTORY_SURVEYS = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/surveys";
    private static final String DIRECTORY_DATA = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/data";
    private static final String DIRECTORY_TRANSFER = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/transfer";


    private static final String DIRECTORY_FEATURE = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/feature";

    private static final String DIRECTORY_WATCH_DATA = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/data-watch";
    private static final String DIRECTORY_WATCH_LOGS = DIRECTORY_EXTERNAL_STORAGE_ROOT + "/." + STUDY_NAME + "/logs-watch";


    public static final int START_HOUR = 01;
    public static final int START_MINUTE = 0;
    public static final int END_HOUR = 23;
    public static final int END_MINUTE = 0;

	public static final String DEFAULT_SLEEP_TIME = "23:59";
	public static final String DEFAULT_WAKE_TIME = "01:01";

    public static final String KEY_EMA_DEMO = "KEY_EMA_DEMO";
    public static final String KEY_WEEKLY_SURVEY = "Child";
    public static final String KEY_EMA_SURVEY = "Mother";

    public static final List<String> SUPPORTED_WHEEL_DIAMETER_CM = Arrays.asList("20","21","22","23","24","25","26","27","28");

    public static final String CSC_SERVICE_UUID = "00001816-0000-1000-8000-00805f9b34fb";

    public static final double LB_KG_CONVERT = 0.453592d;
    public static final double MET_DIVIDE = 200d;


//    public static final String KEY_SALIVA_WAKING_DEMO = "KEY_SALIVA_WAKING_DEMO";
//	public static final String KEY_SALIVA_WAKING = "KEY_SALIVA_WAKING";
//	public static final String KEY_SALIVA_WAKING_PLUS_30 = "KEY_SALIVA_WAKING_PLUS_30";
//	public static final String KEY_SALIVA_AFTERNOON_330TO4_MOTHER = "KEY_SALIVA_AFTERNOON_330TO4_MOTHER";
//	public static final String KEY_SALIVA_AFTERNOON_4TO430_CHILD = "KEY_SALIVA_AFTERNOON_4TO430_CHILD";
//	public static final String KEY_SALIVA_BEDTIME = "KEY_SALIVA_BEDTIME";

    public static void init(Context mContext) {
        DataManager.setStudyName(STUDY_NAME, mContext);
        DataManager.setDirectoryLogs(DIRECTORY_LOGS, mContext);
        DataManager.setDirectorySurveys(DIRECTORY_SURVEYS, mContext);
        DataManager.setDirectoryData(DIRECTORY_DATA, mContext);
        DataManager.setDirectoryTransfer(DIRECTORY_TRANSFER, mContext);

        DataManager.setDirectoryFeature(DIRECTORY_FEATURE,mContext);

        DataManager.setDirectoryWatchData(DIRECTORY_WATCH_DATA, mContext);
        DataManager.setDirectoryWatchLogs(DIRECTORY_WATCH_LOGS, mContext);


    }
}
