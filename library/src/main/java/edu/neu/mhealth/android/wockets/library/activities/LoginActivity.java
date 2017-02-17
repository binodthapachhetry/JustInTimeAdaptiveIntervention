package edu.neu.mhealth.android.wockets.library.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.R;
import edu.neu.mhealth.android.wockets.library.R2;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class LoginActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

	private static final String TAG = "LoginActivity";
	private FirebaseAuth mAuth;

    private Context mContext;
    private Activity mActivity;

	@BindView(R2.id.wockets_activity_login_email)
	EditText email;

	@BindView(R2.id.wockets_activity_login_password)
	EditText password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Inside onCreate", getApplicationContext());

		setContentView(R.layout.wockets_activity_login);
		ButterKnife.bind(this);
        mContext = getApplicationContext();
        mActivity = this;

		mAuth = FirebaseAuth.getInstance();

		isGooglePlayServicesAvailable(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		mAuth.removeAuthStateListener(this);
	}

	/**
	 * Disable back press on login screen
	 */
	@Override
	public void onBackPressed() {
	}

	@OnClick(R2.id.wockets_activity_login_login)
	public void onClickLogin(View v) {
		mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						if (!task.isSuccessful()) {
							ToastManager.showLongToast(getApplicationContext(), "Login Unsuccessful, Please Try Again !!!");
							Log.w(TAG, "Login Unsuccessful - Email - " + email.getText().toString() + " - Password - " + password.getText().toString(), getApplicationContext());
						}
					}
				});
	}

	@Override
	public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
		final FirebaseUser user = firebaseAuth.getCurrentUser();

        @SuppressLint("HardwareIds")
		final String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (user == null) {
            Log.d(TAG, "User signed out");
            return;
        }

        if (user.getDisplayName() != null && !user.getDisplayName().equals(android_id)) {
            final AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("User Login")
                    .setMessage("This email has already been used on some another phone. Do you want to swap?")
                    .setPositiveButton("Swap it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
							updateProfile(user, android_id);
                            dialog.dismiss();
                        }
                    })
					.setNegativeButton("Use another id", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
                            email.setText("");
                            password.setText("");
							firebaseAuth.signOut();
							dialog.dismiss();
						}
					})
                    .create();
            alertDialog.show();
			return;
        }

		updateProfile(user, android_id);

	}

	private void updateProfile(FirebaseUser user, String android_id) {
		UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
				.setDisplayName(android_id)
				.build();
		user.updateProfile(profileChangeRequest)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						// User is signed in
						Log.d(TAG, "User signed in");
						mActivity.finish();
					}
				});
	}

	public boolean isGooglePlayServicesAvailable(Activity activity) {
		GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
		int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
		if(status != ConnectionResult.SUCCESS) {
			if(googleApiAvailability.isUserResolvableError(status)) {
				googleApiAvailability.getErrorDialog(activity, status, 2404).show();
			}
			return false;
		}
		return true;
	}
}
