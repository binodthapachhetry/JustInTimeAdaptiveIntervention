package edu.neu.mhealth.android.wockets.library.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class UserManager {

	private static final String TAG = "UserManager";

	/**
	 * Get the current authenticated user.
	 *
	 * @return {@link FirebaseUser}
	 */
	public static FirebaseUser getAuthenticatedUser() {
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		return mAuth.getCurrentUser();
	}

	public static String getUserEmailFormatted() {
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
			return mAuth.getCurrentUser().getEmail();
		} else {
			return "unknown_user";
		}
	}

	public static String getUserEmail() {
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
			return mAuth.getCurrentUser().getEmail().replace('.','_');
		} else {
			return "unknown_user";
		}
	}

	/**
	 * Check if the user is authenticated.
	 *
	 * @return boolean
	 */
    public static boolean isUserAuthenticated(Context mContext) {
	    FirebaseAuth mAuth = FirebaseAuth.getInstance();
	    FirebaseUser user = mAuth.getCurrentUser();
		@SuppressLint("HardwareIds")
		String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

		if (user == null) {
			return false;
		}

		user.reload();

		if (user.getDisplayName() == null) {
			return false;
		}

		if (!user.getDisplayName().equals(android_id)) {
			mAuth.signOut();
			Log.w(TAG, "User signed in on some other device. Signing out from here.", mContext);
			return false;
		}

	    return true;
    }
}
