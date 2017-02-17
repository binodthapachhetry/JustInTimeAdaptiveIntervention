package edu.neu.android.wocketslib.activities.wocketsnews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.NetworkChecker;
import edu.neu.android.wocketslib.support.NewsChecker;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class NewsViewerActivity extends BaseActivity {
	private static String TAG = "NewsViewerActivity";

	private WebView faqBrowse;
	private static String newsURL = Globals.NEWS_URL; 
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.news_activity);

		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this, false); // Show Toast

		Button btnNeverMind = (Button) findViewById(R.id.btnNeverMind);
		btnNeverMind.setOnClickListener(neverMindBtnListener);

		faqBrowse = (WebView) findViewById(R.id.faqBrowse);
		faqBrowse.setWebViewClient(new FAQBrowseClient());
		faqBrowse.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
	}

	public void setNewsURL(String aNewsURL)
	{
		newsURL = aNewsURL; 
	}
	
	public void onStart() {
		super.onStart();
		Log.h(TAG, "Check news", Log.LOG_SHOW);
		
		if (NetworkChecker.isOnline(getApplicationContext()))
		{
			faqBrowse.loadUrl(newsURL);
		}
		else
		{
			showNoInternetDialogue(); 
		}
	}
	
	private void showNoInternetDialogue()
	{
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage("Your phone cannot connect to the Internet to read the latest study news right now. Try again later when you might have a connection.").setCancelable(false).
			setPositiveButton("Got it", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Action for 'Yes' Button
						Log.i(TAG, "NoInternetNoNews");
						finish();						
						}});
		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		alert.setTitle("Argh");
		// Icon for AlertDialog
		//alert.setIcon(R.drawable.icon);
		alert.show();
	}

	public void onRestart() {
		super.onRestart(); 
		faqBrowse.reload();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && faqBrowse.canGoBack()) {
			faqBrowse.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private OnClickListener neverMindBtnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			NewsChecker.setRead(getApplicationContext()); 
			AppUsageLogger.logClick(TAG, arg0);
			finish();
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