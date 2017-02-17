

package edu.neu.android.wocketslib.wakefulintent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import edu.neu.android.wocketslib.utils.Log;

abstract public class WakefulIntentService extends IntentService {
	private static final String TAG = "WakefulIntentService";

	public static final String EXTRA_TYPE_MSG = "EXTRA_TYPE_MSG";
	private static final String LOCK_NAME_STATIC = "edu.neu.android.wocketslib.wakefulintent.WakefulIntentService";
	private static PowerManager.WakeLock lockStatic = null;

	abstract protected void doWakefulWork(Intent intent);

	private static int totalLocks = 0;

	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
			totalLocks++;
			Log.d(TAG, "Get lock: " + totalLocks);
		}

		return (lockStatic);
	}

	public static void sendWakefulWork(Context ctxt, Intent i) {
		if (PackageManager.PERMISSION_DENIED == ctxt.getPackageManager().checkPermission("android.permission.WAKE_LOCK",
				ctxt.getPackageName())) {
			throw new RuntimeException(
					"Application requires the WAKE_LOCK permission!");
		}

		getLock(ctxt).acquire();
		Log.d(TAG,"Get wakelock in onHandleIntent. Is held? " + lockStatic.isHeld());
		Log.d(TAG, "Held locks: " + totalLocks);
		ctxt.startService(i);
	}

	public static void sendWakefulWork(Context ctxt, Class<?> clsService, String msg) {
		Intent i = new Intent(ctxt, clsService);
		i.putExtra(EXTRA_TYPE_MSG, msg);
		sendWakefulWork(ctxt, i);
	}

	public WakefulIntentService(String name) {
		super(name);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!getLock(this).isHeld()) { // fail-safe for crash restart
			Log.d(TAG, "Failsafe for crash restart. Trying to get lock");
			getLock(this).acquire();
			Log.d(TAG, "Held locks: " + totalLocks);
		}

		Log.d(TAG, "OnStartCommand");
		Log.d(TAG, "Held locks: " + totalLocks);
		super.onStartCommand(intent, flags, startId);

		return (START_REDELIVER_INTENT);
	}

	@Override
	final protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
		try {
			Log.d(TAG, "Start");
			doWakefulWork(intent);
			Log.d(TAG, "End");
		} finally {
			if (getLock(this).isHeld())
			{
				Log.d(TAG, "Total locks: " + totalLocks);
				Log.d(TAG,"Release wakelock in onHandleIntent");
				getLock(this).release();
				totalLocks--;
				Log.d(TAG, "Held locks: " + totalLocks);
			}
			else
			{
				Log.e(TAG,"Could not release a wakelock in onHandleIntent because wakelock not held. Held locks: " + totalLocks);
			}
		}

		WakefulIntentService.this.stopSelf();
	}
}