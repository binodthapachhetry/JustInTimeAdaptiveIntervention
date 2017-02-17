package edu.neu.android.wocketslib.mhealthformat.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneInfo {
	public static String TAG = "PhoneInfo";

	public static String getID(Context aContext) {

		TelephonyManager mTelephonyMgr = null;

		try {
			mTelephonyMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
		} catch (Exception e) {
			Log.e(TAG, "Error: Could not get access to TelephonyManager. Most likely manifest does not include android.permission.READ_PHONE_STATE permission.");
			return "";
		}

//		if (Globals.IS_DEBUG)
//			Log.d(TAG, "ID discovered is :" + mTelephonyMgr.getDeviceId());
		String id = mTelephonyMgr.getDeviceId();
		if (id == null) {
			id = Secure.getString(aContext.getContentResolver(), Secure.ANDROID_ID);
		}

        String obfuscatedId = obfuscateID(id);
		return obfuscatedId;
	}

    private static String obfuscateID(String id) {
        char[] idArray = id.toCharArray();

        boolean isEven = false;
        if(id.length()%2 == 0) {
            isEven = true;
        }

        if(isEven) {
            for (int i = 0 ; i < id.length() ; i = i + 2) {
                char temp = idArray[i];
                idArray[i] = idArray[i+1];
                idArray[i+1] = temp;
            }
        } else {
            for (int i = 0 ; i < id.length() ; i = i + 2 ) {
                if (i == (id.length()/2 - 1)) {
                    char temp = idArray[i];
                    idArray[i] = idArray[i+2];
                    idArray[i+2] = temp;
                    i++;
                } else {
                    char temp = idArray[i];
                    idArray[i] = idArray[i+1];
                    idArray[i+1] = temp;
                }
            }
        }
        return new String(idArray);
    }

    public static String getBluetoothMacAddressConcated() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // if device does not support Bluetooth
        if(mBluetoothAdapter==null){
            Log.d(TAG,"device does not support bluetooth");
            return null;
        }

        String addr = mBluetoothAdapter.getAddress();
        return addr.replace(":", "");
    }

}
