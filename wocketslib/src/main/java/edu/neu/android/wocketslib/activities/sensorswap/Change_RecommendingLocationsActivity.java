package edu.neu.android.wocketslib.activities.sensorswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Wockets;

public class Change_RecommendingLocationsActivity extends BaseActivity{
	private static final String TAG = "Change_RecommendingLocationsActivity"; 
	private Button conBtn;
	private Button chaBtn;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_change_recommendinglocation_activity);
		chaBtn = (Button)this.findViewById(R.id.page14cha);
		conBtn = (Button)this.findViewById(R.id.page14con);
		final Wockets record = (Wockets) this.getIntent().getSerializableExtra("record");
		chaBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				finish();
			}
			});
		conBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
			Intent i = new Intent(Change_RecommendingLocationsActivity.this,Change_DoubleCheckActivity.class);
			i.putExtra("record", record);
			startActivity(i);
			finish();
			}
			});
	}

}

