package edu.neu.android.wocketslib.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import edu.neu.android.wocketslib.utils.Log;


/**
 * This class provides a serializable wrapper to the Android Intent class.
 * Because Intent is not serializable and because Parcel (and by extension
 * Bundle) can't be serialized we have to write our own class.
 * 
 * Since extras can be of multiple types storage/retrieve is handled with a
 * single HashMap that directs us to the appropriate data storage. A subset of
 * types are implemented now, additional types can be added as needed.
 * 
 * @author Jonathan Lester
 * 
 */
public class IntentWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<String, Integer> mMap = new HashMap<String, Integer>();
	private HashMap<String, Integer> mStorage_Integer = null;
	private HashMap<String, Long> mStorage_Long = null;
	private HashMap<String, Float> mStorage_Float = null;
	private HashMap<String, Double> mStorage_Double = null;
	private HashMap<String, String> mStorage_String = null;
	private HashMap<String, Boolean> mStorage_Boolean = null;

	private static final int STORAGE_INTEGER = 1;
	private static final int STORAGE_LONG = 2;
	private static final int STORAGE_FLOAT = 3;
	private static final int STORAGE_DOUBLE = 4;
	private static final int STORAGE_STRING = 5;
	private static final int STORAGE_BOOLEAN = 6;

	// Internal storage to contain the necessary data
	private HashSet<String> mCategories;
	private int mFlags;
	private String mAction;
//	private Uri mData;
	private String mData;
	private String mMimeType;
	private String mPackage;
	private String mClassName;
	public int type;
	public static final int TYPE_ACTIVITY = 0;
	public static final int TYPE_SERVICE = 1;
	public static final int TYPE_BROADCAST = 2;

	// Constructors matching the intent footprint:
	public IntentWrapper() {
	}

	public IntentWrapper(String action) {
		mAction = action;
	}

//	public IntentWrapper(String action, Uri uri) {
//		mAction = action;
//		mData = uri;
//	}

	public IntentWrapper(String action, String uri) {
		mAction = action;
		mData = uri;
	}

	public IntentWrapper setType(int type_in) {
		switch (type_in) {
		case TYPE_ACTIVITY:
			type = TYPE_ACTIVITY;
			break;
		case TYPE_SERVICE:
			type = TYPE_SERVICE;
			break;
		case TYPE_BROADCAST:
			type = TYPE_BROADCAST;
			break;
		default:
			return this;
		}
		return this;
	}

	// Add a new category to the intent.
	public IntentWrapper addCategory(String category) {
		if (mCategories == null)
			mCategories = new HashSet<String>(4);
		mCategories.add(category);
		return this;
	}

	// Add additional flags to the intent (or with existing flags value).
	public IntentWrapper addFlags(int flags) {
		mFlags |= flags;
		return this;
	}

	// Set special flags controlling how this intent is handled.
	public IntentWrapper setFlags(int flags) {
		mFlags = flags;
		return this;
	}

	public boolean hasCategory(String category) {
		return mCategories != null && mCategories.contains(category);
	}

	public Set<String> getCategories() {
		return mCategories;
	}

	// Remove an category from an intent.
	void removeCategory(String category) {
		if (mCategories != null) {
			mCategories.remove(category);
			if (mCategories.size() == 0) {
				mCategories = null;
			}
		}
	}

	// Returns true if an extra value is associated with the given name.
	boolean hasExtra(String name) {
		if (mMap.containsKey(name))
			return true;
		else
			return false;
	}

	// Set the general action to be performed.
	public IntentWrapper setAction(String action) {
		mAction = action;
		return this;
	}

	// Convenience for calling setComponent(ComponentName) with an explicit
	// application package name and class name.
	public IntentWrapper setClassName(String packageName, String className) {
		mPackage = packageName;
		mClassName = className;
		return this;
	}

	// Set the data this intent is operating on.
//	public IntentWrapper setData(Uri data) {
//		mData = data;
//		return this;
//	}

	// Set the data this intent is operating on.
	public IntentWrapper setData(String data) {
		mData = data;
		return this;
	}

	
//	// (Usually optional) Set the data for the intent along with an explicit
//	// MIME data type.
//	public IntentWrapper setDataAndType(Uri data, String type) {
//		mData = data;
//		mMimeType = type;
//		return this;
//	}

	// (Usually optional) Set the data for the intent along with an explicit
	// MIME data type.
	public IntentWrapper setDataAndType(String data, String type) {
		mData = data;
		mMimeType = type;
		return this;
	}

	// (Usually optional) Set an explicit application package name that limits
	// the components this Intent will resolve to.
	public IntentWrapper setPackage(String packageName) {
		mPackage = packageName;
		return this;
	}

	// Set an explicit MIME data type.
	public IntentWrapper setType(String type) {
		mMimeType = type;
		return this;
	}

	public static String tag = "IntentWrapper";

	public static byte[] marshallToByteArray(IntentWrapper intent) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(intent);
			oos.close();
			oos = null;
		} catch (IOException e) {
			Log.e(tag, "Error while serializing, IOException: " + e.toString());
			return null;
		}

		// Save byte array for the BLOB and cleanup:
		byte[] blob = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
		}
		return blob;
	}

	public static IntentWrapper createFromBlob(byte[] blob) {
		IntentWrapper wrapper = null;
		if (blob == null || blob.length == 0)
			Log.v(tag, "WARNING: INTENTWRAPPER IS EMPTY!");
		if (blob != null && blob.length != 0) {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(blob));
				wrapper = (IntentWrapper) in.readObject();
				in.close();
			} catch (StreamCorruptedException e) {
				Log.e(tag, "StreamCorruptedException creating blob: "
						+ e.getMessage());
			} catch (IOException e) {
				Log.e(tag, "IOException creating blob: " + e.getMessage());
			} catch (ClassNotFoundException e) {
				Log.e(tag, "ClassNotFoundException creating blob: "
						+ e.getMessage());
			}
		}
		return wrapper;
	}

	public Intent createIntent() {
		Intent intent = new Intent();
		if (mAction != null)
			intent.setAction(mAction);
		if (mData != null)
			intent.setData(Uri.parse(mData));
		if (mMimeType != null)
			intent.setType(mMimeType);
		intent.setFlags(mFlags);
		if (mCategories != null)
			for (String category : mCategories)
				intent.addCategory(category);
		if (mPackage != null && mClassName != null)
			intent.setClassName(mPackage, mClassName);

		if (mStorage_Integer != null) {
			Set<String> keySet = mStorage_Integer.keySet();
			for (String key : keySet)
				intent.putExtra(key, mStorage_Integer.get(key));
		}
		if (mStorage_Long != null) {
			Set<String> keySet = mStorage_Long.keySet();
			for (String key : keySet)
				intent.putExtra(key, mStorage_Long.get(key));
		}
		if (mStorage_Float != null) {
			Set<String> keySet = mStorage_Float.keySet();
			for (String key : keySet)
				intent.putExtra(key, mStorage_Float.get(key));
		}
		if (mStorage_Double != null) {
			Set<String> keySet = mStorage_Double.keySet();
			for (String key : keySet)
				intent.putExtra(key, mStorage_Double.get(key));
		}
		if (mStorage_String != null) {
			Set<String> keySet = mStorage_String.keySet();
			for (String key : keySet)
				intent.putExtra(key, mStorage_String.get(key));
		}
		if (mStorage_Boolean != null) {
			Set<String> keySet = mStorage_Boolean.keySet();
			for (String key : keySet)
				intent.putExtra(key, mStorage_Boolean.get(key));
		}

		return intent;
	}

	public static void launchIntent(Intent intent, int type,
			Context callingContext) {
		if (intent.getExtras() != null) {
			Set<String> keys = intent.getExtras().keySet();
			if (keys.size() == 0)
				Log.v(tag, "IntentWrapper.launchIntent, No extras!");
			for (String key : keys)
				Log.v(tag, "IntentWrapper.launchIntent, Contains key: " + key);
		}

		try {
			switch (type) {
			case TYPE_ACTIVITY:
				callingContext.startActivity(intent);
				break;
			case TYPE_BROADCAST:
				callingContext.sendBroadcast(intent);
				break;
			case TYPE_SERVICE:
				callingContext.startService(intent);
				break;
			default:
				Log.e(tag, "ERROR: Unknown intent type present! Type is: "
						+ type);
			}
		} catch (java.lang.RuntimeException e) {
			Log.e(tag,
					"ERROR: Unable to start this intent because there was a runtime error: "
							+ e.getMessage());
		}
	}

	public void launchIntent(Context callingContext) {
		Intent intent = createIntent();

		if (intent.getExtras() != null) {
			Set<String> keys = intent.getExtras().keySet();
			if (keys.size() == 0)
				Log.v("Alarm1", "No extras!");
			for (String key : keys)
				Log.v("Alarm1", "Contains key: " + key);
		}

		switch (this.type) {
		case TYPE_ACTIVITY:
			callingContext.startActivity(intent);
			break;
		case TYPE_BROADCAST:
			callingContext.sendBroadcast(intent);
			break;
		case TYPE_SERVICE:
			callingContext.startService(intent);
			break;
		default:
			Log.e(tag, "ERROR: Unknown intent type present! Type is: "
					+ this.type);
		}

	}

	// Add extended data to the intent.
	private void removeInternal(String name) {
		final int type = mMap.get(name);
		switch (type) {
		case STORAGE_INTEGER:
			if (mStorage_Integer != null)
				mStorage_Integer.remove(name);
			break;
		case STORAGE_LONG:
			if (mStorage_Long != null)
				mStorage_Long.remove(name);
			break;
		case STORAGE_FLOAT:
			if (mStorage_Float != null)
				mStorage_Float.remove(name);
			break;
		case STORAGE_DOUBLE:
			if (mStorage_Double != null)
				mStorage_Double.remove(name);
			break;
		case STORAGE_STRING:
			if (mStorage_String != null)
				mStorage_String.remove(name);
			break;
		case STORAGE_BOOLEAN:
			if (mStorage_Boolean != null)
				mStorage_Boolean.remove(name);
			break;
		}
	}

	public IntentWrapper putExtra(String name, long value) {
		if (mMap.containsKey(name))
			removeInternal(name);
		if (mStorage_Long == null)
			mStorage_Long = new HashMap<String, Long>();
		mStorage_Long.put(name, value);
		return this;
	}

	// Add extended data to the intent.
	public IntentWrapper putExtra(String name, boolean value) {
		if (mMap.containsKey(name))
			removeInternal(name);
		if (mStorage_Boolean == null)
			mStorage_Boolean = new HashMap<String, Boolean>();
		mStorage_Boolean.put(name, value);
		return this;
	}

	// Add extended data to the intent.
	public IntentWrapper putExtra(String name, double value) {
		if (mMap.containsKey(name))
			removeInternal(name);
		if (mStorage_Double == null)
			mStorage_Double = new HashMap<String, Double>();
		mStorage_Double.put(name, value);
		return this;
	}

	// Add extended data to the intent.
	public IntentWrapper putExtra(String name, int value) {
		if (mMap.containsKey(name))
			removeInternal(name);
		if (mStorage_Integer == null)
			mStorage_Integer = new HashMap<String, Integer>();
		mStorage_Integer.put(name, value);
		return this;
	}

	// Add extended data to the intent.
	public IntentWrapper putExtra(String name, float value) {
		if (mMap.containsKey(name))
			removeInternal(name);
		if (mStorage_Float == null)
			mStorage_Float = new HashMap<String, Float>();
		mStorage_Float.put(name, value);
		return this;
	}

	// Add extended data to the intent.
	public IntentWrapper putExtra(String name, String value) {
		if (mMap.containsKey(name))
			removeInternal(name);
		if (mStorage_String == null)
			mStorage_String = new HashMap<String, String>();
		mStorage_String.put(name, value);
		return this;
	}

	// Remove extended data from the intent.
	void removeExtra(String name) {
		this.removeInternal(name);
	}

	// Flatten this object in to a Parcel.
	// void writeToParcel(Parcel out, int flags)

	/*
	 * Intent putExtra(String name, Parcelable value) Add extended data to the
	 * intent. Intent putExtra(String name, long value) Add extended data to the
	 * intent. Intent putExtra(String name, boolean value) Add extended data to
	 * the intent. Intent putExtra(String name, double value) Add extended data
	 * to the intent. Intent putExtra(String name, CharSequence[] value) Add
	 * extended data to the intent. Intent putExtra(String name, Parcelable[]
	 * value) Add extended data to the intent. Intent putExtra(String name, char
	 * value) Add extended data to the intent. Intent putExtra(String name,
	 * int[] value) Add extended data to the intent. Intent putExtra(String
	 * name, int value) Add extended data to the intent. Intent putExtra(String
	 * name, double[] value) Add extended data to the intent. Intent
	 * putExtra(String name, float value) Add extended data to the intent.
	 * Intent putExtra(String name, short value) Add extended data to the
	 * intent. Intent putExtra(String name, long[] value) Add extended data to
	 * the intent. Intent putExtra(String name, boolean[] value) Add extended
	 * data to the intent. Intent putExtra(String name, short[] value) Add
	 * extended data to the intent. Intent putExtra(String name, String value)
	 * Add extended data to the intent. Intent putExtra(String name,
	 * Serializable value) Add extended data to the intent. Intent
	 * putExtra(String name, float[] value) Add extended data to the intent.
	 * Intent putExtra(String name, Bundle value) Add extended data to the
	 * intent. Intent putExtra(String name, byte[] value) Add extended data to
	 * the intent. Intent putExtra(String name, CharSequence value) Add extended
	 * data to the intent. Intent putExtra(String name, char[] value) Add
	 * extended data to the intent. Intent putExtra(String name, byte value) Add
	 * extended data to the intent. Intent putExtras(Intent src) Copy all extras
	 * in 'src' in to this intent. Intent putExtras(Bundle extras) Add a set of
	 * extended data to the intent. Intent putIntegerArrayListExtra(String name,
	 * ArrayList<Integer> value) Add extended data to the intent. Intent
	 * putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable>
	 * value) Add extended data to the intent. Intent
	 * putStringArrayListExtra(String name, ArrayList<String> value) Add
	 * extended data to the intent. void readFromParcel(Parcel in) void
	 * removeCategory(String category) Remove an category from an intent. void
	 * removeExtra(String name) Remove extended data from the intent. Intent
	 * replaceExtras(Intent src) Completely replace the extras in the Intent
	 * with the extras in the given Intent. Intent replaceExtras(Bundle extras)
	 * Completely replace the extras in the Intent with the given Bundle of
	 * extras. ComponentName resolveActivity(PackageManager pm) Return the
	 * Activity component that should be used to handle this intent.
	 * ActivityInfo resolveActivityInfo(PackageManager pm, int flags) Resolve
	 * the Intent into an ActivityInfo describing the activity that should
	 * execute the intent. String resolveType(ContentResolver resolver) Return
	 * the MIME data type of this intent. String resolveType(Context context)
	 * Return the MIME data type of this intent. String
	 * resolveTypeIfNeeded(ContentResolver resolver) Return the MIME data type
	 * of this intent, only if it will be needed for intent resolution. Intent
	 * setAction(String action) Set the general action to be performed. Intent
	 * setClass(Context packageContext, Class<?> cls) Convenience for calling
	 * setComponent(ComponentName) with the name returned by a Class object.
	 * Intent setClassName(String packageName, String className) Convenience for
	 * calling setComponent(ComponentName) with an explicit application package
	 * name and class name. Intent setClassName(Context packageContext, String
	 * className) Convenience for calling setComponent(ComponentName) with an
	 * explicit class name. Intent setComponent(ComponentName component)
	 * (Usually optional) Explicitly set the component to handle the intent.
	 * Intent setData(Uri data) Set the data this intent is operating on. Intent
	 * setDataAndType(Uri data, String type) (Usually optional) Set the data for
	 * the intent along with an explicit MIME data type. void
	 * setExtrasClassLoader(ClassLoader loader) Sets the ClassLoader that will
	 * be used when unmarshalling any Parcelable values from the extras of this
	 * Intent. Intent setFlags(int flags) Set special flags controlling how this
	 * intent is handled. Intent setPackage(String packageName) (Usually
	 * optional) Set an explicit application package name that limits the
	 * components this Intent will resolve to. void setSourceBounds(Rect r) Set
	 * the bounds of the sender of this intent, in screen coordinates. Intent
	 * setType(String type) Set an explicit MIME data type. String toString()
	 * Returns a string containing a concise, human-readable description of this
	 * object. String toURI() This method is deprecated. Use toUri(int) instead.
	 * String toUri(int flags) Convert this Intent into a String holding a URI
	 * representation of it. void writeToParcel(Parcel out, int flags) Flatten
	 * this object in to a Parcel.
	 */

	// NOT IMPLEMENTED:
	// Creates and returns a copy of this Object.
	// Object clone()
	// Intent cloneFilter()
	// Make a clone of only the parts of the Intent that are relevant for filter
	// matching: the action, data, type, component, and categories.
	// static Intent createChooser(Intent target, CharSequence title)
	// Convenience function for creating a ACTION_CHOOSER Intent.
	// int describeContents()
	// Describe the kinds of special objects contained in this Parcelable's
	// marshalled representation.
	// int fillIn(Intent other, int flags)
	// Copy the contents of other in to this object, but only where fields are
	// not defined by this object.
	// boolean filterEquals(Intent other)
	// Determine if two intents are the same for the purposes of intent
	// resolution (filtering).
	// int filterHashCode()
	// Generate hash code that matches semantics of filterEquals().
	// String getAction()
	// Retrieve the general action to be performed, such as ACTION_VIEW.
	// boolean[] getBooleanArrayExtra(String name)
	// Retrieve extended data from the intent.
	// boolean getBooleanExtra(String name, boolean defaultValue)
	// Retrieve extended data from the intent.
	// Bundle getBundleExtra(String name)
	// Retrieve extended data from the intent.
	// byte[] getByteArrayExtra(String name)
	// Retrieve extended data from the intent.
	// byte getByteExtra(String name, byte defaultValue)
	// Retrieve extended data from the intent.
	// Set<String> getCategories()
	// Return the set of all categories in the intent.
	// char[] getCharArrayExtra(String name)
	// Retrieve extended data from the intent.
	// char getCharExtra(String name, char defaultValue)
	// Retrieve extended data from the intent.
	// CharSequence[] getCharSequenceArrayExtra(String name)
	// Retrieve extended data from the intent.
	// ArrayList<CharSequence> getCharSequenceArrayListExtra(String name)
	// Retrieve extended data from the intent.
	// CharSequence getCharSequenceExtra(String name)
	// Retrieve extended data from the intent.
	// ComponentName getComponent()
	// Retrieve the concrete component associated with the intent.
	// Uri getData()
	// Retrieve data this intent is operating on.
	// String getDataString()
	// The same as getData(), but returns the URI as an encoded String.
	// double[] getDoubleArrayExtra(String name)
	// Retrieve extended data from the intent.
	// double getDoubleExtra(String name, double defaultValue)
	// Retrieve extended data from the intent.
	// Bundle getExtras()
	// Retrieves a map of extended data from the intent.
	// int getFlags()
	// Retrieve any special flags associated with this intent.
	// float[] getFloatArrayExtra(String name)
	// Retrieve extended data from the intent.
	// float getFloatExtra(String name, float defaultValue)
	// Retrieve extended data from the intent.
	// int[] getIntArrayExtra(String name)
	// Retrieve extended data from the intent.
	// int getIntExtra(String name, int defaultValue)
	// Retrieve extended data from the intent.
	// ArrayList<Integer> getIntegerArrayListExtra(String name)
	// Retrieve extended data from the intent.
	// static Intent getIntent(String uri)
	// This method is deprecated. Use parseUri(String, int) instead.
	// static Intent getIntentOld(String uri)
	// long[] getLongArrayExtra(String name)
	// Retrieve extended data from the intent.
	// long getLongExtra(String name, long defaultValue)
	// Retrieve extended data from the intent.
	// String getPackage()
	// Retrieve the application package name this Intent is limited to.
	// Parcelable[] getParcelableArrayExtra(String name)
	// Retrieve extended data from the intent.
	// <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String
	// name)
	// Retrieve extended data from the intent.
	// <T extends Parcelable> T getParcelableExtra(String name)
	// Retrieve extended data from the intent.
	// String getScheme()
	// Return the scheme portion of the intent's data.
	// Serializable getSerializableExtra(String name)
	// Retrieve extended data from the intent.
	// short[] getShortArrayExtra(String name)
	// Retrieve extended data from the intent.
	// short getShortExtra(String name, short defaultValue)
	// Retrieve extended data from the intent.
	// Rect getSourceBounds()
	// Get the bounds of the sender of this intent, in screen coordinates.
	// String[] getStringArrayExtra(String name)
	// Retrieve extended data from the intent.
	// ArrayList<String> getStringArrayListExtra(String name)
	// Retrieve extended data from the intent.
	// String getStringExtra(String name)
	// Retrieve extended data from the intent.
	// String getType()
	// Retrieve any explicit MIME type included in the intent.
	// boolean hasFileDescriptors()
	// Returns true if the Intent's extras contain a parcelled file descriptor.
	// static Intent parseIntent(Resources resources, XmlPullParser parser,
	// AttributeSet attrs)
	// Parses the "intent" element (and its children) from XML and instantiates
	// an Intent object.
	// static Intent parseUri(String uri, int flags)
	// Create an intent from a URI.
	// Intent putCharSequenceArrayListExtra(String name, ArrayList<CharSequence>
	// value)
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, String[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, Parcelable value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, CharSequence[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, Parcelable[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, char value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, int[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, double[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, short value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, long[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, boolean[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, short[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, Serializable value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, float[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, Bundle value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, byte[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, CharSequence value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, char[] value) {
	// Add extended data to the intent.
	// IntentWrapper putExtra(String name, byte value) {
	// Copy all extras in 'src' in to this intent.
	// IntentWrapper putExtras(Intent src) {
	// Add a set of extended data to the intent.
	// IntentWrapper putExtras(Bundle extras) {
	// Add extended data to the intent.
	// IntentWrapper putIntegerArrayListExtra(String name, ArrayList<Integer>
	// value) {
	// Add extended data to the intent.
	// IntentWrapper putParcelableArrayListExtra(String name, ArrayList<?
	// extends Parcelable> value) {
	// Add extended data to the intent.
	// IntentWrapper putStringArrayListExtra(String name, ArrayList<String>
	// value) {

	// void readFromParcel(Parcel in) {
	// Completely replace the extras in the Intent with the extras in the given
	// Intent.
	// IntentWrapper replaceExtras(Intent src) {
	// Completely replace the extras in the Intent with the given Bundle of
	// extras.
	// IntentWrapper replaceExtras(Bundle extras) {
	// Return the Activity component that should be used to handle this intent.
	// ComponentName resolveActivity(PackageManager pm) {
	// Resolve the Intent into an ActivityInfo describing the activity that
	// should execute the intent.
	// ActivityInfo resolveActivityInfo(PackageManager pm, int flags) {
	// Return the MIME data type of this intent.
	// String resolveType(ContentResolver resolver) {
	// Return the MIME data type of this intent.
	// String resolveType(Context context) {
	// Return the MIME data type of this intent, only if it will be needed for
	// intent resolution.
	// String resolveTypeIfNeeded(ContentResolver resolver) {

}
