package edu.neu.android.wocketslib.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.HashMap;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.sensorstatus.StatusScreenActivity;
import edu.neu.android.wocketslib.emasurvey.SurveyActivity;

public class PhoneNotifier {

	private static NotificationManager mNM;
	private static NotificationManagerCompat mNMC;

	// Defines used for notifications
	public static final int READING_NOTIFICATION = 0;
	public static final int UPLOADING_NOTIFICATION = 1;
	public static final int SURVEY_NOTIFICATION = 2;
	public static final int END_OF_DAY_ANNOTATION_NOTIFICATION = 3;
	public static final int LOST_CONNECTION_NOTIFICAITON = 4;
	public static final int UPLOADING_HALT_NOTIFICATION = 5;

	// private static final int STILL_NOTIFICATION_ID = 1;
	// private static final int EMOTION_NOTIFICATION_ID = 2;

	public static int sIcon  = R.drawable.w_ocketspolygonalyellow24x38;
	public static int sArgb  = 0x0000ff;
	public static int sOnMs  = 1000;
	public static int sOffMs = 0;

	public static OnNotificationCreated sHook = null;

	private static HashMap<Integer, Notification> notificationHashMap = new HashMap<>();

	public static void showNotification(Context aContext, Intent mIntent, String title, String textToShow, int icon, boolean isOngoing, int id){
		if(mNMC == null){
			mNMC = NotificationManagerCompat.from(aContext);
		}
		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		long[] vibration = new long[]{};
		PendingIntent contentIntent = null;
		if(mIntent != null) {
			contentIntent = PendingIntent.getActivity(aContext, 1, mIntent, 0);
		}
		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(aContext);
		nBuilder.setVibrate(vibration)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setTicker(textToShow)
				.setSound(sound)
				.setOngoing(isOngoing)
				.setContentText(textToShow)
				.setContentTitle(title)
				.setContentIntent(contentIntent)
				.setLights(sArgb, sOnMs, sOffMs)
				.setSmallIcon(icon);

		Notification notification = nBuilder.build();

		if (sHook != null) {
			notification = sHook.onNotificationCreated(notification);
		}

		// Send the notification.
		if (mNMC != null) {
			mNMC.notify(id, notification);
			notificationHashMap.put(id, notification);
		}

	}

	public static void showLostConnectionNotification(String aTAG, Context aContext){
		showNotification(aContext, null, "Lost connection with watch", "Please check and make sure phone and watch are connected", R.drawable.ic_launcher_wallpaper, true, LOST_CONNECTION_NOTIFICAITON);
	}

	/**
	 * Show normal notification
	 * @param aContext
	 * @param mIntent
	 * @param textToShow
	 */
	public static void showNormalNotification(Context aContext, Intent mIntent, String textToShow) {
		Log.e("versionchecker", "phonenotification");
		mNM = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(aContext, 1, mIntent, 0);
		Notification notification = new Notification(sIcon, textToShow, System.currentTimeMillis());
		notification.flags =  Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(aContext, aContext.getString(R.string.app_name), textToShow, contentIntent);
		// Send the notification.
		if (mNM != null)
			mNM.notify(3, notification);
		else
			Log.e(aContext.getString(R.string.app_name), "Could not get notification manager.");
	}


	/**
	 * Show a notification while this service is running. Enables a blue
	 * notification LED
	 */
	public static void showReadingNotification(String aTAG, Context aContext) {
		mNM = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// CharSequence text =
		// aContext.getString(Globals.MSG_READING_SENSOR_DATA_NOTIFICATION); //
		// R.string.readingNotification);
		//CharSequence text = aContext.getString(R.string.readingNotification);
		CharSequence text = Globals.MSG_READING_SENSOR_DATA_NOTIFICATION;

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(sIcon, text, System.currentTimeMillis());
		notification.ledARGB  = sArgb;
		notification.ledOnMS  = sOnMs;
		notification.ledOffMS = sOffMs;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;

		Intent mainIntent = new Intent(aContext, StatusScreenActivity.class); // TODO fix
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(aContext, 0, mainIntent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(aContext, aContext.getString(R.string.app_name), text, contentIntent);

		if (sHook != null) {
			notification = sHook.onNotificationCreated(notification);
		}

		// Send the notification.
		if (mNM != null)
			mNM.notify(READING_NOTIFICATION, notification);
		else
			Log.e(aTAG, "Could not get notification manager.");
	}

	public static void showSurveyNotification(String aTAG, Context aContext, long[] vibratePattern){
		mNM = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);


		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(sIcon, "Time for survey", System.currentTimeMillis());
		notification.ledARGB  = sArgb;
		notification.ledOnMS  = sOnMs;
		notification.ledOffMS = sOffMs;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
		notification.vibrate = vibratePattern;
		notification.priority = Notification.PRIORITY_HIGH;


		Intent mainIntent = new Intent(aContext, SurveyActivity.class); // TODO fix
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(aContext, 0, mainIntent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(aContext, "Time for survey", "Please Check your phone to answer", null);

		if (sHook != null) {
			notification = sHook.onNotificationCreated(notification);
		}

		// Send the notification.
		if (mNM != null)
			mNM.notify(SURVEY_NOTIFICATION, notification);
		else
			Log.e(aTAG, "Could not get notification manager.");
	}

	/**
	 * Show a notification while this service is running. Enables a blue
	 * notification LED
	 */
	public static void showUploadingNotification(String aTAG, String message, Context aContext) {
		mNM = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// CharSequence text =
		// aContext.getString(Globals.MSG_READING_SENSOR_DATA_NOTIFICATION); //
		// R.string.readingNotification);
		//CharSequence text = aContext.getString(R.string.readingNotification);
		CharSequence text = Globals.MSG_UPLOADING_DATA_NOTIFICATION;

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(sIcon, message, System.currentTimeMillis());
//		notification.ledARGB  = sArgb;
//		notification.ledOnMS  = sOnMs;
//		notification.ledOffMS = sOffMs;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		Intent mainIntent = new Intent(aContext, StatusScreenActivity.class); // TODO fix
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(aContext, 0, mainIntent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(aContext, aContext.getString(R.string.app_name), text, contentIntent);

		if (sHook != null) {
			notification = sHook.onNotificationCreated(notification);
		}

		// Send the notification.
		if (mNM != null)
			mNM.notify(UPLOADING_NOTIFICATION, notification);
		else
			Log.e(aTAG, "Could not get notification manager.");
	}

	public static void showUploadingHaltNotification(String aTAG, String message, Context aContext) {
		mNM = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// CharSequence text =
		// aContext.getString(Globals.MSG_READING_SENSOR_DATA_NOTIFICATION); //
		// R.string.readingNotification);
		//CharSequence text = aContext.getString(R.string.readingNotification);
		CharSequence text = "Uploading halted, please find time connect to WIFI (under WIFI only mode)";

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(sIcon, message, System.currentTimeMillis());
//		notification.ledARGB  = sArgb;
//		notification.ledOnMS  = sOnMs;
//		notification.ledOffMS = sOffMs;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		Intent mainIntent = new Intent(aContext, StatusScreenActivity.class); // TODO fix
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(aContext, 0, mainIntent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(aContext, aContext.getString(R.string.app_name), text, contentIntent);

		if (sHook != null) {
			notification = sHook.onNotificationCreated(notification);
		}

		// Send the notification.
		if (mNM != null)
			mNM.notify(UPLOADING_HALT_NOTIFICATION, notification);
		else
			Log.e(aTAG, "Could not get notification manager.");
	}


	public static void cancel(int aNotificationID) {
		if (mNM != null) {
			mNM.cancel(aNotificationID);
			mNM = null;
		}
		if (mNMC != null){
			mNMC.cancel(aNotificationID);
			notificationHashMap.remove(aNotificationID);
			mNMC = null;
		}
	}

	public static boolean isShowing(int aNoticiationID){
		return notificationHashMap.get(aNoticiationID) != null;
	}

	public static void setIcon(int icon) {
		sIcon = icon;
	}

	public static void setLights(int argb, int onMs, int offMs) {
		sArgb  = argb;
		sOnMs  = onMs;
		sOffMs = offMs;
	}

	public interface OnNotificationCreated {
		Notification onNotificationCreated(Notification notification);
	}
	// /**
	// * Show a notification for the emotional event. No LED is shown, but the
	// phone will vibrate
	// * and play the default notification sound
	// */
	// private void showEmotionNotification() {
	// CharSequence text = getString(R.string.emotionNotification);
	//
	// // Set the icon, scrolling text and timestamp
	// Notification notification = new Notification(R.drawable.emotion, text,
	// System.currentTimeMillis());
	//
	// notification.defaults |= Notification.DEFAULT_SOUND;
	// notification.flags |= Notification.FLAG_AUTO_CANCEL;
	//
	// //Three short vibrations with a longer gap between them
	// long[] vibrate = {0,100,200,100,200,100};
	// notification.vibrate = vibrate;
	//
	// Intent mainIntent = new Intent(this, StatusScreenActivity.class);
	// mainIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP |
	// Intent.FLAG_ACTIVITY_SINGLE_TOP);
	//
	// // The PendingIntent to launch our activity if the user selects this
	// notification
	// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	// mainIntent, 0);
	//
	// // Set the info for the views that show in the notification panel.
	// notification.setLatestEventInfo(getApplicationContext(),
	// getString(R.string.emotionNotificationDetail),
	// text, contentIntent);
	//
	// // Send the notification.
	// mNM.notify(EMOTION_NOTIFICATION_ID, notification);
	// }
	//
	// /**
	// * Show a notification for stillness. No LED is shown, but the phone will
	// vibrate
	// * and play the default notification sound
	// */
	// private void showStillNotification() {
	// CharSequence text = getString(R.string.inactivityNotification);
	//
	// // Set the icon, scrolling text and timestamp
	// Notification notification = new Notification(R.drawable.sitting, text,
	// System.currentTimeMillis());
	//
	// notification.defaults |= Notification.DEFAULT_SOUND;
	// notification.flags |= Notification.FLAG_AUTO_CANCEL;
	//
	// //Three short vibrations with a small gap between them
	// long[] vibrate = {0,100,100,100,100,100};
	// notification.vibrate = vibrate;
	//
	// Intent mainIntent = new Intent(this, StatusScreenActivity.class);
	// mainIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP |
	// Intent.FLAG_ACTIVITY_SINGLE_TOP);
	//
	// // The PendingIntent to launch our activity if the user selects this
	// notification
	// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	// mainIntent, 0);
	//
	// // Set the info for the views that show in the notification panel.
	// notification.setLatestEventInfo(getApplicationContext(),
	// getString(R.string.inactivityNotificationDetail),
	// text, contentIntent);
	//
	// // Send the notification.
	// mNM.notify(STILL_NOTIFICATION_ID, notification);
	// }

}
