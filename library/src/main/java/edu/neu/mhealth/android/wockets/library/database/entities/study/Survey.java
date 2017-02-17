package edu.neu.mhealth.android.wockets.library.database.entities.study;

import java.util.List;

/**
 * @author Dharam Maniar
 */
public class Survey {

	public String surveyName;

	public List<Question> questions;

	public Integer timeLengthMins;
	public List<Integer> repromptAtMins;

	public Schedule schedule;

	public Survey() {
	}

	public Survey(
			String surveyName,
			List<Question> questions,
			Integer timeLengthMins,
			List<Integer> repromptAtMins,
	        Schedule schedule
	) {
		this.surveyName = surveyName;
		this.questions = questions;
		this.timeLengthMins = timeLengthMins;
		this.repromptAtMins = repromptAtMins;
		this.schedule = schedule;
	}

	public Survey setSurveyName(String surveyName) {
		this.surveyName = surveyName;
		return this;
	}

	public Survey setQuestions(List<Question> questions) {
		this.questions = questions;
		return this;
	}

	public Survey setTimeLengthMins(Integer timeLengthMins) {
		this.timeLengthMins = timeLengthMins;
		return this;
	}

	public Survey setRepromptAtMins(List<Integer> repromptAtMins) {
		this.repromptAtMins = repromptAtMins;
		return this;
	}

	public Survey setSchedule(Schedule schedule) {
		this.schedule = schedule;
		return this;
	}

	public Survey createSurvey() {
		return new Survey(surveyName, questions, timeLengthMins, repromptAtMins, schedule);
	}
}
