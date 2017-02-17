package edu.neu.mhealth.android.wockets.library.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;
import edu.neu.mhealth.android.wockets.library.managers.PermissionManager;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

/**
 * @author Dharam Maniar
 */

public class WocketsActivity extends AppCompatActivity {

	private static final String TAG = "WocketsActivity";

	private Activity mActivity;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Inside onCreate", getApplicationContext());

		mActivity = this;
		mContext = getApplicationContext();

		initialize();
	}

	private void initialize() {
		Log.i(TAG, "Inside initialize", mContext);
		DatabaseManager.setPersistenceEnabledTrue(mContext);
		getPermissions();
	}

	private void getPermissions() {
		Log.i(TAG, "Inside getPermissions", mContext);
		if (!PermissionManager.areAllPermissionsAvailable(mContext)) {
			PermissionManager.requestAllPermissions(mActivity, mContext);
		} else {
			authenticateUser();
		}
	}

	private void authenticateUser() {
		if (!UserManager.isUserAuthenticated(mContext)) {
			startActivity(new Intent(mContext, LoginActivity.class));
		} else {
			Log.i(TAG, "User already logged in with email " + UserManager.getAuthenticatedUser().getEmail(), mContext);
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.i(TAG, "Inside onRequestPermissionsResult", mContext);

		boolean tryToGetPermissionsAgain = false;

		for (int i = 0; i < permissions.length; i++) {
			if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
				boolean showRationale = shouldShowRequestPermissionRationale(permissions[i]);
				if (!showRationale) {
					Log.i(TAG, "User denied flagging NEVER ASK AGAIN for permission - " + permissions[i], mContext);
				} else {
					tryToGetPermissionsAgain = true;
					break;
				}
			}
		}
		if (tryToGetPermissionsAgain) {
			getPermissions();
		} else {
			authenticateUser();
		}
	}
}