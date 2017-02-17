package edu.neu.android.wocketslib.activities.sensorswap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.sensormonitor.Defines;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Wockets;
import edu.neu.android.wocketslib.visualizerdatagenerator.SensorDataInfo;

public class Change_DoubleCheckActivity extends BaseActivity {
	private static final String TAG = "Change_DoubleCheckActivity"; 
	private Button yesBtn;
	private Button noBtn;
	private TextView tv;
	private Wockets record;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_doublecheck_activity);
		yesBtn = (Button) this.findViewById(R.id.page17yes);
		noBtn = (Button) this.findViewById(R.id.page17no);
		tv = (TextView) this.findViewById(R.id.pageseventeen_text);

		record = (Wockets) this.getIntent().getSerializableExtra("record");
		String text = this.getString(R.string.pageseventeen) + record.toString();
		tv.setText(text);

		noBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		yesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				((ApplicationManager) getApplication()).killAllActivities();
				if (!WocketInfoGrabber.isActiveInternetConnection(Change_DoubleCheckActivity.this) && Globals.IS_DEBUG) {
					Toast.makeText(Change_DoubleCheckActivity.this, "No Internet connection. Unable to upload Change events right now. Will later...",
							Toast.LENGTH_LONG).show();
				}
				try {
					record.saveSwapInfoToFile(Change_DoubleCheckActivity.this);
//					Log.i(Globals.SWAP_TAG, "Start to upload swapped sensor info.");
//					record.transmitSwappedSensorsToServer(Change_DoubleCheckActivity.this);
						
					// Store any new sensors if added.
					List<Sensor> allSensors = getPairedSensors();
					Wockets wockets = new Wockets();
					wockets.setSensors(allSensors);
					try {
						wockets.saveSensorInfoToFile(Change_DoubleCheckActivity.this);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Log.i(Globals.SWAP_TAG, "Start to set swapped sensor enabled.");
					record.saveSensorsEnabled(Change_DoubleCheckActivity.this);
					Log.i(Globals.SWAP_TAG, "Start to save swapped Zephyr.");
					record.saveHRInfoToFile(Change_DoubleCheckActivity.this);

					List<SwappedSensor> sensors = record.getSwappedSensors();
					for (SwappedSensor swappedSensor : sensors) {
						SensorDataInfo sensorData = new SensorDataInfo();
						sensorData.setSwappedData(swappedSensor);
					}
				} catch (Exception e) {
					Log.e(Globals.SWAP_TAG, "Error! Unable to write file.");
					e.printStackTrace();
				}				
				Log.i(Globals.SWAP_TAG, "Exit App, Wockets changed.");
				Intent i = new Intent(Globals.WOCKETS_REFRESH_DATA_ACTION);
				sendBroadcast(i);
			}
		});
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
}
