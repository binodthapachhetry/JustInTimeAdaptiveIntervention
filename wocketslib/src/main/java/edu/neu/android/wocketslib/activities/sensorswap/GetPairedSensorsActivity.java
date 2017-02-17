package edu.neu.android.wocketslib.activities.sensorswap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Wockets;

public class GetPairedSensorsActivity extends BaseActivity {
	private static final String TAG = "GetPairedSensorsActivity";
	private Button next = null;
	private Button cancel = null;
	// private Button addNew = null;
	private ListView list = null;
	private Wockets wockets = null;
	private List<String> itemList;
	// private List<Integer> iconList;
	private List<String> idList;
	private ArrayList<HashMap<String, Object>> listItem;
	private SimpleAdapter listAdapter;
	private View dialogLayout;
	private LayoutInflater inflater;
	private AlertDialog getSensor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.swap_getpairedsensors);
		next = (Button) findViewById(R.id.getpairedsensors_next);
		cancel = (Button) findViewById(R.id.getpairedsensors_cancel);
		list = (ListView) findViewById(R.id.getpairedsensors_listView);

		itemList = new ArrayList<String>();
		idList = new ArrayList<String>();
		// iconList = new ArrayList<Integer>();
		int icon = R.drawable.wockets_unknownsensor;
		wockets = new Wockets();
		wockets = (Wockets) getIntent().getSerializableExtra("wockets");
		getPairedID();

		listItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < itemList.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("item", itemList.get(i));
			map.put("icon", icon);
			map.put("id", idList.get(i));
			listItem.add(map);
		}
		listAdapter = new SimpleAdapter(this, listItem, R.layout.swap_addwocketslistview, new String[] { "item", "icon", "id" }, new int[] {
				R.id.addwockets_itemTitle, R.id.addwockets_itemIcon, R.id.addwockets_itemID });
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				setColorLabel(arg2);
				getSensor.show();
			}
		});
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (encapsulater()) {
					Intent i = new Intent(GetPairedSensorsActivity.this, AddWocketsManuallyActivity.class);
					i.putExtra("wockets", wockets);
					startActivity(i);
					finish();
				}
			}

		});
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(GetPairedSensorsActivity.this, SwapMenuActivity.class);
				i.putExtra("flag", 0);
				startActivity(i);
				finish();
			}

		});
	}

	private void getPairedID() {
		boolean isExist = false;
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			Iterator<BluetoothDevice> itr = devices.iterator();
			while (itr.hasNext()) {
				BluetoothDevice dev = itr.next();
				String[] ss = dev.getAddress().split(":");
				String address = new String();
				for (String s : ss) {
					address += s;
				}
				for (Sensor s : wockets.getSensors()) {
					if (address.equalsIgnoreCase(s.macID)) {
						isExist = true;
					}
				}
				for (HRData hr : wockets.getHRData()) {
					if (address.equalsIgnoreCase(hr.hardwareID)) {
						isExist = true;
					}
				}
				if (!isExist) {
					itemList.add(dev.getName());
					idList.add(address);
				}
				isExist = false;
			}
		}
	}

	private void setColorLabel(final int position) {
		final HashMap<String, Object> map = listItem.get(position);
		final HashMap<String, Object> temp = new HashMap<String, Object>();
		inflater = getLayoutInflater();
		dialogLayout = inflater.inflate(R.layout.swap_addwocketsdialog, (ViewGroup) findViewById(R.id.addwockets_dialoglayout));
		getSensor = new AlertDialog.Builder(GetPairedSensorsActivity.this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Select color and label")
				.setView(dialogLayout).setPositiveButton("Done", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (temp.size() > 0) {
							listItem.remove(position);
							listItem.add(position, temp);
							listAdapter.notifyDataSetChanged();
						}
						getSensor.dismiss();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						getSensor.dismiss();
					}
				}).setNeutralButton("Delete", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						listItem.remove(position);
						listAdapter.notifyDataSetChanged();
						getSensor.dismiss();

					}
				}).create();
		final ImageButton redw = (ImageButton) dialogLayout.findViewById(R.id.redw);
		final ImageButton reda = (ImageButton) dialogLayout.findViewById(R.id.reda);
		final ImageButton greenw = (ImageButton) dialogLayout.findViewById(R.id.greenw);
		final ImageButton greena = (ImageButton) dialogLayout.findViewById(R.id.greena);
		final ImageButton hrmonitor = (ImageButton) dialogLayout.findViewById(R.id.hrmonitor);
		TextView or = (TextView) dialogLayout.findViewById(R.id.or);
		redw.setAlpha(100);
		reda.setAlpha(100);
		greenw.setAlpha(100);
		greena.setAlpha(100);
		hrmonitor.setAlpha(100);

		String item = (String) listItem.get(position).get("item");
		if (item.equalsIgnoreCase("Red W"))
			redw.setAlpha(255);
		else if (item.equalsIgnoreCase("Red A"))
			reda.setAlpha(255);
		else if (item.equalsIgnoreCase("Green W"))
			greenw.setAlpha(255);
		else if (item.equalsIgnoreCase("Green A"))
			greena.setAlpha(255);
		or.setVisibility(View.VISIBLE);
		hrmonitor.setVisibility(View.VISIBLE);
		for (int i = 0; i < listItem.size(); i++) {
			item = (String) listItem.get(i).get("item");
			if (item.equalsIgnoreCase("HR Sensor")) {
				or.setVisibility(View.GONE);
				hrmonitor.setVisibility(View.GONE);
			}
		}
		if (wockets.getHRData().size() > 0) {
			or.setVisibility(View.GONE);
			hrmonitor.setVisibility(View.GONE);
		}
		redw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				temp.put("item", "Red W");
				temp.put("icon", R.drawable.wockets_plain_red_w);
				temp.put("id", map.get("id"));
				redw.setAlpha(255);
				reda.setAlpha(100);
				greenw.setAlpha(100);
				greena.setAlpha(100);
				hrmonitor.setAlpha(100);
			}

		});
		reda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				temp.put("item", "Red A");
				temp.put("icon", R.drawable.wockets_plain_red_a);
				temp.put("id", map.get("id"));
				redw.setAlpha(100);
				reda.setAlpha(255);
				greenw.setAlpha(100);
				greena.setAlpha(100);
				hrmonitor.setAlpha(100);
			}

		});
		greenw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				temp.put("item", "Green W");
				temp.put("icon", R.drawable.wockets_plain_green_w);
				temp.put("id", map.get("id"));
				redw.setAlpha(100);
				reda.setAlpha(100);
				greenw.setAlpha(255);
				greena.setAlpha(100);
				hrmonitor.setAlpha(100);
			}

		});
		greena.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				temp.put("item", "Green A");
				temp.put("icon", R.drawable.wockets_plain_green_a);
				temp.put("id", map.get("id"));
				redw.setAlpha(100);
				reda.setAlpha(100);
				greenw.setAlpha(100);
				greena.setAlpha(255);
				hrmonitor.setAlpha(100);
			}

		});
		hrmonitor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				temp.put("item", "HR Sensor");
				temp.put("icon", R.drawable.wockets_heartratemonitor_plain);
				temp.put("id", map.get("id"));
				redw.setAlpha(100);
				reda.setAlpha(100);
				greenw.setAlpha(100);
				greena.setAlpha(100);
				hrmonitor.setAlpha(255);
			}
		});
	}

	private boolean encapsulater() {
		boolean isAllLabeled = true;
		List<Sensor> sensors = wockets.getSensors();
		// List<SwappedSensor> swappedSensors = wockets.getSwappedSensors();
		List<HRData> HRDatas = wockets.getHRData();
		for (HashMap<String, Object> map : listItem) {

			if (((String) map.get("item")).equalsIgnoreCase("Red W")) {
				Sensor s = new Sensor();
				s.color = "Red";
				s.label = "W";
				s.macID = ((String) map.get("id"));
				sensors.add(s);
			} else if (((String) map.get("item")).equalsIgnoreCase("Red A")) {
				Sensor s = new Sensor();
				s.color = "Red";
				s.label = "A";
				s.macID = ((String) map.get("id"));
				sensors.add(s);
			} else if (((String) map.get("item")).equalsIgnoreCase("Green W")) {
				Sensor s = new Sensor();
				s.color = "Green";
				s.label = "W";
				s.macID = ((String) map.get("id"));
				sensors.add(s);
			} else if (((String) map.get("item")).equalsIgnoreCase("Green A")) {
				Sensor s = new Sensor();
				s.color = "Green";
				s.label = "A";
				s.macID = ((String) map.get("id"));
				sensors.add(s);
			} else if (((String) map.get("item")).equalsIgnoreCase("HR Sensor")) {
				HRData hr = new HRData();
				hr.hardwareID = ((String) map.get("id"));
				HRDatas.add(hr);
			} else {
				Toast.makeText(this, "Existing items not labeled. Please set up the label for item or delete it.", Toast.LENGTH_SHORT).show();
				isAllLabeled = false;
			}
		}
		return isAllLabeled;
	}

}
