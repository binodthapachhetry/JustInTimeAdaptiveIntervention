package edu.neu.android.wocketslib.emasurvey;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.PriorityQueue;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.emasurvey.model.QuestionSet;
import edu.neu.android.wocketslib.emasurvey.model.QuestionSetParamHandler;
import edu.neu.android.wocketslib.emasurvey.model.SurveyPromptEvent;
import edu.neu.android.wocketslib.emasurvey.model.SurveyPromptEvent.PROMPT_AUDIO;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PackageChecker;
import edu.neu.android.wocketslib.utils.PhonePrompter;
import edu.neu.android.wocketslib.utils.PhoneVibrator;

/**
 * This class is used as a base class for survey scheduling and prompting.
 * It hides some detail about how to schedule and prompt the survey.
 * Developers need to provide the time for prompt and their own methods 
 * for detecting the condition that whether a new survey should be triggered. 
 * 
 * @author bigbug
 */
abstract public class SurveyScheduler {
	
	protected final static String TAG = "SurveyScheduler";
	
	protected static final String KEY_CS_PROMPT        = "_KEY_CS_PROMPT";
	protected static final String KEY_ALL_PROMPT       = "_KEY_ALL_PROMPT";
	protected static final String KEY_ALL_PROMPT_EVENT = "_KEY_ALL_PROMPT_EVENT";
	protected static final String KEY_SCHEDULE         = "_KEY_SCHEDULE";
	protected static final String LAST_SCHEDULE_UPDATE_TIME = "LAST_SCHEDULE_UPDATE_TIME";
	
	protected Context  mContext; 
	protected Class<?> mClsSurveyActivity; // your survey class
	protected HashMap<String, Class<?>> mType2QuestSet; // hash map from PROMPT_TYPE to QUESTION_SET
	
	/**
	 * Create a new survey scheduler with the given user specific survey activity and question sets.
	 * @param context			The application context
	 * @param clsSurveyActivity The user specific survey activity class
	 * @param type2QuestSet		The hash map which maps the prompt type to its corresponding question set.
	 * 							Usually, each prompt type should has a different question set.
	 */
	public SurveyScheduler(Context context, Class<?> clsSurveyActivity, HashMap<String, Class<?>> type2QuestSet) {
		mContext           = context;
		mClsSurveyActivity = clsSurveyActivity;
		mType2QuestSet     = type2QuestSet;
		
		if (context == null || clsSurveyActivity == null || type2QuestSet == null || type2QuestSet.size() <= 0) {
			throw new IllegalArgumentException("Input parameters should not be null");
		}				
	}	
	
	/**
	 * Try to prompt a new survey based on the updated prompt schedule.
	 * @param isNewSoftwareVersion  Indicate whether the running app is updated to a new version.
	 * @return true if the survey has been prompted, otherwise false
	 */
	public boolean tryToPromptSurvey(boolean isNewSoftwareVersion) {
		
		boolean result = false;
		
		// Detect and add new schedule if possible 
		updateSchedule(isNewSoftwareVersion);
				
		// If it's the time for prompting
		if (isInPromptTime()) {
			
			// Get the survey prompt event from the schedule.
			// This event represents a scheduled prompting. The same event is used for re-prompting.
			SurveyPromptEvent spe = getPendingSurvey();
			
			if (spe != null) {									
				// Request to see whether this prompt is acceptable by the survey activity
				int requestResult = requestPrompt(spe);
				switch (requestResult) {
				case PROMPT_REQUEST_ACCEPTED:					
					result = promptSurvey(spe, false);	
					break;
				case PROMPT_REQUEST_NOT_ACCEPTED:
					Log.i(TAG, "prompt hasn't been accepted");
					break;
				case PROMPT_REQUEST_COMPLETED:
					Log.i(TAG, "prompt has been completed");
					break;
				}									
			}
		}		
		
		return result;
	}
	
	/**
	 * Update the prompt schedule. The user should write his/her own prompt detection code 
	 * in detectNewPrompt for the prompt to be triggered. The time for detecting the prompt
	 * is determined by isInPromptTime, which also should be implemented by the user.
	 * @param isNewSoftwareVersion	Indicate whether the running app is updated to a new version.
	 */
	protected void updateSchedule(boolean isNewSoftwareVersion) {
		// Set a lock so main app waits until this is done before doing anything
		// because variables are temporarily cleared during the reset issue.
		DataStorage.setIsInUpdate(mContext, true);
		
		// Reset the prompting schedule if necessary
		if (isResetNeeded(isNewSoftwareVersion)) {
			resetSchedule();
		}
		
		// Detect whether there should be a new prompt if it's in the prompt time
		if (isInPromptTime()) {
			SurveyPromptEvent spe = detectNewPrompt();				
			if (spe != null) {	
				Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
				addPromptToSchedule(gson.toJson(spe), spe.getScheduledPromptTime());								
			}
		}
		
		DataStorage.SetValue(mContext, LAST_SCHEDULE_UPDATE_TIME, System.currentTimeMillis());

		// Always unset this in case program was updated at awkward time
		// If this is before prior }, it is possible to get stuck always in an update!
		DataStorage.setIsInUpdate(mContext, false);
		
		if (Globals.IS_DEBUG) {
			StringBuffer sb = new StringBuffer();
			long[] promptTimes = DataStorage.getPromptTimesKey(mContext, KEY_ALL_PROMPT);	
			if (promptTimes != null) {
				for (int i = 0; i < promptTimes.length; ++i) {
					String speJSON = DataStorage.GetValueString(mContext, KEY_ALL_PROMPT_EVENT + promptTimes[i], null);	
					Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
					SurveyPromptEvent spe = speJSON != null ? gson.fromJson(speJSON, SurveyPromptEvent.class) : null;
					Date time = new Date(promptTimes[i]);
					sb.append(time + " " + (spe != null ? spe.getPromptType() : "null") + "\n");
				}
				Log.i(TAG, sb.toString());	
			}
		}
	}
	
	/**
	 * The user should write his/her own prompt detection code in this method for the prompt
	 * to be triggered. This method must return a SurveyPromptEvent that at least tells the 
	 * survey scheduler about the prompt type, scheduled prompt time and the reason for this prompt. 
	 * @return A SurveyPromptEvent that at least having the information about the prompt type, 
	 * 		   scheduled prompt time and the reason for which this prompt is triggered.
	 */
	abstract protected SurveyPromptEvent detectNewPrompt();
	
	/**
	 * This method determines the time period that detectNewPrompt should be called. 
	 * The user must define the time period which is suitable for the detection code to work.
	 * @return true if detectNewPrompt should be called, otherwise false.
	 */
	abstract protected boolean isInPromptTime();
	
	protected boolean isResetNeeded(boolean isNewSoftwareVersion) {
		long lastUpdateTime = DataStorage.GetValueLong(mContext, LAST_SCHEDULE_UPDATE_TIME, 0);
		boolean isForceReset = DataStorage.isForceReset(mContext);
		boolean hasCrossDate = !DateHelper.isToday(lastUpdateTime);
		
		if (isNewSoftwareVersion) {
			Log.i(TAG, "Resetting because new software version");
		} else if (isForceReset) {
			Log.i(TAG, "Resetting because force reset");
			DataStorage.setIsForceReset(mContext, false);
		} else if (hasCrossDate) {
			Log.i(TAG, "Resetting because day changed");
		} else {
			return false; // reset is not needed currently
		}
		
		return true;
	}
	
	/**
	 * This method determines which sound should be played during the survey prompting.
	 * Usually different types of prompt should have different kinds of sound.
	 * @param spe	A SurveyPromptEvent that identified a detected prompt.
	 * @return The sound id from PhonePrompter. If the id equals CHIMES_NONE, it will
	 * 		   lead to a silent prompt.
	 */
	protected int onAudioSelecting(final SurveyPromptEvent spe) {
		return PhonePrompter.CHIMES_NONE; // prompts with no audio
	}
	
	protected long[] onVibrationSelecting(final SurveyPromptEvent spe) {
		return PhoneVibrator.VIBRATE_INTENSE;
	}

	protected boolean promptSurvey(SurveyPromptEvent spe, boolean isPostponed) {
		long lastScheduledPromptTime = getLastScheduledPromptTime(System.currentTimeMillis());
		int promptCount = getPromptCount(spe.getID());
		
		// Update the repeated times for this prompt
		setPromptCount(spe.getID(), promptCount + 1);
		
		// Add more information to the survey prompt event
		String surveyClassName = mType2QuestSet.get(spe.getPromptType()).getCanonicalName();		
		int audioID = onAudioSelecting(spe);
		long vibration[] = onVibrationSelecting(spe);
		String msg = PhonePrompter.StartPhoneAlert(TAG, mContext, 
				audioID != PhonePrompter.CHIMES_NONE, audioID, vibration);
		long[] schedule = DataStorage.getPromptTimesKey(mContext, KEY_SCHEDULE);
		if (schedule != null && schedule.length >= 3) {
			spe.setPromptSchedule(lastScheduledPromptTime, (int) schedule[0], (int) schedule[1], schedule[2]);
		}
		if (msg.toLowerCase(Locale.US).contains("silence")) {
			spe.setPromptAudio(PROMPT_AUDIO.NONE);
		} else if (msg.toLowerCase(Locale.US).contains("normal")) {
			spe.setPromptAudio(PROMPT_AUDIO.AUDIO);
		} else if (msg.toLowerCase(Locale.US).contains("vibrate")) {
			spe.setPromptAudio(PROMPT_AUDIO.VIBRATION);
		}	
		spe.setRepromptCount(promptCount - 1); // prompt count starts from 1
		spe.setPromptTime(System.currentTimeMillis());
		
		// Construct intent and start survey activity
		Intent i = new Intent(mContext, mClsSurveyActivity);		
		i.putExtra(QuestionSet.TAG, new QuestionSetParamHandler(surveyClassName, new Object[] { spe }));
		i.putExtra(SurveyActivity.SURVEY_PROMPT_EVENT, spe);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(i);
		Log.i(TAG, "prompted survey");
		
		onSurveyPrompting(spe);
		
		return true;
	}
	
	protected void onSurveyPrompting(final SurveyPromptEvent promptEvent) {
		Log.i(TAG, "onSurveyPrompting");
	}
	
	protected void onPromptAdded() {
		Log.i(TAG,  "onPromptAdded");
	}
	
	protected void onPromptRemoved() {
		Log.i(TAG, "onPromptRemoved");
	}
	
	protected long addPromptToSchedule(String speJSON, long promptTime) {				
		long[] savedPromptTime = DataStorage.getPromptTimesKey(mContext, KEY_CS_PROMPT);
		
		if (speJSON == null) {
			throw new IllegalArgumentException("speJSON is null");			
		}
		
		if (savedPromptTime == null) {
			DataStorage.setPromptTimesKey(mContext, new long[] { promptTime }, KEY_CS_PROMPT);
			// Save the JSON form of survey prompt event 
	        DataStorage.SetValue(mContext, KEY_ALL_PROMPT_EVENT + promptTime, speJSON);
	        
	        reschedule();	        
	        onPromptAdded(); // Add application specific behavior
	        
			return promptTime;
		}
				
		long latestPromptTime = savedPromptTime[savedPromptTime.length - 1];		
		if (promptTime - latestPromptTime < Globals.MIN_MS_BETWEEN_SCHEDULED_PROMPTS) {
			promptTime = latestPromptTime + Globals.MINUTES_15_IN_MS;						
		}
		// Save the survey prompt event
		DataStorage.SetValue(mContext, KEY_ALL_PROMPT_EVENT + promptTime, speJSON);

		long[] finalPromptTime = new long[savedPromptTime.length + 1];
        System.arraycopy(savedPromptTime, 0, finalPromptTime, 0, savedPromptTime.length);

		finalPromptTime[savedPromptTime.length] = promptTime;
		DataStorage.setPromptTimesKey(mContext, finalPromptTime, KEY_CS_PROMPT);
		
		reschedule();		
		onPromptAdded(); // Add application specific behavior
		
		return promptTime;				
	}
	
	protected boolean removePromptFromSchedule(long promptTime) {
		long[] savedPromptTime = DataStorage.getPromptTimesKey(mContext, KEY_CS_PROMPT);				
        
		if (savedPromptTime == null) {
			Log.i(TAG, "No CS prompt to remove");
			return false;
		}
		
		boolean removed = false;
		ArrayList<Long> promptTimes = new ArrayList<Long>();
		for (long time : savedPromptTime) {
			if (time != promptTime) {
				promptTimes.add(time);
			} else {
				removed = true;
				DataStorage.SetValue(mContext, KEY_ALL_PROMPT_EVENT + promptTime, null);
			}
		}
		if (!removed) {
			Log.i(TAG, "Can't find this prompt time to remove");
			return false;
		}
				
		long[] finalPromptTime = promptTimes.size() > 0 ? new long[promptTimes.size()] : new long[] {};
		for (int i = 0; i < promptTimes.size(); ++i) {
			finalPromptTime[i] = promptTimes.get(i);
		}
		DataStorage.setPromptTimesKey(mContext, finalPromptTime, KEY_CS_PROMPT);
		reschedule();
		onPromptRemoved();
		
		return true;
	}

	protected void reschedule() {
		long[] csPromptTime = DataStorage.getPromptTimesKey(mContext, KEY_CS_PROMPT);
		if (csPromptTime == null) {
			return;
		}
		PriorityQueue<Long> promptTimeQueue = new PriorityQueue<Long>(csPromptTime.length);
		
		for (int i = 0; i < csPromptTime.length; i++) {
			promptTimeQueue.add(csPromptTime[i]);
		}
		
		long[] allPromptTime = promptTimeQueue.size() > 0 ? new long[promptTimeQueue.size()] : new long[] {};
		for (int i = 0; i < allPromptTime.length; ++i) {
			allPromptTime[i] = promptTimeQueue.poll();
		}
		
		DataStorage.setPromptTimesKey(mContext, allPromptTime, KEY_ALL_PROMPT);
	}
	
	protected void onScheduleReset() {
		Log.i(TAG, "onScheduleReset");
	}
	
	protected boolean isTimeForNextSurvey(long nextPromptTime) {
		long now = System.currentTimeMillis();
		long lastTimePrompted  = AppInfo.GetLastTimePrompted(mContext, Globals.SURVEY);
		long timeSincePrompted = now - lastTimePrompted;
		long timeSinceScheduledPrompt = now - nextPromptTime;
		
		return timeSinceScheduledPrompt < Globals.REPROMPT_TIMES * Globals.REPROMPT_DELAY_MS &&
			   timeSincePrompted > Globals.REPROMPT_DELAY_MS;
	}

	private SurveyPromptEvent getPendingSurvey() {
		SurveyPromptEvent spe = null;
		
		long now = System.currentTimeMillis();
		long lastTimePrompted  = AppInfo.GetLastTimePrompted(mContext, Globals.SURVEY);
		long lastTimeCompleted = AppInfo.GetLastTimeCompleted(mContext, Globals.SURVEY);		
		
		// Reset the time if user manipulates the phone's time 
		if (lastTimePrompted > now) {
			AppInfo.SetLastTimePrompted(mContext, Globals.SURVEY, now);
		}
		if (lastTimeCompleted > now) {
			AppInfo.SetLastTimeCompleted(mContext, Globals.SURVEY, now);
		}
		
		long timeSincePrompted  = now - lastTimePrompted;
		long timeSinceCompleted = now - lastTimeCompleted;
		long lastScheduledPromptTime  = getLastScheduledPromptTime(now);
		long timeSinceScheduledPrompt = now - lastScheduledPromptTime;
		Log.i(TAG, "LastScheduledPromptTime: " + DateHelper.getDate(lastScheduledPromptTime));

		// If no scheduled prompt, just return
		if (lastScheduledPromptTime == 0) {
			return null;
		}

		if (lastTimeCompleted == 0) {
			Log.i(TAG, "LastTimeCompleted: never");
		} else {
			Log.i(TAG, "LastTimeCompleted: " + DateHelper.getDate(lastTimeCompleted));
		}

		// If completed after the last scheduled prompt time, then just return
		if (lastTimeCompleted > lastScheduledPromptTime) {
			return null; // the task of the current time is finished
		}

		if (Globals.IS_DEBUG) {
			Log.d(TAG, "Time from scheduled prompt (min): " + (now - lastScheduledPromptTime) / Globals.MINUTES_1_IN_MS);
			Log.d(TAG, "Time since completed (min): " + timeSinceCompleted / Globals.MINUTES_1_IN_MS);
			Log.d(TAG, "Time since prompted (min): " + timeSincePrompted / Globals.MINUTES_1_IN_MS);
		}

		// Check if it is the time to prompt the next survey		
		if (isTimeForNextSurvey(lastScheduledPromptTime)) {
			spe = getEventByScheduledTime(lastScheduledPromptTime);
		}
		if (timeSinceScheduledPrompt < (Globals.REPROMPT_TIMES + 1) * Globals.REPROMPT_DELAY_MS + Globals.MINUTES_10_IN_MS &&
				timeSincePrompted > (Globals.REPROMPT_TIMES + 1) * Globals.REPROMPT_DELAY_MS) {
			NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(Globals.SURVEY_NOTIFICATION_ID);
		}
		
		return spe;
	}
	
	protected SurveyPromptEvent getEventByScheduledTime(long scheduledPromptTime) {
		String speJSON = DataStorage.GetValueString(mContext, KEY_ALL_PROMPT_EVENT + scheduledPromptTime, null);
		Log.i(TAG, "getEventByScheduledTime: " + scheduledPromptTime);
		
		if (speJSON == null) {
			Log.i(TAG, "speJSON == null");
			return null;
		}
		// Transform JSON string to object
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		SurveyPromptEvent spe = gson.fromJson(speJSON, SurveyPromptEvent.class);
		
		return spe;
	}
	
	protected final static int PROMPT_REQUEST_ACCEPTED     = 0;
	protected final static int PROMPT_REQUEST_NOT_ACCEPTED = 1;
	protected final static int PROMPT_REQUEST_COMPLETED    = 2;
	
	/**
	 * A good place to check whether the re-prompt should be accepted or not.
	 * 
	 * @param spe The survey prompt event representing this prompting
	 * @return PROMPT_REQUEST_ACCEPTED if the current prompt is accepted; PROMPT_REQUEST_NOT_ACCEPTED
	 * if the current prompt is rejected; PROMPT_REQUEST_COMPLETED if the prompting is completed.
	 * The prompt will only be triggered when the return value is PROMPT_REQUEST_ACCEPTED.
	 * 
	 */
	protected int requestPrompt(SurveyPromptEvent spe) {
		int promptCount = getPromptCount(spe.getID());
		
		if (promptCount == 1) { // first time the survey prompted
			if (SurveyActivity.isWorking(mClsSurveyActivity)) {
				// check whether the previous survey activity was reclaimed by the OS
				WeakReference<SurveyActivity> ref = SurveyActivity.getSelf(mClsSurveyActivity);
				if (ref.get() == null) {
					Log.i(TAG, "Always prompt the survey if the previous survey was reclaimed by the OS");
					return PROMPT_REQUEST_ACCEPTED;
				}
				Log.i(TAG, "The previous survey is still there to be completed");
				return PROMPT_REQUEST_NOT_ACCEPTED;
			}
		} else { // promptCount > 1
			if (SurveyActivity.isWorking(mClsSurveyActivity)) {
				// the weak reference object is still there but the actual activity might be reclaimed.
				WeakReference<SurveyActivity> ref = SurveyActivity.getSelf(mClsSurveyActivity);
				if (ref.get() == null) {
					Log.i(TAG, "Always prompt the survey if the previous survey was reclaimed by the OS");
					return PROMPT_REQUEST_ACCEPTED;
				} else if (!ref.get().isRepromptAccepted(promptCount)) {
					Log.i(TAG, "Reprompt request hasn't been accepted");
					return PROMPT_REQUEST_NOT_ACCEPTED;
				}
			} else {
				// If the activity is killed before the survey has been completed,
				// pop up the survey again if possible.
				int status = SurveyActivity.getSurveyCompleteStatus(spe.getID());
				if (status == SurveyActivity.SURVEY_COMPLETED) {
					return PROMPT_REQUEST_COMPLETED;
				} else if (status == SurveyActivity.SURVEY_DISMISSED) {
					return PROMPT_REQUEST_COMPLETED;
				} else if (status == SurveyActivity.SURVEY_LIFE_OVER) {
					return PROMPT_REQUEST_COMPLETED;
				} else if (status == SurveyActivity.SURVEY_NOT_COMPLETED) {					
					;					
				} else if (status == SurveyActivity.SURVEY_RECLAIMED) {
					;
				}
			}
		}	
		
		return PROMPT_REQUEST_ACCEPTED;
	}

	// Return 0 if none, otherwise return time
	protected long getLastScheduledPromptTime(long currentTime) {
		long[] promptTimes = DataStorage.getPromptTimesKey(mContext, KEY_ALL_PROMPT);
		
		if (promptTimes == null) {
			return 0;
		}

		long lastTime = 0;
		for (int i = 0; i < promptTimes.length && promptTimes[i] < currentTime; ++i) { 
			lastTime = promptTimes[i];
		}

		return lastTime;
	}

	protected boolean resetSchedule() {			
		// Clear the prompt history
		long[] somePromptTimes = DataStorage.getPromptTimesKey(mContext, KEY_ALL_PROMPT);
		if (somePromptTimes != null) {
			for (int i = 0; i < somePromptTimes.length; ++i) {				
				DataStorage.SetValue(mContext, KEY_ALL_PROMPT_EVENT + somePromptTimes[i], null);
			}
		}
		DataStorage.setPromptTimesKey(mContext, new long[] {}, KEY_ALL_PROMPT);
		clearPromptTimes();

		PackageChecker.installedPackageLogging(TAG, mContext);		
		AppInfo.resetAvailabilityAndTiming(mContext);
		DataStorage.SetValue(mContext, LAST_SCHEDULE_UPDATE_TIME, System.currentTimeMillis());
		
		onScheduleReset();
		
		return true;
	}	
	
	protected int getPromptCount(long speID) {		
		Integer promptCount = PromptTimesMap.getMap(mContext).get(speID);
		if (promptCount != null) {
			return promptCount;
		}
		return 1;
	}
	
	protected void setPromptCount(long speID, Integer promptCount) {
		HashMap<Long, Integer> map = PromptTimesMap.getMap(mContext);
		map.put(speID, promptCount);
		PromptTimesMap.setMap(mContext, map);
	}
	
	protected void clearPromptTimes() {
		PromptTimesMap.setMap(mContext, null);
	}
	
	protected static class PromptTimesMap {		
		private static final String KEY_PROMPT_TIMES_MAP = "KEY_PROMPT_TIMES_MAP";

		public static HashMap<Long, Integer> getMap(Context context) {
			String json = DataStorage.GetValueString(context, KEY_PROMPT_TIMES_MAP, null);
			LinkedHashMap<Long, Integer> map = null;
			
			if (json != null) {
				Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
				map = gson.fromJson(json, new TypeToken<LinkedHashMap<Long, Integer>>(){}.getType());				
			} else {
				map = new LinkedHashMap<Long, Integer>();
			}
			
			return map;
		}
		
		public static void setMap(Context context, HashMap<Long, Integer> map) {
			String json = null;
			
			if (map != null) {
				Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
				json = gson.toJson(map);
			}
			
			DataStorage.SetValue(context, KEY_PROMPT_TIMES_MAP, json);
		}
	}
}
