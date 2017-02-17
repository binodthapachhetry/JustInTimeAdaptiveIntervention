package edu.neu.android.wocketslib.audio.record;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.media.MediaRecorder;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.WOCKETSException;

/**
 * Created by Intille on 7/28/13.
 *
 * TODO For the case where we want audio amplitude but not audio clips,it would be better to set
 * this up to use a temporary file that is not named audioclip.*.mp3. Then to move the temporary
 * file to the right place if we do want to save the audio clips. Otherwise, delete it. This will
 * prevent someone from finding it and getting worried we are recording audio (when we are not) and
 * also avoid creating a lot of empty data directories if other data are not getting saved each
 * hour.
 *
 */
public class MHealthAudioClipRecorder {

    private MediaRecorder aMediaRecorder = null;

    RecordAmplitudeWhileRecordingTask aRecordAmplitudeWhileRecordingTask;
    String audioFileName;

    /**
     * Using the current time, get the audio filename using the mHealth format
     * TODO should this code go in the mHealth format directory?
     * @return A String of the filename
     */
    private String getAudioFileName(String phoneID, boolean isACC)
    {
        //TODO defaults to external. Change to allow an option internal or external
        String path = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY + File.separator + "mhealth" + File.separator + "sensors" + File.separator;
        Date aDate = Calendar.getInstance().getTime();

        path += Globals.mHealthDateDirFormat.format(aDate) + File.separator + Globals.mHealthHourDirFormat.format(aDate) + File.separator;
        path += "audioclip" + "." + phoneID + "." + Globals.mHealthFileNameFormat.format(aDate);

        if (isACC)
            path += ".mp3";
        else
            path += ".3gp";

        Log.d("AUDIO", path);
        return path;
    }

    public int getMaxAmplitude()
    {
        int ampValue = 0;
        if (aMediaRecorder != null)
        {
            ampValue = aMediaRecorder.getMaxAmplitude();
        }
        return ampValue;
    }

    private void setupPath(String TAG, String aPath)
    {
        try {
            FileHelper.createDirsIfDontExist(aPath);
        } catch (WOCKETSException e) {
            Log.e(TAG, "Problem creating path to audio file");
            e.printStackTrace();
        }
    }

    //
    public MHealthAudioClipRecorder(String TAG, String phoneID, Context aContext){
        // Setup to record a sound clip while the phone is processing the sensor data

        aRecordAmplitudeWhileRecordingTask = new RecordAmplitudeWhileRecordingTask(this, aContext);

        if (aMediaRecorder != null)
           stop();

        aMediaRecorder = new MediaRecorder();

        boolean isACC = false;
        // AAC encoding is not supported until API level 10
        try {
            Class<?> c = MediaRecorder.AudioEncoder.class;
            java.lang.reflect.Field field = c.getField("AAC");
            field.getInt(MediaRecorder.AudioEncoder.class);
            isACC = true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        aMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        if (isACC)
            aMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        else
            aMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        if (isACC)
        {
            Class<?> c = MediaRecorder.AudioEncoder.class;
            java.lang.reflect.Field field = null;
            try {
                field = c.getField("AAC");
                aMediaRecorder.setAudioEncoder(field.getInt(MediaRecorder.AudioEncoder.class));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        else
            aMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        audioFileName = getAudioFileName(phoneID, isACC);
        setupPath(TAG, audioFileName);
        aMediaRecorder.setOutputFile(audioFileName);        

        try {
            aMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder prepare() failed");
            aMediaRecorder = null;
        }
    }

    public void start()
    {
        if (aMediaRecorder != null)
        {
            aMediaRecorder.start();

            if (Globals.IS_AUDIO_AMPLITUDE_MONITORING_ENABLED)
            {            	
                //int currentApiVersion = android.os.Build.VERSION.SDK_INT;
            	java.lang.reflect.Field  field;
            	java.lang.reflect.Method method;
            	
            	try {                		
        			field = android.os.Build.VERSION_CODES.class.getField("HONEYCOMB");
        			Class<?> cls = RecordAmplitudeWhileRecordingTask.class;
        			Class<?>[] parameterTypes = { java.util.concurrent.Executor.class, Object[].class };
        			field  = cls.getField("THREAD_POOL_EXECUTOR");
            		method = cls.getMethod("executeOnExecutor", parameterTypes);
            		method.invoke(aRecordAmplitudeWhileRecordingTask, field.get(cls), (Object[]) null);
            	} catch (SecurityException e) {
            		e.printStackTrace();
            		aRecordAmplitudeWhileRecordingTask.execute();
            	} catch (NoSuchMethodException e) {
            		e.printStackTrace();
            		aRecordAmplitudeWhileRecordingTask.execute();
            	} catch (IllegalArgumentException e) {
            		e.printStackTrace();
            		aRecordAmplitudeWhileRecordingTask.execute();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					aRecordAmplitudeWhileRecordingTask.execute();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					aRecordAmplitudeWhileRecordingTask.execute();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
					aRecordAmplitudeWhileRecordingTask.execute();
				}                	          
            }
        }
    }

    public void stop()
    {
        aRecordAmplitudeWhileRecordingTask.isRunning = false;
        aMediaRecorder.stop();
        aMediaRecorder.release();
        aMediaRecorder = null;

        // If sound clip recording is not enabled, delete the file. Recording the file is
        // used to get the audio amplitude
        if (!Globals.IS_SOUND_CLIP_RECORDING_ENABLED)
        {
            FileHelper.deleteFile(audioFileName);
        }
    }
}
