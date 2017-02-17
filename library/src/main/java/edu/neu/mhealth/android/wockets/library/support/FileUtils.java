package edu.neu.mhealth.android.wockets.library.support;

import java.io.File;

/**
 * @author Dharam Maniar
 */
public class FileUtils {

	public static void delete(File file) {
		if (!file.isDirectory()) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
			return;
		}

		for(File fileInDirectory : file.listFiles()) {
			delete(fileInDirectory);
		}
		//noinspection ResultOfMethodCallIgnored
		file.delete();
	}

	public static void renameFile(File fileToRename, String newFileName) {
		if (!fileToRename.exists()) {
			return;
		}
		//noinspection ResultOfMethodCallIgnored
		fileToRename.renameTo(new File(newFileName));
	}

	public static boolean ifExists(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}
}
