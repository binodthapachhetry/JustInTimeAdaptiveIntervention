package edu.neu.android.wocketslib.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class PackageChecker {
	
	public static void installedPackageLogging(String aTAG, Context aContext)
	{
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List pkgAppsList = aContext.getPackageManager().queryIntentActivities( mainIntent, 0);
		
		for (Object ri: pkgAppsList) 
		{
			Log.o(aTAG, "Inst", ((ResolveInfo) ri).activityInfo.packageName.toString()); 
		}
		if (pkgAppsList != null)
			Log.o(aTAG, "InstNum", Integer.toString(pkgAppsList.size()));
		else
			Log.o(aTAG, "InstNum", 0);			
	}


}
