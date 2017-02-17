package edu.neu.android.wocketslib.mhealthformat.entities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by qutang on 4/23/15.
 */
public class AnnotationSet extends mHealthEntity{
    public final static String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,LABEL_NAME,LABEL_ID,RATING_TIME_STAMP,RATING_INTENSITY,RATING_CONFIDENCE";
    private String labelId;
    private String labelName;
    private Date startTime;
    private Date stopTime;
    private Date ratingTimestamp;
    private String ratingIntensity;
    private String ratingConfidence;

    public AnnotationSet(String annotationSet, String annotatorId, Date startTime, Date stopTime, String label, String labelID,
                         Date ratingTimestamp, String ratingIntensity,
                         String ratingConfidence){
        super();
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.labelName = label;
        this.labelId = labelID;
        this.ratingTimestamp = ratingTimestamp;
        this.ratingIntensity = ratingIntensity;
        this.ratingConfidence = ratingConfidence;
        section1 = annotationSet;
        section2 = annotatorId + "-" + annotationSet;
    }


    @Override
    public mHealthFormat.MHEALTH_FILE_TYPE getEntityType() {
        return mHealthFormat.MHEALTH_FILE_TYPE.ANNOTATION;
    }

    @Override
    public String tomHealthRow() {
        return tomHealthRow(startTime, stopTime);
    }

    private String tomHealthRow(Date startDate, Date endDate) {
        String ts = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(new Date());
        String annotationStr = ts + "," + new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(startDate) + ","
                + new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(endDate) + ","
                + labelName + "," + labelId + ","
                + new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(ratingTimestamp) + ","
                + ratingIntensity + ","
                + ratingConfidence;
        return annotationStr;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public byte[] encodeRowAsBinary() {
        return new byte[0];
    }


    /*
    *
    * Override the default function for annotation saving
    *
    * It supports at most one day annotation (No cross day for one annotation row)
    *
    * TODO: Add support for multiple-day or even multiple-month (unlikely) annotation splitting
    *
    * It will split the annotations into hours and save to proper hour folders
    *
    * */
    @Override
    public boolean writeTomHealthCsv(boolean isAppend){
        String startHour = new SimpleDateFormat(mHealthFormat.mHealthHourDirFormat).format(startTime);
        String endHour = new SimpleDateFormat(mHealthFormat.mHealthHourDirFormat).format(stopTime);
        int startH = Integer.parseInt(startHour);
        int endH = Integer.parseInt(endHour);

        boolean result = true;

        Calendar current = Calendar.getInstance();
        current.setTime(startTime);
        for(int currentH = startH; currentH <= endH; currentH++){
            String path = null;
            try {
                path = mHealthFormat.buildmHealthPath(current.getTime(), mHealthFormat.PATH_LEVEL.HOURLY, getEntityType());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            String filename = mHealthFormat.getmHealthFilenameForCurrentHour(current.getTime(), section1, section2, getEntityType(), false);
            String row;
            if(currentH != endH){
                Calendar temp = (Calendar) current.clone();
                temp.set(Calendar.MINUTE, 59);
                temp.set(Calendar.SECOND, 59);
                temp.set(Calendar.MILLISECOND, 999);
                row = tomHealthRow(current.getTime(), temp.getTime());
            }else{
                row = tomHealthRow(current.getTime(), stopTime);
            }
            result = result & saveCsv(path, filename, getHeader(), row, isAppend);
            current.add(Calendar.HOUR_OF_DAY, 1);
            current.set(Calendar.MINUTE, 0);
            current.set(Calendar.SECOND, 0);
            current.set(Calendar.MILLISECOND, 0);
        }
        return result;
    };
}
