package edu.neu.android.wocketslib.utils;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;

public class FileTester {

	public static void printAllFilesInInternal(Context c){
		File internalDir = c.getFilesDir();			
		for (File file : getAllFilesInFolder(internalDir)) {
			Log.d("FileTester", file.getAbsolutePath()); //TODO what is this?
		}
	}
	private static ArrayList<File> getAllFilesInFolder(File folder){
		ArrayList<File> files = new ArrayList<File>();
		if(!folder.isDirectory()){
			throw new IllegalArgumentException("File: "+folder.getAbsolutePath()+" is not a directory.");
		}
		File[] subFiles = folder.listFiles();
		if(subFiles != null && subFiles.length > 0){
			for (File file : subFiles) {
				if(file.isFile()){
					files.add(file);
				}
				else if(file.isDirectory()){
					files.addAll(getAllFilesInFolder(file));
				}
			}
		}
		return files;
	}
}
