package edu.neu.android.wocketslib.mhealthformat.entities;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;

/**
 * Created by qutang on 4/24/15.
 */
public class AndroidPhoneGPSLocation extends mHealthEntity {
    private static final String header = "HEADER_TIME_STAMP,LATITUDE,LONGITUDE,ACCURACY";
    public static final String SENSOR_TYPE = "AndroidPhone";
    public static final String DATA_TYPE = "Location";
    public static final String VERSION_INFO = "NA";
    private double latitude;
    private double longitude;
    private double accuracy;
    private long timestamp;
    private String sensorType;
    private String sensorID;

    public AndroidPhoneGPSLocation(double latitude, double longitude, double accuracy, Context aContext){
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = System.currentTimeMillis();
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        sensorID = PhoneInfo.getID(aContext) + "-" + DATA_TYPE;
        section1 = sensorType;
        section2 = sensorID;
    }

    public AndroidPhoneGPSLocation(Context aContext){
        super();
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        sensorID = PhoneInfo.getID(aContext) + "-" + DATA_TYPE;
        section1 = sensorType;
        section2 = sensorID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public mHealthFormat.MHEALTH_FILE_TYPE getEntityType() {
        return mHealthFormat.MHEALTH_FILE_TYPE.SENSOR;
    }

    @Override
    public String tomHealthRow() {
        Date date = new Date(this.getTimestamp());
        String timestampString=  new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(date);
        String row = String.format("%s,%.6f,%.6f,%.6f", timestampString, this.getLatitude(), this.getLongitude(), this.getAccuracy());
        return row;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public byte[] encodeRowAsBinary() {
        return new byte[0];
    }
}
