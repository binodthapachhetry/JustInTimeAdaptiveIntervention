package edu.neu.android.wocketslib.encryption;

import android.content.Context;
import android.content.res.AssetManager;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

/**
 * Created by Dharam on 2/19/2015.
 */
public class RSACipher {

    public static String encrypt(String rawText, Context context)
            throws IOException, GeneralSecurityException {

        if (rawText.length() <= 117) {
            return encryptText(rawText, context);
        } else {
            String encryptedString = "";
            for(int i = 0 ; i < (rawText.length()/117)+1 ; i++) {
                encryptedString = encryptedString + rawText.substring((i*117),((i+1)*117));
            }
            return encryptedString;
        }
    }

    private static String encryptText(String rawText, Context context) throws IOException, GeneralSecurityException {
        AssetManager am = context.getAssets();
        InputStream publicKey = am.open("public_key.der");
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(IOUtils.toByteArray(publicKey));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec));

        return "enc:" + new String(Base64.encodeBase64(cipher.doFinal(rawText.getBytes("UTF-8")))) + ":";
    }

}
