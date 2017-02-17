package edu.neu.android.wocketslib.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;

public class AuthorizationChecker {
    public static final String TAG = "AuthorizationChecker";

    public static final String FLAG_PID_UNDEFINED = "FLAG_PID_UNDEFINED";
    public static final String FLAG_COULD_NOT_CHECK_PID = "FLAG_COULD_NOT_CHECK_PID";
    public static final String FLAG_COULD_NOT_SET_PHONE_ID = "FLAG_COULD_NOT_SET_PHONE_ID";
    //	public static final String FLAG_COULD_NOT_CHECK_PHONE_ID = "FLAG_COULD_NOT_CHECK_PHONE_ID";
    public static final String FLAG_NO_PHONE_ID = "FLAG_NO_PHONE_ID";
    public static final String FLAG_NO_PHONE_ID_COULD_NOT_SET_PHONE_ID = "FLAG_NO_PHONE_ID_COULD_NOT_SET_PHONE_ID";
    public static final String FLAG_NO_PHONE_ID_FLAG_PID_UNDEFINED = "FLAG_NO_PHONE_ID_FLAG_PID_UNDEFINED";
    public static final String FLAG_NO_PHONE_ID_SUCCESS = "FLAG_NO_PHONE_ID_SUCCESS";
    public static final String FLAG_SUCCESS = "FLAG_SUCCESS";
    public static final String FLAG_DUPLICATED = "FLAG_DUPLICATED";

    private static final String ERROR_STRING_SERVER_ERROR_2 = "ERROR -2:";
    private static final String ERROR_STRING_SERVER_ERROR_3 = "ERROR -3:";
    private static final String ERROR_STRING_SERVER_ERROR_4 = "ERROR -4:";

    public static final String FLAG_PID_UNDEFINED_STR = "FLAG_PID_UNDEFINED";
    public static final String FLAG_PID_UNDEFINED_THIS_STUDY_STR = "FLAG_PID_UNDEFINED_THIS_STUDY";
    public static final String FLAG_COULD_NOT_CHECK_PID_STR = "FLAG_COULD_NOT_CHECK_PID";
    public static final String FLAG_COULD_NOT_CHECK_PHONE_ID_STR = "FLAG_COULD_NOT_CHECK_PHONE_ID";

    public static final long NEVER = 0;
    public static final String SUBJECT_ID_UNDEFINED = "SUBJECT_ID_UNDEFINED";
    public static final long SUBJECT_DATE_OF_BIRTH_UNDEFINED = 0;
    public static final String SUBJECT_LAST_NAME_UNDEFINED = null;

    // There are four conditions that we might have when trying to determine if authorized
    public static final int AUTH_SERVER_REPORTS_NOT_AUTH = 0;
    public static final int AUTH_SERVER_REPORTS_AUTHORIZED = 1;
    public static final int AUTH_SERVER_NOT_CHECKED = 2;
    public static final int AUTH_SERVER_COULD_NOT_BE_CHECKED = 3;
    public static final int AUTH_SERVER_NO_DATA_SO_CANT_CHECK = 4;
    public static final int AUTH_SERVER_CHECK_NOT_REQUIRED_YET = 5;
    public static final int AUTH_SERVER_CHECK_STARTED = 6;

    public static boolean isAuthorized(Context aContext) {
        AuthorizationChecker.isAuthorized24hrs(aContext);

        String subjectID = DataStorage.GetValueString(aContext,
                DataStorage.KEY_SUBJECT_ID, AuthorizationChecker.SUBJECT_ID_UNDEFINED);
        String subjectLastName = DataStorage.GetValueString(aContext,
                DataStorage.KEY_LAST_NAME, AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED);
        long dob = DataStorage.GetValueLong(aContext,
                DataStorage.KEY_DATE_OF_BIRTH, AuthorizationChecker.SUBJECT_DATE_OF_BIRTH_UNDEFINED);

        return subjectID != AuthorizationChecker.SUBJECT_ID_UNDEFINED &&
                subjectLastName != AuthorizationChecker.SUBJECT_LAST_NAME_UNDEFINED &&
                dob != AuthorizationChecker.SUBJECT_DATE_OF_BIRTH_UNDEFINED;
    }

    // TODO Probably want to ensure no project uses this and eventually remove
    private static boolean IsAuthorized(Context aContext, boolean isShowToast) {
        int result = IsAuthorizedServer(aContext, Globals.MINUTES_60_IN_MS, false);

        if (result == AUTH_SERVER_REPORTS_AUTHORIZED) {
            if (isShowToast)
                Toast.makeText(aContext, "Result: authorized", Toast.LENGTH_LONG).show();
            return true;
        } else if (result == AUTH_SERVER_COULD_NOT_BE_CHECKED) {
            if (isShowToast)
                Toast.makeText(aContext, "Result: can't check", Toast.LENGTH_LONG).show();
            return true;
        } else if (result == AUTH_SERVER_CHECK_STARTED) {
            if (isShowToast)
                Toast.makeText(aContext, "Result: check started", Toast.LENGTH_LONG).show();
            return true;
        } else if (result == AUTH_SERVER_REPORTS_NOT_AUTH) {
            if (isShowToast)
                Toast.makeText(aContext, "Result: not authorized", Toast.LENGTH_LONG).show();
            return false;
        } else if (result == AUTH_SERVER_NOT_CHECKED) {
            if (isShowToast)
                Toast.makeText(aContext, "Result: server not checked", Toast.LENGTH_LONG).show();
            return true;
        } else if (result == AUTH_SERVER_NO_DATA_SO_CANT_CHECK) {
            if (isShowToast)
                Toast.makeText(aContext, "Result: server not checked because info not retrieved from datastorage", Toast.LENGTH_LONG).show();
            return true;
        } else {
            if (isShowToast)
                Toast.makeText(aContext, "Result: case unknown!", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    public static void DoAuthorization(Context aContext, Activity anActivity) {
        DoAuthorization(aContext, anActivity, false);
    }

//    public static void DoAuthorization(Context aContext, Service aService) {
//        DoAuthorization(aContext, aService, false);
//    }

    public static void DoAuthorization(Context aContext, Activity anActivity, boolean isShowToast) {
        if (IsAuthorized(aContext, isShowToast)) {
            Log.o(TAG, "Authorized");
        } else {
            // Otherwise unauthorized user
            Log.o(TAG, "Not Authorized");
            String packageURI_string = "package:" + aContext.getPackageName().trim();
            Uri packageURI = Uri.parse(packageURI_string);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            aContext.startActivity(uninstallIntent);
            anActivity.finish();
        }
    }

//    public static void DoAuthorization(Context aContext, Service aService, boolean isShowToast) {
//        if (IsAuthorized(aContext, isShowToast)) {
//            Log.v("check", "Authorized");
//        } else {
//            // Otherwise unauthorized user
//            Log.v("check", "Not Authorized");
//            String packageURI_string = "package:" + aContext.getPackageName().trim();
//            Uri packageURI = Uri.parse(packageURI_string);
//            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
//            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            aContext.startActivity(uninstallIntent);
//            aService.stopSelf();
//        }
//    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
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
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Error in closing file in convertStreamToString: " + e.toString());
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Grab the subjectID from the server.
     *
     * @param aContext  Context
     * @param aLastname Participant last name
     * @param aDOB      Participant date of birth
     * @return A String with the subject ID that is prepended with "PID:"
     */
    public static String getSubjectIDServer(Context aContext, String aLastname, Date aDOB) {
        // Default to not knowing in case get some odd error that kicks out
        String pid = FLAG_COULD_NOT_CHECK_PID;

        HttpGet httpget;
        if (!NetworkChecker.isOnline(aContext)) {
            Log.o(TAG, "No Internet connection so cannot check for subjectID now");
            return FLAG_COULD_NOT_CHECK_PID;
        }
        try {
            httpget = new HttpGet(Globals.PHP_SERVER_GET_PID + "?study=" + Globals.STUDY_SERVER_NAME + "&lastName=" + aLastname.replace(" ", "%20") + "&dob="
                    + DateHelper.getServerDateString(aDOB));
            String msg = Globals.PHP_SERVER_GET_PID + "?study=" + Globals.STUDY_SERVER_NAME + "&lastName=" + aLastname.replace(" ", "%20") + "&dob="
                    + DateHelper.getServerDateString(aDOB);
            Log.o(TAG, "Server HttpGet call: " + msg);

            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is
            // established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            HttpResponse httpresponse = httpClient.execute(httpget);
            HttpEntity resEntity = httpresponse.getEntity();
            InputStream is = resEntity.getContent();
            String return_result;
            return_result = convertStreamToString(is);

            if (return_result != null) {
                if (return_result.contains(ERROR_STRING_SERVER_ERROR_2)) {
                    Log.e(TAG, "Error from getSubjectIDServer: ERROR_STRING_SERVER_ERROR_2: " + return_result);
                    return FLAG_PID_UNDEFINED;
                } else if (return_result.contains(ERROR_STRING_SERVER_ERROR_3)) {
                    Log.e(TAG, "Error from getSubjectIDServer: ERROR_STRING_SERVER_ERROR_3: " + return_result);
                    return FLAG_PID_UNDEFINED;
                }

                // Should have a valid PID string. Parse it.
                // Remove the /n
                if (return_result.length() > 1)
                    return_result = return_result.substring(0, return_result.length() - 1);
                pid = return_result;
                Log.o(TAG, "Query of server for subject ID successful. ID: " + pid);
                return pid;
            }
            else { // Server result equals null
                Log.e(TAG, "Server returned null value");
                return FLAG_COULD_NOT_CHECK_PID;
            }
        } catch (ConnectTimeoutException e) {
            //if (Globals.IS_DEBUG)
                Log.e(TAG, "Connect timeout exception. Cannot get to server to check for subjectID " + e.toString());
            e.printStackTrace();
            return FLAG_COULD_NOT_CHECK_PID;
        } catch (Exception e) {
            //if (Globals.IS_DEBUG)
                Log.e(TAG, "Unknown error in connecting to server to check for subjectID: " + e.toString() + " " + Log.getStackTraceString(e));
            e.printStackTrace();
            return FLAG_COULD_NOT_CHECK_PID;
        }
    }

    public static String getPhoneIDServer(Context aContext, String aLastname, Date aDOB) {
        String phoneid = FLAG_COULD_NOT_CHECK_PHONE_ID_STR;

        if (!NetworkChecker.isOnline(aContext)) {
            Log.o(TAG, "No Internet connection so cannot check for phoneID now");
            return FLAG_COULD_NOT_CHECK_PHONE_ID_STR;
        }

        try {
            HttpGet httpget;

            httpget = new HttpGet(Globals.PHP_SERVER_GET_PHONE_ID + "?study=" + Globals.STUDY_SERVER_NAME + "&lastName=" + aLastname.replace(" ", "%20") + "&dob="
                    + DateHelper.getServerDateString(aDOB));
            String msg = Globals.PHP_SERVER_GET_PHONE_ID + "?study=" + Globals.STUDY_SERVER_NAME + "&lastName=" + aLastname.replace(" ", "%20") + "&dob="
                    + DateHelper.getServerDateString(aDOB);

            Log.o(TAG, "Try HttpGet: " + msg);

            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is
            // established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            HttpResponse httpresponse = httpClient.execute(httpget);
            HttpEntity resEntity = httpresponse.getEntity();
            InputStream is = resEntity.getContent();
            String return_result;
            return_result = convertStreamToString(is);

            if (return_result != null) {
                if (return_result.contains(ERROR_STRING_SERVER_ERROR_2)) {
                    Log.e(TAG, "Authorization server check returned error ERROR_STRING_SERVER_ERROR_2: " + return_result);
                    return FLAG_PID_UNDEFINED_STR;
                } else if (return_result.contains(ERROR_STRING_SERVER_ERROR_3)) {
                    Log.e(TAG, "Authorization server check returned error ERROR_STRING_SERVER_ERROR_3: " + return_result);
                    return FLAG_PID_UNDEFINED_THIS_STUDY_STR;
                } else if (return_result.contains(ERROR_STRING_SERVER_ERROR_4)) {
                    Log.e(TAG, "Authorization server check returned error ERROR_STRING_SERVER_ERROR_4: " + return_result + "Note: this can happen the first time a new ID is used.");
                    return FLAG_NO_PHONE_ID;
                }

                // Should have the phone ID. Parse the string to get it.
                // Remove the /n
                if (return_result.length() > 1)
                    return_result = return_result.substring(0, return_result.length() - 1);
                phoneid = return_result;
                if (phoneid.startsWith("PHONEID:")) {
                    phoneid = phoneid.substring(8);
                    if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                        Log.o(TAG,
                              "Query of server for phone ID successful. Phone ID: "
                                      + RSACipher.encrypt(phoneid, aContext));
                    } else {
                        Log.o(TAG,
                              "Query of server for phone ID successful. Phone ID: " + phoneid);
                    }
                    return phoneid;
                } else {
                    Log.e(TAG, "Could not parse PhoneID returned from server (should start with PHONEID): " + phoneid);
                    return FLAG_COULD_NOT_CHECK_PHONE_ID_STR;
                }
            } else { //return_result == null
                return FLAG_COULD_NOT_CHECK_PHONE_ID_STR;
            }
        } catch (ConnectTimeoutException e) {
            //if (Globals.IS_DEBUG)
                Log.e(TAG, "Connect timeout exception. Cannot get to server to check for phone ID " + e.toString());
            //e.printStackTrace();
            return FLAG_COULD_NOT_CHECK_PHONE_ID_STR;
        } catch (Exception e) {
            //if (Globals.IS_DEBUG)
                Log.e(TAG, "Unknown error in connecting to server to check for phone ID: " + e.toString() + " " + Log.getStackTraceString(e));
            e.printStackTrace();
            return FLAG_COULD_NOT_CHECK_PHONE_ID_STR;
        }
    }

    public static String setPhoneIDServer(Context aContext, String anID, String aLastname, Date aDOB) {
        String result = FLAG_COULD_NOT_SET_PHONE_ID;

        if (!NetworkChecker.isOnline(aContext)) {
            //if (Globals.IS_DEBUG)
                Log.e(TAG, "No Internet connection so cannot try to set Phone ID now");
            return FLAG_COULD_NOT_SET_PHONE_ID;
        }

//		HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpget;
            httpget = new HttpGet(Globals.PHP_SERVER_REGISTER_PARTICIPANT + "?imei=" + anID + "&study=" + Globals.STUDY_SERVER_NAME + "&lastName="
                    + aLastname.replace(" ", "%20") + "&dob=" + DateHelper.getServerDateString(aDOB));
            String temp = Globals.PHP_SERVER_REGISTER_PARTICIPANT + "?imei=" + anID + "&study=" + Globals.STUDY_SERVER_NAME + "&lastName=" + aLastname.replace(" ", "%20")
                    + "&dob=" + DateHelper.getServerDateString(aDOB);
            Log.o(TAG, "Check: " + temp);

            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is
            // established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            HttpResponse httpresponse = httpClient.execute(httpget);
            HttpEntity resEntity = httpresponse.getEntity();
            InputStream is = resEntity.getContent();
            String return_result;
            return_result = convertStreamToString(is);

            if (return_result != null) {
                if (return_result.contains(ERROR_STRING_SERVER_ERROR_2)) {
                    Log.e(TAG, "Server error from setPhoneIDServer: ERROR_STRING_SERVER_ERROR_2: " + return_result);
                    return FLAG_PID_UNDEFINED;
                }
                // Parse message of form: SUCCESS: Registered IMEI 123 to
                // participant 6818
                if (return_result.length() > 1) {
                    if (return_result.startsWith("SUCCESS")) {
                        StringTokenizer st = new StringTokenizer(return_result);
                        // for (int i = 0; i < 6; i++) {
                        // Log.d("TOKEN", st.nextToken());
                        // }
                        // This used to be an Integer. Changed to String
                        // 3/1/13
                        result = st.nextToken();
                        Log.o(TAG, "SUCCESS! Set subject phone ID successful. ID: " + result);
                    }
                } else
                {
                    Log.e(TAG, "Unknown server error from setPhoneIDServer: " + return_result);
                    return FLAG_COULD_NOT_SET_PHONE_ID;
                }
            }
        } catch (ConnectTimeoutException e) {
            Log.e(TAG, "Connect timeout. Cannot get to server to check for subjectID " + e.toString());
            e.printStackTrace();
            return FLAG_COULD_NOT_SET_PHONE_ID;
        } catch (Exception e) {
            Log.e(TAG, "Error in connecting to server to check for subjectID: " + e.toString());
            e.printStackTrace();
            return FLAG_COULD_NOT_SET_PHONE_ID;
        }
        return result;
    }

//    private static boolean isNumeric(String input) {
//        for (char c : input.toCharArray()) {
//            if (!Character.isDigit(c))
//                return false;
//        }
//        return true;
//    }

    /**
     * Check either the saved value or the server (if more than 24 hours has elapsed) to see if this person is
     * authorized.
     *
     * @param aContext           Context
     * @param checkAfterTimeInMS Only check the server if it has been more than this time since the
     *                           last check that reached the server (in milliseconds)
     * @return Int representing authorization status
     */
    public static int IsAuthorizedServer(final Context aContext, long checkAfterTimeInMS, boolean isPopDialog) {
        if(!Globals.IS_AUTHORIZATION_NEEDED) { 
        	return AUTH_SERVER_CHECK_NOT_REQUIRED_YET;
        }
    	long lastAuthorizationTime = DataStorage.GetValueLong(aContext, DataStorage.KEY_LAST_AUTHORIZED_TIME, NEVER);

        // Grab all the relevant information from the datastore
        long dob = DataStorage.GetValueLong(aContext, DataStorage.KEY_DATE_OF_BIRTH, SUBJECT_DATE_OF_BIRTH_UNDEFINED);
        String subjectID = DataStorage.GetValueString(aContext, DataStorage.KEY_SUBJECT_ID, SUBJECT_ID_UNDEFINED);
        String subjectLastName = DataStorage.GetValueString(aContext, DataStorage.KEY_LAST_NAME, SUBJECT_LAST_NAME_UNDEFINED);
        String testData = DataStorage.GetValueString(aContext, DataStorage.KEY_TEST_DATA, "DefaultReturnValue");
        Date aDOB = null;

        // Aida checking for error in date when trying to debug auth checker
        if (dob != SUBJECT_DATE_OF_BIRTH_UNDEFINED)
        {
        	try {
        		aDOB = DateHelper.getDate(dob);
        	} catch (Exception e) {
        		Log.e(TAG, "Date is defined but unable to parse date: " + e.toString() + ". Return server not checked for authorization.");
        		return AUTH_SERVER_NOT_CHECKED; // There is a problem with the DataStorage. Don't prevent from checking again the next time.
            }
        }

        // First, check if we have valid info for the three bits of info needed to check authorization
        if (subjectID.equals(SUBJECT_ID_UNDEFINED) || subjectLastName.equals(SUBJECT_LAST_NAME_UNDEFINED) || aDOB == null) {       	

            Log.o(TAG, "Some important info for authorization is not defined yet.");
        	Log.o(TAG, "subjectID: " + subjectID + " subjectLastName: " + subjectLastName
           		 + " DOB in long: " + dob + " DOB in date: " + aDOB + "testData: " + testData);

            if (isPopDialog) {
                Log.o(TAG, "Pop dialog for authorization check because login info not entered");
                Intent i = new Intent(aContext, AuthorizationCheckerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                aContext.startActivity(i);
                return AUTH_SERVER_NO_DATA_SO_CANT_CHECK;
            } else {
                // Should never get here
                Log.e(TAG, "App cannot run because login info not available but SHOULD have data.");
                return AUTH_SERVER_NO_DATA_SO_CANT_CHECK;
            }
        } else { // All the info we need is defined correctly in the datastorage and has been retrieved

            long lastAttemptedAuthorizationTime = DataStorage.GetValueLong(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, NEVER);
            long currentTime = System.currentTimeMillis();

            // In case someone has manipulated dates on the phone, reset values to something reasonable
            if (lastAuthorizationTime > currentTime)
                DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_TIME, NEVER);
            if (lastAttemptedAuthorizationTime > currentTime)
                DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, NEVER);

			// Check if we need to check again, based on the last time authorized (usually 24 hours)
			// but only check once every 10 minutes if last check not successful.
			// We used this successfully for testing: if (((currentTime - lastAuthorizationTime) > Globals.MINUTES_5_IN_MS) &&
			if (((currentTime - lastAuthorizationTime) > checkAfterTimeInMS) &&
				 ((currentTime - lastAttemptedAuthorizationTime) > Globals.MINUTES_10_IN_MS)) { //TODO consider a longer time?
				
                DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, currentTime);

//                try {
                    Log.o(TAG, "Launch AuthorizationAsyncChecker");
                    new AuthorizationAsyncChecker(aContext, subjectLastName, dob).execute((Void) null);
                    return AUTH_SERVER_CHECK_STARTED;
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "Error in Authorization checker: " + e.toString() + " " + Log.getStackTraceString(e));
//                    return AUTH_SERVER_COULD_NOT_BE_CHECKED;
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "Error in Authorization checker: " + e.toString() + " " + Log.getStackTraceString(e));
//                    return AUTH_SERVER_COULD_NOT_BE_CHECKED;
//                }
            }
            else // We don't need to check if authorized because not enough time has elapsed
            {
                return AUTH_SERVER_CHECK_NOT_REQUIRED_YET;
            }
        }
    }
    
    /*public static int IsAuthorizedServer(final Context aContext, long checkAfterTimeInMS, boolean isPopDialog) {
    	long currentTime = System.currentTimeMillis();
        long lastAuthorizationTime = DataStorage.GetValueLong(aContext, DataStorage.KEY_LAST_AUTHORIZED_TIME, NEVER);       
        long lastAttemptedAuthorizationTime = DataStorage.GetValueLong(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, NEVER);

        // In case someone has manipulated dates on the phone, reset values to something reasonable
        if (lastAuthorizationTime > currentTime) {
            DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_TIME, NEVER);
            Log.o(TAG, "lastAuthorizationTime was incorrect and set to zero");
        }
        if (lastAttemptedAuthorizationTime > currentTime) {
            DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, NEVER);
            Log.o(TAG, "lastAttemptedAuthorizationTime was incorrect and set to zero");
        }
        
        // Check if we need to check again, based on the last time authorized (usually 24 hours)
		// but only check once every 10 minutes if last check not successful.
		// We used this successfully for testing: if (((currentTime - lastAuthorizationTime) > Globals.MINUTES_5_IN_MS) &&
		if (((currentTime - lastAuthorizationTime) > checkAfterTimeInMS)
				&& ((currentTime - lastAttemptedAuthorizationTime) > Globals.MINUTES_10_IN_MS)) { // TODO // consider a longer time?

			DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, currentTime);

			// Grab all the relevant information from the datastore
			long dob = DataStorage.GetValueLong(aContext, DataStorage.KEY_DATE_OF_BIRTH, SUBJECT_DATE_OF_BIRTH_UNDEFINED);
			String subjectID = DataStorage.GetValueString(aContext, DataStorage.KEY_SUBJECT_ID, SUBJECT_ID_UNDEFINED);
			String subjectLastName = DataStorage.GetValueString(aContext, DataStorage.KEY_LAST_NAME, SUBJECT_LAST_NAME_UNDEFINED);
			String testData = DataStorage.GetValueString(aContext, "TestData", "DefaultReturnValue");
			Date aDOB = null;
			if (dob != SUBJECT_DATE_OF_BIRTH_UNDEFINED) {
				aDOB = DateHelper.getDate(dob);
			}

			// First, check if we have valid info for the three bits of info
			// needed to check authorization
			if (subjectID.equals(SUBJECT_ID_UNDEFINED)
					|| subjectLastName.equals(SUBJECT_LAST_NAME_UNDEFINED)
					|| aDOB == null) {

				Log.o(TAG, "Some important info for authorization is not defined yet.");
				Log.o(TAG, "subjectID: " + subjectID + " subjectLastName: "
						+ subjectLastName + " DOB in long: " + dob
						+ " DOB in date: " + aDOB + "testData: " + testData);

				if (isPopDialog) {
					Log.o(TAG, "Pop dialog for authorization check because login info not entered");
					Intent i = new Intent(aContext, AuthorizationCheckerActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					aContext.startActivity(i);
					return AUTH_SERVER_NO_DATA_SO_CANT_CHECK;
				} else { // Should never get here
					Log.e(TAG, "App cannot run because login info not available but SHOULD have data.");
					return AUTH_SERVER_COULD_NOT_BE_CHECKED;
				}
			} else { // All the info we need is defined correctly in the datastorage and has been retrieved
				Log.o(TAG, "Launch AuthorizationAsyncChecker");
				new AuthorizationAsyncChecker(aContext, subjectLastName, dob).execute((Void) null);
				return AUTH_SERVER_CHECK_STARTED;
			}

		} else {// We don't need to check if authorized because not enough time has elapsed
			return AUTH_SERVER_CHECK_NOT_REQUIRED_YET;
		}
        
    }*/

    public static boolean isAuthorized24hrs(Context aContext) {
        return isAuthorized24hrs(aContext, true);
    }

    /**
     * Check if the phone is currently authorized. Check no more than once very 24 hours.
     * Do not de-authorize unless there is a definite confirmation with the server.
     *
     * @param aContext    Context
     * @param isPopDialog If true, pop up a dialog box
     * @return True if phone was authorized at some point and has definitely not been de-authorized
     */
    public static boolean isAuthorized24hrs(Context aContext, boolean isPopDialog) {
        int isAuthorized = IsAuthorizedServer(aContext, Globals.HOURS24_MS, isPopDialog);
        switch (isAuthorized) {
            case AUTH_SERVER_REPORTS_AUTHORIZED:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_REPORTS_AUTHORIZED");
                return true;
            case AUTH_SERVER_NOT_CHECKED:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_NOT_CHECKED");
                return true;
            case AUTH_SERVER_CHECK_NOT_REQUIRED_YET:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_CHECK_NOT_REQUIRED_YET");
                return true;
            case AUTH_SERVER_COULD_NOT_BE_CHECKED:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_COULD_NOT_BE_CHECKED");
                return true;
            case AUTH_SERVER_CHECK_STARTED:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_CHECK_STARTED");
                return true;

            case AUTH_SERVER_NO_DATA_SO_CANT_CHECK:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_NO_DATA_SO_CANT_CHECK");
                return false;
            case AUTH_SERVER_REPORTS_NOT_AUTH:
                Log.o(TAG, "isAuthorized24hrs received AUTH_SERVER_REPORTS_NOT_AUTH");
                return false;
        }
        return false;
    }
}
