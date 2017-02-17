package edu.neu.android.wocketslib.activities.helpcomment;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class MessageActivity extends BaseActivity {
	private boolean finishActivity = false;
	private static String TAG = "WOCKETSHelpComment";
	private static String CHARACTERS_LEFT = "Characters left:";

	private int messageType;
	private String subject;
	private Boolean isComment;
	private int maxSMScharacters;
	private String receiverEmailId;
	private TextView charactersLeft;
	private String msg;
	private String prevMsg;
	
	private TextWatcher textWatcher;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.message_activity);

		Button btnCancel = (Button) findViewById(R.id.messageActivityBtnNevermind);
		btnCancel.setOnClickListener(cancelBtnListener);

		Button btnSend = (Button) findViewById(R.id.messageActivityBtnSend);
		btnSend.setOnClickListener(sendBtnListener);

		EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);
		msgEditBox.setOnTouchListener(msgEditBoxTouchListener);
		
		messageType = getIntent().getIntExtra(SendCommentsActivity.MESSAGE_TYPE, SendCommentsActivity.MESSAGE_TYPE_EMAIL);
		
		charactersLeft = (TextView)findViewById(R.id.messageActivityTextCharactersLeft);
		if (messageType == SendCommentsActivity.MESSAGE_TYPE_SMS) {
			charactersLeft.setVisibility(TextView.VISIBLE);
		}
		else {
			charactersLeft.setVisibility(TextView.GONE);
		}
		
		subject = "WOCKETS App Comment ";
		isComment = true;
		
		String parentActivity = getIntent().getStringExtra(SendCommentsActivity.PARENT_ACTIVITY);
		if (getResources().getString(R.string.parent_get_help).equals(parentActivity))
		{
			subject = Globals.STUDY_NAME + " Tech Support Request ";
			isComment = false; 
		}

		String anID = DataStorage.getPhoneID(getApplicationContext(), DataStorage.UNKNOWN_STRING);
		String aCondition = DataStorage.getStudyConditionShortString(getApplicationContext(), DataStorage.UNKNOWN_STRING);
		String aVersion = DataStorage.getVersion(getApplicationContext(), "unk"); 
		int dayNumber = DataStorage.getDayNumber(getApplicationContext(), true); 
		
		String release = "rel"; 
		if (getApplicationContext().getPackageName().contains("wocketsrel"))
			release = "rel";
		else // Using development version 
			release = "dev";
		
		subject += "(" + aCondition + " " + anID + ") [" + aVersion + "] [" + release + "] [D " + dayNumber + "]";

		receiverEmailId = Globals.DEFAULT_CC; 
		
		maxSMScharacters = 160 - (subject + " ").length();
		
		((ApplicationManager) getApplication()).addActivity(this);
	}

	public void onStart() {
		super.onStart();

		if (finishActivity) {
			finishThisActivity();
			return;
		}

		TextView title = (TextView) findViewById(R.id.messageActivityTitle);
		EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);
		String parentActivity = getIntent().getStringExtra(SendCommentsActivity.PARENT_ACTIVITY);
		if (getResources().getString(R.string.parent_get_help).equals(
				parentActivity)) {
			title.setText(getResources().getString(R.string.title1));
			msgEditBox.setText(getResources().getString(R.string.msg1));
		} else {
			title.setText(getResources().getString(R.string.title2));
			if (messageType == SendCommentsActivity.MESSAGE_TYPE_SMS) {
				msgEditBox.setText(getResources().getString(R.string.msg2_sms));
			}
			else {
				msgEditBox.setText(getResources().getString(R.string.msg2));
			}
		}
		
		charactersLeft.setText(CHARACTERS_LEFT + " " + maxSMScharacters);
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(maxSMScharacters);
		
		msgEditBox.setFilters(filterArray);
		
		textWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);
				charactersLeft.setText(CHARACTERS_LEFT + " " + (maxSMScharacters - msgEditBox.length()));
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);
				prevMsg = msgEditBox.getText().toString();
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);

				// removing text watcher here for less confusing call chain
				msgEditBox.removeTextChangedListener(textWatcher);

				if ((start >= 0)
						&& ((start + count) <= s.length())
						&& ((prevMsg.equals(getResources().getString(R.string.msg1)))
								|| (prevMsg.equals(getResources().getString(R.string.msg2)))
								|| (prevMsg.equals(getResources().getString(R.string.msg2_sms))))) {
					msgEditBox.setText(s.subSequence(start, start + count));
					msgEditBox.setSelection(msgEditBox.getText().length());
					afterTextChanged(msgEditBox.getText());
				}

				msgEditBox.addTextChangedListener(textWatcher);
			}
		};
        
        msgEditBox.addTextChangedListener(textWatcher);
	}

	private void finishThisActivity() {
		String parentActivity = getIntent().getStringExtra(SendCommentsActivity.PARENT_ACTIVITY);

		Intent intent = null;
		if (getResources().getString(R.string.parent_get_help).equals(
				parentActivity))
			intent = new Intent(MessageActivity.this,
					GetHelpExitMsgActivity.class);
		else
			intent = new Intent(MessageActivity.this,
					SendCommentsExitMsgActivity.class);
		startActivity(intent);
		MessageActivity.this.finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finishActivity = true;
	}

	private OnTouchListener msgEditBoxTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);
			String msg = msgEditBox.getText().toString();
			if (msg.equals(getResources().getString(R.string.msg1))
					|| msg.equals(getResources().getString(R.string.msg2))
					|| msg.equals(getResources().getString(R.string.msg2_sms))) {
				msgEditBox.setText("");
				return true;
			}
			return false;
		}
	};

	private OnClickListener cancelBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			MessageActivity.this.finish();
		}
	};
	
	private OnClickListener sendBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);

			EditText msgEditBox = (EditText) findViewById(R.id.messageActivityMsgEditBox);
			msg = msgEditBox.getText().toString();

			if (isComment)
			{
				Log.h(TAG, "comment: " + msg, Log.NO_LOG_SHOW);
				Log.o(TAG, "comment");				
			}
			else
			{
				Log.h(TAG, "support: " + msg, Log.NO_LOG_SHOW);
				Log.o(TAG, "support");				
			}

			if (messageType == SendCommentsActivity.MESSAGE_TYPE_EMAIL) {
				Mail m = new Mail();
				m.setUsername(Globals.DEFAULT_MAIL_USERNAME);
				m.setPassword(Globals.DEFAULT_MAIL_PASSWORD);				
				
				String emailAddress = DataStorage.getEmail(getApplicationContext(), "");
				if (emailAddress.equals(""))
					emailAddress = Globals.DEFAULT_MAIL_USERNAME; 
							
				m.setFromAddress(emailAddress);
				m.setToAddresses(new ArrayList<String>(Arrays.asList(new String[] { receiverEmailId, emailAddress })));
				m.setBody(msg);
				m.setSubject(subject);
				m.setHost("smtp.gmail.com");
				m.setSmtpPort("465");
				m.setSocketFactoryPort("465");
				m.setUsesSmtpAuthentication(true);
				
				boolean sendSuccess = true;

				try {					
					sendSuccess = m.send();
				} catch (Exception e) {
					sendSuccess = false;
				}
				
				if (!(sendSuccess)) {
					new AlertDialog.Builder(MessageActivity.this)
							.setTitle("Error sending email")
							.setMessage("There was a problem sending the email. Would you like to try the phone's built-in email app?")
							.setNegativeButton("Stay Here", new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									return;
								}})
							.setPositiveButton("Try Email App", new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
									emailIntent.setType("plain/text");
									emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { receiverEmailId });
									emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
									emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);
									emailIntent.setType("message/rfc822");
									startActivityForResult(Intent.createChooser(emailIntent, "Send mail..."), 1);
								}
							}).show();
				}
				else {
					Intent intent = new Intent(MessageActivity.this, SendCommentsExitMsgActivity.class);
					startActivity(intent);
				}

			}
			else if (messageType == SendCommentsActivity.MESSAGE_TYPE_SMS) {
				PendingIntent pi = PendingIntent.getActivity(arg0.getContext(), 0,
						new Intent(arg0.getContext(), SendCommentsExitMsgActivity.class), 0);
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(Globals.HOTLINE_NUMBER, null, subject + " " + msg, pi, null);
			}
		}
	};
}