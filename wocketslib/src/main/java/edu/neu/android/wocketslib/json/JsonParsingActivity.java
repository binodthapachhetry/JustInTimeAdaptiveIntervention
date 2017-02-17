package edu.neu.android.wocketslib.json;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.json.model.PromptEvent;
import edu.neu.android.wocketslib.json.model.WocketInfo;

public class JsonParsingActivity extends Activity {

	String url = "http://search.twitter.com/search.json?q=javacodegeeks";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.json_main);

		// InputStream source2 = retrieveStream(Globals.URL_GET_WOCKETS_DETAIL);

		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		// Reader reader2 = new InputStreamReader(source2);
		//
		// WocketData wi = gson.fromJson(reader2, WocketData.class);
		//
		// String json = gson.toJson(wi);
		//
		// String newjson = json.replace("1.63", "JUNK");
		//
		// try
		// {
		// WocketData wi2 = gson.fromJson(newjson, WocketData.class);
		// }
		// catch (JsonSyntaxException e)
		// {
		// Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		// }
		//
		// Log.e("---------------------------", json);
		// Toast.makeText(this, newjson, Toast.LENGTH_SHORT).show();

		// List<WI> results2 = wi2.wockets;
		//
		// for (WI result : results2) {
		// Toast.makeText(this, result.macID, Toast.LENGTH_SHORT).show();
		// }

		WocketInfo wi = new WocketInfo(getApplicationContext());
		PromptEvent pe = new PromptEvent();
		Date today = new Date();
		pe.promptTime = today;
		wi.somePrompts = new ArrayList<PromptEvent>();
		wi.somePrompts.add(pe);
		String json = gson.toJson(wi);
		Toast.makeText(this, json, Toast.LENGTH_LONG).show();

		WocketInfo wi2 = gson.fromJson(json, WocketInfo.class);

		Date adate = wi2.somePrompts.get(0).promptTime;

		Toast.makeText(this, adate.toString(), Toast.LENGTH_LONG).show();

	}

//	private InputStream retrieveStream(String url) {
//
//		DefaultHttpClient client = new DefaultHttpClient();
//
//		HttpGet getRequest = new HttpGet(url);
//
//		try {
//
//			HttpResponse getResponse = client.execute(getRequest);
//			final int statusCode = getResponse.getStatusLine().getStatusCode();
//
//			if (statusCode != HttpStatus.SC_OK) {
//				Log.w(getClass().getSimpleName(), "Error " + statusCode + " for URL " + url);
//				return null;
//			}
//
//			HttpEntity getResponseEntity = getResponse.getEntity();
//			return getResponseEntity.getContent();
//
//		} catch (IOException e) {
//			getRequest.abort();
//			Log.e(getClass().getSimpleName(), "Error for URL " + url, e);
//		}
//
//		return null;
//
//	}

}