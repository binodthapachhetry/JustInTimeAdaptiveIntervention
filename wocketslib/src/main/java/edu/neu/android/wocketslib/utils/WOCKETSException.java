package edu.neu.android.wocketslib.utils;


public class WOCKETSException extends Exception {
	private static final long serialVersionUID = 1L;

	public WOCKETSException(String aTAG, String msg) {
		super(msg);
		Log.e(aTAG, "WOCKETSException: " + msg);
	}

	public WOCKETSException(String aTAG, String msg, Throwable ex) {
		super(msg, ex);
		Log.e(aTAG, "WOCKETSException: " + msg + " " + ex.toString());
	}
}
