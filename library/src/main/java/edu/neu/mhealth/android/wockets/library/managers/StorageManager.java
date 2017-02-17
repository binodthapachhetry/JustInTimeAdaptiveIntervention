package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import edu.neu.mhealth.android.wockets.library.support.Log;

public class StorageManager {

	private static final String TAG = "StorageManager";

	private static long BYTES_TO_1_MB = 1048576L; // 1024 * 1024

	public static void logStorageStatus(Context context) {
		String logString =
				"Internal Total" +
						"," +
						totalInternalMemory() +
						"," +
						"Internal Free" +
						"," +
						freeInternalMemory() +
						"," +
						"External Total" +
						"," +
						totalExternalMemory() +
						"," +
						"External Free" +
						"," +
						freeExternalMemory();

		Log.i(TAG, logString, context);
	}

	public static int totalInternalMemory() {
		StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return (int)(statFs.getTotalBytes()/BYTES_TO_1_MB);
		} else {
			return (int)((statFs.getBlockCount() * statFs.getBlockSize())/BYTES_TO_1_MB);
		}
	}

	public static int freeInternalMemory() {
		StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return (int)(statFs.getAvailableBytes()/BYTES_TO_1_MB);
		} else {
			return (int)((statFs.getAvailableBlocks() * statFs.getBlockSize())/BYTES_TO_1_MB);
		}
	}

	public static int totalExternalMemory() {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return (int)(statFs.getTotalBytes()/BYTES_TO_1_MB);
		} else {
			return (int)((statFs.getBlockCount() * statFs.getBlockSize())/BYTES_TO_1_MB);
		}
	}

	public static int freeExternalMemory() {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return (int)(statFs.getAvailableBytes()/BYTES_TO_1_MB);
		} else {
			return (int)((statFs.getAvailableBlocks() * statFs.getBlockSize())/BYTES_TO_1_MB);
		}
	}
}
