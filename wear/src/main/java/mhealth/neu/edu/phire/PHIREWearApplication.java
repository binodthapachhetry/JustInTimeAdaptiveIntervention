package mhealth.neu.edu.phire;

import edu.neu.android.wearwocketslib.core.context.WearableApplication;

/**
 * Created by qutang on 8/7/15.
 */
public class PHIREWearApplication extends WearableApplication {
    @Override
    public void onCreate() {
        Globals.initGlobals(getApplicationContext());

        super.onCreate();
    }
}
