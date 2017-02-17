package edu.neu.mhealth.android.wockets.library.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Dharam Manair
 */
public class DateTime {

    private static final java.util.Date date = new java.util.Date();

    public static final long DAYS_8_IN_MILLIS = 8 * 24 * 60 * 60 * 1000;
    public static final long DAYS_7_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;
    public static final long DAYS_1_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final long HOURS_1_IN_MILLIS = 60 * 60 * 1000;
    public static final long MINUTES_1_IN_MILLIS = 60 * 1000;
    public static final long MINUTES_5_IN_MILLIS = 5 * 60 * 1000;
    public static final long MINUTES_10_IN_MILLIS = 10 * 60 * 1000;
    public static final long MINUTES_15_IN_MILLIS = 15 * 60 * 1000;
    public static final long SECONDS_10_IN_MILLIS = 10 * 1000;
    public static final long SECONDS_30_IN_MILLIS = 30 * 1000;

    /**
     * @return Timestamp String of the form: yyyy-MM-dd hh:mm:ss z
     */
    public static String getCurrentTimestampString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
        date.setTime(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * Returns the given timestamp in the form: dow mon dd hh:mm:ss zzz yyyy
     *
     * @param timeStampInMillis The timestamp which needs to be converted to string
     * @return String format of the given timestamp
     */
    public static String getTimestampString(long timeStampInMillis) {
        if (timeStampInMillis == -1) {
            return "";
        }
        date.setTime(timeStampInMillis);
        return date.toString();
    }

    /**
     * @return Timestamp
     */
    public static long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @return DateTime String of the form: yyyy-MM-dd
     */
    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        date.setTime(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * @return DateTime String of the form: yyyy-MM-dd
     */
    public static Date getDate(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date parsedDate = null;
        try {
            parsedDate = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

    public static String getCurrentHourWithTimezone() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-z", Locale.US);
        date.setTime(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String getTimezone() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("z", Locale.US);
        date.setTime(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static int getCurrentHour() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH", Locale.US);
        date.setTime(System.currentTimeMillis());
        return Integer.parseInt(simpleDateFormat.format(date));
    }

    public static int getCurrentMinute() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm", Locale.US);
        date.setTime(System.currentTimeMillis());
        return Integer.parseInt(simpleDateFormat.format(date));
    }

    public static boolean isWeekend() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    public static long getTimeInMillis(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }

    public static int getYear(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.MONTH);
    }

    public static int getDayOfMonth(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static long getTimeInMillis(int year, int month, int dayOfMonth, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
