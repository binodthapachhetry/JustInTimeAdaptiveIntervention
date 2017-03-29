package mhealth.neu.edu.phire.panobike;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyTestService extends IntentService {

    public static final String TAG = MyTestService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeScanner mBluetoothLEScanner;
    private ScanCallback mScanCallback;
    private String sensorID;
    private int diameterCM;

    public static final ParcelUuid CSC_SERVICE_UUID = ParcelUuid.fromString(TEMPLEConstants.CSC_SERVICE_UUID);


    public MyTestService() {
        super("MyTestService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

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
                Log.i(TAG,"Scan result: " + result.toString());

                if(result.getDevice().getAddress().equals(sensorID)) {
                    Log.i(TAG,"Matching Scan result found");
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
//
                        BikeActivity bikeActivity = new BikeActivity(getApplicationContext(), result.getDevice(),diameterCM,sensorID);
                        bikeActivity.startRead();
                        break;
                    }

                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG,"Scan Failed, Error Code: " + errorCode);
                // DISABLE AND ENABLE BLUETOOTH PROGRAMATICALLY
                mBluetoothAdapter.disable();
                Boolean disabled = true;

                if(disabled){
                    Log.d(TAG, "bluetooth adapter turned off");
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "bluetooth adapter try to enable");
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

        mBluetoothLEScanner.startScan(filters, settings, mScanCallback);
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
//        batteryLevel();

    }

//    public void cancelAlarm(){
//        Log.i(TAG,"Cancelling alarm after device detection");
//        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
//        PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        manager.cancel(pIntent);
//    }



}
