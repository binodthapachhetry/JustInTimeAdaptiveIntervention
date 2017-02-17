package edu.neu.android.wocketslib.activities.wocketsnews;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.sensorstatus.StatusScreenActivity;
import edu.neu.android.wocketslib.broadcastreceivers.MonitorServiceBroadcastReceiver;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.dataupload.RawUploader;
import edu.neu.android.wocketslib.support.AppInfo;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Util;
import edu.neu.android.wocketslib.utils.WOCKETSException;

public class StaffSetupActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "StaffSetupActivity";

	private TextView txtID;
	private TextView txtCondition;
	private EditText txtFirstName;
	private EditText txtEmail;
	private EditText txtStartDate;
	private EditText txtHeight;
	private EditText txtStartWeight;
	private EditText txtGender;

	// private EditText txtFakeDate;

	// private static final String DEFAULT_FIRST_NAME =
	// "Click to enter first name";

	private void setFillParentHeightForView(View v, int height) {
		android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
		params.height = height;
		v.setLayoutParams(params);
	}

	private void setFillParentWidthForView(View v, int width) {
		android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
		params.width = width;
		v.setLayoutParams(params);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		setContentView(R.layout.staff_setup_activity);

		AuthorizationChecker.DoAuthorization(this.getApplicationContext(), this, false); // Show
																							// Toast

		Context aContext = getApplicationContext();
		DataStorage.SetIDAndCondition(aContext);

		findViewById(R.id.buttonCleanFiles).setOnClickListener(this);
		findViewById(R.id.buttonSendFiles).setOnClickListener(this);
		findViewById(R.id.buttonStatusScreen).setOnClickListener(this);
		findViewById(R.id.buttonBack).setOnClickListener(this);
		findViewById(R.id.buttonSave).setOnClickListener(this);
		findViewById(R.id.buttonShutdown).setOnClickListener(this);
		findViewById(R.id.buttonGenerateError).setOnClickListener(this);

		if (Util.isAria(this)) {
			setFillParentHeightForView((View) findViewById(R.id.ScrollView01), 340);
			setFillParentWidthForView(findViewById(R.id.buttonStatusScreen), 200);
			setFillParentWidthForView(findViewById(R.id.buttonCleanFiles), 200);
			setFillParentWidthForView(findViewById(R.id.buttonBack), 100);
			setFillParentWidthForView(findViewById(R.id.buttonSave), 200);
			setFillParentWidthForView(findViewById(R.id.buttonGenerateError), 200);
			setFillParentWidthForView(findViewById(R.id.buttonShutdown), 200);
		}

		txtID = (TextView) findViewById(R.id.txtID);
		txtCondition = (TextView) findViewById(R.id.txtCondition);
		txtFirstName = (EditText) findViewById(R.id.txtEditFirstName);
		txtEmail = (EditText) findViewById(R.id.txtEditEmail);
		txtHeight = (EditText) findViewById(R.id.txtEditHeight);
		txtStartDate = (EditText) findViewById(R.id.txtEditStartDate);
		txtStartWeight = (EditText) findViewById(R.id.txtEditStartWeight);
		txtGender = (EditText) findViewById(R.id.txtEditGender);

		// if (DataStorage.getStartDate(aContext, "").equals("")) // StartDate
		// not defined
		// {
		// Log.e(TAG,
		// "Warning: Attempted to read the sharedPreferences from the file.");
		// try {
		// DataStorage.ReadDataFile(getApplicationContext());
		// } catch (WOCKETSException e) {
		// Log.e(TAG, "Trouble reading sharedPreferences from the file.");
		// }
		// }
		//
		// Pull initial values from the datastore
		txtID.setText("Phone ID: " + DataStorage.getPhoneID(aContext, DataStorage.EMPTY));
		txtCondition.setText("Condtion: " + DataStorage.getStudyCondition(aContext, DataStorage.EMPTY));

		txtFirstName.setText(DataStorage.getFirstName(aContext, DataStorage.EMPTY));
		txtGender.setText(DataStorage.getGender(aContext, DataStorage.EMPTY));
		txtStartDate.setText(DataStorage.getStartDate(aContext, DataStorage.EMPTY));
		txtEmail.setText(DataStorage.getEmail(aContext, GetEmail()));

		float weight = DataStorage.getStartWeight(aContext, 0.0f);
		if (weight == 0)
			txtStartWeight.setText("");
		else
			txtStartWeight.setText(Float.toString(weight));

		float height = DataStorage.getHeight(aContext, 0.0f);
		if (height == 0)
			txtHeight.setText("");
		else
			txtHeight.setText(Float.toString(height));

	}

	public void onPause() {
		super.onPause();
	}

	private String GetEmail() {
		Account[] accounts = AccountManager.get(this).getAccounts();
		String possibleEmail = "";
		for (Account account : accounts) {
			// TODO: Check possibleEmail against an email regex or treat
			// account.name as an email address only for certain account.type
			// values.
			possibleEmail = account.name;
		}
		return possibleEmail;
	}

	private boolean isValidEmail(String possibleEmail) {
		// Set the email pattern string
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");

		// Match the given string with the pattern
		Matcher m = p.matcher(possibleEmail);

		// check whether match is found
		return m.matches();
	}

	private boolean isDataValid() {
		if (txtFirstName.getText().toString().equals("")) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Enter name").setMessage("You need to enter the subject's first name to save the data.")
					.setPositiveButton("Try again", null).show();
			return false;
		}

		if (txtGender.getText().toString().equals("")) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Enter gender").setMessage("You need to enter the subject's gender as m or f.")
					.setPositiveButton("Try again", null).show();
			return false;
		}

		if ((txtEmail.getText().toString().equals("")) || (!isValidEmail(txtEmail.getText().toString()))) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Enter email")
					.setMessage("You need to enter the subject's email to save the data. It doesn't look right.").setPositiveButton("Try again", null).show();
			return false;
		}

		float height = 0.0f;
		try {
			height = Float.parseFloat(txtHeight.getText().toString());
		} catch (Exception e) {
		}
		;

		if ((height < 90) || (height > 243)) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Enter height")
					.setMessage("You need to enter the height in centimeters. This value does not look right: " + height).setPositiveButton("Try again", null)
					.show();
			return false;
		}

		float weight = 0.0f;
		try {
			weight = Float.parseFloat(txtStartWeight.getText().toString());
		} catch (Exception e) {
		}
		;

		if ((weight < 80) || (weight > 450)) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Enter weight")
					.setMessage("You need to enter the weight in pounds. This value does not look right.").setPositiveButton("Try again", null).show();
			return false;
		}

		Date startDate = DataStorage.GetDate(txtStartDate.getText().toString());
		if ((startDate == null) || (startDate.before(DataStorage.GetDate("1/1/2011"))) || (startDate.after(DataStorage.GetDate("1/1/2013")))) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Enter start date")
					.setMessage("You need to enter the valid start date in this format MM/DD/YYYY. This value does not look right.")
					.setPositiveButton("Try again", null).show();
			return false;
		}

		// Set the values in the shared preferences

		boolean isSetAllSuccessfully = true;
		isSetAllSuccessfully = DataStorage.SetValue(getApplicationContext(), DataStorage.KEY_FIRST_NAME, txtFirstName.getText().toString());
		isSetAllSuccessfully = DataStorage.SetValue(getApplicationContext(), DataStorage.KEY_EMAIL, txtEmail.getText().toString());
		isSetAllSuccessfully = DataStorage.SetValue(getApplicationContext(), DataStorage.KEY_START_DATE, txtStartDate.getText().toString());
		isSetAllSuccessfully = DataStorage.SetValue(getApplicationContext(), DataStorage.KEY_START_WEIGHT, weight);
		isSetAllSuccessfully = DataStorage.SetValue(getApplicationContext(), DataStorage.KEY_HEIGHT, height);
		isSetAllSuccessfully = DataStorage.SetValue(getApplicationContext(), DataStorage.KEY_GENDER, txtGender.getText().toString());

		if (!isSetAllSuccessfully) {
			new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Error! Be careful!")
					.setMessage("One of the values could not be saved properly. Check that you don't have odd characters (these can mess up the software).")
					.setPositiveButton("Try again", null).show();
			Log.i(TAG, "Tried to set data on StaffSetup but malformed.");
			return false;
		}

		return true;
	}

	private Boolean cleanFiles() {
		// Do it
		DataSender.deleteQueuedJsonDataInternal();
		DataSender.deleteQueuedRawDataExternal();
		return true;
	}

	private class CleanFilesTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return cleanFiles();
		}

		protected void onPostExecute(Boolean isNeedUpdate) {
			Toast.makeText(getApplicationContext(), "Done removing files", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		AppUsageLogger.logClick(TAG, v);
		Intent intent;
		if (v.getId() == R.id.buttonSave) {
			Intent bintent = new Intent(MonitorServiceBroadcastReceiver.TYPE_START_SENSOR_MONITOR_SERVICE_NOW);
			sendBroadcast(bintent);
			if (isDataValid()) {
				new AlertDialog.Builder(StaffSetupActivity.this).setTitle("Data saved").setMessage("You have saved the data to the phone.")
						.setPositiveButton("Done", null).show();

				DataStorage.setIsForceReset(getApplicationContext(), true);
				AppInfo.resetPromptCompletedTimes(getApplicationContext());

				// Save data to the SD card as a backup
				try {
					DataStorage.SaveDataFile(getApplicationContext());
				} catch (WOCKETSException e) {
					Log.e(TAG, "Trouble saving the shared preferences to the SD card.");
				}
				// DataStoreDBHelper.staffUpdateServerParticipantInfo(this);
				// DataStoreDBHelper.updateServerParticipantInfo(this);
			}
		} else if (v.getId() == R.id.buttonBack) {
			finish();
		} else if (v.getId() == R.id.buttonCleanFiles) {
			Log.i(TAG, "Clean some files");
			Toast.makeText(getApplicationContext(), "Start removing files", Toast.LENGTH_LONG).show();
			new CleanFilesTask().execute();
		} else if (v.getId() == R.id.buttonShutdown) {
			Log.i(TAG, "Shutdown Wockets");
			DataStorage.setShutdown(getApplicationContext(), true);
		} else if (v.getId() == R.id.buttonSendFiles) {
			// TODO fix
			Log.i(TAG, "Send all queued files");
			String msg = "Starting staff-initiated file upload";
			ServerLogger.sendNote(getApplicationContext(), msg, true);

			// Change backup to false
			int filesRemaining = RawUploader.uploadDataFromExtUploadDir(getApplicationContext(), false, true, true, Globals.UPLOAD_SUCCESS_PERCENTAGE, true);
			msg = "Completed staff-initiated file upload. Files remaining to upload: " + filesRemaining;
			ServerLogger.sendNote(getApplicationContext(), msg, true);
		} else if (v.getId() == R.id.buttonStatusScreen) {
			Log.i(TAG, "Status screen");
			intent = new Intent(StaffSetupActivity.this, StatusScreenActivity.class);
			startActivity(intent);
		} else if (v.getId() == R.id.buttonGenerateError) {
			Log.i(TAG, "StaffSetupActivity generate error");
			String[] strArray = new String[10];
			strArray[100] = "Error";
		}
	}
}