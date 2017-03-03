package mhealth.neu.edu.phire.panobike;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
//import android.util.//Log;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
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
    private double instSpeed, instCadence;
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
                    TEMPLEDataManager.setPanoBikeConnectionStatus(context,false);

//                    scheduleAlarm();

                    mSensor = null;
                } else if (newState == BikeSensor.ConnectionState.CONNECTED) {
                    TEMPLEDataManager.setPanoBikeConnectionStatus(context,true);
                    parent.hasSpeed = parent.mSensor.hasSpeed();
                    parent.hasSpeed = parent.mSensor.hasCadence();
                    //Log.i(TAG, "Connected to device",mContext);
                    parent.mSensor.setNotificationsEnabled(true);
                }
            }

            @Override
            public void onSpeedUpdate(BikeSensor sensor, double distance, double elapsedUs, long rot) {
                BikeActivity parent = BikeActivity.this;
                speedMessage = "";
                newSpeed = true;
                if (elapsedUs == 0) {
                    parent.instSpeed = 0.0;
                } else {
                    parent.instSpeed = distance / elapsedUs;
                }
                DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
                final String speed_dec = String.valueOf(oneDigit.format(parent.instSpeed));
                //Log.i(TAG, "Speed: " + " = " + speed_dec + " m/s" + " CumRotation: " + rot,mContext);


                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                speedMessage = df.format(c.getTime()) + ',' + sensorID + ',' + diameter+ ',' + rot;

                String[] speedEntry = {
                        df.format(c.getTime()),
                        String.valueOf(rot)
                };
                String dataDirectory = DataManager.getDirectoryData(context);
                String speedFile = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Speed.csv";

                CSV.write(speedEntry, speedFile, true);
//                TEMPLEDataManager.setPanoBikeLastConnectionTime(mContext,df.format(c.getTime()));
            }

            @Override
            public void onCadenceUpdate(BikeSensor sensor, int rotations, double elapsedUs, int crankRot) {
                BikeActivity parent = BikeActivity.this;
                cadenceMessage ="";
                newCadence = true;
                if (elapsedUs == 0) {
                    parent.instCadence = 0.0;
                } else {
                    parent.instCadence = rotations * 60 / (elapsedUs);
                }
                DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
                final String cadence_dec = String.valueOf(oneDigit.format(parent.instCadence));
                //Log.i(TAG, "Cadence: " + " = " + cadence_dec + " rpm" + " CumCrankRotation: " + crankRot,mContext);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                cadenceMessage = df.format(c.getTime()) + ',' + sensorID +',' + diameter +','+ crankRot;

                String[] cadenceEntry = {
                        df.format(c.getTime()),
                        String.valueOf(crankRot)
                };
                String dataDirectory = DataManager.getDirectoryData(context);
                String cadenceFile = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "Cadence.csv";
                CSV.write(cadenceEntry, cadenceFile, true);
//                TEMPLEDataManager.setPanoBikeLastConnectionTime(mContext,df.format(c.getTime()));



            }
        };
    }


}
