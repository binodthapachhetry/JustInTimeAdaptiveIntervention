package edu.neu.android.wocketslib.activities.datasummaryviewer;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;

public class GetDataSummaryActivity extends BaseActivity{
	private static final String TAG = "GetDataSummaryActivity";
	public final static String INTENT_ACTION_EXIT = "INTENT_ACTION_EXIT";
	public final static String INTENT_ACTION_UPDATE_DATA = "INTENT_ACTION_UPDATE_DATA";
	public final static String[] labels = {"Wrist L Summary","Wrist L Raw","Wrist R Summary","Wrist R Raw"
			,"Ankle L Summary","Ankle L Raw","Ankle R Summary","Ankle R Raw","Pocket L Summary","Pocket L Raw"
			,"Pocket R Summary","Pocket R Raw", "Heart Rate Monitor Summary", "Internal Accelerometer Summary"};
	public final static int[][] offsets = {{10, 68},{10,102},{10,142},{10,176},{10,216},{10,250}
	,{10,290},{10,324},{10,364},{10,398},{10,438},{10,472}, {10,510}, {10,540}};
	public final static int pointPxs = 4;
	private int xOffset = 20;
	private Button closeBtn;
	private FrameLayout frame;
	private LinearLayout dataViewer;
	private GraphPaperView g;
	private DateOverlayView dateOverlayView;
	private DataSummaryViewer drawData = null;
//	private HorizontalScrollView scroller;
	private UpdateViewBroadcastReceiver updateReceiver;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.data_summary_activity);
		closeBtn = (Button)findViewById(R.id.datasummarybuttonback);		
		closeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((ApplicationManager) getApplication()).killAllActivities();
			}
		});
		frame = (FrameLayout)findViewById(R.id.datasummaryviewer);
		dataViewer = (LinearLayout) findViewById(R.id.list);
//		scroller = (HorizontalScrollView)findViewById(R.id.scroller);
		
		updateReceiver = new UpdateViewBroadcastReceiver();

		new DrawDataSummarySyncTask().execute();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerReceiver(updateReceiver, new IntentFilter(INTENT_ACTION_UPDATE_DATA));

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(updateReceiver);
		if(dialog != null)
			dialog.dismiss();
	}

	private class DrawDataSummarySyncTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(GetDataSummaryActivity.this, "Drawing graph...", 
					"It may take minutes to draw the summary graph, " +
					"please wait.");
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.dismiss();
			frame.addView(g);
			frame.addView(dateOverlayView);
			dataViewer.addView(drawData);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			g = new GraphPaperView(GetDataSummaryActivity.this, 7);
			dateOverlayView = new DateOverlayView(GetDataSummaryActivity.this, xOffset);
			drawData = new DataSummaryViewer(GetDataSummaryActivity.this, xOffset);
			drawData.setLayoutParams(new HorizontalScrollView.LayoutParams(60*25*pointPxs+20,
					((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight()));
			return null;
		}
		
	}
	private class UpdateViewBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(drawData != null)
				drawData.update();
		}
		
	}

}
