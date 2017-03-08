package mhealth.neu.edu.phire.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.services.UploadManagerService;
import edu.neu.mhealth.android.wockets.library.services.WocketsService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
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

        int emaSurveysPrompted = DataManager.getEMASurveyPromptedCountForDate(mContext, DateTime.getDate());
        int emaSurveysCompleted = DataManager.getEMASurveyCompletedCountForDate(mContext, DateTime.getDate());
        int emaSurveysMissed = emaSurveysPrompted - emaSurveysCompleted;

        startForeground(
                NotificationManager.NOTIFICATION_ID_MINUTE_SERVICE,
                NotificationManager.getAlwaysOnServiceNotification(
                        mContext,
                        R.mipmap.ic_launcher,
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

        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

        countDownTimer = new CountDownTimer(DateTime.MINUTES_1_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "MillisUntilFinished - " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                startService(new Intent(mContext, UploadManagerService.class));
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
}