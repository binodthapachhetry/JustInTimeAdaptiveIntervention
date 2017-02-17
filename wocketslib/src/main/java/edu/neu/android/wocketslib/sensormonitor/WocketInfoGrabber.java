package edu.neu.android.wocketslib.sensormonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.encryption.RSACipher;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Sensor;
import edu.neu.android.wocketslib.json.model.SwapEvent;
import edu.neu.android.wocketslib.json.model.SwappedSensor;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.json.model.WocketInfo_old;
import edu.neu.android.wocketslib.support.NetworkChecker;
import edu.neu.android.wocketslib.support.ServerLogger;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.PhoneInfo;
import edu.neu.android.wocketslib.utils.Wockets;

public class WocketInfoGrabber {

	private static final String TAG = "DataGrabber";
		
	private static String convertStreamToString(InputStream is) {
	   BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	   StringBuilder sb = new StringBuilder();
	 
	   String line = null;
	   try {
	       while ((line = reader.readLine()) != null) {
	           sb.append(line + "\n");
	       }
	   } catch (IOException e) {
	   Log.e(TAG, "Error in convertStreamToString: " + e.toString());
	       e.printStackTrace();
	   } finally {
	       try {
	           is.close();
	       } catch (IOException e) {
	    	   Log.e(TAG, "Error in closing file in convertStreamToString: " + e.toString());
	           e.printStackTrace();
	       }
	   }
	   return sb.toString();
	}

//	/** 
//	 * Temporary function until server code change! 
//	 * @param return_result
//	 * @return
//	 */
//	private static String tempFixJSON(String return_result) //TODO 
//	{
//		String result = return_result.replace("wockets", "someSensors");
//		result = result.replace("mac_id", "mac"); 
//		result = result.replace("color", "col"); 
//		result = result.replace("hardware_version", "hver"); 
//		result = result.replace("firmware_version", "fver"); 
//		result = result.replace("printed_id", "label"); 
//		return result; 
//	}		
	
	public static WocketInfo getWocketInfoServer(Context aContext, String anID)
	{
        if(Globals.IS_PHONE_ID_ENCRYPTION_ENABLED) {
            try {
                Log.i(TAG, "Try to get assigned Wockets from Server for phoneID: "
                        + RSACipher.encrypt(anID,aContext));
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Try to get assigned Wockets from Server for phoneID: "
                    + anID);
        }
		if (!NetworkChecker.isOnline(aContext))
		{
            if (Globals.IS_DEBUG)
			    Log.i(TAG, "No Internet connection so cannot get assigned Wockets from Server now");
			return null; 
		}

		HttpClient httpclient = new DefaultHttpClient();
		try {
			String request = Globals.GET_WOCKETS_DETAIL_URL + "?phoneID=" + anID;
			Log.i(TAG, "Request sent: " + request);
			HttpGet httpget = new HttpGet(request); //TODO change to phoneID
			HttpResponse httpresponse = httpclient.execute(httpget);
			HttpEntity resEntity = httpresponse.getEntity();
			InputStream is = resEntity.getContent();
			String return_result = null; 		
			return_result = convertStreamToString(is);
		
			if (return_result != null) {
				Pattern regex = Pattern.compile("(?<=<body><h1>)(.+?)(?=<)");
				Matcher m = regex.matcher(return_result);
				if (m.find()) {
					Log.e(TAG, "Error. Query of server for assigned Wockets not successful: "+ return_result + " MATCH: " + m.group());
				}
				else
				{ 
					
					Log.i(TAG, "STRING RECOVERED: " + return_result);
					Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create(); 
					//Hibernate code in the server is not updated, so use the old code temporarily
//					WocketInfo wi = gson.fromJson(return_result, WocketInfo.class);
					WocketInfo_old wi_old = gson.fromJson(return_result, WocketInfo_old.class);
					WocketInfo wi = new WocketInfo(aContext);
					wi.someSensors = wi_old.someSensors;
					
					Log.i(TAG, "SUCCESS! Query of server for Wocket info successful.");
					ServerLogger.sendNote(aContext, "SUCCESS! Query of server for Wocket info successful.", false);
					return wi; 
				}
			}
		}
		catch (ConnectTimeoutException e) {
            if (Globals.IS_DEBUG)
                Log.e(TAG, "Connect timeout. Cannot get to server to check for Wocket info: " + e.toString());
			e.printStackTrace();
		}
		catch (Exception e) {
            if (Globals.IS_DEBUG)
    			Log.e(TAG, "Error in connecting to server to check for Wocket info: " + e.toString());
			e.printStackTrace();
		}
		return null; 
	}	
	
	public static List<edu.neu.android.wocketslib.json.model.Sensor> getSensors(Context c){
		File localFile = new File(Globals.WOCKETS_INFO_JSON_FILE_PATH);
		if(localFile.exists()){
			Log.i(Globals.SWAP_TAG, "Start to load Wockets info from local file.");
			int ch;
			StringBuffer fileContent = new StringBuffer("");
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(localFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(Globals.SWAP_TAG, "Local sensor info file not found.");
				e.printStackTrace();
			} 
			try {
				while((ch = fis.read()) != -1)   
					fileContent.append((char)ch);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(Globals.SWAP_TAG, "Fail to load wockets info from local file.");
				e.printStackTrace();
			} 
			String json = new String(fileContent);
			List<Sensor> sensors = new ArrayList<Sensor>();
			try{
				Type collectionType = new TypeToken<ArrayList<Sensor>>(){}.getType();
				Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
				sensors = gson.fromJson(json, collectionType);
			}
			catch(JsonSyntaxException e){
				Log.e(Globals.SWAP_TAG, "Failed to load wockets info. Local file crashed or in different format");
			}
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(sensors.size() > 0){
				Log.i(Globals.SWAP_TAG, "Load wockets info successfully.");
				ServerLogger.sendNote(c, "SUCCESS! Load Wocket info from internal memory successfully.", false);
				return sensors;
			}
		}

		Log.i(Globals.SWAP_TAG, "Local wockets info doesn't exist.");
		return new ArrayList<Sensor>();

	}
	public static List<SwappedSensor> getSwappedSensors(Context c){
		File dataFile = new File(Globals.SWAPPED_WOCKETS_JSON_FILE_PATH);
		if(dataFile.exists()){
			Log.i(Globals.SWAP_TAG, "Start to load swapped sensors info from local file.");
			int ch;
			StringBuffer fileContent = new StringBuffer("");
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(dataFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(Globals.SWAP_TAG, "Swapped sensor info file not found.");

				e.printStackTrace();
			} 
			try {
				while((ch = fis.read()) != -1)   
					fileContent.append((char)ch);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(Globals.SWAP_TAG, "Fail to load swapped sensors info from local file.");
				e.printStackTrace();
			} 
			String json = new String(fileContent);
			Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
			SwapEvent swapEvent = gson.fromJson(json, SwapEvent.class);
			if(swapEvent == null)
				swapEvent = new SwapEvent();
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return swapEvent.swappedSensor;
		}
		return new ArrayList<SwappedSensor>();
	}
	public static HRData getHRSensor(Context c){
		File HRSensorInfoFile = new File("/data/data/edu.neu.android.wocketslib/files/"+Globals.swappedHRFile);
		if(HRSensorInfoFile.exists()){
			Log.i(Globals.SWAP_TAG, "swapped HRSensor info file exists");
			int ch;
			StringBuffer fileContent = new StringBuffer("");
			FileInputStream fis = null;
			try {
				fis = c.openFileInput(Globals.swappedHRFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			try {
				while((ch = fis.read()) != -1)   
					fileContent.append((char)ch);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			String json = new String(fileContent);
			Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
			HRData zephyrSwapped = gson.fromJson(json, HRData.class);
 			
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return zephyrSwapped;
		}
		return new HRData();
	}

	public static Wockets updateLocalSensorInfo(Context c){
		Log.i(Globals.SWAP_TAG, "start to update local sensor info file.");
		Wockets wockets = null;
		WocketInfo wi = WocketInfoGrabber.getWocketInfoServer(c, PhoneInfo.getID(c));
		if(wi != null){
			wockets = new Wockets();
			wockets.setSensors(wi.someSensors);
			try {
				wockets.saveSensorInfoToFile(c);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(Globals.SWAP_TAG, "Cannot write sensor info to local file.");
			}
			Log.i(Globals.SWAP_TAG, "get Wockets from server, "+wockets.getSensorsInfo());
		}
		else{
			Log.e(Globals.SWAP_TAG, "Failed to retrieve data from server side.");
		}
		return wockets;
	}
	private static boolean isConnected(Context cxt){
		ConnectivityManager nw=(ConnectivityManager)cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiinfo=nw.getNetworkInfo(1);
		NetworkInfo mobileinfo = nw.getNetworkInfo(0);
		return wifiinfo.isConnected()||mobileinfo.isConnected();
	}
	public static boolean isActiveInternetConnection(Context context) {
	    if (isConnected(context)) {
	        try {
	            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
	            urlc.setRequestProperty("User-Agent", "Test");
	            urlc.setRequestProperty("Connection", "close");
	            urlc.setConnectTimeout(1500); 
	            urlc.connect();
	            return (urlc.getResponseCode() == 200);
	        } catch (IOException e) {
	            Log.e(Globals.SWAP_TAG, "Error checking internet connection", e);
	        }
	    } else {
	        Log.e(Globals.SWAP_TAG, "No network available!");
	    }
	    return false;
	}
}