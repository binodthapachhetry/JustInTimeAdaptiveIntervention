package edu.neu.android.wocketslib.mhealthformat.entities.event;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;

/**
 * Created by qutang on 5/22/15.
 */
public class PhoneCallEvent extends GeneralEvent {
    private static final String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,CALL_INCOMMING_OR_OUTGOING, PHONE_NUMBER";
    public static final String EVENT_TYPE = "PhoneCalls";

    private boolean incomming;
    private String phoneNumber;

    public PhoneCallEvent(String deviceId, Date startTime, Date endTime, boolean incomming, String phoneNumber) {
        super(EVENT_TYPE, deviceId + "-" + EVENT_TYPE, startTime, endTime, "");
        this.incomming = incomming;
        this.phoneNumber = phoneNumber;
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
        String direction = incomming? "In" : "Out";
        return tsStr + "," + stStr + "," + etStr + "," + direction + "," + phoneNumber;
    }
}
