package mhealth.neu.edu.phire.services;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;
import edu.neu.mhealth.android.wockets.library.managers.AnalyticsManager;
import edu.neu.mhealth.android.wockets.library.managers.BatteryManager;
import edu.neu.mhealth.android.wockets.library.managers.ConnectivityManager;
import edu.neu.mhealth.android.wockets.library.managers.MemoryManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.managers.PowerManager;
import edu.neu.mhealth.android.wockets.library.managers.StorageManager;
//import edu.neu.mhealth.android.wockets.library.services.AccelerationManagerService;
import edu.neu.mhealth.android.wockets.library.services.LocationManagerService;
import edu.neu.mhealth.android.wockets.library.services.UploadManagerService;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.study.StudyManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

import edu.neu.android.wocketslib.sensormonitor.BluetoothSensorService;

import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.support.Util;
import mhealth.neu.edu.phire.services.AccelerationManagerService;

/**
 * @author Binod Thapa Chhetry
 */
public class MinuteService extends WocketsIntentService {

    public final static String TAG = "MinuteService";

    private Context mContext;

    @Override
    protected void onHandleIntent(Intent intent) {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Inside onCreate", getApplicationContext());
        initialize();
    }

    private void initialize() {
        mContext = getApplicationContext();

        boolean isStudyFinished = DataManager.isStudyFinished(mContext);

        if (isStudyFinished) {
            notifyStudyFinished();
            return;
        }

        startService(new Intent(this, AlwaysOnService.class));

        String minuteServiceAlarmSetter = DataManager.getMinuteServiceAlarmSetter(mContext);
        Log.i(TAG, "Minute Service Alarm was set by - " + minuteServiceAlarmSetter, mContext);

        long minuteServiceLastRun = DataManager.getMinuteServiceLastRun(mContext);
        int timeSincePreviousRun = (int)(DateTime.getCurrentTimeInMillis() - minuteServiceLastRun)/1000;
        Log.i(TAG, "Time since previous run - " + timeSincePreviousRun, mContext);

        if (timeSincePreviousRun > 0 && timeSincePreviousRun < 50) {
            Log.i(TAG, "Previous run within last 50 seconds, ignoring this run", mContext);
            return;
        }

        Log.i(TAG, "Setting Minute Service Last Run", mContext);
        DataManager.setMinuteServiceLastRun(mContext);

        Log.i(TAG, "Setting Minute Service Alarm", mContext);
        Util.setMinuteServiceAlarmFromMinuteService(mContext);

        Log.i(TAG, "Setting Minute Service Backup Alarm", mContext);
        Util.setBackupMinuteServiceAlarm(mContext);

        Log.i(TAG, "Initializing TEMPLEConstants", mContext);
        TEMPLEConstants.init(mContext);

        if (!UserManager.isUserAuthenticated(mContext)) {
            Log.i(TAG, "User is not authenticated", mContext);
            notifyUserLoggedOut();
            return;
        }

        Log.i(TAG, "Notifying user about minute service run", mContext);
        notifyUser();

        Log.i(TAG, "Writing status to Firebase Database", mContext);
        DatabaseManager.writeNote(mContext, DatabaseManager.MINUTE_SERVICE_PATH, timeSincePreviousRun);

        int prevInternetWifiStatus = DataManager.getInternetWifiStatus(mContext);
        int internetWifiStatus = ConnectivityManager.isWifiConnected(mContext) ? 1 : 0;
        if (internetWifiStatus != prevInternetWifiStatus) {
            DatabaseManager.writeNote(mContext, DatabaseManager.INTERNET_WIFI, internetWifiStatus);
            DataManager.setInternetWifiStatus(mContext, internetWifiStatus);
        }

        int prevInternetMobileStatus = DataManager.getInternetMobileStatus(mContext);
        int internetMobileStatus = ConnectivityManager.isMobileInternetConnected(mContext) ? 1 : 0;
        if (internetMobileStatus != prevInternetMobileStatus) {
            DatabaseManager.writeNote(mContext, DatabaseManager.INTERNET_MOBILE, internetMobileStatus);
            DataManager.setInternetMobileStatus(mContext, internetMobileStatus);
        }

        int prevMemoryFree = DataManager.getMemoryFree(mContext);
        int memoryFree = (MemoryManager.getAvailableMemory(mContext) * 100)/ MemoryManager.getTotalMemory(mContext);
        if (memoryFree != prevMemoryFree) {
            DatabaseManager.writeNote(mContext, DatabaseManager.MEMORY_FREE, memoryFree);
            DataManager.setMemoryFree(mContext, memoryFree);
        }

        int prevStorageInternalFree = DataManager.getInternalStorageFree(mContext);
        int storageInternalFree = StorageManager.freeInternalMemory();
        if (storageInternalFree != prevStorageInternalFree) {
            DatabaseManager.writeNote(mContext, DatabaseManager.STORAGE_INTERNAL_FREE, storageInternalFree);
            DataManager.setInternalStorageFree(mContext, storageInternalFree);
        }

        int prevStorageExternalFree = DataManager.getExternalStorageFree(mContext);
        int storageExternalFree = StorageManager.freeExternalMemory();
        if (storageExternalFree != prevStorageExternalFree) {
            DatabaseManager.writeNote(mContext, DatabaseManager.STORAGE_EXTERNAL_FREE, storageExternalFree);
            DataManager.setExternalStorageFree(mContext, storageExternalFree);
        }

        int prevBatteryPercentage = DataManager.getBatteryPercentage(mContext);
        int batteryPercentage = BatteryManager.getBatteryPercentage(mContext);
        if (batteryPercentage != prevBatteryPercentage) {
            DatabaseManager.writeNote(mContext, DatabaseManager.BATTERY_PERCENTAGE, batteryPercentage);
            DataManager.setBatteryPercentage(mContext, batteryPercentage);
        }

        if (PowerManager.isDeviceIdleMode(mContext)) {
            DatabaseManager.writeNote(mContext, DatabaseManager.POWER_DEVICE_IDLE, 1);
        }
        if (PowerManager.isInteractive(mContext)) {
            DatabaseManager.writeNote(mContext, DatabaseManager.POWER_INTERACTIVE, 1);
        }
        if (PowerManager.isPowerSaveMode(mContext)) {
            DatabaseManager.writeNote(mContext, DatabaseManager.POWER_SAVE, 1);
        }

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            String previousVersionName = DataManager.getAppVersionName(mContext);
            if (!versionName.equals(previousVersionName)) {
                DatabaseManager.writeNote(mContext, DatabaseManager.APP_VERSION, versionName);
                DataManager.setAppVersionName(mContext, versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot get version info", e, mContext);
        }

        long startDate = DataManager.getStartDate(mContext);
        long endDate = DataManager.getEndDate(mContext);
        DatabaseManager.writeNote(mContext, DatabaseManager.CONFIG_START_DATE, DateTime.getTimestampString(startDate));
        DatabaseManager.writeNote(mContext, DatabaseManager.CONFIG_END_DATE, DateTime.getTimestampString(endDate));

        Bundle bundle = new Bundle();
        bundle.putInt("timeSincePreviousRun", timeSincePreviousRun);
        bundle.putBoolean("isInternet", ConnectivityManager.isInternetConnected(mContext));
        bundle.putBoolean("isWifi", ConnectivityManager.isWifiConnected(mContext));
        bundle.putBoolean("isMobileInternet", ConnectivityManager.isMobileInternetConnected(mContext));
        bundle.putBoolean("isCharging", BatteryManager.isCharging(mContext));
        bundle.putBoolean("isUSBCharging", BatteryManager.isUSBCharging(mContext));
        bundle.putBoolean("isACCharging", BatteryManager.isACCharging(mContext));
        bundle.putBoolean("isWirelessCharging", BatteryManager.isWirelessCharging(mContext));
        bundle.putInt("batteryPercentage", BatteryManager.getBatteryPercentage(mContext));

        AnalyticsManager.logEvent(mContext, TAG, bundle);

        Log.i(TAG, "Logging Network Status", mContext);
        ConnectivityManager.logConnectivityStatus(mContext);

        Log.i(TAG, "Logging Battery Status", mContext);
        BatteryManager.logBatteryStatus(mContext);

        Log.i(TAG, "Logging Memory Status", mContext);
        MemoryManager.logMemoryStatus(mContext);

        Log.i(TAG, "Logging Storage Status", mContext);
        StorageManager.logStorageStatus(mContext);

        Log.i(TAG, "Logging Power Status", mContext);
        PowerManager.logPowerManagerStatus(mContext);

        Log.i(TAG, "Logging Notifications Status", mContext);
        NotificationManager.logNotificationStatus(mContext);

        Log.i(TAG, "Fetching latest study data", mContext);
        StudyManager.getInstance().fetchLatestStudyData(mContext);

        Log.i(TAG, "Logging Acceleration", mContext);
        startService(new Intent(this, AccelerationManagerService.class));

        Log.i(TAG, "Starting SurveyManagerService", mContext);
        startService(new Intent(this, SurveyManagerService.class));

        Log.i(TAG, "Starting PanobikeSensorService", mContext);
        startService(new Intent(this, PanobikeSensorService.class));

        Log.i(TAG, "Logging Location", mContext);
        startService(new Intent(this, LocationManagerService.class));

        Log.i(TAG, "Starting UploadManagerService", mContext);
        startService(new Intent(this, UploadManagerService.class));

        Log.i(TAG, "Starting WatchUploadManagerService", mContext);
        startService(new Intent(this, WatchUploadManagerService.class));


    }

    private void notifyStudyFinished() {
        NotificationManager.showMinuteServiceNotification(
                mContext,
                TEMPLEConstants.STUDY_NAME,
                "Study is finished !!!",
                R.mipmap.ic_launcher
        );
    }

    private void notifyUserLoggedOut() {
        NotificationManager.showMinuteServiceNotification(
                mContext,
                TEMPLEConstants.STUDY_NAME,
                "User not signed in !!!",
                R.mipmap.ic_launcher
        );
    }

    private void notifyUser() {
        int emaSurveysPrompted = DataManager.getEMASurveyPromptedCountForDate(mContext, DateTime.getDate());
        int emaSurveysCompleted = DataManager.getEMASurveyCompletedCountForDate(mContext, DateTime.getDate());
        int emaSurveysMissed = emaSurveysPrompted - emaSurveysCompleted;
        NotificationManager.showMinuteServiceNotification(
                mContext,
                TEMPLEConstants.STUDY_NAME,
                "Prompted: " + emaSurveysPrompted +
                        ", Completed: " + emaSurveysCompleted +
                        ", Missed: " + emaSurveysMissed,
                R.mipmap.ic_launcher
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }
}
