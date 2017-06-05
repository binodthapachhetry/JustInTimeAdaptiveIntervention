package edu.neu.android.wearwocketslib.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Date;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.AlwaysOnService;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.utils.log.Logger;

public class SystemBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "SystemBroadcastReceiver";
    private Context mContext;
    private Logger logger = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        logger = new Logger(TAG);

        logger.i("stating minute service using intent",mContext);
        startRepeatedWakefulService();

        logger.i("starting always on service using intent",mContext);
        Intent alwaysOnServiceIntent = new Intent(mContext,AlwaysOnService.class);
        mContext.startService(alwaysOnServiceIntent);
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
}
