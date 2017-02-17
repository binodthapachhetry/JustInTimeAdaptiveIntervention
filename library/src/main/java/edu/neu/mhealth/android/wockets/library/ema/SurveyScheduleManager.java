package edu.neu.mhealth.android.wockets.library.ema;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.PromptTime;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Survey;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author dmaniar
 */
public class SurveyScheduleManager {

    private static final String TAG = "SurveyScheduleManager";

    public static void scheduleSurveysForToday(Survey survey, Context mContext) {
        List<PromptTime> promptTimes = DateTime.isWeekend() ? survey.schedule.weekends : survey.schedule.weekdays;
        if(promptTimes != null){
            for (PromptTime promptTime : promptTimes) {
                Log.i(TAG, "Scheduling Survey for - " + promptTime.key, mContext);
                if (DataManager.getSurveyScheduleTimeForDateByKey(mContext, promptTime.key, DateTime.getDate()) != 0) {
                    Log.i(TAG, "Survey " + promptTime.key + " is already scheduled", mContext);
                    continue;
                }
                if (promptTime.type.equals("RANDOM")) {
                    long startTime = DateTime.getTimeInMillis(
                            Integer.parseInt(promptTime.startTime.split(":")[0]),
                            Integer.parseInt(promptTime.startTime.split(":")[1])
                    );
                    long endTime = DateTime.getTimeInMillis(
                            Integer.parseInt(promptTime.endTime.split(":")[0]),
                            Integer.parseInt(promptTime.endTime.split(":")[1])
                    );

                    long diff = endTime - startTime + 1;
                    long randomTime = startTime + (long)(Math.random() * diff);
                    Log.i(TAG, "Survey " + promptTime.key + " scheduled for " + DateTime.getTimestampString(randomTime), mContext);
                    DataManager.setSurveyScheduleTimeForDateByKey(mContext, promptTime.key, DateTime.getDate(), randomTime);
                }
            }
        }else{
            Log.w(TAG, "Failed to schedule survey, because promptTimes is null", mContext);
        }
    }

    public static boolean timeToPrompt(Context mContext, Survey survey, int sleepHour, int sleepMinute, int wakeHour, int wakeMinute) {
        String activePromptKey = DataManager.getActivePromptKey(mContext);

        List<PromptTime> promptTimes = DateTime.isWeekend() ? survey.schedule.weekends : survey.schedule.weekdays;
        if(promptTimes == null){
            Log.i(TAG, "Did not find schedule for today, there will be no prompt", mContext);
            promptTimes = new ArrayList<>();
        }
        for (PromptTime promptTime : promptTimes) {
            if (DataManager.isPromptCompleteForDate(mContext, promptTime.key, DateTime.getDate())) {
                Log.i(TAG, "Prompt " + promptTime.key + " already completed for the day", mContext);
                continue;
            }
            long time = DataManager.getSurveyScheduleTimeForDateByKey(mContext, promptTime.key, DateTime.getDate());
            Log.i(TAG, "Survey Schedule for " + promptTime.key + " is " + DateTime.getTimestampString(time), mContext);

            if (DateTime.getCurrentTimeInMillis() < time) {
                Log.i(TAG, "Still not time to prompt the survey", mContext);
                continue;
            }

            if (time > DateTime.getCurrentTimeInMillis() - DateTime.MINUTES_5_IN_MILLIS) {
                Log.i(TAG, "Time to Prompt - " + promptTime.key, mContext);

                if (isAnyPromptActive(mContext)) {
                    if (activePromptKey.equals(promptTime.key)) {
                        Log.i(TAG, "Prompt " + activePromptKey + " is already active.", mContext);
                        return false;
                    }
                    Log.i(TAG, "Prompt " + activePromptKey + " is already active. Delaying " + promptTime.key + " by 10 minutes", mContext);
                    DataManager.setSurveyScheduleTimeForDateByKey(mContext, promptTime.key, DateTime.getDate(), time + DateTime.MINUTES_10_IN_MILLIS);
                    break;
                }

                if (!isWithinWakeSleepTime(sleepHour, sleepMinute, wakeHour, wakeMinute)) {
                    Log.i(TAG, "Prompt " + promptTime.key + " out of wake sleep window", mContext);
                    SurveyManager.writePrompt(mContext, promptTime.key, time, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Outside Wake Sleep Time");
                    DataManager.setPromptCompleteForDate(mContext, promptTime.key, DateTime.getDate());
                    break;
                }
                Log.i(TAG, "Prompting - " + promptTime.key, mContext);
                DataManager.setActivePromptKey(mContext, promptTime.key);
                DataManager.setActivePromptStartTime(mContext, DateTime.getCurrentTimeInMillis());
                return true;
            }
            Log.i(TAG, "Survey never prompted for the user. Trying to figure out the reason why.", mContext);
            long lastPhoneOffTime = DataManager.getLastPhoneOffTime(mContext);
            long lastPhoneOnTime = DataManager.getLastPhoneOnTime(mContext);

            if (lastPhoneOffTime > 0 && lastPhoneOnTime > 0) {
                if (lastPhoneOffTime < time && time < lastPhoneOnTime) {
                    Log.i(TAG, "Prompt " + promptTime.key + " not prompted due to phone switch off", mContext);
                    SurveyManager.writePrompt(mContext, promptTime.key, time, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Phone switched off");
                    DataManager.setPromptCompleteForDate(mContext, promptTime.key, DateTime.getDate());
                    break;
                }
            }
            if (DateTime.getCurrentTimeInMillis() > time + DateTime.MINUTES_10_IN_MILLIS) {
                if (activePromptKey.equals(promptTime.key)) {
                    Log.i(TAG, "Prompt " + promptTime.key + " - something went wrong", mContext);
                    SurveyManager.writePrompt(mContext, promptTime.key, time, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Something went wrong");
                    DataManager.setPromptCompleteForDate(mContext, promptTime.key, DateTime.getDate());
                    DataManager.setActivePromptKey(mContext, "");
                    DataManager.setActivePromptStartTime(mContext, -1);
                } else {
                    Log.i(TAG, "Prompt " + promptTime.key + " - not prompted due to minute service issue", mContext);
                    SurveyManager.writePrompt(mContext, promptTime.key, time, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Minute Service Issue");
                    DataManager.setPromptCompleteForDate(mContext, promptTime.key, DateTime.getDate());
                }
            }
        }

        return false;
    }

    public static boolean isAnyPromptActive(Context mContext) {
        Log.i(TAG, "Inside isAnyPromptActive", mContext);
        String activePromptKey = DataManager.getActivePromptKey(mContext);
        if (activePromptKey.equals("")) {
            Log.i(TAG, "No Active Prompt Key. Returning false.", mContext);
            return false;
        }

        long activePromptStartTime = DataManager.getActivePromptStartTime(mContext);
        if (activePromptStartTime <= 0) {
            Log.i(TAG, "Active Prompt Key - " + activePromptKey + ". But activePromptStartTime invalid. Returning false.", mContext);
            return false;
        }

        if (activePromptStartTime > DateTime.getCurrentTimeInMillis()) {
            Log.i(TAG, "Active Prompt Key - " + activePromptKey + ". But activePromptStartTime invalid. Returning false.", mContext);
            return false;
        }
        if (activePromptStartTime < DateTime.getCurrentTimeInMillis() - DateTime.MINUTES_15_IN_MILLIS) {
            Log.e(TAG, "Active Prompt Key - " + activePromptKey + ". But activePromptStartTime is before 15 minutes. Something went wrong. Returning false.", mContext);
            return false;
        }

        Log.i(TAG, "Returning true", mContext);
        return true;
    }

    private static boolean isWithinWakeSleepTime(int sleepHour, int sleepMinute, int wakeHour, int wakeMinute) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(DateTime.getCurrentTimeInMillis());

        Calendar sleepCalendar = Calendar.getInstance();
        sleepCalendar.setTimeInMillis(DateTime.getCurrentTimeInMillis());
        sleepCalendar.set(Calendar.HOUR_OF_DAY, sleepHour);
        sleepCalendar.set(Calendar.MINUTE, sleepMinute);

        Calendar wakeCalendar = Calendar.getInstance();
        wakeCalendar.setTimeInMillis(DateTime.getCurrentTimeInMillis());
        wakeCalendar.set(Calendar.HOUR_OF_DAY, wakeHour);
        wakeCalendar.set(Calendar.MINUTE, wakeMinute);

        return currentCalendar.after(wakeCalendar) && currentCalendar.before(sleepCalendar);
    }
}
