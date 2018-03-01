package edu.neu.android.wearwocketslib.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.FeatureLogger;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.log.Logger;
import edu.neu.android.wearwocketslib.utils.system.ByteUtils;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;

/**
 * Created by Binod on 5/1/2015.
 */
public class SensorManagerService extends Service implements SensorEventListener2 {

    private Logger logger;
    private Logger logger_feature;
    private FeatureLogger logger_feature_day;


    private static final String TAG = "SensorManagerService";
    private static final String TAGF = "ComputedFeature";
    private static final String TAGFD = "ComputedFeatureDay";

    private android.hardware.SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mHeartRate;

    private static float[] gravity;
    private byte[] valuesHeartRate;
    private int countHeartRate;
    private final static float alpha = 0.8f;
    private final static float freqCutOff = 5.0f;
    private final static float samplingRate = 50.0f;
    private int count;
    private int sr = 0;
    private long timeInMillis;
    private long lastBootupTime;
    private Context mContext;


    private ArrayList<Float> xReading;
    private ArrayList<Float> yReading;
    private ArrayList<Float> zReading;

//    private ArrayList<Float> xReadingLPF;
//    private ArrayList<Float> yReadingLPF;
//    private ArrayList<Float> zReadingLPF;


    private ArrayList<Long> timeReading;
    private ArrayList<Float> filteredAr;

    private int lenArray;
    private DateFormat df;


    private int counter;

    private AndroidWearAccelerometerRaw accelRaw;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        logger_feature = new FeatureLogger(TAGF);

        logger_feature_day = new FeatureLogger(TAGFD);
        logger_feature = new Logger(TAGF);
        logger = new Logger(TAG);


        mContext = getApplicationContext();

        SharedPrefs.setBoolean(Globals.SENSOR_MANAGER_SERVICE_STATUS, true, mContext);
        logger.i("Inside onCreate", getApplicationContext());
        initializeMembers();
    }

    private void initializeMembers() {
        logger.i("Inside initializeMembers", mContext);
        gravity = new float[3];
        gravity[0] = 0.0f;
        gravity[1] = 0.0f;
        gravity[2] = 0.0f;

        count = 0;
        counter = 0;

        xReading = new ArrayList<Float>();
        yReading = new ArrayList<Float>();
        zReading = new ArrayList<Float>();
        timeReading = new ArrayList<Long>();

//        xReadingLPF = new ArrayList<Float>();
//        yReadingLPF = new ArrayList<Float>();
//        zReadingLPF = new ArrayList<Float>();


        df = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");

        lastBootupTime = 0;

        valuesHeartRate = new byte[1600];
        countHeartRate = 0;

        accelRaw = new AndroidWearAccelerometerRaw(mContext);

        mSensorManager = (android.hardware.SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d(TAG,Float.toString(mAccelerometer.getMaximumRange()));
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        Log.d(TAG, "FIFO Count - " + mAccelerometer.getFifoMaxEventCount());
        Log.d(TAG, "FIFO Res Count - " + mAccelerometer.getFifoReservedEventCount());
        registerSensorListeners();
    }

    private void registerSensorListeners(){
        if (Globals.IS_ACCELEROMETER_LOGGING_ENABLED) {
            if(mSensorManager.registerListener(this, mAccelerometer, android.hardware.SensorManager.SENSOR_DELAY_GAME, 90000000)){
                logger.i("Accelerometer listener registered", mContext);
            }else{
                logger.i("Accelerometer listener register unsuccessful", mContext);
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            logger.i("Inside onStartCommand: " + intent.getAction(), mContext);
        }else {
            logger.i("Inside onStartCommand", mContext);
        }
//        }
        if (intent != null) {
            if ("FLUSH".equals(intent.getAction())) {
                logger.i("Got Flush command", mContext);

                String currentDate = new SimpleDateFormat("yyyy-MM-dd-HH").format(new Date());

                logger.i("Length of list:" + Integer.toString(xReading.size()),this);

                if(xReading.size()!=0) {

//                    float meanX = getMean(xReading);

                    float varY = getVariance(yReading);
                    float varX = getVariance(xReading);

                    // all-activities features
                    int zeroCrossingZ = calculateZeroCross(zReading);
                    int meanCross = getMeanCrossing(xReading, yReading, zReading);
                    float madMedianX = calculateMadMed(xReading);

                    float madMean = getMadMean(xReading, yReading, zReading);
//                    float varY = getVariance(yReading);
//                    float varX = getVariance(xReading);

                    double rmsY = getRMS(yReading);
                    double rmsZ = getRMS(zReading);

                    float madMedianXYZ = getMadMedian(xReading, yReading, zReading);
                    // not-moving activities features
                    float madMedianZ = calculateMadMed(zReading);
                    int meanCrossX = calculateMeanCross(xReading);

//                    float meanX = getMean(xReading);

//                    int zeroCrossingY = calculateZeroCross(yReading);
//                    float madMeanX = calculateMadMed(xReading);
//                    float madMean = getMadMean(xReading, yReading, zReading);
//
//                    float fluctInAmp = getMaxValue(xReading) - getMinValue(xReading);
//                    float xReadingMean = getMean(xReading);
//                    float zReadingMean = getMean(zReading);
//                    float distXZ = xReadingMean - zReadingMean;



                    Date firstDate = new Date(timeReading.get(0));
                    Date lastDate = new Date(timeReading.get(timeReading.size()-1));
                    String firstD = df.format(firstDate);
                    String lastD = df.format(lastDate);

//                    String row = String.format("%s,%s,%d,%.5f,%d,%.5f,%.5f,%.5f",firstD,lastD,xReading.size(),xReadingMean,meanCross,distXZ,fluctInAmp,madMedian);
                    String row = String.format("%s,%s,%d,%d,%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%d",firstD,lastD,xReading.size(),zeroCrossingZ,meanCross,madMedianX,rmsY,madMean,varY,madMedianXYZ,varX,rmsZ,madMedianZ,meanCrossX);

                    logger_feature.i(row,mContext);
                    logger_feature_day.i(row,mContext);
                    logger.i(row, mContext);
//                    logger.i(String.format("%.5f",meanX),mContext);

                }

                boolean success = mSensorManager.flush(this);
                logger.i("Flush result: " + success, mContext);
                if(!success){
                    try {
                        accelRaw.flushAndCloseBinary(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage(), mContext);
                        logger.logStackTrace(e, mContext);
                    }
                    notifyFlushFailure();
                }

                logger.i("Whole WakeLock cycle SamplingRate,"+sr, mContext);
                logger.i("Avail Memory percentage," + DeviceInfo.getMemoryUsageInPercentage(mContext),mContext);

                xReading.clear();
                yReading.clear();
                zReading.clear();
                timeReading.clear();
//                xReadingLPF.clear();
//                yReadingLPF.clear();
//                zReadingLPF.clear();

                sr = 0;
                if(!success){
                    stopSelf();
                }

                if (Globals.IS_HEART_RATE_LOGGING_ENABLED) {

                    if ((System.currentTimeMillis() - SharedPrefs.getLong(Globals.LAST_HEART_RATE_TIMESTAMP, 0, getApplicationContext())) > Globals.HEART_RATE_DELAY_MS) {

                        mSensorManager.registerListener(this, mHeartRate, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
                        Log.d(TAG, "HeartRate listener registered");
                        CountDownTimer timer = new CountDownTimer(30000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                mSensorManager.unregisterListener(SensorManagerService.this, mHeartRate);
                                logger.i("HeartRate listener unregistered", mContext);
                            }
                        };
                        timer.start();
                        SharedPrefs.setLong(Globals.LAST_HEART_RATE_TIMESTAMP, System.currentTimeMillis(), getApplicationContext());
                    }
                }
            }
            else if("STOP".equals(intent.getAction())){
                logger.i("Stop sensor manager service by STOP signal", getApplicationContext());
                stopSelf();
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.i("Inside onDestroy", getApplicationContext());
        try {
            accelRaw.flushAndCloseBinary(true);
        } catch (Exception e) {
            logger.e(e.getMessage(), getApplicationContext());
            logger.logStackTrace(e, getApplicationContext());
            e.printStackTrace();
        }
        SharedPrefs.setBoolean(Globals.SENSOR_MANAGER_SERVICE_STATUS, false, getApplicationContext());
        mSensorManager.unregisterListener(this);
        logger.i("Accelerometer listener unregistered", getApplicationContext());
        notifyServiceStop();
    }

    private void notifyFlushComplete(){
        logger.i("Notify sensor flushing completed", getApplicationContext());
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "COMPLETED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        logger.close();
        logger_feature.close();
        logger_feature_day.close();
    }

    private void notifyFlushFailure(){
        logger.i("Notify sensor flushing failure", getApplicationContext());
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "FAILURE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        logger.close();
        logger_feature.close();
        logger_feature_day.close();
    }

    private void notifyServiceStop(){
        logger.i("Notify sensor manager service stops", getApplicationContext());
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "SERVICE_STOP");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        logger.close();
        logger_feature.close();
        logger_feature_day.close();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(lastBootupTime == 0){
            lastBootupTime = SharedPrefs.getLong(Globals.LAST_BOOT_UP_TIME, System.currentTimeMillis() - SystemClock.elapsedRealtime(), getApplicationContext());
        }

        count++;
        if(count % 1000 == 0) {
            logger.i(event.sensor.getName() + " sensor onSensorChanged got called: " + count + " times", getApplicationContext());
            count = 0;
            if((lastBootupTime - (System.currentTimeMillis() - SystemClock.elapsedRealtime())) >= 100){
                logger.e("boot up time changed weired (cherrypick): " + (lastBootupTime - (System.currentTimeMillis() - SystemClock.elapsedRealtime())) + " ms", getApplicationContext());
            }else{
                logger.i("Verify boot up time difference (cherrypick): " + (lastBootupTime - (System.currentTimeMillis() - SystemClock.elapsedRealtime())) + " ms", getApplicationContext());
            }
            logger.i("Last boot up time (cherrypick): " + new Date(lastBootupTime).toString(), getApplicationContext());

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            timeInMillis = (new Date()).getTime()
                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;

            if(System.currentTimeMillis() - timeInMillis >= 1000*3600){
                if(count % 1000 == 0) {
                    logger.e("The timestamp is not yet initialized or not correct, skip saving this sample: " + new Date(timeInMillis).toString(), getApplicationContext());
                }
                return;
            }else{
                if(count % 1000 == 0){
                    logger.e("Time is valid, saving continues: " + new Date(timeInMillis).toString(), getApplicationContext());
                }
            }

            sr++;

            if(accelRaw.verifyCurrentDate(new Date(timeInMillis))) {

//                accelRaw.setRawx(event.values[0] - gravity[0]);
//                accelRaw.setRawy(event.values[1] - gravity[1]);
//                accelRaw.setRawz(event.values[2] - gravity[2]);

                accelRaw.setRawx(event.values[0]);
                accelRaw.setRawy(event.values[1]);
                accelRaw.setRawz(event.values[2]);

                accelRaw.setTimestamp(timeInMillis);

                // populate the array
//                xReading.add(event.values[0] - gravity[0]);
//                yReading.add(event.values[1] - gravity[1]);
//                zReading.add(event.values[2] - gravity[2]);

                xReading.add(event.values[0]);
                yReading.add(event.values[1]);
                zReading.add(event.values[2]);

                timeReading.add(timeInMillis);

                try {
                    accelRaw.bufferedWriteTomHealthBinary(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.e(e.getMessage(), getApplicationContext());
                    logger.logStackTrace(e, getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage(), getApplicationContext());
                    logger.logStackTrace(e, getApplicationContext());
                }
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            byte[] x_axis = ByteUtils.float2ByteArray(
                    event.values[0]);
            byte[] y_axis = ByteUtils.float2ByteArray(
                    event.values[1]);
            byte[] z_axis = ByteUtils.float2ByteArray(
                    event.values[2]);
            long timeInMillis = (new Date()).getTime()
                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;
            byte[] timestamp = ByteUtils.long2ByteArray(timeInMillis);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            byte[] x_axis = ByteUtils.float2ByteArray(
                    event.values[0]);
            byte[] y_axis = ByteUtils.float2ByteArray(
                    event.values[1]);
            byte[] z_axis = ByteUtils.float2ByteArray(
                    event.values[2]);
            long timeInMillis = (new Date()).getTime()
                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;
            byte[] timestamp = ByteUtils.long2ByteArray(timeInMillis);
        }

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            System.out.println("Heart Rate - " + event.values[0] + ", Accuracy - " + event.accuracy + ",Count - " + countHeartRate);
            byte[] heartRate = ByteUtils.float2ByteArray(
                    event.values[0]);
            byte[] accuracy = ByteUtils.float2ByteArray(
                    event.accuracy);
            long timeInMillis = (new Date()).getTime()
                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;
            byte[] timestamp = ByteUtils.long2ByteArray(timeInMillis);

            valuesHeartRate[countHeartRate++] = heartRate[0];
            valuesHeartRate[countHeartRate++] = heartRate[1];
            valuesHeartRate[countHeartRate++] = heartRate[2];
            valuesHeartRate[countHeartRate++] = heartRate[3];

            valuesHeartRate[countHeartRate++] = accuracy[0];
            valuesHeartRate[countHeartRate++] = accuracy[1];
            valuesHeartRate[countHeartRate++] = accuracy[2];
            valuesHeartRate[countHeartRate++] = accuracy[3];

            valuesHeartRate[countHeartRate++] = timestamp[0];
            valuesHeartRate[countHeartRate++] = timestamp[1];
            valuesHeartRate[countHeartRate++] = timestamp[2];
            valuesHeartRate[countHeartRate++] = timestamp[3];
            valuesHeartRate[countHeartRate++] = timestamp[4];
            valuesHeartRate[countHeartRate++] = timestamp[5];
            valuesHeartRate[countHeartRate++] = timestamp[6];
            valuesHeartRate[countHeartRate++] = timestamp[7];

            if (countHeartRate == 1600) {
                processHeartRateValues();
            }
        }
    }

    private void processHeartRateValues() {
        logger.i("Inside processHeartRateValues", getApplicationContext());
        Log.saveSensorData("HEART-RATE", valuesHeartRate, new Date(), getApplicationContext());
        countHeartRate = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        logger.i("Accuracy changed to: " + accuracy, getApplicationContext());
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {
        logger.i("Flush is completed:" + sensor.getName(), this);
        // reregister the sensor listener when flush is completed
        mSensorManager.unregisterListener(this);
        logger.i("Accelerometer listener unregistered", getApplicationContext());
        logger.i("MagneticField listener unregistered", getApplicationContext());
        logger.i("HeartRate listener unregistered", getApplicationContext());
        registerSensorListeners();
        notifyFlushComplete();
    }

    public float getMean(ArrayList<Float> ar){
        float total = 0;
        float num = (float) ar.size();
        for (float element : ar) {
            total += element;
        }
        float average = total/num;
        return average;
    }

    public float getVariance(ArrayList<Float> ar){
        float meanVal = getMean(ar);
        float num = (float) (ar.size()-1);

        float temp = 0;
        for(float element :ar)
            temp += (element-meanVal)*(element-meanVal);
        return temp/num;
    }

    public float getMaxValue(ArrayList<Float> ar) {
        float maxValue = ar.get(0);
        for (float element : ar){
            if(element > maxValue){
                maxValue = element;
            }
        }
        return maxValue;
    }

    public float getMinValue(ArrayList<Float> ar) {
        float minValue = ar.get(0);
        for (float element : ar){
            if(element < minValue){
                minValue = element;
            }
        }
        return minValue;
    }

    public double getRMS(ArrayList<Float> ar){
        int n = ar.size();
        double rms = 0;
        for (int i = 0; i < ar.size(); i++) {
            rms += ar.get(i) * ar.get(i);
        }
        rms/=n;
        return Math.sqrt(rms);
    }

    public int getMeanCrossing(ArrayList<Float> xAr, ArrayList<Float> yAr, ArrayList<Float> zAr) {
        ArrayList<Float> RMS = new ArrayList<Float>();

        for (int i = 1; i < xAr.size(); i++) {
            float tmp = (float) Math.sqrt((xAr.get(i)*xAr.get(i) + yAr.get(i)*yAr.get(i) + zAr.get(i)*zAr.get(i)));
            RMS.add(tmp);
        }
        logger.i("RMS size " + Integer.toString(RMS.size()), getApplicationContext());

        return calculateMeanCross(RMS);

    }


    public int calculateZeroCross(ArrayList<Float> data)
    {
        int numCrossing = 0;

        for (int i = 0; i < data.size()-1; i++)
        {
            if ((data.get(i) > 0 && data.get(i+1)  <= 0) ||
                    (data.get(i) < 0 && data.get(i+1) >= 0))
            {
                numCrossing++;
            }
        }

        logger.i("Number of zero crossing is half " + Integer.toString(numCrossing), getApplicationContext());
        int numCycles = Math.round(numCrossing);
        return numCycles;
    }




    public int calculateMeanCross(ArrayList<Float> dataIn)
    {
        ArrayList<Float> data = (ArrayList<Float>)dataIn.clone();
        float meanV = getMean(data);

        for (int i = 0; i < data.size(); i++) {
            float val = data.get(i);
            data.set(i, (val-meanV));
        }

//        logger.i("Demeaned data size " + Integer.toString(data.size()), getApplicationContext());
        int numCrossing = 0;

        for (int i = 0; i < data.size()-1; i++)
        {
            if ((data.get(i) > 0 && data.get(i+1)  <= 0) ||
                    (data.get(i) < 0 && data.get(i+1) >= 0))
            {
                numCrossing++;
            }
        }

//        logger.i("Number of mean crossing is half " + Integer.toString(numCrossing), getApplicationContext());
//        int numCycles = Math.round(numCrossing);
        return numCrossing;
    }

    public float getMadMedian(ArrayList<Float> xAr, ArrayList<Float> yAr, ArrayList<Float> zAr) {
        ArrayList<Float> RMS = new ArrayList<Float>();

        for (int i = 0; i < xAr.size(); i++) {
            float tmp = (float) Math.sqrt((xAr.get(i)*xAr.get(i) + yAr.get(i)*yAr.get(i) + zAr.get(i)*zAr.get(i)));
            RMS.add(tmp);
        }
        return calculateMadMed(RMS);
    }

    public float getMadMean(ArrayList<Float> xAr, ArrayList<Float> yAr, ArrayList<Float> zAr) {
        ArrayList<Float> RMS = new ArrayList<Float>();

        for (int i = 0; i < xAr.size(); i++) {
            float tmp = (float) Math.sqrt((xAr.get(i)*xAr.get(i) + yAr.get(i)*yAr.get(i) + zAr.get(i)*zAr.get(i)));
            RMS.add(tmp);
        }
        return calculateMadMean(RMS);
    }


    public float calculateMadMean(ArrayList<Float> dataIn)
    {
        ArrayList<Float> data = (ArrayList<Float>)dataIn.clone();
        float meanVal = getMean(data);

        for (int i = 0; i < data.size(); i++) {
            float val = data.get(i);
            data.set(i, (Math.abs(val-meanVal)));
        }

        float finalMadVal = getMean(data);

        return finalMadVal;
    }


    public float calculateMadMed(ArrayList<Float> dataIn)
    {
        ArrayList<Float> data = (ArrayList<Float>)dataIn.clone();
        Collections.sort(data);

        int n = data.size();

        float medianVal = 0;

        if (n%2 == 0){
            medianVal = ( data.get(n/2) + data.get((n/2)-1)) / 2;
        }else{
            medianVal = data.get((n-1)/2);
        }

        for (int i = 0; i < data.size(); i++) {
            float val = data.get(i);
            data.set(i, (Math.abs(val-medianVal)));
        }

        Collections.sort(data);

        float finalMedianVal = 0;

        if (n%2 == 0){
            finalMedianVal = ( data.get(n/2) + data.get((n/2)-1)) / 2;
        }else{
            finalMedianVal = data.get((n-1)/2);
        }

        return finalMedianVal;
    }

    public ArrayList<Float> lowPassFilter(ArrayList<Float> data){
        float pi = (float) Math.PI;
        float RC = 1/(2*pi*freqCutOff);
        float dt = 1/samplingRate;
        float alp = dt/(dt+RC);

        filteredAr = new ArrayList<Float>();
        float smh = data.get(0)*alp;
        int start = 0;

        filteredAr.add(start,smh);

        for (int i = 1; i < data.size(); i++) {

            float tmp = alp *(data.get(i)-filteredAr.get(i-1)) + filteredAr.get(i-1);

            filteredAr.add(i,tmp);
        }

        return filteredAr;

    }


}