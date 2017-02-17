package edu.neu.android.wocketslib.sensormonitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.Vibrator;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.activities.datasummaryviewer.WocketsDataSaver;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Note;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class ZephyrReader {
	private final static String TAG = "ZephyrReader";
	private boolean NO_PLOT = false;
	private WocketInfo wi;
	private Thread readerThread;

	// TODO Do we want this to be static?
	private static BluetoothAdapter blueToothAdapter;
	private WocketsDataSaver dataSaver;
	private ZephyrSensor zephyrSensor = null;
	private boolean isRunning = false;
	private ArrayList<ZephyrData> zephyrDatas;

	public ZephyrReader(Context c, WocketInfo wi) {
		super();
		this.wi = wi;
		this.readerThread = new Thread(readerRun);
		blueToothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.dataSaver = new WocketsDataSaver(c);
		this.zephyrDatas = new ArrayList<ZephyrData>();
	}

	public void startThread() {
		Log.d(TAG, "ZephyrReader thread starts.");
		isRunning = true;
		readerThread.start();
	}

	public void stopThread() {
		Log.d(TAG, "ZephyrReader thread stops.");
		isRunning = false;

		if (readerThread != null) {
			Thread moribund = readerThread;
			readerThread = null;
			moribund.interrupt();
		}
		if (zephyrDatas.size() > 0) {
			saveHRDataIntoFile(zephyrDatas);
			dataSaver.setHRData(zephyrSensor);
			dataSaver.commitHRData();
		}
	}

	private Runnable readerRun = new Runnable() {

		@Override
		public void run() {
			if (blueToothAdapter != null) {
				blueToothAdapter.enable();
			}
			resetZephyr();

			Set<BluetoothDevice> devices = blueToothAdapter.getBondedDevices();
			Iterator<BluetoothDevice> itr = devices.iterator();
			while (itr.hasNext()) {
				BluetoothDevice dev = itr.next();

				for (int x = 0; x < DataStore.mSensors.size(); x++) {
					if (dev.getName().equals(DataStore.mSensors.get(x).mName) && DataStore.mSensors.get(x).mEnabled
							&& (DataStore.mSensors.get(x).mType == Sensor.ZEPHYR)) {
						zephyrSensor = (ZephyrSensor) DataStore.mSensors.get(x);

						if (zephyrSensor != null) {
							// We've found a device we want to read from.
							Log.i(TAG, "Attempt read from enabled Zephyr sensor: " + dev.getName() + " " + dev.getAddress());
							addNote("Start connect to : " + dev.getAddress(), NO_PLOT);
							cancelDiscovery();
							BluetoothSocket sock = getConnectedBTSocket(dev, zephyrSensor);

							while (isRunning) {
								ZephyrData zephyrData = new ZephyrData();
								readDataEnabledDevice(sock, zephyrSensor, zephyrData);
								if (Globals.IS_DEBUG) {
									Log.d(TAG, "CHECKING ZEPHYR --------------------------------------------------------: " + zephyrSensor.mAddress);
									Log.i(TAG, "Checking Zephyr");
								}
								addZephyrData(zephyrSensor, zephyrData);
								zephyrDatas.add(zephyrData);
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									Log.e(TAG, "Error InterruptedException in ZephyrReader: " + e.toString());
									break;
								} finally {
									if (sock != null) {
										try {
											sock.close();
										} catch (IOException ioe) {
											ioe.printStackTrace();
											Log.e(TAG, "Error: Could not close BT socket: " + zephyrSensor.mAddress + " " + ioe.toString());
										}
									}
								}
							}
						}
					} // End valid sensor that is enabled
				} // End iteration through all BT sensors paired
			}
		}
	};

	private void resetZephyr() {
		for (int x = 0; x < DataStore.mSensors.size(); x++) {
			if (DataStore.mSensors.get(x).mName.contains(Defines.ZEPHYR_DEVICE_NAME))
				DataStore.mSensors.get(x).reset();
		}
	}

	private void addZephyrData(ZephyrSensor zephyrSensor, ZephyrData zephyrData) {
		if (wi.someHRData == null)
			wi.someHRData = new ArrayList<HRData>();
		ZephyrSensor zephyr = zephyrSensor;
		for (HRPoint aHRPoint : zephyr.mHRPoints) {
			if (!aHRPoint.mJsonQueued) {
				Log.i(TAG, "SENDING a HR to JSON: " + aHRPoint.mRate + " Num: " + aHRPoint.mHeartBeatNumber);
				HRData aHRData = new HRData();
				aHRData.battery = aHRPoint.mBatteryPercent;
				aHRData.createTime = aHRPoint.mPhoneReadTime;
				aHRData.heartBeatNumber = aHRPoint.mHeartBeatNumber;
				aHRData.heartRate = aHRPoint.mRate;
				aHRPoint.mJsonQueued = true;
				wi.someHRData.add(aHRData);
				zephyrData.addHRData(aHRData);
			}
		}
	}

	private void readDataEnabledDevice(BluetoothSocket sock, Sensor aSensor, ZephyrData zephyrData) {
		if (sock == null) {
			aSensor.mConnectionErrors++;
		} else {
			boolean isReadData = false;

			// Have a connected Socket to a BT device so read data accordingly
			InputStream in = null;
			try {
				in = sock.getInputStream();
			} catch (IOException e) {
				Log.e(TAG, "Connection error: Could not get input stream from BT socket: " + aSensor.mAddress);
				e.printStackTrace();
				startVibrationAlert(ERROR5);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Log.e(TAG, "Connection error: Could not close inputstream: " + aSensor.mAddress + " " + e1.toString());
					}
					in = null;
				}
			}

			if (in != null) {

				isReadData = readDataZephyr(in, aSensor, zephyrData);

				if (isReadData) {
					aSensor.mLastConnectionTime = new Date();
					aSensor.mConnectionErrors = 0;
				} else {
					aSensor.mConnectionErrors++;
					Log.e(TAG, "Increment connection errors: " + aSensor.mAddress + " Errors: " + aSensor.mConnectionErrors);
				}

				// if (in != null)
				// {
				// try
				// {
				// in.close();
				// } catch (IOException e)
				// {
				// e.printStackTrace();
				// Log.e(TAG, "Error: Could not close inputstream: " +
				// aSensor.mAddress + " " + e.toString());
				// }
				// }
				//
				// if (out != null)
				// {
				// try
				// {
				// out.close();
				// } catch (IOException e)
				// {
				// e.printStackTrace();
				// Log.e(TAG, "Error: Could not close outputstream: " +
				// aSensor.mAddress + " " + e.toString());
				// }
				// }

			}
		}
	}

	private boolean readDataZephyr(InputStream in, Sensor aSensor, ZephyrData zephyrData) {
		// Vibrate the phone for debugging purposes
		startVibrationAlert(ZEPHYR);
		zephyrData.resetDetailedData();
		byte[] data = new byte[Defines.MAX_ZEPHYR_PACKET_SIZE];

		int count;
		try {
			count = in.read(data, 0, Defines.MAX_ZEPHYR_PACKET_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		while (count < Defines.MAX_ZEPHYR_PACKET_SIZE) {
			try {
				count += in.read(data, count, Defines.MAX_ZEPHYR_PACKET_SIZE - count);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		aSensor.parsePacket(data, count);
		zephyrData.parseDetailedZephyrData(data);
		return true;
	}

	private void addNote(String aMsg, boolean isPlot) {
		if (wi.someNotes == null)
			wi.someNotes = new ArrayList<Note>();

		Note aNote = new Note();
		aNote.startTime = new Date();
		aNote.note = aMsg;
		if (isPlot)
			aNote.plot = 1;
		wi.someNotes.add(aNote);
	}

	private void cancelDiscovery() {
		if (blueToothAdapter.isDiscovering()) {
			Log.e(TAG, "Cancelled a Bluetooth discovery operation before data read operation.");
			blueToothAdapter.cancelDiscovery();
		}
	}

	private BluetoothSocket getConnectedBTSocket(BluetoothDevice dev, Sensor aSensor) {
		BluetoothSocket sock = null;
		try {
			Method m = dev.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
			sock = (BluetoothSocket) m.invoke(dev, 1);
			if (Globals.IS_DEBUG)
				Log.d(TAG, "Created insecure socket");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error creating insecure socket");
		}

		// If we couldn't create an insecure socket, just try a regular one
		if (sock == null) {
			try {
				Log.e(TAG, "Could not create insecure socket so trying to create regular one.");
				// This uses the "well known" SPP UUID to connect to
				sock = dev.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				if (sock == null)
					Log.e(TAG, "Error creating RFcommSocketToServiceRecord");
				else if (Globals.IS_DEBUG)
					Log.d(TAG, "Creating RFcommSocketToServiceRecord");
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Error creating regular socket");
			}
		}

		// Retry for up to 10 seconds, waiting until the Bluetooth adapter is
		// actually on and ready to go
		int retryCount = 0;
		while (blueToothAdapter.getState() != BluetoothAdapter.STATE_ON && retryCount < 100) {
			synchronized (mBinder) {
				try {
					mBinder.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			retryCount++;
		}

		// Hold off 30ms just to give time for everything to be ready.
		// This delay might be able to be removed to speed things up a tiny bit
		// and increase battery life
		synchronized (mBinder) {
			try {
				mBinder.wait(30);
			} catch (InterruptedException e) {
				Log.e(TAG, "Error InterruptedException in ZephyrReader: " + e.toString());
				e.printStackTrace();
			}
		}
		try {
			sock.connect();
		} catch (Exception e) {
			startVibrationAlert(ERROR1);
			Date d = new Date();
			Log.e(TAG, "Error when connecting to socket: " + aSensor.mAddress + " " + e.toString());
			addNote("Error when connecting to socket: " + aSensor.mAddress + " " + e.toString() + " Time: " + d, NO_PLOT);

			try {
				sock.close();
			} catch (IOException e1) {
			}

			return null;
		}

		return sock;
	}

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};
	private static Vibrator vb = null;
	private static final long[] vibrateToneAlive = { 30, 60, 20, 60, 10, 60 };
	private static final long[] vibrateToneWocket = { 40, 40 };
	private static final long[] vibrateToneZephyr = { 10, 10, 10, 10 };

	private static final long[] vibrateToneError1 = { 10, 600, 50, 300, 50 };
	private static final long[] vibrateToneError2 = { 10, 600, 50, 300, 50, 300, 50 };
	private static final long[] vibrateToneError3 = { 10, 600, 50, 300, 50, 300, 50, 300, 50 };
	private static final long[] vibrateToneError4 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50 };
	private static final long[] vibrateToneError5 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50 };
	private static final long[] vibrateToneError6 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50 };
	private static final long[] vibrateToneError7 = { 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50 };

	private static final int WOCKET = 0;
	private static final int ZEPHYR = 1;
	private static final int ALIVE = 2;
	// private static final int POLAR = 3;
	private static final int ERROR1 = 4;
	private static final int ERROR2 = 5;
	private static final int ERROR3 = 6;
	private static final int ERROR4 = 7;
	private static final int ERROR5 = 8;
	private static final int ERROR6 = 9;
	private static final int ERROR7 = 10;

	private void startVibrationAlert(int type) {

		if (DataStore.isVibrate()) {
			if (vb == null)
				vb = (Vibrator) DataStore.getContext().getSystemService(Context.VIBRATOR_SERVICE);
			if (vb != null) {
				if (type == WOCKET)
					vb.vibrate(vibrateToneWocket, -1);
				if (type == ZEPHYR)
					vb.vibrate(vibrateToneZephyr, -1);
				else if (type == ALIVE)
					vb.vibrate(vibrateToneAlive, -1);
				else if (type == ERROR1)
					vb.vibrate(vibrateToneError1, -1);
				else if (type == ERROR2)
					vb.vibrate(vibrateToneError2, -1);
				else if (type == ERROR3)
					vb.vibrate(vibrateToneError3, -1);
				else if (type == ERROR4)
					vb.vibrate(vibrateToneError4, -1);
				else if (type == ERROR5)
					vb.vibrate(vibrateToneError5, -1);
				else if (type == ERROR6)
					vb.vibrate(vibrateToneError6, -1);
				else if (type == ERROR7)
					vb.vibrate(vibrateToneError7, -1);
			} else
				Log.e(TAG, "Couldn't get vibration object.");
		}
	}

	private void saveHRDataIntoFile(ArrayList<ZephyrData> zephyrDatas) {
		Date now = new Date();
		SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hour = new SimpleDateFormat("HH");
		SimpleDateFormat detail = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa");

		// TODO fix the data directory saving location
		String dirName = Globals.DATA_DIRECTORY + File.separator + day.format(now) + "/Zephyr/";
		// String dirName = Globals.rawDataFilePath + day.format(now) +
		// "/Zephyr/";

		String dataFileName = dirName + "Zephyr_" + hour.format(now) + ".csv";
		String header = "Date,Milliseconds,Battery,Heart Rate,Heart Beat Number,Heart Beat TimeStamp #1(Newest)"
				+ ",Heart Beat TimeStamp #2,Heart Beat TimeStamp #3,Heart Beat TimeStamp #4"
				+ ",Heart Beat TimeStamp #5,Heart Beat TimeStamp #6,Heart Beat TimeStamp #7"
				+ ",Heart Beat TimeStamp #8,Heart Beat TimeStamp #9,Heart Beat TimeStamp #10"
				+ ",Heart Beat TimeStamp #11,Heart Beat TimeStamp #12,Heart Beat TimeStamp #13"
				+ ",Heart Beat TimeStamp #14,Heart Beat TimeStamp #15(Oldest),Distance,Instantaneous speed,Strides\n";
		try {
			FileHelper.createDir(dirName);
			StringBuffer content = new StringBuffer();
			if (!FileHelper.isFileExists(dataFileName))
				content.append(header);
			for (ZephyrData zephyrData : zephyrDatas) {
				HRData hrData = zephyrData.hrData;
				int[] hbTimeStamps = zephyrData.heartBeatTimeStamps;
				Date saveTime = hrData.createTime;
				if (saveTime != null)
					content.append(detail.format(saveTime) + "," + saveTime.getTime() + "," + hrData.battery + "," + hrData.heartRate + ","
							+ hrData.heartBeatNumber);
				else
					break;
				for (int i = 0; i < hbTimeStamps.length; i++) {
					content.append("," + hbTimeStamps[i]);
				}
				content.append("," + zephyrData.distance + "," + zephyrData.instantanenousSpeed + "," + zephyrData.strides + "\n");
			}
			FileHelper.appendToFile(content.toString(), dataFileName);

		} catch (WOCKETSException e) {
			Log.e(TAG, "Can not write to file. Failed to create file: " + e.toString());
		}
	}

	class ZephyrData {
		HRData hrData;
		int[] heartBeatTimeStamps;
		int distance;
		int instantanenousSpeed;
		int strides;

		public ZephyrData() {
			super();
			this.hrData = new HRData();
			this.heartBeatTimeStamps = new int[15];
			this.distance = 0;
			this.instantanenousSpeed = 0;
			this.strides = 0;
			resetDetailedData();
		}

		public void resetDetailedData() {
			this.distance = 0;
			this.instantanenousSpeed = 0;
			this.strides = 0;
			for (int i = 0; i < heartBeatTimeStamps.length; i++)
				heartBeatTimeStamps[i] = 0;
		}

		public void addHRData(HRData hrData) {
			// this.hrData.participantID = hrData.participantID;
			this.hrData.hardwareID = hrData.hardwareID;
			this.hrData.createTime = hrData.createTime;
			this.hrData.heartRate = hrData.heartRate;
			this.hrData.heartBeatNumber = hrData.heartBeatNumber;
			this.hrData.battery = hrData.battery;

		}

		public void parseDetailedZephyrData(byte[] data) {
			for (int i = 0; i < heartBeatTimeStamps.length; i++) {
				heartBeatTimeStamps[i] = ((int) data[15 + 2 * i] & 0xFF) * 256 + ((int) data[14 + 2 * i] & 0xFF);
			}
			// representing the cumulative distance travelled since the device
			// was powered on, in 16ths of a meter (valid range 0 to 4095).
			// The distance travelled rolls over every 256m.
			distance = ((int) data[51] & 0xFF) * 256 + ((int) data[50] & 0xFF);
			// representing the Instantaneous speed of the wearer in
			// steps of 1/256m/s. The valid range is 0 to 4095 steps or 0 to
			// 15.996m/s.
			instantanenousSpeed = ((int) data[53] & 0xFF) * 256 + ((int) data[52] & 0xFF);
			// representing the number of strides since the unit was powered on.
			// The
			// valid range is 0 to 255 strides. The number of strides rolls over
			// every 128 strides.
			strides = (int) data[54] & 0xFF;
		}
	}

}
