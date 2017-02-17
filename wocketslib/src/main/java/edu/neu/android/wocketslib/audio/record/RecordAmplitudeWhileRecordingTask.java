package edu.neu.android.wocketslib.audio.record;

import java.util.Date;

import android.content.Context;
import android.os.AsyncTask;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.mhealthformat.LowSamplingRateDataSaver;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

public class RecordAmplitudeWhileRecordingTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "RecordAmplitudeWhileRecordingTask";

    public boolean isRunning = true;
    private MHealthAudioClipRecorder recorder = null;
    Context aContext;

    /**
     * Time between amplitude checks
     */
    private static final int SAMPLE_TIME_MS = 1000;

    public RecordAmplitudeWhileRecordingTask(MHealthAudioClipRecorder amHealthAudioClipRecorder, Context aContext) {
        recorder = amHealthAudioClipRecorder;
        this.aContext = aContext;
    }

    @Override
    protected void onPreExecute() {
        // Tell UI recording is starting
        super.onPreExecute();
    }

    /**
     * Note: only uses the first recorder passed in
     */
    @Override
    protected Void doInBackground(Void ... voids) {
        Log.d(TAG, "Checking amplitude");
        int maxAmplitude;
        isRunning = true;

        long lastSendTime = System.currentTimeMillis();
        int totalAudioAmplitude = 0;
        int totalAudioSamples = 0;
        double avgAudioAmplitude = 0;
		//int oneMinuteAve = 0;
		//int oneMinuteSampleNumber = 0;		
		int oneMinuteMax = 0;
		/*int oneMinuteMed = 0;
		int oneMinuteSD = 0;
		int oneMinuteMin = 100000;
		ArrayList<Integer> audioValues = new ArrayList<Integer>();*/
		String[] header = { "TIME_STAMP", "MAX_AMPLITUDE"};
		LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(Globals.IS_SENSOR_DATA_EXTERNAL, Globals.SENSOR_TYPE_MICROPHONE, PhoneInfo.getID(aContext), header);

        while (isRunning) {
            if (recorder != null) {
                maxAmplitude = recorder.getMaxAmplitude();

                if (maxAmplitude != 0) {
                    // Ignore the 0 values when computing mean, because they are invalid measurements
                    totalAudioAmplitude += maxAmplitude;
                    totalAudioSamples += 1;                    
                    
                    /*audioValues.add(maxAmplitude);
                    oneMinuteAve += maxAmplitude;
                    if (maxAmplitude > oneMinuteMax)
                    	oneMinuteMax = maxAmplitude;
                    if (maxAmplitude < oneMinuteMin)
                    	oneMinuteMin = maxAmplitude;*/
                }                
                
    			String[] data = {Integer.toString(maxAmplitude)};
    			dataSaver.saveData(data);    			
                
                if ((System.currentTimeMillis() - lastSendTime) > 10000) {// 10 seconds
                    if (totalAudioSamples != 0) {
                        avgAudioAmplitude = totalAudioAmplitude / ((double) totalAudioSamples);
                        
                        //oneMinuteAve += totalAudioAmplitude;
                        //oneMinuteSampleNumber += totalAudioSamples;
                        if (avgAudioAmplitude > oneMinuteMax) {
                        	 oneMinuteMax = (int) avgAudioAmplitude;
                        }
                    }                        					

                    lastSendTime = System.currentTimeMillis();
                    totalAudioAmplitude = 0;
                    totalAudioSamples = 0;
                    // send to the graph viewer
                    ServerLogger.addAudioData(TAG, avgAudioAmplitude, aContext);
                }

                try {
                    Thread.sleep(SAMPLE_TIME_MS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Problem during Thread.sleep: " + e.toString());
                    e.printStackTrace();
                }
            }
            else
                isRunning = false;
        }
        
        if (Globals.IS_MAX_AUDIO_SAVING_ENABALED) {
	        int minute = new Date().getMinutes() + new Date().getHours() * 60;
	        if (minute == 1260) { // Reset audio values at 9pm
	        	DataStorage.SetValue(aContext, Globals.AUDIO_MINUTE, "");
	        }	        
			
			String temp = DataStorage.GetValueString(aContext, Globals.AUDIO_MINUTE, "");
			if (!temp.equals("")) {
				temp += ",";
			}
			DataStorage.SetValue(aContext, Globals.AUDIO_MINUTE, temp + oneMinuteMax + "_" + minute);
						
			//saving audio stats
			/*String[] fileHeader = { "TIME_STAMP", "MIN", "MAX", "MEDIAN", "MEAN", "SD"};
			LowSamplingRateDataSaver audioSaver = new LowSamplingRateDataSaver(Globals.IS_SENSOR_DATA_EXTERNAL, "audioStats", PhoneInfo.getID(aContext), fileHeader);
			int size = audioValues.size();
			if (size != 0) {
				oneMinuteAve /= size; 
				for (int i = 0; i < size; i++) {
					oneMinuteSD += Math.pow(audioValues.get(i) - oneMinuteAve , 2);
				}
				oneMinuteSD /= size;
				oneMinuteSD = (int) Math.sqrt(oneMinuteSD);
				Collections.sort(audioValues);
				oneMinuteMed = audioValues.get(size/2);
			}			
			String[] msg = {Integer.toString(oneMinuteMin), Integer.toString(oneMinuteMax), Integer.toString(oneMinuteMed), Integer.toString(oneMinuteAve), Integer.toString(oneMinuteSD)};
			audioSaver.saveData(msg);*/
        }
    	
        //TODO Need to make sure that EVERYWHERE ServerLogger is that the send command is sent

        ServerLogger.send(TAG, aContext);

        // Missing a small amount of time if this isn't enabled
//        if ((System.currentTimeMillis() - lastSendTime) > 3000) // 6 seconds
//        {
//            if (totalAudioSamples != 0) {
//                avgAudioAmplitude = totalAudioAmplitude / ((double) totalAudioSamples);
//                Log.d(TAG, "Average audio amplitude: " + avgAudioAmplitude);
//            }
//            ServerLogger.addAudioData(TAG, avgAudioAmplitude, aContext);
//        }

        //return true;
        return null;
    }

//    @Override
//    protected void onPostExecute()
//    {
////        super.onPostExecute();
//    }

    @Override
    protected void onCancelled()
    {
        Log.d(TAG, "Cancelled");
        super.onCancelled();
    }
     
    
    
}



