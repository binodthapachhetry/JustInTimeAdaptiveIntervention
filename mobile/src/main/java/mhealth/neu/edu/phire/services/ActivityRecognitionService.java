package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.provider.ContactsContract;


import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;


import com.opencsv.CSVReader;

import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.pmml.jaxbbindings.INTERPOLATIONMETHOD;
import weka.estimators.Estimator;

import edu.neu.mhealth.android.wockets.library.support.DateTime;
import org.apache.commons.lang3.time.DateUtils;


public class ActivityRecognitionService extends WocketsIntentService {

    private static final String TAG = "ActivityRecognitionService";
    private static final String TAG_NOTES = "ActivityRecognitionServiceNotes";

    private static final String TAG_NOTES_SECOND = "EENotes";

    private static final Double MULT = 3.5d;
    private static final String dayFormat = "yyyy-MM-dd";
    public static final String hourFormat = "HH-z";
    private Context mContext;
    private Date dateNow;
    private BluetoothAdapter mBluetoothAdapter;
    public Classifier allActivitiesClassifier;
    public Classifier movingClassifier;
    public Classifier nonMovingClassifier;

    private NavigableMap<Date,Double> EEboth;
    private NavigableMap<Date,Double> EEpano;
    private NavigableMap<Date,Double> EEwatch;

//    private NavigableMap<Date,Integer> BoutBoth;
//    private NavigableMap<Date,Integer> BoutPano;
//    private NavigableMap<Date,Integer> BoutWatch;

    private Double prevRot;
    private Date prevDate;

    private NavigableMap<Long, Double> map;
    private Long lastTimeSpeed;
    private long thisMilliseconds;
    private long currentMilliseconds;
    private long lastARserviceRun;
    private File sFile;
    private File wfFile;
    private String arFile;
    private String arFileDay;
    private String arFilePanobike;
    private String arFilePanobikeDay;
    private String arFileWatch;
    private String arFileWatchDay;
    private long lastARwindowStopTime;
    private long lastWatchDataReadingStopTime;
    private long lastPanobikeReadingStopTime;

    private Double partWeightKg;
    private String sciLevel;
    private Double partSciLevel;
    private Double partMETmultiply;
    private Double METthresh;
    private Double participantMETkcal;
    private HashMap<String,Double> mapMET;
    private HashMap<String,Double> mapSciLevel;

    public Instances allActivitiesUnpredicted;
    public Instances movingUnpredicted;
    public Instances nonMovingUnpredicted;


    private static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
    private static final long PANOBIKE_GAP_MINUTE = 35;
    private double wheelCircumference;
    private static final float nearStationary = 3f;
    private static final float someWheelMovement = 12f;

    private long eeCalcLastRun;
    private String eeKcal;
    private String eeKcalPanobike;
    private String eeKcalWatch;

    private String eeKcalBothTot;
    private String eeKcalPanobikeTot;
    private String eeKcalWatchTot;
    private String eeKcalTot;


    private String distanceMeter;
    private String useForDistance;

    private int dayOfMonth;
    private int dayOfMonthEEcalcLastRun;

    private SimpleDateFormat dateFormatToConvert;
    private SimpleDateFormat dateFormatConvertFromWatch;

    // order of attributes/classes needs to be exactly equal to those used for training
    final Attribute attribute_zcr_Z = new Attribute("zcr_Z");
    final Attribute attribute_mcr_XYZ = new Attribute("mcr_XYZ");
    final Attribute attribute_mad_median_X = new Attribute("mad_median_X");
    final Attribute attribute_rms_Y = new Attribute("rms_Y");
    final Attribute attribute_mad_XYZ = new Attribute("mad_XYZ");

    final List<String> allActivitiesclasses = new ArrayList<String>() {
        {
            add("11"); // cls nr 1
            add("12"); // cls nr 2
            add("13"); // cls nr 3
        }
    };
    final Attribute allActivitiesAttributeClass = new Attribute("class",allActivitiesclasses);
    ArrayList<Attribute> allActivitiesAttributeList = new ArrayList<Attribute>(3) {
        {
            add(attribute_zcr_Z);
            add(attribute_mcr_XYZ);
            add(attribute_mad_median_X);
            add(attribute_rms_Y);
            add(attribute_mad_XYZ);
            add(allActivitiesAttributeClass);
        }
    };

    // order of attributes/classes needs to be exactly equal to those used for training
    final Attribute attribute_Var_Y = new Attribute("Var_Y");
    final Attribute attribute_mad_med_XYZ = new Attribute("mad_med_XYZ");
    final Attribute attribute_VAR_X = new Attribute("Var_X");
    final Attribute attribute_rms_Z = new Attribute("rms_Z");

    final List<String> movingclasses = new ArrayList<String>() {
        {
            add("4"); // cls nr 1
            add("5"); // cls nr 2
            add("6"); // cls nr 3
        }
    };
    final Attribute movingAttributeClass = new Attribute("class",movingclasses);
    ArrayList<Attribute> movingAttributeList = new ArrayList<Attribute>(3) {
        {
            add(attribute_Var_Y);
            add(attribute_mad_med_XYZ);
            add(attribute_VAR_X);
            add(attribute_rms_Z);
            add(movingAttributeClass);
        }
    };

    // order of attributes/classes needs to be exactly equal to those used for training
    final Attribute attribute_mad_median_XYZ_nm = new Attribute("mad_median_XYZ");
    final Attribute attribute_mad_median_Z = new Attribute("mad_median_Z");
    final Attribute attribute_mcr_X = new Attribute("mcr_X");

    final List<String> nonmovingclasses = new ArrayList<String>() {
        {
            add("1"); // cls nr 1
            add("2"); // cls nr 2
            add("3"); // cls nr 3
        }
    };
    final Attribute nonmovingAttributeClass = new Attribute("class",nonmovingclasses);
    ArrayList<Attribute> nonmovingAttributeList = new ArrayList<Attribute>(3) {
        {
            add(attribute_mad_median_XYZ_nm);
            add(attribute_mad_median_Z);
            add(attribute_mcr_X);
            add(nonmovingAttributeClass);
        }
    };

    String[] arr = new String[] {"4", "6"};
    String eeWatchFile;
    String eePanoFile;
    String eeBothFile;
    String boutBothFile;
    String boutPanoFile;
    String boutWatchFile;

//    String[] arr = new String[] {  "1",  "3", "5"};


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);
        Log.i(TAG, "Getting last run of activity recognition service", mContext);
        try {
            doAR();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAR() throws Exception {

        NotificationManager.clearFeedbackNotification(mContext);

        // get participant related info for computing energy expenditure
        mapMET = new HashMap<String,Double>();
        mapMET.put("1",1d);
        mapMET.put("2",3d);
        mapMET.put("3",1.6d);
        mapMET.put("4",3.5d);
        mapMET.put("5",1d);
        mapMET.put("6",4.8d);
        mapMET.put("7",3.2d);
        mapMET.put("11",1d);
        mapMET.put("12",3.5d);
        mapMET.put("13",3.2d);

        mapSciLevel = new HashMap<String,Double>();
        mapSciLevel.put("paraplagia",2.77d);
        mapSciLevel.put("tetraplagia",2.52d);

        if(TEMPLEDataManager.getParticipantWeight(mContext)!="" && TEMPLEDataManager.getParticipantSciLevel(mContext)!=""){
            partWeightKg = (Double.parseDouble(TEMPLEDataManager.getParticipantWeight(mContext))* TEMPLEConstants.LB_KG_CONVERT)/TEMPLEConstants.MET_DIVIDE;
            Log.i(TAG,"Participant weight in lbs is :"+ TEMPLEDataManager.getParticipantWeight(mContext),mContext);

            partSciLevel = mapSciLevel.get(TEMPLEDataManager.getParticipantSciLevel(mContext));
            Log.i(TAG,"Participant sci level is :"+ TEMPLEDataManager.getParticipantSciLevel(mContext),mContext);

            partMETmultiply = partWeightKg*partSciLevel;
        }else{
            Log.i(TAG,"Patient information to calculate energy expenditure not found",mContext);
            partMETmultiply = 0d;
        }
        METthresh = MULT*partMETmultiply;
        Log.i(TAG, "MET threshold set to include PA minutes when great than "+ Double.toString(METthresh), mContext);

        int pamin = TEMPLEDataManager.getPAminutesGoal(mContext);
        Log.i(TAG,"goal PA mins="+Integer.toString(pamin),mContext);
//        Log.i(TAG_NOTES,"Goal PA mins="+Integer.toString(pamin),mContext);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateNow = new Date();
        String featureDirectory = DataManager.getDirectoryFeature(mContext);
        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
        // see if all the six serialized files exist
        // if not create them
        // find the code down where for a new day it calculates the total PA minutes
        // find the code down where for a new day it calculates the average bout length

        File path = new File(featureDirectory + "/" + dayDirectory);
        if(!path.exists()){
            path.mkdirs();
        }

        if(EEboth==null) {
            eeBothFile = featureDirectory + "/" + dayDirectory + "/" + "eeBoth.csv";
            File eeBfile = new File(eeBothFile);
            if (eeBfile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(eeBothFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                try {
                    EEboth = (NavigableMap<Date, Double>) objectInputStream.readObject();
                }catch (EOFException e){
                    e.printStackTrace();
                    EEboth = new TreeMap<Date, Double>();
                }
                objectInputStream.close();
            } else {
                EEboth = new TreeMap<Date, Double>();
            }
        }


        if(EEpano==null) {
            eePanoFile = featureDirectory + "/" + dayDirectory + "/" + "eePano.csv";
            File eePfile = new File(eePanoFile);
            if (eePfile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(eePanoFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                try {
                    EEpano = (NavigableMap<Date, Double>) objectInputStream.readObject();
                }catch (EOFException e){
                    e.printStackTrace();
                    EEpano = new TreeMap<Date, Double>();
                }
//                EEpano = (NavigableMap<Date, Double>) objectInputStream.readObject();
                objectInputStream.close();
            } else {
                EEpano = new TreeMap<Date, Double>();
            }
        }


        if(EEwatch==null) {
            eeWatchFile = featureDirectory + "/" + dayDirectory + "/" + "eeWatch.csv";
            File eeWfile = new File(eeWatchFile);
            if (eeWfile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(eeWatchFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                try {
                    EEwatch = (NavigableMap<Date, Double>) objectInputStream.readObject();
                }catch (EOFException e){
                    e.printStackTrace();
                    EEwatch = new TreeMap<Date, Double>();
                }
//                EEwatch = (NavigableMap<Date, Double>) objectInputStream.readObject();
                objectInputStream.close();
            } else {
                EEwatch = new TreeMap<Date, Double>();
            }
        }


//        if(BoutBoth ==null) {
//            boutBothFile = featureDirectory + "/" + dayDirectory + "/" + "boutBoth.csv";
//            File boutBfile = new File(boutBothFile);
//            if (boutBfile.exists()) {
//                FileInputStream fileInputStream = new FileInputStream(boutBothFile);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                BoutBoth = (NavigableMap<Date, Integer>) objectInputStream.readObject();
//                objectInputStream.close();
//            } else {
//                BoutBoth = new TreeMap<Date,Integer>();
//            }
//        }
//
//        if(BoutPano==null) {
//            boutPanoFile = featureDirectory + "/" + dayDirectory + "/" + "boutPano.csv";
//            File boutPfile = new File(boutPanoFile);
//            if (boutPfile.exists()) {
//                FileInputStream fileInputStream = new FileInputStream(boutPanoFile);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                BoutPano = (NavigableMap<Date, Integer>) objectInputStream.readObject();
//                objectInputStream.close();
//            } else {
//                BoutPano = new TreeMap<Date,Integer>();
//            }
//        }
//
//        if(BoutWatch==null) {
//            boutWatchFile = featureDirectory + "/" + dayDirectory + "/" + "boutWatch.csv";
//            File boutWfile = new File(boutWatchFile);
//            if (boutWfile.exists()) {
//                FileInputStream fileInputStream = new FileInputStream(boutWatchFile);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                BoutWatch = (NavigableMap<Date, Integer>) objectInputStream.readObject();
//                objectInputStream.close();
//            } else {
//                BoutWatch = new TreeMap<Date, Integer>();
//            }
//        }


        dateFormatToConvert = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatConvertFromWatch = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");
        // check if bluetooth connected
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            ToastManager.showShortToast(mContext, "This device does not support bluetooth.");
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                ToastManager.showShortToast(mContext, "Please enable bluetooth for the app to function properly.");
                return;
            }
        }
//        wheelCircumference = (Integer.valueOf(TEMPLEDataManager.getWheelDiameterCm(mContext))/200)*((float) Math.PI);
        if(TEMPLEDataManager.getWheelDiameterCm(mContext)!="") {
            wheelCircumference = ((Integer.valueOf(TEMPLEDataManager.getWheelDiameterCm(mContext)) * ((double) Math.PI))) * 0.0254d;
        }else{
            wheelCircumference = 0.0d;
        }
        Log.i(TAG, "Wheel circumference:" + Double.toString(wheelCircumference), mContext);

        // get current time in milliseconds
        currentMilliseconds = System.currentTimeMillis();


        Log.i(TAG, "Getting last run of activity recognition service", mContext);
        lastARwindowStopTime = DataManager.getLastARwindowStopTime(mContext);
        Log.i(TAG, "Getting last time when watch feature data was used",mContext);
        lastWatchDataReadingStopTime = DataManager.getLastWatchDataReadingStopTime(mContext);
        Log.i(TAG, "Getting last time when panobike data was used", mContext);
        lastPanobikeReadingStopTime = DataManager.getLastPanoBikeReadingStopTime(mContext);
//        Log.i(TAG_NOTES,"Last Panobike data used time:" + simpleDateFormat.format(lastPanobikeReadingStopTime),mContext);


        Log.i(TAG,"Last AR stop time:" + simpleDateFormat.format(lastARwindowStopTime),mContext);
//        Log.i(TAG_NOTES,"Last AR stop time:" + simpleDateFormat.format(lastARwindowStopTime),mContext);



        eeCalcLastRun = DataManager.getLastARwindowStopTime(mContext);
        eeKcal = TEMPLEDataManager.getEEKcalBoth(mContext);
        eeKcalPanobike = TEMPLEDataManager.getEEKcalPanobike(mContext);
        eeKcalWatch = TEMPLEDataManager.getEEKcalWatch(mContext);
        Log.i(TAG,"Last recorded Energy Expenditure based on both sensors in kCal = " + eeKcal,mContext);
        Log.i(TAG,"Last recorded Energy Expenditure based only on panobike data in kCal = " + eeKcalPanobike,mContext);
        Log.i(TAG,"Last recorded Energy Expenditure basedn only on watch data in kCal = " + eeKcalWatch,mContext);

        eeKcalBothTot = TEMPLEDataManager.getEEBoth(mContext);
        eeKcalPanobikeTot = TEMPLEDataManager.getEEpano(mContext);
        eeKcalWatchTot = TEMPLEDataManager.getEEwatch(mContext);

        eeKcalTot = Float.toString(Float.valueOf(eeKcalBothTot)+ Float.valueOf(eeKcalPanobikeTot) + Float.valueOf(eeKcalWatchTot));
//        Log.i(TAG_NOTES_SECOND,eeKcalTot,mContext);

//        Log.i(TAG_NOTES,"Last recorded EE based on both sensors = " + eeKcal,mContext);
//        Log.i(TAG_NOTES,"Last recorded EE Expenditure based only on panobike = " + eeKcalPanobike,mContext);
//        Log.i(TAG_NOTES,"Last recorded EE basedn only on watch = " + eeKcalWatch,mContext);


        distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
        if(distanceMeter.startsWith("-")){
            distanceMeter = "0";
        }

        Log.i(TAG,"Last recorded distance travelled in meter = " + distanceMeter,mContext);
//        Log.i(TAG_NOTES,"Last recorded distance(m) = " + distanceMeter,mContext);


        lastARserviceRun = TEMPLEDataManager.getLastRunOfARService(mContext);

        if(lastARserviceRun!=0){
            Date dateEEcalcLastRun = new Date();
            dateEEcalcLastRun.setTime(lastARserviceRun);
            Calendar calEEcalcLastRun = Calendar.getInstance();
            calEEcalcLastRun.setTime(dateEEcalcLastRun);
            int dayOfMonthEEcalcLastRun = calEEcalcLastRun.get(Calendar.DAY_OF_MONTH);
            int monthEEcalcLastRun = calEEcalcLastRun.get(Calendar.MONTH);

            Date dateCurrent = new Date();
            dateCurrent.setTime(DateTime.getCurrentTimeInMillis());
            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(dateCurrent);
            int dayOfMonthCurrent = calCurrent.get(Calendar.DAY_OF_MONTH);
            int monthCurrent = calCurrent.get(Calendar.MONTH);


            Log.i(TAG,"Day of month current:"+ Integer.toString(dayOfMonthCurrent)+",day of month last AR service run:"+ Integer.toString(dayOfMonthEEcalcLastRun),mContext);
//            Log.i(TAG_NOTES,"Day of month current:"+ Integer.toString(dayOfMonthCurrent)+",day of month last AR service run:"+ Integer.toString(dayOfMonthEEcalcLastRun),mContext);


            if((dayOfMonthCurrent>dayOfMonthEEcalcLastRun)||(monthCurrent>monthEEcalcLastRun)){
                TEMPLEDataManager.setEEKcalBoth(mContext,"0");
                TEMPLEDataManager.setEEKcalPanobike(mContext,"0");
                TEMPLEDataManager.setEEKcalWatch(mContext,"0");

                Integer today_goal = TEMPLEDataManager.getPanoPAminutes(mContext)+TEMPLEDataManager.getWatchPAminutes(mContext)+TEMPLEDataManager.getBothPAminutes(mContext);
                if(today_goal>35) {
                    TEMPLEDataManager.setPAMinutesGoal(mContext, today_goal);
                }else{
                    Log.i(TAG,"Not setting goal PA minutes from yesterday's total PA minutes since it equals to "+Integer.toString(today_goal),mContext);
//                    Log.i(TAG_NOTES,"Not setting goal PA minutes from yesterday's total PA minutes since it equals to "+Integer.toString(today_goal),mContext);

                }

                Double totalEEkcal = Double.valueOf(TEMPLEDataManager.getEEpano(mContext))+Double.valueOf(TEMPLEDataManager.getEEwatch(mContext))+Double.valueOf(TEMPLEDataManager.getEEBoth(mContext));
                Integer totalEEkcalInt = (int) Math.round(totalEEkcal);
                SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyy-MM-dd");
                String lastDateEEcalc = yearMonthDay.format(calEEcalcLastRun.getTime());
                Log.i(TAG,"Last EE calculation date:"+lastDateEEcalc+",total EE kcal set to:"+ Integer.toString(totalEEkcalInt),mContext);
//                Log.i(TAG_NOTES,"Last EE calc date:"+lastDateEEcalc+",total EE(kCal) set to:"+ Integer.toString(totalEEkcalInt),mContext);

                TEMPLEDataManager.setTotalEEkcal(mContext,lastDateEEcalc,totalEEkcalInt);

                TEMPLEDataManager.setEEPano(mContext,"0");
                TEMPLEDataManager.setEEboth(mContext,"0");
                TEMPLEDataManager.setEEwatch(mContext,"0");
                Integer lastPAboutLength = TEMPLEDataManager.getDailyPAboutLength(mContext);
                if (lastPAboutLength>2) {
                    TEMPLEDataManager.setDailyPaBoutLengthGoal(mContext,lastPAboutLength);
                    Log.i(TAG,"Set minimum bout length to:"+Integer.toString(lastPAboutLength),mContext);
//                    Log.i(TAG_NOTES,"Set min bout length to:"+Integer.toString(lastPAboutLength),mContext);

                }
            }
        }

//        // get participant related info for computing energy expenditure
//        mapMET = new HashMap<String,Double>();
//        mapMET.put("1",1d);
//        mapMET.put("2",3d);
//        mapMET.put("3",1.6d);
//        mapMET.put("4",3.5d);
//        mapMET.put("5",1d);
//        mapMET.put("6",4.8d);
//        mapMET.put("7",3.2d);
//        mapMET.put("11",1d);
//        mapMET.put("12",3.5d);
//        mapMET.put("13",3.2d);
//
//        mapSciLevel = new HashMap<String,Double>();
//        mapSciLevel.put("paraplagia",2.77d);
//        mapSciLevel.put("tetraplagia",2.52d);
//
//        if(TEMPLEDataManager.getParticipantWeight(mContext)!="" && TEMPLEDataManager.getParticipantSciLevel(mContext)!=""){
//            partWeightKg = (Double.parseDouble(TEMPLEDataManager.getParticipantWeight(mContext))* TEMPLEConstants.LB_KG_CONVERT)/TEMPLEConstants.MET_DIVIDE;
//            Log.i(TAG,"Participant weight in lbs is :"+ TEMPLEDataManager.getParticipantWeight(mContext),mContext);
//
//            partSciLevel = mapSciLevel.get(TEMPLEDataManager.getParticipantSciLevel(mContext));
//            Log.i(TAG,"Participant sci level is :"+ TEMPLEDataManager.getParticipantSciLevel(mContext),mContext);
//
//            partMETmultiply = partWeightKg*partSciLevel;
//        }else{
//            Log.i(TAG,"Patient information to calculate energy expenditure not found",mContext);
//            partMETmultiply = 0d;
//        }
//        METthresh = MULT*partMETmultiply;
//        Log.i(TAG, "MET threshold set to include PA minutes when greeat than "+ Double.toString(METthresh), mContext);


        // this is end for participant related info for computing energy expenditure

        // based on the end time of the last window decide on a new time window
        Log.i(TAG, "Reading speed file for today", mContext);


        useForDistance = TEMPLEDataManager.getDistanceCalculation(mContext);
        if(useForDistance==null){
            useForDistance = "Speed";
            TEMPLEDataManager.setDistanceCalculation(mContext,"Speed");
        }
        Log.i(TAG, "Using reading from " + useForDistance + " to calculate speed", mContext);


        String speedFile = featureDirectory + "/" + dayDirectory + "/" + useForDistance+"Day.csv";
        sFile = new File(speedFile);

        Log.i(TAG, "Reading watch feature file for today", mContext);
        String watchFeatureFile = featureDirectory + "/" + dayDirectory + "/" + "Watch-ComputedFeatureDay.log.csv";
        wfFile = new File(watchFeatureFile);

        arFile = DataManager.getDirectoryData(mContext) + "/" + dayDirectory + "/" + DateTime.getCurrentHourWithTimezone()+ "/" + "ActivityRecognitionResult.log.csv";
        arFileDay = DataManager.getDirectoryFeature(mContext) + "/" + dayDirectory + "/" + "ActivityRecognitionResult.log.csv";

        arFilePanobike = DataManager.getDirectoryData(mContext) + "/" + dayDirectory + "/" + DateTime.getCurrentHourWithTimezone()+ "/" + "ActivityRecognitionResultPanobike.log.csv";
        arFilePanobikeDay = DataManager.getDirectoryFeature(mContext) + "/" + dayDirectory + "/" + "ActivityRecognitionResultPanobike.log.csv";

        arFileWatch = DataManager.getDirectoryData(mContext) + "/" + dayDirectory + "/" + DateTime.getCurrentHourWithTimezone()+ "/" + "ActivityRecognitionResultWatch.log.csv";
        arFileWatchDay = DataManager.getDirectoryFeature(mContext) + "/" + dayDirectory + "/" + "ActivityRecognitionResultWatch.log.csv";
        Log.i(TAG, "Loading all activities model", mContext);
        AssetManager assetManager = mContext.getAssets();
        String allActivitiesModelPath = "all_activities.model";

        try {
            allActivitiesClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(allActivitiesModelPath));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "All activities model loaded", mContext);

        allActivitiesUnpredicted = new Instances("test", allActivitiesAttributeList, 0);
        allActivitiesUnpredicted.setClassIndex(allActivitiesUnpredicted.numAttributes() - 1);


        Log.i(TAG, "Loading moving model", mContext);
        String movingModelPath = "moving.model";
        try {
            movingClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(movingModelPath));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Moving model loaded", mContext);



        movingUnpredicted = new Instances("test", movingAttributeList, 0);
        movingUnpredicted.setClassIndex(movingUnpredicted.numAttributes() - 1);

        Log.i(TAG, "Loading not moving model", mContext);
        String nonMovingModelPath = "non_moving.model";
        try {
            nonMovingClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(nonMovingModelPath));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Non-moving model loaded", mContext);

        nonMovingUnpredicted = new Instances("test", nonmovingAttributeList, 0);
        nonMovingUnpredicted.setClassIndex(nonMovingUnpredicted.numAttributes() - 1);


        if (!wfFile.exists() && !sFile.exists()) {
            Log.i(TAG, "No speed file or watch feature file for today", mContext);
            stopSelf();
        }

        if(wfFile.exists() && !sFile.exists()) {
            Log.i(TAG, "No speed file only watch file present.", mContext);
            doARwatchOnly();
        }

        if (sFile.exists()) {
            Log.i(TAG, "Reading speed file into tree map.", mContext);
            map = new TreeMap<Long, Double>();
            CSVReader reader = null;

                reader = new CSVReader(new FileReader(sFile));
                String[] line;
                boolean first = true;
                while ((line = reader.readNext()) != null) {
                    final String[] lineCp = line;
                    SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date thisDate = simpleDateFormatPano.parse(lineCp[0]);
                    thisMilliseconds = thisDate.getTime();
                    Double thisRot = Double.parseDouble(lineCp[1]);
//                    Log.i(TAG,lineCp[0]+","+lineCp[1],mContext);
                    if (first) {
                        prevDate = thisDate;
                        prevRot = thisRot;
                        map.put(thisDate.getTime(), thisRot);
                        first = false;
                    } else {
                        int rotGap = Double.compare(thisRot,prevRot);
                        if(rotGap==0){
                            Log.i(TAG, "Rotation diff is 0. So not distributing.", mContext);
                        }else if (rotGap<0){
                            Log.i(TAG, "Rotation diff is negative. Panobike reading must have reinitialized. So not distributing.", mContext);
                        }else{
                            long diff = thisDate.getTime() - prevDate.getTime();
                            int timeGapUpper = Long.compare(diff,PANOBIKE_GAP_MINUTE*ONE_MINUTE_IN_MILLIS);
                            int timeGapLower = Long.compare(diff,ONE_MINUTE_IN_MILLIS);

                            if(timeGapUpper>0||timeGapLower<0){
                                Log.i(TAG, "Time gap is great than allowed threshold of 35 minutes, or smaller than 1 minute. So not distributing.", mContext);
                            }else {
                                Log.i(TAG, "Time gap is smaller or equal to allowed threshold of 35 minutes.", mContext);
                                double distanceTravelled = (thisRot - prevRot)*wheelCircumference;

                                double avgSpeed = distanceTravelled/(double) (diff/1000l);
                                int avgSpeedUpper = Double.compare(avgSpeed,107d);
                                int avgSpeedLower = Double.compare(avgSpeed,12d);
                                if(avgSpeedLower<=0||avgSpeedUpper>0){
                                    Log.i(TAG, "Avg. speed in greater than 3749 meters/minute or less than 12 meters/minute. So not distributing.", mContext);
                                }else{
                                    Log.i(TAG, "Re-distributing panobike reading now.", mContext);
                                    long diffSeconds = diff / 1000;
                                    double binsDouble = (double) diffSeconds / (double) 30;
                                    int bins = (int) Math.round(binsDouble);
                                    double rotAdd = (thisRot - prevRot) / (double) bins;
                                    if (bins == 0) {
                                        bins = 1;
                                    }
                                    Log.i(TAG, "Bins needed:"+Integer.toString(bins), mContext);

                                    for (int i = 1; i < bins; i++) {
                                        long j = (long) i;
                                        long time = prevDate.getTime() + (30000L * j);
                                        double k = (double) i;
                                        double dist = prevRot + (rotAdd*k);
                                        map.put(time, dist);
                                        Log.i(TAG, "Adding:"+simpleDateFormatPano.format(time)+","+Double.toString(dist), mContext);
                                    }
                                }
                            }
                        }

                        map.put(thisDate.getTime(), thisRot);
                        prevDate = thisDate;
                        prevRot = thisRot;
                    }
                    lastTimeSpeed = thisMilliseconds;

                }
            SimpleDateFormat simpleDateFormatS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long lk = map.lastKey();
            simpleDateFormatS.format(lk);
            Log.i(TAG, "Last time for speed detected at:"+simpleDateFormatS.format(lk), mContext);
//            Log.i(TAG_NOTES, "Last time for speed detected at:"+simpleDateFormatS.format(lk), mContext);

        }

        long lastSpeedReadTime = TEMPLEDataManager.getSpeedLastReadTime(mContext);
        Log.i(TAG, "Last speed read time:"+simpleDateFormat.format(lastSpeedReadTime), mContext);
//        Log.i(TAG_NOTES, "Last speed read time:"+simpleDateFormat.format(lastSpeedReadTime), mContext);


        if (sFile.exists()&& !wfFile.exists()) {

            Log.i(TAG, "Only speed file exists for today", mContext);
            // need to get last line from the map:DONE
            // based on that divide into intervals of 60 seconds
//            Long startRot = map.ceilingKey(lastARwindowStopTime);
            Long startRot = map.ceilingKey(lastSpeedReadTime);

//            if(Long.signum(lastSpeedReadTime)==-1){
//                startRot = map.firstKey();
//            }else {
//                startRot = map.floorKey(lastSpeedReadTime);
//            }
            Long stopRot = map.lastKey();

            if(startRot==null || stopRot==null) {
                Log.i(TAG, "No new speed recorded after last AR instance.", mContext);
                stopSelf();
            } else if(stopRot.compareTo(startRot) <0){
                    Log.i(TAG, "Rotation number decreased.", mContext);
                    stopSelf();
            }else if (stopRot.compareTo(startRot) ==0){
                Log.i(TAG, "No new rotation recorded after last AR instance.", mContext);
                stopSelf();
            } else{
                long diff = stopRot - startRot;
                long diffSeconds = diff / 1000l;
                double binsDouble = (double) diffSeconds / (double) 60;
                int bins = (int) Math.round(binsDouble);
                if (bins == 0) {
                    bins = 1;
                }
                Log.i(TAG,"number of bins="+Integer.toString(bins),mContext);
                for (int i = 1; i < bins; i++) {
                    eeKcalPanobike = TEMPLEDataManager.getEEKcalPanobike(mContext);
                    long j = (long) i;
                    long jSubOne = j - 1L;
                    long start = startRot + (60000L * jSubOne);
                    long stop = startRot + (60000L * j);

                    String predictClass;
                    String predictSubClass;
                    SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = simpleDateFormatPano.format(start);
                    String stopTime = simpleDateFormatPano.format(stop);
                    Log.i(TAG, "Total distance from panobike between:" + startTime + "," + stopTime , mContext);

                    double totalDistance;
                    if(map.get(stop)==null || map.get(start)==null){
                        totalDistance = 0d;
                    }else {
                        totalDistance = (map.get(stop) - map.get(start)) * wheelCircumference;
                    }

                    Log.i(TAG, "Total distance from panobike between:" + startTime + "," + stopTime + " is " + Double.toString(totalDistance), mContext);
                    if (totalDistance < nearStationary) {
                        predictClass = "11";
                        predictSubClass = "11";
                        participantMETkcal = partMETmultiply * mapMET.get("11");

                    } else if (totalDistance > someWheelMovement) {
                        predictClass = "12";
                        predictSubClass = "12";
                        participantMETkcal = partMETmultiply * mapMET.get("12");
                    } else {
                        predictClass = "13";
                        predictSubClass = "13";
                        participantMETkcal = partMETmultiply * mapMET.get("13");
                    }
                    Log.i(TAG, "Activity detected only using panobike data:" + predictClass, mContext);
                    Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);

//                    Log.i(TAG_NOTES, "Activity detected (panobike):" + predictClass, mContext);

                    TEMPLEDataManager.setEEKcalPanobike(mContext, String.valueOf(Double.valueOf(eeKcalPanobike) + participantMETkcal));
                    String[] row = {
                            startTime,
                            stopTime,
                            predictClass,
                            predictSubClass,
                            String.valueOf(totalDistance),
                            String.valueOf(participantMETkcal)
                    };
                    CSV.write(row, arFilePanobike, true);
                    CSV.write(row, arFilePanobikeDay, true);
                    EEpano.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stop),Calendar.MINUTE),participantMETkcal);
                    DataManager.setLastARwindowStopTime(mContext, stop);
                    TEMPLEDataManager.setSpeedLastReadTime(mContext, stop);

                    if(Arrays.asList(arr).contains(predictSubClass)){
                        Log.i(TAG,"Trigerring intervention service",mContext);
//                        Log.i(TAG_NOTES,"Trigerring intervention service",mContext);

                        startService(new Intent(this, JustInTimeFeedbackService.class));
                    }


                }
            }

        }


        if(sFile.exists() && wfFile.exists()){
            Log.i(TAG, "Both speed file and watch feature file present.", mContext);
            long speedLastReadTime = TEMPLEDataManager.getSpeedLastReadTime(mContext);
            long watchLastReadTime = TEMPLEDataManager.getWatchLastReadTime(mContext);
            SimpleDateFormat simpleDateFormatP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String speedLastTime = simpleDateFormatP.format(speedLastReadTime);
            String watchLastTime = simpleDateFormatP.format(watchLastReadTime);

            Log.i(TAG, "Speed last read time:"+ speedLastTime+ ",watch last read time:"+ watchLastTime, mContext);
//            Log.i(TAG_NOTES, "Speed last read time:"+ speedLastTime+ ",watch last read time:"+ watchLastTime, mContext);


//            long refTime = speedLastReadTime;
//            if(speedLastReadTime > watchLastReadTime) {
                long refTime = speedLastReadTime;
                // read panobike data if speedreadtime is smaller, if not read csv data
                Long startRot = map.ceilingKey(refTime);
                Long stopRot = map.lastKey();
                if(startRot!=null) {
                    String startRotTime = simpleDateFormatP.format(startRot);
                    String stopRotTime = simpleDateFormatP.format(stopRot);
                    Log.i(TAG, "Start rot time:" + startRotTime + ",stop rot time:" + stopRotTime, mContext);

                    if (stopRot.compareTo(startRot) > 0) {
                        long diff = stopRot - startRot;
                        long diffSeconds = diff / 1000l;
                        double binsDouble = (double) diffSeconds / (double) 60;
                        int bins = (int) Math.round(binsDouble);
                        if (bins == 0) {
                            bins = 1;
                        }
                        Log.i(TAG, "number of bins:" + Integer.toString(bins), mContext);

                        String startKeyTime;
                        String stopKeyTime;
                        for (int i = 1; i <= bins; i++) {
                            eeKcal = TEMPLEDataManager.getEEKcalBoth(mContext);
                            eeKcalPanobike = TEMPLEDataManager.getEEKcalPanobike(mContext);
                            eeKcalWatch = TEMPLEDataManager.getEEKcalWatch(mContext);

                            long j = (long) i;
                            long jSubOne = j - 1L;
                            long start = startRot + (60000L * jSubOne);
                            long stop = startRot + (60000L * j);

                            Log.i(TAG, "Start:" + Long.toString(start) + ",stop:" + Long.toString(stop), mContext);

                            String startTimeS = simpleDateFormatP.format(start);
                            String stopTimeS = simpleDateFormatP.format(stop);
                            Log.i(TAG, "Start time:" + startTimeS + ",stop time:" + stopTimeS, mContext);

                            long startKey = map.ceilingKey(start);
                            long stopKey = map.floorKey(stop);
                            double totalDistance;


                            if (startKey >= stopKey) {
                                Log.i(TAG, "Data not present for this window in panobike file", mContext);
                                totalDistance = 0d;
                                startKeyTime = simpleDateFormatP.format(start);
                                stopKeyTime = simpleDateFormatP.format(stop);
                                stopKey = stop;
                            } else {

                                Log.i(TAG, "Start key:" + Long.toString(startKey) + ",stop key:" + Long.toString(stopKey), mContext);
                                startKeyTime = simpleDateFormatP.format(startKey);
                                stopKeyTime = simpleDateFormatP.format(stopKey);
                                Log.i(TAG, "Start key time:" + startKeyTime + ",stop key time:" + stopKeyTime, mContext);

                                Double beginRot = map.get(startKey);
                                Double endRot = map.get(stopKey);

                                if (endRot.compareTo(beginRot) < 0) {
                                    totalDistance = 0d;
                                } else {
                                    totalDistance = (endRot - beginRot) * wheelCircumference;
                                }

                            }
                            CSVReader readerSS = null;
                            String[] gotline = null;

                            try {
                                readerSS = new CSVReader(new FileReader(wfFile));
                                String[] lineSS;
                                while ((lineSS = readerSS.readNext()) != null) {
                                    final String[] lineCp = lineSS;
                                    SimpleDateFormat simpleDateFormatMicro = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");
                                    Date startDate = simpleDateFormatMicro.parse(lineCp[2]);
                                    long startMilliseconds = startDate.getTime();
                                    Date stopDate = simpleDateFormatMicro.parse(lineCp[3]);
                                    long stopMilliseconds = stopDate.getTime();
//                                Log.i(TAG, "Start time:"+ lineCp[2]+ ",stop time:"+ lineCp[3] +" start long="+Long.toString(startMilliseconds)+" stop long="+Long.toString(stopMilliseconds), mContext);
                                    if ((startMilliseconds <= startKey && stopMilliseconds > stopKey) || (startMilliseconds > startKey && stopMilliseconds <= stopKey)) {
                                        Log.i(TAG, "Found our line", mContext);
                                        gotline = lineCp;
                                        stop = stopMilliseconds;
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String predictClass;
                            String predictSubClass;
//                        SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        String startTime = simpleDateFormatPano.format(startKey);
//                        String stopTime = simpleDateFormatPano.format(stopKey);

                            if (gotline == null) {
                                Log.i(TAG, "No watch data found for start key time:" + startKeyTime + ",stop key time:" + stopKeyTime, mContext);
                                if (startKey > speedLastReadTime) {
                                    if (totalDistance < nearStationary) {
                                        predictClass = "11";
                                        predictSubClass = "11";
                                        participantMETkcal = partMETmultiply * mapMET.get("11");

                                    } else if (totalDistance > someWheelMovement) {
                                        predictClass = "12";
                                        predictSubClass = "12";
                                        participantMETkcal = partMETmultiply * mapMET.get("12");
                                    } else {
                                        predictClass = "13";
                                        predictSubClass = "13";
                                        participantMETkcal = partMETmultiply * mapMET.get("13");
                                    }
                                    Log.i(TAG, "Activity detected only using panobike data:" + predictClass, mContext);
                                    Log.i(TAG_NOTES, predictClass, mContext);

                                    Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);
                                    EEpano.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stopKey),Calendar.MINUTE),participantMETkcal);

                                    TEMPLEDataManager.setEEKcalPanobike(mContext, String.valueOf(Double.valueOf(eeKcalPanobike) + participantMETkcal));
                                    String[] row = {
                                            startKeyTime,
                                            stopKeyTime,
                                            predictClass,
                                            predictSubClass,
                                            String.valueOf(totalDistance),
                                            String.valueOf(participantMETkcal)
                                    };
                                    CSV.write(row, arFilePanobike, true);
                                    CSV.write(row, arFilePanobikeDay, true);
                                    DataManager.setLastARwindowStopTime(mContext, stopKey);
                                    TEMPLEDataManager.setSpeedLastReadTime(mContext, stopKey);
                                    if (Arrays.asList(arr).contains(predictSubClass)) {
                                        Log.i(TAG,"trigerring intervention service",mContext);
//                                        Log.i(TAG_NOTES,"trigerring intervention service",mContext);

                                        startService(new Intent(this, JustInTimeFeedbackService.class));
                                    }
                                }
                            } else {
                                if (totalDistance < nearStationary) {
                                    doNonMovingInstance(gotline, stop, totalDistance, eeKcal, "pano");
                                } else if (totalDistance > someWheelMovement) {
                                    doMovingInstance(gotline, stop, totalDistance, eeKcal, "pano");
                                } else {
                                    Log.i(TAG, "Activity detected using panobike data and watch:13", mContext);
                                    Log.i(TAG_NOTES, "13", mContext);

                                    participantMETkcal = partMETmultiply * mapMET.get("13");
                                    Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);
                                    TEMPLEDataManager.setEEKcalBoth(mContext, String.valueOf(Double.valueOf(eeKcal) + participantMETkcal));
                                    String[] row = {
                                            startKeyTime,
                                            stopKeyTime,
                                            "13",
                                            "13",
                                            String.valueOf(totalDistance),
                                            String.valueOf(participantMETkcal),
                                    };
                                    CSV.write(row, arFile, true);
                                    CSV.write(row, arFileDay, true);
                                    EEboth.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stop),Calendar.MINUTE),participantMETkcal);
                                    DataManager.setLastARwindowStopTime(mContext, stop);

//                                    if (Arrays.asList(arr).contains("13")) {
//                                        Log.i(TAG,"Trigerring intervention service",mContext);
//                                        startService(new Intent(this, JustInTimeFeedbackService.class));
//                                    }
                                }
                                DataManager.setLastARwindowStopTime(mContext, stop);
                                TEMPLEDataManager.setWatchLastReadTime(mContext, stop);
                                TEMPLEDataManager.setSpeedLastReadTime(mContext, stop);

                            }

                        }
                    }
                }
//            }

//            }else {
                // read csv
            long speedLastReadTimeS = TEMPLEDataManager.getSpeedLastReadTime(mContext);
            long watchLastReadTimeS = TEMPLEDataManager.getWatchLastReadTime(mContext);
            String speedLastTimeS = simpleDateFormatP.format(speedLastReadTimeS);
            String watchLastTimeS = simpleDateFormatP.format(watchLastReadTimeS);

            Log.i(TAG, "Speed last read time switch:"+ speedLastTimeS + ",watch last read time switch :"+ watchLastTimeS, mContext);
//            Log.i(TAG_NOTES, "Speed last read time switch:"+ speedLastTimeS + ",watch last read time switch :"+ watchLastTimeS, mContext);



//            long refTimeS = watchLastReadTimeS;
                CSVReader readerS = null;
                try {
                    Log.i(TAG, "Reading watch feature file.", mContext);
                    readerS = new CSVReader(new FileReader(wfFile));
                    Log.i(TAG, "Finished reading watch feature file.", mContext);

                    String[] lineS;
                    while ((lineS = readerS.readNext()) != null) {
                        final String[] lineCp = lineS;
                        SimpleDateFormat simpleDateFormatMicro = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");

                        Date startDate = simpleDateFormatMicro.parse(lineCp[2]);
                        long startMilliseconds = startDate.getTime();

                        Date stopDate = simpleDateFormatMicro.parse(lineCp[3]);
                        long stopMilliseconds = stopDate.getTime();

                        if (startMilliseconds > stopMilliseconds) {
                            Log.i(TAG, "Caught an erroneous feature vector: start time greater than end time. Skipping...", mContext);
                            continue;
                        } else if (stopMilliseconds > currentMilliseconds) {
                            Log.i(TAG, "Caught an erroneous feature vector: start time greater than end time. Skipping...", mContext);
                            continue;
                        } else {
//                            Log.i(TAG, "Current line start:"+simpleDateFormatMicro.format(startMilliseconds)+",AR start from:"+ simpleDateFormatMicro.format(watchLastReadTimeS), mContext);
                            if (startMilliseconds > watchLastReadTimeS) {
                                Log.i(TAG, lineCp[2] + "," + lineCp[3], mContext);
                                // convert speed csv to dictionary
                                // check if anything in speed data between startmilli and stopmilli
//                                String eeKcalTmp = TEMPLEDataManager.getEEKcalBoth(mContext);
                                eeKcal = TEMPLEDataManager.getEEKcalBoth(mContext);
                                eeKcalPanobike = TEMPLEDataManager.getEEKcalPanobike(mContext);
                                eeKcalWatch = TEMPLEDataManager.getEEKcalWatch(mContext);

                                Long startKey = map.ceilingKey(startMilliseconds);
                                Long stopKey = map.floorKey(stopMilliseconds);
                                if (startKey != null && stopKey != null) {
                                    Double startRotS = map.get(startKey);
                                    Double stopRotS = map.get(stopKey);

                                    double totalDistance;
                                    if (stopRotS.compareTo(startRotS) < 0) {
                                        totalDistance = 0f;
                                    } else {
                                        totalDistance = (stopRotS - startRotS) * wheelCircumference;
                                    }
                                    Log.i(TAG, "Total distance from panobike between:" + lineCp[2] + "," + lineCp[3] + " is " + Double.toString(startRotS) + "," + Double.toString(stopRotS) + "=" + Double.toString(totalDistance) + " coz " + Double.toString(wheelCircumference), mContext);
                                    if (totalDistance < nearStationary) {
                                        doNonMovingInstance(lineS, stopMilliseconds, totalDistance, eeKcal,"pano");
                                    } else if (totalDistance > someWheelMovement) {
                                        doMovingInstance(lineS, stopMilliseconds, totalDistance, eeKcal,"pano");
                                    } else {
                                        Log.i(TAG, "Activity detected using panobike and watch:13", mContext);
                                        Log.i(TAG, "Activity detected (panobike+watch):13", mContext);

                                        participantMETkcal = partMETmultiply * mapMET.get("13");
                                        Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);
                                        TEMPLEDataManager.setEEKcalBoth(mContext, String.valueOf(Double.valueOf(eeKcal) + participantMETkcal));
                                        String[] row = {
                                                dateFormatToConvert.format(startDate),
                                                dateFormatToConvert.format(stopDate),
                                                "13",
                                                "13",
                                                String.valueOf(totalDistance),
                                                String.valueOf(participantMETkcal),
                                        };
                                        CSV.write(row, arFile, true);
                                        CSV.write(row, arFileDay, true);
                                        EEboth.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stopMilliseconds),Calendar.MINUTE),participantMETkcal);

                                        DataManager.setLastARwindowStopTime(mContext, stopMilliseconds);
//                                        if(Arrays.asList(arr).contains("13")){
//                                            Log.i(TAG,"Trigerring intervention service",mContext);
//                                            startService(new Intent(this, JustInTimeFeedbackService.class));
//                                        }

//                                    TEMPLEDataManager.setSpeedLastReadTime(mContext,stopMilliseconds);
//                                    TEMPLEDataManager.setWatchLastReadTime(mContext,stopMilliseconds);

                                    }
                                    DataManager.setLastARwindowStopTime(mContext, stopMilliseconds);
                                    TEMPLEDataManager.setSpeedLastReadTime(mContext, stopMilliseconds);
                                    TEMPLEDataManager.setWatchLastReadTime(mContext, stopMilliseconds);

                                } else {
                                        doARwatchOnlyInstance(lineS, stopMilliseconds, eeKcalWatch);

                                }

                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            }

        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.i(TAG, "Inside onDestroy", mContext);
        if(EEboth!=null && EEwatch!=null){
            if(EEboth.size()>0 && EEwatch.size()>0){
                Set<Date> s = new HashSet<Date>(EEwatch.keySet());
                s.retainAll(EEboth.keySet());
                EEwatch.keySet().removeAll(s);
            }
        }

        if(EEboth!=null && EEpano!=null){
            if(EEboth.size()>0 && EEpano.size()>0){
                Set<Date> s = new HashSet<Date>(EEpano.keySet());
                s.retainAll(EEboth.keySet());
                EEpano.keySet().removeAll(s);
            }
        }

        NavigableMap<Date, Integer> EEwatchFilt = new TreeMap<Date, Integer>();
        NavigableMap<Date, Integer> EEbothFilt = new TreeMap<Date, Integer>();
        NavigableMap<Date, Integer> EEpanoFilt = new TreeMap<Date, Integer>();

        if(EEwatch!=null) {
            Log.i(TAG,"Size of EE watch:"+Integer.toString(EEwatch.size()),mContext);
            double sumWatch = 0;
            for (double f : EEwatch.values()) {
                sumWatch += f;
            }
            TEMPLEDataManager.setEEwatch(mContext,Double.toString(sumWatch));

            Integer size = 0;
            if(EEwatch.size()>0) {
                for (Map.Entry<Date, Double> entry : EEwatch.entrySet()) {
                    if (entry.getValue() >= METthresh) {
                        EEwatchFilt.put(entry.getKey(), 1);
                        size += 1;
                    } else {
                        EEwatchFilt.put(entry.getKey(), 0);
                    }
                }
            }
            if(EEwatchFilt!=null){
                Log.i(TAG,"Size of ee watch PA minutes:"+Integer.toString(size),mContext);
//                Log.i(TAG_NOTES,"Size of EE(watch) PA minutes:"+Integer.toString(size),mContext);

                TEMPLEDataManager.setWatchPAMinutes(mContext,size);
            }

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(eeWatchFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.writeObject(EEwatch);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(EEboth!=null) {
            Log.i(TAG,"Size of ee both:"+Integer.toString(EEboth.size()),mContext);
            double sumBoth = 0;
            for (double f : EEboth.values()) {
                sumBoth += f;
            }
            TEMPLEDataManager.setEEboth(mContext,Double.toString(sumBoth));
            Integer size = 0;
            if(EEboth.size()>0) {
                for (Map.Entry<Date, Double> entry : EEboth.entrySet()) {
                    if (entry.getValue() >= METthresh) {
                        EEbothFilt.put(entry.getKey(), 1);
                        size += 1;
                    } else {
                        EEbothFilt.put(entry.getKey(), 0);
                    }
                }
            }
            if(EEbothFilt!=null){
                Log.i(TAG,"Size of ee both PA minutes:"+Integer.toString(size),mContext);
//                Log.i(TAG_NOTES,"Size of EE(both) PA minutes:"+Integer.toString(size),mContext);

                TEMPLEDataManager.setBothPAMinutes(mContext,size);
            }

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(eeBothFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.writeObject(EEboth);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(EEpano!=null) {
            Log.i(TAG,"Size of ee pano:"+Integer.toString(EEpano.size()),mContext);
            double sumPano = 0;
            for (double f : EEpano.values()) {
                sumPano += f;
            }
            TEMPLEDataManager.setEEPano(mContext,Double.toString(sumPano));
            Integer size = 0;
            if(EEpano.size()>0) {
                for (Map.Entry<Date, Double> entry : EEpano.entrySet()) {
                    Log.i(TAG, "EE pano value:" + Double.toString(entry.getValue()), mContext);
                    if (entry.getValue() >= METthresh) {
                        EEpanoFilt.put(entry.getKey(), 1);
                        size += 1;
                    } else {
                        EEpanoFilt.put(entry.getKey(), 0);

                    }
                }
            }

            if(EEpanoFilt!=null){
                Log.i(TAG,"Size of ee pano PA minutes:"+Integer.toString(size),mContext);
//                Log.i(TAG_NOTES,"Size of EE(pano) PA minutes:"+Integer.toString(size),mContext);

                TEMPLEDataManager.setPanoPAMinutes(mContext,size);
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(eePanoFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.writeObject(EEpano);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        NavigableMap<Date, Integer> EEFiltMerged = new TreeMap<Date, Integer>();
        EEFiltMerged.putAll(EEbothFilt);
        EEFiltMerged.putAll(EEpanoFilt);
        EEFiltMerged.putAll(EEwatchFilt);

        Boolean firstEntry = true;
        Integer sum =0;
        ArrayList<Integer> bouts = new ArrayList<Integer>();
        Integer medianBout;
        if(EEFiltMerged.size()>1) {
            Log.i(TAG,"EE filt merge size:"+Integer.toString(EEFiltMerged.size()),mContext);
//            Log.i(TAG_NOTES,"EE filt merge size:"+Integer.toString(EEFiltMerged.size()),mContext);

            for (Map.Entry<Date, Integer> entry : EEFiltMerged.entrySet()) {
                if (firstEntry == true) {
                    if (entry.getValue() == 1) {
                        sum += 1;
                    }
                    firstEntry = false;
                } else {
                    if (entry.getValue() == 1) {
                        sum += 1;
                    } else {
                        if (sum != 0) {
                            bouts.add(sum);
                            sum = 0;
                        }
                    }
                }
            }
            if(bouts.size()>0) {
                Log.i(TAG,"bout lis size:"+Integer.toString(bouts.size()),mContext);
//                Integer sumAll = 0;
//                for (Integer bout : bouts) {
//                    sumAll += bout;
//                }
                int n = bouts.size();
                if (n%2 == 0){
                    medianBout = ( bouts.get(n/2) + bouts.get((n/2)-1)) / 2;
                }else{
                    medianBout = bouts.get((n-1)/2);
                }
//                avgBout = sumAll/bouts.size();


                if(medianBout>0){
                    Log.i(TAG,"Median bout set:"+Integer.toString(medianBout),mContext);
//                    Log.i(TAG_NOTES,"Median bout set:"+Integer.toString(medianBout),mContext);

                    TEMPLEDataManager.setDailyPaBoutLength(mContext,medianBout);
                }
            }
        }
        TEMPLEDataManager.setLastRunOfARService(mContext);
    }

    public void doNonMovingInstance(String[] line, long stop, double dist, String eeKCalIn, String sensor){
        final String[] lineCp = line;
        Log.i(TAG,lineCp[11]+","+lineCp[14]+","+lineCp[15], mContext);
        DenseInstance newInstanceNM = new DenseInstance(nonMovingUnpredicted.numAttributes()) {
            {
                setValue(attribute_mad_median_XYZ_nm, Double.parseDouble(lineCp[11]));
                setValue(attribute_mad_median_Z, Double.parseDouble(lineCp[14]));
                setValue(attribute_mcr_X, Double.parseDouble(lineCp[15]));
            }
        };
        newInstanceNM.setDataset(nonMovingUnpredicted);
//        double[] tmp = newInstanceNM.toDoubleArray();
//        Log.i(TAG,String.valueOf(tmp.length),mContext);
//        Log.i(TAG,String.valueOf(tmp[0])+","+String.valueOf(tmp[1])+","+String.valueOf(tmp[2]),mContext);
        try {
            double result = nonMovingClassifier.classifyInstance(newInstanceNM);
            Log.i(TAG,String.valueOf(result),mContext);
            String className = nonmovingclasses.get(new Double(result).intValue());
            participantMETkcal = partMETmultiply*mapMET.get(className);
            Log.i(TAG, "Energy expenditure in kCal="+eeKCalIn, mContext);
            Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);
            Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal), mContext);
//            TEMPLEDataManager.setEEKcal(mContext,String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal));

            String[] row = {
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[2])),
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[3])),
                    "11",
                    className,
//                    String.valueOf(Float.valueOf(distanceMeter)+dist),
                    String.valueOf(dist),
                    String.valueOf(participantMETkcal),
            };
//            if(level.equals("lower")) {
                if (sensor.equals("pano")) {
                    CSV.write(row, arFile, true);
                    CSV.write(row, arFileDay, true);
                    TEMPLEDataManager.setEEKcalBoth(mContext, String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal));
                    EEboth.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stop),Calendar.MINUTE),participantMETkcal);
                    Log.i(TAG, "Activity detected using panobike and watch:"+className, mContext);
                    Log.i(TAG_NOTES, className, mContext);


                } else {
                    CSV.write(row, arFileWatch, true);
                    CSV.write(row, arFileWatchDay, true);
                    TEMPLEDataManager.setEEKcalWatch(mContext, String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal));
                    EEwatch.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stop),Calendar.MINUTE),participantMETkcal);
                    Log.i(TAG, "Non-moving activity detected using only watch:"+className, mContext);
                    Log.i(TAG_NOTES, className, mContext);


                }
                DataManager.setLastARwindowStopTime(mContext, stop);
                if (Arrays.asList(arr).contains(className)) {
                    Log.i(TAG, "Trigerring intervention service", mContext);
//                    Log.i(TAG_NOTES, "Trigerring intervention service", mContext);

                    startService(new Intent(this, JustInTimeFeedbackService.class));
                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doMovingInstance(String[] line, long stop, double dist, String eeKcalIn, String sensor){
        final String[] lineCpd = line;
        Log.i(TAG,lineCpd[10]+","+lineCpd[11]+","+lineCpd[12]+","+lineCpd[13], mContext);
        DenseInstance newInstanceM = new DenseInstance(movingUnpredicted.numAttributes()) {
            {
                setValue(attribute_Var_Y, Double.parseDouble(lineCpd[10]));
                setValue(attribute_mad_med_XYZ, Double.parseDouble(lineCpd[11]));
                setValue(attribute_VAR_X, Double.parseDouble(lineCpd[12]));
                setValue(attribute_rms_Z, Double.parseDouble(lineCpd[13]));

            }
        };
        newInstanceM.setDataset(movingUnpredicted);

        Log.i(TAG,String.valueOf(newInstanceM.value(0))+","+String.valueOf(newInstanceM.value(1))+","+String.valueOf(newInstanceM.value(2))+","+String.valueOf(newInstanceM.value(3)),mContext);

        double[] tmp = newInstanceM.toDoubleArray();
        Log.i(TAG,String.valueOf(tmp.length),mContext);
        Log.i(TAG,String.valueOf(tmp[0])+","+String.valueOf(tmp[1])+","+String.valueOf(tmp[2])+","+String.valueOf(tmp[3]),mContext);
        try {
            double result = movingClassifier.classifyInstance(newInstanceM);
            Log.i(TAG,String.valueOf(result),mContext);
            String className = movingclasses.get(new Double(result).intValue());
            participantMETkcal = partMETmultiply*mapMET.get(className);
            Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);
//            TEMPLEDataManager.setEEKcal(mContext,String.valueOf(Double.valueOf(eeKcalIn) + participantMETkcal));
            distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);

            String[] row = {
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCpd[2])),
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCpd[3])),
                    "12",
                    className,
                    String.valueOf(dist),
                    String.valueOf(participantMETkcal)
            };
                if (sensor.equals("pano")) {
                    CSV.write(row, arFile, true);
                    CSV.write(row, arFileDay, true);
                    TEMPLEDataManager.setEEKcalBoth(mContext, String.valueOf(Double.valueOf(eeKcalIn) + participantMETkcal));
                    EEboth.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stop),Calendar.MINUTE),participantMETkcal);
                    Log.i(TAG, "Activity detected using both panobike and watch:"+className, mContext);
                    Log.i(TAG_NOTES, className, mContext);


                } else {
                    CSV.write(row, arFileWatch, true);
                    CSV.write(row, arFileWatchDay, true);
                    TEMPLEDataManager.setEEKcalWatch(mContext, String.valueOf(Double.valueOf(eeKcalIn) + participantMETkcal));
                    EEwatch.put(org.apache.commons.lang3.time.DateUtils.round(new Date(stop),Calendar.MINUTE),participantMETkcal);
                    Log.i(TAG, "Moving activity detected using only watch:"+className, mContext);
                    Log.i(TAG_NOTES,className, mContext);


                }
                DataManager.setLastARwindowStopTime(mContext, stop);
                if (Arrays.asList(arr).contains(className)) {
                    Log.i(TAG, "Trigerring intervention service", mContext);
//                    Log.i(TAG_NOTES, "Trigerring intervention service", mContext);

                    startService(new Intent(this, JustInTimeFeedbackService.class));
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
//        DataManager.setLastARwindowStopTime(mContext,stop);
//        TEMPLEDataManager.setSpeedLastReadTime(mContext,stop);
//        TEMPLEDataManager.setWatchLastReadTime(mContext,stop);
    }

    public void doARwatchOnlyInstance(String[] line, long stop, String eeKcalTm){
        final String[] lineCp = line;
        DenseInstance newInstance = new DenseInstance(allActivitiesUnpredicted.numAttributes()) {
            {
                setValue(attribute_zcr_Z, Double.parseDouble(lineCp[5]));
                setValue(attribute_mcr_XYZ, Double.parseDouble(lineCp[6]));
                setValue(attribute_mad_median_X, Double.parseDouble(lineCp[7]));
                setValue(attribute_rms_Y, Double.parseDouble(lineCp[8]));
                setValue(attribute_mad_XYZ, Double.parseDouble(lineCp[9]));
            }
        };
        newInstance.setDataset(allActivitiesUnpredicted);
        try {
            double result = allActivitiesClassifier.classifyInstance(newInstance);
          String className = allActivitiesclasses.get(new Double(result).intValue());
            if(className.equals("11")){
                doNonMovingInstance(lineCp,stop,0d,eeKcalTm,"watch");
            }else if(className.equals("12")){
                doMovingInstance(lineCp,stop,0d,eeKcalTm,"watch");
            }else if(className.equals("13")) {
                participantMETkcal = partMETmultiply*mapMET.get(className);
                Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);
                TEMPLEDataManager.setEEKcalWatch(mContext,String.valueOf(Double.valueOf(eeKcalTm) + participantMETkcal));
                String[] row = {
                        dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[2])),
                        dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[3])),
                        "13",
                        "13",
                        "0",
                        String.valueOf(participantMETkcal),
                };
                CSV.write(row, arFileWatch, true);
                CSV.write(row,arFileWatchDay,true);
                EEwatch.put(new Date(stop),participantMETkcal);
                DataManager.setLastARwindowStopTime(mContext,stop);
                Log.i(TAG, "Activity detected only using watch:"+className, mContext);
                Log.i(TAG_NOTES, className, mContext);

//                Log.i(TAG,"Trigerring intervention service",mContext);
//                startService(new Intent(this, JustInTimeFeedbackService.class));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DataManager.setLastARwindowStopTime(mContext,stop);
        TEMPLEDataManager.setWatchLastReadTime(mContext,stop);

    }

    public void doARwatchOnly(){
        Log.i(TAG, "No speed file, only watch feature file.", mContext);
        // read csv
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(wfFile));
            String[] line;
            while ((line = reader.readNext()) != null) {
                final String[] lineS = line;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");

                Date startDate = simpleDateFormat.parse(lineS[2]);
                long startMilliseconds = startDate.getTime();

                Date stopDate = simpleDateFormat.parse(lineS[3]);
                long stopMilliseconds = stopDate.getTime();

                if(startMilliseconds > stopMilliseconds){
                    Log.i(TAG, "Caught an erroneous feature vector: start time greater than end time. Skipping...", mContext);
                    continue;
                }else if(stopMilliseconds>currentMilliseconds){
                    Log.i(TAG, "Caught an erroneous feature vector: start time greater than end time. Skipping...", mContext);
                    continue;
                }else {
                    long watchLastReadTime = TEMPLEDataManager.getWatchLastReadTime(mContext);
                    if (startMilliseconds > watchLastReadTime) {
                        Log.i(TAG, lineS[2] + "," + lineS[3], mContext);
                        Log.i(TAG, line[5] + "," + line[6] + "," + line[7] + "," + line[8] + "," + line[9], mContext);
                        String eeKcalT = TEMPLEDataManager.getEEKcalWatch(mContext);
                        Log.i(TAG, "Retrieved last energy expenditure=" + eeKcalT, mContext);
                        DenseInstance newInstance = new DenseInstance(allActivitiesUnpredicted.numAttributes()) {
                            {
                                setValue(attribute_zcr_Z, Double.parseDouble(lineS[5]));
                                setValue(attribute_mcr_XYZ, Double.parseDouble(lineS[6]));
                                setValue(attribute_mad_median_X, Double.parseDouble(lineS[7]));
                                setValue(attribute_rms_Y, Double.parseDouble(lineS[8]));
                                setValue(attribute_mad_XYZ, Double.parseDouble(lineS[9]));

                            }
                        };
                        newInstance.setDataset(allActivitiesUnpredicted);
                        try {
                            double result = allActivitiesClassifier.classifyInstance(newInstance);
                            String className = allActivitiesclasses.get(new Double(result).intValue());
                            Log.i(TAG, "Activity detected:" + className, mContext);

                            if (className.equals("11")) {
                                doNonMovingInstance(lineS, stopMilliseconds, 0d, eeKcalT,"watch");
                            } else if (className.equals("12")) {
                                doMovingInstance(lineS, stopMilliseconds, 0d, eeKcalT,"watch");
                            } else if (className.equals("13")) {
                                participantMETkcal = partMETmultiply * mapMET.get(className);
                                Log.i(TAG_NOTES, "13", mContext);

                                Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);
                                TEMPLEDataManager.setEEKcalWatch(mContext, String.valueOf(Double.valueOf(eeKcalT) + participantMETkcal));
                                String[] row = {
                                        dateFormatToConvert.format(startDate),
                                        dateFormatToConvert.format(stopDate),
                                        "13",
                                        "13",
                                        "0",
                                        String.valueOf(participantMETkcal),
                                };

                                CSV.write(row, arFileWatch, true);
                                CSV.write(row, arFileWatchDay, true);
                                EEwatch.put(new Date(stopMilliseconds),participantMETkcal);
                                DataManager.setLastARwindowStopTime(mContext, stopMilliseconds);
//                                Log.i(TAG,"Trigerring intervention service",mContext);
//                                startService(new Intent(this, JustInTimeFeedbackService.class));


                            }
                            DataManager.setLastARwindowStopTime(mContext, stopMilliseconds);
                            TEMPLEDataManager.setWatchLastReadTime(mContext,stopMilliseconds);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


}
