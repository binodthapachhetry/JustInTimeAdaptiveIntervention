package edu.neu.android.wocketslib.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.neu.android.wocketslib.utils.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
	
	//public static final String TAG = "SQLiteHelper";
	public static final String TABLE_NAME = "WocketDBTable";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_VALUE = "value";
    private static final String DATABASE_NAME = "sqlDatabase";
    private static final int DATABASE_VERSION = 1;
    
 // Database creation sql statement
 	private static final String DATABASE_CREATE = "create table "
 	      + TABLE_NAME + "(" + COLUMN_KEY + " text not null, " + COLUMN_VALUE
 	      + " text not null);";

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.o(SQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(db);
	}

}
