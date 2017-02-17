package edu.neu.android.wocketslib.activities.sensorswap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.sensormonitor.Defines;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Wockets;

public class MyWockets extends BaseActivity {
	private static final String TAG = "MyWockets"; 
	private SensorDataReceiver sensorDataReceiver = null;
	private ImageView imgBG = null;
	private Button doneBtn = null;
	private final int points[][] = { { 117, 330 }, { 160, 580 }, { 357, 330 }, { 300, 580 }, { 175, 384 }, { 271, 384 }, { 258, 238 } };
	private final int pointsNum = points.length;
	private String[] items;
	private int[] pics;
	private ImageButton[] imgBN = null;
	private TextView[] sensorInfo = null;
	private int[] btnID = { R.id.righthand, R.id.rightfoot, R.id.lefthand, R.id.leftfoot, R.id.rightpocket, R.id.leftpocket, R.id.chest };
	private int[] infoID = { R.id.mywocketsdetailrighthand, R.id.mywocketsdetailrightfoot, R.id.mywocketsdetaillefthand, R.id.mywocketsdetailleftfoot,
			R.id.mywocketsdetailrightpocket, R.id.mywocketsdetailleftpocket, R.id.mywocketsdetailhr };
	private Wockets record;
	private int[] sensorLabels;
	private final static int SENSOR_UNPAIRED = -1;
	private final static int SENSOR_PAIRED = 0;
	private String[] MacID;
	private boolean isHRUSed;
	private int[] isSwapped;
	private static final Date REASONABLE_DATE = new Date(2011 - 1900, 0, 1);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.swap_mywockets);

		imgBN = new ImageButton[pointsNum];
		sensorInfo = new TextView[pointsNum];

		DataStore.init(MyWockets.this);
		sensorDataReceiver = new SensorDataReceiver();
		isHRUSed = false;
		initilize();

		imgBG = (ImageView) findViewById(R.id.backgound);
		if (isHRUSed)
			imgBG.setImageResource(R.drawable.wockets_mywockets_body_hr);
		else
			imgBG.setImageResource(R.drawable.wockets_mywockets_body);
		/**
		 * show tips
		 */
		doneBtn = (Button) this.findViewById(R.id.mywocketsdone);
		doneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO fix logging
				Log.i(Globals.SWAP_TAG, "Exit app, done checking Wockets info");
				((ApplicationManager) getApplication()).killAllActivities();

			}

		});

	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(sensorDataReceiver, new IntentFilter(Defines.NEW_DATA_READY_BROADCAST_STRING));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sensorDataReceiver);
	}

	private void initilize() {
		Log.i(Globals.SWAP_TAG, "Initialize Wockets info, start to show wockets info");
		record = combineWockets((Wockets) getIntent().getSerializableExtra("wockets"));
		sensorLabels = new int[record.getSensors().size()];
		int fullSize = record.getSensors().size() + record.getHRData().size();
		items = new String[fullSize + 1];
		MacID = new String[fullSize];
		pics = new int[fullSize];
		isSwapped = new int[fullSize];
		for (int i = 0; i < record.getSensors().size(); i++) {
			sensorLabels[i] = sensorLabel(record.getSensors().get(i), record.getSensors());
			items[i] = record.getItemsLable()[i];
			MacID[i] = record.getItemsMacID()[i];
			isSwapped[i] = -1;
			if (items[i].equalsIgnoreCase("Red W")) {
				pics[i] = R.drawable.wockets_plain_red_w;
			} else if (items[i].equalsIgnoreCase("Green A")) {
				pics[i] = R.drawable.wockets_plain_green_a;
			} else if (items[i].equalsIgnoreCase("Red A")) {
				pics[i] = R.drawable.wockets_plain_red_a;
			} else if (items[i].equalsIgnoreCase("Green W")) {
				pics[i] = R.drawable.wockets_plain_green_w;
			} else {
				pics[i] = R.drawable.wockets_unknownsensor_plain;
			}
		}
		for (int i = record.getSensors().size(); i < fullSize; i++) {
			items[i] = "HR Sensor";
			MacID[i] = record.getHRData().get(i - record.getSensors().size()).hardwareID;
			pics[i] = R.drawable.wockets_heartratemonitor_plain;
			isSwapped[i] = -1;
		}
		if (record.getSwappedHRSensor() != null && record.getSwappedHRSensor().hardwareID != null)
			isHRUSed = true;
		items[fullSize] = "None";

		for (int i = 0; i < pointsNum; i++) {
			imgBN[i] = (ImageButton) findViewById(btnID[i]);
			imgBN[i].setVisibility(View.INVISIBLE);
			sensorInfo[i] = (TextView) findViewById(infoID[i]);
			sensorInfo[i].setVisibility(View.INVISIBLE);
		}
		setSwapInfo();
	}

	private void setSwapInfo() {
		if (record.getSwappedHRSensor() != null && record.getSwappedHRSensor().hardwareID != null) {
			isSwapped[record.getSensors().size()] = Globals.locations.length;
			sensorInfo[Globals.locations.length].setVisibility(View.VISIBLE);
			imgBN[Globals.locations.length].setVisibility(View.VISIBLE);
			imgBN[Globals.locations.length].setBackgroundResource(pics[record.getSensors().size()]);

		}
		for (int i = 0; i < record.getSwappedSensors().size(); i++)
			for (int j = 0; j < record.getSensors().size(); j++) {
				if (record.getSwappedSensors().get(i).macID.equals(MacID[j]))
					for (int k = 0; k < Globals.locations.length; k++)
						if (record.getSwappedSensors().get(i).bodyLocation.equals(Globals.locations[k])) {
							isSwapped[j] = k;
							sensorInfo[k].setVisibility(View.VISIBLE);
							imgBN[k].setVisibility(View.VISIBLE);
							imgBN[k].setBackgroundResource(pics[j]);
						}
			}
		updateDisplay();
	}

	private void updateDisplay() {
		for (int i = 0; i < isSwapped.length; i++) {
			if (isSwapped[i] >= 0) {
				final int item = i;
				imgBN[isSwapped[i]].setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						showInfoDialog(item);
					}
				});
				sensorInfo[isSwapped[i]].setText(showWocketsInfoText(i));
			}
		}

	}

	/**
	 * @param item
	 */
	private void showInfoDialog(int item) {
		String sensorID = "";
		if (item < record.getSensors().size()) {
			sensorID = record.getSensors().get(item).macID;
		} else {
			sensorID = record.getSwappedHRSensor().hardwareID;
		}
		for (edu.neu.android.wocketslib.sensormonitor.Sensor s : DataStore.mSensors) {
			if (s.mAddress.equals(sensorID)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MyWockets.this).setMessage(s.getDetailedInfo()).setPositiveButton("Got it",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				builder.show();
			}
		}
	}

	private String showWocketsInfoText(int item) {
		String sensorID = "";
		if (item < record.getSensors().size()) {
			sensorID = record.getSensors().get(item).macID;
		} else {
			sensorID = record.getSwappedHRSensor().hardwareID;
		}
		for (edu.neu.android.wocketslib.sensormonitor.Sensor s : DataStore.mSensors) {
			if (s.mAddress.equals(sensorID)) {
				SimpleDateFormat hours = new SimpleDateFormat("hh:mm a");
				SimpleDateFormat detailedTime = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
				if (s.mLastConnectionTime == null || s.mLastConnectionTime.before(REASONABLE_DATE))
					return "Last time connected:\nUnknown";

				else if (isToday(s.mLastConnectionTime))
					return "Last time connected:\n" + hours.format(s.mLastConnectionTime) + "\nBattery was at " + (int) (s.getBatteryPercentage() * 100) + "%";

				else
					return "Last time connected:\n" + detailedTime.format(s.mLastConnectionTime) + "";
			}
		}
		return "Unable to obtain";
	}

	private boolean isToday(Date d) {
		Date today = new Date();
		today.setHours(0);
		today.setMinutes(0);
		today.setSeconds(0);
		return d.after(today);
	}

	private class SensorDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			DataStore.init(context);
			Log.i(Globals.SWAP_TAG, "Sensor info text updated.");
			updateDisplay();
		}

	}

	private Wockets combineWockets(Wockets record) {
		Wockets wockets = record;
		List<Sensor> pairedSensors = getPairedSensors();
		for (Sensor s1 : pairedSensors) {
			boolean isExist = false;
			for (Sensor s2 : wockets.getSensors()) {
				if (s1.macID.equals(s2.macID)) {
					isExist = true;
					break;
				}
			}
			if (!isExist)
				wockets.getSensors().add(s1);
		}
		Log.i(Globals.SWAP_TAG, wockets.getSensorsInfo());
		return wockets;
	}

	public static ArrayList<Sensor> getPairedSensors() {
		ArrayList<Sensor> avaiSensors = new ArrayList<Sensor>();
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
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

	private int sensorLabel(Sensor unknownSensor, List<Sensor> assignedSensors) {
		ArrayList<Sensor> pairedSensors = getPairedSensors();
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
		for (String string : unpairedAssignedSensors_IDs) {
			if (unknownSensor.macID.equals(string))
				return SENSOR_UNPAIRED;
		}
		return SENSOR_PAIRED;
	}

}