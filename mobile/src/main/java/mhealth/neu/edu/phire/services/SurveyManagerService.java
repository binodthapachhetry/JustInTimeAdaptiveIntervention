package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.PromptTime;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Survey;
import edu.neu.mhealth.android.wockets.library.ema.SurveyManager;
import edu.neu.mhealth.android.wockets.library.ema.SurveyScheduleManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;


/**
 * @author Dharam Maniar
 */
public class SurveyManagerService extends IntentService {

    private static final String TAG = "SurveyManagerService";

    private Context mContext;

    public SurveyManagerService() {
        super("SurveyManagerService");
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mContext = getApplicationContext();
//        Log.i(TAG, "Inside onCreate", mContext);
//        processSurvey();
//    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = getApplicationContext();
        Log.i(TAG, "Inside on Handle Intent", mContext);
        processSurvey();

    }


//    public SurveyManagerService(){
//        super("SurveyManagerService");
//    }
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        mContext = getApplicationContext();
//        Log.i(TAG, "Inside onCreate", mContext);
//        processSurvey();
//
//    }

    private void processSurvey() {

        if (Looper.myLooper() == Looper.getMainLooper()){
            Log.i(TAG,"In main thread",mContext);
        }else{
            Log.i(TAG,"Not in main thread",mContext);
        }


        List<Survey> surveys = SurveyManager.getSelectedSurveys(mContext);
        if (surveys.isEmpty()) {
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
        for(Survey survey : surveys){
            Log.i(TAG,"Survey name to schedule:" + survey.surveyName,mContext);
            switch(survey.surveyName){
                case TEMPLEConstants.KEY_WEEKLY_SURVEY:
                    String DayOfWeekToPrompt = TEMPLEDataManager.getWeeklySurveyDay(mContext);
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    String today = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                    if(DayOfWeekToPrompt.equals(today)){
                        SurveyScheduleManager.scheduleSurveysForToday(survey,mContext);
                    }else{
                        Log.i(TAG,"Weekly survey is prompted only on: " + DayOfWeekToPrompt,mContext);
                    }
                    break;
                case TEMPLEConstants.KEY_EMA_SURVEY:
                    SurveyScheduleManager.scheduleSurveysForToday(survey, mContext);
                    break;
            }
        }

//        // Schedule prompts for today
//        SurveyScheduleManager.scheduleSurveysForToday(survey, mContext);

        int sleepHour = Integer.parseInt(DataManager.getSleepTime(mContext, TEMPLEConstants.DEFAULT_SLEEP_TIME).split(":")[0]);
        int sleepMinute = Integer.parseInt(DataManager.getSleepTime(mContext, TEMPLEConstants.DEFAULT_SLEEP_TIME).split(":")[1]);
        int wakeHour = Integer.parseInt(DataManager.getWakeTime(mContext, TEMPLEConstants.DEFAULT_WAKE_TIME).split(":")[0]);
        int wakeMinute = Integer.parseInt(DataManager.getWakeTime(mContext, TEMPLEConstants.DEFAULT_WAKE_TIME).split(":")[1]);

        // Prompt survey if time
        for(Survey survey : surveys){
            Log.i(TAG,"Survey name:"+survey.surveyName,mContext);
            if (SurveyScheduleManager.timeToPrompt(mContext, survey, sleepHour, sleepMinute, wakeHour, wakeMinute)) {
                Log.i(TAG,"Starting survey service to prompt:"+survey.surveyName,mContext);
                DataManager.setSelectedSurveyName(mContext,survey.surveyName);
                startService(new Intent(getApplicationContext(), SurveyService.class));
            }
        }
    }

//    private void scheduleSalivaSurveysForToday(Survey survey) {
//        if (!isSalivaDay()) {
//            Log.i(TAG, "Today is not a saliva day", mContext);
//            return;
//        }

//        if (DataManager.getSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_WAKING, DateTime.getDate()) != 0) {
//            Log.i(TAG, "Saliva surveys are already scheduled", mContext);
//            scheduleSalivaBedTimeSurveyForToday();
//            return;
//        }

//        boolean isMother = true;
//        if ("Child".equals(survey.surveyName)) {
//            isMother = false;
//        }
//
//        String wakeTime = DataManager.getWakeTime(mContext, TEMPLEConstants.DEFAULT_WAKE_TIME);
//        Calendar today = Calendar.getInstance();
//        today.setTimeInMillis(DateTime.getCurrentTimeInMillis());
//        today.set(Calendar.HOUR_OF_DAY, Integer.parseInt(wakeTime.split(":")[0]));
//        today.set(Calendar.MINUTE, Integer.parseInt(wakeTime.split(":")[1]));
//
//        Log.i(TAG, "Survey " + KEY_SALIVA_WAKING + " scheduled for " + DateTime.getTimestampString(today.getTimeInMillis()), mContext);
//        DataManager.setSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_WAKING, DateTime.getDate(), today.getTimeInMillis());
//
//        today.add(Calendar.MINUTE, 30);
//
//        Log.i(TAG, "Survey " + KEY_SALIVA_WAKING_PLUS_30 + " scheduled for " + DateTime.getTimestampString(today.getTimeInMillis()), mContext);
//        DataManager.setSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_WAKING_PLUS_30, DateTime.getDate(), today.getTimeInMillis());
//
//        if (isMother) {
//            long startTime = DateTime.getTimeInMillis(15, 30);
//            long endTime = DateTime.getTimeInMillis(16, 0);
//            long diff = endTime - startTime + 1;
//            long randomTime = startTime + (long) (Math.random() * diff);
//            Log.i(TAG, "Survey " + KEY_SALIVA_AFTERNOON_330TO4_MOTHER + " scheduled for " + DateTime.getTimestampString(randomTime), mContext);
//            DataManager.setSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_AFTERNOON_330TO4_MOTHER, DateTime.getDate(), randomTime);
//        } else {
//            long startTime = DateTime.getTimeInMillis(16, 0);
//            long endTime = DateTime.getTimeInMillis(16, 30);
//            long diff = endTime - startTime + 1;
//            long randomTime = startTime + (long) (Math.random() * diff);
//            Log.i(TAG, "Survey " + KEY_SALIVA_AFTERNOON_4TO430_CHILD + " scheduled for " + DateTime.getTimestampString(randomTime), mContext);
//            DataManager.setSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_AFTERNOON_4TO430_CHILD, DateTime.getDate(), randomTime);
//        }
//
//        scheduleSalivaBedTimeSurveyForToday();
//    }

//    private void scheduleSalivaBedTimeSurveyForToday() {
//        String sleepTime = DataManager.getSleepTime(mContext, TEMPLEConstants.DEFAULT_SLEEP_TIME);
//        Calendar today = Calendar.getInstance();
//        today.setTimeInMillis(DateTime.getCurrentTimeInMillis());
//        today.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sleepTime.split(":")[0]));
//        today.set(Calendar.MINUTE, Integer.parseInt(sleepTime.split(":")[1]));
//        Log.i(TAG, "Survey " + KEY_SALIVA_BEDTIME + " scheduled for " + DateTime.getTimestampString(today.getTimeInMillis()), mContext);
//        DataManager.setSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_BEDTIME, DateTime.getDate(), today.getTimeInMillis());
//    }

    private boolean isSalivaDay() {
        long startDate = DataManager.getStartDate(mContext);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(DateTime.getCurrentTimeInMillis());
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.setTimeInMillis(startDate);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.THURSDAY:
                return today == Calendar.FRIDAY ||
                        today == Calendar.SATURDAY ||
                        today == Calendar.SUNDAY ||
                        today == Calendar.MONDAY;
            case Calendar.FRIDAY:
                return today == Calendar.SATURDAY ||
                        today == Calendar.SUNDAY ||
                        today == Calendar.MONDAY ||
                        today == Calendar.TUESDAY;
            case Calendar.MONDAY:
            case Calendar.SATURDAY:
            case Calendar.SUNDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            default:
                return today == Calendar.THURSDAY ||
                        today == Calendar.FRIDAY ||
                        today == Calendar.SATURDAY ||
                        today == Calendar.SUNDAY;
        }
    }


//    private void promptSalivaIfTime(Survey survey) {
//        if (!isSalivaDay()) {
//            Log.i(TAG, "Today is not a saliva day", mContext);
//            return;
//        }
//
//        boolean isMother = true;
//        if ("Child".equals(survey.surveyName)) {
//            isMother = false;
//        }
//
//        long wakeUpSalivaPromptTime = DataManager.getSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_WAKING, DateTime.getDate());
//        Log.i(TAG, KEY_SALIVA_WAKING + " is scheduled for " + DateTime.getTimestampString(wakeUpSalivaPromptTime), mContext);
//        promptSalivaIfTime(KEY_SALIVA_WAKING, wakeUpSalivaPromptTime);
//
//        long wakeUpPlus30SalivaPromptTime = DataManager.getSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_WAKING_PLUS_30, DateTime.getDate());
//        Log.i(TAG, KEY_SALIVA_WAKING_PLUS_30 + " is scheduled for " + DateTime.getTimestampString(wakeUpPlus30SalivaPromptTime), mContext);
//        promptSalivaIfTime(KEY_SALIVA_WAKING_PLUS_30, wakeUpPlus30SalivaPromptTime);
//
//        if (isMother) {
//            long afternoonSalivaMotherPromptTime = DataManager.getSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_AFTERNOON_330TO4_MOTHER, DateTime.getDate());
//            Log.i(TAG, KEY_SALIVA_AFTERNOON_330TO4_MOTHER + " is scheduled for " + DateTime.getTimestampString(afternoonSalivaMotherPromptTime), mContext);
//            promptSalivaIfTime(KEY_SALIVA_AFTERNOON_330TO4_MOTHER, afternoonSalivaMotherPromptTime);
//        } else {
//            long afternoonSalivaChildPromptTime = DataManager.getSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_AFTERNOON_4TO430_CHILD, DateTime.getDate());
//            Log.i(TAG, KEY_SALIVA_AFTERNOON_4TO430_CHILD + " is scheduled for " + DateTime.getTimestampString(afternoonSalivaChildPromptTime), mContext);
//            promptSalivaIfTime(KEY_SALIVA_AFTERNOON_4TO430_CHILD, afternoonSalivaChildPromptTime);
//        }
//
//        long bedTimeSalivaPromptTime = DataManager.getSurveyScheduleTimeForDateByKey(mContext, KEY_SALIVA_BEDTIME, DateTime.getDate());
//        Log.i(TAG, KEY_SALIVA_BEDTIME + " is scheduled for " + DateTime.getTimestampString(bedTimeSalivaPromptTime), mContext);
//        promptSalivaIfTime(KEY_SALIVA_BEDTIME, bedTimeSalivaPromptTime);
//    }

//    private void promptSalivaIfTime(String promptKey, long promptTime) {
//        if (DataManager.isPromptCompleteForDate(mContext, promptKey, DateTime.getDate())) {
//            Log.i(TAG, "Prompt " + promptKey + " already completed for the day", mContext);
//            return;
//        }
//
//        if (DateTime.getCurrentTimeInMillis() < promptTime) {
//            Log.i(TAG, "Prompt " + promptKey + " is in the future - " + DateTime.getTimestampString(promptTime), mContext);
//            return;
//        }
//        String activePromptKey = DataManager.getActivePromptKey(mContext);
//        if (promptTime > DateTime.getCurrentTimeInMillis() - DateTime.MINUTES_5_IN_MILLIS) {
//            Log.i(TAG, "Time to prompt - " + promptKey, mContext);
//
//            if (SurveyScheduleManager.isAnyPromptActive(mContext)) {
//                if (activePromptKey.equals(promptKey)) {
//                    Log.i(TAG, "Prompt " + activePromptKey + " is already active.", mContext);
//                    return;
//                }
//                Log.i(TAG, "Prompt " + activePromptKey + " is already active. Delaying " + promptKey + " by 10 minutes", mContext);
//                DataManager.setSurveyScheduleTimeForDateByKey(mContext, promptKey, DateTime.getDate(), promptTime + DateTime.MINUTES_10_IN_MILLIS);
//                return;
//            }
//            Log.i(TAG, "Prompting - " + promptKey, mContext);
//            DataManager.setActivePromptKey(mContext, promptKey);
//            DataManager.setActivePromptStartTime(mContext, DateTime.getCurrentTimeInMillis());
//            startService(new Intent(this, SurveyService.class));
//        }
//
//        Log.i(TAG, "Survey never prompted for the user. Trying to figure out the reason why.", mContext);
//        long lastPhoneOffTime = DataManager.getLastPhoneOffTime(mContext);
//        long lastPhoneOnTime = DataManager.getLastPhoneOnTime(mContext);
//        if (lastPhoneOffTime > 0 && lastPhoneOnTime > 0) {
//            if (lastPhoneOffTime < promptTime && promptTime < lastPhoneOnTime) {
//                Log.i(TAG, "Prompt " + promptKey + " not prompted due to phone switch off", mContext);
//                SurveyManager.writePrompt(mContext, promptKey, promptTime, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Phone switched off");
//                DataManager.setPromptCompleteForDate(mContext, promptKey, DateTime.getDate());
//                return;
//            }
//        }
//        if (DateTime.getCurrentTimeInMillis() > promptTime + DateTime.MINUTES_10_IN_MILLIS) {
//            if (activePromptKey.equals(promptKey)) {
//                Log.i(TAG, "Prompt " + promptKey + " - something went wrong", mContext);
//                SurveyManager.writePrompt(mContext, promptKey, promptTime, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Something went wrong");
//                DataManager.setPromptCompleteForDate(mContext, promptKey, DateTime.getDate());
//                DataManager.setActivePromptKey(mContext, "");
//                DataManager.setActivePromptStartTime(mContext, -1);
//            } else {
//                Log.i(TAG, "Prompt " + promptKey + " - not prompted due to minute service issue", mContext);
//                SurveyManager.writePrompt(mContext, promptKey, promptTime, -1, -1, "", false, 0, SurveyManager.Status.NEVER_PROMPTED, "Minute Service Issue");
//                DataManager.setPromptCompleteForDate(mContext, promptKey, DateTime.getDate());
//            }
//        }
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }



}