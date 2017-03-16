package mhealth.neu.edu.phire.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;
import mhealth.neu.edu.phire.panobike.MyAlarmReceiver;


/**
 * @author jarvis
 */
public class PanobikeSensorService extends WocketsIntentService {

    private static final String TAG = "PanobikeSensorService";

    private Context mContext;

    private static final long PANOBIKE_ATTEMPT_INTERVAL = 30 * 1000; // 3600

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
        Log.i(TAG,"Wheel diameter(cm): " + wheelDiameterCm, mContext);

        if(wheelDiameterCm == null || wheelDiameterCm.isEmpty()){
            Log.i(TAG,"Wheel diameter not selected. Select one using setupactivity menu", mContext);
            return;
        }

        String lastConnectedTime = String.valueOf(TEMPLEDataManager.getPanoBikeLastConnectionTime(mContext));
        Log.i(TAG,"Last connected time:" + lastConnectedTime, mContext);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date lastDate = simpleDateFormat.parse(lastConnectedTime);
            if(System.currentTimeMillis() - lastDate.getTime()<PANOBIKE_ATTEMPT_INTERVAL){
                Log.i(TAG,"Panobike sensor might still be in connection!", mContext);
                return;
            }
        } catch (ParseException e) {
            Log.i(TAG,"Error while converting string to datetime" + e, mContext);
            e.printStackTrace();
            return;
        }

        Log.i(TAG,"Panobike not connected, so trying to connect", mContext);





//        Log.i(TAG,String.valueOf(TEMPLEDataManager.getPanoBikeConnectionStatus(mContext)), mContext);
//
//        if(TEMPLEDataManager.getPanoBikeConnectionStatus(mContext)){
//            Log.i(TAG,"Panobike sensor already in connection!", mContext);
//            return;
//        }
//
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            // Perform the operation associated with our pendingIntent
            Log.i(TAG,"Intent sent to connect/read panobike sensor", mContext);
            pIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG,"Intent to connect/read panobike sensor failed", mContext);
            e.printStackTrace();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside onDestroy", mContext);
    }
}