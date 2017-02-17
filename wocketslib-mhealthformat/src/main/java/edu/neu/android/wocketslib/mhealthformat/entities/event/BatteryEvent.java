package edu.neu.android.wocketslib.mhealthformat.entities.event;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;

/**
 * Created by qutang on 9/21/15.
 */
public class BatteryEvent extends GeneralEvent {
    private static final String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,BATTERY_LEVEL,BATTERY_CHARGING";

    public static final String EVENT_TYPE = "Battery";
    private boolean batteryCharging;
    private int batteryLevel;

    public BatteryEvent(String deviceId, Date startTime, Date endTime, boolean batteryCharging, int batteryLevel) {
        super(EVENT_TYPE, deviceId + "-" + EVENT_TYPE, startTime, endTime, "");
        this.batteryLevel = batteryLevel;
        this.batteryCharging = batteryCharging;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String tomHealthRow() {
        Date ts = new Date(timestamp);
        String tsStr = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(ts);
        String stStr = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(startTime);
        String etStr = "";
        if(endTime != null){
            etStr = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(endTime);
        }
        String charging = batteryCharging ? "YES" : "NO";
        return tsStr + "," + stStr + "," + etStr + "," + batteryLevel + "," + charging;
    }
}
