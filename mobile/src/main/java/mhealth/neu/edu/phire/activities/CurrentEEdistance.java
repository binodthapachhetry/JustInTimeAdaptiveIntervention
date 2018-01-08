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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import edu.neu.mhealth.android.wockets.library.support.Log;



public class CurrentEEdistance extends AppCompatActivity {

    private static final String TAG = "CurrentEEdistance";
    private static final String TAG_NOTES = "CurrentEEdistanceNotes";

    private static final Float METER_TO_MILE = 0.000621371f;
    private static final String dayFormat = "yyyy-MM-dd";

    private Context mContext;
    private CombinedChart combinedChartEE;
    private CombinedChart combinedChartDist;
    private TextView goalEE;
    private TextView goalDist;
    private NumberPicker numberPickerEE;
    private NumberPicker numberPickerDist;
    private Button doneButton;

    private Float eeKcal;
    private String eeKcalBoth;
    private String eeKcalPanobike;
    private String eeKcalWatch;

    private String actualGoalEEkCal;
    private Float goalEEkCal;

    private String distMeter;
    private Float distMile;
    private String actualGoalDistMile;
    private Float goalDistMile;

    private CombinedData dataEE;
    private CombinedData dataDist;

    private NavigableMap<Date,Double> EEboth;
    private NavigableMap<Date,Double> EEpano;
    private NavigableMap<Date,Double> EEwatch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_eedistance);
        mContext = getApplicationContext();
//        Log.i(TAG,"Inside",mContext);
        try {
            addListenerOnButtonClick();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addListenerOnButtonClick() throws IOException, ClassNotFoundException {
        combinedChartEE = (CombinedChart) findViewById(R.id.current_ee_barchart);
        combinedChartDist = (CombinedChart) findViewById(R.id.current_dist_barchart);
        goalEE = (TextView) findViewById(R.id.selectedEEgoal);
        goalDist = (TextView) findViewById(R.id.selectedDistanceGoal);
        numberPickerEE = (NumberPicker) findViewById(R.id.eeGoalPicker);
        numberPickerDist = (NumberPicker) findViewById(R.id.distGoalPicker);
        doneButton = (Button) findViewById(R.id.DailyDone);

        // EE current and goal

//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date dateNow = new Date();
//
//        String featureDirectory = DataManager.getDirectoryFeature(mContext);
//        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
//
//            String eeBothFile = featureDirectory + "/" + dayDirectory + "/" + "eeBoth.csv";
//            File eeBfile = new File(eeBothFile);
//            if (eeBfile.exists()) {
//                FileInputStream fileInputStream = new FileInputStream(eeBothFile);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                EEboth = (NavigableMap<Date, Double>) objectInputStream.readObject();
//                objectInputStream.close();
//                double sum = 0.0d;
//                for (double f : EEboth.values()) {
//                    sum += f;
//                }
//                eeKcalBoth = Double.toString(sum);
//            }else{
//                eeKcalBoth = "0";
//            }
//
//
//            String eePanoFile = featureDirectory + "/" + dayDirectory + "/" + "eePano.csv";
//            File eePfile = new File(eePanoFile);
//            if (eePfile.exists()) {
//                FileInputStream fileInputStream = new FileInputStream(eePanoFile);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                EEpano = (NavigableMap<Date, Double>) objectInputStream.readObject();
//                objectInputStream.close();
//                double sum = 0.0d;
//                for (double f : EEpano.values()) {
//                    sum += f;
//                }
//                eeKcalPanobike = Double.toString(sum);
//            }else{
//                eeKcalPanobike = "0";
//            }
//
//
//            String eeWatchFile = featureDirectory + "/" + dayDirectory + "/" + "eeWatch.csv";
//            File eeWfile = new File(eeWatchFile);
//            if (eeWfile.exists()) {
//                FileInputStream fileInputStream = new FileInputStream(eeWatchFile);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                EEwatch = (NavigableMap<Date, Double>) objectInputStream.readObject();
//                objectInputStream.close();
//                double sum = 0.0d;
//                for (double f : EEwatch.values()) {
//                    sum += f;
//                }
//                eeKcalWatch = Double.toString(sum);
//            }else{
//                eeKcalWatch = "0";
//            }

        eeKcalBoth = TEMPLEDataManager.getEEBoth(mContext);
        eeKcalPanobike = TEMPLEDataManager.getEEpano(mContext);
        eeKcalWatch = TEMPLEDataManager.getEEwatch(mContext);

        eeKcal = Float.valueOf(eeKcalBoth)+ Float.valueOf(eeKcalPanobike) + Float.valueOf(eeKcalWatch);


        Log.i(TAG,"Energy expenditure(kCal):"+eeKcal,mContext);
        actualGoalEEkCal = TEMPLEDataManager.getGoalEEKcal(mContext);
        if (actualGoalEEkCal == null || actualGoalEEkCal == "") {
            actualGoalEEkCal = "0";
        }
        goalEEkCal = Float.valueOf(actualGoalEEkCal) * 1.0f;
        Log.i(TAG,"Goal Energy expenditure(kCal):"+Float.toString(goalEEkCal),mContext);

        Log.i(TAG_NOTES,"EE(kCal):Goal="+Float.toString(goalEEkCal)+",Current="+ Float.toString(eeKcal),mContext);

//        if (eeKcal == null || eeKcal == "") {
//            eeKcal = "0";
//        }

        // bar plot for EE
        int[] colors = {Color.rgb(100, 149, 237)};
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> theDates = new ArrayList<>();

            barEntries.add(new BarEntry(Float.valueOf(eeKcal), 0));

            if (eeKcal > goalEEkCal) {
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
        goalDistMile = Float.valueOf(actualGoalDistMile) * 1.0f;
        Log.i(TAG,"Goal distance(miles):"+Float.toString(goalDistMile),mContext);

        if (distMeter == null || distMeter == "") {
            distMeter = "0";
        }

        // bar plot for Distance
        int[] colorsDist = {Color.rgb(100, 149, 237)};
        ArrayList<BarEntry> barEntriesDist = new ArrayList<>();
        ArrayList<String> theDatesDist = new ArrayList<>();



        distMile = Float.valueOf(distMeter)*METER_TO_MILE;
        Log.i(TAG,"Distance travelled(miles):"+Float.toString(distMile),mContext);
        Log.i(TAG_NOTES,"Distance(mi):Goal="+Float.toString(goalDistMile)+",Current="+ Float.toString(distMile),mContext);

        barEntriesDist.add(new BarEntry(distMile,0));

//            if(distMile>Float.valueOf(actualGoalDistMile)) {
            if(distMile>goalDistMile) {
                    colors[0] = Color.rgb(34,139,54);
                }
//            }


//        }

        theDatesDist.add("Today");
        BarDataSet barDataSetDist = new BarDataSet(barEntriesDist, "Actual");

        barDataSetDist.setColors(colors);
        BarData barDataDist = new BarData(theDatesDist, barDataSetDist);

        ArrayList<Entry> lineDist = new ArrayList<>();
//        lineDist.add(new Entry(Float.valueOf(actualGoalDistMile),0));
        lineDist.add(new Entry(goalDistMile,0));



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
