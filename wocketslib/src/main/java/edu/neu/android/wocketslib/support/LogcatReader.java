package edu.neu.android.wocketslib.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.neu.android.wocketslib.utils.Log;

public class LogcatReader {
	private static final String TAG = "LogcatReader";

	public static boolean isInLogcat(String aTargetString, boolean isClear) {
		StringBuilder log = new StringBuilder();
		boolean isFoundTarget = false;
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(aTargetString)) {
					isFoundTarget = true;
					log.append("Found inhaler! ");
					break;
				}
			}
			if (isFoundTarget) {
				if (isClear)
					process = Runtime.getRuntime().exec("logcat -c");
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not read or clear logcat.");
		}

		if (isFoundTarget)
			Log.d(TAG, "Logcat items found NEW: TRUE");
		else
			Log.d(TAG, "Logcat items found NEW: FALSE");
		Log.d(TAG, log.toString());
		return isFoundTarget;
	}

	public static boolean isElapsedWithinTime(int threshold) {
		StringBuilder log = new StringBuilder();
		boolean isElapsed = false;
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("elapsed")) {
					line = line.substring(line.indexOf("elapsed"));
					if (line.indexOf("=") != -1 && line.indexOf(",") != -1) {
						line = line.substring(line.indexOf("=") + 1, line.indexOf(","));
						int elapsedTime = Integer.parseInt(line);
						log.append("Inhaler elapsed time found: " + elapsedTime);
						isElapsed = elapsedTime < threshold;
						if (isElapsed)
							break;
					}
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not read or clear logcat.");
		}
		Log.d(TAG, log.toString());
		return isElapsed;
	}

	public static void clearLogcat() {
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (IOException e) {
			Log.e(TAG, "Could not clear logcat.");
		}
	}

}
