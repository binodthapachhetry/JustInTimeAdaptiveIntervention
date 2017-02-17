package edu.neu.android.wocketslib.activities.paema.model;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import edu.neu.android.wocketslib.utils.DateHelper;

public class LastAccessTimeKeeper {
	private static LastAccessTimeKeeper inst = new LastAccessTimeKeeper();

	public Calendar get4oClockTime() {
		Calendar time4oclock = DateHelper.getCurrentDateNoTime();
		time4oclock.set(Calendar.HOUR, 4);

		// If time is between 12AM and 4AM then date needs to be set to previous
		// day. For this application date change happens at 4:00 AM
		Calendar currentTime = Calendar.getInstance();
		int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
		if (currentHour >= 0 && currentHour < 4)
			time4oclock.setTimeInMillis(time4oclock.getTimeInMillis() - 24 * 60
					* 60 * 1000);
		return time4oclock;
	}

	public Calendar getLastAccessTime(Activity activity) {
		SharedPreferences prefs = activity.getSharedPreferences("LEVEL3PA", 0);

		Calendar time4oclock = get4oClockTime();

		Calendar lastAccessTime = time4oclock;
		if (prefs != null) {
			long lastAccssTimeInMillis = prefs.getLong("LAST_ACCESS_TIME", 0);
			if (lastAccssTimeInMillis != 0) {
				lastAccessTime = Calendar.getInstance();
				lastAccessTime.setTime(new Date(lastAccssTimeInMillis));
			}
		}

		Calendar currentTime = Calendar.getInstance();
		if (currentTime.after(time4oclock)) {
			if (lastAccessTime.before(time4oclock))
				return time4oclock;
			return lastAccessTime;
		}

		Calendar previousDayTime4oclock = DateHelper.getCurrentDateNoTime();
		previousDayTime4oclock.add(Calendar.DATE, -1);
		previousDayTime4oclock.set(Calendar.HOUR, 4);
		if (lastAccessTime.before(previousDayTime4oclock))
			return previousDayTime4oclock;
		return lastAccessTime;
	}

	public void saveCurrentTime(Activity activity) {
		SharedPreferences settings = activity.getSharedPreferences("LEVEL3PA",
				0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("LAST_ACCESS_TIME", Calendar.getInstance()
				.getTimeInMillis());

		// Commit the edits!
		editor.commit();
	}

	public static LastAccessTimeKeeper getInst() {
		return inst;
	}
}
