package edu.neu.android.wocketslib.activities.sensorswap;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Wockets;

public class Change_CancellingWarningActivity extends BaseActivity {
	private final static String TAG = "Change_CancellingWarningActivity"; 
	private Button yesBtn;
	private Button noBtn;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.swap_change_cancellingwarning_activity);
		yesBtn = (Button) this.findViewById(R.id.page21yes);
		noBtn = (Button) this.findViewById(R.id.page21no);
		tv = (TextView) this.findViewById(R.id.cancelchangeTV);
		String warningMsg = "";

		List<SwappedSensor> swappedSensors = WocketInfoGrabber.getSwappedSensors(Change_CancellingWarningActivity.this);
		List<Sensor> sensors = WocketInfoGrabber.getSensors(Change_CancellingWarningActivity.this);

		if (swappedSensors != null && swappedSensors.size() > 0) {
			Wockets wockets = new Wockets();
			wockets.setSwappedSensors(swappedSensors);
			wockets.setSensors(sensors);
			wockets.setSwappedHRSensor(WocketInfoGrabber.getHRSensor(Change_CancellingWarningActivity.this));

			warningMsg = "Ok. Canceling your changes and leaving your original Wockets locations:" + wockets;
		} else {
			warningMsg = "Ok. Canceling your changes and leaving your original Wockets locations.";
		}
		tv.setText(warningMsg);
		noBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		yesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// Intent intent = new Intent(Intent.ACTION_MAIN);
				// intent.addCategory(Intent.CATEGORY_HOME);
				// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(intent);
				// finish();
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Log.i(Globals.SWAP_TAG, "Exit app, change Wockets cancelled.");
				((ApplicationManager) getApplication()).killAllActivities();
			}
		});
	}

}
