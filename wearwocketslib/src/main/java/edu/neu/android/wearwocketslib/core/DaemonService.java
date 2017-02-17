package edu.neu.android.wearwocketslib.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulBroadcastAlarm;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by qutang on 9/11/15.
 *
 * This service did nothing but just stand-by all the time to make sure restoring everything else when
 * something wrong happens
 *
 */
public class DaemonService extends Service {

    public static final String TAG = "DaemonService";
    private Logger logger;
    private long lastRunningTime;

    private BroadcastReceiver mTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_TIME_TICK)){
                lastRunningTime = SharedPrefs.getLong(WearableWakefulService.KEY_WAKFUL_SERVICE_LAST_RUN, 0, getApplicationContext());

                if(!WearableWakefulService.isRunning() && System.currentTimeMillis() - lastRunningTime >= 120 * 1000) {
                    WearableWakefulBroadcastAlarm mAlarm = new WearableWakefulBroadcastAlarm(getApplicationContext(), "DAEMON_MONITOR");
                    mAlarm.setAlarm();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        logger = new Logger(TAG);
        logger.i("Create daemon service", getApplicationContext());
        logger.i("onCreate", getApplicationContext());
        super.onCreate();
        logger.i("Register tick receiver", getApplicationContext());
        getApplicationContext().registerReceiver(mTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        logger.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getBooleanExtra("CRASH", false)){
            logger.i("Restart daemon service upon crashing", getApplicationContext());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        logger.i("onDestroy", getApplicationContext());
        super.onDestroy();
        logger.i("Unregister tick receiver", getApplicationContext());
        getApplicationContext().unregisterReceiver(mTickReceiver);
        logger.close();
    }
}
