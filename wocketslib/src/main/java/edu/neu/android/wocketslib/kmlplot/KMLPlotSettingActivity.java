package edu.neu.android.wocketslib.kmlplot;

import java.util.Calendar;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import edu.neu.android.wocketslib.utils.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Toast;
import android.widget.DatePicker.OnDateChangedListener;

public class KMLPlotSettingActivity extends Activity {
	private String TAG = "PlotSetting";
	private String isPath = "isPath";
	private String isMerge = "isMerge";
	private boolean ISPath = false;
	private boolean ISMerge = true;
	CheckBox pathCheckBox;
	CheckBox mergeCheckBox;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.kmlplotsetting);
		DatePicker KMLStartDatePicker = (DatePicker) findViewById(R.id.start_date);
		DatePicker KMLEndDatePicker = (DatePicker) findViewById(R.id.end_date);
		pathCheckBox = (CheckBox) findViewById(R.id.isPlotLine);
		pathCheckBox.setChecked(false);
		mergeCheckBox = (CheckBox) findViewById(R.id.isMergePoints);
		mergeCheckBox.setChecked(true);
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int monthOfYear = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		Globals.KMLStartDate = String.valueOf(year) + "-"
				+ String.format("%02d", monthOfYear + 1) + "-"
				+ String.format("%02d", dayOfMonth);
		Log.d(TAG, "Start Date: " + Globals.KMLStartDate);
		Globals.KMLEndDate = String.valueOf(year) + "-"
				+ String.format("%02d", monthOfYear + 1) + "-"
				+ String.format("%02d", dayOfMonth);
		Log.d(TAG, "End Date: " + Globals.KMLEndDate);
		KMLStartDatePicker.init(year, monthOfYear, dayOfMonth,
				new OnDateChangedListener() {
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						Globals.KMLStartDate = String.valueOf(year) + "-"
								+ String.format("%02d", monthOfYear + 1) + "-"
								+ String.format("%02d", dayOfMonth);
						Log.d(TAG, "Start Date: " + Globals.KMLStartDate);
					}
				});
		
		KMLEndDatePicker.init(year, monthOfYear, dayOfMonth,
				new OnDateChangedListener() {
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						Globals.KMLEndDate = String.valueOf(year) + "-"
								+ String.format("%02d", monthOfYear + 1) + "-"
								+ String.format("%02d", dayOfMonth);
						Log.d(TAG, "End Date: " + Globals.KMLEndDate);
					}

				});
	}

	public void plot(View view) {
		ISPath = pathCheckBox.isChecked();
		ISMerge = mergeCheckBox.isChecked();
		if (checkValid()) {
			Intent intent = new Intent(this, KMLPlotInMapActivity.class);
			intent.putExtra(isPath, ISPath);
			intent.putExtra(isMerge, ISMerge);
			startActivity(intent);
		} else {
			show_toast("Please Choose the Valid Start date and End date!");
		}
	}

	public void cancel(View view) {
		finish();
	}
	
	private boolean checkValid() {
		if (Globals.KMLStartDate.compareTo(Globals.KMLEndDate) <= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	private void show_toast(String answer) {
		Toast.makeText(this, answer, Toast.LENGTH_SHORT).show();
	}
}
