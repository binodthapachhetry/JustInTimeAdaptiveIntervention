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


}