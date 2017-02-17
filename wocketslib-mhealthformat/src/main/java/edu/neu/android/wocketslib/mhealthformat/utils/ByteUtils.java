package edu.neu.android.wocketslib.mhealthformat.utils;

import java.nio.ByteBuffer;

/**
 * Created by Dharam on 5/1/2015.
 */
public class ByteUtils {
	
	public static byte [] long2ByteArray (long value)
	{
	    return ByteBuffer.allocate(8).putLong(value).array();
	}

	public static byte [] float2ByteArray (float value)
	{  
	     return ByteBuffer.allocate(4).putFloat(value).array();
	}
	
	public static long byteArray2Long (byte[] value) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(value);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	public static float byteArray2Float (byte[] value) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.put(value);
		buffer.flip();
		return buffer.getFloat();
	}

}
