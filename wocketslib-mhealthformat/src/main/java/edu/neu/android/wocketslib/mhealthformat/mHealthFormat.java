package edu.neu.android.wocketslib.mhealthformat;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.neu.android.wocketslib.mhealthformat.utils.FileHelper;

/**
 * Created by qutang on 4/23/15.
 */
public class mHealthFormat {
    public static final String mHealthFileNameFormat = "yyyy-MM-dd-HH-mm-ss-SSS";
    public static final String mHealthTimeZoneFormat = "Z";
    public static final String mHealthYearDirFormat = "yyyy";
    public static final String mHealthMonthDirFormat = "MM";
    public static final String mHealthDayDirFormat = "dd";
    public static final String mHealthHourDirFormat = "HH-z";
    public static final String mHealthTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String mHealthDateFormat = "yyyy-MM-dd";

    public static final String ORIGINAL_RAW_DIRECTORY = "OriginalRaw";
    public static final String MASTER_SYNCED_DIRECTORY = "MasterSynced";
    public static final String METADATA_PREFIX = "Metadata-";

//    private static String STUDY_NAME = null;
    private static String STUDY_NAME = "TEMPLE";
    private static String SUBJECT_ID = null;
    private static boolean IS_EXTERNAL_STORAGE;
    private static final String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().getAbsolutePath();


    public static void init(String studyName, boolean isExternal){
        STUDY_NAME = studyName;
        IS_EXTERNAL_STORAGE = isExternal;
    }

    public static String getSubjectId(){
        return SUBJECT_ID;
    }

    public static void setSubjectId(String subjectId){
        SUBJECT_ID = subjectId;
    }

    public static String getExternalStorage(){
        return EXTERNAL_STORAGE;
    }

    public static String buildmHealthPath(ROOT_DIRECTORY root, PATH_LEVEL pathLevel) throws Exception {
        return buildmHealthPath(new Date(), pathLevel, root, null);
    }

    public static String buildmHealthPath(Date specificDate, PATH_LEVEL pathLevel, MHEALTH_FILE_TYPE entityType) throws Exception {
        return buildmHealthPath(specificDate, pathLevel, entityType, null);
    }

    public static String buildmHealthPath(Date specificDate, PATH_LEVEL pathLevel, MHEALTH_FILE_TYPE entityType, String extraPaths) throws Exception {
        switch (entityType) {
            case ANNOTATION:
                return buildmHealthPath(specificDate, pathLevel, ROOT_DIRECTORY.MASTER, extraPaths);
            case FEATURE:
                return buildmHealthPath(specificDate, pathLevel, ROOT_DIRECTORY.METADATA, extraPaths);
            case EVENT:
                return buildmHealthPath(specificDate, pathLevel, ROOT_DIRECTORY.MASTER, extraPaths);
            default:
                return buildmHealthPath(specificDate, pathLevel, ROOT_DIRECTORY.MASTER, extraPaths);
        }
    }

    public static String buildmHealthPath(Date specificDate, PATH_LEVEL pathLevel, ROOT_DIRECTORY root) throws Exception {
        return buildmHealthPath(specificDate, pathLevel, root, null);
    }

    public static String buildmHealthPath(Date specificDate, PATH_LEVEL pathLevel, ROOT_DIRECTORY root, String extraPaths) throws Exception {
        if(STUDY_NAME == null){
            throw new Exception("Initialize with study name first");
        }
        StringBuilder pathBuilder = new StringBuilder();
        if (IS_EXTERNAL_STORAGE) {
            pathBuilder.append(EXTERNAL_STORAGE).append(File.separator);
        } else {
            pathBuilder.append(File.separator);
        }
        String subjectId = SUBJECT_ID;
        if(subjectId == null){
            subjectId = "data";
        }else{
            subjectId = "data" + File.separator + SUBJECT_ID;
        }
        pathBuilder.append(".").append(STUDY_NAME).append(File.separator).append(subjectId).append(File.separator);
//        switch (root) {
//            case RAW:
//                pathBuilder.append(ORIGINAL_RAW_DIRECTORY).append(File.separator);
//                break;
//            case MASTER:
//                pathBuilder.append(MASTER_SYNCED_DIRECTORY).append(File.separator);
//                break;
//            case METADATA:
//                if(extraPaths == null){
//                    throw new Exception("Please give a name to the metadata task");
//                }
//                pathBuilder.append(METADATA_PREFIX).append("-").append(extraPaths).append("-").append(_buildFilenameTimestamp(new Date()))
//                        .append(File.separator);
//                break;
//        }
        if (extraPaths != null) {
            pathBuilder.append(extraPaths).append(File.separator);
        }
        pathBuilder.append(_buildPathHelper(specificDate, pathLevel));
        return pathBuilder.toString();
    }

    public static String buildCsvFilename(Date specificDate, String sensorType, String sensorId) {
        return _buildFilenameHelper(specificDate, sensorType, sensorId, "csv");
    }

    public static String buildBafFilename(Date specificDate, String sensorType, String sensorId, String suffix){
        return _buildFilenameHelper(specificDate, sensorType, sensorId, suffix + ".baf");
    }

    public static String buildAnnotationFilename(Date specificDate, String annotationSet, String annotatorId) {
        return _buildFilenameHelper(specificDate, annotationSet, annotatorId, "annotation.csv");
    }

    public static String buildFeatureFilename(Date specificDate, String algorithmType, String algorithmId) {
        return _buildFilenameHelper(specificDate, algorithmType, algorithmId, "feature.csv");
    }

    public static String buildEventFilename(Date specificDate, String eventType, String eventId){
        return _buildFilenameHelper(specificDate, eventType, eventId, "event.csv");
    }

    public static String buildSensorFilename(Date specificDate, String sensorType, String sensorId){
        return _buildFilenameHelper(specificDate, sensorType, sensorId, "sensor.csv");
    }

    public static String getmHealthFilenameForCurrentHour(final String section1, final String section2, MHEALTH_FILE_TYPE fileType) {
        return getmHealthFilenameForCurrentHour(new Date(), section1, section2, fileType, false);
    }

    public static String getmHealthFilenameForCurrentHour(Date specificDate, final String section1, final String section2, final MHEALTH_FILE_TYPE fileType, final boolean binary) {
        ROOT_DIRECTORY root;
        switch (fileType) {
            case CSV:
            case SENSOR:
            case ANNOTATION:
            case EVENT:
                root = ROOT_DIRECTORY.MASTER;
                break;
            case FEATURE:
                root = ROOT_DIRECTORY.METADATA;
                break;
            default:
                root = ROOT_DIRECTORY.MASTER;
        }
        File dir = null;
        try {
            dir = new File(buildmHealthPath(specificDate, PATH_LEVEL.HOURLY, root));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String[] files = dir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                String suffix = "";
                switch (fileType) {
                    case CSV:
                        break;
                    case SENSOR:
                        suffix = ".sensor";
                        break;
                    case ANNOTATION:
                        suffix = ".annotation";
                        break;
                    case FEATURE:
                        suffix = ".feature";
                        break;
                    case EVENT:
                        suffix = "event";
                        break;
                    default:
                        suffix = "";
                }
                String extension = suffix + ".csv";
                String binaryExtension = suffix + ".baf";
                if (filename.endsWith(extension) && filename.contains(section1) && filename.contains(section2))
                    return true;
                else if(binary && filename.endsWith(binaryExtension) && filename.contains(section1) && filename.contains(section2)){
                    return true;
                }else {
                    return false;
                }
            }
        });
        if (files == null || files.length == 0) {
            if(!binary) {
                switch (fileType) {
                    case SENSOR:
                        return buildSensorFilename(specificDate, section1, section2);
                    case CSV:
                        return buildCsvFilename(specificDate, section1, section2);
                    case ANNOTATION:
                        return buildAnnotationFilename(specificDate, section1, section2);
                    case FEATURE:
                        return buildFeatureFilename(specificDate, section1, section2);
                    case EVENT:
                        return buildEventFilename(specificDate, section1, section2);
                    default:
                        return buildCsvFilename(specificDate, section1, section2);
                }
            }else{
                switch (fileType) {
                    case SENSOR:
                        return _buildFilenameHelper(specificDate, section1, section2, "sensor.baf");
                    case CSV:
                        return _buildFilenameHelper(specificDate, section1, section2, "baf");
                    case ANNOTATION:
                        return _buildFilenameHelper(specificDate, section1, section2, "annotation.baf");
                    case FEATURE:
                        return _buildFilenameHelper(specificDate, section1, section2, "feature.baf");
                    case EVENT:
                        return _buildFilenameHelper(specificDate, section1, section2, "event.baf");
                    default:
                        return _buildFilenameHelper(specificDate, section1, section2, "baf");
                }
            }
        } else
            return files[0];
    }

    public static String getmHealthBinaryFilenameForCurrentHour(Date specificDate, final String section1, final String section2, MHEALTH_FILE_TYPE fileType){
        return getmHealthFilenameForCurrentHour(specificDate, section1, section2, fileType, true);
    }

    private static String _buildFilenameHelper(Date specificDate, String section1, String section2, String extension) {
        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(section1).append(".");
        if (section2 != null)
            filenameBuilder.append(section2).append(".");
        String dateStr = _buildFilenameTimestamp(specificDate);
        filenameBuilder.append(dateStr).append(".");
        filenameBuilder.append(extension);
        return filenameBuilder.toString();
    }

    private static String _buildFilenameTimestamp(Date specificDate){
        String dateStr = new SimpleDateFormat(mHealthFileNameFormat).format(specificDate);
        String timeZone = new SimpleDateFormat(mHealthTimeZoneFormat).format(specificDate);
        timeZone = timeZone.replace("-", "M").replace("+", "P");
        dateStr = dateStr + "-" + timeZone;
        return dateStr;
    }

    private static String _buildPathHelper(Date specificDate, PATH_LEVEL pathLevel) {
        StringBuilder pathBuilder = new StringBuilder();
        if (pathLevel.ordinal() >= PATH_LEVEL.YEARLY.ordinal()) {
            String yearDirStr = new SimpleDateFormat(mHealthYearDirFormat).format(specificDate);
//            pathBuilder.append(yearDirStr).append(File.separator);
            pathBuilder.append(yearDirStr).append("-");
        }
        if (pathLevel.ordinal() >= PATH_LEVEL.MONTHLY.ordinal()) {
            String monthDirStr = new SimpleDateFormat(mHealthMonthDirFormat).format(specificDate);
//            pathBuilder.append(monthDirStr).append(File.separator);
            pathBuilder.append(monthDirStr).append("-");
        }
        if (pathLevel.ordinal() >= PATH_LEVEL.DAILY.ordinal()) {
            String dayDirStr = new SimpleDateFormat(mHealthDayDirFormat).format(specificDate);
            pathBuilder.append(dayDirStr).append(File.separator);
        }

        if (pathLevel.ordinal() >= PATH_LEVEL.HOURLY.ordinal()) {
            String hourDirStr = new SimpleDateFormat(mHealthHourDirFormat).format(specificDate);
            pathBuilder.append(hourDirStr).append(File.separator);
        }
        return pathBuilder.toString();
    }

    /*
    *
    * Used for convert hour, minute or seconds int
    * */
    public static String integerToTwoDigitsString(int time) {
        String result;
        if (time < 10) {
            result = "0" + time;
        } else {
            result = Integer.toString(time);
        }
        return result;
    }

    public static boolean deleteAnnotationFiles(Date specificDate) {
        boolean result = true;
        List<String> hourDirs = null;
        try {
            hourDirs = FileHelper.getRecusiveDirs(mHealthFormat.buildmHealthPath(specificDate, PATH_LEVEL.DAILY, ROOT_DIRECTORY.MASTER), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        for (String hourDir : hourDirs) {
            File[] annotationFiles = FileHelper.findFiles(hourDir, ".*annotation.csv.*");
            for (File annotationFile : annotationFiles) {
                result = result & annotationFile.delete();
            }
        }
        return result;
    }

    public static Date extractDateFromFilename(String filename) {
        String[] tokens = filename.split("\\.");
        try {
            Date currentDate = new SimpleDateFormat(mHealthFileNameFormat).parse(tokens[2].substring(0, tokens[2].length() - 5));
            return currentDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static enum PATH_LEVEL {
        ROOT, YEARLY, MONTHLY, DAILY, HOURLY
    }

    public static enum ROOT_DIRECTORY {
        RAW, MASTER, METADATA
    }

    public static enum MHEALTH_FILE_TYPE {
        CSV, ANNOTATION, FEATURE, BATCH, EVENT, BINARY, SENSOR
    }
}
