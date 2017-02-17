package edu.neu.android.wocketslib.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;

public class WOCKETSSharedPrefs {
	private static final String TAG = "WOCKETSSharedPrefs";
	
	private static void logNullKey(String aKey) {
		Log.e(TAG, "key is null or blank");
		try {
			aKey.charAt(0);
		} catch (Exception e) {				
			if (e != null) {
                Log.e(TAG, Log.getStackTraceString(e));
			} else {
				Log.e(TAG, "can't print the StackTrace");
			}
		}
	}
	
	public static String getString(Context ctx, String key, String defaultValue) {
		if (key == null) {
			logNullKey(key);
			return defaultValue;
		}		
		//TODO Fix these methods in this class to do something reasonable if Globals.WOCKETS_SP_URI is null!
		if (Globals.WOCKETS_SP_URI == null)
		{
			Log.e(TAG, "Globals.WOCKETS_SP_URI is null!");
		}
				
		Cursor cursor = ctx.getContentResolver().query(Globals.WOCKETS_SP_URI, null, key, null,
				null);
		String strToReturn = null;
		if (cursor != null) {
			if (cursor.moveToFirst())
				strToReturn = cursor.getString(1);
			cursor.close();
		} else
        {
            // Cursor is null. SP not working. Could be a problem
            Log.e(TAG, "Cursor is null!");
        }
		return (strToReturn != null) ? strToReturn : defaultValue;
	}

	public static void putString(Context ctx, String key, String value) {
		if (key == null||key.equals("")) {
			logNullKey(key);
			return;
		}

        //TODO Fix these methods in this class to do something reasonable if Globals.WOCKETS_SP_URI is null!
        if (Globals.WOCKETS_SP_URI == null)
        {
            Log.e(TAG, "Globals.WOCKETS_SP_URI is null!");
        }

		ContentValues values = new ContentValues();
		values.clear();
		values.put("Key", key);
		values.put("Value", value);
		ctx.getContentResolver().update(Globals.WOCKETS_SP_URI, values, null, null);
	}

	public static long getLong(Context ctx, String key, long defaultValue) {
		String value = getString(ctx, key, null);
		return (value == null||value.equals("")) ? defaultValue : Long.parseLong(value);
	}

	public static void putLong(Context ctx, String key, long value) {
		putString(ctx, key, Long.toString(value));
	}

	public static int getInt(Context ctx, String key, int defaultValue) {
		String value = getString(ctx, key, null);
		return (value == null||value.equals("")) ? defaultValue : Integer.parseInt(value);
	}

	public static void putInt(Context ctx, String key, int value) {
		putString(ctx, key, Integer.toString(value));
	}

	public static float getFloat(Context ctx, String key, float defaultValue) {
		String value = getString(ctx, key, null);
		return (value == null||value.equals("")) ? defaultValue : Float.parseFloat(value);
	}

	public static void putFloat(Context ctx, String key, float value) {
		putString(ctx, key, Float.toString(value));
	}

	public static boolean getBoolean(Context ctx, String key,
			boolean defaultValue) {
		String value = getString(ctx, key, null);
		return (value == null) ? defaultValue : Boolean.parseBoolean(value);
	}

	public static void putBoolean(Context ctx, String key, boolean value) {
		putString(ctx, key, Boolean.toString(value));
	}
}
