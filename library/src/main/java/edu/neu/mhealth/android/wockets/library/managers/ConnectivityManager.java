package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.net.NetworkInfo;

import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class ConnectivityManager {

	private static final String TAG = "ConnectivityManager";

	public static void logConnectivityStatus(Context context) {
		Log.i(TAG,
				"isConnected - " + isInternetConnected(context) +
				" - isWifi - " + isWifiConnected(context) +
				" - isMobileInternet - " + isMobileInternetConnected(context) ,
				context);
	}

	public static boolean isInternetConnected(Context context) {
		android.net.ConnectivityManager cm =
				(android.net.ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	public static boolean isWifiConnected(Context context) {
		android.net.ConnectivityManager cm =
				(android.net.ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.getType() == android.net.ConnectivityManager.TYPE_WIFI;
	}

	public static boolean isMobileInternetConnected(Context context) {
		android.net.ConnectivityManager cm =
				(android.net.ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.getType() == android.net.ConnectivityManager.TYPE_MOBILE;
	}
}
