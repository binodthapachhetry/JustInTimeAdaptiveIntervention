package edu.neu.android.wocketslib.activities.helpcomment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class SendCommentsActivity extends BaseActivity {

	private static String TAG = "WOCKETSHelpComment";

	static String PARENT_ACTIVITY = "parentActivity";
	static String MESSAGE_TYPE = "messageType";
	static int MESSAGE_TYPE_EMAIL = 0;
	static int MESSAGE_TYPE_SMS = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.send_comments_activity);

		Log.i(TAG, "Send comments");

		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this);

		Button btnSendEmail = (Button) findViewById(R.id.btnSendEmail);
		btnSendEmail.setOnClickListener(btnSendEmailListener);

		Button btnSendText = (Button) findViewById(R.id.btnSendText);
		btnSendText.setOnClickListener(btnSendTextListener);

		Button btnNeverMind = (Button) findViewById(R.id.btnNeverMind);
		btnNeverMind.setOnClickListener(neverMindBtnListener);

		Button btnCallHotline = (Button) findViewById(R.id.btnCallHotline);
		btnCallHotline.setOnClickListener(phoneBtnListener);

		((ApplicationManager) getApplication()).addActivity(this);
	}

	private OnClickListener neverMindBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};

	private boolean isConnected() {
		try {
			ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr
					.getActiveNetworkInfo().isConnected());
		} catch (Throwable ex) {
		}
		return false;
	}

	private boolean hasMobileConnectivity() {
		// Context.CONNECTIVITY_SERVICE apparently doesn't work when WIFI is on
		/*
		 * ConnectivityManager conMgr = (ConnectivityManager)
		 * getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo
		 * mobileInfo = conMgr .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		 * return mobileInfo.isConnected();
		 */

		TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		return ((telMgr.getNetworkType() != android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN) && (telMgr
				.getSimState() == TelephonyManager.SIM_STATE_READY));
	}

	private OnClickListener btnSendEmailListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			if (!(isConnected())) {
				dialogForNoInternetConnectivity().show();
				return;
			}

			Intent intent = new Intent(SendCommentsActivity.this, MessageActivity.class);
			intent.putExtra(PARENT_ACTIVITY, getResources().getString(R.string.parent_send_comment));
			intent.putExtra(MESSAGE_TYPE, MESSAGE_TYPE_EMAIL);
			startActivity(intent);
		}
	};

	private OnClickListener btnSendTextListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			if (!(hasMobileConnectivity())) {
				dialogForNoMobileConnectivity("Texting Unavailable").show();
				return;
			}
			Intent intent = new Intent(SendCommentsActivity.this, MessageActivity.class);
			intent.putExtra(PARENT_ACTIVITY, getResources().getString(R.string.parent_send_comment));
			intent.putExtra(MESSAGE_TYPE, MESSAGE_TYPE_SMS);
			startActivity(intent);
		}
	};

	private OnClickListener phoneBtnListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			if (!(hasMobileConnectivity())) {
				dialogForNoMobileConnectivity("Phone Unavailable").show();
				return;
			}
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Globals.HOTLINE_NUMBER));
			startActivity(intent);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};

	private AlertDialog.Builder dialogForNoInternetConnectivity() {
		if (hasMobileConnectivity()) {
			return new AlertDialog.Builder(SendCommentsActivity.this).setTitle("Internet Unavailable")
					.setMessage("Sorry, no Internet connection is available. Would you like to send a text instead?")
					.setPositiveButton("Send Text", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SendCommentsActivity.this.btnSendTextListener.onClick(null);
						}
					}).setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		} else {
			return new AlertDialog.Builder(SendCommentsActivity.this).setTitle("Internet Unavailable")
					.setMessage("Sorry, no Internet connection is available. Please try again later.")
					.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		}
	}

	private AlertDialog.Builder dialogForNoMobileConnectivity(String title) {
		if (isConnected()) {
			return new AlertDialog.Builder(SendCommentsActivity.this).setTitle(title)
					.setMessage("Sorry, you're out of coverage right now. Would you like to send an email instead?")
					.setPositiveButton("Send Email", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SendCommentsActivity.this.btnSendEmailListener.onClick(null);
						}
					}).setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		} else {
			return new AlertDialog.Builder(SendCommentsActivity.this).setTitle(title)
					.setMessage("Sorry, you're out of coverage right now.")
					.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		}
	}
}