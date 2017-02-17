/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Main Activity of the application.  Shows the main user interface and starts
 * 			the BluetoothSensorService
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.activities.sensorstatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.sensormonitor.BluetoothSensorService;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.sensormonitor.Defines;
import edu.neu.android.wocketslib.sensormonitor.HeartRateSensor;
import edu.neu.android.wocketslib.sensormonitor.Sensor;
import edu.neu.android.wocketslib.sensormonitor.SummaryPoint;
import edu.neu.android.wocketslib.sensormonitor.WocketSensor;
import edu.neu.android.wocketslib.utils.Util;

//TODO - future work
//move all strings to strings.xml
//show arrows for improving scores/heart rate
//set Wocket location names ( and store in sensorData.xml) and use in log files
//store heart rate data in format that can be read by Merger/Viewer
//subtract from activity score when sitting idle
//improve logic for setting summary count time stamps


//Maybe TODO
//cancel notification for inactivity on movement?
//add delay between emotional events?


public class StatusScreenActivity extends Activity {

	//Defines for Option menu items
	static final int OPTION_SENSORS = 0;
	static final int OPTION_EMOTION = 1;
	static final int OPTION_STILLNESS = 2;
	static final int OPTION_HELP = 3;

	//Defines for Dialog menus
	static final int DIALOG_SENSORS = 0;
	static final int DIALOG_EMOTION = 1;
	static final int DIALOG_STILLNESS = 2;
	static final int DIALOG_HELP =3;

	static final int TIMER_PERIOD = 60;
	static PendingIntent mAlarmSender = null;
	BroadcastReceiver mHRReceiver = new HRUpdateReceiver();
	static ArrayAdapter<CharSequence> adapter = null;
	static ArrayList<CharSequence> list = null;
	static Sensor currentSensor = null;

//	private void TestDataSaver()
//	{
//		int x = 0; 
//		Date aDate;
//		Log.d("--------------------","Directory: " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/junkme");
//		
//		DataSaver ds = new DataSaver("TestProtocol", "Subject", "TestSession", TYPE.WOCKET12BITRAW,52,
//				Environment.getExternalStorageDirectory().getAbsolutePath() + "/junkme/");
//		WocketSensorData[] wsd = new WocketSensorData[5];
//		
//		for (int i = 0; i < 5; i++)
//		{
//			aDate = new Date(); 
//			wsd[i] = new WocketSensorData(TYPE.WOCKET12BITRAW,aDate, x, x, x);
//		}
//
//		ds.SaveRawData(wsd);
//	}
	
	/**
	 * BroadcastReciever that receives an Intent meaning that new
	 * data is available from the BluetoothSensorService
	 * 
	 */
	public class HRUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateDisplay();
		}
	};


	/**
	 * ItemSelectedListener to handle an item in the sensor drop down box being selected.
	 * Updates the current sensor object to reference the selected item
	 *
	 */
	public class SpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			currentSensor = DataStore.getSensor(list.get(pos).toString());
			updateDisplay();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	};

	/**
	 *  Updateds the UI display with the most recent data available.
	 */
	void updateDisplay()
	{
		if( currentSensor != null)
		{    		
			((TextView)findViewById(R.id.activityScoreValue)).setText("" + DataStore.getActivityScore());

			
			((TextView)findViewById(R.id.battery)).setText("" + currentSensor.mBattery + "%");

			String status = "OK";
			TextView connection = ((TextView)findViewById(R.id.connection));

			if( currentSensor.mConnectionErrors == 0 && DataStore.getRunning())
			{
				status = "OK";
				connection.setTextColor(Color.GREEN);
			}
			else if(currentSensor.mConnectionErrors< Defines.NO_CONNECTION_LIMIT && 
					DataStore.getRunning())
			{
				status = "POOR";
				connection.setTextColor(Color.YELLOW);
			}
			else
			{
				status = "NONE";
				connection.setTextColor(Color.RED);
			}
			connection.setText(status);


			switch(currentSensor.mType)
			{
			case Sensor.ZEPHYR:
			case Sensor.POLAR:
			{
				HeartRateSensor hr = (HeartRateSensor)currentSensor;
				((TextView)findViewById(R.id.min)).setText("" + hr.mMinRate);
				((TextView)findViewById(R.id.max)).setText("" + hr.mMaxRate);
				((TextView)findViewById(R.id.current)).setText("" + hr.mCurrentRate);
				((TextView)findViewById(R.id.average)).setText("" + hr.mAvgRate);
				((TextView)findViewById(R.id.tAverage)).setText("" + hr.mTrailingAvg);

				//Labels
				((TextView)findViewById(R.id.currentLabel)).setText("Current");
				((TextView)findViewById(R.id.minLabel)).setText("Min");
				((TextView)findViewById(R.id.maxLabel)).setText("Max");
				((TextView)findViewById(R.id.averageLabel)).setText("Average");
				((TextView)findViewById(R.id.trailingLabel)).setText("Trailing Average");
				((TextView)findViewById(R.id.HeartRateLabel)).setText("Heart Rate:");


				///TODO show comparator arrow based on average
			}
			break;
			case Sensor.WOCKET:
			{
				WocketSensor wocket = (WocketSensor)currentSensor;
				
				ArrayList<SummaryPoint> summaries = wocket.mSummaryPoints;

				((TextView)findViewById(R.id.min)).setText("" + wocket.mBytesReceived);

				if( summaries.size() > 1 && wocket.mBytesReceived > 0)
				{
					((TextView)findViewById(R.id.current)).setText("" + summaries.get(summaries.size()-1).mActivityCount);
				}
				else
				{
					((TextView)findViewById(R.id.current)).setText("-");
				}
				
				if( summaries.size() > 2 && wocket.mBytesReceived > 0)
				{
					((TextView)findViewById(R.id.max)).setText("" + summaries.get(summaries.size()-2).mActivityCount);
				}
				else
				{
					((TextView)findViewById(R.id.max)).setText("-");
				}
				
				if( summaries.size() > 3 && wocket.mBytesReceived > 0)
				{
					((TextView)findViewById(R.id.average)).setText("" + summaries.get(summaries.size()-3).mActivityCount);
				}
				else
				{
					((TextView)findViewById(R.id.average)).setText("-");
				}
				
				((TextView)findViewById(R.id.tAverage)).setText("");

				//Labels
				((TextView)findViewById(R.id.minLabel)).setText("Bytes received");
				((TextView)findViewById(R.id.currentLabel)).setText("Last readings");
				((TextView)findViewById(R.id.maxLabel)).setText("");
				((TextView)findViewById(R.id.averageLabel)).setText("");
				((TextView)findViewById(R.id.trailingLabel)).setText("");
				((TextView)findViewById(R.id.HeartRateLabel)).setText("Accelerometer:");

			}
			break;
			}
		}
		else
		{
			((TextView)findViewById(R.id.min)).setText("-");
			((TextView)findViewById(R.id.max)).setText("-");
			((TextView)findViewById(R.id.current)).setText("-");
			((TextView)findViewById(R.id.average)).setText("-");
			((TextView)findViewById(R.id.tAverage)).setText("-");
		}
		
		
		//Previous week's scores
		
		Time now = new Time();
		now.setToNow();
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		
		((TextView)findViewById(R.id.day_7)).setText(now.format("%m/%d"));
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		((TextView)findViewById(R.id.day_6)).setText(now.format("%m/%d"));
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		((TextView)findViewById(R.id.day_5)).setText(now.format("%m/%d"));
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		((TextView)findViewById(R.id.day_4)).setText(now.format("%m/%d"));
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		((TextView)findViewById(R.id.day_3)).setText(now.format("%m/%d"));
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		((TextView)findViewById(R.id.day_2)).setText(now.format("%m/%d"));
		now.set(now.monthDay-1, now.month, now.year);
		now.normalize(false);
		((TextView)findViewById(R.id.day_1)).setText(now.format("%m/%d"));
		
		
		((TextView)findViewById(R.id.day_7_score)).setText("" + DataStore.mPreviousActivityScores[0]);
		((TextView)findViewById(R.id.day_6_score)).setText("" + DataStore.mPreviousActivityScores[1]);
		((TextView)findViewById(R.id.day_5_score)).setText("" + DataStore.mPreviousActivityScores[2]);
		((TextView)findViewById(R.id.day_4_score)).setText("" + DataStore.mPreviousActivityScores[3]);
		((TextView)findViewById(R.id.day_3_score)).setText("" + DataStore.mPreviousActivityScores[4]);
		((TextView)findViewById(R.id.day_2_score)).setText("" + DataStore.mPreviousActivityScores[5]);
		((TextView)findViewById(R.id.day_1_score)).setText("" + DataStore.mPreviousActivityScores[6]);


	}



	@Override
	/**
	 * onCreate for the Main Activity.  Sets the UI display layout, loads NV data, populates sensor list
	 * and registers callbacks for the buttons
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		TestDataSaver();
			
		DataStore.init(getApplicationContext());
		
		setContentView(R.layout.activity_monitor_main);
		
		if(mAlarmSender == null)
		{
			mAlarmSender = PendingIntent.getService(this,
				0, new Intent(this, BluetoothSensorService.class), 0);
		}
		
		if( DataStore.getRunning())
		{
			((Button)findViewById(R.id.start_alarm)).setEnabled(false);
			((Button)findViewById(R.id.stop_alarm)).setEnabled(true);
		}
		else
		{			
			((Button)findViewById(R.id.start_alarm)).setEnabled(true);
			((Button)findViewById(R.id.stop_alarm)).setEnabled(false);
		}


		// Watch for button clicks.
		Button button = (Button)findViewById(R.id.start_alarm);
		button.setOnClickListener(mStartAlarmListener);
		button = (Button)findViewById(R.id.stop_alarm);
		button.setOnClickListener(mStopAlarmListener);

		list = new ArrayList<CharSequence>();
		ArrayList<CharSequence> names = DataStore.getSensorNames(true);

		for( int x=0;x<names.size();x++)
		{
			list.add( names.get(x));
		}
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);

		adapter = new ArrayAdapter<CharSequence>(
				this,  android.R.layout.simple_spinner_item, list);
		spinner.setAdapter(adapter); 
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

		if( adapter.isEmpty())
		{
			adapter.add( "Enable a sensor");
		}
		
		//If this is the very first run of the app, show the help dialog to explain
		// how the app works
		if( !DataStore.getFirstRunShown() )
		{
			DataStore.setFirstRunShown();
			showDialog(DIALOG_HELP);
		}
	}
	
	/**
	 * When the user selects the Options button, launch a menu with these values
	 */
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, OPTION_SENSORS, 0, "Sensors");
		menu.add(0, OPTION_EMOTION, 0, "Stress trigger");
		menu.add(0, OPTION_STILLNESS, 0, "Inactivity trigger");
		menu.add(0, OPTION_HELP, 0, "Help");


		return true;
	}

	/**
	 * When the user selects an item from the Options menu, launch the appropriate
	 * dialog view.
	 */
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case OPTION_SENSORS:
			showDialog(DIALOG_SENSORS);
			break;
		case OPTION_EMOTION:
			showDialog(DIALOG_EMOTION);
			break;
		case OPTION_STILLNESS:
			showDialog( DIALOG_STILLNESS);
			break;
		case OPTION_HELP:
			showDialog( DIALOG_HELP);
			break;
		}
		return true;
	}

	/**
	 * Create a dialog view based on the type of dialog needed
	 */
	public Dialog onCreateDialog(int dialog)
	{
		Dialog retVal = null;
		switch( dialog)
		{

		//Shows a list of all the available supported sensor devices and allows
		// the user to enable or disable them
		case DIALOG_SENSORS:
			{    			
				if( BluetoothAdapter.getDefaultAdapter() != null)
				{
					Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
					Iterator<BluetoothDevice> itr = devices.iterator();
					while( itr.hasNext())
					{
						BluetoothDevice dev = itr.next();
						DataStore.checkAndAddSensor(getApplicationContext()
								, dev.getName(), Util.removeColons(dev.getAddress() ));
					}
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select Sensors:");

				ArrayList<CharSequence> nameArray = DataStore.getSensorNames(false);
				CharSequence[] names = new CharSequence[nameArray.size()];

				for( int x=0;x<nameArray.size();x++)
				{
					names[x] = nameArray.get(x);
				}

				builder.setCancelable(false);
				
				builder.setNegativeButton("Add new sensor", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
				
						removeDialog(DIALOG_SENSORS);

						Intent intentBluetooth = new Intent();
						intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(intentBluetooth); 
					}
				});

				//When the user clicks done, all the values in shared preferences are erased to remove
				//any potentially old sensors that are no longer paired.  After doing this clear
				// all the other needed values need to be re-written back to the shared preferences
				builder.setNeutralButton("Done", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_SENSORS);

						Editor edit = getSharedPreferences(Defines.SHARED_PREF_NAME, 
								Context.MODE_PRIVATE).edit();
						edit.clear();
						
						//Put back other values we neeed for SharedPref's
						edit.putInt(Defines.SHARED_PREF_EMOTION_THRESHOLD, DataStore.mEmotionalEventThreshold);
						edit.putInt(Defines.SHARED_PREF_INACTIVITY_TIME, DataStore.mStillnessDuration);
						edit.putBoolean(Defines.SHARED_PREF_FIRST_RUN, true);
						edit.putInt(Defines.SHARED_PREF_ACTIVITY_SCORE, DataStore.getActivityScore());
						edit.putBoolean(Defines.SHARED_PREF_RUNNNING, DataStore.getRunning());
						if( DataStore.getStartRecordingTime() != null)
						{
							edit.putString(Defines.SHARED_PREF_START_TIME, DataStore.getStartRecordingTime().format2445());
						}
						for( int x=0;x<Defines.NUM_DAYS_SCORE_TO_SAVE;x++)
						{
							edit.putInt(Defines.SHARED_PREF_PREV_SCORE + x, DataStore.mPreviousActivityScores[x]);

						}
						
						if( DataStore.mActivityScoreDate != null)
						{
							edit.putString(Defines.SHARED_PREF_SCORE_DATE, DataStore.mActivityScoreDate.format2445());
						}

						
						ArrayList<CharSequence> enabledNames = DataStore.getSensorNames(true);

						int enabledSize = enabledNames.size();
						edit.putInt(Defines.SHARED_PREF_NUM_SENSORS, enabledSize);

						for( int x=0;x<enabledSize;x++)
						{
							edit.putString(Defines.SHARED_PREF_SENSOR+x, enabledNames.get(x).toString());
						}

						edit.commit();
					}
				});

				builder.setMultiChoiceItems(names, DataStore.getSensorStates(),new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {

						DataStore.mSensors.get(which).mEnabled = isChecked;

						if( isChecked)
						{
							adapter.add( DataStore.mSensors.get(which).mName);
						}
						else
						{
							adapter.remove(DataStore.mSensors.get(which).mName);
							if( currentSensor == DataStore.mSensors.get(which))
							{
								currentSensor = null;
							}
						}

						if( adapter.isEmpty())
						{
							adapter.add( "Enable a sensor");
							currentSensor = null;
						}
						else
						{
							adapter.remove( "Enable a sensor");
						}

						if( currentSensor == null)
						{
							currentSensor = DataStore.getFirstSensor();
						}

						updateDisplay();
					}
				});
				retVal = builder.create();
			}
			break;
		case DIALOG_EMOTION:
			{					
				///Note: To change the options available here, just change the arrays in Defines.java
				// EMOTION_OPTION_VALUES and EMOTION_OPTION_STRINGS
				
				int selectedIndex = 0;
				for( int x=0;x<Defines.EMOTION_OPTION_VALUES.length;x++)
				{
					if( Defines.EMOTION_OPTION_VALUES[x] == DataStore.mEmotionalEventThreshold)
					{
						selectedIndex = x;
						break;
					}
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Set threshold above trailing average");
				builder.setSingleChoiceItems(Defines.EMOTION_OPTION_STRINGS, selectedIndex, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	DataStore.mEmotionalEventThreshold = Defines.EMOTION_OPTION_VALUES[item];
				    }
				});
				
				
				builder.setCancelable(false);
				builder.setNeutralButton("Done", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_EMOTION);

						Editor edit = getSharedPreferences(Defines.SHARED_PREF_NAME, 
								Context.MODE_PRIVATE).edit();
						edit.putInt(Defines.SHARED_PREF_EMOTION_THRESHOLD, DataStore.mEmotionalEventThreshold);
						edit.commit();
					}
				});
				
				retVal = builder.create();
			}
			break;
		case DIALOG_STILLNESS:
			{
				///Note: To change the options available here, just change the arrays in Defines.java
				// INACTIVTIY_OPTION_VALUES and INACTIVTIY_OPTION_STRINGS
				int selectedIndex = 0;
				for( int x=0;x<Defines.INACTIVTIY_OPTION_VALUES.length;x++)
				{
					if( Defines.INACTIVTIY_OPTION_VALUES[x] == DataStore.mStillnessDuration)
					{
						selectedIndex = x;
						break;
					}
				}				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select duration of inactivity before alert:");
				builder.setSingleChoiceItems(Defines.INACTIVITY_OPTION_STRINGS, selectedIndex, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	DataStore.mStillnessDuration = Defines.INACTIVTIY_OPTION_VALUES[item];
				    }
				});
				
			
				builder.setCancelable(false);
				builder.setNeutralButton("Done", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_STILLNESS);

						Editor edit = getSharedPreferences(Defines.SHARED_PREF_NAME, 
								Context.MODE_PRIVATE).edit();
						edit.putInt(Defines.SHARED_PREF_INACTIVITY_TIME, DataStore.mStillnessDuration);
						edit.commit();
					}
				});
				
				retVal = builder.create();
				
			}
			break;
		case DIALOG_HELP:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			String message = getString(R.string.app_name) + 
							"\nVersion: " + getString(R.string.version) + 
							"\n\n" +
							"This app requires Wocket motion sensors and a Zephyr HXM heart rate monitor for full functionality.\n\n" +
							"To use, select the desired sensors by touching the Menu button, then \"Sensors\". If the desired sensor is "+
							"not listed, you have not yet paired the device.  Use the \"Add new sensor\" button to pair with the new sensors.\n\n" +
							"Press the Start button to begin recording data.  Notifications will appear if you are stationary for an extended period of time " +
							"or if your heart rate increases suddenly without any physical activity.";
			
			builder.setMessage(message).setPositiveButton("Done", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
						removeDialog(DIALOG_HELP);
		           }
		       });
			
			retVal = builder.create();
			break;
		default:
			break;
		}
		return retVal;
	}
	
	/**
	 * Resume the activity from a paused state.  If the service is not running, make sure the
	 * alarm is canceled as a fallback saftey check so the alarm doesn't get stuck running forever
	 * accidentally
	 */
	protected void onResume()
	{
		super.onResume();		
		updateDisplay();
		registerReceiver(mHRReceiver, new IntentFilter(Defines.NEW_DATA_READY_BROADCAST_STRING));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mHRReceiver);
	}
	
	/**
	 * Callback function for the Start button being pressed. 
	 */
	private OnClickListener mStartAlarmListener = new OnClickListener() {
		public void onClick(View v) {
		}
	};

	/**
	 * Callback function for the Stop button being pressed
	 */
	private OnClickListener mStopAlarmListener = new OnClickListener() {
		public void onClick(View v) {			
			//Set all connection states to none
			int size = DataStore.mSensors.size();
			for( int x=0;x<size;x++)
			{
				DataStore.mSensors.get(x).mConnectionErrors = Defines.NO_CONNECTION_LIMIT;
			}
			updateDisplay();
		}
	};
}