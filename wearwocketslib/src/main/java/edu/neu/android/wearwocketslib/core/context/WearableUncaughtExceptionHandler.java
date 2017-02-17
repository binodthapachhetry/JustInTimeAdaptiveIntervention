package edu.neu.android.wearwocketslib.core.context;

import android.content.Context;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulBroadcastAlarm;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by qutang on 8/7/15.
 */
public class WearableUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultHandler;

    private Logger logger;
    public static final String TAG = "CrashReport";
    public static final String KEY_CRASH_RETRY_COUNT = "KEY_CRASH_RETRY_COUNT";
    private Context mContext;

    public WearableUncaughtExceptionHandler(Context context){
        mContext = context;
        logger = new Logger(TAG);
    }

    public void setDefaultHandler(Thread.UncaughtExceptionHandler handler){
        defaultHandler = handler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        int count = SharedPrefs.getInt(KEY_CRASH_RETRY_COUNT, 0, mContext);

        logger.e("Caught a crash", mContext);
        logger.e(ex.getMessage(), mContext);
        logger.logStackTrace(ex, mContext);

        // try to set a new alarm to restart the wakeful service
        if(count < 3) {
            logger.i("Current crash retry: " + count, mContext);
            logger.i("Set alarm to restart wakeful service", mContext);
            WearableWakefulBroadcastAlarm alarm = new WearableWakefulBroadcastAlarm(mContext, "CRASH");
            alarm.setAlarm();
            // increase retry count
            count++;
            SharedPrefs.setInt(KEY_CRASH_RETRY_COUNT, count, mContext);
        }
        logger.close();

        defaultHandler.uncaughtException(thread, ex);
    }
}
