package edu.neu.android.wocketslib.activities.sensorswap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.sensormonitor.Defines;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Util;
import edu.neu.android.wocketslib.utils.Wockets;

public class LoadingInfoActivity extends BaseActivity {
	private static final String TAG = "LoadingInfoActivity";
	private Wockets wockets = null;
	public final static String CANCEL = "_Cancel_Retrieving_data";
	public final static String broadcastAction = "WOCKETS.DATA";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.loading_activity);
		wockets = new Wockets();
		new LoadWocketsInfoAndSave().execute();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent(broadcastAction);
			i.putExtra("wockets", wockets);
			i.putExtra("warningmsg", CANCEL);
			LoadingInfoActivity.this.finish();
			sendBroadcast(i);
			overridePendingTransition(0, 0);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class LoadWocketsInfoAndSave extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			List<SwappedSensor> swappedSensors = WocketInfoGrabber
					.getSwappedSensors(LoadingInfoActivity.this);
			wockets.setSwappedSensors(swappedSensors);
			setHRSensors(wockets, LoadingInfoActivity.this);

			if (wockets.getSensors() == null
					|| wockets.getSensors().size() == 0)
				Log.e(Globals.SWAP_TAG, "No sensor info retrieved.");
			else
				Log.i(Globals.SWAP_TAG, "Load wockets info from local file, "
						+ wockets.getSensorsInfo());

			Intent i = new Intent(broadcastAction);
			i.putExtra("wockets", wockets);
			i.putExtra("warningmsg", compareWockets(wockets.getSensors()));
			LoadingInfoActivity.this.finish();
			sendBroadcast(i);
			overridePendingTransition(0, 0);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Log.i(Globals.SWAP_TAG,
					"Swap/Change Locations, start to load sensors info.");
			boolean isReload = LoadingInfoActivity.this.getIntent()
					.getBooleanExtra("isReload", false);

			if (isReload || !isLocalSensorInfoExist()) {
				/*
				 * if(WocketInfoGrabber.isActiveInternetConnection(
				 * LoadingInfoActivity.this)){ //TODO this might need to be
				 * fixed with jason because we changed json WocketInfo wi =
				 * WocketInfoGrabber
				 * .getWocketInfoServer(LoadingInfoActivity.this,
				 * PhoneInfo.getID(LoadingInfoActivity.this)); if(wi != null){
				 * wockets.setSensors(wi.someSensors); try {
				 * wockets.saveSensorInfoToFile(LoadingInfoActivity.this); }
				 * catch (Exception e) { Log.e(Globals.SWAP_TAG,
				 * "Cannt write sensor info to local file."); } //TODO check log
				 * Log.i(Globals.SWAP_TAG,
				 * "Get Wockets info from server, "+wockets.getSensorsInfo()); }
				 * else{ Log.e(Globals.SWAP_TAG,
				 * "Failed to retrieve data from server side."); } } else{
				 * Log.e(Globals.SWAP_TAG,
				 * "No internet Connection. Unable to retrieve Wockets info from server."
				 * ); ((ApplicationManager)
				 * getApplication()).killAllActivities(); Intent i = new
				 * Intent(LoadingInfoActivity.this,
				 * NoInternetANDNoLocalFileWarningActivity.class);
				 * startActivity(i); overridePendingTransition(0,0); }
				 */

				// Don't query server. Users can use any paired Wockets they
				// have.
				List<Sensor> sensors = getPairedSensors();
				wockets.setSensors(sensors);
				try {
					wockets.saveSensorInfoToFile(LoadingInfoActivity.this);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				List<Sensor> sensors = WocketInfoGrabber
						.getSensors(LoadingInfoActivity.this);
				wockets.setSensors(sensors);
				Log.i(Globals.SWAP_TAG, "Get Wockets info from local, "
						+ wockets.getSensorsInfo());
			}

			return null;
		}

	}

	public static List<HRData> getHRSensors() {
		List<HRData> HRSensors = new ArrayList<HRData>();
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			DataStore.unsetPairedFlagAllSensors();
			Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter()
					.getBondedDevices();
			Iterator<BluetoothDevice> itr = devices.iterator();
			while (itr.hasNext()) {
				BluetoothDevice dev = itr.next();
				String name = dev.getName();
				String address = Util.removeColons(dev.getAddress());
				if (name.contains(Defines.ZEPHYR_DEVICE_NAME)) {
					HRData zephyrPaired = new HRData();
					zephyrPaired.hardwareID = address;
					HRSensors.add(zephyrPaired);
				}
			}
		}
		return HRSensors;
	}

	public static void setHRSensors(Wockets wockets, Context c) {
		List<HRData> HRSensors = getHRSensors();
		HRData swappedZephyr = WocketInfoGrabber.getHRSensor(c);
		wockets.setHRData(HRSensors);
		if (swappedZephyr != null && swappedZephyr.hardwareID != null
				&& HRSensors.size() > 0)
			for (HRData hrData : HRSensors) {
				if (swappedZephyr.hardwareID.equals(hrData.hardwareID))
					wockets.setSwappedHRSensor(swappedZephyr);
			}

	}

	public static boolean isLocalSensorInfoExist() {
		File sensorInfoFile = new File(Globals.WOCKETS_INFO_JSON_FILE_PATH);
		return sensorInfoFile.exists();
	}

	public static ArrayList<Sensor> getPairedSensors() {
		ArrayList<Sensor> avaiSensors = new ArrayList<Sensor>();
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter()
					.getBondedDevices();
			Iterator<BluetoothDevice> itr = devices.iterator();
			while (itr.hasNext()) {
				BluetoothDevice dev = itr.next();
				if (dev.getName().contains(Defines.WOCKET_DEVICE_NAME)) {
					String[] ss = dev.getAddress().split(":");
					String address = new String();
					for (String s : ss) {
						address += s;
					}
					Sensor s = new Sensor();
					s.macID = address;
					s.label = dev.getName();
					avaiSensors.add(s);
				}
			}
		}
		return avaiSensors;
	}

	private String compareWockets(final List<Sensor> assignedSensors) {
		ArrayList<Sensor> pairedSensors = getPairedSensors();

		ArrayList<String> unassignedPairedSensors_IDs = new ArrayList<String>();
		ArrayList<String> unpairedAssignedSensors_IDs = new ArrayList<String>();
		for (Sensor sensor : assignedSensors) {
			boolean isExist = false;
			for (Sensor s : pairedSensors) {
				if (sensor.macID.equals(s.macID)) {
					isExist = true;
					break;
				}
			}
			if (!isExist)
				unpairedAssignedSensors_IDs.add(sensor.macID);
		}
		for (Sensor sensor : pairedSensors) {
			boolean isExist = false;
			for (Sensor s : assignedSensors) {
				if (sensor.macID.equals(s.macID)) {
					isExist = true;
					break;
				}
			}
			if (!isExist)
				unassignedPairedSensors_IDs.add(sensor.macID);
		}

		String warningMsg = "";
		if (unpairedAssignedSensors_IDs.size() > 0) {
			String unpairedSensorMsg = "";
			for (String id : unpairedAssignedSensors_IDs) {
				for (Sensor unpariedSensor : assignedSensors) {
					if (id.equals(unpariedSensor.macID))
						unpairedSensorMsg += unpariedSensor.color + " "
								+ unpariedSensor.label + " ("
								+ unpariedSensor.macID + "), ";
				}
			}
			Log.e(Globals.SWAP_TAG, "Some assigned Wockets are not paired: "
					+ unpairedSensorMsg);
			warningMsg += "Some assigned Wockets are not paired: "
					+ unpairedSensorMsg.substring(0,
							unpairedSensorMsg.length() - 2) + ". ";
		}
		if (unassignedPairedSensors_IDs.size() > 0) {
			String unassignedSensorMsg = "";
			for (String id : unassignedPairedSensors_IDs) {
				for (Sensor sensor : pairedSensors) {
					if (id.equals(sensor.macID))
						unassignedSensorMsg += sensor.label + ", ";
				}
			}
			Log.e(Globals.SWAP_TAG, "Some paired Wockets are not assigned: "
					+ unassignedSensorMsg);
			warningMsg += "Some paired Wockets are not assigned: "
					+ unassignedSensorMsg.substring(0,
							unassignedSensorMsg.length() - 2) + ". ";

		}
		if (unpairedAssignedSensors_IDs.size() > 0
				|| unassignedPairedSensors_IDs.size() > 0)
			return warningMsg;
		else
			return null;
	}
}
