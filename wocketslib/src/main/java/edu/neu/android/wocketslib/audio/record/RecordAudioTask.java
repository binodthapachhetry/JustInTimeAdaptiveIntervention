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

import android.content.Context;
import android.media.AudioFormat;
import android.os.AsyncTask;
import edu.neu.android.wocketslib.utils.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class RecordAudioTask extends AsyncTask<AudioClipListener, Void, Boolean>
{
    private static final String TAG = "RecordAudioTask";
    
//    private Context context;
//   private String taskName;
    
    public RecordAudioTask(Context context, String taskName)
    {
//        this.context = context;
//        this.taskName = taskName;
    }
    
    @Override
    protected void onPreExecute()
    {
//        status.setText("Recording " + " for " + getTaskName());
//        AudioTaskUtil.appendToStartOfLog(log, "started " + getTaskName());
        super.onPreExecute();
    }
    
    @Override
    protected Boolean doInBackground(AudioClipListener... listeners)
    {
        if (listeners.length == 0)
        {
            return false;
        }

        AudioClipListener listener = listeners[0];
        
        AudioClipRecorder recorder = new AudioClipRecorder(listener, this);

        boolean heard = false;
        for (int i = 0; i < 10; i++)
        {
            try
            {
                heard =
                        recorder.startRecordingForTime(1000,
                                AudioClipRecorder.RECORDER_SAMPLERATE_8000,
                                AudioFormat.ENCODING_PCM_16BIT);
                break;
            } catch (IllegalStateException ie)
            {
                // failed to setup, sleep and try again
                // if still can't set it up, just fail
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                }
            }
        }

        //collect the audio
        return heard;
    }
    
    @Override
    protected void onPostExecute(Boolean result)
    {
        Log.d(TAG, "After execute got result: " + result);
        //update the UI with what happened
        //add to log
        //redraw perhaps
        if (result)
        {
//            AudioTaskUtil.appendToStartOfLog(log, getTaskName() + " detected " + AudioTaskUtil.getNow());
        }
        else
        {
 //           AudioTaskUtil.appendToStartOfLog(log, "stopped");
        }
        
        //log the result
        super.onPostExecute(result);
    }
    
    @Override
    protected void onCancelled()
    {
        Log.d(TAG, "OnCancelled");
        //the recorder should have shut down, this method
        //needs to just clean up resources
//        AudioTaskUtil.appendToStartOfLog(log, "cancelled " + getTaskName());
        super.onCancelled();
    }
    
}
