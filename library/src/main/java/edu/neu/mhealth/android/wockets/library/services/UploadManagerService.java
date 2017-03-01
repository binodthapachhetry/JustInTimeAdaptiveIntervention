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

        // process transferred files from watch
        unzipFromWatch();

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

        Log.i(TAG, "Processing Logs Watch", mContext);
        String logWatchDirectory = DataManager.getDirectoryWatchLogs(mContext);
        File logWatchFiles = new File(logWatchDirectory);
        if (!logWatchFiles.exists()) {
            Log.w(TAG, "Log directory does not exist - " + logDirectory, mContext);
            return;
        }
        if (logWatchFiles.listFiles() == null) {
            Log.e(TAG, "No files present in logs directory. This should never happen", mContext);
            return;
        }
        for (File logWatchDate : logWatchFiles.listFiles()) {
            processLogs(logWatchDate);
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

        Log.i(TAG, "Processing Watch Data files", mContext);
        String dataWatchDirectory = DataManager.getDirectoryWatchData(mContext);
        File dataWatchFiles = new File(dataWatchDirectory);
        if (!dataWatchFiles.exists()) {
            Log.w(TAG, "Watch Data directory does not exist", mContext);
            return;
        }

        if (dataWatchFiles.listFiles() == null) {
            Log.w(TAG, "No files present in watch data directory.", mContext);
            return;
        }

        for (File dataDate : dataWatchFiles.listFiles()) {
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

//            // make sure folder has watch data
//            File[] watchFile = finder(hourDirectory.getName());
//            if(watchFile == null){
//                Log.d(TAG, hourDirectory.getAbsolutePath() + " - Does not contain Watch sensor data");
//                continue;
//            }

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

        Log.i(TAG, "Processing Watch Log Uploads", mContext);
        String logWatchDirectory = DataManager.getDirectoryWatchLogs(mContext);
        File logWatchFiles = new File(logWatchDirectory);
        if (!logWatchFiles.exists()) {
            Log.w(TAG, "Watch Log directory does not exist - " + logDirectory, mContext);
            return;
        }

        for (File logWatchDate : logWatchFiles.listFiles()) {
            // TODO: This is a MATCH specific fix. Need to come up with a better solution
            if (DateTime.getDate(logWatchDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
                Log.i(TAG, "Ignoring logs for date - " + logWatchDate.getName(), mContext);
                continue;
            }

            if (logWatchDate.isDirectory()) {
                for (File logHour : logWatchDate.listFiles()) {
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
                if (!logWatchDate.getName().contains("zip")) {
                    continue;
                }
                // We don't want to re-upload uploaded files.
                if (logWatchDate.getName().contains("uploaded")) {
                    continue;
                }

                Log.i(TAG, "Calling UploadManager.uploadFile on - " + logWatchDate.getAbsolutePath(), mContext);
                UploadManager.uploadFile(logWatchDate.getAbsolutePath(), mContext);
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

        Log.i(TAG, "Processing Watch Data Uploads", mContext);
        String dataWatchDirectory = DataManager.getDirectoryWatchData(mContext);
        File dataWatchFiles = new File(dataWatchDirectory);
        if (!dataWatchFiles.exists()) {
            Log.w(TAG, "Watch Data directory does not exist - " + dataDirectory, mContext);
            return;
        }

        for (File dataWatchDate : dataWatchFiles.listFiles()) {
            // TODO: This is a MATCH specific fix. Need to come up with a better solution
            if (DateTime.getDate(dataWatchDate.getName()).getTime() < (DateTime.getCurrentTimeInMillis() - (DateTime.DAYS_1_IN_MILLIS * 90))) {
                Log.i(TAG, "Ignoring data for date - " + dataWatchDate.getName(), mContext);
                continue;
            }
            if (dataWatchDate.isDirectory()) {
                for (File dataHour : dataWatchDate.listFiles()) {
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
                if (!dataWatchDate.getName().contains("zip")) {
                    continue;
                }
                // We don't want to re-upload uploaded files.
                if (dataWatchDate.getName().contains("uploaded")) {
                    continue;
                }

                Log.i(TAG, "Calling UploadManager.uploadFile on - " + dataWatchDate.getAbsolutePath(), mContext);
                UploadManager.uploadFile(dataWatchDate.getAbsolutePath(), mContext);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }


    private void unzipFromWatch() {
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
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

//                if(unzippedFile.getName().endsWith("baf")) {
//                    File currentFile = new File(unzippedFile.getParent() + File.separator + unzippedFile.getName());
//                    Log.i(TAG, "Decoding sensor file: " + currentFile, mContext);
//                    try {
//                        boolean result;
//                        InputStream assetInputStream = new FileInputStream(currentFile);
//                        final byte[] b = IOUtils.toByteArray(assetInputStream);
//                        result = decodeBinarySensorFile(b, currentFile.getParent(), currentFile.getName());
//
//                        if (result == true) {
//                            Log.i(TAG, "Successfully decoded sensor file: " + currentFile.getAbsolutePath(), mContext);
//                            currentFile.delete();
//                            Log.i(TAG, "Deleted the original binary file: " + currentFile.getAbsolutePath(), mContext);
//                        } else {
//                            Log.e(TAG, "Fail to decode binary sensor file: " + currentFile.getAbsolutePath(), mContext);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

            }

        }
    }



//    private void decodePassBinaryDataFromWatch(String path){
//        String folderPath = null;
//        String filePath = path;
////        try {
////            folderPath = mHealthFormat.buildmHealthPath(new Date(), mHealthFormat.PATH_LEVEL.YEARLY, mHealthFormat.ROOT_DIRECTORY.MASTER);
////            List<String> filepaths = null;
////            filepaths = FileHelper.listDirectory(new File(folderPath), ".baf");
////            if (filepaths != null)
////                for(String filepath : filepaths){
//                    File currentFile = new File(folderPath + File.separator + filepath.substring(5));
//                    Date currentDate = mHealthFormat.extractDateFromFilename(currentFile.getName());
//
//                    if(currentFile.exists() && DateHelper.isHourBefore(currentDate)) {
//                        Log.i(TAG, "Decoding sensor file: " + currentFile.getAbsolutePath(),mContext);
//                        boolean result;
//                        InputStream assetInputStream = new FileInputStream(currentFile);
//                        final byte[] b = IOUtils.toByteArray(assetInputStream);
//                        result = decodeBinarySensorFile(b, currentFile.getParent(), currentFile.getName(),mContext);
//
//                        if(result == true){
//                            Log.i(TAG, "Successfully decoded sensor file: " + currentFile.getAbsolutePath(),mContext);
//                            currentFile.delete();
//                            Log.i(TAG, "Deleted the original binary file: " + currentFile.getAbsolutePath(),mContext);
//                        }else{
//                            Log.e(TAG, "Fail to decode binary sensor file: " + currentFile.getAbsolutePath(),mContext);
//                        }
//                    }
//                }
//        }
//        catch (FileNotFoundException e){
//            e.printStackTrace();
//            Log.e(TAG, "File not found " + e.getMessage(),mContext);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Error in decodePassBinaryDataFromWatch: " + e.getMessage(),mContext);
//        }catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "Error: can't build mhealth path successfully, please initialize properly",mContext);
//        }
//    }


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
                Log.e(TAG, "IO error when decoding binary from watch, skip current bits and just to next 20 bits",mContext);
                Log.e(TAG, e.getMessage(),mContext);
            }
        }
        try {
            Log.e(TAG, "Before flush",mContext);
            accelRaw.flushAndCloseCsv();
        }catch(IOException e){
            Log.e(TAG, "IO error when closing the buffered writer when decoding binary from watch",mContext);
            Log.e(TAG, e.getMessage(),mContext);
        }
        Log.i(TAG, "Decoding file time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds",mContext);
        return result;
    }


    public File[] finder( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.startsWith("AndroidWearWatch"); }
        } );

    }


}
