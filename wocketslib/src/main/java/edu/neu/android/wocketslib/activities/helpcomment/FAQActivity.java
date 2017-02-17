package edu.neu.android.wocketslib.activities.helpcomment;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class FAQActivity extends BaseActivity {
	private WebView faqBrowse;
	private static String TAG = "WOCKETSHelpComment";

	private static long lastTimeSentNote = 0; 
	private static long MINUTES_3_IN_MS = 1000*60*3; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.faq_activity);

		Log.i(TAG, "View FAQ");

		Button btnAskWOCKETS = (Button) findViewById(R.id.btnAskWOCKETS);
		btnAskWOCKETS.setOnClickListener(askWOCKETSBtnListener);

		Button btnNeverMind = (Button) findViewById(R.id.btnNeverMind);
		btnNeverMind.setOnClickListener(neverMindBtnListener);

		faqBrowse = (WebView) findViewById(R.id.faqBrowse);
		faqBrowse.setWebViewClient(new FAQBrowseClient());
		String summary = "<html><body>Hold on ... trying to load FAQ from the Internet...</body></html>"; 
		faqBrowse.loadData(summary, "text/html", null);
		 
		((ApplicationManager) getApplication()).addActivity(this);
	}

	public void onStart() {
		super.onStart();

		faqBrowse.loadUrl(Globals.FAQ_URL);
	}

	public void onResume() {
		super.onResume();
		// Force an update
		faqBrowse.reload();
		
		if ((System.currentTimeMillis()-lastTimeSentNote) > MINUTES_3_IN_MS) // more than 3 minutes
		{
			ServerLogger.sendNote(getApplicationContext(), "Checked FAQ", true);
			lastTimeSentNote = System.currentTimeMillis(); 			
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && faqBrowse.canGoBack()) {
			faqBrowse.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private OnClickListener askWOCKETSBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			Intent intent = new Intent(FAQActivity.this, EmailActivity.class);
			intent.putExtra("parentActivity", getResources().getString(
					R.string.parent_get_help));
			startActivity(intent);
		}
	};
	private OnClickListener neverMindBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			AppUsageLogger.logClick(TAG, arg0);
			((ApplicationManager) getApplication()).killAllActivities();
		}
	};

	private class FAQBrowseClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}