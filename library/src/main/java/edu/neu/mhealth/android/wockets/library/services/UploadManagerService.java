package edu.neu.mhealth.android.wockets.library.services;

import android.content.Context;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Arrays;
import java.io.FilenameFilter;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.ConnectivityManager;
import edu.neu.mhealth.android.wockets.library.managers.UploadManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.Zipper;

import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.utils.ByteUtils;


/**
 * @author Dharam Maniar
 */
public class UploadManagerService extends WocketsIntentService {

    private static final String TAG = "UploadManagerService";

    private Context mContext;

    private boolean isStudyFinished;
    private boolean isZipTransferFinished;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Inside onCreate", getApplicationContext());
        initialize();
    }

    private void initialize() {
        mContext = getApplicationContext();

        long lastRunTime = DataManager.getLastRunOfUploadManagerService(mContext);

        isStudyFinished = DataManager.isStudyFinished(mContext);

//        isZipTransferFinished = DataManager.isZipTransferFinished(mContext);
//
//        Log.i(TAG, "Is zip transfer complete: " + String.valueOf(isZipTransferFinished), mContext);
//
//
//
//        if(!isZipTransferFinished){
//            Log.i(TAG, "UploadManagerService decoding the binary watch file. So exiting.", mContext);
//            return;
//        }

//        // process transferred files from watch
//        unzipFromWatch();
//
//        DataManager.setZipTransferFinished(mContext,true);

//        unzipFromWatch();

        if (!isStudyFinished && lastRunTime != 0) {
            long currentTime = DateTime.getCurrentTimeInMillis();
            if (currentTime > lastRunTime && currentTime < lastRunTime + DateTime.HOURS_1_IN_MILLIS) {
                Log.i(TAG, "UploadManagerService executed within the last 1 hour - " + DateTime.getTimestampString(lastRunTime), mContext);
                return;
            }

        }

        // stuff related to phone
        // Zip required log files
        processLogs();
        // Zip required survey files
        processSurveys();
        // Zip required data files
        processDataFiles();


        if (!ConnectivityManager.isInternetConnected(mContext)) {
            Log.i(TAG, "Internet is not connected. Not trying to upload files.", mContext);
            return;
        }

        // stuff related to phone
        // Upload required log files
        processLogUploads();
        // Upload required survey files
        processSurveyUploads();
        // Upload required data files
        processDataUploads();

        DataManager.setLastRunOfUploadManagerService(mContext);


    }

    private void processLogs() {
        Log.i(TAG, "Processing Logs Phone", mContext);
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
        for (File logDate : logFiles.listFiles()) {
            processLogs(logDate);
        }
    }

    private void processLogs(File logDate) {
        Log.i(TAG, "Processing Logs for Date - " + logDate.getAbsolutePath(), mContext);
        if (logDate.getName().contains("zip")) {
            Log.d(TAG, "Logs already zipped for date - " + logDate.getAbsolutePath());
            return;
        }
        if (logDate.listFiles() == null) {
            Log.e(TAG, "No hourly files present. This should usually not happen", mContext);
            return;
        }
        for (File hourDirectory : logDate.listFiles()) {
            if (hourDirectory.getName().contains("zip")) {
                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Already zipped");
                continue;
            }
            if (!isStudyFinished && hourDirectory.getName().equals(DateTime.getCurrentHourWithTimezone())) {
                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Still writing");
                continue;
            }
            Zipper.zipFolderWithEncryption(hourDirectory.getAbsolutePath(), mContext);
        }

    }

    private void processSurveys() {
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

        for (File surveyDate : surveyFiles.listFiles()) {
            if (surveyDate.getName().contains("zip")) {
                Log.d(TAG, surveyDate.getAbsolutePath() + " - Already zipped");
                continue;
            }
            if (!isStudyFinished && surveyDate.getName().equals(DateTime.getDate())) {
                Log.d(TAG, "Still writing today's surveys, ignoring " + surveyDate.getAbsolutePath());
                continue;
            }

            processPastSurveys(surveyDate);
        }
    }

    private void processPastSurveys(File surveyDate) {
        Log.i(TAG, "Processing past surveys - " + surveyDate.getAbsolutePath(), mContext);
        Zipper.zipFolder(surveyDate.getAbsolutePath(), mContext);
    }

    private void processDataFiles() {
        Log.i(TAG, "Processing Data files", mContext);
        String dataDirectory = DataManager.getDirectoryData(mContext);
        File dataFiles = new File(dataDirectory);
        if (!dataFiles.exists()) {
            Log.w(TAG, "Data directory does not exist", mContext);
            return;
        }

        if (dataFiles.listFiles() == null) {
            Log.w(TAG, "No files present in data directory.", mContext);
            return;
        }

        for (File dataDate : dataFiles.listFiles()) {
            processData(dataDate);
        }

    }

    private void processData(File dataDate) {
        Log.i(TAG, "Processing data for date - " + dataDate.getAbsolutePath(), mContext);
        if (dataDate.getName().contains("zip")) {
            Log.d(TAG, "Logs already zipped for date - " + dataDate.getAbsolutePath());
            return;
        }
        if (dataDate.listFiles() == null) {
            Log.e(TAG, "No hourly files present. This should usually not happen", mContext);
            return;
        }
        for (File hourDirectory : dataDate.listFiles()) {
            Log.i(TAG, "Processing data for hour " + hourDirectory.getAbsolutePath(), mContext);
            if (hourDirectory.getName().contains("zip")) {
                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Already zipped");
                continue;
            }
            if (!isStudyFinished && hourDirectory.getName().equals(DateTime.getCurrentHourWithTimezone())) {
                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Still writing");
                continue;
            }
            Zipper.zipFolderWithEncryption(hourDirectory.getAbsolutePath(), mContext);
        }
    }

    private void processLogUploads() {
        Log.i(TAG, "Processing Log Uploads", mContext);
        String logDirectory = DataManager.getDirectoryLogs(mContext);
        File logFiles = new File(logDirectory);
        if (!logFiles.exists()) {
            Log.w(TAG, "Log directory does not exist - " + logDirectory, mContext);
            return;
        }

        for (File logDate : logFiles.listFiles()) {
            // TODO: This is a MATCH specific fix. Need to come up with a better solution
            if (DateTime.getDate(logDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
                Log.i(TAG, "Ignoring logs for date - " + logDate.getName(), mContext);
                continue;
            }

            if (logDate.isDirectory()) {
                for (File logHour : logDate.listFiles()) {
                    // We only want to upload zip files
                    if (!logHour.getName().contains("zip")) {
                        continue;
                    }
                    // We don't want to re-upload uploaded files.
                    if (logHour.getName().contains("uploaded")) {
                        continue;
                    }

                    Log.i(TAG, "Calling UploadManager.uploadFile on - " + logHour.getAbsolutePath(), mContext);
                    UploadManager.uploadFile(logHour.getAbsolutePath(), mContext);
                }
            } else {
                // We only want to upload zip files
                if (!logDate.getName().contains("zip")) {
                    continue;
                }
                // We don't want to re-upload uploaded files.
                if (logDate.getName().contains("uploaded")) {
                    continue;
                }

                Log.i(TAG, "Calling UploadManager.uploadFile on - " + logDate.getAbsolutePath(), mContext);
                UploadManager.uploadFile(logDate.getAbsolutePath(), mContext);
            }
        }
    }

    private void processSurveyUploads() {
        Log.i(TAG, "Processing Survey Uploads", mContext);
        String surveyDirectory = DataManager.getDirectorySurveys(mContext);
        File surveyFiles = new File(surveyDirectory);
        if (!surveyFiles.exists()) {
            Log.w(TAG, "Survey directory does not exist - " + surveyDirectory, mContext);
            return;
        }

        for (File surveyDate : surveyFiles.listFiles()) {
            // TODO: This is a MATCH specific fix. Need to come up with a better solution
            if (DateTime.getDate(surveyDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
                Log.i(TAG, "Ignoring surveys for date - " + surveyDate.getName(), mContext);
                continue;
            }
            // We only want to upload zip files
            if (!surveyDate.getName().contains("zip")) {
                continue;
            }
            // We don't want to re-upload uploaded files.
            if (surveyDate.getName().contains("uploaded")) {
                continue;
            }

            Log.i(TAG, "Calling UploadManager.uploadFile on - " + surveyDate.getAbsolutePath(), mContext);
            UploadManager.uploadFile(surveyDate.getAbsolutePath(), mContext);
        }
    }

    private void processDataUploads() {
        Log.i(TAG, "Processing Data Uploads", mContext);
        String dataDirectory = DataManager.getDirectoryData(mContext);
        File dataFiles = new File(dataDirectory);
        if (!dataFiles.exists()) {
            Log.w(TAG, "Data directory does not exist - " + dataDirectory, mContext);
            return;
        }

        for (File dataDate : dataFiles.listFiles()) {
            // TODO: This is a MATCH specific fix. Need to come up with a better solution
            if (DateTime.getDate(dataDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
                Log.i(TAG, "Ignoring data for date - " + dataDate.getName(), mContext);
                continue;
            }
            if (dataDate.isDirectory()) {
                for (File dataHour : dataDate.listFiles()) {
                    // We only want to upload zip files
                    if (!dataHour.getName().contains("zip")) {
                        continue;
                    }
                    // We don't want to re-upload uploaded files.
                    if (dataHour.getName().contains("uploaded")) {
                        continue;
                    }

                    Log.i(TAG, "Calling UploadManager.uploadFile on - " + dataHour.getAbsolutePath(), mContext);
                    UploadManager.uploadFile(dataHour.getAbsolutePath(), mContext);
                }
            } else {
                // We only want to upload zip files
                if (!dataDate.getName().contains("zip")) {
                    continue;
                }
                // We don't want to re-upload uploaded files.
                if (dataDate.getName().contains("uploaded")) {
                    continue;
                }

                Log.i(TAG, "Calling UploadManager.uploadFile on - " + dataDate.getAbsolutePath(), mContext);
                UploadManager.uploadFile(dataDate.getAbsolutePath(), mContext);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }

    public File[] finder( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.startsWith("AndroidWearWatch"); }
        } );

    }


}
