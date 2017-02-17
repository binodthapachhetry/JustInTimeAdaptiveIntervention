package edu.neu.android.wocketslib.support;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.Log;

//import android.app.Activity;
//import android.content.Context;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Vibrator;

public class AlertPlayer {
	private static final String TAG = "AlertPlayer";
	
	// public static final int BEEP = R.raw.beep;
	// public static final int CHIME_GOAL_DONE = R.raw.goal_done_chime;
	// public static final int CHIME_NEW_GOAL = R.raw.new_goal_chime;

//	private void alertUser(Uri soundUri, int numMS, Context aContext, String TAG, Activity anActivity) {
//
//		try {
//			AudioManager am = (AudioManager) aContext
//					.getSystemService(Activity.AUDIO_SERVICE);
//
//			if (am != null) {
//				switch (am.getRingerMode()) {
//				case AudioManager.RINGER_MODE_SILENT:
//				case AudioManager.RINGER_MODE_VIBRATE:
//					((Vibrator) aContext.getSystemService(aContext.VIBRATOR_SERVICE)).vibrate(numMS);
//					break;
//				case AudioManager.RINGER_MODE_NORMAL:
//					MediaPlayer mp = MediaPlayer.create(aContext, soundUri);
//					if (mp != null)
//						mp.start();
//					else
//						// Log.e(TAG,"Couldn't get the MediaPlayer.");
//						break;
//				}
//			}
//		} catch (Exception e) {
//			// Log.e(TAG, "Problem with sound or vibration: " + e.toString());
//		}
//	}
	
//	private void beep() {
//	MediaPlayer mp = new MediaPlayer();
//	try {
//		// http://www.soundjay.com/beep-sounds-1.html lots of free beeps
//		// here
//		mp = MediaPlayer.create(getApplicationContext(), R.raw.beep);
//		mp.setLooping(false);
//		mp.start();
//	} catch (Exception e) {
//		Log.e("beep", "error: " + e.getMessage(), e);
//		e.printStackTrace();
//	}
//	try {
//		Thread.sleep(1000);
//	} catch (InterruptedException e) {
//		Log.e("beep", "error: " + e.getMessage(), e);
//		e.printStackTrace();
//	}
//	mp.release();
//}


	
	public static void beepPhone() {
		android.media.ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 40);
		tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
	}

	public static void beepPhone2() {
		android.media.ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 10);
		tg.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE);
	}
	
	public static void beep(Context aContext)
	{
		MediaPlayer mp = new MediaPlayer();
			    try {
			    	// http://www.soundjay.com/beep-sounds-1.html lots of free beeps here
			    	mp = MediaPlayer.create(aContext, R.raw.beep);
			    	mp.setLooping(false);
			    	mp.start();
			    }
			    catch (Exception e) {
			    Log.e("beep", "error: " + e.getMessage(), e);
			    }
		 try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.e(TAG, "Error InterruptedException in IdleTimeKeeper: " + e.toString());
			e.printStackTrace();
		}
		 mp.release(); 
	}
	
}
