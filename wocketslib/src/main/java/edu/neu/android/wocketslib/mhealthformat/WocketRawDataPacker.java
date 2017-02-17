package edu.neu.android.wocketslib.mhealthformat;

import java.io.FileOutputStream;

import edu.neu.android.wocketslib.utils.Log;


public class WocketRawDataPacker extends RawDataPacker {
	private static final String TAG = "WocketRawDataPacker";
	public static final String FILE_EXT = ".baf";
	private static final int SIGN_BIT_1 = 1;
	private static final int SIGN_BIT_0 = 0;

	private int signX; 	
	private int signY; 
	private int signZ; 
	private short diffX;
	private short diffY;
	private short diffZ; 

	private byte currByte = (byte) 0x00; 
	private byte tempByte;
	private byte[] outCompressed = new byte[2];
	private byte[] outUncompressed = new byte[4];

	private boolean isPreviousSensorValuesDefined = false; 
	private short x;
	private short y;
	private short z;
	private int mPrevSensorX = 0;
	private int mPrevSensorY = 0;
	private int mPrevSensorZ = 0;	

	public WocketRawDataPacker(SensorData.TYPE aType, int maxSizeByteArray) {
		super(aType, maxSizeByteArray);
	}

	@Override
	public void packRaw(SensorData aSensorDataPoint, boolean isForceNoDifferentialCompression, FileOutputStream bw) {
		if (aSensorDataPoint.mType != mType)
			Log.e(TAG, "Error: Wrong type of decoder called");
		else
		{
			mNumEntries++; 
			saveWocket3Axis10BitData((WocketSensorData) aSensorDataPoint, isForceNoDifferentialCompression, bw);
		}
	}

	@Override
	public void reset() {
		mRawDataBytesIndex = 0; 
	}

	/**
	 * Pack an x,y,z value into the uncompressed 10 bit Wockets format
	 * Places the result in the outUncompressed variable (4 bytes)
	 * @param x Positive x value
	 * @param y Positive y value
	 * @param z Positive z value
	 */
	private void packUncompressed(short x, short y, short z)
	{				
		// Pack first byte
		currByte = (byte) ((byte)(0x03) << 6);  // 11 are first two bits marking uncompressed 4 byte packet
		tempByte = (byte) ((x >>> 8) &0xFF);
		currByte |= (byte) ((tempByte << 4));		
		tempByte = (byte) (x &0xFF); //bits 3-10
		currByte |= (byte) (tempByte >>> 4);//bits 3-6							
		outUncompressed[0] = currByte;

		// Pack second byte 												
		currByte = (byte) ((byte)(tempByte << 4));//bits 7-10 for diffX
		tempByte = (byte) ((y >>> 8 ) & 0xFF); //bits 1-2 for diffY
		currByte |= (byte)(tempByte << 4);
		tempByte = (byte)(y & 0xFF);// bits 3-10
		currByte |= (byte) (tempByte >>> 6 ); //bits 3-4 for diffY
		outUncompressed[1] = currByte;

		// Pack third byte 	
		currByte = (byte)(tempByte << 2);// bits 5-10 for diffY
		tempByte = (byte) ((z >>> 8) & 0xFF); // bits 1-2 for diffZ
		currByte |= (byte) (tempByte);
		outUncompressed[2] = currByte;

		// Pack fourth byte							
		tempByte = (byte) (z & 0xFF); //bits 3-10 for diffZ
		currByte = (byte) (tempByte);
		outUncompressed[3] = currByte;
	}						

	/**
	 * Pack dx,dy,dz values (between -15 to 15) into the compressed Wockets format (4 bits plus a sign bit)
	 * Places the result in the outCompressed variable (2 bytes)
	 * We could have used twos-compliment and picked up a value, but at the expense of making it more likely
	 * people writing encoders/decoders will mess it up. 
	 * @param dX Change in X from -15 to 15
	 * @param dY Change in Y from -15 to 15
	 * @param dZ Change in Z from -15 to 15
	 */
	private void packCompressed(short dX, short dY, short dZ)
	{				
		//TODO Until code is debugged might want to check ranges of input to this method

		if(dX < 0)
			signX = SIGN_BIT_1;
		else 
			signX = SIGN_BIT_0;
		if(dY < 0)
			signY = SIGN_BIT_1;
		else 
			signY = SIGN_BIT_0;
		if(dZ < 0)
			signZ = SIGN_BIT_1;
		else 
			signZ = SIGN_BIT_0;

		diffX = (short) Math.abs(dX);
		diffY = (short) Math.abs(dY);
		diffZ = (short) Math.abs(dZ);

		// Pack first byte 
		currByte = (byte) 0x00; // First bit is 0 to indicate a compressed packet
		currByte |= (byte) ((byte)signX << 7); // Sign bit X
		currByte |= (byte) ((byte)diffX << 2); // 4 bytes X
		currByte |= (byte) ((byte)signY << 1); // Sign bit Y
		tempByte =  (byte) ((diffY >>> 3) & 0x01); // msb bits 1 for diffY		
		currByte |= (byte) ((byte)tempByte); // msb bit 1 for diffY in last two bits of byte 1
		outCompressed[0] = currByte;

		// Pack second byte 
		currByte = (byte) ((byte)signY << 5); // last 3 bits of Y in first 3 positions
		currByte |= (byte) ((byte)signZ << 4); // Sign bit Z in position 4
		tempByte =  (byte) (diffZ & 0x0F); // 4 bits of Z		
		currByte |= (byte) tempByte; 
		outCompressed[1] = currByte;
	}			

	private static boolean isUseDifferentialCompression(short diffX, short diffY, short diffZ)
	{
		if (((diffX >= -15) && (diffX <= 15)) &&
				((diffY >= -15) && (diffY <= 15)) &&
				((diffZ >= -15) && (diffZ <= 15)))
			return true;
		else
			return false;
	}

	private byte[] out; 
	private void saveWocket3Axis10BitData(WocketSensorData aSensorDataPoint, boolean isForceNoDifferentialCompression, FileOutputStream bw)
	{
		x = (short) aSensorDataPoint.mX;
		y = (short) aSensorDataPoint.mY;
		z = (short) aSensorDataPoint.mZ;			
		diffX = (short) (x - mPrevSensorX) ;
		diffY = (short) (y - mPrevSensorY);
		diffZ = (short) (z - mPrevSensorZ);

		if (isForceNoDifferentialCompression ||  // Force a write of the full point no matter what (e.g., start of file) 
				!isPreviousSensorValuesDefined ||    // No prior sensor values defined, so can't use differential compression
				!isUseDifferentialCompression(diffX, diffY, diffZ)) 
		{
			packUncompressed(x, y, z);
			isPreviousSensorValuesDefined = true;
			out = outUncompressed; 

		}
		else
		{
			packCompressed(diffX, diffY, diffZ);			
			out = outCompressed;  
		}

		mPrevSensorX = x;
		mPrevSensorY = y;
		mPrevSensorZ = z;

		if (bw != null)
		{
			try
			{
				for(int j = 0 ; j < out.length ; j++)
					bw.write(out[j]);
			} catch(Exception ex)
			{
				Log.e(TAG, "Could not write to byte file.");
			}																						
		}		
	}		
}
