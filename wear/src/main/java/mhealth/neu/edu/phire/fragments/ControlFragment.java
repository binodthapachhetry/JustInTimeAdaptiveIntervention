/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mhealth.neu.edu.phire.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulBroadcastReceiver;
import edu.neu.android.wearwocketslib.services.WearableFileTransferService;
import edu.neu.android.wearwocketslib.utils.io.SizeLimitedZipper;
import edu.neu.android.wearwocketslib.utils.log.BatteryLogger;
import edu.neu.android.wearwocketslib.utils.system.DateHelper;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import mhealth.neu.edu.phire.PHIREArbitrater;
import mhealth.neu.edu.phire.R;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.services.SensorManagerService;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * A fragment that shows a list of DataItems received from the phone
 */
public class ControlFragment extends Fragment implements View.OnClickListener {



    private Button mTransferAllButton;
    private Button mRefreshSensorButton;
    private boolean mInitialized;
    private Context mContext;
    private Intent incomingIntent;
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

    @Override
    public void onPause() {
        super.onPause();
        logger.close();
    }

    public ControlFragment(){
        logger = new Logger(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.control_fragment, container, false);
        mTransferAllButton = (Button) view.findViewById(R.id.button_transfer_all);
        mTransferAllButton.setOnClickListener(this);
        mRefreshSensorButton = (Button) view.findViewById(R.id.button_refresh_sensor);
        mRefreshSensorButton.setOnClickListener(this);
        mInitialized = true;
        mContext = getActivity().getApplicationContext();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_transfer_all:
                if(mTransferAllButton.getText().toString().equalsIgnoreCase("Force Transfer")) {
                    transferAll();
                }
                break;
            case R.id.button_refresh_sensor:
                restartSensorListenerService();
                break;
        }
    }

    private void transferAll(){

        logger.i("Reached transfer override function", mContext);
        logger.i("Reset last transfer attempt to start forcing transfer", mContext);
        zipForceCheck();
        transferForceCheck();
        logger.i("Done with force transfer", mContext);




    }
    public void zipForceCheck() {

        double battery = BatteryLogger.getBatteryLevel(mContext);
        double storage = DeviceInfo.getAvailableStorageInMB(mContext);
        double ram = DeviceInfo.getMemoryUsageInMB(mContext);

        // check battery, storage and RAM
        if(battery >= ZIP_BATTERY_THRESHOLD && storage >= ZIP_STORAGE_THRESHOLD && ram >= ZIP_RAM_THRESHOLD) {
            logger.i("battery level: " + battery + "(>=" + ZIP_BATTERY_THRESHOLD + "%), available space: " + storage + "(>=" + ZIP_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                    + "(>=" + ZIP_RAM_THRESHOLD + "MB): set zip attempt time: " + new Date().toString(), mContext);
            long startOperationTime = System.currentTimeMillis();
            SharedPrefs.setLong(KEY_LAST_ZIP_ATTEMPT, System.currentTimeMillis(), mContext);

            // zip operation
            zipLog();
            zipData();
            logger.i("Zipping operation takes: " +
                    (System.currentTimeMillis() - startOperationTime) / 1000.0 + " seconds", mContext);
        }else {
            // no need to go on, so just terminate the entire service earlier
            logger.i("battery level: " + battery + "(<" + ZIP_BATTERY_THRESHOLD + "%), available space: " + storage + "(<" + ZIP_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                    + "(<" + ZIP_RAM_THRESHOLD + "MB): last zip attempt time: ", mContext);

            return;
        }
    }
    private void stopWakefulService(Intent intent){
        long serviceRunningTime = System.currentTimeMillis() - startServiceTime;
        logger.i("Wakeful Service has completed after: " + serviceRunningTime / 1000.0 + " seconds", mContext);


    }


    private void zipLog(){
        // list each day logs
        logger.i("Start to zip log", mContext);
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
                    logger.e(e.getMessage(), mContext);
                    logger.logStackTrace(e, mContext);
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
                    logger.e(e.getMessage(), mContext);
                    logger.logStackTrace(e, mContext);
                    e.printStackTrace();
                    return false;
                }
            }
        });

        // previous day logs
        previousDaysLogFolders = previousDaysLogFolders == null? new File[]{} : previousDaysLogFolders;
        if(previousDaysLogFolders.length == 0){
            logger.i("No previous day logs found, skip zipping previous days' logs", mContext);
        }
        SizeLimitedZipper previousDayZipper = new SizeLimitedZipper("PREVIOUS_DAY", Globals.TRANSFER_FOLDER, "log", DATA_ZIP_FILE_SIZE_LIMIT);
        for(File previousDayLog : previousDaysLogFolders) {
            try {
                logger.i("Previous day log folder path" + previousDayLog.getPath(), mContext);
                String zipPathname = previousDayZipper.addToZip(previousDayLog.getAbsolutePath(), true, false);
                logger.i("Added " + previousDayLog.getAbsolutePath() + " to " + zipPathname, mContext);
            } catch (ZipException e) {
                logger.e("ERROR when zipping file, so skip this file: " + previousDayLog.getAbsolutePath(), mContext);
                logger.e(e.getMessage(), mContext);
                logger.logStackTrace(e, mContext);
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.e("ERROR during zipping file: " + previousDayLog.getAbsolutePath(), mContext);
                logger.e(e.getMessage(), mContext);
                logger.logStackTrace(e, mContext);
                e.printStackTrace();
                continue;
            }
        }

        // today logs
        todayLogFolder = todayLogFolder == null? new File[]{} : todayLogFolder;
        if(todayLogFolder.length == 0){
            logger.i("No today log found, skip zipping today's log", mContext);
        }
        SizeLimitedZipper todayZipper = new SizeLimitedZipper("TODAY", Globals.TRANSFER_FOLDER, "log", DATA_ZIP_FILE_SIZE_LIMIT);
        for(File todayLog : todayLogFolder) {
            try {
                logger.i("Today log folder path" + todayLog.getPath(), mContext);
                String zipPathname = todayZipper.addToZip(todayLog.getAbsolutePath(), false, true);
                logger.i("Added " + todayLog.getAbsolutePath() + " to " + zipPathname, mContext);
            } catch (ZipException e) {
                logger.e("ERROR when zipping file, so skip this file: " + todayLog.getAbsolutePath(), mContext);
                logger.e(e.getMessage(), mContext);
                logger.logStackTrace(e, mContext);
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.e("ERROR during zipping file: " + todayLog.getAbsolutePath(), mContext);
                logger.e(e.getMessage(), mContext);
                logger.logStackTrace(e, mContext);
                e.printStackTrace();
                continue;
            }
        }
    }

    private void zipData(){
        logger.i("Start to zip data", mContext);
        File dataRootFolder;
        try {
            dataRootFolder = new File(mHealthFormat.buildmHealthPath(mHealthFormat.ROOT_DIRECTORY.MASTER, mHealthFormat.PATH_LEVEL.ROOT));
        } catch (Exception e) {
            logger.e(e.getMessage(), mContext);
            logger.logStackTrace(e, mContext);
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
            logger.i("No data file found, skip zipping data", mContext);
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
                logger.i("Added " + dataFile.getAbsolutePath() + " to " + zipPathname, mContext);
//                }
            } catch (ZipException e) {
                logger.e("ERROR when zipping file, so skip this file: " + dataFile.getAbsolutePath(), mContext);
                logger.e(e.getMessage(), mContext);
                logger.logStackTrace(e, mContext);
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.e("ERROR during zipping file: " + dataFile.getAbsolutePath(), mContext);
                logger.e(e.getMessage(), mContext);
                logger.logStackTrace(e, mContext);
                e.printStackTrace();
                continue;
            }
        }
    }

    public void transferForceCheck() {
        long lastTransferAttempt = SharedPrefs.getLong(KEY_LAST_TRANSFER_ATTEMPT, 0, mContext);


        double battery = BatteryLogger.getBatteryLevel(mContext);
        double storage = DeviceInfo.getAvailableStorageInMB(mContext);
        double ram = DeviceInfo.getMemoryUsageInMB(mContext);
        boolean transferFlag = SharedPrefs.getBoolean(KEY_ENABLE_TRANSFER, true, mContext);

        // check battery, storage and RAM
        if(battery >= TRANSFER_BATTERY_THRESHOLD && storage >= TRANSFER_STORAGE_THRESHOLD && ram >= TRANSFER_RAM_THRESHOLD && transferFlag) {
            logger.i("battery level: " + battery + "(>=" + TRANSFER_BATTERY_THRESHOLD + "%), available space: " + storage + "(>=" + TRANSFER_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                    + "(>=" + TRANSFER_RAM_THRESHOLD + "MB): start transferring", mContext);
            startTransferTime = System.currentTimeMillis();

            // transfer operation
            forceTransferAFile(lastTransferAttempt);
        }else {
            // no need to go on, so just terminate the entire service earlier
            logger.i("battery level: " + battery + "(<" + TRANSFER_BATTERY_THRESHOLD + "%), available space: " + storage + "(<" + TRANSFER_STORAGE_THRESHOLD + "MB), free RAM: " + ram
                    + "(<" + TRANSFER_RAM_THRESHOLD + " MB): last transfer failure attempt time: " + new Date(lastTransferAttempt).toString(), mContext);
            stopWakefulService(incomingIntent);
            return;
        }
    }


    private void forceTransferAFile(long lastTransferAttempt){
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
            logger.i("Register transfer result broadcast manager", mContext);
            //LocalBroadcastManager.getInstance(this).registerReceiver(mTransferResultReceiver, new IntentFilter("FILE_TRANSFER_RESULT"));

            //start service
            logger.i("Start file transfer service", mContext);
            Intent transferIntent = new Intent(mContext, WearableFileTransferService.class);
            transferIntent.setAction("TRANSFER_FILE");
            transferIntent.putExtra("FILE_PATH", selectedZip.getAbsolutePath());
           // startService(transferIntent);
        } else {
            logger.i("No file in the transfer folder needs to be transferred, since last failure transfer attempt: " + new Date(lastTransferAttempt).toString(), mContext);
            stopWakefulService(incomingIntent);
        }
    }

    private void cancelTransfer(){

    }

    private void restartSensorListenerService(){
        Intent intent = new Intent(mContext, SensorManagerService.class);
        mContext.stopService(intent);
        mContext.startService(intent);
    }
}
