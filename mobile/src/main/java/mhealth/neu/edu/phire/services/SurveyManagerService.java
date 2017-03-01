package mhealth.neu.edu.phire.services;

import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.PromptTime;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Survey;
import edu.neu.mhealth.android.wockets.library.ema.SurveyManager;
import edu.neu.mhealth.android.wockets.library.ema.SurveyScheduleManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.TEMPLEConstants;

/**
 * @author Dharam Maniar
 */
public class SurveyManagerService extends WocketsIntentService {

    private static final String TAG = "SurveyManagerService";

    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);
        processSurvey();
    }

    private void processSurvey() {
        Survey survey = SurveyManager.getSelectedSurvey(mContext);
        if (survey == null) {
            return;
        }

        long currentTime = DateTime.getCurrentTimeInMillis();
        long startTime = DataManager.getStartDate(mContext);
        long endTime = DataManager.getEndDate(mContext);

        Log.i(TAG, "Current time is - " + DateTime.getTimestampString(currentTime), mContext);
        Log.i(TAG, "Start Date is - " + DateTime.getTimestampString(startTime), mContext);
        Log.i(TAG, "End Date is - " + DateTime.getTimestampString(endTime), mContext);
        if (startTime > currentTime || currentTime > endTime) {
            Log.i(TAG, "Outside of start and end date. Not processing surveys", mContext);
            return;
        }

        // Schedule prompts for today
        SurveyScheduleManager.scheduleSurveysForToday(survey, mContext);

        int sleepHour = Integer.parseInt(DataManager.getSleepTime(mContext, TEMPLEConstants.DEFAULT_SLEEP_TIME).split(":")[0]);
        int sleepMinute = Integer.parseInt(DataManager.getSleepTime(mContext, TEMPLEConstants.DEFAULT_SLEEP_TIME).split(":")[1]);
        int wakeHour = Integer.parseInt(DataManager.getWakeTime(mContext, TEMPLEConstants.DEFAULT_WAKE_TIME).split(":")[0]);
        int wakeMinute = Integer.parseInt(DataManager.getWakeTime(mContext, TEMPLEConstants.DEFAULT_WAKE_TIME).split(":")[1]);

        // Prompt survey if time
        if (SurveyScheduleManager.timeToPrompt(mContext, survey, sleepHour, sleepMinute, wakeHour, wakeMinute)) {
            startService(new Intent(this, SurveyService.class));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }
}