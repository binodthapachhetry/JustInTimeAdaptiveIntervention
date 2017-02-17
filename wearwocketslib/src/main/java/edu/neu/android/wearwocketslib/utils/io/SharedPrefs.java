package edu.neu.android.wearwocketslib.utils.io;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.neu.android.wearwocketslib.Globals;

/**
 * Created by Dharam on 11/25/2014.
 */
public class SharedPrefs {

    private static final String SharedPrefsName = Globals.STUDY_NAME;

    public static void setInt(String key, int intValue, Context mContext) {

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putInt(key, intValue);
        mEditor.commit();
    }

    public static int getInt(String key, int defaultValue, Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        try {
            return sharedPreferences.getInt(key, defaultValue);
        }catch (ClassCastException e){
            return defaultValue;
        }
    }

    public static void setString(String key, String stringValue,
                                 Context mContext) {

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putString(key, stringValue);
        mEditor.commit();
    }

    public static String getString(String key, String defaultValue,
                                   Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setStringSet(String key, Set<String> stringSet, Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putStringSet(key, stringSet);
        mEditor.commit();
    }

    public static Set<String> getStringSet(String key, Set<String> defaultStringSet, Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        return new HashSet<>(sharedPreferences.getStringSet(key, defaultStringSet));
    }

    public static void removeStringFromStringSet(String key, String string, Context mContext) {
        Set<String> stringSet = getStringSet(key, new HashSet<String>(), mContext);
        stringSet.remove(string);
        setStringSet(key, stringSet, mContext);
    }

    public static void addStringToStringSet(String key, String string, Context mContext){
        Set<String> stringSet = getStringSet(key, new HashSet<String>(), mContext);
        stringSet.add(string);
        setStringSet(key, stringSet, mContext);
    }

    public static void setBoolean(String key, Boolean boolValue,
                                  Context mContext) {

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putBoolean(key, boolValue);
        mEditor.commit();
    }

    public static Boolean getBoolean(String key, Boolean boolValue,
                                     Context mContext) {
        if(mContext != null) {
            SharedPreferences sharedPreferences = mContext
                    .getSharedPreferences(SharedPrefsName,
                            Context.MODE_PRIVATE);
            if (sharedPreferences == null) {
                return null;
            }
            return sharedPreferences.getBoolean(key, boolValue);

        }else{
            return null;
        }
    }

    public static void setLong(String key, long longValue, Context mContext) {

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putLong(key, longValue);
        mEditor.commit();
    }

    public static long getLong(String key, long longValue, Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName,
                        Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, longValue);
    }

    public static void remove(String key, Context mContext) {

        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.remove(key);
        mEditor.commit();
    }

    public static void clearAll(Context mContext) {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        Map<String, ?> allSharedPrefs = sharedPreferences.getAll();
        Set<String> allKeys = allSharedPrefs.keySet();
        Iterator<String> iterator = allKeys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            System.out.println("Removing Key = " + key);
            mEditor.remove(key);
        }
        mEditor.commit();
    }
}
