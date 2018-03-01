package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.ActivityRecognition;

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
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.study.StudyManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

import edu.neu.android.wocketslib.sensormonitor.BluetoothSensorService;

import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.activities.CurrentEEdistance;
import mhealth.neu.edu.phire.activities.FeedbackChoices;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import mhealth.neu.edu.phire.support.Util;
import mhealth.neu.edu.phire.services.AccelerationManagerService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * @author Binod Thapa Chhetry
 */
public class MinuteService extends WocketsIntentService {

    public final static String TAG = "MinuteService";

    private static final int NOTIFICATION_ID_MINUTE_SERVICE = 34001;

    private Context mContext;

//    public MinuteService() {
//        super("MinuteService");
//    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        mContext = getApplicationContext();
//        Log.i(TAG, "Inside onHandleIntent", mContext);
//        initialize();
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);
        initialize();
    }

    private void initialize() {
//        mContext = getApplicationContext();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.i(TAG, "In main thread", mContext);
        } else {
            Log.i(TAG, "Not in main thread", mContext);
        }

        if(BatteryManager.getBatteryPercentage(mContext) < 5){
            Log.i(TAG, "Battery less than 5%. Return", mContext);
            return;
        }

        boolean isStudyFinished = DataManager.isStudyFinished(mContext);

        if (isStudyFinished) {
            notifyStudyFinished();
            return;
        }

        startService(new Intent(this, AlwaysOnService.class));

//        // if audioManager is in silent mode, change it to vibrate mode
//        android.media.AudioManager audioManager = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        int ringerMode = audioManager.getRingerMode();
//        switch (ringerMode) {
//            case android.media.AudioManager.RINGER_MODE_SILENT:
//                Log.i(TAG,"Phone's ringer profile is in silent mode, switching it to vibrate mode.",mContext);
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//                break;
//        }
//        //

        String minuteServiceAlarmSetter = DataManager.getMinuteServiceAlarmSetter(mContext);
        Log.i(TAG, "Minute Service Alarm was set by - " + minuteServiceAlarmSetter, mContext);

        long minuteServiceLastRun = DataManager.getMinuteServiceLastRun(mContext);
        int timeSincePreviousRun = (int) (DateTime.getCurrentTimeInMillis() - minuteServiceLastRun) / 1000;
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


        long currentTime = DateTime.getCurrentTimeInMillis();
        long endTime = DataManager.getEndDate(mContext);

        if (currentTime > endTime) {
            notifyUserStudyEnded(endTime);
        }

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
        int memoryFree = (MemoryManager.getAvailableMemory(mContext) * 100) / MemoryManager.getTotalMemory(mContext);
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

        Log.i(TAG, "Sending message to watch to trigger alarm", mContext);
        startService(new Intent(this, SendMessageToWatch.class));

        Log.i(TAG,"Updating distance travelled",mContext);
        startService(new Intent(this,DistanceCalculationService.class));

        Log.i(TAG, "Starting ActivityRecognition", mContext);
        startService(new Intent(this, ActivityRecognitionService.class));

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

        Log.i(TAG, "Starting FilebaseDeletingService", mContext);
        startService(new Intent(this, FilebaseCleaningService.class));

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

        if(TEMPLEDataManager.getSecondPhaseActive(mContext)){
            Intent myIntent = new Intent(mContext, CurrentEEdistance.class);
            PendingIntent pIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    myIntent,
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            showMinuteServiceNotification(
                    mContext,
                    TEMPLEConstants.STUDY_NAME,
                    pIntent,
                    "Prompted: " + emaSurveysPrompted +
                            ", Completed: " + emaSurveysCompleted +
                            ", Missed: " + emaSurveysMissed,
                    R.mipmap.ic_launcher
            );
        }else{
            NotificationManager.showMinuteServiceNotification(
                            mContext,
                            TEMPLEConstants.STUDY_NAME,
                            "Prompted: " + emaSurveysPrompted +
                                    ", Completed: " + emaSurveysCompleted +
                                    ", Missed: " + emaSurveysMissed,
                            R.mipmap.ic_launcher
                    );
        }

//        Intent myIntent = new Intent(mContext, CurrentEEdistance.class);
//        PendingIntent pIntent = PendingIntent.getActivity(
//                mContext,
//                0,
//                myIntent,
//                Intent.FLAG_ACTIVITY_NEW_TASK);
//
////        NotificationManager.showMinuteServiceNotification(
//        showMinuteServiceNotification(
//                mContext,
//                TEMPLEConstants.STUDY_NAME,
//                pIntent,
//                "Prompted: " + emaSurveysPrompted +
//                        ", Completed: " + emaSurveysCompleted +
//                        ", Missed: " + emaSurveysMissed,
//                R.mipmap.ic_launcher
//        );
    }

    private void notifyUserStudyEnded(long eTime) {
        NotificationManager.showMinuteServiceNotification(
                mContext,
                TEMPLEConstants.STUDY_NAME,
                "Study ended on: " + DateTime.getTimestampString(eTime),
                R.mipmap.ic_launcher
        );

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }

    private static Notification getAlwaysOnServiceNotification(Context context, int notificationIcon, PendingIntent pIntent, String text) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                notificationIcon);
        return new Notification.Builder(context)
                .setContentTitle(DataManager.getStudyName(context))
                .setContentText(text)
                .setSmallIcon(notificationIcon)
                .setLargeIcon(icon)
                .build();
    }

    public static void showMinuteServiceNotification(Context mContext, String title, PendingIntent pendingIntent, String text, int notificationIcon) {
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                notificationIcon);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(notificationIcon)
                        .setLargeIcon(icon)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true);

        android.app.NotificationManager mNotificationManager =
                (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID_MINUTE_SERVICE, mBuilder.build());
    }
}
