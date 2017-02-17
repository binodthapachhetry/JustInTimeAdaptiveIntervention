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
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import android.util.Log;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.utils.FileHelper;
import edu.neu.android.wocketslib.utils.WOCKETSException;

/**
 * <p>
 * This class is used to write the mHealth format annotation XML file.
 * </p>
 * 
 * It hides the details of the mHealth file path, mHealth file format and XML writing.
 * Developers should use this class rather than write their own code for generating
 * the annotation XML file.
 * 
 * @author bigbug
 *
 */
public class AnnotationSaver {
	private final static String TAG = "AnnotationSaver";
	
	private String   mDataset;
	private String   mAnnotator;
	private String   mEmail;
	private String   mDescription;
	private String   mMethod;
	private String   mNotes;
		
	private Date	 mDate;
	private String   mDirPath;
	private Document mDocument;
	
	private boolean  mInitialized;
	
	public AnnotationSaver() {
		mInitialized = false;
		setDate(new Date());		
	}
	
	public void initialize(String dataset, String annotator, String email, 
			String description, String method, String notes) {
		initialize(false, dataset, annotator, email, description, method, notes);
	}

	/**
	 * 
	 * @param isAppend
	 *            if the file does exist, append content to the file if isAppend is true.
	 * @param dataset
	 * @param annotator
	 * @param email
	 * @param description
	 * @param method
	 * @param notes
	 */
	public void initialize(boolean isAppend, String dataset, String annotator, String email, 
			String description, String method, String notes) {
		
		if (mInitialized) {
			return;
		}
		mInitialized = true;
		
		mDataset     = dataset;
		mAnnotator   = annotator;
		mEmail       = email;
		mDescription = description;
		mMethod      = method;
		mNotes	     = notes;
		mDirPath = (Globals.IS_ANNOTATION_EXTERNAL ? Globals.EXTERNAL_DIRECTORY_PATH : Globals.INTERNAL_DIRECTORY_PATH) + 
				File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY+File.separator;
		Log.i(TAG,mDirPath);

		if (isAppend) {
			File file = getAnnotationFile();
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
			mDocument.addElement("ANNOTATIONS")
		        .addAttribute("DATASET", mDataset)
		        .addAttribute("ANNOTATOR", mAnnotator)
		        .addAttribute("EMAIL", mEmail)
		        .addAttribute("DESCRIPTION", mDescription)
		        .addAttribute("METHOD", mMethod)
		        .addAttribute("NOTES", mNotes);
		}				
	}
	
	public void setDate(Date date) {		
		mDate = date;
	}
	
	public void addAnnotation(String annotatorID, String labelID, String labelText,
			Date startDate, Date stopDate, String annotationSet, Date modifiedDate, Date createdDate) {		
		
		// ROOT ELEMENT
		Element annotations = mDocument.getRootElement();
		// ANNOTATION
		Element annotation = annotations.addElement("ANNOTATION")
			.addAttribute("GUID", annotatorID);
		// LABEL
		annotation.addElement("LABEL")
			.addAttribute("GUID", labelID)
			.addText(labelText);
		// START_DT
		annotation.addElement("START_DT")
//			.addText(Globals.mHealthTimestampFormat.format(startDate));
			.addText(Globals.mHealthTimestampFormat.format(startDate));
		// STOP_DT
		annotation.addElement("STOP_DT")
//			.addText(Globals.mHealthTimestampFormat.format(stopDate));
			.addText(Globals.mHealthTimestampFormat.format(stopDate));
		// PROPERTIE
		annotation.addElement("PROPERTIES")
			.addAttribute("ANNOTATION_SET", annotationSet)		       
			.addAttribute("LAST_MODIFIED", Globals.mHealthTimestampFormat.format(modifiedDate))
			.addAttribute("DATE_CREATED", Globals.mHealthTimestampFormat.format(createdDate));
	}

	public boolean commitToFile() {
		return commitToFile(null);
	}
	
	public boolean commitToFile(OutputFormat format) {
		boolean result = false;
		String filePath = getAnnotationFile().getAbsolutePath();
  
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public File getAnnotationFile() {		
		File dir = new File(mDirPath + Globals.mHealthDateDirFormat.format(mDate == null ? new Date() : mDate) + File.separator);
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml") && filename.contains(".annotation");
			}
		});
		
		if (files == null || files.length == 0) {			
	        String filename = mDataset + "." + mAnnotator + ".annotation.xml"; // TODO: I guess it should be annotator id
			return new File(dir.getAbsolutePath() + File.separator + filename);
		} 
		
		return new File(dir.getAbsolutePath() + File.separator + files[0]);
	}   
}
