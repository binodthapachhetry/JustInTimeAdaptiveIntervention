package edu.neu.mhealth.android.wockets.library.support;

import android.content.Context;
import android.os.Looper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Dharam Maniar
 */
public class CSV {

    private static final String TAG = "CSV";

    /**
     * Write a csv file.
     *
     * @param line      The line that needs to be written to the file.
     * @param file      The file to which the line needs to be written
     * @param isAppend  Boolean indicating whether the line needs to be appended or overwritten
     */
    public static synchronized void write(String[] line, String file, boolean isAppend) {

//        if (Looper.myLooper() == Looper.getMainLooper()){
//            Log.d(TAG,"Inside Main Thread");
//        }
        try {
            File fileToWrite = new File(file);
            File directory = fileToWrite.getParentFile();
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
            CSVWriter writer = new CSVWriter(new FileWriter(file, isAppend));
            writer.writeNext(line);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a csv file.
     *
     * @param line      The line that needs to be written to the file.
     * @param file      The file to which the line needs to be written
     * @param isAppend  Boolean indicating whether the line needs to be appended or overwritten
     */
    public static synchronized void writeAndZip(String[] line, String file, boolean isAppend, Context context) {


        try {
            File zippedFile = new File(file + ".zip");
            if (zippedFile.exists()) {
                Zipper.unZipFileWithEncryption(zippedFile.getAbsolutePath(), context);
            }
            File fileToWrite = new File(file);
            File directory = fileToWrite.getParentFile();
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
            CSVWriter writer = new CSVWriter(new FileWriter(file, isAppend));
            writer.writeNext(line);
            writer.close();
            Zipper.zipFileWithEncryption(file, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a csv file
     *
     * @param file  The file which needs to be read
     * @return The list of string arrays read from the file
     */
    public static List<String[]> read(String file) {
        try {
            CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
            return reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
