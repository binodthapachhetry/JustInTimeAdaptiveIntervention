package edu.neu.android.wocketslib.utils;

public class PasswordChecker {

	private StringBuffer aPassword;
	private int i = 0;

	public PasswordChecker(String aTargetPassword) {
		aPassword = new StringBuffer(aTargetPassword);
	}

	public boolean isMatch(int keyCode) {
		if ((keyCode + 68) == ((int) aPassword.charAt(i))) // 68 is the offset
															// for the Keycode
															// values to ascii
			i++;
		else
			i = 0;

		if (i == aPassword.length()) {
			i = 0;
			return true;
		}

		return false;
	}
}
