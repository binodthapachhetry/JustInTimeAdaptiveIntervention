package edu.neu.android.wearwocketslib.core.repeatedwakefulservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import edu.neu.android.wearwocketslib.utils.log.Logger;

public class AlwaysOnService extends Service {
    private static final String TAG = "AlwaysOnService";

    private PowerManager.WakeLock wakeLock;

    private Context mContext;

    private CountDownTimer countDownTimer;

    private Logger logger = null;

    private static final long MINUTES_1_IN_MILLIS = 60 * 1000;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        logger = new Logger(TAG);
        logger.i("Inside onCreate", mContext);
        Log.i(TAG, "Inside On Create");

        logger.i("Acquired wake lock", mContext);
        acquireWakelock();

        countDownTimer = new CountDownTimer(MINUTES_1_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "MillisUntilFinished - " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                logger.i("starting wearable broadcast alarm", mContext);

                startWakefulAlarm();

                startCountDownTimer();
            }
        };
        countDownTimer.start();

//        WearableWakefulBroadcastAlarm alarm = new WearableWakefulBroadcastAlarm(this, "ALWAYS ON SERVICE");
//        alarm.setAlarm();
        logger.close();
    }

    private void startWakefulAlarm(){
        WearableWakefulBroadcastAlarm alarm = new WearableWakefulBroadcastAlarm(this, "ALWAYS ON SERVICE");
        alarm.setAlarm();

    }

    private void startCountDownTimer() {
        countDownTimer.start();
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        Log.i(TAG, "Acquired Full Wake Lock");
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            logger.i("Released Wake Lock", mContext);
            Log.i(TAG, "Released Wake Lock");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.i("Inside onDestroy", mContext);
        releaseWakeLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
