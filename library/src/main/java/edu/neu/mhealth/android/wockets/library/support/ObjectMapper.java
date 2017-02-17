package edu.neu.mhealth.android.wockets.library.support;

import com.google.gson.Gson;

/**
 * @author Dharam Maniar
 */

public class ObjectMapper {

    private static final String TAG = "ObjectMapper";

    /**
     * Serialize the given object into a JSON string
     *
     * @param object    The object for which Json representation is to be created
     * @return JSON representation for the given object
     */
    public static String serialize(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    /**
     *  Deserialize the given JSON string into the given type of the desired object.
     *
     * @param json      the class of T
     * @param classOfT  the string from which the object is to be deserialized
     * @param <T>       the type of the desired object
     * @return
     */
    public static <T> T deserialize(String json, Class<T> classOfT) {
        Gson gson = new Gson();
        return gson.fromJson(json, classOfT);
    }

}
