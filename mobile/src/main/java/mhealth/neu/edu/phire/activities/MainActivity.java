package mhealth.neu.edu.phire.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.activities.WocketsActivity;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.managers.BuildManager;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.user.UserManager;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import mhealth.neu.edu.phire.services.AlwaysOnService;

import edu.neu.android.wocketslib.utils.SharedPrefs;

/**
 * @author Binod Thapa Chhetry
 */
public class MainActivity extends WocketsActivity {

    private static final String TAG = "MainActivity";
    private static final String PASSWORD_PHASETWO = "passive";
    private static final String PASSWORD_PHASETHREE = "intervene";

    public static final String watchTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private Context mContext;
    private Activity mActivity;

    @BindView(R.id.activate_phase_second)
    Button activateStageTwo;

    @BindView(R.id.activate_phase_three)
    Button activateStageThree;

    @BindView(R.id.activity_main_app_version)
    TextView appVersionTextView;

    @BindView(R.id.activity_survey_day_time)
    TextView weeklySurveyScheduleView;

    @BindView(R.id.activity_main_watch_connected_time)
    TextView watchConnectionView;

    @BindView(R.id.activity_wheel_diameter_cm)
    TextView selectedWheelDiameterCm;

    @BindView(R.id.activity_panobike_sensor_id)
    TextView selectedPanoBikeId;

    @BindView(R.id.activity_panobike_lastconnected_time)
    TextView panobikeLastConnectedTime;

    @BindView(R.id.activity_main_selected_survey)
    TextView selectedSurvey;

    @BindView(R.id.activity_main_selected_language)
    TextView selectedLanguage;

    @BindView(R.id.activity_main_logged_in_user)
    TextView loggedInUser;

    @BindView(R.id.activity_main_start_date)
    TextView startDate;

    @BindView(R.id.activity_main_end_date)
    TextView endDate;

    private int backgroundImageClick = 1;

    private String secondPhase_Text = "";
    private String thirdPhase_Text = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
    }

    @SuppressLint("SetTextI18n")
    private void initialize() {
        mContext = getApplicationContext();
        mActivity = this;

        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String version = pInfo.versionName;
            Log.d(TAG,version);
            appVersionTextView.setText("App Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot get version info", e, mContext);
        }

        if(TEMPLEDataManager.getSecondPhaseActive(mContext)){
            activateStageTwo.setEnabled(false);
        }

        if(TEMPLEDataManager.getThirdPhaseActive(mContext)){
            activateStageThree.setEnabled(false);
        }

        String dateString = new SimpleDateFormat(watchTimeFormat).format(new Date(SharedPrefs.getLong("LAST_WATCH_IN_CONNECTION_TIME",0, mContext)));

        if(TEMPLEDataManager.getWeeklySurveyDay(mContext) == null){
            Log.i(TAG, "No weekly survey schedule set", mContext);
        }else{
           String tmp =  TEMPLEDataManager.getWeeklySurveyDay(mContext) +","+ Integer.toString(DataManager.getWeeklySurveyStartHour(mContext))+":" + Integer.toString(DataManager.getWeeklySurveyStartMinute(mContext)) +" to " + Integer.toString(DataManager.getWeeklySurveyStopHour(mContext))+":" + Integer.toString(DataManager.getWeeklySurveyStopMinute(mContext));
            weeklySurveyScheduleView.setText("Weekly survey schedule:"+tmp);
        }
        watchConnectionView.setText("WatchConnected: " + dateString);
        selectedSurvey.setText("Selected Survey: " + DataManager.getSelectedSurveyName(mContext));
        selectedLanguage.setText("Selected Language: " + DataManager.getSelectedLanguage(mContext));
        loggedInUser.setText("Logged In User: " + UserManager.getUserEmailFormatted());
        selectedWheelDiameterCm.setText("Selected Wheel Diameter(inch): " + TEMPLEDataManager.getWheelDiameterCm(mContext));
        selectedPanoBikeId.setText("Selected PanoBike ID: " + TEMPLEDataManager.getPanoBikeSensorId(mContext));
        panobikeLastConnectedTime.setText("PanoBike Last Connected: " + TEMPLEDataManager.getPanoBikeLastConnectionTime(mContext));

        TEMPLEConstants.init(mContext);

        //Util.setMinuteServiceAlarm(mContext, TAG);
        startService(new Intent(this, AlwaysOnService.class));

        if (DataManager.getStartDate(mContext) == 0) {
            long startTime = DateTime.getTimeInMillis(TEMPLEConstants.START_HOUR, TEMPLEConstants.START_MINUTE);
            DataManager.setStartDate(mContext, startTime);
            String today = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(startTime);
//            TEMPLEDataManager.setWeeklySurveyDay(mContext,today);
            ToastManager.showShortToast(mContext, "Start Time set as " + DateTime.getTimestampString(startTime));
            Log.i(TAG, "Start Time set as " + DateTime.getTimestampString(startTime), mContext);
        }

        if (DataManager.getEndDate(mContext) == 0) {
            long endTime = DateTime.getTimeInMillis(TEMPLEConstants.END_HOUR, TEMPLEConstants.END_MINUTE);
            Date endDate = new Date(endTime);

            // https://bitbucket.org/mhealthresearchgroup/wockets-android/issues/127/show-start-and-end-date-on-screen
            Calendar threeMonthsAhead = Calendar.getInstance();
            threeMonthsAhead.setTime(endDate);
            threeMonthsAhead.add(Calendar.MONTH, +4);
            DataManager.setEndDate(mContext, threeMonthsAhead.getTime().getTime());

//            endTime = endTime + DateTime.DAYS_8_IN_MILLIS;
//            DataManager.setEndDate(mContext, endTime);
            ToastManager.showShortToast(mContext, "End Time set as " + DateTime.getTimestampString(endTime));
            Log.i(TAG, "End Time set as " + DateTime.getTimestampString(endTime), mContext);
        }

        startDate.setText("Start Date: " + DateTime.getTimestampString(DataManager.getStartDate(mContext)));
        endDate.setText("End Date: " + DateTime.getTimestampString(DataManager.getEndDate(mContext)));

        BuildManager.logBuildStatus(mContext);
    }

    @OnClick(R.id.activity_main_background_home)
    public void onClickBackgroundHome(View view) {
        Log.d(TAG, "Clicked Background Home - " + backgroundImageClick);
        if (backgroundImageClick < 4) {
            backgroundImageClick++;
            return;
        }

        backgroundImageClick = 1;
        startActivity(new Intent(this, SetupActivity.class));
    }

//    @OnClick(R.id.activity_ee_plot)
//    public void onClickEnergyExpenditure(View view) {
//        Log.i(TAG,"Clicked EE screen",mContext);
//        Study study = DataManager.getStudy(mContext);
//        if (study == null) {
//            Log.e(TAG, "onClickEnergyExpenditure - No study found", mContext);
//            return;
//        }
//        Intent intent = new Intent(this, FeedbackChoices.class);
//        startActivity(intent);
//    }

    @OnClick(R.id.activate_phase_second)
    public void onClickActivatePhaseTwo(View view) {
        Log.i(TAG,"Clicked to activate phase two",mContext);
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onClickActivatePhaseTwo - No study found", mContext);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password to unlock phase II:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                secondPhase_Text = input.getText().toString();
                if(secondPhase_Text.equals(PASSWORD_PHASETWO)){
                    TEMPLEDataManager.setSecondPhaseActive(mContext,true);
                }else{
                    ToastManager.showShortToast(mContext, "Wrong password. Try again.");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @OnClick(R.id.activate_phase_three)
    public void onClickActivatePhaseThree(View view) {
        Log.i(TAG,"Clicked to activate phase three",mContext);
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onClickActivatePhaseThree - No study found", mContext);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password to unlock phase III:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thirdPhase_Text = input.getText().toString();
                if(thirdPhase_Text.equals(PASSWORD_PHASETHREE)){
                    TEMPLEDataManager.setThirdPhaseActive(mContext,true);
                }else{
                    ToastManager.showShortToast(mContext, "Wrong password. Try again.");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


}

