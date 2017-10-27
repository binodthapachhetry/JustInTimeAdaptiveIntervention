package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.lang.UCharacter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;

import edu.neu.mhealth.android.wockets.library.support.Log;

import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

public class ParticipantInfoActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Context mContext;
    private CheckBox gender;
    private CheckBox sciLevel;
    private CheckBox completeness;
    private CheckBox dataTranfer;
    private CheckBox distanceCalc;

    private NumberPicker weight;
    private NumberPicker age;
    private NumberPicker heightFt;
    private NumberPicker heightIn;

    private EditText ageCurrent;
    private EditText weightCurrent;
    private EditText genderCurrent;
    private EditText heightFtCurrent;
    private EditText heightInCurrent;
    private EditText sciLevelCurrent;
    private EditText completeCurrent;
    private EditText data;
    private EditText distance;

    private Button doneButton;

    private String partAge;
    private String partWeight;
    private String partGender;
    private String partHeightFt;
    private String partHeightIn;
    private String partSciLevel;
    private String partCompleteness;
    private Boolean onlyWifi;
    private String useForDistance;

    private static final String TAG = "ParticipantInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_info2);
        mContext = getApplicationContext();
        addListenerOnButtonClick();
        Log.i(TAG, "Inside onCreate", mContext);

    }

    private void addListenerOnButtonClick(){

        doneButton = (Button) findViewById(R.id.done);
        // text boxes
        ageCurrent = (EditText) findViewById(R.id.ageCurrent);
        ageCurrent.setEnabled(false);
        partAge = TEMPLEDataManager.getParticipantAge(mContext);
        if(partAge!=null){
            ageCurrent.setText(partAge);
        }

        genderCurrent = (EditText) findViewById(R.id.genderCurrent);
        genderCurrent.setEnabled(false);
        partGender = TEMPLEDataManager.getParticipantGender(mContext);
        if(partGender!=null){
            genderCurrent.setText(partGender);
        }

        weightCurrent = (EditText) findViewById(R.id.weightCurrent);
        weightCurrent.setEnabled(false);
        partWeight = TEMPLEDataManager.getParticipantWeight(mContext);
        if(partWeight!=null){
            weightCurrent.setText(partWeight);
        }

        heightFtCurrent = (EditText) findViewById(R.id.heightFtCurrent);
        heightFtCurrent.setEnabled(false);
        partHeightFt = TEMPLEDataManager.getParticipantHeightFt(mContext);
        if(partHeightFt!=null){
            heightFtCurrent.setText(partHeightFt);
        }

        heightInCurrent = (EditText) findViewById(R.id.heightInCurrent);
        heightInCurrent.setEnabled(false);
        partHeightIn = TEMPLEDataManager.getParticipantHeightIn(mContext);
        if(partHeightIn!=null){
            heightInCurrent.setText(partHeightIn);
        }

        completeCurrent = (EditText) findViewById(R.id.completenessCurrent);
        completeCurrent.setEnabled(false);
        partCompleteness = TEMPLEDataManager.getParticipantCompleteness(mContext);
        if(partCompleteness!=null){
            completeCurrent.setText(partCompleteness);
        }

        sciLevelCurrent = (EditText) findViewById(R.id.sciLevelCurrent);
        sciLevelCurrent.setEnabled(false);
        partSciLevel = TEMPLEDataManager.getParticipantSciLevel(mContext);
        if(partSciLevel!=null){
            sciLevelCurrent.setText(partSciLevel);
        }

        data = (EditText) findViewById(R.id.transferDataAlways);
        data.setEnabled(false);
        onlyWifi = TEMPLEDataManager.onlyWifi(mContext);
        if(onlyWifi){
            data.setText("Wifi");
        }else{
            data.setText("Both");
        }


        distance = (EditText) findViewById(R.id.distanceCalcUsed);
        distance.setEnabled(false);
        useForDistance = TEMPLEDataManager.getDistanceCalculation(mContext);
        distance.setText(useForDistance);


        // checkboxes
        distanceCalc = (CheckBox) findViewById(R.id.needspeed);
        if(useForDistance.equals("Speed")){
            distanceCalc.setChecked(true);
        }
        distanceCalc.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    distance.setText("Speed");
                    TEMPLEDataManager.setDistanceCalculation(mContext,"Speed");
                    Log.i(TAG, "Distance calculation measure set to Speed", mContext);
                }else{
                    distance.setText("Cadence");
                    TEMPLEDataManager.setDistanceCalculation(mContext,"Cadence");
                    Log.i(TAG, "Distance calculation measure set to Cadence", mContext);

                }
            }
        });

        dataTranfer = (CheckBox) findViewById(R.id.transferdata);
        if(onlyWifi){
            dataTranfer.setChecked(true);
        }else{
            dataTranfer.setChecked(false);
        }
        dataTranfer.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    data.setText("Wifi");
                    TEMPLEDataManager.setOnlyWifi(mContext,true);
                    Log.i(TAG, "Data transfer set to wifi", mContext);
                }else{
                    data.setText("Both");
                    TEMPLEDataManager.setOnlyWifi(mContext,false);
                    Log.i(TAG, "Data transfer set to both wifi and cellular network", mContext);

                }
            }
        });


        gender = (CheckBox) findViewById(R.id.gender);
        if(partGender!=null){
            if(partGender.equals("male")){
                gender.setChecked(true);
            }
        }
        gender.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    genderCurrent.setText("male");
                    TEMPLEDataManager.setParticipantGender(mContext,"male");
                    Log.i(TAG, "Gender set to male", mContext);
                }else{
                    genderCurrent.setText("female");
                    TEMPLEDataManager.setParticipantGender(mContext,"female");
                    Log.i(TAG, "Gender set to female", mContext);
                }
            }
        });

        sciLevel = (CheckBox) findViewById(R.id.sciLevel);
        if(partSciLevel!=null){
            if(partSciLevel.equals("paraplagia")){
                sciLevel.setChecked(true);
            }
        }
        sciLevel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    sciLevelCurrent.setText("paraplagia");
                    TEMPLEDataManager.setParticipantSciLevel(mContext,"paraplagia");
                    Log.i(TAG, "Sci level set to paraplagia", mContext);
                }else{
                    sciLevelCurrent.setText("tetraplagia");
                    TEMPLEDataManager.setParticipantSciLevel(mContext,"tetraplagia");
                    Log.i(TAG, "Sci level set to tetraplagia", mContext);
                }
            }
        });

        completeness = (CheckBox) findViewById(R.id.completeness);
        if(partCompleteness!=null){
            if(partCompleteness.equals("complete")){
                completeness.setChecked(true);
            }
        }
        completeness.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    completeCurrent.setText("complete");
                    TEMPLEDataManager.setParticipantCompleteness(mContext,"complete");
                    Log.i(TAG, "Completeness set to complete", mContext);
                }else{
                    completeCurrent.setText("incomplete");
                    TEMPLEDataManager.setParticipantCompleteness(mContext,"incomplete");
                    Log.i(TAG, "Completeness set to incomplete", mContext);
                }
            }
        });

        // number pickers
        weight = (NumberPicker) findViewById(R.id.weightPicker);
        weight.setMaxValue(300);
        weight.setMinValue(50);
        weight.setValue(175);
        weight.setWrapSelectorWheel(true);
        if(partWeight!=null && partWeight!=""){
            weight.setValue(Integer.parseInt(partWeight));
        }
        weight.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                weightCurrent.setText(String.valueOf(newval));
                TEMPLEDataManager.setParticipantWeight(mContext,String.valueOf(newval));
                Log.i(TAG, "Weight(in lbs) set to:"+String.valueOf(newval), mContext);

            }
        });

        age = (NumberPicker) findViewById(R.id.agePickerFinal);
        age.setMaxValue(100);
        age.setMinValue(20);
        age.setValue(35);
        age.setWrapSelectorWheel(true);
        if(partAge!=null && partAge!=""){
            age.setValue(Integer.parseInt(partAge));
        }
        age.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener(){

            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                ageCurrent.setText(String.valueOf(newval));
                TEMPLEDataManager.setParticipantAge(mContext,String.valueOf(newval));
                Log.i(TAG, "Age set to:"+String.valueOf(newval), mContext);
            }
        });


        heightFt = (NumberPicker) findViewById(R.id.heightFtPicker);
        heightFt.setMaxValue(10);
        heightFt.setMinValue(3);
        heightFt.setValue(5);
        heightFt.setWrapSelectorWheel(true);
        if(partHeightFt!=null && partHeightFt!=""){
            heightFt.setValue(Integer.parseInt(partHeightFt));
        }
        heightFt.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                heightFtCurrent.setText(String.valueOf(newval));
                TEMPLEDataManager.setParticipantHeightFt(mContext,String.valueOf(newval));
                Log.i(TAG, "Height(ft) set to:"+String.valueOf(newval), mContext);
            }
        });

        heightIn = (NumberPicker) findViewById(R.id.heightIncPicker);
        heightIn.setMaxValue(12);
        heightIn.setMinValue(0);
        heightIn.setValue(4);
        heightIn.setWrapSelectorWheel(true);
        if(partHeightIn!=null && partHeightIn!=""){
            heightIn.setValue(Integer.parseInt(partHeightIn));
        }
        heightIn.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker numberPicker, int oldval, int newval) {
                heightInCurrent.setText(String.valueOf(newval));
                TEMPLEDataManager.setParticipantHeightIn(mContext,String.valueOf(newval));
                Log.i(TAG, "Height(inch) set to:"+String.valueOf(newval), mContext);

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
