/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mhealth.neu.edu.phire;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.AlwaysOnService;
import mhealth.neu.edu.phire.fragments.AssetFragment;
import mhealth.neu.edu.phire.fragments.ControlFragment;
import mhealth.neu.edu.phire.fragments.DataFragment;
import mhealth.neu.edu.phire.fragments.KeyInformationFragment;
import mhealth.neu.edu.phire.fragments.SplashFragment;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulBroadcastAlarm;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * The main activity with a view pager, containing three pages:<p/>
 * <ul>
 * <li>
 * Page 1: shows a list of DataItems received from the phone application
 * </li>
 * <li>
 * Page 2: shows the photo that is sent from the phone application
 * </li>
 * <li>
 * Page 3: includes two buttons to show the connected phone and watch devices
 * </li>
 * </ul>
 */
public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener {

    private static final String TAG = "MainActivity";

    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;
    private GridViewPager mPager;
    private DataFragment mDataFragment;
    private AssetFragment mAssetFragment;
    private ControlFragment mControlFragment;
    private SplashFragment mSplashFragment;
    private KeyInformationFragment mKeyInfoFragment;

    private Logger logger = null;

    @Override
    public void onCreate(Bundle b) {
        logger = new Logger(TAG);
        super.onCreate(b);
        mHandler = new Handler();
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Globals.initGlobals(getApplicationContext());
        setupViews();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(getIntent().getBooleanExtra("FINISH_TRANSFER", false)){
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This doesn't really work now
        if(getIntent().getBooleanExtra("FINISH_TRANSFER", false)){
            finish();
        }
        mGoogleApiClient.connect();
        showNodes(edu.neu.android.wearwocketslib.Globals.CAPABILITY_NAME);

        startService(new Intent(this, AlwaysOnService.class));

//        setAlarmForWakefulService();

    }

//    private void setAlarmForWakefulService(){
//        WearableWakefulBroadcastAlarm alarm = new WearableWakefulBroadcastAlarm(this, "MAIN_ACTIVITY");
//        alarm.setAlarm();
//    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        logger.close();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        logger.e("onConnectionFailed(): Failed to connect, with result: " + result, getApplicationContext());
    }

    private void generateEvent(final String title, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataFragment.appendItem(title, text);
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (PHIREWearListenerService.IMAGE_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset photo = dataMapItem.getDataMap()
                            .getAsset(PHIREWearListenerService.IMAGE_KEY);
                    final Bitmap bitmap = loadBitmapFromAsset(mGoogleApiClient, photo);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            logger.d("Setting background image on second page..");
                            moveToPage(1);
                            mAssetFragment.setBackgroundImage(bitmap);
                        }
                    });

                } else if (PHIREWearListenerService.COUNT_PATH.equals(path)) {
                    logger.i("Data Changed for COUNT_PATH", this);
                    generateEvent("DataItem Changed", event.getDataItem().toString());
                } else {
                    logger.i("Unrecognized path: " + path, this);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                generateEvent("DataItem Deleted", event.getDataItem().toString());
            } else {
                generateEvent("Unknown data event type", "Type = " + event.getType());
            }
        }
    }

    /**
     * Find the connected nodes that provide at least one of the given capabilities
     */
    private void showNodes(final String... capabilityNames) {
        Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(

                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(
                            CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            logger.e("Failed to get capabilities", getApplicationContext());
                            return;
                        }
                        Map<String, CapabilityInfo>
                                capabilitiesMap = getAllCapabilitiesResult.getAllCapabilities();
                        Set<Node> nodes = new HashSet<>();
                        if (capabilitiesMap.isEmpty()) {
                            showDiscoveredNodes(nodes);
                            return;
                        }
                        for (String capabilityName : capabilityNames) {
                            CapabilityInfo capabilityInfo = capabilitiesMap.get(capabilityName);
                            if (capabilityInfo != null) {
                                nodes.addAll(capabilityInfo.getNodes());
                            }
                        }
                        showDiscoveredNodes(nodes);
                    }

                    private void showDiscoveredNodes(Set<Node> nodes) {
                        List<String> nodesList = new ArrayList<>();
                        for (Node node : nodes) {
                            nodesList.add(node.getDisplayName());
                        }
                        logger.d("Connected Nodes: " + (nodesList.isEmpty()
                                ? "No connected device was found for the given capabilities"
                                : TextUtils.join(",", nodesList)));
                        String msg;
                        if (!nodesList.isEmpty()) {
                            msg = getString(R.string.connected_nodes,
                                    TextUtils.join(", ", nodesList));
                        } else {
                            msg = getString(R.string.no_device);
                        }
                        mSplashFragment.setConnectedDevice(msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Extracts {@link Bitmap} data from the
     * {@link Asset}
     */
    private Bitmap loadBitmapFromAsset(GoogleApiClient apiClient, Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                apiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            logger.w("Requested an unknown Asset.", this);
            return null;
        }
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        logger.i("onMessageReceived: " + event, this);
        generateEvent("Message", event.toString());
    }

    @Override
    public void onPeerConnected(Node node) {
        generateEvent("Node Connected", node.getId());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        generateEvent("Node Disconnected", node.getId());
    }

    private void setupViews() {
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageCount(3);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
        dotsPageIndicator.setPager(mPager);
        mDataFragment = new DataFragment();
        mAssetFragment = new AssetFragment();
        mControlFragment = new ControlFragment();
        mSplashFragment = new SplashFragment();
        mKeyInfoFragment = new KeyInformationFragment();
        List<Fragment> pages = new ArrayList<>();
        pages.add(mSplashFragment);
        pages.add(mKeyInfoFragment);
        pages.add(mControlFragment);
        pages.add(mDataFragment);
        pages.add(mAssetFragment);
        final MyPagerAdapter adapter = new MyPagerAdapter(getFragmentManager(), pages);
        mPager.setAdapter(adapter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Switches to the page {@code index}. The first page has index 0.
     */
    private void moveToPage(int index) {
        mPager.setCurrentItem(0, index, true);
    }

    private class MyPagerAdapter extends FragmentGridPagerAdapter {

        private List<Fragment> mFragments;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Override
        public Fragment getFragment(int row, int column) {
            return mFragments.get(column);
        }

    }
}
