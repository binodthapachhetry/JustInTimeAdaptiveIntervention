package edu.neu.android.wocketslib.visualizerdatagenerator;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.sensormonitor.AccelPoint;
import edu.neu.android.wocketslib.utils.Log;

public class DataEncoder {
	private final static String TAG = "DataEncoder";
	private String localPath;
	private static String configurationFileName = "SensorData.xml";
	private int previousX;
	private int previousY;
	private int previousZ;
	private double prevTimeStamp;
	private byte[] data;
	private byte[] timeInBytes;
	public double TIME_DIFF = 25; // default sampling rate is 40Hz

	private long ref_time;

	public DataEncoder() {
		resetPrevData();
	}

	public double getTIME_DIFF() {
		return TIME_DIFF;
	}

	public void setTIME_DIFF(double fAKE_TIME_DIFF) {
		TIME_DIFF = fAKE_TIME_DIFF;
	}

	/*
	 * *****************************************************************************
	 * ******************Encoding code for PC or continuous
	 * mode********************
	 * **************************************************
	 * ***************************
	 */
	public void encodeAndSaveData(Calendar time, int x, int y, int z, String ID) {
		FileOutputStream outputStream = null;
		localPath = "c:/test/";
		try {
			long timeStamp = time.getTimeInMillis();
			String outputFilePath = getFilePathByTime(time);
			boolean isNewFile = false;
			File outputDir = new File(outputFilePath);
			if (!outputDir.isDirectory()) {
				outputDir.mkdirs();
			}
			// String outputFileName = getFileNameForCurrentHour(outputFilePath,
			// time, ID);
			String outputFileName = "WocketSensor.00.XXXXXXXXXXXX.baf";
			File outputFile = new File(outputFilePath + outputFileName);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
				isNewFile = true;
			}
			outputStream = new FileOutputStream(outputFile, true);

			if (isNewFile) {
				resetPrevData();
			}
			encodeTime(timeStamp);
			encoRawData(x, y, z);
			outputStream.write(timeInBytes);
			outputStream.write(data);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * *****************************************************************************
	 * ******************Encoding code for Android or burst
	 * mode*******************
	 * ***************************************************
	 * **************************
	 */

	public void encodeAndSaveData(Calendar time,
			ArrayList<AccelPoint> accelPoints, String MacID, Context c) {

		if (accelPoints != null && accelPoints.size() > 0) {
			BufferedOutputStream outputStream = null;
			// localPath = c.getFilesDir()+"/";
			if (Globals.IS_SENSOR_DATA_EXTERNAL) {
				localPath = Globals.EXTERNAL_DIRECTORY_PATH;
			} else {
				localPath = Globals.INTERNAL_DIRECTORY_PATH;
			}
			localPath += File.separator
					+ Globals.DATA_MHEALTH_SENSORS_DIRECTORY + File.separator;

			Log.i("some", localPath);
			String outputFilePath = getFilePathByTime(time);
			String outputFileName = getFileNameForCurrentHour(outputFilePath,
					time, c, MacID);
			try {
				File outputDir = new File(outputFilePath);
				if (!outputDir.isDirectory()) {
					if (!outputDir.mkdirs())
						Log.e(TAG,
								"Error: can not make output directory in memory.");
				}
				File outputFile = new File(outputFilePath + outputFileName);
				if (!outputFile.exists()) {
					if (!outputFile.createNewFile())
						Log.e(TAG,
								"Error: can not create output file in memory.");
				}

				outputStream = new BufferedOutputStream(new FileOutputStream(
						outputFilePath + outputFileName, true));
				AccelPoint accelPoint = accelPoints.get(0);
				byte[] timeInBytes = encodeFullTimeStamp((long) (time
						.getTimeInMillis() - accelPoints.size()
						* Math.round(TIME_DIFF)));
				byte[] dataInBytes = encoRawData(accelPoint.getmX(),
						accelPoint.getmY(), accelPoint.getmZ());
				outputStream.write(timeInBytes);
				outputStream.write(dataInBytes);

				// for the following data, just put fake time diff and save data
				for (int i = 1; i < accelPoints.size(); i++) {
					accelPoint = accelPoints.get(i);
					timeInBytes = encodeDiffTimeStamp(TIME_DIFF);
					dataInBytes = encoRawData(accelPoint.getmX(),
							accelPoint.getmY(), accelPoint.getmZ());
					outputStream.write(timeInBytes);
					outputStream.write(dataInBytes);
				}
				// for testing purpose
				// outputRawDataInCSV(time,accelPoints,ID);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: can not find BAF file. File path: "
						+ outputFilePath + outputFileName + ". System error: "
						+ e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: can not write BAF file. File path: "
						+ outputFilePath + outputFileName + ". System error: "
						+ e.toString());
			} finally {
				try {
					if (outputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private void resetPrevData() {
		previousX = 0;
		previousY = 0;
		previousZ = 0;
		prevTimeStamp = 0;
		ref_time = 0;
	}

	private void encodeTime(long stamp) {
		if ((prevTimeStamp == 0) || (stamp - prevTimeStamp > 127)
				|| (stamp - ref_time > 60000)) {
			encodeFullTimeStamp(stamp);
			ref_time = stamp;
		} else {
			encodeDiffTimeStamp(stamp - prevTimeStamp);
		}
	}

	private byte[] encodeTime1(long stamp) {
		if ((prevTimeStamp == 0) || (stamp - prevTimeStamp > 127)
				|| (stamp - ref_time > 60000)) {
			ref_time = stamp;
			return encodeFullTimeStamp(stamp);

		} else {
			return encodeDiffTimeStamp(stamp - prevTimeStamp);
		}
	}

	public synchronized byte[] encodeFullTimeStamp(long time) {
		prevTimeStamp = time;
		Calendar timeStamp = Calendar.getInstance();
		timeStamp.setTimeInMillis(time);
		int year = timeStamp.get(Calendar.YEAR);
		int month = timeStamp.get(Calendar.MONTH);
		month++;
		int day = timeStamp.get(Calendar.DAY_OF_MONTH);

		String tag = "encodeFullTimeStamp";
		int mills = (int) (timeStamp.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
				+ timeStamp.get(Calendar.MINUTE) * 60 * 1000
				+ timeStamp.get(Calendar.SECOND) * 1000 + timeStamp
				.get(Calendar.MILLISECOND));

		Log.i(tag, year + " | " + month + " | " + day + " | " + time);

		timeInBytes = new byte[8];
		timeInBytes[0] = (byte) ((byte) ((year >>> 8)) | 0x80);
		timeInBytes[1] = (byte) (year);
		timeInBytes[2] = (byte) (month);
		timeInBytes[3] = (byte) (day);
		timeInBytes[4] = (byte) (mills >>> 24);
		timeInBytes[5] = (byte) (mills >>> 16);
		timeInBytes[6] = (byte) (mills >>> 8);
		timeInBytes[7] = (byte) (mills);

		return timeInBytes;
	}

	public byte[] encodeDiffTimeStamp(double diff) {
		double newTime = prevTimeStamp + diff;
		long roundedNewTime = Math.round(newTime);
		double updatedDiff = roundedNewTime - Math.round(prevTimeStamp);
		timeInBytes = new byte[1];
		timeInBytes[0] = (byte) updatedDiff;

		prevTimeStamp += diff;
		return timeInBytes;
	}

	public byte[] encoRawData(int x, int y, int z) {
		if (previousX == 0 && previousY == 0 && previousZ == 0) {
			previousX = x;
			previousY = y;
			previousZ = z;
			uncompressedData(x, y, z);
		} else {
			int dx, dy, dz;
			dx = x - previousX;
			dy = y - previousY;
			dz = z - previousZ;
			previousX = x;
			previousY = y;
			previousZ = z;
			if (dx >= -15 && dx <= 15 && dy >= -15 && dy <= 15 && dz >= -15
					&& dz <= 15) {
				compressedData(dx, dy, dz);
			} else
				uncompressedData(x, y, z);
		}
		return data;
	}

	private void compressedData(int dx, int dy, int dz) {
		data = new byte[2];
		if (dx < 0) {
			dx *= -1;
			data[0] = (byte) (((byte) (dx << 2) & 0x3c) | 0x40);
		} else
			data[0] = (byte) (((byte) (dx << 2) & 0x3c));
		if (dy < 0) {
			dy *= -1;
			data[0] = (byte) (data[0] | 0x02 | ((byte) dy >> 3));
			data[1] = (byte) (dy << 5);
		} else {
			data[0] = (byte) (data[0] | ((byte) dy >> 3));
			data[1] = (byte) (dy << 5);
		}
		if (dz < 0) {
			dz *= -1;
			data[1] = (byte) (data[1] | 0x10 | (byte) dz);
		} else
			data[1] = (byte) (data[1] | (byte) dz);
	}

	private void uncompressedData(int x, int y, int z) {
		data = new byte[4];
		data[0] = (byte) ((x >>> 4) | 0xc0);
		data[1] = (byte) (((x << 4) & 0xf0) | ((y >>> 6) & 0x0f));
		data[2] = (byte) (((y << 2) & 0xfc) | ((z >>> 8) & 0x03));
		data[3] = (byte) (z);
	}

	/**
	 * Gets the path for the raw data file by day and hour.
	 *
	 * @return the path by time
	 */
	private synchronized String getFilePathByTime(Calendar time) {
		Date timeStamp = time.getTime();
		SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hour = new SimpleDateFormat("HH");
		String path = localPath + day.format(timeStamp) + "/"
				+ hour.format(timeStamp) + "/";
		return path;
	}

	/**
	 * Checks the file name for current hour. If there is a file exists, return
	 * it; if not, generate the file name by the time.
	 *
	 * @param path
	 *            the path
	 * @return the file name for current hour
	 */
	private String getFileNameForCurrentHour(String path, Calendar time,
			Context c, final String MacID) {
		/*
		 * SensorDataInfo sensorInfo = null; List<SwappedSensor> sensors =
		 * WocketInfoGrabber.getSwappedSensors(c); if(sensors.size() > 0){ for
		 * (SwappedSensor swappedSensor : sensors) {
		 * if(swappedSensor.macID.equals(MacID)){ sensorInfo =new
		 * SensorDataInfo(); sensorInfo.setSwappedData(swappedSensor); } } }
		 */
		Date timeStamp = time.getTime();
		SimpleDateFormat detailedTime = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss-SSS");
		// if(sensorInfo != null){
		File dir = new File(path);
		// final String bodyLocation = sensorInfo.getBodyLocation();
		String[] files = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				return filename.startsWith("Wocket." + MacID)
						&& filename.endsWith(".baf");
			}
		});
		if (files == null || files.length == 0) {
			return "Wocket." + MacID + "." + detailedTime.format(timeStamp)
					+ ".baf";
		} else
			return files[0];
		// }
		// else{
		// return "Wocket."+MacID+detailedTime.format(timeStamp)+".baf";
	}

	private synchronized String getFileNameForCurrentHourForInternalAccel(
			String path, Calendar time, Context c, final String MacID) {

		Date timeStamp = time.getTime();
		SimpleDateFormat detailedTime = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss-SSS");
		// if(sensorInfo != null){
		File dir = new File(path);
		// final String bodyLocation = sensorInfo.getBodyLocation();
		String[] files = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				return filename.startsWith("PhoneAccelerometer." + MacID)
						&& filename.endsWith(".baf");
			}
		});
		if (files == null || files.length == 0) {
			return "PhoneAccelerometer." + MacID + "."
					+ detailedTime.format(timeStamp) + ".baf";
		} else
			return files[0];
		// }
		// else{
		// return "Wocket."+MacID+detailedTime.format(timeStamp)+".baf";
	}

	public synchronized void encodeAndSaveDataForInternalAccel(Calendar time,
			double x, double y, double z, String MacID, Context c) {

		BufferedOutputStream outputStream = null;
		// localPath = c.getFilesDir()+"/";
		if (Globals.IS_SENSOR_DATA_EXTERNAL) {
			localPath = Globals.EXTERNAL_DIRECTORY_PATH;
		} else {
			localPath = Globals.INTERNAL_DIRECTORY_PATH;
		}
		localPath += File.separator + Globals.DATA_MHEALTH_SENSORS_DIRECTORY
				+ File.separator;

		String outputFilePath = getFilePathByTime(time);
		String outputFileName = getFileNameForCurrentHourForInternalAccel(
				outputFilePath, time, c, MacID);
		// Log.d(TAG,"Mansoor outputFileName: "+outputFileName);

		try {
			File outputDir = new File(outputFilePath);
			if (!outputDir.isDirectory()) {
				if (!outputDir.mkdirs())
					Log.e(TAG,
							"Error: can not make output directory in memory.");
			}
			File outputFile = new File(outputFilePath + outputFileName);
			if (!outputFile.exists()) {
				if (!outputFile.createNewFile())
					Log.e(TAG, "Error: can not create output file in memory.");
			}

			outputStream = new BufferedOutputStream(new FileOutputStream(
					outputFilePath + outputFileName, true));

			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss.SSS");

			Log.i("debug", sdf.format(time.getTimeInMillis()) + " | " + x
					+ " | " + y + " | " + z);
			// byte[] timeInBytes = this.encodeTime1(time.getTimeInMillis());//
			// encodeFullTimeStamp(time.getTimeInMillis());
			byte[] timeInBytes = this.encodeFullTimeStamp(time
					.getTimeInMillis());// encodeFullTimeStamp(time.getTimeInMillis());
			int offsetNMultiplyWith = 100;
			// byte[] dataInBytes =
			// encoRawData((int)(x*100),(int)(y*100),(int)(z*100));
			byte[] dataInBytes = encoRawData((int) ((x + 4) * 100),
					(int) ((y + 4) * 100), (int) ((z + 4) * 100));
			// System.out.println(",Mansoor is testing,x:,"+(int)(x*100)+",y:,"+(int)(y*100)+",z:, "+(int)(x*100));
			outputStream.write(timeInBytes);
			outputStream.write(dataInBytes);

			// for testing purpose
			// outputRawDataInCSV(time,accelPoints,ID);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,
					"Error: can not find BAF file. File path: "
							+ outputFilePath + outputFileName
							+ ". System error: " + e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,
					"Error: can not write BAF file. File path: "
							+ outputFilePath + outputFileName
							+ ". System error: " + e.toString());
		} finally {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void encodeAndSaveDataForInternalAccel(Calendar time,
			ArrayList<AccelPoint> accelPoints, String MacID, Context c) {
		if (accelPoints != null && accelPoints.size() > 0) {
			BufferedOutputStream outputStream = null;
			// localPath = c.getFilesDir()+"/";
			if (Globals.IS_SENSOR_DATA_EXTERNAL) {
				localPath = Globals.EXTERNAL_DIRECTORY_PATH;
			} else {
				localPath = Globals.INTERNAL_DIRECTORY_PATH;
			}
			localPath += File.separator
					+ Globals.DATA_MHEALTH_SENSORS_DIRECTORY + File.separator;

			String outputFilePath = getFilePathByTime(time);
			String outputFileName = getFileNameForCurrentHourForInternalAccel(
					outputFilePath, time, c, MacID);
			// Log.d(TAG,"Mansoor outputFileName: "+outputFileName);

			try {
				File outputDir = new File(outputFilePath);
				if (!outputDir.isDirectory()) {
					if (!outputDir.mkdirs())
						Log.e(TAG,
								"Error: can not make output directory in memory.");
				}
				File outputFile = new File(outputFilePath + outputFileName);
				if (!outputFile.exists()) {
					if (!outputFile.createNewFile())
						Log.e(TAG,
								"Error: can not create output file in memory.");
				}

				outputStream = new BufferedOutputStream(new FileOutputStream(
						outputFilePath + outputFileName, true));
				AccelPoint accelPoint = accelPoints.get(0);
				byte[] timeInBytes = encodeFullTimeStamp((long) (time
						.getTimeInMillis() - accelPoints.size() * TIME_DIFF));
				byte[] dataInBytes = encoRawData(accelPoint.getmX(),
						accelPoint.getmY(), accelPoint.getmZ());
				outputStream.write(timeInBytes);
				outputStream.write(dataInBytes);
				Log.o("FAHAD", "Mansoor xyz: " + accelPoint.getmX() + " "
						+ accelPoint.getmY() + " " + accelPoint.getmZ());
				Log.o("Mansoor", "For loop");
				// for the following data, just put fake time diff and save data
				for (int i = 1; i < accelPoints.size(); i++) {
					accelPoint = accelPoints.get(i);
					timeInBytes = encodeDiffTimeStamp((long) TIME_DIFF);
					Log.o("Mansoor111",
							"Mansoor xyz: " + accelPoint.getmX() + " "
									+ accelPoint.getmY() + " "
									+ accelPoint.getmZ());
					Log.d(TAG, "Mansoor xyz: " + accelPoint.getmX() + " "
							+ accelPoint.getmY() + " " + accelPoint.getmZ());
					dataInBytes = encoRawData(accelPoint.getmX(),
							accelPoint.getmY(), accelPoint.getmZ());
					outputStream.write(timeInBytes);
					outputStream.write(dataInBytes);
				}
				// for testing purpose
				// outputRawDataInCSV(time,accelPoints,ID);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: can not find BAF file. File path: "
						+ outputFilePath + outputFileName + ". System error: "
						+ e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: can not write BAF file. File path: "
						+ outputFilePath + outputFileName + ". System error: "
						+ e.toString());
			} finally {
				try {
					if (outputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * save raw data as well for testing purpose
	 * 
	 * @param time
	 * @param accelPoints
	 * @param ID
	 */
	public File saveRawDataInCSV(Calendar time,
			ArrayList<AccelPoint> accelPoints, String ID) {
		long startTime = (long) (time.getTimeInMillis() - accelPoints.size()
				* TIME_DIFF);
		String content = new String();
		AccelPoint accelPoint = null;
		content += startTime + ",-1,-1," + accelPoints.size() + "\r\n";
		content += time.getTimeInMillis() + ",-1,-1," + TIME_DIFF + "\r\n";
		for (int i = 0; i < accelPoints.size(); i++) {
			accelPoint = accelPoints.get(i);
			content += startTime + "," + accelPoint.getmX() + ","
					+ accelPoint.getmY() + "," + accelPoint.getmZ() + "\r\n";
			startTime += TIME_DIFF;
		}
		BufferedWriter writer = null;
		try {
			File file = new File(getFilePathByTime(time) + "rawData_" + ID
					+ ".csv");
			if (!file.exists())
				file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file, true));
			writer.write(content);
			return file;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/*
	 * *****************************************************************************
	 * ****************** Encoding code for Android or burst mode to store data
	 * in a file oldata.baf, to be used by the next burst cycle.
	 * *******************
	 * *******************************************************
	 * **********************
	 */

	public void encodeAndSaveOldData(double data[][], Date lastConnectionTime,
			String macID) {

		if (data != null && data[0].length > 0) {
			BufferedOutputStream outputStream = null;
			// localPath = c.getFilesDir()+"/";
			if (Globals.IS_SENSOR_DATA_EXTERNAL) {
				localPath = Globals.EXTERNAL_DIRECTORY_PATH;
			} else {
				localPath = Globals.INTERNAL_DIRECTORY_PATH;
			}

			localPath = Globals.EXTERNAL_DIRECTORY_PATH;

			localPath += File.separator
					+ Globals.DATA_MHEALTH_SENSORS_DIRECTORY + File.separator;

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lastConnectionTime);
			// String outputFilePath = getFilePathByTime(calendar);

			String outputFilePath = localPath;

			String outputFileName = macID + ".old_data.baf";
			try {
				File outputDir = new File(outputFilePath);
				if (!outputDir.isDirectory()) {
					if (!outputDir.mkdirs())
						Log.e(TAG,
								"Error: can not make output directory in memory.");
				}
				File outputFile = new File(outputFilePath + outputFileName);
				if (!outputFile.exists()) {
					if (!outputFile.createNewFile())
						Log.e(TAG,
								"Error: can not create output file in memory.");
				}

				outputStream = new BufferedOutputStream(new FileOutputStream(
						outputFilePath + outputFileName, true));

				ByteArrayBuffer buff = new ByteArrayBuffer(data[0].length);

				for (int i = 0; i < data[0].length-1; i++) {
//					Log.i("encodeFullTimeStamp", String.format("%.0f",data[3][i])+" | "+(long)data[3][i]);
					byte[] timeInBytes = encodeFullTimeStamp((long)data[3][i]);
					byte[] dataInBytes = encoRawData(
							(int) Math.round(data[0][i]),
							(int) Math.round(data[1][i]),
							(int) Math.round(data[2][i]));
					buff.append(timeInBytes, 0, timeInBytes.length);
					buff.append(dataInBytes, 0, dataInBytes.length);
				}

				outputStream.write(buff.toByteArray());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: can not find BAF file. File path: "
						+ outputFilePath + outputFileName + ". System error: "
						+ e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error: can not write BAF file. File path: "
						+ outputFilePath + outputFileName + ". System error: "
						+ e.toString());
			} finally {
				try {
					if (outputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
