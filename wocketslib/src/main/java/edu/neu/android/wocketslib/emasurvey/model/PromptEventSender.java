package edu.neu.android.wocketslib.emasurvey.model;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;

import android.content.Context;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.json.model.PromptEvent;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.utils.Log;

/**
 * This class is used to send the prompt information to the server for
 * data visualization.
 * 
 * @author bigbug
 *
 */
public class PromptEventSender {
	private final static String TAG = "PromptEventSender";
	private Context    mContext;
	private WocketInfo mWocketInfo;
	
	public PromptEventSender(Context context) {
		mContext = context;
		reset();		
	}
	
	public void reset() {
		mWocketInfo = new WocketInfo(mContext);
	}

	public boolean send() {
        if (mWocketInfo.isEmpty()) {
            return false;
        }

        try {
			DataSender.queueWocketInfo(mContext, mWocketInfo);
		} catch (ConcurrentModificationException e) {
			Log.e(TAG, "Failed to queue Wocket info");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				Log.e(TAG, "Error: InterruptedException in ServerLogger.send(): " + e1.toString());
			}
			DataSender.queueWocketInfo(mContext, mWocketInfo);
		}
        
        return true;
    }
	
	public void addPromptEvent(String aMsg, Date promptTime, String promptType, Date responseTime) {
		if (mWocketInfo.somePrompts == null) {
			mWocketInfo.somePrompts = new ArrayList<PromptEvent>();
		}

		PromptEvent aPromptEvent = new PromptEvent();
		aPromptEvent.promptTime      = promptTime; 
		aPromptEvent.promptType      = promptType;
		aPromptEvent.responseTime    = responseTime;
		aPromptEvent.primaryActivity = aMsg;
		mWocketInfo.somePrompts.add(aPromptEvent);	
	}
}
