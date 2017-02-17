package edu.neu.android.wocketslib.activities.guidelines;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class GuidelinesActivity extends BaseActivity {
	private static final String TAG = "Guidelines";

	private ArrayList<String> guideLines;
	private ArrayList<String> recommendedTargets;
	
	private static long lastTimeSentNote = 0; 
	private static long MINUTES_3_IN_MS = 1000*60*3; 


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.guidelines_activity);

		Log.h(TAG, "Review guidelines", Log.LOG_SHOW);
		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this, false); // Show																		// Toast

		Button btnDone = (Button) findViewById(R.id.btnDone);
		btnDone.setOnClickListener(doneBtnListener);

		generateStaticData();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.guidelines_list_item, guideLines) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = LayoutInflater.from(GuidelinesActivity.this).inflate(
						R.layout.guidelines_list_item, null);
				((TextView) v.findViewById(R.id.guideline)).setText(guideLines
						.get(position));
				((TextView) v.findViewById(R.id.recommendedTarget))
						.setText(recommendedTargets.get(position));
				return v;
			}
		};
		((ListView) findViewById(R.id.guidelinesList)).setAdapter(adapter);

		((ApplicationManager) getApplication()).addActivity(this);
	}
	
	public void onResume() {
		super.onResume();
		 
		if ((System.currentTimeMillis()-lastTimeSentNote) > MINUTES_3_IN_MS) // more than 3 minutes
		{
			ServerLogger.sendNote(getApplicationContext(), "Checked Tips", true);
			lastTimeSentNote = System.currentTimeMillis(); 			
		}
	}	

	
	private OnClickListener doneBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			AppInfo.MarkAppCompleted(getApplicationContext(), Globals.GUIDELINES);
			((ApplicationManager) getApplication())
					.killActivity(GuidelinesActivity.this);
		}

	};

	private void generateStaticData() {
		guideLines = new ArrayList<String>();
		recommendedTargets = new ArrayList<String>();

		guideLines.add("Wear the Wockets every day, as much as you can.");
		recommendedTargets.add("");
		guideLines.add("Use the project phone as your only phone throughout the study."); 
		recommendedTargets.add("");
		guideLines.add("Wear Wockets and keep your mobile phone with you as much as possible, 24/7."); 
		recommendedTargets.add("");
		guideLines.add("Charge your phone daily, a minimum of at least 4 hours each night."); 
		recommendedTargets.add("Overnight while the phone is charging Wocket data are uploaded to the server (between 2-5am).");
		guideLines.add("Charge your Wockets daily, a minimum of at least 4 hours each night (alternate sets daily)."); 
		recommendedTargets.add("");
		guideLines.add("Perform a \'Swap\' every day."); 
		recommendedTargets.add("This keeps the Wockets you are wearing fully charged.");
		guideLines.add("Respond to prompts and text messages from project staff."); 
		recommendedTargets.add("");
		guideLines.add("Tip: The Wockets continue to collect data even when the phone is not nearby."); 
		recommendedTargets.add("");
		guideLines.add("Tip: You may need to charge the phone during the day depending on your phone use."); 
		recommendedTargets.add("Please do NOT run a task killer app on your phone.");
		guideLines.add("Tip: If you need to change the location of the Wockets, remember to tell the software you've change the location."); 
		recommendedTargets.add("");
		guideLines.add("Tip: When you are traveling in a car, the phone can be in your pocket, bag or case or on the seat."); 
		recommendedTargets.add("");
		guideLines.add("Tip: If you don\'t carry your phone during vigorous exercise, remember to continue to wear the Wockets and the information about your activity will be automatically transferred to the phone later.");
		recommendedTargets.add("");
		guideLines.add("Tip: While you should take the Wockets off while showering, bathing, or  swimming, they can be worn while washing your hands, dishes or working in the garden."); 
		recommendedTargets.add("");
		guideLines.add("Tip: Most people find it easier to remember to do a specific task if it is part of a daily routine."); 
		recommendedTargets.add("");
		guideLines.add("Tip: Decide on a specific place and time to charge your phone-such as keeping a charger in the bedroom and always plug the phone into the charger before going to bed."); 
		recommendedTargets.add("");
		guideLines.add("Tip: If you shower each morning you can remove the set of Wockets you wore yesterday and last night and then after the shower Swap to the charged set."); 
		recommendedTargets.add("");
		guideLines.add("Tip: The best way to keep the phone near the Wockets during the day is to routinely carry the phone in a pocket, on your belt or in a purse or satchel."); 
		recommendedTargets.add("");
		guideLines.add("Tip: Some people set the alarm on their phone at a time shortly before leaving for work to remind them to be sure they are wearing their Wockets and have their mobile phone."); 				
		recommendedTargets.add("");
		guideLines.add("Tip: If you think of ways that the Wockets could be improved, please let the research team know! We value your feedback."); 				
		recommendedTargets.add("You can use the \'Questions or Problems\' button to get us a message.");
	}
}