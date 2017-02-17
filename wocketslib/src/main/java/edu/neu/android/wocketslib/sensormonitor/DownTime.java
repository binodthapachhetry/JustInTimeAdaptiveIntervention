package edu.neu.android.wocketslib.sensormonitor;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.Log;

public class DownTime {
	private final static String TAG = "DownTime";
	private static ArrayList<DownTimeWindow> downTimePool = new ArrayList<DownTimeWindow>();
	@SuppressLint("NewApi")

	public static void checkIfDown(Context mContext) {
		Log.i(TAG, "Check If Down");
		long installTime = 0;
		PackageManager packageManager = mContext.getPackageManager();
		String packageName = mContext.getPackageName().trim();
		try {
			installTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
			Log.o(TAG, "Install time: " + new Date(installTime).toString());
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long lastUpTime = DataStorage.GetValueLong(mContext, Globals.KEY_LAST_UPTIME, installTime);
		long lastDownTime = DataStorage.GetValueLong(mContext, Globals.KEY_LAST_DOWNTIME, installTime);
		if (System.currentTimeMillis() - lastUpTime > Globals.MINUTES_1_IN_MS * 2) {
			Log.o(TAG, "On", new Date(lastDownTime).toString(), new Date(
					lastUpTime).toString());
			Log.o(TAG, "Off", new Date(lastUpTime).toString(), new Date().toString());
			downTimePool.add(new DownTimeWindow(lastUpTime, System.currentTimeMillis()));
			DataStorage.SetValue(mContext, Globals.KEY_LAST_DOWNTIME, System.currentTimeMillis());
			DataStorage.SetValue(mContext, Globals.KEY_LAST_UPTIME, System.currentTimeMillis());
		} else {
			DataStorage.SetValue(mContext, Globals.KEY_LAST_UPTIME, System.currentTimeMillis());
		}
	}

	public static boolean isDown(Context mContext){
		long installTime = 0;
		PackageManager packageManager = mContext.getPackageManager();
		String packageName = mContext.getPackageName().trim();
		try {
			installTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
			Log.o(TAG, "Install time: " + new Date(installTime).toString());
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long lastUpTime = DataStorage.GetValueLong(mContext, Globals.KEY_LAST_UPTIME, installTime);
		if (System.currentTimeMillis() - lastUpTime > Globals.MINUTES_1_IN_MS * 2)
			return true;
		else
			return false;
	}



	public static boolean checkIfWithinDownTimeWindow(long promptTime) {
		for(DownTimeWindow d:downTimePool) {
			if(promptTime >= d.mstartDownTime && promptTime <= d.mendDownTime) {
				return false;
			}
		}
		return true;
	}
}
