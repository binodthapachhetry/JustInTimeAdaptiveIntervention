package edu.neu.android.wocketslib.utils;


public class ParticipantChecker {
	public static String TAG = "ParticipantChecker";

//	public static int getParticipantID(Context c){
//		String imei = PhoneInfo.getID(c);
//		String address = Globals.SERVER_ADDRESS_PID;
//		String params = "?imei=" + imei + "&study=" + Globals.STUDY_SERVER_NAME;
//		HttpClient client=new DefaultHttpClient();
//		HttpPost request;
//		String pId = "";
//		try {
//			request = new HttpPost(new URI(address + params));
//			HttpResponse response=client.execute(request);
//			if(response.getStatusLine().getStatusCode()==200){
//				HttpEntity entity=response.getEntity();
//				if(entity!=null){
//					pId=EntityUtils.toString(entity);
//					Log.v(TAG, "pid is-------->"+pId);
//					}
//				}
//		}catch (URISyntaxException e){
//			e.printStackTrace();
//		}catch (ClientProtocolException e){
//			e.printStackTrace();
//		}catch (IOException e){
//			e.printStackTrace();
//		}
//		return 0;
//	}
}
