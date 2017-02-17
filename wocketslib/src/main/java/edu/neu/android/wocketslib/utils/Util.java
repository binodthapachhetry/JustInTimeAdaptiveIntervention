package edu.neu.android.wocketslib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Display;
import edu.neu.android.wocketslib.activities.helpcomment.Mail;

public class Util {

	private static NumberFormat numberFormat = new DecimalFormat("#.##");

	private static String insertChar(String anAddress, String aChar) {
		StringBuffer sb = new StringBuffer();
		sb.append(anAddress.substring(0, 2));
		sb.append(aChar);
		sb.append(anAddress.substring(2, 4));
		sb.append(aChar);
		sb.append(anAddress.substring(4, 6));
		sb.append(aChar);
		sb.append(anAddress.substring(6, 8));
		sb.append(aChar);
		sb.append(anAddress.substring(8, 10));
		return sb.toString();
	}

	public static String removeColons(String aStr) {
		String result = aStr.replace(":", "");
		return result;
	}

	public static String insertColons(String anAddress) {
		return insertChar(anAddress, ":");
	}

	public static String insertUnderscores(String anAddress) {
		return insertChar(anAddress, "_");
	}

	public static String formatFloat(float number) {
		return numberFormat.format(number);
	}

	public static String getVersion(Context c) {
		PackageManager pm = c.getPackageManager();
		PackageInfo pi = null;
		try {
			if (pm != null)
				pi = pm.getPackageInfo(c.getApplicationInfo().packageName, 0);
			if (pi != null)
				return String.valueOf(pi.versionName);
			return "";
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	public static boolean isAria(Context c) {
		Display d = ((android.view.WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		return ((d.getWidth() == 320) && (d.getHeight() == 480));
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public static boolean sendCommonEmail(Context context, String MAIL_USERNAME, String MAIL_PASSWORD, String[] addressList, String mailSubject,
			String mailContent) {
		Log.i("Util", "Beginning Sending Email");
		Mail m = new Mail();
		m.setUsername(MAIL_USERNAME);
		m.setPassword(MAIL_PASSWORD);

		String senderAddress = MAIL_USERNAME;
		if (addressList == null || addressList.length == 0)
			Log.i("Util", "Empty email address");
		m.setFromAddress(senderAddress);
		m.setToAddresses(new ArrayList<String>(Arrays.asList(addressList)));
		m.setBody(mailContent);
		m.setSubject(mailSubject);
		m.setHost("smtp.gmail.com");
		m.setSmtpPort("465");
		m.setSocketFactoryPort("465");
		m.setUsesSmtpAuthentication(true);

		boolean sendSuccess = true;

		try {
			sendSuccess = m.send();
			Log.i("Util", "Send email");
		} catch (Exception e) {
			sendSuccess = false;
			Log.i("Util", "Failed send email");
		}
		return sendSuccess;
	}
}
