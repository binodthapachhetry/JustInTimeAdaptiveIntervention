package edu.neu.android.wocketslib.activities.helpcomment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;

public class SendCommentsExitMsgActivity extends BaseActivity {

	private static String TAG = "WOCKETSHelpComment";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.send_comments_exit_msg_activity);

		Button btnDone = (Button) findViewById(R.id.btnDone);
		btnDone.setOnClickListener(doneBtnListener);

		((ApplicationManager) getApplication()).addActivity(this);
	}

	private OnClickListener doneBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};
}