package edu.neu.android.wearwocketslib.utils.log;

import android.content.Context;

import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;

/**
 * Created by qutang on 8/12/15.
 */
public class StorageLogger {
    public static final String TAG = "StorageLogger";

    public static void logStorageInfo(Context context) {
        double availableMeg = DeviceInfo.getAvailableStorageInMB(context);
        Log.i(TAG, String.format("%.2f", availableMeg), context);
    }
}
