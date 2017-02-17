package edu.neu.mhealth.android.wockets.library.services;

import android.app.IntentService;
import android.content.Intent;

/**
 * @author Dharam Maniar
 */

/**
 * Base Service for all services related to Wockets.
 *
 */
public class WocketsIntentService extends IntentService {

    private final static String TAG = "WocketsIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WocketsIntentService(String name) {
        super(name);
    }

    public WocketsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
