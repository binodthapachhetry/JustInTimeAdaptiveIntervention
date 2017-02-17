package edu.neu.android.wocketslib.activities.datasummaryviewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.sensormonitor.HRPoint;
import edu.neu.android.wocketslib.sensormonitor.SummaryPoint;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.sensormonitor.WocketSensor;
import edu.neu.android.wocketslib.sensormonitor.ZephyrSensor;

public class WocketsDataSaver {
	public final static int MARKER_LOCATION = -2;
	private final static String TAG = "WocketsDataViewer";
	private WocketsDataSPHelper dataHelper;
	private final static int NO_DATA = -1;
	private int[] rawDataByTAG;
	private int[] HRData;
	private int[] HRTimeStamp;
	private ArrayList< ArrayList<int[]>> summaryData;
	private List<SwappedSensor> swappedSensors;
	
	public WocketsDataSaver(Context c) {
		super();
		this.rawDataByTAG = new int[WocketsDataSPHelper.TAGS.length];
		for (int i = 0; i < rawDataByTAG.length; i++) {
			rawDataByTAG[i] = NO_DATA;
		}
		this.swappedSensors = WocketInfoGrabber.getSwappedSensors(c);
		this.dataHelper = new WocketsDataSPHelper(c);
		this.summaryData = new ArrayList< ArrayList<int[]>>();		
	};

	public void saveRawDataById(String address, int rawData){
		String bodyLocation = findLocationById(address);
		if(bodyLocation != null){
			if(bodyLocation.equals(Globals.locations[0])){
				rawDataByTAG[3] = rawData;
			}
			else if(bodyLocation.equals(Globals.locations[1])){
				rawDataByTAG[7] = rawData;
			}
			else if(bodyLocation.equals(Globals.locations[2])){
				rawDataByTAG[1] = rawData;
			}
			else if(bodyLocation.equals(Globals.locations[3])){
				rawDataByTAG[5] = rawData;
			}
			else if(bodyLocation.equals(Globals.locations[4])){
				rawDataByTAG[11] = rawData;
			}
			else if(bodyLocation.equals(Globals.locations[5])){
				rawDataByTAG[9] = rawData;
			}

		}
	}
	public void setSummaryDataById(String address, int[] summaryPoints, int[] timeStampsInMin){
		if(summaryPoints.length != timeStampsInMin.length){
			Log.e(TAG, "Error in summary data: data size and timestamp size not match.");

			return;
		}
		
		ArrayList<int[]> data = new ArrayList<int[]>();
		int[] locationID = new int[2];
		locationID[0] = MARKER_LOCATION;
		String bodyLocation = findLocationById(address);
		if(bodyLocation != null){
			if(bodyLocation.equals(Globals.locations[0])){
				locationID[1] = 2;
			}
			else if(bodyLocation.equals(Globals.locations[1])){
				locationID[1] = 6;
			}
			else if(bodyLocation.equals(Globals.locations[2])){
				locationID[1] = 0;
			}
			else if(bodyLocation.equals(Globals.locations[3])){
				locationID[1] = 4;
			}
			else if(bodyLocation.equals(Globals.locations[4])){
				locationID[1] = 10;
			}
			else if(bodyLocation.equals(Globals.locations[5])){
				locationID[1] = 8;
			}
			data.add(locationID);
			data.add(summaryPoints);
			data.add(timeStampsInMin);
			
			summaryData.add(data);
		}
	}
	
	public void setInternalData(int data){
		rawDataByTAG[13] = data;
	}
	public void setHRData(ZephyrSensor zephyr){
		if(zephyr != null){
			ArrayList<HRPoint> hrPoints = new ArrayList<HRPoint>();
			hrPoints.addAll(zephyr.mHRPoints);
			int[] data = getHRPoints(hrPoints);
			int[] timeStamp = getZephyrTimeStamps(hrPoints);
			if(data!=null && timeStamp!=null && 
					data.length == timeStamp.length){
				HRData = data;
				HRTimeStamp = timeStamp;
				Log.i(TAG, "Zephyr data added: from "+data.length+" to "+HRData.length);
			}
		}
	}
	public void cleanAndCommitData(Date date){
		Log.d(TAG, "Start cleaning old data");
		dataHelper.clearAllOldData(date);
		Log.d(TAG, "Start committing summary data.");
		this.commitSummaryData();
		Log.d(TAG, "Start committing raw data.");
		this.commitRawData(date);
		Log.d(TAG, "Start committing internal data.");
		this.commitInternalData(date);
		Log.d(TAG, "Finish cleaning old data and commit new data.");
	}
	public void commitHRData(){
		if(HRData != null && HRData.length > 0){
			for (int i = 0; i < HRData.length; i++) {
				dataHelper.setDataForMinute(WocketsDataSPHelper.TAGS[12], HRTimeStamp[i], HRData[i]);
			}
			Log.i(TAG, "Zephyr data committed.");
		}
	}

	public void commitInternalData(Date date){
		if(rawDataByTAG[13] != NO_DATA){
			Date dateAfterShift = dataHelper.dateShifted(WocketsDataSPHelper.TAGS[13], date);
			dataHelper.setLastDataTime(WocketsDataSPHelper.TAGS[13], dateAfterShift);
			dataHelper.setDataTime(WocketsDataSPHelper.TAGS[13], dateAfterShift, rawDataByTAG[13]);
		}
	}

	public void commitRawData(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		Log.d("debug", "save raw data in: "+sdf.format(date));

		for (int i = 0; i < rawDataByTAG.length; i++) {
			if(WocketsDataSPHelper.TAGS[i].contains("RAW")){
				if(rawDataByTAG[i] != NO_DATA){
					Date dateAfterShift = dataHelper.dateShifted(WocketsDataSPHelper.TAGS[i], date);
					dataHelper.setLastDataTime(WocketsDataSPHelper.TAGS[i], dateAfterShift);
					dataHelper.setDataTime(WocketsDataSPHelper.TAGS[i], dateAfterShift, rawDataByTAG[i]);
				}
			}
		}
	}

	public void commitSummaryData(){
		if(summaryData.size() > 0){
			for (ArrayList<int[]> data : summaryData) {
				int[] locationRecord = data.get(0);
				int location;
				if(locationRecord[0] == MARKER_LOCATION){
					location = locationRecord[1];
					int[] points = data.get(1);
					int[] timeStamps = data.get(2);
					for (int i = 0; i < points.length; i++) {
						dataHelper.setDataForMinute(WocketsDataSPHelper.TAGS[location], timeStamps[i], points[i]);						
					}
				}
			}
		}
	}

	private String findLocationById(String address){
		for (SwappedSensor sensor : swappedSensors) {
			if(address.equals(sensor.macID))
				return sensor.bodyLocation;
		}
		return null;
	}
	public static int[] getSummaryPoints(WocketSensor wocket){
		ArrayList<Integer> pointList = new ArrayList<Integer>();
		for (SummaryPoint sp: wocket.mSummaryPoints)
		{
			pointList.add(sp.mActivityCount);
		}
		int[] summarypoints = new int[pointList.size()];
		for (int i = 0; i < pointList.size(); i++) {
			summarypoints[i] = pointList.get(i);
		}
		return summarypoints;
	}
	public static int[] getHRPoints(ArrayList<HRPoint> HRPoints){
		ArrayList<Integer> pointList = new ArrayList<Integer>();
		if(HRPoints!=null && HRPoints.size() > 0){
			for (HRPoint aHRPoint: HRPoints)
			{			
				pointList.add(aHRPoint.mRate);
			}
			int[] summarypoints = new int[pointList.size()];
			for (int i = 0; i < pointList.size(); i++) {
				summarypoints[i] = pointList.get(i);
			}
			return summarypoints;
			}
		else
			return null;
	}
	public static int[] getSummaryTimeStamps(WocketSensor wocket){
		ArrayList<Integer> timeStampList = new ArrayList<Integer>();
		for (SummaryPoint sp: wocket.mSummaryPoints)
		{
			Date time = sp.mActualTime;
			if(time != null)
				timeStampList.add(time.getMinutes()+time.getHours()*60);
		}
		int[] summaryTime = new int[timeStampList.size()];
		for (int i = 0; i < timeStampList.size(); i++) {
			summaryTime[i] = timeStampList.get(i);
		}
		return summaryTime;
	}
	public static int[] getZephyrTimeStamps(ArrayList<HRPoint> HRPoints){
		ArrayList<Integer> timeStampList = new ArrayList<Integer>();
		if(HRPoints !=null && HRPoints.size() > 0){
			for (HRPoint aHRPoint: HRPoints)
			{
				Date time = aHRPoint.mPhoneReadTime;
				if(time != null)
					timeStampList.add(time.getMinutes()+time.getHours()*60);
			}
			int[] summaryTime = new int[timeStampList.size()];
			for (int i = 0; i < timeStampList.size(); i++) {
				summaryTime[i] = timeStampList.get(i);
			}
			return summaryTime;
		}
		else 
			return null;
	}
}
