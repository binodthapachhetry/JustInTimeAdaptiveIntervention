	package edu.neu.android.wocketslib.mhealthformat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import android.util.Log;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.WOCKETSException;

/**
 * <p>
 * This class is used to write the mHealth format location KML file.
 * </p>
 * 
 * It hides the details of the mHealth file path, mHealth file format and KML writing.
 * Developers should use this class rather than write their own code for generating
 * the location KML file.
 * 
 * Format of the File:
 * 
 * 
 * @author Dharam maniar
 *
 */
public class LocationSaver {
	private final static String TAG = "LocationSaver";
	
	private String   mDataset;
	private String   mAnnotator;
		
	private Date	 mDate;
	private String   mDirPath;
	private Document mDocument;
	
	private boolean  mInitialized;
	
	public LocationSaver() {
		mInitialized = false;
		setDate(new Date());		
	}
	
	public void initialize(String dataset, String annotator) {
		initialize(false, dataset, annotator);
	}

	/**
	 * 
	 * @param isAppend
	 *            if the file does exist, append content to the file if isAppend is true.
	 * @param dataset
	 * @param annotator
	 */
	public void initialize(boolean isAppend, String dataset, String annotator) {
		
		if (mInitialized) {
			return;
		}
		mInitialized = true;
		
		mDataset     = dataset;
		mAnnotator   = annotator;
		mDirPath = (Globals.IS_ANNOTATION_EXTERNAL ? Globals.EXTERNAL_DIRECTORY_PATH : Globals.INTERNAL_DIRECTORY_PATH) + 
				File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY+File.separator;
		Log.i(TAG,mDirPath);

		if (isAppend) {
			File file = getLocationFile();
			if (file.exists()) {			
				try {
					mDocument = new SAXReader().read(file);
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if (mDocument == null) {
			mDocument = DocumentHelper.createDocument();
			// Add root
			Namespace namespace = new Namespace("", "http://www.opengis.net/kml/2.2");
			mDocument.addElement("kml");
			mDocument.getRootElement().addElement("Document");
			mDocument.getRootElement().add(namespace);
		}				
	}
	
	public void setDate(Date date) {		
		mDate = date;
	}
	
	public void addLocation(String labelText,
			Date startDate, Date stopDate, String startCoordinates, String stopCoordinates) {		
		
		// ROOT ELEMENT
		Element document = mDocument.getRootElement().element("Document");
		// ANNOTATION
		Element startPlacemark = document.addElement("Placemark");
		startPlacemark.addElement("name").addText(labelText + " Start");
		startPlacemark.addElement("description").addText("Start Time = " + Globals.mHealthTimestampFormat.format(startDate));
		Element startPoint = startPlacemark.addElement("Point");
		Element startCoordinate = startPoint.addElement("coordinates").addText(startCoordinates);
		
		Element stopPlacemark = document.addElement("Placemark");		
		stopPlacemark.addElement("name").addText(labelText + " Stop");
		stopPlacemark.addElement("description").addText("Stop Time = " + Globals.mHealthTimestampFormat.format(stopDate));
		Element stopPoint = stopPlacemark.addElement("Point");
		Element stopCoordinate = stopPoint.addElement("coordinates").addText(stopCoordinates);
		
	}

	public boolean commitToFile() {
		return commitToFile(null);
	}
	
	public boolean commitToFile(OutputFormat format) {
		boolean result = false;
		String filePath = getLocationFile().getAbsolutePath();
  
		FileOutputStream fos = null;
		try {			
			FileHelper.createDirsIfDontExist(filePath);
			fos = new FileOutputStream(filePath);
			// The file will be truncated if it exists, and created if it doesn't exist.
			// Can be optimized by inserting string directly to the file			
			XMLWriter writer = (format != null ? new XMLWriter(fos, format) : new XMLWriter(fos)); 		
			writer.write(mDocument);
	        writer.close();
	        result = true;	        
	        Log.d(TAG, "Commit xml document to file successfully!");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.d(TAG, "UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d(TAG, "FileNotFoundException");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "IOException");
		} catch (WOCKETSException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public File getLocationFile() {		
		File dir = new File(mDirPath + Globals.mHealthDateDirFormat.format(mDate == null ? new Date() : mDate) + File.separator);
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".kml") && filename.contains(".location");
			}
		});
		
		if (files == null || files.length == 0) {			
	        String filename = mDataset + "." + mAnnotator + ".location.kml";
			return new File(dir.getAbsolutePath() + File.separator + filename);
		} 
		
		return new File(dir.getAbsolutePath() + File.separator + files[0]);
	}   
}
