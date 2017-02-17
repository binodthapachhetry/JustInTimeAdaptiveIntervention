package edu.neu.android.wocketslib.activities.sensorswap;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;

public class Change_DisplayMoreThanTwoWocketsSelectedWarningActivity extends BaseActivity {
	private static final String TAG = "Change_DisplayMoreThanTwoWocketsSelectedWarningActivity";
	private Button backBtn;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_change_displaymorethan2warning_activity);
		backBtn = (Button) this.findViewById(R.id.page15back);
		tv = (TextView) this.findViewById(R.id.pagefifteen_text);
		String location = this.getIntent().getStringExtra("location");
		String text = this.getString(R.string.pagefifteen) + " " + location;
		tv.setText(text);

		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

}
