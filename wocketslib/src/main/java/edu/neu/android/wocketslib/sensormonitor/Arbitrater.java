package edu.neu.android.wocketslib.sensormonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PackageChecker;
import edu.neu.android.wocketslib.utils.PhonePrompter;

public class Arbitrater implements ArbitraterInterface {
	private static final String TAG = "DefaultArbitrater";

	private static Context aContext = null;

	// Temp for one run
	private boolean isOkAudioPrompt = false;
	// private static boolean isPostponedPromptWaiting = false;
	// private static Timer alert_timer;

	// Status info
	private ArrayList<String> someTasks = new ArrayList<String>();

	public Arbitrater() {

	}

	public Arbitrater(Context aContext) {
		Arbitrater.aContext = aContext;
	}

	// TODO change to private
	public void PromptApp(Context aContext, String aKey, boolean isAudible,
						  boolean isPostponed) {

		Log.i(TAG, "prompt: " + aKey + ",audible: " + isAudible
				+ ",postponed: " + isPostponed);
		String msg = PhonePrompter.StartPhoneAlert(TAG, aContext, isAudible);

		Intent appIntentToRun = new Intent(Intent.ACTION_MAIN);
		String pkg = AppInfo.GetPackageName(aContext, aKey);
		String className = AppInfo.GetClassName(aContext, aKey);

		// Indicate that a prompt took place so another one isn't done too soon
		// DataStorage.setTime(aContext, DataStorage.KEY_LAST_ALARM_TIME,
		// System.currentTimeMillis());
		// Indicate that this particular app was prompted
		AppInfo.SetLastTimePrompted(aContext, aKey, System.currentTimeMillis());
		// AppInfo.SetStartEntryTime(aContext, aKey,
		// System.currentTimeMillis());

		appIntentToRun.setClassName(pkg, pkg + className);
		appIntentToRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			Intent anIntent = new Intent(aContext,
					Globals.getClassFromKey(aKey));
			anIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			aContext.startActivity(anIntent);
			// aContext.startActivity(appIntentToRun);
		} catch (Exception e) {
			Log.e(TAG,
					"Trouble starting activity for " + aKey + ": "
							+ e.toString());
		}
		appIntentToRun = null;

		// Give the audio or vibration time to work
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getAndPrintPromptingSchedule() {
		if (Globals.IS_DEBUG) {
			long[] promptTimes = DataStorage.getPromptTimes(aContext);
			if (promptTimes != null) {
				for (int i = 0; i < promptTimes.length; i++) {
					Log.d(TAG,
							"SAVED Time to prompt: "
									+ DateHelper.getDate(promptTimes[i]));
				}
			} else {
				Log.d(TAG, "No prompt times");
			}
		}
	}

	private void setAndSavePromptingSchedule(int promptsPerDay,
											 int startTimeHour, int endTimeHour) {
		long totalPromptingWindowMS = (endTimeHour - startTimeHour) * 60 * 60 * 1000;
		long intervalIncMS = (long) (totalPromptingWindowMS / ((double) promptsPerDay));
		Random r = new Random();
		long promptTimes[] = new long[promptsPerDay];

		int startIntervalTimeMS = startTimeHour * 60 * 60 * 1000;

		long startDayTime = DateHelper.getDailyTime(0, 0); // Midnight

		StringBuffer promptSchedule = new StringBuffer();

		promptSchedule.append("Scheduled prompts today: " + promptsPerDay
				+ Globals.NEWLINE);
		promptSchedule.append("Start hour: " + startTimeHour + Globals.NEWLINE);
		promptSchedule.append("End hour: " + endTimeHour + Globals.NEWLINE);

		for (int i = 0; i < promptsPerDay; i++) {
			// Add a random number of MS to the first start time block
			promptTimes[i] = startDayTime + startIntervalTimeMS + i
					* intervalIncMS + r.nextInt((int) intervalIncMS);
			Log.i(TAG, "Time to prompt: " + DateHelper.getDate(promptTimes[i]));

			if (i > 0) {
				// Shift any prompts too close together
				if ((promptTimes[i] - promptTimes[i - 1]) < Globals.MIN_MS_BETWEEN_SCHEDULED_PROMPTS) {
					promptTimes[i] += (Globals.MIN_MS_BETWEEN_SCHEDULED_PROMPTS - (promptTimes[i] - promptTimes[i - 1]));
					Log.i(TAG,
							"SHIFTED Time to prompt: "
									+ DateHelper.getDate(promptTimes[i]));
				}
			}

		}

		if (promptsPerDay > 0)
			for (int i = 0; i < promptsPerDay; i++) {
				promptSchedule.append("Prompt: "
						+ DateHelper.getDate(promptTimes[i]) + Globals.NEWLINE);
			}

		ServerLogger.addNote(aContext, promptSchedule.toString(),
				Globals.NO_PLOT);

		DataStorage.setPromptTimes(aContext, promptTimes);
	}

	/**
	 * This is the key function to determine which tasks are active for
	 * prompting at any given moment. How to prompt is based on which tasks are
	 * active.
	 *
	 * @param aContext
	 */
	private void GatherPendingTasks(Context aContext) {
		someTasks.clear();

		String aKey = Globals.LEVEL3PA;
		long currentTime = System.currentTimeMillis();
		long lastTimeCompleted = AppInfo.GetLastTimeCompleted(aContext, aKey);
		if (lastTimeCompleted > currentTime) {
			AppInfo.SetLastTimeCompleted(aContext, aKey, currentTime);
		}
		long lastTimePrompted = AppInfo.GetLastTimePrompted(aContext, aKey);
		if (lastTimeCompleted > currentTime) {
			AppInfo.SetLastTimePrompted(aContext, aKey, currentTime);
		}
		long timeSinceCompleted = currentTime - lastTimeCompleted;
		long timeSincePrompted = currentTime - lastTimePrompted;
		long[] somePromptTimes = DataStorage.getPromptTimes(aContext);
		// Will be 0 if none
		long lastScheduledPromptTime = getLastScheduledPromptTime(currentTime,
				somePromptTimes);

		Log.i(TAG,
				"LastScheduledPromptTime: "
						+ DateHelper.getDate(lastScheduledPromptTime));

		// If no scheduled prompt, just return
		if (lastScheduledPromptTime == 0)
			return;

		if (lastTimeCompleted == 0)
			Log.i(TAG, "LastTimeCompleted: never");
		else
			Log.i(TAG,
					"LastTimeCompleted: "
							+ DateHelper.getDate(lastTimeCompleted));

		// If completed after the last scheduled prompt time, then just return
		if (lastTimeCompleted > lastScheduledPromptTime)
			return;

		if (Globals.IS_DEBUG) {
			Log.d(TAG, "Time from scheduled prompt (min): "
					+ ((currentTime - lastScheduledPromptTime) / 1000 / 60));
			Log.d(TAG, "Time since completed (min): "
					+ (timeSinceCompleted / 1000 / 60));
			Log.d(TAG, "Time since prompted (min): "
					+ (timeSincePrompted / 1000 / 60));
		}

		// Check if time to prompt
		if (((currentTime - lastScheduledPromptTime) < (Globals.REPROMPT_TIMES
				* Globals.REPROMPT_DELAY_MS + 2000)
				&& (timeSinceCompleted > Globals.MIN_MS_BETWEEN_SCHEDULED_PROMPTS) && (timeSincePrompted > Globals.REPROMPT_DELAY_MS))) {
			Log.i(TAG, "Prompt!");
			someTasks.add(aKey);
		}
	}

	// Return 0 if none, otherwise return time
	private long getLastScheduledPromptTime(long currentTime,
											long[] somePromptTimes) {
		long lastTime = 0;

		if (somePromptTimes == null)
			return lastTime;

		for (int i = 0; i < somePromptTimes.length; i++) {
			if (somePromptTimes[i] > currentTime)
				return lastTime;
			else
				lastTime = somePromptTimes[i];
		}
		return lastTime;
	}

	/**
	 * Set which apps are available based on intervention schedule based on days
	 * into the study
	 *
	 * @param aContext
	 */
	private void SetAppActivityUsingSchedule(long lastArbitrationTime,
											 int studyDay, boolean isNewSoftwareVersion) {
		boolean isForceReset = DataStorage.isForceReset(aContext);
		String thisWeekMsg = "";

		if ((!DateHelper.isToday(lastArbitrationTime)) || isNewSoftwareVersion
				|| isForceReset) {
			// if (Globals.IS_DEBUG)
			if (isNewSoftwareVersion)
				Log.i(TAG, "Resetting because new software version");
			else if (isForceReset)
				Log.i(TAG, "Resetting because force reset");
			else
				Log.i(TAG, "Resetting because day changed");

			// This is causing a problem so commented out ...
			// Log a few things we want to be sure are logged every day
			// if (VersionChecker.isNewUpdateAvailable(aContext))
			// Log.h(TAG, "NewVersionAvailable", Log.NO_LOG_SHOW);

			PackageChecker.installedPackageLogging(TAG, aContext);

			// // Force download check of all key files, including tutorials
			// Intent i = new Intent(aContext, FileGrabberService.class);
			// i.putExtra(FileGrabberService.EXTRA_FILES_LIST,
			// Globals.MASTER_FILE_LIST);
			// aContext.startService(i);

			// This is too project specific
			// BasicLogger.basicLogging(TAG, aContext);

			if (isForceReset) {
				DataStorage.setIsForceReset(aContext, false);
			}

			// Set a lock so main app waits until this is done before doing
			// anything
			// because variables are temporarily cleared then reset. Don't want
			// to
			// access during that.
			DataStorage.setIsInUpdate(aContext, true);

			AppInfo.resetAvailabilityAndTiming(aContext);

			// All conditions for first seven weeks

			AppInfo.SetIsAvailable(aContext, Globals.THISWEEK, true);
			AppInfo.SetIsAvailable(aContext, Globals.LEVEL3PA, true);
			AppInfo.SetIsAvailable(aContext, Globals.SWAP, true);
			AppInfo.SetIsAvailable(aContext, Globals.GUIDELINES, true);
			AppInfo.SetIsAvailable(aContext, Globals.STATUS, true);
			AppInfo.SetIsAvailable(aContext, Globals.GETHELP, true);
			AppInfo.SetIsAvailable(aContext, Globals.SURVEY, true);

			int promptsPerDay = Globals.DEFAULT_PROMPTS_PER_DAY;
			int startTimeHour = Globals.DEFAULT_START_HOUR;
			int endTimeHour = Globals.DEFAULT_END_HOUR;

			thisWeekMsg = "First day? This is where you can check for the latest on what is going on for your WOCKETS app for the study and learn about the current activity prompting schedule.";

			// Week 1
			// ------------------------------------------------------------------
			// LEVEL3PA prompts every 3 hours during waking hours
			// if ((studyDay >= 1) && (studyDay <= 7)) {
			// promptsPerDay = 14;
			// startTimeHour = Globals.DEFAULT_START_HOUR;
			// endTimeHour = Globals.DEFAULT_END_HOUR;
			// // AppInfo.SetPromptIntervalMS(aContext, AppInfo.LEVEL3PA,
			// HOURS3_MS);
			// thisWeekMsg =
			// "Week 1: Welcome to WOCKETS and your WOCKET App! Check back here to learn more about what your Wocket software is doing at any time during the study."
			// + SKIPLINE
			// +
			// "In this study you will wear Wocket sensors 24/7. Add more intro description here."
			// + SKIPLINE
			// +
			// "Today your phone will prompt you to enter your physical activity about once every 4 hours. The phone is doing this so that the researchers can learn more about whether the Wockets are detecting the activities you are doing. After a few days of prompting, you will get a break. Please try to answer the prompts when you get them. You can also click on \'Track Your Activity\' whenever you would like to report a specific activity that you have done.";
			// }
			//
			// // Week 2 (first part)
			// // ----------------------------------------------------
			// // if (studyDay > 7) {
			// // AppInfo.SetIsAvailable(aContext, AppInfo.LEVEL3PA, true);
			// // }
			// // Goals and PA tracking
			// if (studyDay > 7) {
			// promptsPerDay = Globals.DEFAULT_PROMPTS_PER_DAY;
			// startTimeHour = Globals.DEFAULT_START_HOUR;
			// endTimeHour = Globals.DEFAULT_END_HOUR;
			// // AppInfo.SetPromptIntervalMS(aContext, AppInfo.LEVEL3PA,
			// HOURS4_MS);
			// thisWeekMsg =
			// "Today your phone will prompt you to enter your physical activity about once every 4 hours. The phone is doing this so that the researchers can learn more about whether the Wockets are detecting the activities you are doing. After a few days of prompting, you will get a break. Please try to answer the prompts when you get them. You can also click on \'Track Your Activity\' whenever you would like to report a specific activity that you have done.";
			// }

			DataStorage.setThisWeekMsg(aContext, thisWeekMsg);

			setAndSavePromptingSchedule(promptsPerDay, startTimeHour,
					endTimeHour);

		}
		// Always unset this in case program was updated at awkward time
		// If this is before prior }, it is possible to get stuck always in an
		// update!

		DataStorage.setIsInUpdate(aContext, false); // TODO Change to use a date
		// so this is less brittle
		// to an odd crash
	}

	// TODO Fix to use settings by week
	public static boolean isOkAudioPrompt() {
		Date now = new Date();
		int hour = now.getHours();
		// if (Constants.IS_DEBUG) Log.e(TAG, "CURRENT HOUR: " + hour);
		if ((hour < Globals.AUDIO_PROMPT_START_HOUR)
				|| (hour >= Globals.AUDIO_PROMPT_END_HOUR))
			return false;
		else
			return true;
	}

	public void doArbitrate(boolean isNewSoftwareVersion) {
		/**
		 * This is the app that decides what to do in terms of prompting each
		 * time it is called.
		 */
		if (Globals.IS_DEBUG) {
			Log.d(TAG, "Arbitrate in library");
			Log.d(TAG, "Begin arbitrate");
		}

		// Temporary
		getAndPrintPromptingSchedule();

		// Mark that arbitration taking place
		long lastArbitrationTime = DataStorage
				.getLastTimeArbitrate(aContext, 0);
		DataStorage.setLastTimeArbitrate(aContext, System.currentTimeMillis());
		int studyDay = DataStorage.getDayNumber(aContext, true);

		// Set which apps are available based on the day of the study
		SetAppActivityUsingSchedule(lastArbitrationTime, studyDay,
				isNewSoftwareVersion);

		isOkAudioPrompt = isOkAudioPrompt();

		// if (Globals.IS_DEBUG)
		// Log.e(TAG, "IS OK TO AUDIO PROMPT: " + isOkAudioPrompt);
		// // Log.h(TAG, aDataStore.GetSummaryString(getApplicationContext()));
		// if (Globals.IS_DEBUG)
		// Log.e(TAG, "STUDY DAY: " +
		// DataStorage.getDayNumber(getApplicationContext(), true));
		// if (Globals.IS_DEBUG)
		// Log.e(TAG, AppInfo.AllAppStatus(getApplicationContext()));

		// Determine which apps are in the task list as needing to run
		// Sets the postponed app info as well
		GatherPendingTasks(aContext);

		String aKey = null;

		// Now just check tasks
		if (someTasks.size() > 0) {
			aKey = someTasks.get(0);
			PromptApp(aContext, aKey, isOkAudioPrompt, false);
		}

		if (Globals.IS_DEBUG)
			Log.d(TAG, "End arbitrate");
	}

	@Override
	public void doWakefulArbitrate() {

	}

	@Override
	public void doOnSensorChangedArbitrate(SensorEvent event, Object extraData) {

	}
}