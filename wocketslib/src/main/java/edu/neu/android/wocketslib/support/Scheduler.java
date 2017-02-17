package edu.neu.android.wocketslib.support;

import java.util.Calendar;

import android.content.Intent;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;

public class Scheduler {
	private static final String TAG = "Scheduler";

	private static final String ALARM_BASE_NAME = "edu.neu.android.wocketslib.alerts.alarmhelper";
	public static final String SET_ALARM_BROADCAST = ALARM_BASE_NAME + ".SET_ALARM";

	public static final String SET_ALARM_EXTRA_INTENTWRAPPER = "intentwrapper";
	public static final String SET_ALARM_EXTRA_APPNAME = "appname";
	public static final String SET_ALARM_EXTRA_HOUR = "athour";
	public static final String SET_ALARM_EXTRA_MIN = "atmin";
	public static final String SET_ALARM_EXTRA_SEC = "atsec";
	public static final String SET_ALARM_EXTRA_DAYS = "daysoftheweek";
	public static final String EXTRA_ISONETIME = "isonetime";
	public static final String EXTRA_ISENABLED = "isenabled";

	// private static final String INTENT_ACTION_CHANGE_TEXT_ON_ALARM =
	// "edu.mit.android.test.alarmtest.AlarmTest_1.FROM_ALARM";

	// Setup the timing on the Intent for the alarm
	private static Intent SetupAlarmIntent(String broadcastType, int alarmInSeconds) {
		Intent intent = new Intent(broadcastType);
		Calendar future = Calendar.getInstance();
		future.setTimeInMillis(System.currentTimeMillis());
		future.add(Calendar.SECOND, alarmInSeconds);
		future.getTimeInMillis(); // Compute the changes we made using add
		int hour = future.get(Calendar.HOUR_OF_DAY);
		int min = future.get(Calendar.MINUTE);
		int sec = future.get(Calendar.SECOND);

		intent.putExtra(EXTRA_ISONETIME, true);
		intent.putExtra(SET_ALARM_EXTRA_DAYS, 0);
		intent.putExtra(SET_ALARM_EXTRA_HOUR, hour);
		intent.putExtra(SET_ALARM_EXTRA_MIN, min);
		intent.putExtra(SET_ALARM_EXTRA_SEC, sec);
		intent.putExtra(SET_ALARM_EXTRA_APPNAME, "Alarm 1");
		return intent;
	}

	// Get the intent needed to run an app after an alarm triggers the scheduler
	public static Intent BroadcastToSetRunViaSchedulerViaAlarm(String packageName, String className, int alarmInSeconds) {
		return BroadcastToSetRunViaSchedulerViaAlarm(packageName, className, alarmInSeconds, null);
	}

	// Get the intent needed to run an app after an alarm triggers the scheduler
	// Allow for pass through of an existing wrappedIntent
	public static Intent BroadcastToSetRunViaSchedulerViaAlarm(String packageName, String className, int alarmInSeconds, byte[] wrappedIntentByteArray) {
		if (wrappedIntentByteArray == null)
			if (Globals.IS_DEBUG)
				Log.d(TAG, "BroadcastToSetRunViaSchedulerViaAlarm");
			else if (Globals.IS_DEBUG)
				Log.d(TAG, "BroadcastToSetRunViaSchedulerViaAlarm (passing forward a wrapped intent)");

		Intent intent = SetupAlarmIntent(SET_ALARM_BROADCAST, alarmInSeconds);

		if (wrappedIntentByteArray == null) {
			// We need an IntentWrapper for the alarm to call when it goes off
			// that invokes scheduler
			IntentWrapper wrappedIntent = new IntentWrapper();
			// wrappedIntent.setAction(RUN_APP_BROADCAST);
			wrappedIntent.putExtra("type", "activity");
			wrappedIntent.putExtra("packageName", packageName);
			wrappedIntent.putExtra("className", className);
			// Call service with this broadcast
			wrappedIntent.setType(IntentWrapper.TYPE_BROADCAST);
			// wrappedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// Put the wrapped intent into our broadcast and send it:
			intent.putExtra(SET_ALARM_EXTRA_INTENTWRAPPER, IntentWrapper.marshallToByteArray(wrappedIntent));
		} else
			intent.putExtra(SET_ALARM_EXTRA_INTENTWRAPPER, wrappedIntentByteArray);

		return intent;

	}

	// Simply wakeup the scheduler to arbitrate
	public static Intent BroadcastToWakeupSchedulerViaAlarm(int alarmInSeconds) {
		if (Globals.IS_DEBUG)
			Log.d(TAG, "BroadcastToWakeupSchedulerViaAlarm");
		Intent intent = SetupAlarmIntent(SET_ALARM_BROADCAST, alarmInSeconds);

		// We need an IntentWrapper for the alarm to call when it goes off that
		// invokes scheduler
		IntentWrapper wrappedIntent = new IntentWrapper();
		wrappedIntent.putExtra("type", "wakeup");
		// wrappedIntent.setAction(ControllerService.ARBITRATE_NOW_BROADCAST);
		// Call service with this broadcast
		wrappedIntent.setType(IntentWrapper.TYPE_BROADCAST);
		// wrappedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// Put the wrapped intent into our broadcast and send it:
		intent.putExtra(SET_ALARM_EXTRA_INTENTWRAPPER, IntentWrapper.marshallToByteArray(wrappedIntent));

		return intent;
	}

	// Setup a program to run directly from an alarm (does not use scheduler)
	public static Intent BroadcastToSetRunViaAlarm(String packageName, String className, int alarmInSeconds) {
		return BroadcastToSetRunViaAlarm(packageName, className, alarmInSeconds, null);
	}

	// Setup a program to run directly from an alarm (does not use scheduler)
	// Can pass through a wrappedIntent
	public static Intent BroadcastToSetRunViaAlarm(String packageName, String className, int alarmInSeconds, byte[] wrappedIntentByteArray) {
		if (wrappedIntentByteArray == null)
			if (Globals.IS_DEBUG)
				Log.d(TAG, "BroadcastToSetRunViaAlarm");
			else if (Globals.IS_DEBUG)
				Log.d(TAG, "BroadcastToSetRunViaAlarm (passing forward a wrapped intent)");

		Intent intent = SetupAlarmIntent(SET_ALARM_BROADCAST, alarmInSeconds);

		if (wrappedIntentByteArray == null) {
			// No intentWrapper data sent, so create information
			IntentWrapper wrappedIntent = new IntentWrapper();

			// We need an IntentWrapper for the alarm to call when it goes off:
			wrappedIntent.setClassName(packageName, packageName + className);

			// Call startActivity with this intent
			wrappedIntent.setType(IntentWrapper.TYPE_ACTIVITY);
			wrappedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Put the wrapped intent into our broadcast and send it:
			intent.putExtra(SET_ALARM_EXTRA_INTENTWRAPPER, IntentWrapper.marshallToByteArray(wrappedIntent));
		} else
			intent.putExtra(SET_ALARM_EXTRA_INTENTWRAPPER, wrappedIntentByteArray);

		return intent;
	}

	// public static Intent BroadcastToSetRunTimerOld(int alarmInSeconds)
	// {
	// if (Constants.IS_DEBUG) Log.d(TAG,"BroadcastToSetRunTimer");
	//
	// Intent intent = new Intent(SET_ALARM_BROADCAST);
	// Calendar future = Calendar.getInstance();
	// future.setTimeInMillis(System.currentTimeMillis());
	// future.add(Calendar.SECOND, alarmInSeconds);
	// future.getTimeInMillis(); // Compute the changes we made using add
	// int hour = future.get(Calendar.HOUR_OF_DAY);
	// int min = future.get(Calendar.MINUTE);
	// int sec = future.get(Calendar.SECOND);
	//
	// intent.putExtra(SET_ALARM_EXTRA_DAYS, 0);
	// intent.putExtra(SET_ALARM_EXTRA_HOUR, hour);
	// intent.putExtra(SET_ALARM_EXTRA_MIN, min);
	// intent.putExtra(SET_ALARM_EXTRA_SEC, sec);
	// IntentWrapper wrappedIntent = new IntentWrapper();
	//
	// // Test to change text in this app
	// // Populate the broadcast to send:
	// intent.putExtra(SET_ALARM_EXTRA_APPNAME, "Alarm 1");
	//
	// // We need an IntentWrapper for the alarm to call when it goes off:
	// // wrappedIntent.setClassName(this.getPackageName(),
	// AlarmTest_1.class.getName());
	// //
	// wrappedIntent.setClassName("edu.mit.android.test.alarmtest","edu.mit.android.test.alarmtest.AlarmTest_1");
	// wrappedIntent.setClassName("edu.neu.android.wocketslib",
	// "edu.neu.android.wocketslib.level3pa.Level3PAActivity");
	//
	// wrappedIntent.setType(IntentWrapper.TYPE_ACTIVITY); // Call startActivity
	// with this intent
	// wrappedIntent.setAction(INTENT_ACTION_CHANGE_TEXT_ON_ALARM);
	// wrappedIntent.putExtra("new_title", String.format("Alarm %2d:%02d", hour,
	// min));
	// wrappedIntent.putExtra("new_text",
	// String.format("Integer ligula augue, aliquet quis consequat sed, aliquet eget risus.\n\nCurabitur at ante nec lectus feugiat consequat.\n\nAenean a nunc massa.\nDonec semper nulla ac sapien pharetra convallis. Nam congue gravida luctus. Cras quis ipsum nec dolor congue posuere sed et ipsum. Nulla facilisi. Ut quis lorem tellus, et volutpat felis. Maecenas dapibus justo vitae justo eleifend pretium non suscipit est. Fusce vitae odio ac magna laoreet convallis sit amet eu orci. Etiam lectus lectus, tincidunt sed auctor egestas, pharetra ac felis. Nulla lacinia malesuada pharetra. Nam ullamcorper pretium diam eu fringilla.\nSuspendisse et erat lacus, sit amet eleifend lectus. Sed bibendum facilisis diam vitae sodales. Ut ac semper orci. Aliquam ac lorem sit amet sem posuere fringilla blandit et nisi. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vivamus ullamcorper suscipit dapibus. Nulla facilisi. Vestibulum ornare rhoncus risus non dictum. Suspendisse tempus molestie nunc at semper. Etiam vel erat elit, in egestas magna. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla vulputate fringilla nisi eget porttitor. Etiam fermentum convallis massa, non congue mi tristique ac. Nam eget dolor tortor."));
	// wrappedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// // Intent intent = new Intent(SET_ALARM_BROADCAST);
	// // // Compute the hour/minutes to use:
	// // Calendar future = Calendar.getInstance();
	// // future.setTimeInMillis(System.currentTimeMillis());
	// // future.add(Calendar.SECOND, alarmInSeconds);
	// // future.getTimeInMillis(); // Compute the changes we made using add
	// // int hour = future.get(Calendar.HOUR_OF_DAY);
	// // int min = future.get(Calendar.MINUTE);
	// // int sec = future.get(Calendar.SECOND);
	//
	// // intent.putExtra(SET_ALARM_EXTRA_DAYS, 0);
	// // intent.putExtra(SET_ALARM_EXTRA_HOUR, hour);
	// // intent.putExtra(SET_ALARM_EXTRA_MIN, min);
	// // intent.putExtra(SET_ALARM_EXTRA_SEC, sec);
	// // IntentWrapper wrappedIntent = new IntentWrapper();
	// //
	// // // Test sending an alarm to launch the PA app
	// // // Populate the broadcast to send:
	// // intent.putExtra(SET_ALARM_EXTRA_APPNAME, "Run PA App");
	// //
	// // // We need an IntentWrapper for the alarm to call when it goes off:
	// // wrappedIntent.setClassName("edu.neu.android.wocketslib",
	// "edu.neu.android.wocketslib.level3pa.Level3PAActivity");
	// // wrappedIntent.setType(IntentWrapper.TYPE_ACTIVITY); // Call
	// startActivity with this intent
	// // //wrappedIntent.setAction(INTENT_ACTION_CHANGE_TEXT_ON_ALARM);
	// // //wrappedIntent.putExtra("new_title", String.format("Alarm %2d:%02d",
	// hour, min));
	// // //wrappedIntent.putExtra("new_text",
	// String.format("Integer ligula augue, aliquet quis consequat sed, aliquet eget risus.\n\nCurabitur at ante nec lectus feugiat consequat.\n\nAenean a nunc massa.\nDonec semper nulla ac sapien pharetra convallis. Nam congue gravida luctus. Cras quis ipsum nec dolor congue posuere sed et ipsum. Nulla facilisi. Ut quis lorem tellus, et volutpat felis. Maecenas dapibus justo vitae justo eleifend pretium non suscipit est. Fusce vitae odio ac magna laoreet convallis sit amet eu orci. Etiam lectus lectus, tincidunt sed auctor egestas, pharetra ac felis. Nulla lacinia malesuada pharetra. Nam ullamcorper pretium diam eu fringilla.\nSuspendisse et erat lacus, sit amet eleifend lectus. Sed bibendum facilisis diam vitae sodales. Ut ac semper orci. Aliquam ac lorem sit amet sem posuere fringilla blandit et nisi. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Vivamus ullamcorper suscipit dapibus. Nulla facilisi. Vestibulum ornare rhoncus risus non dictum. Suspendisse tempus molestie nunc at semper. Etiam vel erat elit, in egestas magna. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla vulputate fringilla nisi eget porttitor. Etiam fermentum convallis massa, non congue mi tristique ac. Nam eget dolor tortor."));
	// // wrappedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// // Put the wrapped intent into our broadcast and send it:
	// intent.putExtra(SET_ALARM_EXTRA_INTENTWRAPPER,
	// IntentWrapper.marshallToByteArray(wrappedIntent));
	// return intent;
	// }

	// Example of how you call BroadcastToRunActivity from any App.
	// if (Constants.IS_DEBUG)
	// Log.d("Try to schedule another app from Tutorial via service.");
	// Intent intent =
	// Scheduler.BroadcastToRunActivity("edu.neu.android.wocketslib",
	// ".level3pa.Level3PAActivity");
	// sendBroadcast(intent);

	// public static Intent BroadcastToRunActivity(String packageName, String
	// className)
	// {
	// if (Constants.IS_DEBUG)
	// Log.d(TAG,"BroadcastToRunActivity via the service");
	// Intent intent = new Intent(RUN_APP_BROADCAST);
	// intent.putExtra("packageName", packageName);
	// intent.putExtra("className", className);
	// return intent;
	// }

}
