package edu.neu.android.wocketslib.mhealthformat.entities.event;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by qutang on 10/13/15.
 */
public class EmaPromptEvent extends GeneralEvent {

    private static final String header = "HEADER_TIME_STAMP,START_TIME,STOP_TIME,PROMPT_INDEX,PROMPT_COUNT,PROMPT_TYPE,PROMPT_REMINDER,PROMPT_QUESTION_INDEX,PROMPT_QUESTION";

    public static final String EVENT_TYPE = "EmaPrompt";

    private int promptIndex;
    private int promptCount;
    private String promptType;
    private String promptReminder;
    private int promptQuestionIndex;
    private String promptQuestion;


    public EmaPromptEvent(String eventId, Date startDate){
        super(EVENT_TYPE, eventId + "-" + EVENT_TYPE, startDate, null,"");
    }

    public void setPromptIndex(int promptIndex) {
        this.promptIndex = promptIndex;
    }

    public void setPromptCount(int promptCount) {
        this.promptCount = promptCount;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

    public void setPromptReminder(String promptReminder) {
        this.promptReminder = promptReminder;
    }

    public void setPromptQuestion(int promptQuestionIndex, String promptQuestion) {
        this.promptQuestionIndex = promptQuestionIndex;
        this.promptQuestion = promptQuestion;
    }

    public void setDismissTime(Date dismissTime){
        this.endTime = dismissTime;
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
                promptIndex + "," + promptCount + "," + promptType + "," +
                promptReminder + "," + promptQuestionIndex + "," + "\"" + promptQuestion + "\"";
    }
}
