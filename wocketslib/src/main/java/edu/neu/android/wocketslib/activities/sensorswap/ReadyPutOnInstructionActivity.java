package edu.neu.android.wocketslib.activities.sensorswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Wockets;

public class ReadyPutOnInstructionActivity extends BaseActivity {
	private static final String TAG = "ReadyPutOnInstructionActivity"; 
	private Button nmBtn;
	private Button conBtn;
//	private Button tipBtn;
	private Wockets wockets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_readyputoninstruction_activity);
		nmBtn = (Button) this.findViewById(R.id.page4nm);
		conBtn = (Button) this.findViewById(R.id.page4con);
		// tipBtn = (Button)this.findViewById(R.id.page4tips);
		wockets = (Wockets) getIntent().getSerializableExtra("wockets");

		nmBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(ReadyPutOnInstructionActivity.this, Swap_NoWocketsSelectedWarningActivity.class);
				i.putExtra("counter", 0);
				startActivity(i);

			}
		});
		conBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(ReadyPutOnInstructionActivity.this, ChangeWocketsBodyDiagramActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("wockets", wockets);
				startActivity(i);
				finish();
			}
		});

		// tipBtn.setOnClickListener(new OnClickListener(){
		//
		// @Override
		// public void onClick(View arg0) {
		// if(new FileUtils().isConnected(PageFour.this)){
		// Intent i = new Intent(PageFour.this, WebTips.class);
		// i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		// String address = "http://web.mit.edu/city/wockets/orientation.html" ;
		// i.putExtra("address", address);
		// startActivity(i);
		// }
		// else{
		// String t =
		// "Network unconnected! Please turn on your internet to view tips.";
		// Toast.makeText(PageFour.this, t, Toast.LENGTH_SHORT).show();
		// }
		// }
		// });

	}

}
