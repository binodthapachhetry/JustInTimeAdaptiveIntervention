package edu.neu.android.wocketslib.sensormonitor;

import java.util.List;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.Log;

public class WocketSensorDataStorer {
	private static final String TAG = "WocketSensorDataStorer";
	private static final String KEY = "WocketSensorDataStorer-";

	@SerializedName("msp")
	public List<SummaryPoint> mSummaryPoints;

	@SerializedName("mac")
	public String macID; 
	
//	private static String cleanID(String aString)
//	{
//		return aString.replace(':', '-'); 		
//	}
	
	public static void storeData(Context aContext, WocketSensorDataStorer aWSDS, String macID)
	{
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		String aJSONString = gson.toJson(aWSDS);
		//Log.e(TAG, "JSON STRING CREATED: " + aJSONString);
//		boolean isSavedInt = FileUtils.saveStringInternal(aContext, aJSONString, "", cleanID(macID));		
////		isSavedInt = FileUtils.saveStringExternal(aJSONString, "", cleanID(macID));		
//		Log.e(TAG, "===========================================================================JSON STRING SAVED: " + isSavedInt);
		DataStorage.SetValue(aContext, KEY + macID, aJSONString);
	}	
	
	public static WocketSensorDataStorer loadData(Context aContext, String macID)
	{
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create(); 
		String aJSONString = DataStorage.GetValueString(aContext, KEY + macID, "");
//		String aJSONString = FileUtils.readStringInternal(aContext, "", cleanID(macID));
		//Log.e(TAG, "JSON STRING READ: " + aJSONString);
		if ((aJSONString == null) || (aJSONString.compareTo("")==0))
		{
			Log.e(TAG,"WARNING: No WocketSensorDataStorer data loaded");
			return null; 
		}
		else
		{
			return gson.fromJson(aJSONString, WocketSensorDataStorer.class); 
		}
			
	}	

}
