package edu.neu.mhealth.android.wockets.library.ema;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import edu.neu.mhealth.android.wockets.library.events.EMABackPressedEvent;
import edu.neu.mhealth.android.wockets.library.events.EMANextPressedEvent;
import edu.neu.mhealth.android.wockets.library.events.EMASurveyCompleteEvent;
import edu.neu.mhealth.android.wockets.library.support.HtmlTagger;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;
import edu.neu.mhealth.android.wockets.library.support.WocketsUtil;

/**
 * @author Dharam Maniar
 */
public class EMAMultiChoiceActivity extends AppCompatActivity {

	private static final String TAG = "EMAMultiChoiceActivity";

	private Context mContext;

	@BindView(R2.id.wockets_activity_ema_multi_choice_text_question)
	TextView questionTextView;

	@BindView(R2.id.wockets_activity_ema_multi_choice_checkbox_group)
	LinearLayout linearLayout;

	@BindView(R2.id.wockets_activity_ema_multi_choice_button_back)
	Button backButton;

	@BindView(R2.id.wockets_activity_ema_multi_choice_button_next)
	Button nextButton;

	private Question question;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		setContentView(R.layout.wockets_activity_ema_multi_choice);
		mContext = getApplicationContext();
		ButterKnife.bind(this);
		EventBus.getDefault().register(this);
		DataManager.setPromptOnScreen(mContext, false);

		Log.i(TAG, "Inside onCreate, Starting EMA Multi Choice Activity", mContext);

		String questionJson = getIntent().getStringExtra("questionJson");
		if (questionJson == null || questionJson.isEmpty()) {
			Log.w(TAG, "Question to ask is either null or empty, finishing activity", mContext);
			this.finish();
		}

		String answerString = getIntent().getStringExtra("answerString");

		boolean isFirstPromptOfTheDay = getIntent().getBooleanExtra("isFirstPromptOfTheDay", false);

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

		if (question.isBackButtonDisabled) {
			backButton.setVisibility(View.GONE);
		}
		if (question.isReplaceNextWithFinish) {
			nextButton.setText("Finish");
		}

		for (Answer answer : question.answers) {
			CheckBox checkBox = new CheckBox(mContext);
			checkBox.setText(WocketsUtil.getStringFromListofTextForLanguage(answer.text, selectedLanguage));
			checkBox.setTag(answer.key);
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
			);
			//layoutParams.setMargins(0, 0, 0, 25);
			checkBox.setLayoutParams(layoutParams);
			checkBox.setTextSize(20f);
			checkBox.setTypeface(Typeface.DEFAULT_BOLD);
			checkBox.setTextColor(getResources().getColor(R.color.primary_text));
			if (answerString.contains(answer.key)) {
				checkBox.setChecked(true);
			}
			linearLayout.addView(checkBox);
		}
	}

	@Override
	public void onBackPressed() {
		// Disable Android's back button
	}

	@OnClick(R2.id.wockets_activity_ema_multi_choice_button_back)
	public void onClickBackButton() {
		Log.i(TAG, "Back button pressed", mContext);
		EventBus.getDefault().post(new EMABackPressedEvent());
		this.finish();
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

	@OnClick(R2.id.wockets_activity_ema_multi_choice_button_next)
	public void onClickNextButton() {
		Log.i(TAG, "Next button pressed", mContext);
		List<String> answers = new ArrayList<>();
		int numCheckboxes = linearLayout.getChildCount();
		for (int i = 0 ; i < numCheckboxes ; i++) {
			CheckBox checkBox = (CheckBox)linearLayout.getChildAt(i);
			if (checkBox.isChecked()) {
				answers.add(String.valueOf(checkBox.getTag()));
			}
		}
		if (answers.size() > 0) {
			String answer = WocketsUtil.listOfStringsToString(answers);
			EventBus.getDefault().post(new EMANextPressedEvent(answer));
		} else {
            Log.w(TAG, "Next selected without selecting any answer", mContext);
            if (question.allowToSkip) {
                AlertDialog alertDialog = new AlertDialog.Builder(EMAMultiChoiceActivity.this)
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
                final AlertDialog alertDialog = new AlertDialog.Builder(EMAMultiChoiceActivity.this)
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