package edu.neu.android.wocketslib.mhealthformat.entities.event;

import android.net.Network;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by qutang on 9/22/15.
 */
public class NetworkEvent extends GeneralEvent {
    private static final String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,SWITCH_TO_NETWORK,DATA_USAGE_APP,DATA_USAGE_TOTAL";

    public static final String EVENT_TYPE = "Network";
    private String networkType;
    private int dataUsageApp;
    private int dataUsageTotal;

    public NetworkEvent(String deviceId, Date startTime, Date endTime, String networkType, int dataUsageApp, int dataUsageTotal) {
        super(EVENT_TYPE, deviceId + "-" + EVENT_TYPE, startTime, endTime, "");
        this.networkType = networkType;
        this.dataUsageApp = dataUsageApp;
        this.dataUsageTotal = dataUsageTotal;
    }

    public NetworkEvent(String deviceId, Date startTime, Date endTime){
        super(EVENT_TYPE, deviceId + "-" + EVENT_TYPE, startTime, endTime, "");
    }

    public void setNetworkType(String networkType){
        this.networkType = networkType;
    }

    public void setDataUsageApp(int dataUsageApp){
        this.dataUsageApp = dataUsageApp;
    }

    public void setDataUsageTotal(int dataUsageTotal){
        this.dataUsageTotal = dataUsageTotal;
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
        return tsStr + "," + stStr + "," + etStr + "," + networkType + "," + dataUsageApp + "," + dataUsageTotal;
    }
}
