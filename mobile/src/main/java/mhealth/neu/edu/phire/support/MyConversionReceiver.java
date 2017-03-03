package mhealth.neu.edu.phire.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyConversionReceiver extends BroadcastReceiver {

    public static final String TAG = MyConversionReceiver.class.getSimpleName();
    public static final int REQUEST_CODE = 54321;
    public static final String ACTION = "mhealth.neu.edu.phire.panobike.myconversionreceiver";

    public MyConversionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MyCSVconversionService.class);
        i.putExtra("foot", "bart");
        Log.i(TAG,"MyCSVconversionService starting!");
        context.startService(i);
    }
}

