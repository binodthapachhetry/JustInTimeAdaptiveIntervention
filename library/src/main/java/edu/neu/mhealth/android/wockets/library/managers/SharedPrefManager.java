package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * @author Dharam Maniar
 */

public class SharedPrefManager {

    private static final String TAG = "SharedPrefManager";
    private static final String SharedPrefsName = "Wockets";

    public static void setInt(String key, int intValue, Context mContext) {
        //Log.d(TAG, "Saving Int - " + key + ":" + intValue);
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putInt(key, intValue);
        mEditor.apply();
    }

    public static int getInt(String key, int defaultValue, Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        int intValue = sharedPreferences.getInt(key, defaultValue);
        //Log.d(TAG, "Retrieving Int - " + key + ":" + intValue);
        return intValue;
    }



    public static void setString(String key, String stringValue,
                                 Context mContext) {
        //Log.d(TAG, "Saving String - " + key + ":" + stringValue);
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putString(key, stringValue);
        mEditor.apply();
    }

    public static String getString(String key, String defaultValue,
                                   Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        String stringValue = sharedPreferences.getString(key, defaultValue);
        //Log.d(TAG, "Retrieving String - " + key + ":" + stringValue);
        return stringValue;
    }

    public static void setBoolean(String key, boolean boolValue,
                                  Context mContext) {
        //Log.d(TAG, "Saving Boolean - " + key + ":" + boolValue);
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putBoolean(key, boolValue);
        mEditor.apply();
    }

    public static Boolean getBoolean(String key, boolean defaultValue,
                                     Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        boolean boolValue = sharedPreferences.getBoolean(key, defaultValue);
        //Log.d(TAG, "Retrieving boolean - " + key + ":" + boolValue);
        return boolValue;
    }

    public static void setLong(String key, long longValue, Context mContext) {
        //Log.d(TAG, "Saving long - " + key + ":" + longValue);
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putLong(key, longValue);
        mEditor.apply();
    }

    public static long getLong(String key, long defaultValue, Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        long longValue = sharedPreferences.getLong(key, defaultValue);
        //Log.d(TAG, "Retrieving long - " + key + ":" + longValue);
        return longValue;
    }

    public static void remove(String key, Context mContext) {
        //Log.d(TAG, "Removing entry for key - " + key);
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.remove(key);
        mEditor.apply();
    }

    public static void removeAll(Context mContext) {
        //Log.d(TAG, "Removing all entries");
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        Map<String, ?> allSharedPrefs = sharedPreferences.getAll();
        Set<String> allKeys = allSharedPrefs.keySet();
        for (String key : allKeys) {
            //Log.d(TAG, "Removing entry for key - " + key);
            mEditor.remove(key);
        }
        mEditor.apply();
    }
}
