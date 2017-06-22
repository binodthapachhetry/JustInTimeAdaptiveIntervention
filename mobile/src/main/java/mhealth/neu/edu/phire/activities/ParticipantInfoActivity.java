package mhealth.neu.edu.phire.activities;

import android.content.Context;
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

import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

public class ParticipantInfoActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Context mContext;
    private CheckBox gender;
    private CheckBox sciLevel;
    private CheckBox completeness;
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

    private String partAge;
    private String partWeight;
    private String partGender;
    private String partHeightFt;
    private String partHeightIn;
    private String partSciLevel;
    private String partCompleteness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_info2);
        mContext = getApplicationContext();
        addListenerOnButtonClick();

    }

    public void addListenerOnButtonClick(){

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

        // checkboxes
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
                }else{
                    genderCurrent.setText("female");
                    TEMPLEDataManager.setParticipantGender(mContext,"female");
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
                }else{
                    sciLevelCurrent.setText("tetraplagia");
                    TEMPLEDataManager.setParticipantSciLevel(mContext,"tetraplagia");
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
                }else{
                    completeCurrent.setText("incomplete");
                    TEMPLEDataManager.setParticipantCompleteness(mContext,"incomplete");
                }
            }
        });

        // number pickers
        weight = (NumberPicker) findViewById(R.id.weightPicker);
        weight.setMaxValue(300);
        weight.setMinValue(50);
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

            }
        });

        age = (NumberPicker) findViewById(R.id.agePickerFinal);
        age.setMaxValue(100);
        age.setMinValue(20);
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
            }
        });


        heightFt = (NumberPicker) findViewById(R.id.heightFtPicker);
        heightFt.setMaxValue(10);
        heightFt.setMinValue(3);
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
            }
        });

        heightIn = (NumberPicker) findViewById(R.id.heightIncPicker);
        heightIn.setMaxValue(12);
        heightIn.setMinValue(0);
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

            }
        });


    }




}
