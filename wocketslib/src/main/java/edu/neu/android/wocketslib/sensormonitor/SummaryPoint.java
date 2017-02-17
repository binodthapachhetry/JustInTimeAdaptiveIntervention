/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Class used to store a single decoded activity summary point of data 
 * from an accelerometer sensor.  The accelerometer sensor creates these summaries
 * approximately once a minute that summarize the previous minutes activity levels
 * 
 * 
 *****************************************************************************/


package edu.neu.android.wocketslib.sensormonitor;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class SummaryPoint implements Comparable<SummaryPoint> {
	
	public static final int UNDEFINED_SEQ_NUMBER = -1; 

	//The activity level value for this summary
	@SerializedName("ac")
	public int mActivityCount;

	//The original sequence number of this summary
	@SerializedName("sn")
	public int mSeqNum;

	//The minute of the day represented by the SummaryPoint starting at midnight, where there are (24*60) in a day
	@SerializedName("dsn")
	public int mDaySeqNum;
	
	//The time the phone received the data 
	@SerializedName("prt")
	public Date mPhoneReadTime;

	//The actual time the AC was computed for. This must be figured out after the fact. When this is null, the time is still unknown
	@SerializedName("at")
	public Date mActualTime;

	//Flag indicating if this point has already been stored to NV storage.
	//True indicates it has and does not need to be written again, false means it hasn't
	@SerializedName("ww")
	public boolean mWritten;

	//Flag indicating if this point has been sent to the server yet. 
	//True indicates it has and does not need to be sent again, false means it hasn't	
	@SerializedName("mj")
	public boolean mJsonQueued;

	public SummaryPoint()
	{
		this(0, UNDEFINED_SEQ_NUMBER, false);
	}
	
	/**
	 * 
	 * @param seqNum - the value indicating the order of this activity summary
	 * @param value - the actual value of the activity summary
	 */
	public SummaryPoint(int seqNum, int value, boolean isSetActualTime)
	{
		mActivityCount = value;
		mSeqNum = seqNum;
		mDaySeqNum = UNDEFINED_SEQ_NUMBER; 				
		mPhoneReadTime = new Date();
		
		if (isSetActualTime)
			mActualTime = new Date();
		else
			mActualTime = null; 
		
		mWritten = false;
		mJsonQueued = false; 		
	}

	@Override
	public int compareTo(SummaryPoint another) {
		if (this.mSeqNum < another.mSeqNum)
			return -1;
		else if (this.mSeqNum > another.mSeqNum)
			return 1;
		else 
			return 0;
	}
}
