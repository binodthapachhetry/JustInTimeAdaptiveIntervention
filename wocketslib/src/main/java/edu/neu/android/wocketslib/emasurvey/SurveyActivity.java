package edu.neu.android.wocketslib.emasurvey;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.emasurvey.model.ChoiceAdapter;
import edu.neu.android.wocketslib.emasurvey.model.FreeFormNumAns;
import edu.neu.android.wocketslib.emasurvey.model.NumberPickerSpinner;
import edu.neu.android.wocketslib.emasurvey.model.NumberPickerSpinnerAdapter;
import edu.neu.android.wocketslib.emasurvey.model.PromptEventSender;
import edu.neu.android.wocketslib.emasurvey.model.PromptRecorder;
import edu.neu.android.wocketslib.emasurvey.model.PromptRecorder.SURVEY_LOG_TYPE;
import edu.neu.android.wocketslib.emasurvey.model.QuestionComparator;
import edu.neu.android.wocketslib.emasurvey.model.QuestionQueueHandler;
import edu.neu.android.wocketslib.emasurvey.model.QuestionSet;
import edu.neu.android.wocketslib.emasurvey.model.QuestionSetParamHandler;
import edu.neu.android.wocketslib.emasurvey.model.SurveyAnswer;
import edu.neu.android.wocketslib.emasurvey.model.SurveyPromptEvent;
import edu.neu.android.wocketslib.emasurvey.model.SurveyQuestion;
import edu.neu.android.wocketslib.emasurvey.model.SurveyQuestion.TYPE;
import edu.neu.android.wocketslib.emasurvey.rule.ChanceBeChosen;
import edu.neu.android.wocketslib.emasurvey.rule.Rule;
import edu.neu.android.wocketslib.emasurvey.rule.Rule.RULE_TYPE;
import edu.neu.android.wocketslib.numberpicker.NumberPicker;
import edu.neu.android.wocketslib.numberpicker.NumberPicker.OnChangedListener;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

/**
 * Survey activity should not be finished unless the following cases happen:
 * 1. user clicks back button when the first question is being presented
 * 2. the first question is timed out twice
 * 3. some other question rather than the first one is timed out
 * 4. user clicks done button to complete the survey
 * 
 * @author Yifei, bigbug
 */
public class SurveyActivity extends BaseActivity {
	public static String TAG = "SurveyActivity";
	public static final String SURVEY_PROMPT_EVENT = "SURVEY_PROMPT_EVENT";	
	public static final String FORCE_POP_BACK = "FORCE_POP_BACK";
	public static final String FORCE_UPDATE_QUESTION_LIFE = "FORCE_UPDATE_QUESTION_LIFE";
	
	public static int MINUTES_FOR_FIRST_QUESTION = 5;
	public static int MINUTES_FOR_OTHER_QUESTION = 2;
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
	protected TextView  mQuestTextView;
	protected Button 	mBackButton;
	protected Button 	mNextButton;
	
	protected SurveyQuestion  mMainQuestion;
	protected SurveyQuestion  mStartQuestion;
	protected SurveyQuestion  mCurrentQuestion;	
	protected ArrayList<SurveyQuestion> mOriginQuestions;
	protected ArrayList<SurveyQuestion> mPoppedQuestions;
	
	protected QuestionQueueHandler mQuestQueueHandler;
	
	protected ListView 	  mListView;
	protected ChoiceAdapter mListAdapter;	
	
	protected LinearLayout  mFreeFormChoice;
	protected LinearLayout  mTimePicker;
	protected Button	  mDatePicker;
//	private TimePicker    mDatePicker;
	protected EditText	    mHourInput;
	protected EditText      mMinuteInput;
	protected NumberPicker  mHourPicker;
	protected TextView      mHourTextView;
	protected NumberPicker  mMinutePicker;
	
	
	protected Date surveyStartedTime;  //Time User completed first Question to track Start time not Prompt time.
	protected boolean surveyStartTimeSet; //Set when user interacts for the first time only.
	
	private final int MINUTE_INTERVAL = 5;
	protected QuestionSet       mQuestionSet;
	protected SurveyPromptEvent mPromptEvent;
	protected PromptEventSender mPromptSender;
		
	protected int mQuestIndex;
	protected int mTotalQuestions;
	protected int mCompletedQuestions;
	
	protected boolean mIsLastQuestion;
	protected boolean mIsCompleted;
	protected boolean mIsDismissed;
	
	private IntentFilter mIntentFilter;
		
	protected Context mContext;
	protected SurveyLifeMonitor mLifeMonitor;
	
	// For the description of each value, please see getSurveyCompleteStatus
	public static final int SURVEY_NOT_COMPLETED = 0;
	public static final int SURVEY_COMPLETED	 = 1;
	public static final int SURVEY_DISMISSED     = 2;
	public static final int SURVEY_LIFE_OVER     = 3;
	public static final int SURVEY_RECLAIMED     = 4;
	
	private static HashMap<Class<?>, WeakReference<SurveyActivity>> sSurveysMap = new HashMap<Class<?>, WeakReference<SurveyActivity>>();

	/**
	 * <p>
	 * This method tells whether there is at least one survey activity active in the system.
	 * </p>
	 * 
	 * @return true if there is at least one survey activity is working.
	 */
	public static boolean isWorking() {
		boolean isWorking = false;
		Iterator iter = sSurveysMap.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter.next(); 		   
		    Object value = entry.getValue();
		    if (value != null) {		    	
		    	isWorking = true;
		    	break;
		    }
		} 		
		return isWorking;
	}
	
	/**
	 * <p>
	 * This method stops all survey activities currently active in the system.
	 * </p>	 
	 */
	public static void stopWorking() {		
		Iterator iter = sSurveysMap.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter.next(); 		   
		    Object value = entry.getValue();
		    if (value != null) {
		    	WeakReference<SurveyActivity> ref = (WeakReference<SurveyActivity>) value;
		    	if (ref.get() != null) {
		    		ref.get().finish();
		    	}
		    }
		} 	
		sSurveysMap.clear();
	}
	
	/**
	 * <p>
	 * This method tells whether the survey specified by the clazz paremeter is still working.
	 * </p>
	 * 
	 * @param clazz
	 *            is the Class object of a specified survey activity that user defined.
	 * @return true if the survey activity is not finished, otherwise false.
	 */
	public static boolean isWorking(Class<?> clazz) {
		return getSelf(clazz) != null;
	}
	
	/**
	 * <p>
	 * Try to finish the survey activity specified by the input Class object if it's
	 * still working.
	 * </p>
	 * 
	 * @param clazz
	 *            is the Class object of a specified survey activity that user defined. 
	 */
	public static void stopWorking(Class<?> clazz) {		
		WeakReference<SurveyActivity> self = getSelf(clazz);
		if (self != null && self.get() != null) {
			self.get().finish();
		}
		sSurveysMap.clear();
	}
	
	/**
	 * <p>
	 * Get the survey activity object specified by the input Class object.
	 * </p>
	 * 
	 * @param clazz
	 *            is the Class object of a specified survey activity that user defined. 
	 */
	public static WeakReference<SurveyActivity> getSelf(Class<?> clazz) {		
		return sSurveysMap.get(clazz);
	}
	
	/**
	 * Return the survey complete status.
	 * The returned status can be: SURVEY_COMPLETED, SURVEY_DISMISSED, SURVEY_NOT_COMPLETED.
	 * 
	 * SURVEY_COMPLETED: All questions of the survey are completed.
	 * SURVEY_DISMISSED: User quits the survey on the first question by clicking back button on the device.
	 * SURVEY_NOT_COMPLETED: User has completed some of the questions but not all of them.
	 * SURVEY_LIFE_OVER: The survey is killed because it's life is over.
	 * SURVEY_RECLAIMED: System killed the survey because of the lack of resources.
	 * 
	 * @param surveyID The id to identifies the survey.
	 * @return The value described above.
	 */
	public static int getSurveyCompleteStatus(long surveyID) {
		return (int) DataStorage.GetValueLong(ApplicationManager.getAppContext(), Long.toString(surveyID), SURVEY_NOT_COMPLETED);
	}
	
	public static void setSurveyCompleteStatus(long surveyID, int status) {
		DataStorage.SetValue(ApplicationManager.getAppContext(), Long.toString(surveyID), status);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState, String TAG) {
        super.onCreate(savedInstanceState, TAG);
        this.TAG = TAG;
		Log.d(TAG, "onCreate");
		setContentView(R.layout.usc_survey_activity);
		
		sSurveysMap.put(getClass(), new WeakReference<SurveyActivity>(this)); // register the current survey
		
		surveyStartTimeSet = false;
		
		// Initialize all stuff
		initViews();	
		initExtra();		
		if (!initFromIntent()) {
			finish();
            return;
		}
		
		// Log the last prompt time
		AppInfo.SetLastTimePrompted(mContext, Globals.SURVEY, System.currentTimeMillis());

		// Write records to file		
		PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.START_QUESTION, mPromptEvent.getScheduledPromptTime(),
			System.currentTimeMillis(), mPromptEvent.getSurveyName(), mPromptEvent.getPromptType()
		);		
		// Write info to server
		String extraInfo = mPromptEvent.getPromptAudio() != null ? " [" + mPromptEvent.getPromptAudio() + "]" : "";
		mPromptSender.addPromptEvent(
			"Prompted " + mPromptEvent.getSurveyName(),
			new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType() + extraInfo, new Date()
		);					
				
		// set the initial status
		setSurveyCompleteStatus(mPromptEvent.getID(), SURVEY_NOT_COMPLETED);
		
		prepairQuestion();		
		
		// Start survey delay time monitor
		mLifeMonitor.startMonitor(mQuestIndex);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();						

		// Add prompt log to server		
		mPromptSender.addPromptEvent(
			"Presented Q" + mQuestIndex + ": " + mCurrentQuestion.getQuestionText(),
			new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType(), new Date()
		);
		
		// Register broadcast receiver		
		this.registerReceiver(choiceUpdate, mIntentFilter);
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();				

		// Unregister broadcast receiver
		unregisterReceiver(choiceUpdate);
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onDestroy() {		
		Log.d(TAG, "onDestory");
		
		// Stop survey delay time monitor
		mLifeMonitor.stopMonitor();
						
		if (mPromptEvent != null) {
			// Write all the local logs
			
			//New CSV Structures for  "prompts.csv and promptresponses_....csv" for MATCH. Other Survey-Based applications may feel free to Adopt or advance this structure.
			if (Globals.STUDY_NAME.equals("MATCH")||Globals.STUDY_NAME.equals("UEMATEST")) {

			PromptRecorder.writePromptLog(getApplicationContext(), surveyStartedTime ,mPromptEvent, isDismissed(), isResponded(), isCompleted(),!isResponded(), Globals.STUDY_NAME, "");
			PromptRecorder.writePromptResponses(mContext, mPromptEvent, mQuestionSet.getReadableQuestionSetName(), 
					isDismissed(), isResponded(), isCompleted(), mQuestionSet.getAllQuestionIDs(), getQuestionaire(), Globals.STUDY_NAME
				);

				if (isCompleted()) {
					DataStorage.SetValue(mContext, mPromptEvent.getPromptType()
							+ "COMPLETED", true);
				}
				
			//Old CSV Structures for other applications.
			}else{
				
				PromptRecorder.writePromptLog(mPromptEvent, isDismissed(), isResponded(), isCompleted());
			PromptRecorder.writePromptResponses(mContext, mPromptEvent, mQuestionSet.getReadableQuestionSetName(), 
					isDismissed(), isResponded(), isCompleted(), mQuestionSet.getAllQuestionIDs(), getQuestionaire()
				);
			}
			PromptRecorder.writeDetailedLog(
					mContext, SURVEY_LOG_TYPE.END_QUESTION, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis()
			);			
			
			
			
			// Write logs to the server
			if (isCompleted()) {
				onSurveyCompleted();
				mPromptSender.addPromptEvent( 
					"Completed " + mPromptEvent.getSurveyName(),  
					new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType(), new Date()				
				);
				// the map should be updated in case that the system killed the activity
				setSurveyCompleteStatus(mPromptEvent.getID(), SURVEY_COMPLETED);
			} else {
				onSurveyNotCompleted();	
				if(Globals.STUDY_NAME.equals("MATCH")||Globals.STUDY_NAME.equals("UEMATEST")){
				//DONT SEND PROMPT.
			}else {
				mPromptSender.addPromptEvent(
						"Ended " + mPromptEvent.getSurveyName(),
						new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType(), new Date()
					);
			}
				
				// update the survey complete status
				// the survey can be killed by system for resources or killed by the survey life monitor
				// if it's life is over or killed by the users if they press the back button on the first question.
				if (isDismissed()) {
					// user decided to quit the survey on the first question
					setSurveyCompleteStatus(mPromptEvent.getID(), SURVEY_DISMISSED);
				} else {
					// check whether it's killed by the system
					// before the survey life monitor killing the survey, it default update the status to 
					// SURVEY_LIFE_OVER, so just check whether this status is set
					int status = getSurveyCompleteStatus(mPromptEvent.getID());
					if (status != SURVEY_LIFE_OVER) {
						// killed by system
						setSurveyCompleteStatus(mPromptEvent.getID(), SURVEY_RECLAIMED);
					}
				}
			}			
			mPromptSender.send();
			mPromptSender.reset();						
		}
				
		sSurveysMap.put(getClass(), null); // unregister this survey activity
        Globals.IS_DEMO = false;
        super.onDestroy();
	}
	
	@Override
	public void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}
	
	@Override
	public void onNewIntent(Intent intent) {   
		Log.d(TAG, "onNewIntent");
	    super.onNewIntent(intent);		    	    
	    if (intent.getBooleanExtra(FORCE_POP_BACK, false)) {
	    	if (intent.getBooleanExtra(FORCE_UPDATE_QUESTION_LIFE, false)) {
	    		mLifeMonitor.updateMonitor(mQuestIndex);
	    	}
	    	return; // It's the monitor that pop back the question at 90 seconds, not a reprompt
	    } 
	    setIntent(intent);	 
	    
	    // Write the last prompt log before updating the survey prompt event
	    if (Globals.STUDY_NAME.equals("MATCH")||Globals.STUDY_NAME.equals("UEMATEST")) {
			//PromptRecorder.writePromptLog(mPromptEvent, isDismissed(), isResponded(), isCompleted(), Globals.STUDY_NAME);
	    }else{
				PromptRecorder.writePromptLog(mPromptEvent, isDismissed(), isResponded(), isCompleted());
				PromptRecorder.writePromptResponses(mContext, mPromptEvent, mQuestionSet.getReadableQuestionSetName(), 
						isDismissed(), isResponded(), isCompleted(), mQuestionSet.getAllQuestionIDs(), getQuestionaire()
					);
	    }
			
	 		
	    // Get the prompt event from intent
		if (updatePromptEvent()) {
			// Update question time to its default value
	 		mLifeMonitor.updateMonitor(mQuestIndex);
			// Start question	 		
			PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.REPROMPT,
				mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(), mPromptEvent.getSurveyName()
			);			
			// Log the last prompt time
			AppInfo.SetLastTimePrompted(mContext, Globals.SURVEY, System.currentTimeMillis());
			
			// Write info to server
			String extraInfo = mPromptEvent.getPromptAudio() != null ? " [" + mPromptEvent.getPromptAudio() + "]" : "";
			mPromptSender.addPromptEvent(
				"Reprompted " + mPromptEvent.getSurveyName(),
				new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType() + extraInfo, new Date()
			);						
		} else {
			finish();
		}
	}
	
	protected void onSurveyCompleted() {}
	
	protected void onSurveyNotCompleted() {}
	
	public long getSurveyLife() {
		return Globals.MIN_MS_BETWEEN_SCHEDULED_PROMPTS;
	}
	
	public long getQuestionLife(int whichQuestion) {
		long questionLife = SurveyActivity.MINUTES_FOR_OTHER_QUESTION * Globals.MINUTES_1_IN_MS;
		if (whichQuestion == 1) {
			questionLife = SurveyActivity.MINUTES_FOR_FIRST_QUESTION * Globals.MINUTES_1_IN_MS;
		}
		return questionLife;
	}
	
	public boolean isRepromptAccepted(long promptCount) {
		return mQuestIndex == 1 && promptCount == 2;
	}
	
	/**
	 * <p>
	 * If user has set the survey maximum life for each question and the question life
	 * is expired as the survey exists, this method will be called from the survey 
	 * life monitor and user can decide the behavior by overriding this method.
	 * </p>
	 * 
	 * @param whichQuestion
	 *            is to indicate which question it is. The value starts from 1.
	 */
	public void onQuestionLifeExpired(int whichQuestion) {
		Log.d(TAG, "life for question " + whichQuestion + " was expired");
	}
	
	/**
	 * <p>
	 * If user has set the survey maximum life and that life is expired as the 
	 * survey exists, this method will be called from the survey life monitor
	 * and user can decide the behavior by overriding this method.
	 * </p>
	 */
	public void onSurveyLifeExpired() {
		setSurveyCompleteStatus(mPromptEvent.getID(), SURVEY_LIFE_OVER);
		finish();
		Toast.makeText(mContext, "Survey timed out!", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Survey life expired");
	}
	
	private void initViews() {
		mQuestTextView  = (TextView) findViewById(R.id.surveyQuestions);
		mBackButton     = (Button) findViewById(R.id.previousQuestion);
		mNextButton     = (Button) findViewById(R.id.nextQuestion);
		mListView       = (ListView) findViewById(R.id.lstChoices);
		mFreeFormChoice = (LinearLayout) findViewById(R.id.freeform);
		mTimePicker     = (LinearLayout) findViewById(R.id.timepicker);
		mHourTextView   = (TextView) findViewById(R.id.hourtext);
		
		mDatePicker = (Button) findViewById(R.id.datepicker);
		mDatePicker.setText("Select time");
		mDatePicker.setTextSize(22);
		mDatePicker.setOnClickListener(mDatePickerClickListener);
		
		mMinutePicker = (NumberPicker) findViewById(R.id.minutes);
		mMinutePicker.setRange(0, 11, new String[] { "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" });
		mMinutePicker.setTextFocusable(false);
		mMinutePicker.setOnChangeListener(mTimePickerChangeListener);
		
		mHourPicker = (NumberPicker) findViewById(R.id.hours);
		mHourPicker.setRange(0, 23);
		mHourPicker.setTextFocusable(false);
		mHourPicker.setOnChangeListener(mTimePickerChangeListener);
	}
	
	private void initExtra() {
		mQuestIndex         = 1;
		mMainQuestion       = null;				
		mCurrentQuestion    = null;
		mTotalQuestions     = 0;
		mCompletedQuestions = 0;			
		mIsCompleted        = false;
		mIsDismissed        = false;
		mIsLastQuestion     = false;	
		
		mContext = getApplicationContext();
		mPromptSender = new PromptEventSender(mContext);
		
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(TAG + ChoiceAdapter.UPDATE_ANSWER_SELECTED);
		mIntentFilter.addAction(TAG + ChoiceAdapter.UPDATE_ANSWER_CANCELLED);
		
		mLifeMonitor = createSurveyLifeMonitor();
	}

	private boolean initFromIntent() {
		Intent intent = getIntent();
		QuestionSetParamHandler param = (QuestionSetParamHandler) intent.getSerializableExtra(QuestionSet.TAG);
		
		// Create the question set
		try {
			Constructor[] constructors = Class.forName(param.getQuestionSetClassName()).getDeclaredConstructors();
			mQuestionSet = (QuestionSet) constructors[0].newInstance(param);			
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Error ClassNotFoundException in SurveyActivity: " + e.toString());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Error IllegalArgumentException in SurveyActivity: " + e.toString());
			e.printStackTrace();
		} catch (InstantiationException e) {
			Log.e(TAG, "Error InstantiationException in SurveyActivity: " + e.toString());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Log.e(TAG, "Error IllegalAccessException in SurveyActivity: " + e.toString());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Log.e(TAG, "Error InvocationTargetException in SurveyActivity: " + e.toString());
			e.printStackTrace();
		} finally {
			if (mQuestionSet == null) {
				Toast.makeText(mContext, "Error: No survey data exists.", Toast.LENGTH_SHORT).show();
                Log.e("STEVETEST", Log.getStackTrace("ERROR: ")); // TODO remove
				return false;
			}
		}
		
		// Get survey prompt event
		mPromptEvent = (SurveyPromptEvent) getIntent().getSerializableExtra(SURVEY_PROMPT_EVENT);
		if (mPromptEvent == null) {
			Toast.makeText(mContext, "Error: No survey event exists.", Toast.LENGTH_SHORT).show();
            Log.e("STEVETEST", Log.getStackTrace("ERROR: ")); //TODO remove
			return false;
		}					
		String reason = mPromptEvent.getPromptReason();
		mPromptEvent.setSurveyName(mQuestionSet.getReadableQuestionSetName() + (reason.equals("") ? "" : "(" + reason + ")"));		
		
		// The first prompt for the current question set
		// Get questions
		mOriginQuestions = new ArrayList<SurveyQuestion>(mQuestionSet.getQuestionNum() + 10);
		mOriginQuestions.addAll(mQuestionSet.getDefaultQuestionSet());
		mPoppedQuestions = new ArrayList<SurveyQuestion>();
		// Initialize question queue
		mQuestQueueHandler = new QuestionQueueHandler(10, new QuestionComparator());
		mQuestQueueHandler.setStartTime(mPromptEvent.getScheduledPromptTime());
		mQuestQueueHandler.quesEnQueue(
			QuestionSet.getQuestionByID(mOriginQuestions, QuestionSet.findFirstQuestionID(mOriginQuestions))
		);
		
		return true;
	}
	
	protected SurveyLifeMonitor createSurveyLifeMonitor()
	{
		return new SurveyLifeMonitor(this);
	}
	
	private boolean updatePromptEvent() {
		mPromptEvent = (SurveyPromptEvent) getIntent().getSerializableExtra(SURVEY_PROMPT_EVENT);
		
		if (mPromptEvent == null) {
			Toast.makeText(mContext, "Error: No survey event exists.", Toast.LENGTH_SHORT).show();
            Log.e("STEVETEST", Log.getStackTrace("ERROR: ")); //TODO remove
            return false;
		}	
		String reason = mPromptEvent.getPromptReason();
		mPromptEvent.setSurveyName(mQuestionSet.getReadableQuestionSetName() + (reason.equals("") ? "" : "(" + reason + ")"));	
		
		return true;
	}

	private void prepairQuestion() {		
		// Update the first question
		isProbNextQuestion();
		mStartQuestion = mQuestQueueHandler.peek();				
		updateQuestion();				
	}

	private void updateQuestion() {
		initQuestion();
		setupQuestion();
		updateViews();
	}

	private void initQuestion() {
		if (mQuestQueueHandler.isEmpty()) {
			//Toast.makeText(SurveyActivity.this, "Question stack is empty, close program.", Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		mTotalQuestions++;
		SurveyQuestion nextQuestion = mQuestQueueHandler.poll();
		mQuestQueueHandler.addQuesInGroup(nextQuestion);
		if (nextQuestion.isMainActivity()) {
			mMainQuestion = nextQuestion;
		}
		if (mCurrentQuestion != null) {
			mCurrentQuestion.setNextQuestionID(nextQuestion.getQuestionId());
			nextQuestion.setPrevQuestionID(mCurrentQuestion.getQuestionId());
		}
		mCurrentQuestion = nextQuestion;
		addGeneratedQuestion(mCurrentQuestion);
        if (mCurrentQuestion.getQuestionTYPE() != TYPE.MESSAGE) {
            for (int i = 0; i < mCurrentQuestion.getAnswers().length; i++) {
                if (mCurrentQuestion.getAnswers()[i] != null &&
                    mCurrentQuestion.getAnswers()[i].isSelected()) {
                    mQuestQueueHandler
                            .addFollowQuesFromAns(mContext, mOriginQuestions, mMainQuestion, mCurrentQuestion,
                                                  i);
                }
            }
            PromptRecorder.writeDetailedLog(mContext,
		                                    SURVEY_LOG_TYPE.QUESTION,
                                            mPromptEvent.getScheduledPromptTime(),
                                            System.currentTimeMillis(),
                                            "QuesIndex: " + mQuestIndex,
                                            mCurrentQuestion.getQuestionId(),
                                            mCurrentQuestion.getQuestionText());
        }
    }

	private void setupQuestion() {
		mQuestTextView.setText(mCurrentQuestion.getQuestionText());
		// add sequential question of current question to question queue
		mQuestQueueHandler.addFollowQuesAsSeq(mCurrentQuestion);

		switch (mCurrentQuestion.getQuestionTYPE()) {
		case SINGLE_CHOICE:
			setSingleChoiceLayout(SurveyActivity.this, mCurrentQuestion);
			break;
		case MULTI_CHOICE:
			setMultiChoiceLayout(SurveyActivity.this, mCurrentQuestion);
			break;
		case TIME_PICKER:
			setTimePicker(SurveyActivity.this, mCurrentQuestion);
			break;
		case MINUTES_PICKER:
			setMinutePicker(SurveyActivity.this, mCurrentQuestion);
			break;
		case FREE_FORM_TEXT:
			setEditText(SurveyActivity.this, mCurrentQuestion);
			break;
		case NUMBER_RANGE_SELECTER:
			setNumberPickerSpinner(SurveyActivity.this, mCurrentQuestion);
			break;
        case MESSAGE:
            setMessage();
            break;
		}
	}

	protected boolean isCompleted() {
		return mIsCompleted;
	}
	
	protected boolean isResponded() {
		return mTotalQuestions > 1;
	}
	
	protected boolean isDismissed() {
		return mIsDismissed;
	}

	private void backToPrevQuestion() {
		if (mCurrentQuestion == mStartQuestion) {
			mIsDismissed = true;
			finish();			
			return;
		}
		
		PromptRecorder.writeDetailedLog(mContext,
			SURVEY_LOG_TYPE.BACKPRESSED, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(), 
			"QuesIndex: " + mQuestIndex, mCurrentQuestion.getQuestionId(), mCurrentQuestion.getQuestionText()
		);
		mQuestQueueHandler.clearFollowQuesAsSeq(mCurrentQuestion);
		mQuestQueueHandler.clearFollowQuesFromAns(mCurrentQuestion);
		mQuestQueueHandler.clearFollowQuesAsGroup(mCurrentQuestion);

		if (mCurrentQuestion.getQuestionTYPE() == TYPE.FREE_FORM_TEXT) {
			for (int i = 0; i < mCurrentQuestion.getAnswers().length; i++) {
				View ansView = mFreeFormChoice.getChildAt(i);
				EditText ans = (EditText) ansView.findViewById(R.id.freeformanswer);
				if (ans.getText().length() > 0) {
					if (mCurrentQuestion.getAnswers()[i] == null)
						mCurrentQuestion.getAnswers()[i] = new SurveyAnswer(i, null);
					mCurrentQuestion.getAnswers()[i].setText(ans.getText().toString());
					mCurrentQuestion.getAnswers()[i].setSelected(true);
				} else {
					if (mCurrentQuestion.getAnswers()[i] != null) {
						mCurrentQuestion.getAnswers()[i].setText(null);
						mCurrentQuestion.getAnswers()[i].setSelected(false);
					}
				}
			}
		} else if (mCurrentQuestion.getQuestionTYPE() == TYPE.NUMBER_RANGE_SELECTER) {
			for (int i = 0; i < mCurrentQuestion.getAnswers().length; i++) {
				FreeFormNumAns currentAns = (FreeFormNumAns) mCurrentQuestion.getAnswers()[i];
				View ansView = mFreeFormChoice.getChildAt(i);
				NumberPickerSpinner numberPicker = (NumberPickerSpinner) ansView.findViewById(R.id.freeFormSpinner);
				int position = numberPicker.getSelectedItemPosition();
				int[] range = currentAns.getAnsRange();

				if (position != -1) {
					currentAns.setText((position + range[0]) + "");
					currentAns.setSelected(true);
				} else {
					currentAns.setText(null);
					currentAns.setSelected(false);
				}
			}
		}				
		
		mQuestQueueHandler.add(mCurrentQuestion);
		mCurrentQuestion = QuestionSet.getQuestionByID(mOriginQuestions, mCurrentQuestion.getPrevQuestionID());
		mQuestQueueHandler.clearFollowQuesAsSeq(mCurrentQuestion);
		mQuestQueueHandler.clearFollowQuesFromAns(mCurrentQuestion);
		mQuestQueueHandler.clearFollowQuesAsGroup(mCurrentQuestion);
		mQuestQueueHandler.add(mCurrentQuestion);
		mCurrentQuestion = null;
		--mQuestIndex;
				
		updateQuestion();
		
		mPromptSender.addPromptEvent( 
			"Back to: Q" + mQuestIndex + " " + mCurrentQuestion.getQuestionText(),
			new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType(), new Date()
		);
		
		// update the question to monitor
		mLifeMonitor.updateMonitor(mQuestIndex);
	}

	private void updateViews() {
		if (mCurrentQuestion == mStartQuestion) {
			// In the beginning, hide the back button
			mBackButton.setVisibility(View.GONE);
		} else {
			mBackButton.setVisibility(View.VISIBLE);
			mBackButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackButtonClicked();
					backToPrevQuestion();
					mTotalQuestions -= 2;
					--mCompletedQuestions;
				}
			});
		}

		// check the probability of next question first
		mIsLastQuestion = isProbNextQuestion();

		//Once any gesture is made in the survey set the time as 
		//time started and set to true to avoid any further changes to the start time.
		if((mCurrentQuestion != mStartQuestion) || mIsLastQuestion) {
			if (!surveyStartTimeSet) {
			
			surveyStartedTime = new Date();
			Log.i(TAG, "SURVEY STARTED AT : " + surveyStartedTime);
			surveyStartTimeSet = true;
			} 
		}

		mNextButton.setText(mIsLastQuestion ? getString(R.string.done_button) : getString(R.string.next_button));
		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsLastQuestion) {
					mIsCompleted = true;
				}
				onNextButtonClicked();
				checkSkipQuestion(SurveyActivity.this, mCurrentQuestion, mIsLastQuestion);
			}
		});
	}
	
	protected void onBackButtonClicked() {
		DataStorage.SetValue(ApplicationManager.getAppContext(), Globals.KEY_LAST_SURVEY_INTERACTION_TIME, new Date().getTime());
	}
	
	protected void onNextButtonClicked() {
		DataStorage.SetValue(ApplicationManager.getAppContext(), Globals.KEY_LAST_SURVEY_INTERACTION_TIME, new Date().getTime());
	}

	private boolean isProbNextQuestion() {
		if (mQuestQueueHandler.isEmpty()) {
			return true;
		}
		
		SurveyQuestion nextQuestion = mQuestQueueHandler.peek();
		if (!nextQuestion.isSeedSet()) {
			nextQuestion.setRandomSeed();
		}
		ArrayList<Rule> chance = nextQuestion.getRuleByType(RULE_TYPE.CHANCE_CHOSEN);
		if (chance.size() > 0 && ((ChanceBeChosen) chance.get(0)).isChosen(nextQuestion.getRandomProb())) {
			mQuestQueueHandler.poll();
			mQuestQueueHandler.addQuesInGroup(nextQuestion);
			PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.DELIBERATION, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(), "SKIP", nextQuestion.getQuestionId(),
					"Question", "RandomProb: " + nextQuestion.getRandomProb(), "ThresholdProb: " + ((ChanceBeChosen) chance.get(0)).getProbability());
			return isProbNextQuestion();
		}
		PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.DELIBERATION, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(), "USE", nextQuestion.getQuestionId(), "Question",
				"RandomProb: " + nextQuestion.getRandomProb(), "ThresholdProb: " + (chance.size() > 0 ? ((ChanceBeChosen) chance.get(0)).getProbability() : "None"));
		
		return false;
	}
	

	private void checkSkipQuestion(Context c, final SurveyQuestion currentQuestion, final boolean isCompleted) {
		SurveyAnswer[] answers = currentQuestion.getAnswers();
		boolean isSkip = true;
		
		if (currentQuestion.getQuestionTYPE() == TYPE.MINUTES_PICKER || currentQuestion.getQuestionTYPE() == TYPE.TIME_PICKER) {
			if (currentQuestion.getAnswers()[0] != null) {
				currentQuestion.getAnswers()[0].setSelected(true);
				isSkip = false;
			}
		} else
            if (currentQuestion.getQuestionTYPE() == TYPE.FREE_FORM_TEXT) {
                boolean isAnswered = false;
                for (int i = 0; i < currentQuestion.getAnswers().length; i++) {
                    View ansView = mFreeFormChoice.getChildAt(i);
                    EditText ans = (EditText) ansView.findViewById(R.id.freeformanswer);
                    if (ans.getText().length() > 0) {
                        if (currentQuestion.getAnswers()[i] == null)
                            currentQuestion.getAnswers()[i] = new SurveyAnswer(i, null);
                        currentQuestion.getAnswers()[i].setText(ans.getText().toString());
                        currentQuestion.getAnswers()[i].setSelected(true);
                        isAnswered = true;
                    } else {
                        if (currentQuestion.getAnswers()[i] != null) {
                            currentQuestion.getAnswers()[i].setText(null);
                            currentQuestion.getAnswers()[i].setSelected(false);
                        }
                    }
                }
                isSkip = !isAnswered;
            } else {
                if (currentQuestion.getQuestionTYPE() == TYPE.NUMBER_RANGE_SELECTER) {
                    boolean isAnswered = true;
                    for (int i = 0; i < currentQuestion.getAnswers().length; i++) {
                        FreeFormNumAns currentAns = (FreeFormNumAns) currentQuestion
                                .getAnswers()[i];
                        View ansView = mFreeFormChoice.getChildAt(i);
                        NumberPickerSpinner numberPicker = (NumberPickerSpinner) ansView
                                .findViewById(R.id.freeFormSpinner);
                        int position = numberPicker.getSelectedItemPosition();
                        int[] range = currentAns.getAnsRange();

                        if (position != -1) {
                            currentAns.setText((position + range[0]) + "");
                            currentAns.setSelected(true);
                        } else {
                            currentAns.setText(null);
                            currentAns.setSelected(false);
                            isAnswered = false;
                        }
                    }
                    isSkip = !isAnswered;
                } else {
                    if (currentQuestion.getQuestionTYPE() == TYPE.MESSAGE) {
                        isSkip = false;

                    } else {
                        for (int i = 0; i < answers.length; i++) {
                            if (answers[i] != null && answers[i].isSelected()) {
                                isSkip = false;
                                break;
                            }
                        }
                    }
                }
            }
		
		if (currentQuestion.isSkip() || !isSkip) {
			++mCompletedQuestions;
			forwardToNextQuestion(isCompleted);
		} else {
			if (currentQuestion.isMainActivity()) {
				new AlertDialog.Builder(c).setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.skip_title))
					.setMessage(getString(R.string.cannot_skip)).setPositiveButton(getString(R.string.got_it), null).show();
			} else {
				new AlertDialog.Builder(c).setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.skip_title))
					.setMessage(getString(R.string.skip_question))
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							currentQuestion.setSkip(true);
							forwardToNextQuestion(isCompleted);
						}
					})
					.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mNextButton.getText().equals(getString(R.string.done_button))) {
								mIsCompleted = false;
							}
						}
					})
					.show();
			}
		}
	}
			
	protected ArrayList<SurveyQuestion> getPoppedQuestions() {
		return mPoppedQuestions;
	}
	
	public final SurveyPromptEvent getSurveyPromptEvent() {
		return mPromptEvent;
	}

	protected void completeSurvey() {
		finish();
		AppInfo.SetLastTimeCompleted(SurveyActivity.this, Globals.SURVEY, System.currentTimeMillis());
		
		NotificationManager nm = (NotificationManager) SurveyActivity.this.getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(Globals.SURVEY_NOTIFICATION_ID);
	}

	private void forwardToNextQuestion(boolean isCompleted) {
        if(mCurrentQuestion.getQuestionTYPE() != TYPE.MESSAGE) {
            mPromptSender.addPromptEvent(
                    "Answered: " + getCurrentQuestionSelectedAnswer(),
                    new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType(), new Date()
            );
        }
		if (isCompleted) {
			completeSurvey();
			Log.d(TAG, String.format("Total questions: %d, completed questions: %d", mTotalQuestions, mCompletedQuestions));
		} else {
			++mQuestIndex;
			updateQuestion();
			
			mPromptSender.addPromptEvent(
				"Presented Q" + mQuestIndex + ": " + mCurrentQuestion.getQuestionText(),
				new Date(mPromptEvent.getPromptTime()), mPromptEvent.getPromptType(), new Date()
			);
			
			// update the question to monitor
			mLifeMonitor.updateMonitor(mQuestIndex);
		}
	}

	private void setSingleChoiceLayout(Context c, final SurveyQuestion currentQuestion) {
		setListView(c, currentQuestion.getAnswers(), true);
	}

	private void setMultiChoiceLayout(Context c, final SurveyQuestion currentQuestion) {
		setListView(c, currentQuestion.getAnswers(), false);
	}

	private void setListView(Context c, SurveyAnswer[] answers, boolean isSingleChoice) {
		mListView.setVisibility(View.VISIBLE);
		mTimePicker.setVisibility(View.GONE);
		mDatePicker.setVisibility(View.GONE);
		mFreeFormChoice.removeAllViews();
		mListAdapter = new ChoiceAdapter(c, answers, isSingleChoice);
		mListView.setAdapter(mListAdapter);
	}

	private void setMinutePicker(Context c, final SurveyQuestion currentQuestion) {
		mListView.setVisibility(View.GONE);
		mTimePicker.setVisibility(View.VISIBLE);
		mDatePicker.setVisibility(View.GONE);
		mFreeFormChoice.removeAllViews();
		mHourPicker.setVisibility(View.GONE);
		mHourTextView.setVisibility(View.GONE);
		
		SurveyAnswer[] answers = currentQuestion.getAnswers();
		if (answers[0] != null) {
			int currentTime = Integer.parseInt(answers[0].getAnswerText());
			mMinutePicker.setCurrent(currentTime / MINUTE_INTERVAL);
		} else {
			mMinutePicker.setCurrent(0);
		}
	}

	private void setTimePicker(Context c, final SurveyQuestion currentQuestion) {
		mListView.setVisibility(View.GONE);
		mTimePicker.setVisibility(View.GONE);
		mDatePicker.setVisibility(View.VISIBLE);
		mFreeFormChoice.removeAllViews();
		
		SurveyAnswer[] answers = currentQuestion.getAnswers();		
	}

	private void setNumberPickerSpinner(Context c, final SurveyQuestion currentQuestion) {
		
		SurveyAnswer[] answers = currentQuestion.getAnswers();
		
		mListView.setVisibility(View.GONE);
		mTimePicker.setVisibility(View.GONE);
		mDatePicker.setVisibility(View.GONE);
		mFreeFormChoice.removeAllViews();
		
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < answers.length; i++) {			
			View freeForm = layoutInflater.inflate(R.layout.freeformview, mFreeFormChoice, false);
			mFreeFormChoice.addView(freeForm);
			
			if (answers[i] instanceof FreeFormNumAns) {
				final FreeFormNumAns answer = (FreeFormNumAns) answers[i];
				TextView title = (TextView) freeForm.findViewById(R.id.freeFormTitle);
				EditText freeFormAns = (EditText) freeForm.findViewById(R.id.freeformanswer);

				title.setVisibility(View.GONE);
				freeFormAns.setVisibility(View.GONE);
				
				NumberPickerSpinner numberPicker = (NumberPickerSpinner) freeForm.findViewById(R.id.freeFormSpinner);
				numberPicker.setPrompt(answer.getFreeFormDescription());
				int[] range = answer.getAnsRange();
				final String[] options = new String[range[1] - range[0] + 1];
				for (int j = range[0]; j < range[1]; j++) {
					options[j] = answer.getFreeFormDescription() + ":  " + j;
				}
				options[range[1]] = answer.getFreeFormDescription() + ":  " + range[1] + " +";

				NumberPickerSpinnerAdapter adapter = new NumberPickerSpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, options);
				numberPicker.setAdapter(adapter);
				if (answer.getAnswerText() != null) {
					int position = Integer.parseInt(answer.getAnswerText());
					numberPicker.setSelection(position + range[0]);
				}
				
				numberPicker.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						answer.setText(options[arg2]);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {}
				});
			}
		}
	}

	private void setEditText(Context c, final SurveyQuestion currentQuestion) {
		
		SurveyAnswer[] answers = currentQuestion.getAnswers();
		
		mListView.setVisibility(View.GONE);
		mTimePicker.setVisibility(View.GONE);
		mDatePicker.setVisibility(View.GONE);
		mFreeFormChoice.removeAllViews();
		
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < answers.length; i++) {						
			View freeForm = layoutInflater.inflate(R.layout.freeformview, mFreeFormChoice, false);
			mFreeFormChoice.addView(freeForm);			
		
			NumberPickerSpinner numberPicker = (NumberPickerSpinner) freeForm.findViewById(R.id.freeFormSpinner);
			numberPicker.setVisibility(View.GONE);
			
			EditText freeFormAns = (EditText) freeForm.findViewById(R.id.freeformanswer);
			freeFormAns.setInputType(InputType.TYPE_CLASS_TEXT);
			if (answers[i] != null) {
				freeFormAns.setText(answers[i].getAnswerText());
			}
		}
	}

    private void setMessage() {
        mListView.setVisibility(View.GONE);
        mTimePicker.setVisibility(View.GONE);
        mDatePicker.setVisibility(View.GONE);
        mFreeFormChoice.removeAllViews();
    }

//	private OnTimeChangedListener mDateChangeListerner = new OnTimeChangedListener() {
//
//		@Override
//		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//			Date currentTime = new Date();
//			
//			currentTime.setMinutes(minute);
//			currentTime.setHours(hourOfDay);
//
//			String time = sdf.format(currentTime);
//			if (mCurrentQuestion.getAnswers()[0] == null) {
//				mCurrentQuestion.getAnswers()[0] = new SurveyAnswer(0, time);
//			}
//			mCurrentQuestion.getAnswers()[0].setText(time);			
//		}
//	};
	
//	private TextWatcher mTextChangedListener = new TextWatcher() {
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//        	Resources resources = getResources().getSystem();			
//			View hourSpinner   = mDatePicker.findViewById(resources.getIdentifier("hour", "id", "android"));
//			View minuteSpinner = mDatePicker.findViewById(resources.getIdentifier("minute", "id", "android"));
//			EditText hourInput   = (EditText) hourSpinner.findViewById(resources.getIdentifier("numberpicker_input", "id", "android"));
//			EditText minuteInput = (EditText) minuteSpinner.findViewById(resources.getIdentifier("numberpicker_input", "id", "android"));
//			
//			String hour   = hourInput.getText().toString();
//			String minute = minuteInput.getText().toString();
//			
//			Date currentTime = new Date();
//			currentTime.setHours(Integer.parseInt(hour.equals("") ? "0" : hour));
//			currentTime.setMinutes(Integer.parseInt(minute.equals("") ? "0" : minute));				
//
//			String time = sdf.format(currentTime);
//			Log.d(TAG, time);
//			if (mCurrentQuestion.getAnswers()[0] == null) {
//				mCurrentQuestion.getAnswers()[0] = new SurveyAnswer(0, time);
//			}
//			mCurrentQuestion.getAnswers()[0].setText(time);			
//        }
//	};
	
	static final int TIME_DIALOG_ID = 999;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			Date now = Calendar.getInstance().getTime();
			return new TimePickerDialog(this, mDateSetListener, now.getHours(), now.getMinutes(), false); 
		}
		return null;
	}
	
	private OnClickListener mDatePickerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showDialog(TIME_DIALOG_ID);
		}		
	};
	
	private TimePickerDialog.OnTimeSetListener mDateSetListener = 
            new TimePickerDialog.OnTimeSetListener() {
		
		public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
			
			Date currentTime = new Date();
			currentTime.setHours(selectedHour);
			currentTime.setMinutes(selectedMinute);				

			String time = sdf.format(currentTime);
			Log.d(TAG, time);
			if (mCurrentQuestion.getAnswers()[0] == null) {
				mCurrentQuestion.getAnswers()[0] = new SurveyAnswer(0, time);
			}
			mCurrentQuestion.getAnswers()[0].setText(time);	
			
			mDatePicker.setText(time);
		}
	};
	
	//		
	
	private OnChangedListener mTimePickerChangeListener = new OnChangedListener() {

		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {
			int min = mMinutePicker.getCurrent() * MINUTE_INTERVAL;
			
			if (mCurrentQuestion.getAnswers()[0] == null) {
				mCurrentQuestion.getAnswers()[0] = new SurveyAnswer(0, "" + min);
			}
			mCurrentQuestion.getAnswers()[0].setText("" + min);
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToPrevQuestion();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Stupid code here because of the thread problem.
	private BroadcastReceiver choiceUpdate = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			action = action.substring(TAG.length());
			int flag = Integer.parseInt(action);
			int position = intent.getIntExtra("index", -1);
			
			switch (flag) {
			case ChoiceAdapter.UPDATE_ANSWER_SELECTED:
				setCurrentQuestionAnsSelected(position);
				break;
			case ChoiceAdapter.UPDATE_ANSWER_CANCELLED:
				setCurrentQuestionAnsCancelled(position);
				break;
			}
		}
	};

	private void setCurrentQuestionAnsSelected(int position) {
		if (position == -1) {
			Log.e(TAG, "Error: selected ans index from broadcast unknown.");
			return;
		}
		
		SurveyAnswer answer = mCurrentQuestion.getAnswers()[position];
		answer.setSelected(true);
		mQuestQueueHandler.addFollowQuesFromAns(mContext, mOriginQuestions, mMainQuestion, mCurrentQuestion, position);
		if (mCurrentQuestion.getQuestionTYPE() == TYPE.SINGLE_CHOICE) {
			uncheckOtherItems(position);
		}
		PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.ANSWER, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(),
				"AnsIndex: " + answer.getId(),
				"AnsText: " + answer.getAnswerText(), "Selected(Y)");
		
		updateViews();
	}

	private void setCurrentQuestionAnsCancelled(int position) {
		if (position == -1) {
			Log.e(TAG, "Error: cancelled ans index from broadcast unknown.");
			return;
		}
		
		SurveyAnswer answer = mCurrentQuestion.getAnswers()[position];
		answer.setSelected(false);
		mQuestQueueHandler.clearFollowQuesFromAns(mCurrentQuestion, position);
		PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.ANSWER, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(),
				"AnsIndex: " + answer.getId(),
				"AnsText: " + answer.getAnswerText(), "Selected(N)");
		
		updateViews();
	}

	private void uncheckOtherItems(int position) {
		SurveyAnswer[] answers = mCurrentQuestion.getAnswers();
		
		for (int i = 0; i < answers.length; ++i) {
			SurveyAnswer answer = answers[i];
			if (i == position || !answer.isSelected()) {
				continue;
			}
			answers[i].setSelected(false);
			mQuestQueueHandler.clearFollowQuesFromAns(mCurrentQuestion, i);
			PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.ANSWER, mPromptEvent.getScheduledPromptTime(), System.currentTimeMillis(),
					"AnsIndex: " + answer.getId(), 
					"AnsText: " + answer.getAnswerText(), "Selected(N)");
		}
	}
	
	private String getCurrentQuestionSelectedAnswer() {
		SurveyAnswer[] answers = mCurrentQuestion.getAnswers();
		TYPE type = mCurrentQuestion.getQuestionTYPE();
		String textAnswers = "";		
		
		boolean isFirstSelected = true;		
		for (int i = 0; i < answers.length; ++i) {
			SurveyAnswer answer = answers[i];
			if (answer == null) { continue; }
			if (answer.isSelected()) {				
				textAnswers = textAnswers + (isFirstSelected ? "" : ", ") + answer.getAnswerText();
				isFirstSelected = false;
			}	
			if (type == TYPE.MINUTES_PICKER || type == TYPE.TIME_PICKER) {
				textAnswers = answer.getAnswerText();
				break;
			}
		}
		
		if (textAnswers.equals("")) {
			textAnswers = "[skipped]";
		}
		
		return textAnswers;
	}

	private void addGeneratedQuestion(SurveyQuestion ques) {
		boolean isExist = false;
		for (int i = 0; i < mPoppedQuestions.size(); ++i) {
			if (mPoppedQuestions.get(i).getQuestionId().equals(ques.getQuestionId())) {
				isExist = true;
				break;
			}
		}
		if (!isExist) {
			mPoppedQuestions.add(ques);
		}		
	}

	private SurveyQuestion[] getQuestionaire() {
		ArrayList<SurveyQuestion> questions = new ArrayList<SurveyQuestion>();
		
		SurveyQuestion currentques = mStartQuestion;
		questions.add(mStartQuestion);
		while (currentques.getNextQuestionID() != SurveyQuestion.NO_DATA) {
			currentques = findQuestionByID(mPoppedQuestions, currentques.getNextQuestionID());
			if (currentques != null) {
				questions.add(currentques);
			}
		}
		SurveyQuestion[] questionaire = new SurveyQuestion[questions.size()];
		for (int i = 0; i < questionaire.length; ++i) {
			questionaire[i] = questions.get(i);
		}
		
		return questionaire;
	}

	private SurveyQuestion findQuestionByID(ArrayList<SurveyQuestion> questionSet, String questionID) {
		
		for (SurveyQuestion question : questionSet) {
			if (question.getQuestionId().equals(questionID)) {
				return question;
			}
		}
		
		return null;
	}
}
