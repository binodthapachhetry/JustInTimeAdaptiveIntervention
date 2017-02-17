package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 * Unused
 */
public class SensorDataFileChecker extends DefaultHandler{
	private final static String TAG = "SensorDataFileChecker";
	public ArrayList<SensorDataInfo> sensors = new ArrayList<SensorDataInfo>();
	private int tempID = -1;
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		if(localName.equalsIgnoreCase("receiver")){
			SensorDataInfo sensor = new SensorDataInfo();
			for (int i = 0; i < attributes.getLength(); i++) {
				if(attributes.getLocalName(i).equalsIgnoreCase("macaddress")){
					sensor.setMacID(attributes.getValue(i));
				}
				if(attributes.getLocalName(i).equalsIgnoreCase("id")){
//					sensor.setID(Integer.parseInt(attributes.getValue(i)));
				}
			}
			if(sensor.getMacID() != null)
				sensors.add(sensor);
		}
		if(localName.equalsIgnoreCase("ID")){
			for (int i = 0; i < attributes.getLength(); i++) {
				if(attributes.getLocalName(i).equalsIgnoreCase("id"))
					tempID = Integer.parseInt(attributes.getValue(i));
			}
		}
		if(localName.equalsIgnoreCase("LOCATION")){
			if(tempID != -1){
				SensorDataInfo sensor = new SensorDataInfo();
				for (int i = 0; i < sensors.size(); i++) {
					SensorDataInfo dataInfo = sensors.get(i);
//					if(dataInfo.getID() == tempID){
//						sensor = dataInfo;
//						sensors.remove(dataInfo);
//					}
				}
//				if(sensor.getID() != -1){
//					String location = "";
//					for (int i = 0; i < attributes.getLength(); i++) {
//						if(attributes.getLocalName(i).equalsIgnoreCase("text"))
//							location = attributes.getValue(i);
//					}
//					sensor.setBodyLocation(location);
//					Log.d(TAG, "new Sensor Added: "+sensor.toString());
//					sensors.add(sensor);
//				}
			}
		}
	}
}
