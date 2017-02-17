package edu.neu.android.wocketslib.kmlplot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import edu.neu.android.wocketslib.utils.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class KMLPlotInMapActivity extends Activity{
	private String TAG = "PlotInMap"; 
	private String isPath = "isPath";
	private String isMerge = "isMerge";
	private static ArrayList<String> csvData = new ArrayList<String>();
	private boolean ISPath = false;
	private boolean ISMerge = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub	
		super.onCreate(savedInstanceState);
		csvData.clear();
		Intent intent = getIntent();
		ISPath = intent.getBooleanExtra(isPath, false);
		ISMerge = intent.getBooleanExtra(isMerge, true);
		collectionData(Globals.KMLStartDate, Globals.KMLEndDate);
		if(ISMerge) {
			mergeData();
		}
		if(csvData.isEmpty()) {
			show_toast("There is no data for the chosen days");
			finish();
		} else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
			setContentView(R.layout.kmlplotview);
			WebView webView;
			if(!isConnectingToInternet())
			{
				show_toast("Internet is not available, try to connect");
			}
			webView = (WebView) findViewById(R.id.plot_view);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setWebViewClient(new WebViewClient());
			webView.setWebChromeClient(new WebChromeClient());
			webView.addJavascriptInterface(KMLPlotInMapActivity.this,
					"AndroidFunction");
			if(!ISPath) {
				webView.loadUrl("file:///android_asset/google_map_plot.html");
			}
			else {
				webView.loadUrl("file:///android_asset/google_map_plot_polylines.html");
			}
			webView.requestFocus();
		}
	}

	@JavascriptInterface
	public String read_file(int index) {
		Log.d(TAG, "csvData get index: " + index + ", Data:" + csvData.get(index));
		return csvData.get(index);
	}
	
	@JavascriptInterface
	public int file_size() {
		Log.d(TAG, "csvData file size: " + csvData.size());
		return csvData.size();
	}
	
	private void show_toast(String answer) {
		Toast.makeText(this, answer, Toast.LENGTH_LONG).show();
	}
	private boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			return true;
		} else {
			return false;
		}
	}
	private void collectionData(final String startDate, final String endDate) {
		String basePath = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY;
		FileFilter dateFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
               return (pathname.getName().compareTo(startDate) >=0 && pathname.getName().compareTo(endDate) <= 0);
            }
        };
        Log.d(TAG, basePath);
        File dateDirectory = new File(basePath);
        if(dateDirectory.exists()) {
	        for(File tempDate : dateDirectory.listFiles(dateFilter)) {
	        	if(tempDate.isDirectory()) {
		        	String path = basePath + File.separator + tempDate.getName();
		        	Log.d(TAG, path);
					File directory = new File(path);
					for(File tempDirectory : directory.listFiles()) {
						if(tempDirectory.isDirectory()) {
							String curPath = path + File.separator + tempDirectory.getName();
							File file = new File(curPath);
							for (File tempFile : file.listFiles()) {
								if(tempFile.isFile() && tempFile.getName().endsWith(".csv") && tempFile.getName().contains(Globals.SENSOR_TYPE_GPS)) {
									readcsvFile(tempFile);
								}
							}
						}
					}
	        	}
	        }
        }
        Log.d(TAG, "Merge date data: \n");
        for(String data:csvData) {
        	Log.d(TAG, data);
        }
	}
		
	private void readcsvFile(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
			   // process the line.
				if(!line.contains("TIMESTAMP")) {
					csvData.add(line);
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private void mergeData() {
		String laststring = null;
		CopyOnWriteArrayList<String> copyCSVData= new CopyOnWriteArrayList<>(csvData);
		for(String data:copyCSVData) {
			if(laststring != null) {
				if(isNearPoint(data, laststring)) {
					copyCSVData.remove(data);
				}
			}
			laststring = data;
		}
		csvData.clear();
		csvData.addAll(copyCSVData);
	}
	
	private boolean isNearPoint(String str1, String str2) {
		if(calcDistance(str1.split(",")[1], str1.split(",")[2],str2.split(",")[1],str2.split(",")[2]) < 100){
			return true;
		}
		else {
			return false;
		}
	}
	
	private double calcDistance(String lat1, String lng1, String lat2, String lng2) {
		double EarthRadiusKm = 6371.004;
		double p1Lat = Double.valueOf(lat1);
		double p1Lng = Double.valueOf(lng1);
		double p2Lat = Double.valueOf(lat2);
		double p2Lng = Double.valueOf(lng2);
		double dLat1InRad = p1Lat * (Math.PI / 180);
		double dLong1InRad = p1Lng * (Math.PI / 180);
		double dLat2InRad = p2Lat * (Math.PI / 180);
		double dLong2InRad = p2Lng * (Math.PI / 180);
		double dLongitude = dLong2InRad - dLong1InRad;
		double dLatitude = dLat2InRad - dLat1InRad;
		double a = Math.pow(Math.sin(dLatitude / 2), 2) + Math.cos(dLat1InRad) * Math.cos(dLat2InRad) * Math.pow(Math.sin(dLongitude / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dDistance = EarthRadiusKm * c;
		Log.d(TAG, String.valueOf(dDistance * 1000));
		return dDistance * 1000;
	}
}
