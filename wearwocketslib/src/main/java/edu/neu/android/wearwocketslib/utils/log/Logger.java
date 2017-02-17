package edu.neu.android.wearwocketslib.utils.log;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wocketslib.mhealthformat.entities.mHealthEntity;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by Dharam on 5/1/2015.
 */
public class Logger {

    private BufferedWriter writer = null;
    private String tag = "";
    public static final String TAG = "Logger";

    public Logger(String logTag){
        tag = logTag;
    }

    public void d(String message) {
        android.util.Log.d(TAG, message);
    }

    public void e(String message, Context context) {
        android.util.Log.e(tag, message);
        logTAGFile("error", message, context);
    }

    public void i(String message, Context context) {
        android.util.Log.i(tag, message);
        logTAGFile("info", message, context);
    }

    public void v(String message, Context context) {
        android.util.Log.v(tag, message);
        logTAGFile("verbose", message, context);
    }

    public void w(String message, Context context) {
        android.util.Log.w(tag, message);
        logTAGFile("warn", message, context);
    }

    public void wtf(String message, Context context) {
        android.util.Log.wtf(tag, message);
        logTAGFile("wtf", message, context);
    }

    private void logTAGFile(String type, String message, Context context) {
        Boolean logSaveFlag = SharedPrefs.getBoolean(WearableWakefulService.KEY_ENABLE_SAVE_LOG, WearableWakefulService.FLAG_ENABLE_SAVE_LOG, context);
        logSaveFlag = logSaveFlag == null? WearableWakefulService.FLAG_ENABLE_SAVE_LOG : logSaveFlag;
        if(!logSaveFlag){
            return;
        }
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String currentHour = new SimpleDateFormat("HH-z").format(new Date());
        File folder = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/" + currentDate + "/" + currentHour + "/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + "/" + "Watch-" + tag + ".log.csv");
        FileWriter aWriter = null;

        try {
            if(writer == null) {
                aWriter = new FileWriter(file, true);
                writer = new BufferedWriter(aWriter);
            }
            String logString = new Date().toString() + "," + type + "," + message;
            if(writer != null) {
                writer.write(logString);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                if (aWriter != null) {
                    aWriter.flush();
                    aWriter.close();
                }
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void logStackTrace(Exception e, Context context){
        StackTraceElement[] stackTrace = e.getStackTrace();
        for(StackTraceElement trace : stackTrace){
            this.e(trace.toString(), context);
        }
    }

    public void logStackTrace(Throwable ex, Context context){
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for(StackTraceElement trace : stackTrace){
            this.e(trace.toString(), context);
        }
    }

    public void close() {
        IOUtils.closeQuietly(writer);
        writer = null;
    }
}
