package mhealth.neu.edu.phire.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.DatePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.activities.WocketsActivity;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Question;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Survey;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Text;
import edu.neu.mhealth.android.wockets.library.ema.EMATimePickerActivity;
import edu.neu.mhealth.android.wockets.library.managers.ConnectivityManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.managers.VibrationManager;
import edu.neu.mhealth.android.wockets.library.services.UploadManagerService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;

import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;

import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import mhealth.neu.edu.phire.services.JustInTimeFeedbackService;
import mhealth.neu.edu.phire.services.SurveyService;
import mhealth.neu.edu.phire.panobike.SelectSensorActivity;


/**
 * @author Binod Thapa Chhetry
 */
public class SetupActivity extends WocketsActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SetupActivity";
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        mContext = getApplicationContext();
    }


    @OnClick(R.id.activity_setup_wheel_diameter_cm)
    public void onClickSelectSelectWheelDiameterCm(){
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "OnClickSelectWheelDiameterCm - No study found", mContext);
            return;
        }
        final List<String> supportedWheelDiameterCm = TEMPLEConstants.SUPPORTED_WHEEL_DIAMETER_CM;
        CharSequence diameters[] = new CharSequence[supportedWheelDiameterCm.size()];
        for (int i = 0 ; i < supportedWheelDiameterCm.size() ; i++) {
            diameters[i] = supportedWheelDiameterCm.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a wheel diameter(inch)");
        builder.setItems(diameters, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastManager.showShortToast(mContext, "Selected wheel diameter(cm) - " + supportedWheelDiameterCm.get(which));
                TEMPLEDataManager.setWheelDiameterCm(mContext, supportedWheelDiameterCm.get(which));
            }
        });
        builder.show();

    }

    @OnClick(R.id.activity_setup_crashreporting_test)
    public void onClickForceCrash(){
        Log.i(TAG,"Force crash to test reporting on google developer console",mContext);
        throw new RuntimeException("This is a forced crash");
    }

    @OnClick(R.id.activity_participant_info)
    public void onClickSelectParticipantInfo(){
        Log.i(TAG,"Patient info screen",mContext);
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onClickSelectParticipantInfo- No study found", mContext);
            return;
        }
        android.util.Log.i(TAG,"inside select sensor");
        Intent intent = new Intent(this, ParticipantInfoActivity.class);
        startActivity(intent);

    }

    @OnClick(R.id.activity_select_dailygoal)
    public void onClickSelectDailyGoal(){
        Log.i(TAG,"Select daily goal screen",mContext);
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onClickSelectDailyGoal - No study found", mContext);
            return;
        }
        android.util.Log.i(TAG,"inside select daily goal");
        Intent intent = new Intent(this, SelectDailyGoal.class);
        startActivity(intent);

    }

//    @OnClick(R.id.activity_visual_feedback)
//    public void onClickVisualFeedback(){
//        Log.i(TAG,"Patient visual feecback screen",mContext);
//        Study study = DataManager.getStudy(mContext);
//        if (study == null) {
//            Log.e(TAG, "onClickVisualFeedback - No study found", mContext);
//            return;
//        }
//        android.util.Log.i(TAG,"inside patient visual feecback");
//        Intent intent = new Intent(this, FeedbackChoices.class);
//        startActivity(intent);
//    }

    @OnClick(R.id.activity_select_panobike_sensor)
    public void onClickSelectPanoBikeSensor(){
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "OnClickSelectSelectWheelDiameterCm - No study found", mContext);
            return;
        }
        android.util.Log.i(TAG,"inside select sensor");
        Intent intent = new Intent(this, SelectSensorActivity.class);
        startActivity(intent);

    }

    @OnClick(R.id.activity_setup_select_language)
    public void onClickSelectLanguage() {
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "OnClickSelectSurvey - No study found", mContext);
            return;
        }
        final List<String> supportedLanguages = study.supportedLanguages;
        CharSequence languages[] = new CharSequence[supportedLanguages.size()];
        for (int i = 0 ; i < supportedLanguages.size() ; i++) {
            languages[i] = supportedLanguages.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a survey");
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastManager.showShortToast(mContext, "Selected survey - " + supportedLanguages.get(which));
                DataManager.setSelectedLanguage(mContext, supportedLanguages.get(which));
            }
        });
        builder.show();
    }

    @OnClick(R.id.activity_setup_select_survey)
    public void onClickSelectSurvey() {
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "OnClickSelectSurvey - No study found", mContext);
            return;
        }
        final List<Survey> surveys = study.surveys;
        CharSequence surveyNames[] = new CharSequence[surveys.size()];
        for (int i = 0 ; i < surveys.size() ; i++) {
            surveyNames[i] = surveys.get(i).surveyName;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a survey");
        builder.setItems(surveyNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastManager.showShortToast(mContext, "Selected survey - " + surveys.get(which).surveyName);
                DataManager.setSelectedSurveyName(mContext, surveys.get(which).surveyName);
            }
        });
        builder.show();
    }

    @OnClick(R.id.activity_setup_start_demo)
    public void onClickStartDemo() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DataManager.setActivePromptKey(mContext, TEMPLEConstants.KEY_EMA_DEMO);
                DataManager.setActivePromptStartTime(mContext, DateTime.getCurrentTimeInMillis());
                startService(new Intent(mContext, SurveyService.class));
            }
        }, DateTime.SECONDS_10_IN_MILLIS);
    }

    @OnClick(R.id.activity_setup_start_demo_intervene)
    public void onClickStartInterveneDemo() {
        NotificationManager.showFeedbackNotification(
                mContext,
                "Good job!",
                "5 mins completed,\n 30 mins remaining to complete daily goal.",
                R.mipmap.temple,
                Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio2),
                VibrationManager.VIBRATION_PATTERN_CONGRATULATORY
        );
    }


    @OnClick(R.id.activity_setup_select_start_date)
    public void onClickSelectStartDate() {
        long millis = DataManager.getStartDate(mContext);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        long startTime = DateTime.getTimeInMillis(year, month, dayOfMonth, TEMPLEConstants.START_HOUR, TEMPLEConstants.START_MINUTE);
                        DataManager.setStartDate(mContext, startTime);
                        ToastManager.showShortToast(mContext, "Start Date set as " + DateTime.getTimestampString(startTime));
                        Log.i(TAG, "Start Date set as " + DateTime.getTimestampString(startTime), mContext);

                        String today = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(startTime);
//                        TEMPLEDataManager.setWeeklySurveyDay(mContext,today);

                        Date endDate = new Date(startTime);
                        Calendar myCal = Calendar.getInstance();
                        myCal.setTime(endDate);
                        myCal.add(Calendar.MONTH, +3);
                        long endT = myCal.getTime().getTime();
                        DataManager.setEndDate(mContext,endT);
                        Log.i(TAG, "End Date set as " + DateTime.getTimestampString(endT), mContext);

                    }
                },
                DateTime.getYear(millis),
                DateTime.getMonth(millis),
                DateTime.getDayOfMonth(millis)
        );
        datePickerDialog.show();
    }

    @OnClick(R.id.activity_setup_select_day_time_for_survey)
    public void onClickSelectDayTimeForSurvey() {
        Log.i(TAG,"onClickSelectDayTimeForSurvey screen",mContext);
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onClickSelectDayTimeForSurvey - No study found", mContext);
            return;
        }
        Intent intent = new Intent(this, SelectDayTimeForWeeklySurvey.class);
        startActivity(intent);
    }

    @OnClick(R.id.activity_setup_select_end_date)
    public void onClickSelectEndDate() {
        long millis = DataManager.getEndDate(mContext);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        long endTime = DateTime.getTimeInMillis(year, month, dayOfMonth, TEMPLEConstants.END_HOUR, TEMPLEConstants.END_MINUTE);
                        DataManager.setEndDate(mContext, endTime);
                        ToastManager.showShortToast(mContext, "End Date set as " + DateTime.getTimestampString(endTime));
                        Log.i(TAG, "End Date set as " + DateTime.getTimestampString(endTime), mContext);
                    }
                },
                DateTime.getYear(millis),
                DateTime.getMonth(millis),
                DateTime.getDayOfMonth(millis)
        );
        datePickerDialog.show();
    }

    @OnClick(R.id.activity_setup_select_wake_time)
    public void onClickSelectWakeTime() {
        Question question = new Question().setKey("SETUP_WAKE_TIME")
                .setType("TIME_PICKER")
                .setText(new Text().setEnglish("Select Wake Time")
                        .setSpanish("Select Wake Time")
                        .createText())
                .setFirstPromptPrefixText(null)
                .setNonFirstPromptPrefixText(null)
                .setAnswers(null)
                .setProbability(0)
                .setFirstSurveyForTheDay(false)
                .setFirstAnsweredSurveyForTheDay(false)
                .setOnlyWeekdays(false)
                .setAlwaysPrompt(true)
                .setAnswerDependency(null)
                .setSpecificPrompts(null)
                .setIsBackButtonDisabled(false)
                .setIsReplaceNextWithFinish(true)
                .setAllowToSkip(false)
                .setIsSleepTimeQuestion(false)
                .setIsWakeTimeQuestion(true)
                .createQuestion();
        Intent emaTimePicker = new Intent(this, EMATimePickerActivity.class);
        emaTimePicker.putExtra("questionJson", ObjectMapper.serialize(question));
        emaTimePicker.putExtra("answerString", DataManager.getWakeTime(mContext, TEMPLEConstants.DEFAULT_WAKE_TIME));
        startActivity(emaTimePicker);
    }

    @OnClick(R.id.activity_setup_select_sleep_time)
    public void onClickSelectSleepTime() {
        Question question = new Question().setKey("SETUP_SLEEP_TIME")
                .setType("TIME_PICKER")
                .setText(new Text().setEnglish("Select Sleep Time")
                        .setSpanish("Select Sleep Time")
                        .createText())
                .setFirstPromptPrefixText(null)
                .setNonFirstPromptPrefixText(null)
                .setAnswers(null)
                .setProbability(0)
                .setFirstSurveyForTheDay(false)
                .setFirstAnsweredSurveyForTheDay(false)
                .setOnlyWeekdays(false)
                .setAlwaysPrompt(true)
                .setAnswerDependency(null)
                .setSpecificPrompts(null)
                .setIsBackButtonDisabled(false)
                .setIsReplaceNextWithFinish(true)
                .setAllowToSkip(false)
                .setIsSleepTimeQuestion(true)
                .setIsWakeTimeQuestion(false)
                .createQuestion();
        Intent emaTimePicker = new Intent(this, EMATimePickerActivity.class);
        emaTimePicker.putExtra("questionJson", ObjectMapper.serialize(question));
        emaTimePicker.putExtra("answerString", DataManager.getSleepTime(mContext, TEMPLEConstants.DEFAULT_SLEEP_TIME));
        startActivity(emaTimePicker);
    }

    @OnClick(R.id.activity_setup_compliance)
    public void onClickCompliance() {
        int emaSurveyPromptedCount = DataManager.getEMASurveyPromptedCount(mContext);
        int emaSurveyCompletedCount = DataManager.getEMASurveyCompletedCount(mContext);
//        int salivaSurveyPromptedCount = TEMPLEDataManager.getSalivaSurveyPromptedCount(mContext);
//        int salivaSurveyCompletedCount = TEMPLEDataManager.getSalivaSurveyCompletedCount(mContext);

        int emaCompliancePercentage = 0;
        if (emaSurveyPromptedCount > 0) {
            emaCompliancePercentage = (emaSurveyCompletedCount * 100) / emaSurveyPromptedCount;
        }

//        int salivaCompliancePercentage = 0;
//        if (salivaSurveyPromptedCount > 0) {
//            salivaCompliancePercentage = (salivaSurveyCompletedCount * 100) / salivaSurveyPromptedCount;
//        }

        String compliance;
        compliance = "EMA Surveys Prompted - " + emaSurveyPromptedCount + "\n" +
                "EMA Surveys Completed - " + emaSurveyCompletedCount + "\n" +
                "EMA Compliance - " + emaCompliancePercentage + "%\n";
//                "Saliva Surveys Prompted - " + salivaSurveyPromptedCount + "\n" +
//                "Saliva Surveys Completed - " + salivaSurveyCompletedCount + "\n" +
//                "Saliva Compliance - " + salivaCompliancePercentage + "%\n";
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(SetupActivity.this)
                .setTitle("Compliance")
                .setMessage(compliance)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    @OnClick(R.id.activity_setup_finish_study)
    public void onClickFinishStudy() {
        if (!ConnectivityManager.isInternetConnected(mContext)) {
            ToastManager.showShortToast(mContext, "Internet is not connected. Cannot finish the study.");
            return;
        }
        if (DataManager.isStudyFinished(mContext)) {
            ToastManager.showLongToast(mContext, "Study is already finished.");
            return;
        }
        ToastManager.showShortToast(mContext, "Uploading pending files now");
        DataManager.setStudyFinished(mContext);
        startService(new Intent(this, UploadManagerService.class));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG,"INSIDE ONCONNECTED",mContext);
//        mGoogleApiClient.disconnect();
//        Log.i(TAG,"GOOGLE API CLIENT DISCONNECTED",mContext);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"INSIDE ONCONNECTION SUSPENDED",mContext);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG,"INSIDE ONCONNECTION FAILED",mContext);

    }
}

