package edu.neu.mhealth.android.wockets.library.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;


//public class AccelerationManagerService extends Service implements SensorEventListener2{

public class AccelerationManagerService extends WocketsIntentService implements SensorEventListener {


    private static final String TAG = "AccelerationManager";
    public static final String mHealthTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    private SensorManager mSensorManager;
    private HandlerThread mHandlerThread;

    private Context mContext;
    private Sensor mAccel;
    private static float[] gravity;
    private final int maxDelay = 90000000;
    private final static float alpha = 0.8f;
    private long timeInMillis;

    @Override
    public void onCreate() {
        super.onCreate();

        gravity = new float[3];
        gravity[0] = 0.0f;
        gravity[1] = 0.0f;
        gravity[2] = 0.0f;

        mContext = getApplicationContext();
        Log.i(TAG,"INSIDE ONCREATE",mContext);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG,"Is wakeup sensor? " + String.valueOf(mAccel.isWakeUpSensor()),mContext);
        Log.i(TAG, "FIFO Count - " + mAccel.getFifoMaxEventCount(),mContext);
        Log.i(TAG, "FIFO Res Count - " + mAccel.getFifoReservedEventCount(),mContext);

        registerSensorListeners();
    }

    private void registerSensorListeners(){
        Log.i(TAG,"registering listener",mContext);
//        mHandlerThread = new HandlerThread("AccelerometerLogListener");
//        mHandlerThread.start();
//        Handler handler = new Handler(mHandlerThread.getLooper());

        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI, maxDelay);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.flush(this);
        mSensorManager.unregisterListener(this);

        notifyServiceStop();
        Log.i(TAG,"INSIDE ONDESTROY",mContext);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"on start command",mContext);
//        mSensorManager.flush(this);
        return Service.START_STICKY;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

//            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
//            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
//            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

//            Date dateNow = new Date();
//            String timestampStringNow = new SimpleDateFormat(mHealthTimestampFormat).format(dateNow);
//
//            timeInMillis = (new Date()).getTime()
//                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;
//
//            Date date = new Date(timeInMillis);
//            String timestampString = new SimpleDateFormat(mHealthTimestampFormat).format(date);


//            String[] accEntry = {
//                    timestampStringNow,
//                    timestampString,
//                    Float.toString(event.values[0]- gravity[0]),
//                    Float.toString(event.values[1]- gravity[1]),
//                    Float.toString(event.values[2]- gravity[2])
//            };

//            String[] accEntry = {
//                    timestampStringNow,
//                    timestampString,
//                    Float.toString(event.values[0]),
//                    Float.toString(event.values[1]),
//                    Float.toString(event.values[2])
//            };

//            String dataDirectory = DataManager.getDirectoryData(mContext);
//            String accFile = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Acceleration.csv";
//            CSV.write(accEntry, accFile, true);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

//    @Override
//    public void onFlushCompleted(Sensor sensor) {
//        mSensorManager.unregisterListener(this);
//        registerSensorListeners();
//        notifyFlushComplete();
//    }

    private void notifyFlushComplete(){
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "COMPLETED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void notifyFlushFailure(){
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "FAILURE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void notifyServiceStop(){
        Intent broadcastIntent = new Intent("FLUSH_RESULT");
        broadcastIntent.putExtra("MESSAGE", "SERVICE_STOP");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}

