package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.os.Vibrator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;

import edu.neu.mhealth.android.wockets.library.events.VibrationStopEvent;
import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */
public class VibrationManager {

    private static final String TAG = "VibrationManager";

    public static final long[] VIBRATION_PATTERN_INTENSE = { 1000, 1000, 1000, 1000, 1000, 1000, 10, 600, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50, 300, 50};
    public static final long[] VIBRATION_PATTERN_BASIC = { 10, 600, 50, 300, 50};

    private static Vibrator vibrator = null;
    private static Context context = null;

    public VibrationManager(Context mContext) {
        Log.i(TAG, "VibrationManager initialized", mContext);
        EventBus.getDefault().register(this);
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        context = mContext;
    }

    public void vibrate(long[] vibrationPattern) {
        Log.i(TAG, "Vibrate called with pattern - " + Arrays.toString(vibrationPattern), context);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationPattern, -1);
        }
    }

    public void vibrate(long milliseconds) {
        Log.i(TAG, "Vibrate called for " + milliseconds + " milliseconds", context);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds);
        }
    }

    // This method will be called when a VibrationStopEvent is posted
    @Subscribe
    public void cancel(VibrationStopEvent event) {
        Log.i(TAG, "Received VibrationStopEvent, cancelling vibration", context);
        if (vibrator != null) {
            vibrator.cancel();
        }
        EventBus.getDefault().unregister(this);
    }
}
