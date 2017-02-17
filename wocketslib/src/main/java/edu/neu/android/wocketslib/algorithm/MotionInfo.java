package edu.neu.android.wocketslib.algorithm;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * The motion info used to hold the resulting information from the motion detection.
 * </p>
 * 
 * @author bigbug
 *
 */
public class MotionInfo implements Serializable {
	
	private static final long serialVersionUID = -987466237866182313L;
	
	// Motion codes
	public final static int ERROR	    = 0; // "error";
	public final static int NO_INTEREST = 1; // "normal";
	public final static int NO_DATA     = 2; // "10minNoData";
	public final static int LOW_MOTION  = 3; // "60minLowActivity";
	public final static int HIGH_MOTION = 4; // "30minActivityThen10minLowActivity";
	
	private int	   mCode;
	private Date   mStartTime;
	private Date   mStopTime;	
	private Object mExtra;
	
	public MotionInfo() {}
	
	public MotionInfo(int motionCode, Date startTime, Date stopTime) {
		mCode      = motionCode;
		mStartTime = startTime;
		mStopTime  = stopTime;				
		mExtra     = null;
	}
	
	public MotionInfo(int motionCode, Date startTime, Date stopTime, Object extra) {
		mCode      = motionCode;
		mStartTime = startTime;
		mStopTime  = stopTime;		
		mExtra     = extra;
	}
	
	public int getMotionCode() {
		return mCode;
	}
	
	public Object getExtra() {
		return mExtra;
	}
	
	public String getDetail() {
		return getDetailFromMotionCode(mCode);
	}
	
	public long getStartTimeInMS() {
		return mStartTime.getTime();
	}
	
	public long getStopTimeInMS() {
		return mStopTime.getTime();
	}
	
	public String getStartTime() {
		if (mStartTime == null) {
			return "undefined";
		}
		int hour   = mStartTime.getHours();
		int minute = mStartTime.getMinutes();
		String postfix = hour >= 12 ? " PM" : " AM";
		hour = hour > 12 ? hour - 12 : hour;
		String time = hour + ":" + (minute < 10 ? "0" + minute : minute) + postfix;
		return time;
	}
	
	public String getStopTime() {
		if (mStopTime == null) {
			return "undefined";
		}
		int hour   = mStopTime.getHours();
		int minute = mStopTime.getMinutes();
		String postfix = hour >= 12 ? " PM" : " AM";
		hour = hour > 12 ? hour - 12 : hour;
		String time = hour + ":" + (minute < 10 ? "0" + minute : minute) + postfix;
		return time;
	}
	
	/**
	 * Override this method to modify the detailed text according to the program.
	 * 
	 * @param motionCode	
	 * 			  The motion code representing the result of the motion detection
	 * 			  from MotionDetectAlgorithm. 
	 * 	
	 * @return A string giving the detailed information about the input motion code.
	 */
	protected String getDetailFromMotionCode(int motionCode) {
		String detail = "unknown";
		
		switch (motionCode) {
		case ERROR:
			detail = "error";
			break;
		case NO_INTEREST:
			detail = "normal";
			break;
		case NO_DATA:
			detail = "10minNoData";
			break;
		case LOW_MOTION:
			detail = "60minLowActivity";
			break;
		case HIGH_MOTION:
			detail = "30minActivityThen10minLowActivity";
			break;
		default:	
			break;
		}
		
		return detail;
	}
}
