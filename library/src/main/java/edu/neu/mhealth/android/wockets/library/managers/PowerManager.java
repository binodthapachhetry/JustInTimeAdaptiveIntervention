package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.os.Build;

import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class PowerManager {

	private static final String TAG = "PowerManager";

	public static void logPowerManagerStatus(Context context) {
		Log.i(TAG,
				"isDeviceIdleMode - " + isDeviceIdleMode(context) +
						" - isInteractive - " + isInteractive(context) +
						" - isPowerSaveMode - " + isPowerSaveMode(context) +
						" - isSustainedPerformanceModeSupported - " + isSustainedPerformanceModeSupported(context),
				context);
	}

	public static boolean isDeviceIdleMode(Context context) {
		android.os.PowerManager powerManager = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager.isDeviceIdleMode();
	}

	public static boolean isInteractive(Context context) {
		android.os.PowerManager powerManager = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && powerManager.isInteractive();
	}

	public static boolean isPowerSaveMode(Context context) {
		android.os.PowerManager powerManager = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager.isPowerSaveMode();
	}

	public static boolean isSustainedPerformanceModeSupported(Context context) {
		android.os.PowerManager powerManager = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && powerManager.isSustainedPerformanceModeSupported();
	}
}