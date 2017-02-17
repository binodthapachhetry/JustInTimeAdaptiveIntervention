package edu.neu.android.wocketslib.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.view.Gravity;
import android.widget.Toast;

/*
 * 'External storage' matches what normally is the SD card, and 'data directory' is the application data directory.
 * There are some weird distinctions though when you check the 'SD card and phone storage' menu on either the emulator or the Samsung, 
 * though I'm not sure how much this matters in practice.
 * 
 * On the emulator, getExternalStorageAvailable() corresponds to 'SD card' available space, 
 * and getDataDirectoryStorageAvailable() to 'Internal phone storage'.
 * 
 * On the Samsung, getExternalStorageAvailable() corresponds to 'Phone storage' available
 * space (13 GB), and getDataDirectoryStorageAvailable() to 'Application storage'. The Samsung
 * also has a section called 'SD card' (2 GB), but I can't figure out how to get that, or why 
 * it's not included under phone storage.
 */



public class MemoryChecker {
	
	private static final int WARN_INT_MEM_LOWER_THIS_MB = 100;  
	private static final int WARN_EXT_MEM_LOWER_THIS_MB = 1000;  
	
	private static double getAvailableStorageForPath(String path) {
		StatFs stat = new StatFs(path);
		double blockSize = stat.getBlockSize();
		double availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}
	
	public static String getMemoryReport()
	{
		return "External: " + Math.round(Math.floor(getExternalStorageAvailable())) + " MB, Internal: " + Math.round(Math.floor(getDataDirectoryStorageAvailable())) + " MB";
	}

	public static boolean isInternalMemoryLow()
	{
		if (getDataDirectoryStorageAvailable() < WARN_INT_MEM_LOWER_THIS_MB)
			return true;
		else
			return false;
	}

	public static boolean isExternalMemoryLow()
	{
		if (getExternalStorageAvailable() < WARN_EXT_MEM_LOWER_THIS_MB)
			return true;
		else
			return false;
	}

	/**
	 * Pop up a toast message if the memory is too low, either external or internal
	 */
	public static void alertIfMemoryLow(Context aContext)
	{

		String msg = ""; 

		if (isInternalMemoryLow())
		{
			msg = "The internal memory on the phone is too low (" + getDataDirectoryStorageAvailable() + "). Please delete some programs so CITY can run properly!";
		}
		if (isExternalMemoryLow())
		{
			msg += "The SD card is running out of space (" + getExternalStorageAvailable() + "). Please delete some pictures/videos so CITY can run properly!";
		}		
		
		if (!msg.equals(""))
		{
			Toast aToast = Toast.makeText(aContext, msg, Toast.LENGTH_LONG);
			aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			aToast.show();
		}
	}
	
	
	/**
	 * Return the amount of storage available on the SD card in MB
	 * @return Amount in MB
	 */
	public static double getExternalStorageAvailable() {
		return (getAvailableStorageForPath(Environment.getExternalStorageDirectory().getPath())/1000.0/1000.0);
	}

	/**
	 * Return the amount of storage available on the main memory in MB
	 * @return Amount in MB
	 */
	public static double getDataDirectoryStorageAvailable() {
		return (getAvailableStorageForPath(Environment.getDataDirectory().getPath())/1000.0/1000.0);
	}	
}
