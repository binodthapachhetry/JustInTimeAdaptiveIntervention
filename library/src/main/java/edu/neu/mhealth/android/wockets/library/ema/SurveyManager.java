package edu.neu.mhealth.android.wockets.library.ema;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.WocketsConstants;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Answer;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Question;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Survey;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.FileUtils;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.WocketsUtil;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

/**
 * @author Dharam Maniar
 */
public class SurveyManager {

	private static final String TAG = "SurveyManager";

    public enum Status {
        COMPLETED("Completed"),
        INCOMPLETE("Incomplete"),
        NEVER_STARTED("Never Started"),
        NEVER_PROMPTED("Never Prompted");

        private String id;

        Status(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

	public static Survey getSelectedSurvey(Context context) {
		Log.d(TAG,"inside getSelectedSurvey");
		Study study = DataManager.getStudy(context);
		if (study == null) {
			Log.d(TAG,"Study is null");
			return null;
		}
		Survey survey = null;
		for(Survey individualSurvey : study.surveys) {
			Log.d(TAG,"Survey name:"+individualSurvey.surveyName);
			if (DataManager.getSelectedSurveyName(context).contains(individualSurvey.surveyName)) {
				survey = individualSurvey;
			}
		}
		// If no selected survey, set the first survey as default
		if (survey == null) {
			Log.d(TAG,"Survey is null");
			survey = study.surveys.get(0);
			DataManager.setSelectedSurveyName(context, survey.surveyName);
		}
		return survey;
	}

	public static List<Survey> getSelectedSurveys(Context context){
		Log.d(TAG,"inside getSelectedSurveys");
		Study study = DataManager.getStudy(context);
		List<Survey> survey = new ArrayList<>();
		if (study == null) {
			Log.d(TAG,"Study is null");
			return survey;
		}
		Log.d(TAG,"Size of survey:" + Integer.toString(study.surveys.size()));
//		for(Survey individualSurvey : study.surveys) {
//			Log.d(TAG,"Survey name:" + individualSurvey.surveyName);
//			if (DataManager.getSelectedSurveyName(context).contains(individualSurvey.surveyName)) {
//				survey.add(individualSurvey);
//			}
//		}
		// If no selected survey, set the first survey as default
		if (survey.isEmpty()) {
			Log.d(TAG,"Survey is empty");
			for(Survey individualSurvey : study.surveys) {
				survey.add(individualSurvey);
			}

//			survey.add(study.surveys.get(0));
//			DataManager.setSelectedSurveyName(context, survey.get(0).surveyName);
//			survey.add(study.surveys.get(1));
//			DataManager.setSelectedSurveyName(context, survey.get(1).surveyName);


		}
		for(Survey indSur: survey){
			Log.d(TAG,"Passing survey to survey manager service: " +indSur.surveyName);
		}
		return survey;
	}

    public static void writeSurveyStart(Context context, String promptKey) {
        String surveyDirectory = DataManager.getDirectorySurveys(context);
        String surveyFile = surveyDirectory + "/" + DateTime.getDate() + "/Survey_"+ promptKey +".csv";

        List<String> surveyEntry = new ArrayList<>();
        surveyEntry.add(String.valueOf(DateTime.getCurrentTimeInMillis()));
        surveyEntry.add(DateTime.getCurrentTimestampString());
        surveyEntry.add(UserManager.getUserEmail());
        surveyEntry.add("Start Survey");
        surveyEntry.add(DataManager.getSelectedSurveyName(context));
        surveyEntry.add(promptKey);

        CSV.write(WocketsUtil.listOfStringsToStringArray(surveyEntry), surveyFile, true);
    }

    public static void writeSurveyReprompt(Context context, String promptKey) {
        String surveyDirectory = DataManager.getDirectorySurveys(context);
        String surveyFile = surveyDirectory + "/" + DateTime.getDate() + "/Survey_"+ promptKey +".csv";

        List<String> surveyEntry = new ArrayList<>();
        surveyEntry.add(String.valueOf(DateTime.getCurrentTimeInMillis()));
        surveyEntry.add(DateTime.getCurrentTimestampString());
        surveyEntry.add(UserManager.getUserEmail());
        surveyEntry.add("Reprompt");
        surveyEntry.add(DataManager.getSelectedSurveyName(context));

        CSV.write(WocketsUtil.listOfStringsToStringArray(surveyEntry), surveyFile, true);
    }

    public static void writeSurveyQuestion(Context context, String promptKey, String questionKey) {
        String surveyDirectory = DataManager.getDirectorySurveys(context);
        String surveyFile = surveyDirectory + "/" + DateTime.getDate() + "/Survey_"+ promptKey +".csv";

        List<String> surveyEntry = new ArrayList<>();
        surveyEntry.add(String.valueOf(DateTime.getCurrentTimeInMillis()));
        surveyEntry.add(DateTime.getCurrentTimestampString());
        surveyEntry.add(UserManager.getUserEmail());
        surveyEntry.add("Question");
        surveyEntry.add(questionKey);

        CSV.write(WocketsUtil.listOfStringsToStringArray(surveyEntry), surveyFile, true);
    }

    public static void writeSurveyAnswer(Context context, String promptKey, String answerKey) {
        String surveyDirectory = DataManager.getDirectorySurveys(context);
        String surveyFile = surveyDirectory + "/" + DateTime.getDate() + "/Survey_"+ promptKey +".csv";

        List<String> surveyEntry = new ArrayList<>();
        surveyEntry.add(String.valueOf(DateTime.getCurrentTimeInMillis()));
        surveyEntry.add(DateTime.getCurrentTimestampString());
        surveyEntry.add(UserManager.getUserEmail());
        surveyEntry.add("Answer");
        surveyEntry.add(answerKey);

        CSV.write(WocketsUtil.listOfStringsToStringArray(surveyEntry), surveyFile, true);
    }

    public static void writeSurveyEnd(Context context, String promptKey) {
        String surveyDirectory = DataManager.getDirectorySurveys(context);
        String surveyFile = surveyDirectory + "/" + DateTime.getDate() + "/Survey_"+ promptKey +".csv";

        List<String> surveyEntry = new ArrayList<>();
        surveyEntry.add(String.valueOf(DateTime.getCurrentTimeInMillis()));
        surveyEntry.add(DateTime.getCurrentTimestampString());
        surveyEntry.add(UserManager.getUserEmail());
        surveyEntry.add("End Survey");
        surveyEntry.add(DataManager.getSelectedSurveyName(context));
        surveyEntry.add(promptKey);

        CSV.writeAndZip(WocketsUtil.listOfStringsToStringArray(surveyEntry), surveyFile, true, context);
    }

	public static void writePrompt(Context context,
	                               String promptKey,
	                               long promptTime,
	                               long startTime,
	                               long completeTime,
	                               String audio,
	                               boolean isReprompt,
	                               int numReprompt,
	                               Status status,
	                               String reason) {
		String surveyDirectory = DataManager.getDirectorySurveys(context);
		String promptsFile = surveyDirectory + "/" + DateTime.getDate() + "/Prompts.csv";
		String promptsFileZip = surveyDirectory + "/" + DateTime.getDate() + "/Prompts.csv.zip";
		if (!FileUtils.ifExists(promptsFile) && !FileUtils.ifExists(promptsFileZip)) {
			List<String> headers = new ArrayList<>();
			headers.add("Subject_ID");
			headers.add("TimeStampPrompted");
			headers.add("TimeStampStarted");
			headers.add("TimeStampCompleted");
			headers.add("PromptType");
			headers.add("PromptAudio");
			headers.add("IsReprompt");
			headers.add("NumReprompt");
			headers.add("Status");
			headers.add("Reason");

			CSV.writeAndZip(WocketsUtil.listOfStringsToStringArray(headers), promptsFile, true, context);
		}

		List<String> prompt = new ArrayList<>();
		prompt.add(UserManager.getUserEmail());
		prompt.add(DateTime.getTimestampString(promptTime));
		prompt.add(DateTime.getTimestampString(startTime));
		prompt.add(DateTime.getTimestampString(completeTime));
		prompt.add(promptKey);
		prompt.add(audio);
		prompt.add(String.valueOf(isReprompt));
		prompt.add(String.valueOf(numReprompt));
		prompt.add(status.id());
		prompt.add(reason);

		CSV.writeAndZip(WocketsUtil.listOfStringsToStringArray(prompt), promptsFile, true, context);
	}

	public static void writePromptResponses(Context context,
	                                        Survey survey,
	                                        HashMap<Question, String> questionAnswerMap,
	                                        String promptKey,
	                                        boolean isReprompt,
	                                        boolean isSurveyComplete,
	                                        long promptTime) {
		if (survey == null) {
			return;
		}

		String surveyDirectory = DataManager.getDirectorySurveys(context);
		String promptResponsesFile = surveyDirectory + "/" + DateTime.getDate() + "/PromptResponses_" + survey.surveyName + ".csv";
        String promptResponsesFileZip = surveyDirectory + "/" + DateTime.getDate() + "/PromptResponses_" + survey.surveyName + ".csv.zip";
		if (!FileUtils.ifExists(promptResponsesFile) && !FileUtils.ifExists(promptResponsesFileZip)) {
			List<String> headers = new ArrayList<>();
			headers.add("Subject_ID");
			headers.add("PromptID");
			headers.add("PromptType");
			headers.add("Status");
			headers.add("PromptDate");
			headers.add("PromptTime");

			for(Question question : survey.questions) {
				headers.add(question.key);
			}

			CSV.writeAndZip(WocketsUtil.listOfStringsToStringArray(headers), promptResponsesFile, true, context);
		}

		List<String> promptResponse = new ArrayList<>();
		promptResponse.add(UserManager.getUserEmail());
		promptResponse.add(promptKey);
		if (!isReprompt) {
			promptResponse.add("Prompt");
		} else {
			promptResponse.add("Reprompt");
		}
		if (isSurveyComplete) {
			promptResponse.add("Completed");
		} else if (questionAnswerMap.size() > 0) {
			promptResponse.add("Incomplete");
		} else {
			promptResponse.add("Never Started");
		}
		promptResponse.add(DateTime.getDate());
		promptResponse.add(DateTime.getTimestampString(promptTime));
		for (Question question : survey.questions) {
			if (!questionAnswerMap.containsKey(question)) {
				promptResponse.add("");
			} else {
				String answer = questionAnswerMap.get(question);
				switch (question.type) {
					case WocketsConstants.EMA_QUESTION_TYPE_MESSAGE:
						promptResponse.add("");
						break;
					case WocketsConstants.EMA_QUESTION_TYPE_SINGLE_CHOICE:
						for (Answer promptAnswer : question.answers) {
							if (promptAnswer.key.equals(answer)) {
								promptResponse.add(promptAnswer.text.english);
								break;
							}
						}
						break;
					case WocketsConstants.EMA_QUESTION_TYPE_MULTI_CHOICE:
						String selectedAnswers = "";
						for (Answer promptAnswer : question.answers) {
							if (answer.contains(promptAnswer.key)) {
								if (selectedAnswers.equals("")) {
									selectedAnswers = promptAnswer.text.english;
								} else {
									selectedAnswers = selectedAnswers + ",|" + promptAnswer.text.english;
								}
							}
						}
						promptResponse.add(selectedAnswers);
						break;
					case WocketsConstants.EMA_QUESTION_TYPE_TIME_PICKER:
						promptResponse.add(answer);
						break;
					case WocketsConstants.EMA_QUESTION_TYPE_NUMBER_PICKER:
						promptResponse.add(answer);
						break;
					case WocketsConstants.EMA_QUESTION_TYPE_TEXT_INPUT:
						promptResponse.add(answer);
						break;
					default:
						Log.e(TAG, "Unknown question type. This should never happen.", context);
						break;
				}
			}
		}

		DatabaseManager.writeNote(context, DatabaseManager.SURVEY_COMPLETE, promptResponse);

		CSV.writeAndZip(WocketsUtil.listOfStringsToStringArray(promptResponse), promptResponsesFile, true, context);
	}
}
