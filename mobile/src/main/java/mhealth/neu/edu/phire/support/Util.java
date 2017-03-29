package mhealth.neu.edu.phire.support;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.AlarmManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.WocketsUtil;
import mhealth.neu.edu.phire.services.BackupMinuteService;
import mhealth.neu.edu.phire.services.MinuteService;

/**
 * @author Binod Thapa Chhetry
 */
public class Util extends WocketsUtil {

    private static final String TAG = "Util";

    /**
     * Set the minute service alarm
     */
    public static void setMinuteServiceAlarmFromMinuteService(Context context) {
        Log.i(TAG, "Inside setMinuteServiceAlarmFromMinuteService", context);
        // By default set the minute service alarm setter to MinuteService.
        DataManager.setMinuteServiceAlarmSetter(context, MinuteService.TAG);
        long minuteServiceLastRun = DataManager.getMinuteServiceLastRun(context);
        Log.i(TAG, "MinuteService last run at - " + DateTime.getTimestampString(minuteServiceLastRun), context);
        long minuteServiceNextRun = minuteServiceLastRun + DateTime.MINUTES_1_IN_MILLIS;
        Log.i(TAG, "MinuteService next run at - " + DateTime.getTimestampString(minuteServiceNextRun), context);
        Intent minuteServiceIntent = new Intent(context, MinuteService.class);
        PendingIntent minuteServicePendingIntent = PendingIntent.getService(context, 1, minuteServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager.setOneTimeAlarm(context, minuteServiceNextRun, minuteServicePendingIntent);
    }

    /**
     * Set the minute service alarm
     */
    public static void setBackupMinuteServiceAlarm(Context context) {
        Log.i(TAG, "Inside setBackupMinuteServiceAlarm", context);
        long backupMinuteServiceLastRun = DataManager.getBackupMinuteServiceLastRun(context);
        Log.i(TAG, "BackupMinuteService last run in long "+ backupMinuteServiceLastRun, context);
        Log.i(TAG, "BackupMinuteService last run at - " + DateTime.getTimestampString(backupMinuteServiceLastRun), context);
        long minuteServiceNextRun = (backupMinuteServiceLastRun < DateTime.getCurrentTimeInMillis() - DateTime.MINUTES_1_IN_MILLIS) ?
                DateTime.getCurrentTimeInMillis() :
                backupMinuteServiceLastRun + DateTime.MINUTES_1_IN_MILLIS;
        Log.i(TAG, "BackupMinuteService next run at - " + DateTime.getTimestampString(minuteServiceNextRun), context);
        Intent backupMinuteServiceIntent = new Intent(context, BackupMinuteService.class);
        PendingIntent backupMinuteServicePendingIntent = PendingIntent.getService(context, 2, backupMinuteServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager.setRepeatingAlarm(context, minuteServiceNextRun, DateTime.MINUTES_1_IN_MILLIS, backupMinuteServicePendingIntent);
    }
}
