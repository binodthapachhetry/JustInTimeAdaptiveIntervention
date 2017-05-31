package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.support.Util;

/**
 * @author Binod Thapa Chhetry
 */
public class BackupMinuteService extends WocketsIntentService {

    private final static String TAG = "BackupMinuteService";

    private Context mContext;

//    public BackupMinuteService() {
//        super("BackupMinuteService");
//    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        mContext = getApplicationContext();
//        Log.i(TAG, "Inside on Handle Intent", mContext);
//        initialize();
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", getApplicationContext());
        initialize();
    }

    private void initialize() {
//        mContext = getApplicationContext();

        if (Looper.myLooper() == Looper.getMainLooper()){
            Log.i(TAG,"In main thread",mContext);
        }else{
            Log.i(TAG,"Not in main thread",mContext);
        }

        DataManager.setBackupMinuteServiceLastRun(mContext);
        Util.setBackupMinuteServiceAlarm(mContext);
        startService(new Intent(this, MinuteService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }
}
