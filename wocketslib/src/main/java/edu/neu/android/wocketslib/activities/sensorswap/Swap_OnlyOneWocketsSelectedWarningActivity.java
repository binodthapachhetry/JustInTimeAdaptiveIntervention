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

public class Swap_OnlyOneWocketsSelectedWarningActivity extends BaseActivity {
	private static final String TAG = "Swap_OnlyOneWocketsSelectedWarningActivity"; 
	
	private Button justBtn;
	private Button addBtn;
	private TextView tv;
	private Wockets record = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.swap_onlyonewocketswornwarning_activity);
		justBtn = (Button) this.findViewById(R.id.page16just);
		addBtn = (Button) this.findViewById(R.id.page16add);
		tv = (TextView) this.findViewById(R.id.pagesixteen_text);

		record = (Wockets) this.getIntent().getSerializableExtra("record");
		int i = this.getIntent().getIntExtra("counter", 0);
		String num = "";
		switch (i) {
		case 1:
			num = " only one ";
			break;
		case 0:
			num = " no ";
			break;
		}
		String text = this.getString(R.string.pagesixteenpart1) + num + this.getString(R.string.pagesixteenpart2);
		tv.setText(text);
		if (i != 0) {
			justBtn.setText("Just One");
			addBtn.setText("Add Another");

		} else {
			justBtn.setText("Just None");
			addBtn.setText("Add One");
		}
		justBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Swap_OnlyOneWocketsSelectedWarningActivity.this, Swap_DoubleCheckActivity.class);
				i.putExtra("record", record);
				startActivity(i);
				finish();
			}
		});
		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

}
