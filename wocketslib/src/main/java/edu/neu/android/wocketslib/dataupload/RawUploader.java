package edu.neu.android.wocketslib.dataupload;

import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.json.model.FileUploadEvent;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.NetworkMonitor;
import edu.neu.android.wocketslib.utils.PhoneInfo;
import edu.neu.android.wocketslib.utils.Zipper;

/**
 * Support for raw upload of files from the phone to the server.
 */
public class RawUploader {
    private static final String TAG = "RawUploader";

    //    private static final int HTTP_RESPONSE_SERVER_UP = 200;
    private static final int BUFFER = 2048;

    /**
     * Get an object used to send a message to the server about an upload start
     * using the JSON model. THis is called at thet start, but getEndFileUploadEvent should be called
     * to fill in the end time and isSuccessful fields.
     *
     * @param aFile File to grab information about
     * @param startDataTime Upload start time
     * @param endDataTime Upload end time
     * @return FileUploadEvent object for JSON
     */
    private static FileUploadEvent getStartFileUploadEvent(File aFile, Date startDataTime, Date endDataTime) {
        FileUploadEvent fue = new FileUploadEvent();
        // TODO FIX so bytes is a long
        if (aFile != null)
            fue.bytes = (int) aFile.length();
        // value in WocketInfo
        fue.startDataTime = startDataTime;
        fue.endDataTime = endDataTime;
        if (aFile != null) {
            fue.fileName = aFile.getAbsolutePath();
        } else
            fue.fileName = null;
        fue.isSuccessful = false;
        fue.note = "File upload event";
        fue.startUploadTime = new Date();
        fue.endUploadTime = null;
        return fue;
    }

    /**
     * Get an object used to send a message to the server about an upload
     * completion using the JSON model
     *
     * @param aFUE A FileUploadEvent object started with getStartFileUploadEvent
     * @param isSuccessful True if the upload was successful
     * @param msg String message to include in the note
     * @return The FileUploadEvent object that can be used to create the JSON
     */
    private static FileUploadEvent getEndFileUploadEvent(FileUploadEvent aFUE, boolean isSuccessful, String msg) {
        aFUE.isSuccessful = isSuccessful;
        aFUE.note = msg;
        aFUE.endUploadTime = new Date();
        return aFUE;
    }

    /**
     * Transmit data to the server from the INTERNAL upload directory
     *
     * @param aContext Context
     * @param isJSONOnly True to only upload JSON and zipped JSON files (e.g., no other data files)
     * @param isRemove True to remove any files successfully uploaded from the original location
     * @param isSendBackup True to send any files successfully transmitted to the uploads backup directory
     * @param minCompletedPercentage Minimum percentage of completed files transferred so far to continue with current transfer before aborting
     * @param zipJson True to zip JSON files before sending (individually and into groups of jsons)
     * @return True if all transfers successful
     */
    private static boolean transmitInternalUploadFiles(Context aContext, boolean isJSONOnly, boolean isRemove,
                                                       boolean isSendBackup, double minCompletedPercentage,
                                                       boolean zipJson) {
        // Key info needed for transmitting data
        String studyID = Globals.STUDY_SERVER_NAME;
        String subjectID = DataStorage.GetSubjectID(aContext);
        String phoneID = PhoneInfo.getID(aContext);

        if (subjectID == null && isJSONOnly) {
            //do not transmit JSON files if subjectID is null
            Log.e(TAG, "subjectID is null. Json files are not sent!");
            return false;
        }

        // Try to transmit all the files in the internal upload directory
        String[] someFiles = DataManager.getFileNamesIntUploadDir();

        if (isJSONOnly) {
            someFiles = DataManager.getJSONOnly(someFiles);
        }

        if (someFiles == null) {
            // TODO change to warning once more carefully tested
            Log.e(TAG, "Warning: No files in internal upload directory to transmit");
            return true;
        }
        return transmitFiles(aContext, someFiles, studyID, subjectID, phoneID, isRemove, isSendBackup, minCompletedPercentage, zipJson);
    }

    /**
     * Transmit data to the server from the EXTERNAL upload directory
     *
     * @param aContext Context
     * @param isJSONOnly True to only upload JSON and zipped JSON files (e.g., no other data files)
     * @param isRemove True to remove any files successfully uploaded from the original location
     * @param isSendBackup True to send any files successfully transmitted to the uploads backup directory
     * @param minCompletedPercentage Minimum percentage of completed files transferred so far to continue with current transfer before aborting
     * @param zipJson True to zip JSON files before sending (individually and into groups of jsons)
     * @return True if all transfers successful
     */
    private static boolean transmitExternalUploadFiles(Context aContext, boolean isJSONOnly, boolean isRemove,
                                                       boolean isSendBackup,
                                                       double minCompletedPercentage, boolean zipJson) {
        // Key info needed for transmitting data
        String studyID = Globals.STUDY_SERVER_NAME;
        String subjectID = DataStorage.GetSubjectID(aContext);
        String phoneID = PhoneInfo.getID(aContext);


        if (subjectID == null && isJSONOnly) {
            //do not transmit JSON files if subjectID is null
            Log.e(TAG, "subjectID is null. Json files are not sent!");
            return false;
        }

        // Try to transmit all the files in the external upload directory
        String[] someFiles = DataManager.getFileNamesExtUploadDir();

        if (isJSONOnly) {
            someFiles = DataManager.getJSONOnly(someFiles);
        }

        if (someFiles == null) {
            // TODO change to warning once more carefully tested
            Log.e(TAG, "Warning: No files in external upload directory to transmit");
            return true;
        }
        return transmitFiles(aContext, someFiles, studyID, subjectID, phoneID, isRemove, isSendBackup, minCompletedPercentage, zipJson);
    }

    /**
     * Upload data to the server from the EXTERNAL upload directory
     *
     * @param aContext Context
     * @param isJSONOnly True to only upload JSON and zipped JSON files (e.g., no other data files)
     * @param isRemove True to remove any files successfully uploaded from the original location
     * @param isSendBackup True to send any files successfully transmitted to the uploads backup directory
     * @param minCompletedPercentage Minimum percentage of completed files transferred so far to continue with current transfer before aborting
     * @param zipJson True to zip JSON files before sending (individually and into groups of jsons)
     * @return Number of files remaining
     */
    public static int uploadDataFromExtUploadDir(Context aContext, boolean isJSONOnly,
                                                 boolean isRemove, boolean isSendBackup,
                                                 double minCompletedPercentage, boolean zipJson) {

        Log.d(TAG, "Start uploadData EXTERNAL");
        DataStorage.setIsTransmitting(aContext);
        RawUploader.transmitExternalUploadFiles(aContext, isJSONOnly, isRemove, isSendBackup, minCompletedPercentage, zipJson);
        DataStorage.setDoneTransmitting(aContext);
        String[] someFiles = DataManager.getFileNamesExtUploadDir();
        Log.d(TAG, "End uploadData EXTERNAL");

        if (someFiles != null)
            return someFiles.length;
        else
            return 0;
    }

    /**
     * Upload data to the server from the INTERNAL upload directory
     *
     * @param aContext Context
     * @param isJSONOnly True to only upload JSON and zipped JSON files (e.g., no other data files)
     * @param isRemove True to remove any files successfully uploaded from the original location
     * @param isSendBackup True to send any files successfully transmitted to the uploads backup directory
     * @param minCompletedPercentage Minimum percentage of completed files transferred so far to continue with current transfer before aborting
     * @param zipJson True to zip JSON files before sending (individually and into groups of jsons)
     * @return Number of files remaining
     */
    public static int uploadDataFromIntUploadDir(Context aContext, boolean isJSONOnly,
                                                 boolean isRemove, boolean isSendBackup,
                                                 double minCompletedPercentage, boolean zipJson) {

        Log.d(TAG, "Start uploadData INTERNAL");
        DataStorage.setIsTransmitting(aContext);
        RawUploader.transmitInternalUploadFiles(aContext, isJSONOnly, isRemove, isSendBackup, minCompletedPercentage, zipJson);
        DataStorage.setDoneTransmitting(aContext);
        String[] someFiles = DataManager.getFileNamesIntUploadDir();
        Log.d(TAG, "End uploadData INTERNAL");
        if (someFiles != null)
            return someFiles.length;
        else
            return 0;
    }


    /**
     * Upload files to the server
     *
     * @param aContext Context
     * @param someFileNames File names of files to upload
     * @param studyID StudyID must be a valid study registered on the server
     * @param subjectID SubjectID must be registered on the server
     * @param phoneID PhoneID must be registered on the server
     * @param isRemove True to remove files after upload
     * @param isSendBackup True to send all successfully uploaded files to backup uploads directory
     * @param minCompletedPercentage The minimum completed percentage of uploads this attempt before uploads are aborted
     * @param zipJson True to zip JSON files together for more efficient transfer
     * @return True if all files are transmitted. False if there is a problem, or if no files were sent.
     */
//    public static boolean transmitFilesNEW(Context aContext, String[] someFileNames, String studyID,
//                                        String subjectID, String phoneID, boolean isRemove,
//                                        boolean isSendBackup, double minCompletedPercentage, boolean zipJson) {
//        if (someFileNames == null)
//            return false;
//
//        ArrayList<File> someJSONFiles = new ArrayList<File>();
//        ArrayList<String> someJSONFileNames = new ArrayList<String>();
//        ArrayList<String> someDataFileNames = new ArrayList<String>();
//
//        int numSuccessful = 0;
//        int numTried = 0;
//        long totalTime = 0;
//        long startTime = 0;
//        boolean finishedUploadingJson = false;
//
//        // Count JSON files and put JSON and Data filenames in different arrays
//        int numFiles = someFileNames.length;
//        int numOfJson = 0;
//        for (String s : someFileNames) {
//            if (s.endsWith(".json") || s.endsWith(".json.zip")) {
//                numOfJson++;
//                someJSONFileNames.add(s);
//            } else
//                someDataFileNames.add(s);
//        }
//
//        Log.d(TAG, "Number of JSON files: " + numOfJson + ". Number of other files: " + (numFiles-numOfJson));
//
//        // Upload the JSON data first so we have status info
//        int numUploaded = uploadPhp(aContext, someJSONFiles, studyID,subjectID, phoneID, isRemove, isSendBackup, minCompletedPercentage, zipJson);
//
//        // Make decision about trying raw
//        uploadPhp(aContext, someDataFiles, studyID, subjectID, phoneID, isRemove, isSendBackup, minCompletedPercentage, zipJson);
//        return false;
//    }

    public static boolean transmitFiles(Context aContext, String[] someFileNames, String studyID,
                                        String subjectID, String phoneID, boolean isRemove,
                                        boolean isSendBackup, double minCompletedPercentage, boolean zipJson) {

        if (someFileNames == null)
            return false;

        ArrayList<File> someJSONFiles = new ArrayList<File>();

        double numFiles = someFileNames.length;
        int numSuccessful = 0;
        int numTried = 0;
        long totalTime = 0;
        long startTime = 0;
        boolean finishedUploadingJson = false;
        int numOfJson = 0;

        for (String s : someFileNames) {
            if (s.endsWith(".json") || s.endsWith(".json.zip")) {
                numOfJson++;
            }
        }

        for (int i = 0; i < someFileNames.length; i++)
        {
            boolean isSuccess = false;

            Log.d(TAG, "Files remaining: " + (numFiles - i));

            startTime = System.currentTimeMillis();

            isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);

//            // TODO This code looks broken
//            if (someFileNames[i].endsWith(".json") || someFileNames[i].endsWith(".json.zip")) {
//                // Transmit JSON file or zipped JSON file
//                isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//            } else {
//                // Transmit non-JSON file
//                isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                //isSuccess = uploadLogs(aContext, someFileNames[i], true);
//            }

            if (!isSuccess) { //&& zipJson) {
                numTried++;
                Log.e(TAG, "Problem transmitting file: " + someFileNames[i]);
//            } else if (!isSuccess && !zipJson) {
//                // return false;
            } else {
                numSuccessful++;

                if (Globals.IS_DEBUG) {
                    totalTime += System.currentTimeMillis() - startTime;
                    Log.d(TAG, "Avg time: " + (totalTime / numSuccessful));
                }
            }

            if ((i > 5) && ((numSuccessful / ((double) i)) < minCompletedPercentage)) {
                Log.e(TAG, "Stopped transmitting files because too many failures. Total files: " + numFiles + " Successful: " + numSuccessful + " Percentage: "
                        + (numSuccessful / (double) i) + " Min completed percentage: " + minCompletedPercentage + " i: " + i);
                return false;
            }
        }
        return true;
    }

//
//        for (int i = 0; i < someFileNames.length; i++) {
//            Log.d(TAG, "JSON Files remaining: " + (numOfJson - i));
//            startTime = System.currentTimeMillis();
//                isSuccess = uploadPhp(aContext, someJSONFiles, isRemove);
//
//            // TODO This code looks broken
//            if (someFileNames[i].endsWith(".json") || someFileNames[i].endsWith(".json.zip")) {
//                // Transmit JSON file or zipped JSON file
//                if (!zipJson) {
//                    isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                } else {
//                    if (!(finishedUploadingJson)) {
//                        someJSONFiles.add(new File(someFileNames[i]));
//
//                        if ((numOfJson >= 20) &&
//                                ((someJSONFiles.size() >= 100) || (someJSONFiles.size() >= numOfJson))) {
//                            isSuccess = uploadPhp(aContext, someJSONFiles, isRemove);
//                            finishedUploadingJson = true;
//                        }
//                    }
//                }
//            } else {
//                // Transmit non-JSON file
//                isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                //isSuccess = uploadLogs(aContext, someFileNames[i], true);
//
//            }
//
//
//
//
//        for (int i = 0; i < someFileNames.length; i++) {
//            boolean isSuccess = false;
//            Log.d(TAG, "Files remaining: " + (numFiles - i));
//            startTime = System.currentTimeMillis();
//
//            // TODO This code looks broken
//            if (someFileNames[i].endsWith(".json") || someFileNames[i].endsWith(".json.zip")) {
//                // Transmit JSON file or zipped JSON file
//                if (!zipJson) {
//                    isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                } else {
//                    if (!(finishedUploadingJson)) {
//                        someJSONFiles.add(new File(someFileNames[i]));
//
//                        if ((numOfJson >= 20) &&
//                                ((someJSONFiles.size() >= 100) || (someJSONFiles.size() >= numOfJson))) {
//                            isSuccess = uploadPhp(aContext, someJSONFiles, isRemove);
//                            finishedUploadingJson = true;
//                        }
//                    }
//                }
//            } else {
//                // Transmit non-JSON file
//                isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                //isSuccess = uploadLogs(aContext, someFileNames[i], true);
//
//            }
//
//            if (!isSuccess && zipJson) {
//                numTried++;
//            } else if (!isSuccess && !zipJson) {
//                Log.e(TAG, "Problem transmitting file: " + someFileNames[i]);
//                // return false;
//            } else {
//                numSuccessful++;
//
//                if (Globals.IS_DEBUG) {
//                    totalTime += System.currentTimeMillis() - startTime;
//                    Log.d(TAG, "Avg time: " + (totalTime / numSuccessful));
//                }
//            }
//
//            if ((i > 5) && (((numSuccessful + numTried) / ((double) i)) < minCompletedPercentage)) {
//                Log.e(TAG, "Stopped transmitting files because too many failures. Total files: " + numFiles + " Successful: " + numSuccessful + " Percentage: "
//                        + (numSuccessful / (double) i) + " Min completed percentage: " + minCompletedPercentage + " i: " + i);
//                return false;
//            }
//        }
//        return true;
//    }

//    public static boolean transmitFiles(Context aContext, String[] someFileNames, String studyID,
//                                        String subjectID, String phoneID, boolean isRemove,
//                                        boolean isSendBackup, double minCompletedPercentage, boolean zipJson) {
//        if (someFileNames == null)
//            return false;
//
//        ArrayList<File> someJSONFiles = new ArrayList<File>();
//
//        double numFiles = someFileNames.length;
//        int numSuccessful = 0;
//        int numTried = 0;
//        long totalTime = 0;
//        long startTime = 0;
//        boolean finishedUploadingJson = false;
//        int numOfJson = 0;
//
//        for (String s : someFileNames) {
//            if (s.endsWith(".json") || s.endsWith(".json.zip")) {
//                numOfJson++;
//            }
//        }
//
//        for (int i = 0; i < someFileNames.length; i++) {
//            boolean isSuccess = false;
//
//            Log.d(TAG, "Files remaining: " + (numFiles - i));
//
//            startTime = System.currentTimeMillis();
//
//            // TODO This code looks broken
//            if (someFileNames[i].endsWith(".json") || someFileNames[i].endsWith(".json.zip")) {
//                // Transmit JSON file or zipped JSON file
//                if (!zipJson) {
//                    isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                } else {
//                    if (!(finishedUploadingJson)) {
//                        someJSONFiles.add(new File(someFileNames[i]));
//
//                        if ((numOfJson >= 20) &&
//                                ((someJSONFiles.size() >= 100) || (someJSONFiles.size() >= numOfJson))) {
//                            isSuccess = uploadPhp(aContext, someJSONFiles, isRemove);
//                            finishedUploadingJson = true;
//                        }
//                    }
//                }
//            } else {
//                // Transmit non-JSON file
//                isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID, subjectID, phoneID, isRemove, isSendBackup, false, false, true);
//                //isSuccess = uploadLogs(aContext, someFileNames[i], true);
//
//            }
//
//            if (!isSuccess && zipJson) {
//                numTried++;
//            } else if (!isSuccess && !zipJson) {
//                Log.e(TAG, "Problem transmitting file: " + someFileNames[i]);
//                // return false;
//            } else {
//                numSuccessful++;
//
//                if (Globals.IS_DEBUG) {
//                    totalTime += System.currentTimeMillis() - startTime;
//                    Log.d(TAG, "Avg time: " + (totalTime / numSuccessful));
//                }
//            }
//
//            if ((i > 5) && (((numSuccessful + numTried) / ((double) i)) < minCompletedPercentage)) {
//                Log.e(TAG, "Stopped transmitting files because too many failures. Total files: " + numFiles + " Successful: " + numSuccessful + " Percentage: "
//                        + (numSuccessful / (double) i) + " Min completed percentage: " + minCompletedPercentage + " i: " + i);
//                return false;
//            }
//        }
//        return true;
//    }

    // public static boolean transmitFiles(Context aContext,
    // String[] someFileNames, String studyID, String subjectID,
    // String phoneID, boolean isRemove, boolean isSendBackup,
    // double minCompletedPercentage) {
    // boolean isSuccess;
    // if (someFileNames == null)
    // return false;
    //
    // double numFiles = someFileNames.length;
    // int numSuccessful = 0;
    // long totalTime = 0;
    // long startTime = 0;
    // int counter = 0;
    //
    // for (int i = 0; i < someFileNames.length; i++) {
    // if (Globals.IS_DEBUG)
    // Log.d(TAG,
    // "======================================================================================================= Files remaining: "
    // + (numFiles - i));
    // startTime = System.currentTimeMillis();
    // // if (USE_PHP) {
    // // isSuccess = transmitFilePhp(aContext, someFileNames[i], studyID,
    // // subjectID, phoneID, isRemove, isSendBackup, false, false, false,
    // // true);
    // // }
    // // else {
    // // isSuccess = transmitFile(aContext, someFileNames[i], studyID,
    // // subjectID, phoneID, isRemove, isSendBackup);
    // // }
    //
    // if (someFileNames[i].contains("json")) {
    //
    // if (counter == 0) {
    // }
    //
    // // if ( counter =0 create the zip file)
    // // esle (add to the zipfile
    // // counter ++
    // // isSuccess = transmitFile(aContext, zipfile, studyID,
    // // subjectID, phoneID, isRemove, isSendBackup);
    //
    // isSuccess = transmitFile(aContext, someFileNames[i], studyID,
    // subjectID, phoneID, isRemove, isSendBackup);
    // } else {
    // isSuccess = transmitFilePhp(aContext, someFileNames[i],
    // studyID, subjectID, phoneID, isRemove, isSendBackup,
    // false, false, false, true);
    //
    // }
    //
    // if (!isSuccess) {
    // Log.e(TAG, "Problem transmitting file: " + someFileNames[i]);
    // // return false;
    // } else {
    // numSuccessful++;
    //
    // if (Globals.IS_DEBUG) {
    // totalTime += System.currentTimeMillis() - startTime;
    // Log.d(TAG,
    // "======================================================================================================= Avg time: "
    // + (totalTime / numSuccessful));
    // }
    // }
    //
    // if ((i > 5)
    // && ((numSuccessful / ((double) i)) < minCompletedPercentage)) {
    // Log.e(TAG,
    // "Stopped transmitting files because too many failures. Total files: "
    // + numFiles + " Successful: " + numSuccessful
    // + " Percentage: "
    // + (numSuccessful / (double) i)
    // + " Min completed percentage: "
    // + minCompletedPercentage + " i: " + i);
    // return false;
    // }
    // }
    // return true;
    // }
    //

    /**
     * Strip the <h1>and blank line from the error message returned from the
     * server and remove endlines for better readability.
     *
     * @param aMsg
     * @return
     */
    private static String getCleanErrorMsg(String aMsg) {
        String lines[] = aMsg.split("\\r?\\n");
        StringBuffer sb = new StringBuffer();
        for (int i = 2; i < lines.length; i++)
            sb.append(lines[i]);
        return sb.toString();
    }

//    public static boolean transmitFile(Context aContext, String aFileName, String studyID, String subjectID, String phoneID, boolean isRemove,
//                                       boolean isSendBackup) {
//        WocketInfo wi = new WocketInfo(aContext);
//        File f = new File(aFileName);
//        String errMsg = "";
//
//        // TODO fix date (not sure what this means...)
//        FileUploadEvent aFileUploadEvent = getStartFileUploadEvent(f, new Date(), new Date());
//
//        if (Globals.IS_DEBUG)
//            Log.i(TAG, "Try to upload: " + aFileName + " StudyID: " + studyID + " SubjectID: " + subjectID + " PhoneID: " + phoneID); // TODO
//        boolean isSuccess = true;
//        // get the md5 checksum of file
//
//        if (!NetworkMonitor.isNetworkAvailable(aContext)) {
//            errMsg = "Error in transmitFile. Network not available.";
//            Log.e(TAG, errMsg);
//            isSuccess = false;
//        }
//
//        HttpPost httppost = null;
//        MultipartEntity entity = null;
//        HttpClient httpclient = null;
//
//        if (isSuccess) {
//            String md5Checksum = getMD5ForFile(aFileName);
//
//            httpclient = new DefaultHttpClient();
//
//            // TODO confirm this does not cause trouble with larger files!
//            httpclient.getParams().setParameter("http.connection.timeout", 15000); // TODO
//            // change
//            // from
//            // 3000
//            httpclient.getParams().setParameter("http.socket.timeout", 15000);
//            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
//
//            httppost = new HttpPost(Globals.URL_FILE_UPLOAD_SERVLET);
//
//            entity = new MultipartEntity();
//            // adding parameters for request
//            try {
//                // entity.addPart("protocol", new StringBody("protocol"));
//                entity.addPart("studyID", new StringBody(studyID));
//                entity.addPart("subjectID", new StringBody(subjectID));
//                entity.addPart("phoneID", new StringBody(phoneID));
//                // entity.addPart("sessionNumber", new
//                // StringBody(sessionNumber));
//                entity.addPart("md5Checksum", new StringBody(md5Checksum));
//                entity.addPart(f.getName(), new FileBody(f));
//            } catch (UnsupportedEncodingException e) {
//                errMsg = "Error in uploadFile. Unsupported encoding. " + e.toString();
//                Log.e(TAG, errMsg);
//                e.printStackTrace();
//                isSuccess = false;
//            }
//        }
//
//        // TOD remove after testing
//        // Log.d(TAG, "PhoneID: " + phoneID + " SubjectID: " + subjectID +
//        // " StudyID: " + studyID);
//
//        if (isSuccess) {
//            httppost.setEntity(entity);
//            HttpResponse httpresponse;
//            InputStream is = null;
//            BufferedReader rd = null;
//            try {
//                httpresponse = httpclient.execute(httppost);
//                String statusLine = httpresponse.getStatusLine().toString();
//                int statusCode = httpresponse.getStatusLine().getStatusCode();
//                if (statusCode != HTTP_RESPONSE_SERVER_UP) {
//                    errMsg = "Error in uploadFile communicating with server. Did not get a 200 response. " + statusLine;
//                    Log.e(TAG, errMsg);
//                    isSuccess = false;
//                } else {
//                    HttpEntity resEntity = httpresponse.getEntity();
//                    String responseBody = EntityUtils.toString(resEntity);
//
//                    if (responseBody.contains("Json file successfully submitted")) {
//                        if (Globals.IS_DEBUG) {
//                            Log.i(TAG, "HTTPRESPONSE BODY: " + "Json upload and processing successful");
//                            Log.i(TAG, "Response from server: " + responseBody);
//                        }
//                        errMsg = "Json file uploaded and processed.";
//                        isSuccess = true;
//                    } else if (responseBody.contains("File written to destination directory")) {
//                        if (Globals.IS_DEBUG)
//                            Log.i(TAG, "HTTPRESPONSE BODY: " + "File upload successful");
//                        errMsg = "File uploaded to server without error.";
//                        isSuccess = true;
//                    } else // Response body must contain an error
//                    {
//                        // Log.e(TAG, "HTTPRESPONSE BODY (ERROR): " +
//                        // getCleanErrorMsg(responseBody));
//                        Log.e(TAG, "HTTPRESPONSE BODY (ERROR): " + responseBody);
//                        errMsg = "Error message from server after upload attempt: " + responseBody;
//                        isSuccess = false;
//                    }
//                }
//            } catch (ClientProtocolException e) {
//                errMsg = "Error in uploadFile. ClientProtocolException in httpclient.execute." + e.toString();
//                Log.e(TAG, errMsg);
//                e.printStackTrace();
//                isSuccess = false;
//            } catch (IOException e) {
//                errMsg = "Error in uploadFile. IOException in httpclient.execute." + e.toString();
//                Log.e(TAG, errMsg);
//                e.printStackTrace();
//                isSuccess = false;
//            } finally {
//                if (rd != null)
//                    try {
//                        rd.close();
//                    } catch (IOException e) {
//                        errMsg = "Error closing file";
//                        Log.e(TAG, errMsg);
//                        e.printStackTrace();
//                    }
//                if (is != null)
//                    try {
//                        is.close();
//                    } catch (IOException e) {
//                        errMsg = "Error closing file";
//                        Log.e(TAG, errMsg);
//                        e.printStackTrace();
//                    }
//            }
//        }
//
//        if (isSendBackup && isSuccess) {
//            File origfile = new File(aFileName);
//            File destfile = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.BACKUP_DIRECTORY + File.separator
//                    + Log.getFolderDateFormatForToday() + File.separator + origfile.getName());
//            if (Globals.IS_DEBUG)
//                Log.i(TAG, "Success uploading. Send to backup dir: " + aFileName + " Backup dir: " + destfile.getAbsolutePath());
//
//            if (!FileHelper.copyFile(origfile, destfile)) {
//                errMsg = "Error: could not backup file on phone after upload: " + aFileName + " in directory: " + destfile.getAbsolutePath();
//                Log.e(TAG, errMsg);
//            } else
//                Log.d(TAG, errMsg);
//        }
//
//        if (isRemove && isSuccess) {
//            if (Globals.IS_DEBUG)
//                Log.i(TAG, "Success uploading. Try to remove: " + aFileName + " StudyID: " + studyID + " SubjectID: " + subjectID + " PhoneID: " + phoneID);
//            if (!FileHelper.deleteFile(new File(aFileName))) {
//                errMsg = "Error: could not remove file on phone after upload: " + aFileName;
//            } else
//                Log.d(TAG, errMsg);
//        }
//
//        // Only send the file upload extra info if the file is not a json file
//        if (!(aFileName.endsWith(".json.zip") || aFileName.endsWith(".json"))) {
//            aFileUploadEvent = getEndFileUploadEvent(aFileUploadEvent, isSuccess, errMsg);
//            wi.someFileUploads = new ArrayList<FileUploadEvent>();
//            wi.someFileUploads.add(aFileUploadEvent);
//            DataSender.transmitOrQueueWocketInfo(aContext, wi);
//        }
//
//        return isSuccess;
//    }

    private static long te = 0;

    private static void timeElapsed(String msg) {
        Log.e(TAG, msg + ": Time elapsed: " + (System.currentTimeMillis() - te));
        te = System.currentTimeMillis();
    }

    // // This method calls the service with protocol, subjectid, session number
    // and filename for each file
    // // in the list of files
    // public static boolean transmitFile(String aFileName, String protocol,
    // String subjectID, String sessionNumber, boolean isCompress)
    // {
    // boolean isSuccess = true;
    // // get the md5 checksum of file
    // String md5Checksum = getMD5forfile(aFileName);
    // File f = new File(aFileName);
    //
    // HttpClient httpclient = new DefaultHttpClient();
    // HttpPost httppost = new HttpPost(URL);
    //
    // MultipartEntity entity = new MultipartEntity();
    // // adding parameters for request
    // try {
    // entity.addPart("protocol", new StringBody(protocol));
    // entity.addPart("subjectID", new StringBody(subjectID));
    // entity.addPart("sessionNumber", new StringBody(sessionNumber));
    // entity.addPart("md5Checksum", new StringBody(md5Checksum));
    // entity.addPart(f.getName(), new FileBody(f));
    // } catch (UnsupportedEncodingException e) {
    // Log.e(TAG, "Error in uploadFile. Unsupported encoding. " +
    // e.toString());;
    // e.printStackTrace();
    // }
    //
    // httppost.setEntity(entity);
    // HttpResponse httpresponse;
    // try {
    // httpresponse = httpclient.execute(httppost);
    // Log.e(TAG, "HTTPRESPONSE: " + httpresponse.getStatusLine().toString());
    // HttpEntity resEntity = httpresponse.getEntity();
    // InputStream is = resEntity.getContent();
    // BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    // String return_result = rd.readLine();
    // if (return_result != null) {
    // Pattern regex = Pattern.compile("(?<=<body><h1>)(.+?)(?=<)");
    // Matcher m = regex.matcher(return_result);
    // if (m.find()) {
    // isSuccess = false;
    // Log.e(TAG, "Error http response: " + m.group().toString());
    // }
    // }
    // } catch (ClientProtocolException e) {
    // Log.e(TAG,
    // "Error in uploadFile. ClientProtocolException in httpclient.execute." +
    // e.toString());
    // e.printStackTrace();
    // } catch (IOException e) {
    // Log.e(TAG, "Error in uploadFile. IOException in httpclient.execute." +
    // e.toString());
    // e.printStackTrace();
    // }
    //
    // return isSuccess;
    // }
    //
    // // This method calls the service with protocol, subjectid, session number
    // and filename for each file
    // // in the list of files
    // public static boolean transmitFile(String aFileName, String protocol,
    // String subjectID, String sessionNumber, boolean isCompress)
    // {
    // boolean isSuccess = true;
    // // get the md5 checksum of file
    // String md5Checksum = getMD5forfile(aFileName);
    // File f = new File(aFileName);
    //
    // HttpClient httpclient = new DefaultHttpClient();
    // HttpPost httppost = new HttpPost(URL);
    //
    // MultipartEntity entity = new MultipartEntity();
    // // adding parameters for request
    // try {
    // entity.addPart("protocol", new StringBody(protocol));
    // entity.addPart("subjectID", new StringBody(subjectID));
    // entity.addPart("sessionNumber", new StringBody(sessionNumber));
    // entity.addPart("md5Checksum", new StringBody(md5Checksum));
    // entity.addPart(f.getName(), new FileBody(f));
    // } catch (UnsupportedEncodingException e) {
    // Log.e(TAG, "Error in uploadFile. Unsupported encoding. " +
    // e.toString());;
    // e.printStackTrace();
    // }
    //
    // httppost.setEntity(entity);
    // HttpResponse httpresponse;
    // try {
    // httpresponse = httpclient.execute(httppost);
    // Log.e(TAG, "HTTPRESPONSE: " + httpresponse.getStatusLine().toString());
    // HttpEntity resEntity = httpresponse.getEntity();
    // InputStream is = resEntity.getContent();
    // BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    // String return_result = rd.readLine();
    // if (return_result != null) {
    // Pattern regex = Pattern.compile("(?<=<body><h1>)(.+?)(?=<)");
    // Matcher m = regex.matcher(return_result);
    // if (m.find()) {
    // isSuccess = false;
    // Log.e(TAG, "Error http response: " + m.group().toString());
    // }
    // }
    // } catch (ClientProtocolException e) {
    // Log.e(TAG,
    // "Error in uploadFile. ClientProtocolException in httpclient.execute." +
    // e.toString());
    // e.printStackTrace();
    // } catch (IOException e) {
    // Log.e(TAG, "Error in uploadFile. IOException in httpclient.execute." +
    // e.toString());
    // e.printStackTrace();
    // }
    //
    // return isSuccess;
    // }

    /**
     * Uploads the file to the server. This is a blocking process. You must
     * manually spawn a thread if you want this to run asynchronously.
     *
     * @param targetURL
     * @param parameters
     * @param f
     * @param targetFilename
     * @return The response from the server, or <code>null</code> if there was
     *         an error.
     */
    private static String upload(String targetURL, HashMap<String, String> parameters, File f, String targetFilename)
            throws ClientProtocolException, IOException {

        // Create the http client that will upload the data for us
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        // By default, the client will retry an upload up to three times if it
        // encounters an error
        // However, it's very careful about not causing double-requests - we
        // prefer double requests over
        // loss of data, so we set requestSentRetryEnabled to true
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(3, true);
        client.setHttpRequestRetryHandler(retryHandler);

        // Since we are uploading a file, this will be a POST request (as
        // opposed to GET)
        HttpPost postRequest = new HttpPost(targetURL);

        // We encapsulate the data to upload in a multipart entity
        MultipartEntity multipart = new MultipartEntity();

        // First we add any POST variables to the multipart entity
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            try {
                multipart.addPart(entry.getKey(), new StringBody(entry.getValue()));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "[File uploader] Unsupported encoding in file: " + f.getAbsolutePath());
            }
        }

        // Finally, we add the file itself to the multipart entity
        ContentBody fileContent = new FileBody(f, targetFilename);
        multipart.addPart(targetFilename, fileContent);

        // Time to upload!
        postRequest.setEntity(multipart);

        //TODO do we want this?! Slow!
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error in sleep: " + e.toString());
        }

        // This might throw an exception, passed up
        HttpResponse response = client.execute(postRequest);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // read the response
            HttpEntity responseEntity = response.getEntity();
            String content = EntityUtils.toString(responseEntity);
            return content;
        } else {
            Log.d(TAG, "Bad request status: " + response.getStatusLine().getStatusCode());
            HttpEntity responseEntity = response.getEntity();
            responseEntity.consumeContent();
            return null;
        }
    }

    public static HashMap<File, String> uploadZipped(Context ctx, String uploadURL, HashMap<File, String> filesToUpload, String dob, String lastName,
                                                     String study, String deviceID, String desiredPath, String rootDirectory, HashMap<File, String> desiredNames) {
        return uploadPhp(ctx, uploadURL, filesToUpload, dob, lastName, study, deviceID, desiredPath, rootDirectory, true, true, true, desiredNames, true, Globals.UPLOAD_SUCCESS_PERCENTAGE);
    }

//    public static HashMap<File, String> uploadZippedSecond(Context ctx, String uploadURL, HashMap<File, String> filesToUpload, String dob, String lastName,
//                                                           String study, String deviceID, String desiredPath, String rootDirectory) {
//        return uploadPhp(ctx, uploadURL, filesToUpload, dob, lastName, study, deviceID, desiredPath, rootDirectory, true, false, true);
//    }

    public static HashMap<File, String> uploadZipped(Context ctx, String uploadURL, HashMap<File, String> filesToUpload, String dob, String lastName,
                                                     String study, String deviceID, String desiredPath, String rootDirectory, boolean shouldZip) {
        return uploadPhp(ctx, uploadURL, filesToUpload, dob, lastName, study, deviceID, desiredPath, rootDirectory, shouldZip, false, true);
    }

    public static HashMap<File, String> uploadPhp(Context ctx, String uploadURL, HashMap<File, String> filesToUpload, String dob, String lastName,
                                                  String study, String deviceID, String desiredPath, String rootDirectory, boolean shouldZip, boolean shouldBackup, boolean shouldUnzipOnServer) {
        return uploadPhp(ctx, uploadURL, filesToUpload, dob, lastName, study, deviceID, desiredPath, rootDirectory, shouldZip, shouldBackup, shouldUnzipOnServer, null, true, Globals.UPLOAD_SUCCESS_PERCENTAGE);
    }

    public static HashMap<File, String> uploadPhp(Context ctx, String uploadURL, HashMap<File, String> filesToUpload,
                                                  String dob, String lastName,
                                                  String study, String deviceID, String desiredPath,
                                                  String rootDirectory, boolean shouldZip,
                                                  boolean shouldBackup, boolean shouldUnzipOnServer,
                                                  HashMap<File, String> desiredFileNames, boolean preventBackup,
                                                  double minCompletedPercentage) {

        int successful = 0;
        int attempted = 0;

        // Upload files
        long size = 0;
        long attemptedSize = 0;
        double rate = 1.0;

        HashMap<File, String> result = new HashMap<File, String>();

        for (File f : filesToUpload.keySet()) {
            if (rate > minCompletedPercentage)
            {
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("dob", dob);
                params.put("lastName", lastName);
                params.put("study", study);
                params.put("deviceID", deviceID);
                params.put("desiredPath", desiredPath);
                params.put("rootDirectory", rootDirectory);

                attempted++;

                String desiredFileName = filesToUpload.get(f);

                if ((f != null) && (f.isDirectory())) {
                    params.put("isFolder", "1");
                }

                if (shouldBackup) {
                    params.put("shouldBackup", "1");
                }

                if (shouldUnzipOnServer) {
                    params.put("shouldUnzip", "1");
                }

                if (preventBackup) {
                    params.put("preventBackup", "1");
                }

                File fileToUpload = f;
                boolean shouldDelete = false;

                if (shouldZip) {

                    String desired = null;
                    if (desiredFileNames != null) {
                        desired = desiredFileNames.get(f);
                    }

                    // params.put("shouldUnzip", "1");
                    fileToUpload = Zipper.zipFile(ctx, f, "", desired);
                    shouldDelete = true;
                    if (fileToUpload == null) {
                        Log.e(TAG, "Can't zip file: " + f.getAbsoluteFile());
                        continue;
                    }
                }

                if ((fileToUpload != null) && (fileToUpload.isFile())) {
                    params.put("md5Checksum", FileHelper.getMD5ForFile(fileToUpload.getAbsolutePath()));
                }

                // TODO is this what we want for logging?
                if (Globals.IS_DEBUG)
                    Log.i(TAG, "Attempted upload file:" + f.getAbsolutePath() + "; Attempted size: " + fileToUpload.length());

                // params.put("desiredFileName", f.getName() + ".zip");
                if (shouldZip) {
                    params.put("desiredFileName", desiredFileName + ".zip");
                } else {
                    params.put("desiredFileName", desiredFileName);
                }

                attemptedSize += fileToUpload.length();
                try {
                    String targetFilename = f.getName();
                    if (shouldZip) {
                        targetFilename += ".zip";
                    }
                    if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                        Log.d(TAG, "DOB: " + dob + " lastName: " + lastName
                                + " study: " + study + " DeviceID: "
                                + RSACipher.encrypt(deviceID,ctx));
                    } else {
                        Log.d(TAG, "DOB: " + dob + " lastName: " + lastName
                                + " study: " + study + " DeviceID: "
                                + deviceID);
                    }

                    String response = upload(uploadURL, params, fileToUpload, targetFilename);

                    //Reduce the length of the response if it is greater than 1000 characters (i.e. HTML Code).
                    //Keeping it at 500 characters so that it will help in identifying the issue by reading the html code.
                    if(response.length()>1000) {
                        response = response.substring(0, 999);
                    }

                    // TODO Do we want this logging
                    if (Globals.IS_DEBUG)
                        Log.i(TAG, "RESPONSE:" + response);

                    result.put(f, response);

                    if ((response != null) && (response.contains("SUCCESS"))) {
                        size += fileToUpload.length();

                        if (Globals.IS_DEBUG)
                            Log.i(TAG, f.getName() + ":" + fileToUpload.length());
                        successful++;
                        // successes.put(desiredFileName, response);
                    } else if (response != null && response.startsWith("ERROR")) {
                        Log.e(TAG, "Server returned the following error: " + response);
                        if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                            Log.e(TAG,
                                    "Upload info: DOB: " + dob + " lastName: "
                                            + lastName + " study: " + study
                                            + " DeviceID: " + RSACipher.encrypt(deviceID,ctx));
                        } else {
                            Log.e(TAG,
                                    "Upload info: DOB: " + dob + " lastName: "
                                            + lastName + " study: " + study
                                            + " DeviceID: " + deviceID);
                        }
                    } else if (response == null) {
                        Log.e(TAG, "Null response from the server");
                        if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                            Log.e(TAG,
                                    "Upload info: DOB: " + dob + " lastName: "
                                            + lastName + " study: " + study
                                            + " DeviceID: " + RSACipher.encrypt(deviceID,ctx));
                        } else {
                            Log.e(TAG,
                                    "Upload info: DOB: " + dob + " lastName: "
                                            + lastName + " study: " + study
                                            + " DeviceID: " + deviceID);
                        }
                    } else {
                        Log.e(TAG, "Unknown error! Response from server was: " + response);
                        if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                            Log.e(TAG,
                                    "Upload info: DOB: " + dob + " lastName: "
                                            + lastName + " study: " + study
                                            + " DeviceID: " + RSACipher.encrypt(deviceID,ctx));
                        } else {
                            Log.e(TAG,
                                    "Upload info: DOB: " + dob + " lastName: "
                                            + lastName + " study: " + study
                                            + " DeviceID: " + deviceID);
                        }
                    }
                } catch (ClientProtocolException e) {
                    Log.e(TAG, "[File uploader] Client protocol exception while uploading " + f.getAbsolutePath() + " to " + uploadURL + " - " + e.getMessage(), e);
                    if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                        try {
                            Log.e(TAG, "Upload info: DOB: " + dob + " lastName: "
                                    + lastName + " study: " + study + " DeviceID: "
                                    + RSACipher.encrypt(deviceID,ctx));
                        } catch (IOException | GeneralSecurityException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        Log.e(TAG,
                                "Upload info: DOB: " + dob + " lastName: "
                                        + lastName + " study: " + study
                                        + " DeviceID: "
                                        + deviceID);
                    }
                } catch (IOException e)  {
                    Log.e(TAG, "[File uploader] IO exception while uploading " + f.getAbsolutePath() + " to " + uploadURL + " - " + e.getMessage(), e);
                    if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                        try {
                            Log.e(TAG, "Upload info: DOB: " + dob + " lastName: "
                                    + lastName + " study: " + study + " DeviceID: "
                                    + RSACipher.encrypt(deviceID,ctx));
                        } catch (IOException | GeneralSecurityException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        Log.e(TAG,
                                "Upload info: DOB: " + dob + " lastName: "
                                        + lastName + " study: " + study
                                        + " DeviceID: "
                                        + deviceID);
                    }
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }

                if ((shouldDelete) && (f != fileToUpload)) {
                    if ((fileToUpload != null) && (!(fileToUpload.delete()))) {
                        Log.e(TAG, "Could not delete zipped file: " + fileToUpload.getName());
                    }
                }

                // Check if upload should stop because files failing
                if (attempted > 10) {
                    rate = ((double) successful / attempted);

                    // TODO want this logging?
                    if (Globals.IS_DEBUG) {
                        Log.i(TAG, "success = " + successful);
                        Log.i(TAG, "progress = " + attempted);
                        Log.i(TAG, "Total num = " + filesToUpload.size());
                        Log.i(TAG, "rate = " + rate);
                    }

                    if (rate < minCompletedPercentage)
                    {
                        Log.e(TAG, "Aborting upload because rate (" + rate + ") below target rate (" + minCompletedPercentage + ").");
                    }
                }
            }
        }

        if (successful >= filesToUpload.size()) {
            if (Globals.IS_DEBUG)
                Log.i(TAG, "All uploads complete");


        }

        // TODO want this logging?
        if (Globals.IS_DEBUG) {
            Log.i(TAG, "Size in total:" + size);
            Log.i(TAG, "Attempted size in total:, " + attemptedSize);
        }


        return result;
    }

    //

    public static boolean transmitFilePhp(Context aContext, String aFileName, String studyID, String subjectID, String phoneID, boolean isRemove,
                                          boolean isSendBackup, boolean shouldZip, boolean shouldBackup, boolean shouldUnzipOnServer) {
        WocketInfo wi = new WocketInfo(aContext);
        File f = new File(aFileName);
        String errMsg = "";

        // TODO fix date (not sure what this means...)
        FileUploadEvent aFileUploadEvent = getStartFileUploadEvent(f, new Date(), new Date());

        if (Globals.IS_DEBUG) {
            if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                try {
                    Log.i(TAG,
                            "Try to upload: " + aFileName + " StudyID: " + studyID
                                    + " SubjectID: " + subjectID + " PhoneID: "
                                    + RSACipher.encrypt(phoneID,aContext)); // TODO
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG,
                        "Try to upload: " + aFileName + " StudyID: " + studyID
                                + " SubjectID: " + subjectID + " PhoneID: "
                                + phoneID); // TODO
            }
        }

        boolean isSuccess = true;
        // get the md5 checksum of file

        if (!NetworkMonitor.isNetworkAvailable(aContext)) {
            errMsg = "Error in transmitFile. Network not available.";
            Log.e(TAG, errMsg);
            isSuccess = false;
        }

        // TOD remove after testing
        if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
            try {
                Log.d(TAG, "PhoneID: " + RSACipher.encrypt(phoneID,aContext) + " SubjectID: " + subjectID
                        + " StudyID: " + studyID);
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "PhoneID: " + phoneID + " SubjectID: " + subjectID
                    + " StudyID: " + studyID);
        }

        HashMap<File, String> fileToUpload = new HashMap<File, String>();

        File file = new File(aFileName);

        fileToUpload.put(file, file.getName());

        String dob = getDOB(aContext);
        String lastName = DataStorage.GetLastName(aContext, "");

        HashMap<File, String> result = uploadPhp(aContext, Globals.PHP_SERVER_UPLOAD_FILE_ZIPPED, fileToUpload, dob, lastName, studyID, phoneID, "", Globals.UPLOAD_SERVER_DIR_NAME, shouldZip,
                shouldBackup, shouldUnzipOnServer);

        String responseBody = result.get(file);

        if (responseBody == null) {
            Log.e(TAG, "HTTPRESPONSE BODY (ERROR): " + "Response is null");
            errMsg = "Error message from server after upload attempt: " + "response is null";
            isSuccess = false;
        } else if (responseBody.contains("Json file successfully submitted")) {
            if (Globals.IS_DEBUG) {
                Log.i(TAG, "HTTPRESPONSE BODY: " + "Json upload and processing successful");
                Log.i(TAG, "Response from server: " + responseBody);
            }
            errMsg = "Json file uploaded and processed.";
            isSuccess = true;
        } else if (responseBody.contains("File written to destination directory")) {
            if (Globals.IS_DEBUG)
                Log.i(TAG, "HTTPRESPONSE BODY: " + "File upload successful");
            errMsg = "File uploaded to server without error.";
            isSuccess = true;
        } else if (responseBody.contains("SUCCESS")) {
            if (Globals.IS_DEBUG)
                Log.i(TAG, "HTTPRESPONSE BODY: " + "Files have been successfully uploaded");
            errMsg = "Files have been uploaded successfully.";
            isSuccess = true;
        } else // Response body must contain an error
        {
            // Log.e(TAG, "HTTPRESPONSE BODY (ERROR): " +
            // getCleanErrorMsg(responseBody));
            Log.e(TAG, "HTTPRESPONSE BODY (ERROR): " + responseBody);
            errMsg = "Error message from server after upload attempt: " + responseBody;
            isSuccess = false;
        }

        if (isSendBackup && isSuccess) {
            File origfile = new File(aFileName);
            File destfile = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.BACKUP_DIRECTORY + File.separator
                    + Log.getFolderDateFormatForToday() + File.separator + origfile.getName());
            if (Globals.IS_DEBUG)
                Log.i(TAG, "Success uploading. Send to backup dir: " + aFileName + " Backup dir: " + destfile.getAbsolutePath());

            if (!FileHelper.copyFile(origfile, destfile)) {
                errMsg = "Error: could not backup file on phone after upload: " + aFileName + " in directory: " + destfile.getAbsolutePath();
                Log.e(TAG, errMsg);
            }
        }

        if (isRemove && isSuccess) {
            if (Globals.IS_DEBUG) {
                if (Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                    try {
                        Log.i(TAG, "Success uploading. Try to remove: " + aFileName
                                + " StudyID: " + studyID + " SubjectID: "
                                + subjectID
                                + " PhoneID: " + RSACipher.encrypt(phoneID,aContext));
                    } catch (IOException | GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "Success uploading. Try to remove: " + aFileName
                            + " StudyID: " + studyID + " SubjectID: "
                            + subjectID
                            + " PhoneID: " + phoneID);
                }
            }
            if (!FileHelper.deleteFile(new File(aFileName))) {
                errMsg = "Error: could not remove file on phone after upload: " + aFileName;
                Log.e(TAG, errMsg);
            }
        }

        // Only send the file upload extra info if the file is not a json file or a zip of json files
        if (!(aFileName.endsWith(".json.zip") || aFileName.endsWith(".json") || aFileName.endsWith(".jsons.zip"))) {
            aFileUploadEvent = getEndFileUploadEvent(aFileUploadEvent, isSuccess, errMsg);
            wi.someFileUploads = new ArrayList<FileUploadEvent>();
            wi.someFileUploads.add(aFileUploadEvent);
            DataSender.transmitOrQueueWocketInfo(aContext, wi);
        }

        return isSuccess;
    }

    // getting the DateOfBirth for participant for the matter of saving the CSV
    // file on the server
    private static String getDOB(Context c) {

        long dob = edu.neu.android.wocketslib.support.DataStorage.GetDateOfBirth(c, -1l);
        if (dob != -1) {
            Date aDOB = DateHelper.getDate(dob);
            return edu.neu.android.wocketslib.utils.DateHelper.getServerDateString(aDOB);
        }
        return "";
    }

    public static boolean uploadPhp(Context aContext, ArrayList<File> someJSONFiles, boolean isRemove) {
        boolean isSuccess = false;
        String errMsg = "";
        Date now = new Date();
        String timeStamp = now.toString();

        File[] output = someJSONFiles.toArray(new File[someJSONFiles.size()]);
        File zippedJsons = Zipper.zipThenZipFiles(aContext, output, timeStamp, aContext.getFilesDir(), ".json");

        HashMap<File, String> filesToUpload = new HashMap<File, String>();
        filesToUpload.put(zippedJsons, timeStamp);

        isSuccess = uploadZippedPhp(aContext, timeStamp, Globals.PHP_SERVER_UPLOAD_FILE_ZIPPED, filesToUpload);

        if (zippedJsons != null) {
            FileHelper.deleteFile(zippedJsons);
        }

        if (isRemove && isSuccess) {
            for (File temp : someJSONFiles) {
                if (!temp.delete()) {
                    errMsg = "Error: could not remove file on phone: " + temp;
                    Log.e(TAG, errMsg);
                }
            }
            someJSONFiles.clear();
        } else {//move them to the SDCARD
            // Move JSON to external upload folder
            DataSender.sendInternalUploadDataToExternalUploadDir(aContext, false, true);
        }

        return isSuccess;
    }

    // Different because desired path is different
    private static boolean uploadLogs(Context aContext, String aFile, boolean isRemove) {
        boolean isSuccess = false;
        String fileUniqueName = aFile.replace("/", "").replace(":", "").replace("_", "").replace("\\", "");


        File todayLog = new File(aFile);
        HashMap<File, String> filesToUpload = new HashMap<File, String>();
        filesToUpload.put(todayLog, fileUniqueName);

        isSuccess = uploadZippedPhp(aContext, Globals.APP_DIRECTORY + "__logs", Globals.PHP_SERVER_UPLOAD_FILE_ZIPPED, filesToUpload);
        if (isSuccess && isRemove) {
            todayLog.delete();
        }
        return isSuccess;
    }

    private static boolean uploadZippedPhp(Context c, String timeStamp, final String anUploadURL, HashMap<File, String> filesToUpload) {

        return uploadZipped(c, anUploadURL, filesToUpload, getDOB(c), edu.neu.android.wocketslib.support.DataStorage.GetLastName(c, ""),
                edu.neu.android.wocketslib.Globals.STUDY_SERVER_NAME, PhoneInfo.getID(c), timeStamp, Globals.UPLOAD_SERVER_DIR_NAME, false).keySet().size() > 0;

    }

    private static boolean isUploadSuccess(String response) {
        return ((response != null) && (response.contains("SUCCESS")));
    }

    // TODO to move to CITY-M
    public static List<File> uploadZippedAndReturnSuccesses(Context ctx, String uploadURL, HashMap<File, String> filesToUpload, String dob, String lastName,
                                                            String study, String deviceID, String desiredPath, String rootDirectory, HashMap<File, String> desiredNames) {
        HashMap<File, String> result = uploadZipped(ctx, uploadURL, filesToUpload, dob, lastName, study, deviceID, desiredPath, rootDirectory, desiredNames);
        ArrayList<File> successes = new ArrayList<File>();
        for (File s : result.keySet()) {
            if (isUploadSuccess(result.get(s))) {
                successes.add(s);
            }
        }
        return successes;
    }

}