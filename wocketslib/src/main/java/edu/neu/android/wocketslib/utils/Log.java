package edu.neu.android.wocketslib.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Stack;

import android.content.Context;
import android.os.Environment;

import org.apache.james.mime4j.field.datetime.DateTime;

import au.com.bytecode.opencsv.CSVWriter;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;

/**
 * IMPORTANT: You MUST add the following line to your app's AndroidManifest.xml
 * file:
 * <code><uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /></code>
 *
 * Remove this import from the file: import android.util.Log; and replace with:
 * import edu.neu.android.wocketslib.util.Log;
 *
 * This class provides log-to-file capabilities for mHealth Research Group apps.
 * Logs are stored in internal memory by default, unless the SD card flag is
 * specified. If the SD card flag is specified and it is not available, logs are
 * lost. If errors occur when logging, those errors are written to the console
 * but lost.
 *
 * <p>
 * This class is designed to replace the android.util.Log class for mHealth
 * Research Group apps. This class duplicates all of the standard Android
 * logging methods and adds two more: h and o. All of these methods log their
 * data to a file in addition to echoing it to the normal Android log (readable
 * with logcat). Logged messages are in different files depending on the logging
 * method used:
 * </p>
 * <table>
 * <tr>
 * <td>d()</td>
 * <td>Debug</td>
 * <td>Echoed to logcat but not written to a file. Use this debug your apps.</td>
 * </tr>
 * <tr>
 * <td>o()</td>
 * <td>Machine-readable output</td>
 * <td>output.csv</td>
 * </tr>
 * <tr>
 * <td>h()</td>
 * <td>Human-readable output</td>
 * <td>journal.csv</td>
 * </tr>
 * <tr>
 * <td>i()</td>
 * <td>Information</td>
 * <td>log.txt</td>
 * </tr>
 * <tr>
 * <td>w()</td>
 * <td>Warning</td>
 * <td>log.txt</td>
 * </tr>
 * <tr>
 * <td>e()</td>
 * <td>Error</td>
 * <td>log.txt AND err.txt</td>
 * </tr>
 * <tr>
 * <td>wtf()</td>
 * <td>WTF Error</td>
 * <td>log.txt AND err.txt</td>
 * </tr>
 * </table>
 *
 * <p>
 * The location of a particular log depends on the aModuleName that is passed to
 * the logging method, the date, and the type of log involved. Logs are stored
 * in:
 * </p>
 * <code>{app internal storage}/.[studyservername]/logs/{YYYY}/{MM}/{DD}/{aModuleName}.{err|log|output|journal}.{csv|txt}</code>
 * unless the IS_LOG_EXTERNAL global is set to true, in which case the logs go
 * to
 * <code>{external storage}/.[studyservername]/logs/{YYYY}/{MM}/{DD}/{aModuleName}.{err|log|output|journal}.{csv|txt}</code>
 *
 * The studyservername is a string such as wockets, USCAsthma, etc.
 *
 * <p>
 * output.csv and journal.csv are comma-delimited, fully-escaped CSV files of
 * the format:
 * </p>
 * <code> Millisecond Unix time (long int), Human-readable local time (String), Data 1 (String), Data 2 (String), etc...</code>
 *
 * <p>
 * log.txt and err.txt are flat text files with simple columns separated by
 * tabs.
 * </p>
 * <code> Millisecond Unix time (long int), Human-readable local time (String), {i|w|e|wtf} (String), message (String)</code>
 *
 * <p>
 * It can sometimes be tedious to type out the same "aModuleName" parameter for
 * each log call. Consider creating a special Log class in your project that
 * hard-codes the "aModuleName" parameter to an appropriate string. See
 * ExampleConvenienceLog for an example.
 * </p>
 *
 * @author pixel@media.mit.edu
 *
 */

public class Log {
	private static final String TAG = "mHealthLog";

	public static final boolean LOG_SHOW = true;
	public static final boolean NO_LOG_SHOW = false;

	private static String dateFormatter = "yyyy-MM-dd HH:mm:ss.SSS";

	private static boolean writeToFile = true;

	// Log.o keys
	public static final String USER_ACTION = "UserAction";
	public static final String AUTHORIZATION = "Auth";

	// /**
	// * Initializes the logger. Call this in the onCreate() method of EVERY
	// Activity that uses this class.
	// * @param aModuleName The name of your module, e.g. "Level3"
	// */
	// public static void init(String aModuleName) {
	// aModuleName = aModuleName;
	// Log.init(aModuleName, true);
	// }
	//
	// /**
	// * Initializes the logger. Call this in the onCreate() method of EVERY
	// Activity that uses this class.
	// * @param aModuleName The name of your module, e.g. "Level3"
	// * @param writeToFile [Debug option] If false, then logged messages are
	// echoed to logcat but not actually written to files.
	// */
	public static void enableWritingToFile(boolean writeToFile) {
		Log.writeToFile = writeToFile;
	}

	/**
	 * Prints a debug {message} to the android Log. Does *NOT* write to the log
	 * file. This is the only method that does not log to a file. Use this while
	 * debugging.
	 */
	public static void d(String aModuleName, String message) {
		if (Globals.IS_DEBUG) {
			android.util.Log.d(Globals.UNIQUE_LOG_STRING + aModuleName, message);
		}
	}

	/** Log a human-readable message. Written to journal.csv */
	public static void h(String aModuleName, String message, boolean isLogShow) {
		android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, "[h] " + message);

		if (writeToFile)
			writeToCSV(aModuleName, "journal", message);

		if (isLogShow)
			logShow(aModuleName, message);
	}


	/** Log a human-readable message. Written to journal.csv */
	public static void h(String aModuleName, String message, boolean isLogShow, boolean isEncrypt, Context mContext) {
		android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, "[h] " + message);
		if(isEncrypt) {
			try {
				message = RSACipher.encrypt(message, mContext);
			} catch (IOException | GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
		if (writeToFile)
			writeToCSV(aModuleName, "journal", message);

		if (isLogShow)
			logShow(aModuleName, message);
	}


	public static void logShow(String aModuleName, String text) {
		i(Globals.UNIQUE_LOG_STRING + aModuleName, "\"Show: " + text.replace("\"", "\"\"") + "\"");
	}

	// /** Read messages written to journal.csv */
	// public static String[] rh(String aModuleName, Calendar historyDate)
	// {
	// aModuleName = aModuleName;
	// String[][] messages = readFromCSV(aModuleName,"journal",historyDate);
	// if(messages == null || messages.length == 0)
	// return null;
	// String[] msgs = new String[messages.length];
	// for(int i=0;i<messages.length;i++)
	// msgs[i] = messages[i][2];
	// return msgs;
	// }

	/**
	 * Log a set of machine-readable data columns to output.csv. You may pass
	 * any number of arguments after {message}. Each additional argument will be
	 * placed in its own column in the output CSV. These arguments MUST BE
	 * STRINGS or you will generate a Mysterious Error.
	 */
	public static void o(String aModuleName, Object... arguments) {

		// If DEBUG is set, then print out all the column values. Otherwise, just print out the first one.
		if (Globals.IS_DEBUG)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arguments.length; i++)
			{
				sb.append((String) arguments[i]);
				sb.append(" ");
			}
			android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, "[o] " + sb.toString());
		}
		else
			android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, "[o] " + arguments[0]);

		if (writeToFile)
			writeToCSV(aModuleName, "output", arguments);
	}

	/**
	 * Log a set of machine-readable data columns to output.csv. You may pass
	 * any number of arguments after {message}. Each additional argument will be
	 * placed in its own column in the output CSV. These arguments MUST BE
	 * STRINGS or you will generate a Mysterious Error.
	 */
	public static void o(String aModuleName, Boolean isEncrypt, Context mContext, Object... arguments) {

		// If DEBUG is set, then print out all the column values. Otherwise, just print out the first one.
		if (Globals.IS_DEBUG)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arguments.length; i++)
			{
				sb.append((String) arguments[i]);
				sb.append(" ");
			}
			android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, "[o] " + sb.toString());
		}
		else
			android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, "[o] " + arguments[0]);

		if (writeToFile)
			writeToCSV(aModuleName, "output", isEncrypt, mContext, arguments);
	}

	/** Log a verbose message. Written to log.txt */
	public static void v(String aModuleName, String message) {
		android.util.Log.v(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile)
			writeToTXT(aModuleName, "log", "v", message);
	}

	/** Log a verbose message. Written to log.txt */
	public static void v(String aModuleName, String message, Boolean isEncrypt, Context mContext) {
		android.util.Log.v(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if(isEncrypt) {
			try {
				message = RSACipher.encrypt(message, mContext);
			} catch (IOException | GeneralSecurityException e) {
				e.printStackTrace();
			}
		}

		if (writeToFile)
			writeToTXT(aModuleName, "log", "v", message, isEncrypt, mContext);
	}

	/** Log an informational message. Written to log.txt */
	public static void i(String aModuleName, String message) {
		android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile)
			writeToTXT(aModuleName, "log", "i", message);
	}

	/** Log an informational message. Written to log.txt */
	public static void i(String aModuleName, String message, Boolean isEncrypt, Context mContext) {
		android.util.Log.i(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile)
			writeToTXT(aModuleName, "log", "i", message, isEncrypt, mContext);
	}

	/** Log a warning message. Written to log.txt */
	public static void w(String aModuleName, String message) {
		android.util.Log.w(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile)
			writeToTXT(aModuleName, "log", "w", message);
	}

	/** Log a warning message. Written to log.txt */
	public static void w(String aModuleName, String message, Boolean isEncrypt, Context mContext) {
		android.util.Log.w(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile)
			writeToTXT(aModuleName, "log", "w", message, isEncrypt, mContext);
	}

	/** Log an error message. Written to err.txt and log.txt */
	public static void e(String aModuleName, String message) {
		android.util.Log.e(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile) {
			writeToTXT(aModuleName, "err", "e", message);
		}
	}

	/** Log an error message. Written to err.txt and log.txt */
	public static void e(String aModuleName, String message, Boolean isEncrypt, Context mContext) {
		android.util.Log.e(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile) {
			writeToTXT(aModuleName, "err", "e", message, isEncrypt, mContext);
		}
	}

	/** Log an error message. Written to err.txt and log.txt */
	public static void e(String aModuleName, String message, Exception ex) {
		android.util.Log.e(Globals.UNIQUE_LOG_STRING + aModuleName, message, ex);

		if (writeToFile) {
			writeToTXT(aModuleName, "err", "e", message);
		}
	}

	/** Log an error message. Written to err.txt and log.txt */
	public static void e(String aModuleName, String message, Exception ex, Boolean isEncrypt, Context mContext) {
		android.util.Log.e(Globals.UNIQUE_LOG_STRING + aModuleName, message, ex);

		if (writeToFile) {
			writeToTXT(aModuleName, "err", "e", message, isEncrypt, mContext);
		}
	}

	/** Log a wtf message. Written to err.txt and log.txt */
	public static void wtf(String aModuleName, String message) {
		android.util.Log.e(Globals.UNIQUE_LOG_STRING + aModuleName, message);

		if (writeToFile) {
			writeToTXT(aModuleName, "err", "wtf", message);
		}
	}

	/**
	 * Erases all files in the active logs folder, including the logs folder
	 * itself.
	 */
	public static void eraseLogs() {
		if (Globals.IS_LOG_EXTERNAL)
			eraseLogsExternal();
		else
			eraseLogsInternal();
	}

	public static void eraseLogsExternal() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			android.util.Log.w(Globals.UNIQUE_LOG_STRING + TAG, "Can't erase logs; SD card is not accessible: " + Environment.getExternalStorageState());
			return;
		}

		File folder = getLogDirectoryExternal();

		if (folder.exists()) {
			if (deleteDir(folder))
				android.util.Log.i(Globals.UNIQUE_LOG_STRING + TAG, "Logs erased");
			else
				android.util.Log.e(Globals.UNIQUE_LOG_STRING + TAG, "Couldn't erase the previous logs");
		}
	}

	public static void eraseLogsInternal() {
		File folder = getLogDirectoryInternal();

		if (folder.exists()) {
			if (deleteDir(folder))
				android.util.Log.i(Globals.UNIQUE_LOG_STRING + TAG, "Logs erased");
			else
				android.util.Log.e(Globals.UNIQUE_LOG_STRING + TAG, "Couldn't erase the previous logs");
		}
	}

	public static File getLogDirectory()
	{
		if (Globals.IS_LOG_EXTERNAL)
			return getLogDirectoryExternal();
		else
			return getLogDirectoryInternal();
	}

	public static File getLogDirectoryExternal() {
		return new File(Environment.getExternalStorageDirectory() + File.separator + Globals.LOG_DIRECTORY);
	}

	public static File getLogDirectoryInternal() {
		return new File(Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.LOG_DIRECTORY);
	}

	// public static void onCreate(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onCreateL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onCreateUL", activityName);
	// else
	// o(aModuleName, "onCreateUNK", activityName);
	// }
	//
	// public static void onStart(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onStartL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onStartUL", activityName);
	// else
	// o(aModuleName, "onStartUNK", activityName);
	// }
	//
	// public static void onRestart(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onRestartL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onRestartUL", activityName);
	// else
	// o(aModuleName, "onRestartUNK", activityName);
	// }
	//
	// public static void onResume(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onResumeL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onResumeUL", activityName);
	// else
	// o(aModuleName, "onResumeUNK", activityName);
	// }
	//
	// public static void onPause(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onPauseL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onPauseUL", activityName);
	// else
	// o(aModuleName, "onPauseUNK", activityName);
	// }
	//
	// public static void onDestroy(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onDestroyL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onDestroyUL", activityName);
	// else
	// o(aModuleName, "onDestroyUNK", activityName);
	// }
	//
	// public static void onStop(String aModuleName, String activityName, int
	// lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onStopL", activityName);
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onStopUL", activityName);
	// else
	// o(aModuleName, "onStopUNK", activityName);
	// }
	//
	// public static void onActivityResult(String aModuleName, String
	// activityName, int requestCode, int resultCode, int lockStatus) {
	// if (lockStatus == KEYGUARD_IS_LOCKED)
	// o(aModuleName, "onActivityResultL", activityName,
	// Integer.toString(requestCode), Integer.toString(resultCode));
	// else if (lockStatus == KEYGUARD_NOT_LOCKED)
	// o(aModuleName, "onActivityResultUL", activityName,
	// Integer.toString(requestCode), Integer.toString(resultCode));
	// else
	// o(aModuleName, "onActivityResultUNK", activityName,
	// Integer.toString(requestCode), Integer.toString(resultCode));
	// }

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				deleteDir(child);
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	// private static String[][] readFromCSV(String aModuleName, String fileType,
	// Calendar date) {
	// if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
	// {
	// Log.w(aModuleName, "SD card is not available for logging: " +
	// Environment.getExternalStorageState());
	// return null;
	// }
	//
	// // add 1 to month because starts with 0, which is confusing
	// String folderPath = String.format(Globals.DEFAULT_LOGS_DIR +
	// "logs/%04d-%02d-%02d", date.get(Calendar.YEAR), date.get(Calendar.MONTH)
	// + 1, date.get(Calendar.DAY_OF_MONTH));
	//
	// File folder = new
	// File(Environment.getExternalStorageDirectory().getAbsolutePath() +
	// folderPath);
	// File logFile = new File(folder, String.format("%s.%s.csv", aModuleName,
	// fileType));
	//
	// if(!logFile.exists())
	// return null;
	//
	// CSVReader reader = null;
	// try
	// {
	// reader = new CSVReader(new FileReader(logFile));
	// List<String[]> rows = reader.readAll();
	// if(rows != null && rows.size() != 0)
	// return (String[][])rows.toArray(new String[rows.size()][]);
	// }
	// catch(IOException e)
	// {
	// android.util.Log.e(TAG, "Error while writing log: " + e.toString());
	// }
	// finally
	// {
	// if(reader != null)
	// try
	// {
	// reader.close();
	// }
	// catch (IOException e)
	// {
	// }
	// }
	// return null;
	//
	// }

	public static String getFolderDateFormatForToday(Calendar cal)
	{
		// hopefully "default" locale is correct in cal object

		// add 1 to month because starts with 0, which is confusing
		String folderPath = String.format("/%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));

//		String folderPath = DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone()

		return folderPath;
	}

	public static String getFolderDateFormatForToday()
	{
		return getFolderDateFormatForToday(Calendar.getInstance());
	}

	private static void writeToCSV(String aModuleName, String fileType, Object... arguments) {

		if (Globals.IS_LOG_EXTERNAL) {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.w(aModuleName, "SD card is not available for logging: " + Environment.getExternalStorageState());
				return;
			}
		}

		Calendar cal = Calendar.getInstance();
		String folderPath = getFolderDateFormatForToday(cal);

		// Optional check for free space
		// disabled for now for performance reasons
		// if we're out of space, we'll just get an IOException
		// StatFs fs = new StatFs(folderPath);
		// if(fs.getAvailableBlocks() < 4)
		// {
		// Log.e("Error while writing log: Not enough space on the SD card");
		// return;
		// }

//		File folder = new File(getLogDirectory().getAbsolutePath() + File.separator + folderPath);
//		folder.mkdirs();

		String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String currentHour = new SimpleDateFormat("HH-z").format(new Date());
		File folder = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/" + currentDate + "/" + currentHour + "/");

		if (!folder.exists()) {
			folder.mkdirs();
		}

		File logFile = new File(folder, String.format("%s.%s.csv", aModuleName, fileType));

		CSVWriter writer = null;
		try {
			if (!logFile.exists())
				logFile.createNewFile();

			writer = new CSVWriter(new FileWriter(logFile, true));

			String[] outArray = new String[arguments.length + 2];
			System.arraycopy(arguments, 0, outArray, 2, arguments.length);

			Date nowDate = cal.getTime();

			outArray[0] = String.valueOf(nowDate.getTime());
			outArray[1] = new SimpleDateFormat(dateFormatter).format(nowDate);

			writer.writeNext(outArray);
		} catch (IOException e) {
			android.util.Log.e(TAG, "Error while writing log: " + logFile.toString());
		} finally{
			if(writer != null){
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void writeToCSV(String aModuleName, String fileType, Boolean isEncrypt, Context mContext, Object... arguments) {

		if (Globals.IS_LOG_EXTERNAL) {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.w(aModuleName, "SD card is not available for logging: " + Environment.getExternalStorageState());
				return;
			}
		}

		Calendar cal = Calendar.getInstance();
		String folderPath = getFolderDateFormatForToday(cal);

		// Optional check for free space
		// disabled for now for performance reasons
		// if we're out of space, we'll just get an IOException
		// StatFs fs = new StatFs(folderPath);
		// if(fs.getAvailableBlocks() < 4)
		// {
		// Log.e("Error while writing log: Not enough space on the SD card");
		// return;
		// }

//		File folder = new File(getLogDirectory().getAbsolutePath() + File.separator + folderPath);
//		folder.mkdirs();

		String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String currentHour = new SimpleDateFormat("HH-z").format(new Date());
		File folder = new File("/sdcard/." + Globals.STUDY_NAME + "/logs/" + currentDate + "/" + currentHour + "/");
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File logFile = new File(folder, String.format("%s.%s.csv", aModuleName, fileType));

		CSVWriter writer = null;
		try {
			if (!logFile.exists())
				logFile.createNewFile();

			writer = new CSVWriter(new FileWriter(logFile, true));

			if(isEncrypt) {
				for (int i = 0 ; i < arguments.length ; i++) {
					arguments[i] = RSACipher.encrypt((String)arguments[i], mContext);
				}
			}

			String[] outArray = new String[arguments.length + 2];
			System.arraycopy(arguments, 0, outArray, 2, arguments.length);

			Date nowDate = cal.getTime();

			outArray[0] = String.valueOf(nowDate.getTime());
			outArray[1] = new SimpleDateFormat(dateFormatter).format(nowDate);

			writer.writeNext(outArray);
		} catch (IOException | GeneralSecurityException e) {
			android.util.Log.e(TAG, "Error while writing log: " + logFile.toString());
		} finally{
			if(writer != null){
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void flushBufferedMsgsToLogFiles() {
		if (msgBuffers.isEmpty())
			return;

		synchronized (syncObj) {
			Iterator<String> keysItr = msgBuffers.keySet().iterator();
			while (keysItr.hasNext()) {
				String key = keysItr.next();
				String[] comps = key.split("\\|");
				if (comps.length < 3)
					continue;
				writeToFile(comps[0], comps[1], comps[2], msgBuffers.get(key).toString(), Calendar.getInstance());
				msgBuffers.remove(key);
			}
		}
	}

	private static String LOG_TYPE_VERBOSE = "v";
	private static String LOG_TYPE_INFORMATIONAL = "i";
	private static HashMap<String, StringBuffer> msgBuffers = new HashMap<String, StringBuffer>();
	private static int MAX_BUFFER_SIZE = 8000;
	private static Object syncObj = new Object();

	private static void writeToTXT(String aModuleName, String logFileName, String logType, String message) {
		if (Globals.IS_LOG_EXTERNAL)
		{
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				android.util.Log.w(TAG, "SD card is not available for logging: " + Environment.getExternalStorageState());
				return;
			}
		}

		// hopefully "default" locale is correct here
		Calendar cal = Calendar.getInstance();

		Date nowDate = cal.getTime();

		StringBuilder sb = new StringBuilder();

		sb.append(String.valueOf(nowDate.getTime()));

		sb.append(',');
		sb.append(new SimpleDateFormat(dateFormatter).format(nowDate));

		sb.append(',');
		sb.append(logType);

		sb.append(',');
		sb.append(message);

		sb.append('\n');

		// Allow only one thread to write to the log file
		synchronized (syncObj) {
			if (LOG_TYPE_VERBOSE.equals(logType) || LOG_TYPE_INFORMATIONAL.equals(logType)) {
				String key = aModuleName + "|" + logFileName + "|" + logType;
				StringBuffer msgBuffer = msgBuffers.get(key);
				if (msgBuffer == null) {
					msgBuffer = new StringBuffer(MAX_BUFFER_SIZE + 1000);
					msgBuffers.put(key, msgBuffer);
				}
				msgBuffer.append(sb.toString());
				// TODO
				// if(msgBuffer.length() < MAX_BUFFER_SIZE)
				// return;

				sb = new StringBuilder();
				sb.append(msgBuffer.toString());
				msgBuffer.setLength(0);
			}
			writeToFile(aModuleName, logFileName, logType, sb.toString(), cal);
		}
	}

	private static void writeToTXT(String aModuleName, String logFileName, String logType, String message, Boolean isEncrypt, Context mContext) {
		if (Globals.IS_LOG_EXTERNAL)
		{
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				android.util.Log.w(TAG, "SD card is not available for logging: " + Environment.getExternalStorageState());
				return;
			}
		}

		// hopefully "default" locale is correct here
		Calendar cal = Calendar.getInstance();

		Date nowDate = cal.getTime();

		StringBuilder sb = new StringBuilder();

		sb.append(String.valueOf(nowDate.getTime()));

		sb.append(',');
		sb.append(new SimpleDateFormat(dateFormatter).format(nowDate));

		sb.append(',');
		sb.append(logType);

		sb.append(',');
		sb.append(message);

		sb.append('\n');

		// Allow only one thread to write to the log file
		synchronized (syncObj) {
			if (LOG_TYPE_VERBOSE.equals(logType) || LOG_TYPE_INFORMATIONAL.equals(logType)) {
				String key = aModuleName + "|" + logFileName + "|" + logType;
				StringBuffer msgBuffer = msgBuffers.get(key);
				if (msgBuffer == null) {
					msgBuffer = new StringBuffer(MAX_BUFFER_SIZE + 1000);
					msgBuffers.put(key, msgBuffer);
				}
				msgBuffer.append(sb.toString());
				// TODO
				// if(msgBuffer.length() < MAX_BUFFER_SIZE)
				// return;

				sb = new StringBuilder();
				sb.append(msgBuffer.toString());
				msgBuffer.setLength(0);
			}
			writeToFile(aModuleName, logFileName, logType, sb.toString(), cal, isEncrypt, mContext);
		}
	}

	private static void writeToFile(String aModuleName, String logFileName, String logType, String message, Calendar cal) {
		// add 1 to month because starts with 0, which is confusing
//		String folderPath = String.format(Globals.DEFAULT_LOGS_DIR + "logs/%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
//				cal.get(Calendar.DAY_OF_MONTH));

		String folderPath = String.format("/%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));

		// Optional check for free space
		// disabled for now for performance reasons
		// if we're out of space, we'll just get an IOException
		// StatFs fs = new StatFs(folderPath);
		// if(fs.getAvailableBlocks() < 4)
		// {
		// Log.e("Error while writing log: Not enough space on the SD card");
		// return;
		// }

		File folder = new File(getLogDirectory().getAbsolutePath() + File.separator + folderPath);
		folder.mkdirs();

		File logFile = new File(folder, String.format("%s.%s.csv", aModuleName, logFileName));
		FileOutputStream fos = null;
		try {
			if (!logFile.exists())
				logFile.createNewFile();

			fos = new FileOutputStream(logFile, true);
			fos.write(message.getBytes());
		} catch (IOException e) {
			android.util.Log.e(TAG, "Error while writing log: " + logFile.toString());
		} finally{
			if(fos != null){
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void writeToFile(String aModuleName, String logFileName, String logType, String message, Calendar cal, Boolean isEncrypt, Context mContext) {
		// add 1 to month because starts with 0, which is confusing
//		String folderPath = String.format(Globals.DEFAULT_LOGS_DIR + "logs/%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
//				cal.get(Calendar.DAY_OF_MONTH));

		String folderPath = String.format("/%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));

		// Optional check for free space
		// disabled for now for performance reasons
		// if we're out of space, we'll just get an IOException
		// StatFs fs = new StatFs(folderPath);
		// if(fs.getAvailableBlocks() < 4)
		// {
		// Log.e("Error while writing log: Not enough space on the SD card");
		// return;
		// }

		File folder = new File(getLogDirectory().getAbsolutePath() + File.separator + folderPath);
		folder.mkdirs();

		File logFile = new File(folder, String.format("%s.%s.csv", aModuleName, logFileName));

		FileOutputStream fos = null;
		try {
			if (!logFile.exists())
				logFile.createNewFile();

			if(isEncrypt) {
				message = RSACipher.encrypt(message, mContext);
			}

			fos = new FileOutputStream(logFile, true);
			fos.write(message.getBytes());
		} catch (IOException | GeneralSecurityException e) {
			android.util.Log.e(TAG, "Error while writing log: " + logFile.toString());
		} finally{
			if(fos != null){
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getStackTraceString(Exception e)
	{
		e.printStackTrace();
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String dummy = null;
	public static String getStackTrace(String msg)
	{
		try
		{
			if (dummy.contains("ss"))
				Log.d(TAG,"Junk");
		} catch (Exception e)
		{
			return msg + getStackTraceString(e);
		}

		return "Unable to get stack trace.";
	}

	public static void logStackTrace(String aTAG, Throwable ex) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		for(StackTraceElement trace : stackTrace){
			Log.e(aTAG, trace.toString());
		}
	}
}