package edu.neu.android.wocketslib.activities.sensorswap;

import java.util.List;

import android.content.Intent;
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

public class Swap_ExitActivity extends BaseActivity{
	private static final String TAG = "Swap_ExitActivity"; 
	
	private Button doneBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_exit_activity);
		doneBtn = (Button)this.findViewById(R.id.page18done);
		final Wockets record = (Wockets) this.getIntent().getSerializableExtra("record");
		doneBtn.setOnClickListener(new OnClickListener(){ 

			@Override
			public void onClick(View arg0) {
				((ApplicationManager) getApplication()).killAllActivities();
				if(!WocketInfoGrabber.isActiveInternetConnection(Swap_ExitActivity.this)
						&&Globals.IS_DEBUG){
					Toast.makeText(Swap_ExitActivity.this, 
							"No Internet connection. Unable to upload swap events right now. Will later...", Toast.LENGTH_LONG).show();
				}
//				record.dataChecker(Swap_ExitActivity.this);
				try {
					record.saveSwapInfoToFile(Swap_ExitActivity.this);
					Log.i(Globals.SWAP_TAG, "Start to upload swapped sensor info.");
					record.transmitSwappedSensorsToServer(Swap_ExitActivity.this);
					Log.i(Globals.SWAP_TAG, "Start to set swapped sensor enabled.");
					record.saveSensorsEnabled(Swap_ExitActivity.this);
					Log.i(Globals.SWAP_TAG, "Start to save swapped Zephyr.");
					record.saveHRInfoToFile(Swap_ExitActivity.this);
					
					List<SwappedSensor> sensors = record.getSwappedSensors();
					for (SwappedSensor swappedSensor : sensors) {
						SensorDataInfo sensorData = new SensorDataInfo();
						sensorData.setSwappedData(swappedSensor);
//						DataStore.setSensorDataInfo(Swap_ExitActivity.this, sensorData);
					}

				} catch (Exception e) {
					Log.e(Globals.SWAP_TAG,"Error! Unable to write file.");
					e.printStackTrace();
				}
				Log.i(Globals.SWAP_TAG, "Exit App, Wockwets Swapped.");
				
				Intent i = new Intent(Globals.WOCKETS_REFRESH_DATA_ACTION);
				sendBroadcast(i);
			}
			});
		
	}

}
