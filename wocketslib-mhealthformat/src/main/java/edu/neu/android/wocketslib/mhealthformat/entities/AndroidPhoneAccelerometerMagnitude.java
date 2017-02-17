package edu.neu.android.wocketslib.mhealthformat.entities;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;

/**
 * Created by Dharam on 4/9/2015.
 */
public class AndroidPhoneAccelerometerMagnitude extends mHealthEntity{

    private static final String header = "HEADER_TIME_STAMP,MAGNITUDE,SAMPLE_COUNTS";
    public static final String SENSOR_TYPE = "AndroidPhone";
    public static final String DATA_TYPE = "AccelerationMagnitude";
    public static final String VERSION_INFO = "NA";
    private long timestamp;
    private int phoneAccelAverage;
    private int phoneAccelSamples;
    private String sensorID;
    private String sensorType;

    public AndroidPhoneAccelerometerMagnitude(Context context) {
        super();
        sensorID = PhoneInfo.getID(context) + "-" + DATA_TYPE;
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        section1 = sensorType;
        section2 = sensorID;
    }

    public AndroidPhoneAccelerometerMagnitude(int phoneAccelAverage, int phoneAccelSamples, Context context) {
        super();
        this.phoneAccelAverage = phoneAccelAverage;
        this.phoneAccelSamples = phoneAccelSamples;
        this.timestamp = System.currentTimeMillis();
        sensorID = PhoneInfo.getID(context) + "-" + DATA_TYPE;
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        section1 = sensorType;
        section2 = sensorID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPhoneAccelAverage() {
        return phoneAccelAverage;
    }

    public void setPhoneAccelAverage(int phoneAccelAverage) {
        this.phoneAccelAverage = phoneAccelAverage;
    }

    public int getPhoneAccelSamples() {
        return phoneAccelSamples;
    }

    public void setPhoneAccelSamples(int phoneAccelSamples) {
        this.phoneAccelSamples = phoneAccelSamples;
    }

    @Override
    public mHealthFormat.MHEALTH_FILE_TYPE getEntityType() {
        return mHealthFormat.MHEALTH_FILE_TYPE.SENSOR;
    }

    @Override
    public String tomHealthRow() {
        Date date = new Date(this.getTimestamp());
        String timestampString = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(date);
        return timestampString + "," + this.getPhoneAccelAverage() + "," + this.getPhoneAccelSamples();
    }

    @Override
    public String getHeader(){
        return header;
    }

    @Override
    public byte[] encodeRowAsBinary() {
        return new byte[0];
    }
}
