package edu.neu.android.wocketslib.utils;

import java.io.StringWriter;
import java.util.Calendar;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class XmlWriter {
	private String paticipant_Id = "00001";
	public final static int EVENT_SWAP = 0;
	public final static int EVENT_CHANGE = 1;
	private int event;
	private String[] Mac_Id;
	private String[] location;
	public XmlWriter(String[] mac_Id, String[] location, int event) {
		super();
		Mac_Id = mac_Id;
		this.location = location;
		this.event = event;
	}
	public String writeXml(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("ISO-8859-1",true);
	
			serializer.startTag("","SWAPPING");
			serializer.startTag("", "row");
			
			serializer.startTag("", "Paticipant_Id");
			serializer.text(paticipant_Id);
			serializer.endTag("", "Paticipant_Id");
			
			serializer.startTag("", "Swap_Time");
			serializer.text(getCurrentTime());
			serializer.endTag("", "Swap_Time");
			
			if(event == EVENT_SWAP){
				serializer.startTag("", "Swap_Event");
				serializer.text("1");
				serializer.endTag("", "Swap_Event");
				
				serializer.startTag("", "Restarted_Event");
				serializer.text("0");
				serializer.endTag("", "Restarted_Event");
				
				serializer.startTag("", "LocationChanged_Event");
				serializer.text("0");
				serializer.endTag("", "LocationChanged_Event");
			}
			else if(event == EVENT_CHANGE){
				serializer.startTag("", "Swap_Event");
				serializer.text("0");
				serializer.endTag("", "Swap_Event");
				
				serializer.startTag("", "Restarted_Event");
				serializer.text("0");
				serializer.endTag("", "Restarted_Event");
				
				serializer.startTag("", "LocationChanged_Event");
				serializer.text("1");
				serializer.endTag("", "LocationChanged_Event");
			}
			for (int i = 0; i < Mac_Id.length; i++) {
				if(Mac_Id[i] != null){
					serializer.startTag("","Swapped_Sensor");
					serializer.attribute("","Mac_Id",Mac_Id[i]);
					serializer.text(location[i]);
					serializer.endTag("","Swapped_Sensor");	
				}
			}
	
			serializer.endTag("","row");
			serializer.endTag("","SWAPPING");
			serializer.endDocument();
			return writer.toString();
		}
		catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	
	public String[] getMac_Id() {
		return Mac_Id;
	}

	public void setMac_Id(String[] mac_Id) {
		Mac_Id = mac_Id;
	}

	public String[] getLocation() {
		return location;
	}

	public void setLocation(String[] location) {
		this.location = location;
	}
	public String getCurrentTime(){
		Calendar now = Calendar.getInstance();
		String month = null;
		if((now.get(Calendar.MONTH)+1)<10){
			month = "0"+(now.get(Calendar.MONTH)+1);
		}
		else 
			month = (now.get(Calendar.MONTH)+1)+"";
		
		String day = null;
		if(now.get(Calendar.DAY_OF_MONTH)<10){
			day ="0"+ now.get(Calendar.DAY_OF_MONTH);
		}
		else
			day = now.get(Calendar.DAY_OF_MONTH)+"";
		
		String hour = null;
		if(now.get(Calendar.HOUR_OF_DAY)<10){
			hour = "0"+now.get(Calendar.HOUR_OF_DAY);
		}
		else
			hour = ""+now.get(Calendar.HOUR_OF_DAY);
		
		String minute = null;
		if(now.get(Calendar.MINUTE)<10){
			minute = "0"+now.get(Calendar.MINUTE);
		}
		else
			minute = ""+now.get(Calendar.MINUTE);
		
		String second = null;
			if(now.get(Calendar.SECOND)<10){
				second = "0"+now.get(Calendar.SECOND);
			}
			else
				second = ""+now.get(Calendar.SECOND);
		String time = now.get(Calendar.YEAR)+"-"+month+"-"+day+"T"+hour+":"+minute+":"+second;
		 return time;
	}
}
