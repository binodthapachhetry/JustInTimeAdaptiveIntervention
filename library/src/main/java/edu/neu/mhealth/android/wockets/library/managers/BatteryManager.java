package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class BatteryManager {

	private static final String TAG = "BatteryManager";

	public static void logBatteryStatus(Context context) {
		Log.i(TAG,
				"Percentage - " + getBatteryPercentage(context) +
				" - isCharging - " + isCharging(context) +
				" - isUSBCharging - " + isUSBCharging(context) +
				" - isACCharging - " + isACCharging(context) +
				" - isWirelessCharging - " + isWirelessCharging(context),
				context);
	}

	private static Intent getBatteryStatusIntent(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		return context.registerReceiver(null, ifilter);
	}

	public static boolean isCharging(Context context) {
		Intent batteryStatus = getBatteryStatusIntent(context);
		if (batteryStatus == null) {
			return false;
		}
		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1);
		return status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
				status == android.os.BatteryManager.BATTERY_STATUS_FULL;
	}

	public static boolean isUSBCharging(Context context) {
		Intent batteryStatus = getBatteryStatusIntent(context);
		if (batteryStatus == null) {
			return false;
		}
		// How are we charging?
		int chargePlug = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
		return chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_USB;
	}

	public static boolean isACCharging(Context context) {
		Intent batteryStatus = getBatteryStatusIntent(context);
		if (batteryStatus == null) {
			return false;
		}
		// How are we charging?
		int chargePlug = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
		return chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_AC;
	}

	public static boolean isWirelessCharging(Context context) {
		Intent batteryStatus = getBatteryStatusIntent(context);
		if (batteryStatus == null) {
			return false;
		}
		// How are we charging?
		int chargePlug = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
				chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS;
	}

	public static int getBatteryPercentage(Context context) {
		Intent batteryStatus = getBatteryStatusIntent(context);
		if (batteryStatus == null) {
			return 0;
		}
		int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);

		return ((level*100)/scale);
	}
}
