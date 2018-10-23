package edu.neu.mhealth.android.wockets.library.managers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.Log;

import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_LOW;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_MAX;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_MIN;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_NONE;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;

/**
 * @author Dharam Maniar
 */
public class NotificationManager {

    private static final String TAG = "NotificationManager";

	public static final int NOTIFICATION_ID_MINUTE_SERVICE = 34001;
	private static final int NOTIFICATION_ID_SURVEY = 34002;
	private static final int NOTIFICATION_ID_UPLOAD_COUNT = 34003;
	private static final int NOTIFICATION_ID_FEEDBACK = 34004;

	private static final String CHANNEL_NAME = "ALWAYS_ON";
	private static final String CHANNEL_DESCRIPTION = "ALWAYS_ON";
	private static final int OREO_SDK_INT = 25;
	private static final boolean TRUE = true;


    public static void logNotificationStatus(Context context) {
        Log.i(TAG,
                "AreNotificationsEnabled - " + areNotificationsEnabled(context) +
                " - Importance - " + getImportance(context),
                context
        );
    }

	private static boolean areNotificationsEnabled(Context context) {
		return NotificationManagerCompat.from(context).areNotificationsEnabled();
	}

    private static String getImportance(Context context) {
        int notificationImportance = NotificationManagerCompat.from(context).getImportance();
        String importance = "";
        switch (notificationImportance) {
            case IMPORTANCE_DEFAULT:
                importance = "Default";
                break;
            case IMPORTANCE_HIGH:
                importance = "High";
                break;
            case IMPORTANCE_LOW:
                importance = "Low";
                break;
            case IMPORTANCE_MAX:
                importance = "Max";
                break;
            case IMPORTANCE_MIN:
                importance = "Min";
                break;
            case IMPORTANCE_NONE:
                importance = "None";
                break;
            case IMPORTANCE_UNSPECIFIED:
                importance = "Unspecified";
                break;
        }
        return importance;
    }

	public static void showMinuteServiceNotification(Context mContext, String title, String text, int notificationIcon) {
		Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
				notificationIcon);
//		NotificationCompat.Builder mBuilder =
//				new NotificationCompat.Builder(mContext)
//						.setSmallIcon(notificationIcon)
//						.setLargeIcon(icon)
//						.setContentTitle(title)
//						.setContentText(text)
//						.setAutoCancel(true);

//		Notification notification =
//				new Notification.Builder(mContext)
//						.setSmallIcon(notificationIcon)
//						.setLargeIcon(icon)
//						.setContentTitle(title)
//						.setContentText(text)
//						.setAutoCancel(true)
//						.build();

		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "MINUTE_SERVICE";
			String description = "MINUTE_SERVICE_DESC";
			String channel_id = "MINUTE_SERVICE_ID";
			int importance = android.app.NotificationManager.IMPORTANCE_LOW;
			NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
			channel.setDescription(description);
			channel.setBypassDnd(true);
			channel.setLightColor(Color.GREEN);
			Notification notification =
					new Notification.Builder(mContext)
							.setSmallIcon(notificationIcon)
							.setLargeIcon(icon)
							.setContentTitle(title)
							.setContentText(text)
							.setAutoCancel(false)
							.setChannelId(channel_id)
							.build();

			mNotificationManager.createNotificationChannel(channel);
			mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, notification);

		}else{
			Notification notification =
					new Notification.Builder(mContext)
							.setSmallIcon(notificationIcon)
							.setLargeIcon(icon)
							.setContentTitle(title)
							.setContentText(text)
							.setAutoCancel(false)
							.build();
			mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, notification);


		}
//		mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, mBuilder.build());
//		mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, notification);

	}

	public static void showUploadCountNotification(Context mContext, String title, String text) {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(mContext)
						.setContentTitle(title)
						.setContentText(text)
                        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
						.setAutoCancel(true);

		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID_UPLOAD_COUNT, mBuilder.build());
	}

	public static void showFeedbackNotification(Context mContext, String title, String text, int notificationIcon, Uri soundUri, long[] vibrationPattern) {
		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "FEEDBACK_SERVICE";
			String description = "FEEDBACK_SERVICE_DESC";
			String channel_id = "FEEDBACK_SERVICE_ID";
			int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
			channel.setDescription(description);
			channel.setBypassDnd(true);
			channel.setLightColor(Color.GREEN);
			Notification.Builder notification =
					new Notification.Builder(mContext)
							.setContentTitle(title)
							.setContentText(text)
							.setSmallIcon(notificationIcon)
							.setAutoCancel(true)
							.setChannelId(channel_id);
//							.build();

			mNotificationManager.createNotificationChannel(channel);
			if (soundUri != null) {
				notification.setSound(soundUri);
			}
			if (vibrationPattern.length > 1) {
				notification.setVibrate(vibrationPattern);
			}

			mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, notification.build());

		}else{
			Notification.Builder notification =
					new Notification.Builder(mContext)
							.setContentTitle(title)
							.setContentText(text)
							.setSmallIcon(notificationIcon)
							.setAutoCancel(true);

			if (soundUri != null) {
				notification.setSound(soundUri);
			}
			if (vibrationPattern.length > 1) {
				notification.setVibrate(vibrationPattern);
			}
			mNotificationManager.notify(NOTIFICATION_ID_FEEDBACK, notification.build());


		}

//		NotificationCompat.Builder mBuilder =
//				new NotificationCompat.Builder(mContext)
//						.setContentTitle(title)
//						.setContentText(text)
//						.setSmallIcon(notificationIcon)
//						.setAutoCancel(true);

//		if (soundUri != null) {
//			mBuilder.setSound(soundUri);
//		}
//		if (vibrationPattern.length > 1) {
//			mBuilder.setVibrate(vibrationPattern);
//		}
//
//		android.app.NotificationManager mNotificationManager =
//				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.notify(NOTIFICATION_ID_FEEDBACK, mBuilder.build());
	}

	public static void showFeedbackNotificationNoVib(Context mContext, String title, String text, int notificationIcon, Uri soundUri) {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(mContext)
						.setContentTitle(title)
						.setContentText(text)
						.setSmallIcon(notificationIcon)
						.setAutoCancel(true);

		if (soundUri != null) {
			mBuilder.setSound(soundUri);
		}

		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID_FEEDBACK, mBuilder.build());
	}

	public static void showPromptNotification(Context mContext, String title, String text, int notificationIcon, Uri soundUri, long[] vibrationPattern, PendingIntent pendingIntent) {
		Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
				notificationIcon);

		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "PROMPT_SERVICE";
			String description = "PROMPT_SERVICE_DESC";
			String channel_id = "PROMPT_SERVICE_ID";
			int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
			channel.setDescription(description);
			channel.setBypassDnd(true);
			channel.setLightColor(Color.GREEN);
			Notification.Builder notification =
					new Notification.Builder(mContext)
							.setSmallIcon(notificationIcon)
							.setLargeIcon(icon)
							.setContentTitle(title)
							.setContentText(text)
							.setAutoCancel(true)
							.setContentIntent(pendingIntent)
							.setChannelId(channel_id);
//							.build();

			mNotificationManager.createNotificationChannel(channel);
			if (soundUri != null) {
				notification.setSound(soundUri);
			}
			if (vibrationPattern.length > 1) {
				notification.setVibrate(vibrationPattern);
			}

			mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, notification.build());

		}else{
			Notification.Builder notification =
					new Notification.Builder(mContext)
							.setSmallIcon(notificationIcon)
							.setLargeIcon(icon)
							.setContentTitle(title)
							.setContentText(text)
							.setAutoCancel(true)
							.setContentIntent(pendingIntent);

			if (soundUri != null) {
				notification.setSound(soundUri);
			}
			if (vibrationPattern.length > 1) {
				notification.setVibrate(vibrationPattern);
			}
			mNotificationManager.notify(NOTIFICATION_ID_SURVEY, notification.build());


		}


//		NotificationCompat.Builder mBuilder =
//				new NotificationCompat.Builder(mContext)
//						.setSmallIcon(notificationIcon)
//						.setLargeIcon(icon)
//						.setContentTitle(title)
//						.setContentText(text)
//						.setAutoCancel(true)
//						.setContentIntent(pendingIntent);
//
//		if (soundUri != null) {
//			mBuilder.setSound(soundUri);
//		}
//		if (vibrationPattern.length > 0) {
//			mBuilder.setVibrate(vibrationPattern);
//		}
//
//		android.app.NotificationManager mNotificationManager =
//				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.notify(NOTIFICATION_ID_SURVEY, mBuilder.build());
	}

	public static void clearSurveyNotification(Context mContext) {
		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID_SURVEY);
	}

	public static void clearFeedbackNotification(Context mContext) {
		android.app.NotificationManager mNotificationManager =
				(android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID_FEEDBACK);
	}

//	public static Notification getAlwaysOnServiceNotification(Context context, int notificationIcon, String text) {
//		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
//				notificationIcon);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//			return new Notification.Builder(context)
//					.setContentTitle(DataManager.getStudyName(context))
//					.setContentText(text)
//					.setSmallIcon(notificationIcon)
//					.setLargeIcon(icon)
//					.build();
//
//		} else {
//			return new Notification.Builder(context)
//					.setContentTitle(DataManager.getStudyName(context))
//					.setContentText(text)
//					.setSmallIcon(notificationIcon)
//					.setLargeIcon(icon)
//					.build();
//		}
//	}

}
