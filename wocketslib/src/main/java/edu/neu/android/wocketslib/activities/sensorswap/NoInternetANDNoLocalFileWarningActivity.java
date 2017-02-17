package edu.neu.android.wocketslib.activities.sensorswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class NoInternetANDNoLocalFileWarningActivity extends BaseActivity{
	private static final String TAG = "NoInternetANDNoLocalFileWarningActivity"; 

	private Button btn_tryagain;
	private Button btn_later;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.nointernetnofilewarning_activity);
		btn_tryagain = (Button)this.findViewById(R.id.warning_tryagain);
		btn_later = (Button)this.findViewById(R.id.warning_later);
		
		btn_tryagain.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intentBluetooth = new Intent();
				intentBluetooth.setAction(android.provider.Settings.ACTION_WIFI_SETTINGS);
				startActivity(intentBluetooth); 
				Log.i(Globals.SWAP_TAG, "Exit app, no internet connection and no local record, start to check wifi");
				((ApplicationManager) getApplication()).killAllActivities();
			}
		});
		btn_later.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(Globals.SWAP_TAG, "Exit app, no internet connection and no local record, do later");
				((ApplicationManager) getApplication()).killAllActivities();

			}
		});
		
	}

}
