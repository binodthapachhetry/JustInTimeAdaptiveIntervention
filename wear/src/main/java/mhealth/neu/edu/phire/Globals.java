package mhealth.neu.edu.phire;

import android.content.Context;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;

/**
 * Created by jarvis on 02/02/2017.
 */
public class Globals {

    public static void initGlobals(Context context) {
        edu.neu.android.wearwocketslib.Globals.STUDY_NAME = "TEMPLE";
        edu.neu.android.wearwocketslib.Globals.CAPABILITY_NAME = "phire";
        WearableWakefulService.setArbitrator(new PHIREArbitrater(context));

        edu.neu.android.wearwocketslib.Globals.IS_ACCELEROMETER_LOGGING_ENABLED = true;
        edu.neu.android.wearwocketslib.Globals.IS_GYROSCOPE_LOGGING_ENABLED = false;
        edu.neu.android.wearwocketslib.Globals.IS_MAGNETIC_FIELD_LOGGING_ENABLED = false;
        edu.neu.android.wearwocketslib.Globals.IS_BATTERY_LOGGING_ENABLED = true;
        edu.neu.android.wearwocketslib.Globals.IS_STORAGE_LOGGING_ENABLED = true;
        edu.neu.android.wearwocketslib.Globals.IS_MHEALTH_EXTERNAL = true;
        edu.neu.android.wearwocketslib.Globals.init();
    }
}
