package edu.neu.android.wocketslib.support;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;

public class NewsChecker {
	private static final String TAG = "NewsChecker";
	
	public static boolean isNewNewsAvailable(Context aContext)
	{
		int newsCheckSum = NewsChecker.getNewsChecksum(aContext);
		int thisVersion = DataStorage.getNewsChecksum(aContext, 0);
		if ((newsCheckSum != 0) &&
			//(thisVersion != 0) && 
			(thisVersion != newsCheckSum))
			return true;
		else
			return false;		
	}

	public static void setRead(Context aContext)
	{
		int newsCheckSum = NewsChecker.getNewsChecksum(aContext);
		if (newsCheckSum != 0)
			DataStorage.setNewsChecksum(aContext, newsCheckSum);
	}
	
	public static int getNewsChecksum(Context aContext)
	{
		String urlString;
		urlString = Globals.NEWS_URL;
			
		String htmlSource = "";
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	
			// get html as string
			InputStream is = new BufferedInputStream(urlConnection.getInputStream(),8000);
			BufferedReader r = new BufferedReader(new InputStreamReader(is),8000);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				sb.append(line);
			}
			htmlSource = sb.toString();
			urlConnection.disconnect();
		} catch (MalformedURLException e) {
			Log.e(TAG, "[newsChecker] MalformedURLException for url: "
					+ urlString + ", " + e.getMessage());
			return 0;
		} catch (IOException e) {
			Log.e(TAG, "[newsChecker()] IOException for url: " + urlString
					+ ", " + e.getMessage());
			return 0;
		}

		return htmlSource.length();
	}
	
//	private static String getVersionFromMarketWebsite(String urlString, String packageName) {
//		String htmlSource = "";
//		// using the https site since http doesn't seem to work
//		try {
//			URL url = new URL(urlString);
//			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//			urlConnection.setHostnameVerifier(new HostnameVerifier() {
//				// if this isn't included, will get a "Hostname not verified"
//				// error
////				@Override
////				public boolean verify(String hostname, SSLSession session) {
////					return true;
////				}
//
//				@Override
//				public boolean verify(String hostname, SSLSession session) {
//					return true;
//				}
//			});
//
//			// get html as string
//			InputStream is = new BufferedInputStream(urlConnection.getInputStream(),8000);
//			BufferedReader r = new BufferedReader(new InputStreamReader(is),8000);
//			StringBuilder sb = new StringBuilder();
//			String line;
//			while ((line = r.readLine()) != null) {
//				sb.append(line);
//			}
//			htmlSource = sb.toString();
//
//			urlConnection.disconnect();
//		} catch (MalformedURLException e) {
//			Log.e(TAG, "[getVersionFromMarket()] MalformedURLException for url: "
//					+ urlString + ", " + e.getMessage());
//			return "";
//		} catch (IOException e) {
//			Log.e(TAG, "[getVersionFromMarket()] IOException for url: " + urlString
//					+ ", " + e.getMessage());
//			return "";
//		}
//
//		// look for version in html
//		Pattern p = Pattern
//				.compile("\\<dt\\>Current Version\\:\\<\\/dt\\>\\<dd\\>(.*?)\\<\\/dd\\>");
//		Matcher m = p.matcher(htmlSource);
//		while (m.find()) {
//			return m.group(1);
//		}
//		Log.e(TAG, "[getVersionFromMarket()] No regular expression match in market html for package: " + packageName);
//		return "";
//	}	
}