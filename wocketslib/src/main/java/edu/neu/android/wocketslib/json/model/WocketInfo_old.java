package edu.neu.android.wocketslib.json.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WocketInfo_old {

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

	@SerializedName("phoneID")
	public String phoneID;

	@SerializedName("err")
	public String errorMsg;

	public void clear()
	{
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

	public boolean isEmpty()
	{
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
	
	
	public WocketInfo_old()
	{
		clear();
	}
}
