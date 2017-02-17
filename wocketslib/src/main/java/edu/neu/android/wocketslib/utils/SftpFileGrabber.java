package edu.neu.android.wocketslib.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import edu.neu.android.wocketslib.Globals;

/**
 * <p>
 * This is the tool to download file/directory from a SFTP server.
 * </p>
 * You can't use FileGrabber class to do that because that class can't deal with
 * the SFTP security authorization.
 * 
 * @author bigbug
 *
 */
public class SftpFileGrabber extends FileGrabber {
	private static final String TAG = "FileGrabber";
	
	private String mUserName;
	private String mPassword;
	private String mServerURL;
	
	/**
	 * Default constructor
	 * user name, password and server url are got from Globals
	 */
	public SftpFileGrabber() {
		this(Globals.SFTP_SERVER_USER_NAME, Globals.SFTP_SERVER_PASSWORD, Globals.SFTP_SERVER_URL);
	}
	
	public SftpFileGrabber(String userName, String password, String serverURL) {
		mUserName  = userName;
		mPassword  = password;
		mServerURL = serverURL;
	}
	
	/**
	 * <p>
	 * Download one file from SFTP server to the local phone.	
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
		
		File f = new File(localFilePath);	
		
		if (f.exists()) {
			if (isOverwrite) {
				if (!isFileClosed(f)) { // we can't overwrite an opened file
					Log.i(TAG, f + " can't be overwritten because it's been opened.");
					return IO_ERROR;
				}
			} else {
				Log.i(TAG, f + " does already exists.");
				return f.toString();
			}
		}
						
		File dir = f.getParentFile();
		if (!(dir.mkdirs() || dir.isDirectory())) {
			Log.e(TAG, "fail to create local directory");
			return IO_ERROR;
		}

		String result = localFilePath;
		try {	
			Session session = createSession();
			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			sftpChannel.get(remoteFilePath, localFilePath); // If the file exists, "GET" will overwrite it.
			Log.i(TAG, "successfully download file from: " + remoteFilePath);
		} catch (SftpException e) {
			Log.e(TAG, "SftpException downloading file: " + remoteFilePath + " with exception: " + e.getMessage());
			result = ERROR_PREFIX + e.getMessage().toLowerCase(Locale.US);			
		} catch (JSchException e) {
			Log.e(TAG, "JSchException downloading file: " + remoteFilePath + " with exception: " + e.getMessage());
			result = ERROR_PREFIX + e.getMessage().toLowerCase(Locale.US);
		}

		return result;
	}
	
	/**
	 * <p>
	 * Download all files from SFTP server directory to the local phone directory.
	 * </p>
	 * 
	 * @param remoteDirPath
	 * 		      is the given server directory
	 * @param remoteDirPath
	 * 			  is the given local directory
	 * @param isOverwrite
	 * 			  is to indicate whether the file should be overwrite if it exists locally
	 * @param isRecursive
	 *            is to indicate whether the download should be recursive for the 
	 *            sub directories.
	 * @return the relative paths of all files download from the server if successful,  
	 *         otherwise some error message. Different paths are connected with the comma.
	 * 
	 */
	public String downloadFiles(String remoteDirPath, String localDirPath, boolean isOverwrite, boolean isRecursive) {
		
		File dir = new File(localDirPath);
		
		if (!(dir.mkdirs() || dir.isDirectory())) {
			Log.e(TAG, "fail to create local directory");
			return IO_ERROR;
		}
		
		if (!localDirPath.endsWith("/"))  { localDirPath  += File.separator; }
		if (!remoteDirPath.endsWith("/")) { remoteDirPath += File.separator; }		

		String result = "";
		try {
			Session session = createSession();
			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			
			@SuppressWarnings("unchecked")
			Vector<LsEntry> list = sftpChannel.ls(remoteDirPath);
			for (int i = 0; i < list.size(); ++i) {
				LsEntry entry = list.get(i);
				String name = entry.getFilename();
				
				if (entry.getAttrs().isDir()) {
					if (!isRecursive || name.equals(".") || name.equals("..")) { continue; }					
					String names = downloadFiles(remoteDirPath + name, localDirPath + name, isOverwrite, isRecursive);
					result += ":" + appendParentDirName(names, name);
				} else {			
					File localFile = new File(localDirPath + name);
					if (!localFile.exists()) { 
						sftpChannel.get(remoteDirPath + name, localDirPath + name);
						result += ":" + name;
					} else { // file does exist, check if it is opened because we can't overwrite an opened file										   
						if (isOverwrite && isFileClosed(localFile)) {							
							sftpChannel.get(remoteDirPath + name, localDirPath + name);
							result += ":" + name;
						}					
					}
				}
			}
			result = result.startsWith(":") ? result.substring(1) : result;
		} catch (SftpException e) {
			Log.e(TAG, "SftpException downloading dir: " + remoteDirPath + " with exception: " + e.getMessage());
			result = ERROR_PREFIX + e.getMessage().toLowerCase(Locale.US);
		} catch (JSchException e) {
			Log.e(TAG, "JSchException downloading dir: " + remoteDirPath + " with exception: " + e.getMessage());
			result = ERROR_PREFIX + e.getMessage().toLowerCase(Locale.US);
		}

		Log.d(TAG, result);
		return result;
	}
	
	private boolean isFileClosed(File file) {
		
		Process plsof = null;
		BufferedReader reader = null;
		
	    try {
	        plsof  = new ProcessBuilder(new String[]{"lsof", "|", "grep", file.getAbsolutePath()}).start();
	        reader = new BufferedReader(new InputStreamReader(plsof.getInputStream()));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            if (line.contains(file.getAbsolutePath())) {                            
	                reader.close();
	                plsof.destroy();
	                return false;
	            }
	        }
	    } catch (Exception e) {
	        // TODO: handle exception ...
	    } finally {
	    	try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	plsof.destroy();
	    }
	    
	    return true;
	}
	
	private String appendParentDirName(String fileNames, String parentDirName) {
		String result = "";
		String[] names = fileNames.split("[,]");
		for (String name : names) {
			result += "," + parentDirName + File.separator + name;
		}
		return result.substring(1);
	}
	
	private Session createSession() throws JSchException {
		JSch jsch = new JSch();		
	 	Session session = jsch.getSession(mUserName, mServerURL, 22);
	 	
		session.setUserInfo(mUserInfo);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setConfig("UserKnownHostsFile", "/dev/null");
		session.setPassword(mUserInfo.getPassword());
		session.setTimeout(20000);		
		session.connect();
				
		return session;
	}
	
	private SftpUserInfo mUserInfo = new SftpUserInfo();
	
	private class SftpUserInfo implements UserInfo {
		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		@Override
		public boolean promptPassword(String arg0) {
			return false;
		}

		@Override
		public boolean promptYesNo(String arg0) {
			return false;
		}

		@Override
		public void showMessage(String arg0) {
		}

		@Override
		public String getPassword() {
			return mPassword;
		}
	}
}
