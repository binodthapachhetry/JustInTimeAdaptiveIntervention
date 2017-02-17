package edu.neu.android.wocketslib.dataupload;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;

import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.PhoneServiceScheduler;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneNotifier;
import edu.neu.android.wocketslib.wakefulintent.WakefulIntentService;

/**
 * This Service runs in the background and attempts to upload data in both
 * the upload directories, on located in the internal memory, and on located
 * on the external memory.
 */
public class DataUploaderService extends WakefulIntentService {
    public static final String TAG = "DataUploaderService";

    // DataStorage constants for saving last upload states
    public static final String KEY_MOVE_LOG_TO_EXTERNAL = "_KEY_MOVETOEXTERNAL";
    public static final String KEY_UPLOAD_JSON = "_KEY_UPLOAD_JSON";
    public static final String KEY_WAKEFUL_RUN = "_KEY_WAKEFUL_RUN";

    private boolean forceUpload = false;

    private PhoneServiceScheduler mScheduler;

    public DataUploaderService() {
        super("DataUploaderService");
    }

//    // TODO change json to use internal directory for uploads
//
//    private void uploadDataToServer(Context aContext) {
//        long currentTime = System.currentTimeMillis();
//
//        // This is checked in the BTSensorService, but for good measure,checking again here
////        if (forceUpload || (currentTime - lastUploadJSONToServer) > Globals.JSON_DATA_UPLOAD_INTERVAL) {
//        if(forceUpload || UploadScheduler.shouldUploadJsonData(aContext)){
//            Log.i(TAG, "Uploading JSON data");
//            PhoneNotifier.showUploadingNotification(TAG, "Uploading JSON data", aContext);
//            long startTime= System.currentTimeMillis();
//            int minute = (int) Globals.JSON_DATA_UPLOAD_INTERVAL/(1000*60);
//
//            // Mark that code tried the upload so this code is not re-entered again while upload in progress
//            DataStorage.SetValue(aContext, KEY_UPLOAD_JSON, currentTime);
//
//            String msg = "Starting " + minute + "-minute file upload";
//            // Move JSON to external upload folder
//            DataSender.sendInternalUploadDataToExternalUploadDir(aContext, false, true);
//
////            if (forceUpload || (currentTime - lastMoveLogExternalTime) > Globals.MHEALTH_DATA_UPLOAD_INTERVAL) {
//            if (forceUpload || UploadScheduler.shouldUploadMhealthData(aContext)) {
//                Log.i(TAG, "Uploading mhealth data");
//                forceUpload = false;
//                PhoneNotifier.showUploadingNotification(TAG, "Uploading mhealth and log data", aContext);
//
//                //TODO We really want this to run in the middle of the night, not just 24 hours after the last time, as it is doing now.
//
//                Log.o(TAG, "StartUpload", "ExternalUploadDir", "false", "true", "true", Boolean.toString(Globals.BACKUP_UPLOADS_EXTERNAL),Float.toString(Globals.UPLOAD_SUCCESS_PERCENTAGE));
//
//                //To prevent reenter while upload is in progress
//                DataStorage.SetValue(aContext, KEY_MOVE_LOG_TO_EXTERNAL, currentTime);
//
//                // send logs
//                msg += " and 24-hour log and survey files upload";
//                // Move Standard Log files to external upload folder (compress and do not include today)
//                DataSender.sendLogsToExternalUploadDir(aContext, true, false);
//
//                // Copy Standard Log files to external upload folder (for today)
//                if (Globals.IS_DEBUG)
//                    DataSender.copyLogsToExternalUploadDir(aContext, true, true);
//
//                // TODO check where these files are saved
//                // Move Survey Log files to upload folder
////                DataSender.copyExternalSurveyLogsToExternalUploadDir(aContext, true, false);
//
//                /*// Move Log files to upload folder
//                DataSender.copyExternalDataLogsToExternalUploadDir(aContext, true, false);
//
//                // Move Data files to upload folder
//                DataSender.copyExternalDataLogsToExternalUploadDir(aContext, true, false);*/
//
//                // Move mHealth Data files to upload folder
//                // TODO check this works
//                // 2015-03-02 Qu
//                // I tested this with an upload interval 2 min, and it works properly
//                // When the flag is true, it will only move the data files for uploading the next day
//                // When the flag is false, it will copy data files for uploading, and the old zip file for uploading will be overwritten by the new one
//                if (!Globals.IS_COPY_TO_UPLOAD_DIRECTORY) {
//                    DataSender.sendMHealthToExternalUploadDir(aContext, true, false);
//                    DataSender.sendExternalSurveyLogsToExternalUploadDir(aContext, true, false);
//                } else {
//                    /* When copying the data files, we can safely include today data, as newer data will overwrite the old data on the server for today */
//                    DataSender.copyMHealthToExternalUploadDir(aContext, true, true);
//                    DataSender.copyExternalSurveyLogsToExternalUploadDir(aContext, true, true);
//                }
//            }
//            Log.i(TAG, msg);
//            ServerLogger.transmitOrQueueNote(aContext, msg, true);
//
//            // Count the files before starting so this number can be sent to server for debugging
//            int numFilesStart = DataManager.countFilesExtUploadDir();
//            Log.d(TAG, "Files before JSON zip of zips: " + numFilesStart);
//            Log.i(TAG, "Files before JSON zip of zips: " + numFilesStart);
//
//            // Zip the JSON zips so fewer uploads are required
//            DataManager.zipJSONSExternalUploads(aContext);
//            DataManager.zipJSONSInternalUploads(aContext);
//
//            numFilesStart = DataManager.countFilesExtUploadDir();
//            Log.d(TAG, "Files AFTER JSON zip of zips: " + numFilesStart);
//            Log.i(TAG, "Files AFTER JSON zip of zips: " + numFilesStart);
//
//            // Upload JSON files and remove
//            RawUploader.uploadDataFromExtUploadDir(aContext, true, true, Globals.BACKUP_UPLOADS_EXTERNAL, Globals.UPLOAD_SUCCESS_PERCENTAGE, false);
//
//            // Upload Log and SurveyLog files, backup and remove
//            int filesRemaining = RawUploader.uploadDataFromExtUploadDir(aContext, false, true, Globals.BACKUP_UPLOADS_EXTERNAL, Globals.UPLOAD_SUCCESS_PERCENTAGE, false);
//
//            msg = "Completed file upload attempt of " + numFilesStart + " files after " + String.format("%.1f", ((System.currentTimeMillis() - currentTime) / 1000.0 / 60.0))
//                    + " minutes. Files remaining to upload: " + filesRemaining;
//            ServerLogger.sendNote(aContext, msg, true);
//            Log.i(TAG, msg);
//            Log.o(TAG, "EndUpload", "InternalUploadDir", "Seconds", String.format("%.1f", ((System.currentTimeMillis() - startTime) / 1000.0 / 60.0)), "FilesRemaining", Integer.toString(filesRemaining));
//        }
//    }

    @Override
    protected void doWakefulWork(Intent intent) {
        mScheduler = PhoneServiceScheduler.getScheduler(this);
        if(intent != null){
            forceUpload = intent.getBooleanExtra("force_upload", false);
            Log.d(TAG, "Force upload: " + String.valueOf(forceUpload));
        }
        WifiManager wifimgr = null;
        try{
            //noinspection ConstantConditions
            wifimgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        } catch (NullPointerException e){
            Log.e(TAG, "Error getting WIFI system service");
        }

        // WifiManager.WIFI_MODE_FULL,"wlTag");
        WifiLock wl = null;
        if (wifimgr != null)
            wl= wifimgr.createWifiLock(3, "wlTag");

        try {
            if (wl != null)
                wl.acquire();

            mScheduler.runDeletingOperation();

                 /*
            *
            * Before gzipping the files, check if there are any baf files from the watch, if so, that means
            *
            * so decoding process has not been finished or interrupted
            *
            * */

            if(Globals.IS_WEAR_APP_ENABLED && mScheduler.checkDataSavingStatus()) {
                Log.i(TAG, "Unzip zip files from watch");
                unzipFromWatch();
            }

            Log.i(TAG, "Finish a wakeful run...");
            DataStorage.SetValue(getApplicationContext(), KEY_WAKEFUL_RUN, System.currentTimeMillis());

            // do wakeful arbitrator before calling the original upload data function so that we can overwrite any default operation
            if(Globals.myArbitrater != null){
                Log.i(TAG, "Doing wakeful arbitrator work");
                Globals.myArbitrater.doWakefulArbitrate();
            }

            // Check if need to do an upload, and do it if needed
//                uploadDataToServer(getApplicationContext());
            try {
                mScheduler.runZippingOperation(forceUpload);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
                Log.logStackTrace(TAG, e);
            }

//            mScheduler.runUploadingOperation(forceUpload);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in DataUploaderService doWakefulWork: " + e.toString());
            Log.e(TAG, e.getMessage());
            Log.logStackTrace(TAG, e);
        } finally {
            if (wl != null)
                wl.release();
            PhoneNotifier.cancel(PhoneNotifier.UPLOADING_NOTIFICATION);
        }
    }

    private void _unzipFromWatchHelper(String filePath, String fileName) throws ZipException {
        ZipFile zipFile = new ZipFile(filePath + File.separator + fileName);
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        for(FileHeader file : fileHeaders) {
            String entryPath = file.getFileName();
            Log.i(TAG, "Unzipping " + entryPath);
            if(entryPath.endsWith("baf") || entryPath.contains("event.csv")){
                String subjectID = DataStorage.GetValueString(getApplicationContext(), DataStorage.KEY_SUBJECT_ID,
                        AuthorizationChecker.SUBJECT_ID_UNDEFINED);
                entryPath = entryPath.replace("data", "data" + File.separator + subjectID);
            }
            File unzippedFile = new File(entryPath);
            if(!unzippedFile.isDirectory()) {
                unzippedFile.getParentFile().mkdirs();
                zipFile.extractFile(file, unzippedFile.getParent(), new UnzipParameters(), unzippedFile.getName());
                Log.i(TAG, "Unzipped folder: " + unzippedFile.getParent() + ", name: " + unzippedFile.getName());
            }
        }
    }


    private void unzipFromWatch() {
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
        String watchZipFolder = mHealthFormat.getExternalStorage() + File.separator + "." + Globals.STUDY_NAME +
                File.separator + "transfer";
        File watchZipFile = new File(watchZipFolder);
        watchZipFile.mkdirs();
        File[] zipFiles = new File[0];
        if(watchZipFile.isDirectory()) {
            zipFiles = watchZipFile.listFiles();
        }else{
            Log.e(TAG, "This is not a directory: " + watchZipFile.getAbsolutePath());

            if(watchZipFile.delete()){
                Log.i(TAG, "Delete and quit!");
            }else{
                Log.e(TAG, "Can't delete so just quit!");
            }
            return;
        }
        for(File zipFile : zipFiles) {
            try {
                Log.i(TAG, "Unzipping " + zipFile.getAbsolutePath());
                _unzipFromWatchHelper(watchZipFolder, zipFile.getName());
                if(zipFile.delete()){
                    Log.i(TAG, "Delete watch zip file: " + zipFile.getAbsolutePath() + " upon successfully unzipping");
                }else{
                    Log.e(TAG, "Fail to delete watch zip file: " + zipFile.getAbsolutePath() + " after successfully unzipping");
                }
            } catch (ZipException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR when unzipping from watch:" + e.getMessage());
                Log.e(TAG, "skip delete the file");
            }
        }
    }
}
