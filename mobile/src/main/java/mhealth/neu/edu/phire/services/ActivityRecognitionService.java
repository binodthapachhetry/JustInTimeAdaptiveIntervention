package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.content.res.AssetManager;
import android.provider.ContactsContract;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
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
import weka.estimators.Estimator;

import edu.neu.mhealth.android.wockets.library.support.DateTime;


public class ActivityRecognitionService extends WocketsIntentService {

    private static final String TAG = "ActivityRecognitionService";
    private static final String TAGF = "ActivityRecognitionResult";
    public static final String dayFormat = "yyyy-MM-dd";
    public static final String hourFormat = "HH-z";
    private Context mContext;
    private Date dateNow;
    private BluetoothAdapter mBluetoothAdapter;
    public Classifier allActivitiesClassifier;
    public Classifier movingClassifier;
    public Classifier nonMovingClassifier;

    private NavigableMap<Long, Integer> map;
    private Long lastTimeSpeed;
    private long thisMilliseconds;
    private long currentMilliseconds;
    private File sFile;
    private File wfFile;
    private String arFile;
    private String arFileDay;
    private long lastARwindowStopTime;

    private Double partWeightKg;
    private String sciLevel;
    private Double partSciLevel;
    private Double partMETmultiply;
    private Double participantMETkcal;
    private HashMap<String,Double> mapMET;
    private HashMap<String,Double> mapSciLevel;

    public Instances allActivitiesUnpredicted;
    public Instances movingUnpredicted;
    public Instances nonMovingUnpredicted;


    private static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
    private float wheelCircumference;
    private static final float nearStationary = 3f;
    private static final float someWheelMovement = 12f;

    private long eeCalcLastRun;
    private String eeKcal;
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
            wheelCircumference = ((Integer.valueOf(TEMPLEDataManager.getWheelDiameterCm(mContext)) * ((float) Math.PI))) * 0.0254f;
        }else{
            wheelCircumference = 0.0f;
        }
        Log.i(TAG, "Wheel circumference:" + Float.toString(wheelCircumference), mContext);

        // get current time in milliseconds
        currentMilliseconds = System.currentTimeMillis();


        Log.i(TAG, "Getting last run of activity recognition service", mContext);
        lastARwindowStopTime = DataManager.getLastARwindowStopTime(mContext);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG,"Last AR stop time:" + simpleDateFormat.format(lastARwindowStopTime),mContext);

        eeCalcLastRun = DataManager.getLastARwindowStopTime(mContext);
        eeKcal = TEMPLEDataManager.getEEKcal(mContext);
        Log.i(TAG,"Last recorded Energy Expenditure in kCal = " + eeKcal,mContext);

        distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
        if(distanceMeter.startsWith("-")){
            distanceMeter = "0";
        }

        Log.i(TAG,"Last recorded distance travelled in meter = " + distanceMeter,mContext);

        if(eeCalcLastRun!=0){
            Date dateEEcalcLastRun = new Date();
            dateEEcalcLastRun.setTime(eeCalcLastRun);
            Calendar calEEcalcLastRun = Calendar.getInstance();
            calEEcalcLastRun.setTime(dateEEcalcLastRun);
            int dayOfMonthEEcalcLastRun = calEEcalcLastRun.get(Calendar.DAY_OF_MONTH);

            Date dateCurrent = new Date();
            dateCurrent.setTime(DateTime.getCurrentTimeInMillis());
            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(dateCurrent);
            int dayOfMonthCurrent = calCurrent.get(Calendar.DAY_OF_MONTH);

            if(dayOfMonthCurrent>dayOfMonthEEcalcLastRun){
                TEMPLEDataManager.setEEKcal(mContext,"0");
                TEMPLEDataManager.setDistanceTravelledMeter(mContext,"0");
            }
        }

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

        // this is end for participant related info for computing energy expenditure

        dateNow = new Date();
        // based on the end time of the last window decide on a new time window
        Log.i(TAG, "Reading speed file for today", mContext);
        String featureDirectory = DataManager.getDirectoryFeature(mContext);
        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);

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
            map = new TreeMap<Long, Integer>();
            CSVReader reader = null;
            try {
                reader = new CSVReader(new FileReader(sFile));
                String[] line;
                while ((line = reader.readNext()) != null) {
                    final String[] lineCp = line;
                    SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date thisDate = simpleDateFormatPano.parse(lineCp[0]);
                    thisMilliseconds = thisDate.getTime();
                    Integer thisRot = Integer.parseInt(lineCp[1]);
                    map.put(thisDate.getTime(), thisRot);
                }
                lastTimeSpeed = thisMilliseconds;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (sFile.exists()&& !wfFile.exists()) {

            Log.i(TAG, "Only speed file exists for today", mContext);
            // need to get last line from the map:DONE
            // based on that divide into intervals of 60 seconds
            Long startRot = map.ceilingKey(lastARwindowStopTime);
            Long stopRot = map.ceilingKey(lastARwindowStopTime+ONE_MINUTE_IN_MILLIS);
//            int diff = Math.round((lastTimeSpeed - startRot) / ONE_MINUTE_IN_MILLIS);
            if(startRot==null || stopRot==null) {
                Log.i(TAG, "No new speed recorded after last AR instance.", mContext);
                stopSelf();
            } else if(stopRot.compareTo(startRot) <0){
                    Log.i(TAG, "No new speed recorded after last AR instance.", mContext);
                    stopSelf();
            }else{
                String predictClass;
                String predictSubClass;
                SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String startTime = simpleDateFormatPano.format(startRot);
                String stopTime = simpleDateFormatPano.format(stopRot);

                float totalDistance = (map.get(stopRot)-map.get(startRot))*wheelCircumference;
                Log.i(TAG,"Total distance from panobike between:" + startTime+","+stopTime+" is "+ Float.toString(totalDistance),mContext);
                if(totalDistance<nearStationary){
                    predictClass = "11";
                    predictSubClass = "11";
                    participantMETkcal = partMETmultiply*mapMET.get("11");

                }else if(totalDistance>someWheelMovement){
                    predictClass = "12";
                    predictSubClass = "12";
                    participantMETkcal = partMETmultiply*mapMET.get("12");
                }else{
                    predictClass = "13";
                    predictSubClass = "13";
                    participantMETkcal = partMETmultiply*mapMET.get("13");
                }
                Log.i(TAG, "Activity detected:"+predictClass, mContext);
                Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);

                TEMPLEDataManager.setEEKcal(mContext,String.valueOf(Double.valueOf(eeKcal) + participantMETkcal));
                TEMPLEDataManager.setDistanceTravelledMeter(mContext,String.valueOf(Float.valueOf(distanceMeter)+totalDistance));

//                TEMPLEDataManager.setEECalculationLastRun(mContext);

                String[] row = {
                        startTime,
                        stopTime,
                        predictClass,
                        predictSubClass,
                        String.valueOf(Float.valueOf(distanceMeter)+totalDistance),
                        String.valueOf(Double.valueOf(eeKcal) + participantMETkcal)
                };
                CSV.write(row, arFile, true);
                CSV.write(row,arFileDay,true);
                DataManager.setLastARwindowStopTime(mContext,stopRot);

            }

        }


        if(sFile.exists() && wfFile.exists()){
            Log.i(TAG, "Both speed file and watch feature file present.", mContext);
            // read csv
            CSVReader reader = null;
            try {
                Log.i(TAG, "Reading watch feature file.", mContext);
                reader = new CSVReader(new FileReader(wfFile));
                Log.i(TAG, "Finished reading watch feature file.", mContext);

                String[] line;
                while ((line = reader.readNext()) != null) {
                    final String[] lineCp = line;
                    SimpleDateFormat simpleDateFormatMicro = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");

                    Date startDate = simpleDateFormatMicro.parse(lineCp[2]);
                    long startMilliseconds = startDate.getTime();

                    Date stopDate = simpleDateFormatMicro.parse(lineCp[3]);
                    long stopMilliseconds = stopDate.getTime();

                    if(startMilliseconds > stopMilliseconds){
                        Log.i(TAG, "Caught an erroneous feature vector: start time greater than end time. Skipping...", mContext);
                        continue;
                    }else if(stopMilliseconds>currentMilliseconds){
                        Log.i(TAG, "Caught an erroneous feature vector: start time greater than end time. Skipping...", mContext);
                        continue;
                    }
                    else {

                        if (startMilliseconds > lastARwindowStopTime) {
                            Log.i(TAG, lineCp[2] + "," + lineCp[3], mContext);
                            // convert speed csv to dictionary
                            // check if anything in speed data between startmilli and stopmilli
                            String eeKcalTmp = TEMPLEDataManager.getEEKcal(mContext);
                            Long startKey = map.ceilingKey(startMilliseconds);
                            Long stopKey = map.floorKey(stopMilliseconds);
                            if (startKey != null && stopKey != null) {
                                Integer startRot = map.get(startKey);
                                Integer stopRot = map.get(stopKey);

                                float totalDistance;
                                if (stopRot.compareTo(startRot) < 0) {
                                    totalDistance = 0f;
                                } else {
                                    totalDistance = (stopRot - startRot) * wheelCircumference;
                                }
                                Log.i(TAG, "Total distance from panobike between:" + lineCp[2] + "," + lineCp[3] + " is " + Integer.toString(startRot) + "," + Integer.toString(stopRot) + "=" + Float.toString(totalDistance) + " coz " + Float.toString(wheelCircumference), mContext);
                                if (totalDistance < nearStationary) {
                                    doNonMovingInstance(line, stopMilliseconds, totalDistance, eeKcalTmp);
                                } else if (totalDistance > someWheelMovement) {
                                    doMovingInstance(line, stopMilliseconds, totalDistance, eeKcalTmp);
                                } else {
                                    Log.i(TAG, "Activity detected:13", mContext);
                                    participantMETkcal = partMETmultiply * mapMET.get("13");
                                    Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);
                                    TEMPLEDataManager.setEEKcal(mContext, String.valueOf(Double.valueOf(eeKcalTmp) + participantMETkcal));
                                    distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
                                    TEMPLEDataManager.setDistanceTravelledMeter(mContext, String.valueOf(Float.valueOf(distanceMeter) + totalDistance));

                                    String[] row = {
//                                            lineCp[2],
//                                            lineCp[3],
                                            dateFormatToConvert.format(startDate),
                                            dateFormatToConvert.format(stopDate),
                                            "13",
                                            "13",
                                            String.valueOf(Float.valueOf(distanceMeter) + totalDistance),
                                            String.valueOf(Double.valueOf(eeKcalTmp) + participantMETkcal),
                                    };
                                    CSV.write(row, arFile, true);
                                    CSV.write(row, arFileDay, true);
                                    DataManager.setLastARwindowStopTime(mContext, stopMilliseconds);

                                }

                            } else {
                                // watch only model
                                doARwatchOnlyInstance(line, stopMilliseconds, eeKcalTmp);
                            }

                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }

    public void doNonMovingInstance(String[] line, long stop, double dist, String eeKCalIn){
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
            Log.i(TAG, "Activity detected:"+className, mContext);
            participantMETkcal = partMETmultiply*mapMET.get(className);
            Log.i(TAG, "Energy expenditure in kCal="+eeKCalIn, mContext);
            Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);
            Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal), mContext);
            TEMPLEDataManager.setEEKcal(mContext,String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal));
            distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
            TEMPLEDataManager.setDistanceTravelledMeter(mContext,String.valueOf(Float.valueOf(distanceMeter)+dist));

            String[] row = {
//                    lineCp[2],
//                    lineCp[3],
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[2])),
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[3])),
                    "11",
                    className,
                    String.valueOf(Float.valueOf(distanceMeter)+dist),
                    String.valueOf(Double.valueOf(eeKCalIn) + participantMETkcal),
            };
            CSV.write(row, arFile, true);
            CSV.write(row,arFileDay,true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DataManager.setLastARwindowStopTime(mContext,stop);
    }

    public void doMovingInstance(String[] line, long stop, double dist, String eeKcalIn){
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
            Log.i(TAG, "Activity detected:"+className, mContext);
            participantMETkcal = partMETmultiply*mapMET.get(className);
            Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);
            TEMPLEDataManager.setEEKcal(mContext,String.valueOf(Double.valueOf(eeKcalIn) + participantMETkcal));
            distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
            TEMPLEDataManager.setDistanceTravelledMeter(mContext,String.valueOf(Float.valueOf(distanceMeter)+dist));

            String[] row = {
//                    lineCpd[2],
//                    lineCpd[3],
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCpd[2])),
                    dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCpd[3])),
                    "12",
                    className,
                    String.valueOf(Float.valueOf(distanceMeter)+dist),
                    String.valueOf(Double.valueOf(eeKcalIn) + participantMETkcal),
            };
            CSV.write(row, arFile, true);
            CSV.write(row,arFileDay,true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DataManager.setLastARwindowStopTime(mContext,stop);
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
            Log.i(TAG, "Activity detected:"+className, mContext);

            if(className.equals("11")){
                doNonMovingInstance(lineCp,stop,0d,eeKcalTm);
            }else if(className.equals("12")){
                doMovingInstance(lineCp,stop,0d,eeKcalTm);
            }else if(className.equals("13")) {
                participantMETkcal = partMETmultiply*mapMET.get(className);
                Log.i(TAG, "Energy expenditure in kCal="+String.valueOf(participantMETkcal), mContext);
                TEMPLEDataManager.setEEKcal(mContext,String.valueOf(Double.valueOf(eeKcalTm) + participantMETkcal));
                distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
                String[] row = {
//                        lineCp[2],
//                        lineCp[3],
                        dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[2])),
                        dateFormatToConvert.format(dateFormatConvertFromWatch.parse(lineCp[3])),
                        "13",
                        "13",
                        distanceMeter,
                        String.valueOf(Double.valueOf(eeKcalTm) + participantMETkcal),
                };

                CSV.write(row, arFile, true);
                CSV.write(row,arFileDay,true);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DataManager.setLastARwindowStopTime(mContext,stop);
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

                    if (startMilliseconds > lastARwindowStopTime) {
                        Log.i(TAG, lineS[2] + "," + lineS[3], mContext);
                        Log.i(TAG, line[5] + "," + line[6] + "," + line[7] + "," + line[8] + "," + line[9], mContext);
                        String eeKcalT = TEMPLEDataManager.getEEKcal(mContext);
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
//                    double[] tmp = newInstance.toDoubleArray();
//                    Log.i(TAG,String.valueOf(tmp.length),mContext);
//                    Log.i(TAG,String.valueOf(tmp[0])+","+String.valueOf(tmp[1])+","+String.valueOf(tmp[2])+","+String.valueOf(tmp[3])+","+String.valueOf(tmp[4]),mContext);
                        try {
                            double result = allActivitiesClassifier.classifyInstance(newInstance);
                            String className = allActivitiesclasses.get(new Double(result).intValue());
                            Log.i(TAG, "Activity detected:" + className, mContext);

                            if (className.equals("11")) {
                                doNonMovingInstance(lineS, stopMilliseconds, 0d, eeKcalT);
                            } else if (className.equals("12")) {
                                doMovingInstance(lineS, stopMilliseconds, 0d, eeKcalT);
                            } else if (className.equals("13")) {
                                participantMETkcal = partMETmultiply * mapMET.get(className);
                                Log.i(TAG, "Energy expenditure in kCal=" + String.valueOf(participantMETkcal), mContext);
                                TEMPLEDataManager.setEEKcal(mContext, String.valueOf(Double.valueOf(eeKcalT) + participantMETkcal));
                                distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
                                String[] row = {
//                                        lineS[2],
//                                        lineS[3],
                                        dateFormatToConvert.format(startDate),
                                        dateFormatToConvert.format(stopDate),
                                        "13",
                                        "13",
                                        distanceMeter,
                                        String.valueOf(Double.valueOf(eeKcalT) + participantMETkcal),
                                };

                                CSV.write(row, arFile, true);
                                CSV.write(row, arFileDay, true);
                                DataManager.setLastARwindowStopTime(mContext, stopMilliseconds);
                            }
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
