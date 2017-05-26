package mhealth.neu.edu.phire.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import edu.neu.android.wocketslib.wear.WearNoteSender;
import edu.neu.mhealth.android.wockets.library.support.Log;


public class SendMessageToWatch extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    public final static String TAG = "SendMessageToWatch";

    public SendMessageToWatch(){
        super("SendMessageToWatch");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = getApplicationContext();
        Log.i(TAG,"INSIDE ONHANDLE INTENT",mContext);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG,"INSIDE ONCONNECTED",mContext);
        Log.i(TAG, "Sending trigger for alarm to watch", mContext);
        WearNoteSender.sendNote("trigger",mContext);
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"INSIDE ONCONNECTION SUSPENDED",mContext);
        stopSelf();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG,"INSIDE ONCONNECTION FAILED",mContext);
        stopSelf();

    }
}
