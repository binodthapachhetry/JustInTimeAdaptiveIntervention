package edu.neu.android.wocketslib.activities.helpcomment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;

public class GetHelpExitMsgActivity extends BaseActivity {

	private static String TAG = "WOCKETSHelpComment";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.get_help_exit_msg_activity);

		Button btnCallHotline = (Button) findViewById(R.id.btnCallHotline);
		btnCallHotline.setOnClickListener(callHotlineBtnListener);

		Button btnDone = (Button) findViewById(R.id.btnDone);
		btnDone.setOnClickListener(doneBtnListener);

		((ApplicationManager) getApplication()).addActivity(this);
	}

	public void onResume() {
		super.onResume();
	}

	private OnClickListener callHotlineBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Globals.HOTLINE_NUMBER));
			startActivity(intent);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};
	private OnClickListener doneBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};
}