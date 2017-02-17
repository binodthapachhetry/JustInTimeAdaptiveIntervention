package mhealth.neu.edu.phire;


import android.content.Context;
import android.os.Environment;

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

    public static final int START_HOUR = 19;
    public static final int START_MINUTE = 0;
    public static final int END_HOUR = 16;
    public static final int END_MINUTE = 0;

//	public static final String DEFAULT_SLEEP_TIME = "22:00";
//	public static final String DEFAULT_WAKE_TIME = "06:30";

    public static final String KEY_EMA_DEMO = "KEY_EMA_DEMO";

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
    }
}
