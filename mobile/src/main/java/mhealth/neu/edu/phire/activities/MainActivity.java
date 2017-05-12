package mhealth.neu.edu.phire.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.activities.WocketsActivity;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
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
    public static final String watchTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private Context mContext;
    private Activity mActivity;

    @BindView(R.id.activity_main_app_version)
    TextView appVersionTextView;

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

        String dateString = new SimpleDateFormat(watchTimeFormat).format(new Date(SharedPrefs.getLong("LAST_WATCH_IN_CONNECTION_TIME",0, mContext)));


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
            ToastManager.showShortToast(mContext, "Start Time set as " + DateTime.getTimestampString(startTime));
            Log.i(TAG, "Start Time set as " + DateTime.getTimestampString(startTime), mContext);
        }

        if (DataManager.getEndDate(mContext) == 0) {
            long endTime = DateTime.getTimeInMillis(TEMPLEConstants.END_HOUR, TEMPLEConstants.END_MINUTE);
            // https://bitbucket.org/mhealthresearchgroup/wockets-android/issues/127/show-start-and-end-date-on-screen
            endTime = endTime + DateTime.DAYS_8_IN_MILLIS;
            DataManager.setEndDate(mContext, endTime);
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
        if (backgroundImageClick < 10) {
            backgroundImageClick++;
            return;
        }

        backgroundImageClick = 1;
        startActivity(new Intent(this, SetupActivity.class));
    }

}

