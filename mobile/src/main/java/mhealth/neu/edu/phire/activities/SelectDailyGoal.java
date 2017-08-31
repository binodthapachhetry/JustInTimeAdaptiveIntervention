package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;

import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;


public class SelectDailyGoal extends AppCompatActivity {

    private static final String TAG = "SelDailyGoal";
    private static final Float METER_TO_MILE = 0.000621371f;

    private Context mContext;
    private TextView goalEE;
    private TextView goalDist;
    private NumberPicker numberPickerEE;
    private NumberPicker numberPickerDist;
    private Button doneButton;

    private String actualGoalDistMile;
    private String actualGoalEEkCal;
    private Float goalDistMile;
    private Float goalEEkCal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_daily_goal);
        mContext = getApplicationContext();
        try {
            addListenerOnButtonClick();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addListenerOnButtonClick() throws IOException {
        goalEE = (TextView) findViewById(R.id.selectedEEgoal);
        goalDist = (TextView) findViewById(R.id.selectedDistanceGoal);
        numberPickerEE = (NumberPicker) findViewById(R.id.eeGoalPicker);
        numberPickerDist = (NumberPicker) findViewById(R.id.distGoalPicker);
        doneButton = (Button) findViewById(R.id.SelectGoalDone);

        actualGoalDistMile = TEMPLEDataManager.getGoaldistanceTravelledMiles(mContext);
        if (actualGoalDistMile == null || actualGoalDistMile == "") {
            actualGoalDistMile = "0";
        }
        goalDistMile = Float.valueOf(actualGoalDistMile) * METER_TO_MILE;

        actualGoalEEkCal = TEMPLEDataManager.getGoalEEKcal(mContext);
        if (actualGoalEEkCal == null || actualGoalEEkCal == "") {
            actualGoalEEkCal = "0";
        }
        goalEEkCal = Float.valueOf(actualGoalEEkCal) * 1.2f;

        // set text for goals
        goalEE.setText(actualGoalEEkCal + " kCal");
        goalDist.setText(actualGoalDistMile + " mile");

        // set number picker for EE goal
        numberPickerEE.setMaxValue(30);
        numberPickerEE.setMinValue(0);

        int minValue = 1000;
        int maxValue = 4000;
        int step = 100;

        int count = (maxValue - minValue) / step;
        final String[] numberValues = new String[count + 1];

        for (int i = 0; i < numberValues.length; i++) {
            numberValues[i] = String.valueOf(minValue + i * step);
        }

        numberPickerEE.setDisplayedValues(numberValues);
        numberPickerEE.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                goalEE.setText(String.valueOf(numberValues[newval]) + " kCal");
                TEMPLEDataManager.setGoalEEKcal(mContext, String.valueOf(numberValues[newval]));
            }
        });

        // set number picker for EE goal
        numberPickerDist.setMaxValue(29);
        numberPickerDist.setMinValue(0);

        double minValueDist = 0d;
        double maxValueDist = 3d;
        double stepDist = 0.2d;

        final String[] numberValuesDist = new String[16];

        for (int i = 0; i < numberValuesDist.length; i++) {
            numberValuesDist[i] = String.valueOf(Math.round((minValueDist + i * stepDist) * 10.0) / 10.0);
        }

        double minValueDist1 = 3d;
        double maxValueDist1 = 10d;
        double stepDist1 = 0.5d;

        final String[] numberValuesDist1 = new String[14];

        for (int i = 1; i < numberValuesDist1.length + 1; i++) {
            numberValuesDist1[i - 1] = String.valueOf(minValueDist1 + i * stepDist1);
        }

        final String[] numberV = (String[]) ArrayUtils.addAll(numberValuesDist, numberValuesDist1);

        for (int i = 0; i < numberV.length; i++) {
            Log.i(TAG, String.valueOf(numberV[i]), mContext);
        }
        numberPickerDist.setDisplayedValues(numberV);
        numberPickerDist.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                goalDist.setText(String.valueOf(numberV[newval]) + " mile");
                TEMPLEDataManager.setGoaldistanceTravelledMiles(mContext, String.valueOf(numberV[newval]));
            }
        });

        // done button
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SetupActivity.class);
                startActivity(intent);
            }
        });


    }
}
