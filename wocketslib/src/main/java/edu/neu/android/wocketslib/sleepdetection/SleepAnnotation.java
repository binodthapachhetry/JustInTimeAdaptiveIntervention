package edu.neu.android.wocketslib.sleepdetection;

import java.util.Date;

public class SleepAnnotation {
	Date startTime;
	Date stopTime;
	String label;
	
	
	public SleepAnnotation(Date startTime, Date endTime, String label) {
		super();
		this.startTime = startTime;
		this.stopTime = endTime;
		this.label = label;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return stopTime;
	}
	public void setEndTime(Date endTime) {
		this.stopTime = endTime;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	

}
