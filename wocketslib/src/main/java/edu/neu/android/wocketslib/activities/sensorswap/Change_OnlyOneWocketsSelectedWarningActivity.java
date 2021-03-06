package edu.neu.android.wocketslib.activities.sensorswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Wockets;

public class Change_OnlyOneWocketsSelectedWarningActivity extends BaseActivity{
	private static final String TAG = "Change_OnlyOneWocketsSelectedWarningActivity";
	private Button justBtn;
	private Button addBtn;
	private TextView tv;
	private Wockets record = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_change_onlyonewocketswornwarning_activity);
		justBtn = (Button)this.findViewById(R.id.page16just);
		addBtn = (Button)this.findViewById(R.id.page16add);
		tv = (TextView)this.findViewById(R.id.pagesixteen_text);
		
		record = (Wockets) this.getIntent().getSerializableExtra("record");
		int i = this.getIntent().getIntExtra("counter", 0);
		String num = "";
		switch(i){
		case 1:num = " only one ";break;
		case 0:num = " no ";break;
		}
		String text = this.getString(R.string.pagesixteenpart1)+ 
		 num + this.getString(R.string.pagesixteenpart2);
		if(i!=0){
			tv.setText(text);
			justBtn.setText("Just One");	
			addBtn.setText("Add Another");
			
		}
		else{
			tv.setText(text);
			justBtn.setText("Just None");	
			addBtn.setText("Add One");
		}
		justBtn.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Change_OnlyOneWocketsSelectedWarningActivity.this,Change_DoubleCheckActivity.class);
				i.putExtra("record", record);
				startActivity(i);
				finish();			
				}
			});
		addBtn.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
			});
	}

}