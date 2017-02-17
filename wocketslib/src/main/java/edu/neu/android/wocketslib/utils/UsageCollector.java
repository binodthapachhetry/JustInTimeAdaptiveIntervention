package edu.neu.android.wocketslib.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class UsageCollector {
	private static final String TAG = "UsageCollector";
	
	private static UsageCollector inst = new UsageCollector();

	private HashMap<String, Usage> usageList = new HashMap<String, Usage>();
	private Calendar appStartTime;
	private Calendar appEndTime;
	private String appName;
	private String reasonForStop;

	public void appStarted(String appName) {
		appStartTime = Calendar.getInstance();
		this.appName = appName;
	}

	// reasonForStop should have a value when app is stopped abnormally. It
	// could be null for normal exit
	public void appStopped(String reasonForStop) {
		appEndTime = Calendar.getInstance();
		this.reasonForStop = reasonForStop;
		save();
	}

	public void activityResumed(String activityName) {
		if (usageList.get(activityName) == null)
			usageList.put(activityName, new Usage(activityName));
		usageList.get(activityName).setStartTime(Calendar.getInstance());
	}

	public void activityPaused(String activityName) {
		if (usageList.get(activityName) != null)
			usageList.get(activityName).setEndTime(Calendar.getInstance());
	}

	private void save() {
		StringBuffer buff = new StringBuffer();
		buff.append(appName + " started at "
				+ DateHelper.extractDateTime(appStartTime)
				+ " and finished at " + DateHelper.extractDateTime(appEndTime));
		double duration = (double) Math
				.round((appEndTime.getTimeInMillis() - appStartTime
						.getTimeInMillis()) / 1000.0);
		if (this.reasonForStop == null)
			buff.append(". App finished normally,");
		else
			buff.append(". App finished abnormally. " + reasonForStop + ",");
		Iterator<String> keys = usageList.keySet().iterator();
		while (keys.hasNext()) {
			Usage usage = usageList.get(keys.next());
			buff.append(usage + "\t");
			buff.append(usage + ",");
		}
		AppUsageLogger.logTimeUsage(TAG, (int) duration);
		AppUsageLogger.logTimeUsageString(TAG, buff.toString());
		usageList.clear();
	}

	public static UsageCollector getInst() {
		return inst;
	}
}

class Usage {
	private String activityName;
	private Calendar startTime;

	private int count;
	private int totalTime;

	Usage(String activityName) {
		this.activityName = activityName;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Calendar endTime) {
		endTime = Calendar.getInstance();
		totalTime = (int) (endTime.getTimeInMillis() - startTime
				.getTimeInMillis());
		count++;
	}

	public String toString() {
		return activityName + "," + count + "," + totalTime;
	}
}
