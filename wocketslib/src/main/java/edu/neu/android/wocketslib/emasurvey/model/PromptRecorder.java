package edu.neu.android.wocketslib.emasurvey.model;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;

public class PromptRecorder {
	private static final String TAG = "PromptRecorder";
	private static final String DIRECTORY_PATH = Globals.EXTERNAL_DIRECTORY_PATH;

	public static enum SURVEY_LOG_TYPE {
		START_QUESTION("Start Survey"), 
		PAUSE("Pause"), 
		REPROMPT("Reprompt"), 
		CONTINUE("Continue with questions"), 
		END_QUESTION("End Survey"), 
		QUESTION("Question"), 
		ANSWER("Answer"), 
		DELIBERATION("Deliberation"), 
		BACKPRESSED("BackPressed");
		
		private String type;

		SURVEY_LOG_TYPE(String itemType) {
			type = itemType;
		}

		public String getType() {
			return type;
		}
	}

	public static void writeDetailedLog(Context aContext, SURVEY_LOG_TYPE type, long startCal, long currentTimeInMillis, Object... arguments) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
        if(Globals.IS_DEMO) {
            return;
        }
		Date startDate = new Date(startCal);
		SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy-MM-dd");
		String folderPath = Globals.SURVEY_LOG_DIRECTORY + File.separator + folderFormat.format(startDate);
		File folder = new File(DIRECTORY_PATH + File.separator + folderPath);
		if(!folder.exists()) {
			folder.mkdirs();
		}

		SimpleDateFormat fileFormat = new SimpleDateFormat("HH_mm");
		File logFile = new File(folder, "Survey_" + fileFormat.format(startDate) + ".csv");

		try {
			if (!logFile.exists())
				logFile.createNewFile();

			CSVWriter writer = new CSVWriter(new FileWriter(logFile, true));
			String[] outArray = new String[arguments.length + 4];

			Date today = new Date(currentTimeInMillis);
			outArray[0] = DataStorage.GetValueString(aContext, DataStorage.KEY_SUBJECT_ID, AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED);
			outArray[1] = String.valueOf(today.getTime());
			outArray[2] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(today);
			outArray[3] = type.getType();
			for (int i = 0; i < arguments.length; i++) {
				outArray[4 + i] = arguments[i] + "";
			}

			writer.writeNext(outArray);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			android.util.Log.e("CITYLog", "Error while writing log: " + e.toString());
		}
	}

	public static String getPromptInfo(SurveyPromptEvent event) {
		if (event == null)
			return null;
		Date promptTime = new Date(event.getPromptTime());
		SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String[] header  = { "TimeStamp", "PromptType", "Schedule(If available)", "PromptAudio", "IsReprompt", "NumReprompt" };
		String[] content = { timeStampFormat.format(promptTime), event.getPromptType(), event.getPromptSchedule(), 
				event.getPromptAudio(), event.isReprompt() ? "Yes" : "No", event.getRepromptCount() + "" };
		String promptInfo = header[0] + ":" + content[0];
		for (int i = 1; i < content.length; ++i) {
			promptInfo += "<br>" + header[i] + ":" + content[i];
		}
		
		return promptInfo;
	}

	public static void writePromptLog(SurveyPromptEvent event, boolean isDismissed, boolean isResponded, boolean isCompleted) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
        if(Globals.IS_DEMO) {
            return;
        }
		Date promptTime = new Date(event.getPromptTime());
		SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy-MM-dd");
		String folderPath = Globals.SURVEY_LOG_DIRECTORY + File.separator + folderFormat.format(promptTime);
		File folder = new File(DIRECTORY_PATH + File.separator + folderPath);
		
		if(!folder.exists()) {
			folder.mkdirs();	
		}

		File logFile = new File(folder, "Prompts.csv");
		try {
			String[] header = { 
				"TimeStamp", "PromptType", "Schedule/Trigger", "PromptAudio", "IsReprompt", "NumReprompt", "Survey" 
			};
			boolean isExist = logFile.exists();
			if (!isExist)
				logFile.createNewFile();
			CSVWriter writer = new CSVWriter(new FileWriter(logFile, true));
			if (!isExist)
				writer.writeNext(header);
			
			String[] content = { 
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(promptTime), 
				event.getPromptType(),
				event.getPromptType().equals("CS") ? event.getPromptReason() : event.getPromptSchedule(), 
				event.getPromptAudio(),
				event.isReprompt() ? "Yes" : "No", 
				event.getRepromptCount() + "",
				isCompleted ? "Completed" : (isDismissed ? "Dismissed" : (isResponded ? "Not completed" : "Never started"))
			};
			writer.writeNext(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "Error IOException when writing to Prompts.csv in folder: " + folder + " " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	/*
	 * 
	 *  This is a method added to create a new structure of CSV Outputs. 
	 *  
	 *  @param aContext -> the application Context
	 *  @param started -> the time that the printed survey was started.
	 *  @param event -> the Survey event for the Survey
	 *  @param isDismissed -> a value to determine if the survey was dismissed
	 *  @param isResponded - >
	 *  @param isComplete -> determines if the survey has been completed.
	 *  @param studyName -> the name of the study.
	 *  
	 *  This CSV format is currently used by only MATCH and should only be called by MATCH code.
	 * 
	 * 
	 */
	public static void writePromptLog(Context aContext, Date started, SurveyPromptEvent event, boolean isDismissed, boolean isResponded, boolean isCompleted, boolean isPrompted, String studyName, String reason) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
        if(Globals.IS_DEMO) {
            return;
        }
		Date promptTime = new Date(event.getPromptTime());
		Date endDate = new Date();
		String endDate1;
		String promptTimeString;
		String startTimeString;
		SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy-MM-dd");
		String folderPath = Globals.SURVEY_LOG_DIRECTORY + File.separator + folderFormat.format(promptTime);
		File folder = new File(DIRECTORY_PATH + File.separator + folderPath);
		
		if(!folder.exists()) {
			folder.mkdirs();	
		}

		File logFile = new File(folder, "Prompts.csv");
		try {
			String[] header = { 
				"Subject_ID","TimeStampPrompted", "TimeStampStarted", "TimeStampCompleted", "PromptType",  "PromptAudio", "IsReprompt", "NumReprompt", "Status" , "Reason"
			};
			boolean isExist = logFile.exists();
			if (!isExist)
				logFile.createNewFile();
			CSVWriter writer = new CSVWriter(new FileWriter(logFile, true));
			if (!isExist)
				writer.writeNext(header);
			
			
			if (isCompleted) {
				startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(started);
				promptTimeString = "";
				endDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate);
			} else {
				startTimeString = "";
				promptTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(promptTime);
				endDate1 = "";
			}

			String[] content = { 
					DataStorage.GetValueString(aContext, DataStorage.KEY_SUBJECT_ID, AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED),
					promptTimeString,
					startTimeString,
					endDate1,
					event.getPromptType(),
					event.getPromptAudio(),
					event.isReprompt() ? "YES" : "NO", 
							event.getRepromptCount() + "",
							isCompleted ? "Completed" : (isDismissed ? "Dismissed" : (isResponded ? "Not completed" : isPrompted ? "Never started" : "Never Prompted")), reason
			};
			writer.writeNext(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "Error IOException when writing to Prompts.csv in folder: " + folder + " " + e.toString());
			e.printStackTrace();
		}
	}


	public static void writePromptSchedule(Context aContext, long currentTime, String promptKey, int promptsPerDay, double startTimeHour, double endTimeHour) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
        if(Globals.IS_DEMO) {
            return;
        }
		Date promptTime = new Date(currentTime);
		SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy-MM-dd");
		String folderPath = Globals.SURVEY_LOG_DIRECTORY + File.separator + folderFormat.format(promptTime);
		File folder = new File(DIRECTORY_PATH + File.separator + folderPath);
		if(!folder.exists()) {
			folder.mkdirs();	
		}

		File logFile = new File(folder, "RandomPromptSchedule.csv");
		try {
			String[] header = { "Prompt Schedule", "Actual Prompt" };
			Log.i(TAG, "LOGFILE: " + logFile.getAbsolutePath());

			boolean isExist = logFile.exists();
			if (!isExist)
				logFile.createNewFile();
			CSVWriter writer = new CSVWriter(new FileWriter(logFile, true));
			if (!isExist)
				writer.writeNext(header);

			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat scheduleFormat = new SimpleDateFormat("HH:mm");
			long[] promptTimes = DataStorage.getPromptTimesKey(aContext, promptKey);

			long startDayTime = DateHelper.getDailyTime(0, 0); // Midnight
			long totalPromptingWindowMS = (long) ((endTimeHour - startTimeHour) * 60 * 60 * 1000);
			long intervalIncMS = (long) (totalPromptingWindowMS / ((double) promptsPerDay));
			int startIntervalTimeMS = (int) (startTimeHour * 60 * 60 * 1000);

			Date startPeriod, endPeriod, prompted;
			String schedule = null;
			for (int i = 0; i < promptTimes.length; i++) {
				startPeriod = new Date(startDayTime + startIntervalTimeMS + i * intervalIncMS);
				endPeriod = new Date(startDayTime + startIntervalTimeMS + (i + 1) * intervalIncMS);
				prompted = new Date(promptTimes[i]);
				schedule = scheduleFormat.format(startPeriod) + " to " + scheduleFormat.format(endPeriod);
				String[] content = { schedule, timeFormat.format(prompted) };
				writer.writeNext(content);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "Error IOException when writing to RandomPromptSchedule.csv in folder: " + folder + " " + e.toString());
			e.printStackTrace();
		}

	}
	
	public static String writePromptResponses(Context aContext, SurveyPromptEvent event, String questionSetName, 
			boolean isDismissed, boolean isResponded, boolean isCompleted, String[] defaultIDs, SurveyQuestion[] questionaire) {
    	if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		return null;
    	}
        if(Globals.IS_DEMO) {
            return null;
        }
    	Date promptTime = new Date(event.getPromptTime());
    	SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String folderPath = Globals.SURVEY_LOG_DIRECTORY + File.separator + folderFormat.format(promptTime);
    	File folder = new File(DIRECTORY_PATH + File.separator + folderPath);
    	if(!folder.exists()) {
			folder.mkdirs();	
		}
    	
    	File logFile = new File(folder, "PromptResponses_" + questionSetName + ".csv");
		try {
			String[] header = new String[defaultIDs.length + 6 + event.getSurveySpecifiedRecord().size()];
			header[0] = "LAST_NAME";
			header[1] = "PROMPT_TYPE";
			header[2] = event.getPromptType().contains("CS") ? "TRIGGER" : "SCHEDULE(If available)";
			header[3] = "COMPLETED";
			header[4] = "PROMPT_DATE";
			header[5] = "PROMPT_TIME";
			if (event.getSurveySpecifiedRecord().keySet().size() > 0) {
				int i = 0;
				for (Iterator<String> iterator = event.getSurveySpecifiedRecord().keySet().iterator(); iterator.hasNext();) {
					header[6 + i] = (String) iterator.next();
					++i;
				}
			}
			for (int i = 0; i < defaultIDs.length; i++) {
				header[i + 6 + event.getSurveySpecifiedRecord().size()] = defaultIDs[i];
			}
			boolean isExist = logFile.exists();
			if (!isExist)
				logFile.createNewFile();
			CSVWriter writer = new CSVWriter(new FileWriter(logFile, true));
			if (!isExist)
				writer.writeNext(header);

	    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    	SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

	    	String[] responses = new String[defaultIDs.length];
	    	boolean isFound = false;
	    	int j = 0;
	    	for (int i = 0; i < questionaire.length; i++) {
	    		isFound = false;
	    		j = 0;
	    		while (j < defaultIDs.length && !isFound){
					if (questionaire[i].getAliasID().equals(defaultIDs[j])) {
                        boolean answered = false;
						for (int k = 0; k < questionaire[i].getAnswers().length; ++k) {
							SurveyAnswer answer = questionaire[i].getAnswers()[k];
							if (answer != null && answer.isSelected()) {
								if (responses[j] == null)
									responses[j] = "";
								else
									responses[j] += ",";
//								if (questionaire[i].getQuestionTYPE() == TYPE.FREE_FORM_TEXT ||
//										questionaire[i].getQuestionTYPE() == TYPE.MINUTES_PICKER ||
//										questionaire[i].getQuestionTYPE() == TYPE.NUMBER_RANGE_SELECTER ||
//										questionaire[i].getQuestionTYPE() == TYPE.TIME_PICKER)
//									responses[j] += questionaire[i].getAnswers()[k].getAnswerText();
//								else
//									responses[j] += (k + 1);
								responses[j] += responses[j].equals("") ? answer.getAnswerText() : "|" + answer.getAnswerText();
                                answered = true;
							}
						}
                        if (!answered) {
                            responses[j] = " ";
                        }
						isFound = true;
					}
					j++;
				}
			}
	    	String[] content = new String[defaultIDs.length + 6 + event.getSurveySpecifiedRecord().size()];
	    	content[0] = DataStorage.GetValueString(aContext, DataStorage.KEY_LAST_NAME, AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED);
	    	content[1] = event.getPromptType() + (event.isReprompt() ? " Reprompt" : " Prompt");
	    	content[2] = event.getPromptType().contains("CS") ? event.getPromptReason() : event.getPromptSchedule();
	    	content[3] = isCompleted ? "Completed" : (isDismissed ? "Dismissed" : (isResponded ? "Not completed" : "Never started"));
	    	content[4] = dateFormat.format(promptTime);
	    	content[5] = timeFormat.format(promptTime);
	    	for (int i = 0; i < event.getSurveySpecifiedRecord().size(); i++) {
	    		content[6 + i] = event.getSurveySpecifiedRecord().get(header[6 + i]);
			}
	    	for (int i = 0; i < defaultIDs.length; i++) {
	    		content[i + 6 + event.getSurveySpecifiedRecord().size()] = responses[i];
			}
			writer.writeNext(content);
			writer.flush();
			writer.close();
			String response = header[0] + ":" + content[0];
			for (int i = 1; i < header.length; i++) {
				if (content[i] != null)
					response += "<br>" + header[i] + ":" + content[i];
			}
			return response;
		} catch (IOException e) {
			Log.e(TAG, "Error IOException when writing to prompt responses question set: " + questionSetName + " " + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	/*
	 * 
	 * 
	 */
	public static String writePromptResponses(Context aContext, SurveyPromptEvent event, String questionSetName, 
			boolean isDismissed, boolean isResponded, boolean isCompleted, String[] defaultIDs, SurveyQuestion[] questionaire, String StudyName) {
    	if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		return null;
    	}
        if(Globals.IS_DEMO) {
            return null;
        }
    	Date promptTime = new Date(event.getPromptTime());
    	SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String folderPath = Globals.SURVEY_LOG_DIRECTORY + File.separator + folderFormat.format(promptTime);
    	File folder = new File(DIRECTORY_PATH + File.separator + folderPath);
    	if(!folder.exists()) {
			folder.mkdirs();	
		}
    	
    	File logFile = new File(folder, "PromptResponses_" + questionSetName + ".csv");
		try {
			String[] header = new String[defaultIDs.length + 6 + event.getSurveySpecifiedRecord().size()];
			header[0] = "Subject_ID";
			header[1] = "PromptID";
			header[2] = "PromptType";
			header[3] = "Status";
			header[4] = "PromptDate";
			header[5] = "PromptTime";
			if (event.getSurveySpecifiedRecord().keySet().size() > 0) {
				int i = 0;
				for (Iterator<String> iterator = event.getSurveySpecifiedRecord().keySet().iterator(); iterator.hasNext();) {
					header[6 + i] = (String) iterator.next();
					++i;
				}
			}
			for (int i = 0; i < defaultIDs.length; i++) {
				header[i + 6 + event.getSurveySpecifiedRecord().size()] = defaultIDs[i];
			}
			boolean isExist = logFile.exists();
			if (!isExist)
				logFile.createNewFile();
			CSVWriter writer = new CSVWriter(new FileWriter(logFile, true));
			if (!isExist)
				writer.writeNext(header);

	    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    	SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

	    	String[] responses = new String[defaultIDs.length];
	    	boolean isFound = false;
	    	int j = 0;
	    	for (int i = 0; i < questionaire.length; i++) {
	    		isFound = false;
	    		j = 0;
	    		while (j < defaultIDs.length && !isFound){
					if (questionaire[i].getAliasID().contains(defaultIDs[j])) {
                        boolean answered = false;
                        if(questionaire[i].getQuestionTYPE() != SurveyQuestion.TYPE.MESSAGE) {
                            for (int k = 0; k < questionaire[i].getAnswers().length; ++k) {
                                SurveyAnswer answer = questionaire[i].getAnswers()[k];
                                if (answer != null && answer.isSelected()) {
                                    if (responses[j] == null)
                                        responses[j] = "";
                                    else
                                        responses[j] += ",";
//								if (questionaire[i].getQuestionTYPE() == TYPE.FREE_FORM_TEXT ||
//										questionaire[i].getQuestionTYPE() == TYPE.MINUTES_PICKER ||
//										questionaire[i].getQuestionTYPE() == TYPE.NUMBER_RANGE_SELECTER ||
//										questionaire[i].getQuestionTYPE() == TYPE.TIME_PICKER)
//									responses[j] += questionaire[i].getAnswers()[k].getAnswerText();
//								else
//									responses[j] += (k + 1);
                                    responses[j] += responses[j].equals("") ? answer
                                            .getAnswerText() : "|" + answer.getAnswerText();
                                    answered = true;
                                }
                            }
                        }
                        if (!answered) {
                            responses[j] = " ";
                        }
						isFound = true;
					}
					j++;
				}
			}
	    	String[] content = new String[defaultIDs.length + 6 + event.getSurveySpecifiedRecord().size()];
	    	content[0] = DataStorage.GetValueString(aContext, DataStorage.KEY_SUBJECT_ID, AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED);
	    	content[1] = event.getPromptType();
	    	content[2] = (event.isReprompt() ? "Reprompt" : "Prompt");
	    	content[3] = isCompleted ? "Completed" : (isDismissed ? "Dismissed" : (isResponded ? "Not completed" : "Never started"));
	    	content[4] = dateFormat.format(promptTime);
	    	content[5] = timeFormat.format(promptTime);
	    	for (int i = 0; i < event.getSurveySpecifiedRecord().size(); i++) {
	    		content[6 + i] = event.getSurveySpecifiedRecord().get(header[6 + i]);
			}
	    	for (int i = 0; i < defaultIDs.length; i++) {
	    		content[i + 6 + event.getSurveySpecifiedRecord().size()] = responses[i];
			}
			writer.writeNext(content);
			writer.flush();
			writer.close();
			String response = header[0] + ":" + content[0];
			for (int i = 1; i < header.length; i++) {
				if (content[i] != null)
					response += "<br>" + header[i] + ":" + content[i];
			}
			return response;
		} catch (IOException e) {
			Log.e(TAG, "Error IOException when writing to prompt responses question set: " + questionSetName + " " + e.toString());
			e.printStackTrace();
		}
		return null;
	}
}
