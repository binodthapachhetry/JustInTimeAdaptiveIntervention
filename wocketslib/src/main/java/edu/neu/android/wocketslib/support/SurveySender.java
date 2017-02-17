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

public class SurveySender {
	private static final String TAG = "SurveySender";
		
	public static int sendSurveyResults(Context aContext)
	{
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
}