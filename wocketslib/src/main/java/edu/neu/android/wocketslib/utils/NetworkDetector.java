package edu.neu.android.wocketslib.utils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import edu.neu.android.wocketslib.Globals;

public class NetworkDetector {

	public static boolean isConnected(Context cxt){
		ConnectivityManager nw=(ConnectivityManager)cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiinfo=nw.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileinfo = nw.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return wifiinfo.isConnected()||mobileinfo.isConnected();
	}

	public static boolean isWifiConnected(Context cxt){
		ConnectivityManager nw=(ConnectivityManager)cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiinfo=nw.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifiinfo.isConnected();
	}

	public static boolean isMobileConnected(Context cxt){
		if(!isWifiConnected(cxt)){
			ConnectivityManager nw=(ConnectivityManager)cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobileinfo = nw.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			return mobileinfo.isConnected();
		}else{
			return false;
		}
	}

	public static boolean isServerAvailable() {
		URLConnection connection;
		try {
			URL url = new URL(Globals.DEFAULT_PING_ADDRESS);
			connection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			httpConnection.setConnectTimeout(3000);
			httpConnection.setReadTimeout(3000);
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = httpConnection.getInputStream();
				String is = Util.convertStreamToString(in);
				if (is.equals("true"))
					return true;
			}
			httpConnection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}