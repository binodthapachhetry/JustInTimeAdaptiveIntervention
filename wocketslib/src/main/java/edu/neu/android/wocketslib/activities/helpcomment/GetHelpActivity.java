package edu.neu.android.wocketslib.activities.helpcomment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.MemoryChecker;

public class GetHelpActivity extends BaseActivity {
 
	private static String TAG = "WOCKETSHelpComment";
//	final MonitorServiceBroadcastReceiver Logging = new MonitorServiceBroadcastReceiver();


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.get_help_activity);

		Log.i(TAG, "Get help");

		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this);

		Button btnGetHelp = (Button) findViewById(R.id.btnGetHelp);
		btnGetHelp.setOnClickListener(getHelpBtnListener);

		TextView txtVersion = (TextView)findViewById(R.id.txtVersion);
		
		txtVersion.setText("You are running version " + DataStorage.getVersion(getApplicationContext(), "unk") + "\n" + MemoryChecker.getMemoryReport());
		
//		txtVersion.setText(txtVersion.getText().toString() + "\n" + "Current version on Android Market: " + VersionChecker.getVersionCodeFromMarket());
		
		Button btnNeverMind = (Button) findViewById(R.id.btnNeverMind);
		btnNeverMind.setOnClickListener(neverMindBtnListener);
		((ApplicationManager) getApplication()).addActivity(this);
	}

	private OnClickListener getHelpBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			Intent intent = new Intent(GetHelpActivity.this, FAQActivity.class);
			startActivity(intent);
//			Logging.addNote(getApplicationContext(), TAG + ": The user clicked on FAQ.", true);

			
		}
	};
	private OnClickListener neverMindBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			((ApplicationManager) getApplication()).killAllActivities();
//			Logging.addNote(getApplicationContext(), TAG + ": The user clicked on nevermind.", true);

			
		}
	};
}