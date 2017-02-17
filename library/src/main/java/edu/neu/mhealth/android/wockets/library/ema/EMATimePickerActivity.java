package edu.neu.mhealth.android.wockets.library.ema;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.R;
import edu.neu.mhealth.android.wockets.library.R2;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Question;
import edu.neu.mhealth.android.wockets.library.events.EMABackPressedEvent;
import edu.neu.mhealth.android.wockets.library.events.EMANextPressedEvent;
import edu.neu.mhealth.android.wockets.library.events.EMASurveyCompleteEvent;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.HtmlTagger;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;
import edu.neu.mhealth.android.wockets.library.support.WocketsUtil;

/**
 * @author Dharam Maniar
 */
public class EMATimePickerActivity extends Activity {

	private static final String TAG = "EMATimePickerActivity";

	private Context mContext;

	@BindView(R2.id.wockets_activity_ema_time_picker_text_question)
	TextView questionTextView;

	@BindView(R2.id.wockets_activity_ema_time_picker_time_picker)
	TimePicker timePicker;

	@BindView(R2.id.wockets_activity_ema_time_picker_button_back)
	Button backButton;

	@BindView(R2.id.wockets_activity_ema_time_picker_button_next)
	Button nextButton;

    private Question question;

	private boolean isDemoSurvey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		setContentView(R.layout.wockets_activity_ema_time_picker);
		mContext = getApplicationContext();
		ButterKnife.bind(this);
		EventBus.getDefault().register(this);
		DataManager.setPromptOnScreen(mContext, false);

		Log.i(TAG, "Inside onCreate, Starting EMA Time Picker Activity", mContext);

		String questionJson = getIntent().getStringExtra("questionJson");
		if (questionJson == null || questionJson.isEmpty()) {
			Log.w(TAG, "Question to ask is either null or empty, finishing activity", mContext);
			this.finish();
		}

		String answerString = getIntent().getStringExtra("answerString");

		boolean isFirstPromptOfTheDay = getIntent().getBooleanExtra("isFirstPromptOfTheDay", false);
		isDemoSurvey = getIntent().getBooleanExtra("isDemoSurvey", false);

		String selectedLanguage = DataManager.getSelectedLanguage(mContext);

        question = ObjectMapper.deserialize(questionJson, Question.class);

		Log.i(TAG, "Setting question on screen - " + question.key, mContext);

        String questionText = WocketsUtil.getStringFromListofTextForLanguage(question.text, selectedLanguage);

        if (isFirstPromptOfTheDay) {
            if (question.firstPromptPrefixText != null) {
                questionText = WocketsUtil.getStringFromListofTextForLanguage(question.firstPromptPrefixText, selectedLanguage) + questionText;
            }
        } else {
            if (question.nonFirstPromptPrefixText != null) {
                questionText = WocketsUtil.getStringFromListofTextForLanguage(question.nonFirstPromptPrefixText, selectedLanguage) + questionText;
            }
        }

        questionTextView.setText(HtmlTagger.convertStringToHtmlText(questionText));

		if(!answerString.isEmpty()) {
			timePicker.setCurrentHour(Integer.parseInt(answerString.split(":")[0]));
			timePicker.setCurrentMinute(Integer.parseInt(answerString.split(":")[1]));
		}
		if (question.isBackButtonDisabled) {
			backButton.setVisibility(View.GONE);
		}
		if (question.isReplaceNextWithFinish) {
			nextButton.setText("Finish");
		}
	}

	@Override
	public void onBackPressed() {
		// Disable Android's back button
	}

	@OnClick(R2.id.wockets_activity_ema_time_picker_button_back)
	public void onClickBackButton() {
		Log.i(TAG, "Back button pressed", mContext);
		EventBus.getDefault().post(new EMABackPressedEvent());
		this.finish();
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

	@OnClick(R2.id.wockets_activity_ema_time_picker_button_next)
	public void onClickNextButton() {
		Log.i(TAG, "Next button pressed", mContext);

        int timePickerHour = 0;
        int timePickerMinute = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerHour = timePicker.getHour();
            timePickerMinute = timePicker.getMinute();
        } else {
            timePickerHour = timePicker.getCurrentHour();
            timePickerMinute = timePicker.getCurrentMinute();
        }
        long selectedTime = DateTime.getTimeInMillis(timePickerHour, timePickerMinute);
        long currentTime = DateTime.getCurrentTimeInMillis();

        if (question.isSleepTimeQuestion) {
            if (selectedTime < DateTime.getTimeInMillis(18,0) || selectedTime > DateTime.getTimeInMillis(23, 45)) {
                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(EMATimePickerActivity.this)
                        .setTitle("Sleep Time")
                        .setMessage("Sorry, sleep time should be between 6:00 PM and 11:45 PM")
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
                return;
            }

            if (selectedTime < currentTime) {
                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(EMATimePickerActivity.this)
                        .setTitle("Sleep Time")
                        .setMessage("Sorry, sleep time cannot be in the past.")
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
                return;
            }
        }

        if (question.isWakeTimeQuestion) {
			if (selectedTime < DateTime.getTimeInMillis(3,0) || selectedTime > DateTime.getTimeInMillis(11,59)) {
				final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(EMATimePickerActivity.this)
						.setTitle("Wake Time")
						.setMessage("Sorry, wake time should be between 3:00 AM and 11:59 AM")
						.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
				alertDialog.show();
				return;
			}
        }

		String answer = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();

		if (!isDemoSurvey && question.isSleepTimeQuestion) {
			DataManager.setSleepTime(mContext, answer);
		} else if (!isDemoSurvey && question.isWakeTimeQuestion) {
			DataManager.setWakeTime(mContext, answer);
		}

        if (question.key.contains("SETUP")) {
            this.finish();
        }

		EventBus.getDefault().post(new EMANextPressedEvent(answer));
	}

	@Subscribe
	public void onSurveyCompleteEvent(EMASurveyCompleteEvent event) {
		this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DataManager.isPromptOnScreen(mContext)) {
			this.finish();
			return;
		}
		DataManager.setPromptOnScreen(mContext, true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		DataManager.setPromptOnScreen(mContext, false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Inside onDestroy", mContext);
		EventBus.getDefault().unregister(this);
	}
}
