package mhealth.neu.edu.phire.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.services.AlwaysOnService;
import mhealth.neu.edu.phire.services.MinuteService;

/**
 * @author Dharam Maniar
 */
public class SystemBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "SystemBroadcastReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context mContext, Intent intent) {
        Log.i(TAG, "stating minute service using intent", mContext);
        mContext.startService(new Intent(mContext, MinuteService.class));

        Log.i(TAG, "starting always on service using intent", mContext);
        mContext.startService(new Intent(mContext, AlwaysOnService.class));

//        Log.i(TAG, "writing note to database related to system broadcact and action in intent", mContext);
//        DatabaseManager.writeNote(mContext, DatabaseManager.SYSTEM_BROADCAST, intent.getAction()); // disabling it to check it's impact on ANR

        Log.i(TAG, intent.getAction(), mContext);

        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN) || intent.getAction().equals(Intent.ACTION_REBOOT)) {
            Log.i(TAG, "setting last phone off time", mContext);
            DataManager.setLastPhoneOffTime(mContext, DateTime.getCurrentTimeInMillis());
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG, "setting last phone on time", mContext);
            DataManager.setLastPhoneOnTime(mContext, DateTime.getCurrentTimeInMillis());
        }
    }
}
