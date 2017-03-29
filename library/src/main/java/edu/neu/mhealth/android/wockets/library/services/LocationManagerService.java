package edu.neu.mhealth.android.wockets.library.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import java.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.Format;
import java.util.Locale;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.CSV;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * @author Dharam Maniar
 */

public class LocationManagerService extends WocketsService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private Context mContext;

    public final static String TAG = "LocationManagerService";


    @Override
    public void onCreate() {
        super.onCreate();
        // Create an instance of GoogleAPIClient.
        mContext = getApplicationContext();
        Log.i(TAG,"INSIDE ONCREATE",mContext);

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
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        Log.i(TAG,"INSIDE ONDESTROY",mContext);
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            String dataDirectory = DataManager.getDirectoryData(mContext);
            String gpsFile = dataDirectory + "/" + DateTime.getDate() + "/" + DateTime.getCurrentHourWithTimezone() + "/" + "GPS.csv";

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);

//            Log.i(TAG,DateTime.getCurrentTimestampString());
//            Log.i(TAG,df.format(mLastLocation.getTime()));

            String[] gpsEntry = {
//                    DateTime.getCurrentTimestampString(),
                    df.format(mLastLocation.getTime()),
                    String.valueOf(DateTime.getCurrentTimeInMillis()),
                    String.valueOf(mLastLocation.getLatitude()),
                    String.valueOf(mLastLocation.getLongitude()),
                    String.valueOf(mLastLocation.getAccuracy()),
                    mLastLocation.getProvider()
            };
            CSV.writeAndZip(gpsEntry, gpsFile, true, mContext);
        }


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
