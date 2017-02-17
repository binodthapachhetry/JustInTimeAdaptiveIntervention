package edu.neu.android.wocketslib.sensormonitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.mail.internet.NewsAddress;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.text.format.DateFormat;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.mhealthformat.LowSamplingRateDataSaver;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

public class BLEConnection {
	private static final String TAG = "BLEConnection";
	private static final UUID mService_UUID = UUID
			.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
	protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");
	private static final UUID mCharacteristicRecordIndex_UUID = UUID
			.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
	private static final UUID mCharacteristicRecordCount_UUID = UUID
			.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
	public static boolean isConnection = false;
	private static final int DATA_BYTE = 6;
	// private static final UUID mCharacteristicRecordValue_UUID = UUID
	// .fromString("0000fff3-0000-1000-8000-00805f9b34fb");
	private static int count = 1;
	private static CopyOnWriteArrayList<BLEOUSData> OUSDataArray = new CopyOnWriteArrayList<BLEOUSData>();
	private static HashMap<String, CopyOnWriteArrayList<BLEOUSData>> OUSDataMap = new HashMap<String, CopyOnWriteArrayList<BLEOUSData>>();
	private static BLEOUSData bleousData = null;
	private static BluetoothGattCharacteristic mcharacteristic;
	@SuppressLint("NewApi")
	public static BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Current state " + connectionState(newState));
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				OUSDataArray.clear();
				OUSDataMap.clear();
				isConnection = true;
				gatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.d(TAG, "Lose connection");
				isConnection = false;
				if (gatt != null) {
					gatt.close();
				}
				gatt = null;
			} else if (status != BluetoothGatt.GATT_SUCCESS) {
				gatt.disconnect();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Services Discovered: " + status + "   "
					+ BluetoothGatt.GATT_SUCCESS);
			mcharacteristic = gatt.getService(mService_UUID).getCharacteristic(
					mCharacteristicRecordIndex_UUID);
			gatt.readCharacteristic(mcharacteristic);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onCharacteristicRead");
			count = characteristic.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, 1);
			bleousData = new BLEOUSData();
			mcharacteristic = gatt.getService(mService_UUID).getCharacteristic(
					mCharacteristicRecordCount_UUID);
			gatt.setCharacteristicNotification(mcharacteristic, true);
			BluetoothGattDescriptor mDescriptor;
			mDescriptor = mcharacteristic
					.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
			mDescriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			gatt.writeDescriptor(mDescriptor);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onCharacteristicWrite");
			Log.d(TAG, gatt.getDevice().getAddress() + "Ready to Close...");
			OUSDataMap.put(gatt.getDevice().getAddress(), OUSDataArray);
			isConnection = false;
			gatt.disconnect();
			// gatt = null;
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onCharacteristicChanged");
			int fb, sb, tb;
			long timediff, timenow;
			timenow = new Date().getTime();
			bleousData.setCount(characteristic.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, 0));
			fb = characteristic.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, 1);
			sb = characteristic.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, 2);
			tb = characteristic.getIntValue(
					BluetoothGattCharacteristic.FORMAT_UINT8, 3);
			timediff = ((fb << 16) + (sb << 8) + tb) * 1000;
			bleousData.setTime(new Date(timenow - timediff).toString());
			bleousData.setMoveCount(Integer.toHexString(
					characteristic.getIntValue(
							BluetoothGattCharacteristic.FORMAT_UINT8, 4))
					.toUpperCase());
			bleousData.setIntensity(Integer.toHexString(
					characteristic.getIntValue(
							BluetoothGattCharacteristic.FORMAT_UINT8, 5))
					.toUpperCase());
			if (count == bleousData.getCount()) {
				OUSDataArray.add(bleousData);
				Log.o(TAG,
						gatt.getDevice().getAddress(),
						String.valueOf(bleousData.getCount()),
						bleousData.getTime(),
						"0x"
								+ (bleousData.getMoveCount().length() == 1 ? "0"
										: "") + bleousData.getMoveCount(),
						"0x"
								+ (bleousData.getIntensity().length() == 1 ? "0"
										: "") + bleousData.getIntensity());
				saveConnectedData(gatt.getDevice().getAddress());
				bleousData = new BLEOUSData();
				count++;
			} else {
				mcharacteristic = gatt.getService(mService_UUID)
						.getCharacteristic(mCharacteristicRecordIndex_UUID);
				mcharacteristic.setValue(new byte[] { 0x00, (byte) (count) });
				gatt.writeCharacteristic(mcharacteristic);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onDescriptorWrite");
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onReadRemoteRssi");
		}

		private String connectionState(int status) {
			switch (status) {
			case BluetoothProfile.STATE_CONNECTED:
				return "Connected";
			case BluetoothProfile.STATE_DISCONNECTED:
				return "Disconnected";
			case BluetoothProfile.STATE_CONNECTING:
				return "Connecting";
			case BluetoothProfile.STATE_DISCONNECTING:
				return "Disconnecting";
			default:
				return String.valueOf(status);
			}
		}

		private void saveConnectedData(String CurDeviceID) {
			// TODO: Determine later what to do with scanned devices
			Log.d(TAG, "Start Saving Connecting data");
			String[] header = { "TIMESTAMP", "Sensor ID", "StartTime",
					"EndTime", "Intensity" };
			String[] values = new String[4];
			LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(
					Globals.IS_SENSOR_DATA_EXTERNAL,
					Globals.SENSOR_TYPE_OUS_MOTION_EVENTS, "OUSDevices", header);
			if (OUSDataArray == null || OUSDataArray.size() == 0) {
				return;
			}
			Log.d(TAG, "Record " + String.valueOf(bleousData.getCount())
					+ " data entry");

			values[0] = CurDeviceID;
			values[1] = new Date(new Date(bleousData.getTime()).getTime() - 100
					* Integer.parseInt(bleousData.getMoveCount(), 16))
					.toString();
			values[2] = bleousData.getTime();
			values[3] = String.valueOf(Integer.parseInt(
					bleousData.getIntensity(), 16));
			dataSaver.saveData(new Date(), values);
		}

	};

	public static CopyOnWriteArrayList<BLEOUSData> getmBLEDeviceData(String key) {
		Log.d(TAG, "Key: " + key);
		if (OUSDataMap.get(key) != null) {
			Log.d(TAG,
					"Arraylist Size: "
							+ String.valueOf(OUSDataMap.get(key).size()));
		} else {
			Log.d(TAG, "Arraylist Size: NULL");
		}
		return OUSDataMap.get(key);

	}

}
