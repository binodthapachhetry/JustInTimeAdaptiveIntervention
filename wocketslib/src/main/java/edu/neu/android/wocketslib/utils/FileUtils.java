package edu.neu.android.wocketslib.utils;

public class FileUtils {
//	private static final String TAG = "FileUtils";
	
//	public static File getExtMemoryPathFile() throws WOCKETSException {
//		String aPath = getExtMemoryPath();
//
//		if (aPath != null)
//			return new File(aPath);
//		else
//			return null;
//	}
//
//	/**
//	 * 
//	 * @param aContext
//	 * @return String path (no ending slash) 
//	 */
//	public static File getIntMemoryPathFile(Context aContext)
//	{
//		File dir = aContext.getDir("", Context.MODE_PRIVATE);
//		return dir; 
//	}

//	/**
//	 * 
//	 * @param aContext
//	 * @return String path (no ending slash) 
//	 */
//	public static String getIntMemoryPath(Context aContext)
//	{		
//		File dir = getIntMemoryPathFile(aContext);
//		String fullPath = dir.getAbsolutePath(); 
//		return fullPath; 
//	}


//	public static String[] getFilesExtUploadDir()
//	{
//		try {
//			String uploadDirExternal = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
//			return getFilePathsDir(new File(uploadDirExternal));
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error in getFilesExtUploadDir getting external memory path: " + e.toString());
//			e.printStackTrace();
//			return null; 
//		} 
//	}

//	public static String[] getOldFilesExtUploadDir(Date aDate)
//	{
//		try {
//			String uploadDirExternal = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
//			return getOldFilePathsDir(new File(uploadDirExternal), aDate);
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error in getOldFilesExtUploadDir getting external memory path: " + e.toString());
//			e.printStackTrace();
//			return null; 
//		} 
//	}
	
//	public static String[] getOldFilesExtUploadDir(Date aDate)
//	{
//		String uploadDirExternal = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY;
//		return getOldFilePathsDir(new File(uploadDirExternal), aDate); 
//	}
//	
//
//	public static File getFileExternal(String aRelativeDirectoryPath, String aFileName)
//	{
//		String sdPath; 
//		try {
//			sdPath = getExtMemoryPath();
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error in saveStringExternal getting external storage directory: " + e.toString());
//			e.printStackTrace();
//			return null; 
//		}
//		String dataPath = sdPath + File.separator + aRelativeDirectoryPath;
//
//		try {
//			setupDirectories(dataPath);
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error in saveStringExternal creating directories on external storage: " + e.toString());
//			e.printStackTrace();
//			return null; 
//		}
//
//		File mypath = new File(dataPath + File.separator + aFileName);
//		return mypath; 
//	}			
//
//	/**
//	 * 
//	 * @param aMsg String message
//	 * @param aRelativeDirectoryPath String of the form "dir" + File.seperator + "dir2" + File.seperator
//	 * @param aFileName String filename (without starting File.seperator) 
//	 * @return True if successfully saved 
//	 */
//	public static boolean saveStringExternal(String aMsg, String aRelativeDirectoryPath, String aFileName)
//	{
//		File mypath = getFileExternal(aRelativeDirectoryPath, aFileName);
//
//		if (mypath == null)
//			return false;
//
//		return saveStringToFile(mypath, aMsg);
//	}
//
//	public static File getFileInternal(Context aContext, String aDirectoryPath, String aFileName)
//	{
//		String aPath = getIntMemoryPath(aContext);
//		String dirPath = aPath + File.separator + aDirectoryPath;
//		try {
//			setupDirectories(dirPath);
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error in saveStringInternal when setting up directories: " + e.toString());
//			e.printStackTrace();
//			return null; 
//		}
//		File mypath = new File(dirPath, aFileName); 
//		return mypath;		
//	}
//
//	public static boolean saveStringInternal(Context aContext, String aMsg, String aDirectoryPath, String aFileName)
//	{ 
//		File mypath = getFileInternal(aContext, aDirectoryPath, aFileName);
//
//		if (mypath == null)
//			return false; 
//
//		return saveStringToFile(mypath, aMsg);
//
//		//		aContext.getDir(aDirectoryPath, Context.MODE_PRIVATE);
//		//		dir.mkdirs(); 
//		//		File mypath = new File(dir, aFileName); 
//		//
//		//		return saveStringToFile(mypath, aMsg);
//	}

	//	/**
	//	 * 
	//	 * @param aContext
	//	 * @param aFilePath
	//	 * @return The path without the first slash 
	//	 */
	//	public static String getRelativePathInternal(Context aContext, String aFilePath)
	//	{
	//		String rootdir;
	//		rootdir = getIntMemoryPath(aContext);	
	//		
	//		return aFilePath.substring(rootdir.length()+1);
	//	}
	//
	//	public static String getRelativePathExternal(String aFilePath)
	//	{
	//		String rootdir;
	//		try {
	//			rootdir = getExtMemoryPath();
	//		} catch (WOCKETSException e) {
	//			Log.e(TAG, "Error in getRelativePathExternal: can't get root path: " + e.toString()); 
	//			e.printStackTrace();
	//			return ""; 
	//		}	
	//		
	//		return aFilePath.substring(rootdir.length()+1);
	//	}

//
//	public static File getUploadsInternalDirFile(Context aContext, File aFile)
//	{
//		String relFileName = getPathWithUnderscoresForSlashes(getRelativePath(aContext, aFile.getAbsolutePath()));
//		String uploadsDir = Globals.INTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY + File.separator;
//		try {
//			setupDirectories(uploadsDir);
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error creating internal uploads directory: " + e.toString());
//			e.printStackTrace();
//			return null; 
//		}
//
//		return new File (uploadsDir + relFileName);
//	}
//
//	public static boolean saveToInternalUploadDirectory(Context aContext, File aPath, boolean isRemove)
//	{
//		boolean isSuccess = true; 
//		if (aPath.isDirectory())
//		{
//			isSuccess = zipFolder(aPath, isRemove);
//			if (!isSuccess)
//			{
//				Log.e(TAG, "Error in saveToInternalUploadDirectory when zipping the folder: " + aPath.getAbsolutePath());
//				return false; 
//			}
//		}
//		else
//		{
//			isSuccess = zipFile(aPath, isRemove);
//			if (!isSuccess)
//			{
//				Log.e(TAG, "Error in saveToInternalUploadDirectory when zipping the file: " + aPath.getAbsolutePath());
//				return false; 
//			}
//		}
//
//		File newZip = new File(aPath + ".zip");
//
//		File destination = getUploadsInternalDirFile(aContext, newZip); 
//
//		isSuccess = moveFile(newZip, destination); 
//
//		if (!isSuccess)
//		{
//			Log.e(TAG, "Error in saveToInternalUploadDirectory when moving file " + newZip.getAbsolutePath() + " to " + destination.getAbsolutePath());
//			return false; 
//		}
//
//		return true; 
//	}
//
//
//
//	public static boolean saveLogDirToExternalUploadDirectory(Context aContext, File aDir, boolean isRemove)
//	{		
//		File newZip; 
//
//		boolean isSuccess = true;  
//
//		if (aDir.isDirectory())
//		{
//			isSuccess = zipFolder(aDir, isRemove);
//			if (!isSuccess)
//			{
//				Log.e(TAG, "Error in saveDirToExternalUploadDirectory when zipping the folder: " + aDir.getAbsolutePath());
//				return false; 
//			}
//			
//			File destination;
//			destination = new File(Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.UPLOADS_DIRECTORY + File.separator + "logs." + System.currentTimeMillis() +  ".zip"); 
//			
//			newZip = new File(aDir.getAbsolutePath() + ".zip");
//
//			Log.e(TAG, "DESTINATION: " + destination + " ---------------------------------------------------");
//			isSuccess = moveFile(newZip, destination); 
//
//			if (!isSuccess)
//			{
//				Log.e(TAG, "Error in saveDirToExternalUploadDirectory when moving file " + newZip.getAbsolutePath() + " to " + destination.getAbsolutePath());
//				return false; 
//			}
//
//			return true;
//		}
//		else
//		{
//			Log.e(TAG, "saveDirToExternalUploadDirectory sent a non-directory: " + aDir);
//			return false; 
//		}
//	}
//
//	public static boolean deleteStringInternal(Context aContext, String directoryName, String aFileName)
//	{
//		String aDirFileName = FileUtils.getIntMemoryPath(aContext) + File.separator + directoryName;
//		File dir = new File(aDirFileName);
//		//dir.mkdirs(); 
//		File mypath = new File(dir, aFileName);
//
//		// Make sure the file or directory exists and isn't write protected
//		if (!mypath.exists())
//		{
//			Log.e(TAG, "Delete: no such file or directory: " + mypath);
//			return false; 
//		}
//
//		if (!mypath.canWrite())
//		{
//			Log.e(TAG, "Delete: write protected: " + mypath);
//			return false; 	    	
//		}
//
//		// If it is a directory, make sure it is empty
//		if (mypath.isDirectory()) {
//			String[] files = mypath.list();
//			if (files.length > 0)
//			{
//				Log.e(TAG, "Delete: directory not empty: " + mypath);
//				return false;
//			}
//		}
//
//		// Attempt to delete it
//		boolean success = mypath.delete();
//
//		if (!success)
//		{
//			Log.e(TAG, "Delete: deletion failed");
//			return false; 
//		}
//		else
//			return true;
//	}
//
//
//	public static String[] getFileNamesExternal(Context aContext, String directoryName)
//	{
//		String aFileName = null;
//		try {
//			aFileName = FileUtils.getExtMemoryPath() + File.separator + directoryName;
//		} catch (WOCKETSException e) {
//			e.printStackTrace();
//		}
//
//		if (aFileName == null)
//			return null; 
//
//		File dir = new File(aFileName);
//		//		File dir = aContext.getDir(directoryName, Context.MODE_PRIVATE); 
//		//		dir.mkdirs();
//
//		if (dir.isDirectory())
//		{
//			String[] children = dir.list(); 
//			return children; 
//		}
//		else
//			return null; 		
//	}
//
//	public static boolean deleteExternalFiles(Context aContext, String directoryName)
//	{
//		String aFileName = null;
//		try {
//			aFileName = FileUtils.getExtMemoryPath() + File.separator + directoryName;
//		} catch (WOCKETSException e) {
//			e.printStackTrace();
//		}
//
//		if (aFileName == null)
//			return false; 
//
//		File dir = new File(aFileName);
//
//		return deleteDir(dir);
//	}
//

//	public FileUtils() {
//
//		try {
//			SDCardRoot = getExtMemoryPath();
//		} catch (WOCKETSException e) {
//			Log.e(TAG, "Error in FileUtils constructor. Can't get the external data directory." + e.toString()); 
//			e.printStackTrace();
//		} 			
//	}

//	public File createFileInSDCard(String fileName, String dir)
//	throws IOException {
//		File file = new File(SDCardRoot + dir + File.separator + fileName);
//		file.createNewFile();
//		return file;
//	}
//
//	public File creatSDDir(String dir) {
//		File dirFile = new File(SDCardRoot + dir + File.separator);
//		System.out.println(dirFile.mkdirs());
//		return dirFile;
//	}
//
//	public boolean isFileExist(String fileName, String path) {
//		File file = new File(SDCardRoot + path + File.separator + fileName);
//		return file.exists();
//	}

//	public File write2SDFromInput(String path, String fileName,
//			InputStream input) {
//
//		File file = null;
//		OutputStream output = null;
//		try {
//			creatSDDir(path);
//			file = createFileInSDCard(fileName, path);
//			output = new FileOutputStream(file);
//			byte buffer[] = new byte[4 * 1024];
//			int temp;
//			while ((temp = input.read(buffer)) != -1) {
//				output.write(buffer, 0, temp);
//			}
//			output.flush();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				output.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return file;
//	}
	
	
}