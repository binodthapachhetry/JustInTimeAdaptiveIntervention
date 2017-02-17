package edu.neu.android.wocketslib.utils;

import android.view.Gravity;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;

public class IdleTimeKeeper {
	private static final String TAG = "IdleTimeKeeper";
	
	private static IdleTimeKeeper inst = new IdleTimeKeeper();
	// Idle time in secs. Application will be killed if application is idle for more than 60 seconds
	private int maxIdleTime = 60;
	private long maxIdleTimeMS = maxIdleTime * 1000; 
	private ApplicationManager appMgr;

	private IdleTimeKeeper() {
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
		maxIdleTimeMS = maxIdleTime * 1000; 
	}

	public void init(ApplicationManager appMgr, int maxIdleTime) {
		this.appMgr = appMgr;
		setMaxIdleTime(maxIdleTime);
	}
	
//	private void beep()
//	{
//		MediaPlayer mp = new MediaPlayer();
//			    try {
//			    	// http://www.soundjay.com/beep-sounds-1.html lots of free beeps here
//			    	mp = MediaPlayer.create(appMgr.getAppContext(), R.raw.beep);
//			    	mp.setLooping(false);
//			    	mp.start();
//			    }
//			    catch (Exception e) {
//			    Log.e("beep", "error: " + e.getMessage(), e);
//			    }
//		 try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			Log.e(TAG, "Error InterruptedException in IdleTimeKeeper: " + e.toString());
//			e.printStackTrace();
//		}
//		 mp.release(); 
//	}

	private long startTimerTime = 0; 
	public void startTimer() {
		startTimerTime = System.currentTimeMillis();
	}

	public void checkTimer()
	{	
		if (startTimerTime == 0)
			return; 
		
		if ((System.currentTimeMillis()-startTimerTime) > maxIdleTimeMS)
		{			
//			beep(); 
			AppUsageLogger.logTimeOut(TAG, maxIdleTime);
			appMgr.killAllActivities();
			startTimerTime = 0; 

			String msg = "After a prompt, you must complete the survey within " + ((int) Math.round((maxIdleTimeMS/1000.0/60.0))) + " minutes! Please try to respond next time.";
			Toast aToast = Toast.makeText(appMgr.getAppContext(), msg, Toast.LENGTH_LONG);
			aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			aToast.show();								
		
		}
		else{
			
			// If less than 30 seconds to go, print warning...
			if ((maxIdleTimeMS - ((System.currentTimeMillis() - startTimerTime))) < 60000)
			{				
				String msg = "Quick! You only have " + ((int) Math.round((maxIdleTimeMS - ((System.currentTimeMillis() - startTimerTime)))/1000.0)) + " seconds to finish the survey.";
				Toast aToast = Toast.makeText(appMgr.getAppContext(), msg, Toast.LENGTH_SHORT);
				aToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				aToast.show();								
			}
		}
	}

	public void stopTimer() {
		startTimerTime = 0; 
	}

	public void restartTimer() {
		startTimerTime = System.currentTimeMillis(); 
	}

	public static IdleTimeKeeper getInst() {
		return inst;
	}
}
