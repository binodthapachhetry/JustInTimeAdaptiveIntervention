package edu.neu.android.wearwocketslib.core.context;

import android.app.Application;
import android.content.Intent;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.services.SensorManagerService;
import edu.neu.android.wearwocketslib.utils.log.Logger;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;

/**
 * Created by qutang on 8/7/15.
 */
public class WearableApplication extends Application {
    public static final String TAG = "WearableApplication";
    private Logger logger;
    @Override
    public void onCreate() {
        logger = new Logger(TAG);
        super.onCreate();
        logger.i("onCreate", this);
        WearableUncaughtExceptionHandler exceptionHandler = new WearableUncaughtExceptionHandler(getApplicationContext());
        exceptionHandler.setDefaultHandler(Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        Globals.init();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        logger.i("onLowMemory", this);
        logger.i("Low memory threshold is: " + DeviceInfo.getMemoryUsageInMB(this) + "MB", this);
        logger.i("Stop sensor manager service", this);
        Intent stopIntent = new Intent(this, SensorManagerService.class);
        stopService(stopIntent);
        logger.close();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        logger.i("onTrimMemory", this);
        logger.i("Trim memory when ram is: " + DeviceInfo.getMemoryUsageInMB(this) + "MB", this);
        logger.close();
    }
}