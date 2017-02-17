package edu.neu.android.wocketslib.broadcastreceivers;

public interface BroadcastReceiverProcessorInterface {

		public void respondSendSMS(); 		
		public void respondScreenOn(); 
		public void respondPhoneBooted(); 
		public void respondEndCall(); 
		public void respondStartCall(); 
		public void respondCallOut(); 
		public void respondAirplaneMode(); 
		public void respondCallIn(); 
		public void respondPowerConnected(); 
		public void respondPowerDisconnected(); 
		public void respondSMSReceived(); 
		public void respondLocationChanged(); 
		public void respondHeadsetPluggedIn(); 
}
