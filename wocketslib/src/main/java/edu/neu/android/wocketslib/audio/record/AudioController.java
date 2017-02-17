package edu.neu.android.wocketslib.audio.record;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.audio.interp.ConsistentFrequencyDetector;
import edu.neu.android.wocketslib.audio.interp.LoudNoiseDetector;
import edu.neu.android.wocketslib.audio.interp.SingleClapDetector;

public class AudioController {
    private static final String TAG = "AudioController";
    
    private RecordAudioTask recordAudioTask; 
    private RecordAmplitudeTask recordAmplitudeTask;
    
//    private SoundPool testSounds;

    public void startAmplitude(Context aContext)
    {
    	startAmplitudeTask(aContext);
    }

    public void startAudioLogger(Context aContext)
    {
                 startTask(aContext, createAudioLogger(), "Audio Logger");
    }

    public void startLoudNoiseDetector(Context aContext)
    {
    	
                startTask(aContext, new LoudNoiseDetector(), "Audio Clapper");
    }

    public void startSingingDetector(Context aContext)
    {
                final int HISTORY_SIZE = 3;
                final int RANGE_THRESHOLD = 100;
                startTask(aContext, new ConsistentFrequencyDetector(
                        HISTORY_SIZE, RANGE_THRESHOLD, 
                        ConsistentFrequencyDetector.DEFAULT_SILENCE_THRESHOLD), "Singing Clapper");
    }

    public void stopAudioDetectors()
    {
    	
                stopAll();
            }

//        //set up a sound pool with all the sounds so they playback faster
//        testSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
//        hookSoundButton(R.id.btn_audio_low, R.raw.lowhz);
//        hookSoundButton(R.id.btn_audio_mid, R.raw.midhz);
//        hookSoundButton(R.id.btn_audio_high, R.raw.highhz);
//        hookSoundButton(R.id.btn_audio_all, R.raw.lowhigh);
//    }

//    private void hookSoundButton(int button, final int audioResource)
//    {
//        final int soundId = testSounds.load(this, audioResource, 1);
//        Button soundButton = (Button)findViewById(button);
//        
//        soundButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                testSounds.play(soundId, 1f, 1f, 1, 0, 1.0f);
//                testSounds.play(soundId, 1f, 1f, 1, 0, 1.0f);
//            }
//        });
//    }
    
    private void stopAll()
    {
        if (Globals.IS_AUDIO_AMPLITUDE_MONITORING_ENABLED)
        {
            Log.d(TAG, "Stop record amplitude");
            shutDownTaskIfNecessary(recordAmplitudeTask);
        }
//        if (Globals.IS_SOUND_CLIP_RECORDING_ENABLED)
//        {
//            Log.d(TAG, "Stop record audio");
//            shutDownTaskIfNecessary(recordAudioTask);
//        }
    }
    
    private void shutDownTaskIfNecessary(final AsyncTask task)
    {
        if ( (task != null) && (!task.isCancelled()))
        {
            if ((task.getStatus().equals(AsyncTask.Status.RUNNING))
                    || (task.getStatus()
                            .equals(AsyncTask.Status.PENDING)))
            {
                Log.d(TAG, "CANCEL " + task.getClass().getSimpleName());
                task.cancel(true);
            }
            else
            {
                Log.d(TAG, "task not running");
            }
        }
    }

    private void startTask(Context aContext, AudioClipListener detector, String name)
    {
        stopAll();
        
        recordAudioTask = new RecordAudioTask(aContext, name);
        //wrap the detector to show some output
        List<AudioClipListener> observers = new ArrayList<AudioClipListener>();
//        observers.add(new AudioClipLogWrapper(log, this));
        OneDetectorManyObservers wrapped = 
            new OneDetectorManyObservers(detector, observers);
        recordAudioTask.execute(wrapped);
    }

    private void startAmplitudeTask(Context aContext)
    {
        stopAll();
        recordAmplitudeTask = new RecordAmplitudeTask(aContext, "Clapper");
        SingleClapDetector detector = new SingleClapDetector();
        //wrap the detector to show some output
        List<AmplitudeClipListener> observers = new ArrayList<AmplitudeClipListener>();
//        observers.add(new AmplitudeLogWrapper(log, this));
        OneDetectorManyAmplitudeObservers wrapped = 
            new OneDetectorManyAmplitudeObservers(detector, observers);
        recordAmplitudeTask.execute(wrapped);
    }

    private AudioClipListener createAudioLogger()
    {
        AudioClipListener audioLogger = new AudioClipListener()
        {
            @Override
            public boolean heard(short[] audioData, int sampleRate)
            {
                if (audioData == null || audioData.length == 0)
                {
                    return true;
                }
                
                // returning false means the recording won't be stopped
                // users have to manually stop it via the stop button
                return false;
            }
        };
        
        return audioLogger;
    }
}
