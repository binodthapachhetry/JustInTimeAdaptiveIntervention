package edu.neu.android.wearwocketslib.core.repeatedwakefulservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by qutang on 8/20/15.
 */
public class WearableWakefulBroadcastAlarm {

    public static final String TAG = "WearableWakefulBroadcastAlarm";

    private Intent broadcastIntent;
    private PendingIntent alarmIntent;
    private Context mContext;
    private String fromWhere;

    private Logger logger = null;

    public static final int REQUEST_CODE = 16852;

    public WearableWakefulBroadcastAlarm(Context context, String from){
        logger = new Logger(TAG);
        mContext = context;
        fromWhere = from;
        broadcastIntent = new Intent(context,
                WearableWakefulBroadcastReceiver.class);
        broadcastIntent.putExtra("From", from);
        broadcastIntent.setAction("WAKEFUL_SERVICE_ALARM");
        alarmIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, broadcastIntent, 0);
    }

    public void setAlarm() {
        long lastRunningTime = SharedPrefs.getLong(WearableWakefulService.KEY_WAKFUL_SERVICE_LAST_RUN, 0, mContext);
        if (System.currentTimeMillis() - lastRunningTime > 1000 * 55 || System.currentTimeMillis() - lastRunningTime < 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 1);
            calendar.set(Calendar.SECOND, 0);
            long scheduleTime = calendar.getTimeInMillis();

            AlarmManager am = (AlarmManager) mContext.getSystemService(
                    Context.ALARM_SERVICE);
            am.cancel(alarmIntent);
            am.setExact(AlarmManager.RTC_WAKEUP, scheduleTime, alarmIntent);
            logger.i("Set Alarm from " + fromWhere, mContext);
            logger.i("Alarm set for " + calendar.getTime().toString(), mContext);
        } else{
            logger.i("Time is not ready, no need to set alarm from " + fromWhere, mContext);
        }
        logger.close();
    }

    public void cancelAlarm(){
        AlarmManager am = (AlarmManager) mContext.getSystemService(
                Context.ALARM_SERVICE);
        am.cancel(alarmIntent);
    }
}
