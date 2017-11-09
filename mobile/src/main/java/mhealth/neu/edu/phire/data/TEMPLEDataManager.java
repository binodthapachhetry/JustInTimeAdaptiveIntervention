package mhealth.neu.edu.phire.data;

import android.content.Context;

import edu.neu.mhealth.android.wockets.library.managers.SharedPrefManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;

/**
 * @author Dharam Maniar
 */

public class TEMPLEDataManager {

    //--------------------------------------------------------------------------------------------//

    private static final String LAST_RUN_OF_AR_SERVICE = "LAST_RUN_OF_AR_SERVICE";

    public static long getLastRunOfARService(Context mContext) {
        return SharedPrefManager.getLong(LAST_RUN_OF_AR_SERVICE, 0, mContext);
    }

    public static void setLastRunOfARService(Context mConText) {
        SharedPrefManager.setLong(LAST_RUN_OF_AR_SERVICE, DateTime.getCurrentTimeInMillis(), mConText);
    }

    //--------------------------------------------------------------------------------------------//

    public static int getTotalEEkcal(Context context, String date) {
        return SharedPrefManager.getInt(date, 0, context);
    }
    public static void setTotalEEkcal(Context context, String date, int totalEEkcal) {
        SharedPrefManager.setInt(date, totalEEkcal, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String BOTH_PA_MINUTES = "BOTH_PA_MINUTES";

    public static int getBothPAminutes(Context context) {
        return SharedPrefManager.getInt(BOTH_PA_MINUTES, 0, context);
    }
    public static void setBothPAMinutes(Context context, int minutes) {
        SharedPrefManager.setInt(BOTH_PA_MINUTES, minutes, context);
    }
    //--------------------------------------------------------------------------------------------//
    private static final String PANO_PA_MINUTES = "PANO_PA_MINUTES";

    public static int getPanoPAminutes(Context context) {
        return SharedPrefManager.getInt(PANO_PA_MINUTES, 0, context);
    }
    public static void setPanoPAMinutes(Context context, int minutes) {
        SharedPrefManager.setInt(PANO_PA_MINUTES, minutes, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String WATCH_PA_MINUTES = "WATCH_PA_MINUTES";

    public static int getWatchPAminutes(Context context) {
        return SharedPrefManager.getInt(WATCH_PA_MINUTES, 0, context);
    }
    public static void setWatchPAMinutes(Context context, int minutes) {
        SharedPrefManager.setInt(WATCH_PA_MINUTES, minutes, context);
    }
    //--------------------------------------------------------------------------------------------//

    private static final String EE_WATCH = "EE_TIME";

    public static String getEEwatch(Context context) {
        return SharedPrefManager.getString(EE_WATCH, "0", context);
    }

    public static void setEEwatch(Context context, String eeWatch) {
        SharedPrefManager.setString(EE_WATCH, eeWatch, context);
    }
    //--------------------------------------------------------------------------------------------//

    private static final String EE_PANO = "EE_PANO";

    public static String getEEpano(Context context) {
        return SharedPrefManager.getString(EE_PANO, "0", context);
    }

    public static void setEEPano(Context context, String eePano) {
        SharedPrefManager.setString(EE_PANO, eePano, context);
    }
    //--------------------------------------------------------------------------------------------//

    private static final String EE_BOTH = "EE_BOTH";

    public static String getEEBoth(Context context) {
        return SharedPrefManager.getString(EE_BOTH, "0", context);
    }

    public static void setEEboth(Context context, String eeBoth) {
        SharedPrefManager.setString(EE_BOTH, eeBoth, context);
    }
    //--------------------------------------------------------------------------------------------//

    private static final String LAST_PA_TIME = "LAST_PA_TIME";

    public static long getLastPAtime(Context context) {
        return SharedPrefManager.getLong(LAST_PA_TIME, -1, context);
    }

    public static void setLastPAtime(Context context, long stopTime) {
        SharedPrefManager.setLong(LAST_PA_TIME, stopTime, context);
    }
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
    private static final String DAILY_PA_BOUT_LENGTH_GOAL = "DAILY_PA_BOUT_LENGTH_GOAL";

    public static int getDailyPAboutLengthGoal(Context context) {
        return SharedPrefManager.getInt(DAILY_PA_BOUT_LENGTH_GOAL, 2, context);
    }
    public static void setDailyPaBoutLengthGoal(Context context, int minutes) {
        SharedPrefManager.setInt(DAILY_PA_BOUT_LENGTH_GOAL, minutes, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String DAILY_PA_BOUT_LENGTH = "DAILY_PA_BOUT_LENGTH";

    public static int getDailyPAboutLength(Context context) {
        return SharedPrefManager.getInt(DAILY_PA_BOUT_LENGTH, 0, context);
    }
    public static void setDailyPaBoutLength(Context context, int minutes) {
        SharedPrefManager.setInt(DAILY_PA_BOUT_LENGTH, minutes, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String DAILY_PA_MINUTES_GOAL = "DAILY_PA_MINUTES_GOAL";

    public static int getDailyPAminuteGoal(Context context) {
        return SharedPrefManager.getInt(DAILY_PA_MINUTES_GOAL, 30, context);
    }
    public static void setDailyPAminuteGoal(Context context, int minutes) {
        SharedPrefManager.setInt(DAILY_PA_MINUTES_GOAL, minutes, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PA_MINUTES = "PA_MINUTES";

    public static int getPAminutes(Context context) {
        return SharedPrefManager.getInt(PA_MINUTES, 0, context);
    }
    public static void setPAMinutes(Context context, int minutes) {
        SharedPrefManager.setInt(PA_MINUTES, minutes, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PA_MINUTES_GOAL = "PA_MINUTES_GOAL";

    public static int getPAminutesGoal(Context context) {
        return SharedPrefManager.getInt(PA_MINUTES_GOAL, 35, context);
    }
    public static void setPAMinutesGoal(Context context, int minutes) {
        SharedPrefManager.setInt(PA_MINUTES_GOAL, minutes, context);
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

    private static final String WEEKLY_SURVEY_PROMPTED_COUNT = "WEEKLY_SURVEY_PROMPTED_COUNT";

    public static int getWeeklySurveyPromptedCount(Context context) {
        return SharedPrefManager.getInt(WEEKLY_SURVEY_PROMPTED_COUNT, 0, context);
    }
    public static void incrementWeeklySurveyPromptedCount(Context context) {
        int currentCount = getWeeklySurveyPromptedCount(context);
        currentCount++;
        SharedPrefManager.setInt(WEEKLY_SURVEY_PROMPTED_COUNT, currentCount, context);
    }

//    //--------------------------------------------------------------------------------------------//
//
//    private static final String WEEKLY_SURVEY_START_HOUR = "WEEKLY_SURVEY_START_HOUR";
//
//    public static int getWeeklySurveyStartHour(Context mContext) {
//        return SharedPrefManager.getInt(WEEKLY_SURVEY_START_HOUR,19 , mContext);
//    }
//    public static void setWeeklySurveyStartHour(Context mContext, int startHour) {
//        SharedPrefManager.setInt(WEEKLY_SURVEY_START_HOUR, startHour, mContext);
//    }
//
//    private static final String WEEKLY_SURVEY_START_MINUTE = "WEEKLY_SURVEY_START_MINUTE";
//
//    public static int getWeeklySurveyStartMinute(Context mContext) {
//        return SharedPrefManager.getInt(WEEKLY_SURVEY_START_MINUTE,0 , mContext);
//    }
//    public static void setWeeklySurveyStartMinute(Context mContext, int startMinute) {
//        SharedPrefManager.setInt(WEEKLY_SURVEY_START_MINUTE, startMinute, mContext);
//    }
//
//
//    private static final String WEEKLY_SURVEY_STOP_HOUR = "WEEKLY_SURVEY_STOP_HOUR";
//
//    public static int getWeeklySurveyStopHour(Context mContext) {
//        return SharedPrefManager.getInt(WEEKLY_SURVEY_STOP_HOUR,20 , mContext);
//    }
//    public static void setWeeklySurveyStopHour(Context mContext, int stopHour) {
//        SharedPrefManager.setInt(WEEKLY_SURVEY_STOP_HOUR, stopHour, mContext);
//    }
//
//    private static final String WEEKLY_SURVEY_STOP_MINUTE = "WEEKLY_SURVEY_STOP_MINUTE";
//
//    public static int getWeeklySurveyStopMinute(Context mContext) {
//        return SharedPrefManager.getInt(WEEKLY_SURVEY_STOP_MINUTE,0 , mContext);
//    }
//    public static void setWeeklySurveyStopMinute(Context mContext, int stopMinute) {
//        SharedPrefManager.setInt(WEEKLY_SURVEY_STOP_MINUTE, stopMinute, mContext);
//    }
//    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private static final String WEEKLY_SURVEY_COMPLETED_COUNT = "WEEKLY_SURVEY_COMPLETED_COUNT";

    public static int getWeeklySurveyCompletedCount(Context mContext) {
        return SharedPrefManager.getInt(WEEKLY_SURVEY_COMPLETED_COUNT, 0, mContext);
    }
    public static void incrementWeeklySurveyCompletedCount(Context mContext) {
        int currentCount = getSalivaSurveyCompletedCount(mContext);
        currentCount++;
        SharedPrefManager.setInt(WEEKLY_SURVEY_COMPLETED_COUNT, currentCount, mContext);
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


    private static final String WEEKLY_SURVEY_DAY = "WEEKLY_SURVEY_DAT";

    public static String getWeeklySurveyDay(Context mContext) {
        return SharedPrefManager.getString(WEEKLY_SURVEY_DAY, "", mContext);
    }

    public static void setWeeklySurveyDay(Context mContext, String weeklySurveyDay) {
        SharedPrefManager.setString(WEEKLY_SURVEY_DAY, weeklySurveyDay, mContext);
    }

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
    private static final String PHASE_TWO_ACTIVE = "PHASE_TWO_ACTIVE";

    public static boolean getSecondPhaseActive(Context mContext){
        return SharedPrefManager.getBoolean(PHASE_TWO_ACTIVE , false, mContext);
    }

    public static void setSecondPhaseActive(Context mContext, boolean phasetwoactive) {
        SharedPrefManager.setBoolean(PHASE_TWO_ACTIVE, phasetwoactive, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String PHASE_THREE_ACTIVE = "PHASE_THREE_ACTIVE";

    public static boolean getThirdPhaseActive(Context mContext){
        return SharedPrefManager.getBoolean(PHASE_THREE_ACTIVE , false, mContext);
    }

    public static void setThirdPhaseActive(Context mContext, boolean phasethreeactive) {
        SharedPrefManager.setBoolean(PHASE_THREE_ACTIVE, phasethreeactive, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String SPEED_LAST_READ_TIME = "SPEED_LAST_READ_TIME";

    public static long getSpeedLastReadTime(Context context) {
        return SharedPrefManager.getLong(SPEED_LAST_READ_TIME, -1, context);
    }

    public static void setSpeedLastReadTime(Context context, long stopTime) {
        SharedPrefManager.setLong(SPEED_LAST_READ_TIME, stopTime, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String WATCH_LAST_READ_TIME = "WATCH_LAST_READ_TIME";

    public static long getWatchLastReadTime(Context context) {
        return SharedPrefManager.getLong(WATCH_LAST_READ_TIME, -1, context);
    }

    public static void setWatchLastReadTime(Context context, long stopTime) {
        SharedPrefManager.setLong(WATCH_LAST_READ_TIME, stopTime, context);
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
    private static final String SPEED_LAST_RECEIVED_TIME = "SPEED_LAST_REDEIVED_TIME";

    public static Long getSpeedLastReceivedTime(Context mContext){
        return SharedPrefManager.getLong(SPEED_LAST_RECEIVED_TIME , 0l, mContext);
    }

    public static void setSpeedLastReceivedTime(Context mContext, Long SpeedLastReceivedTime) {
        SharedPrefManager.setLong(SPEED_LAST_RECEIVED_TIME, SpeedLastReceivedTime, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String LAST_SPEED_ROT = "LAST_SPEED_ROT";

    public static int getLastSpeedRot(Context context) {
        return SharedPrefManager.getInt(LAST_SPEED_ROT, -1, context);
    }
    public static void setLastSpeedRot(Context context, int lastSpeedRot) {
        SharedPrefManager.setInt(LAST_SPEED_ROT, lastSpeedRot, context);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String CADENCE_LAST_RECEIVED_TIME = "CADENCE_LAST_REDEIVED_TIME";

    public static Long getCadenceLastReceivedTime(Context mContext){
        return SharedPrefManager.getLong(CADENCE_LAST_RECEIVED_TIME , 0l, mContext);
    }

    public static void setCadenceLastReceivedTime(Context mContext, Long CadenceLastReceivedTime) {
        SharedPrefManager.setLong(CADENCE_LAST_RECEIVED_TIME, CadenceLastReceivedTime, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String LAST_CADENCE_ROT = "LAST_CADENCE_ROT";

    public static int getLastCadenceRot(Context context) {
        return SharedPrefManager.getInt(LAST_CADENCE_ROT, 0, context);
    }
    public static void setLastCadenceRot(Context context, int lastCadenceRot) {
        SharedPrefManager.setInt(LAST_CADENCE_ROT, lastCadenceRot, context);
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
    private static final String ENERGY_EXPENDITURE_KCAL_BOTH= "ENERGY_EXPENDITURE_KCAL_BOTH";

    public static String getEEKcalBoth(Context mContext){
        return SharedPrefManager.getString(ENERGY_EXPENDITURE_KCAL_BOTH , "0", mContext);
    }

    public static void setEEKcalBoth(Context mContext, String energyExpenditureKCalBoth) {
        SharedPrefManager.setString(ENERGY_EXPENDITURE_KCAL_BOTH, energyExpenditureKCalBoth, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String ENERGY_EXPENDITURE_KCAL_PANOBIKE = "ENERGY_EXPENDITURE_KCAL_PANOBIKE";

    public static String getEEKcalPanobike(Context mContext){
        return SharedPrefManager.getString(ENERGY_EXPENDITURE_KCAL_PANOBIKE , "0", mContext);
    }

    public static void setEEKcalPanobike(Context mContext, String energyExpenditureKCalPanobike) {
        SharedPrefManager.setString(ENERGY_EXPENDITURE_KCAL_PANOBIKE, energyExpenditureKCalPanobike, mContext);
    }

    //--------------------------------------------------------------------------------------------//
    private static final String ENERGY_EXPENDITURE_KCAL_WATCH = "ENERGY_EXPENDITURE_KCAL_WATCH";

    public static String getEEKcalWatch(Context mContext){
        return SharedPrefManager.getString(ENERGY_EXPENDITURE_KCAL_WATCH , "0", mContext);
    }

    public static void setEEKcalWatch(Context mContext, String energyExpenditureKCalWatch) {
        SharedPrefManager.setString(ENERGY_EXPENDITURE_KCAL_WATCH, energyExpenditureKCalWatch, mContext);
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