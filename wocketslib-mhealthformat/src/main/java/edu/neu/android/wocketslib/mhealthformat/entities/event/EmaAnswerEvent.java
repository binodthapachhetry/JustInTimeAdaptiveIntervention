package edu.neu.android.wocketslib.mhealthformat.entities.event;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by qutang on 10/13/15.
 */
public class EmaAnswerEvent extends GeneralEvent {

    private static final String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,PROMPT_INDEX,PROMPT_QUESTION_INDEX,PROMPT_ANSWER";

    public static final String EVENT_TYPE = "EmaAnswer";

    private int promptIndex;
    private int promptQuestionIndex;
    private String promptAnswer;

    public EmaAnswerEvent(String eventId, Date startTime){
        super(EVENT_TYPE, eventId + "-" + EVENT_TYPE, startTime, null, "");
    }

    public void setPromptIndex(int promptIndex) {
        this.promptIndex = promptIndex;
    }

    public void setPromptAnswer(int promptQuestionIndex, String promptAnswer) {
        this.promptQuestionIndex = promptQuestionIndex;
        this.promptAnswer = promptAnswer;
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
        return tsStr + "," + stStr + "," + etStr + "," +
                promptIndex + "," + promptQuestionIndex + "," + "\"" + promptAnswer + "\"";
    }
}
