package edu.neu.mhealth.android.wockets.library.services;

import android.Manifest;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import java.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.Format;
import java.util.Locale;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.managers.ToastManager;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */

public class LocationManagerService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    public final static String TAG = "LocationManagerService";


//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mContext = getApplicationContext();
//        Log.i(TAG,"INSIDE ONCREATE",mContext);
//
//        if (mGoogleApiClient == null) {
//            Log.i(TAG, "Inside connecting to Location Services API", mContext);
//            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//            mGoogleApiClient.connect();
//            Log.i(TAG, "Trying to connect", mContext);
//        }
//    }

    public LocationManagerService(){
        super("LocationManagerService");
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

//    @Override
//    public void onDestroy() {
//        mGoogleApiClient.disconnect();
//        Log.i(TAG,"INSIDE ONDESTROY",mContext);
//        super.onDestroy();
//    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG,"Inside onConnected!",mContext);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ToastManager.showShortToast(mContext, "Please enable location service for the app to function properly.");
            stopSelf();

            return;
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled || !network_enabled) {
            ToastManager.showShortToast(mContext, "Please enable location service for the app to function properly.");
            stopSelf();
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i(TAG,"Writing location!",mContext);
            String dataDirectory = DataManager.getDirectoryData(mContext);
            String gpsFile = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "GPS.csv";

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);

//            Log.i(TAG,DateTime.getCurrentTimestampString());
//            Log.i(TAG,df.format(mLastLocation.getTime()));

            String[] gpsEntry = {
                    DateTime.getCurrentTimestampString(),
                    df.format(mLastLocation.getTime()),
//                    String.valueOf(DateTime.getCurrentTimeInMillis()),
                    String.valueOf(mLastLocation.getLatitude()),
                    String.valueOf(mLastLocation.getLongitude()),
                    String.valueOf(mLastLocation.getAccuracy())
//                    mLastLocation.getProvider()
            };
//            CSV.writeAndZip(gpsEntry, gpsFile, true, mContext);
            CSV.write(gpsEntry, gpsFile, true);

        }


        stopSelf();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"INSIDE ONCONNECTION SUSPENDED",mContext);
        stopSelf();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG,"INSIDE ONCONNECTION FAILED",mContext);
        stopSelf();
    }

}
