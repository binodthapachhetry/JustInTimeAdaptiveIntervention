package mhealth.neu.edu.phire.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.services.AccelerationManagerService;
import edu.neu.mhealth.android.wockets.library.services.UploadManagerService;
import edu.neu.mhealth.android.wockets.library.services.WocketsService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.activities.FeedbackChoices;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
//import edu.neu.mhealth.android.wockets.match.data.MATCHDataManager;

/**
 * @author Binod Thapa Chhetry
 */
public class AlwaysOnService extends WocketsService {

    private static final String TAG = "AlwaysOnService";

    private PowerManager.WakeLock wakeLock;

    private Context mContext;

    private CountDownTimer countDownTimer;

    private WakefulBroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);

        acquireWakelock();


        Log.i(TAG, "getting EMA survey prompted for date", mContext);
        int emaSurveysPrompted = DataManager.getEMASurveyPromptedCountForDate(mContext, DateTime.getDate());
        Log.i(TAG, "getting EMA survey completed for date", mContext);
        int emaSurveysCompleted = DataManager.getEMASurveyCompletedCountForDate(mContext, DateTime.getDate());
        int emaSurveysMissed = emaSurveysPrompted - emaSurveysCompleted;


//        long currentTime = DateTime.getCurrentTimeInMillis();
//        long endTime = DataManager.getEndDate(mContext);

        Intent myIntent = new Intent(mContext, FeedbackChoices.class);
        PendingIntent pIntent = PendingIntent.getActivity(
                mContext,
                0,
                myIntent,
                FLAG_ACTIVITY_NEW_TASK);


            startForeground(
                    NotificationManager.NOTIFICATION_ID_MINUTE_SERVICE,
//                    NotificationManager.getAlwaysOnServiceNotification(
                    getAlwaysOnServiceNotification(

                                    mContext,
                            R.mipmap.ic_launcher,
                            pIntent,
                            "Prompted: " + emaSurveysPrompted +
                                    ", Completed: " + emaSurveysCompleted +
                                    ", Missed: " + emaSurveysMissed
                    )
            );


        broadcastReceiver = new WakefulBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Received Intent - " + intent.getAction(), context);
            }
        };

        Log.i(TAG, "registering action time tick", mContext);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        Log.i(TAG, "registering action battery changed", mContext);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.i(TAG, "registering action configuration changed", mContext);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
        Log.i(TAG, "registering action screen off", mContext);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        Log.i(TAG, "reistering action screen on", mContext);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

        countDownTimer = new CountDownTimer(DateTime.MINUTES_1_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "MillisUntilFinished - " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "starting minute service", mContext);
//                startService(new Intent(mContext, AccelerationManagerService.class));
//                startService(new Intent(mContext, UploadManagerService.class));
                startService(new Intent(mContext, MinuteService.class));

                startCountDownTimer();
            }
        };
        countDownTimer.start();
    }

    private void startCountDownTimer() {
        countDownTimer.start();
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        Log.i(TAG, "Acquired Partial Wake Lock", mContext);
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            Log.i(TAG, "Released Wake Lock", mContext);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
        unregisterReceiver(broadcastReceiver);
        releaseWakeLock();
    }

    private static Notification getAlwaysOnServiceNotification(Context context, int notificationIcon, PendingIntent pIntent,String text) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                notificationIcon);
        return new Notification.Builder(context)
                .setContentTitle(DataManager.getStudyName(context))
                .setContentText(text)
                .setContentIntent(pIntent)
                .setSmallIcon(notificationIcon)
                .setLargeIcon(icon)
                .build();
    }
}
