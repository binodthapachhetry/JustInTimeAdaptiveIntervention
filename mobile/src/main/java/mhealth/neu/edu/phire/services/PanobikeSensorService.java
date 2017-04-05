package mhealth.neu.edu.phire.services;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import mhealth.neu.edu.phire.panobike.BikeActivity;
import mhealth.neu.edu.phire.panobike.MyAlarmReceiver;


/**
 * @author jarvis
 */
public class PanobikeSensorService extends WocketsIntentService {

    private static final String TAG = "PanobikeSensorService";

    private Context mContext;

    private static final long PANOBIKE_ATTEMPT_INTERVAL = 30 * 1000; // 3600

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeScanner mBluetoothLEScanner;
    private ScanCallback mScanCallback;
    private String sensorID;
    private int diameterCM;
    public static final ParcelUuid CSC_SERVICE_UUID = ParcelUuid.fromString(TEMPLEConstants.CSC_SERVICE_UUID);


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);
        startCollectingPanobikeData();
    }

    private void startCollectingPanobikeData() {

        String panobikeID = TEMPLEDataManager.getPanoBikeSensorId(mContext);
        Log.i(TAG,"Panobike ID: " + panobikeID, mContext);

        if(panobikeID == null || panobikeID.isEmpty()){
            Log.i(TAG,"No PanoBike Sensor selected. Select one using setupactivity menu", mContext);
            return;
        }

        String wheelDiameterCm = TEMPLEDataManager.getWheelDiameterCm(mContext);
        Log.i(TAG,"Wheel diameter(inch): " + wheelDiameterCm, mContext);

        if(wheelDiameterCm == null || wheelDiameterCm.isEmpty()){
            Log.i(TAG,"Wheel diameter not selected. Select one using setupactivity menu", mContext);
            return;
        }

//        String lastConnectedTime = String.valueOf(TEMPLEDataManager.getPanoBikeLastConnectionTime(mContext));
//        Log.i(TAG,"Last connected time:" + lastConnectedTime, mContext);
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        try {
//            Date lastDate = simpleDateFormat.parse(lastConnectedTime);
//            if(System.currentTimeMillis() - lastDate.getTime()<PANOBIKE_ATTEMPT_INTERVAL){
//                Log.i(TAG,"Panobike sensor might still be in connection!", mContext);
//                return;
//            }
//        } catch (ParseException e) {
//            Log.i(TAG,"Error while converting string to datetime" + e, mContext);
//            e.printStackTrace();
//            return;
//        }
//
//        Log.i(TAG,"Panobike not connected, so trying to connect", mContext);





//        Log.i(TAG,String.valueOf(TEMPLEDataManager.getPanoBikeConnectionStatus(mContext)), mContext);
//
//        if(TEMPLEDataManager.getPanoBikeConnectionStatus(mContext)){
//            Log.i(TAG,"Panobike sensor already in connection!", mContext);
//            return;
//        }
//
//        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
//        // Create a PendingIntent to be triggered when the alarm goes off
//        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        try {
//            // Perform the operation associated with our pendingIntent
//            Log.i(TAG,"Intent sent to connect/read panobike sensor", mContext);
//            pIntent.send();
//        } catch (PendingIntent.CanceledException e) {
//            Log.e(TAG,"Intent to connect/read panobike sensor failed", mContext);
//            e.printStackTrace();
//        }

        // scanning for ble panobike sensor
        scanForPanobike();


    }

    private void scanForPanobike(){
        sensorID = TEMPLEDataManager.getPanoBikeSensorId(getApplicationContext());
        diameterCM = Integer.parseInt(TEMPLEDataManager.getWheelDiameterCm(getApplicationContext()));

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                android.util.Log.i(TAG,"Scan result: " + result.toString());

                if(result.getDevice().getAddress().equals(sensorID)) {
                    Log.i(TAG,"Matching Scan result found",mContext);
                    mBluetoothLEScanner.stopScan(mScanCallback);
                    mScanCallback = null;
//                    cancelAlarm();
                    BikeActivity bikeActivity = new BikeActivity(getApplicationContext(), result.getDevice(),diameterCM,sensorID);
                    bikeActivity.startRead();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {

                    if(result.getDevice().getAddress().equals(sensorID)) {
                        mBluetoothLEScanner.stopScan(mScanCallback);
                        mScanCallback = null;
//                        cancelAlarm();
                        Log.i(TAG,"Scan successful. Starting to read.",mContext);
//
                        BikeActivity bikeActivity = new BikeActivity(getApplicationContext(), result.getDevice(),diameterCM,sensorID);
                        bikeActivity.startRead();
                        break;
                    }

                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG,"Scan Failed, Error Code: " + errorCode,mContext);
                // DISABLE AND ENABLE BLUETOOTH PROGRAMATICALLY
                mBluetoothAdapter.disable();
                Boolean disabled = true;

                if(disabled){
                    Log.e(TAG, "bluetooth adapter turned off",mContext);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "bluetooth adapter try to enable",mContext);
                            mBluetoothAdapter.enable();
                        }}, 500);
                }

            }

        };

        //Scan for devices advertising the cadence and speed service
        ScanFilter cscFilter = new ScanFilter.Builder()
                .setServiceUuid(CSC_SERVICE_UUID)
                .build();

        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(cscFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        Log.i(TAG,"Starting scan.",mContext);
        mBluetoothLEScanner.startScan(filters, settings, mScanCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }
}