package edu.neu.android.wocketslib.mhealthformat.entities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.ByteUtils;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;


/**
 * Created by Dharam on 4/9/2015.
 */
public class AndroidWearAccelerometerRaw extends mHealthEntity{

    private static final String header = "HEADER_TIME_STAMP,X_ACCELATION_METERS_PER_SECOND_SQUARED,Y_ACCELATION_METERS_PER_SECOND_SQUARED,Z_ACCELATION_METERS_PER_SECOND_SQUARED";
    public static final String SENSOR_TYPE = "AndroidWearWatch";
    public static final String DATA_TYPE = "AccelerationCalibrated";
    public static final String VERSION_INFO = "NA";
    private static final String TAG = "AccelerometerRaw";

    private static final int BINARY_BUFFER_SIZE = 20000;
    private long timestamp;
    private float rawx;
    private float rawy;
    private float rawz;
    private String sensorID;
    private String sensorType;
    private long lastTimestamp = 0;
    private File lastFile = null;
    private byte[] binaryBuffer = new byte[BINARY_BUFFER_SIZE];
    private int bufferPos = 0;
//    private long ref = 0;

    public AndroidWearAccelerometerRaw(long timestamp, float rawx, float rawy, float rawz, Context context){
        super();
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        sensorID = PhoneInfo.getBluetoothMacAddressConcated() + "-" + DATA_TYPE;
        this.rawx = rawx;
        this.rawy = rawy;
        this.rawz = rawz;
        this.timestamp = timestamp;
        section1 = sensorType;
        section2 = sensorID;
    }

    public AndroidWearAccelerometerRaw(Context context){
        super();
        sensorType = SENSOR_TYPE + "-" + DATA_TYPE + "-" + VERSION_INFO;
        sensorID = PhoneInfo.getBluetoothMacAddressConcated() + "-" + DATA_TYPE;
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
        byte[] row = new byte[20];
        int count = 0;
        byte[] binaryX = ByteUtils.float2ByteArray(rawx);
        byte[] binaryY = ByteUtils.float2ByteArray(rawy);
        byte[] binaryZ = ByteUtils.float2ByteArray(rawz);
        byte[] binaryTs = ByteUtils.long2ByteArray(timestamp);
        row[count++] = binaryX[0];
        row[count++] = binaryX[1];
        row[count++] = binaryX[3];
        row[count++] = binaryX[1];

        row[count++] = binaryY[0];
        row[count++] = binaryY[1];
        row[count++] = binaryY[2];
        row[count++] = binaryY[3];

        row[count++] = binaryZ[0];
        row[count++] = binaryZ[1];
        row[count++] = binaryZ[2];
        row[count++] = binaryZ[3];

        row[count++] = binaryTs[0];
        row[count++] = binaryTs[1];
        row[count++] = binaryTs[2];
        row[count++] = binaryTs[3];
        row[count++] = binaryTs[4];
        row[count++] = binaryTs[5];
        row[count++] = binaryTs[6];
        row[count++] = binaryTs[7];

        return(row);
    }

    private void addToBinaryBuffer(){
        byte[] binaryX = ByteUtils.float2ByteArray(rawx);
        byte[] binaryY = ByteUtils.float2ByteArray(rawy);
        byte[] binaryZ = ByteUtils.float2ByteArray(rawz);
        byte[] binaryTs = ByteUtils.long2ByteArray(timestamp);
        binaryBuffer[bufferPos++] = binaryX[0];
        binaryBuffer[bufferPos++] = binaryX[1];
        binaryBuffer[bufferPos++] = binaryX[3];
        binaryBuffer[bufferPos++] = binaryX[1];

        binaryBuffer[bufferPos++] = binaryY[0];
        binaryBuffer[bufferPos++] = binaryY[1];
        binaryBuffer[bufferPos++] = binaryY[2];
        binaryBuffer[bufferPos++] = binaryY[3];

        binaryBuffer[bufferPos++] = binaryZ[0];
        binaryBuffer[bufferPos++] = binaryZ[1];
        binaryBuffer[bufferPos++] = binaryZ[2];
        binaryBuffer[bufferPos++] = binaryZ[3];

        binaryBuffer[bufferPos++] = binaryTs[0];
        binaryBuffer[bufferPos++] = binaryTs[1];
        binaryBuffer[bufferPos++] = binaryTs[2];
        binaryBuffer[bufferPos++] = binaryTs[3];
        binaryBuffer[bufferPos++] = binaryTs[4];
        binaryBuffer[bufferPos++] = binaryTs[5];
        binaryBuffer[bufferPos++] = binaryTs[6];
        binaryBuffer[bufferPos++] = binaryTs[7];
    }

    @Override
    public boolean bufferedWriteTomHealthBinary(boolean isAppend) throws Exception {
        flushAndCloseBinary(false);
        addToBinaryBuffer();
        return true;
    }

    @Override
    public void flushAndCloseBinary(boolean force) throws Exception {
        if(lastTimestamp == 0){
            lastTimestamp = getTimestamp();
        }

        Date current = new Date(getTimestamp());
        Date last = new Date(lastTimestamp);

        Calendar cal = Calendar.getInstance();
        cal.set(2017, Calendar.MARCH, 9, 10, 11, 12); //Year, month, day of month, hours, minutes and seconds
        Date toCompare = cal.getTime();

//        Log.i(TAG, "Date current:" + current.toString());
//        Log.i(TAG, "Date last:" + last.toString());
//        Log.i(TAG, "Date reference:" + toCompare.toString());


        if(last.after(toCompare)) {
//            Log.i(TAG, "Last after reference. So saving");
//        }

            File toBeWritten = null;
            lastTimestamp = getTimestamp();

            if (current.getHours() - last.getHours() == 1 || (current.getHours() == 0 && last.getHours() == 23)) {
                //save the whole buffer to previous hour folder
                File folder = new File(mHealthFormat.buildmHealthPath(last, mHealthFormat.PATH_LEVEL.HOURLY, getEntityType()));
                File[] previousHourFolder = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.contains("baf");
                    }
                });
                if (previousHourFolder.length > 0) {
                    toBeWritten = previousHourFolder[0];
                } else {
                    throw new IOException("Can't find the previous hour file, please verify it still exists");
                }
                final byte[] cloned = Arrays.copyOfRange(binaryBuffer, 0, bufferPos);
                bufferPos = 0;
                final File finalToBeWritten = toBeWritten;
                new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        FileOutputStream writer = null;
                        try {
                            writer = new FileOutputStream(finalToBeWritten, true);
                            writer.write(cloned);
                            writer.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (writer != null) {
                                try {
                                    writer.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return null;
                    }
                }.execute();
            } else if (BINARY_BUFFER_SIZE - bufferPos < 20 || force) {
                //save the whole buffer
                File folder = new File(mHealthFormat.buildmHealthPath(current, mHealthFormat.PATH_LEVEL.HOURLY, getEntityType()));
                Log.i(TAG, folder.getPath());
                File[] currentHourFolder = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.contains("baf");
                    }
                });

                if (currentHourFolder != null) {
                    if (currentHourFolder.length > 0) {
                        toBeWritten = currentHourFolder[0];
                        //                }
                    } else {
                        toBeWritten = new File(folder + File.separator + mHealthFormat.buildBafFilename(current, section1, section2, "sensor"));
                    }
                    final byte[] cloned = Arrays.copyOfRange(binaryBuffer, 0, bufferPos);
                    final File finalToBeWritten = toBeWritten;
                    bufferPos = 0;
                    new AsyncTask<String, String, String>() {
                        @Override
                        protected String doInBackground(String... params) {
                            FileOutputStream writer = null;
                            try {
                                writer = new FileOutputStream(finalToBeWritten, true);
                                writer.write(cloned);
                                writer.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (writer != null) {
                                    try {
                                        writer.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }
}
