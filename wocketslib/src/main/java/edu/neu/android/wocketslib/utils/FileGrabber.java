package edu.neu.android.wocketslib.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * <p>
 * This class is used to grab remote file from the server. 
 * </p>
 * If the user wants to download a file from a http url, then he/she should
 * use this class.
 * 
 * @author bigbug
 *
 */
public class FileGrabber {
	private static final String TAG = "FileGrabber";			
	
	public static final String ERROR_PREFIX = "error: ";
	public static final String IO_ERROR = ERROR_PREFIX + "I/O"; 
	
	private static final int BUF_SIZE = 8192;
	
	public FileGrabber() {}
	
	/**
	 * <p>
	 * Download one file from the URL address and saves it to the local phone.	 
	 * </p>
	 * 
	 * @param remoteFilePath
	 * 			  is the remote file path
	 * @param localFilePath
	 * 			  is the local file path
	 * @param isOverwrite
	 *            is to indicate whether the file should be download if the local file 
	 *            with the same name does exist.
	 * @return the name of file download if successful, otherwise some error message.
	 */
	public String downloadFile(String remoteFilePath, String localFilePath, boolean isOverwrite) {
		
		String result = localFilePath;
		
		File file = new File(localFilePath);
		if (file.exists() && !isOverwrite) {
			return localFilePath;		
		}
		
		File dir = file.getParentFile();
		if (!(dir.mkdirs() || dir.isDirectory())) {
			Log.d(TAG, IO_ERROR);
			return IO_ERROR;
		}

		try {
			URL url = new URL(remoteFilePath);					
			URLConnection ucon = url.openConnection();			
								
			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, BUF_SIZE << 1);
			FileOutputStream fos = new FileOutputStream(localFilePath);

			byte[] buffer = new byte[BUF_SIZE];
			int read = 0;
			while ((read = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, read);
			}			
			fos.close();

		} catch (IOException e) {
			Log.e("FileGrabberService", "Error: " + e.toString());
			result = ERROR_PREFIX + e.getMessage().toLowerCase(Locale.US);
		}
		
		return result;
	}
}
