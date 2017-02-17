package mhealth.neu.edu.phire;

import com.google.android.gms.wearable.MessageEvent;

import edu.neu.android.wearwocketslib.notification.WearableNotification;
import edu.neu.android.wearwocketslib.services.WearListenerService;

/**
 * Created by Qu on 6/7/2015.
 */
public class PHIREWearListenerService extends WearListenerService {
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";
    public static final String COUNT_PATH = "/count";

    @Override
    public void onCreate() {
        Globals.initGlobals(getApplicationContext());
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if(messageEvent.getPath().equals(WearListenerService.NOTE_PATH)) {
            String message = new String(messageEvent.getData());
            if(message.equals("TEST_NOTIFICATION")) {
                WearableNotification notification = new WearableNotification("PHIRE", "test", R.drawable.ic_launcher, false, WearableNotification.SURVEY_NOTIFICATION, getApplicationContext());
                notification.show();
            }else if(message.equals("SURVEY_NOTIFICATION")){
                WearableNotification notification = new WearableNotification("PHIRE: Time for survey", "Check your phone to answer",
                        R.drawable.ic_launcher, false, WearableNotification.SURVEY_NOTIFICATION, getApplicationContext());
                notification.show();
            }else if(message.equals("END_OF_DAY_ANNOTATION_NOTIFICATION")){
                WearableNotification notification = new WearableNotification("PHIRE: Time for annotation", "Check your phone to annotate your day",
                        R.drawable.ic_launcher, true, WearableNotification.END_OF_DAY_ANNOTATION_NOTIFICATION, getApplicationContext());
                notification.show();
            }else if(message.equals("CANCEL_SURVEY_NOTIFICATION")){
                WearableNotification.cancel(WearableNotification.SURVEY_NOTIFICATION);
            }else if(message.equals("CANCEL_ANNOTATION_NOTIFICATION")){
                WearableNotification.cancel(WearableNotification.END_OF_DAY_ANNOTATION_NOTIFICATION);
            }
        }
    }
}
