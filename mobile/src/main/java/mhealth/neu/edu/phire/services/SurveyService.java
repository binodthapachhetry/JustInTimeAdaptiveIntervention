package mhealth.neu.edu.phire.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.PowerManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.WocketsConstants;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Answer;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Question;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Survey;
import edu.neu.mhealth.android.wockets.library.ema.EMAMessageActivity;
import edu.neu.mhealth.android.wockets.library.ema.EMAMultiChoiceActivity;
import edu.neu.mhealth.android.wockets.library.ema.EMASingleChoiceActivity;
import edu.neu.mhealth.android.wockets.library.ema.EMATimePickerActivity;
import edu.neu.mhealth.android.wockets.library.ema.SurveyManager;
import edu.neu.mhealth.android.wockets.library.events.EMABackPressedEvent;
import edu.neu.mhealth.android.wockets.library.events.EMANextPressedEvent;
import edu.neu.mhealth.android.wockets.library.events.EMASurveyCompleteEvent;
import edu.neu.mhealth.android.wockets.library.managers.AudioManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.managers.VibrationManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.services.WocketsService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
//import edu.neu.mhealth.android.wockets.match.saliva.SalivaSurveyManager;
//
//import static edu.neu.mhealth.android.wockets.match.TEMPLEConstants.KEY_SALIVA_WAKING;

/**
 * @author Dharam Maniar
 */
public class SurveyService extends WocketsService {

    private static final String TAG = "SurveyService";

    private Context mContext;

    private PowerManager.WakeLock wakeLock;

    private CountDownTimer countDownTimer;

    private Survey survey;

    private String promptKey;

    private List<Question> questionList;
    private int currentQuestionIndex;

    private List<Question> doneQuestionQueue;
    private HashMap<Question, String> questionAnswerMap;

    private boolean isSurveyComplete;
    private boolean isReprompt;
    private int numReprompts;
    private long promptTime;
    private long promptStartTime;

    private boolean isDemoSurvey;
    private boolean isPostponeSurvey;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);

        acquireWakelock();

        promptKey = DataManager.getActivePromptKey(mContext);
        if (promptKey.equals(TEMPLEConstants.KEY_EMA_DEMO)) {
            isDemoSurvey = true;
        }

        Log.i(TAG, "Inside onCreate, got promptkey", mContext);

        survey = SurveyManager.getSelectedSurvey(mContext);
        if (survey == null) {
            Log.i(TAG, "No selected survey, stopping service", mContext);
            stopSelf();
            return;
        }

        Log.i(TAG, "Inside onCreate, got survey", mContext);

        boolean isMother = true;
        if ("Child".equals(survey.surveyName)) {
            isMother = false;
        }

//        if (promptKey.contains("KEY_SALIVA")) {
//            survey = SalivaSurveyManager.getSalivaSurvey(promptKey, isMother);
//        }
//
//        if (promptKey.equals(TEMPLEConstants.KEY_SALIVA_WAKING_DEMO)) {
//            survey = SalivaSurveyManager.getSalivaSurvey(KEY_SALIVA_WAKING, isMother);
//            isDemoSurvey = true;
//        }

        Log.i(TAG, "Selected survey is - " + survey.surveyName, mContext);

        String selectedLanguage = DataManager.getSelectedLanguage(mContext);

        questionList = survey.questions;
        questionAnswerMap = new HashMap<>();
        doneQuestionQueue = new ArrayList<>();
        Log.i(TAG, "It's time to prompt - " + promptKey + " in language " + selectedLanguage, mContext);

        EventBus.getDefault().register(this);

        promptTime = DateTime.getCurrentTimeInMillis();
        promptStartTime = 0;
        numReprompts = 0;

        if (!isDemoSurvey) {
            if (survey.surveyName.equals("Saliva")) {
                TEMPLEDataManager.incrementSalivaSurveyPromptedCount(mContext);
            } else {
                DataManager.incrementEMASurveyPromptedCount(mContext);
                DataManager.incrementEMASurveyPromptedCountForDate(mContext, DateTime.getDate());
            }
        }

        // Handles audio and vibration prompts
        handlePrompting();

        // Handles actual display of messages and questions
        handleSurveyStart();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(null, flags, startId);
        }
        if (intent.getBooleanExtra("isFromNotification", false)) {
            if (!DataManager.isPromptOnScreen(mContext)) {
                promptQuestion(questionList.get(currentQuestionIndex));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleSurveyStart() {
        Log.i(TAG, "Handle Survey Start", mContext);
        promptQuestionIfPossible();
    }

    // This method will be called when a EMABackPressedEvent is posted
    @Subscribe
    public void back(EMABackPressedEvent event) {
        Log.i(TAG, "Received EMABackPressedEvent", mContext);
        if (doneQuestionQueue.size() > 0) {
            // Remove the answer
            Question questionForWhichBackWasPressed = doneQuestionQueue.get(doneQuestionQueue.size() - 1);
            questionAnswerMap.remove(questionForWhichBackWasPressed);
            doneQuestionQueue.remove(doneQuestionQueue.size() - 1);
            currentQuestionIndex = survey.questions.indexOf(questionForWhichBackWasPressed);
        } else {
            Log.i(TAG, "No previous question", mContext);
            currentQuestionIndex = 0;
        }

        DataManager.setLastInteractionTime(mContext, DateTime.getCurrentTimeInMillis());
    }

    // This method will be called when a EMANextPressedEvent is posted
    @Subscribe
    public void next(EMANextPressedEvent event) {
        Log.i(TAG, "Received EMANextPressedEvent", mContext);
        if (event.answer != null) {
            Log.i(TAG, "Received Answer - " + event.answer, mContext);
            if (isPostponeSurveyAnswer(event.answer)) {
                this.stopSelf();
            }
            questionAnswerMap.put(survey.questions.get(currentQuestionIndex), event.answer);
            if (!isDemoSurvey) {
                SurveyManager.writeSurveyAnswer(mContext, promptKey, event.answer);
            }
        }
        doneQuestionQueue.add(questionList.get(currentQuestionIndex));
        if (promptStartTime == 0) {
            promptStartTime = DateTime.getCurrentTimeInMillis();
        }
        DataManager.setLastInteractionTime(mContext, DateTime.getCurrentTimeInMillis());
        promptNextQuestion();
    }

    private boolean isPostponeSurveyAnswer(String answerSelected) {
        if (survey.questions.get(currentQuestionIndex).answers != null) {
            List<Answer> answers = survey.questions.get(currentQuestionIndex).answers;
            for (Answer answer : answers) {
                if (answer.key.equals(answerSelected)) {
                    if (answer.isPostpone) {
                        Log.i(TAG, "Need to postpone survey " + promptKey + " by " + answer.postponeTime + " minutes", mContext);
                        isPostponeSurvey = true;
                        DataManager.setSurveyScheduleTimeForDateByKey(
                                mContext,
                                promptKey,
                                DateTime.getDate(),
                                DateTime.getCurrentTimeInMillis() + answer.postponeTime * 60 * 1000
                        );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void promptNextQuestion() {
        currentQuestionIndex++;
        Log.i(TAG, "Current Question Index - " + currentQuestionIndex, mContext);
        if(currentQuestionIndex >= survey.questions.size()) {
            Log.i(TAG, "Survey Question Size - " + survey.questions.size(), mContext);
            Log.i(TAG, "Survey is complete, stopping the service", mContext);
            isSurveyComplete = true;
            stopSelf();
            return;
        }
        promptQuestionIfPossible();
    }

    private void promptQuestionIfPossible() {
        Question question = survey.questions.get(currentQuestionIndex);
        Log.i(TAG, "Trying to prompt question - " + question.key, mContext);

        if (question.alwaysPrompt || isDemoSurvey) {
            Log.i(TAG, "Question is set to always prompt", mContext);
        } else {
            if (question.probability > 0) {
                double random = Math.random();
                if (random > question.probability) {
                    Log.i(TAG, "Skipping question due to probability", mContext);
                    promptNextQuestion();
                    return;
                }
            }

            if (question.firstSurveyForTheDay && !DataManager.isFirstPromptForDay(mContext, DateTime.getDate())) {
                promptNextQuestion();
                Log.i(TAG, "Skipping question since not first survey for the day", mContext);
                return;
            }

            if (question.firstAnsweredSurveyForTheDay && !DataManager.isFirstAnsweredPromptForDay(mContext, DateTime.getDate())) {
                promptNextQuestion();
                Log.i(TAG, "Skipping question since not first answered survey for the day", mContext);
                return;
            }

            if (question.onlyWeekdays && DateTime.isWeekend()) {
                promptNextQuestion();
                Log.i(TAG, "Skipping question since not a weekday", mContext);
                return;
            }

            if (question.answerDependency != null && question.answerDependency.size() > 0) {
                boolean shouldPrompt = false;
                for (String answer : question.answerDependency) {
                    if (isAnswerSelected(answer)) {
                        shouldPrompt = true;
                    }
                }
                if (!shouldPrompt) {
                    Log.i(TAG, "Skipping question since answer dependency is not fulfilled", mContext);
                    promptNextQuestion();
                    return;
                }
            }

            if (question.specificPrompts != null && question.specificPrompts.size() > 0) {
                boolean shouldPrompt = false;
                for (String prompt : question.specificPrompts) {
                    if (prompt.equals(promptKey)) {
                        shouldPrompt = true;
                    }
                }
                if (!shouldPrompt) {
                    Log.i(TAG, "Skipping question since specific prompt condition is not fulfilled", mContext);
                    promptNextQuestion();
                    return;
                }
            }
        }
        promptQuestion(question);
    }

    private void promptQuestion(Question question) {
        Log.i(TAG, "Trying to prompt question - " + question.key, mContext);
        switch (question.type) {
            case WocketsConstants.EMA_QUESTION_TYPE_MESSAGE:
                promptMessage(question);
                break;
            case WocketsConstants.EMA_QUESTION_TYPE_SINGLE_CHOICE:
                promptSingleChoiceQuestion(question);
                break;
            case WocketsConstants.EMA_QUESTION_TYPE_MULTI_CHOICE:
                promptMultiChoiceQuestion(question);
                break;
            case WocketsConstants.EMA_QUESTION_TYPE_TIME_PICKER:
                promptTimePickerQuestion(question);
                break;
            default:
                Log.e(TAG, "Unknown question type. This should never happen.", mContext);
                promptNextQuestion();
                break;
        }
    }

    private boolean isAnswerSelected(String answer) {
        if(questionAnswerMap.size() == 0) {
            return false;
        }
        Collection<String> allAnswers = questionAnswerMap.values();
        if (allAnswers.size() == 0) {
            return false;
        }

        for (String answerKeys : allAnswers) {
            if (answerKeys.contains(answer)) {
                return true;
            }
        }

        return false;
    }

    private void promptMessage(Question question) {
        Log.i(TAG, "Prompting Message", mContext);
        if (!isDemoSurvey) {
            SurveyManager.writeSurveyQuestion(mContext, promptKey, question.key);
        }
        Intent messageIntent = new Intent(this, EMAMessageActivity.class);
        messageIntent.putExtra("questionJson", ObjectMapper.serialize(question));
        // The FLAG_ACTIVITY_MULTIPLE_TASK is required otherwise
        // multiple instances of message activity do not show up.
        messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(messageIntent);
    }

    private void promptSingleChoiceQuestion(Question question) {
        Log.i(TAG, "Prompting Single Choice Question", mContext);
        if (!isDemoSurvey) {
            SurveyManager.writeSurveyQuestion(mContext, promptKey, question.key);
        }
        String answer = "";
        if (questionAnswerMap.containsKey(question)) {
            answer = questionAnswerMap.get(question);
        }
        questionAnswerMap.remove(question);
        Intent singleChoiceQuestionIntent = new Intent(this, EMASingleChoiceActivity.class);
        singleChoiceQuestionIntent.putExtra("questionJson", ObjectMapper.serialize(question));
        singleChoiceQuestionIntent.putExtra("answerString", answer);
        singleChoiceQuestionIntent.putExtra("isFirstPromptOfTheDay", DataManager.isFirstPromptForDay(mContext, DateTime.getDate()));
//        singleChoiceQuestionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        singleChoiceQuestionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(singleChoiceQuestionIntent);
    }

    private void promptMultiChoiceQuestion(Question question) {
        Log.i(TAG, "Prompting Multi Choice Question", mContext);
        if (!isDemoSurvey) {
            SurveyManager.writeSurveyQuestion(mContext, promptKey, question.key);
        }
        String answer = "";
        if (questionAnswerMap.containsKey(question)) {
            answer = questionAnswerMap.get(question);
        }
        questionAnswerMap.remove(question);
        Intent multiChoiceQuestionIntent = new Intent(this, EMAMultiChoiceActivity.class);
        multiChoiceQuestionIntent.putExtra("questionJson", ObjectMapper.serialize(question));
        multiChoiceQuestionIntent.putExtra("answerString", answer);
        multiChoiceQuestionIntent.putExtra("isFirstPromptOfTheDay", DataManager.isFirstPromptForDay(mContext, DateTime.getDate()));
        multiChoiceQuestionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(multiChoiceQuestionIntent);
    }

    private void promptTimePickerQuestion(Question question) {
        Log.i(TAG, "Prompting Time Picker Question", mContext);
        if (!isDemoSurvey) {
            SurveyManager.writeSurveyQuestion(mContext, promptKey, question.key);
        }
        String answer = "";
        if (questionAnswerMap.containsKey(question)) {
            answer = questionAnswerMap.get(question);
        }
        questionAnswerMap.remove(question);
        Intent timePickerQuestionIntent = new Intent(this, EMATimePickerActivity.class);
        timePickerQuestionIntent.putExtra("questionJson", ObjectMapper.serialize(question));
        timePickerQuestionIntent.putExtra("answerString", answer);
        timePickerQuestionIntent.putExtra("isFirstPromptOfTheDay", DataManager.isFirstPromptForDay(mContext, DateTime.getDate()));
        timePickerQuestionIntent.putExtra("isDemoSurvey", isDemoSurvey);
        timePickerQuestionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(timePickerQuestionIntent);
    }

    private void handlePrompting() {
        final int surveyLengthSeconds = survey.timeLengthMins * 60;
        notifyUser();
        if (!isDemoSurvey) {
            SurveyManager.writeSurveyStart(mContext, promptKey);
            DatabaseManager.writeNote(mContext, DatabaseManager.SURVEY_PROMPT, promptKey + ":" + numReprompts);
        }

        final List<Integer> repromptTimes = survey.repromptAtMins;
        Log.i(TAG, "Starting a countdown timer for " + surveyLengthSeconds + " seconds", mContext);
        countDownTimer = new CountDownTimer(surveyLengthSeconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (isSurveyComplete) {
                    return;
                }
                int secondsLeft = (int)millisUntilFinished/1000;
                Log.d(TAG, "Time left - " + secondsLeft);
                int secondsPassed = surveyLengthSeconds - secondsLeft;
                for (int i = 0 ; i < repromptTimes.size() ; i++) {
                    if (i == numReprompts) {
                        if (secondsPassed > repromptTimes.get(i) * 60) {
                            notifyUser();
                            numReprompts++;
                            if (!isDemoSurvey) {
                                SurveyManager.writeSurveyReprompt(mContext, promptKey);
                                DatabaseManager.writeNote(mContext, DatabaseManager.SURVEY_PROMPT, promptKey + ":" + numReprompts);
                            }
                            isReprompt = true;
                            if (!DataManager.isPromptOnScreen(mContext)) {
                                promptQuestion(questionList.get(currentQuestionIndex));
                            }
                        }
                    }
                }
            }
            public void onFinish() {
                if (isSurveyComplete) {
                    return;
                }
                stopSelf();
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
        EventBus.getDefault().post(new EMASurveyCompleteEvent());
        countDownTimer.cancel();
        if (!isDemoSurvey && !isPostponeSurvey) {
            SurveyManager.writeSurveyEnd(mContext, promptKey);
            SurveyManager.writePromptResponses(
                    mContext,
                    survey,
                    questionAnswerMap,
                    promptKey,
                    isReprompt,
                    isSurveyComplete,
                    promptTime
            );

            SurveyManager.Status status;
            if (isSurveyComplete) {
                status = SurveyManager.Status.COMPLETED;
            } else if (questionAnswerMap.size() > 0) {
                status = SurveyManager.Status.INCOMPLETE;
            } else {
                status = SurveyManager.Status.NEVER_STARTED;
            }
            SurveyManager.writePrompt(
                    mContext,
                    promptKey,
                    promptTime,
                    promptStartTime,
                    DateTime.getCurrentTimeInMillis(),
                    AudioManager.getAudioMode(mContext),
                    isReprompt,
                    numReprompts,
                    status,
                    ""
            );

            DataManager.setPromptCompleteForDate(mContext, promptKey, DateTime.getDate());
            if (isSurveyComplete && !survey.surveyName.equals("Saliva")) {
                DataManager.setFirstAnsweredPromptForDate(mContext, DateTime.getDate());
            }
            if (DataManager.isFirstPromptForDay(mContext, DateTime.getDate()) && !survey.surveyName.equals("Saliva")) {
                DataManager.setFirstPromptForDate(mContext, DateTime.getDate());
            }

            if (isSurveyComplete) {
                if (survey.surveyName.equals("Saliva")) {
                    TEMPLEDataManager.incrementSalivaSurveyCompletedCount(mContext);
                } else {
                    DataManager.incrementEMASurveyCompletedCount(mContext);
                    DataManager.incrementEMASurveyCompletedCountForDate(mContext, DateTime.getDate());
                }
            }
        }
        DataManager.setActivePromptKey(mContext, "");
        DataManager.setActivePromptStartTime(mContext, -1);
        NotificationManager.clearSurveyNotification(mContext);
        EventBus.getDefault().unregister(this);
        releaseWakeLock();
    }

    private void notifyUser() {

        long lastInteractionTime = DataManager.getLastInteractionTime(mContext);
        long timeSinceLastInteraction = DateTime.getCurrentTimeInMillis() - lastInteractionTime;

        if (timeSinceLastInteraction > 0 && timeSinceLastInteraction < DateTime.SECONDS_30_IN_MILLIS) {
            Log.i(TAG, "Last interaction within last 30 seconds, not prompting", mContext);
            return;
        }

        Intent intent = new Intent(this, SurveyService.class);
        intent.putExtra("isFromNotification", true);

        NotificationManager.showPromptNotification(
                mContext,
                TEMPLEConstants.STUDY_NAME,
                "It's time for a survey",
                R.mipmap.ic_launcher,
                Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio1),
                VibrationManager.VIBRATION_PATTERN_INTENSE,
                PendingIntent.getService(mContext, 13513, intent, 0)
        );
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        Log.i(TAG, "Acquired Partial Wake Lock", mContext);
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            Log.i(TAG, "Released Wake Lock", mContext);
        }
    }
}