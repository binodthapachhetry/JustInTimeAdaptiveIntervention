package edu.neu.android.wocketslib.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.os.Environment;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import edu.neu.android.wocketslib.Globals;

public class FileHelper {
	private static final String TAG = "FileHelper";

	private static final String ERR_SD_MISSING_MSG = "The mHealth study software cannot see your SD card. Please reinstall it and do not remove it.";
	private static final String ERR_SD_UNREADABLE_MSG = "The mHealth study software cannot read your SD (memory) card. This is probably because your phone is plugged into your computer. Please unplug it and try again.";
	private static final String ERR_INT_MISSING_MSG = "The mHealth study software cannot access internal storage.";
	private static final String ERR_INT_UNREADABLE_MSG = "The mHealth study software cannot read your internal storage to save data. This may be because your phone has run out of memory.";

	public static void testFunction(Context aContext) {
		Log.d(TAG, "These must match:");
		Log.d(TAG, "External memory path: " + getExternalMemoryPathName());
		Log.d(TAG, "Internal memory path: " + getInternalMemoryPathName(aContext));

		Log.d(TAG, "External memory path: " + Globals.EXTERNAL_DIRECTORY_PATH);
		Log.d(TAG, "Internal memory path: " + Globals.INTERNAL_DIRECTORY_PATH);

		try {
			Log.d(TAG, "External ready? " + isExternalMemoryPathReady());
		} catch (WOCKETSException e) {
			Log.d(TAG, "External ready ERROR: " + e.toString());
			e.printStackTrace();
		}

		try {
			Log.d(TAG, "Internal ready? " + isInternalMemoryPathReady(aContext));
		} catch (WOCKETSException e) {
			Log.d(TAG, "Internal ready ERROR: " + e.toString());
			e.printStackTrace();
		}
	}

	public static void tryClose(Closeable c, String aTAG) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				Log.e(aTAG, "Error closing file: " + e.toString());
			}
		}
	}

	public static List<String> listDirectory(File directory) throws IOException {

		Stack<String> stack = new Stack<String>();
		List<String> list = new ArrayList<String>();

		// If it's a file, just return itself
		if (directory.isFile()) {
			if (directory.canRead())
				list.add(directory.getName());
			return list;
		}
		// Traverse the directory in width-first manner, no-recursively
		String root = directory.getParent();
		stack.push(directory.getName());
		while (!stack.empty()) {
			String current = (String) stack.pop();
			File curDir = new File(root, current);
			String[] fileList = curDir.list();
			if (fileList != null) {
				for (String entry : fileList) {
					File f = new File(curDir, entry);
					if (f.isFile()) {
						if (f.canRead()) {
							list.add(current + File.separator + entry);
						} else {
							System.err.println("File " + f.getPath() + " is unreadable");
							throw new IOException("Can't read file: " + f.getPath());
						}
					} else if (f.isDirectory()) {
						list.add(current + File.separator + entry);
						stack.push(current + File.separator + f.getName());
					} else {
						throw new IOException("Unknown entry: " + f.getPath());
					}
				}
			}
		}
		return list;
	}

	public static List<String> listDirectory(File directory, String extension) throws IOException {

		Stack<String> stack = new Stack<String>();
		List<String> list = new ArrayList<String>();

		// If it's a file, just return itself
		if (directory.isFile() && directory.getName().endsWith(extension)) {
			if (directory.canRead())
				list.add(directory.getName());
			return list;
		}
		// Traverse the directory in width-first manner, no-recursively
		String root = directory.getParent();
		stack.push(directory.getName());
		while (!stack.empty()) {
			String current = (String) stack.pop();
			File curDir = new File(root, current);
			String[] fileList = curDir.list();
			if (fileList != null) {
				for (String entry : fileList) {
					File f = new File(curDir, entry);
					if (f.isFile() &&  f.getName().endsWith(extension)) {
						if (f.canRead()) {
							list.add(current + File.separator + entry);
						}
					} else if (f.isDirectory()) {
						stack.push(current + File.separator + f.getName());
					}
				}
			}
		}
		return list;
	}

	public static List<String> listDirectory(File directory, FilenameFilter filter) throws IOException {

		Stack<String> stack = new Stack<String>();
		List<String> list = new ArrayList<String>();


		// If it's a file, just return itself
		if (directory.isFile() && filter.accept(directory.getParentFile(), directory.getName())) {
			if (directory.canRead())
				list.add(directory.getName());
			return list;
		}
		// Traverse the directory in width-first manner, no-recursively
		String root = directory.getParent();
		stack.push(directory.getName());
		while (!stack.empty()) {
			String current = (String) stack.pop();
			File curDir = new File(root, current);
			String[] fileList = curDir.list(filter);
			if (fileList != null) {
				for (String entry : fileList) {
					File f = new File(curDir, entry);
					if (f.isFile()) {
						if (f.canRead()) {
							list.add(current + File.separator + entry);
						}
					} else if (f.isDirectory()) {
						stack.push(current + File.separator + f.getName());
					}
				}
			}
		}
		return list;
	}

	// this method returns the checksum for the input file
	// It calculates the MD5 hash using the native java algorithm for MD5
	public static String getMD5ForFile(String filename) {
		String md5 = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			File f = new File(filename);
			InputStream is = new FileInputStream(f);
			byte[] buffer = new byte[8192];
			int read = 0;
			try {
				while ((read = is.read(buffer)) > 0) {
					digest.update(buffer, 0, read);
				}
				byte[] md5sum = digest.digest();
				BigInteger bigInt = new BigInteger(1, md5sum);
				md5 = bigInt.toString(16);
				System.out.println("MD5: " + md5);
				while (md5.length() < 32) { //40 for SHA-1
					md5 = "0" + md5;
				}

			} catch (IOException e) {
				Log.e(TAG, "Unable to process file for MD5: " + filename);
				throw new RuntimeException("Unable to process file for MD5", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, "Unable to close input stream for MD5 calculation: " + filename);
					throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error in getMD5ForFile: " + filename + " " + ex.toString());
			ex.printStackTrace();
		}

		return md5;
	}

	public static File getExternalMemoryPath() {
		return Environment.getExternalStorageDirectory();
	}

	public static String getExternalMemoryPathName() {
		return getExternalMemoryPath().getAbsolutePath();
	}

	public static File getInternalMemoryPath(Context aContext) {
		return aContext.getFilesDir();
	}

	public static String getInternalMemoryPathName(Context aContext) {
		return aContext.getFilesDir().getAbsolutePath();
	}

	public static boolean isExternalMemoryPathReady() throws WOCKETSException {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED))
			throw new WOCKETSException(TAG, ERR_SD_MISSING_MSG);
		else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			throw new WOCKETSException(TAG, ERR_SD_UNREADABLE_MSG);

		File sdCard = Environment.getExternalStorageDirectory();
		if (!sdCard.exists())
			throw new WOCKETSException(TAG, ERR_SD_MISSING_MSG);
		if (!sdCard.canRead())
			throw new WOCKETSException(TAG, ERR_SD_UNREADABLE_MSG);
		return true;
	}

	public static Boolean isInternalMemoryPathReady(Context aContext) throws WOCKETSException {
		File aFile = aContext.getFilesDir();

		if (!aFile.exists())
			throw new WOCKETSException(TAG, ERR_INT_MISSING_MSG);
		if (!aFile.canRead())
			throw new WOCKETSException(TAG, ERR_INT_UNREADABLE_MSG);
		return true;
	}

	// public static void createInternalDir(Context c, String path) throws
	// WOCKETSException {
	// File dir = new File(c.getFilesDir() + "/" + path);
	// if (dir.exists())
	// return;
	// if (!dir.mkdirs())
	// throw new WOCKETSException("Error in creating internal directory " +
	// path);
	// }

	public static void createDir(File aDir) throws WOCKETSException {
		if (!(aDir.exists())) {
			try {
				aDir.mkdirs();
			} catch (Exception e) {
				throw new WOCKETSException(TAG, String.format("Can't create %1$s directory: ", aDir));
			}
		}
	}

	public static void createDir(String aDirPathName) throws WOCKETSException {
		File dir = new File(aDirPathName);
		createDir(dir);
	}

	public static File createDirsIfDontExist(File aFile) throws WOCKETSException {
		File dir = aFile.getParentFile();
		createDir(dir);
		return dir;
	}

	public static File createDirsIfDontExist(String aFilePathName) throws WOCKETSException {
		File aFile = new File(aFilePathName);
		File dir = aFile.getParentFile();
		createDir(dir);
		return dir;
	}

	public static boolean isFileExists(File aFile) {
		if (!aFile.exists())
			return false;
		return true;
	}

	public static boolean isFileExists(String aPathName) {
		File aFile = new File(aPathName);
		return isFileExists(aFile);
	}

	// public static boolean internalFileExists(Context c, String path) {
	// File file = new File(c.getFilesDir() + "/" + path);
	// if (file.exists())
	// return true;
	// return false;
	// }

	// File file = new File(sdCard.getAbsolutePath() + "/" + path);
	// if (file.exists())
	// return true;
	// return false;
	// }

	// public static OutputStream openFileForAppending(String fileName, String
	// fileDesc) throws WOCKETSException {
	// File sdCard = new File("/sdcard/");
	// if (!sdCard.exists())
	// throw new WOCKETSException("SD card not installed");
	// if (!sdCard.canWrite())
	// throw new WOCKETSException("Cannot write to SD card");
	//
	// File file = new File(sdCard.getAbsolutePath() + "/" + fileName);
	// if (file.exists() && !file.canWrite())
	// throw new WOCKETSException("Cannot write " + fileDesc.toLowerCase() +
	// " file to SD card");
	//
	// try {
	// return new FileOutputStream(file, true);
	// } catch (FileNotFoundException e) {
	// throw new WOCKETSException("Error in opening " + fileDesc.toLowerCase() +
	// " file from SD card");
	// }
	// }
	//
	// public static InputStream openInternalFileForRead(Context c, String
	// fileName, String fileDesc,
	// boolean exceptionIfFileNotFound) throws WOCKETSException {
	//
	// File file = new File(c.getFilesDir() + "/" + fileName);
	// if (!file.exists()) {
	// if (exceptionIfFileNotFound)
	// throw new WOCKETSException(fileDesc +
	// " file not found in internal storage");
	// return null;
	// }
	// if (!file.canRead()) {
	// if (exceptionIfFileNotFound)
	// throw new WOCKETSException("Cannot read " + fileDesc.toLowerCase() +
	// " file from internal storage");
	// return null;
	// }
	//
	// try {
	// return new FileInputStream(file);
	// } catch (FileNotFoundException e) {
	// throw new WOCKETSException("Error in opening " + fileDesc.toLowerCase() +
	// " file from internal storage");
	// }
	// }
	//
	// public static InputStream openFileForRead(String fileName, String
	// fileDesc, boolean exceptionIfFileNotFound)
	// throws WOCKETSException {
	//
	// File sdCard = getSDCard();
	//
	// File file = new File(sdCard.getAbsolutePath() + "/" + fileName);
	// if (!file.exists()) {
	// if (exceptionIfFileNotFound)
	// throw new WOCKETSException(fileDesc + " file not found in SD card");
	// return null;
	// }
	// if (!file.canRead()) {
	// if (exceptionIfFileNotFound)
	// throw new WOCKETSException("Cannot read " + fileDesc.toLowerCase() +
	// " file from SD card");
	// return null;
	// }
	//
	// try {
	// return new FileInputStream(file);
	// } catch (FileNotFoundException e) {
	// throw new WOCKETSException("Error in opening " + fileDesc.toLowerCase() +
	// " file from SD card");
	// }
	// }
	//

	//
	// public static void writeToInternalFile(Context context, String
	// sourceFile, String destinationFile, boolean isAppend) {
	// InputStream myInput;
	// try {
	// myInput = new FileInputStream(FileHelper.getSDCard().getAbsolutePath() +
	// "/" + sourceFile);
	// OutputStream myOutput = new BufferedOutputStream(new
	// FileOutputStream(destinationFile, isAppend));
	// byte[] buffer = new byte[8192];
	// int length;
	// while ((length = myInput.read(buffer)) > 0) {
	// myOutput.write(buffer, 0, length);
	// }
	// myOutput.flush();
	// myInput.close();
	// myOutput.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (WOCKETSException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void appendToInternalFile(Context c, String data, String
	// fileName, String fileDesc)
	// throws WOCKETSException {
	// OutputStream os = null;
	// try {
	// os = new BufferedOutputStream(new FileOutputStream(c.getFilesDir() + "/"
	// + fileName, true));
	// os.write(data.getBytes());
	// } catch (IOException e) {
	// throw new WOCKETSException("Error in writing " + fileDesc.toLowerCase() +
	// " file to internal storage");
	// } finally {
	// try {
	// os.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	//

	public static OutputStream openFileForWriting(String aFilePathName, boolean append) throws WOCKETSException {
		File file = new File(aFilePathName);
		if (file.exists() && !file.canWrite())
			throw new WOCKETSException(TAG, "Cannot write: " + aFilePathName);

		try {
			return new FileOutputStream(file, append);
		} catch (FileNotFoundException e) {
			throw new WOCKETSException(TAG, "Error in opening: " + aFilePathName);
		}
	}

	public static void appendToFile(String data, String aFilePathName) throws WOCKETSException {
		OutputStream os = openFileForWriting(aFilePathName, true);
		try {
			os.write(data.getBytes());
		} catch (IOException e) {
			throw new WOCKETSException(TAG, "Error in writing to: " + aFilePathName, e);
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				throw new WOCKETSException(TAG, "Error in closing: " + aFilePathName, e);
			}
		}
	}

	//
	// public static void overwriteFile(String data, String fileName, String
	// fileDesc) throws WOCKETSException {
	// OutputStream os = openFileForWriting(fileName, fileDesc, false);
	// try {
	// os.write(data.getBytes());
	// } catch (IOException e) {
	// throw new WOCKETSException("Error in writing " + fileDesc.toLowerCase() +
	// " file to SD card");
	// } finally {
	// try {
	// os.close();
	// } catch (IOException e) {
	// }
	// }
	// }

	public static void printFilesRecursive(String TAG, File dir) {
		Stack<File> stack = new Stack<File>();
		stack.push(dir);
		while (!stack.isEmpty()) {
			File child = stack.pop();
			if (child.isDirectory()) {
				for (File f : child.listFiles())
					stack.push(f);
			} else if (child.isFile()) {
				Log.d(TAG, "File: " + child.getPath());
			}
		}
	}

	public static File[] getFilesDir(File aDir) {
		if (aDir.isDirectory()) {
			File[] someFiles = aDir.listFiles();
			if (someFiles != null)
				return someFiles;
			else
				return null;
		} else {
			Log.e(TAG, "Cannot read files because not a directory: " + aDir.getAbsolutePath());
		}
		return null;
	}

	public static File[] getFilesDir(String aDirFilePath) {
		return getFilesDir(new File(aDirFilePath));
	}

	public static ArrayList<File> getRecusiveDirs(File aDir, int recursiveLevel){
		ArrayList<File> result = new ArrayList<File>();
		if(recursiveLevel >= 0) {
			for (File entry : aDir.listFiles()) {
				if (entry.isDirectory()) {
					result.add(entry);
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

	public static String[] getFilePathsDir(File aDir) {
		File[] someFiles = getFilesDir(aDir);
		if (someFiles == null)
			return null;

		String[] someFileNames = new String[someFiles.length];
		for (int i = 0; i < someFiles.length; i++) {
			someFileNames[i] = someFiles[i].getAbsolutePath();
		}
		return someFileNames;
	}

	public static String[] getFilePathsDir(String aDirFilePathName) {
		File dir = new File(aDirFilePathName);
		return getFilePathsDir(dir);
	}

	public static File[] getOldFiles(File[] someFiles, Date beforeDate) {
		int numOldFiles = 0;
		for (int i = 0; i < someFiles.length; i++) {
			if (someFiles[i].lastModified() < beforeDate.getTime())
				numOldFiles++;
		}

		File[] someOldFiles = new File[numOldFiles];
		numOldFiles = 0;
		for (int i = 0; i < someFiles.length; i++) {
			if (someFiles[i].lastModified() < beforeDate.getTime()) {
				someOldFiles[numOldFiles] = someFiles[i];
				numOldFiles++;
			}
		}
		return someOldFiles;
	}

	public static String[] getOldFilePathNamesDir(File aDir, Date aDate) {
		if (aDir.isDirectory()) {
			File[] someFiles = getOldFiles(aDir.listFiles(), aDate);

			if (someFiles == null)
				return null;

			String[] someFileNames = new String[someFiles.length];
			for (int i = 0; i < someFiles.length; i++) {
				someFileNames[i] = someFiles[i].getAbsolutePath();
			}
			return someFileNames;
		} else
			return null;
	}

	public static String[] getOldFilePathNamessDir(String aDirPathName, Date aDate) {
		return getOldFilePathNamesDir(new File(aDirPathName), aDate);
	}

	/**
	 * Given a path make sure all subdirectories for it are created
	 *
	 * @param dirPathName
	 * @return
	 * @throws WOCKETSException
	 */
	public static File setupDirectories(String dirPathName) throws WOCKETSException {

		File dir = new File(dirPathName);

		if (!(dir.exists())) {
			try {
				dir.mkdirs();
			} catch (Exception e) {
				throw new WOCKETSException(TAG, String.format("Can't create %1$s directory: ", dir));
			}
		}
		return dir;
	}

	public static boolean copyFile(File fileOrig, File fileDest) {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			createDirsIfDontExist(fileDest);

			in = new FileInputStream(fileOrig);
			out = new FileOutputStream(fileDest);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = in.read(buf)) != -1) {
				out.write(buf, 0, i);
			}
			Log.d(TAG, "COPY File successful: " + fileOrig.getAbsolutePath() + " to " + fileDest.getAbsolutePath());
		} catch (IOException e) {
			Log.e(TAG, "Error in copyFile when copying from " + fileOrig.getAbsolutePath() + " to " + fileDest.getAbsolutePath() + " : " + e.toString());
			return false;
		} catch (WOCKETSException we) {
			Log.e(TAG, "Error creating path to destination file: " + fileDest.getAbsolutePath() + " : " + we.toString());
			return false;
		} finally{
			try{
				if(in != null){
					in.close();
				}
				if(out != null){
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static boolean copyAllFiles(File dirOrig, File dirDest, String TAG) {
		try {
			createDirsIfDontExist(dirDest);
		} catch (WOCKETSException e) {
			Log.e(TAG, "Error when creating directory in copyAllFiles. Can't create: " + dirDest.getAbsolutePath());
			e.printStackTrace();
			return false;
		}
		String origPath = dirOrig.getAbsolutePath();
		String destPath = dirDest.getAbsolutePath();
		int origPathChars = origPath.length();

		Stack<File> stack = new Stack<File>();
		stack.push(dirOrig);
		while (!stack.isEmpty()) {
			File child = stack.pop();
			if (child.isDirectory()) {
				for (File f : child.listFiles())
					stack.push(f);
			} else if (child.isFile()) {

				// Figure out the new path name
				String fname = child.getAbsolutePath().substring(origPathChars);
				String newFName = destPath + fname;
				Log.d(TAG, "Copy: " + child.getAbsolutePath() + " to " + newFName);
				if (!(copyFile(child, new File(newFName)))) {
					Log.e(TAG, "Copy file in copyAllFiles failed: " + child.getAbsolutePath() + " to " + newFName);
				}
			}
		}
		Log.d(TAG, "CopyAllFiles Files done: " + dirOrig.getAbsolutePath() + " to " + dirDest.getAbsolutePath());
		return true;
	}

	/**
	 * We need a special method for this on Android because the file.renameto
	 * method will not work across mount points.
	 *
	 * @param orig
	 * @param dest
	 * @return
	 */
	public static boolean moveFile(File orig, File dest) {
		boolean isCopied = copyFile(orig, dest);

		if (!isCopied)
			return false;

		boolean isDeleted = deleteFile(orig);

		if (!isDeleted) {
			Log.e(TAG, "Error: file in moveFile copied but not deleted. " + orig.getAbsolutePath() + " " + orig.getAbsolutePath());
			return false;
		}

		return true;
	}

	public static boolean deleteFile(File aFile) {
		// TODO
		// SecurityManager sm = new SecurityManager();
		// try
		// {
		// sm.checkDelete(orig.getAbsolutePath());
		// Log.e(TAG, "NO WORRIES .........................................");
		// } catch (Exception e)
		// {
		// Log.e(TAG, "Exception from checkDelete: " + e.toString());
		// }

		boolean isDeleted = aFile.delete();

		if (!isDeleted) {
			Log.e(TAG, "Error: could not delete file in deleteFile: " + aFile.getAbsolutePath());
			return false;
		}

		return true;
	}

	public static boolean deleteFile(String aFilePath) {
		return deleteFile(new File(aFilePath));
	}

	public static boolean deleteDir(File dir) {
		boolean isDeleted = true;
		boolean isFileDeleted = false;
		File tempFile;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				tempFile = new File(dir, children[i]);
				if (tempFile.isDirectory())
					isFileDeleted = deleteDir(tempFile);
				else
					isFileDeleted = tempFile.delete();
				if (!isFileDeleted) {
					Log.e(TAG, "DeleteDir failed to delete file or directory: " + children[i]);
					isDeleted = false;
				}
			}
		} else {
			Log.e(TAG, "DeleteDir sent a non-directory" + dir.getAbsolutePath());
			return false;
		}

		isFileDeleted = dir.delete();
		if (!isFileDeleted) {
			Log.e(TAG, "DeleteDir failed to delete top-level directory.");
		}

		return isDeleted;
	}

	public static boolean deleteDir(String aDirFileName) {
		return deleteDir(new File(aDirFileName));
	}

	/**
	 * Check if a full filePath string suggests it is in internal or external
	 * app storage and get the RELATIVE path accordingly
	 *
	 * @param aFilePath
	 * @return
	 */
	public static String getRelativePathAppStorage(Context aContext, String aFilePath) {
		if (Globals.INTERNAL_DIRECTORY_PATH.length() < 5) {
			Log.e(TAG, "Error in getRelativePath can't determine if file is internal or external: " + aFilePath);
			return "";
		}

		if (aFilePath.substring(0, 5).compareTo(Globals.INTERNAL_DIRECTORY_PATH.substring(0, 5)) == 0) {
			// An internal file
			return aFilePath.substring(Globals.INTERNAL_DIRECTORY_PATH.length() + 1);
		} else if (aFilePath.substring(0, 5).compareTo(Globals.EXTERNAL_DIRECTORY_PATH.substring(0, 5)) == 0) {
			// An external file
			return aFilePath.substring(Globals.EXTERNAL_DIRECTORY_PATH.length() + 1);
		} else {
			Log.e(TAG, "Error in getRelativePath can't determine if file is internal or external: " + aFilePath);
			return "";
		}
	}

	private static String getPathWithUnderscoresForSlashes(String aPath) {
		return aPath.replace("/", "__");
		// return replace(aPath, "/", "__");
	}

	private static String stripUploadsDirInfo(String fileName) {
		String header = Globals.APP_DIRECTORY + "__" + "uploads" + "__";
		if (fileName.length() < (header.length() + 1))
			return fileName;
		else if (fileName.startsWith(header))
			return fileName.substring(header.length());
		else
			return fileName;
	}

	public static File getUploadsExternalDirFile(Context aContext, File aFile) {
		// First figure out how to name the new file so that it can be put in
		// the uploads directory as one file but has the directory structure
		// info in the filename
		String relFileName = getPathWithUnderscoresForSlashes(getRelativePathAppStorage(aContext, aFile.getAbsolutePath()));
		String uploadsDir;

		// We want to make sure that we don't have odd file names if we move
		// files from the Internal uploads to external when the files in the
		// internal already have been encoded with __ for the / in the path. The
		// easiest way to do this is to check right here and strip off that
		// info. Not the most elegant or efficient. Would be nice to fix this
		// someday.
		relFileName = FileHelper.stripUploadsDirInfo(relFileName);

		try {
			uploadsDir = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY + File.separator;
			setupDirectories(uploadsDir);
		} catch (WOCKETSException e) {
			Log.e(TAG, "Error creating external uploads directory: " + e.toString());
			e.printStackTrace();
			return null;
		}

		return new File(uploadsDir + relFileName);
	}

	public static File getUploadsInternalDirFile(Context aContext, File aFile) {
		// First figure out how to name the new file so that it can be put in
		// the uploads directory as one file but has the directory structure
		// info in the filename
		String relFileName = getPathWithUnderscoresForSlashes(getRelativePathAppStorage(aContext, aFile.getAbsolutePath()));
		String uploadsDir;

		try {
			uploadsDir = Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY + File.separator;
			setupDirectories(uploadsDir);
		} catch (WOCKETSException e) {
			Log.e(TAG, "Error creating internal uploads directory: " + e.toString());
			e.printStackTrace();
			return null;
		}

		return new File(uploadsDir + relFileName);
	}

	/**
	 * Move a file from the internal uploads to the external uploads without
	 * renaming it (assuming it already has the correct path encoding)
	 *
	 * @param aContext
	 * @param aPath
	 * @param isCompress
	 * @param isRemove
	 * @return
	 */
	public static boolean transferToExternalUploadDirectory(Context aContext, File aPath, boolean isCompress, boolean isRemove) {
		File newZip = null;
		boolean isSuccess = true;

		if (isCompress) {
			if (aPath.isDirectory()) {
				isSuccess = Zipper.zipFolder(aPath, isRemove);
				if (!isSuccess) {
					Log.e(TAG, "Error in transferToExternalUploadDirectory when zipping the folder: " + aPath.getAbsolutePath());
					return false;
				}
			} else {
				isSuccess = Zipper.zipFile(aPath, isRemove);
				if (!isSuccess) {
					Log.e(TAG, "Error in transferToExternalUploadDirectory when zipping the file: " + aPath.getAbsolutePath());
					return false;
				}
			}

			if (aPath.getName().endsWith(".zip"))
				newZip = aPath;
			else
				newZip = new File(aPath + ".zip");
		} else
			// No compression so do nothing
			newZip = new File(aPath.getAbsolutePath());

		File destination = getUploadsExternalDirFile(aContext, newZip);

		isSuccess = moveFile(newZip, destination);

		if (!isSuccess) {
			Log.e(TAG, "Error in saveToExternal UploadDirectory when moving file " + newZip.getAbsolutePath() + " to " + destination.getAbsolutePath());
			return false;
		}

		return true;
	}

	/**
	 * Save a file to the external upload directory
	 *
	 * @param aContext
	 * @param aPath
	 * @param isCompress
	 * @param isRemove
	 * @return
	 */
	public static boolean moveToExternalUploadDirectory(Context aContext, File aPath, boolean isCompress, boolean isRemove) {
		File newZip = null;
		boolean isSuccess = true;

		if(isCompress){
			if (aPath.getName().endsWith(".zip"))
				newZip = aPath;
			else
				newZip = new File(aPath + ".zip");
		}else{
			newZip = new File(aPath.getAbsolutePath());
		}

		File destination = getUploadsExternalDirFile(aContext, newZip);

		if(destination.exists()){
			// TODO check if there is another file already exists (due to delay of watch file, it's possible that previous day's raw data has been zipped and moved before a complete list of watch data of previous day are received, which will cause lose of data if not solving the problem). Just add new files to this existing zip file
			if(isCompress){
				try {
					Log.i(TAG, "Zip file: " + destination.getName() + " already exists, adding new files to it");

					ZipFile existingZip = new ZipFile(destination);
					ZipParameters paras = new ZipParameters();
					paras.setCompressionMethod(Zip4jConstants.COMP_STORE);
					existingZip.addFolder(aPath, paras);
					Log.i(TAG, "Added " + aPath.getAbsolutePath());
					if(isRemove){
						if(deleteDir(aPath)){
							Log.i(TAG, "Deleted " + aPath.getAbsolutePath());
						};
					}
				} catch (ZipException e) {
					e.printStackTrace();
					Log.e(TAG, "Skip zipping and deleting: " + aPath.getAbsolutePath());
					Log.e(TAG, e.getMessage());
				}
			}else{
				moveFile(newZip, destination);
			}

		}else {
			if (isCompress) {
				if (aPath.isDirectory()) {
					isSuccess = Zipper.zipFolder(aPath, isRemove);
					if (!isSuccess) {
						Log.e(TAG, "Error in moveToExternalUploadDirectory when zipping the folder: " + aPath.getAbsolutePath());
						return false;
					}
				} else {
					isSuccess = Zipper.zipFile(aPath, isRemove);
					if (!isSuccess) {
						Log.e(TAG, "Error in moveToExternalUploadDirectory when zipping the file: " + aPath.getAbsolutePath());
						return false;
					}
				}
			}

			isSuccess = moveFile(newZip, destination);

			if (!isSuccess) {
				Log.e(TAG, "Error in saveToExternal UploadDirectory when moving file " + newZip.getAbsolutePath() + " to " + destination.getAbsolutePath());
				return false;
			}
		}

		return true;
	}

	/**
	 * Save a file to the internal upload directory
	 *
	 * @param aContext
	 * @param aPath
	 * @param isCompress
	 * @param isRemove
	 * @return
	 */
	public static boolean moveToInternalUploadDirectory(Context aContext, File aPath, boolean isCompress, boolean isRemove) {
		File newZip = null;
		boolean isSuccess = true;

		if (isCompress) {
			if (aPath.isDirectory()) {
				isSuccess = Zipper.zipFolder(aPath, isRemove);
				if (!isSuccess) {
					Log.e(TAG, "Error in moveToInternalUploadDirectory when zipping the folder: " + aPath.getAbsolutePath());
					return false;
				}
			} else {
				isSuccess = Zipper.zipFile(aPath, isRemove);
				if (!isSuccess) {
					Log.e(TAG, "Error in moveToInternalUploadDirectory when zipping the file: " + aPath.getAbsolutePath());
					return false;
				}
			}

			if (aPath.getName().endsWith(".zip"))
				newZip = aPath;
			else
				newZip = new File(aPath + ".zip");
		} else
			// No compression so do nothing
			newZip = new File(aPath.getAbsolutePath());

		File destination = getUploadsInternalDirFile(aContext, newZip);

		isSuccess = moveFile(newZip, destination);

		if (!isSuccess) {
			Log.e(TAG, "Error in saveToInternal UploadDirectory when moving file " + newZip.getAbsolutePath() + " to " + destination.getAbsolutePath());
			return false;
		}

		return true;
	}

	public static boolean saveStringToFile(String aMsg, File aPath, boolean isAppend) {
		boolean result = false;

		try {
			createDirsIfDontExist(aPath);
		} catch (WOCKETSException e1) {
			Log.e(TAG, "Could not create subdirs needed to save StringToFile: " + e1.toString());
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(aPath, isAppend);
			try {
				fos.write(aMsg.getBytes());
				result = true;
			} catch (IOException e) {
				Log.e(TAG, "SaveStringToFile: problem writing: " + aPath);
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "SaveStringToFile: problem creating: " + aPath);
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					Log.e(TAG, "SaveStringToFile: problem closing: " + aPath);
					e.printStackTrace();
				}
		}
		return result;
	}

	public static boolean saveStringToFile(String aMsg, String aDirectoryPath, String aFileName, boolean isAppend) {
		File mypath = new File(aDirectoryPath, aFileName);
		return saveStringToFile(aMsg, mypath,isAppend);
	}

	public static String readStringFromFile(File aFile) {
		String result = null;
		FileInputStream fir = null;
		BufferedReader br = null;
		try {
			fir = new FileInputStream(aFile);
			InputStreamReader in = new InputStreamReader(fir);
			br = new BufferedReader(in);
			try {
				result = br.readLine();
			} catch (IOException e) {
				Log.e(TAG, "readStringInternal: problem reading: " + aFile.getAbsolutePath());
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "readStringInternal: cannot find: " + aFile.getAbsolutePath());
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					Log.e(TAG, "readStringInternal: cannot close: " + aFile.getAbsolutePath());
					e.printStackTrace();
				}
			if (fir != null)
				try {
					fir.close();
				} catch (IOException e) {
					Log.e(TAG, "readStringInternal: cannot close: " + aFile.getAbsolutePath());
					e.printStackTrace();
				}
		}
		return result;
	}

	public static String readStringFromFile(String aDirectoryPath, String aFileName) {
		File mypath = new File(aDirectoryPath, aFileName);
		return readStringFromFile(mypath);
	}

	public static String readStringFromFile(String aFilePath) {
		return readStringFromFile(new File(aFilePath));
	}

	// public static boolean deleteInternalFiles(String directoryPath) {
	// File dir = new File(directoryPath);
	//
	// return deleteDir(dir);
	// }
	public static File[] findFiles(String dirStr, final String pattern){
		File dir = new File(dirStr);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(pattern);
			}
		});
		return files;
	}
}