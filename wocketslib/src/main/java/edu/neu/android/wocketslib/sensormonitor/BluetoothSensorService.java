/******************************************************************************
 * 
 * @brief Service to read from all enabled Bluetooth sensors and pass the data read in
 * 			to the appropriate objects to be decoded.
 * @author Kyle Bechtel
 * @date  6/1/11
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.RemoteException;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.datasummaryviewer.GetDataSummaryActivity;
import edu.neu.android.wocketslib.activities.datasummaryviewer.WocketsDataSPHelper;
import edu.neu.android.wocketslib.activities.datasummaryviewer.WocketsDataSaver;
import edu.neu.android.wocketslib.audio.record.AudioController;
import edu.neu.android.wocketslib.audio.record.MHealthAudioClipRecorder;
import edu.neu.android.wocketslib.broadcastreceivers.MonitorServiceBroadcastReceiver;
import edu.neu.android.wocketslib.dataupload.DataUploaderService;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.json.model.ActivityCountData;
import edu.neu.android.wocketslib.json.model.WocketStatsData;
import edu.neu.android.wocketslib.mhealthformat.LowSamplingRateDataSaver;
import edu.neu.android.wocketslib.sleepdetection.SleepDetection;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.support.VersionChecker;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.NetworkMonitor;
import edu.neu.android.wocketslib.utils.PhoneInfo;
import edu.neu.android.wocketslib.utils.PhoneNotifier;
import edu.neu.android.wocketslib.utils.PhonePowerChecker;
import edu.neu.android.wocketslib.utils.PhonePrompter;
import edu.neu.android.wocketslib.utils.PhoneVibrator;
import edu.neu.android.wocketslib.utils.WocketsNotifier;
import edu.neu.android.wocketslib.visualizerdatagenerator.DataEncoder;
import edu.neu.android.wocketslib.visualizerdatagenerator.RawDataFileHandler;
import edu.neu.android.wocketslib.wakefulintent.WakefulIntentService;

@SuppressLint("NewApi")
public class BluetoothSensorService extends Service implements SensorEventListener {
	private static final String TAG = "BluetoothSensorService";

	private static PowerManager.WakeLock wl;
	private static BluetoothAdapter mBluetoothAdapter;

	private static boolean isNewSoftwareVersion = false;

	// boolean readMore = true;
	// Sensor currentSensor = null;
	// private DataSaver mWocketDataSaver;

	// Broadcast
	public static MonitorServiceBroadcastReceiver aBroadcastReceiver = new MonitorServiceBroadcastReceiver();
	public static DataEncoder encoderForInternalAccelData = new DataEncoder();
	
	private long serviceStartTime = 0;
	private Date serviceStartDatePlus1Min = null;

    //TODO private?
	SensorManager aSensorManager = null;

	private List<android.hardware.Sensor> someSensors = null;
	private android.hardware.Sensor intAccSensor = null;

	private AudioController anAudioController = null;
    private MHealthAudioClipRecorder amHealthAudioClipRecorder = null;

	private double x;
	private double y;
	private double z;
	private boolean isFirstInteralAccelSample = true;
	double lastx, lasty, lastz;
	double highPassX, highPassY, highPassZ; 
	double alpha = 0.9f;
	double sumInternal = 0;
	private int numSamplesInternal = 0;
	private long lastInternalSampleTime = 0;
//	private static long MS_EACH_INTERNAL_SAMPLE = 1000 / 10; // 10 samples per sec
	private static long MS_EACH_INTERNAL_SAMPLE = 20;
	int skippedSamples = 0;
	long tdiff = 0;
	//private BluetoothGatt mBluetoothGatt;
	long startIntCollectionTime = 0;
	
	
	// private static String phoneID = null;

	private void registerReceivers() {
		/**
		 * TODO figure out if any of these are double registered with manifest.
		 * Leave as many as possible in the manifest, but some of these only
		 * work if registered in code
		 */
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_USER_PRESENT);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_CAMERA_BUTTON);
		intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.addAction(Globals.WOCKETS_REFRESH_DATA_ACTION);

		// intentFilter.addAction(ControllerService.ARBITRATE_NOW_BROADCAST);
		// intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
		// intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		// intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		// intentFilter.addAction(Intent.ACTION_ANSWER); //Incoming phone call
		// intentFilter.addAction(Intent.ACTION_CALL_BUTTON);
		// intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
		// intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
		// This is sticky and needs special handling:
		// intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		// intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		// intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		// intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		// intentFilter.addAction(Intent.ACTION_SET_WALLPAPER);
		// intentFilter.addAction(Intent.ACTION_SHUTDOWN); // In manifest
		// intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		// intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);

        if (aBroadcastReceiver != null)
    		getApplicationContext().registerReceiver(aBroadcastReceiver, intentFilter);
        else
            Log.e(TAG, "BroadcastReceiver is null and should not be.");
	}

	// private void unregisterReceivers() {
	// getApplicationContext().unregisterReceiver(aBroadcastReceiver);
	// }

	// WOCKET INFO FILLERS

	private int checkBattery(Context aContext) {
		int level = (int) PhoneInfo.getBatteryPercentage(aContext);
		
		Log.o(TAG, "PhoneState", "BatteryRemaining: " + level + "%");		
		ServerLogger.addPhoneBatteryReading(TAG, aContext, level);
		ServerLogger.addNote(aContext, "Battery reading: " + level, Globals.NO_PLOT);
		 // remove
		if (Globals.IS_DEBUG)
			Log.i(TAG, "Got ACTION_BATTERY_CHANGED. Level: " + level + "%");
		PhonePowerChecker.isCharging(aContext);
		
		return level;
	}
	
	private void checkRingerMode(Context aContext) {
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		String RingerMode = "";
		int ringerMode = mAudioManager.getRingerMode();
		if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
			ServerLogger.addNote(aContext, "System Ringer Mode: Silent", Globals.NO_PLOT);
			RingerMode = "Silent";
		} else if(ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
			ServerLogger.addNote(aContext, "System Ringer Mode: Vibrate", Globals.NO_PLOT);
			RingerMode = "Vibrate";
		}
		else {
			int volume = mAudioManager.getStreamVolume( AudioManager.STREAM_SYSTEM ); 
			ServerLogger.addNote(aContext, "System Ringer Mode: Normal, System Volume: " + String.valueOf(volume), Globals.NO_PLOT);
			RingerMode = "Normal";
		}
		 // remove
		if (Globals.IS_DEBUG)
			Log.i(TAG, "Got Ringer Mode Information. Ringer Mode: " + RingerMode);
	}

	@Override
	/**
	 * Called when the service is created, which will be 1/minute (or at other interval if changed from default)
	 */
	public void onCreate() {

		// Acquire a wake lock so that the CPU will stay awake while we are
		// reading from the sensors
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.activity_monitor_app_name));
		wl.acquire();
		if (!wl.isHeld()) {
			Log.e(TAG, "WakeLock not held when should be");
		} else {
			Log.d(TAG, "Got wakelock");
		}

		// Keep track of the time this takes to run ... need to keep it fast and
		// less than target interval
		serviceStartTime = System.currentTimeMillis();

		// TODO Do we want this log? Possibly change to debug.
		Log.o(TAG, "RunService", (new Date()).toString(), Long.toString(System.currentTimeMillis()));

		// Determine what the time will be 1 minute from now
		// TODO Does this need to change to do this for the interval (vs
		// assuming 1 minute)? What's this for?
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 1);
		serviceStartDatePlus1Min = calendar.getTime();

        // Get ready to send json data to the server
        ServerLogger.reset(getApplicationContext());

        // Start up the internal accelerometer sensor monitoring
		aSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		someSensors = aSensorManager.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);
		if (someSensors.size() > 0) {
			intAccSensor = someSensors.get(0);
		}
		else 
		{
			Log.d(TAG, "Did not receive any available sensors.");
		}
		isFirstInteralAccelSample = true;
		// Setup the listener
		// TODO add unregister?
		aSensorManager.registerListener(BluetoothSensorService.this, intAccSensor, SensorManager.SENSOR_DELAY_GAME);

		// Register all the receivers
		registerReceivers();
		
		
		if (Globals.IS_RECORDING_LIGHT_SENSOR_ENABALED) { 
			android.hardware.Sensor LightSensor = aSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT);
		     if(LightSensor != null){
		      Log.o(TAG, "Sensor.TYPE_LIGHT Available");
		      aSensorManager.registerListener(BluetoothSensorService.this,
		        //LightSensorListener, 
		        LightSensor, 
		        SensorManager.SENSOR_DELAY_NORMAL);	
		     }else{
		    	 Log.o(TAG, "Sensor.TYPE_LIGHT NOT Available");
		     }
		}

		// writeLogInfoTest("CreateService");
		if (Globals.IS_LOG_PHONE_BATTERY_ENABLED) { 
			checkBattery(getApplicationContext());
		}
		
		if(Globals.IS_LOG_PHONE_RINGER_MODE_ENABLE) {
			checkRingerMode(getApplicationContext());
		}
		Log.d(TAG, "Before getInitialized: " + (System.currentTimeMillis() - serviceStartTime) + " ms");

		/**
		 * The data store will need to be initialized because we are shutting
		 * down the service each time it runs. We need to load all key data back
		 * from the shared preference storage. This checks the BT connection to
		 * see what sensors are available if BT and various sensors are enabled.
		 */
		if (!DataStore.getInitialized()) {
			/**
			 * TODO We want to make sure we do the minimal possible in here and
			 * that all parts are optimized because this runs every minute
			 */
			DataStore.init(getApplicationContext());
		}

		// mWocketDataSaver = new DataSaver("test", "test", "test",
		// edu.neu.android.wocketslib.mhealth.sensordata.SensorData.TYPE.WOCKET12BITRAW,44,"junk");

		// We only need BT on if requested
		mBluetoothAdapter = null;
		if ((Globals.IS_WOCKETS_ENABLED) || (Globals.IS_BLUETOOTH_ENABLED))
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (Globals.IS_LOCATION_ENABLED) {
			// Record current location
			PhoneLocation myLocation = new PhoneLocation(getApplicationContext());
            //TODO change to save location in mHealth format
            //Would also be nice to save in a format easily loadable into a map program
			myLocation.retrieveCurrentLocationAndSave(getApplicationContext());
		}

        // Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.
		
		if(Globals.IS_TIME_CHANGED) {
			TimeChanged.checkIfTimeChanged(getApplicationContext());
		}
		
		if(Globals.IS_SERVICE_DOWN) {
			DownTime.checkIfDown(getApplicationContext());
		}
	
		
		Log.d(TAG, "Before start thread: " + (System.currentTimeMillis() - serviceStartTime) + " ms");

		if (AuthorizationChecker.isAuthorized24hrs(getApplicationContext(), false)) {
			if (Globals.IS_READING_SENSOR_ENABLED&&(!Globals.IS_MICRO_EMA_ENABLED)) {
				// Show the icon in the status bar
				PhoneNotifier.showReadingNotification(TAG, getApplicationContext());
			}
			Thread thr = new Thread(null, mTask, "BluetoothServiceThread");
			thr.start();
		} else {
			Log.o(TAG, "BluetoothSensorService is stopped since the application is not authorized");
            BluetoothSensorService.this.stopSelf();
		}
							
	}

	@Override
	// Called when the service has ended
	public void onDestroy() {

		Log.d(TAG, "Destroy service: " + (System.currentTimeMillis() - serviceStartTime) + " ms");
		//if (mBluetoothGatt == null) {
		//	return;
		//}
		//mBluetoothGatt.close();
		//mBluetoothGatt = null;
		// Shutdown internal accelerometer
		aSensorManager.unregisterListener(BluetoothSensorService.this, intAccSensor);

		// Cancel the notification with the same ID that we had used to start it
		if (Globals.IS_READING_SENSOR_ENABLED)
			PhoneNotifier.cancel(PhoneNotifier.READING_NOTIFICATION);

		// If phone is vibrating, wait for vibration to finish
		while (PhoneVibrator.isVibrating()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		PhoneVibrator.vibratePhoneStop();

		// If phone is audio prompting, wait for audio to finish playing
		while (PhonePrompter.isPrompting()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Log.e(TAG, "Error InterruptedException in BluetoothSensorService: " + e.toString());
				e.printStackTrace();
			}
		}
		PhonePrompter.cancel();

        // TODO Should this be commented out?
		// unregisterReceivers();

		// TODO Do we want to always log this?
		double runTime = (System.currentTimeMillis() - serviceStartTime) / 1000.0;
		Log.o(TAG, "ServiceRunTimeSec", String.format("%.1f", runTime));

		// TODO Change this warning to indicate if over target time, not default
		// target time of 1 min?
		if (runTime > 60)
			Log.e(TAG, "Service run over 1 min. Runtime: " + runTime);

		if (!wl.isHeld()) {
			Log.e(TAG, "WakeLock will be released but not held when should be.");
		} else {
			Log.d(TAG, "WakeLock is held and will be released.");
		}

		// Release the wake lock so that the CPU can go back into low power mode
		wl.release();

		if (wl.isHeld()) {
			Log.e(TAG, "WakeLock was not released when it should have been.");
		}
	}

//	private void writeLogInfoTest(String msg) // TO DO Fix or Move
//	{
//		String dirNameTime = "data/summary/";
//		String fileNameTime = dirNameTime + "Times.csv";
//		File fileTime = new File(DataStore.getContext().getExternalFilesDir(null), fileNameTime); // TODO
//																									// check
//		// Time nowT = new Time();
//		Date nowD = new Date();
//
//		try {
//			if (fileTime != null) {
//				// boolean writeHeader = false;
//				if (!fileTime.exists()) {
//					File directoryTime = new File(DataStore.getContext().getExternalFilesDir(null), dirNameTime);
//					directoryTime.mkdirs();
//					fileTime.createNewFile();
//				}
//
//				FileWriter outTime = new FileWriter(fileTime, true);
//				// if (DataStore.mThreadLastRunSystemTime != 0)
//				outTime.write(nowD.toString() + "," + (System.currentTimeMillis() - DataStore.mThreadLastRunSystemTime) + "," + msg + "\n");
//				outTime.close();
//			}
//		} catch (Exception e) {
//
//		}
//	}

	private void checkAndLogVersion() {
		String version = AppUsageLogger.getVersion(getApplicationContext(), TAG);
		// Check if the version has changed. If so, init AppData
		if (!(DataStorage.getVersion(getApplicationContext(), "unk").equals(version))) {
			Log.o(TAG, "Version", version);
			isNewSoftwareVersion = true;
			// Globals.InitAppInfo(getApplicationContext()); //TODO
			DataStorage.setVersion(getApplicationContext(), version);
		} else
			isNewSoftwareVersion = false;

		// TODO add a last time checked for new files
		if (isNewSoftwareVersion) {
			ServerLogger.addNote(getApplicationContext(), "New software version detected: " + version, Globals.PLOT);

			// TODO: Make sure we have the most recent copy of all important
			// files?
		}
	}

	/**
	 * Check if the last byte read so far is the marker indicating the end of
	 * the Wocket data for this connection minute
	 * 
	 * @param data
	 * @param count
	 * @return
	 */
	private boolean isReadEndRawDataResponsePacket(byte[] data, int count) {
		// Log.e(TAG, "End value: " + ((int) data[count-1]));
		if ((count > 0) && (data != null))
			if (data[count - 1] == ((byte) 0xD2)) // DONE RESPONSE PACKET
				return true;
			else
				return false;
		return false;
	}

	/**
	 * Checks to see if any of the enabled sensors have made a successful
	 * connection in the last 10 minutes. If none of them have, toggle
	 * the state of Bluetooth just to ensure there is no problem in the driver
	 * and that everything is OK.
	 */
	private void toggleBTIfNoConnections() {
		boolean btProblem = true;
		int numSensors = 0;
		for (int x = 0; x < DataStore.mSensors.size(); x++) {
			if (DataStore.mSensors.get(x).mEnabled) {
				numSensors++;
				if (DataStore.mSensors.get(x).mConnectionErrors < Defines.CONNECTION_ERRORS_BEFORE_RESET) {
					btProblem = false;
					break;
				}
			}
		}

		if (btProblem == true && numSensors > 0) {
			mBluetoothAdapter.disable();

			Log.e(TAG, "Blueooth problem. Not getting data from any selected sensors. Disable then reenable BT.");
			ServerLogger.addNote(getApplicationContext(), "Blueooth problem. Not getting data from any selected sensors. Disable then reenable BT.",
					Globals.PLOT);

			// We don't want to be constantly toggling the Bluetooth state
			// because
			// it appears to break the driver so you can't ever turn it back on.
			// To prevent this, reset the error counts to the minimum no
			// connection level
			for (int x = 0; x < DataStore.mSensors.size(); x++) {
				if (DataStore.mSensors.get(x).mEnabled && DataStore.mSensors.get(x).mConnectionErrors >= Defines.CONNECTION_ERRORS_BEFORE_RESET) {
					DataStore.mSensors.get(x).mConnectionErrors = Defines.NO_CONNECTION_LIMIT;
				}
			}

			int retryCount = 0;
			// Retry for up to 10 seconds
			while (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_OFF && retryCount < 100) {
				synchronized (mBinder) {
					try {
						mBinder.wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				retryCount++;
			}
		}
	}

	/**
	 * Cancel discovery if ongoing
	 */
	private void cancelDiscovery() {
		if (mBluetoothAdapter.isDiscovering()) {
			Log.e(TAG, "Cancelled a Bluetooth discovery operation before data read operation.");
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	private BluetoothSocket getConnectedBTSocket(BluetoothDevice dev, Sensor aSensor) {
		BluetoothSocket sock = null;
		try {
			sock = dev.createRfcommSocketToServiceRecord(getDeviceUuid(dev));
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
		while (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON && retryCount < 100) {
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
                Log.e(TAG, "Interrupted exception in synchronized mBinder: " + e.toString());
				e.printStackTrace();
			}
		}

		try {
			sock.connect();
		} catch (Exception e) {
			PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR1);
			Date d = new Date();
			Log.e(TAG, "Error when connecting to socket: " + aSensor.mAddress + " " + e.toString());
			ServerLogger.addNote(getApplicationContext(), "Error when connecting to socket: " + aSensor.mAddress + " " + e.toString() + " Time: " + d,
					Globals.NO_PLOT);

			try {
				sock.close();
			} catch (IOException e1) {
                Log.e(TAG, "Error closing socket after error connecting.");
			}

			return null;
		}

		return sock;
	}

	private boolean readDataPolar(InputStream in, Sensor aSensor) {
		PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.POLAR);

		byte[] data = new byte[Defines.MAX_POLAR_PACKET_SIZE];
		int count;
		try {
			count = in.read(data, 0, Defines.MAX_POLAR_PACKET_SIZE);
		} catch (IOException e) {
			Log.e(TAG, "Error IOException in readDataPolar: " + e.toString());
			e.printStackTrace();
			return false;
		}
		if (count == Defines.MAX_POLAR_PACKET_SIZE) {
			aSensor.parsePacket(data, count);
			return true;
		}
		return false;
	}

	private boolean readDataZephyr(InputStream in, Sensor aSensor) {
		// Vibrate the phone for debugging purposes
		PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ZEPHYR);

		byte[] data = new byte[Defines.MAX_ZEPHYR_PACKET_SIZE];

		int count;
		try {
			count = in.read(data, 0, Defines.MAX_ZEPHYR_PACKET_SIZE);
		} catch (IOException e) {
            Log.e(TAG, "IOException reading Zephyr data: " + e.toString());
			e.printStackTrace();
			return false;
		}

		while (count < Defines.MAX_ZEPHYR_PACKET_SIZE) {
			try {
				count += in.read(data, count, Defines.MAX_ZEPHYR_PACKET_SIZE - count);
			} catch (IOException e) {
                Log.e(TAG, "IOException reading Zephyr data: " + e.toString());
				e.printStackTrace();
				return false;
			}
		}

		aSensor.parsePacket(data, count);
		return true;
	}
	
	public UUID getDeviceUuid(BluetoothDevice dev) {
		UUID devUuid = null;
		ParcelUuid[] phoneUuids = null;
		try {
			Method method = dev.getClass().getMethod("getUuids", null);
			phoneUuids = (ParcelUuid[]) method.invoke(dev, null);
			devUuid = phoneUuids[0].getUuid();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Could not get device UUID");
		}
		return devUuid;
	}

	private boolean readWocketHeader(InputStream in, Sensor aSensor) {
		byte[] wocketData = aSensor.mData;
		wocketData[0] = (byte) 0;
		int count = 0;

		try {
			int delay = 0;
			// read until we don't get an 0xFF byte
			long aTime = System.currentTimeMillis();
			while ((delay < 60) && (wocketData[0] != WocketSensor.WOCKET_HEADER_DATA)) // TODO
																						// -
																						// change
																						// back
																						// to
																						// 30?
			{
				if (delay == 0)
					Log.d(TAG, "Wockets: WAITING FOR HEADER");
				if (in.available() > 0)
					in.read(wocketData, 0, 1);
				else {
					delay++;
					synchronized (mBinder) {
						mBinder.wait(10);
					}
				}
			}

			if (wocketData[0] != WocketSensor.WOCKET_HEADER_DATA) {
				Log.e(TAG, "Header data read timed out after: " + (System.currentTimeMillis() - aTime));
				// Timed out and no header found. Consider this a read failure.
				Log.e(TAG, "ERROR: Wocket appears to have connected but did not send header data in time: " + aSensor.mAddress);
				PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR7);
				ServerLogger.addNote(getApplicationContext(), "Wocket appears to have connected but did not send header data in time: " + aSensor.mAddress,
						Globals.PLOT);
				return false;
			} else {
				Log.d(TAG, "Wockets: HEADER READ IN " + (System.currentTimeMillis() - aTime));

				// Received a header data point, so throw away any more received

				delay = 0;
				// while we are reading 0xFF bytes, just throw them away
				// TODO changed from 10 to 20
				while ((delay < 30) && (wocketData[0] == WocketSensor.WOCKET_HEADER_DATA)) {
					if (in.available() > 0) {
						in.read(wocketData, 0, 1);
						count++;
					} else {
						// No data so wait a bit
						delay++;
						synchronized (mBinder) {
							mBinder.wait(10);
						}
					}
				}

				if (wocketData[0] != WocketSensor.WOCKET_HEADER_DATA) {
					Log.d(TAG, "Wockets: AT GOOD DATA (Header bytes read: " + count + ")");
					return true;
				} else {
					PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR2);
					Log.e(TAG, "ERROR: Wocket appears to have connected but non-header data was not received: " + aSensor.mAddress + " Header count: " + count);
					ServerLogger.addNote(getApplicationContext(), "Wocket appears to have connected but non-header data was not received: " + aSensor.mAddress
							+ " Header count: " + count, Globals.PLOT);
					return false;
				}
			}
		} catch (Exception e) {
			PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR3);
			Log.e(TAG, "Error reading header from Wocket " + aSensor.mAddress + " " + e.toString());
            e.printStackTrace();
			ServerLogger.addNote(getApplicationContext(), "Wocket read errors: " + aSensor.mAddress + " Num: " + aSensor.mConnectionErrors, Globals.PLOT);
		}

		return false;
	}

	private byte[] getACKPacket(int lastSeqNum) {
		byte[] seqNum = new byte[4];
		byte temp;
		temp = (byte) (lastSeqNum >> 8);
		seqNum[0] = (byte) WocketSensor.WOCKET_ACK_PACKET;
		seqNum[1] = (byte) ((byte) (temp >>> 1) & 0x7f);
		seqNum[2] = (byte) ((byte) (temp << 6) & 0x40);
		temp = (byte) (lastSeqNum);
		seqNum[2] |= (byte) ((byte) (temp >> 2) & 0x3f);
		seqNum[3] = (byte) ((byte) (temp << 5) & 0x60);
		return seqNum;
	}

	private int readWocketMainData(InputStream in, OutputStream out, Sensor aSensor) {
		byte[] wocketData = aSensor.mData;
		int sendTries = 0;
		boolean isEnd = false;
		int numread = 0;

		SharedPreferences pref = getSharedPreferences("WocketsACPref", MODE_PRIVATE);
		int lastSeqNum = pref.getInt("lastSeqNum" + aSensor.mAddress, 0);
		byte[] ackPacket = getACKPacket(lastSeqNum);

		Log.d(TAG, "Last sequence number read from memory: " + lastSeqNum);

		// Try reading at least 5 times to make sure we get all the data that
		// is available from the Wocket. Calling inputstream.available() > 0)
		// doesn't always do the right thing
		// while((in.available() > 0 || readTries < 50) &&
		// while((readTries < 60) &&
		// (count < Defines.MAX_WOCKET_PACKET_SIZE) &&
		// (!isEnd))

		int count = 1;
		StringBuffer sb = new StringBuffer("Wocket byte data read: ");
		int unsuccessfulReadTries = 0;
		while ((count > 0) && // Got through header data properly
				(unsuccessfulReadTries < 60) && // 1.5 second // TODO change
												// back to 1 second 40? Probably
												// not because was getting
												// premature disconnect
				(!isEnd) && (count < Defines.MAX_WOCKET_PACKET_SIZE)) {
			try {
				numread = 0;
				if (in.available() > 0) {
					numread = in.read(wocketData, count, Defines.MAX_WOCKET_PACKET_SIZE - count);
					if (Globals.IS_DEBUG)
						sb.append(numread + " ");
				} else
					unsuccessfulReadTries++;
			} catch (IOException e) {
				Log.e(TAG, sb.toString());
				PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR4);
				Log.e(TAG, "Error reading Wocket data " + e.toString());
				ServerLogger.addNote(getApplicationContext(), "Error reading Wocket data " + e.toString(), Globals.PLOT);
				isEnd = true;
				return 0;
			}

			if (numread == -1) {
				isEnd = true;
				Log.d(TAG, "Reached end of read (-1 value)");
			} else if (numread > 0) {
				count += numread;
				if (isReadEndRawDataResponsePacket(wocketData, count)) {
					Log.d(TAG, sb.toString());

					Log.d(TAG, "Read done packet.");
					isEnd = true;
					return count;
				}

				// Send an ack packet for activity count (try a few times to
				// help ensure it gets through)
				if (sendTries < 5) {
					try {
                        // TODO don't send acknowledgement until check order to be sure no count packets missing?
						out.write(ackPacket);
						out.flush();
					} catch (IOException e) {
						Log.e(TAG, "Problem writing activity count ACK packet to BT socket");
					}
					sendTries++;
				}
			}

			// Wait a short time before checking for more available data
			synchronized (mBinder) {
				try {
					mBinder.wait(25);
				} catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException in synchroniezd mBinder: " + e.toString());
					e.printStackTrace();
				}
			}
		}

		PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR6);
		ServerLogger.addNote(getApplicationContext(), "Incomplete read raw for: " + aSensor.mAddress + " Count: " + count, Globals.PLOT);
		Log.e(TAG, "Incomplete read of raw data. Did not read end marker: " + aSensor.mAddress);

		return 0;
	}

	private boolean readDataWocket(InputStream in, OutputStream out, Sensor aSensor) {
		aSensor.mDataToProcess = false;
		// Vibrate the phone for debugging purposes
		PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.WOCKET);

		long startTime = System.currentTimeMillis();
		Log.d(TAG, "Start read data from Wocket");

		// Set Wocket mode or shutdown
		try {
			if (isSetShutdown) {
				out.write(WocketSensor.WOCKET_RESET_PACKET);
				ServerLogger.addNote(getApplicationContext(), "Wocket shutdown command sent: " + aSensor.mAddress, Globals.PLOT);

				Log.o(TAG, "WocketResetPacketSent");
				Log.d(TAG, "WOCKET_RESET_PACKET Sent");
				
			} else {
				// There was some trouble
				// switching from continuous to burst mode before and this had
				// to be sent often. This may no longer be the case and we
				// will only need to send once

				out.write(WocketSensor.WOCKET_60_SEC_BURST_PACKET);
				Log.d(TAG, "WOCKET_60_SEC_BURST_PACKET Sent");
			}
		} catch (Exception e) {
			PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR5);
            Log.e(TAG, "Connection error: Could not send mode data to Wocket socket: " + aSensor.mAddress);
			e.printStackTrace();
			ServerLogger.addNote(getApplicationContext(),
					"Connection error: Could not send mode data to Wocket socket: " + aSensor.mAddress + " " + e.toString(), Globals.PLOT);
			return false;
		}

		boolean isHeaderRead = readWocketHeader(in, aSensor);

		if (!isHeaderRead)
			return false;

		// Data received

		int bytesDataRead = readWocketMainData(in, out, aSensor);
		aSensor.mBytesReceived = bytesDataRead;
		ServerLogger.addNote(getApplicationContext(), "Read data bytes: " + aSensor.mAddress + " Time: " + aSensor.mLastConnectionTime + " Data: "
				+ bytesDataRead, Globals.NO_PLOT);

		Log.d(TAG, "Wockets: Data count: " + bytesDataRead);
		Log.d(TAG, "Wockets: READ TIME: " + ((System.currentTimeMillis() - startTime)) / 1000.0);

		if (bytesDataRead > 0) {
			aSensor.mDataToProcess = true;
			return true;
		} else
			return false;
	}

	// private boolean processData(Sensor aSensor, LocationDetecting
	// locationDetecter)
	// {
	// if (aSensor.mBytesReceived == 0)
	// return false;
	//
	// long startTime = System.currentTimeMillis();
	// if (Globals.IS_DEBUG)
	// Log.d(TAG, "Start process data from Wocket");
	//
	// ((WocketSensor) aSensor).readUnprocessedWSData(DataStore.getContext());
	// aSensor.parsePacket(aSensor.mData, aSensor.mBytesReceived);
	// locationDetecter.addWocketsData(((WocketSensor)
	// aSensor).getWocketDataLocation());
	//
	// if (aSensor.isResetDetected())
	// {
	// Log.i(TAG, "Reset detecton on Wocket: " + aSensor.mAddress);
	// ServerLogger.addNote("Reset detected on Wocket: " + aSensor.mAddress,
	// Globals.PLOT);
	// }
	//
	// int lastSeqNum = ((WocketSensor)aSensor).getLastSeqNum();
	// Log.i(TAG, "LastSeqNumber received: " + lastSeqNum + " (" +
	// aSensor.mAddress + ")");
	// Editor edit = getSharedPreferences("WocketsACPref", MODE_PRIVATE).edit();
	// edit.putInt("lastSeqNum" + aSensor.mAddress, lastSeqNum);
	// edit.commit();
	//
	// Log.i(TAG, "Raw packets: " + aSensor.mPacketsReceived);
	// Log.i(TAG, "Bytes: " + aSensor.mBytesReceived);
	//
	// if (Globals.IS_DEBUG)
	// {
	// Log.d(TAG,
	// "================================================================================================ Bytes received: "
	// + aSensor.mBytesReceived);
	// Log.d(TAG,
	// "================================================================================================ Raw packets: "
	// + aSensor.mPacketsReceived);
	// Log.d(TAG,
	// "================================================================================================ Seq Number received: "
	// + lastSeqNum);
	// Log.d(TAG,
	// "================================================================================================ PROCESS TIME: "
	// + ((System.currentTimeMillis()-startTime))/1000.0);
	// }
	//
	// //TODO Fix
	// //currentSensor.parsePacket(data, count, mWocketDataSaver);
	//
	// return true;
	// }

	private boolean processData(Sensor aSensor) {
		if (aSensor.mBytesReceived == 0)
			return false;

		long startTime = System.currentTimeMillis();
		Log.d(TAG, "Start process data from Wocket");

		((WocketSensor) aSensor).readUnprocessedWSData(DataStore.getContext());
		aSensor.parsePacket(aSensor.mData, aSensor.mBytesReceived);
		
		if (aSensor.isResetDetected()) {
			Log.o(TAG, "WocketReset", aSensor.mAddress);
			ServerLogger.addNote(getApplicationContext(), "Reset detected on Wocket: " + aSensor.mAddress, Globals.PLOT);
		}

		int lastSeqNum = ((WocketSensor) aSensor).getLastSeqNum();
		Log.o(TAG, "WocketLastSeqNumberRec", Integer.toString(lastSeqNum), aSensor.mAddress);

        Editor edit = getSharedPreferences("WocketsACPref", MODE_PRIVATE).edit();
		edit.putInt("lastSeqNum" + aSensor.mAddress, lastSeqNum);
		edit.commit();

		Log.o(TAG, "RawPackets", Integer.toString(aSensor.mPacketsReceived));
		Log.o(TAG, "Bytes", Integer.toString(aSensor.mBytesReceived));

		if (Globals.IS_DEBUG) {
			Log.d(TAG, "Bytes received: " + aSensor.mBytesReceived);
			Log.d(TAG, "Raw packets: " + aSensor.mPacketsReceived);
			Log.d(TAG, "Seq Number received: " + lastSeqNum);
			Log.d(TAG, "PROCESS TIME: " + ((System.currentTimeMillis() - startTime)) / 1000.0);
		}

		// TODO Fix
		// currentSensor.parsePacket(data, count, mWocketDataSaver);

		return true;
	}

	/**
	 * For a given enabled device, read all the data
	 * 
	 * @param dev
	 *            An enabled and paired Bluetooth device
	 * @throws IOException
	 */
	private void readDataEnabledDevice(BluetoothDevice dev, Sensor aSensor) {
		ServerLogger.addNote(getApplicationContext(), "Start connect to : " + dev.getAddress(), Globals.NO_PLOT);
		cancelDiscovery();

		BluetoothSocket sock = getConnectedBTSocket(dev, aSensor);

		if (sock == null) {
			aSensor.mConnectionErrors++;
		} else {
			boolean isReadData = false;
			OutputStream out = null;

			// Have a connected Socket to a BT device so read data accordingly
			InputStream in = null;
			try {
				in = sock.getInputStream();
			} catch (IOException e) {
				Log.e(TAG, "Connection error: Could not get input stream from BT socket: " + aSensor.mAddress);
				e.printStackTrace();
				PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ERROR5);
				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Log.e(TAG, "Connection error: Could not close inputstream: " + aSensor.mAddress + " " + e.toString());
					}
					in = null;
				}
			}

			if (in != null) {
				if (aSensor.mType == Sensor.POLAR) {
					isReadData = readDataPolar(in, aSensor);
				} else if (aSensor.mType == Sensor.ZEPHYR) {
					isReadData = readDataZephyr(in, aSensor);
				} else if (aSensor.mType == Sensor.WOCKET) {
					try {
						out = sock.getOutputStream();
					} catch (IOException e) {
						Log.e(TAG, "Error: Could not get output stream from BT socket: " + aSensor.mAddress);
						e.printStackTrace();
					}
					isReadData = readDataWocket(in, out, aSensor);
				}

				if (isReadData) {
					aSensor.mLastConnectionTime = new Date();
					
					aSensor.mConnectionErrors = 0;
				} else {
					aSensor.mConnectionErrors++;
					Log.e(TAG, "Increment connection errors: " + aSensor.mAddress + " Errors: " + aSensor.mConnectionErrors);
				}

				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Error: Could not close inputstream: " + aSensor.mAddress + " " + e.toString());
					}
				}

				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Error: Could not close outputstream: " + aSensor.mAddress + " " + e.toString());
					}
				}

				if (sock != null) {
					try {
						sock.close();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Error: Could not close BT socket: " + aSensor.mAddress + " " + e.toString());
					}
				}
			}
		}
	}

    boolean isSetShutdown = false;
	/**
	 * The function that runs in our worker thread
	 */
	Runnable mTask = new Runnable() {
		private Object BLEManger;

		public void run() {
			Log.d(TAG, "Run thread " + (new Date()));
			
			Log.i("BluetoothTimeTracker", "Bluetooth Thread just initiated");

            // No longer used. Had to combine with sound clip recording
//            if (Globals.IS_AUDIO_AMPLITUDE_MONITORING_ENABLED) {
//                // Setup to compute the sound amplitude while the phone is processing the data
//                anAudioController = new AudioController();
//                anAudioController.startAmplitude(getApplicationContext());
//            }

            if (Globals.IS_SOUND_CLIP_RECORDING_ENABLED || Globals.IS_AUDIO_AMPLITUDE_MONITORING_ENABLED)
            {
                amHealthAudioClipRecorder = new MHealthAudioClipRecorder(TAG, PhoneInfo.getID(getApplicationContext()),
                        getApplicationContext());
                amHealthAudioClipRecorder.start();
            }

            Arbitrater anArbitrater = new Arbitrater(getApplicationContext());

			if (Globals.IS_WOCKETS_ENABLED && Globals.IS_WOCKETS_SUMMARY_NOTIFICATION_ENABLED)
				sendSummaryNotification();

			// reset configuration info before processing Wocket data
			// compressANDTransferZipFiles(new Date(), getApplicationContext());
			// //TOOD SSI: I took this out. It should not have been here

			/**
			 * start to read Zephyr data from another thread (SSI: something I
			 * was testing....)
			 */
			// ZephyrReader zephyrReader = new
			// ZephyrReader(getApplicationContext(), wi);
			// zephyrReader.startThread();
			// Util.beepPhone();

			// /**
			// * start to read Asthmapolis data from another thread
			// */
			// AsthmapolisReader asthmapolisReader = new
			// AsthmapolisReader(getApplicationContext(), wi);
			// asthmapolisReader.startThread();
			// // Util.beepPhone();

			if (Globals.IS_DEBUG)
				ServerLogger.addNote(getApplicationContext(), "Start thread", Globals.NO_PLOT);
			
			// Debugging only
			long startServiceTime = System.currentTimeMillis();
			// writeLogInfoTest("Start");
			// DataStore.setThreadLastRunTime(startServiceTime);

			PhoneVibrator.startVibrationAlert(TAG, getApplicationContext(), Globals.ALIVE);

			DataStore.resetAll();
			checkAndLogVersion();

			WocketsDataSaver dataSaver = new WocketsDataSaver(getApplicationContext());
			// LocationDetecting locationDetecter = new
			// LocationDetecting(getApplicationContext());
			// Make sure the phone supports Bluetooth

			if ((mBluetoothAdapter != null) && (Globals.IS_WOCKETS_ENABLED)) {
				toggleBTIfNoConnections(); // This could take up to 10 seconds
											// in failure case

				mBluetoothAdapter.enable();

				// Determine if the code has indicated that Wocket sensors
				// should be shutdown.
				isSetShutdown = DataStorage.isSetShutdown(getApplicationContext());
				if (isSetShutdown) {
					Log.o(TAG, "ShutdownWocketsActivated");

					if (Globals.IS_DEBUG)
						Log.i(TAG, "Shutdown Wockets was activated. Shutdown commands will be sent to connected Wockets.");

					// Reset shutdown so in the next minute it won't be set
					DataStorage.setShutdown(getApplicationContext(), false);
				}

				for (int x = 0; x < DataStore.mSensors.size(); x++) {
					// Clear all the sensor-specific data from all sensors
					if (!DataStore.mSensors.get(x).mName.contains(Defines.ZEPHYR_DEVICE_NAME))
						DataStore.mSensors.get(x).reset();
				}
				Log.d(TAG, "Start to read data from enabled sensors.");

				// Loop through all the paired Bluetooth devices on the phone
				// and check to see if
				// any of them match the devices that we want to use and have
				// enabled
				Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
				Iterator<BluetoothDevice> itr = devices.iterator();
				while (itr.hasNext()) {
					BluetoothDevice dev = itr.next();
					Sensor aSensor = null;

					Log.o(TAG, "Device", dev.getAddress(), dev.getName());

                    for (int x = 0; x < DataStore.mSensors.size(); x++) {

                        if (Globals.IS_AUTO_ENABLE_BT_SENSORS)
                        {
                            // Enable any Bluetooth sensors that are paired
                            DataStore.mSensors.get(x).mEnabled = true;
                        }

                        if (dev.getName().equals(DataStore.mSensors.get(x).mName) && DataStore.mSensors.get(x).mEnabled) // TESTING ONLY: && !(DataStore.mSensors.get(x).mName.contains(Defines.ZEPHYR_DEVICE_NAME)))
						{
							aSensor = DataStore.mSensors.get(x);

							if (aSensor != null) {
								// We've found a device we want to read from.
								Log.i(TAG, "Attempt read from enabled device: " + dev.getName() + " " + dev.getAddress());
								readDataEnabledDevice(dev, aSensor);
							}
						} // End valid sensor that is enabled
					} // End iteration through all BT sensors paired
				}

				Log.d(TAG, "Start to save raw data");
				// Now that data grabbed efficiently and Wocket shutdown,
				// process all read data
				// for(Sensor s: DataStore.mSensors)
				// //GotConcurrentModificationException
				ArrayList<WocketBurstModeData> wocketsDatas = new ArrayList<WocketBurstModeData>(DataStore.mSensors.size());

				for (int x = 0; x < DataStore.mSensors.size(); x++) {
					Sensor s = DataStore.mSensors.get(x);
					if (s.mDataToProcess) // TODO fix for HR
					{
						// We've found a device we want to read from
						Log.o(TAG, "ProcessData", s.mAddress);
						// processData(s,locationDetecter);
						processData(s);
						wocketsDatas.add(((WocketSensor) s).getWocketDataPacket());
						dataSaver.saveRawDataById(s.mAddress, s.mPacketsReceived);
					}
				}

				if(Globals.IS_SEND_WOCKET_DATA){
					Log.d(TAG, "Sending Wockets data to the real-time classification application");
					Intent i = new Intent(WocketBurstModeData.INTENT_ACTION_WOCKET_DATA);
					i.putExtra(WocketBurstModeData.INTENT_ACTION_WOCKET_DATA,
							wocketsDatas);
					sendBroadcast(i);
				}


				// //detect Wockets location
				// locationDetecter.locationDetecting();
				
				if (Globals.IS_SLEEP_DETECTION_ENABLED) {
					SleepDetection.resetSleepParametersIfNeeded(getApplicationContext());
				}

				Log.d(TAG, "Start to save summary data.");
				processThenSendActivityData(dataSaver);
				Log.d(TAG, "Start to save internal accelerometer data.");
				// Save the data read from the internal accelerometer
				// TODO If any sensor stats change later (e.g., because of
				// processing of raw data, this may need to move down
				for (int x = 0; x < DataStore.mSensors.size(); x++) {
					Sensor s = DataStore.mSensors.get(x);
					if (s.mEnabled) {
						DataStore.saveSensorStats(getApplicationContext(), s);
					}
				}
				
				// Check if this is a new day and we need to reset the score
				// DataStore.checkForScoreReset();

				// Broadcast an Intent to the the UI Activity now that new data
				// is available
				publishDataUpdated();

				// Only alarm if its been more than 30 minutes after last alert
				/*
				 * Time now = new Time(); now.setToNow();
				 *
				 * Time nextAllowedAlert = new Time(); if(mLastStillAlertTime !=
				 * null) { nextAllowedAlert.set(mLastStillAlertTime.second,
				 * mLastStillAlertTime
				 * .minute+Defines.MINUTES_BETWEEN_STILLNESS_ALERT,
				 * mLastStillAlertTime.hour, mLastStillAlertTime.monthDay,
				 * mLastStillAlertTime.month, mLastStillAlertTime.year);
				 *
				 * nextAllowedAlert.normalize(false); }
				 */

				// if( DataStore.mStillnessDuration != 0 &&
				// (mLastStillAlertTime == null || now.after(nextAllowedAlert))
				// &&
				// checkForStillness(DataStore.mStillnessDuration) )
				// {
				// //Show notification
				// showStillNotification();
				//
				// //Record the current time so that we won't alarm again if the
				// user remains
				// // still until a minimum time has passed
				// if( mLastStillAlertTime == null)
				// {
				// mLastStillAlertTime = new Time();
				// }
				// mLastStillAlertTime.setToNow();
				//
				// }

				// Check if the user has been still, but their heart rate has
				// spiked,
				// show a notification if so
				// if( DataStore.mEmotionalEventThreshold != 0 &&
				// checkForEmotionalEvent() )
				// {
				// showEmotionNotification();
				// }
			} else if ((mBluetoothAdapter != null) && (Globals.IS_BLUETOOTH_ENABLED)) {
				// Bluetooth being used but without Wockets. Just turn it on.
				// (E.g., for Asthma)
				mBluetoothAdapter.enable();
			}

			if (Globals.IS_BLE_ENABLED) {
				// This is for Bluetooth Low Energy
				BLEManager.clear();
				long lastScanTimeStamp = DataStorage.GetValueLong(getApplicationContext(), "LAST_BLE_SCAN_TIMESTAMP", 0L);
				if(mBluetoothAdapter == null){
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				}
				
				// Enable blue tooth adapter if disabled
				if(!mBluetoothAdapter.isEnabled())
					mBluetoothAdapter.enable();
				
				// TODO: Add scan at a particular time
				long now = System.currentTimeMillis();
				if (now - lastScanTimeStamp >= Globals.BLE_NEXT_SCAN_TIME) {
					mBluetoothAdapter.startLeScan(BLEManager.mLeCallback);
					// Scan for the duration set in Globals (milliseconds)
					while (System.currentTimeMillis() - now <= Globals.BLE_SCAN_DURATION) {
						// Time loop
					}
					mBluetoothAdapter.stopLeScan(BLEManager.mLeCallback);
					BLEManager.saveScannedOUSDevices(getApplicationContext());
					DataStorage.SetValue(getApplicationContext(),
							"LAST_BLE_SCAN_TIMESTAMP", now);
				}
				BLEManager.connect(getApplicationContext());
				
			}
			if (Globals.IS_LOG_SHARED_PREFERENCES_SIZE_ENABLED) {
				Long now = System.currentTimeMillis();
				long lastLogSFSize = DataStorage.GetValueLong(getApplicationContext(), "lastLogSFSize", 0);
				if (now - lastLogSFSize > Globals.MINUTES_60_IN_MS) {
					File file = new File("/data/data/" + getApplicationContext().getPackageName() + "/shared_prefs/WOCKETSSharedPrefs.xml");
					
					if(file.exists()){ 
						double kbytes = file.length(); 
						Log.o("sharedPrefSize", kbytes + " Bytes");
					}
					DataStorage.SetValue(getApplicationContext(), "lastLogSFSize", now);
				}
			}
			
			if (Globals.IS_VERSION_CHECK_AVAILABLE)
				VersionChecker.checkNewUpdateAvailable(getApplicationContext(), 1);
				
			if (Globals.myArbitrater != null){
				
				Log.i("BluetoothTimeTracker", "doArbitrate just initiated");
				
				Globals.myArbitrater.doArbitrate(isNewSoftwareVersion);
			
			
			
			}else{
				anArbitrater.doArbitrate(isNewSoftwareVersion);

			}
			
//			writeLogInfoTest("Arbitrate took," + (System.currentTimeMillis() - ct));

			// Check if need to start an upload of data to the server, and if
			// need
			// one, start background task to do it
			// if (InitiateUploadIfTime(aContext)) {
			// if (Globals.IS_DEBUG)
			// Log.e(TAG, "Upload started");
			// }

			// TODO do we want this here or ony when we try to upload. Concerned
			// this my try to make a connection.
			NetworkMonitor.logNetworkStatus(getApplicationContext());
			

//			writeLogInfoTest("End after," + (System.currentTimeMillis() - startServiceTime));

            // Check if it is time to move data around or upload to the server. If so, spawn service
            long lastUploadJSONToServer = DataStorage.GetValueLong(getApplicationContext(), DataUploaderService.KEY_UPLOAD_JSON, -1);
            if ((System.currentTimeMillis() - lastUploadJSONToServer) > Globals.JSON_DATA_UPLOAD_INTERVAL)
			{
				WakefulIntentService.sendWakefulWork(getApplicationContext(), DataUploaderService.class, "Message");
			}
            

			String tmp = "Time running " + ((System.currentTimeMillis() - startServiceTime) / 1000.0) + " s";

			// Send all the information gathered during this arbitrate, or queue
			// up to send later if no network
			long startTimeTransmit = System.currentTimeMillis();

			processThenSendInternalAccelData(dataSaver);
			

			Log.d(TAG, "Start to put the data in the graph");
			dataSaver.cleanAndCommitData(new Date());

			Intent updateDataViewer = new Intent(GetDataSummaryActivity.INTENT_ACTION_UPDATE_DATA);
			sendBroadcast(updateDataViewer);
			

			// zephyrReader.stopThread();

			Log.d(TAG, "Start to queue wocket info.");

			// TODO
			// DataSender.transmitOrQueueWocketInfo(getApplicationContext(), wi,
			// true);

			ServerLogger.send(TAG, getApplicationContext());

			ServerLogger.addNote(getApplicationContext(),
					tmp + " Then time transmitting " + ((System.currentTimeMillis() - startTimeTransmit) / 1000.0) + " s", Globals.NO_PLOT);

			
			Log.d(TAG, "TEST: " + serviceStartTime + " " + Globals.MIN_MS_FOR_SENSING_WHEN_PHONE_PLUGGED_IN + " " + System.currentTimeMillis());
			// TODO Change to only do this if the phone is plugged in!
			
			
			while (System.currentTimeMillis() < (serviceStartTime + Globals.MIN_MS_FOR_SENSING_WHEN_PHONE_PLUGGED_IN)) {
				// Check if the phone is plugged in. If so, wait a while because
				// we want to keep
				// reading sensor data for a while (usually most of the minute)
				// before shutting down
				Log.d(TAG, "Waiting " + (((serviceStartTime + Globals.MIN_MS_FOR_SENSING_WHEN_PHONE_PLUGGED_IN) - System.currentTimeMillis()) / 1000.0)
                        + " more seconds because plugged in....");

                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.e(TAG, "Error in sleep in BluetoothSensorService");
					e.printStackTrace();
				}
			}
			
			
            // If audio amplitude processing running, shut down
            if (anAudioController != null)
            {
                anAudioController.stopAudioDetectors();
            }
            

            // If audio sound clip processing, shut down and save file

            Log.d(TAG, "CHECK audio INNER");
            if (amHealthAudioClipRecorder != null)
            {
                Log.d(TAG, "STOP audio INNER");
                amHealthAudioClipRecorder.stop();
            }

            if (Globals.IS_DEBUG)
				Log.i(TAG, "EXIT THREAD");
			Log.d(TAG, "Stop service from INNER.");
			// Done with our work...stop the service!
			BluetoothSensorService.this.stopSelf();
		}
	};
	
	
	/**
	 * convert short array into a string for debugging purposes
	 * 
	 * @param data
	 * @return data in the form of a string
	 */
	String ByteArrayToString(short[] data) {
		String retVal = "";
		for (int x = 0; x < data.length; x++) {
			retVal += data[x] + " ";
		}
		return retVal;
	}

	/**
	 * Sends a broadcast for other activities to respond to once all data has
	 * been read and processed
	 */
	void publishDataUpdated() {
		Intent i = new Intent(Defines.NEW_DATA_READY_BROADCAST_STRING);
		sendBroadcast(i);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void processThenSendActivityData(WocketsDataSaver dataSaver) {
		// Look through all of the enabled Wocket sensors that do not have any connection errors.
		int numWockets = 0;
		for (int x = 0; x < DataStore.mSensors.size(); x++) {
			if ((DataStore.mSensors.get(x).mType == Sensor.WOCKET) && DataStore.mSensors.get(x).mEnabled && DataStore.mSensors.get(x).mConnectionErrors == 0) {
				WocketSensor wocket = (WocketSensor) (DataStore.mSensors.get(x));
				if (Globals.IS_DEBUG) {
					Log.d(TAG, "CHECKING WOCKET: " + wocket.mAddress
							+ " DATA: " + wocket.mBytesReceived + " PACKETS: " + wocket.mPacketsReceived);
					Log.i(TAG, "Checking Wocket: " + wocket.mAddress);
				}

				// If time is set and not queued, add to JSON object to send and
				// mark as sent
				ServerLogger.addWocketData(TAG, wocket, getApplicationContext());

				wocket.processSummaryPoints(DataStore.getContext());

				dataSaver.setSummaryDataById(wocket.mAddress, WocketsDataSaver.getSummaryPoints(wocket), WocketsDataSaver.getSummaryTimeStamps(wocket));
				
				DataStorage.SetValue(getApplicationContext(), Globals.LAST_WOCKET_CONNCTION_TIME, Globals.mHealthTimestampFormat.format(new Date()));
				
				numWockets++;
			}
		}
		int prevNumWockets = (int)DataStorage.GetValueLong(getApplicationContext(), Globals.NUM_ACTIVE_WOCKET, numWockets);
		if (numWockets != prevNumWockets) {
			DataStorage.SetValue(getApplicationContext(), Globals.NUM_ACTIVE_WOCKET, numWockets);		
			String[] msg = new String[] {"Number of Wockets: " + numWockets};        	
	    	Log.o(Globals.WOCKET_STATUS, msg);
		}
	}	
			
    private void processThenSendInternalAccelData(WocketsDataSaver dataSaver) {
		double difftS = ((lastInternalSampleTime - startIntCollectionTime) / 1000.0);
		int intAC = (int) (1000 * (sumInternal / (double) numSamplesInternal));
		Log.d(TAG, "Internal: SAMPLES: " + numSamplesInternal);
		Log.d(TAG, "Internal: SUM    : " + sumInternal);
		Log.d(TAG, "Internal: TIME    : " + difftS);
		Log.d(TAG, "Internal: SR      : " + (numSamplesInternal / difftS));
		Log.d(TAG, "Internal: SCALED  : " + intAC);
		Context aContext = getApplicationContext();
		WocketSensor wocket = new WocketSensor(aContext, "Internal", PhoneInfo.getID(aContext));

		ServerLogger.initWocketsInfo(getApplicationContext());

		dataSaver.setInternalData((int) numSamplesInternal);

		Date now = new Date();
		if ((now.before(Globals.REASONABLE_DATE))) {
			Log.e(TAG, "Creating internal data when the lastConnectiontime for the internal is not set!: " + now);
		} else {
			ActivityCountData aActivityCountData = new ActivityCountData();
			aActivityCountData = new ActivityCountData();
			aActivityCountData.activityCount = intAC;
			aActivityCountData.createTime = serviceStartDatePlus1Min; // now;
			aActivityCountData.originalTime = serviceStartDatePlus1Min; // now;
			aActivityCountData.macID = wocket.mAddress;
			ServerLogger.addActivityCountData(aActivityCountData, getApplicationContext());

			//TODO Remove after testing:
			WocketStatsData aWocketStatsData = new WocketStatsData();
			aWocketStatsData.createTime = serviceStartDatePlus1Min; // now;
			aWocketStatsData.macID = wocket.mAddress;
			// aWocketStatsData.wocketBattery = wocket.mBattery;
			aWocketStatsData.receivedBytes = numSamplesInternal;
			aWocketStatsData.transmittedBytes = ((int) (numSamplesInternal / difftS));
			ServerLogger.addWocketsStatsData(aWocketStatsData, getApplicationContext());

			// TODO save in proper mhealth data format (not this simplified)
			Log.o("InternalAcc", Integer.toString(intAC));
			
			if (Globals.IS_DEBUG) {
                if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                    try {
                        Log.i(TAG,
                              "Send this info to JSON for INTERNAL ("
                                      + RSACipher.encrypt(wocket.mAddress, getApplicationContext())
                                      + ") and connect time: " + now + ". AC: "
                                      + intAC);
                    } catch (IOException | GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG,
                          "Send this info to JSON for INTERNAL ("
                                  + wocket.mAddress
                                  + ") and connect time: " + now + ". AC: "
                                  + intAC);
                }
            }
		}
	}

	// /**
	// * Checks if there have not been any movements during the specified time
	// period
	// *
	// * @param duration - the time period to check for movements, given in
	// minutes
	// * @return - true if there hasn't been any movement, false if there has
	// */
	// private boolean checkForStillness(int duration)
	// {
	//
	// boolean retVal = false;
	// int sensorsChecked = 0;
	// Time startTime = new Time();
	// startTime.setToNow();
	// //get the start time at which we want to start looking for movement
	// startTime.set(startTime.second, startTime.minute - duration,
	// startTime.hour, startTime.monthDay, startTime.month, startTime.year);
	// //the call to normalize will deal with negative minutes and reset the
	// hour and days accordingly
	// startTime.normalize(false);
	//
	// // make sure we have enough data to measure.
	// if( DataStore.getStartRecordingTime() == null ||
	// DataStore.getStartRecordingTime().after(startTime))
	// {
	// return false;
	// }
	//
	// //Look through all of the enabled Wocket sensors that do not have any
	// connection errors.
	// // If any single one of them does not have recorded movement above the
	// threshold, return true
	// for( int x=0;x<DataStore.mSensors.size();x++)
	// {
	// if( DataStore.mSensors.get(x).mType == TYPE.WOCKET &&
	// DataStore.mSensors.get(x).mEnabled &&
	// DataStore.mSensors.get(x).mConnectionErrors == 0)
	// {
	// sensorsChecked++;
	// boolean idleWocket = true;
	// WocketSensor wocket = (WocketSensor)(DataStore.mSensors.get(x));
	// int summarySize = wocket.mSummaryPoints.size();
	// for( int y = summarySize-1;y>=0;y--)
	// {
	// if(wocket.mSummaryPoints.get(y).mActualTime.after(startTime) &&
	// wocket.mSummaryPoints.get(y).mActivityCount >
	// Defines.WOCKET_STILLNESS_MIN)
	// {
	// idleWocket = false;
	// break;
	// }
	// }
	// if( idleWocket)
	// {
	// retVal = true;
	// break;
	// }
	// }
	// }
	//
	// //If we couldn't find any sensors that were enabled without connection
	// errors,
	// // we couldn't have possible detected stillness
	// if( sensorsChecked == 0)
	// {
	// retVal = false;
	// }
	//
	// Log.i( TAG, "STILL now: " + startTime.format2445() + "duation: " +
	// duration + " " + retVal);
	// return retVal;
	//
	// }

	// /**
	// * Check if the user has not recorded any movement during the defined
	// period
	// * for no movement, and the heart rate has increased above the set
	// threshold
	// * value.
	// *
	// * @return - true if no movement and increase HR, false otherwise
	// */
	// private boolean checkForEmotionalEvent() {
	// // Find the first enabled heart rate sensor without connection errors.
	// // If multiple
	// // sensors are enabled, only the first in the list will be used
	// HeartRateSensor hrSensor = null;
	// for (int x = 0; x < DataStore.mSensors.size(); x++) {
	// if (((DataStore.mSensors.get(x).mType == Sensor.POLAR) ||
	// DataStore.mSensors.get(x).mType == Sensor.ZEPHYR) &&
	// DataStore.mSensors.get(x).mEnabled
	// && DataStore.mSensors.get(x).mConnectionErrors == 0) {
	// hrSensor = (HeartRateSensor) (DataStore.mSensors.get(x));
	// break;
	// }
	// }
	//
	// // If there is no working HR sensor, just return
	// if (hrSensor == null) {
	// return false;
	// }
	//
	// // Calculate the threshold for triggering an event based on the current
	// // set
	// // threshold
	// int currentHR = hrSensor.mCurrentRate;
	// int trailingAverageHR = hrSensor.mTrailingAvg;
	// int thresholdHR = (trailingAverageHR + (int) ((trailingAverageHR *
	// (DataStore.mEmotionalEventThreshold / 100.0))));
	//
	// Log.i(TAG, "Emotion current: " + currentHR + " threshold: " +
	// thresholdHR);
	//
	// // wi.someHRData = new ArrayList<HRData>();
	// // addHRData(currentHR, hrSensor.mBattery);
	//
	// // //If the heart rate is above threshold, check if the user has been
	// // without movement
	// // if( currentHR > thresholdHR &&
	// // checkForStillness(DataStore.mEmotionalEventStillnessDuration))
	// // {
	// // return true;
	// // }
	//
	// return false;
	// }

	/**
	 * This is the object that receives interactions from clients.
	 */
	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

	@Override
	public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
		// TODO Report on what the accuracy actually is?
		Log.d(TAG, "Internal - AccChanged");
	}

	// sec
	private double lastSumInternal = 0;
	private int lastNumSamplesInternal = 0;
	private int recordingCount = 1;
	private long lastInternalRecordingTime = 0;
	private final static long MS_RECORDING_INTERVAL = 1000;
	
	private void addIntPoint(double x, double y, double z, long timeStamp) {
		// Log.d(TAG, "Internal x: " + x + " y: " + y + " z: " + z + " ts: " + timeStamp);
		// Log.o(TAG, "" + x, "" + y, "" + z);	
		lastInternalSampleTime = timeStamp;
		numSamplesInternal++;
		highPassX = highPass(x, lastx, highPassX); 
		highPassY = highPass(y, lasty, highPassY); 
		highPassZ = highPass(z, lastz, highPassZ); 
		sumInternal += highPassX + highPassY + highPassZ;
		lastx = x;
		lasty = y;
		lastz = z;					
	}
	
	double highPass(double current, double last, double filtered) {
		return Math.abs(alpha * (filtered + current - last)); 
	}

	int INTERNAL_ACCEL_DATA_RECORDING_COUNT = 20;

	
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) {

			// TODO make efficient
			long now = (new Date()).getTime()
					+ (event.timestamp - System.nanoTime()) / 1000000L;

			if (isFirstInteralAccelSample) {
				x = event.values[SensorManager.DATA_X]
						/ SensorManager.GRAVITY_EARTH;
				y = event.values[SensorManager.DATA_Y]
						/ SensorManager.GRAVITY_EARTH;
				z = event.values[SensorManager.DATA_Z]
						/ SensorManager.GRAVITY_EARTH;
				highPassX = highPassY = highPassZ = 0;
				numSamplesInternal = 0;
				sumInternal = 0;
				isFirstInteralAccelSample = false;
				startIntCollectionTime = now;
				lastInternalSampleTime = now;

				recordingCount = 0;
				lastSumInternal = 0;
				lastNumSamplesInternal = 0;
				lastInternalRecordingTime = now;

				lastx = x;
				lasty = y;
				lastz = z;
			}

			tdiff = (now - lastInternalSampleTime);
			// Log.d(TAG, "TDIFF: " + tdiff);

			if (tdiff > MS_EACH_INTERNAL_SAMPLE) {
				skippedSamples = (int) (tdiff / MS_EACH_INTERNAL_SAMPLE);

				if (skippedSamples > 0)
					skippedSamples--;

				for (int i = 0; i < skippedSamples; i++) {
					// Log.d(TAG,"Insert");
					addIntPoint(lastx, lasty, lastz, now
							+ MS_EACH_INTERNAL_SAMPLE * i);
				}

				x = event.values[SensorManager.DATA_X]
						/ SensorManager.GRAVITY_EARTH;
				y = event.values[SensorManager.DATA_Y]
						/ SensorManager.GRAVITY_EARTH;
				z = event.values[SensorManager.DATA_Z]
						/ SensorManager.GRAVITY_EARTH;

				addIntPoint(x, y, z, now + MS_EACH_INTERNAL_SAMPLE
						* skippedSamples);

				if (Globals.IS_RECORDING_RAW_PHONE_ACCEL_DATA_ENABLED) {
					encoderForInternalAccelData
							.encodeAndSaveDataForInternalAccel(
									Calendar.getInstance(), x, y, z,
									PhoneInfo.getID(getApplicationContext()),
									getApplicationContext());
				}
			} else {
				// Not enough time has elapsed. Do not count the sample
			}

			if (now - lastInternalRecordingTime >= MS_RECORDING_INTERVAL
					&& recordingCount < INTERNAL_ACCEL_DATA_RECORDING_COUNT) {
				++recordingCount;
				// Calculate the sum average for each second
				double sumDiff = sumInternal - lastSumInternal;
				double numSamplesDiff = numSamplesInternal
						- lastNumSamplesInternal;
				int phoneAccelAverage = (int) (sumDiff / numSamplesDiff * 1000);
				int phoneAccelSamples = numSamplesInternal
						- lastNumSamplesInternal;

				// Update values for the next second
				lastSumInternal = sumInternal;
				lastNumSamplesInternal = numSamplesInternal;
				lastInternalRecordingTime += MS_RECORDING_INTERVAL;

				// save the internal accelerometer average and number of samples
				if (Globals.IS_RECORDING_PHONE_ACCEL_ENABLED) {
					Context aContext = getApplicationContext();
					String[] header = { "TIME_STAMP", "PHONE_ACCEL_AVERAGE",
							"PHONE_ACCEL_SAMPLES" };
					String[] values = { phoneAccelAverage + "",
							phoneAccelSamples + "" };
					LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(
							Globals.IS_SENSOR_DATA_EXTERNAL,
                      							Globals.SENSOR_TYPE_PHONE_ACCELEROMETER, PhoneInfo
									.getIDString(aContext).replace(' ', '_'),
							header);
					dataSaver.saveData(values);
				}
			}
		}
		if (event.sensor.getType() == android.hardware.Sensor.TYPE_LIGHT) {
			if (Globals.IS_RECORDING_LIGHT_SENSOR_ENABALED) {
				int newMinute = new Date().getHours() * 60
						+ new Date().getMinutes();
				int lastLight = (int) DataStorage.GetValueLong(
						getApplicationContext(), "LAST_MINUTE_LIGHT_SAVED", 0);
				float sumLight = (int) DataStorage.GetValueFloat(
						getApplicationContext(), "SUM_LIGHT", 0);
				int cntLight = (int) DataStorage.GetValueLong(
						getApplicationContext(), "CNT_LIGHT", 0);
				if (lastLight != newMinute) {
					String[] header = { "TIME_STAMP", "LIGHT" };
					String[] values = { ((cntLight != 0) ? (sumLight / cntLight)
							: 0)
							+ "" };
					LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(
							Globals.IS_SENSOR_DATA_EXTERNAL,
							Globals.SENSOR_TYPE_PHONE_LIGHT, PhoneInfo
									.getIDString(getApplicationContext())
									.replace(' ', '_'), header);
					dataSaver.saveData(values);
					DataStorage.SetValue(getApplicationContext(),
							"LAST_MINUTE_LIGHT_SAVED", newMinute);
					sumLight = 0;
					cntLight = 0;
				} else {
					sumLight += event.values[0];
					cntLight++;
				}

				DataStorage.SetValue(getApplicationContext(), "SUM_LIGHT",
						sumLight);
				DataStorage.SetValue(getApplicationContext(), "CNT_LIGHT",
						cntLight);
			}
		}
	
	
	}

	public void sendSummaryNotification() {
		Date now = new Date();
		int hour = now.getHours();
		boolean isPrompted = isPromptedToday();
		if (isPrompted && hour < 8) {
			setIsPromtedToday(false);
		} else if (!isPrompted && hour >= 8) {
			Context c = getApplicationContext();
			WocketsNotifier.notifyStatusBar(c, getDataSummary(), Globals.WOCKETS_NOTIFICATION_ID);
			setIsPromtedToday(true);
		}
	}

	public boolean isPromptedToday() {
		SharedPreferences sp = getSharedPreferences(Defines.SHARED_PREF_IS_PROMPT_TODAY, Context.MODE_PRIVATE);
		return sp.getBoolean(Defines.SHARED_PREF_IS_PROMPT_TODAY, false);
	}

	public void setIsPromtedToday(boolean isPromptToday) {
		SharedPreferences sp = getSharedPreferences(Defines.SHARED_PREF_IS_PROMPT_TODAY, Context.MODE_PRIVATE);
		sp.edit().putBoolean(Defines.SHARED_PREF_IS_PROMPT_TODAY, isPromptToday).commit();
	}

	public String getDataSummary() {
		WocketsDataSPHelper wocketsDataSPHelper = new WocketsDataSPHelper(getApplicationContext());
		double summaryData_Wrist = 0;
		double summaryData_Ankle = 0;
		for (String TAG : WocketsDataSPHelper.TAGS) {
			String tag = TAG.toLowerCase();
			if (tag.contains("summary")) {
				if (tag.contains("wrist")) {
					int[] data = wocketsDataSPHelper.getData(TAG);
					for (int i : data) {
						if (i > 0)
							summaryData_Wrist++;
					}
				}
				if (tag.contains("ankle")) {
					int[] data = wocketsDataSPHelper.getData(TAG);
					for (int i : data) {
						if (i > 0)
							summaryData_Ankle++;
					}
				}

			}
		}
		double missedPercentage_Wrist = (1 - summaryData_Wrist / 1440) * 100;
		double missedPercentage_Ankle = (1 - summaryData_Ankle / 1440) * 100;
		String summary = "In the last 24 hours, you have missed " + String.format("%.1f", missedPercentage_Wrist > 0 ? missedPercentage_Wrist : 0)
				+ "% data in the wrist and " + String.format("%.1f", missedPercentage_Ankle > 0 ? missedPercentage_Ankle : 0) + "% data in the ankle.";
		return summary;
	}

	/**
	 * Reset configuration file for sensor info in the beginning of a new day
	 * 
	 * @param now
	 *            the time
	 * @param c
	 *            the c
	 */
	public void compressANDTransferZipFiles(Date now, Context c) {
		// reset configuration info in the beginning of a new day
		Date lastRun = DataStore.getLastTimeRunInDate(c);
		if (lastRun == null) {
			return;
		}
		RawDataFileHandler rawDataFileHandler = new RawDataFileHandler(c);
		// zip encoded data file for the last hour and put into external memory
		if (now.getHours() != lastRun.getHours()) {
			rawDataFileHandler.zipRawData(lastRun, c);
		}
		// transmit zip files to server after 10pm
		int hour = now.getHours();
		boolean isTransmitted = isTransmittedToday();
		if (isTransmitted && hour < 22) {
			setIsTransmittedToday(false);
		} else if (!isTransmitted && hour >= 22) {
			// check the internal memory, make sure all of the raw data except
			// today are compressed and saved in the SD card
			// then delete the folder and files
			rawDataFileHandler.doubleCheckOldFiles(now, c);

			if (rawDataFileHandler.transmitRawData(c))
				setIsTransmittedToday(true);
		}
	}

	public boolean isTransmittedToday() {
		SharedPreferences sp = getSharedPreferences(Defines.SHARED_PREF_IS_TRANSMITTED_TODAY, Context.MODE_PRIVATE);
		return sp.getBoolean(Defines.SHARED_PREF_IS_TRANSMITTED_TODAY, false);
	}

	public void setIsTransmittedToday(boolean isTransmittedToday) {
		SharedPreferences sp = getSharedPreferences(Defines.SHARED_PREF_IS_TRANSMITTED_TODAY, Context.MODE_PRIVATE);
		sp.edit().putBoolean(Defines.SHARED_PREF_IS_TRANSMITTED_TODAY, isTransmittedToday).commit();
	}
	
	
}
