package edu.neu.mhealth.android.wockets.library.managers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/**
 * @author Dharam Maniar
 */
public class JobManager {

	private static final String TAG = "JobManager";

	public static void scheduleRepeatingJob(Context context,int jobId, long intervalMillis, ComponentName componentName) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			return;
		}

		JobInfo.Builder builder = new JobInfo.Builder(jobId, componentName);
		builder.setPeriodic(intervalMillis);
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		jobScheduler.schedule(builder.build());
	}

	public static void cancelAllJobs(Context context) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			return;
		}

		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		jobScheduler.cancelAll();
	}
}
