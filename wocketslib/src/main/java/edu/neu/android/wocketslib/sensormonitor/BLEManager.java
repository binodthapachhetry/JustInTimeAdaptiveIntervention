package edu.neu.android.wocketslib.sensormonitor;

import java.net.ConnectException;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.mhealthformat.LowSamplingRateDataSaver;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

@SuppressLint("NewApi")
public class BLEManager {
	// Bluetooth Low Energy Methods
	private static final String TAG = "BLEManager";
	private static CopyOnWriteArrayList<BLESensorData> mBluetoothDevices = new CopyOnWriteArrayList<BLESensorData>();

	public static void saveScannedOUSDevices(Context mContext) {
		// TODO: Determine later what to do with scanned devices
		Log.d(TAG, "Start saving Scanned Devices..");
		String[] header = { "TIMESTAMP", "Sensor ID", "Battery Life", "Status" };
		String[] values = new String[3];
		LowSamplingRateDataSaver dataSaver = new LowSamplingRateDataSaver(
				Globals.IS_SENSOR_DATA_EXTERNAL,
				Globals.SENSOR_TYPE_OUS_STATUS, PhoneInfo.getID(mContext),
				header);
		if (mBluetoothDevices == null) {
			return;
		}
		Log.d(TAG, "Record " + String.valueOf(mBluetoothDevices.size()) + " OUS Devices");
		for (BLESensorData data : mBluetoothDevices) {
			if (data.getmBluetoothDevice().getName() == null) {
				continue;
			}
			if (data.getmBluetoothDevice().getName().contains("OUS")
					&& (data.getmBluetoothDevice().getName().contains("NMND")
							|| data.getmBluetoothDevice().getName()
									.contains("NMD")
							|| data.getmBluetoothDevice().getName()
									.contains("SM") || data
							.getmBluetoothDevice().getName().contains("CM"))) {
				values[0] = data.getmBluetoothDevice().getAddress();
				values[1] = String.valueOf(Integer.parseInt(data
						.getmBluetoothDevice().getName().substring(7, 9), 16));
				if (data.getmBluetoothDevice().getName().contains("NMND")) {
					values[2] = "Not Moving No Data";
				} else if (data.getmBluetoothDevice().getName().contains("NMD")) {
					values[2] = "Not Moving With data";
				} else if (data.getmBluetoothDevice().getName().contains("SM")) {
					values[2] = "Start Moving";
				} else if (data.getmBluetoothDevice().getName().contains("CM")) {
					values[2] = "Continuous Moving";
				} else {
					values[2] = "Unknown";
				}
				dataSaver.saveData(new Date(), values);
			}	
		}
	}

	public static void connect(Context mContext) {
		Log.d(TAG, "Try to connect...");
		String[] values = new String[3];
		for (BLESensorData data : mBluetoothDevices) {
			values[0] = data.getmBluetoothDevice().getName();
			if (values[0] == null) {
				continue;
			}
			if (values[0].contains("OUS")
					&& (values[0].contains("SM") || values[0].contains("NMD"))) {
				if (!BLEConnection.isConnection) {
					Log.d(TAG, "Connecting " + data.getmBluetoothDevice().getAddress());
					data.getmBluetoothDevice().connectGatt(mContext, false,
							BLEConnection.mGattCallback);
				}
			}
		}
	}

	public static void clear() {
		mBluetoothDevices.clear();
	}

	public static CopyOnWriteArrayList<BLESensorData> getmBluetoothDevices() {
		return mBluetoothDevices;
	}

	protected static LeScanCallback mLeCallback = new LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

			BLESensorData mBleSensorData = new BLESensorData();
			mBleSensorData.setmBluetoothDevice(device);
			mBleSensorData.setRssi(rssi);
			mBleSensorData.setScanRecord(scanRecord);
			if (!mBluetoothDevices.contains(mBleSensorData)) {
				mBluetoothDevices.add(mBleSensorData);
				if (mBleSensorData.getmBluetoothDevice().getName() == null) {
					Log.o(TAG, "Unknown", mBleSensorData.getmBluetoothDevice()
							.getAddress(), String.valueOf(mBleSensorData
							.getRssi()));
				} else {
					Log.o(TAG, mBleSensorData.getmBluetoothDevice().getName(),
							mBleSensorData.getmBluetoothDevice().getAddress(),
							String.valueOf(mBleSensorData.getRssi()));
				}
			}
		}
	};
}
