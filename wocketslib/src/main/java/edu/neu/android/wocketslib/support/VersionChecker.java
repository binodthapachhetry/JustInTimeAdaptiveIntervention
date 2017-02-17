package edu.neu.android.wocketslib.support;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneNotifier;

public class VersionChecker {
	private static final String TAG = "VersionChecker";
	
	
	/**
	 * Check if the new update is available on Google Play Store AND the updated
	 * date is (more than) two days ago. If so, show a notification which leads to Google Play Store
	 * 
	 * @param aContext
	 *            The applications's context
	 * @param days
	 *            The days interval that you want to perform version checking from Google Play Store.
	 *
	 */
	public static void checkNewUpdateAvailable(Context aContext, int days) {
		long now = new Date().getTime();
		long lastTimeChecked = DataStorage.GetValueLong(aContext, Globals.LAST_TIME_VERSION_CHECK, 0L);
		long daysIntervalInMs = days * 24 * 60 * 60 * 1000;
		try {
			Log.e(TAG, "checkNewUpdateAvailable:" + (now - lastTimeChecked) + ":" + daysIntervalInMs);
			if (  now - lastTimeChecked >= daysIntervalInMs ) {
				if(VersionChecker.isNewUpdateAvailable(aContext, Globals.PACKAGE_NAME)) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=" + Globals.PACKAGE_NAME));
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PhoneNotifier.showNormalNotification(aContext, intent,
								"Your application needs to be updated. Click to go to Google Play Store.");
				  }
				DataStorage.SetValue(aContext, Globals.LAST_TIME_VERSION_CHECK, now);
			}
		} catch (IOException e) {
			Log.e(TAG, "[getVersionFromMarket()] IOException for url. " + e.getMessage());
			//It will recheck the version after REPROMPT_DELAY
			DataStorage.SetValue(aContext, Globals.LAST_TIME_VERSION_CHECK, now - daysIntervalInMs + Globals.REPROMPT_DELAY_MS);
		}
	}
	
	
	/**
	 * Check if the new update is available on Google Play Store AND the updated date is (more than) two days ago.
	 * This method assumes that the package name displayed in the url on Google Play is the same as the current package name.
	 * If not, use isNewUpdateAvailable(Context aContext, String packageNameOnMarket) instead.
	 * @param aContext
	 * @return
	 * @throws IOException 
	 */
	public static boolean isNewUpdateAvailable(Context aContext) throws IOException {
		String packageName = aContext.getApplicationInfo().packageName;
		return isNewUpdateAvailable(aContext, packageName);
	}
	
	/**
	 * Check if the new update is available on Google Play Store AND the updated date is (more than) two days ago.
	 * @param aContext The context of your app.
	 * @param packageNameOnMarket Your app's package name displayed in the url on Google Play
	 * @return
	 * @throws IOException 
	 */
	public static boolean isNewUpdateAvailable(Context aContext, String packageNameOnMarket) throws IOException
	{
		int[] marketVersionCode = VersionChecker.getFullVersionFromMarket(packageNameOnMarket);
		Log.e(TAG, "isnewupdated:market:" + marketVersionCode[0] + ":" + marketVersionCode[1]);
		int[] thisVersionCode = VersionChecker.getFullVersionFromLocal(aContext);
		Log.e(TAG, "isnewupdated:local:" + thisVersionCode);
		
		/*
		 * Version code could be 0 in the extreme case. 
		if ((thisVersionCode != 0) &&
			(marketVersionCode != 0) && 
			*/
		return compareVersion(thisVersionCode, marketVersionCode);	
	}
	
	public static boolean compareVersion(int[] localVersion, int[] marketVersion) {
		if (localVersion[0] == -1 || localVersion[1] == -1 || marketVersion[0] == -1 || marketVersion[1] == -1) 
			return false;
		
		if (localVersion[0] < marketVersion[0]) 
			return true;
		
		if ((localVersion[0] == marketVersion[0]) && (localVersion[1] < marketVersion[1]))
			return true;
		
		return false;
	}
	
	/**
	 * Convert version String to an int array with 2 length. The first index of the array is main version, the second is subversion.
	 * @param versionString
	 * @return versionInts
	 * 
	 */
	public static int[] getVersionCodeFromString(String versionString)
	{
		String[] codes = versionString.split("\\.");
		int[] versionInts = new int[2];
		if (codes.length == 2) {
			versionInts[0] = Integer.parseInt(codes[0].trim()); 
			versionInts[1] = Integer.parseInt(codes[1].trim()); 
			return versionInts;
		}
		return new int[]{-1,-1}; 		
	}
	
	public static int getVersionCodeFromMarket(String packageNameOnMarket) throws IOException
	{
		String version = getVersionFromMarket(packageNameOnMarket);
		return getVersionCodeFromFancyVersionString(version);
	}
	
	public static int getVersionCodeFromFancyVersionString(String versionString)
	{
		String[] codes = versionString.split("\\.");
		if (codes.length >= 2)
		{
			return Integer.parseInt(codes[1].trim()); 
		}
		return -1; 		
	}
	
	/** Return an array representing version codes in an array */
	public static int[] getFullVersionFromMarket(String packageNameOnMarket) throws IOException {
		String urlString = "https://market.android.com/details?id=" + packageNameOnMarket;
		String versionFromMarket = getVersionFromMarketWebsite(urlString, packageNameOnMarket);
		int[] versionCodeFromMarket = getVersionCodeFromString(versionFromMarket);
		return versionCodeFromMarket;		
	}
		
	/** Only get the last version code in String*/
	public static String getVersionFromMarket(String packageNameOnMarket) throws IOException {
		Log.e(TAG, "1");
		String urlString = "https://market.android.com/details?id=" + packageNameOnMarket;
		String versionFromMarket = getVersionFromMarketWebsite(urlString, packageNameOnMarket);
		int versionCodeFromMarket = getVersionCodeFromFancyVersionString(versionFromMarket);

		return "1." + ((int) versionCodeFromMarket);		
	}
	
	private static String getVersionFromMarketWebsite(String urlString, String packageNameOnMarket) throws IOException {
		String htmlSource = "";
		// using the https site since http doesn't seem to work
		
			URL url = new URL(urlString);
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setHostnameVerifier(new HostnameVerifier() {
				// if this isn't included, will get a "Hostname not verified"
				// error
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});

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
		
		// look for version in html
//		Pattern p = Pattern
//				.compile("\\<dt\\>Current Version\\:\\<\\/dt\\>\\<dd\\>(.*?)\\<\\/dd\\>");
		
		/*
		 * Use Regex to find the version code
		 * */
		Pattern p = Pattern.compile("<div class=\"content\" itemprop=\"softwareVersion\">\\s*(\\d+\\.\\d+)\\s*</div>");
		Matcher m = p.matcher(htmlSource);
		String versionString = "";
		if (m.find()) {
			 versionString = m.group(1);
		} else {
			Log.e(TAG, "[getVersionFromMarket()] No regular expression match version code in market html for package: " + packageNameOnMarket);
			return "";
		}
				
		/*
		 * Check if the updated date is (more than) 2 days before.
		 * 
		 * */
		
		Pattern p1 = Pattern.compile("<div class=\"content\" itemprop=\"datePublished\">\\s*(\\w+\\s*\\d{1,2},\\s*\\d{4})\\s*</div>");
		Matcher m1 = p1.matcher(htmlSource);
		String dateString = "";
		if(m1.find()) {
			dateString = m1.group(1);
		}else {
			Log.e(TAG, "[getVersionFromMarket()] No regular expression match updated date in market html for package: " + packageNameOnMarket);
			return "";
		}
		
		Date today = new Date();
		Date updatedDate;
		try {
			updatedDate = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(dateString);
		} catch (ParseException e) {
			Log.e(TAG, "[getVersionFromMarket()] Date parse exception. ");
			return "";
		}
		
		if(DateHelper.isMoreThanTwoDaysBefore(updatedDate, today))
			return versionString;
		else
			//No need to update if the app is updated within 2 days.
			return "";
	}	
	
	/**
	 * This method will return an int array representing the installed app's local version code.
	 * @param aContext The app's context
	 * @return
	 */
	public static int[] getFullVersionFromLocal(Context aContext) {
		String packageName = aContext.getApplicationInfo().packageName;
        PackageManager pm = aContext.getPackageManager();
        PackageInfo pi = null; 
        try {
        	if (pm != null) {
        		pi =  pm.getPackageInfo(packageName, 0);
        	}
            if (pi != null) {
            	int[] versionCodeFromLocal = VersionChecker.getVersionCodeFromString(pi.versionName);
            	return versionCodeFromLocal;
            }
            return new int[]{-1,-1}; 
        } catch (NameNotFoundException e) {
        	Log.e(packageName, "Problem getting app version \n" + e.toString());
        	return new int[]{-1,-1}; 
        }
	}
	
	/**
	 * This method will return the string representing the installed app's version.
	 * @param aContext The app's context
	 * @return
	 */
	public static String getVersionName(Context aContext) {
		String packageName = aContext.getApplicationInfo().packageName;
        PackageManager pm = aContext.getPackageManager();
        PackageInfo pi = null; 
        try {
        	if (pm != null)
        		pi =  pm.getPackageInfo(packageName, 0);
            if (pi != null)
            	return pi.versionName;
           	return "Version unknown"; 
        } catch (NameNotFoundException e) {
        	Log.e(TAG, "Problem getting app version \n" + e.toString());
        	return "Version unknown"; 
        }
	}
	
	public static int getVersionCode(Context aContext) {
		String packageName = aContext.getApplicationInfo().packageName;
        PackageManager pm = aContext.getPackageManager();
        PackageInfo pi = null; 
        try {
        	if (pm != null)
        		pi =  pm.getPackageInfo(packageName, 0);
            if (pi != null)
            	return pi.versionCode;
           	return -1; 
        } catch (NameNotFoundException e) {
        	Log.e(TAG, "Problem getting app version \n" + e.toString());
        	return -1; 
        }
	}
	
}