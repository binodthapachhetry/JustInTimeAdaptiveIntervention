package edu.neu.android.wocketslib.utils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.AlertPlayer;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.DataStorage;

/**
 * This is base activity that will be extended by all other activities. This
 * could be used for doing things that are common to all activities.
 */
public class BaseActivity extends Activity {

	private String TAG = "UnknownApp";
	public static final int KEYGUARD_UNKNOWN = -1;
	public static final int KEYGUARD_IS_LOCKED = 1;
	public static final int KEYGUARD_NOT_LOCKED = 0;
	
	public final String simpleClassName = this.getClass().getSimpleName();
	
	@Override
	public void onNewIntent(Intent i) {
		logNotificationLaunch(i);
	}

	private void logNotificationLaunch(Intent i) {
		if ((i != null)
				&& (i.getBooleanExtra(WocketsNotifier.LAUNCHED_FROM_NOTIFICATION_KEY, false))) {
			Log.o(TAG,
					"Launch from prompt",
					"Time prompted", i.getStringExtra(WocketsNotifier.NOTIFICATION_FIRE_DATE_KEY),
					simpleClassName);
		}

	}

	private void shutdownApp(String msg, boolean isBeep) {
		if (isBeep)
			AlertPlayer.beep(getApplicationContext());
		Log.o(TAG, "BaseActivity", "ShutdownApp");
		((ApplicationManager) getApplication()).killAllActivities();
		finish();
	}

	private void timeWarning(String msg) {
		Toast aToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		aToast.show();
	}

	protected void checkTiming(String aKey) {

		long lastTimeStartedManual = AppInfo.GetStartManualTime(getApplicationContext(), aKey);
		long lastTimePrompted = AppInfo.GetLastTimePrompted(getApplicationContext(), aKey);

		if ((lastTimeStartedManual == 0) && (lastTimePrompted == 0)) {
			// This case of neither time being set shouldn't happen
			shutdownApp("Error. No time set for survey.", false);
		} else if (lastTimePrompted > lastTimeStartedManual) {
			// App started because of a prompt; Check if either running out of
			// time or not completed

			if ((System.currentTimeMillis() - lastTimePrompted) > Globals.MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS) {
				shutdownApp(
						"Sorry. You must complete the survey within " + Math.floor(Globals.MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS / 1000.0 / 60.0)
								+ " minutes of the prompt.", false);
			} else if ((Globals.MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS - (System.currentTimeMillis() - lastTimePrompted)) < 30000) {
				// Show warning if less than 30 seconds to go; otherwise do nothing
				timeWarning("Hurry. You only have "
						+ Math.round((Globals.MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_COMPLETION_MS - (System.currentTimeMillis() - lastTimePrompted)) / 1000.0)
						+ " seconds to complete the survey!");
			}
		} else {
			// App started manually; Check if either running out of time or not
			// completed

			if ((System.currentTimeMillis() - lastTimeStartedManual) > Globals.MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS) {
				shutdownApp(
						"Sorry. You must complete the survey within "
								+ Math.floor(Globals.MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS / 1000.0 / 60.0) + " minutes of starting it.",
						false);
			} else if ((Globals.MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS - (System.currentTimeMillis() - lastTimeStartedManual)) < 30000) {
				// Show warning if less than 30 seconds to go; otherwise do
				// nothing
				timeWarning("Hurry. You only have "
						+ Math.round((Globals.MAX_TIME_ALLOWED_BETWEEN_MANUAL_START_AND_COMPLETION_MS - (System.currentTimeMillis() - lastTimeStartedManual)) / 1000.0)
						+ " seconds to complete the survey!");
			}
		}
	}

	public void onCreate(Bundle savedInstanceState, String aTAG) {
		super.onCreate(savedInstanceState);
		((ApplicationManager) getApplication()).addActivity(this);
		this.TAG = aTAG;
		logNotificationLaunch(getIntent());
		// IdleTimeKeeper.getInst().init((ApplicationManager) getApplication(),
		// 600);

		// Logs once only
		AppUsageLogger.logVersion(TAG, this.getApplicationInfo().packageName, this.getApplicationContext());

		// Logs for every activity
		AppUsageLogger.logSubjectID(TAG, DataStorage.GetSubjectID(this.getApplicationContext()));
		AppUsageLogger.logActivities(TAG);		

		// AppUsageLogger.onCreate(TAG, getClass().getName(),
		// screenLockStatus());
	}

	public void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, TAG);
	}

	private int screenLockStatus() {
		KeyguardManager km = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		if (km != null)
			if (km.inKeyguardRestrictedInputMode())
				return KEYGUARD_IS_LOCKED;
			else
				return KEYGUARD_NOT_LOCKED;
		return KEYGUARD_UNKNOWN;
	}

	public void onStart() {
		super.onStart();
		// AppUsageLogger.onStart(TAG, getClass().getName(),
		// screenLockStatus());
	}

	public void onRestart() {
		super.onRestart();
		// AppUsageLogger.onRestart(TAG, getClass().getName(),
		// screenLockStatus());
	}

	public void onResume() {
		super.onResume();
		// Log.i(TAG, "PID: " + android.os.Process.myPid());

		if (screenLockStatus() == KEYGUARD_NOT_LOCKED) {
			// IdleTimeKeeper.getInst().restartTimer();
			// AppUsageLogger.onResume(TAG, getClass().getName(),
			// KEYGUARD_NOT_LOCKED);
			UsageCollector.getInst().activityResumed(getClass().getName());
		}
		// else
		// AppUsageLogger.onResume(TAG, getClass().getName(),
		// screenLockStatus());
	}

	public void onPause() {
		super.onPause();
		// AppUsageLogger.onPause(TAG, getClass().getName(),
		// screenLockStatus());
		UsageCollector.getInst().activityPaused(getClass().getName());
	}

	public void onDestroy() {
		super.onDestroy();
		// AppUsageLogger.onDestroy(TAG, getClass().getName(),
		// screenLockStatus());
	}

	public void onStop() {
		super.onStop();
		// AppUsageLogger.onStop(TAG, getClass().getName(), screenLockStatus());

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// AppUsageLogger.onActivityResult(TAG, getClass().getName(),
		// requestCode, resultCode, screenLockStatus());
	}

}
