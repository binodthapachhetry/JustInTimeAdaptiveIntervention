package mhealth.neu.edu.phire.panobike;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.util.//Log;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import mhealth.neu.edu.phire.services.PanobikeSensorService;
//import edu.neu.mhealth.android.wockets.library.support.//Log;

/**
 * Created by jarvis on 7/21/16.
 */
public class BikeActivity {

    private static final String TAG = BikeActivity.class.getSimpleName();
    private BluetoothDevice mBluetoothDevice;
    private BikeSensor.Callback mCallback;
    private BikeSensor mSensor;
    private boolean hasSpeed, hasCadence;
    private double instSpeed, instCadence, instDistance;
    private boolean newSpeed = false;
    private boolean newCadence = false;
    private static String speedMessage,cadenceMessage;
    private Context context;
    private int diameter;
    private String sensorID;
    public static BluetoothDevice device;

    private Context mContext;


    public BikeActivity(Context context, BluetoothDevice bluetoothDevice,Integer diameter, String sensorID){
        this.context = context;
        this.mBluetoothDevice = bluetoothDevice;
        this.diameter = diameter;
        this.sensorID = sensorID;
        context = context;
        runthis();

    }

    public void startRead(){
        if (mBluetoothDevice == null) {
            return;
        }

        if (mSensor == null)
            mSensor = new BikeSensor(this.context, mBluetoothDevice, this.diameter, mCallback);
    }

//    public void scheduleAlarm() {
//        // Construct an intent that will execute the AlarmReceiver
//        Intent intent = new Intent(this.context, MyAlarmReceiver.class);
//        // Create a PendingIntent to be triggered when the alarm goes off
//        final PendingIntent pIntent = PendingIntent.getBroadcast(this.context, MyAlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarm = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
//        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
//        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
//                1000 * 59, pIntent);
//    }

    private void runthis(){

        mCallback = new BikeSensor.Callback() {
            @Override
            public void onConnectionStateChange(BikeSensor sensor, BikeSensor.ConnectionState newState) {
                BikeActivity parent = BikeActivity.this;
                if (newState == BikeSensor.ConnectionState.ERROR) {
                    //Log.i(TAG, "Error in connection or lost connection!",context);
                    // need to start Alarm activity
                    // tell sharedpref that panobike connection is lost
//                    TEMPLEDataManager.setPanoBikeConnectionStatus(context,false);

//                    scheduleAlarm();

                    mSensor = null;
                } else if (newState == BikeSensor.ConnectionState.CONNECTED) {
//                    TEMPLEDataManager.setPanoBikeConnectionStatus(context,true);
                    parent.hasSpeed = parent.mSensor.hasSpeed();
                    parent.hasSpeed = parent.mSensor.hasCadence();
                    //Log.i(TAG, "Connected to device",mContext);
                    parent.mSensor.setNotificationsEnabled(true);
                }
            }

            @Override
            public void onSpeedUpdate(BikeSensor sensor, double distance, double elapsedUs, long roti) {
                BikeActivity parent = BikeActivity.this;
                speedMessage = "";
                int rot = (int) roti;

                newSpeed = true;
                if (elapsedUs == 0) {
                    parent.instSpeed = 0.0;
                    parent.instDistance = 0.0;
                } else {
                    parent.instSpeed = distance / elapsedUs;
                    parent.instDistance = distance;
                }

                if(rot!=0) {
//                    DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
//                    final String speed_dec = String.valueOf(oneDigit.format(parent.instSpeed));
//                    final String distance_dec = String.valueOf(oneDigit.format(parent.instDistance));

                    Calendar cs = Calendar.getInstance();
                    // check with last data collected time, need shared preference for this
                    // if rot is > prev rot + 15(??), divide the rot diff equally between the time difference


                    SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                    String[] speedEntry = {
                            dfs.format(cs.getTime()),
                            String.valueOf(rot),
                    };
                    String dataDirectory = DataManager.getDirectoryData(context);
                    String featureDirectory = DataManager.getDirectoryFeature(context);
                    String speedFileHour = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Speed.csv";
                    String speedFileDay = featureDirectory + "/" + DateTime.getDate() + "/" + "SpeedDay.csv";


                    CSV.write(speedEntry, speedFileHour, true);
                    CSV.write(speedEntry,speedFileDay,true);

                    Log.i(TAG, "speed rotation"+String.valueOf(rot));

//                    TEMPLEDataManager.setLastSpeedRot(mContext,rot);
//                    TEMPLEDataManager.setSpeedLastReceivedTime(mContext,dfs.format(cs.getTime()));
                }
            }

            @Override
            public void onCadenceUpdate(BikeSensor sensor, int rotations, double elapsedUs, int crankRot){
                BikeActivity parent = BikeActivity.this;
                cadenceMessage ="";
                newCadence = true;
                if (elapsedUs == 0) {
                    parent.instCadence = 0.0;
                } else {
                    parent.instCadence = rotations * 60 / (elapsedUs);
                }

                if(crankRot!=0) {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date currentTime = c.getTime();
//                    long currentTimeLong = currentTime.getTime();
//                    if(TEMPLEDataManager.getCadenceLastReceivedTime(mContext)!= null) {
//                        String lastCadTime = TEMPLEDataManager.getCadenceLastReceivedTime(mContext);
//                        Date lastCadTimeDate = null;
//                        try {
//                            lastCadTimeDate = df.parse(lastCadTime);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        long lastCadTimeDateLong = lastCadTimeDate.getTime();
//
//                        int lastCadRot = TEMPLEDataManager.getLastCadenceRot(mContext);
//
//                        if (crankRot > lastCadRot + 10) {
//                            long diff = currentTime.getTime() - lastCadTimeDate.getTime();
//                            long diffSeconds = diff / 1000 % 60;
//
//                            double binsDouble = (double) diffSeconds / (double) 30;
//                            int bins = (int) Math.round(binsDouble);
//                            double rotAdd = (double) (crankRot - lastCadRot) / (double) bins;
//
//                            for (int i = 1; i < bins; i++) {
//                                long j = (long) i;
//                                double k = (double) i;
//                                double dist = ((double) lastCadRot) + (rotAdd * k);
//                                String[] cadenceEntry = {
//                                        df.format(lastCadTimeDateLong + (30L * j)),
//                                        String.valueOf(dist)
//                                };

//                                String dataDirectory = DataManager.getDirectoryData(context);
//                                String cadenceFileHour = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Cadence.csv";
//                                String featureDirectory = DataManager.getDirectoryFeature(context);
//                                String cadenceFileDay = featureDirectory + "/" + DateTime.getDate() + "/" + "CadenceDay.csv";
//
//                                CSV.write(cadenceEntry, cadenceFileHour, true);
//                                CSV.write(cadenceEntry, cadenceFileDay, true);
//
//
//                            }
//                        }
//                    }

                    String[] cadenceEntry = {
                            df.format(c.getTime()),
                            String.valueOf(crankRot),
                    };

                    String dataDirectory = DataManager.getDirectoryData(context);
                    String cadenceFileHour = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Cadence.csv";
                    String featureDirectory = DataManager.getDirectoryFeature(context);
                    String cadenceFileDay = featureDirectory + "/" + DateTime.getDate() + "/" + "CadenceDay.csv";


                    CSV.write(cadenceEntry, cadenceFileHour, true);
                    CSV.write(cadenceEntry, cadenceFileDay, true);

//                    TEMPLEDataManager.setLastCadenceRot(mContext,crankRot);
//                    TEMPLEDataManager.setCadenceLastReceivedTime(mContext,df.format(c.getTime()));
                }


            }
        };
    }


}
