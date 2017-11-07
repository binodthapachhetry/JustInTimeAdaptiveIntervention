package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.NotificationManager;
import edu.neu.mhealth.android.wockets.library.managers.VibrationManager;
import edu.neu.mhealth.android.wockets.library.services.WocketsIntentService;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;
import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

public class JustInTimeFeedbackService extends WocketsIntentService {

    private static final String TAG = "JITFeedbackService";
    private static final String dayFormat = "yyyy-MM-dd";
    private Context mContext;
    private long currentMilliseconds;
    private long lastARwindowStopTime;
    private int paMinutes;
    private int goalPAminutes;
    private int TotalPAminutes;
    private int dailyPAboutLengthGoal;

    private Double partWeightKg;
    private Double partSciLevel;
    private Double partMETmultiply;
    private HashMap<String,Double> mapSciLevel;

    String[] note = new String[] {"Good job!", "Wow!", "Keep it up!","Amazing!","Congratulations!","Wonderful!","Excellent","Awesome!","Good going!","Nice work!"};



    public JustInTimeFeedbackService() {
        super("JustInTimeFeedbackService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.i(TAG, "Inside onCreate", mContext);

        if(!TEMPLEDataManager.getThirdPhaseActive(mContext)){
            Log.i(TAG, "Phase three is locked and not active", mContext);
            return;
        }
        try {
            giveFeedback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void giveFeedback() throws IOException, ClassNotFoundException {

        // last AR time within 3 minutes of current time
        currentMilliseconds = System.currentTimeMillis();
        lastARwindowStopTime = DataManager.getLastARwindowStopTime(mContext);
        if(currentMilliseconds> lastARwindowStopTime +1800000l){
            Log.i(TAG, "Last PA>= 3 METs detected 3 minutes ago. This should not be from real time data", mContext);
            return;
        }

//        mapSciLevel = new HashMap<String,Double>();
//        mapSciLevel.put("paraplagia",2.77d);
//        mapSciLevel.put("tetraplagia",2.52d);
//
//        if(TEMPLEDataManager.getParticipantWeight(mContext)!="" && TEMPLEDataManager.getParticipantSciLevel(mContext)!=""){
//            partWeightKg = (Double.parseDouble(TEMPLEDataManager.getParticipantWeight(mContext))* TEMPLEConstants.LB_KG_CONVERT)/TEMPLEConstants.MET_DIVIDE;
//            Log.i(TAG,"Participant weight in lbs is :"+ TEMPLEDataManager.getParticipantWeight(mContext),mContext);
//
//            partSciLevel = mapSciLevel.get(TEMPLEDataManager.getParticipantSciLevel(mContext));
//            Log.i(TAG,"Participant sci level is :"+ TEMPLEDataManager.getParticipantSciLevel(mContext),mContext);
//
//            partMETmultiply = partWeightKg*partSciLevel;
//        }else{
//            Log.i(TAG,"Patient information to calculate energy expenditure not found",mContext);
//            partMETmultiply = 0d;
//        }
//
//        if(partMETmultiply==0d){
//            Log.i(TAG, "Participant's weight or sci level is not selected yet.", mContext);
//        }else {

            if (TEMPLEDataManager.getLastPAtime(mContext) == -1l) {
                TEMPLEDataManager.setLastPAtime(mContext, lastARwindowStopTime);
                TEMPLEDataManager.setPAMinutes(mContext, 1);
            } else {
                SimpleDateFormat simpleDateFormatPano = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String lastARtime = simpleDateFormatPano.format(lastARwindowStopTime);
                String lastPAtime = simpleDateFormatPano.format(TEMPLEDataManager.getLastPAtime(mContext));
                Log.i(TAG, "Last AR time:" + lastARtime + ", last PA recorded time:" + lastPAtime, mContext);
                if (lastARwindowStopTime > TEMPLEDataManager.getLastPAtime(mContext) + 180000l) {
                    Log.i(TAG, "Last PA>= 3 METs happened more than 1 minute ago, so starting counter again", mContext);
                    TEMPLEDataManager.setPAMinutes(mContext, 1);
                } else {
                    TotalPAminutes = TEMPLEDataManager.getWatchPAminutes(mContext)+TEMPLEDataManager.getPanoPAminutes(mContext)+TEMPLEDataManager.getBothPAminutes(mContext)+1;
//                    TotalPAminutes = getTotalPAminutes();
                    paMinutes = TEMPLEDataManager.getPAminutes(mContext);
                    goalPAminutes = TEMPLEDataManager.getPAminutesGoal(mContext);
                    dailyPAboutLengthGoal = TEMPLEDataManager.getDailyPAboutLengthGoal(mContext);
                    Log.i(TAG, "Daily goal bout lenth(in minutes) set to:"+Integer.toString(dailyPAboutLengthGoal), mContext);
                    Log.i(TAG, "Total PA minutes so far:"+Integer.toString(TotalPAminutes), mContext);
                    Log.i(TAG, "Goal PA minutes:"+Integer.toString(goalPAminutes), mContext);

                    if (paMinutes >= dailyPAboutLengthGoal) {

                        int randomNum = ThreadLocalRandom.current().nextInt(0, 9 + 1);

                        if (TotalPAminutes < goalPAminutes) {
                            Integer remain = goalPAminutes - TotalPAminutes;
                            String msg = Integer.toString(TotalPAminutes) + " mins completed,\n" + Integer.toString(remain) + " mins remaining to complete daily goal.";
//                            String ttl = "Physical Activity summary:";
                            String ttl = note[randomNum];
                            congratulate(ttl,msg);
                            Log.i(TAG,msg, mContext);
                        } else if(TotalPAminutes == goalPAminutes){
                            String msg = "You completed today's recommended daily goal.";
//                            String ttl = "Congratulations!";
                            String ttl = note[randomNum];
                            congratulateGoal(ttl,msg);
                            Log.i(TAG, msg, mContext);
                        }
                        else {
                            Integer remain = TotalPAminutes - goalPAminutes;
                            String msg = Integer.toString(remain) + " mins beyond recommended daily goal.";
//                            String ttl = "Congratulations!";
                            String ttl = note[randomNum];
                            congratulate(ttl,msg);
                            Log.i(TAG, msg, mContext);
                        }
                    } else if (paMinutes < dailyPAboutLengthGoal) {
                        Log.i(TAG, "PA minutes less than target bout length", mContext);
                    }
                    TEMPLEDataManager.setPAMinutes(mContext, paMinutes + 1);
                }
                TEMPLEDataManager.setLastPAtime(mContext, lastARwindowStopTime);


            }
        }
//    }

//    private int getTotalPAminutes() throws IOException, ClassNotFoundException {
//
//
//        if(TEMPLEDataManager.getParticipantWeight(mContext)!="" && TEMPLEDataManager.getParticipantSciLevel(mContext)!=""){
//            partWeightKg = (Double.parseDouble(TEMPLEDataManager.getParticipantWeight(mContext))* TEMPLEConstants.LB_KG_CONVERT)/TEMPLEConstants.MET_DIVIDE;
//            Log.i(TAG,"Participant weight in lbs is :"+ TEMPLEDataManager.getParticipantWeight(mContext),mContext);
//
//            partSciLevel = mapSciLevel.get(TEMPLEDataManager.getParticipantSciLevel(mContext));
//            Log.i(TAG,"Participant sci level is :"+ TEMPLEDataManager.getParticipantSciLevel(mContext),mContext);
//
//            partMETmultiply = partWeightKg*partSciLevel;
//        }else{
//            Log.i(TAG,"Patient information to calculate energy expenditure not found",mContext);
//            partMETmultiply = 0d;
//        }
//
//        NavigableMap<Date,Double> EEboth;
//        NavigableMap<Date,Double> EEpano;
//        NavigableMap<Date,Double> EEwatch;
//        Integer sizeBoth;
//        Integer sizePano;
//        Integer sizeWatch;
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date dateNow = new Date();
//
//        String featureDirectory = DataManager.getDirectoryFeature(mContext);
//        String dayDirectory = new SimpleDateFormat(dayFormat).format(dateNow);
//
//        String eeBothFile = featureDirectory + "/" + dayDirectory + "/" + "eeBoth.csv";
//        File eeBfile = new File(eeBothFile);
//        if (eeBfile.exists()) {
//            FileInputStream fileInputStream = new FileInputStream(eeBothFile);
//            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//            EEboth = (NavigableMap<Date, Double>) objectInputStream.readObject();
//            objectInputStream.close();
//            NavigableMap<Date, Double> EEbothFilt = new TreeMap<Date, Double>();
//
//            for (Map.Entry<Date, Double> entry : EEboth.entrySet()) {
//                if (entry.getValue()> (3d/partMETmultiply)) {
//                    EEbothFilt.put(entry.getKey(), entry.getValue());
//                }
//            }
//            sizeBoth = EEbothFilt.size();
//        }else{
//            sizeBoth = 0;
//        }
//        String eePanoFile = featureDirectory + "/" + dayDirectory + "/" + "eePano.csv";
//        File eePfile = new File(eePanoFile);
//        if (eePfile.exists()) {
//            FileInputStream fileInputStream = new FileInputStream(eePanoFile);
//            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//            EEpano = (NavigableMap<Date, Double>) objectInputStream.readObject();
//            objectInputStream.close();
//            NavigableMap<Date, Double> EEpanoFilt = new TreeMap<Date, Double>();
//            for (Map.Entry<Date, Double> entry : EEpano.entrySet()) {
//                if (entry.getValue()> (3d/partMETmultiply)) {
//                    EEpanoFilt.put(entry.getKey(), entry.getValue());
//                }
//            }
//            sizePano = EEpanoFilt.size();
//        }else{
//            sizePano = 0;
//        }
//
//
//        String eeWatchFile = featureDirectory + "/" + dayDirectory + "/" + "eeWatch.csv";
//        File eeWfile = new File(eeWatchFile);
//        if (eeWfile.exists()) {
//            FileInputStream fileInputStream = new FileInputStream(eeWatchFile);
//            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//            EEwatch = (NavigableMap<Date, Double>) objectInputStream.readObject();
//            objectInputStream.close();
//            NavigableMap<Date, Double> EEwatchFilt = new TreeMap<Date, Double>();
//            for (Map.Entry<Date, Double> entry : EEwatch.entrySet()) {
//                if (entry.getValue()> (3d/partMETmultiply)) {
//                    EEwatchFilt.put(entry.getKey(), entry.getValue());
//                }
//            }
//            sizeWatch = EEwatchFilt.size();
//        }else{
//            sizeWatch = 0;
//        }
//
//        return sizeBoth+sizePano+sizeWatch;
//    }
//
//    private void acknowledge() {
//        NotificationManager.showFeedbackNotification(
//                mContext,
//                TEMPLEConstants.STUDY_NAME,
//                "Physical activity detected in last minute",
//                R.mipmap.temple,
//                Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio1),
//                VibrationManager.VIBRATION_PATTERN_NONE
//        );
//    }

    private void congratulate(String title, String text) {
        NotificationManager.showFeedbackNotification(
                mContext,
                title,
                text,
                R.mipmap.temple,
                Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio2),
                VibrationManager.VIBRATION_PATTERN_CONGRATULATORY
                );
    }

    private void congratulateGoal(String title, String text) {
        NotificationManager.showFeedbackNotification(
                mContext,
                title,
                text,
                R.mipmap.temple,
                Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio2),
                VibrationManager.VIBRATION_PATTERN_BASIC
        );
    }

}
