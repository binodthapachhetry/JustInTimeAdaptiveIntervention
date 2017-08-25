package edu.neu.mhealth.android.wockets.library.managers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.WocketsUtil;

/**
 * @author Dharam Maniar
 *
 * Class to manage permissions for Android.
 */
public class PermissionManager {

    private final static String TAG = "PermissionManager";

    /**
     * Function to return all dangerous permissions for Android.
     * See https://developer.android.com/guide/topics/security/permissions.html
     *
     * @return list of all dangerous permission
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static List<String> getAllDangerousPermissions(Context mContext) {
        Log.i(TAG, "Inside getAllDangerousPermissions", mContext);
        List<String> permissions = new ArrayList<>();
//        permissions.add(Manifest.permission.READ_CALENDAR);
//        permissions.add(Manifest.permission.WRITE_CALENDAR);
//        permissions.add(Manifest.permission.CAMERA);
//        permissions.add(Manifest.permission.READ_CONTACTS);
//        permissions.add(Manifest.permission.WRITE_CONTACTS);
//        permissions.add(Manifest.permission.GET_ACCOUNTS);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
//        permissions.add(Manifest.permission.CALL_PHONE);
//        permissions.add(Manifest.permission.READ_CALL_LOG);
//        permissions.add(Manifest.permission.WRITE_CALL_LOG);
//        permissions.add(Manifest.permission.USE_SIP);
//        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.BODY_SENSORS);
//        permissions.add(Manifest.permission.SEND_SMS);
//        permissions.add(Manifest.permission.RECEIVE_SMS);
//        permissions.add(Manifest.permission.READ_SMS);
//        permissions.add(Manifest.permission.RECEIVE_WAP_PUSH);
//        permissions.add(Manifest.permission.RECEIVE_MMS);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return permissions;
    }

    /**
     * Check if the given permission is granted to the application
     *
     * @param mContext      {@link Context}
     * @param permission    {@link android.Manifest.permission}
     * @return  True if the given permission is granted
     */
    private static boolean isPermissionGroupGranted(Context mContext, String permission) {
        Log.i(TAG, "Inside isPermissionGroupGranted for permission - " + permission, mContext);
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(mContext, permission);
    }

    /**
     * Check if all permissions are available to the application
     *
     * @param mContext  {@link Context}
     * @return  True if all the permissions are available
     */
    public static boolean areAllPermissionsAvailable(Context mContext) {
        Log.i(TAG, "Inside areAllPermissionsAvailable", mContext);
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        List<String> permissionGroups = PermissionManager.getAllDangerousPermissions(mContext);

        for(String permissionGroup : permissionGroups) {
            if (!PermissionManager.isPermissionGroupGranted(mContext, permissionGroup)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request all permission from the user
     *
     * @param mActivity {@link Activity}
     * @param mContext  {@link Context}
     */
    public static void requestAllPermissions(Activity mActivity, Context mContext) {
        Log.i(TAG, "Inside requestAllPermissions", mContext);
        List<String> allDangerousPermissions = PermissionManager.getAllDangerousPermissions(mContext);
        List<String> dangerousPermissionsToRequest = new ArrayList<>();
        for(String permissionGroup: allDangerousPermissions) {
            if (!isPermissionGroupGranted(mContext, permissionGroup)) {
                dangerousPermissionsToRequest.add(permissionGroup);
            }
        }

        if (dangerousPermissionsToRequest.size() == 0) {
            return;
        }

        Log.i(TAG, "Requesting Permissions", mContext);
        ActivityCompat.requestPermissions(mActivity, WocketsUtil.listOfStringsToStringArray(dangerousPermissionsToRequest), 1);
    }
}