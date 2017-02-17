package edu.neu.mhealth.android.wockets.library.managers;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;

/**
 * AlarmManager class to use the best function call based on the OS version.
 *
 * <p>
 *     Known Issues:
 *     <ul>
 *         <li>Nexus 6 - Minimum intervalMillis 60 seconds</li>
 *     </ul>
 *
 * </p>
 */
public class AlarmManager {

    private static android.app.AlarmManager alarmManager;

    /**
     * Schedule an alarm. If there is already an alarm scheduled for the same IntentSender,
     * that previous alarm will first be canceled.
     *
     * @param context           {@link Context}
     * @param triggerAtMillis   long: time in milliseconds that the alarm should go off, in System.currentTimeMillis() (wall clock time in UTC)
     * @param pendingIntent     {@link PendingIntent}
     */
    public static void setOneTimeAlarm(Context context, long triggerAtMillis, PendingIntent pendingIntent) {
        alarmManager = (android.app.AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= M) {
            alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, triggerAtMillis, pendingIntent);
            return;
        }
        if (Build.VERSION.SDK_INT >= KITKAT) {
            alarmManager.setExact(RTC_WAKEUP, triggerAtMillis, pendingIntent);
            return;
        }
        alarmManager.set(RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    /**
     * Schedule a repeating alarm. If there is already an alarm scheduled for the same IntentSender,
     * it will first be canceled.
     *
     * If an alarm is delayed (by system sleep, for example, for non _WAKEUP alarm types),
     * a skipped repeat will be delivered as soon as possible. After that, future alarms will
     * be delivered according to the original schedule; they do not drift over time.
     *
     * For example, if you have set a recurring alarm for the top of every hour but the phone
     * was asleep from 7:45 until 8:45, an alarm will be sent as soon as the phone awakens,
     * then the next alarm will be sent at 9:00.
     *
     * If your application wants to allow the delivery times to drift in order to guarantee that
     * at least a certain time interval always elapses between alarms, then the approach to take
     * is to use one-time alarms, scheduling the next one yourself when handling each alarm delivery.
     *
     * @param context           {@link Context}
     * @param triggerAtMillis   long: time in milliseconds that the alarm should first go off, in System.currentTimeMillis() (wall clock time in UTC)
     * @param intervalMillis    long: interval in milliseconds between subsequent repeats of the alarm.
     *
     */
    public static void setRepeatingAlarm(Context context, long triggerAtMillis, long intervalMillis, PendingIntent pendingIntent) {
        alarmManager = (android.app.AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
    }
}
