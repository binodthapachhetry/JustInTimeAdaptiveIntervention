package edu.neu.android.wocketslib.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.neu.android.wocketslib.utils.Log;

public class SQLDataSource {
	private static SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private static final String TAG = "SQLDataSource";
	
    public SQLDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
        database = dbHelper.getWritableDatabase();
	}  

    public void close() {
        dbHelper.close();
    }
    
    public void update(String key, String value) {
//		Log.o(TAG, "key: " + key + "," + "value: " + value);
    	ContentValues cv = new ContentValues();
    	cv.put(SQLiteHelper.COLUMN_KEY, key); 
    	cv.put(SQLiteHelper.COLUMN_VALUE, value);
    	int i = database.update(SQLiteHelper.TABLE_NAME, cv, null, null);
    	if (i == 0) {
    		createRecord(cv);
    	}
    }
    
    public void createRecord(ContentValues cv) {
    	database.insert(SQLiteHelper.TABLE_NAME, null, cv);
    }
    
    //public Cursor get(String id) {
    public String get(String id) {
    	Cursor mCursor = null;
    	String[] allColumns = { SQLiteHelper.COLUMN_KEY,
    			SQLiteHelper.COLUMN_VALUE };
    	try {
    		//mCursor = database.rawQuery("select * from " + SQLiteHelper.TABLE_NAME + " where key =?", new String[] {id});
    		mCursor = database.query(SQLiteHelper.TABLE_NAME, allColumns, SQLiteHelper.COLUMN_KEY  + " =? ", new String[] {id}, null, null, null, null);
    	} catch (Exception e) {
    		Log.e(TAG, e.toString() + "for key: " + id);
    	}
        if (mCursor != null) {  
        	if (mCursor.moveToFirst()) {
        		return mCursor.getString(mCursor.getColumnIndex(SQLiteHelper.COLUMN_VALUE));
        	}
        }
        return null;
    }  
    
    /*public void deleteValue(String key) {
        database.delete(SQLiteHelper.TABLE_VALUES, SQLiteHelper.COLUMN_ID
            + " = " + key, null);
    }*/

    
}
