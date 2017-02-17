package edu.neu.android.wocketslib.broadcastreceivers;

import android.content.Context;
import edu.neu.android.wocketslib.utils.Log;

public class BroadcastReceiverProcessor implements BroadcastReceiverProcessorInterface {
	private final static String TAG = "BroadcastReceiverProcessor";
	protected Context mContext;
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	@Override
	public void respondSendSMS() {
		Log.d(TAG, "Respond SendSMS in " + TAG); 
	}

	@Override
	public void respondScreenOn() {
		Log.d(TAG, "Respond Screen On in " + TAG);
	}

	@Override
	public void respondPhoneBooted() {
		Log.d(TAG, "Respond Phone Booted in " + TAG);
	}
	
	@Override
	public void respondEndCall() {
		Log.d(TAG, "Respond End Call in " + TAG);
	}
		
	@Override
	public void respondStartCall() {
		Log.d(TAG, "Respond Start Call in " + TAG);
	}

	@Override
	public void respondCallOut() {
		Log.d(TAG, "Respond Call Out in " + TAG);
	}

	@Override
	public void respondAirplaneMode() {
		Log.d(TAG, "Respond Airplane Mode in " + TAG);
	}

	@Override
	public void respondCallIn() {
		Log.d(TAG, "Respond Call In in " + TAG);
	}

	@Override
	public void respondPowerConnected() {
		Log.d(TAG, "Respond Power Connected in " + TAG);
	}

	@Override
	public void respondPowerDisconnected() {
		Log.d(TAG, "Respond Power Disconnected in " + TAG);
	}

	@Override
	public void respondSMSReceived() {
		Log.d(TAG, "Respond SMS Received in " + TAG);
	}
	
	@Override
	public void respondLocationChanged() {
		Log.d(TAG, "Respond Location Changed in " + TAG);
	}

	@Override
	public void respondHeadsetPluggedIn() {
		Log.d(TAG, "Respond Headset Plugged In in " + TAG);
	}

}
