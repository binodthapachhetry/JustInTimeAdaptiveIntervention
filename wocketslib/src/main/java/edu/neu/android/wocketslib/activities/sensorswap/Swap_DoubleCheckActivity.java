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

public class Swap_DoubleCheckActivity extends BaseActivity {
	private static final String TAG = "Swap_DoubleCheckActivity"; 
	private Button yesBtn;
	private Button noBtn;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swap_doublecheck_activity);
		yesBtn = (Button) this.findViewById(R.id.page17yes);
		noBtn = (Button) this.findViewById(R.id.page17no);
		tv = (TextView) this.findViewById(R.id.pageseventeen_text);

		final Wockets record = (Wockets) this.getIntent().getSerializableExtra("record");
		String text = this.getString(R.string.pageseventeen) + record.toString();
		tv.setText(text);

		noBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		yesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Swap_DoubleCheckActivity.this, Swap_ExitActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("record", record);
				startActivity(i);
				finish();
			}
		});
	}

}
