package mhealth.neu.edu.phire;

import android.content.Context;
import android.content.Intent;
import android.view.ViewDebug;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WakefulServiceArbitrator;
import edu.neu.android.wearwocketslib.notification.LostConnectionWearableNotification;
import edu.neu.android.wearwocketslib.notification.WearableNotification;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * This class is used as a callback which has been invoked from
 * BluetoothSensorService in WocketsLib.
 */
public class PHIREArbitrater extends WakefulServiceArbitrator {
	private static final String TAG = "PHIREArbitrater";

	protected Context mContext;

	private Logger logger = null;

	public PHIREArbitrater(Context aContext) {
		logger = new Logger(TAG);
		mContext = aContext;
	}

	@Override
	public void doArbitrate(Intent intent) {
		logger.i("UEMATEST . doArbitrate", mContext);
		long lastConnection = SharedPrefs.getLong(edu.neu.android.wearwocketslib.Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), mContext);

		Date date = new Date(lastConnection);
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");
		String dateText = df2.format(date);
		logger.i("Last phone connected time was: " + dateText, mContext);
		logger.i("Current time is: " + df2.format(new Date(System.currentTimeMillis())), mContext);
		logger.i("Time difference: " + String.valueOf(System.currentTimeMillis() - lastConnection), mContext);
		logger.i("Compared to:" + String.valueOf(edu.neu.android.wearwocketslib.Globals.PHONE_CONNECTION_NOTIFICATION_THRESHOLD),mContext);

		if(System.currentTimeMillis() - lastConnection >= edu.neu.android.wearwocketslib.Globals.PHONE_CONNECTION_NOTIFICATION_THRESHOLD){
			logger.i("Phone has been disconnected more than 120 seconds", mContext);

			if(!LostConnectionWearableNotification.isShowing(WearableNotification.LOST_CONNECTION_NOTIFICATION)) {
				LostConnectionWearableNotification notification = new LostConnectionWearableNotification("PHIRE:", "Connect phone", R.drawable.ic_launcher, true, WearableNotification.LOST_CONNECTION_NOTIFICATION, mContext);
				notification.show();
				logger.i("Showing lost connection notification", mContext);
			}else{
				logger.d("Already showing lost connection notificaiton");
			}

		}else {
			logger.d("Since last connection: " + (System.currentTimeMillis() - lastConnection) / 1000.0 + " seconds");
		}
		logger.close();
	}
}
