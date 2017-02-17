package edu.neu.android.wocketslib.mhealthformat.entities;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Observable;

import edu.neu.android.wocketslib.mhealthformat.mHealthFormat;

/**
 * Created by qutang on 4/23/15.
 */
public abstract class mHealthEntity{

    public static final String TAG = "mHealthEntity";

    protected String section1, section2;
    public abstract mHealthFormat.MHEALTH_FILE_TYPE getEntityType();
    public abstract String tomHealthRow();
    public abstract String getHeader();
    public abstract byte[] encodeRowAsBinary();

    private BufferedWriter bufferedWriter = null;
    private FileWriter aFileWriter = null;

    private BufferedOutputStream bufferedOutputStream = null;
    private FileOutputStream fileOutputStream = null;

    public boolean writeTomHealthCsv(boolean isAppend){
        String path = null;
        String filename = mHealthFormat.getmHealthFilenameForCurrentHour(section1, section2, getEntityType());
        Date specificDate = mHealthFormat.extractDateFromFilename(filename);
        try {
            path = mHealthFormat.buildmHealthPath(specificDate, mHealthFormat.PATH_LEVEL.HOURLY, getEntityType());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return saveCsv(path, filename, getHeader(), tomHealthRow(), isAppend);
    };

    public static boolean verifyCurrentDate(Date specificDate){
        if(specificDate.getYear() < 80){
            Log.e(TAG, "System time is not initialized correctly, quit!");
            return false;
        }else{
            return true;
        }
    }

    public boolean writeToCustomCsv(String filePath, boolean isAppend){
        String filename = mHealthFormat.getmHealthFilenameForCurrentHour(section1, section2, getEntityType());
        Date specificDate = mHealthFormat.extractDateFromFilename(filename);
        return saveCsv(filePath, filename, getHeader(), tomHealthRow(), isAppend);
    }

    public boolean writeToCustomCsv(String filePath, String filename, boolean isAppend){
        return saveCsv(filePath, filename, getHeader(), tomHealthRow(), isAppend);
    }

    protected boolean saveCsv(String filePath, String filename, String header, String row, boolean isAppend) {
        boolean result = false;
        File outputDir = new File(filePath);
        if(!outputDir.isDirectory()){
            outputDir.mkdirs();
        }

        String fullFilename = filePath + File.separator + filename;

        File file = new File(fullFilename);
        FileWriter aFileWriter = null;
        try {
            aFileWriter = new FileWriter(fullFilename, isAppend);
            if ((!file.exists()) || !isAppend || file.length() == 0) {
                aFileWriter.write(header);
                aFileWriter.write(System.getProperty("line.separator"));
            }

            aFileWriter.write(row);
            aFileWriter.write(System.getProperty("line.separator"));
            aFileWriter.flush();
            aFileWriter.close();
            aFileWriter = null;
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        } finally{
            try {
                if (aFileWriter != null) {
                    aFileWriter.flush();
                    aFileWriter.close();
                }
            }catch(IOException e){
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        }
        return result;
    }

    protected boolean bufferedSaveCsv(String filePath, String filename, String header, String row, boolean isAppend) throws IOException {
        boolean result = false;
        if(bufferedWriter == null) {
            File outputDir = new File(filePath);
            if (!outputDir.isDirectory()) {
                outputDir.mkdirs();
            }
            String fullFilename = filePath + File.separator + filename;

            File file = new File(fullFilename);
            try {
                if ((!file.exists()) || !isAppend || file.length() == 0) {
                    flushAndCloseCsv();
                    aFileWriter = new FileWriter(fullFilename, isAppend);
                    bufferedWriter = new BufferedWriter(aFileWriter);
                    bufferedWriter.write(header);
                    bufferedWriter.newLine();
                }else{
                    aFileWriter = new FileWriter(fullFilename, true);
                    bufferedWriter = new BufferedWriter(aFileWriter);
                }
                bufferedWriter.write(row);
                bufferedWriter.newLine();
                result = true;
            } catch (IOException e) {
                throw e;
            }
        }else{
            try {
                String fullFilename = filePath + File.separator + filename;

                File file = new File(fullFilename);
                if ((!file.exists()) || !isAppend || file.length() == 0) {
                    flushAndCloseCsv();
                    aFileWriter = new FileWriter(fullFilename, isAppend);
                    bufferedWriter = new BufferedWriter(aFileWriter);
                }
                bufferedWriter.write(row);
                bufferedWriter.newLine();
                result = true;
            } catch (IOException e) {
                throw e;
            }

        }
        return result;
    }

    protected boolean bufferedSaveBinary(String filePath, String filename, String header, byte[] row, boolean isAppend) throws Exception {
        boolean result = false;
        if(bufferedOutputStream == null) {
            File outputDir = new File(filePath);
            if (!outputDir.isDirectory()) {
                outputDir.mkdirs();
            }
            String fullFilename = filePath + File.separator + filename;

            File file = new File(fullFilename);
            try {
                if ((!file.exists()) || !isAppend || file.length() == 0) { // When new hour starts
                    flushAndCloseBinary(false);
                    fileOutputStream = new FileOutputStream(fullFilename, isAppend);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 20000);
                }else{
                    fileOutputStream = new FileOutputStream(fullFilename, isAppend);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 20000);
                }
                bufferedOutputStream.write(row);
                result = true;
            } catch (IOException e) {
                throw e;
            }
        }else{
            try {
                String fullFilename = filePath + File.separator + filename;

                File file = new File(fullFilename);
                if ((!file.exists()) || !isAppend || file.length() == 0) {
                    flushAndCloseBinary(false);
                    fileOutputStream = new FileOutputStream(fullFilename, isAppend);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 20000);
                }
                bufferedOutputStream.write(row);
                result = true;
            } catch (IOException e) {
                throw e;
            }

        }
        return result;
    }

    public boolean bufferedWriteTomHealthCsv(boolean isAppend) throws IOException {
        String path = null;
        String filename = mHealthFormat.getmHealthFilenameForCurrentHour(section1, section2, getEntityType());
        Date specificDate = mHealthFormat.extractDateFromFilename(filename);
        try {
            path = mHealthFormat.buildmHealthPath(specificDate, mHealthFormat.PATH_LEVEL.HOURLY, getEntityType());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String row = tomHealthRow();
        return bufferedSaveCsv(path, filename, getHeader(), row, isAppend);
    }

    public boolean bufferedWriteToCustomCsv(String filePath, String filename, boolean isAppend) throws IOException {
        String row = tomHealthRow();
        return bufferedSaveCsv(filePath, filename, getHeader(),row, isAppend);
    }

    public void flushAndCloseCsv() throws IOException {
        try {
            if(bufferedWriter != null) {
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (IOException e) {
            throw e;
        } finally{
            bufferedWriter = null;
            aFileWriter = null;
        }
    }

    public void flushAndCloseBinary(boolean force) throws Exception {
        try {
            if(bufferedOutputStream != null) {
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            }
        } catch (IOException e) {
            throw e;
        } finally{
            bufferedOutputStream = null;
            fileOutputStream = null;
        }
    }

    public boolean bufferedWriteTomHealthBinary(boolean isAppend) throws Exception {
        String path = null;
        String filename = mHealthFormat.getmHealthBinaryFilenameForCurrentHour(new Date(), section1, section2, getEntityType());
        Date specificDate = mHealthFormat.extractDateFromFilename(filename);
        try {
            path = mHealthFormat.buildmHealthPath(specificDate, mHealthFormat.PATH_LEVEL.HOURLY, getEntityType());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        byte[] row = encodeRowAsBinary();
        return bufferedSaveBinary(path, filename, getHeader(), row, isAppend);
    }
}
