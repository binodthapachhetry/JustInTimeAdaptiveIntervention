package edu.neu.android.wocketslib.utils;

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;

public class PhonePrompter {
		
	public static final int CHIMES_NONE     = -1;
	public static final int CHIMES_HIKARI   = 0;
	public static final int CHIMES_CHIMES1  = 1;
	public static final int CHIMES_NAMBOKU1 = 2;
	public static final int CHIMES_YOKOKUO2 = 3;
    public static final int CHIMES_YOKOKU5 = 4;

	private static MediaPlayer mp = null;

	// TODO change so can use other than hikari
	public static void startAudioAlert(String aTAG, Context aContext, int chimeType) {
		// See
		// http://stackoverflow.com/questions/3536391/how-to-correctly-set-mediaplayer-audio-stream-type
		mp = new MediaPlayer();
		FileDescriptor fd = null;
		long offset = 0;
		long length = 0;
		AssetFileDescriptor afd = null;

		if (chimeType == CHIMES_CHIMES1)
			afd = aContext.getResources().openRawResourceFd(R.raw.chimes1);
		else if (chimeType == CHIMES_NAMBOKU1)
			afd = aContext.getResources().openRawResourceFd(R.raw.namboku1short);
		else if (chimeType == CHIMES_YOKOKUO2)
			afd = aContext.getResources().openRawResourceFd(R.raw.yokokuo2);
        else if (chimeType == CHIMES_YOKOKU5)
            afd = aContext.getResources().openRawResourceFd(R.raw.yokoku5);
		else
			afd = aContext.getResources().openRawResourceFd(R.raw.hikari1);
				
		fd = afd.getFileDescriptor();
		offset = afd.getStartOffset();
		length = afd.getDeclaredLength();

		if (mp != null) {
			try {
				// if( afd != null )
				// afd.close();
				mp.setDataSource(fd, offset, length);
				mp.setAudioStreamType(AudioManager.STREAM_RING);
				mp.prepare();
			} catch (IllegalArgumentException e) {
				Log.e(aTAG, "Error playing sound IllegalArgumentException - " + e.getMessage());
			} catch (IllegalStateException e) {
				Log.e(aTAG, "Error playing sound IllegalStateException - " + e.getMessage());
			} catch (IOException e) {
				Log.e(aTAG, "Error playing sound IOException - " + e.getMessage());
			}
			mp.start();
		}
	}

	public static String StartPhoneAlert(String aTAG, Context aContext, boolean isAudible) {
		return StartPhoneAlert(aTAG, aContext, isAudible, CHIMES_HIKARI); 
	}

	public static String StartPhoneAlert(String aTAG, Context aContext, boolean isAudible, int chimeName) {
		return StartPhoneAlert(aTAG, aContext, isAudible, chimeName, PhoneVibrator.VIBRATE_BASIC);		
	}
	
	public static String StartPhoneAlert(String aTAG, Context aContext, boolean isAudible, int chimeName, long[] vibrationPattern) {

		String msg;  
		if (isAudible)
			msg = "Audible prompt ";
		else
			msg = "Silent prompt "; 

		if (isAudible) {
			AudioManager am = (AudioManager) aContext.getSystemService(Context.AUDIO_SERVICE);
			switch (am.getRingerMode()) {
			case AudioManager.RINGER_MODE_SILENT:
				msg += "(Ringer on silent - no prompt)";
				if (Globals.IS_DEBUG)
					Log.i(aTAG, "Prompt, silent");
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				msg += "(Ringer on vibrate - vibrate prompt)";
				if (Globals.IS_DEBUG)
					Log.i(aTAG, "Prompt, vibration");
				PhoneVibrator.vibratePhonePattern(aTAG, aContext, vibrationPattern);
//				PhoneVibrator.vibratePhone(aTAG, 5000, aContext);
				break;
			case AudioManager.RINGER_MODE_NORMAL:
				msg += "(Ringer on normal - audible prompt)";
				if (Globals.IS_DEBUG)
					Log.i(aTAG, "Prompt, audio");
                PhoneVibrator.vibratePhonePattern(aTAG, aContext, vibrationPattern);
                Intent audioPrompter = new Intent(aContext, PhoneAudioPrompter.class);
                Bundle extras = new Bundle();
                extras.putInt("CHIME_TYPE", chimeName);
                audioPrompter.putExtras(extras);
                aContext.stopService(audioPrompter);
                aContext.startService(audioPrompter);
				//startAudioAlert(aTAG, aContext, chimeName);
				break;
			default: 
				Log.e(aTAG, "Unknown and unhandled ringer mode from AudioManager: " + am.getRingerMode());
				break; 
			}
		}

		return msg; 
	}	
	
	public static boolean isPrompting()
	{
		if (mp != null)
		{
			return mp.isPlaying();
		}
		return false; 
	}

	public static void cancel()
	{
		if (mp != null)
		{
			mp.stop();
			mp.release();
			mp = null; 
		}		
	}
}
