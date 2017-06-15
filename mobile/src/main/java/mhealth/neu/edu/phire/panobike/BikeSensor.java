package mhealth.neu.edu.phire.panobike;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import mhealth.neu.edu.phire.TEMPLEConstants;
import mhealth.neu.edu.phire.data.TEMPLEDataManager;

public class BikeSensor
{
    public static final UUID CSC_SERVICE_UUID = UUID.fromString(TEMPLEConstants.CSC_SERVICE_UUID);

    public enum ConnectionState {
        INIT,
        CONNECTED,
        ERROR,
    };

    public interface Callback
    {
        void onConnectionStateChange(BikeSensor sensor, BikeSensor.ConnectionState newState);
        void onSpeedUpdate(BikeSensor sensor, double distance, double elapsedUs, long wheelrot);
        void onCadenceUpdate(BikeSensor sensor, int rotations, double elapsedUs, int crankrot);
    }

    private static final UUID CSC_MEASUREMENT_UUID = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb");
    private static final UUID CSC_FEATURE_UUID = UUID.fromString("00002a5c-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final String TAG = BikeSensor.class.getSimpleName();

    private ConnectionState mState;
    private double mCircumference;
    private boolean mEnabled;
    private String mError;

    private Context mContext;
    private Callback mCallback;


    private BluetoothDevice mBluetoothDevice;
    private static BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mFeatureChar;
    private BluetoothGattCharacteristic mMeasurementChar;


    private boolean mHasWheel, mHasCrank;

    private boolean mWheelStopped, mCrankStopped;
    private long mLastWheelReading;
    private int mLastCrankReading;
    private int mLastWheelTime, mLastCrankTime;




//    public  BluetoothGatt mBluetoothGatt;

    private BluetoothGattCallback mBluetoothGattCb = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            BikeSensor parent = BikeSensor.this;

            if (status != BluetoothGatt.GATT_SUCCESS) {
                doError("Error connecting to device");
//                gatt.close();
                // DISCONNECT AND CLOSE THE DEVICE HERE
//                disConnect();

                return;
            }

            if (parent.mState == ConnectionState.INIT && newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to device");

                if (!parent.mBluetoothGatt.discoverServices()) {
                    doError("Error trying to discover services");
                    return;
                }
            }

            // FIXME: We probably need to handle connection / disconnection events after init
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            BikeSensor parent = BikeSensor.this;

            if (status != BluetoothGatt.GATT_SUCCESS) {
                doError("Error while discovering services");
                return;
            }

            Log.i(TAG, "Services discovered");

            BluetoothGattService service = mBluetoothGatt.getService(CSC_SERVICE_UUID);

            Log.i(TAG,service.toString());


            mFeatureChar = service.getCharacteristic(CSC_FEATURE_UUID);
            mMeasurementChar = service.getCharacteristic(CSC_MEASUREMENT_UUID);

            gatt.readCharacteristic(mFeatureChar);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            BikeSensor parent = BikeSensor.this;

            if (status != BluetoothGatt.GATT_SUCCESS) {
                doError("Error reading characteristic " + characteristic);
                return;
            }

            if (characteristic == parent.mFeatureChar) {
                int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);

                parent.mHasWheel = (flags & 0x1) != 0;
                parent.mHasCrank = (flags & 0x2) != 0;

                // Now we've got all the information we need to start collecting data
                parent.mState = ConnectionState.CONNECTED;

                parent.mCallback.onConnectionStateChange(parent, mState);
            }

            Log.i(TAG, "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d(TAG, "onCharacteristicChanged");
            BikeSensor parent = BikeSensor.this;
            boolean hasWheel, hasCrank;
            long wheelRotations;
            int crankRotations;
            int time;


            Calendar cs = Calendar.getInstance();
            SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TEMPLEDataManager.setPanoBikeLastConnectionTime(mContext, dfs.format(cs.getTime()));



            // We'll only ever be notified on the measurement characteristic

            byte[] value = parent.mMeasurementChar.getValue();

//            String decodedData = new String(value);
//            Log.d(TAG, "Value " + value.toString());


//            Log.d("Value " + value);

            if (value.length < 1) {
                Log.w(TAG, "Bad measurement size " + value.length);
                return;
            }

            hasWheel = (value[0] & 0x1) != 0;
            hasCrank = (value[0] & 0x2) != 0;

//            for(byte b: value){
//                Log.d(TAG, "Value " + String.format("0x%20x", b));
//            }

            if ((hasWheel && hasCrank && value.length < 11) || (hasWheel && value.length < 7) ||
                    (hasCrank && value.length < 5)) {
                Log.w(TAG, "Bad measurement size " + value.length);
                return;
            }

            int i = 1;

            // Note: We only send out a delta update when we have a meaningful
            // delta. If the user was coasting or stopped, the last update will
            // be from a long ago, making the delta meaningless for both
            // instantaneous and average calculations.

            if (hasWheel) {
                wheelRotations = readU32(value, i);
                time = readU16(value, i + 4);
//                Log.d(TAG,"Wheel Rotations: " + wheelRotations + ",Time: " + time);

                if (wheelRotations == 0) {
                    // We've stopped moving
                    mWheelStopped = true;

                } else if (mWheelStopped) {
                    // Wheel's started again
                    mWheelStopped = false;
                    mLastWheelReading = wheelRotations;
                    mLastWheelTime = time;

                    parent.mCallback.onSpeedUpdate(parent, 0, 0.0,0);

                } else {
                    if (wheelRotations != mLastWheelReading) {
                        // Delta over last update
                        int timeDiff;
                        timeDiff = do16BitDiff(time, mLastWheelTime);
                        if(wheelRotations > mLastWheelReading) {
                            parent.mCallback.onSpeedUpdate(parent, (wheelRotations - mLastWheelReading) * mCircumference,
                                    (timeDiff / 1024.0), wheelRotations);
                        }else{
                            parent.mCallback.onSpeedUpdate(parent, (mLastWheelReading - wheelRotations) * mCircumference,
                                    (timeDiff / 1024.0), wheelRotations);
                        }

                        mLastWheelReading = wheelRotations;
                        mLastWheelTime = time;
//                        parent.mCallback.onSpeedUpdate(parent, 0, wheelRotations);
                        // Can happen if bicycle reverses
//                        wheelRotations = 0;
                        // do nothing
//                        return;

                    }

                }

                i += 6;
            }

            if (hasCrank) {
                crankRotations = readU16(value, i);
                time = readU16(value, i + 2);
//                Log.d(TAG,"Crank Rotations: " + crankRotations + ",Time: " + time);

                if (crankRotations == 0) {
                    // Coasting or stopped
                    mCrankStopped = true;

                } else if (mCrankStopped) {
                    // Crank's started up again

                    mCrankStopped = false;
                    mLastCrankReading = crankRotations;
                    mLastCrankTime = time;

                    parent.mCallback.onCadenceUpdate(parent, 0, 0.0,0);

                } else {
                    // Delta over last update
                    int rotDiff;
                    rotDiff = do16BitDiff(crankRotations, mLastCrankReading);
                    if (rotDiff != 0) {
                        int timeDiff;
                        timeDiff = do16BitDiff(time, mLastCrankTime);

                        parent.mCallback.onCadenceUpdate(parent, Math.abs(rotDiff), (timeDiff / 1024.0), crankRotations);

                        mLastCrankReading = crankRotations;
                        mLastCrankTime = time;
                        // do nothing
//                        return;
                    }
                }
            }
        }
    };

    private void doError(String error)
    {
        Log.w(TAG, "this is the error : " + error);
        Log.d(TAG,"connection stoppped!");

        mError = error;
        mState = ConnectionState.ERROR;

        mCallback.onConnectionStateChange(this, mState);

        return;
    }

    private int do16BitDiff(int a, int b)
    {
        if (a >= b)
            return a - b;
        else
            return (a + 65536) - b;
    }

    private int readU32(byte[] bytes, int offset)
    {
        // Does not perform bounds checking
        return ((bytes[offset + 3] << 24) & 0xff000000) +
                ((bytes[offset + 2] << 16) & 0xff0000) +
                ((bytes[offset + 1] << 8) & 0xff00) +
                (bytes[offset] & 0xff);
    }

    private int readU16(byte[] bytes, int offset)
    {
        return ((bytes[offset + 1] << 8) & 0xff00) + (bytes[offset] & 0xff);
    }

    public BikeSensor(Context context, BluetoothDevice device, double diameter, BikeSensor.Callback callback)
    {
        BluetoothGattService service;

        mState = ConnectionState.INIT;
        mContext = context;
        mBluetoothDevice = device;
        mCircumference = diameter * Math.PI * 2/100.0;
        mCallback = callback;

        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCb);

        mWheelStopped = mCrankStopped = true;
    }

    public static void disConnect(){
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();

        mBluetoothGatt = null;

    }

    public boolean hasSpeed()
    {
        if (mState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Not connected");

//        Calendar ca = Calendar.getInstance();
//        SimpleDateFormat dfa = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        TEMPLEDataManager.setPanoBikeLastConnectionTime(mContext,dfa.format(ca.getTime()));
//        Log.i(TAG,"SET LAST CONNECTION TIME AS:"+dfa.format(ca.getTime()));
        return mHasWheel;
    }

    public boolean hasCadence()
    {
        if (mState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Not connected");

//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        TEMPLEDataManager.setPanoBikeLastConnectionTime(mContext,df.format(c.getTime()));
//        Log.i(TAG,"SET LAST CONNECTION TIME AS:"+df.format(c.getTime()));

        return mHasCrank;
    }

    public String getError()
    {
        return mError;
    }

    public void setNotificationsEnabled(boolean enable)
    {
        if (mState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Not connected");

        if (enable == mEnabled)
            return;

        mEnabled = enable;

        mBluetoothGatt.setCharacteristicNotification(mMeasurementChar, mEnabled);

        BluetoothGattDescriptor descriptor = mMeasurementChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }
}
