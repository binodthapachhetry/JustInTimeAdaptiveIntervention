package edu.neu.android.wocketslib;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * The base class used by the application. This can be used to keep track of
 * Activities being used and to kill all of them at once.
 * 
 * @author Intille
 * 
 */
public class ApplicationManager extends Application {
	public static final String TAG = "ApplicationManager";

	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		ApplicationManager.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return context;
	}

	private ArrayList<Activity> activityList = new ArrayList<Activity>();

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public void killActivity(Activity activity) {
		activity.finish();
		activityList.remove(activity);
	}

	public void removeActivity(Activity activity) {
		activityList.remove(activity);
	}

	public void killAllActivities() {
		for (int i = 0; i < activityList.size(); i++)
			activityList.get(i).finish();
	}
}
