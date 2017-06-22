package mhealth.neu.edu.phire.data;

import android.content.Context;

import edu.neu.mhealth.android.wockets.library.managers.SharedPrefManager;

/**
 * @author Dharam Maniar
 */

public class TEMPLEDataManager {

    //--------------------------------------------------------------------------------------------//

    private static final String SALIVA_SURVEY_PROMPTED_COUNT = "SALIVA_SURVEY_PROMPTED_COUNT";

    /**
     * Returns the number of times the participant was prompted saliva surveys
     */
    public static int getSalivaSurveyPromptedCount(Context context) {
        return SharedPrefManager.getInt(SALIVA_SURVEY_PROMPTED_COUNT, 0, context);
    }

    /**
     * Increments the number of times the participant was prompted saliva surveys
     */
    public static void incrementSalivaSurveyPromptedCount(Context context) {
        int currentCount = getSalivaSurveyPromptedCount(context);
        currentCount++;
        SharedPrefManager.setInt(SALIVA_SURVEY_PROMPTED_COUNT, currentCount, context);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String SALIVA_SURVEY_COMPLETED_COUNT = "SALIVA_SURVEY_COMPLETED_COUNT";

    /**
     * Returns the number of times the participant completed saliva surveys
     */
    public static int getSalivaSurveyCompletedCount(Context mContext) {
        return SharedPrefManager.getInt(SALIVA_SURVEY_COMPLETED_COUNT, 0, mContext);
    }

    /**
     * Increments the number of times the participant completed saliva surveys
     */
    public static void incrementSalivaSurveyCompletedCount(Context mContext) {
        int currentCount = getSalivaSurveyCompletedCount(mContext);
        currentCount++;
        SharedPrefManager.setInt(SALIVA_SURVEY_COMPLETED_COUNT, currentCount, mContext);
    }

    //--------------------------------------------------------------------------------------------//

    private static final String WHEEL_DIAMETER_CM = "WHEEL_DIAMETER_CM";

    public static String getWheelDiameterCm(Context mContext) {
        return SharedPrefManager.getString(WHEEL_DIAMETER_CM, "", mContext);
    }

    public static void setWheelDiameterCm(Context mContext, String wheelDiamaterCm) {
        SharedPrefManager.setString(WHEEL_DIAMETER_CM, wheelDiamaterCm, mContext);
    }

    //--------------------------------------------------------------------------------------------//


    //--------------------------------------------------------------------------------------------//

    private static final String PANOBIKE_SENSOR_ID = "PANOBIKE_SENSOR_ID";

    public static String getPanoBikeSensorId(Context mContext) {
        return SharedPrefManager.getString(PANOBIKE_SENSOR_ID , "", mContext);
    }

    public static void setPanoBikeSensorId(Context mContext, String panobikeSensorId) {
        SharedPrefManager.setString(PANOBIKE_SENSOR_ID, panobikeSensorId, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String CONNECTED_TO_PANOBIKE_SENSOR = "CONNECTED_TO_PANOBIKE_SENSOR";

    public static boolean getPanoBikeConnectionStatus(Context mContext){
        return SharedPrefManager.getBoolean(CONNECTED_TO_PANOBIKE_SENSOR , false, mContext);
    }

    public static void setPanoBikeConnectionStatus(Context mContext, boolean panobikeConnectionStatus) {
        SharedPrefManager.setBoolean(CONNECTED_TO_PANOBIKE_SENSOR, panobikeConnectionStatus, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PANOBIKE_LAST_CONNECTED_TIME = "PANOBIKE_LAST_CONNECTED_TIME";

    public static String getPanoBikeLastConnectionTime(Context mContext){
        return SharedPrefManager.getString(PANOBIKE_LAST_CONNECTED_TIME , "", mContext);
    }

    public static void setPanoBikeLastConnectionTime(Context mContext, String panobikeLastConnectionTime) {
        SharedPrefManager.setString(PANOBIKE_LAST_CONNECTED_TIME, panobikeLastConnectionTime, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String WATCH_LAST_CONNECTED_TIME = "WATCH_LAST_CONNECTED_TIME";

    public static String getWatchLastConnectionTime(Context mContext){
        return SharedPrefManager.getString(WATCH_LAST_CONNECTED_TIME , "", mContext);
    }

    public static void setWatchLastConnectionTime(Context mContext, String watchLastConnectionTime) {
        SharedPrefManager.setString(WATCH_LAST_CONNECTED_TIME, watchLastConnectionTime, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_AGE = "PARTICIPANT_AGE";

    public static String getParticipantAge(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_AGE , "", mContext);
    }

    public static void setParticipantAge(Context mContext, String participantAge) {
        SharedPrefManager.setString(PARTICIPANT_AGE, participantAge, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_GENDER = "PARTICIPANT_GENDER";

    public static String getParticipantGender(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_GENDER , "", mContext);
    }

    public static void setParticipantGender(Context mContext, String participantGender) {
        SharedPrefManager.setString(PARTICIPANT_GENDER, participantGender, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_WEIGHT = "PARTICIPANT_WEIGHT";

    public static String getParticipantWeight(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_WEIGHT , "", mContext);
    }

    public static void setParticipantWeight(Context mContext, String participantWeight) {
        SharedPrefManager.setString(PARTICIPANT_WEIGHT, participantWeight, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_HEIGHT_FT = "PARTICIPANT_HEIGHT_FT";

    public static String getParticipantHeightFt(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_HEIGHT_FT , "", mContext);
    }

    public static void setParticipantHeightFt(Context mContext, String participantHeightFt) {
        SharedPrefManager.setString(PARTICIPANT_HEIGHT_FT, participantHeightFt, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_HEIGHT_IN = "PARTICIPANT_HEIGHT_IN";

    public static String getParticipantHeightIn(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_HEIGHT_IN , "", mContext);
    }

    public static void setParticipantHeightIn(Context mContext, String participantHeightIn) {
        SharedPrefManager.setString(PARTICIPANT_HEIGHT_IN, participantHeightIn, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_COMPLETENESS = "PARTICIPANT_COMPLETENESS";

    public static String getParticipantCompleteness(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_COMPLETENESS , "", mContext);
    }

    public static void setParticipantCompleteness(Context mContext, String participantCompleteness) {
        SharedPrefManager.setString(PARTICIPANT_COMPLETENESS, participantCompleteness, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_SCI_LEVEL = "PARTICIPANT_SCI_LEVEL";

    public static String getParticipantSciLevel(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_SCI_LEVEL , "", mContext);
    }

    public static void setParticipantSciLevel(Context mContext, String participantSciLevel) {
        SharedPrefManager.setString(PARTICIPANT_SCI_LEVEL, participantSciLevel, mContext);
    }


}