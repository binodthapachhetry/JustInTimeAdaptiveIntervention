package edu.neu.mhealth.android.wockets.library.data;

import android.content.Context;
import android.os.Environment;

import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.managers.SharedPrefManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;

/**
 * @author Dharam Maniar
 */
public class DataManager {

    //--------------------------------------------------------------------------------------------//

    private static final String STUDY_NAME = "STUDY_NAME";

    public static String getStudyName(Context context) {
        return SharedPrefManager.getString(STUDY_NAME, "Wockets", context);
    }

    public static void setStudyName(String studyName, Context context) {
        SharedPrefManager.setString(STUDY_NAME, studyName, context);
    }

    //--------------------------------------------------------------------------------------------//


    private static final String STUDY = "STUDY";

    public static Study getStudy(Context context) {
        String studyString = SharedPrefManager.getString(STUDY, "", context);
        if (studyString.isEmpty()) {
            return null;
        }
        return ObjectMapper.deserialize(studyString, Study.class);
    }

    public static void setStudy(Context context, Study study) {
        String studyString = ObjectMapper.serialize(study);
        SharedPrefManager.setString(STUDY, studyString, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String SELECTED_LANGUAGE = "SELECTED_LANGUAGE";

    public static String getSelectedLanguage(Context context) {
        return SharedPrefManager.getString(SELECTED_LANGUAGE, "english", context);
    }

    public static void setSelectedLanguage(Context context, String selectedLanguage) {
        SharedPrefManager.setString(SELECTED_LANGUAGE, selectedLanguage, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String SELECTED_SURVEY_NAME = "SELECTED_SURVEY_NAME";

    public static String getSelectedSurveyName(Context context) {
        return SharedPrefManager.getString(SELECTED_SURVEY_NAME, "Survey", context);
    }

    public static void setSelectedSurveyName(Context context, String selectedSurveyName) {
        SharedPrefManager.setString(SELECTED_SURVEY_NAME, selectedSurveyName, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String SURVEY_SCHEDULE_TIME_FOR_DATE = "SURVEY_SCHEDULE_TIME_FOR_DATE";

    public static long getSurveyScheduleTimeForDateByKey(Context context, String key, String date) {
        return SharedPrefManager.getLong(SURVEY_SCHEDULE_TIME_FOR_DATE + "_" + key + "_" + date, 0, context);
    }

    public static void setSurveyScheduleTimeForDateByKey(Context context, String key, String date, long time) {
        SharedPrefManager.setLong(SURVEY_SCHEDULE_TIME_FOR_DATE + "_" + key + "_" + date, time, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String ACTIVE_PROMPT_KEY = "ACTIVE_PROMPT_KEY";

    public static String getActivePromptKey(Context context) {
        return SharedPrefManager.getString(ACTIVE_PROMPT_KEY, "", context);
    }

    public static void setActivePromptKey(Context context, String promptKey) {
        SharedPrefManager.setString(ACTIVE_PROMPT_KEY, promptKey, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String ACTIVE_PROMPT_START_TIME = "ACTIVE_PROMPT_START_TIME";

    public static long getActivePromptStartTime(Context context) {
        return SharedPrefManager.getLong(ACTIVE_PROMPT_START_TIME, -1, context);
    }

    public static void setActivePromptStartTime(Context context, long startTime) {
        SharedPrefManager.setLong(ACTIVE_PROMPT_START_TIME, startTime, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String IS_PROMPT_COMPLETE = "IS_PROMPT_COMPLETE";

    public static boolean isPromptCompleteForDate(Context context, String key, String date) {
        return SharedPrefManager.getBoolean(IS_PROMPT_COMPLETE + "_" + key + "_" + date, false, context);
    }

    public static void setPromptCompleteForDate(Context context, String key, String date) {
        SharedPrefManager.setBoolean(IS_PROMPT_COMPLETE + "_" + key + "_" + date, true, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String FIRST_PROMPT_FOR_DATE = "FIRST_PROMPT_FOR_DATE";

    public static boolean isFirstPromptForDay(Context context, String date) {
        return SharedPrefManager.getBoolean(FIRST_PROMPT_FOR_DATE + "_" + date, true, context);
    }

    public static void setFirstPromptForDate(Context context, String date) {
        SharedPrefManager.setBoolean(FIRST_PROMPT_FOR_DATE + "_" + date, false, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String FIRST_ANSWERED_PROMPT_FOR_DATE = "FIRST_ANSWERED_PROMPT_FOR_DATE";

    public static boolean isFirstAnsweredPromptForDay(Context context, String date) {
        return SharedPrefManager.getBoolean(FIRST_ANSWERED_PROMPT_FOR_DATE + "_" + date, true, context);
    }

    public static void setFirstAnsweredPromptForDate(Context context, String date) {
        SharedPrefManager.setBoolean(FIRST_ANSWERED_PROMPT_FOR_DATE + "_" + date, false, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String IS_PROMPT_ON_SCREEN = "IS_PROMPT_ON_SCREEN";

    public static boolean isPromptOnScreen(Context context) {
        return SharedPrefManager.getBoolean(IS_PROMPT_ON_SCREEN, false, context);
    }

    public static void setPromptOnScreen(Context context, boolean isPromptOnScreen) {
        SharedPrefManager.setBoolean(IS_PROMPT_ON_SCREEN, isPromptOnScreen, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String DIRECTORY_LOGS = "DIRECTORY_LOGS";

    public static void setDirectoryLogs(String directoryLogs, Context context) {
        SharedPrefManager.setString(DIRECTORY_LOGS, directoryLogs, context);
    }

    public static String getDirectoryLogs(Context context) {
        return SharedPrefManager.getString(DIRECTORY_LOGS, Environment.getExternalStorageState() + "/.WOCKETS/logs", context);
    }

    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//

    private static final String DIRECTORY_WATCH_LOGS = "DIRECTORY_WATCH_LOGS";

    public static void setDirectoryWatchLogs(String directoryWatchLogs, Context context) {
        SharedPrefManager.setString(DIRECTORY_WATCH_LOGS, directoryWatchLogs, context);
    }

    public static String getDirectoryWatchLogs(Context context) {
        return SharedPrefManager.getString(DIRECTORY_WATCH_LOGS, Environment.getExternalStorageState() + "/.WOCKETS/logs-watch", context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String DIRECTORY_SURVEYS = "DIRECTORY_SURVEYS";

    public static void setDirectorySurveys(String directorySurveys, Context context) {
        SharedPrefManager.setString(DIRECTORY_SURVEYS, directorySurveys, context);
    }

    public static String getDirectorySurveys(Context context) {
        return SharedPrefManager.getString(DIRECTORY_SURVEYS, Environment.getExternalStorageState() + "/.WOCKETS/surveys", context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String DIRECTORY_DATA = "DIRECTORY_DATA";

    public static void setDirectoryData(String directoryData, Context context) {
        SharedPrefManager.setString(DIRECTORY_DATA, directoryData, context);
    }

    public static String getDirectoryData(Context context) {
        return SharedPrefManager.getString(DIRECTORY_DATA, Environment.getExternalStorageState() + "/.WOCKETS/data", context);
    }

    //--------------------------------------------------------------------------------------------//

    //--------------------------------------------------------------------------------------------//

    private static final String DIRECTORY_WATCH_DATA = "DIRECTORY_WATCH_DATA";

    public static void setDirectoryWatchData(String directoryWatchData, Context context) {
        SharedPrefManager.setString(DIRECTORY_WATCH_DATA, directoryWatchData, context);
    }

    public static String getDirectoryWatchData(Context context) {
        return SharedPrefManager.getString(DIRECTORY_WATCH_DATA, Environment.getExternalStorageState() + "/.WOCKETS/data-watch", context);
    }

    //--------------------------------------------------------------------------------------------//


    private static final String DIRECTORY_TRANSFER = "DIRECTORY_TRANSFER";

    public static void setDirectoryTransfer(String directoryTransfer, Context context) {
        SharedPrefManager.setString(DIRECTORY_TRANSFER, directoryTransfer, context);
    }

    public static String getDirectoryTransfer(Context context) {
        return SharedPrefManager.getString(DIRECTORY_TRANSFER, Environment.getExternalStorageState() + "/.WOCKETS/transfer", context);
    }


    //--------------------------------------------------------------------------------------------//

    private static final String MINUTE_SERVICE_ALARM_SETTER = "MINUTE_SERVICE_ALARM_SETTER";

    public static void setMinuteServiceAlarmSetter(Context context, String minuteServiceAlarmSetter) {
        SharedPrefManager.setString(MINUTE_SERVICE_ALARM_SETTER, minuteServiceAlarmSetter, context);
    }

    public static String getMinuteServiceAlarmSetter(Context context) {
        return SharedPrefManager.getString(MINUTE_SERVICE_ALARM_SETTER, "", context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String MINUTE_SERVICE_LAST_RUN = "MINUTE_SERVICE_LAST_RUN";

    public static long getMinuteServiceLastRun(Context context) {
        return SharedPrefManager.getLong(MINUTE_SERVICE_LAST_RUN, 0L, context);
    }

    public static void setMinuteServiceLastRun(Context context) {
        SharedPrefManager.setLong(MINUTE_SERVICE_LAST_RUN, DateTime.getCurrentTimeInMillis(), context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String BACKUP_MINUTE_SERVICE_LAST_RUN = "BACKUP_MINUTE_SERVICE_LAST_RUN";

    public static long getBackupMinuteServiceLastRun(Context context) {
        return SharedPrefManager.getLong(BACKUP_MINUTE_SERVICE_LAST_RUN, 0L, context);
    }

    public static void setBackupMinuteServiceLastRun(Context context) {
        SharedPrefManager.setLong(BACKUP_MINUTE_SERVICE_LAST_RUN, DateTime.getCurrentTimeInMillis(), context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String FIREBASE_DATABASE_PERSISTENCE_ENABLED = "FIREBASE_DATABASE_PERSISTENCE_ENABLED";

    public static boolean getFirebaseDatabasePersistenceEnabled(Context context) {
        return SharedPrefManager.getBoolean(FIREBASE_DATABASE_PERSISTENCE_ENABLED, false, context);
    }

    public static void setFirebaseDatabasePersistenceEnabled(Context context) {
        SharedPrefManager.setBoolean(FIREBASE_DATABASE_PERSISTENCE_ENABLED, true, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String START_DATE = "START_DATE";

    public static long getStartDate(Context mContext) {
        return SharedPrefManager.getLong(START_DATE, 0, mContext);
    }

    public static void setStartDate(Context mContext, long millis) {
        SharedPrefManager.setLong(START_DATE, millis, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String END_DATE = "END_DATE";

    public static long getEndDate(Context mContext) {
        return SharedPrefManager.getLong(END_DATE, 0, mContext);
    }

    public static void setEndDate(Context mContext, long millis) {
        SharedPrefManager.setLong(END_DATE, millis, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String MEMORY_FREE = "MEMORY_FREE";

    public static int getMemoryFree(Context mContext) {
        String key = MEMORY_FREE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getInt(key, -1, mContext);
    }

    public static void setMemoryFree(Context mContext, int memoryFree) {
        String key = MEMORY_FREE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setInt(key, memoryFree, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String INTERNAL_STORAGE_FREE = "INTERNAL_STORAGE_FREE";

    public static int getInternalStorageFree(Context mContext) {
        String key = INTERNAL_STORAGE_FREE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getInt(key, -1, mContext);
    }

    public static void setInternalStorageFree(Context mContext, int internalStorageFree) {
        String key = INTERNAL_STORAGE_FREE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setInt(key, internalStorageFree, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String EXTERNAL_STORAGE_FREE = "EXTERNAL_STORAGE_FREE";

    public static int getExternalStorageFree(Context mContext) {
        String key = EXTERNAL_STORAGE_FREE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getInt(key, -1, mContext);
    }

    public static void setExternalStorageFree(Context mContext, int externalStorageFree) {
        String key = EXTERNAL_STORAGE_FREE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setInt(key, externalStorageFree, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String BATTERY_PERCENTAGE = "BATTERY_PERCENTAGE";

    public static int getBatteryPercentage(Context mContext) {
        String key = BATTERY_PERCENTAGE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getInt(key, -1, mContext);
    }

    public static void setBatteryPercentage(Context mContext, int batteryPercentage) {
        String key = BATTERY_PERCENTAGE + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setInt(key, batteryPercentage, mContext);
    }

    //--------------------------------------------------------------------------------------------//


    //--------------------------------------------------------------------------------------------//

    private static final String INTERNET_WIFI_STATUS = "INTERNET_WIFI_STATUS";

    public static int getInternetWifiStatus(Context mContext) {
        String key = INTERNET_WIFI_STATUS + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getInt(key, -1, mContext);
    }

    public static void setInternetWifiStatus(Context mContext, int internetWifiStatus) {
        String key = INTERNET_WIFI_STATUS + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setInt(key, internetWifiStatus, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String INTERNET_MOBILE_STATUS = "INTERNET_MOBILE_STATUS";

    public static int getInternetMobileStatus(Context mContext) {
        String key = INTERNET_MOBILE_STATUS + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getInt(key, -1, mContext);
    }

    public static void setInternetMobileStatus(Context mContext, int internetMobileStatus) {
        String key = INTERNET_MOBILE_STATUS + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setInt(key, internetMobileStatus, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String APP_VERSION_NAME = "APP_VERSION_NAME";

    public static String getAppVersionName(Context mContext) {
        String key = APP_VERSION_NAME + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        return SharedPrefManager.getString(key, "", mContext);
    }

    public static void setAppVersionName(Context mContext, String appVersionName) {
        String key = APP_VERSION_NAME + DateTime.getDate() + DateTime.getCurrentHourWithTimezone();
        SharedPrefManager.setString(key, appVersionName, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String SLEEP_TIME = "SLEEP_TIME";

    /**
     * Returns the sleep time in HH:MM format
     */
    public static String getSleepTime(Context mContext, String defaultSleepTime) {
        return SharedPrefManager.getString(SLEEP_TIME, defaultSleepTime, mContext);
    }

    /**
     * Save the sleep time in HH:MM format
     */
    public static void setSleepTime(Context mConText, String sleepTime) {
        SharedPrefManager.setString(SLEEP_TIME, sleepTime, mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String WAKE_TIME = "WAKE_TIME";

    /**
     * Returns the wake time in HH:MM format
     */
    public static String getWakeTime(Context mContext, String defaultWakeTime) {
        return SharedPrefManager.getString(WAKE_TIME, defaultWakeTime, mContext);
    }

    /**
     * Save the wake time in HH:MM format
     */
    public static void setWakeTime(Context mConText, String wakeTime) {
        SharedPrefManager.setString(WAKE_TIME, wakeTime, mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String LAST_INTERACTION_TIME = "LAST_INTERACTION_TIME";

    /**
     * Returns the last interaction time
     */
    public static long getLastInteractionTime(Context mContext) {
        return SharedPrefManager.getLong(LAST_INTERACTION_TIME, 0, mContext);
    }

    /**
     * Sets the last interaction time
     */
    public static void setLastInteractionTime(Context mConText, long lastInteractionTime) {
        SharedPrefManager.setLong(LAST_INTERACTION_TIME, lastInteractionTime, mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String LAST_PHONE_OFF_TIME = "LAST_PHONE_OFF_TIME";

    /**
     * Returns the last phone off time
     */
    public static long getLastPhoneOffTime(Context mContext) {
        return SharedPrefManager.getLong(LAST_PHONE_OFF_TIME, -1, mContext);
    }

    /**
     * Sets the last phone off time
     */
    public static void setLastPhoneOffTime(Context mConText, long lastPhoneOffTime) {
        SharedPrefManager.setLong(LAST_PHONE_OFF_TIME, lastPhoneOffTime, mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String LAST_PHONE_ON_TIME = "LAST_PHONE_ON_TIME";

    /**
     * Returns the last phone on time
     */
    public static long getLastPhoneOnTime(Context mContext) {
        return SharedPrefManager.getLong(LAST_PHONE_ON_TIME, -1, mContext);
    }

    /**
     * Sets the last phone on time
     */
    public static void setLastPhoneOnTime(Context mConText, long lastPhoneOnTime) {
        SharedPrefManager.setLong(LAST_PHONE_ON_TIME, lastPhoneOnTime, mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String LAST_RUN_OF_UPLOAD_MANAGER_SERVICE = "LAST_RUN_OF_UPLOAD_MANAGER_SERVICE";

    /**
     * Returns the last run time of the upload manager service
     */
    public static long getLastRunOfUploadManagerService(Context mContext) {
        return SharedPrefManager.getLong(LAST_RUN_OF_UPLOAD_MANAGER_SERVICE, 0, mContext);
    }

    /**
     * Sets the last run time of the upload manager service
     */
    public static void setLastRunOfUploadManagerService(Context mConText) {
        SharedPrefManager.setLong(LAST_RUN_OF_UPLOAD_MANAGER_SERVICE, DateTime.getCurrentTimeInMillis(), mConText);
    }
    //--------------------------------------------------------------------------------------------//

    private static final String LAST_RUN_OF_WATCH_UPLOAD_MANAGER_SERVICE = "LAST_RUN_OF_WATCH_UPLOAD_MANAGER_SERVICE";

    /**
     * Returns the last run time of the WATCH upload manager service
     */
    public static long getLastRunOfWatchUploadManagerService(Context mContext) {
        return SharedPrefManager.getLong(LAST_RUN_OF_WATCH_UPLOAD_MANAGER_SERVICE, 0, mContext);
    }

    /**
     * Sets the last run time of the WATCH upload manager service
     */
    public static void setLastRunOfWatchUploadManagerService(Context mConText) {
        SharedPrefManager.setLong(LAST_RUN_OF_WATCH_UPLOAD_MANAGER_SERVICE, DateTime.getCurrentTimeInMillis(), mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String IS_ZIP_TRANSFER_FINISHED = "IS_ZIP_TRANSFER_FINISHED";

    /**
     * Returns true if study finish button is pressed.
     */
    public static boolean isZipTransferFinished(Context mContext) {
        return SharedPrefManager.getBoolean(IS_ZIP_TRANSFER_FINISHED, true, mContext);
    }

    /**
     * Sets the flag to indicate that the study finish button was pressed.
     */
    public static void setZipTransferFinished(Context mConText, boolean finished) {
        SharedPrefManager.setBoolean(IS_ZIP_TRANSFER_FINISHED, finished, mConText);
    }


    //--------------------------------------------------------------------------------------------//

    private static final String IS_STUDY_FINISHED = "IS_STUDY_FINISHED";

    /**
     * Returns true if study finish button is pressed.
     */
    public static boolean isStudyFinished(Context mContext) {
        return SharedPrefManager.getBoolean(IS_STUDY_FINISHED, false, mContext);
    }

    /**
     * Sets the flag to indicate that the study finish button was pressed.
     */
    public static void setStudyFinished(Context mConText) {
        SharedPrefManager.setBoolean(IS_STUDY_FINISHED, true, mConText);
    }

    //--------------------------------------------------------------------------------------------//


    private static final String EMA_SURVEY_PROMPTED_COUNT = "EMA_SURVEY_PROMPTED_COUNT";

    /**
     * Returns the number of times the participant was prompted ema surveys
     */
    public static int getEMASurveyPromptedCount(Context context) {
        return SharedPrefManager.getInt(EMA_SURVEY_PROMPTED_COUNT, 0, context);
    }

    /**
     * Increments the number of times the participant was prompted ema surveys
     */
    public static void incrementEMASurveyPromptedCount(Context context) {
        int currentCount = getEMASurveyPromptedCount(context);
        currentCount++;
        SharedPrefManager.setInt(EMA_SURVEY_PROMPTED_COUNT, currentCount, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String EMA_SURVEY_COMPLETED_COUNT = "EMA_SURVEY_COMPLETED_COUNT";

    /**
     * Returns the number of times the participant completed ema surveys
     */
    public static int getEMASurveyCompletedCount(Context mContext) {
        return SharedPrefManager.getInt(EMA_SURVEY_COMPLETED_COUNT, 0, mContext);
    }

    /**
     * Increments the number of times the participant completed ema surveys
     */
    public static void incrementEMASurveyCompletedCount(Context mConText) {
        int currentCount = getEMASurveyCompletedCount(mConText);
        currentCount++;
        SharedPrefManager.setInt(EMA_SURVEY_COMPLETED_COUNT, currentCount, mConText);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String EMA_SURVEY_PROMPTED_COUNT_FOR_DATE = "EMA_SURVEY_PROMPTED_COUNT_FOR_DATE";

    /**
     * Returns the number of times the participant was prompted ema surveys for a particular date
     */
    public static int getEMASurveyPromptedCountForDate(Context context, String date) {
        return SharedPrefManager.getInt(EMA_SURVEY_PROMPTED_COUNT_FOR_DATE + "_" + date, 0, context);
    }

    /**
     * Increments the number of times the participant was prompted ema surveys for a particular date
     */
    public static void incrementEMASurveyPromptedCountForDate(Context context, String date) {
        int currentCount = getEMASurveyPromptedCountForDate(context, date);
        currentCount++;
        SharedPrefManager.setInt(EMA_SURVEY_PROMPTED_COUNT_FOR_DATE + "_" + date, currentCount, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String EMA_SURVEY_COMPLETED_COUNT_FOR_DATE = "EMA_SURVEY_COMPLETED_COUNT_FOR_DATE";

    /**
     * Returns the number of times the participant completed ema surveys for a particular date
     */
    public static int getEMASurveyCompletedCountForDate(Context mContext, String date) {
        return SharedPrefManager.getInt(EMA_SURVEY_COMPLETED_COUNT_FOR_DATE + "_" + date, 0, mContext);
    }

    /**
     * Increments the number of times the participant completed ema surveys for a particular date
     */
    public static void incrementEMASurveyCompletedCountForDate(Context mConText, String date) {
        int currentCount = getEMASurveyCompletedCountForDate(mConText, date);
        currentCount++;
        SharedPrefManager.setInt(EMA_SURVEY_COMPLETED_COUNT_FOR_DATE + "_" + date, currentCount, mConText);
    }

    //--------------------------------------------------------------------------------------------//
}
