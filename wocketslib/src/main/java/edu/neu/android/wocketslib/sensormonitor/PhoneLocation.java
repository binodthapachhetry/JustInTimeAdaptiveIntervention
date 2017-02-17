package edu.neu.android.wocketslib.sensormonitor;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.mhealthformat.LowSamplingRateDataSaver;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

// TODO fix to same in correct location and format
public class PhoneLocation {
	private final static String TAG = "PhoneLocation";
    private LocationManager myLocationManager;
    private Location myLocation;

    public PhoneLocation(Context c) {
		super();
		this.myLocationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		this.myLocation = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
    
    public void retrieveCurrentLocationAndSave(Context c){
    	if(getCurrentLocation() == null){
    		Log.e(TAG, "Can not get current Location.");
    		Log.o(TAG, "Unavailable");
    		return;
    	}
    	saveLocationDataIntoFile(c);
    }
    
	private Location getCurrentLocation(){
    	if(!checkWirelessEnabled())
    		return null;
		myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10*1000, 0, myLocationListener);
		return myLocation;
    }
	
	private void saveLocationDataIntoFile(Context c){
		
		// Save in a standard log file because we know this works now
		Log.o(TAG, Long.toString(myLocation.getTime()), Double.toString(myLocation.getLatitude()), Double.toString(myLocation.getLongitude()), Float.toString(myLocation.getAccuracy()));

		Date timeOfLocation = Calendar.getInstance().getTime();
		timeOfLocation.setTime(myLocation.getTime());
		
		Context aContext = c;
		String[] header = { "TIMESTAMP", "Latitude", "Longitude","Accuracy" };
		String[] values = { Double.toString(myLocation.getLatitude()), Double.toString(myLocation.getLongitude()), Float.toString(myLocation.getAccuracy()) };
		LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(Globals.IS_SENSOR_DATA_EXTERNAL, Globals.SENSOR_TYPE_GPS, PhoneInfo.getID(aContext), header);			

        if(Globals.IS_GPS_ENCRYPTION_ENABLED) {
            dataSaver.saveData(timeOfLocation,values,true,c);
        } else {
            dataSaver.saveData(timeOfLocation, values);
        }
		// This is broken. 
		// TODO fix location to save in proper data format! 
//		Date now = new Date();
//		SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
//		SimpleDateFormat hour = new SimpleDateFormat("HH");
//		SimpleDateFormat detail = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa");
//
//		String dirName = Globals.DATA_DIRECTORY + File.separator + day.format(now) + "/Location/";
////		String dirName = Globals.rawDataFilePath + day.format(now) + "/Location/";
//
//		String dataFileName = dirName +"MyLocation_"+hour.format(now)+".csv";
//		String header = "Date,CreateTime,Latitude,Longtitude,Accuracy(meter)\n";
//		try {
//			FileHelper.createDirsIfDontExist(dataFileName);
//			StringBuffer content = new StringBuffer();
//			if (!FileHelper.isFileExists(dataFileName))
//					content.append(header);
//			content.append(detail.format(now)+","+myLocation.getTime()+","+
//							myLocation.getLatitude()+","+myLocation.getLongitude()+","+myLocation.getAccuracy()+"\n");
//			FileHelper.appendToFile(content.toString(), dataFileName);
//			
//		} catch(WOCKETSException e) {
//			Log.e(TAG, "Can not write to file. Failed to create file: "+ dataFileName + " " + e.toString());
//		}
	}

	private LocationListener myLocationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void onLocationChanged(Location location) {
			if(location!=null){
				if(isBetterLocation(location, myLocation))
					myLocation = location;
			}
		}
	};
	private final static long Two_Minutes = 60*2*1000;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > Two_Minutes;
	    boolean isSignificantlyOlder = timeDelta < -Two_Minutes;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate ) {
	        return true;
	    }
	    return false;
	}

	private boolean checkWirelessEnabled(){
		return myLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
}
