package edu.neu.android.wocketslib.utils;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;

import java.io.FileDescriptor;
import java.io.IOException;

import edu.neu.android.wocketslib.R;

public class PhoneAudioPrompter extends IntentService {

    private static final String aTAG = "PhoneAudioPrompter";
		
	public static final int CHIMES_NONE     = -1;
	public static final int CHIMES_HIKARI   = 0;
	public static final int CHIMES_CHIMES1  = 1;
	public static final int CHIMES_NAMBOKU1 = 2;
	public static final int CHIMES_YOKOKUO2 = 3;
    public static final int CHIMES_YOKOKU5 = 4;

	private static MediaPlayer mp = null;

	public PhoneAudioPrompter(String name) {
		super(name);
	}

	public PhoneAudioPrompter() {
		super("PhoneAudioPrompter");
	}

	@Override
    public void onCreate() {
        super.onCreate();
        Log.d(aTAG, "Inside onCreate");
        Bundle extras = new Bundle();
        int chimeType = extras.getInt("CHIME_TYPE");
        startAudioAlert(getApplicationContext(), chimeType);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(aTAG, "Inside onDestroy");
    }

    public static void startAudioAlert(Context aContext, int chimeType) {
		// See
		// http://stackoverflow.com/questions/3536391/how-to-correctly-set-mediaplayer-audio-stream-type
		Log.d(aTAG, "Inside start audio alert");
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
				mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
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

	@Override
	protected void onHandleIntent(Intent intent) {

	}
}
