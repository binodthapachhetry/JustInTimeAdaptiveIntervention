package edu.neu.android.wocketslib.support;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChecker {
	
	public static final boolean isOnline(Context aContext) { 
		ConnectivityManager cm = (ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE); 
		if (cm != null)
		{
			NetworkInfo aNetworkInfo = cm.getActiveNetworkInfo();
			
			if (aNetworkInfo != null)
				return aNetworkInfo.isConnectedOrConnecting();
			else 
				return false; 	
		}
		else
			return false; 
	} 
	
//	private static void showNoInternetDialogue(Context aContext, String aTAG, String title, String message)
//	{
//		AlertDialog.Builder alt_bld = new AlertDialog.Builder(aContext);
//		alt_bld.setMessage(message).setCancelable(false).
//			setPositiveButton("Got it", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						// Action for 'Yes' Button
//						Log.i(aTAG, "NoInternet");
//						}});
//		AlertDialog alert = alt_bld.create();
//		// Title for AlertDialog
//		alert.setTitle(title);
//		// Icon for AlertDialog
//		//alert.setIcon(R.drawable.icon);
//		alert.show();
//	}	
}
