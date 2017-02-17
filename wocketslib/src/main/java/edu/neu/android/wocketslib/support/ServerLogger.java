package edu.neu.android.wocketslib.support;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.json.model.ActivityCountData;
import edu.neu.android.wocketslib.json.model.Note;
import edu.neu.android.wocketslib.json.model.PhoneData;
import edu.neu.android.wocketslib.json.model.PromptEvent;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.json.model.WocketStatsData;
import edu.neu.android.wocketslib.sensormonitor.SummaryPoint;
import edu.neu.android.wocketslib.sensormonitor.WocketSensor;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.MemoryChecker;
import edu.neu.android.wocketslib.utils.PhoneInfo;

public class ServerLogger {
    private final static String TAG = "ServerLogger";

	private static WocketInfo wi = null;
	//private WocketInfo wi = null;

	public static void reset(Context aContext) {
        if (wi == null)
		    wi = new WocketInfo(aContext);
	}

	// // Helper (might increase speed)
	// private String getPhoneID()
	// {
	// if (phoneID == null)
	// phoneID = PhoneInfo.getID(getApplicationContext());
	//
	// return phoneID;
	// }

	public static void sendNote(Context aContext, String aMsg, boolean isPlot)
	{
		addNote(aContext, aMsg, isPlot); 

		if (wi != null)
		{
//			DataSender.transmitOrQueueWocketInfo(aContext, wi, true);
			DataSender.queueWocketInfo(aContext, wi);
		}
	}	
	public static void transmitOrQueueNote(Context aContext, String aMsg, boolean isPlot){
		addNote(aContext, aMsg, isPlot); 

		if (wi != null)
		{
			DataSender.transmitOrQueueWocketInfo(aContext, wi);
		}
	}
	
	public static void send(String aTAG, Context aContext) {
		if (wi == null)
        {
            wi = new WocketInfo(aContext);
            return;
        }

        wi.updateInfoIfNeeded(aContext);

		if (wi.participantID == null)
		{
			Log.d(aTAG, "SubjectID not defined, so did not send any data to server");
			return; 
		}
		
		try {
			DataSender.queueWocketInfo(aContext, wi);
		} catch (ConcurrentModificationException e) {
			Log.e(aTAG, "Failed to queue Wocket info");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				Log.e(aTAG, "Error: InterruptedException in ServerLogger.send(): " + e1.toString());
			}
			DataSender.queueWocketInfo(aContext, wi);
		}
	}

	public static void initWocketsInfo(Context aContext) {
        setupWI(aContext);

		if (wi.someWocketStatsData == null)
			wi.someWocketStatsData = new ArrayList<WocketStatsData>();
		if (wi.someActivityCountData == null)
			wi.someActivityCountData = new ArrayList<ActivityCountData>();
	}

	public static void addWocketsStatsData(WocketStatsData aWocketsStatsData, Context aContext) {
        setupWI(aContext);

		wi.someWocketStatsData.add(aWocketsStatsData);
	}

	public static void addActivityCountData(ActivityCountData aActivityCountData, Context aContext) {
        setupWI(aContext);

		wi.someActivityCountData.add(aActivityCountData);
	}

	public static void addNote(Context aContext, String aMsg, boolean isPlot) {
        setupWI(aContext);

		if (wi.someNotes == null)
			wi.someNotes = new ArrayList<Note>();

		Note aNote = new Note();
		aNote.startTime = new Date(); 
		aNote.note = aMsg;
		if (isPlot)
			aNote.plot = 1;
		wi.someNotes.add(aNote);
	}
	
	public static void addWocketData(String aTAG, WocketSensor wocket, Context aContext) {
        setupWI(aContext);

		if (wi.someWocketStatsData == null)
			wi.someWocketStatsData = new ArrayList<WocketStatsData>();

		WocketStatsData aWocketStatsData = new WocketStatsData();

		if ((wocket.mLastConnectionTime == null) || (wocket.mLastConnectionTime.before(Globals.REASONABLE_DATE))) {
			Log.i(aTAG, "Creating Wocket data when the lastConnectiontime for the wocket is not set!: " + wocket.mLastConnectionTime + " RD: "
					+ Globals.REASONABLE_DATE);
			Log.e(aTAG, "Creating Wocket data when the lastConnectiontime for the wocket is not set!: " + wocket.mLastConnectionTime + " RD: "
					+ Globals.REASONABLE_DATE);
		} else {
			Log.i(aTAG, "Send this info to JSON for " + wocket.mAddress + " and connect time: " + wocket.mLastConnectionTime + ". Battery: " + wocket.mBattery
					+ " Rx: " + wocket.mBytesReceived + " Packet: " + wocket.mPacketsReceived);
			aWocketStatsData.createTime = wocket.mLastConnectionTime;
			aWocketStatsData.macID = wocket.mAddress;
			aWocketStatsData.wocketBattery = wocket.mBattery;
			aWocketStatsData.receivedBytes = (int) wocket.mBytesReceived; // TODO
			aWocketStatsData.transmittedBytes = wocket.mPacketsReceived; // aSummaryPoint.mSeqNum;
																			// //TODO
			wi.someWocketStatsData.add(aWocketStatsData);
		}

		if (wi.someActivityCountData == null)
			wi.someActivityCountData = new ArrayList<ActivityCountData>();

		ActivityCountData aActivityCountData = new ActivityCountData();

		for (SummaryPoint aSummaryPoint : wocket.mSummaryPoints) {
			if ((!aSummaryPoint.mJsonQueued) && (aSummaryPoint.mActualTime != null)) {
				aActivityCountData = new ActivityCountData();
				aActivityCountData.activityCount = aSummaryPoint.mActivityCount;
				aActivityCountData.createTime = aSummaryPoint.mActualTime;
				aActivityCountData.originalTime = aSummaryPoint.mActualTime; // TODO
																				// FIX
				aActivityCountData.macID = wocket.mAddress;
				aSummaryPoint.mJsonQueued = true;
				Log.i(aTAG, "Send activity count to JSON: " + aSummaryPoint.mActivityCount);
				wi.someActivityCountData.add(aActivityCountData);
			}
		}

		// if (wi.someWocketData == null)
		// wi.someWocketData = new ArrayList<WocketData>();
		//
		// Log.i(TAG, "Send this info to JSON for " + wocket.mAddress +
		// " and connect time: " + wocket.mLastConnectionTime + ". Battery: " +
		// wocket.mBattery + " Rx: " + wocket.mBytesReceived + " Packet: " +
		// wocket.mPacketsReceived);
		// WocketData aWocketData = new WocketData();
		// aWocketData.createTime = wocket.mLastConnectionTime;
		// aWocketData.macID = wocket.mAddress;
		// aWocketData.wocketBattery = wocket.mBattery;
		// aWocketData.receivedBytes = (int) wocket.mBytesReceived; //TODO
		// aWocketData.transmittedBytes = wocket.mPacketsReceived; //
		// aSummaryPoint.mSeqNum; //TODO
		// wi.someWocketData.add(aWocketData);
		//
		// for (SummaryPoint aSummaryPoint: wocket.mSummaryPoints)
		// {
		// if ((!aSummaryPoint.mJsonQueued) && (aSummaryPoint.mActualTime !=
		// null))
		// {
		// aWocketData = new WocketData();
		// aWocketData.activityCount = aSummaryPoint.mActivityCount;
		// aWocketData.createTime = aSummaryPoint.mActualTime;
		// aWocketData.macID = wocket.mAddress;
		// // aWocketData.wocketBattery = wocket.mBattery;
		// // aWocketData.receivedBytes = (int) wocket.mBytesReceived; //TODO
		// // aWocketData.transmittedBytes = wocket.mPacketsReceived; //
		// aSummaryPoint.mSeqNum; //TODO
		// aSummaryPoint.mJsonQueued = true;
		// Log.i(TAG, "Send activity count to JSON: " +
		// aSummaryPoint.mActivityCount);
		// wi.someWocketData.add(aWocketData);
		// }
		// }
	}

	public static void addAudioData(String aTAG, double data, Context aContext) {
        setupWI(aContext);

		if (wi.someWocketStatsData == null)
			wi.someWocketStatsData = new ArrayList<WocketStatsData>();

		if (wi.someActivityCountData == null)
			wi.someActivityCountData = new ArrayList<ActivityCountData>();

		ActivityCountData aActivityCountData = new ActivityCountData();

		aActivityCountData = new ActivityCountData();
		aActivityCountData.activityCount = (int) data;
		aActivityCountData.createTime = new Date();
		aActivityCountData.originalTime = new Date();
		aActivityCountData.macID = "1234";
		wi.someActivityCountData.add(aActivityCountData);
	}
	
	public static void addSleepWakeData(String aTAG, Context aContext, int data, Date timeStamp, String ID) {
        setupWI(aContext);

		if (wi.someActivityCountData == null)
			wi.someActivityCountData = new ArrayList<ActivityCountData>();

		ActivityCountData aActivityCountData = new ActivityCountData();
		aActivityCountData.activityCount = data;
		aActivityCountData.createTime = timeStamp;
		aActivityCountData.originalTime = timeStamp;
		aActivityCountData.macID = ID;
		wi.someActivityCountData.add(aActivityCountData);
	}
	
	public static void addSleepWakeData(String aTAG, Context aContext, int data , Date startTime, int length, String ID) {
        
    	if (wi.someActivityCountData == null)
    		wi.someActivityCountData = new ArrayList<ActivityCountData>();		
			
		for (int i = 0; i < length; i++) {
			ActivityCountData aActivityCountData = new ActivityCountData();
			Date timeStamp = new Date();
			timeStamp.setTime(startTime.getTime() + (i * Globals.MINUTES_1_IN_MS));
			aActivityCountData.activityCount = data;
			aActivityCountData.createTime = timeStamp;
			aActivityCountData.originalTime = timeStamp;
			aActivityCountData.macID = ID;
			wi.someActivityCountData.add(aActivityCountData);
    	}
	}

	// public static void addWocketData(WocketSensor wocket)
	// {
	// if (wi.someWocketData == null)
	// wi.someWocketData = new ArrayList<WocketData>();
	//
	// for (SummaryPoint aSummaryPoint: wocket.mSummaryPoints)
	// {
	// if ((!aSummaryPoint.mJsonQueued) && (aSummaryPoint.mActualTime != null))
	// {
	// WocketData aWocketData = new WocketData();
	// aWocketData.activityCount = aSummaryPoint.mActivityCount;
	// aWocketData.createTime = aSummaryPoint.mActualTime;
	// aWocketData.macID = wocket.mAddress;
	// aWocketData.wocketBattery = wocket.mBattery;
	// aWocketData.receivedBytes = (int) wocket.mBytesReceived; //TODO
	// aWocketData.transmittedBytes = wocket.mPacketsReceived; //
	// aSummaryPoint.mSeqNum; //TODO
	// aSummaryPoint.mJsonQueued = true;
	// Log.i(TAG, "Send activity count to JSON: " +
	// aSummaryPoint.mActivityCount);
	// wi.someWocketData.add(aWocketData);
	// }
	// }
	// }

	// public static void addHRData(int hr, int batteryLevel, int heartBeatNum)
	// {
	// if (wi.someHRData== null)
	// wi.someHRData = new ArrayList<HRData>();
	//
	// HRData aHRData = new HRData();
	// aHRData.createTime = new Date();
	// aHRData.heartRate = hr;
	// aHRData.battery = batteryLevel;
	// // aHRData.heartBeatNumber = heartBeatNum; //TODO
	// wi.someHRData.add(aHRData);
	// }

	public static void addPromptEvent(String aMsg, Date promptTime, String promptType, Date responseTime, Context aContext)
	{
        setupWI(aContext);

		if (wi.somePrompts == null)
			wi.somePrompts = new ArrayList<PromptEvent>();

		PromptEvent aPromptEvent = new PromptEvent();
		aPromptEvent.promptTime = promptTime; 
		aPromptEvent.promptType = promptType;
		aPromptEvent.responseTime = responseTime;
		aPromptEvent.primaryActivity = aMsg;
		wi.somePrompts.add(aPromptEvent);	
	}

    private static void setupWI(Context aContext)
    {
        if (wi == null)
            wi = new WocketInfo(aContext);
        wi.updateInfoIfNeeded(aContext);
    }

    public static void addPhoneBatteryReading(String aTAG, Context aContext, int level) {
        setupWI(aContext);

		if (wi.somePhoneData == null)
			wi.somePhoneData = new ArrayList<PhoneData>();

		PhoneData aPhoneData = new PhoneData();
		aPhoneData.createTime = new Date();
		aPhoneData.phoneBattery = level;

		int ram = (int) PhoneInfo.getAvailableRAM(aContext);
		int externalMemory = (int) Math.floor(MemoryChecker.getExternalStorageAvailable());
		int internalMemory = (int) Math.floor(MemoryChecker.getDataDirectoryStorageAvailable());

		if (Globals.IS_DEBUG) {
			Log.i(aTAG, "AvailableRAM: " + ram);
			Log.i(aTAG, "AvailableInternalMemory: " + internalMemory);
			Log.i(aTAG, "AvailableExternalMemory: " + externalMemory);
		}

		aPhoneData.sDMemory = (int) externalMemory;
		aPhoneData.mainMemory = (int) internalMemory;

		// TODO enable
		// aPhoneData.ram = (int) ram;

		wi.somePhoneData.add(aPhoneData);
	}
}
