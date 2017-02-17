package edu.neu.android.wearwocketslib.utils.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;

import edu.neu.android.wearwocketslib.utils.log.Log;

/**
 * Created by Dharam on 5/1/2015.
 */
public class VersionLogger {

    private static final String TAG = "VersionLogger";

    public static void logAppVersion(Context context) {

        try {
            String appVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            0).versionName;
            Log.i(TAG, "App version - " + appVersion, context);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void logOSVersion(Context context) {

        String codename = Build.VERSION.CODENAME;
        String incremental = Build.VERSION.CODENAME;
        String release = Build.VERSION.RELEASE;
        int sdk = Build.VERSION.SDK_INT;

        Log.i(TAG, "Codename - " + codename + ", Incremental - " + incremental + ", Release - " + release + ",SDK - " + sdk, context);
    }

    public static void logAndroidWearVersion(Context context) {
        final List<PackageInfo> pkgAppsList = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkgInfo : pkgAppsList) {
            if (pkgInfo.packageName.equals("com.google.android.gms")) {
                Log.i(TAG, "Google Play Services - " + pkgInfo.versionName, context);
            }
            if (pkgInfo.packageName.equals("com.google.android.wearable.app")) {
                Log.i(TAG, "Android Wear - " + pkgInfo.versionName, context);
            }
        }
    }

}
