package edu.neu.android.wocketslib.activities.sensorswap;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;

@SuppressWarnings("unused")
public class DisplayWebTipsActivity extends BaseActivity{
	private static final String TAG = "DisplayWebTipsActivity"; 
	private WebView tipBrowse;
	private Button finishBtn = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.swap_webtips);
		tipBrowse = (WebView) findViewById(R.id.web);
		tipBrowse.setWebViewClient(new TipBrowseClient());
		String address = this.getIntent().getStringExtra("address");
		if(address!=null)
			tipBrowse.loadUrl(address);
		else
			tipBrowse.loadUrl("http://www.google.com");
		
		finishBtn = (Button)this.findViewById(R.id.finish);
		finishBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				finish();
			}
			
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && tipBrowse.canGoBack()) {
			tipBrowse.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void onResume() {
		super.onResume();
		// Force an update
		tipBrowse.reload();
	}
	private class TipBrowseClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}
