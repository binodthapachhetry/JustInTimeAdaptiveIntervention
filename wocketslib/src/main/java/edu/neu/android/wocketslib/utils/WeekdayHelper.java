package edu.neu.android.wocketslib.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class provides some useful methods for dealing with
 * issues with weekday programming.
 * 
 * @author bigbug
 *
 */
public class WeekdayHelper {

	private final static String[] sWeekdays = {
		"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
	};
	private final static DateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	// date: yyyy-MM-dd
    public static String getWeekday(String date) {
        int weekday = getWeekdayInNumber(date);
	    
	    return sWeekdays[weekday - 1];
    }      	
    
    /**     
     * Get weekday number from Monday to Sunday
     * @param  date yyyy-MM-dd
     * @return 1-7 indicates the weekday number from Monday to Sunday
     */
    public static int getWeekdayInNumber(String date) {
    	String time[] = date.split("-");
        
        int year  = Integer.parseInt(time[0]);         
        int month = Integer.parseInt(time[1]);         
        int day   = Integer.parseInt(time[2]);
         
        int total = year - 1980 + (year - 1980 + 3) / 4; // initial value of total
        int weekday = 1; // starting day: 1979-12-31 is Monday 
        boolean isLeap = (year % 400 == 0) | ((year % 4 == 0) & (year % 100 != 0));
     
	    for (int i = 1; i < month; ++i) {	         
	        switch (i) {	             
            case 1:	             
            case 3:	             
            case 5:	             
            case 7:	             
            case 8:	             
            case 10:	             
            case 12:	             
            	total += 31;	             
            	break;	             
            case 4:	             
            case 6:	             
            case 9:	             
            case 11:	             
            	total += 30;	             
            	break;	            
            case 2:	  
            	total += isLeap ? 29 : 28;
            	break;	            
            }	        
	    }         
	    total = total + day;	         
	    weekday = (weekday + total) % 7;
	    if (weekday == 0) {
	    	weekday = 7;
	    }
	    
	    return weekday;
    }
    
    /**
     * 
     * @param date1 yyyy-MM-dd
     * @param date2 yyyy-MM-dd
     * @return
     */ 
	public static boolean isSameWeekday(Date date1, Date date2) {   
		Calendar cal1 = Calendar.getInstance();   
		Calendar cal2 = Calendar.getInstance();   
		cal1.setTime(date1);   
		cal2.setTime(date2);   
		int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR); 
		
		if (0 == subYear) {   
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))   
				return true;   
		} else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {       	     
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))   
				return true;   
		} else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {   
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))   
				return true;   
		} 
		
		return false;   
	}   
	
	public static boolean isSameDay(Date date1, Date date2) {
		String strDate1 = sDateFormat.format(date1);
		String strDate2 = sDateFormat.format(date2);
		return strDate1.equals(strDate2);
	}
    	        	        	 
	public static String getSeqWeek(){   
	    Calendar c = Calendar.getInstance(Locale.US);   
	    String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));   
	    if (week.length() == 1) 
	    	week = "0" + week;   
	    String year = Integer.toString(c.get(Calendar.YEAR));     
	    return year + week;   
	}
	
	public static String getMonday(Date date){   
	    Calendar c = Calendar.getInstance();   
	    c.setTime(date);   
	    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);   
	    return sDateFormat.format(c.getTime());   
	}   
	     
	public static String getFriday(Date date){   
	    Calendar c = Calendar.getInstance();   
	    c.setTime(date);   
	    c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);      
	    return sDateFormat.format(c.getTime());     
	}   
	
	public static String afterNDayFrom(Date date, int n){   
	    Calendar c = Calendar.getInstance();   
	    c.setTime(date);   
	    c.add(Calendar.DATE, n);   
	    return sDateFormat.format(c.getTime());
	} 

	public static int daysBetween(String dateStart, String dateEnd) throws ParseException {		
	    Date start = sDateFormat.parse(dateStart);
	    Date end   = sDateFormat.parse(dateEnd);
		return daysBetween(start, end);
	}
	
	public static int daysBetween(Date dateStart, Date dateEnd) {		
	    float diff = dateEnd.getTime() - dateStart.getTime();
	    return (int) (diff / 24 / 3600 / 1000);
	}
}
