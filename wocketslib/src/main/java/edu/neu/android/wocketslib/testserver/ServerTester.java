package edu.neu.android.wocketslib.testserver;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.dataupload.DataSender;
import edu.neu.android.wocketslib.json.model.FileUploadEvent;
import edu.neu.android.wocketslib.json.model.HRData;
import edu.neu.android.wocketslib.json.model.Note;
import edu.neu.android.wocketslib.json.model.PhoneData;
import edu.neu.android.wocketslib.json.model.PromptEvent;
import edu.neu.android.wocketslib.json.model.SwapEvent;
import edu.neu.android.wocketslib.json.model.Swapping;
import edu.neu.android.wocketslib.json.model.WocketData;
import edu.neu.android.wocketslib.json.model.WocketInfo;
import edu.neu.android.wocketslib.utils.Log;


/**
 *
 * @author Salim
 */
public class ServerTester {

	WocketInfo wi = null;  

	public Date getRandomHourDate(Date aDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(aDate);
		Random r = new Random(); 
		cal.set(Calendar.HOUR_OF_DAY, r.nextInt(23));
		cal.set(Calendar.MINUTE, r.nextInt(59));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime(); 
	}

	public Date getTimeFromMinute(Date aDate, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(aDate);
		int hour = (int) Math.floor(minute/60.0); 
		int min = (int) (minute % 60.0); 
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0); 
		return cal.getTime(); 
	}
	
	private PromptEvent getTestPE(Date aDate)
	{
		PromptEvent pe = new PromptEvent(); 
		pe.promptType = "Auto";
		pe.promptTime = getRandomHourDate(aDate); 
		pe.responseTime = getRandomHourDate(aDate); 
		pe.activityInterval = 30;
		pe.primaryActivity = "Walking";
		pe.alternateActivity = "Watching TV";
		return pe; 
	}

	
	private FileUploadEvent getTestFileUpload(Date aDate)
	{
		Random r = new Random(); 
		FileUploadEvent fee = new FileUploadEvent(); 
		fee.bytes = r.nextInt(8000); 
		fee.endDataTime = getRandomHourDate(aDate);
		fee.startDataTime = getRandomHourDate(aDate);
		fee.fileName = "testfile.zip";
		fee.isSuccessful = true;
		fee.note = "A note about a file uplaod";
		fee.startUploadTime =getRandomHourDate(aDate);
		fee.endUploadTime = getRandomHourDate(aDate);
		return fee; 
	}

	private Note getTestNote(Date aDate)
	{
		Note no = new Note();
		no.startTime = getRandomHourDate(aDate); 
		no.endTime = getRandomHourDate(aDate); 
		no.note = "This is a test note!";
		return no; 
	}

	int bat = 10000; 
	int mm = 1000;
	int sdmem = 6000;  
	
	private PhoneData getTestPD(Date aDate, int minute)
	{
		PhoneData pd = new PhoneData(); 
		pd.createTime = getTimeFromMinute(aDate, minute);
		pd.phoneBattery = bat;
		bat = (int) Math.floor(bat * .908);  
		pd.mainMemory = mm;
		mm = (int) Math.floor(mm*1.002);
		pd.sDMemory = sdmem;
		sdmem = (int) Math.floor(.99*sdmem); 
		return pd; 
	}

//	private SwappedSensor getTestSS()
//	{
//		SwappedSensor ss = new SwappedSensor();
//		ss.macID = "AAABBBCCC9999";
//		ss.bodyLocation = "Wrist";
//		return ss; 
//	}
	
	
	private Swapping getTestSE(Date aDate)
	{
		SwapEvent se = new SwapEvent();
		se.swapTime = getRandomHourDate(aDate);
		se.isLocationChange = false;
		se.isSwap = true;
		se.isRestarted = false;
		Swapping swapping = new Swapping();
		swapping.someSwap.add(se);
		return swapping; 
	}

	private int ac = 0;
	private int acMax = 9000; 
	
	private WocketData getTestWD(Date aDate, int minute)
	{
		WocketData wd = new WocketData();
		wd.macID = "124578555";
		wd.createTime = getTimeFromMinute(aDate, minute);
		
		ac += 100; 
		if (ac == acMax)
			ac = 0; 
		
		wd.activityCount = ac;
		wd.wocketBattery = 500;
		wd.transmittedBytes = 567;
		wd.receivedBytes = 908; 

		Log.d("STEVE", "Date: " + wd.createTime.toString() +  "   Activity count: " + ac);
		
		return wd; 
	}

	private int hrMin = 90; 
	private int hr = hrMin;
	private int hrMax = 120;
	private int hrinc = +1; 
	
	private HRData getTestHR(Date aDate, int minute)
	{
		HRData hrd = new HRData();
		hrd.hardwareID = "999999";
		hrd.createTime = getTimeFromMinute(aDate, minute);
		
		hr += hrinc; 
		if (hr == hrMax)
			hrinc = -1;  
		if (hr == hrMin)
			hrinc = 1;  
		
		hrd.heartRate = hr;
		hrd.heartBeatNumber = 23;
		hrd.battery = 55; 

		Log.d("STEVE", "Date: " + hrd.createTime.toString() +  "   HR: " + hr);		

		return hrd; 
	}	
	
	public String setupTest(Date aDate, Context aContext)
	{
		int testpoints = 25; // 60*12; //TODO 

		wi = new WocketInfo(aContext);
				
		wi.somePrompts = new ArrayList<PromptEvent>();
		wi.somePrompts.add(getTestPE(aDate));
		wi.somePrompts.add(getTestPE(aDate));

		wi.somePhoneData = new ArrayList<PhoneData>();
		for (int i = 0; i < (testpoints); i++)
		{ 
			wi.somePhoneData.add(getTestPD(aDate, i));  
		}

		wi.someSwaps = new ArrayList<Swapping>();
		wi.someSwaps.add(getTestSE(aDate));
		wi.someSwaps.add(getTestSE(aDate)); 
		
		wi.someNotes = new ArrayList<Note>();
		wi.someNotes.add(getTestNote(aDate));
		wi.someNotes.add(getTestNote(aDate));
		
//		Random r = new Random(); 

		wi.someWocketData = new ArrayList<WocketData>();
		// Randomly fill up the data starting from midnight 
		for (int i = 0; i < (testpoints); i++)
		{ 
			wi.someWocketData.add(getTestWD(aDate, i)); 
		}

		wi.someHRData = new ArrayList<HRData>();
		// Randomly fill up the data starting from midnight 
		for (int i = 0; i < (testpoints); i++)
		{ 
			wi.someHRData.add(getTestHR(aDate, i)); 
		}

		wi.someFileUploads = new ArrayList<FileUploadEvent>();
		wi.someFileUploads.add(getTestFileUpload(aDate));
		wi.someFileUploads.add(getTestFileUpload(aDate));
		
		Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy hh:mm:ss a").create();
		String json = gson.toJson(wi);

//		String DIRNAME = "uploads";
//		String FILENAME = "test.json"; 
//		String FILENAME2= "test2.json"; 
//
//		FileUtils.saveStringInternal(aContext, json, DIRNAME, FILENAME);
//		FileUtils.saveStringExternal(aContext, json, DIRNAME, FILENAME);
//		String json2 = FileUtils.readStringInternal(aContext, DIRNAME, FILENAME);
//		FileUtils.saveStringExternal(aContext, json2, DIRNAME, FILENAME2);
//		String[] children = FileUtils.getFileNamesInternal(aContext, DIRNAME);
//		FileUtils.deleteStringInternal(aContext, DIRNAME, FILENAME); 
//		
//		for (String c: children)
//		{
//			Log.e("FILES: " + c); 			
//		}
//		Log.e("MESSAGE ORIG: " + json); 
//		Log.e("MESSAGE  NEW: " + json2);
//		
//		WocketInfo wi2 = gson.fromJson(json, WocketInfo.class);		
		
		return json;  
	}
	
	public void Test(Context aContext)
	{	
		Date today = new Date();  
		String json = setupTest(today, aContext); 		
		DataSender.transmitJSON(json); 	
		DataSender.queueJsonData(json);
		
//		DataSender.transmitQueuedJsonData(aContext);
		
		DataSender.sendQueuedJsonDataToExternalUploadDir(aContext, true); //zip = true
				

		//DataSender.transmitQueuedJsonData(aContext);		
	}

	public String getJsonTestString(Context aContext)
	{	
		Date today = new Date();  
		String json = setupTest(today, aContext);
		return json;
	}

	
    public String getStringFromDocument() throws ParserConfigurationException, SAXException, IOException
    {
    try
    {
    	String sd = Globals.EXTERNAL_DIRECTORY_PATH;    
    	    		
        String xmlFile = sd + "//TestData.xml";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMSource domSource = new DOMSource(builder.parse(new File(xmlFile)));
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }
    catch(TransformerException ex)
    {
       ex.printStackTrace();
       return null;
    }
} 
    
}
