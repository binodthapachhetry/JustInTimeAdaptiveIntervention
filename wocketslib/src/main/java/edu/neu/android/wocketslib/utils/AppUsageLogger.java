package edu.neu.android.wocketslib.utils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.Button;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;

// Test 
public class AppUsageLogger {

	private static boolean isWroteVersion = false; 

	private static final int KEYGUARD_IS_LOCKED = 1;
	private static final int KEYGUARD_NOT_LOCKED = 0;

	public static void logActivities(String aModuleName)
	{
		Log.o("Activities", aModuleName);		
	}

	public static void logSubjectID(String aModuleName, String subjectID)
	{
		if (subjectID == null)
			Log.o(aModuleName, "SubjectID", "Unknown");
		else
			Log.o(aModuleName, "SubjectID", subjectID);
	}

	public static void logID(String aModuleName, Context aContext) {
        try {
            if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                Log.o(aModuleName, "PhoneID ", RSACipher.encrypt(PhoneInfo.getIDString(aContext), aContext));
            } else {
                Log.o(aModuleName, "PhoneID ", PhoneInfo.getIDString(aContext));
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

	public static void logNoteExtended(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Note" }, more));						
	}
	
	public static void logEntryExtended(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Entry" }, more));						
	}
	
	public static void logUpdateExtended(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Update" }, more));						
	}
	
	public static void logShowExtended(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Show" }, more));						
	}
	
	public static void logStatusExtended(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Status" }, more));						
	}
	
	public static void logClick(String aModuleName, View aView) {
		if (aView instanceof Button)
		{
			Button tmpButton = (Button) (aView.findViewById(aView.getId()));
			Log.o(aModuleName, "Click",tmpButton.getText());			
		}
		else
			Log.o(aModuleName, "Click", "Non-button view");						
	}    

	// modified from Apache ArrayUtils
	public static Object[] addAll(Object[] array1, Object[] array2) {
        Object[] joinedArray = (Object[]) Array.newInstance(array1.getClass().getComponentType(),
                                                            array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because:
             * - it would be a wasted check most of the time
             * - safer, in case check turns out to be too strict
             */
            final Class<?> type1 = array1.getClass().getComponentType();
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)){
                throw new IllegalArgumentException("Cannot store "+type2.getName()+" in an array of "+type1.getName());
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }
	
	public static void logClickExtended(String aModuleName, View aView, String... more) {
		String clickText = "Unrecognized view";
		if (aView.getTag() instanceof String) {
			clickText = (String) aView.getTag();
		}
		else if (aView instanceof Button) {
			Button tmpButton = (Button) (aView.findViewById(aView.getId()));
			clickText = tmpButton.getText().toString();
		}
		Log.o(aModuleName, addAll(new String[] { "Click", clickText }, more));						
	}
	
	public static void logClickExtended(String aModuleName, View aView) {
		logClickExtended(aModuleName, aView, new String[] { });
	}
	
	public static void logClickExtended(String aModuleName, String clickText, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Click",  clickText }, more));
	}
	
	public static void logBackKey(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Press",  "Back Key" }, more));						
	}
	
	public static void logHomeKey(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Press",  "Home Key" }, more));						
	}
	
	public static void logMenuKey(String aModuleName, String... more) {
		Log.o(aModuleName, addAll(new String[] { "Press",  "Menu Key" }, more));						
	}
	
	public static void logTimeOut(String aModuleName, int secs) {
		Log.o(aModuleName, "Timeout", Integer.toString(secs));
//		Log.i(aModuleName, "Timeout: App closed after " + secs + " inactive.");
	}    

	public static void logTimeUsage(String aModuleName, int secs) {
		Log.o(aModuleName, "UsageTime", Integer.toString(secs));
//		Log.i(aModuleName, "Usage: app ran for seconds: " + secs);
	}    

	public static void logTimeUsageString(String aModuleName, String text) {		
		Log.o(aModuleName, "UsageText", text);
//		Log.i(aModuleName, "Usage: " + text);
	}    


	/**
	 * Writes the version information once for any Activities that extend the BaseActivity. Uses a static to write just once. 
	 * @param packageName packageName to get version info for
	 * @param aContext A context that must be passed in from the calling Activity required to get version info
	 */
	public static void logVersion(String aModuleName, String packageName, Context aContext) {
		if (!isWroteVersion)
		{
			Log.o(aModuleName,"PackageName", packageName);
			Log.o(aModuleName,"Version", getVersion(packageName, aContext, aModuleName));
//			Log.i(aModuleName,"ProgramName: " + packageName); 
//			Log.i(aModuleName,"Version: " + getVersion(packageName, aContext, aModuleName));
			logID(aModuleName, aContext);
			
			Globals.logKeyVariables(aModuleName);

			isWroteVersion = true; 
		}
	}
	
	public static String getVersion(Context aContext, String aModuleName)
	{
		return getVersion(aContext.getApplicationInfo().packageName, aContext, aModuleName); 
	}
	
	/** 
	 * Get the version information given a packageName and Context.
	 * @param packageName packageName to get version info for
	 * @param aContext A context that must be passed in from the calling Activity
	 * @return
	 */
	private static String getVersion(String packageName, Context aContext, String aModuleName) {
        PackageManager pm = aContext.getPackageManager();
        PackageInfo pi = null; 
        try {
        	if (pm != null)
        		pi =  pm.getPackageInfo(packageName, 0);
            if (pi != null)
            	return pi.versionName + " (Code: " + pi.versionCode + ")";
           	return "Version unknown"; 
        } catch (NameNotFoundException e) {
        	Log.e(aModuleName, "Problem getting app version \n" + e.toString());
        	return "Version unknown"; 
        }
	}
	

}
