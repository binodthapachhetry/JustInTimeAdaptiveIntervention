package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;


public class RawDataFileHandler {
	private final static String TAG = "RawDataFileHanlder";
	private static final String SERVER_ADDR = "http://wockets.ccs.neu.edu:8080/";
	private static final String URL_FILE_UPLOAD_SERVLET = SERVER_ADDR + "FileUploader/Commonsfileuploadservlet";
	private static final int HTTP_RESPONSE_SERVER_UP = 200; 
	private String phoneID;
	
	
	public RawDataFileHandler(Context c) {
		super();
		this.phoneID = PhoneInfo.getID(c);
	}
	public boolean zipRawData(Date rawDataTime, Context c){
		SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hour = new SimpleDateFormat("HH");
		File dataDir = new File(c.getFilesDir()+"/"+day.format(rawDataTime)+"/"+hour.format(rawDataTime));
		if(dataDir.isDirectory()){
			File[] dataFiles = dataDir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.endsWith(".baf");
				}
			});
			if(dataFiles != null && dataFiles.length > 0){
				try {
				String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.wockets/data/"+day.format(rawDataTime);
				File externalDir = new File(externalPath);
				if(!externalDir.isDirectory())
					if(!externalDir.mkdirs())
						Log.e(TAG, "Error: can not make directory for output zip folder.");
				SimpleDateFormat serverDir = new SimpleDateFormat("yyyy__MM__dd__");
				SimpleDateFormat detailedTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
				String earlistTimeStampInFiles = findTheEarliestTimeStampInBafFileName(dataFiles);
				if(earlistTimeStampInFiles == null)
					earlistTimeStampInFiles = detailedTime.format(rawDataTime);
				//WocketsRaw.[IMEI].[YYYY]-[MM]-[DD]-[HH]-[mm]-[ss]-[SSS].zip
				String targetFileName = "WocketsRaw__"+serverDir.format(rawDataTime)+"WocketsRaw."+phoneID+"."+earlistTimeStampInFiles+".zip";
				File targetFile = new File(externalPath+"/"+targetFileName);
				if(!targetFile.exists())
					if(!targetFile.createNewFile()){
						Log.e(TAG, "Error: can not create zip file in external.");
						return false;
					}
				if(!zipFiles(dataFiles,targetFile)){
					Log.e(TAG, "Error: can not zip files.");
					return false;
				}
				else
					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "Error: can not create zip file.");
				} 			
			}
			else{
				Log.e(TAG, "Error: can not find encoded raw data file in internal memory.");
				return false;
			}
		}
		else{
			Log.e(TAG, "Error: can not find encoded raw data folder in internal memory.");
			return false;
		}
		return false;
	}
	public void doubleCheckOldFiles(Date today, Context c){
		File internalDir = c.getFilesDir();
		if(!internalDir.isDirectory()){
			Log.e(TAG, "Error: internal memory fail.");
			return;
		}
		SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final String todayDir = currentDateFormat.format(today);
		File[] dateFolders = internalDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				return filename.matches("\\d{4}-\\d{2}-\\d{2}")
						&& !filename.equals(todayDir);
			}
		});
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
		final String currentHourDir = hourFormat.format(today);
		SimpleDateFormat fullTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH");
		String fullTime = "";
		if(dateFolders != null && dateFolders.length > 0){
			for (File dateDir : dateFolders) {
				if(dateDir.isDirectory()){
					String date = dateDir.getAbsolutePath().substring(dateDir.getAbsolutePath().lastIndexOf("/")+1);
					File[] hourFolders = dateDir.listFiles(new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String filename) {
							// TODO Auto-generated method stub
							return filename.matches("\\d{2}")
									&& !filename.equals(currentHourDir);
						}
					});
					if(hourFolders != null && hourFolders.length > 0){
						for (File hourDir : hourFolders) {
							String hour = hourDir.getAbsolutePath().substring(hourDir.getAbsolutePath().lastIndexOf("/")+1);
							fullTime = date+" "+hour;
							try {
								zipRawData(fullTimeFormat.parse(fullTime), c);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								Log.e(TAG, "Error: can not parse date."+e.toString());
							}
							String[] files = hourDir.list();
							if(files == null || files.length == 0)
								if(!hourDir.delete())
									Log.e(TAG, "Error: can not delete empty folder:"+hourDir.getAbsolutePath());
						}
					}
					
				}
				String[] files = dateDir.list();
				if(files == null || files.length == 0)
					if(!dateDir.delete())
						Log.e(TAG, "Error: can not delete empty folder:"+dateDir.getAbsolutePath());
			}
		}
	}
	public boolean transmitRawData(Context c){
		boolean isTransmitted = false;
		String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.wockets/data/";
		File dataFolder = new File(externalPath);
		if(!dataFolder.isDirectory()){
			Log.e(TAG, "Error: SD card unavailable.");
			return isTransmitted;
		}
		ArrayList<File> dataFiles = getAllZipFilesInFolder(dataFolder);
		for (File file : dataFiles) {
			Log.d(TAG, "start to transmit file:  "+file.getAbsolutePath());
			isTransmitted = transmitFile(c, file.getAbsolutePath(), phoneID, true);
		}
		return isTransmitted;
	}
	private ArrayList<File> getAllZipFilesInFolder(File folder){
		ArrayList<File> fileList = new ArrayList<File>();
		File[] subFiles = folder.listFiles();
		for (File file : subFiles) {
			if(file.isFile()){
				if(file.getName().endsWith(".zip"))
					fileList.add(file);
			}
			else if(file.isDirectory())
				fileList.addAll(getAllZipFilesInFolder(file));
		}
		return fileList;
	}
	private boolean zipFiles(File[] sourceFiles, File targetFile){
		BufferedInputStream origin = null;
		ZipOutputStream out = null;
		boolean isSuccess = true; 
		FileInputStream fi = null;
		FileOutputStream dest = null;

		try  {
			dest = new FileOutputStream(targetFile);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[1024];
			for (File sourceFile : sourceFiles) {
				String filePath = sourceFile.getAbsolutePath();
				if (filePath.endsWith(".zip")){
					Log.e(TAG, "Warning: File already compressed: " + sourceFile.getName());
				}
				else{
					fi = new FileInputStream(sourceFile);
					origin = new BufferedInputStream(fi, 1024);
					ZipEntry entry = new ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1));
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, 1024)) != -1) {
							out.write(data, 0, count);
					}

				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error when zipping: " + e.toString());
			isSuccess = false; 
		} finally{
			try {
				if(origin != null)
					origin.close();
				if(out != null){
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Problem closing file when zipping: " + e.toString());
				e.printStackTrace();
				isSuccess = false; 
			}
		}

		if (targetFile.length() == 0)
		{
			Log.e(TAG, "Zip created a file of size 0: " + targetFile);
			isSuccess = false; 
		}
		
		if (isSuccess)
		{
			for (File sourceFile : sourceFiles) {
				if (!sourceFile.delete())
				{
					Log.e(TAG, "Error deleting file after zipping: " + sourceFile.getName());
					isSuccess = false; 
				}
			}
		}	
		
		return isSuccess; 
	}
	public boolean transmitFile(Context aContext, String aFileName, String anID, boolean isRemove)
	{	
		File f = new File(aFileName);
		String errMsg = ""; 
		boolean isSuccess = true;
		// get the md5 checksum of file
		if (!isNetworkAvailable(aContext))
		{
			errMsg = "Error in transmitFile. Network not available."; 
			Log.e(TAG, errMsg);
			isSuccess = false; 
		}
		HttpPost httppost = null;
		MultipartEntity entity = null;
		HttpClient httpclient = null;

		if (isSuccess)
		{
			String md5Checksum = getMD5forfile(aFileName);

			httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter("http.connection.timeout", 15000 ); // TODO change from 3000
			httpclient.getParams().setParameter("http.socket.timeout", 15000 );
			httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
						
			httppost = new HttpPost(URL_FILE_UPLOAD_SERVLET);
 
			entity = new MultipartEntity();
			// adding parameters for request
		try {
//			entity.addPart("protocol", new StringBody("protocol"));
			entity.addPart("phoneID", new StringBody(anID));
//			entity.addPart("sessionNumber", new StringBody(sessionNumber));
			entity.addPart("md5Checksum", new StringBody(md5Checksum));
			entity.addPart(f.getName(), new FileBody(f));
		} catch (UnsupportedEncodingException e) {
			errMsg = "Error in uploadFile. Unsupported encoding. " + e.toString();
			Log.e(TAG, errMsg);
			e.printStackTrace();
			isSuccess = false; 
		}		
		}

		if (isSuccess)
		{
		httppost.setEntity(entity);
		HttpResponse httpresponse;
		InputStream is = null;
		BufferedReader rd = null;
		try {
			httpresponse = httpclient.execute(httppost);
			String statusLine = httpresponse.getStatusLine().toString();
			int statusCode = httpresponse.getStatusLine().getStatusCode();
			if (statusCode != HTTP_RESPONSE_SERVER_UP)
			{
				errMsg = "Error in uploadFile communicating with server. Did not get a 200 response. " + statusLine; 
				Log.e(TAG, errMsg);
				isSuccess = false; 
			}
			else	
			{
				HttpEntity resEntity = httpresponse.getEntity();
				String responseBody = EntityUtils.toString(resEntity);
				
				if (responseBody.contains("Json file successfully submitted"))
				{
					errMsg = "Json file uploaded and processed.";
					isSuccess = true;
				} else if (responseBody.contains("File written to destination directory"))
				{
					errMsg = "File uploaded to server without error.";
					isSuccess = true; 
				} else // Response body must contain an error
				{
					Log.e(TAG, "HTTPRESPONSE BODY (ERROR): " + responseBody);
					errMsg = "Error message from server after upload attempt: " + responseBody; 
					isSuccess = false;
				}
			}
		} catch (ClientProtocolException e) {
			errMsg = "Error in uploadFile. ClientProtocolException in httpclient.execute." + e.toString();
			Log.e(TAG, errMsg);
			e.printStackTrace();
			isSuccess = false; 
		} catch (IOException e) {
			errMsg = "Error in uploadFile. IOException in httpclient.execute." + e.toString();
			Log.e(TAG, errMsg);
			e.printStackTrace();
			isSuccess = false; 
		} finally
		{
			if (rd != null)
				try {
					rd.close();
				} catch (IOException e) {
					errMsg = "Error closing file";
					Log.e(TAG, errMsg);
					e.printStackTrace();
				} 
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					errMsg = "Error closing file";
					Log.e(TAG, errMsg);
					e.printStackTrace();
				} 
		}
		}
		
		if (isRemove && isSuccess)
		{
			if (!deleteFile(new File(aFileName)))
				errMsg = "Error: could not remove file on phone after upload: " + aFileName;
				Log.e(TAG, errMsg);
		}
		return isSuccess; 
	}
	// this method returns the checksum for the input file
	// It calculates the MD5 hash using the native java algorithm for MD5
	private static String getMD5forfile(String filename) {
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

			} catch (IOException e) {
				Log.e(TAG,"Unable to process file for MD5: " + filename);
				throw new RuntimeException("Unable to process file for MD5", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG,"Unable to close input stream for MD5 calculation: " + filename);					
					throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG,"Error in getMD5forfile: " + filename + " " + ex.toString());								
			ex.printStackTrace();
		}

		return md5;
	}
	public static boolean isNetworkAvailable(Context aContext)
	{
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager connectivityManager = (ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); 
	    
	    
	    NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
	    //connectivityManager.getActiveNetworkInfo(); 
	    for (NetworkInfo ni : netInfo) {
	    	// This can fail on some phones (e.g. yifei's phone) 
	        if (ni.getType() == ConnectivityManager.TYPE_WIFI)
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    
	    //TODO This entire method needs revamping 
	    //There is a bug in Android that causes isConnected to fail sometimes without toggling wifi 
	    
	    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
	    boolean isConnected = ((activeNetwork != null) && activeNetwork.isConnectedOrConnecting());
	    
	    Log.i(TAG, "isConnectedOrConnecting: " + isConnected + " " + haveConnectedWifi + " " + haveConnectedMobile);
	    
	    return isConnected || haveConnectedWifi || haveConnectedMobile;	
//		return activeNetworkInfo != null;
	} 
	public static boolean deleteFile(File orig)
	{ 
		boolean isDeleted = orig.delete(); 
		if (!isDeleted)
		{
			Log.e(TAG,"Error: could not delete file in deleteFile: " + orig.getAbsolutePath());
			return false; 
		}
		return true; 
	}

	private static String findTheEarliestTimeStampInBafFileName(File[] dataFiles){
		SimpleDateFormat detailedTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		Date earliestTime = null;
		Date tempTime = null;
		String fileName = "";
		try {
			for (int i = 0; i < dataFiles.length; i++) {
				fileName = dataFiles[i].getName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				fileName = fileName.substring(fileName.lastIndexOf(".")+1);
				tempTime = detailedTime.parse(fileName);
				if(earliestTime == null || earliestTime.after(tempTime))
					earliestTime = tempTime;
			}
			return detailedTime.format(earliestTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
