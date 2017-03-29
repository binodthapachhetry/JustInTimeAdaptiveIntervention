package edu.neu.android.wearwocketslib.core.repeatedwakefulservice;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Date;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.core.DaemonService;
import edu.neu.android.wearwocketslib.core.context.WearableUncaughtExceptionHandler;
import edu.neu.android.wearwocketslib.services.WearableFileTransferService;
import edu.neu.android.wearwocketslib.services.WearableMessageTransferService;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by qutang on 8/20/15.
 */
public class WearableWakefulBroadcastReceiver extends WakefulBroadcastReceiver{

    public static final String TAG = "WearableWakefulBroadcastReceiver";
    private Context mContext;
    private WearableWakefulBroadcastAlarm alarm;
    private String fromWhere;

    private Logger logger = new Logger(TAG);

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        logger.i("Received broadcast event: " + intent.getAction(), mContext);
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            long lastBootup = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            if(new Date().getYear() < 2000) logger.i("Time is not initialized properly, don't save boot up time", mContext);
            else {
                Globals.BOOT_UP_TIME_HOLDER = lastBootup;
                logger.i("Hold boot up time: " + new Date(lastBootup).toString() + " upon boot up", mContext);
            }
            resetCrashRetry();
        }else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            String[] somepkgs = mContext.getPackageManager()
                    .getPackagesForUid(uid);
            logger.i(somepkgs.toString(), mContext);
            if (somepkgs != null) {
                for (String pkgName : somepkgs) {
                    if (pkgName.contains("spades")) {
                        logger.i("PhoneState, PkgChanged, " + pkgName, mContext);
                        resetCrashRetry();
                    }
                }
            }
        }else if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED)){
            long lastBootup = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            Globals.BOOT_UP_TIME_HOLDER = lastBootup;
            logger.i("Hold boot up time: " + new Date(lastBootup).toString() + " upon time change", mContext);
        }

        // Start the service, keeping the device awake while it is launching.
        startRepeatedWakefulService(intent);

        startDeamonService();

        // Set restart alarm
        setMinuteAlarm(fromWhere);

        // Set Always on service
        startAlwaysOnService();

        // Send inverse trigger to phone (in case phone service doesn't running correctly
        sendTriggerToPhone();

        logger.close();
    }

    private void startAlwaysOnService(){
        logger.i("Starting Always on service @ " + new Date().toString(), mContext);
        Intent alwaysOnServiceIntent = new Intent(mContext,AlwaysOnService.class);
        mContext.startService(alwaysOnServiceIntent);
    }

    private void sendTriggerToPhone() {
        logger.i("Send trigger to phone through message transfer", mContext);
        Intent phoneTriggerIntent = new Intent(mContext, WearableMessageTransferService.class);
        phoneTriggerIntent.setAction("TRANSFER_MESSAGE");
        phoneTriggerIntent.putExtra("MESSAGE", "TRIGGER");
        mContext.startService(phoneTriggerIntent);
    }

    private void startDeamonService() {
        Intent daemonServiceIntent = new Intent(mContext, DaemonService.class);
        if(fromWhere != null && fromWhere.equals("CRASH")){
            logger.i("Restart daemon service upon crashing", mContext);
            daemonServiceIntent.putExtra("CRASH", true);
        }
        mContext.startService(daemonServiceIntent);
    }

    private void startRepeatedWakefulService(Intent intent){
        Intent wakefulService = new Intent(mContext, WearableWakefulService.class);
        fromWhere = intent.getStringExtra("FROM");
        fromWhere = fromWhere == null?"WATCH ALARM":fromWhere;
        if(!WearableWakefulService.isRunning()) {
            logger.i("Starting service @ " + new Date().toString(), mContext);
            startWakefulService(mContext, wakefulService);
        }else{
            logger.i("Wakeful service is running, no need to start", mContext);
        }

    }

    private void setMinuteAlarm(String from){
        logger.i("Set minute alarm for wakeful service from " + from, mContext);
        alarm = new WearableWakefulBroadcastAlarm(mContext, from);
        alarm.setAlarm();
    }

    private void resetCrashRetry(){
        logger.i("Reset crash retry count", mContext);
        SharedPrefs.setInt(WearableUncaughtExceptionHandler.KEY_CRASH_RETRY_COUNT, 0, mContext);
    }
}
