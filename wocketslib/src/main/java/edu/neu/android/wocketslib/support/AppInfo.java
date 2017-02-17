package edu.neu.android.wocketslib.support;

import java.util.Date;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;

public class AppInfo {

	private static final String KEY_DESCRIPTION = "_DESC";
	private static final String KEY_PACKAGE_NAME = "_PKG";
	private static final String KEY_CLASS_NAME = "_CLASS";
	private static final String KEY_IS_AVAILABLE = "_ACTIVE";
	private static final String KEY_LAST_TIME_COMPLETED = "_LAST_TIME_COMPLETED";
	private static final String KEY_LAST_TIME_PROMPTED = "_LAST_TIME_PROMPTED";
	private static final String KEY_PROMPT_INTERVAL_MS = "_PROMPT_INTERVAL_MS";
	private static final String KEY_POSTPONE_TIME = "_POSTPONE_TIME_MS";
	private static final String KEY_SINGLE_PROMPT_TARGET_TIME = "_SINGLE_PROMPT_TARGET_TIME";
	private static final String KEY_START_MANUAL_TIME = "_START_MANUAL_TIME";	
	
	public static final long NO_TIME = 0;

	
	public static void MarkAppCompleted(Context aContext, String appTag)
	{
		AppInfo.SetLastTimeCompleted(aContext, appTag, System.currentTimeMillis());
		// Make sure the wallpaper resets immediately. Arbitrate can take a little while
		// so this message shows up in the meantime 
	}
	
	// DataStorage.SetValue(aContext, aKey + KEY_IS_AVAILABLE, isActive);

	private static String GetReadableDate(long aTime) {
		if (aTime == 0) 
			return "";
		Date newDate = new Date(aTime);
		return newDate.toLocaleString();
	}

	public static String AppStatus(Context aContext, String aKey) {
		String TAB = "       ";
		StringBuilder sb = new StringBuilder();
		sb.append("APPINFO " + aKey);
		sb.append("\n");
		sb.append(TAB + "IsAvailable: " + GetIsAvailable(aContext, aKey));
		sb.append("\n");
		sb.append(TAB + GetPackageName(aContext, aKey) + " " + GetClassName(aContext, aKey));
		sb.append("\n");
		sb.append(TAB + "LastTimeCompleted: " + GetReadableDate(GetLastTimeCompleted(aContext, aKey)));
		sb.append("\n");
		sb.append(TAB + "LastTimePrompted: " + GetReadableDate(GetLastTimePrompted(aContext, aKey)));
		sb.append("\n");
		sb.append(TAB + "Postpone Time: " + GetReadableDate(GetPostponeTime(aContext, aKey)));
		sb.append("\n");
		sb.append(TAB + "PromptIntervalMS: " + GetPromptIntervalMS(aContext, aKey));
		sb.append("\n");
		sb.append(TAB + "SinglePromptTarget: " + GetReadableDate(GetSinglePromptTargetTime(aContext, aKey)));
		sb.append("\n");
		return sb.toString();
	}

	public static String getKey(Context aContext, String className) {
		for (String aKey : Globals.ALL_APP_KEYS) {
			if (GetClassName(aContext, aKey).equals(className))
				return aKey;
		}
		return null;
	}

	public static String AllAppStatus(Context aContext) {
		StringBuffer sb = new StringBuffer();
		for (String aKey : Globals.ALL_APP_KEYS) {
			sb.append(AppStatus(aContext, aKey));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static void reset(Context aContext) {
		for (String aKey : Globals.ALL_APP_KEYS) {
			SetIsAvailable(aContext, aKey, false);
			SetPromptIntervalMS(aContext, aKey, NO_TIME);
			SetSinglePromptTargetTime(aContext, aKey, NO_TIME);
			SetPostponeTime(aContext, aKey, NO_TIME);
		}
	}

	public static void resetAvailabilityAndTiming(Context aContext) {
		for (String aKey : Globals.ALL_APP_KEYS) {
			SetIsAvailable(aContext, aKey, false);
			SetPromptIntervalMS(aContext, aKey, NO_TIME);
			SetSinglePromptTargetTime(aContext, aKey, NO_TIME);
		}
	}

	public static void resetPromptCompletedTimes(Context aContext) {
		for (String aKey : Globals.ALL_APP_KEYS) {
			SetLastTimePrompted(aContext, aKey, NO_TIME);
			SetLastTimeCompleted(aContext, aKey, NO_TIME);
			SetPostponeTime(aContext, aKey, NO_TIME);
		}
	}

	public static void Save(Context aContext, String aKey, String aHumanReadableName, String aPackageName, String aClassName) {
		DataStorage.SetValue(aContext, aKey + KEY_DESCRIPTION, aHumanReadableName);
		DataStorage.SetValue(aContext, aKey + KEY_PACKAGE_NAME, aPackageName);
		DataStorage.SetValue(aContext, aKey + KEY_CLASS_NAME, aClassName);

	}

	public static void SetStartManualTime(Context aContext, String aKey, long aStartManualTime) {
		DataStorage.SetValue(aContext, aKey + KEY_START_MANUAL_TIME, aStartManualTime);
	}

	public static long GetStartManualTime(Context aContext, String aKey) {
		return DataStorage.GetValueLong(aContext, aKey + KEY_START_MANUAL_TIME, 0);
	}

	
	public static void SetSinglePromptTargetTime(Context aContext, String aKey, long aSinglePromptTargetTime) {
		DataStorage.SetValue(aContext, aKey + KEY_SINGLE_PROMPT_TARGET_TIME, aSinglePromptTargetTime);
	}

	public static long GetSinglePromptTargetTime(Context aContext, String aKey) {
		return DataStorage.GetValueLong(aContext, aKey + KEY_SINGLE_PROMPT_TARGET_TIME, 0);
	}

	public static void SetPromptIntervalMS(Context aContext, String aKey, long aPromptIntervalMS) {
		DataStorage.SetValue(aContext, aKey + KEY_PROMPT_INTERVAL_MS, aPromptIntervalMS);
	}

	public static long GetPromptIntervalMS(Context aContext, String aKey) {
		return DataStorage.GetValueLong(aContext, aKey + KEY_PROMPT_INTERVAL_MS, 0);
	}

	public static void SetLastTimePrompted(Context aContext, String aKey, long aTime) {
		DataStorage.SetValue(aContext, aKey + KEY_LAST_TIME_PROMPTED, aTime);
	}

	public static long GetLastTimePrompted(Context aContext, String aKey) {
		return DataStorage.GetValueLong(aContext, aKey + KEY_LAST_TIME_PROMPTED, 0);
	}

	public static void SetPostponeTime(Context aContext, String aKey, long aTime) {
		DataStorage.SetValue(aContext, aKey + KEY_POSTPONE_TIME, aTime);
	}

	public static long GetPostponeTime(Context aContext, String aKey) {
		return DataStorage.GetValueLong(aContext, aKey + KEY_POSTPONE_TIME, 0);
	}

	public static void SetLastTimeCompleted(Context aContext, String aKey, long aTime) {

		if (Globals.IS_DEBUG)
			Log.d("STEVETEST", "SetLastTimeCompleted (" + aContext.getPackageName().toString() + "): " + aTime);
		DataStorage.SetValue(aContext, aKey + KEY_LAST_TIME_COMPLETED, aTime);

		// This should also cause the TEASER to be freed up to change 
		DataStorage.setTeaserUpdateTime(aContext, NO_TIME);
	}

	public static long GetLastTimeCompleted(Context aContext, String aKey) {
		return DataStorage.GetValueLong(aContext, aKey + KEY_LAST_TIME_COMPLETED, 0);
	}

	public static void SetIsAvailable(Context aContext, String aKey, boolean isActive) {
		DataStorage.SetValue(aContext, aKey + KEY_IS_AVAILABLE, isActive);
	}

	public static String GetDescription(Context aContext, String aKey) {
		return DataStorage.GetValueString(aContext, aKey + KEY_DESCRIPTION, DataStorage.EMPTY);
	}

	public static String GetPackageName(Context aContext, String aKey) {
		return DataStorage.GetValueString(aContext, aKey + KEY_PACKAGE_NAME, DataStorage.EMPTY);
	}

	public static String GetClassName(Context aContext, String aKey) {
		return DataStorage.GetValueString(aContext, aKey + KEY_CLASS_NAME, DataStorage.EMPTY);
	}

	public static boolean GetIsAvailable(Context aContext, String aKey) {
		return DataStorage.GetValueBoolean(aContext, aKey + KEY_IS_AVAILABLE, false);
	}
	
}
