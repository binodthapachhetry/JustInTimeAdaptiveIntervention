package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.Log;

import com.opencsv.CSVReader;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;


public class ActivityRecognitionService extends WocketsIntentService {

    private static final String TAG = "ActivityRecognitionService";
    public static final String dayFormat = "yyyy-MM-dd";
    public static final String hourFormat = "HH-z";
    private Context mContext;
    private Date dateNow;
    private BluetoothAdapter mBluetoothAdapter;
    private Classifier mClassifier;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);
        doAR();
    }

    private void doAR(){

        // check if bluetooth connected
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            ToastManager.showShortToast(mContext, "This device does not support bluetooth.");
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                ToastManager.showShortToast(mContext, "Please enable bluetooth for the app to function properly.");
                return;
            }
        }

//        Log.i(TAG, "Getting last run of activity recognition service", mContext);
//        long lastARwindow = DataManager.getLastARwindow(mContext);



        // based on the end time of the last window decide on a new time window
        Log.i(TAG, "Getting the watch feature row from feature csv file", mContext);
        // read csv file using scanner, parse for relevant content(time start, time stop, etc)
        dateNow = new Date();
        String dataDirectory = DataManager.getDirectoryLogs(mContext);
        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
        String hourDirectory = new SimpleDateFormat(hourFormat).format(dateNow);
        String csvFile = dataDirectory + "/" + dayDirectory + "/" + hourDirectory + "/" + "Watch-ComputedFeature.log.csv";

        File cFile = new File(csvFile);

        if(cFile.exists()) {

            CSVReader reader = null;
            try {
                reader = new CSVReader(new FileReader(csvFile));
                String[] line;
                while ((line = reader.readNext()) != null) {
                    Log.i(TAG, line[0], mContext);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            // load the model
            Log.i(TAG, "Loading model", mContext);
            AssetManager assetManager = mContext.getAssets();
            String modelPath = "train_model_smartwatch_all_smo.model";
            try {
                mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(modelPath));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                // Weka "catch'em all!"
                e.printStackTrace();
            }
            Log.i(TAG, "Model loaded", mContext);


        }











    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }


//    public float getDistance(){
//        //grab window of data from speed.csv
//        //sum distance vector and divide by time diff
//
//    }
//
//    public float getRMS(){
//
//    }
//
//    public float getMeanCross(){
//
//    }




}
