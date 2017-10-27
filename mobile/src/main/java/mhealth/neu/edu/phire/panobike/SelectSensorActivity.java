package mhealth.neu.edu.phire.panobike;


import android.app.AlarmManager;
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
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.activities.SetupActivity;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;



public class SelectSensorActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = SelectSensorActivity.class.getSimpleName();
    public static final String SENSOR_ID = "sensor_id";
    private static final long SCAN_PERIOD = 30000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLEScanner;
    private ScanCallback mScanCallback;
    private TextView currentSensorView;
    private Button scanButton;
    private Button doneButton;
    private boolean mScanning;
    private Handler mHandler;
    private ScanFilter cscFilter;
    private ArrayList<ScanFilter> filters;
    private ArrayList<String> sensorAddressList;
    private ArrayList<BluetoothDevice> sensorList;
    private ScanSettings settings;
    private String currentSensor;
    private BluetoothDevice currentSensorDevice;
    private BluetoothDevice mBluetoothDevice;
    private String sensorID;
    private int diameterCm;
//    private BluetoothGatt mBluetoothGatt;


    private SharedPreferences prefs;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sensor);
        mContext = getApplicationContext();
        mHandler = new Handler();

        sensorAddressList= new ArrayList<String>();
        sensorList = new ArrayList<BluetoothDevice>();

        currentSensorView = (TextView) findViewById(R.id.current_sensor);
        scanButton = (Button) findViewById(R.id.button_scan_sensor);
        doneButton = (Button) findViewById(R.id.done);

//        prefs = getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE);
//        diameterCm = prefs.getInt(SelectDiameterActivity.DIAMETER_CM,0);

//        sensorID = prefs.getString(SENSOR_ID, null);
        sensorID = TEMPLEDataManager.getPanoBikeSensorId(mContext);
        if(sensorID!= null){
            currentSensorView.setText(sensorID);
        }

    }
    @Override
    public void onClick(final View view) {
        if(view == findViewById(R.id.done)){
            Log.i(TAG,"pressed done button");
            Intent intent = new Intent(mContext, SetupActivity.class);
            startActivity(intent);
        }
        if (view == findViewById(R.id.button_scan_sensor)) {
            Log.i(TAG,"pressed button to scan for devices");
//
//            BikeSensor.disConnect();
//            cancelAlarm();
            if(!sensorAddressList.isEmpty()){
                sensorAddressList.clear();
                sensorList.clear();
            }

            // check for ble on
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            mBluetoothAdapter = bluetoothManager.getAdapter();


            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Log.i(TAG,"This device does not support bluetooth.");
                return;

            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    // Bluetooth is not enable :)
                    Log.i(TAG,"Please enable bluetooth for the app to function properly.");
                    return;

                }
            }

            scanLeDevice(true);
//
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (mScanCallback == null) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                    mScanning = false;
                        Log.i(TAG, "IS IN THREAD");
                        mBluetoothLEScanner.stopScan(mScanCallback);
                        mScanCallback = null;
                        fillRadioGroup();
                        scanButton.setEnabled(true);
                    }
                }, SCAN_PERIOD);
//            mScanning = true;
                Log.i(TAG, "IS OUTSIDE THREAD");
//            cancelAlarm();

                cscFilter = new ScanFilter.Builder()
                        .setServiceUuid(MyTestService.CSC_SERVICE_UUID)
                        .build();

                filters = new ArrayList<ScanFilter>();
                filters.add(cscFilter);

                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();

                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        Log.i("Scan result", result.toString());
                        if(!sensorAddressList.contains(result.getDevice().getAddress())) {
                            sensorAddressList.add(result.getDevice().getAddress());
                            sensorList.add(result.getDevice());
                        }
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult result : results) {
                            Log.i("ScanResult - Results", result.toString());
                            if(!sensorAddressList.contains(result.getDevice().getAddress())) {
                                sensorAddressList.add(result.getDevice().getAddress());
                                sensorList.add(result.getDevice());
                            }
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.e("Scan Failed", "Error Code: " + errorCode);
                    }
                };

                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                mBluetoothLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

                mBluetoothLEScanner.startScan(filters, settings, mScanCallback);
                scanButton.setEnabled(false);

            }
        }
    }

    private void fillRadioGroup() {
        if (!sensorAddressList.isEmpty()) {
            Log.i(TAG, "LIST NOT EMPTY");
            RadioGroup sensorRadioGroup = (RadioGroup) findViewById(R.id.sensor_list);
            int radioButtonID = 0;
            for (int i = 0; i < sensorAddressList.size(); i++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setId(radioButtonID);
                currentSensor = sensorAddressList.get(i);
                currentSensorDevice = sensorList.get(i);
                radioButton.setText(currentSensor);
                radioButtonID++;
                sensorRadioGroup.addView(radioButton);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TEMPLEDataManager.setPanoBikeSensorId(mContext,currentSensor);
                        currentSensorView.setText(currentSensor);
                        ToastManager.showShortToast(mContext, "Selected PanoBike sensor id - " + currentSensor);

                        Calendar cs = Calendar.getInstance();
                        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        TEMPLEDataManager.setPanoBikeLastConnectionTime(mContext,dfs.format(cs.getTime()));

//                        BikeActivity bikeActivity = new BikeActivity(getApplicationContext(),currentSensorDevice ,diameterCm,sensorID);
//                        bikeActivity.startRead();

                    }

                });
            }
        }
    }

//    public void cancelAlarm(){
//        Log.i(TAG,"Cancelling alarm after device detection");
//        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
//        PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        manager.cancel(pIntent);
//    }
//
//    public void scheduleAlarm() {
//        // Construct an intent that will execute the AlarmReceiver
//        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
//        // Create a PendingIntent to be triggered when the alarm goes off
//        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
//        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
//                1000 * 59, pIntent);
//    }


}
