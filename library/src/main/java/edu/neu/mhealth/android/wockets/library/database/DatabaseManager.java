package edu.neu.mhealth.android.wockets.library.database;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.DatabaseEntry;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.managers.ConnectivityManager;
import edu.neu.mhealth.android.wockets.library.support.DateTime;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.support.ObjectMapper;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

/**
 * @author Dharam Maniar
 */
public class DatabaseManager {

	private static final String TAG = "DatabaseManager";

    public static final String MINUTE_SERVICE_PATH = "MinuteService";
    public static final String INTERNET_WIFI = "Internet/Wifi";
    public static final String INTERNET_MOBILE = "Internet/Mobile";
    public static final String MEMORY_FREE = "Memory/Free";
    public static final String STORAGE_INTERNAL_FREE = "Storage/Internal/Free";
    public static final String STORAGE_EXTERNAL_FREE = "Storage/External/Free";
    public static final String BATTERY_PERCENTAGE = "Battery/Available";
    public static final String POWER_DEVICE_IDLE = "Power/DeviceIdleMode";
    public static final String POWER_INTERACTIVE = "Power/Interactive";
    public static final String POWER_SAVE = "Power/SaveMove";
    public static final String APP_VERSION = "AppVersion";
    public static final String SYSTEM_BROADCAST = "SystemBroadcast";

    public static final String SURVEY_PROMPT = "Survey/Prompt";
    public static final String SURVEY_COMPLETE = "Survey/Complete";

    public static final String CONFIG_START_DATE = "Config/StartDate";
    public static final String CONFIG_END_DATE = "Config/EndDate";

	public static void setPersistenceEnabledTrue(Context context) {
		if (!DataManager.getFirebaseDatabasePersistenceEnabled(context)) {
			Log.i(TAG, "Setting Persistence Enabled True", context);
			FirebaseDatabase.getInstance().setPersistenceEnabled(true);
			DataManager.setFirebaseDatabasePersistenceEnabled(context);
		}
	}

	public static void fetchStudyData(final Context context) {
		String studyName = DataManager.getStudyName(context);
		DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
		myRef.child("STUDY").child(studyName).addListenerForSingleValueEvent(
				new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "Start fetching new study data from firebase on data change", context);
						Study study = dataSnapshot.getValue(Study.class);
						DataManager.setStudy(context, study);
                        Log.i(TAG, "Finish Fetching new study data from firebase on data change", context);
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				}
		);
	}

	public static void writeNote(final Context context, final String path, final Object object) {
		String studyName = DataManager.getStudyName(context);
		String userName = UserManager.getUserEmail();

		String datesPath = "NOTES" + "/" + studyName + "/" + userName + "/" + "DATES" + "/" + DateTime.getDate();
        writeToDatabase(context, datesPath, DateTime.getTimezone());

		String participantListPath = "NOTES" + "/" + studyName + "/" + "LIST" + "/" + userName;
        writeToDatabase(context, participantListPath, 1);

        if (path.contains("Config")) {
            String refPath = "NOTES" + "/" + studyName + "/" + userName + "/" + DateTime.getDate() + "/" + path;
            writeToDatabase(context, refPath, object);
        } else {
            String refPath = "NOTES" + "/" + studyName + "/" + userName + "/" + DateTime.getDate() + "/" + path + "/" + String.valueOf(DateTime.getCurrentTimeInMillis());
            writeToDatabase(context, refPath, object);
        }
	}

    private static void writeToDatabase(final Context context, final String path, final Object object) {
        DatabaseEntry localEntry = new DatabaseEntry(path, ObjectMapper.serialize(object));
        localEntry.save();
        if (ConnectivityManager.isInternetConnected(context)) {
            List<DatabaseEntry> databaseEntries = DatabaseEntry.listAll(DatabaseEntry.class);
            for(DatabaseEntry databaseEntry : databaseEntries) {
                Log.i(TAG, "Internet Connected, writing to online database - " + databaseEntry.getPath() + " - " + ObjectMapper.deserialize(databaseEntry.getObject(), Object.class), context);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(databaseEntry.getPath());
                databaseReference.keepSynced(true);
                databaseReference.setValue(ObjectMapper.deserialize(databaseEntry.getObject(), Object.class));
                databaseEntry.delete();
            }
        }
    }
}