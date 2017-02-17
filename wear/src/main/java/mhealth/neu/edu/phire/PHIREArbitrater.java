package mhealth.neu.edu.phire;

import android.content.Context;
import android.content.Intent;

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
	private static final String TAG = "SPADESArbitrater";

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
		if(System.currentTimeMillis() - lastConnection >= edu.neu.android.wearwocketslib.Globals.PHONE_CONNECTION_NOTIFICATION_THRESHOLD * 1000 * 3600){
			if(!LostConnectionWearableNotification.isShowing(WearableNotification.LOST_CONNECTION_NOTIFICATION)) {
				LostConnectionWearableNotification notification = new LostConnectionWearableNotification("SPADES: Connection lost", "Please check phone/watch connection", R.drawable.ic_launcher, true, WearableNotification.LOST_CONNECTION_NOTIFICATION, mContext);
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
