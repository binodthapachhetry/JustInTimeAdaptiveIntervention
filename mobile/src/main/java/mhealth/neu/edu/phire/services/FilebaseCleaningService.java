package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;


public class FilebaseCleaningService extends WocketsIntentService {

    private final String TAG = "FilebaseCleaningService";
    private Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);
        initiatize();
    }

    private void initiatize(){
        boolean isStudyFinished = DataManager.isStudyFinished(mContext);

        long fileDeletionLastRun = TEMPLEDataManager.getFilebaseCleaningLastRun(mContext);

        if (!isStudyFinished && fileDeletionLastRun != 0) {
            long currentTime = DateTime.getCurrentTimeInMillis();
            if (currentTime > fileDeletionLastRun && currentTime < fileDeletionLastRun + DateTime.HOURS_1_IN_MILLIS) {
                Log.i(TAG, "FilebaseCleaningService executed within the last 24 hourS - " + DateTime.getTimestampString(fileDeletionLastRun), mContext);
                return;
            }

        }

        processLogFiles();
        processWatchLogFiles();
        processSurveyFiles();
        processDataFiles();
        processWatchDataFiles();

        TEMPLEDataManager.setFilebaseCleaningLastRun(mContext);


    }

    private void processDataFiles(){
        Log.i(TAG, "Processing data files", mContext);
        String dataDirectory = DataManager.getDirectoryData(mContext);
        File dataFiles = new File(dataDirectory);
        if (!dataFiles.exists()) {
            Log.w(TAG, "Data directory does not exist - " + dataDirectory, mContext);
            return;
        }
        if (dataFiles.listFiles() == null) {
            Log.e(TAG, "No files present in data directory. This should never happen", mContext);
            return;
        }

        if(dataFiles.listFiles().length<=15){
            Log.i(TAG,"Need to save 2 weeks of data files. So skipping deleting anything",mContext);
        }else{
            File[] fileArray = dataFiles.listFiles();
            Arrays.sort(fileArray);
            int numToDelete = dataFiles.listFiles().length-15;
            for(int i=0;i<=numToDelete;i++){
                File day = fileArray[i];
                for (File hourDirectory : day.listFiles()) {
                    if (hourDirectory.getName().contains(".zip.uploaded")) {
                        Log.i(TAG,"Deleting: "+ hourDirectory.getAbsolutePath(),mContext);
                        hourDirectory.delete();
                    }
                }
                File dayAfter = fileArray[i];
                if(dayAfter.listFiles().length==0){
                    Log.i(TAG,"Deleting: "+dayAfter.getAbsolutePath(),mContext);
                    dayAfter.delete();
                }
            }

        }

    }

    private void processWatchDataFiles(){
        Log.i(TAG, "Processing watchdata files", mContext);
        String wdataDirectory = DataManager.getDirectoryWatchData(mContext);
        File wdataFiles = new File(wdataDirectory);
        if (!wdataFiles.exists()) {
            Log.w(TAG, "watch Data directory does not exist - " + wdataDirectory, mContext);
            return;
        }
        if (wdataFiles.listFiles() == null) {
            Log.e(TAG, "No files present in watch data directory. This should never happen", mContext);
            return;
        }

        if(wdataFiles.listFiles().length<=15){
            Log.i(TAG,"Need to save 2 weeks of watch data files. So skipping deleting anything",mContext);
        }else{
            File[] fileArray = wdataFiles.listFiles();
            Arrays.sort(fileArray);
            int numToDelete = wdataFiles.listFiles().length-15;
            for(int i=0;i<=numToDelete;i++){
                File day = fileArray[i];
                for (File hourDirectory : day.listFiles()) {
                    if (hourDirectory.getName().contains(".zip.uploaded")) {
                        Log.i(TAG,"Deleting: "+ hourDirectory.getAbsolutePath(),mContext);
                        hourDirectory.delete();
                    }
                }
                File dayAfter = fileArray[i];
                if(dayAfter.listFiles().length==0){
                    Log.i(TAG,"Deleting: "+dayAfter.getAbsolutePath(),mContext);
                    dayAfter.delete();
                }
            }

        }
    }

    private void processSurveyFiles(){
        Log.i(TAG, "Processing Surveys", mContext);
        String surveyDirectory = DataManager.getDirectorySurveys(mContext);
        File surveyFiles = new File(surveyDirectory);
        if (!surveyFiles.exists()) {
            Log.w(TAG, "Survey directory does not exist", mContext);
            return;
        }
        if (surveyFiles.listFiles() == null) {
            Log.w(TAG, "No files present in survey directory.", mContext);
            return;
        }
        if(surveyFiles.listFiles().length<=15){
            Log.i(TAG,"Need to save 2 weeks of survey files. So skipping deleting anything",mContext);
        }else{
            File[] fileArray = surveyFiles.listFiles();
            Arrays.sort(fileArray);
            int numToDelete = surveyFiles.listFiles().length-15;
            for(int i=0;i<=numToDelete;i++){
                File day = fileArray[i];
                if (day.getName().contains(".zip.uploaded")) {
                    Log.i(TAG,"Deleting: "+ day.getAbsolutePath(),mContext);
                        day.delete();
                }

            }

        }


    }

    private void processLogFiles(){
        Log.i(TAG, "Processing Logs", mContext);
        String logDirectory = DataManager.getDirectoryLogs(mContext);
        File logFiles = new File(logDirectory);
        if (!logFiles.exists()) {
            Log.w(TAG, "Log directory does not exist - " + logDirectory, mContext);
            return;
        }
        if (logFiles.listFiles() == null) {
            Log.e(TAG, "No files present in logs directory. This should never happen", mContext);
            return;
        }

        if(logFiles.listFiles().length<=15){
            Log.i(TAG,"Need to save 2 weeks of logs. So skipping deleting anything",mContext);
        }else{
            File[] fileArray = logFiles.listFiles();
            Arrays.sort(fileArray);
            int numToDelete = logFiles.listFiles().length-15;
            for(int i=0;i<=numToDelete;i++){
                File day = fileArray[i];
                for (File hourDirectory : day.listFiles()) {
                    if (hourDirectory.getName().contains(".zip.uploaded")) {
                        Log.i(TAG,"Deleting: "+ hourDirectory.getAbsolutePath(),mContext);
                        hourDirectory.delete();
                    }
                }
                File dayAfter = fileArray[i];
                if(dayAfter.listFiles().length==0){
                    Log.i(TAG,"Deleting: "+dayAfter.getAbsolutePath(),mContext);
                    dayAfter.delete();
                }
            }

        }

    }

    private void processWatchLogFiles(){
        Log.i(TAG, "Processing Watch Logs", mContext);
        String logDirectory = DataManager.getDirectoryWatchLogs(mContext);
        File logFiles = new File(logDirectory);
        if (!logFiles.exists()) {
            Log.w(TAG, "Watch Log directory does not exist - " + logDirectory, mContext);
            return;
        }
        if (logFiles.listFiles() == null) {
            Log.e(TAG, "No files present in watch logs directory. This should never happen", mContext);
            return;
        }

        if(logFiles.listFiles().length<=15){
            Log.i(TAG,"Need to save 2 weeks of watch logs. So skipping deleting anything",mContext);
        }else {
            File[] fileArray = logFiles.listFiles();
            Arrays.sort(fileArray);
            int numToDelete = logFiles.listFiles().length - 15;
            for (int i = 0; i <= numToDelete; i++) {
                File day = fileArray[i];
                for (File hourDirectory : day.listFiles()) {
                    if (hourDirectory.getName().contains(".zip.uploaded")) {
                        Log.i(TAG, "Deleting: " + hourDirectory.getAbsolutePath(), mContext);
                        hourDirectory.delete();
                    }
                }
                File dayAfter = fileArray[i];
                if(dayAfter.listFiles().length==0){
                    Log.i(TAG,"Deleting: "+dayAfter.getAbsolutePath(),mContext);
                    dayAfter.delete();
                }
            }
        }
    }




}
