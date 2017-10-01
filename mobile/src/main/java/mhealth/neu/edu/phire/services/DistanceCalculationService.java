package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;


public class DistanceCalculationService extends WocketsIntentService {

    private static final String TAG = "DistanceCalculationService";
    public static final String dayFormat = "yyyy-MM-dd";

    private Context mContext;
    private float wheelCircumference;
    private String distanceMeter;
    private Date dateNow;
    private String useForDistance;
    private File sFile;
    private NavigableMap<Long, Integer> map;
    private long thisMilliseconds;
    private Long lastTimeSpeed;
    private String distFile;
    private String distFileDay;
    private Long lastDistanceCalcTime;
    private Long startRot;
    private Long stopRot;
    private Boolean useFirst;

    public DistanceCalculationService() {
        super("DistanceCalculationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG,"INSIDE ONCREATE",mContext);
        try {
            calculateDistance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateDistance() throws ParseException {
        SimpleDateFormat simpleDateFormatS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        lastDistanceCalcTime = TEMPLEDataManager.getLastDistanceCalcTime(mContext);
        Log.i(TAG,"Last distance calcaulated at:"+simpleDateFormatS.format(lastDistanceCalcTime),mContext);
        useFirst = false;

        if(lastDistanceCalcTime!=0){
            if(lastDistanceCalcTime==-1){
                useFirst = true;
            }else {
                Date dateDistanceCalcLastRun = new Date();
                dateDistanceCalcLastRun.setTime(lastDistanceCalcTime);
                Calendar calDistanceCalcLastRun = Calendar.getInstance();
                calDistanceCalcLastRun.setTime(dateDistanceCalcLastRun);
                int dayOfMonthDistanceCalcLastRun = calDistanceCalcLastRun.get(Calendar.DAY_OF_MONTH);

                Date dateCurrent = new Date();
                dateCurrent.setTime(DateTime.getCurrentTimeInMillis());
                Calendar calCurrent = Calendar.getInstance();
                calCurrent.setTime(dateCurrent);
                int dayOfMonthCurrent = calCurrent.get(Calendar.DAY_OF_MONTH);

                if (dayOfMonthCurrent > dayOfMonthDistanceCalcLastRun) {
                    TEMPLEDataManager.setDistanceTravelledMeter(mContext, "0");
                    useFirst = true;
                }
            }
        }

        if(TEMPLEDataManager.getWheelDiameterCm(mContext)!="") {
            wheelCircumference = ((Integer.valueOf(TEMPLEDataManager.getWheelDiameterCm(mContext)) * ((float) Math.PI))) * 0.0254f;
        }else{
            wheelCircumference = 0.0f;
        }
        Log.i(TAG, "Wheel circumference:" + Float.toString(wheelCircumference), mContext);

        distanceMeter = TEMPLEDataManager.getDistanceTravelledMeter(mContext);
        if(distanceMeter.startsWith("-")){
            distanceMeter = "0";
        }
        Log.i(TAG,"Last recorded distance travelled in meter = " + distanceMeter,mContext);

        dateNow = new Date();
        // based on the end time of the last window decide on a new time window
        Log.i(TAG, "Reading speed file for today", mContext);
        String featureDirectory = DataManager.getDirectoryFeature(mContext);
        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);

        distFile = DataManager.getDirectoryData(mContext) + "/" + dayDirectory + "/" + DateTime.getCurrentHourWithTimezone()+ "/" + "DistanceTravelledInMiles.log.csv";
        distFileDay = DataManager.getDirectoryFeature(mContext) + "/" + dayDirectory + "/" + "DistanceTravelledInMiles.log.csv";

        useForDistance = TEMPLEDataManager.getDistanceCalculation(mContext);
        if(useForDistance==null){
            useForDistance = "Speed";
            TEMPLEDataManager.setDistanceCalculation(mContext,"Speed");
        }
        Log.i(TAG, "Using reading from " + useForDistance + " to calculate speed", mContext);


        String speedFile = featureDirectory + "/" + dayDirectory + "/" + useForDistance+"Day.csv";
        sFile = new File(speedFile);
        if (sFile.exists()) {
            Log.i(TAG, "Reading speed file into tree map.", mContext);
            map = new TreeMap<Long, Integer>();
            CSVReader reader = null;
            try {
                reader = new CSVReader(new FileReader(sFile));
                String[] line;
                while ((line = reader.readNext()) != null) {
                    final String[] lineCp = line;
                    SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date thisDate = simpleDateFormatPano.parse(lineCp[0]);
                    thisMilliseconds = thisDate.getTime();
                    Integer thisRot = Integer.parseInt(lineCp[1]);
                    map.put(thisDate.getTime(), thisRot);
                }
                lastTimeSpeed = thisMilliseconds;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"First time in speed file:" + simpleDateFormatS.format(map.firstKey()),mContext);
            Log.i(TAG,"Last time in speed file:" + simpleDateFormatS.format(map.lastKey()),mContext);

//            Long startRot;
            if(useFirst){
                Log.i(TAG,"Inside useFirst",mContext);
                startRot = map.firstKey();
                useFirst = false;

            }else {
                startRot = map.floorKey(lastDistanceCalcTime);
            }

//            startRot = map.floorKey(lastDistanceCalcTime);

//            Long startRot = map.floorKey(lastDistanceCalcTime);
            stopRot = map.lastKey();
            Log.i(TAG,"Start rot:" + Long.toString(startRot),mContext);

            Log.i(TAG,"Start rotation time:" + simpleDateFormatS.format(startRot),mContext);
            Log.i(TAG,"Stop rotation time:" + simpleDateFormatS.format(stopRot),mContext);

//            if(startRot==null){
//                Log.i(TAG,"START KEY IS NULL",mContext);
//            }
//            if(stopRot == null){
//                Log.i(TAG,"STOP ROT IS NULL",mContext);
//            }

//            if(startRot==null || stopRot==null) {
//                Log.i(TAG, "Issue with reading start and stop time in panobike file.", mContext);
//                return;
//            } else
            if(stopRot.compareTo(startRot) <0) {
                Log.i(TAG, "Start and stop panobike read time is same.", mContext);
                return;
//            } else if(stopRot==stopRot) {
//                Log.i(TAG, "No new speed recorded after last AR instance.", mContext);
//                return;
            }else{
                float distance = (map.get(stopRot)-map.get(startRot))*wheelCircumference;
                Log.i(TAG, "Distance travelled at this cycle:"+Float.toString(distance), mContext);
                if(distance==0L){
                    Log.i(TAG, "No new speed recorded after last AR instance.", mContext);
                    return;
                }
                float totalDistance = Float.valueOf(distanceMeter)+distance;

                TEMPLEDataManager.setDistanceTravelledMeter(mContext,String.valueOf(totalDistance));
                TEMPLEDataManager.setLastDistanceCalcTime(mContext,stopRot);
                SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String stopTime = simpleDateFormatPano.format(stopRot);
                String[] row = {
                        stopTime,
                        String.valueOf(totalDistance),
                };
                CSV.write(row, distFile, true);
                CSV.write(row,distFileDay,true);
            }
        }else{
            Log.i(TAG, "No speed file for the day", mContext);
            return;
        }



    }

}
