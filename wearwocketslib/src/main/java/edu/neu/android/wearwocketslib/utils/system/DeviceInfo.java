package edu.neu.android.wearwocketslib.utils.system;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;

import edu.neu.android.wearwocketslib.utils.log.Log;

/**
 * Created by qutang on 5/20/15.
 */
public class DeviceInfo {

    public static final String TAG = "WearDeviceInfo";
    public static String getBluetoothMacAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if device does not support Bluetooth
        if(mBluetoothAdapter==null){
            Log.d(TAG, "device does not support bluetooth");
            return null;
        }

        return mBluetoothAdapter.getAddress();
    }

    public static String getBluetoothMacAddressConcated() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if device does not support Bluetooth
        if(mBluetoothAdapter==null){
            Log.d(TAG,"device does not support bluetooth");
            return null;
        }

        String addr = mBluetoothAdapter.getAddress();
        return addr.replace(":", "");
    }

    public static String getDeviceName(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if device does not support Bluetooth
        if(mBluetoothAdapter==null){
            Log.d(TAG,"device does not support bluetooth");
            return null;
        }

        return mBluetoothAdapter.getName();
    }

    public static String getDeviceNameConcated(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if device does not support Bluetooth
        if(mBluetoothAdapter==null){
            Log.d(TAG,"device does not support bluetooth");
            return null;
        }

        return mBluetoothAdapter.getName().replace(" ", "");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public static boolean isScreenOn(Context mContext){
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn() || pm.isInteractive();
        return isScreenOn;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isPowerSaveMode(Context mContext){
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean isPowerSave = pm.isPowerSaveMode();
        return isPowerSave;
    }

    public static int getMemoryUsageInPercentage(Context mContext){
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        return (int)((mi.availMem / (float)mi.totalMem)*100);
    }

    public static double getMemoryUsageInMB(Context mContext){
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 1048576.0;
        return availableMegs;
    }


    public static boolean isLowMemoryStatus(Context mContext){
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.lowMemory;
    }

    public static double getAvailableStorageInMB(Context mContext){
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getAvailableBytes();
        double megAvailable = bytesAvailable / 1048576.0;
        return megAvailable;
    }
}
