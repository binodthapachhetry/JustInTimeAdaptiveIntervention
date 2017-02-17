package edu.neu.android.wocketslib.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.util.Log;
import edu.neu.android.wocketslib.Globals;

public class Zipper {
    private static final String TAG = "Zipper";

    private static final int BUFFER = 2048;

    public static File zipFile(Context c, File inputFile) {
        return zipFile(c, inputFile, "");
    }

    public static File zipFile(Context c, File inputFile, String extensionToPrepend) {
        return zipFile(c, inputFile, extensionToPrepend, "");
    }

    public static File zipFile(Context c, File inputFile, String extensionToPrepend, String desiredFileName) {

        if (inputFile == null) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Error creating temporary zip file because inputFile null");
            return null;
        }

        if ((inputFile.length() == 0) && (!(inputFile.isDirectory()))) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Error creating temporary zip file because inputFile has zero bytes");
            return null;
        }

        File outputDir = c.getFilesDir();
        File outputFile = null;
        try {
            // prefix needs to be at least three characters for some reason
            outputFile = File.createTempFile(inputFile.getName() + ".tmp", extensionToPrepend + ".zip", outputDir);
        } catch (IOException e) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Error creating temporary zip file: " + inputFile.getName(), e);
            return null;
        }

        if (outputFile == null) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Can't create temporary zip file: " + inputFile.getName());
            return null;
        }

        if (inputFile.isDirectory()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(outputFile);
                createZipFile(inputFile, fos, false);
                FileHelper.tryClose(fos, TAG);
                return outputFile;
            } catch (IOException e) {
                edu.neu.android.wocketslib.utils.Log.e(TAG, "Error when zipping: " + e.toString(), e);
            } finally{
                if(fos != null){
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        BufferedInputStream origin = null;
        ZipOutputStream out = null;
        FileOutputStream dest = null;
        FileInputStream fi = null;

        long startTime = System.currentTimeMillis();
        try {
            dest = new FileOutputStream(outputFile);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            if (Globals.IS_DEBUG) {
                edu.neu.android.wocketslib.utils.Log.d(TAG, "Zip adding: " + outputFile);
            }
            fi = new FileInputStream(inputFile);
            origin = new BufferedInputStream(fi, BUFFER);

            String name = inputFile.getName();
            if ((desiredFileName != null) && (!(desiredFileName.equals("")))) {
                name = desiredFileName;
            }
            ZipEntry entry = new ZipEntry(name);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            if (Globals.IS_DEBUG) {
                edu.neu.android.wocketslib.utils.Log.d(TAG, "Compress time: " + (System.currentTimeMillis() - startTime));
            }
        } catch (IOException e) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Error when zipping: " + e.toString(), e);
            return null;
        } finally {
            if (origin != null)
                try {
                    origin.close();
                } catch (IOException e) {
                    edu.neu.android.wocketslib.utils.Log.e(TAG, "Problem closing file when zipping: " + e.toString(), e);
                }
            if (out != null)
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    edu.neu.android.wocketslib.utils.Log.e(TAG, "Problem closing file when zipping: " + e.toString(), e);
                    return null;
                }
            if (dest != null)
                try {
                    dest.flush();
                    dest.close();
                } catch (IOException e) {
                    edu.neu.android.wocketslib.utils.Log.e(TAG, "Problem closing file when zipping: " + e.toString(), e);
                    return null;
                }
            if (fi != null)
                try {
                    fi.close();
                } catch (IOException e) {
                    edu.neu.android.wocketslib.utils.Log.e(TAG, "Problem closing file when zipping: " + e.toString(), e);
                }
        }

        if (outputFile.length() == 0) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Zip created a file of size 0: " + outputFile.getName());
            return null;
        }

        return outputFile;
    }

    // see
    // http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure

    public static void createZipFile(File srcDir, OutputStream out, boolean verbose) throws IOException {

        List<String> fileList = null;
        try {
            fileList = FileHelper.listDirectory(srcDir);
        } catch (IOException e) {
            throw e;
        }
        ZipOutputStream zout = null;
        zout = new ZipOutputStream(out);

        zout.setLevel(9);
        zout.setComment("Zipper v1.2");

        for (String fileName : fileList) {
            File file = new File(srcDir.getParent(), fileName);
            if (verbose)
                System.out.println("  adding: " + fileName);

            // Zip always use / as separator
            String zipName = fileName;
            if (File.separatorChar != '/')
                zipName = fileName.replace(File.separatorChar, '/');
            ZipEntry ze;
            if (file.isFile()) {
                ze = new ZipEntry(zipName);
                ze.setTime(file.lastModified());
                try {
                    zout.putNextEntry(ze);
                } catch (IOException e) {
                    throw e;
                } finally{
                    if(zout != null){
                        try {
                            zout.close();
                        } catch (IOException e) {
                            throw e;
                        }
                    }
                }
                FileInputStream fin = null;
                try{
                    fin = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    for (int n; (n = fin.read(buffer)) > 0; )
                        zout.write(buffer, 0, n);
                }catch(IOException e){
                    throw e;
                }finally{
                    try {
                        if (fin != null) {
                            fin.close();
                        }
                    }catch(IOException e){
                        throw e;
                    }
                }
            } else {
                ze = new ZipEntry(zipName + '/');
                ze.setTime(file.lastModified());
                try {
                    zout.putNextEntry(ze);
                } catch (IOException e) {
                    throw e;
                } finally{
                    if(zout != null){
                        try {
                            zout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static File zipThenZipFiles(Context c, File[] files, String outputName, File outputDir, String extensionToPrependToZips) {
        File z = null;
        if (files == null) {
            return null;
        }
        int numFilesAdded = 0;
        ArrayList<File> zipped = new ArrayList<File>();
        for (File f : files) {
            if (!f.getName().endsWith(".zip")) {
                z = zipFile(c, f, ".json");
            } else {
                z = f;
            }
            if (z != null) {
                zipped.add(z);
            }
        }

        File outputFile = null;
        try {
            outputFile = File.createTempFile(outputName + ".tmp", ".zip", outputDir);
            zipFiles(zipped.toArray(new File[]{}), outputFile);
        } catch (IOException e) {
            edu.neu.android.wocketslib.utils.Log.e(TAG, "Error creating zip of zips: " + e, e);
        } finally {
            for (File f : zipped) {
                f.delete();
            }
        }
        return outputFile;
    }

    public static File zipThenZipJSONFilesOnePass(Context c, File aDirectory, int minNumFiles, int maxNumFiles) {
        File[] files = aDirectory.listFiles();

        if (files == null) {
            Log.d(TAG, "No files in directory to zip then zip");
            return null;
        }

        ArrayList<File> zipped = new ArrayList<File>();

        int numFilesAdded = 0;

        int index = 0;
        File f;
        while ((index < files.length) &&
                (numFilesAdded < maxNumFiles))
        {
            f = files[index];

            if ((f.getName().endsWith(".json.zip")) ||
                    (f.getName().endsWith(".json")))
            {
                // A JSON or zipped JSON file, so include if haven't maxed out
                zipped.add(f);
                Log.d(TAG, "Add " + numFilesAdded + " file to zip: " + f.toString());
                numFilesAdded++;
            }
            index++;
        }

        if (numFilesAdded < minNumFiles)
        {
            //Too few JSON files to zip, so do nothing
            return null;
        }

        // Proper number of files to zip, so do the work. Output file name is in same
        // directory with a timestamped name [timestamp]-jsons.json.zip

        File outputFile = null;
        String outFileName = System.currentTimeMillis() + ".jsons.zip";
        boolean zipSuccessful = false;
        try {
            outputFile = new File(aDirectory, outFileName);
            zipFiles(zipped.toArray(new File[]{}), outputFile);
            Log.d(TAG, "Zipped " + numFilesAdded + " files to: " + outputFile.getAbsolutePath());
            zipSuccessful = true;
        } catch (IOException e) {
            Log.e(TAG, "Error creating zip of JSONs named: " + outputFile.getAbsolutePath() + " Error: " + e.toString());
            outputFile.delete();
            outputFile = null;
            zipSuccessful = false;
        }

        if (zipSuccessful)
        {
            // Delete the files that were zipped
            for (File zippedFile : zipped) {
                if (!zippedFile.delete())
                    Log.e(TAG, "Error deleting file: " + zippedFile.getAbsolutePath());
            }
        }
        return outputFile;
    }

    public static void zipThenZipJSONFiles(Context c, File aDirectory, int minNumFiles, int maxNumFiles) {

        File outputFile = zipThenZipJSONFilesOnePass(c, aDirectory, minNumFiles, maxNumFiles);

        while (outputFile != null)
        {
            // Keep passing over directory until no more JSON files to zip
            Log.d(TAG, "Another JSON zip pass....");
            outputFile = zipThenZipJSONFilesOnePass(c, aDirectory, minNumFiles, maxNumFiles);
        }
    }

    // see
    // http://www.java2s.com/Code/Java/File-Input-Output/Zipalistoffileintoonezipfile.htm

    /**
     * Zip a list of file into one zip file.
     *
     * @param files         files to zip
     * @param targetZipFile target zip file
     * @throws IOException IO error exception can be thrown when copying ...
     */
    public static void zipFiles(final File[] files, final File targetZipFile) throws IOException {
        FileOutputStream fos = null;

        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(targetZipFile);
            zos = new ZipOutputStream(fos);

            byte[] buffer = new byte[128];
            for (int i = 0; i < files.length; i++) {
                File currentFile = files[i];
                if (!currentFile.isDirectory()) {
                    ZipEntry entry = new ZipEntry(currentFile.getName());
                    FileInputStream fis = new FileInputStream(currentFile);
                    zos.putNextEntry(entry);
                    int read = 0;
                    while ((read = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, read);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
        }catch(IOException e){
            throw e;
        }finally{
            try {
                if (fos != null) {
                    fos.close();
                }
                if (zos != null) {
                    zos.close();
                }
            }catch(IOException e){
                throw e;
            }
        }
    }


    public static boolean zipFile(File aFile, boolean isReplace) {
        return zipFile(aFile, isReplace, true);
    }

    public static boolean zipFile(File aFile, boolean isReplace, boolean removeDoubleUnderscores) {

        boolean isZipped = Zipper.zipFile(aFile.getAbsolutePath(), isReplace, removeDoubleUnderscores);

        if (!isZipped) {
            Log.e(TAG, "Failed to zip file: " + aFile.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Zips a folder and saves the file in the directory in which the folder
     * sits named after the folder name. For example, if the folder is
     * /data/files then the result will be a file /data/files.zip
     *
     * @param aDir Directory to zip
     * @param isReplace True to replace if a file already exists with destination file name
     * @return True if successful
     */
    public static boolean zipFolder(File aDir, boolean isReplace) {
        File aDestinationFile = new File(aDir.getParentFile(), aDir.getName() + ".zip");
        return zipFolder(aDir, aDestinationFile, isReplace);
    }

    public static boolean zipFolder(File aDir, File desDir, boolean isReplace){
        File aDestinationFile = desDir;
        boolean isZipped = Zipper.zipFolder(aDir, aDestinationFile);

        if (!isZipped) {
            Log.e(TAG, "Failed to zip directory: " + aDir.getAbsolutePath());
            return false;
        }

        // Zipping successful, so delete directories and only leave zip
        if (isReplace)
            if (!FileHelper.deleteDir(aDir)) {
                Log.e(TAG, "Error deleting directory after successfully zipping it: " + aDir.getAbsolutePath());
                return false;
            }
        return true;
    }

    // public static boolean zipFile(File aFile, boolean isReplace) {
    //
    // return zipFile(aFile.getAbsolutePath(), isReplace);
    // }

    public static boolean zipFile(String aFileName, boolean isReplace) {
        return zipFile(aFileName, isReplace, true);
    }

    public static boolean zipFile(String aFileName, boolean isReplace, boolean removeDoubleUnderscores) {

        if (aFileName.endsWith(".zip")) {
            Log.e(TAG, "Warning: File already compressed: " + aFileName);
            return true;
        }

        String zipFile = aFileName + ".zip";

        BufferedInputStream origin = null;
        ZipOutputStream out = null;
        boolean isSuccess = true;

        long startTime = System.currentTimeMillis();
        try {
            origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            if (Globals.IS_DEBUG)
                Log.d(TAG, "Zip adding: " + zipFile);
            FileInputStream fi = new FileInputStream(aFileName);
            origin = new BufferedInputStream(fi, BUFFER);
            String newFileName = aFileName.substring(aFileName.lastIndexOf("/") + 1);

            if (removeDoubleUnderscores) {
                final String doubleUnderscore = "__";
                if (newFileName.contains(doubleUnderscore)) {
                    newFileName = newFileName.substring(newFileName.lastIndexOf(doubleUnderscore) + doubleUnderscore.length());
                }
            }

            ZipEntry entry = new ZipEntry(newFileName);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            if (Globals.IS_DEBUG)
                Log.d(TAG, "Compress time: " + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error when zipping: " + e.toString());
            isSuccess = false;
        } finally {
            if (origin != null)
                try {
                    origin.close();
                } catch (IOException e) {
                    Log.e(TAG, "Problem closing file when zipping: " + e.toString());
                    e.printStackTrace();
                    isSuccess = false;
                }
            if (out != null)
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Problem closing file when zipping: " + e.toString());
                    e.printStackTrace();
                    isSuccess = false;
                }
        }

        File aFile = new File(zipFile);
        if (aFile.length() == 0) {
            Log.e(TAG, "Zip created a file of size 0: " + zipFile);
            isSuccess = false;
        }

        if (isSuccess && isReplace) {
            aFile = new File(aFileName);
            if (!aFile.delete()) {
                Log.e(TAG, "Error deleting file after zipping: " + aFileName);
                isSuccess = false;
            }
        }

        return isSuccess;
    }

    static public boolean zipFolder(File srcDir, File destFile) {
        return zipFolder(srcDir.getAbsolutePath(), destFile.getAbsolutePath());
    }

    // static public boolean zipFiles(File[] someFiles, File destZipFile,
    // boolean isRemove) {
    // boolean zipOk = true;
    // ZipOutputStream zip = null;
    // FileOutputStream fileWriter = null;
    //
    // try {
    // fileWriter = new FileOutputStream(destZipFile);
    // } catch (FileNotFoundException e) {
    // Log.e(TAG, "Error in zipFolder. File not found: " + destZipFile + " " +
    // e.toString());
    // e.printStackTrace();
    // return false;
    // }
    // zip = new ZipOutputStream(fileWriter);
    //
    // for (File aFile : someFiles) {
    // Log.e(TAG, "Add file to zip: " + aFile.getAbsolutePath());
    // try {
    // addFileToZip(aFile, zip);
    // } catch (Exception e) {
    // Log.e(TAG, "Error zipping folder: " + e.toString());
    // zipOk = false;
    // e.printStackTrace();
    // }
    // }
    //
    // try {
    // zip.flush();
    // zip.close();
    // } catch (IOException e) {
    // if (e.getMessage().compareTo("No entries") == 0) {
    // Log.e(TAG,
    // "Warning. No files in directory to zip so no zip files created");
    // zipOk = false;
    // }
    //
    // Log.e(TAG, "Error in zipFolder flushing or closing zip file: " +
    // e.toString());
    // e.printStackTrace();
    // zipOk = false;
    // }
    //
    // if (zipOk && isRemove) {
    // for (File aFile : someFiles)
    // {
    // if (!aFile.delete())
    // zipOk = false;
    // }
    // if (!zipOk) {
    // Log.e(TAG, "Error deleting one or more of files zipped.");
    // }
    // }
    //
    // return zipOk;
    // }

    static public boolean zipFiles(File[] someFiles, String srcFolder, String destZipFile) {
        ZipOutputStream zip;
        FileOutputStream fileWriter;

        try {
            fileWriter = new FileOutputStream(destZipFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error in zipFolder. File not found: " + destZipFile + " " + e.toString());
            e.printStackTrace();
            return false;
        }
        zip = new ZipOutputStream(fileWriter);

        try {
            addFilesToZip(someFiles, "", srcFolder, zip);
        } catch (Exception e) {
            Log.e(TAG, "Error zipping folder: " + e.toString());
            e.printStackTrace();
        }

        try {
            zip.flush();
            zip.close();
        } catch (IOException e) {
            if (e.getMessage().compareTo("No entries") == 0) {
                Log.e(TAG, "Warning. No files in directory to zip so no zip files created: " + srcFolder);
                return false;
            }

            Log.e(TAG, "Error in zipFolder flushing or closing zip file: " + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }


    static public boolean zipFolder(String srcFolder, String destZipFile) {
        ZipOutputStream zip;
        FileOutputStream fileWriter;

        try {
            fileWriter = new FileOutputStream(destZipFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error in zipFolder. File not found: " + destZipFile + " " + e.toString());
            e.printStackTrace();
            return false;
        }
        zip = new ZipOutputStream(fileWriter);

        try {
            addFolderToZip("", srcFolder, zip);
        } catch (Exception e) {
            Log.e(TAG, "Error zipping folder: " + e.toString());
            e.printStackTrace();
        }

        try {
            zip.flush();
            zip.close();
        } catch (IOException e) {
            if (e.getMessage().compareTo("No entries") == 0) {
                Log.e(TAG, "Warning. No files in directory to zip so no zip files created: " + srcFolder);
                return false;
            }

            Log.e(TAG, "Error in zipFolder flushing or closing zip file: " + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {

        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);

        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }

    static private void addFilesToZip(File[] someFiles, String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);

        for (File aFile : someFiles) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + aFile.getName(), zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + aFile.getName(), zip);
            }
        }
    }


    public static String uncompressInputStream(InputStream inputStream) throws IOException {
        StringBuilder value = new StringBuilder();

        GZIPInputStream gzipIn = null;
        InputStreamReader inputReader = null;
        BufferedReader reader = null;

        try {
            gzipIn = new GZIPInputStream(inputStream);
            inputReader = new InputStreamReader(gzipIn, "UTF-8");
            reader = new BufferedReader(inputReader);

            String line;
            while ((line = reader.readLine()) != null) {
                value.append(line).append("\n");
            }
        } finally {
            try {
                if (gzipIn != null) {
                    gzipIn.close();
                }

                if (inputReader != null) {
                    inputReader.close();
                }

                if (reader != null) {
                    reader.close();
                }

            } catch (IOException io) {
                Log.e(TAG, "Error in uncompressInputStream: " + io.toString());
                io.printStackTrace();
            }
        }

        return value.toString();
    }

    public static boolean compressGzipFile(String file, String gzipFile) {
        boolean result = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPOutputStream gzipOS = null;
        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(gzipFile);
            gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.flush();
            gzipOS.close();
            gzipOS = null;
            fos.flush();
            fos.close();
            fos = null;
            fis.close();
            fis = null;
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            edu.neu.android.wocketslib.utils.Log.e(TAG, e.getMessage());
            edu.neu.android.wocketslib.utils.Log.logStackTrace(TAG, e);
        } finally{
            try{
                if(gzipOS != null){
                    gzipOS.flush();
                    gzipOS.close();
                }
                if(fis != null){
                    fis.close();
                }
                if(fos != null){
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void decompressGzipFile(String gzipFile, String outputFile){

        byte[] buffer = new byte[1024];
        GZIPInputStream gzis = null;
        FileOutputStream out = null;
        try{

            gzis = new GZIPInputStream(new FileInputStream(gzipFile));

            out = new FileOutputStream(outputFile);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }finally{
            try{
                if(gzis != null){
                    gzis.close();
                }
                if(out != null){
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
