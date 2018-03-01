package edu.neu.android.wearwocketslib.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class WearableFileTransferService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WearableFileTransferService";
    private static final String FILE_PATH = "/filefromwatch";
    private static final String FILE_KEY = "file";
    private static final String PATH_KEY = "path";
    private static final String NAME_KEY = "name";
    private static final String COUNT_KEY = "count";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String SIZE_KEY = "size";

    private static boolean isRunning = false;
    private GoogleApiClient mGoogleApiClient;
    private CheckPhoneConnectionTask checkPhoneConnectionTask;
    private Timer timer;
    private String toBeTransferedFilepath = null;
    private static int count = 0;

    private Logger logger = null;

    public static boolean isRunning(){
        return isRunning;
    }

    @Override
    public void onCreate() {
        logger = new Logger(TAG);
        logger.i("Inside onCreate", getApplicationContext());
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("Inside onStartCommand", getApplicationContext());

        setStopTimer();

        if(intent == null){
            logger.i("Null intent, quit", getApplicationContext());
            notifyTransferFinish("null", "Null intent when starting transfer service");
            stopSelf();
        }else {
            String action = intent.getAction();
            if (action == null) {
                logger.i("Null action, quit", getApplicationContext());
                notifyTransferFinish("null", "Null action when starting transfer service");
                stopSelf();
            }else {
                switch (action) {
                    case "TRANSFER_FILE":
                        toBeTransferedFilepath = intent.getStringExtra("FILE_PATH");
                        logger.i("Start to transfer file: " + toBeTransferedFilepath, getApplicationContext());
                        mGoogleApiClient.connect();
                        break;
                    case "TIMEOUT":
                        logger.e("Has reached 45s time out, kill the service", getApplicationContext());
                        notifyTransferFailure(toBeTransferedFilepath, "Has reached 45s time out, kill the service");
                        stopSelf();
                        break;
                    default:
                        logger.i("Unknown action: " + action, getApplicationContext());
                        notifyTransferFinish("null", "Unknown start action for transfer service: " + action);
                        stopSelf();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // normally it won't be called
        logger.i("Inside onBind", getApplicationContext());
        return null;
    }

    @Override
    public void onDestroy() {
        logger.i("Inside onDestroy", getApplicationContext());
        super.onDestroy();
        if(mGoogleApiClient.isConnected()) {
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
        // after google api is in connection, check phone connection
        checkPhoneConnection(Globals.CAPABILITY_NAME);
//        checkPhoneConnectionTask = new CheckPhoneConnectionTask(mGoogleApiClient);
//        checkPhoneConnectionTask.check(Globals.CAPABILITY_NAME, new CheckPhoneConnectionTask.OnPhoneConnectionCallBack() {
//            @Override
//            public void onPhoneInConnection(Set<Node> nodes) {
//                logger.i("Phone is in connection!", getApplicationContext());
//                runTransfer();
//            }
//
//            @Override
//            public void onPhoneNotInConnection() {
//                notifyTransferFailure(toBeTransferedFilepath, "Phone is not in connection");
//                stopSelf();
//            }
//        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            notifyTransferFailure(toBeTransferedFilepath, "Google Api Connection lost.  Cause: Network Lost");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            notifyTransferFailure(toBeTransferedFilepath, "Google Api Connection lost.  Reason: Service Disconnected");
        } else {
            notifyTransferFailure(toBeTransferedFilepath, "Google Api Connection lost. For unknown reason");
        }
        stopSelf();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        notifyTransferFailure(toBeTransferedFilepath, "Google Api Connection failed. Cause: " + connectionResult.toString());
        stopSelf();
    }

    private void notifyTransferFailure(String filepath, String message){
        Intent intent = new Intent("FILE_TRANSFER_RESULT");
        // You can also include some extra data.
        intent.putExtra("RESULT", "FAILURE");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("FILE_PATH", filepath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logger.close();
    }

    private void notifyTransferSuccess(String filepath, String message){
        Intent intent = new Intent("FILE_TRANSFER_RESULT");
        // You can also include some extra data.
        intent.putExtra("RESULT", "SUCCESS");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("FILE_PATH", filepath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logger.close();
    }

    private void notifyTransferFinish(String filepath, String message){
        Intent intent = new Intent("FILE_TRANSFER_RESULT");
        // You can also include some extra data.
        intent.putExtra("RESULT", "FINISH");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("FILE_PATH", filepath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        logger.close();
    }

    private void checkPhoneConnection(final String capabilityName) {
        Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(

                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            onPhoneNotInConnection();
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
                            onPhoneInConnection();
                        } else {
                            onPhoneNotInConnection();
                        }
                    }
                });
    }

    private void onPhoneInConnection() {
        logger.i("Phone is in connection!", getApplicationContext());
        SharedPrefs.setLong(Globals.LAST_PHONE_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());
        if (WearableNotification.isShowing(WearableNotification.LOST_CONNECTION_NOTIFICATION)) {
            WearableNotification.cancel(WearableNotification.LOST_CONNECTION_NOTIFICATION);
            logger.i("Cancel lost connection notification", getApplicationContext());
        }
        runTransfer();
    }

    private void onPhoneNotInConnection() {
        logger.i("Phone is not in connection!", getApplicationContext());
        notifyTransferFailure(toBeTransferedFilepath, "Phone is not in connection");
        stopSelf();
    }


    private void setStopTimer() {
        timer = new Timer("STOP_FILE_TRANSFER_SERVICE", true);
        TimerTask stopTask = new TimerTask() {
            @Override
            public void run() {
                Intent stopIntent = new Intent(getApplicationContext(), WearableFileTransferService.class);
                stopIntent.setAction("TIMEOUT");
                startService(stopIntent);
                logger.i("Stop file transfer service at 45 seconds with timer", getApplicationContext());
            }
        };
        timer.schedule(stopTask, 45 * 1000);
        logger.i("File transfer service will stop in 45 seconds", getApplicationContext());
    }

    private void runTransfer(){
        if(toBeTransferedFilepath == null){
            logger.e("ERROR: fail to retrieve transfer zip file", getApplicationContext());
            notifyTransferFinish("null", "ERROR: fail to retrieve transfer zip file");
            stopSelf();
        }
        File selectedZip = new File(toBeTransferedFilepath);
        Asset zipAsset = fileToAsset(selectedZip);
        if(zipAsset != null) {
            putZipAssetIntoDataApi(zipAsset, selectedZip.getParent(), selectedZip.getName());
        }else {
            logger.e("Asset is null due to some error, no need to transfer", getApplicationContext());
        }
        return;
    }

    private void putZipAssetIntoDataApi(Asset asset, final String path, final String filename) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(FILE_PATH);
        putDataMapRequest.getDataMap().putAsset(FILE_KEY, asset);
        putDataMapRequest.getDataMap().putLong(TIMESTAMP_KEY, System.currentTimeMillis());
        putDataMapRequest.getDataMap().putString(PATH_KEY, path);
        putDataMapRequest.getDataMap().putString(NAME_KEY, filename);
        putDataMapRequest.getDataMap().putInt(COUNT_KEY, ++count);
        putDataMapRequest.getDataMap().putLong(SIZE_KEY, asset.getData().length);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        logger.i("Sending DataItem: " + request, getApplicationContext());
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(TAG, "ERROR: failed to putDataItem, status code: "
                                    + dataItemResult.getStatus().getStatusCode(), getApplicationContext());
                            notifyTransferFailure(toBeTransferedFilepath, "ERROR: failed to putDataItem, status code: " +
                                    +dataItemResult.getStatus().getStatusCode());
                            isRunning = false;
                            WearableFileTransferService.this.stopSelf();
                        } else {
                            logger.i("File got sent to DataApi successfully!", getApplicationContext());
                            notifyTransferSuccess(toBeTransferedFilepath, "Zip file got sent to DataApi: " + toBeTransferedFilepath);
                            isRunning = false;
                            WearableFileTransferService.this.stopSelf();
                        }
                    }
                });
    }

    private Asset fileToAsset(File file) {
        Asset asset = null;
        try {
            byte[] array = FileUtils.readFileToByteArray(file);
            asset = Asset.createFromBytes(array);
            logger.i("Has created asset from file", getApplicationContext());
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), getApplicationContext());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), getApplicationContext());
            e.printStackTrace();
        } finally {
        }
        return asset;
    }
}
