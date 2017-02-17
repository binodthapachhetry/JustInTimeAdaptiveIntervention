package edu.neu.mhealth.android.wockets.library.study;

import android.content.Context;

import edu.neu.mhealth.android.wockets.library.database.DatabaseManager;

/**
 * @author Dharam Maniar
 */
public class StudyManager {

	private static final String TAG = "StudyManager";

	private static StudyManager instance = new StudyManager();

	private StudyManager() {}

	public static StudyManager getInstance() {
		return instance;
	}

	public void fetchLatestStudyData(Context mContext) {
		DatabaseManager.fetchStudyData(mContext);
	}

}
