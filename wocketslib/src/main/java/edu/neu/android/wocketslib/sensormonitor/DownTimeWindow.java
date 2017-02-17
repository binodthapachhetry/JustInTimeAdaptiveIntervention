package edu.neu.android.wocketslib.sensormonitor;

class DownTimeWindow {
	public long mstartDownTime;
	public long mendDownTime;
	DownTimeWindow(long startDownTime, long endDownTime) {
		mstartDownTime = startDownTime;
		mendDownTime = endDownTime;
	}
	
}