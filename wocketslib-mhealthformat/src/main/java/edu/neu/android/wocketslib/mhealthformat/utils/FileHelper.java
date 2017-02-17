package edu.neu.android.wocketslib.mhealthformat.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;


public class FileHelper {
    public static ArrayList<File> getRecusiveDirs(File aDir, int recursiveLevel) {
        ArrayList<File> result = new ArrayList<File>();
        if (recursiveLevel >= 0) {
            for (File entry : aDir.listFiles()) {
                if (entry.isDirectory()) {
                    result.add(entry);
                    result.addAll(getRecusiveDirs(entry, recursiveLevel - 1));
                }
            }
        } else {
            result.add(aDir);
        }
        return result;
    }

    public static ArrayList<String> getRecusiveDirs(String aDir, int recursiveLevel) {
        ArrayList<String> result = new ArrayList<String>();
        if (recursiveLevel >= 0) {
            for (File entry : new File(aDir).listFiles()) {
                if (entry.isDirectory()) {
                    result.addAll(getRecusiveDirs(entry.getAbsolutePath(), recursiveLevel - 1));
                }
            }
        } else {
            result.add(aDir);
        }
        return result;
    }

    public static File[] findFiles(String dirStr, final String pattern){
        File dir = new File(dirStr);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(pattern);
            }
        });
        return files;
    }
}
