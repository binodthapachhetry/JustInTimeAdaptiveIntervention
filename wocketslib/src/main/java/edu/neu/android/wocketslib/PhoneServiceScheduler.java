package edu.neu.android.wocketslib;

import android.content.Context;
import android.net.wifi.WifiManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.dataupload.RawUploader;
import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.ByteUtils;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.NetworkDetector;
import edu.neu.android.wocketslib.utils.PhoneInfo;
import edu.neu.android.wocketslib.utils.PhoneNotifier;
import edu.neu.android.wocketslib.utils.SharedPrefs;
import edu.neu.android.wocketslib.utils.Zipper;
import edu.neu.android.wocketslib.wear.WearNoteSender;

/**
 * Created by qutang on 6/3/15.
 */
public class PhoneServiceScheduler {

    public static final String KEY_ENABLE_SAVING_DATA = "KEY_ENABLE_SAVING_DATA";
    public static final String KEY_ENABLE_SAVING_LOG = "KEY_ENABLE_SAVING_LOG";
    public static final String KEY_LAST_SAVING_CHECK = "KEY_LAST_SAVING_CHECK";
    public static final int SAVING_DATA_STORAGE_THRESHOLD = 200;
    public static final int SAVING_DATA_RAM_THRESHOLD = 50;
    public static final int SAVING_LOG_STORAGE_THRESHOLD = 100;
    public static final int SAVING_LOG_RAM_THRESHOLD = 50;
    public static final long SAVING_CHECK_INTERVAL = 3600 * 1000;

    public static final String KEY_LAST_ZIPPING_CHECK = "KEY_LAST_ZIPPING_CHECK";
    public static final int ZIPPING_BATTERY_THRESHOLD = 20;
    public static final int ZIPPING_STORAGE_THRESHOLD = 100;
    public static final int ZIPPING_RAM_THRESHOLD = 100;
    public static final long ZIPPING_CHECK_INTERVAL = 3600 * 1000;

    public static final String KEY_LAST_UPLOADING_CHECK = "KEY_LAST_UPLOADING_CHECK";
    public static final String KEY_WIFI_MANUAL_ON = "KEY_WIFI_MANUAL_ON";
    public static final String KEY_WIFI_MANUAL_ON_TIME = "KEY_WIFI_MANUAL_ON_TIME";
    public static final String KEY_WIFI_MANUAL_SWITCH_TIME = "KEY_WIFI_MANUAL_SWITCH_TIME";
    public static final long WIFI_MANUAL_SWITCH_THRESHOLD = 3600 * 1000 * 6;
    public static final long WIFI_MANUAL_ON_THRESHOLD = 300 * 1000;
    public static final int UPLOADING_BATTERY_THRESHOLD = 20;
    public static final int UPLOADING_RAM_THRESHOLD = 100;
    public static final long UPLOADING_CHECK_INTERVAL = 3600 * 1000;

    public static final String KEY_LAST_DELETING_CHECK = "KEY_LAST_DELETING_CHECK";
    public static final int DELETING_RAM_THRESHOLD = 50;
    public static final long DELETING_CHECK_INTERVAL = 3600 * 1000 * 24;
    public static final int OUT_OF_DATE_BACKUP_THRESHOLD = 5; //days

    public static final String TAG = "PhoneServiceScheduler";

    private static PhoneServiceScheduler mScheduler;
    private Context mContext;

    private PhoneServiceScheduler(Context mContext){
        this.mContext = mContext;
    }

    public static final PhoneServiceScheduler getScheduler(Context context){
        if(mScheduler == null){
            mScheduler = new PhoneServiceScheduler(context);
        }
        return mScheduler;
    }

    public boolean checkDataSavingStatus(){
        return SharedPrefs.getBoolean(KEY_ENABLE_SAVING_DATA, true, mContext);
    }

    public boolean checkLogSavingStatus(){
        return SharedPrefs.getBoolean(KEY_ENABLE_SAVING_LOG, true, mContext);
    }

    public void runSavingOperation(){
        long ram = PhoneInfo.getAvailableRAM(mContext);
        long storage = PhoneInfo.getAvailabelStorage(mContext);
        long lastSavingCheck = SharedPrefs.getLong(KEY_LAST_SAVING_CHECK, 0, mContext);
        if(System.currentTimeMillis() - lastSavingCheck >= SAVING_CHECK_INTERVAL) {
            Log.i(TAG, "Storage: " + storage + "(" + SAVING_DATA_STORAGE_THRESHOLD + "), Ram: " + ram + "(" + SAVING_DATA_RAM_THRESHOLD + ")");
            if (storage >= SAVING_DATA_STORAGE_THRESHOLD && ram >= SAVING_DATA_RAM_THRESHOLD) {
                // only set time here, be more aggressive in checking when data or log is not being saved
                SharedPrefs.setLong(KEY_LAST_SAVING_CHECK, System.currentTimeMillis(), mContext);
                Log.i(TAG, "Enable saving data.");
                SharedPrefs.setBoolean(KEY_ENABLE_SAVING_DATA, true, mContext);
            } else {
                Log.i(TAG, "Disable saving data.");
                SharedPrefs.setBoolean(KEY_ENABLE_SAVING_DATA, false, mContext);
            }

            Log.i(TAG, "Storage: " + storage + "(" + SAVING_LOG_STORAGE_THRESHOLD + "), Ram: " + ram + "(" + SAVING_LOG_RAM_THRESHOLD + ")");
            if (storage >= SAVING_LOG_STORAGE_THRESHOLD && ram >= SAVING_LOG_RAM_THRESHOLD) {
                Log.i(TAG, "Enable saving log.");
                SharedPrefs.setBoolean(KEY_ENABLE_SAVING_LOG, true, mContext);
                WearNoteSender.sendNote("ENABLE_TRANSFER", mContext);
            } else {
                Log.i(TAG, "Disable saving log.");
                SharedPrefs.setBoolean(KEY_ENABLE_SAVING_LOG, false, mContext);
                WearNoteSender.sendNote("DISABLE_TRANSFER", mContext);
            }
        }else{
            Log.i(TAG, "Skip saving check, since last check: " + new Date(lastSavingCheck).toString());
        }
    }

    public void runZippingOperation(boolean forceZipping){
        long ram = PhoneInfo.getAvailableRAM(mContext);
        long storage = PhoneInfo.getAvailabelStorage(mContext);
        double battery = PhoneInfo.getBatteryPercentage(mContext);
        boolean isCharging = PhoneInfo.isBatteryCharging(mContext);
        long lastZippingCheck = SharedPrefs.getLong(KEY_LAST_ZIPPING_CHECK, 0, mContext);
        if(System.currentTimeMillis() - lastZippingCheck >= ZIPPING_CHECK_INTERVAL || forceZipping) {
            Log.i(TAG, "Battery: " + battery + "(" + ZIPPING_BATTERY_THRESHOLD + "), " + "Is Charging: " + isCharging + ", " + "Storage: " + storage + "(" + ZIPPING_STORAGE_THRESHOLD + "), Ram: " + ram + "(" + ZIPPING_RAM_THRESHOLD + ")");
            if((battery >= ZIPPING_BATTERY_THRESHOLD || isCharging) && storage >= ZIPPING_STORAGE_THRESHOLD && ram >= ZIPPING_RAM_THRESHOLD) {
                long startOperationTime = System.currentTimeMillis();
                SharedPrefs.setLong(KEY_LAST_ZIPPING_CHECK, System.currentTimeMillis(), mContext);
                // First zipping and moving JSON
                Log.i(TAG, "Zipping JSON for visualizer and move to upload folder");
                DataSender.sendInternalUploadDataToExternalUploadDir(mContext, false, true);


                Log.i(TAG, "Zipping previous days' LOG and move to upload folder");
                // Then zipping and moving LOGs
                DataSender.sendLogsToExternalUploadDir(mContext, true, false);

                Log.i(TAG, "Zipping Today's LOG and move to upload folder");
                // For today's LOG, copy it so the original won't be deleted
                DataSender.copyLogsToExternalUploadDir(mContext, true, true);

                // Then decoding, gzipping and zipping and moving data files

                if(Globals.IS_WEAR_APP_ENABLED && checkDataSavingStatus()) {

                    Log.i(TAG, "Decoding binary data from watch");
                    decodePassBinaryDataFromWatch();
                }

            /*
			*
			* Here we check if previous hours csv files are compressed into gzip
			*
			* Do this at the wakeful service
			*
			* */
                if(Globals.IS_GZIP_INDIVIDUAL_CSV && checkDataSavingStatus()){
                    Log.i(TAG, "Gzipping csv files");
                    gzipPassCsvData();
                }

                if (!Globals.IS_COPY_TO_UPLOAD_DIRECTORY) {
                    Log.i(TAG, "Zipping SURVEY and move to upload folder and save to Backup");
                    DataSender.sendExternalSurveyLogsToExternalUploadDir(mContext, true, false);
                    Log.i(TAG, "Zipping DATA and move to upload folder and save to Backup");
                    DataSender.sendMHealthToExternalUploadDir(mContext, true, false);
                    // also zipping today's data but only before current hour (it will copy files instead of move, this introduce some redundancy but would be much safer)
                    DataSender.sendMHealthToExternalUploadDirForToday(mContext, true);
                } else {
                    /* When copying the data files, we can safely include today data, as newer data will overwrite the old data on the server for today */
                    Log.i(TAG, "Zipping today's DATA and copy to upload folder");
                    DataSender.copyExternalSurveyLogsToExternalUploadDir(mContext, true, true);
                    Log.i(TAG, "Zipping today's DATA and copy to upload folder");
                    DataSender.copyMHealthToExternalUploadDir(mContext, true, true);
                }
                Log.i(TAG, "Zipping operation finishes in: " + (System.currentTimeMillis() - startOperationTime) / 1000.0 + " seconds");
            }else{
                Log.i(TAG, "Criterions not meet, skip zipping check, since last zipping: " + new Date(lastZippingCheck).toString());
            }
        }else{
            Log.i(TAG, "Skip zipping check, since last zipping: " + new Date(lastZippingCheck).toString());
        }

    }

//    public void runUploadingOperation(boolean forceUpload){
//        long ram = PhoneInfo.getAvailableRAM(mContext);
//        double battery = PhoneInfo.getBatteryPercentage(mContext);
//        boolean isCharging = PhoneInfo.isBatteryCharging(mContext);
//        boolean wifi = NetworkDetector.isWifiConnected(mContext);
//        boolean dataPlan = NetworkDetector.isMobileConnected(mContext);
//        boolean onlywifi = Globals.IS_UPLOAD_ONLY_WIFI;
//        String uploadFolder = Globals.APP_EXTERNAL_DIRECTORY_PATH + File.separator + "uploads";
//
//        long lastUploadingCheck = SharedPrefs.getLong(KEY_LAST_UPLOADING_CHECK, 0, mContext);
//
//        File uploadFolderFile = new File(uploadFolder);
//        if(uploadFolderFile.exists() && uploadFolderFile.list().length == 0){
//            // no files
//            Log.i(TAG, "No files found in upload folder, skip uploading");
//            if (SharedPrefs.getBoolean(KEY_WIFI_MANUAL_ON, false, mContext)) {
//                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//                wifiManager.setWifiEnabled(false);
//                SharedPrefs.setBoolean(KEY_WIFI_MANUAL_ON, false, mContext);
//                SharedPrefs.setLong(KEY_WIFI_MANUAL_ON_TIME, 0, mContext);
//                SharedPrefs.setLong(KEY_WIFI_MANUAL_SWITCH_TIME, System.currentTimeMillis(), mContext);
//                Log.i(TAG, "As wifi is turned on manually, turn it off after uploading");
//            }
//            Log.i(TAG, "Skip uploading check, since last uploading attempt: " + new Date(lastUploadingCheck).toString());
//            return;
//        }else if(!uploadFolderFile.exists()){
//            Log.i(TAG, "Upload folder doesn't exist, skip uploading");
//            Log.i(TAG, "Skip uploading check, since last uploading attempt: " + new Date(lastUploadingCheck).toString());
//            return;
//        }else{
//            // has files
//            if(SharedPrefs.getBoolean(KEY_WIFI_MANUAL_ON, false, mContext) && !wifi){
//                //wifi is on for a while but no connection
//                if(System.currentTimeMillis() - SharedPrefs.getLong(KEY_WIFI_MANUAL_ON_TIME, 0, mContext) >= WIFI_MANUAL_ON_THRESHOLD){
//                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//                    wifiManager.setWifiEnabled(false);
//                    SharedPrefs.setBoolean(KEY_WIFI_MANUAL_ON, false, mContext);
//                    SharedPrefs.setLong(KEY_WIFI_MANUAL_ON_TIME, 0, mContext);
//                    SharedPrefs.setLong(KEY_WIFI_MANUAL_SWITCH_TIME, System.currentTimeMillis(), mContext);
//                    Log.i(TAG, "Wifi has been manually on for a while but no connection, turn it off");
//                }
//            }
//        }
//
//        if(onlywifi && !wifi){
//            Log.i(TAG, "No wifi connection, halt uploading as user has set only upload under wifi");
//            PhoneNotifier.showUploadingHaltNotification(TAG, "Uploading halted, please find time connecting to WIFI (under WIFI only mode) or change to normal upload mode", mContext);
//            Log.i(TAG, "Skip uploading check, since last uploading attempt: " + new Date(lastUploadingCheck).toString());
//            return;
//        }else if(!onlywifi){
//            Log.i(TAG, "Resume to normal upload mode (Data files will only be uploaded through wifi");
//            PhoneNotifier.cancel(PhoneNotifier.UPLOADING_HALT_NOTIFICATION);
//        }else if(onlywifi && wifi){
//            PhoneNotifier.cancel(PhoneNotifier.UPLOADING_HALT_NOTIFICATION);
//        }
//
//        if(System.currentTimeMillis() - lastUploadingCheck >= UPLOADING_CHECK_INTERVAL || forceUpload) {
//
//            Log.i(TAG, "Battery: " + battery + "(" + UPLOADING_BATTERY_THRESHOLD + "), " + "ram: " + ram + "(" + UPLOADING_RAM_THRESHOLD + ")" + ", isCharging: " + isCharging + ", wifi: " + wifi + ", data Plan: " + dataPlan);
//            for(int i=0; i<4;i++){
//                //i = {JSON, LOG, SURVEY, DATA}
//
//                if((forceUpload || ((battery > UPLOADING_BATTERY_THRESHOLD || isCharging) && ram >= UPLOADING_RAM_THRESHOLD))){
//                    // check upload folder file amount
//                    if(wifi || dataPlan) {
//                        long startOperationTime = System.currentTimeMillis();
//                        SharedPrefs.setLong(KEY_LAST_UPLOADING_CHECK, System.currentTimeMillis(), mContext);
//                        switch (i) {
//                            case 0:
//                                // No need to back up json files
//                                RawUploader.uploadDataFromExtUploadDir(mContext, true, true, false, Globals.UPLOAD_SUCCESS_PERCENTAGE, true);
//                                Log.i(TAG, "JSON has been uploaded");
//                                break;
//                            case 1:
//                                RawUploader.uploadLogFromExtUploadDir(mContext, true, Globals.BACKUP_UPLOADS_EXTERNAL, Globals.UPLOAD_SUCCESS_PERCENTAGE);
//                                Log.i(TAG, "Log has been uploaded");
//                                break;
//                            case 2:
//                                RawUploader.uploadSurveyFromExtUploadDir(mContext, true, Globals.BACKUP_UPLOADS_EXTERNAL, Globals.UPLOAD_SUCCESS_PERCENTAGE);
//                                Log.i(TAG, "Survey has been uploaded");
//                                break;
//                            case 3:
//                                if (wifi) {
//                                    RawUploader.uploadSensorDataFromExtUploadDir(mContext, true, Globals.BACKUP_UPLOADS_EXTERNAL, Globals.UPLOAD_SUCCESS_PERCENTAGE);
//                                    Log.i(TAG, "Sensor data has been uploaded");
//                                }else{
//                                    //No wifi, but has files to be uploaded and dataplan, try to turn on wifi
//                                    Log.i(TAG, "Does not have wifi, has data file");
//                                    if (!SharedPrefs.getBoolean(KEY_WIFI_MANUAL_ON, false, mContext)) {
//                                        // wifi is not manually on
//                                        if (System.currentTimeMillis() - SharedPrefs.getLong(KEY_WIFI_MANUAL_SWITCH_TIME, 0, mContext) >=
//                                                WIFI_MANUAL_SWITCH_THRESHOLD) {
//                                            Log.i(TAG, "Try to turn on wifi manually");
//                                            // it's been a while since last wifi manual switch
//                                            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//                                            wifiManager.setWifiEnabled(true);
//                                            SharedPrefs.setBoolean(KEY_WIFI_MANUAL_ON, true, mContext);
//                                            SharedPrefs.setLong(KEY_WIFI_MANUAL_ON_TIME, System.currentTimeMillis(), mContext);
//                                            SharedPrefs.setLong(KEY_WIFI_MANUAL_SWITCH_TIME, System.currentTimeMillis(), mContext);
//                                        }
//                                    }
//                                }
//                                break;
//                        }
//                        Log.i(TAG, "Uploading operation finishes in: " + (System.currentTimeMillis() - startOperationTime) / 1000.0 + " seconds");
//                    }else{
//                        //No network, but has files to be uploaded, try to turn on wifi
//                        Log.i(TAG, "Does not have wifi, has data file");
//                        if (!SharedPrefs.getBoolean(KEY_WIFI_MANUAL_ON, false, mContext)) {
//                            // wifi is not manually on
//                            long lastTurnon = SharedPrefs.getLong(KEY_WIFI_MANUAL_SWITCH_TIME, 0, mContext);
//                            if (System.currentTimeMillis() - lastTurnon >=
//                                    WIFI_MANUAL_SWITCH_THRESHOLD) {
//                                Log.i(TAG, "Try to turn on wifi manually");
//                                // it's been a while since last wifi manual switch
//                                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//                                wifiManager.setWifiEnabled(true);
//                                SharedPrefs.setBoolean(KEY_WIFI_MANUAL_ON, true, mContext);
//                                SharedPrefs.setLong(KEY_WIFI_MANUAL_ON_TIME, System.currentTimeMillis(), mContext);
//                                SharedPrefs.setLong(KEY_WIFI_MANUAL_SWITCH_TIME, System.currentTimeMillis(), mContext);
//                            }else{
//                                Log.i(TAG, "Don't retry, since last turn on wifi: " + new Date(lastTurnon).toString());
//                            }
//                        }
//                    }
//                }else{
//                    Log.i(TAG, "Criterions not met, skip uploading, since last uploading attempt: " + new Date(lastUploadingCheck).toString());
//                }
//            }
//        }else{
//            Log.i(TAG, "Skip uploading check, since last uploading attempt: " + new Date(lastUploadingCheck).toString());
//        }
//    }

    public void runDeletingOperation(){
        long ram = PhoneInfo.getAvailableRAM(mContext);
        long lastDeletingCheck = SharedPrefs.getLong(KEY_LAST_DELETING_CHECK, 0, mContext);
        if(System.currentTimeMillis() - lastDeletingCheck >= DELETING_CHECK_INTERVAL){
            Log.i(TAG, "Ram: " + ram + "(" + DELETING_RAM_THRESHOLD +")");
            if(ram >= DELETING_RAM_THRESHOLD){
                SharedPrefs.setLong(KEY_LAST_DELETING_CHECK, System.currentTimeMillis(), mContext);
                Log.i(TAG, "Deleting out of date backup files");
                deleteOutOfDateBackupData();
            }else{
                Log.i(TAG, "skip deleting operation (criteria not meet), since last operation: " + new Date(lastDeletingCheck).toString());
            }
        }else{
            Log.i(TAG, "skip deleting operation, since last operation: " + new Date(lastDeletingCheck).toString());
        }
    }

    private void deleteOutOfDateBackupData() {
        File folder = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.BACKUP_DIRECTORY);
        if(!folder.isDirectory()){
            Log.i(TAG, "Folder:" + folder.getAbsolutePath() + " is not a directoy or not exists, skip deleting");
            return;
        }
        File[] outOfDateFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                Calendar currentDate = Calendar.getInstance();
                if (file.isDirectory()) {
                    try {
                        Date ref = new SimpleDateFormat(mHealthFormat.mHealthDateFormat).parse(file.getName());
                        Calendar interestDate = Calendar.getInstance();
                        interestDate.setTime(ref);
                        return DateHelper.isNDaysBefore(interestDate, currentDate, OUT_OF_DATE_BACKUP_THRESHOLD);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return false;
            }
        });
        for(File file : outOfDateFiles){
            try {
                FileUtils.deleteDirectory(file);
                Log.i(TAG, "Delete out of date backup folder: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Fail to delete back up directory: " + file.getAbsolutePath());
            }
        }
    }

    private void gzipPassCsvData(){
        String folderPath = null;
        try {
            folderPath = mHealthFormat.buildmHealthPath(new Date(), mHealthFormat.PATH_LEVEL.YEARLY, mHealthFormat.ROOT_DIRECTORY.MASTER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: can't build mhealth path successfully, please initialize properly");
            Log.logStackTrace(TAG, e);
        }
        List<String> filepaths = new ArrayList<>();
        try {
            filepaths = FileHelper.listDirectory(new File(folderPath), ".csv");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error when listing csv files");
            Log.e(TAG, e.getMessage());
            Log.logStackTrace(TAG, e);
        }
        for(String filepath : filepaths){
            File currentFile = new File(folderPath + File.separator + filepath.substring(5));
            Date currentDate = mHealthFormat.extractDateFromFilename(currentFile.getName());

            if(currentFile.exists() && DateHelper.isHourBefore(currentDate)) {
                Log.i(TAG, "Gzipping file: " + currentFile.getAbsolutePath());
                boolean result = Zipper.compressGzipFile(currentFile.getAbsolutePath(), currentFile.getAbsolutePath() + ".gz");
                if(result == true){
                    Log.i(TAG, "Deleting csv file: " + currentFile);
                    currentFile.delete();
                }
            }else{
                Log.i(TAG, "File " + currentFile.getAbsolutePath() + " doesn't exist or is today's data, skip gzipping");
            }
        }
    }

    private void decodePassBinaryDataFromWatch(){
        String folderPath = null;
        try {
            folderPath = mHealthFormat.buildmHealthPath(new Date(), mHealthFormat.PATH_LEVEL.YEARLY, mHealthFormat.ROOT_DIRECTORY.MASTER);
            List<String> filepaths = null;
            filepaths = FileHelper.listDirectory(new File(folderPath), ".baf");
            if (filepaths != null)
                for(String filepath : filepaths){
                    File currentFile = new File(folderPath + File.separator + filepath.substring(5));
                    Date currentDate = mHealthFormat.extractDateFromFilename(currentFile.getName());

                    if(currentFile.exists() && DateHelper.isHourBefore(currentDate)) {
                        Log.i(TAG, "Decoding sensor file: " + currentFile.getAbsolutePath());
                        boolean result;
                        InputStream assetInputStream = new FileInputStream(currentFile);
                        final byte[] b = IOUtils.toByteArray(assetInputStream);
                        result = decodeBinarySensorFile(b, currentFile.getParent(), currentFile.getName());

                        if(result == true){
                            Log.i(TAG, "Successfully decoded sensor file: " + currentFile.getAbsolutePath());
                            currentFile.delete();
                            Log.i(TAG, "Deleted the original binary file: " + currentFile.getAbsolutePath());
                        }else{
                            Log.e(TAG, "Fail to decode binary sensor file: " + currentFile.getAbsolutePath());
                        }
                    }
                }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            Log.e(TAG, "File not found " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in decodePassBinaryDataFromWatch: " + e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: can't build mhealth path successfully, please initialize properly");
        }
    }


    private boolean decodeBinarySensorFile(byte[] b, String path, String fileName){
        String newName = fileName.replaceAll(".baf", ".csv");
        File newFile = new File(path + File.separator + newName);
        if(newFile.exists()){
            newFile.delete();
        }
        AndroidWearAccelerometerRaw accelRaw = new AndroidWearAccelerometerRaw(mContext);

        boolean result = true;
        long startTime = System.currentTimeMillis();
        for(int i = 0; i + 20 <= b.length ;i = i+20){
            float rawx = ByteUtils.byteArray2Float(Arrays.copyOfRange(b, i, i + 4));
            float rawy = ByteUtils.byteArray2Float(Arrays.copyOfRange(b, i+4, i + 8));
            float rawz = ByteUtils.byteArray2Float(Arrays.copyOfRange(b, i+8, i + 12));
            long ts = ByteUtils.byteArray2Long(Arrays.copyOfRange(b, i + 12, i + 20));
            accelRaw.setRawx(rawx);
            accelRaw.setRawy(rawy);
            accelRaw.setRawz(rawz);
            accelRaw.setTimestamp(ts);
            try {
                accelRaw.bufferedWriteToCustomCsv(path, newName, true);
            }catch (IOException e){
                Log.e(TAG, "IO error when decoding binary from watch, skip current bits and just to next 20 bits");
                Log.e(TAG, e.getMessage());
            }
        }
        try {
            accelRaw.flushAndCloseCsv();
        }catch(IOException e){
            Log.e(TAG, "IO error when closing the buffered writer when decoding binary from watch");
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "Decoding file time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        return result;
    }
}
