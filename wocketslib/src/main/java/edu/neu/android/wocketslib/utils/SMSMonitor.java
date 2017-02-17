package edu.neu.android.wocketslib.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import edu.neu.android.wocketslib.support.DataStorage;

public class SMSMonitor {
	private static final String TAG = "SMSMonitor";
	
	public static int getSMSSentCount(Context aContext)
	{
		long lastNum = DataStorage.getNumSMS(aContext);

		int number = 0; 
		Uri uriSMSURI = Uri.parse("content://sms"); 
		Cursor cur = aContext.getContentResolver().query(uriSMSURI, null, null, null, null); 
		String protocol; 
		while (cur.moveToNext())
		{
			protocol = cur.getString(cur.getColumnIndex("protocol"));
			if (protocol == null)
			{
				number++;
			}			
		}

		if (lastNum != number)
			Log.d(TAG, "SMS out nums: " + lastNum + " " + number); 
		if (number > lastNum)
		{
			DataStorage.setNumSMS(aContext, number);
			return (int) (number-lastNum);
		}
		else if (number == lastNum)
			return 0;
		else 
		{
			DataStorage.setNumSMS(aContext, number);
			return 0; 
		}
	}	
}
