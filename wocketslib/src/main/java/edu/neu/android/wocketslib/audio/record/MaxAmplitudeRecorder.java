package edu.neu.android.wocketslib.audio.record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import edu.neu.android.wocketslib.audio.util.AudioUtil;
import edu.neu.android.wocketslib.audio.util.RecorderErrorLoggerListener;
import edu.neu.android.wocketslib.json.model.WocketData;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;

/**
 * Records {@link MediaRecorder#getMaxAmplitude()}
 * 
 * @author gmilette
 * 
 */
public class MaxAmplitudeRecorder {
	private static final String TAG = "MaxAmplitudeRecorder";

	private static final long DEFAULT_CLIP_TIME = 1000;
	private long clipTime = DEFAULT_CLIP_TIME;

	// private AmplitudeClipListener clipListener;

	private boolean continueRecording;

	private MediaRecorder recorder;

	private String tmpAudioFile;

	private AsyncTask task;

	private WocketInfo wi = null;

	public void addAudioAmplitude(Context aContext, double value) {

		if (wi == null)
			wi = new WocketInfo(aContext);

		if (wi.someWocketData == null)
			wi.someWocketData = new ArrayList<WocketData>();

		WocketData aWocketData = new WocketData();
		aWocketData.macID = "1234";
		aWocketData.activityCount = (int) value;
		aWocketData.createTime = new Date();
		wi.someWocketData.add(aWocketData);
	}

	/**
	 * 
	 * @param clipTime
	 *            time to wait in between maxAmplitude checks
	 * @param tmpAudioFile
	 *            should be a file where the MediaRecorder class can write
	 *            temporary audio data
	 * @param task
	 *            stop recording if this task is canceled
	 */
	public MaxAmplitudeRecorder(long clipTime, String tmpAudioFile, AsyncTask task) {
		this.clipTime = clipTime;
		this.tmpAudioFile = tmpAudioFile;
		this.task = task;

        Log.d(TAG, "MAX AMPLITUDE RECORDER");
	}

	/**
	 * start recording maximum amplitude and passing it to the clipListener
	 *
	 * @throws {@link IllegalStateException} if there is trouble creating the
	 *         recorder
	 * @throws {@link IOException} if the SD card is not available
	 * @throws {@link RuntimeException} if audio recording channel is occupied
	 * @return true if clipListener succeeded in detecting something
	 *         false if it failed or the recording stopped for some other reason
	 */
	public boolean startRecording(Context aContext) throws IOException {
		Log.d(TAG, "recording maxAmplitude");

		recorder = AudioUtil.prepareRecorder(tmpAudioFile);

		// when an error occurs just stop recording
		recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				// log it
				new RecorderErrorLoggerListener().onError(mr, what, extra);
				// stop recording
				stopRecording();
			}
		});

		// possible RuntimeException if Audio recording channel is occupied
		recorder.start();

		continueRecording = true;
		boolean heard = false;
//		recorder.getMaxAmplitude();

		long lastSendTime = System.currentTimeMillis();
		long totalAudioAmplitude = 0;
		long totalAudioSamples = 0;

		while (continueRecording) {
			Log.d(TAG, "waiting while recording...");
			waitClipTime();
			if (task != null) {
				Log.d(TAG, "continue recording: " + continueRecording + " cancelled after waiting? " + task.isCancelled());
			}

			// in case external code stopped this while read was happening
			if ((!continueRecording) || ((task != null) && task.isCancelled())) {
				break;
			}

			int maxAmplitude = recorder.getMaxAmplitude();
			totalAudioAmplitude += maxAmplitude;
			totalAudioSamples += 1;
//			Log.d(TAG, "Current max amplitude: " + maxAmplitude);
			Log.o(TAG, "Current max amplitude", Integer.toString(maxAmplitude));

			double avgAudioAmplitude = 0;

			if ((System.currentTimeMillis() - lastSendTime) > 10000) { // 10 seconds 
				if (totalAudioSamples != 0) {
					avgAudioAmplitude = totalAudioAmplitude / ((double) totalAudioSamples);
					Log.d(TAG, "Average audio amplitude: " + avgAudioAmplitude);
				}
				
				lastSendTime = System.currentTimeMillis();
				totalAudioAmplitude = 0;
				totalAudioSamples = 0;
				
				ServerLogger.addAudioData(TAG, avgAudioAmplitude, aContext);
			}

			// heard = clipListener.heard(maxAmplitude);
			// if (heard)
			// {
			// stopRecording();
			// }
		}

		ServerLogger.send(TAG, aContext);
		Log.d(TAG, "stopped recording max amplitude");
		done();

		return heard;
	}

	private void waitClipTime() {
		try {
			Thread.sleep(clipTime);
		} catch (InterruptedException e) {
			Log.d(TAG, "interrupted");
		}
	}

	/**
	 * stop recorder and clean up resources
	 */
	public void done() {
		Log.d(TAG, "stop recording on done");
		if (recorder != null) {
			try {
				recorder.stop();
			} catch (Exception e) {
				Log.d(TAG, "failed to stop");
				return;
			}
			recorder.release();
		}
	}

	public boolean isRecording() {
		return continueRecording;
	}

	public void stopRecording() {
		continueRecording = false;
	}
}
