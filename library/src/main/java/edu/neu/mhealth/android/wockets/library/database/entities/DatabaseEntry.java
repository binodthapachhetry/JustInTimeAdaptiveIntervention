package edu.neu.mhealth.android.wockets.library.database.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * @author Dharam Maniar
 */

public class DatabaseEntry extends SugarRecord {

    @Unique
    String path;
    String object;

    public DatabaseEntry() {
    }

    public DatabaseEntry(String path, String object) {
        this.path = path;
        this.object = object;
    }

    public String getPath() {
        return path;
    }

    public String getObject() {
        return object;
    }
}
