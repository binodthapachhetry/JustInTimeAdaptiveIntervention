package edu.neu.android.wocketslib.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * A simple view that covers other things on the screen.
 * This class is currently used with the authorization checker. So user
 * can't see anything else except for the authorizing dialog.
 * It's used to reduce the interests of users who are unrelated to the 
 * study but happen to download the application.
 * 
 * @author bigbug
 *
 */
public class DummyView extends View {

    public DummyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
