package edu.neu.android.wocketslib.mhealthformat.entities.event;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.entities.mHealthEntity;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.mhealthformat.utils.PhoneInfo;

/**
 * Created by qutang on 5/13/15.
 */
public class GeneralEvent extends mHealthEntity {

    private static final String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,DETAILS";
    protected String eventType;
    protected String eventId;

    protected Date startTime;
    protected Date endTime;
    private String details;
    protected long timestamp;

    public GeneralEvent(String eventType, String eventId, Date startTime, Date endTime, String details){
        super();
        this.eventType = eventType;
        this.eventId = eventId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
        section1 = eventType;
        section2 = eventId;
    }

    public GeneralEvent() {
        super();
    }

    @Override
    public mHealthFormat.MHEALTH_FILE_TYPE getEntityType() {
        return mHealthFormat.MHEALTH_FILE_TYPE.EVENT;
    }

    @Override
    public String tomHealthRow() {
        Date ts = new Date(timestamp);
        String tsStr = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(ts);
        String stStr = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(startTime);
        String etStr;
        if(endTime != null) {
            etStr = new SimpleDateFormat(mHealthFormat.mHealthTimestampFormat).format(endTime);
        }else{
            etStr = "";
        }
        return tsStr + "," + stStr + "," + etStr + "," + details;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public byte[] encodeRowAsBinary() {
        return new byte[0];
    }
}
