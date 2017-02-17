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

@SuppressWarnings("unused")
public class CheckReadyActivity extends BaseActivity {
	private static final String TAG = "CheckReadyActivity"; 
	private Button nmBtn;
	private Button yesBtn;
	private Wockets wockets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_checkready_activity);
		nmBtn = (Button) this.findViewById(R.id.page2nm);
		yesBtn = (Button) this.findViewById(R.id.page2yes);
		wockets = (Wockets) getIntent().getSerializableExtra("wockets");

		nmBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Log.i(Globals.SWAP_TAG, "Exit app, exit before swap.");
				((ApplicationManager) getApplication()).killAllActivities();
			}

		});
		yesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(CheckReadyActivity.this, TakeOffWocketsInstructionActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("wockets", wockets);
				startActivity(i);
				finish();
			}
		});
	}

}
