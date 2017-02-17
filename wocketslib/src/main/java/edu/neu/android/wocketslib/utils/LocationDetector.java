package edu.neu.android.wocketslib.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import android.graphics.PointF;
import android.location.Location;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.algorithm.DBSCANAlgorithm;
import edu.neu.android.wocketslib.support.DataStorage;

public class LocationDetector {
	
	private final static String TAG = "LocationDetector";
    // result code
    public final static int LOADING_SUCCEEDED    = 0;
    public final static int ERR_CANCELLED        = -1;
    public final static int ERR_NO_LOCATION_DATA = -2;
    public final static int ERR_IO_EXCEPTION     = -3;
    
    public final static float EPS = 75;
    public final static int MIN_POINTS = 6; // set to small to get more clusters    
    public final static int DATA_OF_INTEREST = 20; // make it small to be sensitive to the changes
	
	private static ArrayList<LocationData> sLocations = new ArrayList<LocationData>(); 

	public static boolean isLocationChanged() {
		
//		String date = DateHelper.serverDateFormat.format(new Date());

		String date = DateHelper.getServerDateString(new Date());

		loadLocations(date);
		
		// Extract the most recent GPS data		
		PointF[] dataset = null;
		if (sLocations.size() < DATA_OF_INTEREST) {
			return false;
		} else {
			dataset = new PointF[DATA_OF_INTEREST];
			for (int i = 0, j = sLocations.size() - 1; i < DATA_OF_INTEREST; ++i, --j) {
				LocationData data = sLocations.get(j);
				dataset[i] = new PointF(data.getLatitude(), data.getLongitude());
			}
		}
				
		// Cluster the data with DBSCAN
		DBSCANAlgorithm algorithm = DBSCANAlgorithm.getInstance();
		long start = System.currentTimeMillis();
		List<HashSet<PointF>> clusters = algorithm.doCluster(dataset, EPS, MIN_POINTS);
		long end = System.currentTimeMillis();
		Log.i(TAG, "cluster: " + (end - start));
		if (clusters.size() == 0) {
			return false;
		}
		
		// Get the centroid of each cluster
		ArrayList<PointF> centroids = new ArrayList<PointF>();
		for (HashSet<PointF> cluster : clusters) {
			centroids.add(getClusterCentroid(cluster));
		}
		
		// Get the centroid of the latest GPS data, use multiple values
		// to get rid of the effect from the noise as much as possible		
		PointF current = new PointF(0, 0);
		for (int i = 0; i < 3; ++i) {
			current.x += dataset[i].x;
			current.y += dataset[i].y;
		}
		current.x /= 3;
		current.y /= 3;
		
		// Check whether the latest GPS data is within one of the cluster
		// If so, get the centroid of the cluster, otherwise return false
		PointF target = null;
		float[] results = new float[3];
		for (PointF centroid : centroids) {			
			Location.distanceBetween(centroid.x, centroid.y, current.x, current.y, results);
			if (results[0] <= EPS) { // The cluster has been found
				target = centroid; 
				break;
			}
		}
		if (target == null) { // Not enough data to form a cluster or too much noise at the time
			return false;
		}
		
		// Check whether the location is changed compared with the last saved location
		if (isLocationDiffFromLastTime(target)) {			
			return true;
		}
		
		return false;
	}
	
	private static void saveLocation(PointF location) {
		Gson gson = new GsonBuilder().create();
		String key = "LAST_GPS_LOCATION";
		String value = gson.toJson(location);
		DataStorage.SetValue(ApplicationManager.getAppContext(), key, value);
	}
	
	private static boolean isLocationDiffFromLastTime(PointF current) {
		// Get the last location
		Gson gson = new GsonBuilder().create();
		String key = "LAST_GPS_LOCATION";
		String value = DataStorage.GetValueString(ApplicationManager.getAppContext(), key, null);				
		if (value == null) { // First call
			saveLocation(current);
			return false;
		}		
		PointF last = gson.fromJson(value, PointF.class);
		
		// Check the distance
		float[] results = new float[3];
		Location.distanceBetween(current.x, current.y, last.x, last.y, results);
		if (results[0] <= EPS) { // Still in the same area
			return false;
		}
		
		// Save the new location
		saveLocation(current);		
		
		return true;
	}
	
	private static PointF getClusterCentroid(HashSet<PointF> cluster) {
		PointF centroid = new PointF(0, 0);		
		for (PointF point : cluster) {
			centroid.x += point.x;
			centroid.y += point.y;
		}
		centroid.x /= cluster.size();
		centroid.y /= cluster.size();
		return centroid;
	}
	
	public static int loadLocations(String date) {
		String path = Globals.IS_SENSOR_DATA_EXTERNAL ? Globals.EXTERNAL_DIRECTORY_PATH : Globals.INTERNAL_DIRECTORY_PATH;
		String[] hourDirs = FileHelper.getFilePathsDir(
			path + File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY + File.separator + date
        );
		
		sLocations.clear();

        // load the daily data from csv files hour by hour
        for (String hourDir : hourDirs) {        	        	
            String[] fileNames = new File(hourDir).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".csv") && filename.startsWith(Globals.SENSOR_TYPE_GPS);
                }
            });
            if (fileNames == null || fileNames.length == 0) {
                continue;
            }
            String filePath = hourDir + File.separator + fileNames[0];
            // load the hourly gps data
            CSVReader csvReader = null;
            try {
                csvReader = new CSVReader(new FileReader(filePath));
                String[] row = csvReader.readNext();
                while ((row = csvReader.readNext()) != null) {
                    // hack the split position
                    LocationData data = new LocationData(
                    	row[0], Float.parseFloat(row[1]), Float.parseFloat(row[2]), Float.parseFloat(row[3])
                    );
                    sLocations.add(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ERR_IO_EXCEPTION;
            } finally {
                try {
                    if (csvReader != null) {
                        csvReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (sLocations.size() == 0) {            
        	return ERR_NO_LOCATION_DATA;            
        }

        return LOADING_SUCCEEDED;
	}
	
	public static void release() {
		sLocations.clear();
	}
}
