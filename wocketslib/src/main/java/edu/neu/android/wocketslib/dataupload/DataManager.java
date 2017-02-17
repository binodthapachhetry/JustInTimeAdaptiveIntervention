package edu.neu.android.wocketslib.dataupload;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Zipper;

/**
 * Manage data files on internal and external storage locations. Utility functions for moving data
 * around and determining what is where.
 */
public class DataManager {
    private static final String TAG = "DataManager";

    public static int countFilesExtUploadDir() {
        String[] someFiles = getFileNamesExtUploadDir();
        if (someFiles != null)
            return someFiles.length;
        else
            return 0;
    }

    public static int countFilesIntUploadDir() {
        String[] someFiles = getFileNamesIntUploadDir();
        if (someFiles != null)
            return someFiles.length;
        else
            return 0;
    }

    public static void listFilesInternalStorage() {
        String uploadDirInternal = Globals.INTERNAL_DIRECTORY_PATH;

        if (Globals.IS_DEBUG) {
            Log.d(TAG, "Files in internal storage for " + uploadDirInternal);
            FileHelper.printFilesRecursive(TAG, new File(uploadDirInternal));
        }
    }

    public static void listFilesExternalStorage() {
        String uploadDirExternal = Globals.APP_EXTERNAL_DIRECTORY_PATH;

        if (Globals.IS_DEBUG) {
            Log.d(TAG, "Files in external storage for " + uploadDirExternal);
            FileHelper.printFilesRecursive(TAG, new File(uploadDirExternal));
        }
    }

    public static void zipJSONSExternalBackups(Context aContext) {
        File backupDirExternal = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.BACKUP_DIRECTORY);
        Zipper.zipThenZipJSONFiles(aContext, backupDirExternal, Globals.MIN_ZIP_OF_ZIP_JSON_FILES, Globals.MAX_ZIP_OF_ZIP_JSON_FILES);
    }

    public static void zipJSONSExternalUploads(Context aContext) {
        File uploadDirExternal = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY);
        Zipper.zipThenZipJSONFiles(aContext, uploadDirExternal, Globals.MIN_ZIP_OF_ZIP_JSON_FILES, Globals.MAX_ZIP_OF_ZIP_JSON_FILES);
    }

    public static void zipJSONSInternalUploads(Context aContext) {
        File uploadDirInternal = new File(Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY);
        Zipper.zipThenZipJSONFiles(aContext, uploadDirInternal, Globals.MIN_ZIP_OF_ZIP_JSON_FILES, Globals.MAX_ZIP_OF_ZIP_JSON_FILES);
    }

    public static int deleteOldDataIntUploadsDir(Date aDate) {
        File dir = new File(Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY);
        String[] someOldFiles = FileHelper.getOldFilePathNamesDir(dir, aDate);

        if (someOldFiles == null) {
            Log.d(TAG, "No old files to delete");
            if (Globals.IS_DEBUG)
                Log.i(TAG, "No old files to delete");
            return 0;
        }

        int numDeleted = 0;

        for (String aFilePath : someOldFiles) {
            try {
                File aFile = new File(aFilePath);
                boolean isSuccess = aFile.delete();
                if (isSuccess) {
                    numDeleted++;
                    if (Globals.IS_DEBUG)
                        Log.i(TAG, "Deleted old data file: " + aFilePath);
                } else {
                    Log.e(TAG, "Unsuccessful deleting file: " + aFilePath);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e(TAG, "Error deleting old file: " + aFilePath + " " + e.toString());
            }
        }
        return numDeleted;
    }

    // public static boolean deleteQueuedRawData(Context aContext) {
    // boolean isSuccess = false;
    // isSuccess = FileUtils.deleteExternalFiles(aContext,
    // Globals.UPLOADS_DIRECTORY);
    // if (!isSuccess) {
    // Log.e(TAG, "Could not delete external files: " +
    // Globals.UPLOADS_DIRECTORY);
    // }
    //
    // return isSuccess;
    // }


//	public static void copyAllIntFilesToExt()
//	{
//		String appDirInternal = Globals.APP_INTERNAL_DIRECTORY_PATH;
//		String appDirExternal = Globals.APP_EXTERNAL_DIRECTORY_PATH;
//
//		Log.d(TAG, "Copying all files from internal (" + appDirInternal + ") to external (" + appDirExternal + ")");
//		FileHelper.copyAllFiles(new File(appDirInternal), new File(appDirExternal), TAG);
//	}

    /**
     * Get a String array of all files in the internal upload directory
     *
     * @return String array of filenames in internal upload directory
     */
    public static String[] getFileNamesIntUploadDir() {
        String uploadDirInternal = Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
        return FileHelper.getFilePathsDir(new File(uploadDirInternal));
    }

    /**
     * Get a String array of all files in the external upload directory
     *
     * @return String array of file names in external upload directory
     */
    public static String[] getFileNamesExtUploadDir() {
        String uploadDirExternal = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
        return FileHelper.getFilePathsDir(new File(uploadDirExternal));
    }

//	/**
//	 * Get a String array of all files in the internal upload directory
//	 *
//	 * @return String array of Files in internal upload directory
//	 */
//	private static File[] getFilesIntUploadDir() {
//		String uploadDirInternal = Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
//		return FileHelper.getFilesDir(new File(uploadDirInternal));
//	}

//	/**
//	 * Get a String array of all files in the external upload directory
//	 *
//	 * @return String array of Files in external upload directory
//	 */
//	private static File[] getFilesExtUploadDir() {
//		String uploadDirExternal = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
//		return FileHelper.getFilesDir(new File(uploadDirExternal));
//	}

    public static String[] getJSONFileNamesIntUploadDir() {
        String[] someFiles = getFileNamesIntUploadDir();
        someFiles = getJSONOnly(someFiles);
        return someFiles;
    }

    public static String[] getNonJSONFileNamesIntUploadDir() {
        String[] someFiles = getFileNamesIntUploadDir();
        someFiles = getNonJSONOnly(someFiles);
        return someFiles;
    }

//	public static File[] getJSONFilesIntUploadDir() {
//		File[] someFiles = getFilesIntUploadDir();
//		someFiles = getJSONOnly(someFiles);
//		return someFiles;
//	}

//	public static File[] getNonJSONFilesIntUploadDir() {
//		File[] someFiles = getFilesIntUploadDir();
//		someFiles = getNonJSONOnly(someFiles);
//		return someFiles;
//	}

    public static String[] getJSONFileNamesExtUploadDir() {
        String[] someFiles = getFileNamesExtUploadDir();
        someFiles = getJSONOnly(someFiles);
        return someFiles;
    }

    public static String[] getNonJSONFileNamesExtDir() {
        String[] someFiles = getFileNamesExtUploadDir();
        someFiles = getNonJSONOnly(someFiles);
        return someFiles;
    }

//	public static File[] getNonJSONFilesExtDir() {
//		File[] someFiles = getFilesExtUploadDir();
//		someFiles = getNonJSONOnly(someFiles);
//		return someFiles;
//	}

//    /**
//     * Get a specific number of JSON files
//     * @param aNumberToGet Number of files to grab
//     * @return Array of Files
//     */
//	public static File[] getNumJsonFiles(int aNumberToGet) {
//		File[] jsons = getFilesIntUploadDir();
//		ArrayList<File> someJSONFiles = new ArrayList<File>();
//		String s;
//		if (jsons != null) {
//			for (File f : jsons) {
//				if (someJSONFiles.size() < 100) {
//					s = f.getAbsolutePath();
//					if (s.endsWith(".json.zip") || (s.endsWith(".json")))
//						someJSONFiles.add(f);
//				} else {
//					break;
//				}
//			}
//		}
//		return someJSONFiles.toArray(new File[someJSONFiles.size()]);
//
//	}

    /**
     * Filter a list of filepath strings and return only those that are json
     * files or zipped json files (Based on looking for .json or .json.zip
     * extensions)
     *
     * @param someFiles String array of file names
     * @return String array of JSON file names
     */
    public static String[] getJSONOnly(String[] someFiles) {
        if (someFiles == null)
            return null;
        ArrayList<String> someJSONFiles = new ArrayList<String>();
        for (String s : someFiles) {
            if (s.endsWith(".json.zip") || (s.endsWith(".json")))
                someJSONFiles.add(s);
        }
        if (someJSONFiles.size() > 0) {
            String[] result = new String[someJSONFiles.size()];
            for (int i = 0; i < someJSONFiles.size(); i++)
                result[i] = someJSONFiles.get(i);
            return result;
        } else
            return null;
    }

    /**
     * Filter a list of filepath strings and return only those that are NOT json
     * files or zipped json files (i.e. data files) (Based on looking for files
     * without .json or .json.zip extensions)
     *
     * @param someFiles String array of file names
     * @return String array of file names of non-JSON files
     */
    private static String[] getNonJSONOnly(String[] someFiles) {
        if (someFiles == null)
            return null;
        ArrayList<String> someDataFiles = new ArrayList<String>();
        for (String s : someFiles) {
            if (!(s.endsWith(".json.zip") || (s.endsWith(".json"))))
                someDataFiles.add(s);
        }
        if (someDataFiles.size() > 0) {
            String[] result = new String[someDataFiles.size()];
            for (int i = 0; i < someDataFiles.size(); i++)
                result[i] = someDataFiles.get(i);
            return result;
        } else
            return null;
    }

//	/**
//	 * Filter a list of filepath strings and return only those that are json
//	 * files or zipped json files (Based on looking for .json or .json.zip
//	 * extensions)
//	 *
//	 * @param someFiles File array of Files
//	 * @return File array of JSON Files
//	 */
//	private static File[] getJSONOnly(File[] someFiles) {
//		if (someFiles == null)
//			return null;
//		ArrayList<File> someJSONFiles = new ArrayList<File>();
//		String s;
//		for (File f : someFiles) {
//			s = f.getAbsolutePath();
//			if (s.endsWith(".json.zip") || (s.endsWith(".json")))
//				someJSONFiles.add(f);
//		}
//		if (someJSONFiles.size() > 0) {
//			File[] result = new File[someJSONFiles.size()];
//			for (int i = 0; i < someJSONFiles.size(); i++)
//				result[i] = someJSONFiles.get(i);
//			return result;
//		} else
//			return null;
//	}

//	/**
//	 * Filter a list of filepath strings and return only those that are NOT json
//	 * files or zipped json files (i.e., data files) (Based on looking for files
//	 * without .json or .json.zip extensions)
//	 *
//	 * @param someFiles File array of Files
//	 * @return File array of non-JSON Files
//	 */
//	private static File[] getNonJSONOnly(File[] someFiles) {
//		if (someFiles == null)
//			return null;
//		ArrayList<File> someDataFiles = new ArrayList<File>();
//		String s;
//		for (File f : someFiles) {
//			s = f.getAbsolutePath();
//			if (!(s.endsWith(".json.zip") || (s.endsWith(".json"))))
//				someDataFiles.add(f);
//		}
//		if (someDataFiles.size() > 0) {
//			File[] result = new File[someDataFiles.size()];
//			for (int i = 0; i < someDataFiles.size(); i++)
//				result[i] = someDataFiles.get(i);
//			return result;
//		} else
//			return null;
//	}

    //TODO Implement
//	public static boolean combineJSONFiles(File[] someJSONFiles, File aDestDir, boolean isRemove) {
//		if (someJSONFiles == null) {
//			// TODO change to warning?
//			Log.e(TAG, "Warning: No JSON files to combine.");
//			return true;
//		}
//
//		Log.e(TAG,  "THIS SHOULD NOT BE RUNNING YET");
//		// Name the destination zip of jsons starting with the first file
//		File destZipFile = new File(aDestDir, someJSONFiles[0].getName() + ".json2.zip");
//		// return Zipper.zipFolder(Globals.EXTERNAL_DIRECTORY_PATH +
//		// File.separator + Globals.UPLOADS_DIRECTORY,
//		// Globals.EXTERNAL_DIRECTORY_PATH + File.separator +
//		// Globals.UPLOADS_DIRECTORY + File.separator + "uploads.json.zip");
//
//		return Zipper.zipFiles(someJSONFiles, Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY, Globals.EXTERNAL_DIRECTORY_PATH
//				+ File.separator + Globals.UPLOADS_DIRECTORY + File.separator + "uploads.json.zip");
//	}

//	public static boolean combineJSONFilesExternal(boolean isRemove) {
//		File[] someFiles = getFilesExtUploadDir();
//		someFiles = getJSONOnly(someFiles);
//
//		if (someFiles == null) {
//			// TODO change to warning?
//			Log.e(TAG, "Warning: No JSON files to combine on external uploads dir.");
//			return true;
//		}
//
//		Log.e(TAG, "THIS SHOULD NOT BE RUNNING YET");
//
//		File destDir = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY);
//		return combineJSONFiles(someFiles, destDir, isRemove);
//	}
}