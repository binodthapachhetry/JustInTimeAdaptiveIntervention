package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author Dharam Maniar
 */

public class ToastManager {

    public static void showShortToast(Context mContext, String message) {
	    Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
	    toast.setGravity(Gravity.CENTER, 0, 0);
	    toast.show();
    }

    public static void showLongToast(Context mContext, String message) {
        Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
	    toast.setGravity(Gravity.CENTER, 0, 0);
	    toast.show();
    }
}
