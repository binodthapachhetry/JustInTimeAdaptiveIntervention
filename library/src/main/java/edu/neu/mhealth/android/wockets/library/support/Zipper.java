package edu.neu.mhealth.android.wockets.library.support;

import android.content.Context;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;

/**
 * @author Dharam Maniar
 */
public class Zipper {

    private static final String TAG = "Zipper";
//    private static final String PASSWORD = "7Qv8e3PfaXF25DLb";
    private static final String PASSWORD = "qo3mD6ON8bvSx9T1";

    public static void zipFileWithEncryption(String filePath, Context context) {
        boolean isException = false;
        Log.i(TAG, "zipFileWithEncryption - " + filePath, context);
        File fileToZip = new File(filePath);
        File zipFile = new File(fileToZip.getAbsolutePath() + ".zip");
        try {
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
            zipParameters.setIncludeRootFolder(false);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(PASSWORD);
            ZipFile zip = new ZipFile(zipFile);
            Log.i(TAG, "Creating ZipFile - " + zipFile.getAbsolutePath(), context);
            zip.createZipFile(fileToZip, zipParameters);
        } catch (ZipException e) {
            isException = true;
            Log.e(TAG, "Exception while creating zip file with encryption", e, context);
        }
        if (!isException) {
            Log.i(TAG, "Deleting File - " + fileToZip.getAbsolutePath(), context);
            FileUtils.delete(fileToZip);
        }
    }

    public static void unZipFileWithEncryption(String filePath, Context context) {
        boolean isException = false;
        Log.i(TAG, "unzipFileWithEncryption", context);
        File fileToUnZip = new File(filePath);
        String destinationPath = fileToUnZip.getParent();
        try {
            ZipFile zip = new ZipFile(fileToUnZip);
            zip.setPassword(PASSWORD);
            zip.extractAll(destinationPath);
        } catch (ZipException e) {
            isException = true;
            Log.e(TAG, "Exception while unzipping file with encryption", e, context);
        }
        if (!isException) {
            Log.i(TAG, "Deleting File - " + fileToUnZip.getAbsolutePath(), context);
            FileUtils.delete(fileToUnZip);
        }
    }

    public static void zipFolderWithEncryption(String folderPath, Context context) {
        boolean isException = false;
        Log.i(TAG, "zipFolderWithEncryption - " + folderPath, context);
        File zipFolder = new File(folderPath);
        try {
            if (folderPath.charAt(folderPath.length() - 1) == '/') {
                folderPath = folderPath.substring(0, folderPath.length() - 1);
            }
            File zipFile = new File(folderPath + ".zip");

            if (zipFile.exists()) {
                Log.i(TAG, "ZipFile already exists, deleting - " + zipFile.getAbsolutePath(), context);
                FileUtils.delete(zipFile);
            }
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
            zipParameters.setIncludeRootFolder(false);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(PASSWORD);
            ZipFile zip = new ZipFile(zipFile);
            Log.i(TAG, "Creating ZipFile from Folder - " + zipFolder.getAbsolutePath(), context);
            zip.createZipFileFromFolder(zipFolder, zipParameters, false, 0);
        } catch (ZipException e) {
            isException = true;
            Log.e(TAG, "Exception while creating zip folder with encryption", e, context);
        }

        if (!isException) {
            Log.i(TAG, "Deleting Folder - " + zipFolder.getAbsolutePath(), context);
            FileUtils.delete(zipFolder);
        }
    }

    public static void zipFolder(String folderPath, Context context) {
        boolean isException = false;
        Log.i(TAG, "zipFolderWithEncryption - " + folderPath, context);
        File zipFolder = new File(folderPath);
        try {
            if (folderPath.charAt(folderPath.length() - 1) == '/') {
                folderPath = folderPath.substring(0, folderPath.length() - 1);
            }
            File zipFile = new File(folderPath + ".zip");

            if (zipFile.exists()) {
                Log.i(TAG, "ZipFile already exists, deleting - " + zipFile.getAbsolutePath(), context);
                FileUtils.delete(zipFile);
            }
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
            zipParameters.setIncludeRootFolder(false);
            ZipFile zip = new ZipFile(zipFile);
            Log.i(TAG, "Creating ZipFile from Folder - " + zipFolder.getAbsolutePath(), context);
            zip.createZipFileFromFolder(zipFolder, zipParameters, false, 0);
        } catch (ZipException e) {
            isException = true;
            Log.e(TAG, "Exception while creating zip folder", e, context);
        }
        if (!isException) {
            Log.i(TAG, "Deleting Folder - " + zipFolder.getAbsolutePath(), context);
            FileUtils.delete(zipFolder);
        }
    }
}
