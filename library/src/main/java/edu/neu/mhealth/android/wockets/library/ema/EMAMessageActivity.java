package edu.neu.mhealth.android.wockets.library.ema;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import edu.neu.mhealth.android.wockets.library.support.HtmlTagger;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;
import edu.neu.mhealth.android.wockets.library.support.WocketsUtil;

/**
 * @author Dharam Maniar
 */
public class EMAMessageActivity extends AppCompatActivity {

	private static final String TAG = "EMAMessageActivity";

	private Context mContext;

	@BindView(R2.id.wockets_activity_ema_message_text_message)
	TextView messageTextView;

	@BindView(R2.id.wockets_activity_ema_message_button_back)
	Button backButton;

	@BindView(R2.id.wockets_activity_ema_message_button_next)
	Button nextButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		setContentView(R.layout.wockets_activity_ema_message);
		mContext = getApplicationContext();
		ButterKnife.bind(this);
		EventBus.getDefault().register(this);
		DataManager.setPromptOnScreen(mContext, false);

		Log.i(TAG, "Inside onCreate, Starting EMA Message Activity", mContext);

		String questionJson = getIntent().getStringExtra("questionJson");
		if (questionJson == null || questionJson.isEmpty()) {
			Log.w(TAG, "Question to ask is either null or empty, finishing activity", mContext);
			this.finish();
		}

		String selectedLanguage = DataManager.getSelectedLanguage(mContext);

		Question question = ObjectMapper.deserialize(questionJson, Question.class);

		String message = WocketsUtil.getStringFromListofTextForLanguage(question.text, selectedLanguage);
		if (message == null || message.isEmpty()) {
			Log.w(TAG, "Message to display is either null or empty, finishing activity", mContext);
			this.finish();
		}

		if (question.isBackButtonDisabled) {
			backButton.setVisibility(View.GONE);
		}
		if (question.isReplaceNextWithFinish) {
			nextButton.setText("Finish");
		}

		Log.i(TAG, "Setting message on screen - " + message, mContext);
		messageTextView.setText(HtmlTagger.convertStringToHtmlText(message));
	}

	@Override
	public void onBackPressed() {
		// Disable Android's back button
	}

	@OnClick(R2.id.wockets_activity_ema_message_button_back)
	public void onClickBackButton() {
		Log.i(TAG, "Back button pressed", mContext);
		this.finish();
		EventBus.getDefault().post(new EMABackPressedEvent());
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

	@OnClick(R2.id.wockets_activity_ema_message_button_next)
	public void onClickNextButton() {
		Log.i(TAG, "Next button pressed", mContext);
		EventBus.getDefault().post(new EMANextPressedEvent());
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
