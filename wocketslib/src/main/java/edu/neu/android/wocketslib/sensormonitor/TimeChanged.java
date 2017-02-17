package edu.neu.android.wocketslib.sensormonitor;

import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.Log;

public class TimeChanged {
	private final static String TAG = "TimeChanged";
	
	@SuppressLint("NewApi")
	public static void checkIfTimeChanged(Context mContext) {
		Log.d(TAG, "Check If Down");
		long installTime = 0;
		PackageManager packageManager = mContext.getPackageManager();
		String packageName = mContext.getPackageName().trim();
		try {
			installTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
			Log.d(TAG, new Date(installTime).toString());
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long lastUpTime = DataStorage.GetValueLong(mContext, Globals.KEY_LAST_UPTIME, installTime);
		if (System.currentTimeMillis() < lastUpTime) {
			Log.o(TAG, "Time changed to the back", new Date(lastUpTime).toString(), new Date().toString());
		}
	}
}
