package edu.neu.android.wearwocketslib.core.repeatedwakefulservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Date;

import edu.neu.android.wearwocketslib.utils.log.Logger;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class AlwaysOnService extends Service {
    private static final String TAG = "AlwaysOnService";

    private PowerManager.WakeLock wakeLock;

    private WakefulBroadcastReceiver broadcastReceiver;
    private String fromWhere;

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


        broadcastReceiver = new WakefulBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Received Intent - " + intent.getAction());
            }
        };

        Log.i(TAG, "registering action time tick");
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        countDownTimer = new CountDownTimer(MINUTES_1_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "MillisUntilFinished - " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                logger.i("starting wearable broadcast alarm", mContext);

//                startWakefulAlarm();
                // Start the service, keeping the device awake while it is launching.
                startRepeatedWakefulService();

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


    private void startRepeatedWakefulService(){
        Intent wakefulService = new Intent(mContext, WearableWakefulService.class);
        if(!WearableWakefulService.isRunning()) {
            logger.i("Starting service @ " + new Date().toString(), mContext);
            startWakefulService(mContext, wakefulService);
        }else{
            logger.i("Wakeful service is running, no need to start", mContext);
        }

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
