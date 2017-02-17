package edu.neu.android.wocketslib.activities.paema.model;

import java.util.ArrayList;
import java.util.Calendar;

import edu.neu.android.wocketslib.utils.DateHelper;

public class HistoryWeek {
	public String displayText;
	public Calendar weekStartDate;
	public Calendar weekEndDate;

	public String toString() {
		return displayText;
	}

	public Calendar getWeekStartDate() {
		return weekStartDate;
	}

	public Calendar getWeekEndDate() {
		return weekEndDate;
	}

	public static HistoryWeek[] getHistoryWeeks(int weeksCount) {
		int oneDayInMs = 24 * 60 * 60 * 1000;
		int oneWeekInMs = 6 * oneDayInMs;

		ArrayList<HistoryWeek> weeks = new ArrayList<HistoryWeek>();
		Calendar dateSlider = Calendar.getInstance();

		HistoryWeek week = new HistoryWeek();
		week.displayText = "This Week";
		week.weekEndDate = (Calendar) dateSlider.clone();

		while (dateSlider.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
			dateSlider.setTimeInMillis(dateSlider.getTimeInMillis()
					- oneDayInMs);
		week.weekStartDate = (Calendar) dateSlider.clone();
		weeks.add(week);

		Calendar minDate = Calendar.getInstance();
		// We will show weeks upto 12/27/2010
		minDate.set(2010, Calendar.DECEMBER, 27, 0, 0, 0);
		for (int i = 1; i < weeksCount; i++) {
			dateSlider.setTimeInMillis(dateSlider.getTimeInMillis()
					- oneDayInMs);
			if (dateSlider.before(minDate))
				break;

			week = new HistoryWeek();
			week.weekEndDate = (Calendar) dateSlider.clone();
			dateSlider.setTimeInMillis(dateSlider.getTimeInMillis()
					- oneWeekInMs);
			week.weekStartDate = (Calendar) dateSlider.clone();
			week.displayText = "Week starting "
					+ DateHelper.extractDate(week.weekStartDate);
			weeks.add(week);
		}

		return weeks.toArray(new HistoryWeek[weeks.size()]);
	}
}
