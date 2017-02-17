package edu.neu.android.wocketslib.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by Intille on 5/7/14.
 */
public class Toaster {

    public static void showToastMessage(Context aContext, String aMsg) {
        Toast toast = Toast.makeText(aContext, aMsg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
