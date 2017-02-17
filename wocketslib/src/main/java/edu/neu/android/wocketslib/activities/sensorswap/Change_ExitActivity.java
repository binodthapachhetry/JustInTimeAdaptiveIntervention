package edu.neu.android.wocketslib.activities.sensorswap;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Wockets;
import edu.neu.android.wocketslib.visualizerdatagenerator.SensorDataInfo;

public class Change_ExitActivity extends BaseActivity {
	private static final String TAG = "Change_ExitActivity";
	private Button doneBtn;
	private Wockets record;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_change_exit_activity);
		doneBtn = (Button) this.findViewById(R.id.page18done);
		record = (Wockets) this.getIntent().getSerializableExtra("record");
		doneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				((ApplicationManager) getApplication()).killAllActivities();
				if (!WocketInfoGrabber.isActiveInternetConnection(Change_ExitActivity.this) && Globals.IS_DEBUG) {
					Toast.makeText(Change_ExitActivity.this, "No Internet connection. Unable to upload Change events right now. Will later...",
							Toast.LENGTH_LONG).show();
				}
				try {
					record.saveSwapInfoToFile(Change_ExitActivity.this);
//					Log.i(Globals.SWAP_TAG, "Start to upload swapped sensor info.");
//					record.transmitSwappedSensorsToServer(Change_ExitActivity.this);
					Log.i(Globals.SWAP_TAG, "Start to set swapped sensor enabled.");
					record.saveSensorsEnabled(Change_ExitActivity.this);
					Log.i(Globals.SWAP_TAG, "Start to save swapped Zephyr.");
					record.saveHRInfoToFile(Change_ExitActivity.this);

					List<SwappedSensor> sensors = record.getSwappedSensors();
					for (SwappedSensor swappedSensor : sensors) {
						SensorDataInfo sensorData = new SensorDataInfo();
						sensorData.setSwappedData(swappedSensor);
					}
				} catch (Exception e) {
					Log.e(Globals.SWAP_TAG, "Error! Unable to write file.");
					e.printStackTrace();
				}
//				new SaveDataAsyncTask().execute(null);
				
				Log.i(Globals.SWAP_TAG, "Exit App, Wockets changed.");
				Intent i = new Intent(Globals.WOCKETS_REFRESH_DATA_ACTION);
				sendBroadcast(i);
			}
		});
	}
	private class SaveDataAsyncTask extends AsyncTask<Void, Void, Void>{
		ProgressDialog dialog = null; 

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(dialog != null)
				dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(getApplicationContext(), "Saving data", 
					"Saving swapped information to local memory. Please wait.");
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				record.saveSwapInfoToFile(Change_ExitActivity.this);
				Log.i(Globals.SWAP_TAG, "Start to upload swapped sensor info.");
				record.transmitSwappedSensorsToServer(Change_ExitActivity.this);
				Log.i(Globals.SWAP_TAG, "Start to set swapped sensor enabled.");
				record.saveSensorsEnabled(Change_ExitActivity.this);
				Log.i(Globals.SWAP_TAG, "Start to save swapped Zephyr.");
				record.saveHRInfoToFile(Change_ExitActivity.this);

				List<SwappedSensor> sensors = record.getSwappedSensors();
				for (SwappedSensor swappedSensor : sensors) {
					SensorDataInfo sensorData = new SensorDataInfo();
					sensorData.setSwappedData(swappedSensor);
				}
			} catch (Exception e) {
				Log.e(Globals.SWAP_TAG, "Error! Unable to write file.");
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
