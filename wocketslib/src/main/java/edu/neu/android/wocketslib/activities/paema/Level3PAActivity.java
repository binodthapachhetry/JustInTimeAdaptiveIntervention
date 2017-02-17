package edu.neu.android.wocketslib.activities.paema;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.paema.model.PhysicalActivity;
import edu.neu.android.wocketslib.activities.paema.model.StaticDataStore;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.UsageCollector;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class Level3PAActivity extends BaseActivity {

	private static final String TAG = "Level3PA";

	private Button btnPostpone;
	private Button btnBack;
	private ListView lstPhysicalActivities;
	private Level3PAListAdapter listAdapter;
	private ArrayList<PhysicalActivity> physicalActivities;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.level3_pa_activity);

		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this);

//		String congratulationMsg = getIntent().getStringExtra("CongratulationMsg");
//		((ApplicationManager) getApplication()).setCongratulationMsg(congratulationMsg);

//		int maxIdleTime = getIntent().getIntExtra("MaxIdleTime", 60); // Default to 60 seconds 

//		IdleTimeKeeper.getInst().init((ApplicationManager) getApplication(), maxIdleTime);
		UsageCollector.getInst().appStarted("Level 3 PA");
		StaticDataStore.getInst().setStoreToUnintialized();

		btnPostpone = (Button) findViewById(R.id.btnPostpone);
		btnPostpone.setOnClickListener(postponeBtnListener);

		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(backBtnListener);

		Button btnNext = (Button) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(nextBtnListener);

		lstPhysicalActivities = (ListView) findViewById(R.id.lstPhysicalActivities);

//		String urlFiles[] = { Globals.WEB_DATA_DIR + StaticDataStore.fileName };
//		String outFiles[] = { StaticDataStore.pathToFile };
//		try {
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.setClassName("edu.neu.android.wocketslib", "edu.neu.android.wocketslib.filetransfer.FileTransferActivity");
//			intent.putExtra(FileTransferActivity.TAG_KEY, TAG);
//			intent.putExtra("inFiles", urlFiles);
//			intent.putExtra("outFiles", outFiles);
//			startActivityForResult(intent, 1);
//		} catch (Exception e) {
//			Log.e(TAG, e.toString());
//		}
		((ApplicationManager) getApplication()).addActivity(this);
//		IdleTimeKeeper.getInst().restartTimer();
	}
	
	
	public void onResume() {
		super.onResume();

		checkTiming(Globals.LEVEL3PA);
		
//		if (lastTimeStarted > lastTimePrompted) // Manual start or responded to within time so timer started 
//		{
//						
//			if ((System.currentTimeMillis() - lastTimeStarted) > BluetoothSensorService.MAX_TIME_ALLOWED_RESPONDING_PROMPT_MS)
//			{
//				Toast aToast = Toast.makeText(getApplicationContext(), "You must complete the " + Globals.STUDY_NAME + " survey within 45 seconds of starting it.", Toast.LENGTH_LONG);
//				aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//				aToast.show();
//
//				Log.i(TAG, "PA entry timeout"); 
//				((ApplicationManager) getApplication()).killAllActivities();
//				finish();
//			}
//			else 
//			{
//				String msg = "You have " + ((int) Math.round((BluetoothSensorService.MAX_TIME_ALLOWED_RESPONDING_PROMPT_MS - ((System.currentTimeMillis() - lastTimeStarted)))/1000.0)) + " seconds to finish the survey.";
//				Toast aToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
//				aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//				aToast.show();								
//			}			
//		}
//		else // Prompted start 
//		{
//			if ((System.currentTimeMillis()-lastTimePrompted) > BluetoothSensorService.MAX_TIME_ALLOWED_BETWEEN_PROMPT_AND_START_SURVEY_MS)
//			{
//				// Too much time has elapsed before survey started 
//				Toast aToast = Toast.makeText(getApplicationContext(), "You must start the " + Globals.STUDY_NAME + " survey within 5 minutes of the prompt. Try next time.", Toast.LENGTH_LONG);
//				aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//				aToast.show();				
//				Log.i(TAG, "PA prompt not responded to"); 
//
//				((ApplicationManager) getApplication()).killAllActivities();
//				finish();			
//			}
//			else // Started survey within the time limit, so start timing how long running 
//			{
//				AppInfo.SetStartEntryTime(getApplicationContext(), AppInfo.LEVEL3PA, System.currentTimeMillis()); 
//
//				Toast aToast = Toast.makeText(getApplicationContext(), "Start timer.", Toast.LENGTH_SHORT);
//				aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//				aToast.show();			
//
//				Log.i(TAG, "PA prompt started after prompt"); 
//			}
//		}
		
//		IdleTimeKeeper.getInst().checkTimer();

		// onResume method is called soon after filetransfer activity is called.
		// This causes problem when this app is launched for the first time as
		// file for static data has not been fetched yet. To avoid this
		// situation, init method checks for the presence of file. If file is
		// not present then it returns false.
		if (!StaticDataStore.getInst().init(getApplicationContext()))
		{
//			Toast aToast = Toast.makeText(getApplicationContext(),
//					"A file is needed from the Internet before you can run the physical activity tracking app again. Try again later.", Toast.LENGTH_LONG);
//			aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//			aToast.show();			
			Log.e(TAG, "Datafile not available");
			finish(); 
			return; 
		}

		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(StaticDataStore.getInst().getTitle());

//		Calendar lastAccessTime = LastAccessTimeKeeper.getInst().getLastAccessTime(this);

		TextView subHeadingView = (TextView) findViewById(R.id.subHeading);
//		subHeadingView.setText("From " + DateHelper.extractTime(lastAccessTime) + " to now, " + StaticDataStore.getInst().getPhrase());
		subHeadingView.setText("In the last 10 minutes, " + StaticDataStore.getInst().getPhrase());

		try {
			if (listAdapter == null) {
				physicalActivities = StaticDataStore.getInst().getAvailablePhysicalActivities();
				listAdapter = new Level3PAListAdapter(this, physicalActivities);
				lstPhysicalActivities.setAdapter(listAdapter);
			}
			setupViews();
		} catch (WOCKETSException e) {
			new AlertDialog.Builder(Level3PAActivity.this).setTitle("Oops!").setMessage(e.getMessage()).setNegativeButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ApplicationManager) getApplication()).killAllActivities();
				}
			}).show();
		}

	}

	private void setupViews() {
		if (listAdapter == null || !listAdapter.hasPreviousPage()) {
			btnPostpone.setVisibility(View.VISIBLE);
			btnBack.setVisibility(View.GONE);
		} else {
			btnPostpone.setVisibility(View.GONE);
			btnBack.setVisibility(View.VISIBLE);
		}
	}

	private OnClickListener backBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			listAdapter.previousPage();
			setupViews();
		}

	};
	private OnClickListener postponeBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			((ApplicationManager) getApplication()).killAllActivities();
			finish();
			}
	};
	
	private OnClickListener nextBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			if (listAdapter.hasNextPage()) {
				listAdapter.nextPage();
				setupViews();
			} else {
				for (int i = 0; i < physicalActivities.size(); i++) {
					if (physicalActivities.get(i).isSelected()) {
						Log.i(TAG, "Selected: " + physicalActivities.get(i).getName());
					}
				}
				for (int i = 0; i < physicalActivities.size(); i++) {
					// Start next intent only when at least one activity is
					// selected
					if (physicalActivities.get(i).isSelected()) {
						Intent intent = new Intent(Level3PAActivity.this, FollowupQuestionActivity.class);
						startActivity(intent);
						return;
					}
				}
				String aMsg = "C'mon! Select at least one activity. You must have been doing something!";
				Log.logShow(TAG, aMsg);
				new AlertDialog.Builder(Level3PAActivity.this).setTitle("Oops!").setMessage(aMsg).setPositiveButton("Try again", null).show();
			}
		}
	};
}