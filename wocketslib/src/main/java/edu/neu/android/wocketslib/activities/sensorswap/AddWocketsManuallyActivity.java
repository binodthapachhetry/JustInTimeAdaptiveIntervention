package edu.neu.android.wocketslib.activities.sensorswap;

import java.io.File;
import java.io.FileOutputStream;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Wockets;

public class AddWocketsManuallyActivity extends BaseActivity {
	private final static String TAG = "AddWocketsManuallyActivity"; 
	private Button done = null;
	private Button cancle = null;
	private Button addNew = null;
	private ListView list = null;
	private Wockets wockets = null;
	private List<String> allIDs;
	private List<String> itemList;
	private List<Integer> iconList;
	private List<String> idList;
	private ArrayList<HashMap<String, Object>> listItem;
	private SimpleAdapter listAdapter;
	private LayoutInflater inflater;
	private View dialogLayout;
	private AlertDialog getSensor;
	private AlertDialog getID;
	// private EditText input;
	private TextView or;
	private ImageButton hrmonitor;

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		listAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.swap_addwocketsmanuallyactivity);
		done = (Button) findViewById(R.id.addwockets_done);
		cancle = (Button) findViewById(R.id.addwockets_cancle);
		addNew = (Button) findViewById(R.id.addwockets_addnew);
		list = (ListView) findViewById(R.id.addwockets_listview);

		wockets = (Wockets) getIntent().getSerializableExtra("wockets");
		allIDs = getPairedID();
		for (Sensor s : wockets.getSensors()) {
			allIDs.add(s.macID);
		}
		for (HRData hr : wockets.getHRData()) {
			allIDs.add(hr.hardwareID);
		}
		if (allIDs.size() == 0) {
			wockets = null;
			Toast.makeText(this, "No data found. Test data generated.", Toast.LENGTH_SHORT).show();
		}
		itemList = new ArrayList<String>();
		iconList = new ArrayList<Integer>();
		idList = new ArrayList<String>();
		for (int i = 0; i < wockets.getSensors().size(); i++) {
			idList.add(wockets.getItemsMacID()[i]);
			itemList.add(wockets.getItemsLable()[i]);

			if (wockets.getItemsLable()[i].equalsIgnoreCase("Red W")) {
				iconList.add(R.drawable.wockets_plain_red_w);
			} else if (wockets.getItemsLable()[i].equalsIgnoreCase("Green A")) {
				iconList.add(R.drawable.wockets_plain_green_a);
			} else if (wockets.getItemsLable()[i].equalsIgnoreCase("Red A")) {
				iconList.add(R.drawable.wockets_plain_red_a);
			} else if (wockets.getItemsLable()[i].equalsIgnoreCase("Green W")) {
				iconList.add(R.drawable.wockets_plain_green_w);
			} else {
				iconList.add(R.drawable.wockets_unknownsensor);
			}
		}
		if (wockets.getHRData().size() > 0) {
			itemList.add("HR Sensor");
			iconList.add(R.drawable.wockets_heartratemonitor_plain);
			idList.add(wockets.getHRData().get(0).hardwareID);
		}

		listItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < itemList.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("item", itemList.get(i));
			map.put("icon", iconList.get(i));
			map.put("id", idList.get(i));
			listItem.add(map);
		}
		listAdapter = new SimpleAdapter(this, listItem, R.layout.swap_addwocketslistview, new String[] { "item", "icon", "id" }, new int[] {
				R.id.addwockets_itemTitle, R.id.addwockets_itemIcon, R.id.addwockets_itemID });
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				itemList.remove(arg2);
				iconList.remove(arg2);
				idList.remove(arg2);
				listItem.remove(arg2);
				listAdapter.notifyDataSetChanged();
			}
		});
		addNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (allIDs.size() > listItem.size()) {
					or.setVisibility(View.VISIBLE);
					hrmonitor.setVisibility(View.VISIBLE);
					for (int i = 0; i < listItem.size(); i++) {
						String item = (String) listItem.get(i).get("item");
						if (item.equalsIgnoreCase("HR Sensor")) {
							or.setVisibility(View.GONE);
							hrmonitor.setVisibility(View.GONE);
						}
					}
					getSensor.show();
				} else {
					Toast.makeText(AddWocketsManuallyActivity.this, "No sensors available.", Toast.LENGTH_SHORT).show();
				}
			}

		});
		done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				encapsulater();
				Intent i = new Intent(AddWocketsManuallyActivity.this, SwapMenuActivity.class);
				i.putExtra("flag", 1);
				startActivity(i);
				finish();
			}

		});
		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(AddWocketsManuallyActivity.this, SwapMenuActivity.class);
				i.putExtra("flag", 0);
				startActivity(i);
				finish();
			}
		});
		inflater = getLayoutInflater();
		dialogLayout = inflater.inflate(R.layout.swap_addwocketsdialog, (ViewGroup) findViewById(R.id.addwockets_dialoglayout));
		getSensor = new AlertDialog.Builder(AddWocketsManuallyActivity.this).setIcon(android.R.drawable.ic_dialog_info).setView(dialogLayout).create();
		ImageButton redw = (ImageButton) dialogLayout.findViewById(R.id.redw);
		ImageButton reda = (ImageButton) dialogLayout.findViewById(R.id.reda);
		ImageButton greenw = (ImageButton) dialogLayout.findViewById(R.id.greenw);
		ImageButton greena = (ImageButton) dialogLayout.findViewById(R.id.greena);
		hrmonitor = (ImageButton) dialogLayout.findViewById(R.id.hrmonitor);
		or = (TextView) dialogLayout.findViewById(R.id.or);
		redw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				itemList.add("Red W");
				iconList.add(R.drawable.wockets_plain_red_w);
				getSensor.dismiss();
				getID();
			}

		});
		reda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				itemList.add("Red A");
				iconList.add(R.drawable.wockets_plain_red_a);
				getSensor.dismiss();
				getID();
			}

		});
		greenw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				itemList.add("Green W");
				iconList.add(R.drawable.wockets_plain_green_w);
				getSensor.dismiss();
				getID();
			}

		});
		greena.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				itemList.add("Green A");
				iconList.add(R.drawable.wockets_plain_green_a);
				getSensor.dismiss();
				getID();
			}

		});
		hrmonitor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				itemList.add("HR Sensor");
				iconList.add(R.drawable.wockets_heartratemonitor_plain);
				getSensor.dismiss();
				getID();
			}
		});

	}

	private void encapsulater() {
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<SwappedSensor> swappedSensors = new ArrayList<SwappedSensor>();
		ArrayList<HRData> HRDatas = new ArrayList<HRData>();
		for (int i = 0; i < itemList.size(); i++) {
			if (itemList.get(i).equalsIgnoreCase("Red W")) {
				Sensor s = new Sensor();
				s.color = "Red";
				s.label = "W";
				s.macID = idList.get(i);
				sensors.add(s);
			} else if (itemList.get(i).equalsIgnoreCase("Red A")) {
				Sensor s = new Sensor();
				s.color = "Red";
				s.label = "A";
				s.macID = idList.get(i);
				sensors.add(s);
			} else if (itemList.get(i).equalsIgnoreCase("Green W")) {
				Sensor s = new Sensor();
				s.color = "Green";
				s.label = "W";
				s.macID = idList.get(i);
				sensors.add(s);
			} else if (itemList.get(i).equalsIgnoreCase("Green A")) {
				Sensor s = new Sensor();
				s.color = "Green";
				s.label = "A";
				s.macID = idList.get(i);
				sensors.add(s);
			} else if (itemList.get(i).equalsIgnoreCase("HR Sensor")) {
				HRData hr = new HRData();
				hr.hardwareID = idList.get(i);
				HRDatas.add(hr);
			}

		}
		try {
			Wockets wockets = new Wockets();
			wockets.setSensors(sensors);
			wockets.setSwappedSensors(swappedSensors);
			wockets.setHRData(HRDatas);
			save2File(wockets);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void save2File(Wockets wockets) throws Exception {
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		String json = gson.toJson(wockets, Wockets.class);
		File data = new File(Globals.WOCKETS_INFO_JSON_FILE_PATH);
		if(!data.getParentFile().isDirectory())
			data.getParentFile().mkdirs();
		if(!data.exists())
			data.createNewFile();
		FileOutputStream fos = new FileOutputStream(data);
		fos.write(json.getBytes());
		fos.flush();
		fos.close();

		/**
		 * save a copy in SD card
		 */
		// InputStream is = openFileInput(Constant.fileName);
		// FileUtils fu = new FileUtils();
		// fu.write2SDFromInput("/data/", Constant.fileName, is);
	}

	private List<String> getPairedID() {
		List<String> avaiIDs = new ArrayList<String>();
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
				if (!isExist)
					avaiIDs.add(address);
				isExist = false;
			}
		}
		return avaiIDs;
	}

	private void getID() {
		final List<String> avaiID = new ArrayList<String>();
		boolean isExist = false;
		for (String string : allIDs) {
			for (HashMap<String, Object> map : listItem) {
				if (((String) map.get("id")).equalsIgnoreCase(string))
					isExist = true;
			}
			if (!isExist)
				avaiID.add(string);
			isExist = false;
		}
		final String[] ids = (String[]) avaiID.toArray(new String[avaiID.size()]);
		dialogLayout = inflater.inflate(R.layout.swap_addwocketsenteriddialog, (ViewGroup) findViewById(R.id.addwockets_enterid));
		getID = new AlertDialog.Builder(AddWocketsManuallyActivity.this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Select the sensor ID")
				.setSingleChoiceItems(ids, 0, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						idList.add(ids[which]);
					}
				}).setPositiveButton("Done", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (idList.size() < itemList.size())
							idList.add(ids[0]);
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("item", itemList.get(itemList.size() - 1));
						map.put("icon", iconList.get(iconList.size() - 1));
						map.put("id", idList.get(idList.size() - 1));
						listItem.add(map);
						getID.dismiss();
						listAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						getID.dismiss();
						listAdapter.notifyDataSetChanged();
					}
				}).create();
		getID.show();
	}

}
