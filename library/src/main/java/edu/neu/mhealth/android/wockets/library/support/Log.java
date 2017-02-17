package edu.neu.mhealth.android.wockets.library.support;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;

import java.io.PrintWriter;
import java.io.StringWriter;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

/**
 * @author Dharam Maniar
 */

public class Log {
    /**
     * Write log entry to the file
     *
     * @param logType   The type of log.
     * @param TAG       The class in which the log message was generated
     * @param message   The message to log
     */
    private static void writeLogToFile(String logType, String TAG, String message, Context mContext) {
	    String[] logEntry = {DateTime.getCurrentTimestampString(), logType, UserManager.getUserEmail(), TAG, message};
	    String logDirectory = DataManager.getDirectoryLogs(mContext);
	    String logFileMaster = logDirectory  + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/master.log.csv";
        String logFileTAG = logDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + TAG +  ".log.csv";
        CSV.write(logEntry, logFileMaster, true);
	    CSV.write(logEntry, logFileTAG, true);
    }

    /**
     * Write error stack to the file
     *
     * @param logType           The type of log
     * @param TAG               The class in which the log message was generated
     * @param exceptionAsString The error
     */
    private static void writeErrorToFile(String logType, String TAG, String exceptionAsString, Context mContext) {

	    String[] logEntry = {DateTime.getCurrentTimestampString(), logType, UserManager.getUserEmail(), TAG, exceptionAsString};

	    String logDirectory = DataManager.getDirectoryLogs(mContext);

	    String logFileMaster = logDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/master.err.log";
	    String logFileTAG = logDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + TAG +  ".err.log";
	    CSV.write(logEntry, logFileMaster, true);
	    CSV.write(logEntry, logFileTAG, true);
    }

    /**
     * Verbose Logging. Value will not be stored in any file. Will just be used to display the message
     * in the logcat.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     */
    public static void v(String TAG, String message) {
        android.util.Log.v(TAG, message);
    }

    /**
     * Verbose Logging. Value will not be stored in any file. Will just be used to display the message
     * in the logcat.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     * @param throwable Throwable with exception stacktrace
     */
    public static void v(String TAG, String message, Throwable throwable) {
        android.util.Log.v(TAG, message, throwable);
    }

    /**
     * Debug Logging. Value will not be stored in any file. Will just be used to display the message
     * in the logcat.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     */
    public static void d(String TAG, String message) {
        android.util.Log.d(TAG, message);
    }

    /**
     * Debug Logging. Value will not be stored in any file. Will just be used to display the message
     * in the logcat.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     * @param throwable Throwable with exception stacktrace
     */
    public static void d(String TAG, String message, Throwable throwable) {
        android.util.Log.d(TAG, message, throwable);
    }

    /**
     * Info Logging. Value will be stored in the log files.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     */
    public static void i(String TAG, String message, Context mContext) {
        android.util.Log.i(TAG, message);
        writeLogToFile("I", TAG, message, mContext);
    }

    /**
     * Info Logging. Value will be stored in the log file along with the stacktrace from the throwable.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     * @param throwable Throwable with exception stacktrace
     */
    public static void i(String TAG, String message, Throwable throwable, Context mContext) {
        String exceptionAsString = getExceptionAsString(throwable);
        android.util.Log.i(TAG, message, throwable);
        writeLogToFile("I", TAG, message, mContext);
        writeErrorToFile("I", TAG, exceptionAsString, mContext);
    }

    /**
     * Warn Logging. Value will be stored in the log files.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     */
    public static void w(String TAG, String message, Context mContext) {
        android.util.Log.w(TAG, message);
        writeLogToFile("W", TAG, message, mContext);
    }

    /**
     * Warn Logging. Value will be stored in the log file along with the stacktrace from the throwable.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     * @param throwable Throwable with exception stacktrace
     */
    public static void w(String TAG, String message, Throwable throwable, Context mContext) {
        String exceptionAsString = getExceptionAsString(throwable);
        android.util.Log.w(TAG, message, throwable);
        writeLogToFile("W", TAG, message, mContext);
        writeErrorToFile("W", TAG, exceptionAsString, mContext);
    }

    /**
     * Warn Logging. Value will be stored in the log file along with the stacktrace from the throwable.
     *
     * @param TAG       The class in which the log message was generated
     * @param throwable Throwable with exception stacktrace
     */
    public static void w(String TAG, Throwable throwable, Context mContext) {
        String exceptionAsString = getExceptionAsString(throwable);
        android.util.Log.w(TAG, throwable);
        writeErrorToFile("W", TAG, exceptionAsString, mContext);
    }

    /**
     * Error Logging. Value will be stored in the log files.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     */
    public static void e(String TAG, String message, Context mContext) {
        android.util.Log.e(TAG, message);
        writeLogToFile("E", TAG, message, mContext);

        FirebaseCrash.log(UserManager.getUserEmail() + " - " + message);
    }

    /**
     * Error Logging. Value will be stored in the log file along with the stacktrace from the throwable.
     *
     * @param TAG       The class in which the log message was generated
     * @param message   The message to be logged
     * @param throwable Throwable with exception stacktrace
     */
    public static void e(String TAG, String message, Throwable throwable, Context mContext) {
        String exceptionAsString = getExceptionAsString(throwable);

        android.util.Log.e(TAG, message, throwable);
        writeLogToFile("E", TAG, message, mContext);
        writeErrorToFile("E", TAG, exceptionAsString, mContext);

        FirebaseCrash.log(UserManager.getUserEmail() + " - " + message + " - " + exceptionAsString);
    }

    private static String getExceptionAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
