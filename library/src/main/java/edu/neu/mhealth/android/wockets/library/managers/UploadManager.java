package edu.neu.mhealth.android.wockets.library.managers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;

import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.FileUploadEntry;
import edu.neu.mhealth.android.wockets.library.support.FileUtils;
import edu.neu.mhealth.android.wockets.library.support.Log;
import edu.neu.mhealth.android.wockets.library.user.UserManager;

/**
 * @author Dharam Maniar
 */
public class UploadManager {

	private final static String TAG = "UploadManager";

    private static UploadTask uploadTask;

	public static void uploadFile(final String filePath, final Context mContext) {
        FileUploadEntry fileUploadEntry = new FileUploadEntry(filePath);
        fileUploadEntry.save();

        if (!ConnectivityManager.isInternetConnected(mContext)) {
            Log.i(TAG, "No internet connectivity. Not trying to upload files right now", mContext);
            return;
        }
        if (uploadTask != null && uploadTask.isInProgress()) {
            Log.i(TAG, "Uploading is already in progress.", mContext);
            return;
        }
        uploadFileNow(mContext);
	}

    private synchronized static void uploadFileNow(final Context mContext) {
        try {
            List<FileUploadEntry> fileUploadEntries = FileUploadEntry.listAll(FileUploadEntry.class);
            if (fileUploadEntries == null) {
                return;
            }
            if (fileUploadEntries.size() == 0) {
                notifyUser(mContext);
                return;
            }
            Log.i(TAG, "Number of files left to upload - " + fileUploadEntries.size(), mContext);
            notifyUser(mContext);
            final FileUploadEntry fileUploadEntry = fileUploadEntries.get(0);

            String studyName = DataManager.getStudyName(mContext);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://project-3196244310896515051.appspot.com/" + studyName + "/");

            final File file = new File(fileUploadEntry.getPath());
            final File uploadedFile = new File(fileUploadEntry.getPath() + ".uploaded");
            if (uploadedFile.exists()) {
                Log.i(TAG, "File already uploaded. Not overwriting it.", mContext);
                fileUploadEntry.delete();
                notifyUser(mContext);
                return;
            }
            // If file doesn't exist, delete it from the uploadQueue
            if (!file.exists()) {
                fileUploadEntry.delete();
                notifyUser(mContext);
                return;
            }

            Log.i(TAG, "Uploading File - " + fileUploadEntry.getPath(), mContext);
            String remotePath = UserManager.getUserEmail() + "/" + fileUploadEntry.getPath().split(studyName)[1];
            Uri fileUri = Uri.fromFile(file);
            uploadTask = storageRef.child(remotePath).putFile(fileUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "File Upload Failed - " + file.getAbsolutePath(), e, mContext);
                    uploadFileNow(mContext);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "File upload successful - " + file.getAbsolutePath(), mContext);
                    FileUtils.renameFile(file, file.getAbsolutePath() + ".uploaded");
                    fileUploadEntry.delete();
                    notifyUser(mContext);
                    uploadFileNow(mContext);
                }
            });
        } catch (RuntimeException e) {
            Log.e(TAG, "Runtime exception while trying to upload the file", e, mContext);
            FirebaseCrash.log(UserManager.getUserEmail() + " - Runtime exception while trying to upload the file");
            uploadFileNow(mContext);
        }
    }

    private static void notifyUser(Context mContext) {
        if (DataManager.isStudyFinished(mContext)) {
            final List<FileUploadEntry> fileUploadEntries = FileUploadEntry.listAll(FileUploadEntry.class);
            NotificationManager.showUploadCountNotification(
                    mContext,
                    DataManager.getStudyName(mContext),
                    "Files pending to upload - " + fileUploadEntries.size()
            );
        }
    }
}
