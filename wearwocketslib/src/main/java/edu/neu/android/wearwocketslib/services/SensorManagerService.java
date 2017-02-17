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
import java.util.Date;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.log.Logger;
import edu.neu.android.wearwocketslib.utils.system.ByteUtils;
import edu.neu.android.wearwocketslib.utils.system.DeviceInfo;
import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;

/**
 * Created by Dharam on 5/1/2015.
 */
public class SensorManagerService extends Service implements SensorEventListener2 {

    private Logger logger;

    private static final String TAG = "SensorManagerService";

    private android.hardware.SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mHeartRate;

    private static float[] gravity;
    private byte[] valuesHeartRate;
    private int countHeartRate;
    private final static float alpha = 0.8f;
    private int count;
    private int sr = 0;
    private long timeInMillis;
    private long lastBootupTime;

    private AndroidWearAccelerometerRaw accelRaw;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger = new Logger(TAG);
        SharedPrefs.setBoolean(Globals.SENSOR_MANAGER_SERVICE_STATUS, true, getApplicationContext());
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
        lastBootupTime = 0;

        valuesHeartRate = new byte[1600];
        countHeartRate = 0;

        accelRaw = new AndroidWearAccelerometerRaw(getApplicationContext());

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
                logger.i("Accelerometer listener registered", getApplicationContext());
            }else{
                logger.i("Accelerometer listener register unsuccessful", getApplicationContext());
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if(intent != null)
                logger.i("Inside onStartCommand: " + intent.getAction(), getApplicationContext());
            else
                logger.i("Inside onStartCommand", getApplicationContext());
//        }
        if (intent != null) {
            if ("FLUSH".equals(intent.getAction())) {
                logger.i("Got Flush command", getApplicationContext());
                boolean success = mSensorManager.flush(this);
                logger.i("Flush result: " + success, getApplicationContext());
                if(!success){
                    try {
                        accelRaw.flushAndCloseBinary(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage(), getApplicationContext());
                        logger.logStackTrace(e, getApplicationContext());
                    }
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
                                mSensorManager.unregisterListener(SensorManagerService.this, mHeartRate);
                                logger.i("HeartRate listener unregistered", getApplicationContext());
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
                accelRaw.setRawx(event.values[0] - gravity[0]);
                accelRaw.setRawy(event.values[1] - gravity[1]);
                accelRaw.setRawz(event.values[2] - gravity[2]);
                accelRaw.setTimestamp(timeInMillis);
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
