package edu.neu.android.wocketslib.activities.paema;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.paema.model.PhysicalActivity;
import edu.neu.android.wocketslib.activities.paema.model.PhysicalActivityStore;
import edu.neu.android.wocketslib.activities.paema.model.StaticDataStore;
import edu.neu.android.wocketslib.numberpicker.NumberPicker;
import edu.neu.android.wocketslib.numberpicker.NumberPicker.OnChangedListener;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class FollowupQuestionActivity extends BaseActivity {

	private static final String TAG = "Level3PA";

	private String BLANK = "";
	private String TAP_TO_ENTER = "Tap to enter";
	private ArrayList<PhysicalActivity> selectedPAs;
	private int selectedPAIndex = 0;
	private TextView headingView;
	private TextView hoursText;
	private NumberPicker hoursView;
	private NumberPicker minutesView;
	private TextView questionView;
	private Spinner answerView;
	private PhysicalActivity currentPhysicalActivity;
	private int minuteInterval;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.followup_question_activity);

		// int maxIdleTime = getIntent().getIntExtra("MaxIdleTime", 60); //
		// Default to 60 seconds
		// IdleTimeKeeper.getInst().init((ApplicationManager) getApplication(),
		// maxIdleTime);

		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(backBtnListener);

		Button btnNext = (Button) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(nextBtnListener);

		questionView = (TextView) findViewById(R.id.question);
		answerView = (Spinner) findViewById(R.id.answer);

		headingView = (TextView) findViewById(R.id.heading);

		hoursView = (NumberPicker) findViewById(R.id.hours);
		hoursView.setRange(0, 23);
		hoursView.setOnChangeListener(timeChangedListener);
		hoursView.setTextFocusable(false);
		hoursText = (TextView) findViewById(R.id.hourstext);

		hoursView.setVisibility(View.GONE);
		hoursText.setVisibility(View.GONE);

		minutesView = (NumberPicker) findViewById(R.id.minutes);
		minuteInterval = 1;
		// minutesView.setRange(0, 11, new String[] { "0", "5", "10", "15",
		// "20",
		// "25", "30", "35", "40", "45", "50", "55" });
		minutesView.setRange(0, 10, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
		minutesView.setOnChangeListener(timeChangedListener);
		minutesView.setTextFocusable(false);

		((ApplicationManager) getApplication()).addActivity(this);
		// IdleTimeKeeper.getInst().restartTimer();

	}

	public void onResume() {
		super.onResume();
		// IdleTimeKeeper.getInst().restartTimer();
		// IdleTimeKeeper.getInst().checkTimer();
		checkTiming(Globals.LEVEL3PA);

		ArrayList<PhysicalActivity> pa = null;
		try {
			pa = StaticDataStore.getInst().getAvailablePhysicalActivities();
		} catch (WOCKETSException e) {
			// At this stage this error will never happen
		}
		selectedPAs = new ArrayList<PhysicalActivity>();
		for (int i = 0; i < pa.size(); i++) {
			if (pa.get(i).isSelected())
				selectedPAs.add(pa.get(i));
		}
		setupViews();
	}

	private void setupViews() {

//		Calendar lastAccessTime = LastAccessTimeKeeper.getInst().getLastAccessTime(this);
		
		currentPhysicalActivity = selectedPAs.get(selectedPAIndex);
		headingView.setText("In the last 10 minutes, about how long were you " + currentPhysicalActivity.getName() + "?");

		// headingView.setText("From " + DateHelper.extractTime(lastAccessTime)
		// + " to now, how long did you "
		// + currentPhysicalActivity.getName() + "?");

		hoursView.setCurrent(currentPhysicalActivity.getHours());
		minutesView.setCurrent(currentPhysicalActivity.getMinutes() / minuteInterval);

		questionView.setVisibility(View.INVISIBLE);
		answerView.setVisibility(View.INVISIBLE);

		if (currentPhysicalActivity.getFollowupQuestion() != null && !BLANK.equals(currentPhysicalActivity.getFollowupQuestion())) {
			questionView.setText(currentPhysicalActivity.getFollowupQuestion());

			ArrayList<String> answers = new ArrayList<String>();
			answers.add(TAP_TO_ENTER);
			answers.addAll(currentPhysicalActivity.getFollowupAnswers());

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, answers) {

				public View getDropDownView(int position, View arg1, ViewGroup arg2) {
					if (position == 0)
						return new View(FollowupQuestionActivity.this);
					CheckedTextView v = (CheckedTextView) LayoutInflater.from(FollowupQuestionActivity.this).inflate(R.layout.spinner_checkedtext_item, null);
					String answer = (String) getItem(position);
					v.setText(answer);
					return v;
				}
			};
			answerView.setAdapter(adapter);
			if (currentPhysicalActivity.getAnswer() != null)
				answerView.setSelection(adapter.getPosition(currentPhysicalActivity.getAnswer()));

			int hours = hoursView.getCurrent();
			int minutes = minutesView.getCurrent();

			if (hours != 0 || minutes != 0) {
				questionView.setVisibility(View.VISIBLE);
				answerView.setVisibility(View.VISIBLE);
			}
		}

	}

	private boolean readViews() {
		int hours = hoursView.getCurrent();
		int minutes = minutesView.getCurrent();
		String answer = (String) answerView.getSelectedItem();
		String msg = null;
		if (currentPhysicalActivity.getFollowupQuestion() != null && !currentPhysicalActivity.getFollowupQuestion().equals("") && answer == TAP_TO_ENTER)
			msg = "Please answer the question: " + currentPhysicalActivity.getFollowupQuestion();
		if (hours == 0 && minutes == 0)
			msg = "If you select an activity, you have to enter some time!";
		if (msg != null) {
			new AlertDialog.Builder(FollowupQuestionActivity.this).setTitle("Gotta do it").setMessage(msg).setPositiveButton("Try again", null).show();
			return false;
		}

		currentPhysicalActivity.setHours(hours);
		currentPhysicalActivity.setMinutes(minutes);
		currentPhysicalActivity.setAnswer(answer);
		return true;
	}

	private OnChangedListener timeChangedListener = new OnChangedListener() {

		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {
			if (currentPhysicalActivity.getFollowupQuestion() != null && !BLANK.equals(currentPhysicalActivity.getFollowupQuestion())) {
				questionView.setVisibility(View.VISIBLE);
				answerView.setVisibility(View.VISIBLE);
			}
		}

	};
	private OnClickListener backBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			if (selectedPAIndex == 0) {
				Intent intent = new Intent(FollowupQuestionActivity.this, Level3PAActivity.class);
				startActivity(intent);
			} else {
				selectedPAIndex--;
				setupViews();
			}
		}

	};
	private OnClickListener nextBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			// IdleTimeKeeper.getInst().restartTimer();
			if (!readViews())
				return;

			if (selectedPAIndex < selectedPAs.size() - 1) {
				selectedPAIndex++;
				setupViews();
			} else {
				// Intent intent = new Intent(FollowupQuestionActivity.this,
				// CaloriesBurntDisplayActivity.class);
				// startActivity(intent);

				try {
					PhysicalActivityStore.getInst().writeSelectedPAs(FollowupQuestionActivity.this, selectedPAs);
					PhysicalActivity randomActivity = selectedPAs.get((int) (Math.random() * 100 % selectedPAs.size()));
					int timeSpent = randomActivity.getHours() * 60 + randomActivity.getMinutes();
					Intent intent = new Intent(FollowupQuestionActivity.this, CongratulateActivity.class);
					intent.putExtra("ActivityKeyWord", randomActivity.getKeyWord());
					intent.putExtra("timeSpent", timeSpent);
					startActivity(intent);
				} catch (WOCKETSException e) {
					new AlertDialog.Builder(FollowupQuestionActivity.this).setTitle("Oops!").setMessage(e.getMessage()).setPositiveButton("Ok", null).show();
				}
			}
		}
	};
}
