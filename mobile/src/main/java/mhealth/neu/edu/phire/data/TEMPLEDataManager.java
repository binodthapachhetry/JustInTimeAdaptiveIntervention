package mhealth.neu.edu.phire.data;

import android.content.Context;

import edu.neu.mhealth.android.wockets.library.managers.SharedPrefManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;

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

    private static final String LAST_DISTANCE_CALC_TIME = "LAST_DISTANCE_CALC_TIME";

    public static long getLastDistanceCalcTime(Context context) {
        return SharedPrefManager.getLong(LAST_DISTANCE_CALC_TIME, -1, context);
    }

    public static void setLastDistanceCalcTime(Context context, long stopTime) {
        SharedPrefManager.setLong(LAST_DISTANCE_CALC_TIME, stopTime, context);
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
        return SharedPrefManager.getString(PARTICIPANT_GENDER , "male", mContext);
    }

    public static void setParticipantGender(Context mContext, String participantGender) {
        SharedPrefManager.setString(PARTICIPANT_GENDER, participantGender, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_WEIGHT = "PARTICIPANT_WEIGHT";

    public static String getParticipantWeight(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_WEIGHT , "175", mContext);
    }

    public static void setParticipantWeight(Context mContext, String participantWeight) {
        SharedPrefManager.setString(PARTICIPANT_WEIGHT, participantWeight, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_HEIGHT_FT = "PARTICIPANT_HEIGHT_FT";

    public static String getParticipantHeightFt(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_HEIGHT_FT , "5", mContext);
    }

    public static void setParticipantHeightFt(Context mContext, String participantHeightFt) {
        SharedPrefManager.setString(PARTICIPANT_HEIGHT_FT, participantHeightFt, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_HEIGHT_IN = "PARTICIPANT_HEIGHT_IN";

    public static String getParticipantHeightIn(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_HEIGHT_IN , "7", mContext);
    }

    public static void setParticipantHeightIn(Context mContext, String participantHeightIn) {
        SharedPrefManager.setString(PARTICIPANT_HEIGHT_IN, participantHeightIn, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_COMPLETENESS = "PARTICIPANT_COMPLETENESS";

    public static String getParticipantCompleteness(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_COMPLETENESS , "complete", mContext);
    }

    public static void setParticipantCompleteness(Context mContext, String participantCompleteness) {
        SharedPrefManager.setString(PARTICIPANT_COMPLETENESS, participantCompleteness, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PARTICIPANT_SCI_LEVEL = "PARTICIPANT_SCI_LEVEL";

    public static String getParticipantSciLevel(Context mContext){
        return SharedPrefManager.getString(PARTICIPANT_SCI_LEVEL , "paraplagia", mContext);
    }

    public static void setParticipantSciLevel(Context mContext, String participantSciLevel) {
        SharedPrefManager.setString(PARTICIPANT_SCI_LEVEL, participantSciLevel, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String DISTANCE_CALCULATION = "DISTANCE_CALCULATION";

    public static String getDistanceCalculation(Context mContext){
        return SharedPrefManager.getString(DISTANCE_CALCULATION , "Speed", mContext);
    }

    public static void setDistanceCalculation(Context mContext, String distanceCalculation) {
        SharedPrefManager.setString(DISTANCE_CALCULATION, distanceCalculation, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String DATA_TRANSFER = "DATA_TRANSFER";

    public static boolean onlyWifi(Context mContext){
        return SharedPrefManager.getBoolean(DATA_TRANSFER , false, mContext);
    }

    public static void setOnlyWifi(Context mContext, Boolean wifi) {
        SharedPrefManager.setBoolean(DATA_TRANSFER, wifi, mContext);
    }


    //--------------------------------------------------------------------------------------------//

    private static final String ENERGY_EXPENDITURE_CALCULATION_LAST_RUN = "ENERGY_EXPENDITURE_CALCULATION_LAST_RUN";

    public static long getEECalculationLastRun(Context context) {
        return SharedPrefManager.getLong(ENERGY_EXPENDITURE_CALCULATION_LAST_RUN, 0L, context);
    }

    public static void setEECalculationLastRun(Context context) {
        SharedPrefManager.setLong(ENERGY_EXPENDITURE_CALCULATION_LAST_RUN, DateTime.getCurrentTimeInMillis(), context);
    }


    //--------------------------------------------------------------------------------------------//
    private static final String ENERGY_EXPENDITURE_KCAL = "ENERGY_EXPENDITURE_KCAL";

    public static String getEEKcal(Context mContext){
        return SharedPrefManager.getString(ENERGY_EXPENDITURE_KCAL , "0", mContext);
    }

    public static void setEEKcal(Context mContext, String energyExpenditureKCal) {
        SharedPrefManager.setString(ENERGY_EXPENDITURE_KCAL, energyExpenditureKCal, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String GOAL_ENERGY_EXPENDITURE_KCAL = "GOAL_ENERGY_EXPENDITURE_KCAL";

    public static String getGoalEEKcal(Context mContext){
        return SharedPrefManager.getString(GOAL_ENERGY_EXPENDITURE_KCAL , "0", mContext);
    }

    public static void setGoalEEKcal(Context mContext, String goalEnergyExpenditureKCal) {
        SharedPrefManager.setString(GOAL_ENERGY_EXPENDITURE_KCAL, goalEnergyExpenditureKCal, mContext);
    }


    //--------------------------------------------------------------------------------------------//
    private static final String DISTANCE_TRAVELLED_METER = "DISTANCE_TRAVELLED_METER";

    public static String getDistanceTravelledMeter(Context mContext){
        return SharedPrefManager.getString(DISTANCE_TRAVELLED_METER , "0", mContext);
    }

    public static void setDistanceTravelledMeter(Context mContext, String distanceTravelledMeter) {
        SharedPrefManager.setString(DISTANCE_TRAVELLED_METER, distanceTravelledMeter, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String GOAL_DISTANCE_TRAVELLED_MILES = "GOAL_DISTANCE_TRAVELLED_MILES";

    public static String getGoaldistanceTravelledMiles(Context mContext){
        return SharedPrefManager.getString(GOAL_DISTANCE_TRAVELLED_MILES , "", mContext);
    }

    public static void setGoaldistanceTravelledMiles(Context mContext, String goalDistanceTravelledMiles) {
        SharedPrefManager.setString(GOAL_DISTANCE_TRAVELLED_MILES, goalDistanceTravelledMiles, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String FILEBASE_CLEANING_LAST_RUN = "FILEBASE_CLEANING_LAST_RUN ";

    public static long getFilebaseCleaningLastRun(Context context) {
        return SharedPrefManager.getLong(FILEBASE_CLEANING_LAST_RUN, 0, context);
    }

    public static void setFilebaseCleaningLastRun(Context context) {
        SharedPrefManager.setLong(FILEBASE_CLEANING_LAST_RUN, DateTime.getCurrentTimeInMillis(), context);
    }




}