package edu.neu.android.wocketslib.activities.sensorswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Wockets;

public class TakeOffWocketsInstructionActivity extends BaseActivity{
	private static final String TAG = "TakeOffWocketsInstructionActivity"; 
	
	private Button nmBtn;
	private Button didBtn;
	private Wockets wockets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_takeoffoldwocketsinstruction_activity);
		nmBtn = (Button)this.findViewById(R.id.page3nm);
		didBtn = (Button)this.findViewById(R.id.page3did);
		wockets = (Wockets) getIntent().getSerializableExtra("wockets");
		
		nmBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Log.i(Globals.SWAP_TAG, "Exit app, exit before swap");
				((ApplicationManager) getApplication()).killAllActivities();
			}
			});
		didBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(TakeOffWocketsInstructionActivity.this,TakeWocketsChargerInstructionActivity.class);
				i.putExtra("wockets", wockets);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(i);
				finish();
			}
		});
	}

}
