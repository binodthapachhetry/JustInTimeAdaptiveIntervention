package edu.neu.android.wearwocketslib.utils.io;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import edu.neu.android.wearwocketslib.utils.log.Logger;

/**
 * Created by qutang on 8/26/15.
 */
public class SizeLimitedZipper {

    private long sizeLimit;
    private String zipBaseFilename;
    private String zipRootFolder;
    private String dataType;
    private ZipParameters parameters;

    public SizeLimitedZipper(String zipBaseFilename, String zipRootFolder, String dataType , long sizeLimit){
        this.sizeLimit = sizeLimit;
        this.zipBaseFilename = zipBaseFilename;
        this.zipRootFolder = zipRootFolder;
        this.dataType = dataType;
        parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
        parameters.setIncludeRootFolder(true);
    }

    // generate file size limited logs
    public String addToZip(String fileOrDir, boolean deleteOrigin, boolean singleFileReplace) throws ZipException, IOException {
        // search for existing and not full zip files
        File zipRoot = new File(zipRootFolder);
        if(zipRoot.isDirectory()){
            File[] existingZips = zipRoot.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean match = pathname.getName().contains(zipBaseFilename) && pathname.getName().contains("zip");
                    match = match && pathname.length() < sizeLimit;
                    return match;
                }
            });
            ZipFile selectedZipFile = null;
            if(existingZips == null || existingZips.length == 0){
                // no existing zip found, create one
                if(!singleFileReplace) {
                    selectedZipFile = new ZipFile(zipRootFolder + zipBaseFilename + "." + System.currentTimeMillis() + "." + dataType + ".zip");
                }else{
                    selectedZipFile = new ZipFile(zipRootFolder + zipBaseFilename + "." + dataType + ".zip");
                }
            }else{
                selectedZipFile = new ZipFile(existingZips[0]);
                if(singleFileReplace){
                    // delete the old one
                    if(!existingZips[0].delete()){
                        throw new IOException("Fail to delete the old zip file: " + existingZips[0].getAbsolutePath());
                    }
                }
            }

            // toBeZipped file
            File toBeZipped = new File(fileOrDir);

            // set zip root folder
            parameters.setRootFolderInZip(toBeZipped.getParent());
            // add to zip
            if(toBeZipped.isDirectory()){
                selectedZipFile.addFolder(fileOrDir, parameters);
            }else{
                selectedZipFile.addFile(toBeZipped, parameters);
            }

            // delete original
            if(deleteOrigin){
                if(toBeZipped.isDirectory()){
                    FileUtils.deleteDirectory(toBeZipped);
                }else{
                    if(!toBeZipped.delete()){
                        throw new IOException("Fail to delete: " + toBeZipped.getAbsolutePath());
                    }
                }
            }
            return selectedZipFile.getFile().getAbsolutePath();
        }else{
            throw new IOException("The provided root folder for zipping is not a directory: " + zipRootFolder);
        }
    }
}
