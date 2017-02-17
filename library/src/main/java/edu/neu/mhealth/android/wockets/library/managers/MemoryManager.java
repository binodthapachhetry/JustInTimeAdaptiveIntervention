package edu.neu.mhealth.android.wockets.library.managers;

import android.app.ActivityManager;
import android.content.Context;

import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class MemoryManager {

	private static final String TAG = "MemoryManager";

	private static long BYTES_TO_1_MB = 1048576L; // 1024 * 1024

	public static void logMemoryStatus(Context context) {
		String logString = "Total Memory" +
				"," +
				getTotalMemory(context) +
				"," +
				"Available Memory" +
				"," +
				getAvailableMemory(context) +
				"," +
				"Threshold Memory" +
				"," +
				getThresholdMemory(context) +
				"," +
				"IsLowMemory" +
				"," +
				isLowMemory(context);
		Log.i(TAG, logString, context);
	}

	private static ActivityManager.MemoryInfo getMemoryInfo(Context context) {
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(memoryInfo);
		return memoryInfo;
	}

	public static int getTotalMemory(Context context) {
		ActivityManager.MemoryInfo memoryInfo = getMemoryInfo(context);
		return (int)(memoryInfo.totalMem/BYTES_TO_1_MB);
	}

	public static int getAvailableMemory(Context context) {
		ActivityManager.MemoryInfo memoryInfo = getMemoryInfo(context);
		return (int)(memoryInfo.availMem/BYTES_TO_1_MB);
	}

	public static int getThresholdMemory(Context context) {
		ActivityManager.MemoryInfo memoryInfo = getMemoryInfo(context);
		return (int)(memoryInfo.threshold/BYTES_TO_1_MB);
	}

	public static boolean isLowMemory(Context context) {
		ActivityManager.MemoryInfo memoryInfo = getMemoryInfo(context);
		return memoryInfo.lowMemory;
	}
}
