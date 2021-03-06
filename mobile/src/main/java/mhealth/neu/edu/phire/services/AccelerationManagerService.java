package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;



public class AccelerationManagerService extends WocketsIntentService implements SensorEventListener {

    private static final String TAG = "AccelerationManagerService";
    private static final char CSV_DELIM = ',';
    public static final String mHealthTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String dayFormat = "yyyy-MM-dd";
    public static final String hourFormat = "HH-z";

    private SensorManager mSensorManager;

    private Context mContext;
    private Sensor mAccel;
    private static float[] gravity;
    private static float[] linear_accleration;
    private Date dateEvent;
    private Date dateNow;
    private final int maxDelay = 90000000;
    private final static float alpha = 0.8f;
    private long timeInMillis;
    private ExecutorService executor;
    private HandlerThread mHandlerThread;
    private Handler handler;
    private PrintWriter printWriter;


//    private AndroidWearAccelerometerRaw accelRaw;

    public AccelerationManagerService(){
        super("AccelerationManagerService");
    }


//    @Override
//    protected void onHandleIntent(Intent intent) {
//        gravity = new float[3];
//        linear_accleration = new float[3];
//
//        gravity[0] = 0.0f;
//        gravity[1] = 0.0f;
//        gravity[2] = 0.0f;
//
//        mContext = getApplicationContext();
//        Log.i(TAG,"INSIDE ONHANDLE INTENT",mContext);
//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        Log.i(TAG,"Is wakeup sensor? " + String.valueOf(mAccel.isWakeUpSensor()),mContext);
//        Log.i(TAG, "FIFO Count - " + mAccel.getFifoMaxEventCount(),mContext);
//        Log.i(TAG, "FIFO Res Count - " + mAccel.getFifoReservedEventCount(),mContext);
//
//        registerSensorListeners();
//    }



    @Override
    public void onCreate() {
        super.onCreate();

        gravity = new float[3];
        linear_accleration = new float[3];

        gravity[0] = 0.0f;
        gravity[1] = 0.0f;
        gravity[2] = 0.0f;

        mContext = getApplicationContext();
        Log.i(TAG,"INSIDE ONCREATE",mContext);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mHandlerThread = new HandlerThread("AccelerometerLogListener");
        mHandlerThread.start();
        handler = new Handler(mHandlerThread.getLooper());

        Log.i(TAG,"Is wakeup sensor? " + String.valueOf(mAccel.isWakeUpSensor()),mContext);
        Log.i(TAG, "FIFO Count - " + mAccel.getFifoMaxEventCount(),mContext);
        Log.i(TAG, "FIFO Res Count - " + mAccel.getFifoReservedEventCount(),mContext);

        dateNow = new Date();
        String dataDirectory = DataManager.getDirectoryData(mContext);
        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
        String hourDirectory = new SimpleDateFormat(hourFormat).format(dateNow);
        String accFilePath = dataDirectory + "/" + dayDirectory + "/" + hourDirectory + "/" + "Acceleration.txt";
        File accFile = new File(accFilePath);
        try{
            if (accFile.createNewFile()){
                Log.i(TAG,"File is created!",mContext);
            }else {
                Log.i(TAG,"File already exists!",mContext);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try
        {
            printWriter =
                    new PrintWriter(new BufferedWriter(new FileWriter(accFilePath,true)));

//            printWriter.println(CSV_HEADER);
        }
        catch (IOException e)
        {
            Log.i(TAG, "Could not open CSV file(s)", mContext);
        }


        registerSensorListeners();
    }

    private void registerSensorListeners(){
        Log.i(TAG,"registering listener",mContext);

        if (Looper.myLooper() == Looper.getMainLooper()){
            Log.i(TAG,"In main thread",mContext);
        }else{
            Log.i(TAG,"Not in main thread",mContext);
        }
//        accelRaw = new AndroidWearAccelerometerRaw(mContext);
//        SensorEventLoggerTask task = new SensorEventLoggerTask(mContext);
//        executor = Executors.newSingleThreadExecutor();

        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL, maxDelay,handler);
    }

//    class InsertHandler implements Runnable {
//        final float[] accelerometerMatrix;
//        final Date eventTime;
//        final Date receivedTime;
//        final Context rContext;
//        final long time;
//
//
//        public InsertHandler(float[] accelerometerMatrix, Date eventTime,Date receivedTime, long time,Context rContext) {
//            this.accelerometerMatrix = accelerometerMatrix;
//            this.eventTime = eventTime;
//            this.receivedTime = receivedTime;
//            this.rContext = rContext;
//            this.time = time;
//        }
//
//        public void run() {
//            String receivedTimeString = new SimpleDateFormat(mHealthTimestampFormat).format(receivedTime);
//            String eventTimeString = new SimpleDateFormat(mHealthTimestampFormat).format(eventTime);
//
//            String[] accEntry = {
//                    receivedTimeString,
//                    eventTimeString,
//                    Float.toString(accelerometerMatrix[0]),
//                    Float.toString(accelerometerMatrix[1]),
//                    Float.toString(accelerometerMatrix[2])
//            };
//
//            String dataDirectory = DataManager.getDirectoryData(rContext);
//            String dayDirectory = new SimpleDateFormat(dayFormat).format(receivedTime);
//            String hourDirectory = new SimpleDateFormat(hourFormat).format(receivedTime);
//            String accFile = dataDirectory + "/" + dayDirectory + "/" + hourDirectory + "/" + "Acceleration.csv";
//            CSV.write(accEntry, accFile, true);
//        }
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        try {
//            accelRaw.flushAndCloseBinary(true);
//        } catch (Exception e) {
//            Log.e(TAG,e.getMessage(), getApplicationContext());
//            e.printStackTrace();
//        }
        mSensorManager.flush(this);
        mSensorManager.unregisterListener(this);
        if (printWriter != null)
        {
            printWriter.close();
        }

        if (printWriter.checkError())
        {
            Log.e(TAG, "Error closing writer",mContext);
        }
        notifyServiceStop();
        Log.i(TAG,"INSIDE ON DESTROY",mContext);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"on start command",mContext);
        if (printWriter != null)
        {
            printWriter.close();
        }

//        if (printWriter.checkError())
//        {
//            Log.e(TAG, "Error closing writer",mContext);
//        }

        dateNow = new Date();
        String dataDirectory = DataManager.getDirectoryData(mContext);
        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
        String hourDirectory = new SimpleDateFormat(hourFormat).format(dateNow);
        String accFilePath = dataDirectory + "/" + dayDirectory + "/" + hourDirectory + "/" + "Acceleration.txt";
        File accFile = new File(accFilePath);
            try{
                if (accFile.createNewFile()){
                    Log.i(TAG,"File is created!",mContext);

                }else {
                    Log.i(TAG,"File already exists!",mContext);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        try
        {
            printWriter =
                    new PrintWriter(new BufferedWriter(new FileWriter(accFilePath,true)));

        }
        catch (IOException e)
        {
            Log.i(TAG, "Could not open CSV file(s)", mContext);
        }


        mSensorManager.flush(this);

        return Service.START_STICKY;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

//            SensorEventLoggerTask task = new SensorEventLoggerTask(mContext);
//            task.execute(event);

//            Log.i(TAG, "incoming accelerometer reading", mContext);

//            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
//            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
//            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
//
//            linear_accleration[0] = event.values[0] - gravity[0];
//            linear_accleration[1] = event.values[1] - gravity[1];
//            linear_accleration[2] = event.values[2] - gravity[2];

            dateNow = new Date();
//            String timestampStringNow = new SimpleDateFormat(mHealthTimestampFormat).format(dateNow);

//            timeInMillis = (new Date()).getTime()
//                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;

//            dateEvent = new Date(timeInMillis);
//            String timestampString = new SimpleDateFormat(mHealthTimestampFormat).format(date);

//            Runnable insertHandler = new InsertHandler(linear_accleration, dateEvent, dateNow, timeInMillis,mContext);
//            executor.execute(insertHandler);



            String timestampStringNow = new SimpleDateFormat(mHealthTimestampFormat).format(dateNow);

            timeInMillis = (new Date()).getTime()
                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;

            Date date = new Date(timeInMillis);
            String timestampString = new SimpleDateFormat(mHealthTimestampFormat).format(date);


//            String[] accEntry = {
//                    timestampStringNow,
//                    timestampString,
//                    Float.toString(event.values[0] - gravity[0]),
//                    Float.toString(event.values[1] - gravity[1]),
//                    Float.toString(event.values[2] - gravity[2])
//            };

//            String[] accEntry = {
//                    timestampStringNow,
//                    timestampString,
//                    Float.toString(event.values[0]),
//                    Float.toString(event.values[1]),
//                    Float.toString(event.values[2])
//            };
//            String dataDirectory = DataManager.getDirectoryData(mContext);
//            String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
//            String hourDirectory = new SimpleDateFormat(hourFormat).format(dateNow);
//            String accFile = dataDirectory + "/" + dayDirectory + "/" + hourDirectory + "/" + "Acceleration.csv";
//            CSV.write(accEntry, accFile, true);

            String accRow = timestampStringNow +","+timestampString+","+Float.toString(event.values[0])+","+Float.toString(event.values[1])+","+Float.toString(event.values[2]);
            if (printWriter != null) {
//                StringBuffer sb = new StringBuffer()
//                        .append(timestampStringNow).append(CSV_DELIM)
//                        .append(timestampString).append(CSV_DELIM)
//                        .append(event.values[0]).append(CSV_DELIM)
//                        .append(event.values[1]).append(CSV_DELIM)
//                        .append(event.values[1]).append(CSV_DELIM);

                printWriter.println(accRow);
                if (printWriter.checkError()) {
                    Log.i(TAG, "Error writing sensor event data", mContext);
                }else{
//                    Log.i(TAG, "Wrote sensor event data", mContext);
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


//    public class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void> {
//
//        private Context aContext;
//        public SensorEventLoggerTask(Context context){
//            aContext = context;
//        }
//
//        @Override
//        protected Void doInBackground(SensorEvent... events) {
//            SensorEvent event = events[0];
//            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
//            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
//            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
//
//            Date dateNow = new Date();
//            String timestampStringNow = new SimpleDateFormat(mHealthTimestampFormat).format(dateNow);
//
//            timeInMillis = (new Date()).getTime()
//                    + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L;
//
//            Date date = new Date(timeInMillis);
//            String timestampString = new SimpleDateFormat(mHealthTimestampFormat).format(date);
//
//            String[] accEntry = {
//                    timestampStringNow,
//                    timestampString,
//                    Float.toString(event.values[0]- gravity[0]),
//                    Float.toString(event.values[1]- gravity[1]),
//                    Float.toString(event.values[2]- gravity[2])
//            };
//
//            String dataDirectory = DataManager.getDirectoryData(aContext);
//            String accFile = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Acceleration.csv";
//            CSV.write(accEntry, accFile, true);
//
//
//            return null;
//        }
//
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

