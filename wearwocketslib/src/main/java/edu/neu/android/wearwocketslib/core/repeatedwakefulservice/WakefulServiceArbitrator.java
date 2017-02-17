package edu.neu.android.wearwocketslib.core.repeatedwakefulservice;

import android.content.Context;
import android.content.Intent;

import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.log.Logger;

public class WakefulServiceArbitrator {
	private static final String TAG = "DefaultArbitrater";

	private static Context aContext = null;

	private Logger logger;

	public WakefulServiceArbitrator() {
		logger = new Logger(TAG);
	}

	public WakefulServiceArbitrator(Context aContext) {
		WakefulServiceArbitrator.aContext = aContext;
		logger = new Logger(TAG);
	}

	public void doArbitrate(Intent intent) {
		logger.i("Inside doArbitrate", aContext);
		logger.close();
	}
}
