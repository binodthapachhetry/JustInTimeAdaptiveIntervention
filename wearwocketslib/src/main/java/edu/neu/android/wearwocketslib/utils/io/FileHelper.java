package edu.neu.android.wearwocketslib.utils.io;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

import edu.neu.android.wearwocketslib.utils.log.Log;

/**
 * Created by qutang on 8/10/15.
 */
public class FileHelper {
    public static final String TAG = "FileHelper";

    public static ArrayList<File> getRecusiveDirs(File aDir, int recursiveLevel){
        ArrayList<File> result = new ArrayList<File>();
        if(recursiveLevel >= 0) {
            for (File entry : aDir.listFiles()) {
                if (entry.isDirectory()) {
                    result.addAll(getRecusiveDirs(entry, recursiveLevel - 1));
                }
            }
        }else{
            result.add(aDir);
        }
        return result;
    }

    public static ArrayList<String> getRecusiveDirs(String aDir, int recursiveLevel){
        ArrayList<String> result = new ArrayList<String>();
        if(recursiveLevel >= 0) {
            for (File entry : new File(aDir).listFiles()) {
                if (entry.isDirectory()) {
                    result.addAll(getRecusiveDirs(entry.getAbsolutePath(), recursiveLevel - 1));
                }
            }
        }else{
            result.add(aDir);
        }
        return result;
    }

    public static boolean deleteDir(File dir, Context context) {
        boolean isDeleted = true;
        boolean isFileDeleted = false;
        File tempFile;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                tempFile = new File(dir, children[i]);
                if (tempFile.isDirectory())
                    isFileDeleted = deleteDir(tempFile, context);
                else
                    isFileDeleted = tempFile.delete();
                if (!isFileDeleted) {
                    Log.e(TAG, "DeleteDir failed to delete file or directory: " + children[i], context);
                    isDeleted = false;
                }
            }
        } else {
            Log.e(TAG, "DeleteDir sent a non-directory" + dir.getAbsolutePath(), context);
            return false;
        }

        isFileDeleted = dir.delete();
        if (!isFileDeleted) {
            Log.e(TAG, "DeleteDir failed to delete top-level directory.", context);
        }

        return isDeleted;
    }

    public static boolean deleteDir(String aDirFileName, Context context) {
        return deleteDir(new File(aDirFileName), context);
    }

}
