package edu.neu.android.wearwocketslib.utils.log;

import android.content.Context;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.entities.mHealthEntity;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by Dharam on 5/1/2015.
 */
public class Log {

    public static final String TAG = "WearLog";

    public static void d(String TAG, String message) {
        android.util.Log.d(TAG, message);
    }

    public static void e(String TAG, String message, Context context) {
        android.util.Log.e(TAG, message);
        logTAGFile("error", TAG, message, context);
    }

    public static void i(String TAG, String message, Context context) {
        android.util.Log.i(TAG, message);
        logTAGFile("info", TAG, message, context);
    }

    public static void v(String TAG, String message, Context context) {
        android.util.Log.v(TAG, message);
        logTAGFile("verbose", TAG, message, context);
    }

    public static void w(String TAG, String message, Context context) {
        android.util.Log.w(TAG, message);
        logTAGFile("warn", TAG, message, context);
    }

    public static void wtf(String TAG, String message, Context context) {
        android.util.Log.wtf(TAG, message);
        logTAGFile("wtf", TAG, message, context);
    }

    private static void logTAGFile(String type, String TAG, String message, Context context) {
        Boolean logSaveFlag = SharedPrefs.getBoolean(WearableWakefulService.KEY_ENABLE_SAVE_LOG, WearableWakefulService.FLAG_ENABLE_SAVE_LOG, context);
        logSaveFlag = logSaveFlag == null? WearableWakefulService.FLAG_ENABLE_SAVE_LOG : logSaveFlag;
        if(!logSaveFlag){
            return;
        }
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File folder = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/" + currentDate + "/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + "/" + "Watch-" + TAG + ".log.csv");
        FileWriter aFileWriter = null;
        try {
            aFileWriter = new FileWriter(file, true);
            String logString = new Date().toString() + "," + type + "," + message;
            aFileWriter.write(logString);
            aFileWriter.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            IOUtils.closeQuietly(aFileWriter);
        }
    }

    public static boolean saveSensorData(String sensor, byte array[], Date specificDate, Context context) {
        String path;
        File folder;
        try {
            if(!mHealthEntity.verifyCurrentDate(specificDate)){
                Log.e(TAG, "Time has not been initialized correctly, quit: " + new Date().toString(),context);
                return false;
            }
            path = mHealthFormat.buildmHealthPath(specificDate, mHealthFormat.PATH_LEVEL.HOURLY, mHealthFormat.ROOT_DIRECTORY.MASTER);
            folder = new File(path);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), context);
            logStackTrace(e, TAG, context);
            e.printStackTrace();
            return false;
        }
            if (!folder.exists()) {
                if(!folder.mkdirs()){
                    Log.e(TAG, "Directory already exists or permission denied: " + folder.getAbsolutePath(), context);
                }
            }


        File[] files = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.contains(".baf");
                }
            });

        File file;
        if(files != null && files.length > 0){
            file = files[0];
        }else{
            String sensorId = DeviceInfo.getBluetoothMacAddressConcated() + "-" + AndroidWearAccelerometerRaw.DATA_TYPE;
            String sensorType = AndroidWearAccelerometerRaw.SENSOR_TYPE + "-" + sensor + "-" + AndroidWearAccelerometerRaw.VERSION_INFO;

            String filename = mHealthFormat.buildBafFilename(specificDate, sensorType, sensorId, "sensor");
            file = new File(path + filename);
        }
        boolean result = false;

        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(file, true);
            writer.write(array);
            Log.i(TAG, file.getAbsolutePath() + " got written", context);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(writer != null){
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    public static void logStackTrace(Exception e, String TAG, Context context){
        StackTraceElement[] stackTrace = e.getStackTrace();
        for(StackTraceElement trace : stackTrace){
            Log.e(TAG, trace.toString(), context);
        }
    }
}
