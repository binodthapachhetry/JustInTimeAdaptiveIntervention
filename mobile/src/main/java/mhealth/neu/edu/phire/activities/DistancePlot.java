package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;


public class DistancePlot extends AppCompatActivity {

    private static final String TAG = "DistancePlot";

    private static final Float METER_TO_MILE = 0.000621371f;

    private Context mContext;
    private String distanceMeter;
    private Float distanceMile;
    private String actualGoalDistanceMile;
    private Float goalDistanceMile;

    private CombinedChart combinedChart;
    private CombinedData data;
    private TextView text;
    private TextView selectedGoal;
    private NumberPicker numPick;
    private Button goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_plot);
        mContext = getApplicationContext();

        distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
        actualGoalDistanceMile = TEMPLEDataManager.getGoaldistanceTravelledMiles(mContext);

        if (actualGoalDistanceMile == null || actualGoalDistanceMile == "") {
            actualGoalDistanceMile = "0";
        }
        Log.i(TAG, "Goal distance = " + actualGoalDistanceMile, mContext);
        try {
            addListenerOnButtonClick();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addListenerOnButtonClick() throws IOException {
        goalDistanceMile = Float.valueOf(actualGoalDistanceMile) * 1.2f;

        int[] colors = {Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237)};
        text = (TextView) findViewById(R.id.DistancePlotTitle);
        text.setPadding(0,0,0,10);

        // add plot
        combinedChart = (CombinedChart) findViewById(R.id.distance_barchart);
        combinedChart.setPadding(0,40,0,10);
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        ArrayList<String> theDates = new ArrayList<>();

        // get today's date, subtract 1 day for 6 times
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE");
        String featureDirectory = DataManager.getDirectoryFeature(mContext);

        Date dateCurrent = new Date();
        dateCurrent.setTime(DateTime.getCurrentTimeInMillis());
        Calendar calCurrent = Calendar.getInstance();
        calCurrent.setTime(dateCurrent);

        calCurrent.add(Calendar.DAY_OF_MONTH, -6);
        String firstDate = sdf.format(calCurrent.getTime());
        String firstFile = featureDirectory + "/" + firstDate + "/" + "ActivityRecognitionResult.log.csv";
        File first = new File(firstFile);
        if (!first.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 0));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(firstFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,0));

            if(distanceMile>goalDistanceMile) {
                colors[0] = Color.rgb(34,139,54);
            }


        }
        String firstDateDay = sdfDay.format(calCurrent.getTime());
        theDates.add(firstDateDay);


        calCurrent.add(Calendar.DAY_OF_MONTH, 1);
        String secondDate = sdf.format(calCurrent.getTime());
        String secondFile = featureDirectory + "/" + secondDate + "/" + "ActivityRecognitionResult.log.csv";
        File second = new File(secondFile);
        if (!second.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 1));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(secondFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            Float distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,1));

            if(distanceMile>goalDistanceMile) {
                colors[1] = Color.rgb(34,139,54);
            }

        }
        String secondDateDay = sdfDay.format(calCurrent.getTime());
        theDates.add(secondDateDay);


        calCurrent.add(Calendar.DAY_OF_MONTH, 1);
        String thirdDate = sdf.format(calCurrent.getTime());
        String thirdFile = featureDirectory + "/" + thirdDate + "/" + "ActivityRecognitionResult.log.csv";
        File third = new File(thirdFile);
        if (!third.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 2));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(thirdFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            Float distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,2));

            if(distanceMile>goalDistanceMile) {
                colors[2] = Color.rgb(34,139,54);
            }

        }
        String thirdDateDay = sdfDay.format(calCurrent.getTime());
        theDates.add(thirdDateDay);

        calCurrent.add(Calendar.DAY_OF_MONTH, 1);
        String fourthDate = sdf.format(calCurrent.getTime());
        String fourthFile = featureDirectory + "/" + fourthDate + "/" + "ActivityRecognitionResult.log.csv";
        File fourth = new File(fourthFile);
        if (!fourth.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 3));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(fourthFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            Float distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,3));

            if(distanceMile>goalDistanceMile) {
                colors[3] = Color.rgb(34,139,54);
            }

        }
        String fourthDateDay = sdfDay.format(calCurrent.getTime());
        theDates.add(fourthDateDay);

        calCurrent.add(Calendar.DAY_OF_MONTH, 1);
        String fifthDate = sdf.format(calCurrent.getTime());
        String fifthFile = featureDirectory + "/" + fifthDate + "/" + "ActivityRecognitionResult.log.csv";
        File fifth = new File(fifthFile);
        if (!fifth.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 4));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(fifthFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            Float distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,4));

            if(distanceMile>goalDistanceMile) {
                colors[4] = Color.rgb(34,139,54);
            }

        }
        String fifthDateDay = sdfDay.format(calCurrent.getTime());
        theDates.add(fifthDateDay);

        calCurrent.add(Calendar.DAY_OF_MONTH, 1);
        String sixthDate = sdf.format(calCurrent.getTime());
        String sixthFile = featureDirectory + "/" + sixthDate + "/" + "ActivityRecognitionResult.log.csv";
        File sixth = new File(sixthFile);
        if (!sixth.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 5));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(sixthFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);
            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            Float distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,5));

            if(distanceMile>goalDistanceMile) {
                colors[5] = Color.rgb(34,139,54);
            }

        }
        String sixthDateDay = sdfDay.format(calCurrent.getTime());
        theDates.add(sixthDateDay);

        calCurrent.add(Calendar.DAY_OF_MONTH, 1);
        String currentDate = sdf.format(calCurrent.getTime());
        String currentFile = featureDirectory + "/" + currentDate + "/" + "ActivityRecognitionResult.log.csv";
        File current = new File(currentFile);
        if (!current.exists()) {
            // set total for the day to be zero
            barEntries.add(new BarEntry(0f, 6));
        } else {
            // read the last line of the file to get total ee kcal
            FileReader logReader = new FileReader(currentFile);
            BufferedReader br = new BufferedReader(logReader);
            String lastLine = "";
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null)
            {
                lastLine = sCurrentLine;
            }
            String distanceMeter = lastLine.split(",")[4].split("\\.")[0].substring(1);

            Log.i(TAG,distanceMeter,mContext);
            if(distanceMeter.equals("-1")){
                distanceMeter = "0";
            }
            Float distanceMile = Float.valueOf(distanceMeter)*METER_TO_MILE;
            barEntries.add(new BarEntry(distanceMile,6));

            if(distanceMile>goalDistanceMile) {
                colors[6] = Color.rgb(34,139,54);
            }

        }
        theDates.add("Today");

        BarDataSet barDataSet = new BarDataSet(barEntries,"Actual");

        barDataSet.setColors(colors);
        BarData barData = new BarData(theDates,barDataSet);


        ArrayList<Entry> line = new ArrayList<>();


        line.add(new Entry(goalDistanceMile,0));
        line.add(new Entry(goalDistanceMile,1));
        line.add(new Entry(goalDistanceMile,2));
        line.add(new Entry(goalDistanceMile,3));
        line.add(new Entry(goalDistanceMile,4));
        line.add(new Entry(goalDistanceMile,5));
        line.add(new Entry(goalDistanceMile,6));


        LineDataSet lineDataSet = new LineDataSet(line,"Goal");
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setDrawCircles(false);
        LineData lineData = new LineData(theDates,lineDataSet);
        lineData.setDrawValues(false);


        data = new CombinedData(theDates);
        data.setData(barData);
        data.setData(lineData);
        data.setDrawValues(false);
        combinedChart.setData(data);
        combinedChart.setDescription("");
        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.setTouchEnabled(false);
        combinedChart.getXAxis().setDrawGridLines(false);
        combinedChart.getLegend().setEnabled(false);
        combinedChart.getAxisRight().setDrawGridLines(false);
        combinedChart.getAxisLeft().setDrawGridLines(false);
        combinedChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);

        combinedChart.getAxisLeft().setTextSize(10f);
        combinedChart.getXAxis().setTextSize(10F);



        goBack = (Button) findViewById(R.id.distanceWeeklyDone);
        goBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FeedbackChoices.class);
                startActivity(intent);
            }
        });
    }

}
