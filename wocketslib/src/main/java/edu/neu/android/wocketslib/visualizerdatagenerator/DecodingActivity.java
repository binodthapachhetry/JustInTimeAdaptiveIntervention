package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;

public class DecodingActivity extends BaseActivity{
	private final static String TAG = "WocketsDecoder";
	
	private ArrayList<String> folderList;
	private LinearLayout fileDisplay;
	private ProgressDialog dialog;
	private View v;
	private LayoutInflater inflater;
	private View dialogLayout;
	private AlertDialog getTime;
	private static String startTime;
	private static String endTime;
	private String[] hours;
	private int startHour, startMin, startsec, endHour, endMin, endSec;
	private ArrayAdapter<CharSequence> hourAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.getlocalbaffile_activity);
		fileDisplay = (LinearLayout)findViewById(R.id.dataDisplay);
		getandDisplayLocalFolderName();
		inflater = getLayoutInflater();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(dialog != null)
			dialog.dismiss();
	}
	private void setupTimeSelector(final String dateStr){
		dialogLayout = inflater.inflate(R.layout.datadecodingactivity_entertime, 
				(ViewGroup)findViewById(R.id.decoding_enterTime_layout));

		startTime = "";
		endTime = "";
		final String[] mins = new String[60];
		final String[] secs = new String[60];
		for (int i = 0; i < 60; i++) {
			mins[i] = String.format("%02d", i);
			secs[i] = String.format("%02d", i);
		}
		hours = getAvaiHours(dateStr);
		Spinner startHourSelector = (Spinner)dialogLayout.findViewById(R.id.startTimeHour);
		hourAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, hours);
		hourAdapter.setDropDownViewResource(R.layout.decodingactivity_spinneritem);
		startHourSelector.setAdapter(hourAdapter);
		startHourSelector.setSelection(0);
		startHourSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				startHour = Integer.parseInt(hours[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		Spinner startMinSelector = (Spinner)dialogLayout.findViewById(R.id.startTimeMinute);
		ArrayAdapter<CharSequence> startMinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, mins);
		startMinAdapter.setDropDownViewResource(R.layout.decodingactivity_spinneritem);
		startMinSelector.setAdapter(startMinAdapter);
		startMinSelector.setSelection(0);
		startMinSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				startMin = Integer.parseInt(mins[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		Spinner startSecSelector = (Spinner)dialogLayout.findViewById(R.id.startTimeSec);
		ArrayAdapter<CharSequence> startSecAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, secs);
		startSecAdapter.setDropDownViewResource(R.layout.decodingactivity_spinneritem);
		startSecSelector.setAdapter(startSecAdapter);
		startSecSelector.setSelection(0);
		startSecSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				startsec = Integer.parseInt(secs[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		Spinner endHourSelector = (Spinner)dialogLayout.findViewById(R.id.endTimeHour);
		endHourSelector.setAdapter(hourAdapter);
		endHourSelector.setSelection(hours.length-1);
		endHourSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				endHour = Integer.parseInt(hours[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		Spinner endMinSelector = (Spinner)dialogLayout.findViewById(R.id.endTimeMinute);
		ArrayAdapter<CharSequence> endMinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, mins);
		endMinAdapter.setDropDownViewResource(R.layout.decodingactivity_spinneritem);
		endMinSelector.setAdapter(endMinAdapter);
		endMinSelector.setSelection(mins.length-1);
		endMinSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				endMin = Integer.parseInt(mins[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		Spinner endSecSelector = (Spinner)dialogLayout.findViewById(R.id.endTimeSec);
		ArrayAdapter<CharSequence> endSecAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, secs);
		endSecAdapter.setDropDownViewResource(R.layout.decodingactivity_spinneritem);
		endSecSelector.setAdapter(endSecAdapter);
		endSecSelector.setSelection(secs.length-1);
		endSecSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				endSec = Integer.parseInt(secs[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});

		getTime = new AlertDialog.Builder(DecodingActivity.this)  
		.setIcon(android.R.drawable.ic_dialog_info)
		.setView(dialogLayout)
		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				startTime = dateStr+" "+String.format("%02d", startHour)+":"+String.format("%02d", startMin)+":"+String.format("%02d", startsec);
				endTime = dateStr+" "+String.format("%02d", endHour)+":"+String.format("%02d", endMin)+":"+String.format("%02d", endSec);
				String[] timePeriod = new String[]{startTime, endTime};
				new DecodeTask().execute(timePeriod);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				getTime.dismiss();
			}
		})
		.create();

	}
	private boolean getandDisplayLocalFolderName(){
			folderList = new ArrayList<String>();
			String folderPath = Globals.EXTERNAL_DIRECTORY_PATH + "/" + Globals.DATA_DIRECTORY;
			File dataFolder = new File(folderPath);
			if(!dataFolder.isDirectory())
				return false;
			String[] days = dataFolder.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.matches("\\d{4}-\\d{2}-\\d{2}");
				}
			});
			days = sortFolderByDay(days);
			folderList.addAll(Arrays.asList(days));

			fileDisplay.removeAllViews();
			if(folderList.size() > 0)
				for (String folderName : folderList) {
					TextView fileView = new TextView(DecodingActivity.this);
					fileView.setLayoutParams(new LayoutParams(1, LayoutParams.WRAP_CONTENT));
					fileView.setWidth(0);
					fileView.setPadding(10, 5, 5, 10);
					fileView.setText(folderName);
					fileView.setTextSize(20);
					fileView.setTextColor(Color.BLACK);
					fileView.setOnClickListener(new TextViewOnClickListener());
					fileDisplay.addView(fileView);
				}
			else{
				TextView fileView = new TextView(DecodingActivity.this);
				fileView.setLayoutParams(new LayoutParams(1, LayoutParams.WRAP_CONTENT));
				fileView.setWidth(0);
				fileView.setPadding(5, 5, 5, 10);
				fileView.setText("No raw data file.");
				fileView.setTextSize(20);
				fileDisplay.addView(fileView);

			}
		return true;
	}

	private String[] getAvaiHours(String folderName){
			String folderPath = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY + File.separator + folderName;
			File dataFolder = new File(folderPath);
			if(!dataFolder.isDirectory())
				return null;
			String[] hoursFromFolder = dataFolder.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.matches("\\d{2}");
				}
			});
			int[] hours = new int[hoursFromFolder.length];
			for (int i = 0; i < hoursFromFolder.length; i++) {
				hours[i] = Integer.parseInt(hoursFromFolder[i]);
			}
			int temp = 0;
			for (int i = 0; i < hoursFromFolder.length; i++) {
				for(int j = i+1; j<hoursFromFolder.length;j++){
					if(hours[j] < hours[i]){
						temp = hours[i];
						hours[i] = hours[j];
						hours[j] = temp;
					}
				}
			}
			String[] hoursFromSmallToBig = new String[hoursFromFolder.length];
			for (int i = 0; i < hoursFromFolder.length; i++) {
				hoursFromSmallToBig[i] = String.format("%02d", hours[i]);
			}

			return hoursFromSmallToBig;
	}

	class TextViewOnClickListener implements OnClickListener{

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			String dateString = (String) ((TextView)view).getText();
			startTime = dateString;
			endTime = dateString;
			setupTimeSelector(dateString);
			getTime.show();
			v = view;
		}
	}
	class DecodeTask extends AsyncTask<String[], Void, Void>{
		boolean isDecoded = false;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			((TextView)v).setBackgroundColor(Color.GRAY);
			dialog = ProgressDialog.show(DecodingActivity.this, "Decoding...", "Decoding raw data may take a few minutes depends on the data size." +
					" You can work on other things while running the decoder.");
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.dismiss();
			if(isDecoded){
				((TextView)v).setBackgroundColor(Color.WHITE);
				((TextView)v).setTextColor(Color.RED);
				((TextView)v).append("            (converted)");
				((TextView)v).setClickable(false);
				sendNotification();
			}
			else{
				((TextView)v).setBackgroundColor(Color.WHITE);
				((TextView)v).setTextColor(Color.LTGRAY);
				((TextView)v).setClickable(false);
				Toast.makeText(DecodingActivity.this, "Failed to decode data from the selected time period.", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Void doInBackground(String[]... params) {
			// TODO Auto-generated method stub
			ArrayList<SensorDataInfo> infos = SensorDataFileWriter.checkSavedInfo(DecodingActivity.this, params[0][0].substring(0, params[0][0].indexOf(" ")));
			if(infos != null && infos.size() > 0)
			for (SensorDataInfo sensorDataInfo : infos) {
				Log.d(TAG, "Decode sensor data "+sensorDataInfo.toString());
				DataDecoder decoder = new DataDecoder();
				SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				try {
					isDecoded = 
							decoder.decodeAndSaveDataWithStream(dayFormat.parse(params[0][0]), dayFormat.parse(params[0][1]), sensorDataInfo.getMacID());
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

	}
	private void sendNotification() {
		String msg = "Converting raw data for visualizer completed!";
		int icon = R.drawable.w_ocketspolygonalred24x38;
		final int id = 2;
		String ns = Context.NOTIFICATION_SERVICE;
		Intent onClickEvent = new Intent(getApplicationContext(), DecodingActivity.class);
		PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), id, onClickEvent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews contentView = new RemoteViews(getApplicationContext().getPackageName(),
				R.layout.wockets_notification);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, "Wockets", when);
		notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL);
		contentView.setTextViewText(R.id.notify_msg, msg);
		contentView.setImageViewResource(R.id.notify_icon, icon);

		notification.contentView = contentView;
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.contentIntent = pending;

		NotificationManager noticedManager = (NotificationManager) getApplicationContext()
				.getSystemService(ns);
		noticedManager.notify(id, notification);
	}
	private String[] sortFolderByDay(String[] fileList){
		try {
			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date[] days = new Date[fileList.length];
			for (int i = 0; i < fileList.length; i++) 
				days[i] = dayFormat.parse(fileList[i]);
			Date temp;
			int insertPosition;
			for (int i = 1; i < days.length; i++) {
				temp = days[i];
				insertPosition = i;
				while(insertPosition > 0 && days[insertPosition-1].after(temp)){
					days[insertPosition] = days[insertPosition -1];
					insertPosition--;
				}
				days[insertPosition] = temp;
			}
			for (int i = 0; i < fileList.length; i++) {
				fileList[i] = dayFormat.format(days[fileList.length-1-i]);
			}
			return fileList;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
