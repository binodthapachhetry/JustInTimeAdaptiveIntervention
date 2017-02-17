package edu.neu.android.wocketslib.utils;

import android.content.Context;
import android.content.Intent;
import edu.neu.android.wocketslib.wakefulintent.WakefulIntentService;

public class PhonePrompterService extends WakefulIntentService{
		public static final String TAG = "PhonePrompterService";

		Context aContext = null; 
		public PhonePrompterService(Context aContext) {
			super("PhonePrompterService");
			this.aContext = aContext; 
		}

		@Override
		protected void doWakefulWork(Intent intent) {
			PhonePrompter.StartPhoneAlert(TAG, aContext, true);
		}
}
