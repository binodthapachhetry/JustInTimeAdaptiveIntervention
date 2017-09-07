package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.common.api.GoogleApiClient;
import com.opencsv.CSVReader;
import com.github.mikephil.charting.components.LimitLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

public class EnergyPlot extends AppCompatActivity {

    private static final String TAG = "EnergyPlot";

    private Context mContext;
    private String eeKcal;
    private Float goalEEkCal;
    private String actualGoalEEkCal;
    private String userEnteredEEkCal;

    private CombinedChart combinedChart;
    private CombinedData data;
    private TextView text;
    private TextView selectedGoal;
    private NumberPicker numPick;
    private Button goBack;

    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_plot);
        mContext = getApplicationContext();

        eeKcal = TEMPLEDataManager.getEEKcal(mContext);
        actualGoalEEkCal = TEMPLEDataManager.getGoalEEKcal(mContext);
        if(actualGoalEEkCal==null|| actualGoalEEkCal==""){
            actualGoalEEkCal = "0";
        }
        Log.i(TAG,"Inside",mContext);
        Log.i(TAG,"Goal EE = "+ actualGoalEEkCal,mContext);
        try {
            addListenerOnButtonClick();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addListenerOnButtonClick() throws IOException {


        float halfEEgoal = Float.valueOf(actualGoalEEkCal)/2f;
        goalEEkCal = Float.valueOf(actualGoalEEkCal) * 1.0f;

        int[] colors = {Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237),Color.rgb(100,149,237)};
        text = (TextView) findViewById(R.id.EEplotTitle);
        text.setPadding(0,0,0,10);


//        selectedGoal = (TextView) findViewById(R.id.selectedEEgoal);
//        selectedGoal.setText(goalEEkCal+" kCal");

        // add plot
        combinedChart = (CombinedChart) findViewById(R.id.ee_barchart);
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 1:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),0));

            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[0] = Color.rgb(250,128,114);
//            }

            if(Float.valueOf(eekCal)>goalEEkCal) {
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 2:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),1));

            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[1] = Color.rgb(250,128,114);
//            }
            if(Float.valueOf(eekCal)>goalEEkCal) {
                colors[0] = Color.rgb(34,139,54);
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 3:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),2));
            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[2] = Color.rgb(250,128,114);
//            }
            if(Float.valueOf(eekCal)>goalEEkCal){
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 4:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),3));
            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[3] = Color.rgb(250,128,114);
//            }
            if(Float.valueOf(eekCal)>goalEEkCal){
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 5:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),4));
            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[4] = Color.rgb(250,128,114);
//            }
            if(Float.valueOf(eekCal)>goalEEkCal){
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 6:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),5));
            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[5] = Color.rgb(250,128,114);
//            }
            if(Float.valueOf(eekCal)>goalEEkCal){
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
            String eekCal = lastLine.split(",")[5].split("\\.")[0].substring(1);
            Log.i(TAG,"Energy expenditure day 7:"+ eekCal,mContext);
            barEntries.add(new BarEntry(Float.valueOf(eekCal),6));
            int retval = Float.compare(Float.valueOf(eekCal),halfEEgoal);
//            if(retval < 0) {
//                colors[6] = Color.rgb(250,128,114);
//            }
            if(Float.valueOf(eekCal)>goalEEkCal){
                colors[6] = Color.rgb(34,139,54);
            }

        }
        theDates.add("Today");

        BarDataSet barDataSet = new BarDataSet(barEntries,"Actual");

        barDataSet.setColors(colors);
        BarData barData = new BarData(theDates,barDataSet);


        ArrayList<Entry> line = new ArrayList<>();
        line.add(new Entry(goalEEkCal,0));
        line.add(new Entry(goalEEkCal,1));
        line.add(new Entry(goalEEkCal,2));
        line.add(new Entry(goalEEkCal,3));
        line.add(new Entry(goalEEkCal,4));
        line.add(new Entry(goalEEkCal,5));
        line.add(new Entry(goalEEkCal,6));


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

//        LimitLine upper_limit = new LimitLine(goalEEkCal, "Goal");
//        upper_limit.setLineWidth(4f);
//        upper_limit.enableDashedLine(10f, 10f, 0f);
//        upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        upper_limit.setTextSize(10f);
//        combinedChart.getAxisLeft().addLimitLine(upper_limit);



        goBack = (Button) findViewById(R.id.eeWeeklyDone);
        goBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FeedbackChoices.class);
                startActivity(intent);
            }
        });


//        numPick = (NumberPicker) findViewById(R.id.eeGoalPicker);
//        numPick.setMaxValue(30);
//        numPick.setMinValue(0);
//
//        int minValue = 1000;
//        int maxValue = 4000;
//        int step = 100;
//
//        int count = (maxValue-minValue)/step;
//
//        final String[] numberValues = new String[count+1];
//        Log.i(TAG,String.valueOf(numberValues.length),mContext);
//
//        for (int i = 0; i < numberValues.length; i++)
//        {
//            numberValues[i] = String.valueOf(minValue + i*step);
//        }
//
//        numPick.setDisplayedValues(numberValues);
//        numPick.setOnValueChangedListener(new NumberPicker.
//                OnValueChangeListener() {
//
//            @Override
//            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
//                selectedGoal.setText(String.valueOf(numberValues[newval])+" kCal");
//                TEMPLEDataManager.setGoalEEKcal(mContext,String.valueOf(numberValues[newval]));
//            }
//        });

    }

}


