/*
 * Copyright (C) 2015 The Android Open Source Project
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

package mhealth.neu.edu.phire.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import mhealth.neu.edu.phire.R;
import edu.neu.android.wearwocketslib.core.repeatedwakefulservice.WearableWakefulService;
import edu.neu.android.wearwocketslib.services.SensorManagerService;
import edu.neu.android.wearwocketslib.utils.log.Log;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;
import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * A fragment that shows a list of DataItems received from the phone
 */
public class ControlFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ControlFragment";

    private Button mTransferAllButton;
    private Button mRefreshSensorButton;
    private boolean mInitialized;
    private Context mContext;

    private Logger logger = null;

    @Override
    public void onPause() {
        super.onPause();
        logger.close();
    }

    public ControlFragment(){
        logger = new Logger(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.control_fragment, container, false);
        mTransferAllButton = (Button) view.findViewById(R.id.button_transfer_all);
        mTransferAllButton.setOnClickListener(this);
        mRefreshSensorButton = (Button) view.findViewById(R.id.button_refresh_sensor);
        mRefreshSensorButton.setOnClickListener(this);
        mInitialized = true;
        mContext = getActivity().getApplicationContext();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_transfer_all:
                if(mTransferAllButton.getText().toString().equalsIgnoreCase("Force Transfer")) {
                    transferAll();
                }
                break;
            case R.id.button_refresh_sensor:
                restartSensorListenerService();
                break;
        }
    }

    private void transferAll(){
        logger.i("Reset last transfer attempt to start forcing transfer", mContext);
        SharedPrefs.setLong(WearableWakefulService.KEY_LAST_TRANSFER_ATTEMPT, 0, mContext);
        SharedPrefs.setLong(WearableWakefulService.KEY_LAST_ZIP_ATTEMPT, 0, mContext);
        Toast.makeText(mContext, "Start force transfer", Toast.LENGTH_SHORT).show();
        // add logs for today
//        MinuteService.scheduleLogFileTransferWithDate(mContext, new Date());
//         transfer current hour's sensor data file
//        MinuteService.scheduleSensorFileTransferWithDate(mContext, new Date());
//        executor = ScheduledDataTransferExecutor.getInstance(mGoogleApiClient, mContext);
//        if(!executor.isRunning()) {
//            executor.addObserver(this);
//            executor.setInterval(500);
//            new Thread(){
//                @Override
//                public void run() {
//                    executor.startFileQueueTransfer();
//                }
//            }.start();
//        }
    }

    private void cancelTransfer(){

    }

    private void restartSensorListenerService(){
        Intent intent = new Intent(mContext, SensorManagerService.class);
        mContext.stopService(intent);
        mContext.startService(intent);
    }
}
