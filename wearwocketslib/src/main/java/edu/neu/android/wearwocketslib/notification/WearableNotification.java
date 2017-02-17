package edu.neu.android.wearwocketslib.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.HashMap;

import edu.neu.android.wearwocketslib.R;

/**
 * Created by qutang on 8/5/15.
 */
public class WearableNotification {

    public static final int END_OF_DAY_ANNOTATION_NOTIFICATION = 0;
    public static final int SURVEY_NOTIFICATION = 1;
    public static final int LOST_CONNECTION_NOTIFICATION = 2;

    protected static NotificationManagerCompat mNotificationManager;
    protected int notificationId;
    protected Notification notification;
    private boolean isShowing = false;
    protected static HashMap<Integer, WearableNotification> notificationMap = new HashMap<>();


    public WearableNotification(String title, String message, int icon, boolean onGoing, int id, Context context){
        if(mNotificationManager == null) mNotificationManager = NotificationManagerCompat.from(context);
        notificationId = id;
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(null)
                        .setOngoing(onGoing)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50});

        notification =  notificationBuilder.build();
        notificationMap.put(id, this);
    }

    protected WearableNotification(){};

    public void show(){
        mNotificationManager.notify(notificationId, notification);
        isShowing = true;
    }

    public static void cancel(int id){
        if(mNotificationManager != null){
            mNotificationManager.cancel(id);
            WearableNotification notification = notificationMap.get(id);
            if(notification != null) {
                notification.isShowing = false;
            }
            notificationMap.remove(id);
        }
    }

    public static boolean isShowing(int id){
        WearableNotification notification = notificationMap.get(id);
        if(notification != null){
            return notification.isShowing;
        }else{
            return false;
        }
    }
}
