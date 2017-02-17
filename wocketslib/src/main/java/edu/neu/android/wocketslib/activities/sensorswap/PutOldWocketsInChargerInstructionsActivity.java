package edu.neu.android.wocketslib.activities.sensorswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.NetworkDetector;
import edu.neu.android.wocketslib.utils.Wockets;

public class PutOldWocketsInChargerInstructionsActivity extends BaseActivity{
	private static final String TAG = "PutOldWocketsInChargerInstructionsActivity"; 
	private Button nmBtn;
	private Button didBtn;
	private Button tipBtn;
	private Wockets wockets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_putwocketsintochangerreminder_activity);
		nmBtn = (Button)this.findViewById(R.id.page5nm);
		didBtn = (Button)this.findViewById(R.id.page5did);
		tipBtn = (Button)this.findViewById(R.id.page5tips);
		wockets = (Wockets) getIntent().getSerializableExtra("wockets");

		nmBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(PutOldWocketsInChargerInstructionsActivity.this,Swap_NoWocketsSelectedWarningActivity.class);
				i.putExtra("counter", 0);
				startActivity(i);

			}
		});
		didBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(PutOldWocketsInChargerInstructionsActivity.this,ReadyPutOnInstructionActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				i.putExtra("wockets", wockets);
				startActivity(i);
				finish();
			}
		});
		tipBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(NetworkDetector.isConnected(PutOldWocketsInChargerInstructionsActivity.this)){
					Intent i = new Intent(PutOldWocketsInChargerInstructionsActivity.this, DisplayWebTipsActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					String address = "http://web.mit.edu/city/wockets/charging.html" ;
					i.putExtra("address", address);
					startActivity(i);
				}
				else{
					Toast.makeText(PutOldWocketsInChargerInstructionsActivity.this, "Network unconnected! Please turn on your internet to view tips.", Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

}