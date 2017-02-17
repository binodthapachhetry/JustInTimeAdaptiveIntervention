package edu.neu.android.wocketslib.emasurvey.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import edu.neu.android.wocketslib.utils.DateHelper;

public class SurveyPromptEvent implements Serializable {
	private static final String NO_SCHEDULE = "N/A";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long speID;
	private long scheduledPromptTime;
	private long promptTime;
	private long repromptCount;
	private String promptType;
	private String surveyName;
	private String promptAudio;
	private String promptSchedule;
	private String promptReason;
	private long answeredTime;
	private LinkedHashMap<String, String> surveySpecifiedRecord;
	
	public static enum PROMPT_AUDIO{
		AUDIO("Audio"),
		VIBRATION("Vibration"),
		NONE("None(sound off)");
		String name;

		PROMPT_AUDIO(String name) {
			this.name = name;
		}
	}
	
	public long getID() {
		return speID;
	}

	public String getPromptSchedule() {
		return promptSchedule;
	}

	public void setPromptSchedule(long promptTime, int promptsPerDay, int startIntervalTimeMS, long intervalIncMS) {
		SimpleDateFormat scheduleFormat = new SimpleDateFormat("HH:mm");
		long startDayTime = DateHelper.getDailyTime(0, 0);
		for (int i = 0; i < promptsPerDay; i++) {
			if (promptTime <= (startDayTime + startIntervalTimeMS + (i + 1) * intervalIncMS)) {
				Date startPeriod = new Date(startDayTime + startIntervalTimeMS + i * intervalIncMS);
				Date endPeriod = new Date(startDayTime + startIntervalTimeMS + (i + 1) * intervalIncMS);
				this.promptSchedule = scheduleFormat.format(startPeriod) + " to " + scheduleFormat.format(endPeriod);
				break;
			}
		}
	}

	public long getPromptTime() {
		return promptTime;
	}

	public void setPromptTime(long promptTime) {
		this.promptTime = promptTime;
	}

	public String getPromptType() {
		return promptType;
	}

	public void setPromptType(String promptType) {
		this.promptType = promptType;
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	
	public String getPromptReason() {
		return promptReason;
	}
	
	public void setPromptReason(String promptReason) {
		this.promptReason = promptReason;
	}

	public boolean isReprompt() {
		return repromptCount > 0;
	}

	public String getPromptAudio() {
		return promptAudio;
	}

	public void setPromptAudio(PROMPT_AUDIO promptAudio) {
		this.promptAudio = promptAudio.name;
	}

	public long getAnsweredTime() {
		return answeredTime;
	}

	public void setAnsweredTime(long answeredTime) {
		this.answeredTime = answeredTime;
	}

	public long getScheduledPromptTime() {
		return scheduledPromptTime;
	}

	public void setScheduledPromptTime(long scheduledPromptTime) {
		this.scheduledPromptTime = scheduledPromptTime;
	}
	
	public long getRepromptCount() {
		return repromptCount;
	}
	
	public void setRepromptCount(long repromptCount) {
		this.repromptCount = repromptCount;  
	}

	public HashMap<String, String> getSurveySpecifiedRecord() {
		return surveySpecifiedRecord;
	}

	public void AddSurveySpecifiedRecord(String key, String value) {
		this.surveySpecifiedRecord.put(key, value);
	}

	public SurveyPromptEvent() { // used for converting from JSON only 
		super();		
	}
	
	public SurveyPromptEvent(long scheduledPromptTime) {
		this(scheduledPromptTime, 0);
	}
	
	public SurveyPromptEvent(long scheduledPromptTime, long promptTime) {
		super();
		this.speID = new Random().nextLong();
		this.scheduledPromptTime = scheduledPromptTime;
		this.promptTime = promptTime;
		this.promptSchedule = NO_SCHEDULE;
		this.repromptCount = 0;
		this.promptReason = "";
		this.surveySpecifiedRecord = new LinkedHashMap<String, String>();
	}

}
