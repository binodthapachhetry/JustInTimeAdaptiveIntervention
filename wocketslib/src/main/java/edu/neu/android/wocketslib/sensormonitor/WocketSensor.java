/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Class that models the data and behaviors of a Wocket sensor.
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.text.format.Time;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.LibGlobals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.mhealthformat.AnnotationSaver;
import edu.neu.android.wocketslib.mhealthformat.LowSamplingRateDataSaver;
import edu.neu.android.wocketslib.mhealthformat.SensorData;
import edu.neu.android.wocketslib.mhealthformat.WocketSensorData;
import edu.neu.android.wocketslib.sleepdetection.SleepAnnotation;
import edu.neu.android.wocketslib.sleepdetection.SleepDetection;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.visualizerdatagenerator.DataEncoder;

public class WocketSensor extends Sensor {

	private static final int MAX_WOCKET_STORAGE_TIME = 2996 * 25;

	private static final String TAG = "WocketSensor";

	// Wocket packet types
	private final static int WOCKET_UNCOMPRESSED = 0;
	// private final static int WOCKET_COMMAND = 1;
	private final static int WOCKET_RESPONSE = 2;
	private final static int WOCKET_COMPRESSED = 3;

	// Wocket response types
	private final static int WOCKET_RESPONSE_BATTERY_LEVEL = 0;
	private final static int WOCKET_RESPONSE_BATTERY_PERCENT = 1;
	// private final static int WOCKET_RESPONSE_PACKET_COUNT = 2;
	private final static int WOCKET_RESPONSE_SUMMARY_COUNT = 13;
	private final static int WOCKET_RESPONSE_SEND_DATA_DONE = 18;

	private final static int WOCKET_RESPONSE_SAMPLING_RATE = 5;
	// private final static int WOCKET_RESPONSE_POWER_DOWN_TIMER = 8;
	private final static int WOCKET_RESPONSE_RADIO_TRANSMISSION_MODE = 8;
	private final static int WOCKET_RESPONSE_FIRMWARE_VERSION = 11;
	private final static int WOCKET_RESPONSE_BATCH_COUNT = 12;
	private final static int WOCKET_RESPONSE_ACTIVITY_COUNT_COUNT = 15;
	private final static int WOCKET_RESPONSE_OFFSET_AC_COUNT = 16;

	// Wocket command values
	public final static int WOCKET_ACK_PACKET = 0xa0 | 27;
	public final static int WOCKET_RESET_PACKET = 0xa0 | 29;
	public final static int WOCKET_BATTERY_PERCENT_PACKET = 0xA1;
	public final static byte[] WOCKET_60_SEC_BURST_PACKET = { (byte) 0xBA,
			(byte) 0x20 };
	public final static byte WOCKET_HEADER_DATA = (byte) -1;

	public final static double FAKE_TIME_DIFF = 25;

	public long MISSING_PACKETS = 0;

	// Header file used in the Activity Summary CSV file
	// private final static String HEADER_STRING =
	// "Phone_Write_Time,AC_DecoderIndex,AC_SeqNum,AC_TimeStamp,AC_Unix_TimeStamp,AC_Value";

	// Header file used in the Activity Summary CSV file
	// private final static String HEADER_STRING_UNPROCESSED =
	// "Phone_Write_Time,AC_DecoderIndex,AC_SeqNum,AC_TimeStamp,AC_Unix_TimeStamp,AC_Value";

	public ArrayList<AccelPoint> mAccelPoints = new ArrayList<AccelPoint>();
	public ArrayList<SummaryPoint> mSummaryPoints = new ArrayList<SummaryPoint>();
	// flag to indicate if this Wocket device has been initialized and had its
	// mode bytes
	// written to it
	public boolean mInit;

	// previous x,y,x values that were read from the device
	private int prevX = 0;
	private int prevY = 0;
	private int prevZ = 0;

	private long lastACOnWocket = 0;
	private int lastRecivedSN = 0;

	// private long lastWrittenTime = 0;

	private Context aContext;
	WocketSensorData[] someWocketSensorData;
	int someWocketSensorDataIndex = 0;
	static private AnnotationSaver aAnnotationSaver;

	private int batch_count;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            - the Bluetooth name of the Wocket
	 * @param address
	 *            - the Bluetooth MAC address of the Wocket
	 */
	public WocketSensor(Context aContext, String name, String address) {
		super(Sensor.WOCKET, name, address);
		this.aContext = aContext;
		mData = new byte[Defines.MAX_WOCKET_PACKET_SIZE];
		mBytesReceived = 0;
		mPacketsReceived = 0;
		mDataToProcess = false;
		mInit = false;
		prevX = 0;
		prevY = 0;
		prevZ = 0;
		// lastACOnWocket = DataStorage.GetValueLong(aContext, "LAST_SEQ_NUM_" +
		// address, 0);

		// Create storage needed to store raw data before saving
		someWocketSensorData = new WocketSensorData[100];
		Date aDate = new Date();
		for (int i = 0; i < 100; i++)
			someWocketSensorData[i] = new WocketSensorData(
					SensorData.TYPE.WOCKET12BITRAW, aDate, 0, 0, 0);
		someWocketSensorDataIndex = 0;

		if (aAnnotationSaver == null) {
			aAnnotationSaver = new AnnotationSaver();
		}

		// loadNVData();
		// readUnprocessedWSData(aContext);
	}

	/*
	 * private void addWocketSensorDataPoint(Date aTime, int x, int y, int z) {
	 * someWocketSensorData[someWocketSensorDataIndex].mDateTime = aTime;
	 * someWocketSensorData[someWocketSensorDataIndex].mX = x;
	 * someWocketSensorData[someWocketSensorDataIndex].mY = y;
	 * someWocketSensorData[someWocketSensorDataIndex].mZ = z;
	 * someWocketSensorDataIndex++; }
	 */

	/**
	 * Estimate the battery percentage remaining
	 * 
	 * @return
	 */
	public double getBatteryPercentage() // TODO fix to use calibration values
	{
		int batteryValue = mBattery;
		int max = 750;
		int min = 550;

		if (batteryValue > max)
			return 100.0;
		else if (batteryValue < min)
			return 0.0;
		else
			return (double) (batteryValue - min) / (double) (max - min);
	}

	public int getLastSeqNum() {
		return lastRecivedSN;
		/*
		 * if(!mSummaryPoints.isEmpty()) return
		 * (short)mSummaryPoints.get(mSummaryPoints.size() -1).mSeqNum; else
		 * return 0;
		 */
	}

	public void readUnprocessedWSData(Context aContext) {
		WocketSensorDataStorer aWSDS = WocketSensorDataStorer.loadData(
				aContext, mAddress);
		if (aWSDS != null) {
			if (Globals.IS_DEBUG)
				Log.d(TAG,
						mAddress
								+ "SUMMARY POINTS ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			mSummaryPoints = new ArrayList<SummaryPoint>();
			for (SummaryPoint sp : aWSDS.mSummaryPoints) {
				mSummaryPoints.add(sp);
			}

			if (Globals.IS_DEBUG)
				for (SummaryPoint sp : mSummaryPoints) {
					Log.d(TAG, mAddress + ": READ SUMMARY POINTS: "
							+ sp.mSeqNum);
				}
		} else {
			if (Globals.IS_DEBUG)
				Log.d(TAG,
						mAddress
								+ ": NO SUMMARY POINTS ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			mSummaryPoints = new ArrayList<SummaryPoint>();
		}
	}

	private void saveUnprocessedWSData(Context aContext) {
		WocketSensorDataStorer aWSDS = new WocketSensorDataStorer();
		if (mSummaryPoints != null) {
			aWSDS.mSummaryPoints = mSummaryPoints;
			WocketSensorDataStorer.storeData(aContext, aWSDS, mAddress);
			Log.i(TAG, "Saving summary points: " + aWSDS.mSummaryPoints.size());
			// for (SummaryPoint sp: aWSDS.mSummaryPoints)
			// {
			// Log.e(TAG, mAddress + ": SAVE SUMMARY POINTS: " + sp.mSeqNum);
			// }
		} else
			Log.i(TAG, mAddress + ": NO SUMMARY POINTS to SAVE");
	}

	// private void loadUnprocessedWSData()
	// {
	// String fileName = "data/summary/UnprocessedAC_" + mName + ".json";
	//
	//
	// if( DataStore.getContext()!= null)
	// {
	// File file = new File(DataStore.getContext().getExternalFilesDir(null),
	// fileName);
	//
	// if( file != null && file.exists())
	// {
	// try
	// {
	// BufferedReader in = new BufferedReader( new FileReader(file));
	//
	// String nextLine = in.readLine();
	// while( nextLine != null)
	// {
	// in.readLine();
	// try
	// {
	// //Get everything after the first comma
	// String firstComma = nextLine.substring(nextLine.indexOf(',')+1);
	// //Get the substring from the first comma, to the next comma
	// int seqNum =
	// Integer.parseInt(firstComma.substring(0,firstComma.indexOf(',')));
	// int value =
	// Integer.parseInt(nextLine.substring(nextLine.lastIndexOf(',')+1));
	// // int daySeqNum =
	// Integer.parseInt(nextLine.substring(nextLine.lastIndexOf(',')+1));
	//
	// SummaryPoint point =new SummaryPoint(seqNum,value);
	// point.mWritten = true;
	// addSummaryPoint(point);
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	// nextLine = in.readLine();
	// }
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	//

	// /**
	// * Read in the current day's activity summary log file. Add the summary
	// points that were already
	// * written out to the array in memory so that if the points are read from
	// the Wocket device
	// * again, we can safely throw them out knowing they were already read in.
	// */
	// private void loadNVData()
	// {
	// Time now = new Time();
	// now.setToNow();
	// String dirName = "data/summary/" + now.format("%Y-%m-%d") + "/" +
	// now.format("%H") + "/";
	// String fileName = dirName + "SummaryAC_" + mName + ".csv";
	//
	// if( DataStore.getContext()!= null)
	// {
	// File file = new File(DataStore.getContext().getExternalFilesDir(null),
	// fileName);
	//
	// if( file != null && file.exists())
	// {
	// try
	// {
	// BufferedReader in = new BufferedReader( new FileReader(file));
	//
	// String nextLine = in.readLine();
	// while( nextLine != null)
	// {
	// if( !nextLine.contains(HEADER_STRING))
	// {
	// try
	// {
	// //Get everything after the first comma
	// String firstComma = nextLine.substring(nextLine.indexOf(',')+1);
	// //Get the substring from the first comma, to the next comma
	// int seqNum =
	// Integer.parseInt(firstComma.substring(0,firstComma.indexOf(',')));
	// int value =
	// Integer.parseInt(nextLine.substring(nextLine.lastIndexOf(',')+1));
	// // int daySeqNum =
	// Integer.parseInt(nextLine.substring(nextLine.lastIndexOf(',')+1));
	//
	// SummaryPoint point =new SummaryPoint(seqNum,value);
	// point.mWritten = true;
	// addSummaryPoint(point);
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	// nextLine = in.readLine();
	// }
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	/**
	 * Checks if the given SummaryPoint matches the sequence number of an
	 * already read Summary point
	 * 
	 * @param point
	 *            - the point to check
	 * @return - True if a match is found and the point is a duplicate, false
	 *         otherwise
	 */
	private boolean containsSummaryPoint(SummaryPoint point) {
		boolean retVal = false;
		if (mSummaryPoints == null)
			return false;

		for (SummaryPoint sp : mSummaryPoints) {
			if ((sp.mSeqNum == point.mSeqNum)
					&& (sp.mActivityCount == point.mActivityCount)) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}

	/**
	 * Checks if the given SummaryPoint matches the sequence number of an
	 * already read Summary point
	 * 
	 * @param point
	 *            - the point to check
	 * @return - True if a match is found and the point is a duplicate, false
	 *         otherwise
	 */
	/*
	 * private boolean containsSummaryPointOld(SummaryPoint point) { boolean
	 * retVal = false; for( int x=0;x<mSummaryPoints.size();x++) { if(
	 * mSummaryPoints.get(x).mSeqNum == point.mSeqNum) { retVal= true; break; }
	 * } return retVal; }
	 */

	/*
	 * private int clearPointsDueToReset() { Log.i(TAG,
	 * "Detected reset Sensor: " + mAddress); ArrayList<SummaryPoint>
	 * cleanedSummaryPoints = new ArrayList<SummaryPoint>(); int
	 * numRemovedSummaryPoints = 0; for(SummaryPoint sp: mSummaryPoints) { if
	 * (Globals.IS_DEBUG) Log.d(TAG,
	 * "CLEANED SUMMARY POINT BECAUSE SENSOR RESET: " + sp.mSeqNum); // if
	 * (sp.mActualTime == null) // { // // Time not yet defined so point is in
	 * flux. Remove it because can no longer determine time due to seqNum reset
	 * // Log.e(TAG, "WARNING: Throwing out SummaryPoint due to reset. Value:" +
	 * sp.mActivityCount + " SeqNum:" + sp.mSeqNum); //
	 * numRemovedSummaryPoints++; // } // else // { // // Time point already
	 * determined for the AC, so leave this point in the list //
	 * cleanedSummaryPoints.add(sp); // } } mSummaryPoints =
	 * cleanedSummaryPoints; return numRemovedSummaryPoints; }
	 */

	/**
	 * Adds the given summary point to the storage array if it is not already
	 * known. Increments the day's activity score if the value of the summary
	 * point is above the defined threshold. Once the size of the storage array
	 * reaches a defined limit, excess points will be removed from the array and
	 * written to a file.
	 * 
	 * @param point
	 *            - the SummaryPoint to add
	 */
	public void addSummaryPoint(SummaryPoint point) {
		// if (point.mSeqNum == 0)
		// {
		// isResetDetected = true;
		// int pointsCleared = clearPointsDueToReset();
		// Log.e(TAG, "WARNING: " + pointsCleared +
		// " points cleared due to reset of seqNum for sensor " + mAddress);
		// }

		// If we already have this point (match for SeqNum and AC value, just
		// dump it
		// TODO this will not always work!
		if (containsSummaryPoint(point)) {
			return;
		}

		mSummaryPoints.add(point);
	}

	private Date getPriorMinute(Date aDate) {
		long aTime = aDate.getTime();
		aTime = aTime - Globals.MINUTES_1_IN_MS;
		return new Date(aTime);
	}

	private Date getPriorMinutes(Date aDate, int mins) {
		long aTime = aDate.getTime();
		aTime = aTime - (mins * Globals.MINUTES_1_IN_MS);
		return new Date(aTime);
	}

	public void setRealTimesSummaryPoints() {

		Date lastTime = null;
		int lastSeqNum = -1;
		boolean isOutOfOrder = false;
		int lastSN = 9999999;
		// Loop through data in reverse to determine timestamps
		for (int i = mSummaryPoints.size() - 1; i >= 0; i--) {

			SummaryPoint sp = mSummaryPoints.get(i);

			if (sp.mSeqNum > lastSN) {
				Log.i(TAG, "COUNTBACK OUT OF ORDER ... WILL SORT LIST: "
						+ sp.mSeqNum);
				isOutOfOrder = true;
			}
			lastSN = sp.mSeqNum;

			Log.i(TAG, "COUNTBACK: " + sp.mSeqNum);
			if (sp.mActualTime != null) {
				// Use the time as a reference point
				lastSeqNum = sp.mSeqNum;
				lastTime = sp.mActualTime;
				// Log.e(TAG, "Leaving SN: " + lastSeqNum + " as " +
				// lastTime.toString());
			} else {
				if ((lastSeqNum != -1) && (sp.mSeqNum == (lastSeqNum - 1))
						&& (lastTime != null)) {
					// One less seqNum than one that had a date

					if (sp.mActualTime == null) {
						// Only set a time if not set already
						sp.mActualTime = getPriorMinute(lastTime);
						lastTime = sp.mActualTime;
						lastSeqNum = sp.mSeqNum;
					}
				} else if ((lastSeqNum != -1) && (lastTime != null)) {
					// Not an adjacent point, but set if within 30 points of a
					// good point.
					int diff = lastSeqNum - sp.mSeqNum;
					if ((diff > 0) && (diff < 30)) {
						// Set point
						sp.mActualTime = getPriorMinutes(lastTime, diff);
						lastTime = sp.mActualTime;
						lastSeqNum = sp.mSeqNum;
					}
				}
			}

		}

		if (isOutOfOrder) {
			// Somehow list has gotten out of order. Sort it
			Log.i(TAG, "Sorting");
			Collections.sort(mSummaryPoints);

			lastSN = 9999999;
			// Loop through data in reverse to determine timestamps
			for (int i = mSummaryPoints.size() - 1; i >= 0; i--) {
				SummaryPoint sp = mSummaryPoints.get(i);

				if (Globals.IS_DEBUG)
					Log.d(TAG, "AFTER SORT: " + sp.mSeqNum);
				if (sp.mSeqNum > lastSN) {
					Log.e(TAG, "Countback still out of order after sort");
				}
				lastSN = sp.mSeqNum;
			}
		}
		saveSummaryDataPointsToMemory();
	}

	public void saveSummaryDataPointsToMemory() {
		aAnnotationSaver.initialize("SleepWakeData", "SazonovAlgorithm", "",
				"", "", "");
		aAnnotationSaver.setDate(new Date());

		String labelForLastSeqNumOnSharedPref = mAddress
				+ "-lastSummaryPointSavedToFile";
		SharedPreferences prefs = aContext.getSharedPreferences(
				Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		int lastSeqNum = prefs.getInt(labelForLastSeqNumOnSharedPref, 0);

		int lastSeqNumInSummaryPointList = 0;

		for (int j = 0; j < mSummaryPoints.size(); j++) {
			SummaryPoint sp = mSummaryPoints.get(j);
			if (sp.mSeqNum == lastSeqNum) {
				lastSeqNumInSummaryPointList = j;
				j = mSummaryPoints.size() + 1; // this is just to get out of
												// code and not use break
			}
		}
		SummaryPoint sp = null;
		for (int j = lastSeqNumInSummaryPointList + 1; j < mSummaryPoints
				.size(); j++) {
			sp = mSummaryPoints.get(j);
			if (sp.mActualTime == null) {
				Log.e(TAG,
						"Actual time is null in the summary point with phone recorded time: "
								+ sp.mPhoneReadTime);
			} else {
				saveSummaryPointToMemory(this.mAddress, sp.mActivityCount,
						sp.mSeqNum, sp.mActualTime, true);
				lastSeqNum = sp.mSeqNum;

				if (Globals.IS_SLEEP_DETECTION_ENABLED) {
					int minute = sp.mActualTime.getMinutes() + sp.mActualTime.getHours() * 60;
					
					//DataStorage.SetValue(aContext, Globals.WOCKET_ACTIVITY_VALUE + minute, sp.mActivityCount);
					String temp = DataStorage.GetValueString(aContext, Globals.WOCKET_ACTIVITY_VALUE, "");
					DataStorage.SetValue(aContext, Globals.WOCKET_ACTIVITY_VALUE, temp + sp.mActivityCount + "_" + minute + ",");
					
					SleepAnnotation annotation = SleepDetection.detect(aContext, sp.mActualTime);
					if (annotation != null) {
						aAnnotationSaver.addAnnotation("Sazonov_0", (annotation.getLabel().equals(Globals.WAKE_LABEL) ? "111" : "000"), 
								annotation.getLabel(), annotation.getStartTime(), annotation.getEndTime(), "Case_Study", new Date(), new Date());
						/*String[] fileHeader = {"EPOCH_NUMBER", "START_TIME_UNIX", "START_TIME_DATE", "START_TIME_ZONE", 
						 	"END_TIME_UNIX", "END_TIME_DATE", "END_TIME_ZONE", "ACTIVITY_COUNT", "SLEEP_WAKE"};
						LowSamplingRateDataSaver labelSaver = new LowSamplingRateDataSaver(Globals.IS_SENSOR_DATA_EXTERNAL, "Sleep_Wake", "00", fileHeader);
						String[] label = {Integer.toString(sp.mDaySeqNum), Long.toString(annotation.getStartTime().getTime()), 
								Globals.mHealthTimestampFormat.format(annotation.getStartTime()), Integer.toString(Calendar.ZONE_OFFSET / Globals.HOURS1_MS),
								Long.toString(annotation.getStartTime().getTime()),
								Globals.mHealthTimestampFormat.format(annotation.getEndTime()), Integer.toString(Calendar.ZONE_OFFSET / Globals.HOURS1_MS),
								Integer.toString(sp.mActivityCount), annotation.getLabel()};
						labelSaver.saveData(label);*/
					}
				}
			}

		}
		ServerLogger.send(TAG, aContext);
		prefs.edit().putInt(labelForLastSeqNumOnSharedPref, lastSeqNum)
				.commit();

		if (Globals.IS_SLEEP_DETECTION_ENABLED) {
			aAnnotationSaver.commitToFile();
		}
	}

	public void saveSummaryPointToMemory(String wocketId, int summaryData,
			int sequenceNumber, Date timeStamp, boolean writeToExternalMemory) {

		String header[] = { "TIME_STAMP", "VALUE", "SEQUENCE_NUMBER" };
		LowSamplingRateDataSaver summaryDataSaver = new LowSamplingRateDataSaver(
				writeToExternalMemory, "Wocket", wocketId, header);

		String[] dataToWrite = new String[2];
		dataToWrite[0] = Integer.toString(summaryData);
		dataToWrite[1] = Integer.toString(sequenceNumber);
		summaryDataSaver.saveData(timeStamp, dataToWrite);

	}

	public void printSummaryPointTimes() {
		// Loop through data
		for (SummaryPoint sp : mSummaryPoints) {
			Log.i(TAG, "SUMMARY POINT STATUS. SN: " + sp.mSeqNum + "    AC:"
					+ sp.mActivityCount + "    Q: " + sp.mJsonQueued + "    T:"
					+ sp.mActualTime);
		}
	}

	private static final int UNDEFINED_AC = -1;

	public void removeSentData(Context aContext) {
		ArrayList<SummaryPoint> somePoints = new ArrayList<SummaryPoint>();
		for (SummaryPoint sp : mSummaryPoints) {
			if (sp.mActivityCount == UNDEFINED_AC) {
				// Leave out. Skipped an AC
				somePoints.add(sp);
			} else if ((sp.mActualTime != null) && (sp.mJsonQueued)) {
				if ((System.currentTimeMillis() - sp.mActualTime.getTime()) < Globals.MINUTES_30_IN_MS) {
					// Leave until 30 minutes gone by after sending
					somePoints.add(sp);
				} else {
					if (Globals.IS_DEBUG)
						Log.d(TAG,
								"Removing point info because sent and 30 minutes past: "
										+ sp.mSeqNum + " " + sp.mActivityCount
										+ " " + sp.mActualTime);

					// // Leave points for at least 10 minutes once assigned
					// time
					// if ((System.currentTimeMillis() -
					// sp.mActualTime.getTime()) < MINUTES_11_MS) //TODO
					// {
					// Log.e(TAG,"Queing point info and leaving: " + sp.mSeqNum
					// + " " + sp.mActivityCount + " " + sp.mActualTime);
					// somePoints.add(sp); //TODO
					// }
					// else
					// {
					// Log.e(TAG,"Queing point info and removing: " + sp.mSeqNum
					// + " " + sp.mActivityCount + " " + sp.mActualTime);
					// }

				}
			} else {
				// Log.e(TAG,"Leaving point info: " + sp.mSeqNum + " " +
				// sp.mActivityCount);
				somePoints.add(sp);
			}
		}
		mSummaryPoints = somePoints;
	}

	public void processSummaryPoints(Context aContext) {
		// Spread times back from the ACs that are properly timestamped
		setRealTimesSummaryPoints();

		// Remove the points that have already been sent
		removeSentData(aContext);

		printSummaryPointTimes();

		// Save data that can't be processed yet
		saveUnprocessedWSData(aContext);
	}

	// if( point.mActivityCount > Defines.WOCKET_STILLNESS_MIN &&
	// !point.mWritten)
	// {
	// DataStore.incrementActivityScore();
	// }

	// ///TODO - to save battery life (and SD card life), we should probably
	// only write new data
	// // to storage periodically instead of on every new point. This is a
	// future enhancement
	// if( DataStore.getContext()!= null)
	// {
	// Time now = new Time();
	// now.setToNow();
	// ///TODO instead of BT name, use name from sensor.xml
	// String dirName = "data/summary/" + now.format("%Y-%m-%d") + "/" +
	// now.format("%H") + "/";
	// String fileName = dirName + "SummaryAC_" + mName + ".csv";
	// File file = new File(DataStore.getContext().getExternalFilesDir(null),
	// fileName);
	//
	// try {
	// if( file != null)
	// {
	// boolean writeHeader = false;
	// if( !file.exists() )
	// {
	// writeHeader = true;
	// File directory = new
	// File(DataStore.getContext().getExternalFilesDir(null), dirName);
	// directory.mkdirs();
	// file.createNewFile();
	// }
	//
	// FileWriter out = new FileWriter(file, true);
	//
	// if( writeHeader)
	// {
	// out.write(HEADER_STRING + "\n");
	// }
	//
	// if( !point.mWritten )
	// {
	// out.write( point.mPhoneReadTime.format("%Y-%m-%d %H:%M:%S") + "," +
	// point.mSeqNum +","+ point.mSeqNum +"," +
	// //create time
	// point.mActualTime.format("%Y-%m-%d %H:%M:%S")+","+
	//
	// //create MS time
	// point.mActualTime.toMillis(false)+"," +
	// point.mActivityCount + "\n");
	// }
	//
	// out.close();
	// }
	// }
	// catch( Exception e)
	// {
	// e.printStackTrace();
	// }

	// //remove excess points from the array
	// while( mSummaryPoints.size() > Defines.MAX_WOCKET_SUMMARIES)
	// {
	// mSummaryPoints.remove(0);
	// }

	/*
	 * private SimpleDateFormat aDateFormat = new
	 * SimpleDateFormat("yyyy-MM-dd hh-mm-ss"); private String
	 * dateFormatFile(Date aDate) { StringBuilder stringDate = new
	 * StringBuilder( aDateFormat.format( aDate ) ); return
	 * stringDate.toString(); }
	 */

	/**
	 * Adds the given summary point to the storage array if it is not already
	 * known. Increments the day's activity score if the value of the summary
	 * point is above the defined threshold. Once the size of the storage array
	 * reaches a defined limit, excess points will be removed from the array and
	 * written to a file.
	 * 
	 * @param point
	 *            - the SummaryPoint to add
	 */
	// public void addReadSummaryPoint( SummaryPoint point)
	// {
	// //If we already have this point, just dump it
	// if( containsSummaryPoint(point))
	// {
	// return;
	// }
	//
	// mSummaryPoints.add(point);
	//
	// if( point.mActivityCount > Defines.WOCKET_STILLNESS_MIN &&
	// !point.mWritten)
	// {
	// DataStore.incrementActivityScore();
	// }
	//
	// ///TODO - to save battery life (and SD card life), we should probably
	// only write new data
	// // to storage periodically instead of on every new point. This is a
	// future enhancement
	// if( DataStore.getContext()!= null)
	// {
	// Time now = new Time();
	// now.setToNow();
	// ///TODO instead of BT name, use name from sensor.xml
	// String dirName = "data/summary/" + now.format("%Y-%m-%d") + "/" +
	// now.format("%H") + "/";
	// String fileName = dirName + "SummaryAC_" + mName + ".csv";
	// File file = new File(DataStore.getContext().getExternalFilesDir(null),
	// fileName);
	//
	// try {
	// if( file != null)
	// {
	// boolean writeHeader = false;
	// if( !file.exists() )
	// {
	// writeHeader = true;
	// File directory = new
	// File(DataStore.getContext().getExternalFilesDir(null), dirName);
	// directory.mkdirs();
	// file.createNewFile();
	// }
	//
	// FileWriter out = new FileWriter(file, true);
	//
	// if( writeHeader)
	// {
	// out.write(HEADER_STRING + "\n");
	// }
	//
	// if( !point.mWritten )
	// {
	// out.write( dateFormatFile(point.mPhoneReadTime) + "," +
	// point.mSeqNum +","+ point.mSeqNum +"," +
	// //create time
	// dateFormatFile(point.mActualTime)+","+
	// //create MS time
	// point.mActualTime.getTime()+"," +
	// point.mActivityCount + "\n");
	// }
	//
	// out.close();
	// }
	// }
	// catch( Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// //remove excess points from the array
	// while( mSummaryPoints.size() > Defines.MAX_WOCKET_SUMMARIES)
	// {
	// mSummaryPoints.remove(0);
	// }
	//
	// }
	// }

	/**
	 * Add a raw acceleration point to the array. Once a limit of points in the
	 * array is reached, older points will be written out to the log file and
	 * removed from the array
	 * 
	 * @param point
	 *            - the new raw point to add.
	 */
	public void addAccelPoint(AccelPoint point, StringBuffer content) {
		mAccelPoints.add(point);
		SimpleDateFormat sdf = new SimpleDateFormat("%H:%M:%S ");
		// Write out raw data here

		String s = sdf.format(mAccelPoints.get(0).mPhoneReadTime)
				+ mAccelPoints.get(0).mX + " " + mAccelPoints.get(0).mY + " "
				+ mAccelPoints.get(0).mZ + "\n";
		content.append(s);
	}

	public void writeAccelPointToExternalFile(StringBuffer content) {
		Time now = new Time();
		now.setToNow();
		String dirName = "data/raw/" + now.format("%Y-%m-%d") + "/"
				+ now.format("%H") + "/";
		String textFileName = dirName + mName + ".txt";
		try {
			long startCreatFile = System.currentTimeMillis();
			File textFile = new File(DataStore.getContext()
					.getExternalFilesDir(null), textFileName);
			if (textFile != null) {
				if (!textFile.exists()) {
					File directory = new File(DataStore.getContext()
							.getExternalFilesDir(null), dirName);
					directory.mkdirs();
					textFile.createNewFile();
				}
				long endCreateFile = System.currentTimeMillis();
				if ((endCreateFile - startCreatFile) > 10) {
					if (pm == null) {
						pm = (PowerManager) DataStore.getContext()
								.getSystemService(Context.POWER_SERVICE);
						wl = pm.newWakeLock(
								PowerManager.FULL_WAKE_LOCK,
								DataStore.getContext().getString(
										R.string.activity_monitor_app_name));
					}
					wl.acquire();
				}
				FileWriter out = new FileWriter(textFile, true);
				out.write(content.toString());
				out.close();
			}
		} catch (Exception e) {
			Log.i(TAG, "File error");
			e.printStackTrace();
		}
		if (wl != null) {
			wl.release();
			wl = null;
			pm = null;
		}
	}

	public void writeAccelPointToInternalFile(StringBuffer content) {
		Time now = new Time();
		now.setToNow();
		String dirName = "data/raw/" + now.format("%Y-%m-%d") + "/"
				+ now.format("%H") + "/";
		String textFileName = dirName + mName + ".txt";

		File fileDir = DataStore.getContext().getDir("dirName",
				Context.MODE_PRIVATE); // Creating an internal dir;
		File outputFile = new File(fileDir, textFileName);

		try {
			FileOutputStream fos = new FileOutputStream(outputFile, true);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(content.toString().getBytes());
			bos.flush();
			bos.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void copyfileExtToInt() {
		Time now = new Time();
		now.setToNow();
		String dirName = "data/raw/" + now.format("%Y-%m-%d") + "/"
				+ now.format("%H") + "/";
		String textFileName = dirName + mName + ".txt";
		try {
			File textFile = new File(DataStore.getContext()
					.getExternalFilesDir(null), textFileName);
			if (textFile != null) {
				BufferedReader in = new BufferedReader(new FileReader(textFile));
				StringBuffer sb = new StringBuffer();
				String inputLine;
				while ((inputLine = in.readLine()) != null)
					sb.append(inputLine);
				in.close();
				textFile.delete();
				writeAccelPointToInternalFile(sb);
			}
		} catch (Exception e) {
			Log.i(TAG, "File error");
			e.printStackTrace();
		}

	}

	// /**
	// * Add a raw acceleration point to the array. Once a limit of points in
	// the array
	// * is reached, older points will be written out to the log file and
	// removed from the array
	// *
	// * @param point - the new raw point to add.
	// */
	// public void addAccelPoint( AccelPoint point)
	// {
	// mAccelPoints.add(point);
	//
	// if( mAccelPoints.size() > Defines.MAX_WOCKET_POINTS)
	// {
	// if( DataStore.getContext()!= null)
	// {
	// Time now = new Time();
	// now.setToNow();
	// ///TODO instead of BT name, use some Wocket format "WocketAccelBytes..."
	// String dirName = "data/raw/PLFormat/" + now.format("%Y-%m-%d") + "/" +
	// now.format("%H") + "/";
	// String fileName = dirName + mName + ".PLFormat";
	// String textFileName = dirName + mName + ".txt";
	// File file = new File(DataStore.getContext().getExternalFilesDir(null),
	// fileName);
	// File textFile = new
	// File(DataStore.getContext().getExternalFilesDir(null), textFileName);
	// try {
	// if( file != null)
	// {
	// if( !file.exists() )
	// {
	// File directory = new
	// File(DataStore.getContext().getExternalFilesDir(null), dirName);
	// directory.mkdirs();
	// file.createNewFile();
	// textFile.createNewFile();
	// }
	//
	// FileWriter out = new FileWriter(file, true);
	// FileOutputStream binaryOut = new FileOutputStream(file, true );
	//
	// while( mAccelPoints.size() > 0)
	// {
	// //Write the x,y,z values as text to a text file to be human readable
	// String output = mAccelPoints.get(0).mPhoneReadTime.format("%H:%M:%S ") +
	// mAccelPoints.get(0).mX + " " + mAccelPoints.get(0).mY + " "
	// + mAccelPoints.get(0).mZ + "\n";
	// out.write( output);
	//
	//
	// //Write the raw bytes out to be readable by the Merger/Viewer graphing
	// tool
	// if (mAccelPoints.get(0).mPhoneReadTime.toMillis(false) < lastWrittenTime)
	// {
	// lastWrittenTime = mAccelPoints.get(0).mPhoneReadTime.toMillis(false);
	// //Log.("Accelerometer: Save: Data overwritten without saving Accelerometer.cs Save "
	// + this._ID + " " + aUnixTime + " " + lastUnixTime);
	// }
	//
	// //If the time difference between the current point and the last one
	// //written to file is close, just write the difference. Otherwise
	// // write out the full 7 byte time stamp (6 bytes data + 1 header)
	// if(mAccelPoints.get(0).mPhoneReadTime.toMillis(false) - lastWrittenTime>
	// 254)
	// {
	// int sec = (int)(mAccelPoints.get(0).mPhoneReadTime.toMillis(false)/1000);
	// short ms=
	// (short)(mAccelPoints.get(0).mPhoneReadTime.toMillis(false)%1000);
	//
	// //Write 0xFF for a full time stamp
	// binaryOut.write(0xFF);
	// //write out the time stamp (6 bytes of data)
	//
	// //Write 4 bytes for the second value
	// binaryOut.write(new byte[] {
	// (byte)(sec&0xFF),
	// (byte)((sec >>> 8)&0xFF),
	// (byte)((sec >>> 16)&0xFF),
	// (byte)((sec >>> 24)&0xFF) },0,4);
	//
	// //write 2 bytes for ms value
	// binaryOut.write(new byte[] {
	// (byte)(ms&0xFF),
	// (byte)((ms >>> 8)&0xFF)},0,2);
	// }
	// else
	// {
	// //Just write the difference in time stamps
	// binaryOut.write((int) (0xFF &
	// (mAccelPoints.get(0).mPhoneReadTime.toMillis(false) - lastWrittenTime)));
	// }
	//
	// //write the raw data
	// binaryOut.write( mAccelPoints.get(0).mRawData);
	//
	// mAccelPoints.remove(0);
	// }
	//
	// binaryOut.close();
	// out.close();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	/**
	 * Reset the default values of this sensor. Make sure NOT to reset the
	 * connection error counter!
	 */
	public void reset() {
		super.reset();
		mAccelPoints.clear();
		mInit = false;
		prevX = 0;
		prevY = 0;
		prevZ = 0;
	}

	// @Override
	// /**
	// * Parse the given data according to the Wocket packet definitions
	// * @param data - the raw data to parse
	// * @param size - the size of the raw data array
	// */
	// public void parsePacketOrig(byte[] data, int size)
	// {
	// mPacketsReceived = 0;
	// int compressedPackets = 0;
	// int fullPackets = 0;
	// int byteAdvance = 0;
	// for( int x=0;x<size;x++)
	// {
	// int Hbit = ((data[x] & 0x80) >> 7);
	// if( Hbit == 1)
	// {
	// int Tbit =((data[x] & 0x60) >> 5);
	// //Log.e(TAG, "T: " + Tbit);
	//
	// switch( Tbit)
	// {
	// case WOCKET_COMPRESSED:
	// {
	// if( x < Defines.MAX_WOCKET_PACKET_SIZE-2)
	// {
	// int xVal = (short)(((data[x] & 0x0f) << 1) | ((data[x+1] & 0x40) >> 6));
	// xVal = ((((short)((data[x] >> 4) & 0x01)) == 1) ? ((short)(prevX + xVal))
	// : ((short)(prevX - xVal)));
	// int yVal = (short)(data[x+1] & 0x1f);
	// yVal = ((((short)((data[x+1] >> 5) & 0x01)) == 1) ? ((short)(prevY +
	// yVal)) : ((short)(prevY - yVal)));
	// int zVal = (short)((data[x+2] >> 1) & 0x1f);
	// zVal = ((((short)((data[x+2] >> 6) & 0x01)) == 1) ? ((short)(prevZ +
	// zVal)) : ((short)(prevZ - zVal)));
	//
	// //Log.i( TAG, "Compressed " + xVal + " " + yVal + " " + zVal);
	//
	// byte[] raw = {data[x], data[x+1], data[x+2]};
	// addAccelPoint( new AccelPoint(xVal,yVal,zVal, true, raw));
	//
	// prevX = xVal;
	// prevY = yVal;
	// prevZ = zVal;
	// mPacketsReceived++;
	// compressedPackets++;
	// }
	// }
	// break;
	// case WOCKET_RESPONSE:
	// {
	// //Got a response
	// int opCode = (data[x]&0x1f);
	// // Log.i(TAG, "opcode: " + opCode);
	// switch( opCode)
	// {
	// case WOCKET_RESPONSE_BATTERY_LEVEL:
	// int batteryLevel = 0;
	// batteryLevel = ((data[x+1]&0x7f)<<3) | ((data[x+2]&0x70)>>4);
	// Log.i(TAG, "battery level: " + batteryLevel );
	// // int batteryCalc = (batteryLevel-Defines.WOCKET_LOW_BATTERY_LEVEL);
	// // if( batteryCalc > 0 && batteryCalc <= 100)
	// // {
	// // mBattery = batteryCalc;
	// // }
	// // else if( batteryCalc > 100)
	// // {
	// // mBattery = 100;
	// // }
	// // TODO
	// mBattery = batteryLevel;
	//
	// break;
	// case WOCKET_RESPONSE_BATTERY_PERCENT:
	// int battery = 0;
	// battery = (data[x+1]&0x7f);
	// Log.i(TAG, "battery: " + battery );
	// if( battery > 0 && battery <= 100)
	// {
	// mBattery = battery;
	// }
	// break;
	//
	// case WOCKET_RESPONSE_SEND_DATA_DONE:
	// // Ignore this here
	// break;
	//
	// // case WOCKET_RESPONSE_PACKET_COUNT:
	// // int packetCountResponse = ((data[x+1]&0x7f)<<9) |
	// ((data[x+2]&0x7f)<<2) |
	// // ((data[x+3]&0x60)>>5);
	// // Log.e("PacketCountResponse" + packetCountResponse);
	// // break;
	// // case WOCKET_RESPONSE_SLEEP_MODE:
	// // int sleep = ((data[x+1]&0x7f)<<9) | ((data[x+2]&0x7f)<<2) |
	// // ((data[x+3]&0x60)>>5);
	// // Log.i(TAG, "sleep : " + sleep);
	// // break;
	// // case WOCKET_RESPONSE_SUMMARY_COUNT:
	// // //read 6 bytes
	// // int seqNum = ((data[x+1] & 0x7f) << 9) | ((data[x+2] & 0x7f) << 2) |
	// ((data[x+3] >> 5) & 0x03);
	// // int count = ((data[x+3] & 0x1f) << 11) | ((data[x+4] & 0x7f)<<4) |
	// ((data[x+5]>>2)&0x0f);
	// //
	// // Log.i(TAG, "Got activity count: " + seqNum + " " + count);
	// //
	// // if (seqNum == (lastACOnWocket-1))
	// // addSummaryPoint( new SummaryPoint(seqNum, count, true)); // AC up to
	// date, so set time of last AC
	// // else
	// // addSummaryPoint( new SummaryPoint(seqNum, count, false)); // AC not up
	// to date. Figure out time later
	// // break;
	// // case WOCKET_RESPONSE_ACTIVITY_COUNT:
	// // //read 6 bytes
	// // int seqNum = ((data[x+1] & 0x7f) << 9) | ((data[x+2] & 0x7f) << 2) |
	// ((data[x+3] >> 5) & 0x03);
	// // int count = ((data[x+3] & 0x1f) << 11) | ((data[x+4] & 0x7f)<<4) |
	// ((data[x+5]>>2)&0x0f);
	// //
	// // Log.i(TAG, "Got activity count: " + seqNum + " " + count);
	// //
	// // if (seqNum == (lastACOnWocket-1))
	// // addSummaryPoint( new SummaryPoint(seqNum, count, true)); // AC up to
	// date, so set time of last AC
	// // else
	// // addSummaryPoint( new SummaryPoint(seqNum, count, false)); // AC not up
	// to date. Figure out time later
	// // break;
	//
	// case WOCKET_RESPONSE_SUMMARY_COUNT:
	// //read 6 bytes
	// int seqNum = ((data[x+1] & 0x7f) << 9) | ((data[x+2] & 0x7f) << 2) |
	// ((data[x+3] >> 5) & 0x03);
	// int count = ((data[x+3] & 0x1f) << 11) | ((data[x+4] & 0x7f)<<4) |
	// ((data[x+5]>>2)&0x0f);
	//
	// Log.i(TAG, "Activity count: " + seqNum + " " + count);
	//
	// if (seqNum == (lastACOnWocket-1))
	// addSummaryPoint( new SummaryPoint(seqNum, count, true)); // AC up to
	// date, so set time of last AC
	// else
	// addSummaryPoint( new SummaryPoint(seqNum, count, false)); // AC not up to
	// date. Figure out time later
	//
	// //if (seqNum > lastRecivedSN){
	// lastRecivedSN = seqNum;
	// //}
	//
	// break;
	// case WOCKET_RESPONSE_SAMPLING_RATE:
	// int sr = 0;
	// sr = (data[x+1]&0x7f);
	// Log.i(TAG, "WocketSensor parse: WOCKET_RESPONSE_SAMPLING_RATE: " + sr);
	// break;
	// // case WOCKET_RESPONSE_POWER_DOWN_TIMER:
	// // int pdt = 0;
	// // pdt = (data[x+1]&0x7f);
	// // Log.e(TAG, "WocketSensor parse: WOCKET_RESPONSE_POWER_DOWN_TIMER: " +
	// pdt);
	// // break;
	// case WOCKET_RESPONSE_RADIO_TRANSMISSION_MODE:
	// int rtm = 0;
	// rtm = (data[x+1]&0x7f);
	// Log.i(TAG, "WOCKET_RESPONSE_RADIO_TRANSMISSION_MODE: " + rtm);
	// break;
	// case WOCKET_RESPONSE_FIRMWARE_VERSION:
	// int fv = 0;
	// fv = (data[x+1]&0x7f << 7) | (data[x+2]&0x7f);
	// Log.i(TAG, "WOCKET_RESPONSE_FIRWARE_VERSION: " + fv);
	// break;
	// case WOCKET_RESPONSE_BATCH_COUNT:
	// int bc = 0;
	// bc = ((data[x+1] & 0x7f) << 9) | ((data[x+2] & 0x7f) << 2) | ((data[x+3]
	// >> 5) & 0x03);
	// Log.i(TAG, "WocketSensor parse: WOCKET_RESPONSE_BATCH_COUNT: " + bc);
	// break;
	// case WOCKET_RESPONSE_ACTIVITY_COUNT_COUNT:
	// lastACOnWocket = ((data[x+1]&0x7f)<<7) | (data[x+2]&0x7f);
	// Log.i(TAG, "WocketSensor parse: WOCKET_RESPONSE_ACTIVITY_COUNT_COUNT: " +
	// lastACOnWocket);
	// break;
	// case WOCKET_RESPONSE_OFFSET_AC_COUNT:
	// int oacr = 0;
	// oacr = ((data[x+1]&0x7f)<<7) | (data[x+2]&0x7f);
	// Log.i(TAG, "WocketSensor parse: WOCKET_RESPONSE_OFFSET_AC_COUNT: " +
	// oacr);
	// break;
	//
	// default:
	// Log.e(TAG, "Did not handle opcode: " + opCode);
	// break;
	// }
	// }
	// break;
	// case WOCKET_UNCOMPRESSED:
	// {
	// if( x < Defines.MAX_WOCKET_PACKET_SIZE-4)
	// {
	// int xVal = 0, yVal = 0, zVal = 0;
	// xVal = ((data[x] & 0x3)<<8) | ((data[x+1]&0x7F)<<1) | ((data[x+2]&0x40)
	// >>6);
	//
	// yVal = ((data[x+2] & 0x3f)<<4) | ((data[x+3]&0x78)>>3);
	//
	// zVal = ((data[x+3] & 0x7)<<7) | (data[x+4]&0x7F);
	// //Log.e(TAG,xVal + " " + yVal + " " + zVal);
	//
	// byte[] raw = {data[x], data[x+1], data[x+2], data[x+3], data[x+4]};
	//
	// addAccelPoint( new AccelPoint( xVal, yVal, zVal, false, raw));
	//
	// prevX = xVal;
	// prevY = yVal;
	// prevZ = zVal;
	// mPacketsReceived++;
	// fullPackets++;
	// }
	// }
	// break;
	// }
	// }
	// }
	// if (Globals.IS_DEBUG)
	// {
	// Log.i(TAG,"UNCOMPRESSED " + fullPackets + " COMPRESSED: " +
	// compressedPackets );
	// Log.i(TAG,"RAW ACCEL POINT PACKETS " + mPacketsReceived);
	// }
	// }
	PowerManager pm = null;
	PowerManager.WakeLock wl = null;

	@Override
	/**
	 * Parse the given data according to the Wocket packet definitions
	 * @param data - the raw data to parse
	 * @param size - the size of the raw data array
	 */
	public void parsePacket(byte[] data, int size) {
		// StringBuffer content = new StringBuffer();
		// long startRunTime = System.currentTimeMillis();
		// int switcher = 0;
		mPacketsReceived = 0;
		int compressedPackets = 0;
		int fullPackets = 0;
		int x = 0;
		int Hbit = 0;
		int byteAdvance = 0;
		// StringBuffer cinfo = new StringBuffer("START \n");
		Log.d(TAG, "LENGTH of byte array: " + data.length);
		Log.d(TAG, "SIZE of byte array: " + size);
		long startTime = System.currentTimeMillis();
		if (Globals.IS_DEBUG)
			Log.d(TAG, "Start parse Wockets packet");

		// Start parsing data packets
		while (x < size) {
			byteAdvance = 1; // Default unless other data found/processed
			Hbit = ((data[x] & 0x80) >> 7);
			// cinfo.append("X: " + x + "of " + size + ". Hbit: " + Hbit +
			// " Time: " + (System.currentTimeMillis()-startTime) + "\n");
			if (Hbit == 1) {
				int Tbit = ((data[x] & 0x60) >> 5);
				// cinfo.append("Tbit: " + Tbit + " Time: " +
				// (System.currentTimeMillis()-startTime) + "\n");

				switch (Tbit) {
				case WOCKET_COMPRESSED:
					if (x < Defines.MAX_WOCKET_PACKET_SIZE - 2) {
						// if ((x % 100) == 0)
						// Log.d(TAG, "C("+ x + ") ");
						int xVal = (short) (((data[x] & 0x0f) << 1) | ((data[x + 1] & 0x40) >> 6));
						xVal = ((((short) ((data[x] >> 4) & 0x01)) == 1) ? ((short) (prevX + xVal))
								: ((short) (prevX - xVal)));
						int yVal = (short) (data[x + 1] & 0x1f);
						yVal = ((((short) ((data[x + 1] >> 5) & 0x01)) == 1) ? ((short) (prevY + yVal))
								: ((short) (prevY - yVal)));
						int zVal = (short) ((data[x + 2] >> 1) & 0x1f);
						zVal = ((((short) ((data[x + 2] >> 6) & 0x01)) == 1) ? ((short) (prevZ + zVal))
								: ((short) (prevZ - zVal)));
						// Log.d( TAG, "Compressed " + xVal + " " + yVal + " " +
						// zVal);

						byte[] raw = { data[x], data[x + 1], data[x + 2] };
						mAccelPoints.add(new AccelPoint(xVal, yVal, zVal, true,
								mLastConnectionTime.getTime(), raw));

						// addAccelPoint( new AccelPoint(xVal,yVal,zVal, true,
						// raw), content);
						// if((x % 200) == 0){
						// long currentTime = System.currentTimeMillis();
						// if(switcher == 0 && (currentTime - startRunTime) >
						// 20*60*1000){
						// switcher = 1;
						// copyfileExtToInt();
						// }
						// switch(switcher){
						// case 0: writeAccelPointToExternalFile(content);break;
						// case 1: writeAccelPointToInternalFile(content);break;
						// }
						// content = new StringBuffer();
						// }
						prevX = xVal;
						prevY = yVal;
						prevZ = zVal;
						mPacketsReceived++;
						compressedPackets++;
					} else {
						Log.d(TAG, "OOPS! "
								+ (System.currentTimeMillis() - startTime));
					}
					byteAdvance = 3;
					break;
				case WOCKET_UNCOMPRESSED:
					if (x < Defines.MAX_WOCKET_PACKET_SIZE - 4) {
						// if ((x % 100) == 0)
						// Log.d(TAG, "U("+ x + ") ");
						int xVal = 0, yVal = 0, zVal = 0;
						xVal = (short) ((((int) ((int) data[x] & 0x03)) << 8)
								| (((int) ((int) data[x + 1] & 0x7f)) << 1) | (((int) ((int) data[x + 2] & 0x40)) >> 6));
						yVal = (short) ((((int) ((int) data[x + 2] & 0x3f)) << 4) | (((int) ((int) data[x + 3] & 0x78)) >> 3));
						zVal = (short) ((((int) ((int) data[x + 3] & 0x07)) << 7) | ((int) ((int) data[x + 4] & 0x7f)));
						// Log.e(TAG,xVal + " " + yVal + " " + zVal);

						byte[] raw = { data[x], data[x + 1], data[x + 2],
								data[x + 3], data[x + 4] };
						mAccelPoints.add(new AccelPoint(xVal, yVal, zVal, true,
								mLastConnectionTime.getTime(), raw));
						// addAccelPoint( new AccelPoint( xVal, yVal, zVal,
						// false, raw),content);
						// if((x % 200) == 0){
						// long currentTime = System.currentTimeMillis();
						// if(switcher == 0 && (currentTime - startRunTime) >
						// 20*60*1000){
						// switcher = 1;
						// copyfileExtToInt();
						// }
						// switch(switcher){
						// case 0: writeAccelPointToExternalFile(content);break;
						// case 1: writeAccelPointToInternalFile(content);break;
						// }
						// content = new StringBuffer();
						// }

						prevX = xVal;
						prevY = yVal;
						prevZ = zVal;
						mPacketsReceived++;
						fullPackets++;
					} else {
						Log.d(TAG, "OOPS! "
								+ (System.currentTimeMillis() - startTime));
					}
					byteAdvance = 5;
					break;
				case WOCKET_RESPONSE:
					int opCode = (data[x] & 0x1f);
					Log.d(TAG, "Opcode: " + opCode);
					Log.d(TAG, "R " + (System.currentTimeMillis() - startTime));
					switch (opCode) {
					case WOCKET_RESPONSE_BATTERY_LEVEL:
						int batteryLevel = 0;
						batteryLevel = ((data[x + 1] & 0x7f) << 3)
								| ((data[x + 2] & 0x70) >> 4);
						Log.d(TAG, "battery level: " + batteryLevel);
						// int batteryCalc =
						// (batteryLevel-Defines.WOCKET_LOW_BATTERY_LEVEL);
						// if( batteryCalc > 0 && batteryCalc <= 100)
						// {
						// mBattery = batteryCalc;
						// }
						// else if( batteryCalc > 100)
						// {
						// mBattery = 100;
						// }
						// TODO
						mBattery = batteryLevel;
						byteAdvance = 3;
						break;
					case WOCKET_RESPONSE_BATTERY_PERCENT:
						int battery = 0;
						battery = (data[x + 1] & 0x7f);
						Log.d(TAG, "battery: " + battery);
						if (battery > 0 && battery <= 100) {
							mBattery = battery;
						}
						byteAdvance = 2;
						break;
					case WOCKET_RESPONSE_SEND_DATA_DONE:
						// Ignore this here
						byteAdvance = 1;
						Log.d(TAG, "Wocket response send data done. X = " + x
								+ " Size = " + size + " Byte length = "
								+ data.length);
						break;

					// case WOCKET_RESPONSE_PACKET_COUNT:
					// int packetCountResponse = ((data[x+1]&0x7f)<<9) |
					// ((data[x+2]&0x7f)<<2) |
					// ((data[x+3]&0x60)>>5);
					// Log.e("PacketCountResponse" + packetCountResponse);
					// break;
					// case WOCKET_RESPONSE_SLEEP_MODE:
					// int sleep = ((data[x+1]&0x7f)<<9) | ((data[x+2]&0x7f)<<2)
					// |
					// ((data[x+3]&0x60)>>5);
					// Log.d(TAG, "sleep : " + sleep);
					// break;
					// case WOCKET_RESPONSE_SUMMARY_COUNT:
					// //read 6 bytes
					// int seqNum = ((data[x+1] & 0x7f) << 9) | ((data[x+2] &
					// 0x7f) << 2) | ((data[x+3] >> 5) & 0x03);
					// int count = ((data[x+3] & 0x1f) << 11) | ((data[x+4] &
					// 0x7f)<<4) | ((data[x+5]>>2)&0x0f);
					//
					// Log.d(TAG, "Got activity count: " + seqNum + " " +
					// count);
					//
					// if (seqNum == (lastACOnWocket-1))
					// addSummaryPoint( new SummaryPoint(seqNum, count, true));
					// // AC up to date, so set time of last AC
					// else
					// addSummaryPoint( new SummaryPoint(seqNum, count, false));
					// // AC not up to date. Figure out time later
					// break;
					// case WOCKET_RESPONSE_ACTIVITY_COUNT:
					// //read 6 bytes
					// int seqNum = ((data[x+1] & 0x7f) << 9) | ((data[x+2] &
					// 0x7f) << 2) | ((data[x+3] >> 5) & 0x03);
					// int count = ((data[x+3] & 0x1f) << 11) | ((data[x+4] &
					// 0x7f)<<4) | ((data[x+5]>>2)&0x0f);
					//
					// Log.d(TAG, "Got activity count: " + seqNum + " " +
					// count);
					//
					// if (seqNum == (lastACOnWocket-1))
					// addSummaryPoint( new SummaryPoint(seqNum, count, true));
					// // AC up to date, so set time of last AC
					// else
					// addSummaryPoint( new SummaryPoint(seqNum, count, false));
					// // AC not up to date. Figure out time later
					// break;

					case WOCKET_RESPONSE_SUMMARY_COUNT:
						// read 6 bytes
						int seqNum = ((data[x + 1] & 0x7f) << 9)
								| ((data[x + 2] & 0x7f) << 2)
								| ((data[x + 3] >> 5) & 0x03);
						int count = ((data[x + 3] & 0x1f) << 11)
								| ((data[x + 4] & 0x7f) << 4)
								| ((data[x + 5] >> 2) & 0x0f);

						Log.d(TAG, "Activity count: " + seqNum + " " + count);

						if (seqNum == (lastACOnWocket - 1))
							addSummaryPoint(new SummaryPoint(seqNum, count,
									true)); // AC up to date, so set time of
											// last AC
						else
							addSummaryPoint(new SummaryPoint(seqNum, count,
									false)); // AC not up to date. Figure out
												// time later

						// if (seqNum > lastRecivedSN){
						lastRecivedSN = seqNum;
						// }
						byteAdvance = 6;
						break;
					case WOCKET_RESPONSE_SAMPLING_RATE:
						int sr = 0;
						sr = (data[x + 1] & 0x7f);
						Log.d(TAG,
								"WocketSensor parse: WOCKET_RESPONSE_SAMPLING_RATE: "
										+ sr);
						byteAdvance = 2;
						break;
					// case WOCKET_RESPONSE_POWER_DOWN_TIMER:
					// int pdt = 0;
					// pdt = (data[x+1]&0x7f);
					// Log.e(TAG,
					// "WocketSensor parse: WOCKET_RESPONSE_POWER_DOWN_TIMER: "
					// + pdt);
					// break;
					case WOCKET_RESPONSE_RADIO_TRANSMISSION_MODE:
						int rtm = 0;
						rtm = (data[x + 1] & 0x7f);
						Log.d(TAG, "WOCKET_RESPONSE_RADIO_TRANSMISSION_MODE: "
								+ rtm);
						byteAdvance = 2;
						break;
					case WOCKET_RESPONSE_FIRMWARE_VERSION:
						int fv = 0;
						fv = (data[x + 1] & 0x7f << 7) | (data[x + 2] & 0x7f);
						Log.d(TAG, "WOCKET_RESPONSE_FIRWARE_VERSION: " + fv);
						byteAdvance = 3;
						break;
					case WOCKET_RESPONSE_BATCH_COUNT:
						int bc = 0;
						bc = ((data[x + 1] & 0x7f) << 9)
								| ((data[x + 2] & 0x7f) << 2)
								| ((data[x + 3] >> 5) & 0x03);
						Log.d(TAG,
								"WocketSensor parse: WOCKET_RESPONSE_BATCH_COUNT: "
										+ bc);
						batch_count = bc;
						byteAdvance = 4;
						break;
					case WOCKET_RESPONSE_ACTIVITY_COUNT_COUNT:
						lastACOnWocket = ((data[x + 1] & 0x7f) << 7)
								| (data[x + 2] & 0x7f);
						Log.d(TAG,
								"WocketSensor parse: WOCKET_RESPONSE_ACTIVITY_COUNT_COUNT: "
										+ lastACOnWocket);
						byteAdvance = 3;
						break;
					case WOCKET_RESPONSE_OFFSET_AC_COUNT:
						int oacr = 0;
						oacr = ((data[x + 1] & 0x7f) << 7)
								| (data[x + 2] & 0x7f);
						Log.d(TAG,
								"WocketSensor parse: WOCKET_RESPONSE_OFFSET_AC_COUNT: "
										+ oacr);
						byteAdvance = 3;
						break;
					default:
						Log.d(TAG, "Did not handle opcode: " + opCode);
						// byteAdvance = 2;
						break;
					} // End WOCKET_RESPONSE OpCode parsing switch
					Log.d(TAG, "After break: "
							+ (System.currentTimeMillis() - startTime));
					break;
				default:
					Log.d(TAG, "UNKNOWN TBIT case");
					break;
				} // End TBit switch

			} // HBit == 1
			else {
				Log.d(TAG, "HBIT not 1");
			}

			// Always move forward one
			if (byteAdvance < 2) {
				Log.d(TAG, "Skip just one byte");
			}

			x += byteAdvance;

		}
		// Stop parsing information

		// Check if there is any missing data
		checkIfMissingData();

		Log.d(TAG, "Done");
		// DataEncoder encoder = new DataEncoder();
		// encoder.encodeAndSaveData(Calendar.getInstance(), mAccelPoints,
		// mAddress, aContext);
		int startIndex = 0;
		Calendar timeStampToSend = Calendar.getInstance();

		if (lastWritenAccelPointPhoneReadTime == null) {
			if (mAccelPoints.size() > 1) {
				lastWritenAccelPointPhoneReadTime = Calendar.getInstance();
				lastWritenAccelPointPhoneReadTime
						.setTimeInMillis(mLastConnectionTime.getTime());

				DataEncoder encoder = new DataEncoder();

				encoder.setTIME_DIFF(FAKE_TIME_DIFF);
				// Log.d(TAG,
				// "First packet so resorting to default (1/frequency)");
				Log.d("temp",
						"First packet so resorting to default (1/frequency)");

				timeStampToSend.setTimeInMillis(mLastConnectionTime.getTime());
				encoder.encodeAndSaveData(timeStampToSend, mAccelPoints,
						mAddress, aContext);
				addWocketRecordTime(timeStampToSend, FAKE_TIME_DIFF);

			}

		} else {
			/*
			 * compare the current lastWritenAccelPointPhoneReadTime time, and
			 * the last time written last time written is the value of
			 * lastWritenAccelPointPhoneReadTime
			 */

			for (int i = mAccelPoints.size() - 1; i >= 0; i--) {

				if (mAccelPoints.get(i).mPhoneReadTime.getTimeInMillis() >= lastWritenAccelPointPhoneReadTime
						.getTimeInMillis()) {
					startIndex = i + 1;

				}
			}

			ArrayList<AccelPoint> unwrittenAccelPointsList = new ArrayList<AccelPoint>();
			unwrittenAccelPointsList = new ArrayList<AccelPoint>(
					mAccelPoints.subList(startIndex, mAccelPoints.size() - 1));

			double timePeriodBetweenSamples = 25d;
			long timeDiff = mLastConnectionTime.getTime()
					- lastWritenAccelPointPhoneReadTime.getTimeInMillis();

			Log.i("temp", "time diff " + timeDiff);

			if (timeDiff <= MAX_WOCKET_STORAGE_TIME) {
				Log.i("temp", "Entered if");
				timePeriodBetweenSamples = timeDiff / (double) batch_count;
			}

			Log.d("temp", "Unfiltered 1/frequency is "
					+ timePeriodBetweenSamples);

			// long timePeriodBetweenSamplesForUnwrittenAccelPoints =
			// ((mLastConnectionTime
			// .getTime() - lastWritenAccelPointPhoneReadTime
			// .getTimeInMillis()) / unwrittenAccelPointsList.size());
			// long timePeriodBetweenSamples = ((mLastConnectionTime.getTime() -
			// lastWritenAccelPointPhoneReadTime
			// .getTimeInMillis()) / mAccelPoints.size());
			if (timePeriodBetweenSamples < 22 || timePeriodBetweenSamples > 28) { // if
				// the
				// time
				// difference
				// between
				// two
				// samples
				// is
				// 1
				// second
				timePeriodBetweenSamples = 25;

				Log.d(TAG, "resorting to default (1/frequency");
			}
			Log.d("temp", "Filtered 1/frequency is " + timePeriodBetweenSamples);

			DataEncoder encoder = new DataEncoder();
			encoder.setTIME_DIFF(timePeriodBetweenSamples);
			timeStampToSend.setTimeInMillis(mLastConnectionTime.getTime());

			encoder.encodeAndSaveData(timeStampToSend,
					unwrittenAccelPointsList, mAddress, aContext);
			addWocketRecordTime(timeStampToSend, timePeriodBetweenSamples);

			// SimpleDateFormat sdf = new SimpleDateFormat(
			// "yyyy-MM-dd HH:mm:ss.SSS");
			//
			// Log.i("debug", "Working on " + mAddress + " | " +
			// sdf.format(lastWritenAccelPointPhoneReadTime .getTimeInMillis())
			// + " | " + sdf.format(mLastConnectionTime) + " | " +
			// encoder.FAKE_TIME_DIFF);
			//

			lastWritenAccelPointPhoneReadTime
					.setTimeInMillis(mLastConnectionTime.getTime());
		}

		if (Globals.IS_DEBUG) {
			Log.d(TAG, "UNCOMPRESSED " + fullPackets + " COMPRESSED: "
					+ compressedPackets);
			Log.d(TAG, "RAW ACCEL POINT PACKETS " + mPacketsReceived);
		}
	}

	private void checkIfMissingData() {
		MISSING_PACKETS = batch_count - mPacketsReceived;
	}

	private void addWocketRecordTime(Calendar timeStampToSend,
			double timePeriodBetweenSamples) {
		// find time of recording for first wocket data point
		Log.i("DetectionHandler",
			"================================= Adding wocket recording for "
						+ mAddress + "============================");
		Calendar recordBegTime = getRecordingBegTime(timeStampToSend,
				Math.round(timePeriodBetweenSamples));
		long recordedTime = recordBegTime.getTimeInMillis();
		int sampleNumber = 1;
		for (AccelPoint accelPoint : mAccelPoints) {
			long timeDiff = Math.round(sampleNumber * timePeriodBetweenSamples);
			accelPoint.mWocketRecordedTime = Calendar.getInstance();
			accelPoint.mWocketRecordedTime.setTimeInMillis(recordedTime
					+ timeDiff);
			sampleNumber++;
		}
	}

	private Calendar getRecordingBegTime(Calendar timeStampToSend,
			long timePeriodBetweenSamples) {
		long timeInMill = timeStampToSend.getTimeInMillis()
				- (timePeriodBetweenSamples * mAccelPoints.size());
		Calendar firstDataPointRecordTime = Calendar.getInstance();
		firstDataPointRecordTime.setTimeInMillis(timeInMill);
		return firstDataPointRecordTime;
	}

	private Calendar lastWritenAccelPointPhoneReadTime = null;

	private static final Date REASONABLE_DATE = new Date(2011 - 1900, 0, 1);

	public String getDetailedInfo() {
		String infoContent = mName + ":\n";
		if (mLastConnectionTime == null
				|| mLastConnectionTime.before(REASONABLE_DATE)) {
			infoContent += "Last time connected:\nUnknown.";
			return infoContent;
		} else {
			SimpleDateFormat detailedTime = new SimpleDateFormat(
					"MM/dd/yyyy hh:mm:ss a");
			infoContent += "Last time connected:\n"
					+ detailedTime.format(mLastConnectionTime) + ".\n"
					+ "Battery was at " + (int) (getBatteryPercentage() * 100)
					+ "%.\n" + "Bytes received was " + mBytesReceived + ".\n"
					+ "Raw samples received was " + mPacketsReceived + ".";
			return infoContent;
		}
	}

	public WocketBurstModeData getWocketDataPacket() {
		WocketBurstModeData wocketData = new WocketBurstModeData(mAddress,
				mAccelPoints, MISSING_PACKETS, mLastConnectionTime);
		return wocketData;
	}

	public String[] getHeader() {
		String[] header = { "TIME_STAMP", "VALUE", "SEQUENCE_NUMBER" };
		return header;
	}

	// LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(true,
	// Globals.SENSOR_TYPE_WOCKET, sensorID, getHeader());

}