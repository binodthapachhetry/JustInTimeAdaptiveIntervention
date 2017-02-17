package edu.neu.android.wocketslib.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class DataStorage {
	public static final String TAG = "DataStorage";

	public static final String PREFS_PRIVATE = "WOCKETSPREFS_RELEASE_VER1";

	// This is the name of the file where the most key datastore info is stored
	// (on the SD card)
	private static final String FILENAME = "ds";

	private static final long HOURS_8 = 1000 * 60 * 60 * 8;

	// Keys (remember to add to ALL_KEYS list below
	public static final String KEY_FIRST_NAME = "KEY_FIRST_NAME";
	public static final String KEY_EMAIL = "KEY_EMAIL";
	public static final String KEY_START_WEIGHT = "KEY_START_WEIGHT";
	public static final String KEY_GENDER = "KEY_GENDER";
	public static final String KEY_HEIGHT = "KEY_HEIGHT";
	public static final String KEY_PHONE_ID = "KEY_PHONE_ID";
	public static final String KEY_START_DATE = "KEY_START_DATE";
	public static final String KEY_FAKE_DATE = "KEY_FAKE_DATE";
	public static final String KEY_STUDY_CONDITION = "KEY_STUDY_CONDITION";
	public static final String KEY_IN_UPDATE = "KEY_IN_UPDATE";
	public static final String KEY_VERSION = "KEY_VERSION";
	public static final String KEY_FORCE_RESET = "KEY_FORCE_RESET";
	public static final String KEY_NEWS_CHECKSUM = "KEY_NEWS_CHECKSUM";

	public static final String KEY_LAST_AUTHORIZED_TIME = "KEY_LAST_AUTHORIZED_TIME";
	public static final String KEY_LAST_AUTHORIZED_ATTEMPT_TIME = "KEY_LAST_AUTHORIZED_ATTEMPT_TIME";
    public static final String KEY_IS_AUTHORIZED = "_IS_AUTHORIZED";

	public static final String KEY_SET_SHUTDOWN = "KEY_SET_SHUTDOWN";
	public static final String KEY_NUM_SMS = "KEY_NUM_SMS";

	public static final String KEY_SUBJECT_ID = "KEY_SUBJECT_ID";
	public static final String KEY_DATE_OF_BIRTH = "_DATE_OF_BIRTH";

	public static final String KEY_LAST_NAME = "KEY_LAST_NAME";
	

	public static final String KEY_WOCKET_INFO = "KEY_WOCKET_INFO";

	public static final String KEY_PROMPT_TIMES = "KEY_PROMPT_TIMES";
	public static final String LAST_PROMPT_TIME = "KEY_LAST_PROMPT_TIME";

	public static final String KEY_IS_FIRST_SURVEY = "KEY_IS_FIRST_SURVEY";
	
	public static final String KEY_TEST_DATA = "KEY_TEST_DATA";

	public static final String LAST_CLEAR_JSON_QUEUE_TIME = "KEY_LAST_CLEAR_JSON_QUEUE_TIME";

	public static final String[] ALL_KEYS = { KEY_FIRST_NAME, KEY_EMAIL, KEY_START_WEIGHT, KEY_GENDER, KEY_HEIGHT,
			KEY_PHONE_ID, KEY_START_DATE, KEY_STUDY_CONDITION, KEY_FAKE_DATE, KEY_NEWS_CHECKSUM };

	// Important times
	public static final String KEY_LAST_TEASER_UPDATE_TIME = "KEY_LAST_TEASER_UPDATE_TIME";
	public static final String KEY_LAST_ARBITRATION_TIME = "KEY_LAST_ARBITRATION_TIME";
	public static final String KEY_LAST_ALARM_TIME = "KEY_LAST_ALARM_TIME";
	public static final String KEY_LAST_UPLOAD_TIME = "KEY_UPLOAD_TIME";
	public static final String KEY_LAST_UPLOAD_NOTES_TIME = "KEY_UPLOAD_NOTES_TIME";
	public static final String KEY_LAST_TIME_SERVICE_ACTIVATED_DISPLAY = "KEY_LAST_TIME_SERVICE_ACTIVATED_DISPLAY";
	public static final String KEY_LAST_TIME_USER_PRESENT = "KEY_LAST_TIME_USER_PRESENT";
	public static final String KEY_LAST_STATDB_DOWNLOAD_TIME = "KEY_LAST_STATDB_DOWNLOAD_TIME";
	public static final String KEY_LAST_SURVEILLANCE_TIME = "KEY_LAST_SURVEILLANCE_TIME";
	public static final String KEY_LAST_DATASTORECHECK_TIME = "KEY_LAST_DATASTORECHECK_TIME";
	public static final String KEY_LAST_DATASTOREUPDATE_TIME = "KEY_LAST_DATASTOREUPDATE_TIME";
	public static final String KEY_LAST_DATASTOREEMAIL_TIME = "KEY_LAST_DATASTOREEMAIL_TIME";
	public static final String KEY_DATABASE_DOWNLOADING = "false";
	public static final String KEY_TEXTMSG_SEND_DATE = "KEY_TEXTMSG_SEND_DATE";
	public static final String KEY_EMAIL_SEND_DATE = "KEY_EMAIL_SEND_DATE";
	public static final String KEY_LAST_LOG_WALLPAPER_TIME = "KEY_LAST_LOG_WALLPAPER_TIME";
	public static final String KEY_LAST_CHECK_WALLPAPER_TIME = "KEY_LAST_CHECK_WALLPAPER_TIME";
	public static final String KEY_LAST_SYNC_TRY_TIME = "KEY_LAST_SYNC_TRY_TIME";
	public static final String CONDITION_PARTICIPANT = "Wockets participant (WP)";
	public static final String CONDITION_TEST = "Tester (WT)";
	public static final String CONDITION_PERSONAL_CONTACT = "Personal contact (PC)";
	public static final String CONDITION_CELL_PHONE = "Cell phone (CP)";
	public static final String CONDITION_TEST_CP = "Tester (CP)";
	public static final String CONDITION_TEST_PC = "Tester (PC)";
	public static final String CONDITION_NONE = "";
	public static final String KEY_IS_TRANSMITTING = "KEY_IS_TRANSMITTING";

	public static final String UNKNOWN_STRING = "UNK";
	public static final int UNKNOWN_DAYS = -1;

	public static final String EMPTY = "";
	private static final String DEFAULT = "DEFAULT";
	private static final String UNK = "UNK";
	private static final String NEWLINE = "\n";
	private static SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");	

	// Important messages set occasionally
	public static final String KEY_THIS_WEEK_MSG = "KEY_THIS_WEEK_MSG";
	public static final String KEY_SPECIAL_MESSAGE_TEXT = "KEY_SPECIAL_MESSAGE_TEXT";
	public static final String KEY_SPECIAL_MESSAGE_TEXT_COLOR = "KEY_SPECIAL_MESSAGE_TEXT_COLOR";
	public static final String KEY_SPECIAL_MESSAGE_PRIORITY = "KEY_SPECIAL_MESSAGE_PRIORITY";
	
	// Important internal accelerometer data
	public static final String KEY_INTERNAL_ACCEL_RECORDING_COUNT = "KEY_INTERNAL_ACCEL_RECORDING_COUNT";
	public static final String KEY_INTERNAL_ACCEL_RECORDING_TIME = "KEY_INTERNAL_ACCEL_RECORDING_TIME";
	public static final String KEY_INTERNAL_ACCEL_AVERAGE = "KEY_INTERNAL_ACCEL_AVERAGE";
	public static final String KEY_INTERNAL_ACCEL_SAMPLES = "KEY_INTERNAL_ACCEL_SAMPLES";

	public static void setIsTransmitting(Context aContext) {
		SetValue(aContext, DataStorage.KEY_IS_TRANSMITTING, System.currentTimeMillis());
	}

	public static void setDoneTransmitting(Context aContext) {
		SetValue(aContext, DataStorage.KEY_IS_TRANSMITTING, 0);
	}

	public static boolean isTransmitting(Context aContext) {
		long value = GetValueLong(aContext, DataStorage.KEY_IS_TRANSMITTING, 0);
		if (value == 0)
			return false;
		else if ((System.currentTimeMillis() - value) > HOURS_8) {
			setDoneTransmitting(aContext);
			return false;
		} else
			return true;
	}

	public static void SetIDAndCondition(Context aContext) {
		// Set the ID and condition in case already not set

		SetValue(aContext, DataStorage.KEY_PHONE_ID, PhoneInfo.getID(aContext));
//TODO fix 
//		if (AuthorizationChecker.IsParticipantAuthorized(aContext))
//			SetValue(aContext, DataStorage.KEY_STUDY_CONDITION, DataStorage.CONDITION_PARTICIPANT);
//		else if (AuthorizationChecker.IsTesterAuthorized(aContext))
//			SetValue(aContext, DataStorage.KEY_STUDY_CONDITION, DataStorage.CONDITION_TEST);
	}

	public static boolean IsWPCondition(Context aContext) {
		String aCondition = getStudyCondition(aContext, DataStorage.CONDITION_NONE);
		if (aCondition.equals(DataStorage.CONDITION_PARTICIPANT) || aCondition.equals(DataStorage.CONDITION_TEST))
			return true;
		else
			return false;
	}

	public static String GetSummaryString(Context aContext) {
		StringBuffer sb = new StringBuffer();
		sb.append("Name: " + getFirstName(aContext, UNK) + NEWLINE);
		sb.append("DayNumber: " + getDayNumber(aContext, true) + NEWLINE);
		sb.append("FakeDate: " + getFakeDate(aContext, UNK) + NEWLINE);
		sb.append("StartDate: " + getStartDate(aContext, UNK) + NEWLINE);
		sb.append("PhoneID: " + getPhoneID(aContext, UNK) + NEWLINE);
		sb.append("StudyCondition: " + getStudyConditionShortString(aContext, UNK) + NEWLINE);
		sb.append("Email: " + getEmail(aContext, UNK) + NEWLINE);
		return sb.toString();
	}

	// Convenience getters
	// ------------------------------------------------------------

	// public float getLastWeightReading(Context aContext)
	// {
	// float weight = 0;
	// return diff;
	// }
	//
	// public Date getLastWeightReadingTime(Context aContext)
	// {
	// float weight = 0;
	// return diff;
	// }

	public static long getTimeFromUserPresent(Context aContext) {
		long diff = System.currentTimeMillis() - getLastTimeUserPresent(aContext, System.currentTimeMillis());
		return diff;
	}

	public static boolean isForceReset(Context aContext) {
		return GetValueBoolean(aContext, DataStorage.KEY_FORCE_RESET, false);
	}

	public static void setIsForceReset(Context aContext, boolean value) {
		SetValue(aContext, DataStorage.KEY_FORCE_RESET, value);
	}

	public static long getTime(Context aContext, String aKey) {
		return GetValueLong(aContext, aKey, AppInfo.NO_TIME);
	}

	public static void setTime(Context aContext, String aKey, long aTime) {
		SetValue(aContext, aKey, aTime);
	}

	public static long getNumSMS(Context aContext) {
		return GetValueLong(aContext, KEY_NUM_SMS, 0);
	}

	public static void setNumSMS(Context aContext, long value) {
		SetValue(aContext, DataStorage.KEY_NUM_SMS, value);
	}

	public static boolean isSetShutdown(Context aContext) {
		return GetValueBoolean(aContext, KEY_SET_SHUTDOWN, false);
	}

	public static void setShutdown(Context aContext, boolean value) {
		SetValue(aContext, DataStorage.KEY_SET_SHUTDOWN, value);
	}

	public static boolean getIsInUpdate(Context aContext) {
		return GetValueBoolean(aContext, KEY_IN_UPDATE, false);
	}

	public static void setIsInUpdate(Context aContext, boolean value) {
		SetValue(aContext, KEY_IN_UPDATE, value);
	}

	public static long getLastTimeArbitrate(Context aContext, long aDefaultValue) {
		return GetValueLong(aContext, KEY_LAST_ARBITRATION_TIME, aDefaultValue);
	}

	public static void setLastTimeArbitrate(Context aContext, long aSystemTime) {
		SetValue(aContext, KEY_LAST_ARBITRATION_TIME, aSystemTime);
	}

	public static long getLastTimeUserPresent(Context aContext, long aDefaultValue) {
		return GetValueLong(aContext, KEY_LAST_TIME_USER_PRESENT, aDefaultValue);
	}

	public static void setLastTimeUserPresent(Context aContext, long aSystemTime) {
		SetValue(aContext, KEY_LAST_TIME_USER_PRESENT, aSystemTime);
	}

	public static long getTeaserUpdateTime(Context aContext, long aDefaultValue) {
		return GetValueLong(aContext, KEY_LAST_TEASER_UPDATE_TIME, aDefaultValue);
	}

	public static void setTeaserUpdateTime(Context aContext, long aSystemTime) {
		SetValue(aContext, KEY_LAST_TEASER_UPDATE_TIME, aSystemTime);
	}

	public static String getPhoneID(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_PHONE_ID, aDefaultValue);
	}

	public static String getThisWeekMsg(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_THIS_WEEK_MSG, aDefaultValue);
	}

	public static void setThisWeekMsg(Context aContext, String aValue) {
		SetValue(aContext, KEY_THIS_WEEK_MSG, aValue);
	}

	public static String getSpecialMessageText(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_SPECIAL_MESSAGE_TEXT, aDefaultValue);
	}

	public static void setSpecialMessageText(Context aContext, String aValue) {
		SetValue(aContext, KEY_SPECIAL_MESSAGE_TEXT, aValue);
	}

	// Wocket info storage

	// private static String getWocketInfoText(Context aContext, String
	// aDefaultValue) {
	// return GetValueString(aContext, KEY_WOCKET_INFO, aDefaultValue);
	// }
	// private static void setWocketIDsText(Context aContext, String aValue) {
	// SetValue(aContext, KEY_WOCKET_INFO, aValue);
	// }
	//
	// public static WocketInfo[] getWocketInfo(Context aContext)
	// {
	// String s = getWocketInfoText(aContext, "");
	//
	// String[] separated = s.split(",");
	// if ((separated.length < 0) ||
	// (separated[0].compareTo("") == 0))
	// return null;
	// return separated;
	// }
	//
	// public static void setWocketInfo(Context aContext, WocketInfo[]
	// someWocketInfo)
	// {
	// StringBuilder sb = new StringBuilder();
	// int totalNum = someWocketInfo.length;
	//
	// for (int i = 0; i < totalNum; i++)
	// {
	// sb.append(someWocketIDs[i]);
	// if (i != (totalNum-1))
	// sb.append(",");
	// }
	// setWocketIDsText(aContext, sb.toString());
	// }

	private static String getPromptTimesText(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_PROMPT_TIMES, aDefaultValue);
	}

	public static String getPromptTimesTextKey(Context aContext, String aDefaultValue, String aKey) {
		return GetValueString(aContext, KEY_PROMPT_TIMES+aKey, aDefaultValue);
	}

	public static long[] getPromptTimes(Context aContext) {
		String s = getPromptTimesText(aContext, "");
		String[] separated = s.split(",");
		long[] someTimes = new long[separated.length];

		if ((separated.length < 0) || (separated[0].compareTo("") == 0))
			return null;

		for (int i = 0; i < separated.length; i++) {
			someTimes[i] = Long.parseLong(separated[i]);
		}
		return someTimes;
	}
	public static long[] getPromptTimesKey(Context aContext, String KEY) {
		String s = getPromptTimesTextKey(aContext, "", KEY);
		String[] separated = s.split(",");
		long[] someTimes = new long[separated.length];

		if ((separated.length < 0) || (separated[0].compareTo("") == 0))
			return null;

		for (int i = 0; i < separated.length; i++) {
			someTimes[i] = Long.parseLong(separated[i]);
		}
		return someTimes;
	}

	public static void setPromptTimes(Context aContext, long[] someTimes) {
		StringBuilder sb = new StringBuilder();
		int totalNum = someTimes.length;
		for (int i = 0; i < totalNum; i++) {
			sb.append(Long.toString(someTimes[i]));
			if (i != (totalNum - 1))
				sb.append(",");
		}
		setPromptTimesText(aContext, sb.toString());
	}

	private static void setPromptTimesText(Context aContext, String aValue) {
		SetValue(aContext, KEY_PROMPT_TIMES, aValue);
	}
	
	public static void setPromptTimesKey(Context aContext, long[] someTimes, String KEY) {
		StringBuilder sb = new StringBuilder();
		int totalNum = someTimes.length;
		for (int i = 0; i < totalNum; i++) {
			sb.append(Long.toString(someTimes[i]));
			if (i != (totalNum - 1))
				sb.append(",");
		}
		setPromptTimesTextKey(aContext, sb.toString(), KEY);
	}

	public static void setPromptTimesTextKey(Context aContext, String aValue, String aKey) {
		SetValue(aContext, KEY_PROMPT_TIMES+aKey, aValue);
	}

	public static long getSpecialMessagePriority(Context aContext, long aDefaultValue) {
		return GetValueLong(aContext, KEY_SPECIAL_MESSAGE_PRIORITY, aDefaultValue);
	}

	public static void setSpecialMessagePriority(Context aContext, long aValue) {
		SetValue(aContext, KEY_SPECIAL_MESSAGE_PRIORITY, aValue);
	}

	public static int getSpecialMessageTextColor(Context aContext, long aDefaultValue) {
		return (int) GetValueLong(aContext, KEY_SPECIAL_MESSAGE_TEXT_COLOR, aDefaultValue);
	}

	public static void setSpecialMessageTextColor(Context aContext, int aValue) {
		SetValue(aContext, KEY_SPECIAL_MESSAGE_TEXT_COLOR, aValue);
	}
	
	public static int getInternalAccelAverage(Context aContext, int aDefaultValue) {
		return (int) GetValueLong(aContext, KEY_INTERNAL_ACCEL_AVERAGE, aDefaultValue);
	}
	
	public static void setInternalAccelAverage(Context aContext, int aValue) {
		SetValue(aContext, KEY_INTERNAL_ACCEL_AVERAGE, aValue);
	}		
	
	public static int getInternalAccelSamples(Context aContext, int aDefaultValue) {
		return (int) GetValueLong(aContext, KEY_INTERNAL_ACCEL_SAMPLES, aDefaultValue);
	}
	
	public static void setInternalAccelSamples(Context aContext, int aValue) {
		SetValue(aContext, KEY_INTERNAL_ACCEL_SAMPLES, aValue);
	}

	public static String getStudyCondition(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_STUDY_CONDITION, aDefaultValue);
	}

	public static String getStudyConditionShortString(Context aContext, String aDefaultValue) {
		String s = GetValueString(aContext, KEY_STUDY_CONDITION, aDefaultValue);
		if (s.equals(CONDITION_PARTICIPANT))
			return "WP";
		else if (s.equals(CONDITION_TEST))
			return "WT";
		else if (s.equals(CONDITION_CELL_PHONE))
			return "CP";
		else if (s.equals(CONDITION_PERSONAL_CONTACT))
			return "PC";
		else if (s.equals(CONDITION_TEST_CP))
			return "TEST_PC";
		else if (s.equals(CONDITION_TEST_PC))
			return "TEST_CP";
		else
			return UNK;
	}

	public static int getNewsChecksum(Context aContext, int aDefaultValue) {
		String val = GetValueString(aContext, KEY_NEWS_CHECKSUM, Integer.toString(aDefaultValue));
		return Integer.parseInt(val);
	}

	public static void setNewsChecksum(Context aContext, int aValue) {
		SetValue(aContext, KEY_NEWS_CHECKSUM, aValue);
	}

	public static String getVersion(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_VERSION, aDefaultValue);
	}

	public static void setVersion(Context aContext, String aValue) {
		SetValue(aContext, KEY_VERSION, aValue);
	}

	public static String getFirstName(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_FIRST_NAME, aDefaultValue);
	}

	public static String getEmail(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_EMAIL, aDefaultValue);
	}

	public static float getStartWeight(Context aContext, float aDefaultValue) {
		return GetValueFloat(aContext, KEY_START_WEIGHT, aDefaultValue);
	}

	public static float getHeight(Context aContext, float aDefaultValue) {
		return GetValueFloat(aContext, KEY_HEIGHT, aDefaultValue);
	}

	public static String getStartDate(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_START_DATE, aDefaultValue);
	}

	public static String getFakeDate(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_FAKE_DATE, aDefaultValue);
	}

	public static String getGender(Context aContext, String aDefaultValue) {
		return GetValueString(aContext, KEY_GENDER, aDefaultValue);
	}

	public static Date GetDate(String aDateString) {
		Date aDate = null;
		try {
			aDate = df.parse(aDateString);
		} catch (Exception e) {
		}
		return aDate;
	}

	private static int MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;

	public static int getDayNumber(Context aContext, Date aDate) {
		String dateString = getStartDate(aContext, EMPTY);

		if (dateString.equals(EMPTY))
			return UNKNOWN_DAYS;

		Date startDate = GetDate(dateString);

		if (startDate == null) {
			Log.e(TAG, "Could not get startdate.");
			return 1;
		}

		if (aDate == null) {
			Log.e(TAG, "Null date sent to getDayNumber.");
			return 1;
		}

		long deltaDays = (aDate.getTime() - startDate.getTime()) / MILLSECS_PER_DAY;

		if (deltaDays < 0) // handle DST odd case
			return 1;

		deltaDays++; // Start at 1
		return (int) deltaDays;
	}

	public static int getDayNumber(Context aContext, boolean isUseFakeDay) {
		String fakeDate = getFakeDate(aContext, "");
		if ((isUseFakeDay) && (fakeDate != null) && (!fakeDate.trim().equals("")))
			return getDayNumber(aContext, GetDate(fakeDate));
		else
			// Use today
			return getDayNumber(aContext, Calendar.getInstance().getTime());
	}

	public static int getWeekNumber(Context aContext, Date aDate) {
		int days = getDayNumber(aContext, aDate);

		if (days == UNKNOWN_DAYS)
			return UNKNOWN_DAYS;

		int weeks = (int) Math.floor((days - 1) / 7.0) + 1;
		// Log.e(TAG,
		// "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Week: "
		// + weeks);
		return weeks;
	}

	public static int getWeekNumber(Context aContext, boolean isUseFakeDay) {
		String fakeDate = getFakeDate(aContext, "");
		if ((isUseFakeDay) && (fakeDate != null) && (!fakeDate.equals("")))
			return getWeekNumber(aContext, GetDate(fakeDate));
		else
			return getWeekNumber(aContext, Calendar.getInstance().getTime());
	}

	public static void SetSubjectID(Context aContext, String subjectID){
		SetValue(aContext,DataStorage.KEY_SUBJECT_ID,subjectID);
	}
	public static String GetSubjectID(Context aContext){
		return DataStorage.GetValueString(aContext, DataStorage.KEY_SUBJECT_ID, null);
	}
	public static void SetLastName(Context aContext, String lastName) {
		SetValue(aContext, KEY_LAST_NAME,lastName);
	}

	public static String GetLastName(Context aContext, String defaultValue) {
		return GetValueString(aContext, KEY_LAST_NAME, defaultValue);
	}

	public static void SetDateofBirth(Context aContext, long dateOfBirth) {
		SetValue(aContext, KEY_DATE_OF_BIRTH,dateOfBirth);
	}

	public static long GetDateOfBirth(Context aContext, long defaultValue) {
		return GetValueLong(aContext, KEY_DATE_OF_BIRTH, defaultValue);
	}

    // Check available ---------------------------------------------------------

    // TODO add a method that returns false if not avaiable?

    // Set values --------------------------------------------------------------
	
	public static boolean SetValue(Context aContext, String aKey, String aValue) {
		WOCKETSSharedPrefs.putString(aContext, aKey, aValue);
		return true;
	}

	public static boolean SetValue(Context aContext, String aKey, float aValue) {
		WOCKETSSharedPrefs.putFloat(aContext, aKey, aValue);
		return true;
	}

	public static boolean SetValue(Context aContext, String aKey, long aValue) {
		WOCKETSSharedPrefs.putLong(aContext, aKey, aValue);
		return true;
	}

	public static boolean SetValue(Context aContext, String aKey, boolean aValue) {
		WOCKETSSharedPrefs.putBoolean(aContext, aKey, aValue);
		return true;
	}

	// Get values ------------------------------------------------------------

	public static String GetValueString(Context aContext, String aKey, String aDefaultValue) {
		String aStr1 = WOCKETSSharedPrefs.getString(aContext, aKey, aDefaultValue);
		return aStr1;
	}

	public static boolean GetValueBoolean(Context aContext, String aKey, boolean aDefaultValue) {
		boolean aVal1 = WOCKETSSharedPrefs.getBoolean(aContext, aKey, aDefaultValue);
		return aVal1;
	}

	public static long GetValueLong(Context aContext, String aKey, long aDefaultValue) {
		long aVal1 = WOCKETSSharedPrefs.getLong(aContext, aKey, aDefaultValue);
		return aVal1;
	}

	public static float GetValueFloat(Context aContext, String aKey, float aDefaultValue) {
		float aVal1 = WOCKETSSharedPrefs.getFloat(aContext, aKey, aDefaultValue);
		return aVal1;
	}

	// CSV version ------------------------------------------------------------

	// see com.google.android.apps.mytracks.content
	public static class ContentTypeIds {
		public static final byte BOOLEAN_TYPE_ID = 0;
		public static final byte LONG_TYPE_ID = 1;
		public static final byte INT_TYPE_ID = 2;
		public static final byte FLOAT_TYPE_ID = 3;
		public static final byte DOUBLE_TYPE_ID = 4;
		public static final byte STRING_TYPE_ID = 5;
		public static final byte UNKNOWN_TYPE_ID = 6;

		private ContentTypeIds() { /* Not instantiable */
		}
	}

	//TODO ds file
	public static void SaveDataFile(Context aContext) throws WOCKETSException {

//		String sdCardLocation = FileUtils.getExtMemoryPath();
		File dir = FileHelper.setupDirectories(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY);
		File csvFile = new File(dir.getAbsolutePath(), FILENAME);

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(csvFile));
			CSVWriter writer = new CSVWriter(bw);

			List<String> header = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			List<String> types = new ArrayList<String>();

			String key, s, type;
			int i = 0;
			float f;

			s = getFirstName(aContext, DEFAULT);
			if (!s.equals(DEFAULT)) {
				key = KEY_FIRST_NAME;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			s = getEmail(aContext, DEFAULT);
			if (!s.equals(DEFAULT)) {
				key = KEY_EMAIL;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			f = getStartWeight(aContext, 0);
			if (f != 0) {
				key = KEY_START_WEIGHT;
				type = Byte.toString(ContentTypeIds.FLOAT_TYPE_ID);
				header.add(i, key);
				values.add(i, Float.toString(f));
				types.add(i, type);
			}

			s = getGender(aContext, EMPTY);
			if (!s.equals(EMPTY)) {
				key = KEY_GENDER;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			f = getHeight(aContext, 0);
			if (f != 0) {
				key = KEY_HEIGHT;
				type = Byte.toString(ContentTypeIds.FLOAT_TYPE_ID);
				header.add(i, key);
				values.add(i, Float.toString(f));
				types.add(i, type);
			}

			s = getPhoneID(aContext, EMPTY);
			if (!s.equals(EMPTY)) {
				key = KEY_PHONE_ID;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			s = getStartDate(aContext, EMPTY);
			if (!s.equals(EMPTY)) {
				key = KEY_START_DATE;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			s = getStudyCondition(aContext, EMPTY);
			if (!s.equals(EMPTY)) {
				key = KEY_STUDY_CONDITION;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			s = getFakeDate(aContext, EMPTY);
			if (!s.equals(EMPTY)) {
				key = KEY_FAKE_DATE;
				type = Byte.toString(ContentTypeIds.STRING_TYPE_ID);
				header.add(i, key);
				values.add(i, s);
				types.add(i, type);
			}

			writer.writeNext(header.toArray(new String[header.size()]));
			writer.writeNext(values.toArray(new String[values.size()]));
			writer.writeNext(types.toArray(new String[types.size()]));

		} catch (IOException ex) {
			throw new WOCKETSException(TAG, "Error in writing preferences", ex);
		} finally {
			if (bw != null)
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
				}
		}
	}

	public static void ReadDataFile(Context aContext) throws WOCKETSException {
//		String sdCardLocation = FileUtils.getExtMemoryPath();
		File dir = FileHelper.setupDirectories(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY);
		File csvFile = new File(dir.getAbsolutePath(), FILENAME);

		if (!(csvFile.exists())) {
			throw new WOCKETSException(TAG, String.format("%1$s not found on external storage", csvFile));
		}

		if (!(csvFile.canRead())) {
			throw new WOCKETSException(TAG, String.format("Cannot read %1$s file from external storage", csvFile));
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csvFile), 8000);
			CSVReader reader = new CSVReader(br);
			List<String[]> entries = reader.readAll();
			String[] header = entries.get(0);
			String[] values = entries.get(1);
			String[] types = entries.get(2);

			for (int i = 0; i < header.length; i++) {
				String name = header[i];
				String value = values[i];
				switch (Byte.parseByte(types[i])) {
				case ContentTypeIds.BOOLEAN_TYPE_ID:
					SetValue(aContext, name, Boolean.parseBoolean(value));
					break;
				case ContentTypeIds.LONG_TYPE_ID:
					SetValue(aContext, name, Long.parseLong(value));
					break;
				case ContentTypeIds.FLOAT_TYPE_ID:
					SetValue(aContext, name, Float.parseFloat(value));
					break;
				case ContentTypeIds.INT_TYPE_ID:
					SetValue(aContext, name, Integer.parseInt(value));
					break;
				case ContentTypeIds.STRING_TYPE_ID:
					SetValue(aContext, name, value);
					break;
				}
			}
		} catch (IOException ex) {
			throw new WOCKETSException(TAG, "Error in reading preferences", ex);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}
	}
}
