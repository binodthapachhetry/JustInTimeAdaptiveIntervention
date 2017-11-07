package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

public class SelectDayTimeForWeeklySurvey extends AppCompatActivity {

    private Context mContext;
    private static final String TAG = "SelectDayTimeForWeeklySurvey";
    private NumberPicker dayOfWeek;
    private TimePicker startTime;
    private TimePicker stopTime;
    private Button done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_day_time_for_weekly_survey);
        mContext = getApplicationContext();
        addListenerOnButtonClick();
        Log.i(TAG, "Inside onCreate", mContext);
    }

    private void addListenerOnButtonClick(){
        // number pickers
        dayOfWeek = (NumberPicker) findViewById(R.id.dayPicker);
        dayOfWeek.setMaxValue(6);
        dayOfWeek.setMinValue(0);
        dayOfWeek.setValue(1);
        dayOfWeek.setWrapSelectorWheel(true);
        final List<String> daysInWeek = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        dayOfWeek.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                TEMPLEDataManager.setWeeklySurveyDay(mContext,daysInWeek.get(newval));
                Log.i(TAG, "Selected day for weekly survey:"+daysInWeek.get(newval), mContext);

            }
        });

        startTime = (TimePicker) findViewById(R.id.weeklySurveyStart);
        stopTime = (TimePicker) findViewById(R.id.weeklySurveyStop);

        done = (Button) findViewById(R.id.setup_survey_done);
        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DataManager.setWeeklySurveyStartHour(mContext,startTime.getCurrentHour());
                DataManager.setWeeklySurveyStartMinute(mContext,startTime.getCurrentMinute());
                DataManager.setWeeklySurveyStopHour(mContext,stopTime.getCurrentHour());
                DataManager.setWeeklySurveyStopMinute(mContext,stopTime.getCurrentMinute());
                Log.i(TAG, "Selected start and stop hour and minute:"+Integer.toString(startTime.getCurrentHour())+","+Integer.toString(stopTime.getCurrentHour()), mContext);

                Intent intent = new Intent(mContext, SetupActivity.class);
                startActivity(intent);
            }
        });

    }
}
