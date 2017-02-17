package edu.neu.android.wocketslib.wear;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import edu.neu.android.wocketslib.utils.Log;

/**
 * Created by jarvis on 2/4/17.
 */

public class WearNoteSender {

    public static final String TAG = "WearNoteSender";

    public static void sendNote(String note, Context context) {

        Intent noteSender = new Intent(context, DataTransfer.class);
        Bundle extras = new Bundle();
        extras.putString("type", "note");
        extras.putString("value", note);
        noteSender.putExtras(extras);
        context.startService(noteSender);
        Log.i(TAG, "Note is sent: " + note);
    }
}
