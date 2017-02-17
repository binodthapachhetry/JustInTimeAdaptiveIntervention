package edu.neu.android.wocketslib.sensormonitor;

import android.bluetooth.BluetoothDevice;

public class BLESensorData {
	// Data Structure for Bluetooth Low Energy
	private BluetoothDevice mBluetoothDevice;
	private int rssi;
	private byte[] scanRecord;

	public BluetoothDevice getmBluetoothDevice() {
		return mBluetoothDevice;
	}

	public void setmBluetoothDevice(BluetoothDevice mBluetoothDevice) {
		this.mBluetoothDevice = mBluetoothDevice;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public byte[] getScanRecord() {
		return scanRecord;
	}

	public void setScanRecord(byte[] scanRecord) {
		this.scanRecord = scanRecord;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((mBluetoothDevice == null) ? 0 : mBluetoothDevice.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BLESensorData other = (BLESensorData) obj;
		if (mBluetoothDevice == null) {
			if (other.mBluetoothDevice != null)
				return false;
		} else if (!mBluetoothDevice.equals(other.mBluetoothDevice))
			return false;
		return true;
	}
}
