package edu.neu.android.wocketslib.dataupload;

import java.util.Date;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.support.DataStorage;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;

/**
 * Upload all the data to the server, typically at the completion of the study. This will send data
 * from the current day as well as past days, leaving data in place on the phone where it started.
 * It should NOT be run regularly because it will resend large amounts of data!
 */
public class SendAllFilesToServerTask extends AsyncTask<Void, Void, Boolean> {
	private final static String TAG = "SendAllFilesToServerTask";
	
	private Context mContext;

	public SendAllFilesToServerTask(Context context) {
		mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

		// send JSON file
		long currentTime = System.currentTimeMillis();
		String msg = "Finish study pressed. Starting data and log files upload";

		//Transmit note about this upload first
		ServerLogger.transmitOrQueueNote(mContext, msg, true);

        //Copy standard log files (wherever saved) to internal upload folder (include today)
        DataSender.copyLogsToInternalUploadDir(mContext, true, true);

		//Copy survey log files (internal or external) to an upload folder (include today)
        DataSender.copyInternalSurveyLogsToInternalUploadDir(mContext, true, true);
        DataSender.copyExternalSurveyLogsToExternalUploadDir(mContext, true, true);

		//Copy data files (internal or external) to an upload folder (include today)
        DataSender.copyInternalDataLogsToInternalUploadDir(mContext, true, true);
        DataSender.copyExternalDataLogsToExternalUploadDir(mContext, true, true);

        //Copy mhealth sensor files (external) to external uploads folder (include today)
        DataSender.copyMHealthToExternalUploadDir(mContext, true, true);

        //Move all data in the internal upload queue to the external upload queue and zip if needed
		if (Globals.IS_DEBUG)
			Log.i(TAG, "Move and zip internal upload dir data to external upload directory");
		DataSender.sendInternalUploadDataToExternalUploadDir(mContext, false, true);

//		Log.d(TAG, "WHATS LEFT ----------------------------------------------------------");
//		DataManager.listFilesInternalStorage();
//		DataManager.listFilesExternalStorage();

        // Zip the JSON zips so fewer uploads are required
        DataManager.zipJSONSExternalUploads(mContext);
        DataManager.zipJSONSInternalUploads(mContext);

        int numFilesStart = DataManager.countFilesExtUploadDir() +
                            DataManager.countFilesIntUploadDir();

		//Upload JSON files and remove
		int filesRemaining = RawUploader.uploadDataFromExtUploadDir(mContext,
                true, true, true, Globals.UPLOAD_SUCCESS_PERCENTAGE, false);

		//Upload all other files in external uploads, backup and remove
		filesRemaining = RawUploader.uploadDataFromExtUploadDir(mContext,
                false, true, true, Globals.UPLOAD_SUCCESS_PERCENTAGE, false);

		//Upload all other files in internal uploads, backup and remove
		filesRemaining = RawUploader.uploadDataFromIntUploadDir(mContext,
                false, true, true, Globals.UPLOAD_SUCCESS_PERCENTAGE, false);

		msg = "Completed user-initiated file upload attempt of " + numFilesStart + " files after "
				+ String.format(
						"%.1f",
						((System.currentTimeMillis() - currentTime) / 1000.0 / 60.0))
				+ " minutes. Files remaining to upload: " + filesRemaining;
		ServerLogger.sendNote(mContext, msg, true);
		return true;
	}

	// Override this method if user wants to update UI
	protected void onPostExecute(Boolean isNeedUpdate) {
		Toast toast = Toast.makeText(mContext, "Transmission complete.", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();		
		Date now = new Date();
        
        Log.i("UPLOAD_CONFIRMATION",  "TIME OF COMPLETE UPLOAD : " +now.toString());
        DataStorage.SetValue(mContext, "LAST_UPLOAD_TIME", now.toString());

	}

}