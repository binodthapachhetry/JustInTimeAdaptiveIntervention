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

public class TakeWocketsChargerInstructionActivity extends BaseActivity{
	private static final String TAG = "TakeWocketsChargerInstructionActivity"; 
	private Button nmBtn;
	private Button didBtn;
	private Button tipBtn;
	private Wockets wockets;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_takewocketschargerinstruction_activity);
		nmBtn = (Button)this.findViewById(R.id.page6nm);
		didBtn = (Button)this.findViewById(R.id.page6did);
		tipBtn = (Button)this.findViewById(R.id.page6tips);
		wockets = (Wockets) getIntent().getSerializableExtra("wockets");
		
		nmBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(TakeWocketsChargerInstructionActivity.this,Swap_NoWocketsSelectedWarningActivity.class);
				i.putExtra("counter", 0);
				startActivity(i);
			}
			});
		didBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(TakeWocketsChargerInstructionActivity.this,PutOldWocketsInChargerInstructionsActivity.class);
				i.putExtra("wockets", wockets);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(i);
				finish();
				}
			});
		tipBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(NetworkDetector.isConnected(TakeWocketsChargerInstructionActivity.this)){
					Intent i = new Intent(TakeWocketsChargerInstructionActivity.this, DisplayWebTipsActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					String address = "http://web.mit.edu/city/wockets/bands.html";
					i.putExtra("address", address);
					startActivity(i);
				}
				else{
					Toast.makeText(TakeWocketsChargerInstructionActivity.this, "Network unconnected! Please turn on your internet to view tips.", Toast.LENGTH_SHORT).show();
				}
			}
			});
	}

}
