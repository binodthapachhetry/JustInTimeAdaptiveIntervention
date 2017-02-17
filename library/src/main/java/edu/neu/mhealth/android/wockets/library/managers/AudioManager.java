package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.media.MediaPlayer;

import edu.neu.mhealth.android.wockets.library.R;

/**
 * @author Dharam Maniar
 */
public class AudioManager {

	public static String getAudioMode(Context context) {
		android.media.AudioManager audioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
        String audioMode = "Audio";
        switch (ringerMode) {
            case android.media.AudioManager.RINGER_MODE_NORMAL:
                audioMode = "Audio";
                break;
            case android.media.AudioManager.RINGER_MODE_VIBRATE:
                audioMode = "Vibrate";
                break;
            case android.media.AudioManager.RINGER_MODE_SILENT:
                audioMode = "Silent";
                break;
        }
        return audioMode;
	}

	public static void promptAudio(Context context, int audioType) {
		MediaPlayer mediaPlayer;
		switch (audioType) {
			case 1:
				mediaPlayer = MediaPlayer.create(context, R.raw.audio1);
				break;
			case 2:
				mediaPlayer = MediaPlayer.create(context, R.raw.audio2);
				break;
			case 3:
				mediaPlayer = MediaPlayer.create(context, R.raw.audio3);
				break;
			case 4:
				mediaPlayer = MediaPlayer.create(context, R.raw.audio4);
				break;
			case 5:
				mediaPlayer = MediaPlayer.create(context, R.raw.audio5);
				break;
			default:
				mediaPlayer = MediaPlayer.create(context, R.raw.audio1);
				break;
		}
		if (mediaPlayer == null) {
			return;
		}
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				mediaPlayer.release();
			}
		});
		mediaPlayer.start();
	}
}
