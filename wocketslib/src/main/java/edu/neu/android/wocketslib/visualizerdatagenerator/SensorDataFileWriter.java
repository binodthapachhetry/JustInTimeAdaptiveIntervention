package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;
import edu.neu.android.wocketslib.Globals;

/**
 * Unused
 * 
 */
public class SensorDataFileWriter {
	private final static String TAG = "SensorDataFileWriter";
	private SensorDataInfo[] sensors;
	// private String phoneType;
	private Context c;
	private static String fileName = "SensorData.xml";

	public SensorDataFileWriter(Context c) {
		super();
		this.c = c;
		// sensors = DataStore.getSensorDataInfo(c);
		// phoneType = Build.MODEL;
	}

	public void writeConfigInternal() {
		try {
			File outputFile = new File(c.getFilesDir() + "/" + fileName);
			if (!outputFile.exists())
				outputFile.createNewFile();
			writeFile(outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeConfigExternal(Calendar.getInstance());
	}

	public void writeConfigExternal(Calendar time) {
		try {
			Date now = time.getTime();
			SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
			String path = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY + File.separator + day.format(now) + "/wockets/";
			File sensorInfoFile = new File(path + fileName);
			if (!sensorInfoFile.exists()) {
				File directory = new File(path);
				directory.mkdirs();
				sensorInfoFile.createNewFile();
			}
			writeFile(sensorInfoFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeFile(File output) {
		XmlSerializer xmlSerializer = Xml.newSerializer();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(output, false));
			xmlSerializer.setOutput(writer);
			xmlSerializer.startDocument("UTF-8", true);

			xmlSerializer.startTag(null, "SENSORDATA");
			xmlSerializer.attribute(null, "xmlns", "urn:mites-schema");
			xmlSerializer.attribute(null, "dataset", "house_n data");

			xmlSerializer.startTag(null, "RECEIVERS");
			if (sensors != null && sensors.length > 0) {
				for (int i = 0; i < sensors.length; i++) {
					SensorDataInfo sensor = sensors[i];
					xmlSerializer.startTag(null, "RECEIVER");
					// xmlSerializer.attribute(null, "id", ""+sensor.getID());
					xmlSerializer.attribute(null, "type", "RFCOMM");
					xmlSerializer.attribute(null, "MacAddress", sensor.getMacID());
					xmlSerializer.attribute(null, "PIN", "1234");
					xmlSerializer.attribute(null, "PortNumber", "9");
					xmlSerializer.attribute(null, "Parity", "False");
					xmlSerializer.attribute(null, "StopBit", "True");
					xmlSerializer.attribute(null, "BaudRate", "57600");
					xmlSerializer.attribute(null, "BufferSize", "4096");
					xmlSerializer.attribute(null, "MaxSR", "40");
					xmlSerializer.endTag(null, "RECEIVER");
				}
				xmlSerializer.startTag(null, "RECEIVER");
				xmlSerializer.attribute(null, "id", "" + sensors.length);
				xmlSerializer.attribute(null, "type", "HTCDiamond");
				xmlSerializer.attribute(null, "BufferSize", "1024");
				xmlSerializer.attribute(null, "MaxSR", "10");
				xmlSerializer.endTag(null, "RECEIVER");
			} else {
				xmlSerializer.startTag(null, "RECEIVER");
				xmlSerializer.attribute(null, "id", "0");
				xmlSerializer.attribute(null, "type", "HTCDiamond");
				xmlSerializer.attribute(null, "BufferSize", "1024");
				xmlSerializer.attribute(null, "MaxSR", "10");
				xmlSerializer.endTag(null, "RECEIVER");

			}
			xmlSerializer.endTag(null, "RECEIVERS");

			xmlSerializer.startTag(null, "DECODERS");
			if (sensors != null && sensors.length > 0) {
				for (int i = 0; i < sensors.length; i++) {
					SensorDataInfo sensor = sensors[i];
					xmlSerializer.startTag(null, "DECODER");
					// xmlSerializer.attribute(null, "id", ""+sensor.getID());
					xmlSerializer.attribute(null, "type", "Wockets");
					xmlSerializer.endTag(null, "DECODER");
				}
				xmlSerializer.startTag(null, "DECODER");
				xmlSerializer.attribute(null, "id", "" + sensors.length);
				xmlSerializer.attribute(null, "type", "HTCDiamondTouch");
				xmlSerializer.endTag(null, "DECODER");
			} else {
				xmlSerializer.startTag(null, "DECODER");
				xmlSerializer.attribute(null, "id", "0");
				xmlSerializer.attribute(null, "type", "HTCDiamondTouch");
				xmlSerializer.endTag(null, "DECODER");

			}
			xmlSerializer.endTag(null, "DECODERS");

			xmlSerializer.startTag(null, "SENSORS");
			if (sensors != null && sensors.length > 0) {
				for (int i = 0; i < sensors.length; i++) {
					SensorDataInfo sensor = sensors[i];
					xmlSerializer.startTag(null, "SENSOR");
					xmlSerializer.attribute(null, "class", "Wockets");
					xmlSerializer.attribute(null, "type", "ACCEL");

					xmlSerializer.startTag(null, "ID");
					// xmlSerializer.attribute(null, "id", ""+sensor.getID());
					xmlSerializer.endTag(null, "ID");

					xmlSerializer.startTag(null, "SR");
					xmlSerializer.attribute(null, "text", "40");
					xmlSerializer.endTag(null, "SR");

					xmlSerializer.startTag(null, "RANGE");
					xmlSerializer.attribute(null, "min", "0");
					xmlSerializer.attribute(null, "max", "1024");
					xmlSerializer.endTag(null, "RANGE");

					xmlSerializer.startTag(null, "OBJECT");
					xmlSerializer.attribute(null, "text", "");
					xmlSerializer.endTag(null, "OBJECT");

					xmlSerializer.startTag(null, "LOCATION");
					xmlSerializer.attribute(null, "text", sensor.getBodyLocation());
					xmlSerializer.endTag(null, "LOCATION");

					xmlSerializer.startTag(null, "DESCRIPTION");
					xmlSerializer.attribute(null, "text", "acc");
					xmlSerializer.endTag(null, "DESCRIPTION");

					xmlSerializer.startTag(null, "RECEIVER");
					xmlSerializer.attribute(null, "id", "" + i);
					xmlSerializer.endTag(null, "RECEIVER");

					xmlSerializer.startTag(null, "DECODER");
					xmlSerializer.attribute(null, "id", "" + i);
					xmlSerializer.endTag(null, "DECODER");

					xmlSerializer.startTag(null, "CALIBRATION");
					xmlSerializer.attribute(null, "x1g", "779.92");
					xmlSerializer.attribute(null, "xn1g", "241.40");
					xmlSerializer.attribute(null, "y1g", "747.20");
					xmlSerializer.attribute(null, "yn1g", "215.72");
					xmlSerializer.attribute(null, "z1g", "832.26");
					xmlSerializer.attribute(null, "zn1g", "293.57");
					xmlSerializer.attribute(null, "xstd", "0.25");
					xmlSerializer.attribute(null, "ystd", "0.26");
					xmlSerializer.attribute(null, "zstd", "0.31");
					xmlSerializer.endTag(null, "CALIBRATION");

					xmlSerializer.endTag(null, "SENSOR");
				}
				xmlSerializer.startTag(null, "SENSOR");
				xmlSerializer.attribute(null, "class", "HTCDiamondTouch");
				xmlSerializer.attribute(null, "type", "ACCEL");

				xmlSerializer.startTag(null, "ID");
				xmlSerializer.attribute(null, "id", "" + sensors.length);
				xmlSerializer.endTag(null, "ID");

				xmlSerializer.startTag(null, "SR");
				xmlSerializer.attribute(null, "text", "20");
				xmlSerializer.endTag(null, "SR");

				xmlSerializer.startTag(null, "RANGE");
				xmlSerializer.attribute(null, "min", "0");
				xmlSerializer.attribute(null, "max", "2048");
				xmlSerializer.endTag(null, "RANGE");

				xmlSerializer.startTag(null, "CHANNEL");
				xmlSerializer.attribute(null, "id", "83");
				xmlSerializer.endTag(null, "CHANNEL");

				xmlSerializer.startTag(null, "LOCATION");
				xmlSerializer.attribute(null, "text", "");
				xmlSerializer.endTag(null, "LOCATION");

				xmlSerializer.startTag(null, "DESCRIPTION");
				xmlSerializer.attribute(null, "text", "acc");
				xmlSerializer.endTag(null, "DESCRIPTION");

				xmlSerializer.startTag(null, "RECEIVER");
				xmlSerializer.attribute(null, "id", "" + sensors.length);
				xmlSerializer.endTag(null, "RECEIVER");

				xmlSerializer.startTag(null, "DECODER");
				xmlSerializer.attribute(null, "id", "" + sensors.length);
				xmlSerializer.endTag(null, "DECODER");

				xmlSerializer.startTag(null, "CALIBRATION");
				xmlSerializer.attribute(null, "xmean", "299.10");
				xmlSerializer.attribute(null, "ymean", "262.50");
				xmlSerializer.attribute(null, "zmean", "280.36");
				xmlSerializer.attribute(null, "xstd", "0.94");
				xmlSerializer.attribute(null, "ystd", "1.07");
				xmlSerializer.attribute(null, "zstd", "0.89");
				xmlSerializer.endTag(null, "CALIBRATION");

				xmlSerializer.endTag(null, "SENSOR");
			} else {
				xmlSerializer.startTag(null, "SENSOR");
				xmlSerializer.attribute(null, "class", "HTCDiamondTouch");
				xmlSerializer.attribute(null, "type", "ACCEL");

				xmlSerializer.startTag(null, "ID");
				xmlSerializer.attribute(null, "id", "0");
				xmlSerializer.endTag(null, "ID");

				xmlSerializer.startTag(null, "SR");
				xmlSerializer.attribute(null, "text", "20");
				xmlSerializer.endTag(null, "SR");

				xmlSerializer.startTag(null, "RANGE");
				xmlSerializer.attribute(null, "min", "0");
				xmlSerializer.attribute(null, "max", "2048");
				xmlSerializer.endTag(null, "RANGE");

				xmlSerializer.startTag(null, "CHANNEL");
				xmlSerializer.attribute(null, "id", "83");
				xmlSerializer.endTag(null, "CHANNEL");

				xmlSerializer.startTag(null, "LOCATION");
				xmlSerializer.attribute(null, "text", "");
				xmlSerializer.endTag(null, "LOCATION");

				xmlSerializer.startTag(null, "DESCRIPTION");
				xmlSerializer.attribute(null, "text", "acc");
				xmlSerializer.endTag(null, "DESCRIPTION");

				xmlSerializer.startTag(null, "RECEIVER");
				xmlSerializer.attribute(null, "id", "0");
				xmlSerializer.endTag(null, "RECEIVER");

				xmlSerializer.startTag(null, "DECODER");
				xmlSerializer.attribute(null, "id", "0");
				xmlSerializer.endTag(null, "DECODER");

				xmlSerializer.startTag(null, "CALIBRATION");
				xmlSerializer.attribute(null, "xmean", "299.10");
				xmlSerializer.attribute(null, "ymean", "262.50");
				xmlSerializer.attribute(null, "zmean", "280.36");
				xmlSerializer.attribute(null, "xstd", "0.94");
				xmlSerializer.attribute(null, "ystd", "1.07");
				xmlSerializer.attribute(null, "zstd", "0.89");
				xmlSerializer.endTag(null, "CALIBRATION");

				xmlSerializer.endTag(null, "SENSOR");

			}
			xmlSerializer.endTag(null, "SENSORS");
			xmlSerializer.endTag(null, "SENSORDATA");
			xmlSerializer.endDocument();

			xmlSerializer.flush();
			writer.flush();
			writer.close();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * read SensorData.xml file and extract the sensor data info
	 * 
	 * @param c
	 * @param time
	 * @return
	 */
	public static ArrayList<SensorDataInfo> checkSavedInfo(Context c, String time) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			SensorDataFileChecker dataHandler = new SensorDataFileChecker();
			xr.setContentHandler(dataHandler);
			String path = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY + File.separator + time + "/wockets/";
			File sensorInfoFile = new File(path + fileName);
			if (sensorInfoFile.exists()) {
				xr.parse(new InputSource(new FileInputStream(sensorInfoFile)));
			}
			return dataHandler.sensors;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
