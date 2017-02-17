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
import java.util.Calendar;
import java.util.Date;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.datasummaryviewer.GetDataSummaryActivity;
import edu.neu.android.wocketslib.dataupload.DataManager;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.dataupload.RawUploader;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.sensormonitor.Defines;
import edu.neu.android.wocketslib.sensormonitor.Sensor;
import edu.neu.android.wocketslib.support.AlertPlayer;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Util;

//TODO - future work
//move all strings to strings.xml
//show arrows for improving scores/heart rate
//set Wocket location names ( and store in sensorData.xml) and use in log files
//store heart rate data in format that can be read by Merger/Viewer
//subtract from activity score when sitting idle
//improve logic for setting summary count time stamps

public class SimpleStatusScreenActivity extends Activity implements OnClickListener {
	private static final String TAG = "SimpleStatusScreenActivity";

	static final int TIMER_PERIOD = 60;
	static PendingIntent mAlarmSender = null;
	BroadcastReceiver mHRReceiver = new HRUpdateReceiver();
	static ArrayAdapter<CharSequence> adapter = null;
	static ArrayList<CharSequence> list = null;
	static Sensor currentSensor = null;
	TextView statusTextView = null; 
	Button buttonVibrate = null; 
	private static final String NEWLINE = "\n";

	//Defines for Dialog menus
	static final int DIALOG_SENSORS = 0;

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

	void updateSensors()
	{	
		AlertPlayer.beepPhone();
		
		for (Sensor s : DataStore.mSensors)
		{			
			DataStore.checkAndAddSensor(SimpleStatusScreenActivity.this, s.mName, s.mAddress);
		}
		
		updateDisplay(); 
	}
	
	/**
	 *  Updates the UI display with the most recent data available.
	 */
	void updateDisplay()
	{			
		StringBuilder statusMsg = new StringBuilder();  

		if (DataStore.isVibrate())
			buttonVibrate.setText("Disable test vibrate");
		else 
			buttonVibrate.setText("Enable test vibrate");

		for (Sensor s : DataStore.mSensors)
		{			
			statusMsg.append( s.mName + " ");
			if (s.mEnabled)
			{
				if (s.mConnectionErrors == Sensor.UNKNOWN)
					statusMsg.append("(enabled) Recent connection errors: Unknown");				
				else
					statusMsg.append("(enabled) Recent connection errors: " + s.mConnectionErrors);				
			}
			else
				statusMsg.append("(not enabled)");
			statusMsg.append(NEWLINE + NEWLINE); 
		}
		
		statusTextView.setText(statusMsg.toString());		

		//		if( currentSensor != null)
		//		{    		
		//			((TextView)findViewById(R.id.activityScoreValue)).setText("" + DataStore.getActivityScore());
		//			
		//			((TextView)findViewById(R.id.battery)).setText("" + currentSensor.mBattery + "%");
		//
		//			String status = "OK";
		//			TextView connection = ((TextView)findViewById(R.id.connection));
		//
		//			if( currentSensor.mConnectionErrors == 0 && DataStore.getRunning())
		//			{
		//				status = "OK";
		//				connection.setTextColor(Color.GREEN);
		//			}
		//			else if(currentSensor.mConnectionErrors< Defines.NO_CONNECTION_LIMIT && 
		//					DataStore.getRunning())
		//			{
		//				status = "POOR";
		//				connection.setTextColor(Color.YELLOW);
		//			}
		//			else
		//			{
		//				status = "NONE";
		//				connection.setTextColor(Color.RED);
		//			}
		//			connection.setText(status);
		//
		//
		//			switch(currentSensor.mType)
		//			{
		//			case ZEPHYR:
		//			case POLAR:
		//			{
		//				HeartRateSensor hr = (HeartRateSensor)currentSensor;
		//				((TextView)findViewById(R.id.min)).setText("" + hr.mMinRate);
		//				((TextView)findViewById(R.id.max)).setText("" + hr.mMaxRate);
		//				((TextView)findViewById(R.id.current)).setText("" + hr.mCurrentRate);
		//				((TextView)findViewById(R.id.average)).setText("" + hr.mAvgRate);
		//				((TextView)findViewById(R.id.tAverage)).setText("" + hr.mTrailingAvg);
		//
		//				//Labels
		//				((TextView)findViewById(R.id.currentLabel)).setText("Current");
		//				((TextView)findViewById(R.id.minLabel)).setText("Min");
		//				((TextView)findViewById(R.id.maxLabel)).setText("Max");
		//				((TextView)findViewById(R.id.averageLabel)).setText("Average");
		//				((TextView)findViewById(R.id.trailingLabel)).setText("Trailing Average");
		//				((TextView)findViewById(R.id.HeartRateLabel)).setText("Heart Rate:");
		//
		//
		//				///TODO show comparator arrow based on average
		//			}
		//			break;
		//			case WOCKET:
		//			{
		//				WocketSensor wocket = (WocketSensor)currentSensor;
		//				
		//				ArrayList<SummaryPoint> summaries = wocket.mSummaryPoints;
		//
		//				((TextView)findViewById(R.id.min)).setText("" + wocket.mBytesReceived);
		//
		//				if( summaries.size() > 1 && wocket.mBytesReceived > 0)
		//				{
		//					((TextView)findViewById(R.id.current)).setText("" + summaries.get(summaries.size()-1).mActivityCount);
		//				}
		//				else
		//				{
		//					((TextView)findViewById(R.id.current)).setText("-");
		//				}
		//				
		//				if( summaries.size() > 2 && wocket.mBytesReceived > 0)
		//				{
		//					((TextView)findViewById(R.id.max)).setText("" + summaries.get(summaries.size()-2).mActivityCount);
		//				}
		//				else
		//				{
		//					((TextView)findViewById(R.id.max)).setText("-");
		//				}
		//				
		//				if( summaries.size() > 3 && wocket.mBytesReceived > 0)
		//				{
		//					((TextView)findViewById(R.id.average)).setText("" + summaries.get(summaries.size()-3).mActivityCount);
		//				}
		//				else
		//				{
		//					((TextView)findViewById(R.id.average)).setText("-");
		//				}
		//				
		//				((TextView)findViewById(R.id.tAverage)).setText("");
		//
		//				//Labels
		//				((TextView)findViewById(R.id.minLabel)).setText("Bytes received");
		//				((TextView)findViewById(R.id.currentLabel)).setText("Last readings");
		//				((TextView)findViewById(R.id.maxLabel)).setText("");
		//				((TextView)findViewById(R.id.averageLabel)).setText("");
		//				((TextView)findViewById(R.id.trailingLabel)).setText("");
		//				((TextView)findViewById(R.id.HeartRateLabel)).setText("Accelerometer:");
		//
		//			}
		//			break;
		//			}
		//		}
		//		else
		//		{
		//			((TextView)findViewById(R.id.min)).setText("-");
		//			((TextView)findViewById(R.id.max)).setText("-");
		//			((TextView)findViewById(R.id.current)).setText("-");
		//			((TextView)findViewById(R.id.average)).setText("-");
		//			((TextView)findViewById(R.id.tAverage)).setText("-");
		//		}
		//		
		//		
		//		//Previous week's scores
		//		
		//		Time now = new Time();
		//		now.setToNow();
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		
		//		((TextView)findViewById(R.id.day_7)).setText(now.format("%m/%d"));
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		((TextView)findViewById(R.id.day_6)).setText(now.format("%m/%d"));
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		((TextView)findViewById(R.id.day_5)).setText(now.format("%m/%d"));
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		((TextView)findViewById(R.id.day_4)).setText(now.format("%m/%d"));
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		((TextView)findViewById(R.id.day_3)).setText(now.format("%m/%d"));
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		((TextView)findViewById(R.id.day_2)).setText(now.format("%m/%d"));
		//		now.set(now.monthDay-1, now.month, now.year);
		//		now.normalize(false);
		//		((TextView)findViewById(R.id.day_1)).setText(now.format("%m/%d"));
		//		
		//		
		//		((TextView)findViewById(R.id.day_7_score)).setText("" + DataStore.mPreviousActivityScores[0]);
		//		((TextView)findViewById(R.id.day_6_score)).setText("" + DataStore.mPreviousActivityScores[1]);
		//		((TextView)findViewById(R.id.day_5_score)).setText("" + DataStore.mPreviousActivityScores[2]);
		//		((TextView)findViewById(R.id.day_4_score)).setText("" + DataStore.mPreviousActivityScores[3]);
		//		((TextView)findViewById(R.id.day_3_score)).setText("" + DataStore.mPreviousActivityScores[4]);
		//		((TextView)findViewById(R.id.day_2_score)).setText("" + DataStore.mPreviousActivityScores[5]);
		//		((TextView)findViewById(R.id.day_1_score)).setText("" + DataStore.mPreviousActivityScores[6]);
		//

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

		setContentView(R.layout.activity_monitor_simple);

		// Watch for button clicks.
		findViewById(R.id.buttonSensors).setOnClickListener(this);
		findViewById(R.id.buttonSendFiles).setOnClickListener(this);
		findViewById(R.id.buttonSendLogFiles).setOnClickListener(this);
		findViewById(R.id.buttonBack).setOnClickListener(this);
		findViewById(R.id.buttonShutdown).setOnClickListener(this);
		findViewById(R.id.buttonVibrate).setOnClickListener(this);
		findViewById(R.id.buttonViewData).setOnClickListener(this);
		findViewById(R.id.buttonCleanFiles).setOnClickListener(this);

		statusTextView = (TextView) findViewById(R.id.textViewStatus);
		buttonVibrate = (Button) findViewById(R.id.buttonVibrate);

		//		if(mAlarmSender == null)
		//		{
		//			mAlarmSender = PendingIntent.getService(this,
		//				0, new Intent(this, BluetoothSensorService.class), 0);
		//		}
		//		
		//		list = new ArrayList<CharSequence>();
		//		ArrayList<CharSequence> names = DataStore.getSensorNames(true);
		//
		//		for( int x=0;x<names.size();x++)
		//		{
		//			list.add( names.get(x));
		//		}
		//		Spinner spinner = (Spinner) findViewById(R.id.spinner1);

		//		adapter = new ArrayAdapter<CharSequence>(
		//				this,  android.R.layout.simple_spinner_item, list);
		//		spinner.setAdapter(adapter); 
		//		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		//
		//		if( adapter.isEmpty())
		//		{
		//			adapter.add( "Enable a sensor");
		//		}

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
				DataStore.unsetPairedFlagAllSensors(); 
				Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
				Iterator<BluetoothDevice> itr = devices.iterator();
				while( itr.hasNext())
				{
					BluetoothDevice dev = itr.next();
					Sensor s = DataStore.checkAndAddSensor(getApplicationContext(), dev.getName(), Util.removeColons(dev.getAddress() ));
					if (s != null)
						s.mPaired = true; 
				}
				DataStore.removeNonPairedSensors(); 
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

					Editor edit = getSharedPreferences(Defines.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
					edit.clear();

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
					updateDisplay();
				}
			});

			builder.setMultiChoiceItems(names, DataStore.getSensorStates(),new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {

					DataStore.mSensors.get(which).mEnabled = isChecked;

					updateSensors();
				}
			});
			retVal = builder.create();
		}
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
	 * Set the update button on/off depending on if the code detects that the software
	 * is or is not at the latest version on the Android Market. 
	 */
	private class SendDataFilesTask extends AsyncTask<Void, Void, Boolean> 
	{ 
		@Override
		protected Boolean doInBackground(Void... params) {
			long startTime = System.currentTimeMillis(); 
			String msg = "Starting user-initiated file upload";
			ServerLogger.sendNote(getApplicationContext(), msg, true);
			
			//TODO change to bakup is false
			int filesRemaining = RawUploader.uploadDataFromExtUploadDir(getApplicationContext(), false, true, Globals.BACKUP_UPLOADS_EXTERNAL, Globals.UPLOAD_SUCCESS_PERCENTAGE, true);

			msg = "Completed user-initiated file upload after " + String.format("%.1f",((System.currentTimeMillis()-startTime)/1000.0/60.0)) + " minutes. Files remaining to upload: " + filesRemaining; 		
			ServerLogger.sendNote(getApplicationContext(), msg, true);
			return true; 
		}

		protected void onPostExecute(Boolean isNeedUpdate) {
			displayToastMessage("Finished sending Wocket data files.");
			Button btn = (Button) findViewById(R.id.buttonSendFiles);
			btn.setEnabled(true);
		}
	}

	/**
	 * Set the update button on/off depending on if the code detects that the software
	 * is or is not at the latest version on the Android Market. 
	 */
	private class CleanDataFilesTask extends AsyncTask<Void, Void, Boolean> 
	{ 
		@Override
		protected Boolean doInBackground(Void... params) {
			String msg = "Starting user-initiated file cleaning";
			ServerLogger.sendNote(getApplicationContext(), msg, true);
			// Delete any files in external upload directory older than a week old
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -7);
			Date weekAgo = calendar.getTime(); 			
			int filesDeleted = DataManager.deleteOldDataIntUploadsDir(weekAgo);
			msg = "Completed user-initiated file cleaning. Deleted " + filesDeleted + " files."; 		
			ServerLogger.sendNote(getApplicationContext(), msg, true);
			return true; 
		}

		protected void onPostExecute(Boolean isNeedUpdate) {
			displayToastMessage("Finished cleaning old Wocket data files.");
			Button btn = (Button) findViewById(R.id.buttonCleanFiles);
			btn.setEnabled(true);
		}
	}

	/**
	 * Set the update button on/off depending on if the code detects that the software
	 * is or is not at the latest version on the Android Market. 
	 */
	private class SendLogFilesTask extends AsyncTask<Void, Void, Boolean> 
	{ 
		@Override
		protected Boolean doInBackground(Void... params) {
			long startTime = System.currentTimeMillis(); 
			String msg = "Starting user-initiated log file upload";
			ServerLogger.sendNote(getApplicationContext(), msg, true);

			//TODO check last parameter correct (only send prior day) 
			DataSender.sendLogsToExternalUploadDir(getApplicationContext(), true, false);
			int filesRemaining = -1; //RawUploader.uploadData(getApplicationContext(), false, true, true, .85);

			msg = "Completed user-initiated log file upload after " + String.format("%.1f",((System.currentTimeMillis()-startTime)/1000.0/60.0)) + " minutes. Files remaining to upload: " + filesRemaining; 		
			ServerLogger.sendNote(getApplicationContext(), msg, true);
			return true; 
		}

		protected void onPostExecute(Boolean isNeedUpdate) {
			displayToastMessage("Finished sending Wocket log files.");
			Button btn = (Button) findViewById(R.id.buttonSendLogFiles);
			btn.setEnabled(true);
		}
	}

	private void displayToastMessage(String aMsg)
	{
		Toast aToast = Toast.makeText(getApplicationContext(),aMsg, Toast.LENGTH_LONG);
		aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		aToast.show();		
	}

	@Override
	public void onClick(View v) {
		AppUsageLogger.logClick(TAG, v);
		if (v.getId() == R.id.buttonSensors) {
			showDialog(DIALOG_SENSORS);
		} else if (v.getId() == R.id.buttonBack) {
			finish();
		} else if (v.getId() == R.id.buttonVibrate) {
			if (DataStore.isVibrate())
			{
				DataStore.setIsVibrate(false);
				displayToastMessage("Turning off test vibration when Wockets connect.");				
			}
			else
			{
				DataStore.setIsVibrate(true);
				displayToastMessage("Turning on test vibration when Wockets connect. This is for test purposes only.");				
			}
			updateDisplay();
		} else if (v.getId() == R.id.buttonShutdown) {
			Log.i(TAG, "Shutdown Wockets");
			displayToastMessage("Trying to shut down Wockets. If lights don't stop blinking after about a minute, try this again. To start Wockets again, charge them for 1 minute.");
			DataStorage.setShutdown(getApplicationContext(), true);
		} else if (v.getId() == R.id.buttonSendFiles) {
			Log.i(TAG, "Send all queued files");
			displayToastMessage("Sending Wocket data files right now! (This may take a while)");
			Button btn = (Button) findViewById(R.id.buttonSendFiles);
			btn.setEnabled(false);
			new SendDataFilesTask().execute();
		} else if (v.getId() == R.id.buttonCleanFiles) {
			Log.i(TAG, "Clean old queued files");
			displayToastMessage("Cleaning old files from phone! (This may take a while)");
			Button btn3 = (Button) findViewById(R.id.buttonCleanFiles);
			btn3.setEnabled(false);
			new CleanDataFilesTask().execute();
		} else if (v.getId() == R.id.buttonSendLogFiles) {
			Log.i(TAG, "Send all log files");
			displayToastMessage("Sending Wocket log files right now! (This may take a while)");
			Button btn2 = (Button) findViewById(R.id.buttonSendLogFiles);
			btn2.setEnabled(false);
			new SendLogFilesTask().execute();
		} else if (v.getId() == R.id.buttonViewData) {
			Log.i(TAG, "View data summaries");
			Intent i = new Intent(SimpleStatusScreenActivity.this, GetDataSummaryActivity.class);
			startActivity(i);
			finish();
		}
	}	
}