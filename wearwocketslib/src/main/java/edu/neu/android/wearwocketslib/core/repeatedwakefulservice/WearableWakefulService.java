package edu.neu.android.wearwocketslib.core.repeatedwakefulservice;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.io.FileInputStream;
import java.util.zip.GZIPOutputStream;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.services.SensorManagerService;
import edu.neu.android.wearwocketslib.services.WearableFileTransferService;
import edu.neu.android.wearwocketslib.utils.log.BatteryLogger;
import edu.neu.android.wearwocketslib.utils.log.Logger;
import edu.neu.android.wearwocketslib.utils.system.DateHelper;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wearwocketslib.utils.io.FileHelper;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.io.SizeLimitedZipper;
import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.entities.event.BatteryEvent;
import edu.neu.android.wocketslib.mhealthformat.entities.mHealthEntity;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.ByteUtils;

/**
 * Created by qutang on 8/20/15.
 */
public class WearableWakefulService extends IntentService {

    public static final String TAG = "WearableWakefulService";
    public static final String KEY_WAKFUL_SERVICE_LAST_RUN = "KEY_WAKFUL_SERVICE_LAST_RUN";
    public static final String KEY_LAST_DELETE_CHECK = "KEY_LAST_DELETE_CHECK";
    public static final String KEY_LAST_SAVE_CHECK = "KEY_LAST_SAVE_CHECK";
    public static final String KEY_LAST_ZIP_ATTEMPT = "KEY_LAST_ZIP_ATTEMPT";
    public static final String KEY_LAST_TRANSFER_ATTEMPT = "KEY_LAST_TRANSFER_ATTEMPT";

    public static final long DELETE_CHECK_INTERVAL = 24 * 3600 * 1000;
    public static final long SAVE_CHECK_INTERVAL = 600 * 1000; // 600 should check more frequently so that we won't miss data when condition meets
    public static final long ZIP_ATTEMPT_INTERVAL = 3600 * 1000; // 3600
    public static final long TRANSFER_ATTEMPT_INTERVAL = 1800 * 1000; //1800
    public static final int DATA_OUT_OF_DATE_DAYS = 7;
    public static final int LOG_OUT_OF_DATE_DAYS = 7;
    public static final long DATA_ZIP_FILE_SIZE_LIMIT = 1024 * 1024 * 7; // 7 MB
    public static final int LOG_SAVING_STORAGE_THRESHOLD = 50;
    public static final int LOG_SAVING_RAM_THRESHOLD = 100;
    public static final int DATA_SAVING_STORAGE_THRESHOLD = 80;
    public static final int DATA_SAVING_RAM_THRESHOLD = 100;
    public static final int ZIP_STORAGE_THRESHOLD = 50;
    public static final int ZIP_RAM_THRESHOLD = 100;
    public static final int ZIP_BATTERY_THRESHOLD = 20;
    public static final int TRANSFER_STORAGE_THRESHOLD = 50;
    public static final int TRANSFER_RAM_THRESHOLD = 100;
    public static final int TRANSFER_BATTERY_THRESHOLD = 20;
    public static final int RESTART_RAM_THRESHOLD = 90;

    public static boolean FLAG_ENABLE_SAVE_LOG = true;
    public static boolean FLAG_ENABLE_SAVE_DATA = true;
    public static final String KEY_ENABLE_SAVE_LOG = "KEY_ENABLE_SAVE_LOG";
    public static final String KEY_ENABLE_SAVE_DATA = "KEY_ENABLE_SAVE_DATA";
    public static final String KEY_ENABLE_TRANSFER = "KEY_ENABLE_TRANSFER";

    private long startServiceTime;
    private long startTransferTime;
    private long startFlushTime;
    private Timer stopSensorManagerServiceTimer;

    private Logger logger = null;


    private Intent incomingIntent;
    private static WakefulServiceArbitrator arbitrator = null;

    private Context mContext;

    private BroadcastReceiver mTransferResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                logger.e("transfer result intent has null action", getApplicationContext());
                doneSignal.countDown();
                return;
            }
            String result = intent.getStringExtra("RESULT");
            String message = intent.getStringExtra("MESSAGE");
            String filepath = intent.getStringExtra("FILE_PATH");
            switch (result) {
                case "SUCCESS":
                    logger.i("SUCCESS:" + message + " when transfer file: " + filepath, getApplicationContext());
                    break;
                case "FAILURE":
                    logger.e("ERROR: " + message + " when transfer file: " + filepath, getApplicationContext());
                    SharedPrefs.setLong(KEY_LAST_TRANSFER_ATTEMPT, System.currentTimeMillis(), getApplicationContext());
                    break;
                case "FINISH":
                    logger.i("FINISH: " + message + " when transfer file: " + filepath, getApplicationContext());
                    break;
            }
            logger.i("Transferring operation takes: " +
                    (System.currentTimeMillis() - startTransferTime) / 1000.0 + " seconds", getApplicationContext());
            doneSignal.countDown();
        }
    };

    private BroadcastReceiver mFlushResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                logger.e("flush broadcast intent has null action", getApplicationContext());
                continueSignal.countDown();
                return;
            }
            String message = intent.getStringExtra("MESSAGE");
            switch (message) {
                case "COMPLETED":
                    logger.i("Flush is completed", getApplicationContext());
                    logger.i("Flushing takes " + (System.currentTimeMillis() - startFlushTime) / 1000.0 + " seconds", getApplicationContext());
                    if(stopSensorManagerServiceTimer != null) {
                        stopSensorManagerServiceTimer.cancel();
                        logger.i("Cancel stop timer for sensor manager service", getApplicationContext());
                    }
                    break;
                case "FAILURE":
                    logger.e("ERROR: " + "flushing initialization failes", getApplicationContext());
                    break;
                case "SERVICE_STOP":
                    logger.i("Sensor manager service stops", getApplicationContext());
                    break;
            }
            continueSignal.countDown();
        }
    };

    private CountDownLatch doneSignal;
    private CountDownLatch continueSignal;

    private static boolean isRunning = false;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public WearableWakefulService() {
        super(TAG);
        setIntentRedelivery(false);
        doneSignal = new CountDownLatch(1);
        continueSignal = new CountDownLatch(1);
        logger = new Logger(TAG);
        isRunning = false;
    }

    public static boolean isRunning(){
        return isRunning;
    }

    public static void setArbitrator(WakefulServiceArbitrator arbitrator){
        WearableWakefulService.arbitrator = arbitrator;
    }

    @Override
    public void onCreate() {
        logger.i("On Create Wakeful Service", getApplicationContext());
        startServiceTime = System.currentTimeMillis();
        Globals.init();
        SharedPrefs.setLong(KEY_WAKFUL_SERVICE_LAST_RUN, startServiceTime, getApplicationContext());

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("On Start Wakeful Service", getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        logger.i("On Handle Intent thread of Wakeful Service", getApplicationContext());
        isRunning = true;
        incomingIntent = intent;

        // boot up time
        if(Globals.BOOT_UP_TIME_HOLDER != 0) {
            SharedPrefs.setLong(Globals.LAST_BOOT_UP_TIME, Globals.BOOT_UP_TIME_HOLDER, getApplicationContext());
            logger.i("Set up boot up time: " + new Date(Globals.BOOT_UP_TIME_HOLDER).toString(), getApplicationContext());
            Globals.BOOT_UP_TIME_HOLDER = 0;
        }

        // Data delete operation
        deleteCheck();

        // Data saving operation
        dataAndLogSavingCheck();

        // periodically logging essential events
        eventLogging();

        // RAM check and decide whether to start sensor manager service
        ramCheck();

        try {
            logger.i("flushing is going on, stand by... until flush complete callback is returned", getApplicationContext());
            setStopTimerForSensorManagerService();
            continueSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Data zipping operation
        zipCheck();

        // Data transferring operation
        transferCheck();

        try {
            doneSignal.await();
            logger.i("Operations have finished, stand by... until transfer callback is returned", getApplicationContext());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        runArbitratorOperation();

        // Stop
        stopWakefulService(intent);
    }

    private void eventLogging() {
        if(Globals.IS_BATTERY_LOGGING_ENABLED){
            logger.i("Write battery event", getApplicationContext());
            boolean isCharing = BatteryLogger.isBatteryCharging(getApplicationContext());
            int level = (int) BatteryLogger.getBatteryLevel(getApplicationContext());
            Date now = new Date();
            if(mHealthEntity.verifyCurrentDate(now)) {
                BatteryEvent batteryEvent = new BatteryEvent(DeviceInfo.getBluetoothMacAddressConcated(), now, null, isCharing, level);
                batteryEvent.writeTomHealthCsv(true);
            }
        }
    }

    private void setStopTimerForSensorManagerService() {
        stopSensorManagerServiceTimer = new Timer("STOP_SENSOR_MANAGER_SERVICE", true);
        TimerTask stopTask = new TimerTask() {
            @Override
            public void run() {
                //release the stand by first
                logger.i("Release sensor manager service standby lock now", getApplicationContext());
                continueSignal.countDown();
                Intent stopIntent = new Intent(getApplicationContext(), SensorManagerService.class);
                stopIntent.setAction("STOP");
                stopService(stopIntent);
                logger.i("Stop sensor manager service now with timer", getApplicationContext());
            }
        };
        stopSensorManagerServiceTimer.schedule(stopTask, 20 * 1000);
        logger.i("Sensor manager service will stop in 20 seconds", getApplicationContext());
    }

    private void ramCheck() {
        double ram = DeviceInfo.getMemoryUsageInMB(getApplicationContext());
        if (ram < RESTART_RAM_THRESHOLD) { // restart sensor manager service
            logger.i("RAM: " + ram + " < " + RESTART_RAM_THRESHOLD + "MB, stop sensor manager service", getApplicationContext());
            Intent stopIntent = new Intent(getApplicationContext(), SensorManagerService.class);
            stopIntent.setAction("STOP");
            stopService(stopIntent);
        } else {
            logger.i("RAM: " + ram + "(" + RESTART_RAM_THRESHOLD + "), OK to start/flush sensor manager service", getApplicationContext());
            // Start Sensor manager service (initialize if it's first time or flush if it's already running
            flushSensorManagerService();
        }
    }

    private void runArbitratorOperation(){
        if(arbitrator != null){
            arbitrator.doArbitrate(incomingIntent);
        }else{
            logger.e("Arbitrator is null", getApplicationContext());
        }
    }

    private void transferCheck() {
        long lastTransferAttempt = SharedPrefs.getLong(KEY_LAST_TRANSFER_ATTEMPT, 0, getApplicationContext());
        if(System.currentTimeMillis() - lastTransferAttempt >= TRANSFER_ATTEMPT_INTERVAL){

            double battery = BatteryLogger.getBatteryLevel(getApplicationContext());
            double storage = DeviceInfo.getAvailableStorageInMB(getApplicationContext());
            double ram = DeviceInfo.getMemoryUsageInMB(getApplicationContext());
            boolean transferFlag = SharedPrefs.getBoolean(KEY_ENABLE_TRANSFER, true, getApplicationContext());

            // check battery, storage and RAM
            if(battery >= TRANSFER_BATTERY_THRESHOLD && storage >= TRANSFER_STORAGE_THRESHOLD && ram >= TRANSFER_RAM_THRESHOLD && transferFlag) {
                logger.i("battery level: " + battery + "(>=" + TRANSFER_BATTERY_THRESHOLD + "%), available space: " + storage + "(>=" + TRANSFER_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                        + "(>=" + TRANSFER_RAM_THRESHOLD + "MB): start transferring", getApplicationContext());
                startTransferTime = System.currentTimeMillis();

                // transfer operation
                transferAFile(lastTransferAttempt);
            }else {
                // no need to go on, so just terminate the entire service earlier
                logger.i("battery level: " + battery + "(<" + TRANSFER_BATTERY_THRESHOLD + "%), available space: " + storage + "(<" + TRANSFER_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                        + "(<" + TRANSFER_RAM_THRESHOLD + " MB): last transfer failure attempt time: " + new Date(lastTransferAttempt).toString(), getApplicationContext());
                stopWakefulService(incomingIntent);
                return;
            }
        } else {
            logger.i("Skip transferring attempt, since last transfer failure attempt: " + new Date(lastTransferAttempt).toString(), getApplicationContext());
            // the last operation
            stopWakefulService(incomingIntent);
        }
    }

    private void transferAFile(long lastTransferAttempt){
        // try to get a log file first
        Globals.init();
        File transferFolder = new File(Globals.TRANSFER_FOLDER);
//        File transferFolder = new File("/sdcard/.TEMPLE/transfer'");
        File[] logZips = transferFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains("log.zip");
            }
        });
        File selectedZip = null;
        if(logZips != null && logZips.length != 0){
            selectedZip = logZips[0];
        }

        if(selectedZip == null) {
            //try to get data file then
            File[] dataZips = transferFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.contains("data.zip");
                }
            });
            if (dataZips != null && dataZips.length != 0) {
                selectedZip = dataZips[0];
            }
        }

        if (selectedZip != null) {
            // register listener
            logger.i("Register transfer result broadcast manager", getApplicationContext());
            LocalBroadcastManager.getInstance(this).registerReceiver(mTransferResultReceiver, new IntentFilter("FILE_TRANSFER_RESULT"));

            //start service
            logger.i("Start file transfer service", getApplicationContext());
            Intent transferIntent = new Intent(getApplicationContext(), WearableFileTransferService.class);
            transferIntent.setAction("TRANSFER_FILE");
            transferIntent.putExtra("FILE_PATH", selectedZip.getAbsolutePath());
            startService(transferIntent);
        } else {
            logger.i("No file in the transfer folder needs to be transferred, since last failure transfer attempt: " + new Date(lastTransferAttempt).toString(), getApplicationContext());
            stopWakefulService(incomingIntent);
        }
    }

    private void zipCheck() {
        long lastZipAttempt = SharedPrefs.getLong(KEY_LAST_ZIP_ATTEMPT, 0, getApplicationContext());
        if(System.currentTimeMillis() - lastZipAttempt >= ZIP_ATTEMPT_INTERVAL){

            double battery = BatteryLogger.getBatteryLevel(getApplicationContext());
            double storage = DeviceInfo.getAvailableStorageInMB(getApplicationContext());
            double ram = DeviceInfo.getMemoryUsageInMB(getApplicationContext());

            // check battery, storage and RAM
            if(battery >= ZIP_BATTERY_THRESHOLD && storage >= ZIP_STORAGE_THRESHOLD && ram >= ZIP_RAM_THRESHOLD) {
                logger.i("battery level: " + battery + "(>=" + ZIP_BATTERY_THRESHOLD + "%), available space: " + storage + "(>=" + ZIP_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                        + "(>=" + ZIP_RAM_THRESHOLD + "MB): set zip attempt time: " + new Date().toString(), getApplicationContext());
                long startOperationTime = System.currentTimeMillis();
                SharedPrefs.setLong(KEY_LAST_ZIP_ATTEMPT, System.currentTimeMillis(), getApplicationContext());

                // zip operation
                zipLog();
                zipData();
                logger.i("Zipping operation takes: " +
                        (System.currentTimeMillis() - startOperationTime) / 1000.0 + " seconds", getApplicationContext());
            }else {
                // no need to go on, so just terminate the entire service earlier
                logger.i("battery level: " + battery + "(<" + ZIP_BATTERY_THRESHOLD + "%), available space: " + storage + "(<" + ZIP_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                        + "(<" + ZIP_RAM_THRESHOLD + "MB): last zip attempt time: " + new Date(lastZipAttempt).toString(), getApplicationContext());
                stopWakefulService(incomingIntent);
                return;
            }
        } else {
            logger.i("Skip zipping attempt, since last zip attempt: " + new Date(lastZipAttempt).toString(), getApplicationContext());
        }
    }

    private void zipLog(){
        // list each day logs
        logger.i("Start to zip log", getApplicationContext());
        File logRootFolder = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/");
        File[] previousDaysLogFolders = logRootFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean match = pathname.isDirectory();
                try {
                    Date dirDate = new SimpleDateFormat(mHealthFormat.mHealthDateFormat).parse(pathname.getName());
                    match = match && DateHelper.isAPreviousDay(dirDate);
                    return match;
                } catch (ParseException e) {
                    logger.e(e.getMessage(), getApplicationContext());
                    logger.logStackTrace(e, getApplicationContext());
                    e.printStackTrace();
                    return false;
                }
            }
        });

        File[] todayLogFolder = logRootFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean match = pathname.isDirectory();
                try {
                    Date dirDate = new SimpleDateFormat(mHealthFormat.mHealthDateFormat).parse(pathname.getName());
                    match = match && DateHelper.isToday(dirDate.getTime());
                    return match;
                } catch (ParseException e) {
                    logger.e(e.getMessage(), getApplicationContext());
                    logger.logStackTrace(e, getApplicationContext());
                    e.printStackTrace();
                    return false;
                }
            }
        });

        // previous day logs
        previousDaysLogFolders = previousDaysLogFolders == null? new File[]{} : previousDaysLogFolders;
        if(previousDaysLogFolders.length == 0){
            logger.i("No previous day logs found, skip zipping previous days' logs", getApplicationContext());
        }
        SizeLimitedZipper previousDayZipper = new SizeLimitedZipper("PREVIOUS_DAY", Globals.TRANSFER_FOLDER, "log", DATA_ZIP_FILE_SIZE_LIMIT);
        for(File previousDayLog : previousDaysLogFolders) {
            try {
                logger.i("Previous day log folder path" + previousDayLog.getPath(), getApplicationContext());
                String zipPathname = previousDayZipper.addToZip(previousDayLog.getAbsolutePath(), true, false);
                logger.i("Added " + previousDayLog.getAbsolutePath() + " to " + zipPathname, getApplicationContext());
            } catch (ZipException e) {
                logger.e("ERROR when zipping file, so skip this file: " + previousDayLog.getAbsolutePath(), getApplicationContext());
                logger.e(e.getMessage(), getApplicationContext());
                logger.logStackTrace(e, getApplicationContext());
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.e("ERROR during zipping file: " + previousDayLog.getAbsolutePath(), getApplicationContext());
                logger.e(e.getMessage(), getApplicationContext());
                logger.logStackTrace(e, getApplicationContext());
                e.printStackTrace();
                continue;
            }
        }

        // today logs
        todayLogFolder = todayLogFolder == null? new File[]{} : todayLogFolder;
        if(todayLogFolder.length == 0){
            logger.i("No today log found, skip zipping today's log", getApplicationContext());
        }
        SizeLimitedZipper todayZipper = new SizeLimitedZipper("TODAY", Globals.TRANSFER_FOLDER, "log", DATA_ZIP_FILE_SIZE_LIMIT);
        for(File todayLog : todayLogFolder) {
            try {
                logger.i("Today log folder path" + todayLog.getPath(), getApplicationContext());
                String zipPathname = todayZipper.addToZip(todayLog.getAbsolutePath(), false, true);
                logger.i("Added " + todayLog.getAbsolutePath() + " to " + zipPathname, getApplicationContext());
            } catch (ZipException e) {
                logger.e("ERROR when zipping file, so skip this file: " + todayLog.getAbsolutePath(), getApplicationContext());
                logger.e(e.getMessage(), getApplicationContext());
                logger.logStackTrace(e, getApplicationContext());
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.e("ERROR during zipping file: " + todayLog.getAbsolutePath(), getApplicationContext());
                logger.e(e.getMessage(), getApplicationContext());
                logger.logStackTrace(e, getApplicationContext());
                e.printStackTrace();
                continue;
            }
        }
    }

    private void zipData(){
        logger.i("Start to zip data", getApplicationContext());
        File dataRootFolder;
        try {
            dataRootFolder = new File(mHealthFormat.buildmHealthPath(mHealthFormat.ROOT_DIRECTORY.MASTER, mHealthFormat.PATH_LEVEL.ROOT));
        } catch (Exception e) {
            logger.e(e.getMessage(), getApplicationContext());
            logger.logStackTrace(e, getApplicationContext());
            e.printStackTrace();
            return;
        }

        // skip current hour file
        if(!dataRootFolder.isDirectory()) return;

        Collection<File> dataFiles = FileUtils.listFiles(dataRootFolder, new IOFileFilter(){
            @Override
            public boolean accept(File file) {
                if(file.isFile() && file.getName().contains("baf")) {
                    return DateHelper.isHourBefore(mHealthFormat.extractDateFromFilename(file.getName()));
                }else if(file.isFile() && file.getName().contains("event.csv")){
                    return DateHelper.isHourBefore(mHealthFormat.extractDateFromFilename(file.getName()));
                }
                return false;
            }

            @Override
            public boolean accept(File dir, String name) {
                if( name.contains("baf")) {
                    return DateHelper.isHourBefore(mHealthFormat.extractDateFromFilename(name));
                }else if(name.contains("event.csv")){
                    return DateHelper.isHourBefore(mHealthFormat.extractDateFromFilename(name));
                }
                return false;
            }
        }, FileFilterUtils.trueFileFilter());

        if(dataFiles.size() == 0){
            logger.i("No data file found, skip zipping data", getApplicationContext());
        }

        // add data file to zip
        SizeLimitedZipper dataZipper = new SizeLimitedZipper("DATA", Globals.TRANSFER_FOLDER, "data", DATA_ZIP_FILE_SIZE_LIMIT);
        for(File dataFile: dataFiles){
            try {

//                // if .baf convert to csv
//                if(dataFile.getName().endsWith("baf")) {
//                    File currentFile = new File(dataFile.getParent() + File.separator + dataFile.getName());
//                    Log.i(TAG, "Decoding sensor file: " + currentFile, mContext);
//                    try {
//                        boolean result;
//                        InputStream assetInputStream = new FileInputStream(currentFile);
//                        final byte[] b = IOUtils.toByteArray(assetInputStream);
//                        result = decodeBinarySensorFile(b, currentFile.getParent(), currentFile.getName());
//
//                        if (result == true) {
//                            Log.i(TAG, "Successfully decoded sensor file: " + currentFile.getAbsolutePath(), mContext);
//                            String newName = dataFile.getParent() + File.separator + currentFile.getName().replaceAll(".baf", ".csv");
//
//                            Log.i(TAG, "Gzipping file: " + newName,mContext);
//                            boolean resultIn = compressGzipFile(newName, newName + ".gz");
//                            if(resultIn == true){
//                                File csvDeleteFile = new File(newName);
//                                Log.i(TAG, "Deleting csv file: " + csvDeleteFile,mContext);
//                                csvDeleteFile.delete();
//                            }
//
//                            String zipPathname = dataZipper.addToZip(newName + ".gz", true, false);
//                            logger.i("Added " + newName + ".gz to " + zipPathname, getApplicationContext());
//                            currentFile.delete();
//                            Log.i(TAG, "Deleted the original binary file: " + currentFile.getAbsolutePath(), mContext);
//                        } else {
//                            Log.e(TAG, "Fail to decode binary sensor file: " + currentFile.getAbsolutePath(), mContext);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }else {

                    String zipPathname = dataZipper.addToZip(dataFile.getAbsolutePath(), true, false);
                    logger.i("Added " + dataFile.getAbsolutePath() + " to " + zipPathname, getApplicationContext());
//                }
            } catch (ZipException e) {
                logger.e("ERROR when zipping file, so skip this file: " + dataFile.getAbsolutePath(), getApplicationContext());
                logger.e(e.getMessage(), getApplicationContext());
                logger.logStackTrace(e, getApplicationContext());
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.e("ERROR during zipping file: " + dataFile.getAbsolutePath(), getApplicationContext());
                logger.e(e.getMessage(), getApplicationContext());
                logger.logStackTrace(e, getApplicationContext());
                e.printStackTrace();
                continue;
            }
        }
    }

//    @Deprecated
//    private void zipLogDeprecated(){
//        // list each day logs
//        File logRootFolder = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/");
//        File[] logDayFolders = logRootFolder.listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File pathname) {
//                return pathname.isDirectory();
//            }
//        });
//
//        // iterate each day log
//        for(File logOneDayFolder : logDayFolders){
//            String zipFilename = "PreviousDays" + ".log.zip";
//            String zipFullpath = Globals.TRANSFER_FOLDER + zipFilename;
//            ZipFile zipFile = null;
//            try {
//                zipFile = new ZipFile(zipFullpath);
//            }
//            catch (ZipException e) {
//                Log.e(TAG, e.getMessage(), getApplicationContext());
//                Log.e(TAG, "Can't create zip file object, so skip this day: " + zipFullpath, getApplicationContext());
//                e.printStackTrace();
//                continue;
//            }
//            Date logDay;
//            try {
//                logDay = new SimpleDateFormat(mHealthFormat.mHealthDateFormat).parse(logOneDayFolder.getName());
//            } catch (ParseException e) {
//                Log.e(TAG, e.getMessage(),getApplicationContext());
//                Log.e(TAG, "can't parse the log day folder, skip this day: " + logOneDayFolder.getAbsolutePath(), getApplicationContext());
//                e.printStackTrace();
//                continue;
//            }
//            if(zipFile != null){
//                File zipFileF = new File(zipFullpath);
//                if(zipFileF.exists()){
//                    // if there is already a zip file, overwrite by first delete it
//                    if(zipFileF.delete()){
//                        Log.i(TAG, "Delete today's log zip file: " + zipFullpath, getApplicationContext());
//                    }else{
//                        Log.e(TAG, "Can't delete today's log zip file (maybe in writing/reading), so skip " + zipFullpath, getApplicationContext());
//                        continue;
//                    }
//                }else{
//                    // if not existing, do nothing here
//                }
//
//                // set up zip parameter
//                ZipParameters parameters = new ZipParameters();
//                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
//                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
//                parameters.setRootFolderInZip(logRootFolder.getAbsolutePath());
//                parameters.setIncludeRootFolder(true);
//
//                // add folder to zip file
//                try {
//                    zipFile.addFolder(logOneDayFolder, parameters);
//                    Log.i(TAG, "successfully created log zip file: " + zipFullpath, getApplicationContext());
//                    // upon successful zipping, delete the input folder if it's not today's log
//                    if(!DateHelper.isToday(logDay.getTime())) {
//                        FileUtils.deleteDirectory(logOneDayFolder);
//                        Log.i(TAG, "successfully deleted input logs: " + logOneDayFolder.getAbsolutePath(), getApplicationContext());
//                    }else{
//                        Log.i(TAG, "Don't delete today's input logs: " + logOneDayFolder.getAbsolutePath(), getApplicationContext());
//                    }
//                } catch (ZipException e) {
//                    Log.e(TAG, e.getMessage(), getApplicationContext());
//                    Log.e(TAG, "error when adding log folder to zip file skip this day: " + logOneDayFolder.getAbsolutePath(), getApplicationContext());
//                    e.printStackTrace();
//                    if(zipFileF.delete()){
//                        Log.i(TAG, "Delete corrupted zip file: " + zipFileF.getAbsolutePath(), getApplicationContext());
//                    }else {
//                        Log.e(TAG, "Can't delete corrupted zip file (maybe in writing/reading), so skip: " + zipFullpath, getApplicationContext());
//                    }
//                    continue;
//                } catch (IOException e) {
//                    Log.e(TAG, e.getMessage(), getApplicationContext());
//                    Log.e(TAG, "Failed to delete the input folder (permission denied), so skip: " + logOneDayFolder.getAbsolutePath(), getApplicationContext());
//                    e.printStackTrace();
//                    continue;
//                }
//            }
//        }
//    }

    private void dataAndLogSavingCheck() {
        long lastSaveCheck = SharedPrefs.getLong(KEY_LAST_SAVE_CHECK, 0, getApplicationContext());
        boolean saveDataFlag = SharedPrefs.getBoolean(KEY_ENABLE_SAVE_DATA, FLAG_ENABLE_SAVE_DATA, getApplicationContext());
        boolean saveLogFlag = SharedPrefs.getBoolean(KEY_ENABLE_SAVE_LOG, FLAG_ENABLE_SAVE_LOG, getApplicationContext());

        if(System.currentTimeMillis() - lastSaveCheck >= SAVE_CHECK_INTERVAL || !saveLogFlag || !saveDataFlag) {
            logger.i("Start data saving status check", getApplicationContext());
            SharedPrefs.setLong(KEY_LAST_SAVE_CHECK, System.currentTimeMillis(), getApplicationContext());
            double storage = DeviceInfo.getAvailableStorageInMB(getApplicationContext());
            double ram = DeviceInfo.getMemoryUsageInMB(getApplicationContext());
            controlLogSavingSwitch(storage, ram);
            controlDataSavingSwitch(storage, ram);
        }else{
            logger.i("Skip saving check, since last check:" + new Date(lastSaveCheck).toString(), getApplicationContext());
        }
    }

    private void controlLogSavingSwitch(double storage, double ram){
        if(storage >= LOG_SAVING_STORAGE_THRESHOLD && ram >= LOG_SAVING_RAM_THRESHOLD){
            logger.i("Watch available storage: " + storage + "MB (>=" + LOG_SAVING_STORAGE_THRESHOLD + "MB)" + ", free RAM: " + ram + "MB(>=" + LOG_SAVING_RAM_THRESHOLD + "MB): enable saving log", getApplicationContext());
            FLAG_ENABLE_SAVE_LOG = true;
        }else{
            logger.i("Watch available storage: " + storage + "MB (<" + LOG_SAVING_STORAGE_THRESHOLD + "MB)" + ", free RAM: " + ram + "MB (<" + LOG_SAVING_RAM_THRESHOLD + "MB): disable saving log", getApplicationContext());
            FLAG_ENABLE_SAVE_LOG = false;
        }
        SharedPrefs.setBoolean(KEY_ENABLE_SAVE_LOG, FLAG_ENABLE_SAVE_LOG, getApplicationContext());
    }

    private void controlDataSavingSwitch(double storage, double ram){
        if(storage >= DATA_SAVING_STORAGE_THRESHOLD && ram >= DATA_SAVING_RAM_THRESHOLD){
            logger.i("Watch available storage: " + storage + "MB (>=" + DATA_SAVING_STORAGE_THRESHOLD + "MB)" + ", free RAM: " + ram + "MB (>=" + DATA_SAVING_RAM_THRESHOLD + "MB): enable saving data", getApplicationContext());
            FLAG_ENABLE_SAVE_DATA = true;
        }else{
            logger.i("Watch available storage: " + storage + "MB (<" + DATA_SAVING_STORAGE_THRESHOLD + "MB)" + ", free RAM: " + ram + "MB " + DATA_SAVING_RAM_THRESHOLD + "MB): disable saving data", getApplicationContext());
            FLAG_ENABLE_SAVE_DATA = false;
        }
        SharedPrefs.setBoolean(KEY_ENABLE_SAVE_DATA, FLAG_ENABLE_SAVE_DATA, getApplicationContext());
    }

    private void deleteCheck(){
        long lastDeleteCheck = SharedPrefs.getLong(KEY_LAST_DELETE_CHECK, 0, getApplicationContext());
        if(System.currentTimeMillis() - lastDeleteCheck >= DELETE_CHECK_INTERVAL) {
            long startOperationTime = System.currentTimeMillis();
            logger.i("Start data deleting operation", getApplicationContext());
            SharedPrefs.setLong(KEY_LAST_DELETE_CHECK, System.currentTimeMillis(), getApplicationContext());
            deleteOutOfDateData();
            deleteOutOfDateLog();
            logger.i("Data deletion operation takes: " +
                    (System.currentTimeMillis() - startOperationTime) / 1000.0 + " seconds", getApplicationContext());
        }else{
            logger.i("Skip deletion check, since last check:" + new Date(lastDeleteCheck).toString(), getApplicationContext());
        }
    }

    private void deleteOutOfDateLog() {
        logger.i("Start deleting Logs", getApplicationContext());
        Calendar now = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        try {
            File logDir = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/");

            ArrayList<File> dirs = FileHelper.getRecusiveDirs(logDir, 0);
            for(File dir : dirs){
                String[] tokens = dir.getName().split("-");

                current.set(Integer.parseInt(tokens[0]),
                            Integer.parseInt(tokens[1]) - 1,
                            Integer.parseInt(tokens[2]));

                if(DateHelper.isNDaysBefore(current, now, LOG_OUT_OF_DATE_DAYS)){
                    try{
                        FileUtils.deleteDirectory(dir);
                        logger.i("Deleted out of date log: " + dir, getApplicationContext());
                    }catch(IOException e){
                        logger.e(e.getMessage(), getApplicationContext());
                        logger.e("Failed to completely delete out of date log: " + dir + " ignore and continue", getApplicationContext());
                    }
                }
            }
        } catch (Exception e) {
            logger.e(e.getMessage(), getApplicationContext());
            logger.logStackTrace(e, getApplicationContext());
            e.printStackTrace();
        }
    }

    private void deleteOutOfDateData() {
        logger.i("Start deleting data files", getApplicationContext());
        Calendar now = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        try {
            String masterDir = mHealthFormat.buildmHealthPath(mHealthFormat.ROOT_DIRECTORY.MASTER, mHealthFormat.PATH_LEVEL.ROOT);
            logger.i("MasterDir: " + masterDir, getApplicationContext());
            ArrayList<String> dirs = FileHelper.getRecusiveDirs(masterDir, 2);
            for(String dir : dirs){
                String[] tokens = dir.split("/");
                int l = tokens.length;
                current.set(Integer.parseInt(tokens[l-3]), Integer.parseInt(tokens[l-2])-1, Integer.parseInt(tokens[l-1]));
                if(DateHelper.isNDaysBefore(current, now, DATA_OUT_OF_DATE_DAYS)){
                    try{
                        FileUtils.deleteDirectory(new File(dir));
                        logger.i("Deleted out of date data: " + dir, getApplicationContext());
                    }catch(IOException e){
                        logger.e(e.getMessage(), getApplicationContext());
                        logger.e("Failed to completely delete out of date data: " + dir + " ignore and continue", getApplicationContext());
                    }
                }
            }
        } catch (Exception e) {
            logger.e(e.getMessage(), getApplicationContext());
            e.printStackTrace();
        }
    }

    private void flushSensorManagerService(){
        // check if saving data is enabled
        boolean saveDataFlag = SharedPrefs.getBoolean(KEY_ENABLE_SAVE_DATA, FLAG_ENABLE_SAVE_DATA, getApplicationContext());
        if(!saveDataFlag){
            logger.i("No space for data saving, stop running sensor manager service", getApplicationContext());
            Intent stopIntent = new Intent(getApplicationContext(), SensorManagerService.class);
            stopIntent.putExtra("FROM", "WAKEFUL_SERVICE_DISABLE");
            if(stopService(stopIntent)){
                logger.i("Sensor manager service stopped successfully", getApplicationContext());
            }else{
                logger.i("Sensor manager service already stopped", getApplicationContext());
            }
        }else {
            logger.i("Register flush result local broadcast manager", getApplicationContext());
            LocalBroadcastManager.getInstance(this).registerReceiver(mFlushResultReceiver, new IntentFilter("FLUSH_RESULT"));
            logger.i("Flush sensor manager service", getApplicationContext());
            Intent sensorIntent = new Intent(getApplicationContext(), SensorManagerService.class);
            sensorIntent.putExtra("FROM", "WAKEFUL_SERVICE_START");
            sensorIntent.setAction("FLUSH");
            if (startService(sensorIntent) != null) {
                startFlushTime = System.currentTimeMillis();
                logger.i("Sensor manager service started successfully", getApplicationContext());
            } else {
                logger.e("Sensor manager service doesn't started successfully", getApplicationContext());
            }
        }
    }

    private void stopWakefulService(Intent intent){
        long serviceRunningTime = System.currentTimeMillis() - startServiceTime;
        logger.i("Wakeful Service has completed after: " + serviceRunningTime / 1000.0 + " seconds", getApplicationContext());
        if(WearableWakefulBroadcastReceiver.completeWakefulIntent(intent)){
            logger.i("Release wake lock", getApplicationContext());
        }else{
            logger.e("No wake lock found, so no need to release", getApplicationContext());
        }
        isRunning = false;
        stopSelf();
    }

    @Override
    public void onDestroy() {
        logger.i("On destroy wakeful service", getApplicationContext());
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTransferResultReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFlushResultReceiver);
        logger.close();
    }

    private boolean decodeBinarySensorFile(byte[] b, String path, String fileName){
        String newName = fileName.replaceAll(".baf", ".csv");
        File newFile = new File(path + File.separator + newName);

        if(newFile.exists()){
            newFile.delete();
        }
        AndroidWearAccelerometerRaw accelRaw = new AndroidWearAccelerometerRaw(mContext);
        logger.i("I am here",mContext);
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
                logger.e("IO error when decoding binary from watch, skip current bits and just to next 20 bits",mContext);
                logger.e(e.getMessage(),mContext);
            }
        }
        try {
            logger.e("Before flush",mContext);
            accelRaw.flushAndCloseCsv();
        }catch(IOException e){
            logger.e("IO error when closing the buffered writer when decoding binary from watch",mContext);
            logger.e(e.getMessage(),mContext);
        }
        logger.i("Decoding file time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds",mContext);
        return result;
    }

    public static boolean compressGzipFile(String file, String gzipFile) {
        boolean result = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPOutputStream gzipOS = null;
        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(gzipFile);
            gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.flush();
            gzipOS.close();
            gzipOS = null;
            fos.flush();
            fos.close();
            fos = null;
            fis.close();
            fis = null;
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
//            Log.e(TAG, e.getMessage(),mContext);
//            Log.e(TAG, e,mContext);
        } finally{
            try{
                if(gzipOS != null){
                    gzipOS.flush();
                    gzipOS.close();
                }
                if(fis != null){
                    fis.close();
                }
                if(fos != null){
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
