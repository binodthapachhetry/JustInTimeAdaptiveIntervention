package edu.neu.android.wocketslib.dataupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.NetworkMonitor;
import edu.neu.android.wocketslib.utils.Zipper;

/**
 * Methods to process data that is stored on the internal or external memory,
 * and to move the data between the locations.
 */
public class DataSender {
    private static final String TAG = "DataSender";

    /*
     * Read from an inputStream as long as there is data and return a String.
     * Used to parse results returned from the server.
     */
    private static String convertStreamToString(InputStream anInputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(anInputStream));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error in convertStreamToString: " + e.toString());
            e.printStackTrace();
        } finally {
            try {
                anInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error in closing file in convertStreamToString: " + e.toString());
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Try to transmit WocketInfo as JSON data to the server, but if not
     * possible, queue the data (to the internal storage uploads directory) for
     * transmission later.
     *
     * @param aContext Context
     * @param wi       WocketInfo object storing information to send
     * @return True if successfully transmitted or queued
     */
    public static boolean transmitOrQueueWocketInfo(Context aContext, WocketInfo wi) {
        if (wi.isEmpty())
            return false;

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
        String aJSONString = gson.toJson(wi);
        wi.clear();
        if (aJSONString.compareTo("") == 0) {
            Log.e(TAG, "Empty JSON");
            return false;
        } else
            return transmitOrQueueJSON(aContext, aJSONString);
    }

    /**
     * Queue WocketInfo data as JSON files (to the internal storage uploads
     * directory) for later transmission to the server.
     *
     * @param aContext Context
     * @param wi       WocketInfo object storing information to send
     * @return True if successful
     */
    public static boolean queueWocketInfo(Context aContext, WocketInfo wi) {
        if (wi.isEmpty()) {
            return false;
        }


        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
        String aJSONString = gson.toJson(wi);
        Log.d(TAG, "JSON STRING: " + aJSONString); // TODO remove
        wi.clear();
        if (aJSONString.compareTo("") == 0) {
            Log.e(TAG, "Empty JSON");
            return false;
        } else
            return queueJsonData(aJSONString);
    }

    /**
     * Transmit a JSON string to the server, or queue it up for later
     * transmission.
     *
     * @param aContext    Context
     * @param aJSONString JSON String to transmit
     * @return True if successful sending or queuing
     */
    public static boolean transmitOrQueueJSON(Context aContext, String aJSONString) {
        boolean isNetwork = NetworkMonitor.isNetworkAvailable(aContext);

        if (isNetwork) {
            // Try to transmit if network is available
            boolean isTransmitted = transmitJSON(aJSONString);

            // If transmission was successful, just return
            if (isTransmitted)
                return true;
            else {
                Log.e(TAG, "Failed to transmit in transmitOrQueueJSON");
            }
        } else {
            Log.d(TAG, "Warning in transmitOrQueueJSON. No network detected.");
        }

        // Either transmission didn't work or network not available so queue
        // data for later transmission

        if (!queueJsonData(aJSONString)) {
            Log.e(TAG, "ERROR. Message will not be delivered. Could not transmit (because no network) or queue JSON string: " + aJSONString);
            return false;
        } else {
            Log.d(TAG, "Queue successful");
            return true;
        }
    }

    /**
     * Try to transmit a JSON string to the server.
     *
     * @param aJSONString JSON String to transmit
     * @return True if successfully transmitted
     */
    public static boolean transmitJSON(String aJSONString) {
        Log.d(TAG, "Try to transmit JSON: " + aJSONString);

        boolean isSuccess = true;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost httppost = new HttpPost(Globals.POST_ANDROID_DATA_LOG_URL);
            StringEntity se = new StringEntity(aJSONString, HTTP.UTF_8);
            se.setContentType("application/x-json");
            // httppost.addHeader("Accept-Encoding", "gzip");
            httppost.setEntity(se);

            httpclient.getParams().setParameter("http.connection.timeout", 3000);
            httpclient.getParams().setParameter("http.socket.timeout", 3000);

            // The commands below are the bottleneck
            // TODO Need to fix the timeout issue
            HttpResponse httpresponse = httpclient.execute(httppost);
            httpresponse.addHeader("Accept-Encoding", "gzip");
            HttpEntity resEntity = httpresponse.getEntity();
            InputStream is = resEntity.getContent();
            String return_result;

            // check if we got a compressed response back
            if (resEntity.getContentEncoding() != null && "gzip".equalsIgnoreCase(resEntity.getContentEncoding().getValue())) {
                return_result = Zipper.uncompressInputStream(is);
            } else {
                return_result = convertStreamToString(is);
            }

            if (return_result != null) {
                Pattern regex = Pattern.compile("(?<=<body><h1>)(.+?)(?=<)");
                Matcher m = regex.matcher(return_result);
                if (m.find()) {
                    isSuccess = false;
                    Log.e(TAG, "Error. Transmission of JSON to server not successful: " + return_result + " MATCH: " + m.group());
                }
            } else {
                Log.d(TAG, "SUCCESS! Transmission of JSON to server successful: " + httpresponse.getStatusLine());
            }
        } catch (Exception e) {
            isSuccess = false;
            Log.e(TAG, "Error in transmission of JSON to server: " + e.toString());
            e.printStackTrace();
        }
        return isSuccess;
    }

    /**
     * Queue JSON data to the internal uploads directory.
     *
     * @param aJSONString JSON String to queue in internal uploads directory
     * @return True if successfully saved
     */
    public static boolean queueJsonData(String aJSONString) {
        // TODO might want to change this to queue to the external if it is
        // available to avoid losing data on an uninstall?
        String fileName = System.currentTimeMillis() + ".json";
        boolean isSaved = FileHelper.saveStringToFile(aJSONString, Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY, fileName, false);

        if (!isSaved)
            Log.e(TAG, "Error. Could not save JSON data file to internal storage to queue for transmission. " + fileName);

        return isSaved;
    }

    /**
     * Delete all .json or .json.zip files in the internal upload directory.
     *
     * @return True if all files were successfully deleted
     */
    public static boolean deleteQueuedJsonDataInternal() {
        boolean allDeleted = true;
        String[] someJSONFiles = DataManager.getJSONFileNamesIntUploadDir();

        for (String aFile : someJSONFiles) {
            Log.d(TAG, "Deleting internal JSON file: " + aFile);
            if (!FileHelper.deleteFile(aFile)) {
                Log.e(TAG, "Could not delete file: " + aFile);
                allDeleted = false;
            }
        }
        return allDeleted;
    }

//	/**
//	 * Delete all .json or .json.zip files in the external upload directory.
//	 *
//	 * @param aContext Context
//	 * @return True if all files were successfully deleted
//	 */
//	public static boolean deleteQueuedJsonDataExternal(Context aContext) {
//		boolean allDeleted = true;
//		String[] someJSONFiles = DataManager.getJSONFileNamesExtUploadDir();
//
//		for (String aFile : someJSONFiles) {
//			Log.d(TAG, "Deleting external JSON file: " + aFile);
//			if (!FileHelper.deleteFile(aFile)) {
//				Log.e(TAG, "Could not delete file: " + aFile);
//				allDeleted = false;
//			}
//		}
//		return allDeleted;
//	}

//	/**
//	 * Delete all the data in the internal uploads directory EXCEPT for .json
//	 * and .json.zip files.
//	 *
//	 * @param aContext Context
//	 * @return True if all the eligible files are deleted.
//	 */
//	public static boolean deleteQueuedRawDataInternal(Context aContext) {
//		boolean allDeleted = true;
//		String[] someDataFiles = DataManager.getNonJSONFileNamesIntUploadDir();
//
//		for (String aFile : someDataFiles) {
//			Log.d(TAG, "Deleting internal data file: " + aFile);
//			if (!FileHelper.deleteFile(aFile)) {
//				Log.e(TAG, "Could not delete file: " + aFile);
//				allDeleted = false;
//			}
//		}
//		return allDeleted;
//	}

    /**
     * Delete all the data in the external uploads directory EXCEPT for .json
     * and .json.zip files.
     *
     * @return True if all the eligible files are deleted.
     */
    public static boolean deleteQueuedRawDataExternal() {
        boolean allDeleted = true;
        String[] someDataFiles = DataManager.getNonJSONFileNamesExtDir();

        for (String aFile : someDataFiles) {
            Log.d(TAG, "Deleting external data file: " + aFile);
            if (!FileHelper.deleteFile(aFile)) {
                Log.e(TAG, "Could not delete file: " + aFile);
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    /**
     * Transmit queued JSON data that is queued up in the internal uploads
     * directory. JSON files are deleted when they are successfully transfered.
     *
     * @param aContext Context
     * @return Number of files remaining (combined from both directories)
     */
    public static int transmitQueuedJsonDataInternal(Context aContext) {
        String[] someJSONFileNames = DataManager.getJSONFileNamesIntUploadDir();

        if (someJSONFileNames == null) {
            Log.d(TAG, "Warning: queued JSON internal directory probably does not exist. No files to move.");
            return 0;
        } else {
            int filesRemaining = someJSONFileNames.length;

            boolean isSuccess;
            String aJSONString;
            for (String aJSONFileName : someJSONFileNames) {
                Log.d(TAG, "Transmitting from internal: " + aJSONFileName);
                aJSONString = FileHelper.readStringFromFile(aJSONFileName);
                isSuccess = transmitJSON(aJSONString);
                if (isSuccess) {
                    Log.d(TAG, "Deleting file from internal queue: " + aJSONFileName);
                    FileHelper.deleteFile(aJSONFileName);
                    filesRemaining--;
                }
            }

            someJSONFileNames = DataManager.getJSONFileNamesIntUploadDir();

            // TODO remove ... only for testing
            if (Globals.IS_DEBUG)
                for (String aJSONFileName : someJSONFileNames) {
                    Log.i(TAG, "AFTER Queued file for transmission: " + aJSONFileName);
                }

            return filesRemaining;
        }
    }

    public static void sendInternalUploadDataToExternalUploadDir(Context aContext, boolean isJSONOnly, boolean isCompress) {

        Log.o(TAG, "MoveData", "InternalJSONToExternalUploads", Boolean.toString(isCompress));
        if (Globals.IS_DEBUG)
            Log.d(TAG, "Start move data EXTERNAL");
        int jsonRemaining = sendQueuedJsonDataToExternalUploadDir(aContext, isCompress);
        Log.o(TAG, "MoveData", "FilesRemaining", Integer.toString(jsonRemaining));

        if (!isJSONOnly) {
            Log.o(TAG, "MoveData", "InternalDataUploadsToExternalUploads", Boolean.toString(isCompress));
            int filesRemaining = sendQueuedDataUploadsToExternalUploadDir(aContext, isCompress);
            Log.o(TAG, "MoveData", "FilesRemaining", Integer.toString(filesRemaining));
        }
        if (Globals.IS_DEBUG)
            Log.d(TAG, "End move data EXTERNAL");
    }

    public static int sendQueuedJsonDataToExternalUploadDir(Context aContext, boolean isCompress) {
        String[] someJSONFileNames = DataManager.getJSONFileNamesIntUploadDir();

        if (someJSONFileNames == null) {
            // TODO change to warning
            Log.e(TAG, "Warning: queued JSON internal directory probably does not exist. No files to move.");
            return 0;
        } else {
            boolean isMoved = false;
            for (String aJSONFileName : someJSONFileNames) {
                Log.d(TAG, "BEFORE Queued file (" + aJSONFileName + ") moved to ExternalUploadDir");
                isMoved = FileHelper.moveToExternalUploadDirectory(aContext, new File(aJSONFileName), isCompress, true);
                if (isMoved) {
                    if (Globals.IS_DEBUG)
                        Log.i(TAG, "File (" + aJSONFileName + ") successfully moved to ExternalUploadDir");
                } else
                    Log.e(TAG, "ERROR: File (" + aJSONFileName + ") was NOT moved to ExternalUploadDir");
            }

            someJSONFileNames = DataManager.getJSONFileNamesIntUploadDir();
            int filesRemaining;
            if (someJSONFileNames != null)
                filesRemaining = someJSONFileNames.length;
            else
                filesRemaining = 0;
            return filesRemaining;
        }
    }

    public static int sendQueuedDataUploadsToExternalUploadDir(Context aContext, boolean isCompress) {
        String[] someFileNames = DataManager.getNonJSONFileNamesIntUploadDir();

        int filesRemaining = 0;
        if (someFileNames == null) {
            Log.d(TAG, "No files in internal data uploads directory to move to external.");
            return 0;
        } else {
            for (String aFileName : someFileNames) {
                Log.d(TAG, "BEFORE Queued file (" + aFileName + ") moved to ExternalUploadDir");
                boolean isMoved = FileHelper.transferToExternalUploadDirectory(aContext, new File(aFileName), isCompress, true);
                if (isMoved) {
                    Log.d(TAG, "File (" + aFileName + ") successfully moved to ExternalUploadDir");
                } else
                    Log.e(TAG, "ERROR: File (" + aFileName + ") was NOT moved to ExternalUploadDir");
            }

            someFileNames = DataManager.getNonJSONFileNamesIntUploadDir();
            if (someFileNames != null)
                filesRemaining = someFileNames.length;
            else
                filesRemaining = 0;

        }
        return filesRemaining;
    }

    private static final int DIRTYPE_LOGS = 1;
    private static final int DIRTYPE_SURVEYS = 2;
    private static final int DIRTYPE_DATA = 3;
    private static final int DIRTYPE_DATA_MHEALTH = 4;
    private static final int DIRTYPE_DATA_MHEALTH_SENSORS = 5;
    private static final int DIRTYPE_DATA_MHEALTH_MASTER = 6;
    private static final int DIRTYPE_DATA_MHEALTH_RAW = 7;
    private static final int DIRTYPE_DATA_MHEALTH_METADATA = 8;
    private static final boolean SEND_EXTERNAL = true;
    private static final boolean SEND_INTERNAL = false;
    private static final boolean FROM_EXTERNAL = true;
    private static final boolean FROM_INTERNAL = false;

    //TODO fix so there is no possibility of an overwrite?
    //This can overwrite because the files are moved not copied, and then they will have the same name. Dangerous. Need to fix.
    public static int sendDateDirToUploadDir(Context aContext, boolean isCompress, Date beforeDate, int dirType, boolean isFromExternal, boolean isSendExternal) {
        return copyOrMoveDateDirToUploadDir(aContext, isCompress, beforeDate, dirType, isFromExternal, isSendExternal, true);
    }

    public static int copyDateDirToUploadDir(Context aContext, boolean isCompress, Date beforeDate, int dirType, boolean isFromExternal, boolean isSendExternal) {
        return copyOrMoveDateDirToUploadDir(aContext, isCompress, beforeDate, dirType, isFromExternal, isSendExternal, false);
    }

    public static int copyOrMoveDateDirToUploadDir(Context aContext, boolean isCompress, Date beforeDate, int dirType, boolean isFromExternal, boolean isSendExternal, boolean isMove) {

        File aLogDir;
        String dirName = "";
        int recursiveLevel = 0;

        if(isSendExternal){
            dirName = Globals.EXTERNAL_DIRECTORY_PATH;
        }else{
            dirName = Globals.INTERNAL_DIRECTORY_PATH;
        }

        if (dirType == DIRTYPE_LOGS)
            dirName += File.separator + Globals.LOG_DIRECTORY;
        else if (dirType == DIRTYPE_SURVEYS)
            dirName += File.separator + Globals.SURVEY_LOG_DIRECTORY;
        else if (dirType == DIRTYPE_DATA)
            dirName = Globals.DATA_DIRECTORY;
        else if (dirType == DIRTYPE_DATA_MHEALTH_SENSORS)
            dirName += File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY;
        else if (dirType == DIRTYPE_DATA_MHEALTH_MASTER) {
            try {
                mHealthFormat.setSubjectId(DataStorage.GetSubjectID(aContext));
                dirName = mHealthFormat.buildmHealthPath(new Date(), mHealthFormat.PATH_LEVEL.ROOT, mHealthFormat.ROOT_DIRECTORY.MASTER);
            } catch (Exception e) {
                e.printStackTrace();
            }
            recursiveLevel = 2;
        }
        else {
            Log.e(TAG, "Invalid dirType used in sendDateDirToUploadDir: " + dirType);
            return 0;
        }

        aLogDir = new File(dirName);
        Log.i(TAG, "Constructed path: " + aLogDir);

        if (!aLogDir.exists()) {
            if (Globals.IS_DEBUG)
                Log.i(TAG, "Cannot copyOrMoveDateDirToUploadDir for directory because does not exist yet: " + aLogDir);
            return 0;
        }

        ArrayList<String> someDirs = FileHelper.getRecusiveDirs(aLogDir.getAbsolutePath(), recursiveLevel);

        Date tempDate;

        if (someDirs == null) {
            if (Globals.IS_DEBUG)
                Log.i(TAG, "Cannot copyOrMoveDateDirToUploadDir for directory because found no files in directory: " + aLogDir);
            return 0;
        }

        String date;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        // Loop over each day directory for the logs. This assumes there is
        // one directory per day and they are in the format: [insert]
        // Assumes directories are not in any other format and that there
        // are no stray files
        for (String aDir : someDirs) {

            // TODO Unfortunately Yifei and Bin did not save survey data and other data in the standard
            // format. This needs to be fixed. However, temporarily, here is
            // a hack to deal with the format
//            if (dirType == DIRTYPE_SURVEYS)
//                date = aDir.substring(aDir.lastIndexOf("_") + 1);
//            else
            if (dirType == DIRTYPE_DATA_MHEALTH_MASTER || dirType == DIRTYPE_DATA_MHEALTH_RAW || dirType == DIRTYPE_DATA_MHEALTH_METADATA){
                String[] tokens = aDir.split(File.separator);
                date = tokens[tokens.length - 3] + "-" + tokens[tokens.length-2] + "-" + tokens[tokens.length - 1];
            }else{
                date = aDir.substring(aDir.lastIndexOf("/") + 1);
            }


            // Check if we see a date (i.e. dirs that are date stamped as they are supposed to be
            // Always upload if directory not in date format except for mHealth data.
            // Otherwise, only upload if data is before given date
            boolean isDoUpload = false;

            // Detect special case of mhealth directory
            if ((dirType == DIRTYPE_DATA) && (date.equals("mhealth"))) {
                Log.d(TAG, "Excluding mHealth data from data uploads (must run that separately)");
                isDoUpload = false;
            }
            else
            {
                // Try to parse the date
                try {
                    tempDate = format.parse(date);
                    isDoUpload = DateHelper.isDateBefore(tempDate, beforeDate);
                    if (!isDoUpload)
                        if (Globals.IS_DEBUG)
                            Log.i(TAG, "Did not send file because " + tempDate + " is not before " + beforeDate);
                } catch (ParseException e) {
                    Log.e(TAG, "Error parse exception in sendDateDirToUploadDir. Data not in day-by-day date format. Error: " + e.toString());
                    Log.e(TAG, "FileName: " + aDir + " Date: " + date);
                    e.printStackTrace();
                    isDoUpload = true;
                }

            }

            if (isDoUpload) {
                if (isSendExternal)
                    FileHelper.moveToExternalUploadDirectory(aContext, new File(aDir), true, isMove);
                else
                    FileHelper.moveToInternalUploadDirectory(aContext, new File(aDir), true, isMove);
            }
        }

        //TODO this doesn't make sense for files copied
        someDirs = FileHelper.getRecusiveDirs(aLogDir.getAbsolutePath(), recursiveLevel);
        int dirsRemaining;
        if (someDirs != null)
            dirsRemaining = someDirs.size();
        else
            dirsRemaining = 0;
        Log.d(TAG, "Dirs remaining: " + dirsRemaining);

        return dirsRemaining;
    }

    public static int sendDateDirToUploadDirCheckToday(Context aContext, boolean isCompress, int dirType, boolean isFromExternal, boolean isSendExternal, boolean isIncludeToday) {
        if (isIncludeToday) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1); // Get the next day
            return sendDateDirToUploadDir(aContext, isCompress, cal.getTime(), dirType, isFromExternal, isSendExternal);
        } else {
            // Don't upload logs from today (to avoid file conflicts and
            // overwriting data on server)
            return sendDateDirToUploadDir(aContext, isCompress, new Date(), dirType, isFromExternal, isSendExternal);
        }
    }

    public static int copyDateDirToUploadDirCheckToday(Context aContext, boolean isCompress, int dirType, boolean isFromExternal, boolean isSendExternal, boolean isIncludeToday) {
        if (isIncludeToday) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1); // Get the next day
            return copyDateDirToUploadDir(aContext, isCompress, cal.getTime(), dirType, isFromExternal, isSendExternal);
        } else {
            // Don't upload logs from today (to avoid file conflicts and
            // overwriting data on server)
            return copyDateDirToUploadDir(aContext, isCompress, new Date(), dirType, isFromExternal, isSendExternal);
        }
    }

    public static int sendLogsToExternalUploadDir(Context aContext, boolean isCompress, Date beforeDate) {
        Log.i(TAG, Globals.IS_LOG_EXTERNAL.toString());
        if (Globals.IS_LOG_EXTERNAL)
            return sendDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_LOGS, FROM_EXTERNAL, SEND_EXTERNAL);
        else
            return sendDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_LOGS, FROM_INTERNAL, SEND_EXTERNAL);
    }

    public static int sendLogsToInternalUploadDir(Context aContext, boolean isCompress, Date beforeDate) {
        if (Globals.IS_LOG_EXTERNAL)
            return sendDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_LOGS, FROM_EXTERNAL, SEND_INTERNAL);
        else
            return sendDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_LOGS, FROM_INTERNAL, SEND_INTERNAL);
    }

    public static int copyExternalSurveyLogsToExternalUploadDir(Context aContext, boolean isCompress, Date beforeDate) {
        return copyDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_SURVEYS, FROM_EXTERNAL, SEND_EXTERNAL);
    }

    public static int copyInternalSurveyLogsToInternalUploadDir(Context aContext, boolean isCompress, Date beforeDate) {
        return copyDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_SURVEYS, FROM_INTERNAL, SEND_INTERNAL);
    }

    public static int copyInternalDataLogsToExternalUploadDir(Context aContext, boolean isCompress, Date beforeDate) {
        return copyDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_DATA, FROM_INTERNAL, SEND_EXTERNAL);
    }

    public static int copyInternalDataLogsToInternalUploadDir(Context aContext, boolean isCompress, Date beforeDate) {
        return copyDateDirToUploadDir(aContext, isCompress, beforeDate, DIRTYPE_DATA, FROM_INTERNAL, SEND_INTERNAL);
    }

    public static int sendLogsToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        if (Globals.IS_LOG_EXTERNAL)
            return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
        else
            return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_INTERNAL, SEND_EXTERNAL, isIncludeToday);
    }

    public static int copyLogsToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        if (Globals.IS_LOG_EXTERNAL)
            return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
        else
            return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_INTERNAL, SEND_EXTERNAL, isIncludeToday);
    }
//
//    public static int sendMHealthToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
//        if (Globals.IS_MHEALTH_EXTERNAL)
//            return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_SENSORS, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
//        else
//            return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_SENSORS, FROM_INTERNAL, SEND_EXTERNAL, isIncludeToday);
//    }

    public static void sendMHealthToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        boolean fromExternal;
        if (Globals.IS_MHEALTH_EXTERNAL)
            fromExternal = true;
        else
            fromExternal = false;

        if(Globals.UPLOAD_MHEALTH_MASTER_DIRECTORY)
            sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_MASTER, fromExternal, SEND_EXTERNAL, isIncludeToday);
        if(Globals.UPLOAD_MHEALTH_RAW_DIRECTORY)
            sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_RAW, fromExternal, SEND_EXTERNAL, isIncludeToday);
        if(Globals.UPLOAD_MHEALTH_METADATA_DIRECTORY)
            sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_METADATA, fromExternal, SEND_EXTERNAL, isIncludeToday);

    }

    public static void sendMHealthToExternalUploadDirForToday(Context aContext, boolean isCompress) {

        final Date now = new Date();
        String todayDir = null;
        try {
            todayDir = mHealthFormat.buildmHealthPath(now, mHealthFormat.PATH_LEVEL.DAILY, mHealthFormat.ROOT_DIRECTORY.MASTER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        File todayDirFile = new File(todayDir);
        if (todayDirFile.exists() && todayDirFile.isDirectory()) {
            File[] toBeZipped = todayDirFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && Integer.valueOf(pathname.getName()) < now.getHours();
                }
            });
            if (isCompress) {
                File originFile = new File(todayDirFile.getAbsolutePath() + ".zip");
                File destination = FileHelper.getUploadsExternalDirFile(aContext, originFile);

                destination.delete();

                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(destination);
                    if(destination.exists()){
                        Log.i(TAG, "Destination zip file exists, add files to it: " + destination);
                    }
                } catch (ZipException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    Log.logStackTrace(TAG, e);
                    return;
                }
                ZipParameters paras = new ZipParameters();
                paras.setIncludeRootFolder(true);
                paras.setRootFolderInZip(todayDirFile.getName());
                paras.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                paras.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
                for (File toBeZippedFolder : toBeZipped) {
                    try {
                        zipFile.addFolder(toBeZippedFolder, paras);
                    } catch (ZipException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error while zipping folder: " + toBeZippedFolder.getAbsolutePath() + ", skip current file");
                        Log.e(TAG, e.getMessage());
                        Log.logStackTrace(TAG, e);
                    }
                }
            }
        }
    }

//    public static int copyMHealthToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
//        if (Globals.IS_MHEALTH_EXTERNAL)
//            return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_SENSORS, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
//        else
//            return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_SENSORS, FROM_INTERNAL, SEND_EXTERNAL, isIncludeToday);
//    }

    public static void copyMHealthToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        boolean fromExternal;
        if (Globals.IS_MHEALTH_EXTERNAL)
            fromExternal = true;
        else
            fromExternal = false;

        if(Globals.UPLOAD_MHEALTH_MASTER_DIRECTORY)
            copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_MASTER, fromExternal, SEND_EXTERNAL, isIncludeToday);
        if(Globals.UPLOAD_MHEALTH_RAW_DIRECTORY)
            copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_RAW, fromExternal, SEND_EXTERNAL, isIncludeToday);
        if(Globals.UPLOAD_MHEALTH_METADATA_DIRECTORY)
            copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA_MHEALTH_METADATA, fromExternal, SEND_EXTERNAL, isIncludeToday);

    }

    public static int sendLogsToInternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        if (Globals.IS_LOG_EXTERNAL)
            return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_EXTERNAL, SEND_INTERNAL, isIncludeToday);
        else
            return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_INTERNAL, SEND_INTERNAL, isIncludeToday);
    }

    public static int copyLogsToInternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        if (Globals.IS_LOG_EXTERNAL)
            return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_EXTERNAL, SEND_INTERNAL, isIncludeToday);
        else
            return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_LOGS, FROM_INTERNAL, SEND_INTERNAL, isIncludeToday);
    }

    public static int copyExternalSurveyLogsToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_SURVEYS, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
    }
    public static int sendExternalSurveyLogsToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_SURVEYS, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
    }

    public static int copyInternalSurveyLogsToInternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_SURVEYS, FROM_INTERNAL, SEND_INTERNAL, isIncludeToday);
    }
    public static int sendInternalSurveyLogsToInternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        return sendDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_SURVEYS, FROM_INTERNAL, SEND_INTERNAL, isIncludeToday);
    }

    public static int copyInternalDataLogsToInternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA, FROM_INTERNAL, SEND_INTERNAL, isIncludeToday);
    }

    public static int copyExternalDataLogsToExternalUploadDir(Context aContext, boolean isCompress, boolean isIncludeToday) {
        return copyDateDirToUploadDirCheckToday(aContext, isCompress, DIRTYPE_DATA, FROM_EXTERNAL, SEND_EXTERNAL, isIncludeToday);
    }

    // public static boolean queueJsonData(Context aContext, String aJSONString)
    // {
    // String fileName = System.currentTimeMillis() + ".json";
    // boolean isSaved = FileUtils.saveStringInternal(aContext, aJSONString,
    // QUEUE_DIRNAME, fileName);
    //
    // if (!isSaved)
    // Log.e(TAG,
    // "Error. Could not save JSON data file to internal storage to queue for transmission. "
    // + fileName);
    //
    // return isSaved;
    // }
    //
    // public static int sendQueuedJsonData(Context aContext)
    // {
    // String[] children = FileUtils.getFileNamesInternal(aContext,
    // QUEUE_DIRNAME);
    // int filesRemaining = children.length;
    // for (String c: children)
    // {
    // Log.e(TAG, "BEFORE Queued file for transmission: " + c);
    // }
    //
    // boolean isSuccess;
    // String aJSONString;
    // for (String c: children)
    // {
    // Log.e(TAG, "Transmitting: " + c);
    // aJSONString = FileUtils.readStringInternal(aContext, QUEUE_DIRNAME, c);
    // isSuccess = transmitJSON(aJSONString);
    // if (isSuccess)
    // {
    // Log.e(TAG, "Deleting file from queue: " + c);
    // FileUtils.deleteStringInternal(aContext, QUEUE_DIRNAME, c);
    // filesRemaining--;
    // }
    // }
    //
    // children = FileUtils.getFileNamesInternal(aContext, QUEUE_DIRNAME);
    // for (String c: children)
    // {
    // Log.e(TAG, "AFTER Queued file for transmission: " + c);
    // }
    //
    // return filesRemaining;
    // }
    //
    // public static String fileWithDirectoryPath(Context aContext, String
    // aFilePath)
    // {
    // String s = "file." + aFilePath + "." + System.currentTimeMillis() +
    // ".file";
    //
    // return s;
    // }

    // public static boolean queueFile(Context aContext, String aFileName)
    // {
    // String fileName = aFileName + "." + System.currentTimeMillis() + ".file";
    // boolean isSaved = FileUtils.saveFileInternal(aContext, aFileName,
    // QUEUE_DIRNAME, fileName);
    //
    // if (!isSaved)
    // Log.e(TAG,
    // "Error. Could not save JSON data file to internal storage to queue for transmission. "
    // + fileName);
    //
    // return isSaved;
    // }
    //
    // public static boolean queueCompressedFile(Context aContext, String
    // aFileName)
    // {
    // // Zip any file transmitted
    // GZipper.zip(aFileName);
    //
    // String fileName = aFileName + "." + System.currentTimeMillis() + ".file";
    // boolean isSaved = FileUtils.saveFileInternal(aContext, aFileName,
    // QUEUE_DIRNAME, fileName);
    //
    // if (!isSaved)
    // Log.e(TAG,
    // "Error. Could not save JSON data file to internal storage to queue for transmission. "
    // + fileName);
    //
    // return isSaved;
    // }
    //
    //
    //
    // public static int sendQueuedFiles(Context aContext)
    // {
    // String[] children = FileUtils.getFileNamesInternal(aContext,
    // QUEUE_DIRNAME);
    // int filesRemaining = children.length;
    // for (String c: children)
    // {
    // Log.e(TAG, "BEFORE Queued file for transmission: " + c);
    // }
    //
    // boolean isSuccess;
    // String aJSONString;
    // for (String c: children)
    // {
    // Log.e(TAG, "Transmitting: " + c);
    // aJSONString = FileUtils.readStringInternal(aContext, QUEUE_DIRNAME, c);
    // isSuccess = transmitJSON(aJSONString);
    // if (isSuccess)
    // {
    // Log.e(TAG, "Deleting file from queue: " + c);
    // FileUtils.deleteStringInternal(aContext, QUEUE_DIRNAME, c);
    // filesRemaining--;
    // }
    // }
    //
    // children = FileUtils.getFileNamesInternal(aContext, QUEUE_DIRNAME);
    // for (String c: children)
    // {
    // Log.e(TAG, "AFTER Queued file for transmission: " + c);
    // }
    //
    // return filesRemaining;
    // }
    //

    // public static int sendQueuedJsonDataInternalUploadDir(Context aContext)
    // {
    // String[] children = FileUtils.getFileNamesInternal(aContext,
    // Globals.DEFAULT_QUEUE_DIRNAME);
    //
    // int filesRemaining = 0;
    // if (children == null)
    // {
    // Log.e(TAG,
    // "Warning: queued JSON internal directory probably does not exist. No files to move.");
    // return 0;
    // }
    // else
    // {
    // filesRemaining = children.length;
    // File aFile;
    // boolean isMoved;
    // for (String c: children)
    // {
    // aFile = FileUtils.getFileInternal(aContext,
    // Globals.DEFAULT_QUEUE_DIRNAME, c);
    // isMoved = FileUtils.saveToInternalUploadDirectory(aContext, aFile, true);
    // // remove
    // if (isMoved)
    // {
    // if (Globals.IS_DEBUG)
    // Log.i(TAG, "File successfully moved to InternalUploadDir: " + c);
    // }
    // else
    // Log.e(TAG, "ERROR: File was not moved to InternalUploadDir: " + c);
    // }
    //
    // children = FileUtils.getFileNamesInternal(aContext,
    // Globals.DEFAULT_QUEUE_DIRNAME);
    // filesRemaining = children.length;
    //
    // return filesRemaining;
    // }
    // }

    // public static int getNumFilesExternalUploadDir(Context aContext)
    // {
    // String[] children = FileUtils.getFileNamesExternal(aContext,
    // Globals.UPLOADS_DIRECTORY);
    // if (children == null)
    // return 0;
    // else
    // return children.length;
    // }
    //

}
