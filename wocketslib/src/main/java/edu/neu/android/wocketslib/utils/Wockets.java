package edu.neu.android.wocketslib.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.json.model.SwapEvent;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.json.model.Swapping;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.sensormonitor.Defines;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;

public class Wockets implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<Sensor> sensors;
	private Swapping swap;
	private List<SwappedSensor> swappedSensors;
	private List<HRData> HRSensors;
	private HRData swappedHRSensor;
	private static final Date REASONABLE_DATE = new Date(2013 - 1900, 0, 1);

	public Wockets() {
		super();
		this.sensors = new ArrayList<Sensor>();
		this.swap = new Swapping();
		this.swap.someSwap.add(new SwapEvent());
		this.swappedSensors = new ArrayList<SwappedSensor>();
		this.HRSensors = new ArrayList<HRData>();
		this.swappedHRSensor = new HRData();
	}

	public void resetAll() {
		this.sensors = new ArrayList<Sensor>();
		this.swap = new Swapping();
		this.swap.someSwap.add(new SwapEvent());
		this.swappedSensors = new ArrayList<SwappedSensor>();
		this.HRSensors = new ArrayList<HRData>();
		this.swappedHRSensor = new HRData();
	}

	public void resetSwap(Context c) {
		this.swappedSensors.clear();
		this.setSwappedHRSensor(new HRData());
		this.swap.someSwap.clear();
		this.swap.someSwap.add(new SwapEvent());
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		checkSetWockets(sensors);
	}

	public List<SwappedSensor> getSwappedSensors() {
		return swappedSensors;
	}

	public void setSwappedSensors(List<SwappedSensor> swappedSensors) {
		// checkSetSwappedWockets(swappedSensors);
		this.swappedSensors = swappedSensors;
	}

	public List<HRData> getHRData() {
		return HRSensors;
	}

	public void setHRData(List<HRData> hRData) {
		HRSensors = hRData;
	}

	public HRData getSwappedHRSensor() {
		return swappedHRSensor;
	}

	public void setSwappedHRSensor(HRData swappedHRSensor) {
		this.swappedHRSensor = swappedHRSensor;
	}

	public void swap(Context c, String MacID, String location) {
		if (!swappedSensors.isEmpty()) {
			for (int i = 0; i < swappedSensors.size(); i++) {
				if (swappedSensors.get(i).macID.equalsIgnoreCase(MacID)) {
					swappedSensors.remove(i);
					break;
				}
			}
		}
		if (location.equalsIgnoreCase("delete")) {
			Log.v("deBug", "sensor deleted");
		} else {
			SwappedSensor swappedSensor = new SwappedSensor();
			swappedSensor.macID = MacID;
			swappedSensor.bodyLocation = location;
			swappedSensors.add(swappedSensor);
		}
		setSwappedEvent(c);
	}

	public void setSwappedEvent(Context c) {
		SwapEvent swapEvent = new SwapEvent();
		swapEvent.swapTime = new Date();
		swapEvent.uploadTime = new Date();
		swapEvent.isLocationChange = true;
		swapEvent.isSwap = true;
		swapEvent.swappedSensor = this.swappedSensors;
		swap.someSwap.clear();
		swap.someSwap.add(swapEvent);
	}

	public String[] getItemsMacID() {
		String[] items = new String[sensors.size()];
		for (int i = 0; i < sensors.size(); i++) {
			items[i] = sensors.get(i).macID;
		}
		return items;
	}

	public String[] getItemsLable() {
		String[] items = new String[sensors.size()];
		for (int i = 0; i < sensors.size(); i++) {
			if (sensors.get(i).color != null)
				items[i] = sensors.get(i).color + " " + sensors.get(i).label;
			else
				items[i] = sensors.get(i).label;
		}
		return items;
	}

	public String getSwappedZephyrName() {
		String name = "";
		List<String> pairedHRNames = new ArrayList<String>();
		List<String> pairedHRAddress = new ArrayList<String>();

		if (BluetoothAdapter.getDefaultAdapter() != null) {
			DataStore.unsetPairedFlagAllSensors();
			Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			Iterator<BluetoothDevice> itr = devices.iterator();
			while (itr.hasNext()) {
				BluetoothDevice dev = itr.next();
				pairedHRNames.add(dev.getName());
				pairedHRAddress.add(Util.removeColons(dev.getAddress()));
			}
		}
		if (swappedHRSensor != null && swappedHRSensor.hardwareID != null)
			for (int j = 0; j < pairedHRAddress.size(); j++) {
				if (swappedHRSensor.hardwareID.equals(pairedHRAddress.get(j))) {
					name = pairedHRNames.get(j);
				}
			}
		return name;
	}

	public String getSensorsInfo() {
		if (sensors.size() > 0) {
			String sensorInfo = "";
			String[] labels = getItemsLable();
			String[] ids = getItemsMacID();
			for (int i = 0; i < sensors.size(); i++) {
				sensorInfo += "Wockets-" + labels[i] + " ID: " + ids[i] + ", ";
			}
			sensorInfo = sensorInfo.substring(0, sensorInfo.length() - 2) + ".";
			return sensorInfo;
		} else
			return "none";
	}
	
	public String toDetailedString(Context c){
		String record = "";
		int counter = 0;
		DataStore.init(c);
		if (swappedHRSensor != null && swappedHRSensor.hardwareID != null) {
			record += " the Zephyr HxM heart rate monitor on your chest and";
			counter++;
		}

		if (swappedSensors != null && swappedSensors.size() > 0) {
			if (counter != 0 && swappedSensors.size() != 1) {
				record = record.substring(0, record.length() - 4) + ",";
				counter = 0;
			}

			for (int i = 0; i < swappedSensors.size(); i++) {
				for (int j = 0; j < sensors.size(); j++)
					if (swappedSensors.get(i).macID.equalsIgnoreCase(sensors.get(j).macID)) {
						record += " the " + (sensors.get(j).color == null ? ""
								: sensors.get(j).color+ " ") 
										+ sensors.get(j).label + " on your "
										+ swappedSensors.get(i).bodyLocation; 
						for (edu.neu.android.wocketslib.sensormonitor.Sensor s : DataStore.mSensors) {
							if (s.mAddress.equals(swappedSensors.get(i).macID)) {
								SimpleDateFormat hours = new SimpleDateFormat("hh:mm a");
								SimpleDateFormat detailedTime = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
								if (s.mLastConnectionTime == null || s.mLastConnectionTime.before(REASONABLE_DATE))
									record += " (Last time connected: Unknown)";

								else if (isToday(s.mLastConnectionTime))
									record += " (Last time connected:" + hours.format(s.mLastConnectionTime) + "Battery was at " + (int) (s.getBatteryPercentage() * 100) + "%)";

								else
									record += " (Last time connected:" + detailedTime.format(s.mLastConnectionTime) + ")";
							}
						}
						record += " and";
						counter++;
					}
			}
		}
		if (counter != 0)
			record = record.substring(0, record.length() - 4) + ".";
		else
			record = " nothing.";
		Log.i("deBug", "wockets info: " + record);
		return record;

	}
	private boolean isToday(Date d) {
		Date today = new Date();
		today.setHours(0);
		today.setMinutes(0);
		today.setSeconds(0);
		return d.after(today);
	}

	public String toString() {
		String record = "";
		int counter = 0;
		if (swappedHRSensor != null && swappedHRSensor.hardwareID != null) {
			record += " the Zephyr HxM heart rate monitor on your chest and";
			counter++;
		}

		if (swappedSensors != null && swappedSensors.size() > 0) {
			if (counter != 0 && swappedSensors.size() != 1) {
				record = record.substring(0, record.length() - 4) + ",";
				counter = 0;
			}

			for (int i = 0; i < swappedSensors.size(); i++) {
				for (int j = 0; j < sensors.size(); j++)
					if (swappedSensors.get(i).macID.equalsIgnoreCase(sensors.get(j).macID)) {
						record += " the "
								+ ((sensors.get(j).color == null) ? ""
										: sensors.get(j).color+ " ") 
								+ sensors.get(j).label + " on your "
								+ swappedSensors.get(i).bodyLocation + " and";
						counter++;
					}
			}
		}
		if (counter != 0)
			record = record.substring(0, record.length() - 4) + ".";
		else
			record = " nothing.";
		Log.i("deBug", "wockets info: " + record);
		return record;
	}

	public void checkSetWockets(List<Sensor> ss) {
		this.sensors.clear();
		for (Sensor sensor : ss) {
			if (sensor.macID.length() == 12)
				this.sensors.add(sensor);
		}

	}

	public void checkSetSwappedWockets(List<SwappedSensor> ss) {
		this.swappedSensors.clear();
		for (SwappedSensor swappedSensor : ss) {
			String id = swappedSensor.macID;
			for (Sensor sensor : this.sensors) {
				if (id.equals(sensor.macID)) {
					this.swappedSensors.add(swappedSensor);
					break;
				}
			}
		}
	}

	public void dataChecker(Context c) {
		Log.i(Globals.SWAP_TAG, "Start to update sensor info.");
		if (WocketInfoGrabber.isActiveInternetConnection(c)) {
			Wockets wocketsFromServer = WocketInfoGrabber.updateLocalSensorInfo(c);
			if (wocketsFromServer != null)
				this.setSensors(wocketsFromServer.getSensors());
			else
				Log.i(Globals.SWAP_TAG, "Wockets info from server is null");
		}
		List<SwappedSensor> ss = new ArrayList<SwappedSensor>();
		for (SwappedSensor swappedSensor : this.swappedSensors) {
			ss.add(swappedSensor);
		}
		this.setSwappedSensors(ss);
		setSwappedEvent(c);
	}

	public void saveSensorInfoToFile(Context c) throws Exception {
		Type sensorsType = new TypeToken<ArrayList<Sensor>>() {
		}.getType();
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		String json = gson.toJson(sensors, sensorsType);
		Log.i(Globals.SWAP_TAG, "Save sensor info: "+json);
		File data = new File(Globals.WOCKETS_INFO_JSON_FILE_PATH);
		if(!data.getParentFile().isDirectory())
			data.getParentFile().mkdirs();
		if(!data.exists())
			data.createNewFile();
		FileOutputStream fos = new FileOutputStream(data);
		fos.write(json.getBytes());
		fos.flush();
		fos.close();
	}

	public void saveSwapInfoToFile(Context c) throws Exception {
		if (swap.someSwap.size() > 0) {
			Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
			String json = gson.toJson(swap.someSwap.get(0), SwapEvent.class);
			Log.i(Globals.SWAP_TAG, "Save swapped sensor info: "+json);
			File data = new File(Globals.SWAPPED_WOCKETS_JSON_FILE_PATH);
			if(!data.getParentFile().isDirectory())
				data.getParentFile().mkdirs();
			if(!data.exists())
				data.createNewFile();
			FileOutputStream fos = new FileOutputStream(data);
			fos.write(json.getBytes());
			fos.flush();
			fos.close();
		}
	}

	public void saveHRInfoToFile(Context c) throws Exception {
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		String json = gson.toJson(swappedHRSensor, HRData.class);
		Log.i(Globals.SWAP_TAG, "Save HR sensor info: " + json);
		FileOutputStream fos = c.openFileOutput(Globals.swappedHRFile, Context.MODE_PRIVATE);
		fos.write(json.getBytes());
		fos.flush();
		fos.close();

		/**
		 * save a copy in SD card
		 */
		// InputStream is = c.openFileInput(Globals.swappedHRFile);
		// FileUtils fu = new FileUtils();
		// fu.write2SDFromInput("/.wockets/logs", Globals.swappedHRFile, is);
	}

	public void transmitSwappedSensorsToServer(Context c) throws IOException {
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		String json = gson.toJson(swap, Swapping.class);
		json = "{\"phoneID\":\"" + PhoneInfo.getID(c) + "\"," + json.substring(1);
		DataSender.transmitOrQueueJSON(c, json);
		// ServerNoteSender.sendNote(c,
		// "SUCCESS! Upload swapped Wocket info successful.", false);

		/**
		 * save a copy in SD card
		 */

		// String uploadFileName = "Swapping_"+new Date().getTime()+".json";
		// FileOutputStream fos =
		// c.openFileOutput(uploadFileName,Context.MODE_PRIVATE);
		// fos.write(json.getBytes());
		// fos.flush();
		// fos.close();

		// InputStream is = c.openFileInput(uploadFileName);
		// FileUtils fu = new FileUtils();
		// fu.write2SDFromInput("/.wockets/logs/", uploadFileName, is);
	}

	public void saveSensorsEnabled(Context c) {
		DataStore.init(c);
		SharedPreferences prefs = c.getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.clear();

		for (edu.neu.android.wocketslib.sensormonitor.Sensor s : DataStore.mSensors) {
			s.mEnabled = false;
			if (swappedSensors != null)
				for (SwappedSensor swappedSensor : swappedSensors) {
					if (s.mAddress.equals(swappedSensor.macID)) {
						s.mEnabled = true;
						break;
					}
				}
			if (swappedHRSensor != null && swappedHRSensor.hardwareID != null)
				if (s.mAddress.equals(swappedHRSensor.hardwareID))
					s.mEnabled = true;
			Log.i("deBug", "check sensor: " + s.mAddress + " swapped: " + s.mEnabled);
		}

		ArrayList<CharSequence> enabledNames = DataStore.getSensorNames(true);
		int enabledSize = enabledNames.size();
		edit.putInt(Defines.SHARED_PREF_NUM_SENSORS, enabledSize);
		for (int i = 0; i < enabledSize; i++) {
			edit.putString(Defines.SHARED_PREF_SENSOR + i, enabledNames.get(i).toString());
		}
		for (CharSequence charSequence : enabledNames) {
			Log.i("deBug", "enabled sensor: " + charSequence.toString());
		}

		edit.commit();
	}

}
