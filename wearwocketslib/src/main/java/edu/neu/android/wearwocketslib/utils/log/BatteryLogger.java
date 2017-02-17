package edu.neu.android.wearwocketslib.utils.log;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import edu.neu.android.wearwocketslib.utils.log.Log;

/**
 * Created by Dharam on 5/1/2015.
 */
public class BatteryLogger {

    public static final String TAG = "BatteryLogger";

    public static void logBatteryInfo(Context context) {
        float batteryLevel = -1.0f;
        String chargingStatus = null;
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int level = batteryIntent
                    .getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent
                    .getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);

            batteryLevel = ((float) level / (float) scale) * 100.0f;
            int status = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) {
                chargingStatus = "Charging";
            } else {
                chargingStatus = "Discharging";
            }
        }
        Log.i(TAG, batteryLevel + "," + chargingStatus, context);
    }

    public static float getBatteryLevel(Context context) {
        float batteryLevel = -1.0f;
        String chargingStatus = null;
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int level = batteryIntent
                    .getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent
                    .getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);

            return ((float) level / (float) scale) * 100.0f;
        } else {
            return -1.0f;
        }
    }

    public static boolean isBatteryCharging(Context context) {
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int status = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
