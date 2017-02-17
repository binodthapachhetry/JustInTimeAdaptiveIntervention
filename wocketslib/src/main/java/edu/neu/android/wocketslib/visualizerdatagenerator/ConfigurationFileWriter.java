package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.Log;

public class ConfigurationFileWriter {
	private static final String TAG = "ConfigurationFileWriter";
	private String path;
	private String fileName = "Configuration.xml";
	
	public ConfigurationFileWriter(Calendar time) {
		super();
		Date now = time.getTime();
		SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
		path = Globals.EXTERNAL_DIRECTORY_PATH + File.separator + Globals.DATA_DIRECTORY + File.separator + day.format(now) + "/wockets";
	}

	public void writeFile(){
		XmlSerializer xmlSerializer = Xml.newSerializer();
		try {
			File sensorInfoFile = new File(path + File.separator + fileName);
			if(!sensorInfoFile.exists()){
				File directory = new File(path);
				directory.mkdirs();
				sensorInfoFile.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(sensorInfoFile));

			xmlSerializer.setOutput(writer);
			xmlSerializer.startDocument("UTF-8",true);
			
			xmlSerializer.startTag(null, "CONFIGURATION");
			xmlSerializer.attribute(null, "version", "1.42");
			xmlSerializer.attribute(null, "mode", "debug");
			xmlSerializer.attribute(null, "memorymode", "non_shared");
			
				xmlSerializer.startTag(null, "FEATURES");
				xmlSerializer.attribute(null, "fft_interpolation_power", "7");
				xmlSerializer.attribute(null, "fft_maximum_frequencies", "2");
				xmlSerializer.attribute(null, "smooth_window_count", "4");
				xmlSerializer.attribute(null, "feature_window_size", "1000");
				xmlSerializer.attribute(null, "feature_window_overlap", "0.5");
				xmlSerializer.attribute(null, "error_window_size", "1000");
				xmlSerializer.attribute(null, "maximum_consecutive_packet_loss", "20");
				xmlSerializer.attribute(null, "maximum_nonconsecutive_packet_loss", "0.8");
				
				xmlSerializer.endTag(null, "FEATURES");
			xmlSerializer.endTag(null, "CONFIGURATION");
			xmlSerializer.endDocument();
			
			xmlSerializer.flush();
			writer.flush();
			writer.close();
			
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Error IllegalArgumentException in writeFile: " + e.toString());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e(TAG, "Error IllegalStateException in writeFile: " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "Error IOException in writeFile: " + e.toString());
			e.printStackTrace();
		}
	}

}
