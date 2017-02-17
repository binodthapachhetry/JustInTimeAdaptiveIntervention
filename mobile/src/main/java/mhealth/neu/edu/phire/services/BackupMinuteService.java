package mhealth.neu.edu.phire.services;

import android.content.Context;
import android.content.Intent;

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

    @Override
    protected void onHandleIntent(Intent intent) {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Inside onCreate", getApplicationContext());
        initialize();
    }

    private void initialize() {
        mContext = getApplicationContext();

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
