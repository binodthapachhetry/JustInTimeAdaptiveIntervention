package edu.neu.android.wocketslib.mhealthformat.entities;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;

/**
 * Created by Dharam on 4/9/2015.
 */
public class AndroidPhoneAccelerometerRaw extends mHealthEntity{

    private static final String header = "HEADER_TIME_STAMP,X_ACCELATION_METERS_PER_SECOND_SQUARED,Y_ACCELATION_METERS_PER_SECOND_SQUARED,Z_ACCELATION_METERS_PER_SECOND_SQUARED";
    public static final String SENSOR_TYPE = "AndroidPhone";
    public static final String DATA_TYPE = "AccelerationCalibrated";
    public static final String VERSION_INFO = "NA";
    private long timestamp;
    private float rawx;
    private float rawy;
    private float rawz;
    private String sensorID;
    private String sensorType;

    public AndroidPhoneAccelerometerRaw(Context context) {
        super();
        sensorID = PhoneInfo.getID(context) + "-" + DATA_TYPE;
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        section1 = sensorType;
        section2 = sensorID;
    }

    public AndroidPhoneAccelerometerRaw(float rawx, float rawy, float rawz, Context context) {
        super();
        this.rawx = rawx;
        this.rawy = rawy;
        this.rawz = rawz;
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

    public float getRawx() {
        return rawx;
    }

    public void setRawx(float rawx) {
        this.rawx = rawx;
    }

    public float getRawy() {
        return rawy;
    }

    public void setRawy(float rawy) {
        this.rawy = rawy;
    }

    public float getRawz() {
        return rawz;
    }

    public void setRawz(float rawz) {
        this.rawz = rawz;
    }

    @Override
    public mHealthFormat.MHEALTH_FILE_TYPE getEntityType() {
        return mHealthFormat.MHEALTH_FILE_TYPE.SENSOR;
    }

    @Override
    public String tomHealthRow() {
        Date date = new Date(this.getTimestamp());
        String timestampString=  new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(date);
        String row = String.format("%s,%.5f,%.5f,%.5f", timestampString, this.getRawx(), this.getRawy(), this.getRawz());
        return row;
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
