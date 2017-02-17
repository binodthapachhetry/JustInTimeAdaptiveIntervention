package edu.neu.android.wocketslib.activities.paema.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class PhysicalActivityStore {
	private static final String TAG = "Level3PA";


	public static String path = ".wockets/data/level3pa/";
	public static String fileName = "Level3-PA.log.csv";
	public static String pathToFile = path + fileName;

	private static PhysicalActivityStore inst = new PhysicalActivityStore();

	public void writeSelectedPAs(Activity activity, ArrayList<PhysicalActivity> selectedPhysicalActivities)
			throws WOCKETSException {
		FileHelper.createDir(path);

		StringWriter buffer = new StringWriter();
		if (!FileHelper.isFileExists(pathToFile))
			buffer.append("Keyword,Name,question,answer,time,hours,minutes\n");

		String finishTime = DateHelper.extractDateTime(Calendar.getInstance());
		CSVWriter writer = new CSVWriter(buffer);
		for (int i = 0; i < selectedPhysicalActivities.size(); i++) {
			PhysicalActivity spa = selectedPhysicalActivities.get(i);
			writer
					.writeNext(new String[] { spa.getKeyWord(), spa.getName(), spa.getFollowupQuestion(),
							spa.getAnswer(), finishTime, Integer.toString(spa.getHours()),
							Integer.toString(spa.getMinutes()) });
		}
		FileHelper.appendToFile(buffer.toString(), pathToFile);
		saveTotalCaloriesBurntToday(activity, selectedPhysicalActivities);
		saveToHistory(activity, selectedPhysicalActivities);
	}

	private void saveToHistory(Context context, ArrayList<PhysicalActivity> selectedPhysicalActivities) {
		StringBuffer history = new StringBuffer();
		// TODO PA_MINS ? 
		//		int PA_MINS = 0;
		for (int i = 0; i < selectedPhysicalActivities.size(); i++) {
			PhysicalActivity pa = selectedPhysicalActivities.get(i);
			history.append(pa.getHours() * 60 + pa.getMinutes());
			//PA_MINS += pa.getHours() * 60 + pa.getMinutes();
			history.append("|");
			if (pa.getAnswer() != null)
				history.append(pa.getName() + " " + pa.getAnswer().toLowerCase());
			else
				history.append(pa.getName());
			if (i < selectedPhysicalActivities.size() - 1)
				history.append("|");
		}
		Log.h(TAG, history.toString(), Log.NO_LOG_SHOW);
	}

	private void saveTotalCaloriesBurntToday(Activity activity, ArrayList<PhysicalActivity> selectedPhysicalActivities)
			throws WOCKETSException {
		int caloriesBurnt = computeTotalCaloriesBurntToday(activity, selectedPhysicalActivities);
		SharedPreferences settings = activity.getSharedPreferences(TAG, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("CALORIES_BURNT", caloriesBurnt);
		editor.putLong("TIME_CALORIES_BURNT", Calendar.getInstance().getTimeInMillis());
		editor.commit();
	}

	public int computeTotalCaloriesBurntToday(Activity activity, ArrayList<PhysicalActivity> selectedPhysicalActivities) {
		int caloriesBurnt = 0;

		SharedPreferences prefs = activity.getSharedPreferences(TAG, 0);
		if (prefs != null) {
			int tempCaloriesBurnt = prefs.getInt("CALORIES_BURNT", 0);
			long timeCaloriesBurnt = prefs.getLong("TIME_CALORIES_BURNT", 0);
			if (timeCaloriesBurnt != 0) {
				Calendar time4oclock = LastAccessTimeKeeper.getInst().get4oClockTime();
				Calendar time = Calendar.getInstance();
				time.setTimeInMillis(timeCaloriesBurnt);
				if (time.after(time4oclock))
					caloriesBurnt = tempCaloriesBurnt;
			}
		}
		for (int i = 0; i < selectedPhysicalActivities.size(); i++) {
			// this is where formula will be put. for now we will return the
			// time spent in an activity as calories
			PhysicalActivity spa = selectedPhysicalActivities.get(i);
			caloriesBurnt += spa.getHours() * 60 + spa.getMinutes();

		}
		return caloriesBurnt;
	}

	public static PhysicalActivityStore getInst() {
		return inst;
	}
}
