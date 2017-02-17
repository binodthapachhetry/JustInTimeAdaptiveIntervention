package edu.neu.android.wocketslib.emasurvey;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.PhonePrompter;
import edu.neu.android.wocketslib.utils.PhoneVibrator;

/**
 * This class is used to monitor the life of the survey.
 * If the question time or the whole time for the current survey is out, 
 * the monitor class will call onQuestionLifeExpired/onSurveyLifeExpired
 * methods of the SurveyActivity.
 * The monitor can also pops the survey back to foreground if the user has
 * set the global variable IS_POPPING_SURVEY_BACK_ENABLED to true and gives
 * a timing for popping survey back by setting TIMING_FOR_POPPING_SURVEY_BACK.
 * 
 * @author bigbug
 */
public class SurveyLifeMonitor extends Handler {
	private final static String TAG = "SurveyLifeMonitor";	
	
	protected int     mWhichQuestion;
	protected long    mSurveyWholeLife;
	protected boolean mIsStarted;
	protected HashMap<Integer, Long> mLifeMap;		
	
	protected SurveyActivity mActivity;
	
	public SurveyLifeMonitor(SurveyActivity activity) {		
		mActivity = activity;
		resetMonitor();
	}
	
	public void startMonitor(int whichQuestion) {
		Log.i(TAG, "start survey life monitor");
		mIsStarted = true;
		mSurveyWholeLife = mActivity.getSurveyLife();
		updateMonitor(whichQuestion);						
		postDelayed(mDelayMonitor, MONITOR_INTERVAL);
	}
	
	public void stopMonitor() {
		removeCallbacksAndMessages(null);
		mIsStarted = false;
		mLifeMap.clear();		
		Log.i(TAG, "stop survey life monitor");
	}		
	
	public void updateMonitor(int whichQuestion) {
		mWhichQuestion = whichQuestion;
		mLifeMap.put(mWhichQuestion, mActivity.getQuestionLife(mWhichQuestion));
		Log.i(TAG, "update life for Q" + mWhichQuestion + " to " + mActivity.getQuestionLife(mWhichQuestion));
	}
	
	@SuppressLint("UseSparseArrays")
	public void resetMonitor() {
		Log.i(TAG, "reset survey life monitor");
		mIsStarted       = false;
		mWhichQuestion   = 1;		
		mLifeMap = new HashMap<Integer, Long>();
		removeCallbacksAndMessages(null);
	}
	
	public boolean isStarted() {
		return mIsStarted;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
	}
	
	protected void popSurveyBack() {		
		if (mWhichQuestion <= 1) {
			return; // The default implementation does not pop the first question back.
		}
		
		Context context = mActivity.getApplicationContext();		
		Intent i = mActivity.getIntent();
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(SurveyActivity.FORCE_POP_BACK, true);
		i.putExtra(SurveyActivity.FORCE_UPDATE_QUESTION_LIFE, false);
		context.startActivity(i);
		
		PhonePrompter.StartPhoneAlert(TAG, mActivity, true, PhonePrompter.CHIMES_YOKOKUO2, PhoneVibrator.VIBRATE_INTENSE);
	}
	
	public final static long MONITOR_INTERVAL = 1000; // one second
	
	protected Runnable mDelayMonitor = new Runnable() {

		@Override
		public void run() {			
			Long questionLife = mLifeMap.get(mWhichQuestion);
			
			if (questionLife == null) {
				Log.i(TAG, "questionLife == null");
				return;
			}

			// Update all life time
			questionLife = Math.max(questionLife - MONITOR_INTERVAL, 0); 
			mLifeMap.put(mWhichQuestion, questionLife);
			mSurveyWholeLife = Math.max(mSurveyWholeLife - MONITOR_INTERVAL, 0);	
			
			if (Globals.IS_DEBUG) {
				Log.i(TAG, "question life: " + questionLife);
				Log.i(TAG, "whole life: " + mSurveyWholeLife);
			}
			
			// If the user pressed home button, the prompt would be in the back stack.
			// Play the warning sound and pop the question back to the foreground again.	
			if (Globals.IS_POPPING_SURVEY_BACK_ENABLED && questionLife == Globals.TIMING_FOR_POPPING_SURVEY_BACK) {
				popSurveyBack();				
			}
			
			// Life is expired or check it some time later
			if (questionLife <= 0) {
				mActivity.onQuestionLifeExpired(mWhichQuestion);		
			}
			if (mSurveyWholeLife <= 0) {
				mActivity.onSurveyLifeExpired();	
			}						
			
			postDelayed(this, MONITOR_INTERVAL);
		}
	};
}
