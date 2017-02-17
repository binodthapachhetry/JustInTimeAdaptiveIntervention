package edu.neu.mhealth.android.wockets.library.database.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * @author Dharam Maniar
 */

public class FileUploadEntry extends SugarRecord {

    @Unique
    String path;

    public FileUploadEntry() {
    }

    public FileUploadEntry(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
