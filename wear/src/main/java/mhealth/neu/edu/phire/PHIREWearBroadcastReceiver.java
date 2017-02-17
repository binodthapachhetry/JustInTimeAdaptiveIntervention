package mhealth.neu.edu.phire;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Date;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulBroadcastAlarm;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by Intille on 11/2/2014.
 */
public class PHIREWearBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "PHIREWearBroadcastReceiver";

    private Logger logger = new Logger(TAG);

    @Override
    public void onReceive(Context aContext, Intent intent) {
        // Most of these do not appear to work on Wear at this time but leaving in for now
        Globals.initGlobals(aContext);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            logger.i("PhoneState, Booted", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_REBOOT)) {
            logger.i("PhoneState, Rebooted", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_USER_FOREGROUND)) {
            logger.i("PhoneState, UsrForeground", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_USER_BACKGROUND)) {
            logger.i("PhoneState, UsrBackground", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            String[] somepkgs = aContext.getPackageManager()
                                        .getPackagesForUid(uid);
            if (somepkgs != null) {
                for (String pkgName : somepkgs) {
                    logger.i("PhoneState, PkgChanged, " + pkgName,
                            aContext);
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            String[] somepkgs = aContext.getPackageManager()
                                        .getPackagesForUid(uid);
            if (somepkgs != null) {
                for (String pkgName : somepkgs) {
                    logger.i("PhoneState, PkgAdded, " + pkgName,
                            aContext);
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            String[] somepkgs = aContext.getPackageManager()
                                        .getPackagesForUid(uid);
            boolean isReplacing = intent
                    .getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (somepkgs != null) {
                for (String pkgName : somepkgs) {
                    logger.i("PhoneState, PkgRemoved, Replacing: "
                            + isReplacing + " Name: " + pkgName, aContext);
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            logger.i("PhoneState, UsrPres", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            logger.i("PhoneState, ScrPff", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            logger.i("PhoneState, ScrOn", aContext);
        } else if (intent.getAction()
                         .equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            logger.i("PhoneState, AirplaneMode: " + intent
                    .getBooleanExtra("state", false), aContext);
        } else if (intent.getAction().equals(Intent.ACTION_DOCK_EVENT)) {
            logger.i("PhoneState, DockState: " + intent
                    .getStringExtra(Intent.EXTRA_DOCK_STATE), aContext);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            logger.i("PhoneState, PowerConnect", aContext);
        } else if (intent.getAction()
                         .equals(Intent.ACTION_POWER_DISCONNECTED)) {
            logger.i("PhoneState, PowerDisconnect", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            logger.i("PhoneState, Shutdown", aContext);
        } else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            String timeZone = intent.getStringExtra("time-zone");
            logger.i("PhoneState, Timezone, " + timeZone,
                    aContext);
        } else if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
            logger.i("PhoneState, DateChange, " + (new Date())
                    .toString(), aContext);
        }
        logger.close();
    }
}
