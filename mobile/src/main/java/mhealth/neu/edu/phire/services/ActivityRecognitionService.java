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
import java.util.Enumeration;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.Log;

import com.opencsv.CSVReader;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.estimators.Estimator;


public class ActivityRecognitionService extends WocketsIntentService {

    private static final String TAG = "ActivityRecognitionService";
    private static final String TAGF = "ActivityRecognitionResult";
    public static final String dayFormat = "yyyy-MM-dd";
    public static final String hourFormat = "HH-z";
    private Context mContext;
    private Date dateNow;
    private BluetoothAdapter mBluetoothAdapter;
    private Classifier mClassifier;
    private Instances dataUnpredicted;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);

        try {
            doAR();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAR() throws Exception {

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

        Log.i(TAG, "Getting last run of activity recognition service", mContext);
        long lastARwindowStopTime = DataManager.getLastARwindowStopTime(mContext);


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

            // load the model
            Log.i(TAG, "Loading model", mContext);
            AssetManager assetManager = mContext.getAssets();
            String modelPath = "train_model_smartwatch_all_smo_binod.model";

            try {
                mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open(modelPath));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Model loaded", mContext);

            // order of attributes/classes needs to be exactly equal to those used for training
            final Attribute attributeFeatOne = new Attribute("feat_1");
            final Attribute attributeFeatTwo = new Attribute("feat_2");
            final List<String> classes = new ArrayList<String>() {
                {
                    add("class_1"); // cls nr 1
                    add("class_2"); // cls nr 2
                    add("class_3"); // cls nr 3
                }
            };
            final Attribute attributeClass = new Attribute("class",classes);
            ArrayList<Attribute> attributeList = new ArrayList<Attribute>(3) {
                {
                    add(attributeFeatOne);
                    add(attributeFeatTwo);
                    add(attributeClass);
                }
            };
            Instances dataUnpredicted = new Instances("test", attributeList, 0);
            dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);

            // read csv
            CSVReader reader = null;
            try {
                reader = new CSVReader(new FileReader(csvFile));
                String[] line;
                while ((line = reader.readNext()) != null) {
                    final String[] lineCp = line;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");

                    Date startDate = simpleDateFormat.parse(lineCp[2]);
                    long startMilliseconds = startDate.getTime();

                    Date stopDate = simpleDateFormat.parse(lineCp[3]);
                    long stopMilliseconds = stopDate.getTime();

                    if(startMilliseconds>lastARwindowStopTime) {
                        DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
                            {
                                setValue(attributeFeatOne, Double.parseDouble(lineCp[5]));
                                setValue(attributeFeatTwo, Double.parseDouble(lineCp[9]));
                            }
                        };
                        newInstance.setDataset(dataUnpredicted);
                        try {
                            double result = mClassifier.classifyInstance(newInstance);
                            String className = classes.get(new Double(result).intValue());
//                            String[] accEntry = {
//                                    lineCp[2],
//                                    lineCp[3],
//                                    className
//                            };
                            String row =String.format("%s,%s,%s",lineCp[2],lineCp[3],className);
                            Log.i(TAGF,row,mContext);
//                            String dataDir = DataManager.getDirectoryData(mContext);
//                            String dayDir = new SimpleDateFormat(dayFormat).format(dateNow);
//                            String hourDir = new SimpleDateFormat(hourFormat).format(dateNow);
//                            String arFile = dataDir + "/" + dayDir + "/" + hourDir + "/" + "ActivityRecognitionPrediction.csv";
//                            CSV.write(accEntry, arFile, true);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DataManager.setLastARwindowStopTime(mContext,stopMilliseconds);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

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
