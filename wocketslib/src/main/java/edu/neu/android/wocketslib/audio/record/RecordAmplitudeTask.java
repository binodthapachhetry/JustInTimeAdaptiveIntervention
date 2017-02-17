/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.neu.android.wocketslib.audio.record;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class RecordAmplitudeTask extends
        AsyncTask<AmplitudeClipListener, Void, Boolean>
{
    private static final String TAG = "RecordAmplitudeTask";

    private Context context;
    private String taskName;

    private static final String TEMP_AUDIO_DIR_NAME = "temp_audio";
    
    /**
     * time between amplitude checks
     */
    private static final int CLIP_TIME = 1000;

    public RecordAmplitudeTask(Context context,
            String taskName)
    {
        this.context = context;
        this.taskName = taskName;
    }

    @Override
    protected void onPreExecute()
    {
        // tell UI recording is starting
        super.onPreExecute();
    }

    /**
     * note: only uses the first listener passed in
     */
    @Override
    protected Boolean doInBackground(AmplitudeClipListener... listeners)
    {
        Log.d(TAG, "recording amplitude");
//        // construct recorder, using only the first listener passed in
//        AmplitudeClipListener listener = listeners[0];
        String appStorageLocation =
            context.getExternalFilesDir(TEMP_AUDIO_DIR_NAME).getAbsolutePath()
                    + File.separator + "audio.3gp";
        MaxAmplitudeRecorder recorder =
                new MaxAmplitudeRecorder(CLIP_TIME, appStorageLocation, this);

        //set to true if the recorder successfully detected something
        //false if it was canceled or otherwise stopped
        boolean heard = false;
        try
        {
            // start recording
            heard = recorder.startRecording(context);
        } catch (IOException io)
        {
            Log.e(TAG, "failed to record", io);
            heard = false;
        } catch (IllegalStateException se)
        {
            Log.e(TAG, "failed to record, recorder not setup properly", se);
            heard = false;
        } catch (RuntimeException se)
        {
            Log.e(TAG, "failed to record, recorder already being used", se);
            heard = false;
        }

        return heard;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        // update UI
        if (result)
        {
        	Log.e(TAG, "Heard clap at "
                    + AudioTaskUtil.getNow());
        }
        else
        {
            Log.e(TAG, "heard no claps");
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled()
    {
        Log.d(TAG, "cancelled " + taskName);
        super.onCancelled();
    }

}

//@Override
//public void heardClip(int maxAmplitude)
//{
//  AudioTaskUtil.appendToStartOfLog(log, "heard no claps: " + maxAmplitude);
//if (xyPlot != null)
//{
//    xyPlot.addSeries(audio,
//                     LineAndPointRenderer.class,
//                     new LineAndPointFormatter(Color.RED, null, null));
//    long uptime = SystemClock.uptimeMillis();
//    audio.addLast(increment, maxAmplitude);
//    xyPlot.redraw();
//    Log.d(TAG, "heard: " + maxAmplitude);
//    increment++;
////    
////        if ((uptime - lastChartRefresh) >= CHART_REFRESH)
////        {
////            long timestamp = (event.timestamp / 1000000) - startTime;
////            
////        }
//}
//}
