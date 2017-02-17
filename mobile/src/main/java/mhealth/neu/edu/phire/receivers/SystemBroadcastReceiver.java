package mhealth.neu.edu.phire.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.services.AlwaysOnService;
import mhealth.neu.edu.phire.services.MinuteService;

/**
 * @author Dharam Maniar
 */
public class SystemBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SystemBroadcastReceiver";

    @Override
    public void onReceive(Context mContext, Intent intent) {
        mContext.startService(new Intent(mContext, MinuteService.class));
        mContext.startService(new Intent(mContext, AlwaysOnService.class));
        DatabaseManager.writeNote(mContext, DatabaseManager.SYSTEM_BROADCAST, intent.getAction());
        Log.i(TAG, intent.getAction(), mContext);

        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN) || intent.getAction().equals(Intent.ACTION_REBOOT)) {
            DataManager.setLastPhoneOffTime(mContext, DateTime.getCurrentTimeInMillis());
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            DataManager.setLastPhoneOnTime(mContext, DateTime.getCurrentTimeInMillis());
        }
    }
}
