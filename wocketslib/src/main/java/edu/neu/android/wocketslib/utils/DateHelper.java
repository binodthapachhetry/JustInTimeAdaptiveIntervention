package edu.neu.android.wocketslib.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.neu.android.wocketslib.Globals;

public class DateHelper {
	public static int MMMMM_dd = 1;
	public static int EEE_MMMMM_dd = 2;

	private static String dateFormat1 = "MMMMM dd";
	private static String dateFormat2 = "EEE, MMMMM dd";

	private static String dateFormat = "MMMMM dd yyyy";
	private static String timeFormat = "hh:mm a";

	private static String dateTimeFormat = "MM/dd/yyyy hh:mm a";

	public static String serverDateFormat = "yyyy-MM-dd";

	public static String serverDateFormatFull = "yyyy-MM-dd kk:mm:ss";

	public static String mhealthFileNameFormat = "yyyy-MM-dd-HH-mm-ss-SSS";
	public static String mhealthTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";

	public static Date getDate(int aYear, int aMonth, int aDay) {
		String date = aYear + "/" + aMonth + "/" + aDay;
		java.util.Date utilDate = null;

		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			utilDate = formatter.parse(date);
		} catch (ParseException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return utilDate;
	}

	public static String getServerDateString(Date aDate) {
		return new SimpleDateFormat(serverDateFormat).format(aDate);
	}

	public static String format(Calendar cal, int dateFormat) {
		if (cal == null)
			return "";
		if (dateFormat == MMMMM_dd)
			return new SimpleDateFormat(dateFormat1).format(cal.getTime());
		if (dateFormat == EEE_MMMMM_dd)
			return new SimpleDateFormat(dateFormat2).format(cal.getTime());
		return "";
	}

	/**
	 * Get the date from a systemtime.
	 *
	 * @param aSystemTime
	 *            Time in milliseconds
	 * @return The date represented by the time in milliseconds
	 */
	public static Date getDate(long aSystemTime) {
		Date aDate = new Date(aSystemTime);
		return aDate;
	}

	public static long getDailyTime(int aHour, int aMinute) {
		Calendar cal = Calendar.getInstance();
		// Date aDate = cal.getTime();
		// Month starts at 0, day starts at 1
		// Log.d(TAG, "YEAR: " + cal.get(cal.YEAR));
		// Log.d(TAG, "MONTH: " + cal.get(cal.MONTH));
		// Log.d(TAG, "DAY_OF_MONTH: " + cal.get(cal.DAY_OF_MONTH));
		// Log.d(TAG, "MINUTE: " + cal.get(cal.MINUTE));
		// long unixTm = getUnixTime(cal.get(cal.YEAR), cal.get(cal.MONTH),
		// cal.get(cal.DAY_OF_MONTH), cal.get(cal.MINUTE), 0, 0);
		// Log.d(TAG, "TimeFromDate: " + cal.getTimeInMillis());
		// //Log.d(TAG, "UNIXTIME: " + unixTm);
		// Log.d(TAG, "SYSTEMTIME: " + System.currentTimeMillis());
		// Log.d(TAG, "DIFF: " + ( cal.getTimeInMillis() -
		// System.currentTimeMillis()));
		// Log.d(TAG, "DATE FROM TIME: " + getDate(System.currentTimeMillis()));

		cal.set(Calendar.HOUR_OF_DAY, aHour);
		cal.set(Calendar.MINUTE, aMinute);
		cal.set(Calendar.SECOND, 0);

		return cal.getTimeInMillis();
	}

	public static long getDailyTime(int aHour, int aMinute, int aSecond) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, aHour);
		cal.set(Calendar.MINUTE, aMinute);
		cal.set(Calendar.SECOND, aSecond);

		return cal.getTimeInMillis();
	}

	/**
	 * Check if the given time is in the same day, defined as starting at
	 * midnight
	 *
	 * @param aSystemTime
	 *            A time in milliseconds
	 * @return True if the given time is today (from midnight to midnight),
	 *         false otherwise
	 */
	public static boolean isToday(long aSystemTime) {
		Date theDate = DateHelper.getDate(aSystemTime);
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();

		if ((theDate.getDate() == today.getDate()) && (theDate.getMonth() == today.getMonth())
				&& (theDate.getYear() == today.getYear()))
			return true;
		else
			return false;
	}

	/**
	 * Check if the given time is the prior day, defined as starting at
	 * midnight by adding 24 hours and then checking if today
	 *
	 * @param aSystemTime
	 *            A time in milliseconds
	 * @return True if the given time is today (from midnight to midnight),
	 *         false otherwise
	 */
	public static boolean isYesterday(long aSystemTime) {
		Date theDate = DateHelper.getDate(aSystemTime + Globals.HOURS24_MS);
		return isToday(theDate.getTime());
	}

	/**
	 * Determine if a given date is prior to the current date (by day)
	 *
	 * TODO This has not been carefully tested and needs to be.
	 *
	 * @param aDate
	 * @return True if the date sent is not the same day as today (i.e., not the same year, month, day)
	 */
	public static boolean isAPreviousDay(Date aDate) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(aDate);
//        Log.e("TEST","Year: " + calendar.get(Calendar.YEAR));
//        Log.e("TEST","Month: " + calendar.get(Calendar.MONTH));
//        Log.e("TEST","Day: " + calendar.get(Calendar.DAY_OF_MONTH));

		Calendar now = Calendar.getInstance();
//        Log.e("TEST","Yearnow: " + now.get(Calendar.YEAR));
//        Log.e("TEST","Monthnow: " + now.get(Calendar.MONTH));
//        Log.e("TEST","Daynow: " + now.get(Calendar.DAY_OF_MONTH));

		if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR))
			return true;
		else if ((calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
				(calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH)))
			return true;
		else if ((calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
				(calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) &&
				(calendar.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH)))
			return true;
		else
			return false;
	}

	public static boolean isHourBefore(Date aDate){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(aDate);

		Calendar now = Calendar.getInstance();
		if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR))
			return true;
		else if ((calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
				(calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH)))
			return true;
		else if ((calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
				(calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) &&
				(calendar.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH)))
			return true;
		else if((calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) &&
				(calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) &&
				(calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) &&
				(calendar.get(Calendar.HOUR_OF_DAY) < now.get(Calendar.HOUR_OF_DAY)))
			return true;
		else
			return false;
	}

	/**
	 * Determine if aDate is not the same calendar day as calendarCompareDate and before it
	 *
	 * TODO This has not be carefully tested and need to be!
	 *
	 * @param aDate The date that should be first
	 * @param aCompareDate The date to compare against that should be second
	 * @return True if aDate is before aCompareDate
	 */
	public static boolean isDateBefore(Date aDate, Date aCompareDate) {

		Calendar calendarDate = Calendar.getInstance();
		Calendar calendarCompareDate = Calendar.getInstance();
		calendarDate.setTime(aDate);
		calendarCompareDate.setTime(aCompareDate);

		if (calendarDate.get(Calendar.YEAR) < calendarCompareDate.get(Calendar.YEAR))
			return true;
		else if ((calendarDate.get(Calendar.YEAR) == calendarCompareDate.get(Calendar.YEAR)) &&
				(calendarDate.get(Calendar.MONTH) < calendarCompareDate.get(Calendar.MONTH)))
			return true;
		else if ((calendarDate.get(Calendar.YEAR) == calendarCompareDate.get(Calendar.YEAR)) &&
				(calendarDate.get(Calendar.MONTH) == calendarCompareDate.get(Calendar.MONTH)) &&
				(calendarDate.get(Calendar.DAY_OF_MONTH) < calendarCompareDate.get(Calendar.DAY_OF_MONTH)))
			return true;
		else
			return false;
	}

	/**
	 * @param aDate The date that should be first
	 * @param aCompareDate The date to compare against that should be second
	 * @return True if aDate is (more than) two days before aCompareDate
	 */
	public static boolean isMoreThanTwoDaysBefore(Date aDate, Date aCompareDate) {
		long timeDifference = aCompareDate.getTime() - aDate.getTime();
		if( timeDifference/(1000 * 60 * 60 * 24) >= 2)
			return true;
		else
			return false;
	}

	public static String extractDate(Calendar cal) {
		if (cal == null)
			return "";
		return new SimpleDateFormat(dateFormat).format(cal.getTime());
	}

	public static String extractTime(Calendar cal) {
		if (cal == null)
			return "";
		return new SimpleDateFormat(timeFormat).format(cal.getTime());
	}

	public static String extractDateTime(Calendar cal) {
		if (cal == null)
			return "";
		return new SimpleDateFormat(dateTimeFormat).format(cal.getTime());
	}

	public static Calendar parseDateTime(String dateTime) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(new SimpleDateFormat(dateTimeFormat).parse(dateTime));
		} catch (ParseException e) {
			return null;
		} catch (Throwable e) {
			return null;
		}
		return cal;
	}

	public static Calendar getCurrentDateTime() {
		Calendar currentDate = Calendar.getInstance();
		return currentDate;
	}

	public static Calendar getCurrentDateNoTime() {
		Calendar currentDate = Calendar.getInstance();
		currentDate.set(Calendar.HOUR, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.set(Calendar.AM_PM, Calendar.AM);
		return currentDate;
	}

	public static boolean isNthHour(Date date, int hour){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int h = cal.get(Calendar.HOUR_OF_DAY);
		if(h == hour){
			return true;
		}else{
			return false;
		}
	}

	public static boolean isNDaysBefore(Calendar interestDate, Calendar referenceDate, int nDays) {
		interestDate.set(Calendar.HOUR_OF_DAY, 0);
		interestDate.set(Calendar.MINUTE, 0);
		interestDate.set(Calendar.SECOND, 0);
		interestDate.set(Calendar.MILLISECOND, 0);

		referenceDate.set(Calendar.HOUR_OF_DAY, 0);
		referenceDate.set(Calendar.MINUTE, 0);
		referenceDate.set(Calendar.SECOND, 0);
		referenceDate.set(Calendar.MILLISECOND, 0);

		if(referenceDate.getTimeInMillis() - interestDate.getTimeInMillis() >= 1000 * 3600 * 24L * nDays){
			return true;
		}else{
			return false;
		}
	}
}
