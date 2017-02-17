package edu.neu.android.wocketslib.wear;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.neu.android.wocketslib.broadcastreceivers.MonitorServiceBroadcastReceiver;
import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;
import edu.neu.android.wocketslib.support.AuthorizationChecker;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;

/**
 * Created by qutang on 6/10/15.
 */
public class WearDataListenerService extends WearableListenerService {

    public static final String TAG = "WearDataListenerService";

    public static final String FILE_PATH = "/filefromwatch";
    private static final String FILE_KEY = "file";
    private static final String PATH_KEY = "path";
    private static final String NAME_KEY = "name";
    private static final String COUNT_KEY = "count";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String NOTE_PATH = "/notefromwatch";
    private static final String SIZE_KEY = "size";

    private ExecutorService mHandleAssetExecutor;
    private GoogleApiClient mGoogleApiClient;




    @Override
    public void onCreate() {
        Log.i(TAG, "InsideOnCreate");
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        mHandleAssetExecutor = Executors.newFixedThreadPool(1);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "InsideOnDestroy");
        mHandleAssetExecutor.shutdown();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "InsideOnStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (FILE_PATH.equals(path)) {

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
//                    mHealthFormat.setSubjectId(DataStorage.GetSubjectID(this));
                    AssetProcessor assetProcessor = new AssetProcessor(dataMapItem, mHealthFormat.getSubjectId());
                    mHandleAssetExecutor.execute(assetProcessor);
                    Log.i(TAG, "Receiving asset");
                } else {
                    sendGeneralMessage("DataItem changed", event.getDataItem().toString());
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                sendGeneralMessage("DataItem deleted", event.getDataItem().toString());
            }
        }
        super.onDataChanged(dataEvents);
    }

    public class AssetProcessor implements Runnable{

        DataMapItem mDataMap;
//        String subjectId;

        public AssetProcessor(DataMapItem dataMap, String subjectId){
            mDataMap = dataMap;
//            this.subjectId = subjectId;
        }

        @Override
        public void run() {
//            processAsset(mDataMap, subjectId);
            processAsset(mDataMap);
        }
    }

//    private void processAsset(DataMapItem dataMapItem, String subjectId){
    private void processAsset(DataMapItem dataMapItem){
        DataMap dataMap = dataMapItem.getDataMap();
        String filePath = dataMap.getString(PATH_KEY);
        String fileName = dataMap.getString(NAME_KEY);
        int count = dataMap.getInt(COUNT_KEY);
        long timestamp = dataMap.getLong(TIMESTAMP_KEY);
        long size = dataMap.getLong(SIZE_KEY);
        Asset asset = dataMap.getAsset(FILE_KEY);

        long currentTs = System.currentTimeMillis();

        Log.i(TAG, "Saving asset... ");
//        if(saveAsset(asset, filePath, fileName, size, subjectId)){
        if(saveAsset(asset, filePath, fileName, size)){
            Log.i(TAG, "Saving asset successful");
            sendAssetMessage(filePath, fileName, timestamp, count);
            WearNoteSender.sendNote("TRANSFER_SUCCESS:"+ filePath + "/" + fileName, getApplicationContext());
        }else{
            Log.i(TAG, "Saving asset unsuccessful");
            sendGeneralMessage("Asset saving failed", timestamp + "\n" + filePath + "\n" + fileName);
            WearNoteSender.sendNote("TRANSFER_FAILURE:"+filePath + "/" + fileName, getApplicationContext());
        }
    }

//    private boolean saveAsset(Asset asset, String path, String name, long size, String subjectId){
    private boolean saveAsset(Asset asset, String path, String name, long size){
        boolean result = false;
        File filePath = new File(path);
        if(path.contains("data")) {
            try {

//                String newPath = path.replace("data", "data" + File.separator + subjectId);
//                filePath = new File(newPath);
                filePath = new File(path);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        File file = new File(filePath.getAbsolutePath() + "/" + name);
        filePath.mkdirs();
        FileOutputStream fileOutputStream = null;
        InputStream assetInputStream = null;
        try {
            Log.i(TAG, "Writting asset to " + file.getAbsolutePath());
            assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            Log.i(TAG, "Got asset stream");
            file.delete(); //delete the old one first
            if(!file.exists()){
                file.createNewFile();
            }
            final byte[] b = IOUtils.toByteArray(assetInputStream);
            Log.i(TAG, "Asset name: " + name);
            Log.i(TAG, "Saved asset size:" + b.length + " Bytes");
            Log.i(TAG, "Asset path: " + filePath.getAbsolutePath());
            int sizeDiff = (int) (b.length - size);
            if(sizeDiff == 0){
                Log.i(TAG, "Received and sent asset size matches");
            }else{
                Log.e(TAG, "Warning: received asset is smaller than sent asset: " + sizeDiff + " in Byte");
            }
            if(sizeDiff == 0){
                FileUtils.writeByteArrayToFile(file, b);
                result = true;
            }else{
                result = false;
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error in parsing received asset:" + e.getMessage() + ". " + e.toString());
            return false;
        }
    }

    private void sendAssetMessage(String path, String name, long ts, int count){
        Log.d(TAG, "Broadcasting asset message");
        Intent intent = new Intent("ASSET_REPORT");
        // You can also include some extra data.
        intent.putExtra(PATH_KEY, path);
        intent.putExtra(NAME_KEY, name);
        intent.putExtra(TIMESTAMP_KEY, ts);
        intent.putExtra(COUNT_KEY, count);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendGeneralMessage(String title, String message){
        Log.d(TAG, "Broadcasting general message");
        Intent intent = new Intent("GENERAL_REPORT");
        // You can also include some extra data.
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "onMessageReceived: " + messageEvent.toString());

        if(messageEvent.getPath().equals(NOTE_PATH)){
            String message = new String(messageEvent.getData());
            switch(message){
                case "TRIGGER":
                    Log.i(TAG, "Received trigger from watch");
                    // inverse trigger, try to set bluetooth sensor service alarm
                    Intent setAlarmIntent = new Intent(getApplicationContext(), MonitorServiceBroadcastReceiver.class);
                    setAlarmIntent.setAction(MonitorServiceBroadcastReceiver.TYPE_SET_MONITOR_SERVICE_ALARM_IF_NECESSARY);
                    setAlarmIntent.putExtra("FROM", "watch");
                    sendBroadcast(setAlarmIntent);
                    break;
            }
        }

        sendGeneralMessage("Message from watch", messageEvent.toString());
        super.onMessageReceived(messageEvent);
    }

    @Override
    public void onPeerConnected(Node peer) {
        sendGeneralMessage("Connected", peer.toString());
        super.onPeerConnected(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        sendGeneralMessage("Disconnected", peer.toString());
        super.onPeerDisconnected(peer);
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        super.onConnectedNodes(connectedNodes);
    }
}
