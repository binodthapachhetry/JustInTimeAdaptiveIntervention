package mhealth.neu.edu.phire.support;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.WakefulBroadcastReceiver;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.neu.android.wocketslib.mhealthformat.entities.AndroidWearAccelerometerRaw;
import edu.neu.android.wocketslib.mhealthformat.utils.ByteUtils;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.support.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyCSVconversionService extends IntentService {

    private Context mContext;

    public static final String TAG = MyCSVconversionService.class.getSimpleName();

    public MyCSVconversionService() {
        super("MyCSVconversionService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mContext = getApplicationContext();
            DataManager.setZipTransferFinished(mContext,false);
            unzipFromWatch();
        }
    }

    private void unzipFromWatch() {
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
        String watchZipFolder = DataManager.getDirectoryTransfer(mContext);
        Log.i(TAG, "This is transfer folder: " + watchZipFolder,mContext);
        File watchZipFile = new File(watchZipFolder);
        watchZipFile.mkdirs();
        File[] zipFiles = new File[0];
        if(watchZipFile.isDirectory()) {
            zipFiles = watchZipFile.listFiles();
        }else{
            Log.e(TAG, "This is not a directory: " + watchZipFile.getAbsolutePath(),mContext);

            if(watchZipFile.delete()){
                Log.i(TAG, "Delete and quit!",mContext);
            }else{
                Log.e(TAG, "Can't delete so just quit!",mContext);
            }
            return;
        }
        for(File zipFile : zipFiles) {
            try {
                Log.i(TAG, "Unzipping " + zipFile.getAbsolutePath(),mContext);
                _unzipFromWatchHelper(watchZipFolder, zipFile.getName());
                if(zipFile.delete()){
                    Log.i(TAG, "Delete watch zip file: " + zipFile.getAbsolutePath() + " upon successfully unzipping",mContext);
                }else{
                    Log.e(TAG, "Fail to delete watch zip file: " + zipFile.getAbsolutePath() + " after successfully unzipping",mContext);
                }
            } catch (ZipException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR when unzipping from watch:" + e.getMessage(),mContext);
                Log.e(TAG, "skip delete the file",mContext);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR when using unzip helper:" + e.getMessage(),mContext);
            }
        }

        DataManager.setZipTransferFinished(mContext,true);

    }

    private void _unzipFromWatchHelper(String filePath, String fileName) throws ZipException, FileNotFoundException {
        ZipFile zipFile = new ZipFile(filePath + File.separator + fileName);
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        for(FileHeader file : fileHeaders) {
            String original_entryPath = file.getFileName();

            File original_unzippedFile = new File(original_entryPath);
            if(!original_unzippedFile.isDirectory()) {

                String entryPathFirst = original_entryPath.replace("/data/","/data-watch/");
                String entryPath = entryPathFirst.replace("/logs/","/logs-watch/");
                Log.i(TAG, "New path for transfer files: " + entryPath,mContext);
                File unzippedFile = new File(entryPath);
                Log.i(TAG, "New  path for transfer files: " + unzippedFile.getParentFile().getAbsolutePath(),mContext);
                unzippedFile.getParentFile().mkdirs();
                zipFile.extractFile(file, unzippedFile.getParent(), new UnzipParameters(), unzippedFile.getName());
                Log.i(TAG, "Unzipped folder: " + unzippedFile.getParent() + ", name: " + unzippedFile.getName(),mContext);

                if(unzippedFile.getName().endsWith("baf")) {
                    File currentFile = new File(unzippedFile.getParent() + File.separator + unzippedFile.getName());
                    Log.i(TAG, "Decoding sensor file: " + currentFile, mContext);
                    try {
                        boolean result;
                        InputStream assetInputStream = new FileInputStream(currentFile);
                        final byte[] b = IOUtils.toByteArray(assetInputStream);
                        result = decodeBinarySensorFile(b, currentFile.getParent(), currentFile.getName());

                        if (result == true) {
                            Log.i(TAG, "Successfully decoded sensor file: " + currentFile.getAbsolutePath(), mContext);
                            currentFile.delete();
                            Log.i(TAG, "Deleted the original binary file: " + currentFile.getAbsolutePath(), mContext);
                        } else {
                            Log.e(TAG, "Fail to decode binary sensor file: " + currentFile.getAbsolutePath(), mContext);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    private boolean decodeBinarySensorFile(byte[] b, String path, String fileName){
        String newName = fileName.replaceAll(".baf", ".csv");
        File newFile = new File(path + File.separator + newName);

        if(newFile.exists()){
            newFile.delete();
        }
        AndroidWearAccelerometerRaw accelRaw = new AndroidWearAccelerometerRaw(mContext);

        boolean result = true;
        long startTime = System.currentTimeMillis();
        for(int i = 0; i + 20 <= b.length ;i = i+20){
            float rawx = ByteUtils.byteArray2Float(Arrays.copyOfRange(b, i, i + 4));
            float rawy = ByteUtils.byteArray2Float(Arrays.copyOfRange(b, i+4, i + 8));
            float rawz = ByteUtils.byteArray2Float(Arrays.copyOfRange(b, i+8, i + 12));
            long ts = ByteUtils.byteArray2Long(Arrays.copyOfRange(b, i + 12, i + 20));
            accelRaw.setRawx(rawx);
            accelRaw.setRawy(rawy);
            accelRaw.setRawz(rawz);
            accelRaw.setTimestamp(ts);
            try {
                accelRaw.bufferedWriteToCustomCsv(path, newName, true);
            }catch (IOException e){
                Log.e(TAG, "IO error when decoding binary from watch, skip current bits and just to next 20 bits",mContext);
                Log.e(TAG, e.getMessage(),mContext);
            }
        }
        try {
            Log.e(TAG, "Before flush",mContext);
            accelRaw.flushAndCloseCsv();
        }catch(IOException e){
            Log.e(TAG, "IO error when closing the buffered writer when decoding binary from watch",mContext);
            Log.e(TAG, e.getMessage(),mContext);
        }
        Log.i(TAG, "Decoding file time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds",mContext);
        return result;
    }



}
