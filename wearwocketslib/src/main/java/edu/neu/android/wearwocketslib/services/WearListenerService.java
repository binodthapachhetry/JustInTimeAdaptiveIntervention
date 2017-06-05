package edu.neu.android.wearwocketslib.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import edu.neu.android.wearwocketslib.Globals;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.AlwaysOnService;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulBroadcastAlarm;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.notification.LostConnectionWearableNotification;
import edu.neu.android.wearwocketslib.notification.WearableNotification;
import edu.neu.android.wearwocketslib.utils.log.Logger;
import edu.neu.android.wearwocketslib.utils.system.DateHelper;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class WearListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "WearListenerService";

    private static final String FILE_PATH = "/filefromphone";
    private static final String FILE_KEY = "file";
    protected static final String NOTE_PATH = "/notefromphone";

    private static boolean isConnected = false;
    private Context mContext;

    private GoogleApiClient mGoogleApiClient = null;

    private Logger logger = null;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        logger = new Logger(TAG);
        logger.i("Inside onCreate", getApplicationContext());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        if(mGoogleApiClient != null)
            mGoogleApiClient.connect();
        new checkConnectionTask().execute();
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        logger.i("Inside onLowMemory", getApplicationContext());
        super.onLowMemory();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("Inside onStart", getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        logger.i("Inside onMessageReceived: " + messageEvent.getPath(), getApplicationContext());
        logger.i("Phone is in connection(from onMessageReceived)!",getApplicationContext());
        SharedPrefs.setLong(Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());

        if (WearableNotification.isShowing(WearableNotification.LOST_CONNECTION_NOTIFICATION)) {
            WearableNotification.cancel(WearableNotification.LOST_CONNECTION_NOTIFICATION);
            logger.i("Cancel lost connection notification", getApplicationContext());
        }


        if(messageEvent.getPath().equals(NOTE_PATH)) {
            String message = new String(messageEvent.getData());
            logger.i("Message Received - " + message, getApplicationContext());
            if (message.contains("trigger")) {
                logger.i("Received message to start minute service", getApplicationContext());
//                logger.i("Phone is in connection(from onMessageReceived)!",getApplicationContext());
//                SharedPrefs.setLong(Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());

                logger.i("starting always on service using intent",mContext);
                Intent alwaysOnServiceIntent = new Intent(mContext,AlwaysOnService.class);
                mContext.startService(alwaysOnServiceIntent);
//                startRepeatedWakefulService();
//                setAlarm(getApplicationContext());
            }else if(message.startsWith("TRANSFER_SUCCESS")){
                String[] tokens = message.split(":");
                File toBeDeleted = new File(tokens[1]);

                String subString = "ComputedFeature";
                String name = toBeDeleted.getName();
                if(!name.toLowerCase().contains(subString.toLowerCase())) {

                    if (toBeDeleted.delete()) {
                        Log.i(WearableWakefulService.TAG, "Successfully deleted file upon successful transfer: " + toBeDeleted.getAbsolutePath(), getApplicationContext());
                    } else {
                        Log.e(WearableWakefulService.TAG, "Fail to delete original zip file upon successful transfer: " + toBeDeleted.getAbsolutePath(), getApplicationContext());
                    }
                }
            }else if(message.startsWith("TRANSFER_FAILURE")){
                //add to queue to transfer again
                String[] tokens = message.split(":");
                Log.i(WearableWakefulService.TAG, "Fail to transfer file: " + tokens[1], getApplicationContext());
            }else if(message.startsWith("ENABLE_TRANSFER")){
                SharedPrefs.setBoolean(WearableWakefulService.KEY_ENABLE_TRANSFER, true, getApplicationContext());
            }else if(message.startsWith("DISABLE_TRANSFER")){
                SharedPrefs.setBoolean(WearableWakefulService.KEY_ENABLE_TRANSFER, false, getApplicationContext());
            }
            else{
                logger.i("Received message: " + message, getApplicationContext());
            }
        }
    }

    private void deleteSensorDataFile(String fullPath){
        File toBeDeleted = new File(fullPath);
        if(!DateHelper.isHourBefore(mHealthFormat.extractDateFromFilename(toBeDeleted.getName()))){
            logger.i("The file is still in collection, so skip deleting: " + fullPath, getApplicationContext());
            return;
        }
        if(toBeDeleted.delete()){
            logger.i("Delete file: " + fullPath + " after successful transfer", getApplicationContext());
        }else{
            if(toBeDeleted.exists()){
                logger.i("Delete file: " + fullPath + " failed", getApplicationContext());
            }else{
                logger.i("File does not exist: " + fullPath + "; abort deleting", getApplicationContext());
            }
        }
    }

    public static boolean isConnected(){
        return isConnected;
    }

    public static void setConnection(boolean isConnected){
        WearListenerService.isConnected = isConnected;
    }

    @Override
    public void onPeerConnected(Node node) {
        logger.i("Inside onPeerConnected", getApplicationContext());
        logger.i("Node Connected - " + node.getDisplayName(), getApplicationContext());
        isConnected = true;
        if (WearableNotification.isShowing(WearableNotification.LOST_CONNECTION_NOTIFICATION)) {
            WearableNotification.cancel(WearableNotification.LOST_CONNECTION_NOTIFICATION);
            logger.i("Cancel lost connection notification", getApplicationContext());
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        logger.i("Inside onPeerDisconnected", getApplicationContext());
        logger.i("Node Disconnected - " + node.getDisplayName(), getApplicationContext());
        isConnected = false;
//        LostConnectionWearableNotification notification = new LostConnectionWearableNotification("PHIRE: Connection lost", "Please check phone/watch connection", R.drawable.ic_launcher, true, WearableNotification.LOST_CONNECTION_NOTIFICATION, mContext);
//        notification.show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        logger.i("Inside onDataChanged", getApplicationContext());
//        logger.i("Phone is in connection(from onDataChanged)!",getApplicationContext());
//        SharedPrefs.setLong(Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        dataEventBuffer.close();
        for (DataEvent event : events) {
            if(event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if(FILE_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset file = dataMapItem.getDataMap().getAsset(FILE_KEY);
                    String filePath = dataMapItem.getDataMap().getString("path");
                    String fileName = dataMapItem.getDataMap().getString("name");
                    logger.i("File Received - " + filePath + "/" + fileName, getApplicationContext());
                    assetToFile(file, filePath, fileName);
                }
            }
        }
    }

    private void assetToFile(Asset asset, String path, String fileName) {
        logger.i("Inside loadFileFromAsset", getApplicationContext());
        File filePath = new File(path);
        File file = new File(path + "/" + fileName);
        filePath.mkdirs();
        try {
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            byte[] b = IOUtils.toByteArray(assetInputStream);
            FileUtils.writeByteArrayToFile(file, b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.i("File write successfully - " + filePath + "/" + fileName, getApplicationContext());
    }

    @Override
    public void onDestroy() {
        logger.i("Inside onDestroy", getApplicationContext());
        logger.close();
        super.onDestroy();
    }

    public static void setAlarm(Context mContext) {
        WearableWakefulBroadcastAlarm alarm = new WearableWakefulBroadcastAlarm(mContext, "PHONE");
        alarm.setAlarm();
    }

    @Override
    public void onConnected(Bundle bundle) {
        logger.i("Google Api Connected", getApplicationContext());
        if(Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes().size() > 0){
            isConnected = true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        logger.i("Google Api Suspended", getApplicationContext());

    }
    private class checkConnectionTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params){
            Collection<String> nodes = getNodes();
            if(nodes.size() > 0 && !WearListenerService.isConnected()){
                WearListenerService.setConnection(true);
//                logger.i("Phone is in connection(from checkConnectionTask)!",getApplicationContext());
//                SharedPrefs.setLong(Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());
            }else if(nodes.size() == 0 && WearListenerService.isConnected()){
                WearListenerService.setConnection(false);
                logger.i("Phone is not in connection!", getApplicationContext());
            }
            return null;
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    private void startRepeatedWakefulService(){
        Intent wakefulService = new Intent(mContext, WearableWakefulService.class);
        if(!WearableWakefulService.isRunning()) {
            logger.i("Starting service @ " + new Date().toString(), mContext);
            startWakefulService(mContext, wakefulService);
        }else{
            logger.i("Wakeful service is running, no need to start", mContext);
        }

    }
}
