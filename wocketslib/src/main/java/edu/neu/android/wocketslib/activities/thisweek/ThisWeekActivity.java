package edu.neu.android.wocketslib.activities.thisweek;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

//TODO This does not deal well if there is no network. Slow start

public class ThisWeekActivity extends BaseActivity {
	private static final String TAG = "ThisWeek";
	TextView txtThisWeek = null;
	private static long lastTimeSentNote = 0;
	private static long MINUTES_3_IN_MS = 1000 * 60 * 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.thisweek_activity);

		Log.h(TAG, "Review this week", Log.LOG_SHOW);
		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this, false); // Show toast

		Button btnDone = (Button) findViewById(R.id.btnDone);
		btnDone.setOnClickListener(doneBtnListener);

		txtThisWeek = (TextView) findViewById(R.id.textThisWeek);

		((ApplicationManager) getApplication()).addActivity(this);
	}

	public void onResume() {
		super.onResume();

		txtThisWeek.setText(getTextForNow());

		if ((System.currentTimeMillis() - lastTimeSentNote) > MINUTES_3_IN_MS) // more
																				// than
																				// 3
																				// minutes
		{
			ServerLogger.sendNote(getApplicationContext(), "Checked Right Now in Study", true);
			lastTimeSentNote = System.currentTimeMillis();
		}
	}

	private String getTextForNow() {
		return DataStorage.getThisWeekMsg(getApplicationContext(), "Hmm. That's odd. The Wockets App is not sure. Please check back later!");
	}

	private OnClickListener doneBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			AppInfo.MarkAppCompleted(getApplicationContext(), Globals.THISWEEK);
			((ApplicationManager) getApplication()).killActivity(ThisWeekActivity.this);
		}
	};
}
