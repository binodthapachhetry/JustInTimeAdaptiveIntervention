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

public class Swap_CancellingWarningActivity extends BaseActivity{
	private static final String TAG = "Swap_CancellingWarningActivity"; 
	private Button yesBtn;
	private Button noBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_cancellingwarning_activity);
		yesBtn = (Button)this.findViewById(R.id.page21yes);
		noBtn = (Button)this.findViewById(R.id.page21no);
		
		noBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Swap_CancellingWarningActivity.this,SwapWocketsBodyDiagramActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(i);
				finish();
			}
			});
		yesBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
//				Intent intent = new Intent(Intent.ACTION_MAIN); 
//				intent.addCategory(Intent.CATEGORY_HOME); 
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//				startActivity(intent);
//				finish();
				Log.i(Globals.SWAP_TAG, "Exit app, swap Wockets Canceled");
				((ApplicationManager) getApplication()).killAllActivities();
			}
			});
	}

}
