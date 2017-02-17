package edu.neu.android.wocketslib.activities.datasummaryviewer;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.sensormonitor.DataStore;
import edu.neu.android.wocketslib.utils.Log;

public class WocketsDataSPHelper {
	private final static String TAG = "DataViewer";
	private final static int ONE_MINUTE_IN_MILLIS = 60*1000;
	private final static int ONE_DAY_IN_MILLIS = 24*60*60*1000;
	public final static String TAG_WRIST_L_SUMMARY = "TAG_WRIST_L_SUMMARY";
	public final static String TAG_WRIST_R_SUMMARY = "TAG_WRIST_R_SUMMARY";
	public final static String TAG_ANKLE_L_SUMMARY = "TAG_ANKLE_L_SUMMARY";
	public final static String TAG_ANKLE_R_SUMMARY = "TAG_ANKLE_R_SUMMARY";
	public final static String TAG_POCKET_L_SUMMARY = "TAG_POCKET_L_SUMMARY";
	public final static String TAG_POCKET_R_SUMMARY = "TAG_POCKET_R_SUMMARY";
	public final static String TAG_WRIST_L_RAW = "TAG_WRIST_L_RAW";
	public final static String TAG_WRIST_R_RAW = "TAG_WRIST_R_RAW";
	public final static String TAG_ANKLE_L_RAW = "TAG_ANKLE_L_RAW";
	public final static String TAG_ANKLE_R_RAW = "TAG_ANKLE_R_RAW";
	public final static String TAG_POCKET_L_RAW = "TAG_POCKET_L_RAW";
	public final static String TAG_POCKET_R_RAW = "TAG_POCKET_R_RAW";
	public final static String TAG_HR = "TAG_HR";
	public final static String TAG_PHONE = "TAG_PHONE";
	public final static String[] TAGS = {TAG_WRIST_L_SUMMARY, TAG_WRIST_L_RAW, TAG_WRIST_R_SUMMARY, TAG_WRIST_R_RAW, TAG_ANKLE_L_SUMMARY, 
		 									TAG_ANKLE_L_RAW ,TAG_ANKLE_R_SUMMARY,TAG_ANKLE_R_RAW, TAG_POCKET_L_SUMMARY, TAG_POCKET_L_RAW,
		 									TAG_POCKET_R_SUMMARY, TAG_POCKET_R_RAW, TAG_HR, TAG_PHONE};	
	
	public final static String LAST_SAVE_TIME = "LAST_SAVE_TIME";

	private final static String KEY_WOCKETS_DATA = "WocketsData";
	private final static int NO_DATA = -1;
	private SharedPreferences sp;
	private Context c;
	
	
	public WocketsDataSPHelper(Context c) {
		super();
		this.c = c;		
	}
	private void setSPbyTAG(String TAG){
		if(TAG.equals(TAG_WRIST_L_SUMMARY)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_WRIST_L_SUMMARY, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_WRIST_R_SUMMARY)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_WRIST_R_SUMMARY, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_ANKLE_L_SUMMARY)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_ANKLE_L_SUMMARY, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_ANKLE_R_SUMMARY)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_ANKLE_R_SUMMARY, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_POCKET_L_SUMMARY)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_POCKET_L_SUMMARY, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_POCKET_R_SUMMARY)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_POCKET_R_SUMMARY, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_WRIST_L_RAW)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_WRIST_L_RAW, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_WRIST_R_RAW)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_WRIST_R_RAW, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_ANKLE_L_RAW)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_ANKLE_L_RAW, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_ANKLE_R_RAW)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_ANKLE_R_RAW, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_POCKET_L_RAW)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_POCKET_L_RAW, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_POCKET_R_RAW)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_POCKET_R_RAW, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_HR)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_HR, Context.MODE_PRIVATE);
		}
		else if(TAG.equals(TAG_PHONE)){
			sp = c.getSharedPreferences(KEY_WOCKETS_DATA+"_"+TAG_PHONE, Context.MODE_PRIVATE);
		}

	}
	/**
	 * add the data from that TAG for minute
	 * @param TAG
	 * @param data
	 */
	public void setDataForMinute(String TAG, int minute, int data){		
			
		setSPbyTAG(TAG);
		Editor edit = sp.edit();
		edit.putInt(KEY_WOCKETS_DATA+minute, data);
		edit.commit();		
		
	}
	/**
	 * add data from the TAG in revised minute into SP
	 * @param TAG
	 * @param minute
	 * @param data
	 */
	public void setDataTime(String TAG, Date time, int data){
		setSPbyTAG(TAG);
		int minute = time.getHours()*60+time.getMinutes();
		Editor edit = sp.edit();
		edit.putInt(KEY_WOCKETS_DATA+minute, data);
		edit.commit();
	}
	/** 
	 * @param TAG
	 * @return all of the data for 24hrs under the TAG. Index of the array is the minute of the day starting
	 * from 0 to 1439. If there is no data in that minute, return -1
	 */
	public int[] getData(String TAG){
		setSPbyTAG(TAG);
		int[] data = new int[1440];
		for (int i = 0; i < 1440; i++) {
			data[i] = sp.getInt(KEY_WOCKETS_DATA+i, NO_DATA);
		}
		return data;
	}
	/**
	 * 
	 * @param TAG
	 * @param minute
	 * @return the data fromt TAG in that minute
	 */
	public int getDatabyTime(String TAG, int minute){
		setSPbyTAG(TAG);
		return sp.getInt(KEY_WOCKETS_DATA+minute, NO_DATA);
	}
	/**
	 * clear the data for all TAGs between the start minute and the end minute and set as -1
	 * @param startMinute starting from 0
	 * @param endMinute ending to 1439
	 */
	public void clearAllOldData(Date newDate){
		Date lastDate = DataStore.getLastTimeRunInDate(c);
		DataStore.setLastTimeRun(c, newDate);
		if(lastDate != null){
			if(newDate.after(lastDate) ){
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				Log.d(TAG, "last data is "+sdf.format(lastDate) + " new date is"+ sdf.format(newDate));
				
				if((newDate.getTime() - lastDate.getTime()) > ONE_DAY_IN_MILLIS)
					clearAllData();
				else{
					Log.d(TAG, "start clean data");

					int lastDateMin = lastDate.getHours()*60+lastDate.getMinutes();
					int newDateMin = newDate.getHours()*60+newDate.getMinutes();
					Log.d(TAG, "lastDateMin: "+lastDateMin+", newDateMin: "+newDateMin+", and gap is: "+(newDateMin - lastDateMin));
					if((newDateMin - lastDateMin)>1){
						Log.d(TAG, "gap > 1min");					
						for (String tag : TAGS) {
							setSPbyTAG(tag);
							Editor edit = sp.edit();
							for (int i = lastDateMin + 1; i <= newDateMin; i++) {
								edit.putInt(KEY_WOCKETS_DATA+i, NO_DATA);
							}
							edit.commit();
						}
//						for (int i = lastDateMin + 1; i <= newDateMin; i++) {
//							for (String tag : TAGS) {
//								setDataForMinute(tag, i, NO_DATA);							
//							}
//						}					
					}
					else if((lastDateMin - newDateMin)>0 &&
							(lastDateMin - newDateMin)<1439){
						Log.d(TAG, "gap > 1min(diff day)");
						
						for (String tag : TAGS) {
							setSPbyTAG(tag);
							Editor edit = sp.edit();
							for (int i = lastDateMin + 1; i < 1440; i++) {
								edit.putInt(KEY_WOCKETS_DATA+i, NO_DATA);
							}
							for (int i = 0; i <= newDateMin; i++) {
								edit.putInt(KEY_WOCKETS_DATA+i, NO_DATA);
							}
							edit.commit();
						}
//						for (int i = lastDateMin + 1; i < 1440; i++) {
//							for (String tag : TAGS) {
//								setDataForMinute(tag, i, NO_DATA);
//							}
//						}
//						for (int i = 0; i <= newDateMin; i++) {
//							for (String tag : TAGS) {
//								setDataForMinute(tag, i, NO_DATA);
//							}
//						}
					}
					else
						cleanData(newDate);
					Log.d(TAG, "end clean data");

				}
			}	
		}
	}
	/**
	 * clear all the data
	 */
	public void clearAllData(){
		Log.d(TAG, "all data cleaned");
//		for (int i = 0; i < 1440; i++) {
//			for (String tag : TAGS) {
//				setDataForMinute(tag, i, NO_DATA);
//			}
//		}
		for (String tag : TAGS) {
			setSPbyTAG(tag);
			Editor edit = sp.edit();
			for (int i = 0; i < 1440; ++i) {				
				edit.putInt(KEY_WOCKETS_DATA+i, NO_DATA);			
			}
			edit.commit();
		}
	}
	public void cleanData(Date date){
		Log.d(TAG, "data in current time cleaned");
		for (String tag : TAGS) {
			setDataForMinute(tag, minuteShifted(tag, date), NO_DATA);
		}	
	}
//	public void cleanMinute(int minute){
//		for (String tag : TAGS) {
//			Date lastSavedDate = getLastDataTime(tag);
//			Date now = new Date();
//			if(lastSavedDate == null){
//				setDataForMinute(tag, minute, NO_DATA);
//			}
//			else if((now.getTime() - lastSavedDate.getTime()) > 60*1000){
//				setDataForMinute(tag, minute, NO_DATA);
//			}
//			else{
//				int lastMin = lastSavedDate.getMinutes()+lastSavedDate.getHours()*60;
//				if(minute > lastMin){
//					setDataForMinute(tag, minute, NO_DATA);
//					if(Globals.IS_DEBUG){
//						Log.d("debug", "data cleaned in "+minute
//								+" minute last time "+lastMin+" minute tag "+tag);
//					}
//				}
//			}
//		}	
//	}
	public void setLastDataTime(String TAG, Date lastSaveTime){
		setSPbyTAG(TAG);
		sp.edit().putLong(LAST_SAVE_TIME, lastSaveTime.getTime()).commit();
	}
	public Date getLastDataTime(String TAG){
		setSPbyTAG(TAG);
		long lastSaveTimeMillis = sp.getLong(LAST_SAVE_TIME, -1);
		if(lastSaveTimeMillis != -1){
			return  new Date(lastSaveTimeMillis);
		}
		else return null;
	}
	public int minuteShifted(String TAG, Date data){
		Date lastSaveTime = dateShifted(TAG, data);
		return (lastSaveTime.getHours()*60+lastSaveTime.getMinutes());
	}
	public Date dateShifted(String TAG, Date date){
		setSPbyTAG(TAG);
		Date lastSaveTime = getLastDataTime(TAG);
		Date revisedDate = date;
		if(lastSaveTime != null){
			if(isWithin70secs(lastSaveTime, date))
				revisedDate = new Date(date.getTime()-ONE_MINUTE_IN_MILLIS);
			else if(isWithIn1Min(lastSaveTime, date))
				revisedDate = new Date(date.getTime()+ONE_MINUTE_IN_MILLIS);
			if(Globals.IS_DEBUG){
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				Log.d(TAG, "data is "+sdf.format(date) + " last time is"+ sdf.format(lastSaveTime)
						+" after shift is "+sdf.format(revisedDate)+" tag "+TAG);
			}

		}

		return revisedDate;
	}
	public boolean isWithIn1Min(Date prev, Date later){
		if(prev != null)
			return (prev.getMinutes() == later.getMinutes() 
				&& Math.abs(later.getTime() - prev.getTime()) < ONE_MINUTE_IN_MILLIS);
		else
			return false;
	}
	public boolean isWithin70secs(Date prev, Date later){
		return (prev.getSeconds() > 54 && later.getSeconds() < 6
				&& (later.getTime() - prev.getTime()) < 71*1000 && (later.getTime() - prev.getTime()) > ONE_MINUTE_IN_MILLIS);
	}
}
