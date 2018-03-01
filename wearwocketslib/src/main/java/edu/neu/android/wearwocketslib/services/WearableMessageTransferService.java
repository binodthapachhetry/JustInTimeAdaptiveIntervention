package edu.neu.android.wearwocketslib.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.notification.WearableNotification;
import edu.neu.android.wearwocketslib.tasks.CheckPhoneConnectionTask;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by Dharam on 5/4/2015.
 */
public class WearableMessageTransferService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WearableMessageTransferService";
    private static final String NOTE_PATH = "/notefromwatch";

    private static boolean isRunning = false;
    private GoogleApiClient mGoogleApiClient;
    private String message;
    private Timer timer;
    private CheckPhoneConnectionTask checkPhoneConnectionTask;

    private Logger logger = null;


    @Override
    public void onCreate() {
        logger = new Logger(TAG);
//        Log.i(TAG, "Inside onCreate", getApplicationContext());
        super.onCreate();
        message = null;
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("Inside onStartCommand", getApplicationContext());
//        Log.i(TAG,"Inside onStartCommand", getApplicationContext());

        setStopTimer();

        if(intent != null){
            String action = intent.getAction();
            if(action == null){
                logger.i("Intent action is null", getApplicationContext());
//                Log.e(TAG,"Intent action is null", getApplicationContext());
                notifyTransferFinish("null", "Intent action is null");
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }else{
                if(action.equals("TRANSFER_MESSAGE")){
                    message = intent.getStringExtra("MESSAGE");
                }else if(action.equals("TIMEOUT")) {
                    logger.e("Has reached 45s time out, kill the service", getApplicationContext());
//                    Log.e(TAG,"Has reached 45s time out, kill the service", getApplicationContext());
                    notifyTransferFailure("null", "Has reached 45s time out, kill the service");
                    stopSelf();
                    return super.onStartCommand(intent, flags, startId);
                }
                else{
                    logger.i("Unknown action: " + action, getApplicationContext());
//                    Log.e(TAG,"Unknown action: " + action, getApplicationContext());
                    notifyTransferFinish("null", "Unknown action: " + action);
                    stopSelf();
                    return super.onStartCommand(intent, flags, startId);
                }
            }
        }else {
            logger.i("Intent is null", getApplicationContext());
//            Log.i(TAG,"Intent is null", getApplicationContext());
            notifyTransferFinish("null", "Intent is null");
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        logger.i("Inside onDestroy", getApplicationContext());
//        Log.i(TAG,"Inside onDestroy", getApplicationContext());
        super.onDestroy();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        isRunning = false;
        timer.cancel();
        timer = null;
        logger.close();
    }

    @Override
    public void onConnected(Bundle bundle) {
        logger.i("Google Api Connected", getApplicationContext());
//        Log.i(TAG,"Google Api Connected", getApplicationContext());
        // after google api is in connection, check phone connection
        checkPhoneConnection(Globals.CAPABILITY_NAME);
//        checkPhoneConnectionTask = new CheckPhoneConnectionTask(mGoogleApiClient);
//        checkPhoneConnectionTask.check(Globals.CAPABILITY_NAME, new CheckPhoneConnectionTask.OnPhoneConnectionCallBack() {
//            @Override
//            public void onPhoneInConnection(Set<Node> nodes) {
//                logger.i("Phone is in connection!", getApplicationContext());
////                Log.i(TAG,"Phone is in connection!", getApplicationContext());
//                String phoneNode = pickBestNodeId(nodes);
//                sendMessage(phoneNode);
////                SharedPrefs.setLong(Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());
////
////                long lastConnection = SharedPrefs.getLong(edu.neu.android.wearwocketslib.Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());
////
////                Date date = new Date(lastConnection);
////                SimpleDateFormat df2 = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");
////                String dateText = df2.format(date);
////                logger.i("Last phone connected time was: " + dateText, getApplicationContext());
//
////                if (WearableNotification.isShowing(WearableNotification.LOST_CONNECTION_NOTIFICATION)) {
////                    WearableNotification.cancel(WearableNotification.LOST_CONNECTION_NOTIFICATION);
////                    logger.i("Cancel lost connection notification", getApplicationContext());
//////                    Log.i(TAG,"Cancel lost connection notification", getApplicationContext());
////                }
//            }
//
//            @Override
//            public void onPhoneNotInConnection() {
//                logger.i("Phone is not in connection!", getApplicationContext());
//                WearableMessageTransferService.this.stopSelf();
////                Log.i(TAG,"Phone is not in connection", getApplicationContext());
//            }
//        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            notifyTransferFailure(message, "Google Api Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            notifyTransferFailure(message, "Google Api Connection lost.  Reason: Service Disconnected");
        } else {
            notifyTransferFailure(message, "Google Api Connection lost. For unknown reason");
        }
        stopSelf();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        notifyTransferFailure(message, "Google Api Connection failed. Cause: " + connectionResult.toString());
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // normally it won't be called
        logger.i("Inside onBind", getApplicationContext());
//        Log.i(TAG,"Inside onBind", getApplicationContext());
        return null;
    }


    private void notifyTransferFailure(String note, String message){
        Intent intent = new Intent("MESSAGE_TRANSFER_RESULT");
        // You can also include some extra data.
        intent.putExtra("RESULT", "FAILURE");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("NOTE", note);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logger.close();
    }

    private void notifyTransferSuccess(String note, String message){
        Intent intent = new Intent("MESSAGE_TRANSFER_RESULT");
        // You can also include some extra data.
        intent.putExtra("RESULT", "SUCCESS");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("NOTE", note);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logger.close();
    }

    private void notifyTransferFinish(String note, String message){
        Intent intent = new Intent("MESSAGE_TRANSFER_RESULT");
        // You can also include some extra data.
        intent.putExtra("RESULT", "FINISH");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("NOTE", note);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logger.close();
    }

    private void setStopTimer() {
        final Service parentService = this;
        timer = new Timer("STOP_MESSAGE_TRANSFER_SERVICE", true);
        TimerTask stopTask = new TimerTask() {
            @Override
            public void run() {
                Intent stopIntent = new Intent(getApplicationContext(), WearableMessageTransferService.class);
                stopIntent.setAction("TIMEOUT");
                startService(stopIntent);
                logger.i("Stop message transfer service at 45 seconds with timer", getApplicationContext());
            }
        };
        timer.schedule(stopTask, 45 * 1000);
//        logger.i("Message transfer service will stop in 45 seconds", getApplicationContext());
//        Log.i(TAG,"Message transfer service will stop in 45 seconds", getApplicationContext());
        logger.i("Message transfer service will stop in 45 seconds", getApplicationContext());
    }


    private void checkPhoneConnection(final String capabilityName) {
        Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(

                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get capabilities", getApplicationContext());
                            logger.i("Phone is not in connection!", WearableMessageTransferService.this.getApplicationContext());
                            WearableMessageTransferService.this.stopSelf();
                            return;
                        }
                        Map<String, CapabilityInfo>
                                capabilitiesMap = getAllCapabilitiesResult.getAllCapabilities();
                        Set<Node> nodes = new HashSet<>();

                        CapabilityInfo capabilityInfo = capabilitiesMap.get(capabilityName);
                        if (capabilityInfo != null) {
                            nodes.addAll(capabilityInfo.getNodes());
                        }
                        if (nodes.size() > 0) {
                            onPhoneInConnection(nodes);
                        } else {
                            logger.i("Phone is not in connection!", WearableMessageTransferService.this.getApplicationContext());
                            onPhoneNotInConnection();
                        }
                    }
                });
    }

    private void onPhoneInConnection(Set<Node> nodes){

    }

    private void onPhoneNotInConnection(){
//        logger.i("Phone is not in connection!", getApplicationContext());
        stopSelf();
    }

    private void sendMessage(String phoneNode) {
        if(message == null){
            Log.e(TAG, "Message is empty", getApplicationContext());
            return;
        }
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, phoneNode, NOTE_PATH, message.getBytes()).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(
                            MessageApi.SendMessageResult sendMessageResult) {
                        logger.i("Message: " + message, getApplicationContext());
//                        Log.i(TAG,"Message: " + message, getApplicationContext());
                        if(!sendMessageResult.getStatus().isSuccess()){
//                            Log.e(TAG, "ERROR: failed to transfer message, status code: " + sendMessageResult.getStatus().getStatusCode(), getApplicationContext());
                            notifyTransferFailure(message, "ERROR: failed to transfer message, status code: " +
                                    +sendMessageResult.getStatus().getStatusCode());
                        }else{
                            logger.i("Message got sent to MessageApi successfully!", getApplicationContext());
//                            Log.i(TAG,"Message got sent to MessageApi successfully!", getApplicationContext());
                            notifyTransferSuccess(message, "Message got sent to MessageApi");
                        }
                        WearableMessageTransferService.this.stopSelf();
                    }
                }
        );
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }
}
