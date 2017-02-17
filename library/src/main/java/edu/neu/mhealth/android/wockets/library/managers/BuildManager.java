package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.os.Build;

import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */

public class BuildManager {

    private static final String TAG = "BuildManager";

    public static void logBuildStatus(Context context) {
        Log.i(TAG, "Board - " + Build.BOARD, context);
        Log.i(TAG, "Bootloader - " + Build.BOOTLOADER, context);
        Log.i(TAG, "Brand - " + Build.BRAND, context);
        Log.i(TAG, "Device - " + Build.DEVICE, context);
        Log.i(TAG, "FingerPrint - " + Build.FINGERPRINT, context);
        Log.i(TAG, "Hardware - " + Build.HARDWARE, context);
        Log.i(TAG, "Host - " + Build.HOST, context);
        Log.i(TAG, "ID - " + Build.ID, context);
        Log.i(TAG, "Manufacturer - " + Build.MANUFACTURER, context);
        Log.i(TAG, "Model - " + Build.MODEL, context);
        Log.i(TAG, "Product - " + Build.PRODUCT, context);
        Log.i(TAG, "Serial - " + Build.SERIAL, context);
        Log.i(TAG, "Tags - " + Build.TAGS, context);
        Log.i(TAG, "Type - " + Build.TYPE, context);
    }
}
