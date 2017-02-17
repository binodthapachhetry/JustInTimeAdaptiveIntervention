package edu.neu.android.wocketslib.wear;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.jcraft.jsch.HASH;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneNotifier;
import edu.neu.android.wocketslib.utils.SharedPrefs;

/**
 * Created by Dharam on 5/4/2015.
 */
public class DataTransfer extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener {

    private static final String TAG = "DataTransfer";
    private static final String FILE_PATH = "/filefromphone";
    private static final String FILE_KEY = "file";
    private static final String NOTE_PATH = "/notefromphone";
    private GoogleApiClient mGoogleApiClient;


    private String type;
    private String value;
    private String watchNode = null;

    @Override
    public void onCreate() {
        Log.i(TAG, "Inside onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Inside onStart");
        if(intent != null){
            Bundle extras = intent.getExtras();
            type = extras.getString("type");
            value = extras.getString("value");
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
        Log.i(TAG, "Inside onDestroy");
        super.onDestroy();
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Inside onConnected");
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        checkConnection(Globals.CAPABILITY_NAME);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Inside onConnectionSuspended");
        this.stopSelf();
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.i(TAG, "Inside onPeerConnected");
        Log.i(TAG, "Node Connected - " + node.getDisplayName());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.i(TAG, "Inside onPeerDisconnected");
        Log.i(TAG, "Node Disconnected - " + node.getDisplayName());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Inside onConnectionFailed");
        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Inside onBind");
        return null;
    }

    private void checkConnection(final String capabilityName) {
        Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(

                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            android.util.Log.e(TAG, "Failed to get capabilities");
                            Log.i(TAG, "Watch is not in connection!");
                            DataTransfer.this.stopSelf();
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
                            Log.i(TAG, "Watch is in connection!");
                            watchNode = pickBestNodeId(nodes);
                            SharedPrefs.setLong(Globals.LAST_WATCH_IN_CONNECTION_TIME, System.currentTimeMillis(), getApplicationContext());
                            if(PhoneNotifier.isShowing(PhoneNotifier.LOST_CONNECTION_NOTIFICAITON)) {
                                PhoneNotifier.cancel(PhoneNotifier.LOST_CONNECTION_NOTIFICAITON);
                                Log.i(TAG, "Cancel lost connection notification");
                            }

                            if (type.equals("note")) {
                                sendNote(value);
                            }

                            if (type.equals("file")) {
                                sendFile(new File(value));
                            }
                        } else {
                            Log.i(TAG, "Watch is not in connection!");
                            DataTransfer.this.stopSelf();
                        }
                    }
                });
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


    private void sendFile(File file) {
        if (file.exists() && mGoogleApiClient.isConnected()) {
            sendAsset(fileToAsset(file), file.getParentFile().getAbsolutePath(), file.getName());
        }
    }

    private void sendAsset(Asset asset, final String path, String name) {
        if (asset == null) {
            System.out.println("Asset is null");
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create(FILE_PATH);
        dataMap.getDataMap().putAsset(FILE_KEY, asset);
        dataMap.getDataMap().putLong("time", new Date().getTime());
        dataMap.getDataMap().putString("path", path);
        dataMap.getDataMap().putString("name", name);
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.i(TAG, "Sending file was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                    }
                });
        this.stopSelf();
    }

    private static Asset fileToAsset(File file) {
        Asset asset = null;
        try {
            byte[] array = FileUtils.readFileToByteArray(file);
            asset = Asset.createFromBytes(array);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return asset;
    }

    private void sendNote(final String note) {
//        new StartWearableActivityTask().execute(note);
        if(watchNode != null) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, watchNode, NOTE_PATH, note.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(
                                MessageApi.SendMessageResult sendMessageResult) {
                            Log.i(TAG, "Message: " + note);
                            Log.i(TAG, "Sending message was successful: " + sendMessageResult.getStatus()
                                    .isSuccess());
                        }

                    }
            );
            this.stopSelf();
        }else{
            Log.e(TAG, "Can't find the watch node, quit");
            this.stopSelf();
        }
    }

    @Deprecated
    private void sendStartActivityMessage(String node, final String message) {

        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, NOTE_PATH, message.getBytes()).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(
                            MessageApi.SendMessageResult sendMessageResult) {
                        Log.i(TAG, "Message: " + message);
                        Log.i(TAG, "Sending message was successful: " + sendMessageResult.getStatus()
                                .isSuccess());
                    }

                }
        );
    }

    @Deprecated
    private class StartWearableActivityTask
            extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node, params[0]);
            }
            DataTransfer.this.stopSelf();
            return null;
        }
    }

    @Deprecated
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

}