package edu.neu.mhealth.android.wockets.library.ema;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.R;
import edu.neu.mhealth.android.wockets.library.R2;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Answer;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Question;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Text;
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
public class EMANumberPickerActivity extends Activity {

	private static final String TAG = "EMANumberPickerActivity";

	private Context mContext;

	@BindView(R2.id.wockets_activity_ema_number_picker_text_question)
	TextView questionTextView;

	@BindView(R2.id.wockets_activity_ema_number_picker_number_picker)
	NumberPicker numberPicker;

	@BindView(R2.id.wockets_activity_ema_number_picker_button_back)
	Button backButton;

	@BindView(R2.id.wockets_activity_ema_number_picker_button_next)
	Button nextButton;

    private Question question;

	private boolean isDemoSurvey;

	private String[] displayedText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		setContentView(R.layout.wockets_activity_ema_number_picker);
		mContext = getApplicationContext();
		ButterKnife.bind(this);
		EventBus.getDefault().register(this);
		DataManager.setPromptOnScreen(mContext, false);

		Log.i(TAG, "Inside onCreate, Starting EMA Number Picker Activity", mContext);

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
		int minValue = question.range.get(0);
		int maxValue = question.range.get(1);
		numberPicker.setMinValue(minValue);
		numberPicker.setMaxValue(maxValue + 1);
		displayedText = new String[maxValue - minValue + 2];
		for(int i = 0; i < maxValue - minValue + 1; i++) {
			displayedText[i] = String.valueOf(minValue + i);
		}
		displayedText[maxValue - minValue + 1] = "";
		numberPicker.setDisplayedValues(displayedText);
		numberPicker.setValue(maxValue + 1);

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

	@OnClick(R2.id.wockets_activity_ema_number_picker_button_back)
	public void onClickBackButton() {
		Log.i(TAG, "Back button pressed", mContext);
		EventBus.getDefault().post(new EMABackPressedEvent());
		this.finish();
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

	@OnClick(R2.id.wockets_activity_ema_number_picker_button_next)
	public void onClickNextButton() {
		Log.i(TAG, "Next button pressed", mContext);
		String answerContent = displayedText[numberPicker.getValue()];
//		Log.i(TAG,"Answer list size:"+Integer.toString(question.answers.size()),mContext);

//		String answerKey = question.answers.get(0).key;
		if(!answerContent.isEmpty()){
			EventBus.getDefault().post(new EMANextPressedEvent(answerContent));
//			EventBus.getDefault().post(new EMANextPressedEvent(answerKey + ":" + answerContent));
		} else {
			Log.w(TAG, "Next selected without selecting any answer", mContext);
			if (question.allowToSkip) {
				AlertDialog alertDialog = new AlertDialog.Builder(EMANumberPickerActivity.this)
						.setTitle("Skip Question")
						.setMessage("Are you sure you want to skip this question?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								EventBus.getDefault().post(new EMANextPressedEvent());
								dialog.dismiss();
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
				alertDialog.show();
			} else {
				final AlertDialog alertDialog = new AlertDialog.Builder(EMANumberPickerActivity.this)
						.setTitle("Skip Question")
						.setMessage("Sorry, you cannot skip this question.")
						.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
				alertDialog.show();
			}
		}
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
