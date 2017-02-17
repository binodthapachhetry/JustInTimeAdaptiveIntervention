package edu.neu.android.wocketslib.activities.sensorswap;

import java.io.IOException;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;
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
import edu.neu.android.wocketslib.utils.NetworkDetector;
import edu.neu.android.wocketslib.utils.Util;
import edu.neu.android.wocketslib.utils.Wockets;

public class SwapWocketsBodyDiagramActivity extends BaseActivity {
	private static final String TAG = "SwapWocketsBodyDiagramActivity"; 
	
	private int hrFlag = 0;
	private ImageView imgBG = null;
//	private Button showTips = null;
	private Button doneBtn = null;
	private Button nmBtn = null;
	private AlertDialog selectLocation;
	private View dialogLayout;
	private static float radius = 40;
	private ListView dialogList;
	private final int points[][]={{117,330},{160,580},{357,330},{300,580},{175,384},{271,384},{258,238}};
	private final int pointsNum = points.length;	
	private String[] items;
	private int[] pics;
	private int[] icons;
	private ImageButton[] imgBN = new ImageButton[pointsNum];
	private int[] btnID = {R.id.righthand,R.id.rightfoot,R.id.lefthand,R.id.leftfoot,R.id.rightpocket,R.id.leftpocket,R.id.chest};
	private boolean[] isExist;
	private Wockets record;
	private int[] sensorLabels;
	private final static int SENSOR_UNPAIRED = -1;
	private final static int SENSOR_PAIRED = 0;
	private String[] MacID;
	private int[] tipBtn = {114,90};

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);

		setContentView(R.layout.swap_changewocketsbodydiagram_activity);
		initilize();
		
		imgBG = (ImageView)findViewById(R.id.backgound);
		if(hrFlag > 0)
			imgBG.setImageResource(R.drawable.wockets_swap_body_hr);       		
		else if(hrFlag == 0)
			imgBG.setImageResource(R.drawable.wockets_swap_body);       

		doneBtn = (Button)this.findViewById(R.id.page20done);
		doneBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(countLabel()<2){
					Intent i = new Intent(SwapWocketsBodyDiagramActivity.this,Swap_OnlyOneWocketsSelectedWarningActivity.class);
					i.putExtra("counter", countLabel());
					i.putExtra("record", record);
					startActivity(i);
				}
				else if(countLabel()==2){
					if(isRightLabel()){
						Intent i = new Intent(SwapWocketsBodyDiagramActivity.this,Swap_DoubleCheckActivity.class);
						i.putExtra("record", record);
						startActivity(i);
					}
					else{
						Intent i = new Intent(SwapWocketsBodyDiagramActivity.this,Swap_RecommendingLocationsActivity.class);
						i.putExtra("record", record);
						startActivity(i);
					}
				}

			}

		});
		nmBtn = (Button)this.findViewById(R.id.page20nm);
		nmBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!isSwapChanged()){
					Log.i(Globals.SWAP_TAG, "Exit app, no Wockets changed");
					((ApplicationManager) getApplication()).killAllActivities();
				}
				else{
					Intent i = new Intent(SwapWocketsBodyDiagramActivity.this, Swap_CancellingWarningActivity.class);
					startActivity(i);
				}
			}

		});
	
	}

	private void initilize(){
		Log.i(Globals.SWAP_TAG, "Initilize Wockets info, start to change Wockets");
		record = combineWockets((Wockets) getIntent().getSerializableExtra("wockets"));
		record.resetSwap(SwapWocketsBodyDiagramActivity.this);
		hrFlag = record.getHRData().size();
		int fullSize = record.getSensors().size()+record.getHRData().size();
		sensorLabels = new int[record.getSensors().size()];
		items = new String[fullSize+1];
		MacID = new String[fullSize+1];
		pics = new int[fullSize];
		icons = new int[fullSize];
		for(int i = 0; i< record.getSensors().size();i++){
			sensorLabels[i] = sensorLabel(record.getSensors().get(i),record.getSensors());
			items[i] = record.getItemsLable()[i];
			MacID[i] = record.getItemsMacID()[i];
			if(items[i].equalsIgnoreCase("Red W")){
				pics[i] = R.drawable.wockets_red_w;
				icons[i] = R.drawable.wockets_plain_red_w;
			}
			else if(items[i].equalsIgnoreCase("Green A")){
				pics[i] = R.drawable.wockets_green_a;
				icons[i] = R.drawable.wockets_plain_green_a;
			}
			else if(items[i].equalsIgnoreCase("Red A")){
				pics[i] = R.drawable.wockets_red_a;
				icons[i] = R.drawable.wockets_plain_red_a;
			}
			else if(items[i].equalsIgnoreCase("Green W")){
				pics[i] = R.drawable.wockets_green_w;
				icons[i] = R.drawable.wockets_plain_green_w;
			}
			else{
				pics[i] = R.drawable.wockets_unknownsensor;
				icons[i] = R.drawable.wockets_unknownsensor_plain;
			}
		}
		for (int i = record.getSensors().size(); i < fullSize; i++) {
			items[i] = "HR Sensor";
			MacID[i] = record.getHRData().get(i-record.getSensors().size()).hardwareID;
			pics[i] = R.drawable.wockets_heartratemonitor;
			icons[i] = R.drawable.wockets_heartratemonitor_plain;
		}
		items[fullSize] = "None";
		isExist = new boolean[pics.length - hrFlag];
		for(int i = 0; i < pointsNum;i++){
			if(i<(pics.length - hrFlag))
				isExist[i] = false;
			imgBN[i] = (ImageButton)findViewById(btnID[i]);
			imgBN[i].setVisibility(View.INVISIBLE);

			LayoutInflater inflater = getLayoutInflater();
			dialogLayout = inflater.inflate(R.layout.swap_wocketsselection_dialog, 
					(ViewGroup)findViewById(R.id.dialogview));		
			selectLocation = new AlertDialog.Builder(this)  
			.setView(dialogLayout)
			.create();        
		}
		try {
			setDefaultLabel();
		} catch (IOException e) {
			Log.e(TAG, "Error. IOException in SwapWocketsBodyDiagramActivity: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void addList(final int position, int num){
		if(num < 3){
			int counter = num;
			final int[] itemID = new int[pics.length - counter - hrFlag];
			counter = 0;
			for (int i = 0; i < pics.length - hrFlag; i++) {
				if(isExist[i])
					counter++;
				else{
					itemID[i-counter] = 0;
					itemID[i-counter] +=counter;
				}			
			}
			// count repeating lables 
			int repeatLables = 0;
			for(int i = 0; i < items.length -1;i++){
				for(int j = i+1; j < items.length -1; j++)
					if(items[i].equals(items[j]))
						repeatLables++;
			}
			if(repeatLables == 0){
				dialogList = (ListView)dialogLayout.findViewById(R.id.dialoglistview);      
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < pics.length  - hrFlag; i++) {
					if(!isExist[i]){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("title", items[i]);
						map.put("pic", icons[i]);
						if(sensorLabels[i] == SENSOR_UNPAIRED)
							map.put("ID", "(Unpaired)");
						else
							map.put("ID", null);
						listItem.add(map);
					}
				}
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", items[pics.length]);
				map.put("pic", null);
				map.put("ID", null);
				listItem.add(map);
				SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,  
						R.layout.swap_wocketsselection_dialoglist,            
						new String[] {"pic","title","ID"},    
						new int[] {R.id.itemIcon,R.id.itemTitle,R.id.itemID}   
				);   
				dialogList.setAdapter(listItemAdapter);
			}
			else if(repeatLables > 0){
				dialogList = (ListView)dialogLayout.findViewById(R.id.dialoglistview);      
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < pics.length  - hrFlag; i++) {
					if(!isExist[i]){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("title", items[i]);
						map.put("pic", icons[i]);
						if(sensorLabels[i] == SENSOR_UNPAIRED)
							map.put("ID", MacID[i].substring(MacID[i].length()-5)+" (Unpaired)");
						else
							map.put("ID", MacID[i].substring(MacID[i].length()-5));
						listItem.add(map);
					}
				}
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", items[pics.length]);
				map.put("pic", null);
				map.put("ID", null);
				listItem.add(map);
				SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,  
						R.layout.swap_wocketsselection_dialoglist,            
						new String[] {"pic","title","ID"},    
						new int[] {R.id.itemIcon,R.id.itemTitle,R.id.itemID}   
				);   
				dialogList.setAdapter(listItemAdapter);
				
			}
			dialogList.setOnItemClickListener(new OnItemClickListener(){
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int item,
						long arg3) {
					if(itemID.length>0&&item<itemID.length){
						final int arg2 = item + itemID[item];
						if(arg2<pics.length - hrFlag){
							if(sensorLabels[arg2] == SENSOR_UNPAIRED){
								AlertDialog.Builder builder = new AlertDialog.Builder(SwapWocketsBodyDiagramActivity.this);
								builder.setMessage("You must pair this Wockets: "+MacID[arg2]
										+" before you can use it.")
								.setPositiveButton("Pair Now", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										Intent intentBluetooth = new Intent();
										intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
										startActivity(intentBluetooth); 					
									}
								})
								.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										Log.i(Globals.SWAP_TAG, "Exit app, some assigned Wockets are not paired, do later");
									}
								})
								.show();
							}
							else{
								setLabelButton(position, arg2);
							}
						}
					}
					selectLocation.dismiss();
				}
	
			});
		}
		/**
		 * for hr sensor
		 */
		else if(num == 3){
			dialogList = (ListView)dialogLayout.findViewById(R.id.dialoglistview);      
			ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
			if(record.getHRData().size() == 1){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", "HR Sensor");
				map.put("pic", icons[record.getSensors().size()+record.getHRData().size()-1]);
				listItem.add(map);
			}
			else{
				for (int i = 0; i < record.getHRData().size(); i++) {
					String name = "HR Sensor";
					HashMap<String, Object> map = new HashMap<String, Object>();
					List<String> pairedHRNames = new ArrayList<String>();
					List<String> pairedHRAddress = new ArrayList<String>();
					if( BluetoothAdapter.getDefaultAdapter() != null){
						DataStore.unsetPairedFlagAllSensors(); 
						Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
						Iterator<BluetoothDevice> itr = devices.iterator();
						while( itr.hasNext()){
							BluetoothDevice dev = itr.next();
							pairedHRNames.add(dev.getName());
							pairedHRAddress.add(Util.removeColons(dev.getAddress()));
					    }
					}
					for (int j = 0; j < pairedHRAddress.size(); j++) {
						if(record.getHRData().get(i).hardwareID.equals(pairedHRAddress.get(j))){
							name = pairedHRNames.get(j);
						}
					}
					map.put("title", name);
					map.put("pic", icons[record.getSensors().size()+record.getHRData().size()-1]);
					listItem.add(map);
				}
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("title", "None");
			map.put("pic", null);
			listItem.add(map);

			SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,  
					R.layout.swap_wocketsselection_dialoglist,            
					new String[] {"pic","title"},    
					new int[] {R.id.itemIcon,R.id.itemTitle}   
			);   
			dialogList.setAdapter(listItemAdapter);
			dialogList.setOnItemClickListener(new OnItemClickListener(){
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int item,
						long arg3) {
					if(item < record.getHRData().size()){
						setLabelButton(6, item);
						selectLocation.dismiss();
					}
					else
						selectLocation.dismiss();					
				}
	
			});

		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = 0;float y = 0;
		int itemNum = 0;
		if(record.getHRData().size()>0)
			itemNum = pointsNum;
		else
			itemNum = pointsNum-1;
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			x = event.getX(); y = event.getY();
			if(x!=0&&y!=0){
				float[] onTouch = new float[itemNum+1];
				for (int i = 0; i < itemNum ; i++) {
					onTouch[i] = -1;
					float xDis = Math.abs(x-points[i][0]);
					float yDis =  Math.abs(y-points[i][1]-60);

					if((xDis < radius)&& (yDis < radius)){
						onTouch[i] = xDis*xDis +yDis*yDis;
					}
				}
				onTouch[itemNum] = -1;
				float xDis = Math.abs(x-tipBtn[0]);
				float yDis =  Math.abs(y-tipBtn[1]-60);
				if((xDis < radius)&& (yDis < radius)){
					onTouch[itemNum] = xDis*xDis +yDis*yDis;
				}

				int item = itemNum+1;
				float value = radius*radius*2;
				for (int i = 0; i < itemNum+1; i++) {
					if((onTouch[i]>=0) && onTouch[i]<value){
						value = onTouch[i];
						item = i;
					}
				}
				
				if(item<itemNum){
					//					if(isDots[item]){  	
					if((record.getHRData().size()>0)&&(item == pointsNum - 1)){
						addList(item,3);
						String label = "Select the Heart Rate Sensor";
						TextView tv = (TextView)dialogLayout.findViewById(R.id.dialogtitle);
						tv.setText(label);
						selectLocation.show();
					}
					else if(countLabel() < 2){					
						addList(item,countLabel());
						String label = this.getString(R.string.label)+" "+Globals.locations[item];
						TextView tv = (TextView)dialogLayout.findViewById(R.id.dialogtitle);
						tv.setText(label);
						selectLocation.show();								
					}
					else if(countLabel() == 2){
						Intent intent = new Intent(SwapWocketsBodyDiagramActivity.this,Swap_DisplayMoreThanTwoWocketsSelectedWarningActivity.class);
						intent.putExtra("location", Globals.locations[item]);
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						this.startActivity(intent);
					}
				}
				else if(item == itemNum){
					if(NetworkDetector.isConnected(SwapWocketsBodyDiagramActivity.this)){
						Intent i = new Intent(SwapWocketsBodyDiagramActivity.this, DisplayWebTipsActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						String address = "http://web.mit.edu/city/wockets/swap.html";
						i.putExtra("address", address);
						startActivity(i);
					}
					else{
						Toast.makeText(SwapWocketsBodyDiagramActivity.this, "No network is available", Toast.LENGTH_LONG).show();
					}				
				}
			}
		}

		return true;
	}
	private void setLabelButton(final int position, final int arg2){
		if(position < 6){
			imgBN[position].setVisibility(View.VISIBLE);
			imgBN[position].setBackgroundResource(pics[arg2]);
			imgBN[position].setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View arg0) {
					imgBN[position].setVisibility(View.GONE);
					isExist[arg2] = false;
					record.swap(SwapWocketsBodyDiagramActivity.this,MacID[arg2], "delete");
				}
			});
			isExist[arg2] = true;
			record.swap(SwapWocketsBodyDiagramActivity.this,MacID[arg2], Globals.locations[position]);
			Log.i(Globals.SWAP_TAG, "Sensor changed: " +record.getItemsLable()[arg2] + " to "+Globals.locations[position]);
		}
		else if( position == 6){
			imgBN[position].setVisibility(View.VISIBLE);
			imgBN[position].setBackgroundResource(pics[record.getSensors().size()+record.getHRData().size()-1]);
			imgBN[position].setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View arg0) {
					imgBN[position].setVisibility(View.GONE);
					record.setSwappedHRSensor(new HRData());
				}
			});
			record.setSwappedHRSensor(record.getHRData().get(arg2));	
			Log.i(Globals.SWAP_TAG, "Sensor changed: Zephyr to the chest");
		}
	}
	private boolean isRightLabel(){
		List<SwappedSensor> swappedSensors = record.getSwappedSensors();
		List<Sensor> sensors = record.getSensors();
		if(swappedSensors == null || swappedSensors.size() < 2)
			return false;
		else{
			SwappedSensor swappedSensor1 = swappedSensors.get(0);
			SwappedSensor swappedSensor2 = swappedSensors.get(1);
			Sensor sensor1 = null;
			Sensor sensor2 = null;
			for (Sensor sensor : sensors) {
				if(swappedSensor1.macID.equals(sensor.macID))
					sensor1 = sensor;
				else if(swappedSensor2.macID.equals(sensor.macID))
					sensor2 = sensor;
			}
			if(sensor1 == null || sensor2 == null)
				return false;
			boolean isSameColor = false;
			boolean isSameSide = false;
			
			if(sensor1.color != null && sensor2.color != null &&
					sensor1.color.equals(sensor2.color))
				isSameColor = true;
			if(isSameColor){
				String[] sensor1_bodylocation = swappedSensor1.bodyLocation.toLowerCase().split(" ");
				String[] sensor2_bodylocation = swappedSensor2.bodyLocation.toLowerCase().split(" ");
				String sensor1_side = sensor1_bodylocation[0];
				String sensor2_side = sensor2_bodylocation[0];

				if(sensor1_side.equals(sensor2_side))
					isSameSide = true;
				if(isSameSide){
					if((sensor1.label.toLowerCase().contains("w") 
							&& swappedSensor1.bodyLocation.toLowerCase().contains("wrist")
					&& sensor2.label.toLowerCase().contains("a") 
							&& swappedSensor2.bodyLocation.toLowerCase().contains("ankle"))
				||(sensor2.label.toLowerCase().contains("w") 
						&& swappedSensor2.bodyLocation.toLowerCase().contains("wrist")
				&& sensor1.label.toLowerCase().contains("a") 
						&& swappedSensor1.bodyLocation.toLowerCase().contains("ankle"))){
						Log.i(Globals.SWAP_TAG, "Label checker: wearing same colored W on the wrist and A on the ankle.");
						return true;			
					}
				}		
			}
			Log.i(Globals.SWAP_TAG, "Label checker: wockets wore not as suggested.");
			return false;
		}
	}

	private int countLabel(){
		int counter = 0;
		for (int j = 0; j < pics.length - hrFlag; j++) {
			if(isExist[j])
				counter++;
		}
		return counter;
	}
	private void setDefaultLabel() throws IOException{
		if(record.getSwappedHRSensor() != null && record.getSwappedHRSensor().hardwareID != null){
			for (int i = 0; i < record.getHRData().size(); i++) {
				if(record.getSwappedHRSensor().hardwareID.equals(record.getHRData().get(i).hardwareID))
					setLabelButton(6,i);
			}
		}
		if(record.getSwappedSensors() != null)
		for(int i = 0; i < record.getSwappedSensors().size();i++)
			for(int j = 0; j<record.getSensors().size();j++){
				if(record.getSwappedSensors().get(i).macID.equals(MacID[j]))
					for(int k = 0; k<Globals.locations.length;k++)
						if(record.getSwappedSensors().get(i).bodyLocation.equals(Globals.locations[k])){
							final int position = k;
							final int item = j;
							imgBN[position].setVisibility(View.VISIBLE);
							imgBN[position].setBackgroundResource(pics[item]);
							imgBN[position].setOnClickListener(new OnClickListener(){
					
								@Override
								public void onClick(View arg0) {
									imgBN[position].setVisibility(View.GONE);
									isExist[item] = false;
									record.swap(SwapWocketsBodyDiagramActivity.this,MacID[item], "delete");
								}
							});
							isExist[item] = true;
						}
			}
	}
	private boolean isSwapChanged(){
		List<SwappedSensor> sensors_local = WocketInfoGrabber.getSwappedSensors(SwapWocketsBodyDiagramActivity.this);
		List<SwappedSensor> sensors_changed = record.getSwappedSensors();
		HRData HRSensor_Local = WocketInfoGrabber.getHRSensor(SwapWocketsBodyDiagramActivity.this);
		HRData HRSensor_changed = record.getSwappedHRSensor();
		
		String HRSensor_Local_ID = "";
		if(HRSensor_Local != null && HRSensor_Local.hardwareID != null)
			HRSensor_Local_ID = HRSensor_Local.hardwareID;
		String HRSensor_changed_ID = "";
		if(HRSensor_changed != null && HRSensor_changed.hardwareID != null)
			HRSensor_changed_ID = HRSensor_changed.hardwareID;
		if(!HRSensor_Local_ID.equals(HRSensor_changed_ID))
			return true;
		
		if(sensors_local.size() == 0 && sensors_changed.size() == 0)
			return false;
		else if(sensors_changed.size() != sensors_local.size()){
			return true;
		}
		else if(sensors_changed.size() == 1){	
			if(sensors_changed.get(0).macID.equals(sensors_local.get(0).macID)
					&& sensors_changed.get(0).bodyLocation.equals(sensors_local.get(0).bodyLocation))
				return false;
			else
				return true;
		}
		else{
			if(sensors_changed.get(0).macID.equals(sensors_local.get(0).macID)
					&& sensors_changed.get(0).bodyLocation.equals(sensors_local.get(0).bodyLocation)){
				if(sensors_changed.get(1).macID.equals(sensors_local.get(1).macID)
						&& sensors_changed.get(1).bodyLocation.equals(sensors_local.get(1).bodyLocation))
					return false;
				else 
					return true;
			}
			else if(sensors_changed.get(0).macID.equals(sensors_local.get(1).macID)
					&& sensors_changed.get(0).bodyLocation.equals(sensors_local.get(1).bodyLocation)){
				if(sensors_changed.get(1).macID.equals(sensors_local.get(0).macID)
						&& sensors_changed.get(1).bodyLocation.equals(sensors_local.get(0).bodyLocation))
					return false;
				else 
					return true;
			}
			else
				return true;
		}			
	}
	private Wockets combineWockets(Wockets record){
		Wockets wockets = record;
		List<Sensor> pairedSensors = getPairedSensors();
		for (Sensor s1 : pairedSensors) {
			boolean isExist = false;
			for (Sensor s2 : wockets.getSensors()) {
				if(s1.macID.equals(s2.macID)){
					isExist = true;
					break;
				}
			}
			if(!isExist)
				wockets.getSensors().add(s1);
		}
		Log.i(Globals.SWAP_TAG, wockets.getSensorsInfo());
		return wockets;
	}
	public static ArrayList<Sensor> getPairedSensors(){
		ArrayList<Sensor> avaiSensors = new ArrayList<Sensor>();
		if( BluetoothAdapter.getDefaultAdapter() != null)
		{
			Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			Iterator<BluetoothDevice> itr = devices.iterator();
			while( itr.hasNext())
			{
				BluetoothDevice dev = itr.next();
				if(dev.getName().contains(Defines.WOCKET_DEVICE_NAME)){
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
	private int sensorLabel(Sensor unknownSensor, List<Sensor> assignedSensors){
		ArrayList<Sensor> pairedSensors = getPairedSensors();
		ArrayList<String> unpairedAssignedSensors_IDs = new ArrayList<String>();
		for (Sensor sensor : assignedSensors) {
			boolean isExist = false;
			for (Sensor s : pairedSensors) {
				if(sensor.macID.equals(s.macID)){
					isExist = true;
					break;
				}
			}
			if(!isExist)
				unpairedAssignedSensors_IDs.add(sensor.macID);
		}
		for (String string : unpairedAssignedSensors_IDs) {
			if(unknownSensor.macID.equals(string))
				return SENSOR_UNPAIRED;
		}
		return SENSOR_PAIRED;
	}
}