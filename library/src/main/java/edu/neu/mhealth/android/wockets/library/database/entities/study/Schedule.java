package edu.neu.mhealth.android.wockets.library.database.entities.study;

import java.util.List;

/**
 * @author Dharam Maniar
 */
public class Schedule {

	public List<PromptTime> weekdays;

	public List<PromptTime> weekends;

	public Schedule() {
	}

	public Schedule(List<PromptTime> weekdays, List<PromptTime> weekends) {
		this.weekdays = weekdays;
		this.weekends = weekends;
	}

	public Schedule setWeekdays(List<PromptTime> weekdays) {
		this.weekdays = weekdays;
		return this;
	}

	public Schedule setWeekends(List<PromptTime> weekends) {
		this.weekends = weekends;
		return this;
	}

	public Schedule createSchedule() {
		return new Schedule(weekdays, weekends);
	}
}
