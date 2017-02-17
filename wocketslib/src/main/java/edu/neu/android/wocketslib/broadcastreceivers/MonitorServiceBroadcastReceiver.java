package edu.neu.android.wocketslib.broadcastreceivers;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.emasurvey.SurveyActivity;
import edu.neu.android.wocketslib.sensormonitor.BluetoothSensorService;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.sensormonitor.DownTime;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.SMSMonitor;

//TODO Test if monitor service is also needed to capture all the broadcasts we really want to capture

public class MonitorServiceBroadcastReceiver extends BroadcastReceiver {
	public static final String TAG = "MonitorServiceBroadcastReceiver";

	public static final String TYPE_ARBITRATE_NOW = "Arbitrate";

	// TODO I'm concerned the string below will cause problems with
	// multiple apps using the library. Does this need to be project specific?
	public static final String TYPE_START_SENSOR_MONITOR_SERVICE_NOW = "edu.neu.android.wocketslib.broadcastreceivers.MonitorServiceBroadcastReceiver.StartService";
	public static final String TYPE_USER_PRESENT = "UserPresent";
	public static final String TYPE_SCREEN_OFF = "ScreenOff";
	public static final String TYPE_SCREEN_ON = "ScreenOn";
	public static final String TYPE_BOOT_COMPLETED = "BootCompleted";
	public static final String TYPE_SET_MONITOR_SERVICE_ALARM_IF_NECESSARY = "edu.neu.android.wocketslib.broadcastreceivers.MonitorServiceBroadcastReceiver.SetAlarm";;

	static PendingIntent mAlarmSender = null;

	private static BroadcastReceiverProcessor myBroadcastReceiverProcessor = new BroadcastReceiverProcessor();

	// WocketInfo wi = null;

	// Start the BluetoothSensorService and arm an alarm to keep starting the
	// service once a minute (or designated time)
	private void setAlarm(Context aContext) {
		//Log.d(TAG, "Set alarm");
		Log.o(TAG, "PhoneState", "StartAlarm", Globals.BLUETOOTH_SENSOR_SERVICE_RUNNING_PERIOD_MS + " ms");

		if (mAlarmSender == null) {
			Intent alarmIntent = new Intent(aContext, BluetoothSensorService.class);
			alarmIntent.putExtra("FROM", "Alarm");
			mAlarmSender = PendingIntent.getService(aContext, 12345, alarmIntent, 0);
		}
		DataStore.setRunning(true);

		// We want the alarm to go off some time from now
		long firstTime = SystemClock.elapsedRealtime();

		// Schedule the alarm
		Log.d(TAG, "Alarm created.");
		AlarmManager am = (AlarmManager) aContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(mAlarmSender);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, Globals.BLUETOOTH_SENSOR_SERVICE_RUNNING_PERIOD_MS, mAlarmSender);

		// make sure the survey is restarted
		if (SurveyActivity.isWorking())
			SurveyActivity.stopWorking();
	}

	// public void addNote(Context aContext, String aMsg, boolean isPlot) {
	//
	// if (wi == null)
	// wi = new WocketInfo(aContext);
	//
	// if (wi.someNotes == null)
	// wi.someNotes = new ArrayList<Note>();
	//
	// // Util.beepPhone();
	//
	// Note aNote = new Note();
	// if (isPlot)
	// aNote.plot = 1;
	// aNote.startTime = new Date();
	// aNote.note = aMsg;
	// wi.someNotes.add(aNote);
	// }

	private void setAlarmAndStartService(Context aContext, String intentMessage) {
		setAlarm(aContext);
		// And run the service immediately as well
		Intent mi = new Intent(aContext, BluetoothSensorService.class);
		mi.putExtra("FROM", intentMessage);
		aContext.startService(mi);
	}

	@Override
	public void onReceive(Context aContext, Intent intent) {

		Log.d(TAG, "WocketsLib BR ------------------------------------------------------------------------------------------------------------------------- ");

		if (Globals.myBroadcastReceiverProcessor != null)
			Globals.myBroadcastReceiverProcessor.setContext(aContext);

		// Check for outgoing SMS when other events fire
		// TODO make this more efficient; make it work (seems broken)
		int numNewSMS = SMSMonitor.getSMSSentCount(aContext);
		if (numNewSMS > 0) {
			Log.o(TAG, "PhoneState", "SMSOut", Integer.toString(numNewSMS));
			ServerLogger.addNote(aContext, "SMS sent: " + numNewSMS, Globals.PLOT);
			// addNote(context, "SMS sent: " + numNewSMS, true);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondSendSMS();

			Log.d(TAG, "SMS Sent: " + numNewSMS);
		}

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			Log.o(TAG, "PhoneState", "Booted");
			ServerLogger.addNote(aContext, "Phone booted", Globals.PLOT);
			// addNote(aContext, "Phone booted", true);
			Log.d(TAG, "Got ACTION_BOOT_COMPLETED");

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondPhoneBooted();

			// Set the alarm for the next minute
			setAlarmAndStartService(aContext, "BOOT_COMPLETED");

		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {

			int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
			String[] somepkgs = aContext.getPackageManager().getPackagesForUid(uid);

			if (somepkgs != null) {
				for (String pkgName : somepkgs) {
					Log.o(TAG, "PhoneState", "PkgChanged", pkgName);
					ServerLogger.addNote(aContext, "Software update: " + pkgName, Globals.PLOT);
					// addNote(aContext, "Software update: " + pkgName, true);
					Log.d(TAG, "Got ACTION_PACKAGE_CHANGED: " + pkgName);
				}

				// Set the alarm for the next minute because package replaced
				// TODO Is this necessary?
				String pkgName = somepkgs[0];
				if (pkgName.compareTo(aContext.getPackageName()) == 0) {
					setAlarmAndStartService(aContext, "PACKAGED_REPLACED");
				}
			}

		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {

			int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
			String[] somepkgs = aContext.getPackageManager().getPackagesForUid(uid);

			if (somepkgs != null) {
				for (String pkgName : somepkgs) {
					Log.o(TAG, "PhoneState", "PkgAdded: " + pkgName);
					ServerLogger.addNote(aContext, "Package added: " + pkgName, Globals.PLOT);
					// addNote(context, "Package added: " + pkgName,
					// Globals.PLOT);
					Log.d(TAG, "Got ACTION_PACKAGE_ADDED: " + pkgName);
				}
			}
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

			int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
			String[] somepkgs = aContext.getPackageManager().getPackagesForUid(uid);

			boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

			if (somepkgs != null) {
				for (String pkgName : somepkgs) {
					Log.o(TAG, "PhoneState", "PkgRemoved", "Replacing: " + isReplacing + " Name: " + pkgName);
					if (isReplacing)
						ServerLogger.addNote(aContext, "Package replaced. Name: " + pkgName, Globals.PLOT);
					else
						ServerLogger.addNote(aContext, "Package removed. Name: " + pkgName, Globals.PLOT);
					// addNote(context, "Package removed. Replacing: " +
					// isReplacing + " Name: " + pkgName, Globals.PLOT);
					Log.d(TAG, "Got ACTION_PACKAGE_REMOVED: " + isReplacing + " Name: " + pkgName);
				}
			}

		} else if (intent.getAction().equals(TYPE_START_SENSOR_MONITOR_SERVICE_NOW)) {
			Log.o(TAG, "PhoneState", "StartSensorMonitorServiceNow");
			ServerLogger.addNote(aContext, "Start sensor monitor now", Globals.PLOT);
			// addNote(context, "Start sensor monitor now", Globals.PLOT);
			Log.d(TAG, "Got ACTION START_SENSOR_MONITOR_SERVICE_NOW");

			// Set the alarm for the next minute
			String from = intent.getStringExtra("FROM");
			if (from == null) from = "";

			setAlarmAndStartService(aContext, from);

		} else if(intent.getAction().equals(TYPE_SET_MONITOR_SERVICE_ALARM_IF_NECESSARY)){
			String from = intent.getStringExtra("FROM");
			if (from == null) from = "";
			if(DownTime.isDown(aContext)){
				Log.i(TAG, "Service is down, reset service alarm from: " + from);
				setAlarmAndStartService(aContext, from);
			}else{
				Log.i(TAG, "Service is not down, no need to set service alarm");
			}

		} else if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

			if (incomingNumber == null)
				incomingNumber = "Num not accessible";

			if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				if (Globals.IS_LOG_PHONE_NUMS_ENABLED)
					ServerLogger.addNote(aContext, "End phone call: " + incomingNumber, Globals.PLOT);
				else
					ServerLogger.addNote(aContext, "End phone call (num hidden)", Globals.PLOT);
				// addNote(context, "End phone call", Globals.PLOT);

				if (Globals.myBroadcastReceiverProcessor != null)
					Globals.myBroadcastReceiverProcessor.respondEndCall();

//				if (Globals.IS_LOG_PHONE_NUMS_ENABLED)
//					Log.o(TAG, "PhoneState", "End phone call", incomingNumber);
//				else
//					Log.o(TAG, "PhoneState", "End phone call");
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				if (Globals.IS_LOG_PHONE_NUMS_ENABLED)
					ServerLogger.addNote(aContext, "Start phone call: " + incomingNumber, Globals.PLOT);
				else
					ServerLogger.addNote(aContext, "Start phone call (num hidden)", Globals.PLOT);

				if (Globals.myBroadcastReceiverProcessor != null)
					Globals.myBroadcastReceiverProcessor.respondStartCall();

				// addNote(context, "Start phone call: " + incomingNumber,
				// Globals.PLOT);
				if (Globals.IS_LOG_PHONE_NUMS_ENABLED)
					Log.o(TAG, "PhoneState", "Start phone call", incomingNumber);
				else
					Log.o(TAG, "PhoneState", "Start phone call");
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				if (Globals.IS_LOG_PHONE_NUMS_ENABLED)
					Log.o(TAG, "PhoneState", "Ringing", incomingNumber);
				else
					Log.o(TAG, "PhoneState", "Ringing (num hidden)");
			}
			Log.d(TAG, "Got ACTION_PHONE_STATE: " + phoneState + " Number: " + incomingNumber);
		} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
			Log.o(TAG, "PhoneState", "UsrPres");
			ServerLogger.addNote(aContext, "UserPresent", Globals.PLOT);
			// addNote(aContext, "UserPresent", Globals.PLOT);
			Log.d(TAG, "Got ACTION_USER_PRESENT");
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Log.o(TAG, "PhoneState", "ScrOff");
			ServerLogger.addNote(aContext, "ScreenOff", Globals.PLOT);
			// addNote(aContext, "ScreenOff", Globals.PLOT);
			Log.d(TAG, "Got ACTION_SCREEN_OFF");
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.o(TAG, "PhoneState", "ScrOn");
			ServerLogger.addNote(aContext, "ScreenOn", Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondScreenOn();
			else
				myBroadcastReceiverProcessor.respondScreenOn();

			// addNote(aContext, "ScreenOn", Globals.PLOT);
			Log.d(TAG, "Got ACTION_SCREEN_ON");
			// } else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
			// {
			// Log.o(TAG, "PhoneState", "LowBat");
			// addNote(context, "Low battery", Globals.PLOT);
			// if (Globals.IS_DEBUG)
			// Log.i(TAG, "Got ACTION_BATTERY_LOW");
		} else if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
			Log.o(TAG, "PhoneState", "AirplaneMode: " + intent.getBooleanExtra("state", false));
			ServerLogger.addNote(aContext, "Airplane mode changed: " + intent.getBooleanExtra("state", false), Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondAirplaneMode();

			Log.d(TAG, "Got ACTION_AIRPLANE_MODE_CHANGED: " + intent.getBooleanExtra("state", false));
		} else if (intent.getAction().equals(Intent.ACTION_ANSWER)) {
			Log.o(TAG, "PhoneState", "CallIn");
			ServerLogger.addNote(aContext, "Call in", Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondCallIn();

			Log.d(TAG, "Got ACTION_ANSWER");
		} else if (intent.getAction().equals(Intent.ACTION_CALL_BUTTON)) {
			Log.o(TAG, "PhoneState", "CallButton");
			ServerLogger.addNote(aContext, "Call button", Globals.PLOT);
			Log.d(TAG, "Got ACTION_CALL_BUTTON");
		} else if (intent.getAction().equals(Intent.ACTION_CAMERA_BUTTON)) {
			// TODO Not working
			Log.o(TAG, "PhoneState", "CameraButton");
			ServerLogger.addNote(aContext, "Camera button", Globals.PLOT);
			Log.d(TAG, "Got ACTION_CAMERA_BUTTON");
			// } else if
			// (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
			// // TODO check this does not repeatedly fire
			// Log.o(TAG, "PhoneState", "LowStorage");
			// ServerLogger.addNote(aContext, "Low storage", Globals.PLOT);
			// Log.d(TAG, "Got ACTION_DEVICE_STORAGE_LOW");
		} else if (intent.getAction().equals(Intent.ACTION_DOCK_EVENT)) {
			// TODO this can generate an error. Investigate
			Log.o(TAG, "PhoneState", "DockState: " + intent.getStringExtra(Intent.EXTRA_DOCK_STATE));
			ServerLogger.addNote(aContext, "Dock event: " + intent.getStringExtra(Intent.EXTRA_DOCK_STATE), Globals.PLOT);
			Log.d(TAG, "Got ACTION_DOCK_EVENT: " + intent.getStringExtra(Intent.EXTRA_DOCK_STATE));
		} else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
			Log.o(TAG, "PhoneState", "Headset: " + intent.getStringExtra("name"));
			ServerLogger.addNote(aContext, "Headset plug: " + intent.getStringExtra("name"), Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondHeadsetPluggedIn();

			Log.d(TAG, "Got ACTION_HEADSET_PLUG: " + intent.getStringExtra("name"));
		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
			Log.o(TAG, "PhoneState", "MediaBadRemove");
			ServerLogger.addNote(aContext, "Media bad remove", Globals.PLOT);
			Log.d(TAG, "Got ACTION_MEDIA_BAD_REMOVAL");
		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
			Log.o(TAG, "PhoneState", "MediaRemoved");
			ServerLogger.addNote(aContext, "Media removed", Globals.PLOT);
			Log.d(TAG, "Got ACTION_MEDIA_REMOVED");
		} else if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			// TODO Add EXTRA_PHONE_NUMBER to get number?
			Log.o(TAG, "PhoneState", "CallOut");
			ServerLogger.addNote(aContext, "Call out", Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondCallOut();

			Log.d(TAG, "Got ACTION_NEW_OUTGOING_CALL");
		} else if (intent.getAction().equals(Intent.ACTION_WALLPAPER_CHANGED)) {
			Log.o(TAG, "PhoneState", "WallPaperChanged");
			ServerLogger.addNote(aContext, "Wallpaper changed", Globals.PLOT);
			Log.d(TAG, "Got ACTION_SET_WALLPAPER_CHANGED");
		} else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Log.o(TAG, "PhoneState", "PowerConnect");
			ServerLogger.addNote(aContext, "Power connected", Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondPowerConnected();

			Log.d(TAG, "Got ACTION_POWER_CONNECTED");
		} else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Log.o(TAG, "PhoneState", "PowerDisconnect");
			ServerLogger.addNote(aContext, "Power disconnected", Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondPowerDisconnected();

			Log.d(TAG, "Got ACTION_POWER_DISCONNECTED");
		} else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Log.o(TAG, "PhoneState", "Shutdown");
			ServerLogger.addNote(aContext, "Phone shutdown", Globals.PLOT);
			Log.d(TAG, "Got ACTION_SHUTDOWN");
		} else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
			String timeZone = intent.getStringExtra("time-zone");
			Log.o(TAG, "PhoneState", "Timezone", timeZone);
			ServerLogger.addNote(aContext, "Timezone changed: " + timeZone, Globals.PLOT);
			Log.d(TAG, "Got ACTION_TIMEZONE_CHANGED: " + timeZone);
		} else if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
			// TODO Doesn't work for set date (work for everyday date change?)
			Log.o(TAG, "PhoneState", "DateChange", (new Date()).toString());
			ServerLogger.addNote(aContext, "Date changed: " + (new Date()), Globals.PLOT);
			Log.d(TAG, "Got ACTION_DATE_CHANGED: " + (new Date()));
			// } else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
			// {
			// //Gets called frequently when phone getting time from the network
			// //Need to change this code to only send a note when the time
			// changes substantially
			// Log.o(TAG, "PhoneState", "TimeSet", (new Date()).toString());
			// addNote(context, "Time set: " + (new Date()), Globals.PLOT);
			// if (Globals.IS_DEBUG)
			// Log.i(TAG, "Got ACTION_TIME_CHANGED: " + (new Date()));
		} else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Log.o(TAG, "PhoneState", "SMSIn");
			ServerLogger.addNote(aContext, "SMS received", Globals.PLOT);

			if (Globals.myBroadcastReceiverProcessor != null)
				Globals.myBroadcastReceiverProcessor.respondSMSReceived();

			Log.d(TAG, "Got SMS_RECEIVED");
		} else if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
			Log.o(TAG, "PhoneState", "LocaleChange");
			ServerLogger.addNote(aContext, "Locale change", Globals.PLOT);
			Log.d(TAG, "Got ACTION_LOCALE_CHANGED");
		} else if (intent.getAction().equals(Globals.WOCKETS_REFRESH_DATA_ACTION)) {
			DataStore.init(aContext);
		}

		// If any json notes created, queue them up to send
		ServerLogger.send(TAG, aContext);
	}
}