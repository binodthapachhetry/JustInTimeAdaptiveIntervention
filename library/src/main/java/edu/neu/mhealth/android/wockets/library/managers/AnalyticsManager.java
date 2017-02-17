package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import edu.neu.mhealth.android.wockets.library.user.UserManager;

/**
 * @author Dharam Maniar
 */
public class AnalyticsManager {

    private static final String TAG = "AnalyticsManager";

    public static void logEvent(Context context, String event, Bundle bundle) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setUserId(UserManager.getAuthenticatedUser().getUid());
        firebaseAnalytics.setUserProperty("userId", UserManager.getUserEmail());
        firebaseAnalytics.logEvent(event, bundle);
    }
}
