package edu.neu.mhealth.android.wockets.library.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import edu.neu.mhealth.android.wockets.library.managers.helper.Base64;

/**
 * Used to encrypt single line files.
 * NOTE: Do not use this. Use {@link edu.neu.mhealth.android.wockets.library.support.Zipper}
 *
 * @author Dharam Maniar
 */
public class EncryptionManager {

	private static final String TAG = "EncryptionManager";

	public static String encrypt(String rawText, Context context)
			throws IOException, GeneralSecurityException {

		if (rawText.length() <= 117) {
			return encryptText(rawText, context);
		} else {
			String encryptedString = "";
			for(int i = 0 ; i < (rawText.length()/117)+1 ; i++) {
				if (rawText.length() < ((i+1)*117)) {
					encryptedString = encryptedString + rawText.substring((i * 117), rawText.length()-1);
				} else {
					encryptedString = encryptedString + rawText.substring((i * 117), ((i + 1) * 117));
				}
			}
			return encryptedString;
		}
	}

	private static String encryptText(String rawText, Context context) throws IOException, GeneralSecurityException {
		AssetManager am = context.getAssets();
		InputStream publicKey = am.open("public_key.der");
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(IOUtils.toByteArray(publicKey));

		@SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec));

		return "enc:" + new String(Base64.encodeBase64(cipher.doFinal(rawText.getBytes("UTF-8")))) + ":";
	}
}
