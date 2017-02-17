package edu.neu.android.wocketslib.sensormonitor;

public class BLEOUSData {
	private String time;
	private String moveCount;
	private String intensity;
	private int count;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getMoveCount() {
		return moveCount;
	}
	public void setMoveCount(String moveCount) {
		this.moveCount = moveCount;
	}
	public String getIntensity() {
		return intensity;
	}
	public void setIntensity(String intensity) {
		this.intensity = intensity;
	}
}
