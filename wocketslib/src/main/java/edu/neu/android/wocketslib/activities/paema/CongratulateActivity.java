package edu.neu.android.wocketslib.activities.paema;

import java.util.ArrayList;
import java.util.Date;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.paema.model.LastAccessTimeKeeper;
import edu.neu.android.wocketslib.activities.paema.model.PhysicalActivity;
import edu.neu.android.wocketslib.activities.paema.model.StaticDataStore;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.json.model.PromptEvent;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class CongratulateActivity extends BaseActivity {
	private static final String TAG = "CongratulateActivity";

	WocketInfo wi = new WocketInfo(getApplicationContext()); 
	
	private void registerAnsweredPromptEvent(String aMsg, String primaryActivity, String secondaryActivity, int mins)
	{
		if (wi.somePrompts == null)
			wi.somePrompts = new ArrayList<PromptEvent>();
		
		PromptEvent aPromptEvent = new PromptEvent();
		aPromptEvent.promptTime = DateHelper.getDate(getPromptStartTime()); //  new Date();
		aPromptEvent.responseTime = new Date();
		aPromptEvent.promptType = getPromptType();
		aPromptEvent.activityInterval = 10; //TODO 
		aPromptEvent.primaryActivity = primaryActivity + " [" + mins + " min]";
		aPromptEvent.alternateActivity = secondaryActivity; 		
		
		wi.somePrompts.add(aPromptEvent);
	}

	protected String getPromptType()
	{
		String aKey = Globals.LEVEL3PA;
		long lastTimeStartedManual = AppInfo.GetStartManualTime(getApplicationContext(), aKey);
		long lastTimePrompted = AppInfo.GetLastTimePrompted(getApplicationContext(), aKey);
	
		if ((lastTimeStartedManual == 0) && (lastTimePrompted == 0))
		{
			return "Error. Prompt type unknown."; 
		}
		else if (lastTimePrompted > lastTimeStartedManual)
		{
			// App started because of a prompt; Check if either running out of time or not completed 
			return "Prompt";
		}
		else 
		{
			// App started manually; Check if either running out of time or not completed
			return "Self-initiated";
		}
	}	

	protected long getPromptStartTime()
	{
		String aKey = Globals.LEVEL3PA;
		long lastTimeStartedManual = AppInfo.GetStartManualTime(getApplicationContext(), aKey);
		long lastTimePrompted = AppInfo.GetLastTimePrompted(getApplicationContext(), aKey);
	
		if ((lastTimeStartedManual == 0) && (lastTimePrompted == 0))
		{
			return 0; 
		}
		else if (lastTimePrompted > lastTimeStartedManual)
		{
			// App started because of a prompt; Check if either running out of time or not completed 
			return lastTimePrompted;
		}
		else 
		{
			// App started manually; Check if either running out of time or not completed
			return lastTimeStartedManual;
		}
	}	
	
	private void sendDataServer()
	{
		ArrayList<PhysicalActivity> pa = null;
		try {
			pa = StaticDataStore.getInst().getAvailablePhysicalActivities();
		} catch (WOCKETSException e) {
			// At this stage this error will never happen
		}

		String activityname; 
		String subactivityname; 
		PhysicalActivity aPA;
		String promptType = getPromptType();
		
		for (int i = 0; i < pa.size(); i++) {
			aPA = pa.get(i);
			if (aPA.isSelected())
			{
				activityname = aPA.getName(); 
				subactivityname = "";
				if ((aPA.getFollowupQuestion() != null) && (aPA.getFollowupQuestion().compareTo("") != 0))
					subactivityname = aPA.getAnswer(); 

				registerAnsweredPromptEvent(promptType, activityname, subactivityname, aPA.getMinutes());			
			}
		}
		
		if (wi.phoneID != null)
		{			
			DataSender.transmitOrQueueWocketInfo(getApplicationContext(), wi);
		}
	}
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.congratulate_activity);
		
//		int maxIdleTime = getIntent().getIntExtra("MaxIdleTime", 60); // Default to 60 seconds 
//		IdleTimeKeeper.getInst().init((ApplicationManager) getApplication(), maxIdleTime);
		
		Button btnDone = (Button) findViewById(R.id.btnDone);
		btnDone.setOnClickListener(doneBtnListener);

		Button btnYourHistory = (Button) findViewById(R.id.btnYourHistory);
//		btnYourHistory.setOnClickListener(yourHistoryBtnListener);
		btnYourHistory.setVisibility(View.INVISIBLE);

//		String keyWord = getIntent().getStringExtra("ActivityKeyWord");
//		int timeSpent = getIntent().getIntExtra("timespent", 100);

		// First see if congratulation message was passed in the intent. If not
		// then see if it is set in application manager. If not then get from
		// static data store
		String congratulationMsg = "Thank you for your time. By labeling your activities when the phone requests it, you are helping to make the " + Globals.STUDY_NAME + " study successful."; //getIntent().getStringExtra("CongratulationMsg");
//		if (congratulationMsg == null)
//			congratulationMsg = ((ApplicationManager) getApplication()).getCongratulationMsg();
//		if (congratulationMsg == null)
//			congratulationMsg = StaticDataStore.getInst().getCongratulationMsg(keyWord, timeSpent);

		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(StaticDataStore.getInst().getTitle());

		TextView congratulationMsgView = (TextView) findViewById(R.id.congratulationMsg);
		congratulationMsgView.setText(congratulationMsg);
		Log.logShow(TAG, congratulationMsg);

		((ApplicationManager) getApplication()).addActivity(this);
//		IdleTimeKeeper.getInst().restartTimer();

		// Mark that this app has been used
		AppInfo.MarkAppCompleted(getApplicationContext(), Globals.LEVEL3PA);

	}

	public void onResume() {
		super.onResume();
		
		
//		checkTiming(AppInfo.LEVEL3PA);

//		IdleTimeKeeper.getInst().checkTimer(); 
	}

	private OnClickListener doneBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			new SendSurveyTask().execute();
			LastAccessTimeKeeper.getInst().saveCurrentTime(CongratulateActivity.this);			
			((ApplicationManager) getApplication()).killAllActivities();
		}

	};
		
	private Integer sendSurvey()
	{
		sendDataServer();
		return 0; 
		//return SurveySender.sendSurveyResults(getApplicationContext());  
	}

	private void showToast(String aMsg)
	{
		Toast aToast = Toast.makeText(getApplicationContext(), aMsg, Toast.LENGTH_LONG);
		aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		aToast.show();								
	}
	
	/**
	 * Try to send the survey data right now 
	 */
	private class SendSurveyTask extends AsyncTask<Void, Void, Integer> 
	{
		@Override
		protected Integer doInBackground(Void... params) {
			Integer temp = sendSurvey();
			return temp; 
		}		

		protected void onPreExecute() {
			showToast("Sending your survey answers to the research team...");			
		}
		
		protected void onPostExecute(Integer value) {
				showToast(Globals.STUDY_NAME + " survey sent. Thank you!");
//			else
//				showToast("Thank you!");				
		}
	}
}