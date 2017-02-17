package edu.neu.android.wocketslib.utils;

import android.content.Context;
import android.os.Vibrator;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.sensormonitor.DataStore;

public class PhoneVibrator {
	// Vibration pattern
	private static final long[] vibrateTonePrompt = { 30, 10, 20, 60, 10, 60};
	private static final long[] vibrateToneAlive = { 30, 60, 20, 60, 10, 60};
	private static final long[] vibrateToneWocket = { 40, 40};
	private static final long[] vibrateToneZephyr = { 10, 10, 10, 10};

	private static final long[] vibrateToneError1 = { 10, 600, 50, 300, 50};
	private static final long[] vibrateToneError2 = { 10, 600, 50, 300, 50, 300, 50};
	private static final long[] vibrateToneError3 = { 10, 600, 50, 300, 50, 300, 50, 300, 50};
	private static final long[] vibrateToneError4 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50};
	private static final long[] vibrateToneError5 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50};
	private static final long[] vibrateToneError6 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50};
	private static final long[] vibrateToneError7 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50};

	public static final long[] VIBRATE_INTENSE = { 1000, 1000, 1000, 1000, 1000, 1000, 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50};
	public static final long[] VIBRATE_BASIC = { 10, 600, 50, 300, 50};
	
	private static Vibrator vb = null; 
	
	private static long endTime = 0; 
	
	private static long getTotalMS (long[] aVibSeq)
	{
		long sum = 0; 
		for (long i: aVibSeq)
			sum += i; 
		return sum; 
	}
	
	public static void startVibrationAlert(String aTAG, Context aContext) {
		if (vb == null)
			vb = (Vibrator) aContext.getSystemService(Context.VIBRATOR_SERVICE);
		if (vb != null)
		{
			vb.vibrate(vibrateTonePrompt, -1);
			endTime = System.currentTimeMillis() + getTotalMS(vibrateTonePrompt); 
		}
		else
			Log.e(aTAG, "Couldn't get vibration object.");
	}

	public static void startVibrationAlert(String aTAG, Context aContext, int type) {

		if (DataStore.isVibrate())
		{
			if (vb == null)
				vb = (Vibrator) aContext.getSystemService(Context.VIBRATOR_SERVICE);		
			if (vb != null)
			{
				endTime = System.currentTimeMillis(); 
				if (type == Globals.WOCKET)
				{
					vb.vibrate(vibrateToneWocket, -1);
					endTime += getTotalMS(vibrateToneWocket); 
				}
				if (type == Globals.ZEPHYR)
				{
					vb.vibrate(vibrateToneZephyr, -1);
					endTime += getTotalMS(vibrateToneZephyr); 
				}
				else if (type == Globals.ALIVE)
				{
					vb.vibrate(vibrateToneAlive, -1);
					endTime += getTotalMS(vibrateToneAlive); 
				}
				else if (type == Globals.ERROR1)
				{
					vb.vibrate(vibrateToneError1, -1);
					endTime += getTotalMS(vibrateToneError1); 
				}
				else if (type == Globals.ERROR2)
				{
					vb.vibrate(vibrateToneError2, -1);
					endTime += getTotalMS(vibrateToneError2); 
				}
				else if (type == Globals.ERROR3)
				{
					vb.vibrate(vibrateToneError3, -1);
					endTime += getTotalMS(vibrateToneError3); 
				}
				else if (type == Globals.ERROR4)
				{
					vb.vibrate(vibrateToneError4, -1);
					endTime += getTotalMS(vibrateToneError4); 
				}
				else if (type == Globals.ERROR5)
				{
					vb.vibrate(vibrateToneError5, -1);
					endTime += getTotalMS(vibrateToneError5); 
				}
				else if (type == Globals.ERROR6)
				{
					vb.vibrate(vibrateToneError6, -1);
					endTime += getTotalMS(vibrateToneError6); 
				}
				else if (type == Globals.ERROR7)
				{
					vb.vibrate(vibrateToneError7, -1);
					endTime += getTotalMS(vibrateToneError7); 
				}
			}
			else
				Log.e(aTAG, "Couldn't get vibration object.");
		}
	}

	/**
	 * Vibrate the phone.
	 * @param numMS Milliseconds to vibrate for.
	 * @param aContext Requires a context object
	 */
	public static void vibratePhone(String aTAG, int numMS, Context aContext) {
		if (vb == null)
			vb = (Vibrator) aContext.getSystemService(Context.VIBRATOR_SERVICE);	
		if (vb != null)
		{
			vb.vibrate(numMS);
			endTime = System.currentTimeMillis() + numMS;  			
		}
		else
			Log.e(aTAG, "Couldn't get vibration object.");
	}	

	/**
	 * Vibrate the phone.
	 * @param aContext Requires a context object
	 */
	public static void vibratePhonePattern(String aTAG, final Context aContext, final long[] vibrationPattern) {
        final Vibrator v = (Vibrator)aContext.getSystemService(Context.VIBRATOR_SERVICE);
        Thread vibrateThread= new Thread(new Runnable() {
            public void run() {
                try {
                    v.vibrate(vibrationPattern, -1);
                    endTime = System.currentTimeMillis() + getTotalMS(vibrationPattern);
                }
                catch (Throwable t) {
                    Log.i("Vibration", "Thread  exception "+t);
                }
            }
        });

        vibrateThread.start();

	}

	public static boolean isVibrating()
	{
		if (vb != null)
		{
			if (System.currentTimeMillis() < endTime)
				return true;
			else
				return false; 
		}
		return false; 
	}
	
	/**
	 * Stop vibrating the phone.
	 */
	public static void vibratePhoneStop() {
		if (vb != null)
		{
			endTime = 0; 
			vb.cancel();
			vb = null; 			
		}
	}	

}
