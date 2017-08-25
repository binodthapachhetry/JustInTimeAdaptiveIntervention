package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Arrays;
import java.io.FilenameFilter;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.ConnectivityManager;
import edu.neu.mhealth.android.wockets.library.managers.UploadManager;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.Zipper;

import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.utils.ByteUtils;


/**
 * @author Dharam Maniar
 */
public class UploadManagerService extends IntentService {

    private static final String TAG = "PhireUploadManagerService";

    private Context mContext;

    private boolean isStudyFinished;
    private boolean isZipTransferFinished;
    private ExecutorService executor;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mContext = getApplicationContext();
//        Log.i(TAG, "Inside onCreate", getApplicationContext());
//        initialize();
//    }

    public UploadManagerService(){
        super("PhireUploadManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mContext = getApplicationContext();
        Log.i(TAG, "Inside onHandle Intent", mContext);
        initialize();

    }

    class PhireUploadHandler implements Runnable {
        final String pathToUpload;
        final Context rContext;


        public PhireUploadHandler(String pathToUpload, Context rContext) {
            this.pathToUpload = pathToUpload;
            this.rContext = rContext;
        }

        public void run() {
            UploadManager.uploadFile(pathToUpload, rContext);
        }
    }


    class PhireZipHandler implements Runnable {
        final String pathToZip;
        final Context rContext;


        public PhireZipHandler(String pathToZip, Context rContext) {
            this.pathToZip = pathToZip;
            this.rContext = rContext;
        }

        public void run() {
            Zipper.zipFolderWithEncryption(pathToZip, rContext);
        }
    }

    private void initialize() {


        executor = Executors.newSingleThreadExecutor();

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

//        unzipFromWatch();

        // stuff related to phone
        // Zip required log files
        processLogs();

        // Zip required survey files
        processSurveys();
        // Zip required data files
        processDataFiles();

        executor.shutdown();


        if(!executor.isTerminated()){
            Log.i(TAG,"Executor service zipping file is not finished, so stopping the service",mContext);
            return;
        }
        Log.i(TAG,"Moving on to uploads",mContext);


        if (!ConnectivityManager.isInternetConnected(mContext)) {
            Log.i(TAG, "Internet is not connected. Not trying to upload files.", mContext);
            return;
        }

        executor = Executors.newSingleThreadExecutor();
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
        if (Looper.myLooper() == Looper.getMainLooper()){
            Log.i(TAG,"In main thread",mContext);
        }else{
            Log.i(TAG,"Not in main thread",mContext);
        }
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

//        Log.i(TAG, "Processing Logs Watch", mContext);
//        String logWatchDirectory = DataManager.getDirectoryWatchLogs(mContext);
//        File logWatchFiles = new File(logWatchDirectory);
//        if (!logWatchFiles.exists()) {
//            Log.w(TAG, "Log directory does not exist - " + logWatchDirectory, mContext);
//            return;
//        }
//        if (logWatchFiles.listFiles() == null) {
//            Log.e(TAG, "No files present in logs directory. This should never happen", mContext);
//            return;
//        }
//        for (File logWatchDate : logWatchFiles.listFiles()) {
//            processLogs(logWatchDate);
//        }
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

        Date log_date = DateTime.getDate(logDate.getName());
        Date current_date = Calendar.getInstance().getTime();
        if (log_date.compareTo(current_date)>0){
            Log.e(TAG, " Deleting log date as it is in future for : " + logDate.getName(), mContext);
            logDate.delete();
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
            if (hourDirectory.getName().contains("sdcard")) {
                Log.d(TAG, "Deleting " + hourDirectory.getAbsolutePath());
                hourDirectory.delete();
                continue;
            }
            if (hourDirectory.getName().contains("Watch-")) {
                Log.d(TAG, "Deleting " + hourDirectory.getAbsolutePath());
                hourDirectory.delete();
                continue;
            }
            if(hourDirectory.isFile()){
                Log.d(TAG, "File instead of folder. Deleting: " + hourDirectory.getAbsolutePath());
                hourDirectory.delete();
                    continue;
            }

            int current_hour = DateTime.getCurrentHour();
            String[] name_split = hourDirectory.getName().split("-");

            if(name_split!=null){
                if (name_split.length > 1) {
                    Log.e(TAG, "Log hour folder name to be considered: " + hourDirectory.getName(), mContext);
                    int log_hour = Integer.parseInt(name_split[0]);
                    if (log_hour > current_hour) {
                        Log.e(TAG, "Log hour is in future for : " + hourDirectory.getName(), mContext);
                        hourDirectory.delete();
                        continue;
                    }else{
                        Log.e(TAG, "Log hour is within scope for : " + hourDirectory.getName(), mContext);

                    }
                }else{
                    Log.e(TAG, "Log hour folder name NOT to be considered: " + hourDirectory.getName(), mContext);
                    continue;
                }
            } else {
                Log.e(TAG, "Log hour folder name NOT to be considered: " + hourDirectory.getName(), mContext);
                continue;
            }

            Log.i(TAG, "Processing Logs for hour - " + hourDirectory.getAbsolutePath(), mContext);
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

//        Log.i(TAG, "Processing Watch Data files", mContext);
//        String dataWatchDirectory = DataManager.getDirectoryWatchData(mContext);
//        File dataWatchFiles = new File(dataWatchDirectory);
//        if (!dataWatchFiles.exists()) {
//            Log.w(TAG, "Watch Data directory does not exist", mContext);
//            return;
//        }
//
//        if (dataWatchFiles.listFiles() == null) {
//            Log.w(TAG, "No files present in watch data directory.", mContext);
//            return;
//        }
//
//        for (File dataDate : dataWatchFiles.listFiles()) {
//            processData(dataDate);
//        }

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

//        executor = Executors.newSingleThreadExecutor();

        for (File hourDirectory : dataDate.listFiles()) {
//            Log.i(TAG, "Processing data for hour " + hourDirectory.getAbsolutePath(), mContext);
            if (hourDirectory.getName().contains("zip")) {
                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Already zipped");
                continue;
            }
            if (!isStudyFinished && hourDirectory.getName().equals(DateTime.getCurrentHourWithTimezone())) {
                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Still writing");
                continue;
            }

            if(hourDirectory.isFile()){
                Log.d(TAG, "File instead of folder. Deleting: " + hourDirectory.getAbsolutePath());
                hourDirectory.delete();
                continue;
            }


//            Zipper.zipFolderWithEncryption(hourDirectory.getAbsolutePath(), mContext);

//            Zipper.zipFolderWithEncryption(hourDirectory.getAbsolutePath(), mContext);
//            ZipTask task = new ZipTask(mContext);
//            task.execute(hourDirectory.getAbsolutePath());

            Runnable zipHandler = new PhireZipHandler(hourDirectory.getAbsolutePath(), mContext);
            executor.execute(zipHandler);
        }


    }


//    public class ZipTask extends AsyncTask<String,Void,Void> {
//        private Context aContext;
//
//        public ZipTask(Context context){
//            aContext = context;
//        }
//
//        @Override
//        protected Void doInBackground(String... strings) {
//            String path = strings[0];
//            Zipper.zipFolderWithEncryption(path, aContext);
//            return null;
//        }
//    }

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
            // Delete any hour folders that are older than current hour
            // Delete any day folder that are newer than current date



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

                    if (logHour.isDirectory()){
                        continue;
                    }

                    Log.i(TAG, "Calling UploadManager.uploadFile on - " + logHour.getAbsolutePath(), mContext);
                    Runnable uploadHandler = new PhireUploadHandler(logHour.getAbsolutePath(), mContext);
                    executor.execute(uploadHandler);
//                    UploadManager.uploadFile(logHour.getAbsolutePath(), mContext);
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

                Runnable uploadHandler = new PhireUploadHandler(logDate.getAbsolutePath(), mContext);
                executor.execute(uploadHandler);
//                UploadManager.uploadFile(logDate.getAbsolutePath(), mContext);
            }
        }

//        Log.i(TAG, "Processing Watch Log Uploads", mContext);
//        String logWatchDirectory = DataManager.getDirectoryWatchLogs(mContext);
//        File logWatchFiles = new File(logWatchDirectory);
//        if (!logWatchFiles.exists()) {
//            Log.w(TAG, "Watch Log directory does not exist - " + logWatchDirectory, mContext);
//            return;
//        }
//
//        for (File logWatchDate : logWatchFiles.listFiles()) {
//            // TODO: This is a MATCH specific fix. Need to come up with a better solution
//
//            if (logWatchDate.getName().contains("sdcard")) {
//                Log.d(TAG, "Deleting " + logWatchDate.getAbsolutePath());
//                logWatchDate.delete();
//                continue;
//            }
//
//            if (DateTime.getDate(logWatchDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
//                Log.i(TAG, "Ignoring logs for date - " + logWatchDate.getName(), mContext);
//                continue;
//            }
//
//            if (logWatchDate.isDirectory()) {
//                for (File logHour : logWatchDate.listFiles()) {
//                    // We only want to upload zip files
//                    if (!logHour.getName().contains("zip")) {
//                        continue;
//                    }
//                    // We don't want to re-upload uploaded files.
//                    if (logHour.getName().contains("uploaded")) {
//                        continue;
//                    }
//
//                    Log.i(TAG, "Calling UploadManager.uploadFile on - " + logHour.getAbsolutePath(), mContext);
//                    UploadManager.uploadFile(logHour.getAbsolutePath(), mContext);
//                }
//            } else {
//                // We only want to upload zip files
//                if (!logWatchDate.getName().contains("zip")) {
//                    continue;
//                }
//                // We don't want to re-upload uploaded files.
//                if (logWatchDate.getName().contains("uploaded")) {
//                    continue;
//                }
//
//                Log.i(TAG, "Calling UploadManager.uploadFile on - " + logWatchDate.getAbsolutePath(), mContext);
//                UploadManager.uploadFile(logWatchDate.getAbsolutePath(), mContext);
//            }
//        }
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

                    if(dataHour.isDirectory()){
                        continue;
                    }

                    Log.i(TAG, "Calling UploadManager.uploadFile on - " + dataHour.getAbsolutePath(), mContext);
                    Runnable uploadHandler = new PhireUploadHandler(dataHour.getAbsolutePath(), mContext);
                    executor.execute(uploadHandler);

//                    UploadManager.uploadFile(dataHour.getAbsolutePath(), mContext);
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
                Runnable uploadHandler = new PhireUploadHandler(dataDate.getAbsolutePath(), mContext);
                executor.execute(uploadHandler);

//                UploadManager.uploadFile(dataDate.getAbsolutePath(), mContext);
            }
        }

//        Log.i(TAG, "Processing Watch Data Uploads", mContext);
//        String dataWatchDirectory = DataManager.getDirectoryWatchData(mContext);
//        File dataWatchFiles = new File(dataWatchDirectory);
//        if (!dataWatchFiles.exists()) {
//            Log.w(TAG, "Watch Data directory does not exist - " + dataWatchDirectory, mContext);
//            return;
//        }
//
//        for (File dataWatchDate : dataWatchFiles.listFiles()) {
//            // TODO: This is a MATCH specific fix. Need to come up with a better solution
//            if (DateTime.getDate(dataWatchDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
//                Log.i(TAG, "Ignoring data for date - " + dataWatchDate.getName(), mContext);
//                continue;
//            }
//            if (dataWatchDate.isDirectory()) {
//                for (File dataHour : dataWatchDate.listFiles()) {
//                    // We only want to upload zip files
//                    if (!dataHour.getName().contains("zip")) {
//                        continue;
//                    }
//                    // We don't want to re-upload uploaded files.
//                    if (dataHour.getName().contains("uploaded")) {
//                        continue;
//                    }
//
//                    Log.i(TAG, "Calling UploadManager.uploadFile on - " + dataHour.getAbsolutePath(), mContext);
//                    UploadManager.uploadFile(dataHour.getAbsolutePath(), mContext);
//                }
//            } else {
//                // We only want to upload zip files
//                if (!dataWatchDate.getName().contains("zip")) {
//                    continue;
//                }
//                // We don't want to re-upload uploaded files.
//                if (dataWatchDate.getName().contains("uploaded")) {
//                    continue;
//                }
//
//                Log.i(TAG, "Calling UploadManager.uploadFile on - " + dataWatchDate.getAbsolutePath(), mContext);
//                UploadManager.uploadFile(dataWatchDate.getAbsolutePath(), mContext);
//            }
//        }
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

    private void unzipFromWatch() {
        DataManager.setZipTransferFinished(mContext,false);
        String watchZipFolder = DataManager.getDirectoryTransfer(mContext);
        Log.i(TAG, "This is transfer folder: " + watchZipFolder,mContext);
        File watchZipFile = new File(watchZipFolder);
        watchZipFile.mkdirs();
        File[] zipFiles = new File[0];
        if(watchZipFile.isDirectory()) {
            zipFiles = watchZipFile.listFiles();
        }else{
            Log.e(TAG, "This is not a directory: " + watchZipFile.getAbsolutePath(),mContext);

            if(watchZipFile.delete()){
                Log.i(TAG, "Delete and quit!",mContext);
            }else{
                Log.e(TAG, "Can't delete so just quit!",mContext);
            }
            return;
        }
        for(File zipFile : zipFiles) {
            try {
                Log.i(TAG, "Unzipping " + zipFile.getAbsolutePath(),mContext);
                _unzipFromWatchHelper(watchZipFolder, zipFile.getName());
                if(zipFile.delete()){
                    Log.i(TAG, "Delete watch zip file: " + zipFile.getAbsolutePath() + " upon successfully unzipping",mContext);
                }else{
                    Log.e(TAG, "Fail to delete watch zip file: " + zipFile.getAbsolutePath() + " after successfully unzipping",mContext);
                }
            } catch (ZipException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR when unzipping from watch:" + e.getMessage(),mContext);
                Log.e(TAG, "skip delete the file",mContext);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR when using unzip helper:" + e.getMessage(),mContext);
            }
        }
    }

    private void _unzipFromWatchHelper(String filePath, String fileName) throws ZipException, FileNotFoundException {
        ZipFile zipFile = new ZipFile(filePath + File.separator + fileName);
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        for(FileHeader file : fileHeaders) {
            String original_entryPath = file.getFileName();

            File original_unzippedFile = new File(original_entryPath);
            if(!original_unzippedFile.isDirectory()) {

                String entryPathFirst = original_entryPath.replace("/data/","/data-watch/");
                String entryPath = entryPathFirst.replace("/logs/","/logs-watch/");
                Log.i(TAG, "New path for transfer files: " + entryPath,mContext);
                File unzippedFile = new File(entryPath);
                Log.i(TAG, "New  path for transfer files: " + unzippedFile.getParentFile().getAbsolutePath(),mContext);
                unzippedFile.getParentFile().mkdirs();
                zipFile.extractFile(file, unzippedFile.getParent(), new UnzipParameters(), unzippedFile.getName());
                Log.i(TAG, "Unzipped folder: " + unzippedFile.getParent() + ", name: " + unzippedFile.getName(),mContext);
            }

        }
    }



}
