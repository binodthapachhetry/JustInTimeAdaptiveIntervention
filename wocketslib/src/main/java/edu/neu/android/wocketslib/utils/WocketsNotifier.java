package edu.neu.android.wocketslib.utils;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.wocketsnews.NewsViewerActivity;

public class WocketsNotifier {
	
	public static final String LAUNCHED_FROM_NOTIFICATION_KEY = "LAUNCHED_FROM_NOTIFICATION_KEY";
	public static final String NOTIFICATION_FIRE_DATE_KEY = "NOTIFICATION_FIRE_DATE_KEY";
	
	public static void notifyStatusBar(Context c, String msg, int id) {

		int icon = R.drawable.w_ocketspolygonalyellow24x38;
		String ns = Context.NOTIFICATION_SERVICE;
		
//		Intent onClickEvent = new Intent(c, WOCKETSApplication.class); 
		Intent onClickEvent = new Intent(c, NewsViewerActivity.class);//TODO Must fix!!
		addNotificationLaunchDetailsToIntent(onClickEvent);

		PendingIntent pending = PendingIntent.getActivity(c, id, onClickEvent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews contentView = new RemoteViews(c.getPackageName(),
				R.layout.wockets_notification);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, "Wockets", when);
		notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL);
		contentView.setTextViewText(R.id.notify_msg, spliteForNotification(msg)[0]);
		contentView.setTextViewText(R.id.notify_msg2, spliteForNotification(msg)[1]);
		contentView.setImageViewResource(R.id.notify_icon, icon);

		notification.contentView = contentView;
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.contentIntent = pending;

		// noticed.setLatestEventInfo(c, text, desc, pending);
		NotificationManager noticedManager = (NotificationManager) c
				.getSystemService(ns);
		noticedManager.notify(id, notification);
	}


	public static void addNotificationLaunchDetailsToIntent(Intent onClickEvent) {
		onClickEvent.putExtra(WocketsNotifier.LAUNCHED_FROM_NOTIFICATION_KEY, true);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		onClickEvent.putExtra(WocketsNotifier.NOTIFICATION_FIRE_DATE_KEY, f.format(new GregorianCalendar().getTime()));
	}
	
	
	public static void notifyStatusBarCustomized(Context c, int id, Class<?> cls, int icon, String title, String msg) {

		String ns = Context.NOTIFICATION_SERVICE;
		
//		Intent onClickEvent = new Intent(c, WOCKETSApplication.class); 
		Intent onClickEvent = new Intent(c, cls);//TODO Must fix!!
		addNotificationLaunchDetailsToIntent(onClickEvent);

		PendingIntent pending = PendingIntent.getActivity(c, id, onClickEvent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews contentView = new RemoteViews(c.getPackageName(),
				R.layout.wockets_notification);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, "Wockets", when);
		notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL);
		contentView.setTextViewText(R.id.notify_msg, title);
		contentView.setTextViewText(R.id.notify_msg2, msg);
		contentView.setImageViewResource(R.id.notify_icon, icon);

		notification.contentView = contentView;
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.contentIntent = pending;

		// noticed.setLatestEventInfo(c, text, desc, pending);
		NotificationManager noticedManager = (NotificationManager) c
				.getSystemService(ns);
		noticedManager.notify(id, notification);
	}
	
	
	public static boolean checkTextLength(String msg){
		return msg.length() < 100;
	}
	
	private static String[] spliteForNotification(String msg){
		String[] msgs = new String[2];
		if(msg.length() < 68){
			msgs[0] = msg;
			msgs[1] = "";
		}
		else{
			msgs[0] = "";
			msgs[1] = "";
			String[] words = msg.split(" ");
			for (String string : words) {
				msgs[0] += string;
				if(msgs[0].length() > 74)
					break;
				msgs[0] += " ";
				msgs[1] = msgs[0];
			}
			msgs[0] = msgs[1];
			msgs[1] = msg.substring(msgs[0].length());
		}
		return msgs;
	}
	
}
