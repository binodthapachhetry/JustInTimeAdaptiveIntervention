package edu.neu.android.wocketslib.activities.sensorswap;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class Change_NoWocketsSelectedWarningActivity extends BaseActivity{
	private static final String TAG = "Change_NoWocketsSelectedWarningActivity";
	
	private Button yesBtn;
	private Button noBtn;
	private TextView tv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_change_nowocketswornwarning_activity);
		yesBtn = (Button)this.findViewById(R.id.page19yes);
		noBtn = (Button)this.findViewById(R.id.page19no);
		tv = (TextView)this.findViewById(R.id.pagenineteen_text);
		
		int i = this.getIntent().getIntExtra("counter", 0);
		String num = "";
		String text = "";
		switch(i){
		case 0:num = " no ";break;
		case 1:num = " only one ";break;
		}
			text = this.getString(R.string.pagenineteenpart1)+ 
							 num +this.getString(R.string.pagenineteenpart2);			
		tv.setText(text);
		noBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
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
				Log.i(Globals.SWAP_TAG, "Exit app, exit before change");
				((ApplicationManager) getApplication()).killAllActivities();
			}
			});
	}

}
