/******************************************************************************
 * 
 * @author Kyle Bechtel
 * @date  6/1/11
 * @brief Abstract class that models data and functions common to all Heart Rate sensors
 * 
 * 
 *****************************************************************************/

package edu.neu.android.wocketslib.sensormonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;

import android.text.format.Time;

public abstract class HeartRateSensor extends Sensor {

	//Variables that track the heart rate values
	public int mMinRate = 0;
	public int mMaxRate = 0;
	public int mAvgRate = 0;
	public int mCurrentRate = 0;
	
	//The number of heart rate values that have been recorded in the current day.
	//Used to calculate average HR
	public int mRateCount = 0;
	//The sum of all the heart values for the current day.  Used to calculate the average
	public int mRateTotal=0;
	//A list of all the previous heart rate points for the day that haven't been written
	// to NV storage.
	public ArrayList<HRPoint> mHRPoints;
	//The trailing average heart rate.  Calculated using the variable in DataStore to determine
	// how far back this should go
	public int mTrailingAvg = 0;
	
	//An array of the previous week's daily heart rate averages
	public int prevWeekAvg[];

	/**
	 * Constructor
	 * @param type - The type of sensor
	 * @param name - The Bluetooth device name
	 * @param address - The Bluetooth device MAC address
	 */
	public HeartRateSensor( int type, String name, String address)
	{
		super(type ,name, address);
		mMinRate = 0;
		mMaxRate = 0;
		mAvgRate = 0;
		mCurrentRate = 0;
		mRateCount = 0;
		mRateTotal=0;
		mHRPoints = new ArrayList<HRPoint>();
		mTrailingAvg = 0;
		prevWeekAvg = new int[7];
		
		loadNVData();
	}
	
	/**
	 * Reads in the previous week's heart rate files and calculates the daily average for each day.
	 * 
	 * Also reads in the current days running total of HR incase recording was stopped then restarted
	 */
	private void loadNVData()
	{
		Time now = new Time();
		now.setToNow();
		
		for( int x=0;x<7;x++)
		{
			now.setToNow();
			prevWeekAvg[x] = 0;

			now.set(now.monthDay-(7-x), now.month, now.year);
			now.normalize(true);
			
			String fileName = now.format(mName + "_%m_%d_%Y.txt");
			prevWeekAvg[x] = readSavedData(fileName, false);
		}
		
		//read the current day's data
		now.setToNow();
		String fileName = now.format(mName + "_%m_%d_%Y.txt");
		readSavedData(fileName, true);
	}
	
	private static Time getTimeFromDate(Date aDate)
	{
		long ms = aDate.getTime();
		Time aTime = new Time();
		aTime.set(ms);
		return aTime; 
	}

	/**
	 * Adds a decoded heart rate point to the list of previous values.  Updates the 
	 * statistics for the daily heart rate data and trailing average.
	 * 
	 * If the number of points stored in the point array is too large, excess values will be removed
	 * from the array and written out to a file in NV storage.
	 * 
	 * @param point - the new point to add
	 */
	public void addPoint( HRPoint point)
	{
		if( point.mRate > 0)
		{
			mHRPoints.add(point);
			
			if( point.mRate > Defines.MINIMUM_HEART_RATE_DURING_EXERCISE)
			{
				DataStore.incrementActivityScore();
			}

			if( point.mRate < mMinRate || mMinRate == 0)
			{
				mMinRate = point.mRate;
			}
			if( point.mRate > mMaxRate)
			{
				mMaxRate = point.mRate;
			}
			mRateCount++;
			mRateTotal+=point.mRate;
			mAvgRate = mRateTotal/mRateCount;
			mCurrentRate = point.mRate;

			
			//Calculate the trailing average HR
			int index = mHRPoints.size()-1;
			int trailingSum=0;
			int x;
			for( x=0;x<Defines.HEART_RATE_AVERAGE_TIME && index >= 0;x++)
			{
				trailingSum+=mHRPoints.get(index).mRate;
				index--;
			}
			if( x > 0)
			{
				mTrailingAvg = trailingSum/x;
				//Log.i("ActivityMonitor", "trailing " + mTrailingAvg);
			}
		}

		//If the array of HR points has grown too large, write the oldest points
		// to NV storage until the size in the array is small enough
		if( mHRPoints.size() > (Defines.HEART_RATE_AVERAGE_TIME *2))
		{
			if( DataStore.getContext()!= null)
			{
				Time now = new Time();
				now.setToNow();
				String fileName = now.format(mName + "_%m_%d_%Y.txt");
				File file = new File(DataStore.getContext().getExternalFilesDir(null), fileName);
				try {
					if( file != null)
					{
						if( !file.exists() )
						{
							loadNVData();
							file.createNewFile();
						}

						FileWriter out = new FileWriter(file, true);

						//remove all points except those needed for calculating trailing average
						while( mHRPoints.size() > Defines.HEART_RATE_AVERAGE_TIME)
						{
							Time aTime = getTimeFromDate(mHRPoints.get(0).mPhoneReadTime);
							out.write( aTime.format("%Y-%m-%d %H:%M:%S") + "," + mHRPoints.get(0).mRate + "\n");

							mHRPoints.remove(0);
						}
						out.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Helper function to read the contents of a file with a previous day's heart rate values
	 * and process the data.
	 * 
	 * @param fileName - The file name to read from
	 * @param updateMembers - if true, will save the min/max/average data that is calculated
	 * 					to the member variables to be saved for future use.  If false these new
	 * 					calculated values are lost
	 * @return - The average heart rate calculated from the data in the given file name.  Returns 0
	 * 			if the file could not be read
	 */
	private int readSavedData(String fileName, boolean updateMembers)
	{
		int average = 0, rateCount=0, rateTotal=0, min=0,max=0;
		if( DataStore.getContext()!= null)
		{
			File file = new File(DataStore.getContext().getExternalFilesDir(null), fileName);

			if( file != null && file.exists())
			{
				try
				{
					BufferedReader in = new BufferedReader( new FileReader(file));

					String nextLine = in.readLine();
					while( nextLine != null)
					{
						try
						{
							int rate = Integer.parseInt(nextLine.substring(nextLine.indexOf(",")+1));

							if( rate < min || min == 0)
							{
								min = rate;
							}
							if( rate > max)
							{
								max = rate;
							}
							rateCount++;
							rateTotal+=rate;
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						nextLine = in.readLine();
					}
					if( rateCount > 0)
					{
						average = rateTotal/rateCount;
					}
				}
				catch( Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if( updateMembers)
		{
			mMinRate = min;
			mMaxRate = max;
			mRateTotal = rateTotal;
			mRateCount = rateCount;
			mAvgRate = average;
		}
		
		return average;

	}

	/**
	 * Reset all fields to their default
	 */
	public void reset()
	{
		mMinRate = 0;
		mMaxRate = 0;
		mAvgRate = 0;
		mCurrentRate = 0;
		mRateCount = 0;
		mRateTotal=0;
		mTrailingAvg = 0;

		mHRPoints.clear();
	}
}
