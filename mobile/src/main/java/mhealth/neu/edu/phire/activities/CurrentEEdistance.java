package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.lang.UCharacter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import edu.neu.mhealth.android.wockets.library.support.Log;



public class CurrentEEdistance extends AppCompatActivity {

    private static final String TAG = "CurrentEEdistance";
    private static final Float METER_TO_MILE = 0.000621371f;

    private Context mContext;
    private CombinedChart combinedChartEE;
    private CombinedChart combinedChartDist;
    private TextView goalEE;
    private TextView goalDist;
    private NumberPicker numberPickerEE;
    private NumberPicker numberPickerDist;
    private Button doneButton;

    private String eeKcal;
    private String actualGoalEEkCal;
    private Float goalEEkCal;

    private String distMeter;
    private Float distMile;
    private String actualGoalDistMile;
    private Float goalDistMile;

    private CombinedData dataEE;
    private CombinedData dataDist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_eedistance);
        mContext = getApplicationContext();
        try {
            addListenerOnButtonClick();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addListenerOnButtonClick() throws IOException {
        combinedChartEE = (CombinedChart) findViewById(R.id.current_ee_barchart);
        combinedChartDist = (CombinedChart) findViewById(R.id.current_dist_barchart);
        goalEE = (TextView) findViewById(R.id.selectedEEgoal);
        goalDist = (TextView) findViewById(R.id.selectedDistanceGoal);
        numberPickerEE = (NumberPicker) findViewById(R.id.eeGoalPicker);
        numberPickerDist = (NumberPicker) findViewById(R.id.distGoalPicker);
        doneButton = (Button) findViewById(R.id.DailyDone);

        // EE current and goal
        eeKcal = TEMPLEDataManager.getEEKcal(mContext);
        actualGoalEEkCal = TEMPLEDataManager.getGoalEEKcal(mContext);
        if (actualGoalEEkCal == null || actualGoalEEkCal == "") {
            actualGoalEEkCal = "0";
        }
        goalEEkCal = Float.valueOf(actualGoalEEkCal) * 1.2f;
        if (eeKcal == null || eeKcal == "") {
            eeKcal = "0";
        }

        // bar plot for EE
        int[] colors = {Color.rgb(100, 149, 237)};
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> theDates = new ArrayList<>();

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String featureDirectory = DataManager.getDirectoryFeature(mContext);
//        Date dateCurrent = new Date();
//        dateCurrent.setTime(DateTime.getCurrentTimeInMillis());
//        Calendar calCurrent = Calendar.getInstance();
//        calCurrent.setTime(dateCurrent);
//        String firstDate = sdf.format(calCurrent.getTime());
//        String firstFile = featureDirectory + "/" + firstDate + "/" + "ActivityRecognitionResult.log.csv";
//        File file = new File(firstFile);
//        if (!file.exists()) {
//            barEntries.add(new BarEntry(0f, 0));
//        } else {
            // read the last line of the file to get total ee kcal
//            FileReader logReader = new FileReader(file);
//            BufferedReader br = new BufferedReader(logReader);
//            String lastLine = "";
//            String sCurrentLine;
//            while ((sCurrentLine = br.readLine()) != null) {
//                lastLine = sCurrentLine;
//            }
//            String eekCal = lastLine.split(",")[5].split("\\.")[0];
//            Log.i(TAG, eekCal, mContext);

            barEntries.add(new BarEntry(Float.valueOf(eeKcal), 0));

            if (Float.valueOf(eeKcal) > goalEEkCal) {
                colors[0] = Color.rgb(34, 139, 54);
            }


//        }

        theDates.add("Today");
        BarDataSet barDataSet = new BarDataSet(barEntries, "Actual");

        barDataSet.setColors(colors);
        BarData barData = new BarData(theDates, barDataSet);

        ArrayList<Entry> line = new ArrayList<>();
        line.add(new Entry(goalEEkCal, 0));


        LineDataSet lineDataSet = new LineDataSet(line, "Goal");
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(R.color.colorGreen);
        LineData lineData = new LineData(theDates, lineDataSet);
        lineData.setDrawValues(false);

        dataEE = new CombinedData(theDates);
        dataEE.setData(barData);
        dataEE.setData(lineData);
        dataEE.setDrawValues(false);
        combinedChartEE.setData(dataEE);
        combinedChartEE.setDescription("");
        combinedChartEE.getAxisRight().setEnabled(false);
        combinedChartEE.setTouchEnabled(false);
        combinedChartEE.getXAxis().setDrawGridLines(false);
        combinedChartEE.getLegend().setEnabled(false);
        combinedChartEE.getAxisRight().setDrawGridLines(false);
        combinedChartEE.getAxisLeft().setDrawGridLines(false);
        combinedChartEE.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        combinedChartEE.getAxisLeft().setTextSize(10f);
        combinedChartEE.getXAxis().setTextSize(10F);




        // Distance current and goal
        distMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
        actualGoalDistMile = TEMPLEDataManager.getGoaldistanceTravelledMiles(mContext);
        if (actualGoalDistMile == null || actualGoalDistMile == "") {
            actualGoalDistMile = "0";
        }
        goalDistMile = Float.valueOf(actualGoalDistMile) * METER_TO_MILE;
        if (distMeter == null || distMeter == "") {
            distMeter = "0";
        }

        // bar plot for Distance
        int[] colorsDist = {Color.rgb(100, 149, 237)};
        ArrayList<BarEntry> barEntriesDist = new ArrayList<>();
        ArrayList<String> theDatesDist = new ArrayList<>();

//        if (!file.exists()) {
//            barEntries.add(new BarEntry(0f, 0));
//        } else {
//            // read the last line of the file to get total ee kcal
//            FileReader logReader = new FileReader(file);
//            BufferedReader br = new BufferedReader(logReader);
//            String lastLine = "";
//            String sCurrentLine;
//            while ((sCurrentLine = br.readLine()) != null) {
//                lastLine = sCurrentLine;
//            }
//            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
//            Log.i(TAG,distanceMeter,mContext);
//            if(distanceMeter.equals("-1")){
//                distanceMeter = "0";
//            }

            distMile = Float.valueOf(distMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distMile,0));

            if(distMile>Float.valueOf(actualGoalDistMile)) {
                colors[0] = Color.rgb(34,139,54);
            }


//        }

        theDatesDist.add("Today");
        BarDataSet barDataSetDist = new BarDataSet(barEntriesDist, "Actual");

        barDataSetDist.setColors(colors);
        BarData barDataDist = new BarData(theDatesDist, barDataSetDist);

        ArrayList<Entry> lineDist = new ArrayList<>();
        lineDist.add(new Entry(Float.valueOf(actualGoalDistMile),0));


        LineDataSet lineDataSetDist = new LineDataSet(lineDist, "Goal");
        lineDataSetDist.setColor(Color.BLACK);
        lineDataSetDist.setCircleColor(R.color.colorGreen);


        LineData lineDataDist = new LineData(theDatesDist, lineDataSetDist);
        lineDataDist.setDrawValues(false);

        dataDist = new CombinedData(theDatesDist);
        dataDist.setData(barDataDist);
        dataDist.setData(lineDataDist);
        dataDist.setDrawValues(false);
        combinedChartDist.setData(dataDist);
        combinedChartDist.setDescription("");
        combinedChartDist.getAxisRight().setEnabled(false);
        combinedChartDist.setTouchEnabled(false);
        combinedChartDist.getXAxis().setDrawGridLines(false);
        combinedChartDist.getLegend().setEnabled(false);
        combinedChartDist.getAxisRight().setDrawGridLines(false);
        combinedChartDist.getAxisLeft().setDrawGridLines(false);
        combinedChartDist.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        combinedChartDist.getAxisLeft().setTextSize(10f);
        combinedChartEE.getXAxis().setTextSize(10F);





//        // set text for goals
//        goalEE.setText(actualGoalEEkCal + " kCal");
//        goalDist.setText(actualGoalDistMile + " mile");
//
//        // set number picker for EE goal
//        numberPickerEE.setMaxValue(30);
//        numberPickerEE.setMinValue(0);
//
//        int minValue = 1000;
//        int maxValue = 4000;
//        int step = 100;
//
//        int count = (maxValue - minValue) / step;
//        final String[] numberValues = new String[count + 1];
//
//        for (int i = 0; i < numberValues.length; i++) {
//            numberValues[i] = String.valueOf(minValue + i * step);
//        }
//
//        numberPickerEE.setDisplayedValues(numberValues);
//        numberPickerEE.setOnValueChangedListener(new NumberPicker.
//                OnValueChangeListener() {
//
//            @Override
//            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
//                goalEE.setText(String.valueOf(numberValues[newval]) + " kCal");
//                TEMPLEDataManager.setGoalEEKcal(mContext, String.valueOf(numberValues[newval]));
//            }
//        });
//
//        // set number picker for EE goal
//        numberPickerDist.setMaxValue(29);
//        numberPickerDist.setMinValue(0);
//
//        double minValueDist = 0d;
//        double maxValueDist = 3d;
//        double stepDist = 0.2d;
//
//        final String[] numberValuesDist = new String[16];
//
//        for (int i = 0; i < numberValuesDist.length; i++) {
//            numberValuesDist[i] = String.valueOf(Math.round((minValueDist + i * stepDist) * 10.0) / 10.0);
//        }
//
//        double minValueDist1 = 3d;
//        double maxValueDist1 = 10d;
//        double stepDist1 = 0.5d;
//
//        final String[] numberValuesDist1 = new String[14];
//
//        for (int i = 1; i < numberValuesDist1.length + 1; i++) {
//            numberValuesDist1[i - 1] = String.valueOf(minValueDist1 + i * stepDist1);
//        }
//
//        final String[] numberV = (String[]) ArrayUtils.addAll(numberValuesDist, numberValuesDist1);
//
//        for (int i = 0; i < numberV.length; i++) {
//            Log.i(TAG, String.valueOf(numberV[i]), mContext);
//        }
//        numberPickerDist.setDisplayedValues(numberV);
//        numberPickerDist.setOnValueChangedListener(new NumberPicker.
//                OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
//                goalDist.setText(String.valueOf(numberV[newval]) + " mile");
//                TEMPLEDataManager.setGoaldistanceTravelledMiles(mContext, String.valueOf(numberV[newval]));
//            }
//        });

        // done button
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FeedbackChoices.class);
                startActivity(intent);
            }
        });

    }
}
