package edu.neu.android.wocketslib.support;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.broadcastreceivers.MonitorServiceBroadcastReceiver;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

public class AuthorizationCheckerActivity extends BaseActivity {
    public static final String TAG = "AuthorizationCheckerActivity";
//	public static final int RESULT_SUCCESS = 0;
//	public static final int RESULT_FAIL = 1;

    private String lastName = null;
    private long dob;
    private String IMEI;
    private String encryptedIMEI;
    private String subjectID;

    private EditText lastNameText;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    private Button btnDOB;

    public AuthorizationCheckerActivity() {
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }

    private String dateStr(Date aDate) {
        return sdf.format(aDate);
    }

    private String dateStr(long aDate) {
        return sdf.format(new Date(aDate));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, TAG);
        setContentView(R.layout.authorization_info_dialog);
        lastNameText = (EditText) findViewById(R.id.editLastName);
        btnDOB = (Button) findViewById(R.id.authrzDOB);
        Button cancel = (Button) findViewById(R.id.authorizeCancel);
        Button submit = (Button) findViewById(R.id.authorizeSubmit);
        TextView title = (TextView) findViewById(R.id.authrzTitle);

        lastName = DataStorage.GetLastName(getApplicationContext(), null);
        dob = DataStorage.GetDateOfBirth(getApplicationContext(), -1);
        IMEI = PhoneInfo.getID(AuthorizationCheckerActivity.this);
        if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
            try {
                encryptedIMEI = RSACipher.encrypt(IMEI,getApplicationContext());
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        } else {
            encryptedIMEI = IMEI;
        }
        subjectID = AuthorizationChecker.SUBJECT_ID_UNDEFINED;

        if (lastName != null && dob != -1)
            title.setText("Please confirm your authorized information for this app and submit.");
        else
            title.setText("Please input your authorized information for this app and submit.");

        if (lastName != null)
            lastNameText.setText(lastName);
        Date dobDate;
        if (dob != -1) {
            dobDate = new Date(dob);
            btnDOB.setText(dateStr(dobDate));
        } else {
            dobDate = new Date();
            btnDOB.setText("Select date");
        }
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                Date changedDate = new Date(selectedYear - 1900, selectedMonth, selectedDay);
                btnDOB.setText(sdf.format(changedDate));
                dob = changedDate.getTime();
            }
        }, dobDate.getYear() + 1900, dobDate.getMonth(), dobDate.getDate());
        datePickerDialog.setTitle("Select your date of birth");
        btnDOB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                uninstallApp();
            }
        });
        submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    //noinspection ConstantConditions
                    lastName = lastNameText.getText().toString();
                } catch (java.lang.NullPointerException e) {
                    lastName = null;
                }
                if (lastName != null && dob != -1) {
                    Log.o(TAG, Log.USER_ACTION, "User entered login info: " + lastName + " " + dateStr(dob));
                    if (NetworkChecker.isOnline(AuthorizationCheckerActivity.this))
                        new CheckAuthorizationSyncTask().execute((Void) null);
                    else {
                        Log.o(TAG, Log.USER_ACTION, "User entered login info but no Internet connection: " + lastName + " " + dateStr(dob));
                        new AlertDialog.Builder(AuthorizationCheckerActivity.this).setTitle("No Internet connection")
                        		.setCancelable(false)
                                .setMessage("Please connect Internet in order to complete authorization checking.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intentBluetooth = new Intent();
                                        intentBluetooth.setAction(android.provider.Settings.ACTION_WIFI_SETTINGS);
                                        startActivity(intentBluetooth);
                                        ((ApplicationManager) getApplication()).killAllActivities();
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ((ApplicationManager)
                                // getApplication()).killAllActivities();
                            }
                        }).create().show();
                    }
                } else {
                    Log.o(TAG, Log.USER_ACTION, "User entered invalid login information: " + lastName + " " + dateStr(dob));
                    new AlertDialog.Builder(AuthorizationCheckerActivity.this).setCancelable(false).setTitle("Information invalid").setMessage("Please input valid information.")
                            .setPositiveButton("OK", null).create().show();
                }
            }
        });
    }

    private class CheckAuthorizationSyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog = null;

        private void setupAuthorization(long dob, String lastName, String subjectID){
            Log.o(TAG,"Authorized", "Authorization completed and so sending broadcast to start the service");
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_DATE_OF_BIRTH, dob);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_LAST_NAME, lastName);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_SUBJECT_ID, subjectID);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_LAST_AUTHORIZED_TIME, System.currentTimeMillis());
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, System.currentTimeMillis());
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_IS_AUTHORIZED, true);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_TEST_DATA, "InitializedValue");
            if (Globals.IS_AUTO_START_SERVICE_AT_AUTHORIZATION) {
                Intent i = new Intent(
                        MonitorServiceBroadcastReceiver.TYPE_START_SENSOR_MONITOR_SERVICE_NOW);
                sendBroadcast(i);
                Toast.makeText(getApplicationContext(),
                        "Starting the service...", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(AuthorizationCheckerActivity.this, "Checking authorization...",
                    "It may take some time to check if you are registered for the study on the server. Please wait...");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dialog != null)
                dialog.dismiss();
            if (result.equals(AuthorizationChecker.FLAG_SUCCESS)) {
                Log.o(TAG, Log.USER_ACTION, "User received authorization message: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                setupAuthorization(dob, lastName, subjectID);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this).setTitle("Authorized!")
                		.setCancelable(false)
                        .setMessage("Congratulations! Welcome to the study. " + "You can continue using the app.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AuthorizationCheckerActivity.this.finish();
                            }
                        }).create().show();
            } else if (result.equals(AuthorizationChecker.FLAG_PID_UNDEFINED)) {
                Log.o(TAG, Log.USER_ACTION, "User received not registered on study server message; asked to contact research team: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this)
                		.setCancelable(false)
                		.setTitle("Authorization failed")
                        .setMessage(
                                "Sorry, your information is not registered on the study server. Please contact the study staff "
                                        + "to set up your information so you can run it.").setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ((ApplicationManager)
                        // getApplication()).killAllActivities();
                    }
                }).create().show();
            } else if (result.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID)) {
                Log.o(TAG, Log.USER_ACTION, "User received cannot connect to the study server at this time message: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this)
                		.setCancelable(false)
                		.setTitle("Cannot authorize at this time")
                        .setMessage(
                                "Unfortunately, the app cannot connnect to the study server right now. Check that your phone can connnect to the Internet and try again. If you can connect to the Internet and still get this message, the study server may be down. Please try again later. ")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ((ApplicationManager)
                                // getApplication()).killAllActivities();
                            }
                        }).create().show();
            } else if ((result.equals(AuthorizationChecker.FLAG_DUPLICATED))) {
                Log.o(TAG, Log.USER_ACTION, "User received another phone registered message: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this).setTitle("Duplicate information found")
                        .setCancelable(false)
                		.setMessage("Warning, there is another phone registered with your information." + "Would you like to swap to this phone?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Log.o(TAG, Log.USER_ACTION, "User indicates Yes to new phone ID: " + encryptedIMEI);
                                new DuplicatedPhoneIDAsyncTask().execute((Void) null);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.o(TAG, Log.USER_ACTION, "User indicates NO to new phone ID: " + encryptedIMEI);
                        // ((ApplicationManager)
                        // getApplication()).killAllActivities();
                    }
                }).create().show();
            } else if (result.equals(AuthorizationChecker.FLAG_NO_PHONE_ID_COULD_NOT_SET_PHONE_ID)) {

                    new AlertDialog.Builder(AuthorizationCheckerActivity.this)
                            .setCancelable(false)
                            .setTitle("Cannot set phone ID in the server at this time")
                            .setMessage(
                                    "Unfortunately, the app cannot set your phone ID right now. Check that your phone can connnect to the Internet and try again. if you can connect to the Internet and still get this message, the study server may be down. Please try again later. ")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // ((ApplicationManager)
                                    // getApplication()).killAllActivities();
                                }
                            }).create().show();
                } else if (result.equals(AuthorizationChecker.FLAG_NO_PHONE_ID_FLAG_PID_UNDEFINED)) {
                        new AlertDialog.Builder(AuthorizationCheckerActivity.this)
                                .setTitle("Phone ID not defined")
                                .setCancelable(false)
                                .setMessage(
                                        "Sorry, the phone ID is not defined. Please contact the study staff " +
                                        "to set up your phone ID.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // ((ApplicationManager)
                                        // getApplication()).killAllActivities();
                                    }
                                }).create().show();
                    } else if(result.equals(AuthorizationChecker.FLAG_NO_PHONE_ID_SUCCESS)) {
                setupAuthorization(dob, lastName, subjectID);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this).setTitle("Authorized!")
                                                                          .setCancelable(false)
                                                                          .setMessage(
                                                                                  "Congratulations! Welcome to the study. " +
                                                                                  "You can continue using the app.")
                                                                          .setPositiveButton("OK",
                                                                                             new DialogInterface.OnClickListener() {

                                                                                                 @Override
                                                                                                 public void onClick(
                                                                                                         DialogInterface dialog,
                                                                                                         int which) {

                                                                                                     AuthorizationCheckerActivity.this
                                                                                                             .finish();
                                                                                                 }
                                                                                             })
                                                                          .create().show();
            }

        }

        @Override
        protected String doInBackground(Void... params) {
            subjectID = AuthorizationChecker.getSubjectIDServer(AuthorizationCheckerActivity.this, lastName, new Date(dob));
            if (subjectID.equals(AuthorizationChecker.FLAG_PID_UNDEFINED)) {
                Log.o(TAG, Log.AUTHORIZATION, "User not authorized: PID undefined", lastName, dateStr(dob));
                return AuthorizationChecker.FLAG_PID_UNDEFINED;
            } else if (subjectID.startsWith("PID:")) {
                subjectID = subjectID.substring(4);
                Log.d(TAG, "PID is : " + subjectID + "(" + lastName + " " + dateStr(dob) + ")");
                String aPhoneID = AuthorizationChecker.getPhoneIDServer(AuthorizationCheckerActivity.this, lastName, new Date(dob));
                // String aPhoneID =
                // AuthorizationChecker.setPhoneIDServer(AuthorizationCheckerActivity.this,
                // IMEI, lastName, new Date(dob));
                if (aPhoneID.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID_STR)) {
                    Log.e(TAG, "Could not check PID: " + lastName + " " + dateStr(dob));
                    return AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID;
                } else if (aPhoneID.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PHONE_ID_STR)) {
                    Log.e(TAG, "Could not check Phone ID: " + lastName + " " + dateStr(dob));
                    return AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID;
                } else if (aPhoneID.equals(AuthorizationChecker.FLAG_PID_UNDEFINED_STR)) {
                    Log.o(TAG, Log.AUTHORIZATION, "Phone not authorized", lastName, dateStr(dob));
                    return AuthorizationChecker.FLAG_PID_UNDEFINED;
                } else if (aPhoneID.equals(AuthorizationChecker.FLAG_NO_PHONE_ID)) {
                    Log.o(TAG, Log.AUTHORIZATION, "No phone id registered on server", lastName, dateStr(dob));
                    String registrationResult = AuthorizationChecker.setPhoneIDServer(getApplicationContext(), IMEI, lastName, new Date(dob));
                    if (registrationResult.equals(AuthorizationChecker.FLAG_COULD_NOT_SET_PHONE_ID)) {
                        Log.o(TAG, Log.USER_ACTION, "User receives cannot set phone ID in the server at this time message: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                        return AuthorizationChecker.FLAG_NO_PHONE_ID_COULD_NOT_SET_PHONE_ID;
                    } else if (registrationResult.equals(AuthorizationChecker.FLAG_PID_UNDEFINED)) {
                        Log.o(TAG, Log.USER_ACTION, "User receives sorry phone ID is not defined message; contact study staff: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                        return AuthorizationChecker.FLAG_NO_PHONE_ID_FLAG_PID_UNDEFINED;
                    } else {
                        Log.o(TAG, Log.USER_ACTION, "User receives welcome to the study message: " + registrationResult + " " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                        return AuthorizationChecker.FLAG_NO_PHONE_ID_SUCCESS;
                    }
                } else {
                    if (aPhoneID.equals(IMEI)) {
                        if (Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
                            try {
                                Log.d(TAG, "Phone ID from server is : (" + RSACipher.encrypt(aPhoneID, getApplicationContext())
                                        + ") IMEI is:(" + encryptedIMEI + "): "
                                        + lastName + " " + dateStr(dob));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (GeneralSecurityException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "Phone ID from server is : (" + aPhoneID
                                    + ") IMEI is:(" + IMEI + "): "
                                    + lastName + " " + dateStr(dob));
                        }
                        return AuthorizationChecker.FLAG_SUCCESS;
                    } else {
                        Log.d(TAG, "Phone ID from server duplicated : (" + aPhoneID + ") IMEI is:(" + encryptedIMEI + "): " + lastName + " " + dateStr(dob));
                        return AuthorizationChecker.FLAG_DUPLICATED;
                    }
                }
            } else if (subjectID.equals(AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID)) {
                Log.e(TAG, "Could not check PID: " + lastName + " " + dateStr(dob));
                return AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID;
            } else {
                Log.e(TAG, "Could not check PID: " + lastName + " " + dateStr(dob) + " (Possible portal error)");
                return AuthorizationChecker.FLAG_COULD_NOT_CHECK_PID;
            }
        }
    }

    private class DuplicatedPhoneIDAsyncTask extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog = null;

        private void setupAuthorization(long dob, String lastName, String subjectID){
            Log.o(TAG,"Authorized", "Authorization completed and so sending broadcast to start the service");
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_DATE_OF_BIRTH, dob);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_LAST_NAME, lastName);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_SUBJECT_ID, subjectID);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_LAST_AUTHORIZED_TIME, System.currentTimeMillis());
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_LAST_AUTHORIZED_ATTEMPT_TIME, System.currentTimeMillis());
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_IS_AUTHORIZED, true);
            DataStorage.SetValue(AuthorizationCheckerActivity.this, DataStorage.KEY_TEST_DATA, "InitializedValue");
            if (Globals.IS_AUTO_START_SERVICE_AT_AUTHORIZATION) {
                Intent i = new Intent(
                        MonitorServiceBroadcastReceiver.TYPE_START_SENSOR_MONITOR_SERVICE_NOW);
                sendBroadcast(i);
                Toast.makeText(getApplicationContext(),
                               "Starting the service...", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(AuthorizationCheckerActivity.this, "Setting PhoneID...",
                                         "It may take some time to set the phoneID for the study on the server. Please wait...");
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog != null)
                dialog.dismiss();
            if (result.equals(AuthorizationChecker.FLAG_COULD_NOT_SET_PHONE_ID)) {
                Log.o(TAG, Log.USER_ACTION, "User receives could not set phone ID message: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this)
                        .setCancelable(false)
                        .setTitle("Cannot set phone ID in the server at this time")
                        .setMessage(
                                "Unfortunately, the app cannot set your phone ID right now. Check that your phone can connnect to the Internet and try again. if you can connect to the Internet and still get this message, the study server may be down. Please try again later. ")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ((ApplicationManager)
                                // getApplication()).killAllActivities();
                            }
                        }).create().show();
            } else if (result.equals(AuthorizationChecker.FLAG_PID_UNDEFINED)) {
                Log.o(TAG, Log.USER_ACTION, "User receives sorry phone ID not defined message; contact study staff: " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this).setTitle("Phone ID not defined")
                                                                          .setCancelable(false)
                                                                          .setMessage("Sorry, the phone ID is not defined. Please contact the study staff " + "to set up your phone ID.")
                                                                          .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                                                              @Override
                                                                              public void onClick(DialogInterface dialog, int which) {
                                                                                  // ((ApplicationManager)
                                                                                  // getApplication()).killAllActivities();
                                                                              }
                                                                          }).create().show();
            } else {
                Log.o(TAG, Log.USER_ACTION, "User receives welcome to the study message " + lastName + " " + dateStr(dob) + " " + encryptedIMEI);
                setupAuthorization(dob, lastName, subjectID);
                new AlertDialog.Builder(AuthorizationCheckerActivity.this).setTitle("Authorized!")
                                                                          .setCancelable(false)
                                                                          .setMessage("Congratulations! Welcome to the study. " + "You can continue using the app.")
                                                                          .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                                                              @Override
                                                                              public void onClick(DialogInterface dialog, int which) {

                                                                                  AuthorizationCheckerActivity.this.finish();
                                                                              }
                                                                          }).create().show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = AuthorizationChecker.setPhoneIDServer(getApplicationContext(), IMEI, lastName, new Date(dob));
            return result;
        }
    }

    private void uninstallApp() {
        Uri packageUri = Uri.parse("package:" + Globals.PACKAGE_NAME);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        startActivity(uninstallIntent);
    }
}
