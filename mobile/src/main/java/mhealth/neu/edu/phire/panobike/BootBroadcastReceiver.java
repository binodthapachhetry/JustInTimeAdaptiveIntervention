package mhealth.neu.edu.phire.panobike;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by jarvis on 7/14/16.
 */
public class BootBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
        Intent startServiceIntent = new Intent(context, MyTestService.class);
        startWakefulService(context, startServiceIntent);

    }
}
