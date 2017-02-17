package edu.neu.android.wocketslib.sensormonitor;

import android.hardware.SensorEvent;

public interface ArbitraterInterface {

	public void doArbitrate(boolean isNewSoftwareVersion);

	public void doWakefulArbitrate();

	public void doOnSensorChangedArbitrate(SensorEvent event, Object extraData);

}