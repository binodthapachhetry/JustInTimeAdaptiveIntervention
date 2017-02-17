package edu.neu.android.wearwocketslib.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qutang on 11/23/15.
 */
public class LostConnectionWearableNotification extends WearableNotification {

    private List extras = new ArrayList<>();

    private void buildExtras(Context context){
// Extra pages of information for the notification that will
// only appear on the wearable
        NotificationCompat.BigTextStyle extraPageStyle = new NotificationCompat.BigTextStyle();
        extraPageStyle.setBigContentTitle("Please restart your watch")
                    .bigText("Try switch off and on bluetooth on the phone and then restart your watch");
        Notification extraPageNotification = new NotificationCompat.Builder(context)
                    .setStyle(extraPageStyle)
                    .build();
        extras.add(extraPageNotification);
    }

    public LostConnectionWearableNotification(String title, String message, int icon, boolean onGoing, int id, Context context) {
        if(mNotificationManager == null) mNotificationManager = NotificationManagerCompat.from(context);
        buildExtras(context);
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
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50}).extend(new NotificationCompat.WearableExtender().addPages(extras));

        notification =  notificationBuilder.build();
        notificationMap.put(id, this);
    }
}
