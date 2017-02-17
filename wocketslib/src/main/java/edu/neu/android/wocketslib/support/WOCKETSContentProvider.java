package edu.neu.android.wocketslib.support;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;

public class WOCKETSContentProvider extends ContentProvider {
	private static final String TAG = "WOCKETSContentProvider";
	private static final String NULLEQIVALENT = "nullEquivalent";
	private static SQLDataSource sqlitDB;
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		sqlitDB = new SQLDataSource(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		MatrixCursor cursor = new MatrixCursor(new String[] { "key", "value" }, 1);

		SharedPreferences prefs = getContext().getSharedPreferences(TAG, 0);
		String value = prefs.getString(selection, null);
		// System.out.println("----Value is " + value);
				
		if (value == null) {
			//Log.e(TAG, "Null value retrieved from sharedPreferences for key: " + selection);
			if  (Globals.IS_BACK_UP_DATABASE_ENABLED) {				
				value = sqlitDB.get(selection);
				if (value == null) {	
					//Log.e(TAG, "Null value retrieved from sqlitDB for key: " + selection);
				} else {					
					prefs.edit().putString(selection, value).commit();
					Log.e(TAG, "ERROR: SharedPreferences was corrupted! It is updated from sqliteDB for key: " + selection);
				}
			}
		} else if (!value.equals(NULLEQIVALENT)) {
			cursor.addRow(new String[] { selection, value });
		}
				
		return cursor;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		// System.out.println("-----------------------------------------");
		SharedPreferences settings = getContext().getSharedPreferences(TAG, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		String key = values.getAsString("Key");
		String value = values.getAsString("Value");
		
		if (key == null || key.equals("")) {
			Log.e(TAG, "Error: Null/empty key. SP/SQLite is not updated!");
			return 0;
		}
		if (value == null) {
			value = NULLEQIVALENT;
		}
		
		editor.putString(key, value);
		editor.commit();		
		
		if  (Globals.IS_BACK_UP_DATABASE_ENABLED) {
			sqlitDB.update(key, value);
			
			//////////for test////////
			String s1 = settings.getString(key, null);
			String s2 = sqlitDB.get(key);
			if (!s1.equals(s2)) {
				Log.e(TAG, "ERROR: The value saved in SF and DB are different for key: " + key + " SF: " + s1 + " DB: " + s2);
			}
			//////////////////////
		}
		
		
		
		return 1;
	}
}