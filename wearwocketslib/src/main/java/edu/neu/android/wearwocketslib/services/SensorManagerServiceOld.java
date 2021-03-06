package edu.neu.android.wearwocketslib.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.utils.log.Logger;
import edu.neu.android.wearwocketslib.utils.system.ByteUtils;
import edu.neu.android.wearwocketslib.utils.system.DateHelper;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.entities.mHealthEntity;

/**
 * Created by Dharam on 5/1/2015.
 */
public class SensorManagerServiceOld extends Service implements SensorEventListener2 {

    private Logger logger;

    private static final String TAG = "SensorManagerService";

    private android.hardware.SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private Sensor mGyroscope;
    private Sensor mHeartRate;

    private static float[] gravity;
    private byte[] valuesAccelerometer;
    private byte[] valuesAccelerometerPreviousHour;
    private byte[] valuesMagneticField;
    private byte[] valuesGyroscope;
    private byte[] valuesHeartRate;
    private int countAccelerometer;
    private int countMagneticField;
    private int countGyroscope;
    private int countHeartRate;
    private final static float alpha = 0.8f;
    private int count;
    private int sr = 0;
    private long timeInMillis;
    private long lastTimeInMillisAccel;
    private long lastTimeInMillisMag;
    private long lastTimeInMillisGyro;
    private long lastTimeInMillisHR;
    private long lastEventTimestamp;
    private byte[] x_axis;
    private byte[] y_axis;
    private byte[] z_axis;
    private byte[] timestamp;
    private long lastBootupTime;
    private String action;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(TAG);
        SharedPrefs.setBoolean(Globals.SENSOR_MANAGER_SERVICE_STATUS, true, getApplicationContext());
//        Log.i(TAG, "Inside onCreate", getApplicationContext());
        logger.i("Inside onCreate", getApplicationContext());
        initializeMembers();
    }

    private void initializeMembers() {
        logger.i("Inside initializeMembers", getApplicationContext());
        gravity = new float[3];
        gravity[0] = 0.0f;
        gravity[1] = 0.0f;
        gravity[2] = 0.0f;

        count = 0;
        lastTimeInMillisAccel = 0;
        lastTimeInMillisGyro = 0;
        lastTimeInMillisHR = 0;
        lastTimeInMillisMag = 0;
        lastBootupTime = 0;
        lastEventTimestamp = 0;

        valuesAccelerometer = new byte[20000]; // Now the sampling rate is set as 50Hz, so 20000 means a (20s) buffer
        valuesMagneticField = new byte[20000];
        valuesGyroscope = new byte[20000];
        valuesHeartRate = new byte[1600];
        countAccelerometer = 0;
        countMagneticField = 0;
        countGyroscope = 0;
        countHeartRate = 0;

        mSensorManager = (android.hardware.SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        Log.d(TAG, "FIFO Count - " + mAccelerometer.getFifoMaxEventCount());
        Log.d(TAG, "FIFO Res Count - " + mAccelerometer.getFifoReservedEventCount());
        registerSensorListeners();
    }

    private void registerSensorListeners(){
        if (Globals.IS_ACCELEROMETER_LOGGING_ENABLED) {
            if(mSensorManager.registerListener(this, mAccelerometer, android.hardware.SensorManager.SENSOR_DELAY_GAME, 90000000)){
                logger.i("Accelerometer listener registered", getApplicationContext());
            }else{
                logger.i("Accelerometer listener register unsuccessful", getApplicationContext());
            }

        }
        if (Globals.IS_MAGNETIC_FIELD_LOGGING_ENABLED) {
            if(mSensorManager.registerListener(this, mMagneticField, android.hardware.SensorManager.SENSOR_DELAY_GAME, 90000000)){
                Log.d(TAG, "MagneticField listener registered");
            }else{
                Log.d(TAG, "MagneticField listener register unsuccessful");
            }

        }
        if (Globals.IS_GYROSCOPE_LOGGING_ENABLED) {
            if(mSensorManager.registerListener(this, mGyroscope, android.hardware.SensorManager.SENSOR_DELAY_GAME, 90000000)){
                Log.d(TAG, "Gyroscope listener registered");
            }else{
                Log.d(TAG, "Gyroscope listener register unsuccessful");
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if((flags & START_FLAG_REDELIVERY) != 0) {
//            Log.i(TAG, "Inside onStartCommand: " + "crash restart (FLAG_REDELIVERY)", getApplicationContext());
//            initializeMembers();
//        }else if((flags & START_FLAG_RETRY) != 0){
//            Log.i(TAG, "Inside onStartCommand: " + "retry (FLAG_RETRY)", getApplicationContext());
//            initializeMembers();
//        }else{
            if(intent != null)
                logger.i("Inside onStartCommand: " + intent.getAction(), getApplicationContext());
            else
                logger.i("Inside onStartCommand", getApplicationContext());
//        }
        if (intent != null) {
            action = intent.getAction();
            if ("FLUSH".equals(intent.getAction())) {
                logger.i("Got Flush command", getApplicationContext());
                boolean success = mSensorManager.flush(this);
                logger.i("Flush result: " + success, getApplicationContext());
                if(!success){
                    notifyFlushFailure();
                }
                logger.i("Whole WakeLock cycle SamplingRate,"+sr, getApplicationContext());
                logger.i("Avail Memory percentage," + DeviceInfo.getMemoryUsageInPercentage(getApplicationContext()),getApplicationContext());
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
                                mSensorManager.unregisterListener(SensorManagerServiceOld.this, mHeartRate);
                                logger.i("HeartRate listener unregistered", getApplicationContext());
                            }
                        };
                        timer.start();
                        SharedPrefs.setLong(Globals.LAST_HEART_RATE_TIMESTAMP, System.currentTimeMillis(), getApplicationContext());
                    }
                }
            }
            else if("STOP".equals(intent.getAction())){
                stopSelf();
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.i("Inside onDestroy", getApplicationContext());
        processAccelerometerValues();
        SharedPrefs.setBoolean(Globals.SENSOR_MANAGER_SERVICE_STATUS, false, getApplicationContext());
        mSensorManager.unregisterListener(this);
        logger.i("Accelerometer listener unregistered", getApplicationContext());
        notifyServiceStop();
    }

    private void notifyFlushComplete(){
        logger.i("Notify sensor flusing completed", getApplicationContext());
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "COMPLETED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        logger.close();
    }

    private void notifyFlushFailure(){
        logger.i("Notify sensor flushing failure", getApplicationContext());
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "FAILURE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        logger.close();
    }

    private void notifyServiceStop(){
        logger.i("Notify sensor manager service stops", getApplicationContext());
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "SERVICE_STOP");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        logger.close();
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

            x_axis = ByteUtils.float2ByteArray(
                    event.values[0] - gravity[0]);
            y_axis = ByteUtils.float2ByteArray(
                    event.values[1] - gravity[1]);
            z_axis = ByteUtils.float2ByteArray(
                    event.values[2] - gravity[2]);

            timeInMillis = (new Date()).getTime()
                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;

            if(lastEventTimestamp != 0 && event.timestamp / 1000000L - lastEventTimestamp < 10){
                logger.d("Timestamp gap: " + (event.timestamp / 1000000L - lastEventTimestamp) + " ms");
            }

            if(count % 1000 == 0) {
                if(lastEventTimestamp != 0 && event.timestamp / 1000000L - lastEventTimestamp < 5){
                    logger.e("Timestamp gap weired (cherrypick): " + (event.timestamp / 1000000L - lastEventTimestamp) + " ms", getApplicationContext());
                }else {
                    logger.i("Timestamp gap (cherrypick): " + (event.timestamp / 1000000L - lastEventTimestamp) + " ms", getApplicationContext());
                }
                logger.i("Timestamp (cherrypick): " + new Date(timeInMillis).toString(), getApplicationContext());
            }

            lastEventTimestamp = event.timestamp/1000000L;

//            Log.d(TAG, "Current timestamp: " + new Date(timeInMillis).toString());
            if(lastTimeInMillisAccel != 0){
                int lastHour = new Date(lastTimeInMillisAccel).getHours();
                int currentHour = new Date(timeInMillis).getHours();
                if(currentHour - lastHour == 1 || (currentHour == 0 && lastHour == 23)){
                    //new hour, write the previous buffer first
                    logger.i("Before new hour: " + countAccelerometer + "time: " + new Date(timeInMillis).toString(), getApplicationContext());
                    valuesAccelerometerPreviousHour = Arrays.copyOfRange(valuesAccelerometer, 0, countAccelerometer);
                    countAccelerometer = 0;
                    logger.i("After new hour: " + countAccelerometer + "time: " + new Date(timeInMillis).toString(), getApplicationContext());
                }
            }
            lastTimeInMillisAccel = timeInMillis;
            timestamp = ByteUtils.long2ByteArray(timeInMillis);
            sr++;

            if(System.currentTimeMillis() - timeInMillis >= 1000*3600 || !mHealthEntity.verifyCurrentDate(new Date(timeInMillis))){
                logger.e("The timestamp is not yet initialized or not correct, skip saving this sample: " + new Date(timeInMillis).toString(), getApplicationContext());
            }else {
                valuesAccelerometer[countAccelerometer++] = x_axis[0];
                valuesAccelerometer[countAccelerometer++] = x_axis[1];
                valuesAccelerometer[countAccelerometer++] = x_axis[2];
                valuesAccelerometer[countAccelerometer++] = x_axis[3];

                valuesAccelerometer[countAccelerometer++] = y_axis[0];
                valuesAccelerometer[countAccelerometer++] = y_axis[1];
                valuesAccelerometer[countAccelerometer++] = y_axis[2];
                valuesAccelerometer[countAccelerometer++] = y_axis[3];

                valuesAccelerometer[countAccelerometer++] = z_axis[0];
                valuesAccelerometer[countAccelerometer++] = z_axis[1];
                valuesAccelerometer[countAccelerometer++] = z_axis[2];
                valuesAccelerometer[countAccelerometer++] = z_axis[3];

                valuesAccelerometer[countAccelerometer++] = timestamp[0];
                valuesAccelerometer[countAccelerometer++] = timestamp[1];
                valuesAccelerometer[countAccelerometer++] = timestamp[2];
                valuesAccelerometer[countAccelerometer++] = timestamp[3];
                valuesAccelerometer[countAccelerometer++] = timestamp[4];
                valuesAccelerometer[countAccelerometer++] = timestamp[5];
                valuesAccelerometer[countAccelerometer++] = timestamp[6];
                valuesAccelerometer[countAccelerometer++] = timestamp[7];
            }

            if (countAccelerometer == 20000) {
                logger.i("Start processing sensor buffer", getApplicationContext());
                processAccelerometerValues();
                logger.i("End ocessing sensor buffer", getApplicationContext());
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
            valuesMagneticField[countMagneticField++] = x_axis[0];
            valuesMagneticField[countMagneticField++] = x_axis[1];
            valuesMagneticField[countMagneticField++] = x_axis[2];
            valuesMagneticField[countMagneticField++] = x_axis[3];

            valuesMagneticField[countMagneticField++] = y_axis[0];
            valuesMagneticField[countMagneticField++] = y_axis[1];
            valuesMagneticField[countMagneticField++] = y_axis[2];
            valuesMagneticField[countMagneticField++] = y_axis[3];

            valuesMagneticField[countMagneticField++] = z_axis[0];
            valuesMagneticField[countMagneticField++] = z_axis[1];
            valuesMagneticField[countMagneticField++] = z_axis[2];
            valuesMagneticField[countMagneticField++] = z_axis[3];

            valuesMagneticField[countMagneticField++] = timestamp[0];
            valuesMagneticField[countMagneticField++] = timestamp[1];
            valuesMagneticField[countMagneticField++] = timestamp[2];
            valuesMagneticField[countMagneticField++] = timestamp[3];
            valuesMagneticField[countMagneticField++] = timestamp[4];
            valuesMagneticField[countMagneticField++] = timestamp[5];
            valuesMagneticField[countMagneticField++] = timestamp[6];
            valuesMagneticField[countMagneticField++] = timestamp[7];

            if (countMagneticField == 20000) {
                processMagneticFieldValues();
            }
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
            valuesGyroscope[countGyroscope++] = x_axis[0];
            valuesGyroscope[countGyroscope++] = x_axis[1];
            valuesGyroscope[countGyroscope++] = x_axis[2];
            valuesGyroscope[countGyroscope++] = x_axis[3];

            valuesGyroscope[countGyroscope++] = y_axis[0];
            valuesGyroscope[countGyroscope++] = y_axis[1];
            valuesGyroscope[countGyroscope++] = y_axis[2];
            valuesGyroscope[countGyroscope++] = y_axis[3];

            valuesGyroscope[countGyroscope++] = z_axis[0];
            valuesGyroscope[countGyroscope++] = z_axis[1];
            valuesGyroscope[countGyroscope++] = z_axis[2];
            valuesGyroscope[countGyroscope++] = z_axis[3];

            valuesGyroscope[countGyroscope++] = timestamp[0];
            valuesGyroscope[countGyroscope++] = timestamp[1];
            valuesGyroscope[countGyroscope++] = timestamp[2];
            valuesGyroscope[countGyroscope++] = timestamp[3];
            valuesGyroscope[countGyroscope++] = timestamp[4];
            valuesGyroscope[countGyroscope++] = timestamp[5];
            valuesGyroscope[countGyroscope++] = timestamp[6];
            valuesGyroscope[countGyroscope++] = timestamp[7];

            if (countGyroscope == 20000) {
                processGyroscopeValues();
            }
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

    private void processAccelerometerValues() {
        logger.i("Inside processAccelerometerValues: " + countAccelerometer, getApplicationContext());
        String dataType = AndroidWearAccelerometerRaw.DATA_TYPE;
        if(countAccelerometer == 20000) {
            logger.i("Writing to new hour normally: " + valuesAccelerometer.length, getApplicationContext());
            Log.saveSensorData(dataType, valuesAccelerometer, new Date(), getApplicationContext());
        }
        else{
            byte[] values = Arrays.copyOfRange(valuesAccelerometer, 0, countAccelerometer);
            logger.i("Writing earlier to new hour: " + values.length, getApplicationContext());
            Log.saveSensorData(dataType, values, new Date(), getApplicationContext());
        }
        if(valuesAccelerometerPreviousHour != null) {
            Date now = new Date();
            Date preHourDate = DateHelper.getPreviousHourDate(now);
            logger.i("Writing to old hours when crossing hour: " + valuesAccelerometerPreviousHour.length, getApplicationContext());
            Log.saveSensorData(dataType, valuesAccelerometerPreviousHour, preHourDate, getApplicationContext());
            valuesAccelerometerPreviousHour = null;
        }
        countAccelerometer = 0;
    }

    private void processMagneticFieldValues() {
        logger.i("Inside processMagneticFieldValues", getApplicationContext());
        Log.saveSensorData("MAGNETIC-FIELD", valuesMagneticField, new Date(), getApplicationContext());
        countMagneticField = 0;
    }

    private void processGyroscopeValues() {
        logger.i("Inside processGyroscopeValues", getApplicationContext());
        Log.saveSensorData("ANGULAR-SPEED-CALIBRATED", valuesGyroscope, new Date(), getApplicationContext());
        countGyroscope = 0;
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
        logger.i("Flush is completed:" + sensor.getName(), this);
        // reregister the sensor listener when flush is completed
        mSensorManager.unregisterListener(this);
        logger.i("Accelerometer listener unregistered", getApplicationContext());
        logger.i("MagneticField listener unregistered", getApplicationContext());
        logger.i("HeartRate listener unregistered", getApplicationContext());
        registerSensorListeners();
        notifyFlushComplete();

    }
}
