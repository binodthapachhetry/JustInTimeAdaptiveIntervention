package edu.neu.android.wocketslib.support;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.utils.DateHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

public class AuthorizationAsyncChecker extends AsyncTask<Void, Integer, Integer> {
    public static final String TAG = "AuthorizationAsyncChecker";

    private Context aContext;
    private String subjectLastName;
    private long dob;

    public AuthorizationAsyncChecker(Context aContext, String subjectLastName, long dob) {
        super();
        this.aContext = aContext;
        this.subjectLastName = subjectLastName;
        this.dob = dob;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result == AuthorizationChecker.AUTH_SERVER_REPORTS_NOT_AUTH) {
            //Toast.makeText(aContext, "This phone is not authorized to run the study app...", Toast.LENGTH_LONG).show();
            Intent i = new Intent(aContext, AuthorizationCheckerActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Required
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DataStorage.SetValue(aContext, DataStorage.KEY_IS_AUTHORIZED, false);
            DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, System.currentTimeMillis());
            aContext.startActivity(i);
        } else if (result == AuthorizationChecker.AUTH_SERVER_REPORTS_AUTHORIZED) {
            DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_TIME, System.currentTimeMillis());
            DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, System.currentTimeMillis());
            DataStorage.SetValue(aContext, DataStorage.KEY_IS_AUTHORIZED, true);
        } else
        {
            // Do nothing because for some reason check not possible
            DataStorage.SetValue(aContext, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, System.currentTimeMillis());
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        // We assume the calling code has checked that all params sent are legitimate

        Date aDOB = DateHelper.getDate(dob);
        String IMEI = PhoneInfo.getID(aContext);

        Log.o(TAG, "in Async subjectLastName: " + subjectLastName + "DOB in long: " + dob + "DOB in date: " + aDOB.toString());

        // This can probably be removed later
        if (IMEI == null)
        {
            Log.e(TAG, "PhoneInfo.getID returned null and so could not check server for authorization");
            return AuthorizationChecker.AUTH_SERVER_COULD_NOT_BE_CHECKED;
        }

        // First, check for and set the aSubjectID
        String aSubjectID = AuthorizationChecker.getSubjectIDServer(aContext, subjectLastName, aDOB);
        if (aSubjectID.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID)) {
            Log.e(AuthorizationChecker.TAG, "Could not check subjectID");
            return AuthorizationChecker.AUTH_SERVER_COULD_NOT_BE_CHECKED;
        } else if (aSubjectID.equals(AuthorizationChecker.FLAG_PID_UNDEFINED)) {
            Log.e(AuthorizationChecker.TAG, "User not authorized");
            // TODO This is a major bug. This could either be (1) the PID is not defined, in which case this
            // is correct, or () that the MY_SQL on the server is down, in which case it is not.

            // TODO This is a temporary fix until the server PHP code can be improved: if previously authorized, then ignore this result
            if (DataStorage.GetValueBoolean(aContext, DataStorage.KEY_IS_AUTHORIZED, false))
            {
                Log.e(TAG, "ERROR: PID undefined but can't differentiate between server down and PID removed. Leaving authorized because previously authorized.");
                return AuthorizationChecker.AUTH_SERVER_COULD_NOT_BE_CHECKED;
            }
            else
            {
                Log.e(TAG, "ERROR: PID undefined but can't differentiate between server down and PID removed. Deauthorizing because NOT previously authorized.");
                return AuthorizationChecker.AUTH_SERVER_REPORTS_NOT_AUTH;
            }
        } else if (aSubjectID.startsWith("PID:")) {
            // Only accept the PID if the PHP code on the server returns a value with PID: in front of it.
            aSubjectID = aSubjectID.substring(4);
            DataStorage.SetValue(aContext, DataStorage.KEY_SUBJECT_ID, aSubjectID);
        } else {
            Log.e(AuthorizationChecker.TAG, "Could not check PID (possible portal error)");
            return AuthorizationChecker.AUTH_SERVER_COULD_NOT_BE_CHECKED;
        }

        // Only get here if subjectID(PID) is retrieved. Now check if phoneID on server equals IMEI
        boolean isPhoneIDDuplicated = false;
        // Try to check server
        String phoneIDServer = AuthorizationChecker.getPhoneIDServer(aContext, subjectLastName, aDOB);
        // Check if valid phoneID or error message
        if (phoneIDServer.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID_STR) ||
                phoneIDServer.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PHONE_ID_STR) ||
                phoneIDServer.equals(AuthorizationChecker.FLAG_PID_UNDEFINED_STR) ||
                phoneIDServer.equals(AuthorizationChecker.FLAG_PID_UNDEFINED_THIS_STUDY_STR) ||
                phoneIDServer.equals(AuthorizationChecker.FLAG_NO_PHONE_ID)) {
            // A phoneID is either not set or not retrievable
            Log.o(TAG, "Trouble checking for phone ID after successfully grabbed a subjectID");
            return AuthorizationChecker.AUTH_SERVER_COULD_NOT_BE_CHECKED;
        }
        else {
            isPhoneIDDuplicated = !phoneIDServer.equals(IMEI);
        }

        if (isPhoneIDDuplicated) {  //Removed isNumeric(phoneIDServer) &&
            Log.o(TAG, "Not Authorized because Phone ID is duplicated");
            return AuthorizationChecker.AUTH_SERVER_REPORTS_NOT_AUTH;
        } else {
            if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                try {
                    Log.o(AuthorizationChecker.TAG,
                          "Authorized. SubjectID (PID) and phoneID: " + aSubjectID
                                  + " " + RSACipher.encrypt(phoneIDServer,aContext));
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
            } else {
                Log.o(AuthorizationChecker.TAG,
                      "Authorized. SubjectID (PID) and phoneID: " + aSubjectID
                              + " " + phoneIDServer);
            }
            return AuthorizationChecker.AUTH_SERVER_REPORTS_AUTHORIZED;
        }
//            // Pop up uninstall program
//            String packageURI_string = "package:" + aContext.getPackageName().trim();
//            Uri packageURI = Uri.parse(packageURI_string);
//            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
//            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            aContext.startActivity(uninstallIntent);
    }
}
