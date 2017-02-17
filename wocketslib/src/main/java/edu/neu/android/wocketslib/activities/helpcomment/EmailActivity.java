package edu.neu.android.wocketslib.activities.helpcomment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
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
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class EmailActivity extends BaseActivity {
	private boolean finishActivity = false;
	private static String TAG = "WOCKETSHelpComment";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.email_activity);

		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(cancelBtnListener);

		Button btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(sendBtnListener);

		Button btnPhone = (Button) findViewById(R.id.btnPhone);
		btnPhone.setOnClickListener(phoneBtnListener);

		EditText msgEditBox = (EditText) findViewById(R.id.msgEditBox);
		msgEditBox.setOnTouchListener(msgEditBoxTouchListener);
	}

	public void onStart() {
		super.onStart();

		if (finishActivity) {
			finishThisActivity();
			return;
		}

		if (!isConnected()) {
			new AlertDialog.Builder(this)
					.setTitle("Error")
					.setMessage(
							"No connection to internet. Please try another time")
					.setPositiveButton(
							"Ok",
							new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// EmailActivity.this.finish();
								}
							}).show();
			return;
		}

		TextView title = (TextView) findViewById(R.id.title);
		EditText msgEditBox = (EditText) findViewById(R.id.msgEditBox);
		String parentActivity = getIntent().getStringExtra("parentActivity");
		if (getResources().getString(R.string.parent_get_help).equals(
				parentActivity)) {
			title.setText(getResources().getString(R.string.title1));
			msgEditBox.setText(getResources().getString(R.string.msg1));
		} else {
			title.setText(getResources().getString(R.string.title2));
			msgEditBox.setText(getResources().getString(R.string.msg2));
		}

	}

	private void finishThisActivity() {
		String parentActivity = getIntent().getStringExtra("parentActivity");

		Intent intent = null;
		if (getResources().getString(R.string.parent_get_help).equals(
				parentActivity))
			intent = new Intent(EmailActivity.this,
					GetHelpExitMsgActivity.class);
		else
			intent = new Intent(EmailActivity.this,
					SendCommentsExitMsgActivity.class);
		startActivity(intent);
		EmailActivity.this.finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finishActivity = true;
	}

	private boolean isConnected() {
		try {
			ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			return (conMgr.getActiveNetworkInfo() != null
					&& conMgr.getActiveNetworkInfo().isAvailable() && conMgr
					.getActiveNetworkInfo().isConnected());
		} catch (Throwable ex) {
		}
		return false;
	}

	private OnTouchListener msgEditBoxTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			EditText msgEditBox = (EditText) findViewById(R.id.msgEditBox);
			String msg = msgEditBox.getText().toString();
			if (msg.equals(getResources().getString(R.string.msg1))
					|| msg.equals(getResources().getString(R.string.msg2))) {
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
			EmailActivity.this.finish();
		}
	};

	private OnClickListener phoneBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			ServerLogger.sendNote(getApplicationContext(), "Called hotline", true);
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Globals.HOTLINE_NUMBER));
			startActivity(intent);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};

	private OnClickListener sendBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);

			String subject = Globals.STUDY_NAME + " App Comment ";
			boolean isComment = true;
			
			String parentActivity = getIntent().getStringExtra("parentActivity");
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

			String receiverEmailId = Globals.DEFAULT_CC;

			EditText msgEditBox = (EditText) findViewById(R.id.msgEditBox);
			String msg = msgEditBox.getText().toString();

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

			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { receiverEmailId });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);
			emailIntent.setType("message/rfc822");
			ServerLogger.sendNote(getApplicationContext(), "Sent email: " + subject + " " + msg, true);
			startActivityForResult(Intent.createChooser(emailIntent,
					"Send mail..."), 1);
		}
	};
}