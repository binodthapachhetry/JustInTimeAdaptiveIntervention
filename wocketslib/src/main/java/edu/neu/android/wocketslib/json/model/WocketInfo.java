package edu.neu.android.wocketslib.json.model;

import java.util.List;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;

public class WocketInfo {
	private static final String TAG = "WocketInfo";

	public List<Note> someNotes;
	public List<FileUploadEvent> someFileUploads;
	public List<Sensor> someSensors;
	public List<Swapping> someSwaps;
	public List<PromptEvent> somePrompts;
	public List<PhoneData> somePhoneData;
	public List<WocketData> someWocketData;
	public List<WocketStatsData> someWocketStatsData;
	public List<ActivityCountData> someActivityCountData;
	public List<HRData> someHRData;

	@SerializedName("studyID")
	public String studyID;

	@SerializedName("pid")
	public String participantID;

	@SerializedName("phoneID")
	public String phoneID;

	@SerializedName("err")
	public String errorMsg;

	public void clear() {
		// If a change is made here, be sure to change isEmpty as well.

		someNotes = null;
		someFileUploads = null;
		someSensors = null;
		someSwaps = null;
		somePrompts = null;
		somePhoneData = null;
		someWocketData = null;
		someActivityCountData = null;
		someWocketStatsData = null;
		someHRData = null;

		errorMsg = null;
	}

	public boolean isEmpty() {
		if (someNotes != null)
			return false;
		else if (someFileUploads != null)
			return false;
		else if (someSensors != null)
			return false;
		else if (someSwaps != null)
			return false;
		else if (somePhoneData != null)
			return false;
		else if (someActivityCountData != null)
			return false;
		else if (someWocketStatsData != null)
			return false;
		else if (somePrompts != null)
			return false;
		else if (someWocketData != null)
			return false;
		else if (someHRData != null)
			return false;
		else if (errorMsg != null)
			return false;

		// Looks empty
		return true;
	}

    public boolean updateInfoIfNeeded(Context aContext)
    {
        if ((Globals.STUDY_SERVER_NAME == null) ||
                (participantID == null) ||
                (phoneID == null))
        {
            Log.e(TAG, "Need to update WocketInfo study name, participant ID, or phone ID");
            updateInfo(aContext);
            return true;
        } else
            return false;
    }

    private void updateInfo(Context aContext)
    {
        if (Globals.STUDY_SERVER_NAME == null)
            Log.e(TAG, "Error: STUDY_SERVER_NAME is null and should be defined.");
        else
            studyID = Globals.STUDY_SERVER_NAME;

        String pid = DataStorage.GetSubjectID(aContext);
        if (pid == null)
            Log.e(TAG, "Error: Subject ID is null and should be defined");
        else if (pid.length() == 0)
            Log.e(TAG, "Error: Subject ID is empty and should be defined.");
        else
            participantID = pid;

        String aPhoneID = PhoneInfo.getID(aContext);
        if (aPhoneID == null)
            Log.e(TAG, "Error: Phone ID is null and should be defined");
        else if (aPhoneID.length() == 0)
            Log.e(TAG, "Error: Phone ID is empty and should be defined.");
        else
            phoneID = aPhoneID;

    }

	public WocketInfo(Context aContext) {
		clear();
        updateInfo(aContext);
	}
}
