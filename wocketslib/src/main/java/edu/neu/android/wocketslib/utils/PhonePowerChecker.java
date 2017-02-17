package edu.neu.android.wocketslib.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class PhonePowerChecker {
	private static final String TAG = "PhonePowerChecker";

	public static int getBatteryRemaining(Context aContext) {
		Intent intent = aContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int level = -1;
		if (rawlevel >= 0 && scale > 0) {
			level = (rawlevel * 100) / scale;
		}

		return level;
	}

	public static boolean isCharging(Context aContext) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = aContext.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		return ((status==BatteryManager.BATTERY_STATUS_CHARGING) ||
				(status == BatteryManager.BATTERY_STATUS_FULL) || 
				(status==BatteryManager.BATTERY_PLUGGED_AC));	
//		
//		
//		Intent intent = aContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//		int status = intent.getIntExtra("status", BatteryManager.BATTERY_HEALTH_UNKNOWN);
//		int charging = intent.getIntExtra("plugged", BatteryManager.BATTERY_PLUGGED_AC);
////		int batterylevel = intent.getIntExtra("level", 0);
//
//		if (charging == 1) {
//			Log.d(TAG, "Charging..................................................................................................................");
//			return true;
//		} else if (status == BatteryManager.BATTERY_PLUGGED_USB) {
//			Log.d(TAG, "Charging USB..................................................................................................................");
//			return true;
//		} else
//			Log.d(TAG, "NOT Charging.................................................................................................................");
//
//		return false;
	}
}
