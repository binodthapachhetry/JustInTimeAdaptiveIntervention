package edu.neu.android.wocketslib.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;

public class PhoneInfo {
	public static String TAG = "PhoneInfo";

	public static String getID(Context aContext) {

		TelephonyManager mTelephonyMgr = null;

		try {
			mTelephonyMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Exception e) {
			Log.e(TAG, "Error: Could not get access to TelephonyManager. Most likely manifest does not include android.permission.READ_PHONE_STATE permission.");
			return "";
		}

//		if (Globals.IS_DEBUG)
//			Log.d(TAG, "ID discovered is :" + mTelephonyMgr.getDeviceId());
		String id = mTelephonyMgr.getDeviceId();
		if (id == null) {
			id = Secure.getString(aContext.getContentResolver(), Settings.Secure.ANDROID_ID);
		}

		String obfuscatedId = obfuscateID(id);

		String participantLastName = DataStorage.GetValueString(aContext, DataStorage.KEY_LAST_NAME, AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED);

		if((participantLastName == null)||DataStorage.GetValueBoolean(aContext, Globals.IS_SENDING_OBFUSCATED_ID,false)) {
			DataStorage.SetValue(aContext, Globals.IS_SENDING_OBFUSCATED_ID, true);
			return obfuscatedId;
		} else {
			DataStorage.SetValue(aContext, Globals.IS_SENDING_OBFUSCATED_ID, false);
			return id;
		}
	}

	private static String obfuscateID(String id) {
		char[] idArray = id.toCharArray();

		boolean isEven = false;
		if(id.length()%2 == 0) {
			isEven = true;
		}

		if(isEven) {
			for (int i = 0 ; i < id.length() ; i = i + 2) {
				char temp = idArray[i];
				idArray[i] = idArray[i+1];
				idArray[i+1] = temp;
			}
		} else {
			for (int i = 0 ; i < id.length() ; i = i + 2 ) {
				if (i == (id.length()/2 - 1)) {
					char temp = idArray[i];
					idArray[i] = idArray[i+2];
					idArray[i+2] = temp;
					i++;
				} else {
					char temp = idArray[i];
					idArray[i] = idArray[i+1];
					idArray[i+1] = temp;
				}
			}
		}
		return new String(idArray);
	}

	public static long getAvailableRAM(Context aContext) {
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) aContext.getSystemService(aContext.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		return availableMegs;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static long getAvailabelStorage(Context aContext){
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long bytesAvailable = stat.getAvailableBytes();
		long megAvailable = bytesAvailable / 1048576L;
		return megAvailable;
	}

	public static boolean isBatteryCharging(Context aContext){
		return PhonePowerChecker.isCharging(aContext);
//
//		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//		Intent batteryStatus = aContext.registerReceiver(null, ifilter);
//		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//		return (status==BatteryManager.BATTERY_STATUS_CHARGING ||
//                status == BatteryManager.BATTERY_STATUS_FULL);
	}
	public static float getBatteryPercentage(Context aContext){
		return PhonePowerChecker.getBatteryRemaining(aContext);
//		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//		Intent batteryStatus = aContext.registerReceiver(null, ifilter);
//		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//		return (float)level/(float)scale*100;
	}


	public static String getIDString(Context aContext) {
		TelephonyManager mTelephonyMgr = null;

		try {
			mTelephonyMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Exception e) {
			Log.e(TAG, "Could not get access to TelephonyManager. Most likely manifest does not include android.permission.READ_PHONE_STATE permission.");
			return "";
		}

		String aStr = "";
		try {
			String id = PhoneInfo.getID(aContext);
			if(id == null){
				throw new Exception();
			}
			aStr += id + " ";
		} catch (Exception e) {
			aStr += "none ";
		}
		try {
			String lineNum = mTelephonyMgr.getLine1Number();
			if(lineNum == null){
				throw new Exception();
			}
			aStr += lineNum + " ";
		} catch (Exception e) {
			aStr += "none ";
		}
		try {
			String ver = mTelephonyMgr.getDeviceSoftwareVersion();
			if(ver == null){
				throw new Exception();
			}
			aStr += ver + " ";
		} catch (Exception e) {
			aStr += "none ";
		}
		try {
			String simNum = mTelephonyMgr.getSimSerialNumber();
			if(simNum == null){
				throw new Exception();
			}
			aStr += simNum + " ";
		} catch (Exception e) {
			aStr += "none ";
		}
		try {
			String subId = mTelephonyMgr.getSubscriberId();
			if(subId == null){
				throw new Exception();
			}
			aStr += subId + " ";
		} catch (Exception e) {
			aStr += "none ";
		}
		aStr = aStr.trim();
		return aStr;
	}

}
