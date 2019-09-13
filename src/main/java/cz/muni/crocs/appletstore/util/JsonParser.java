package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.util.List;

public interface JsonParser {

    /**
     * Get a list of JSON objects of the store items
     * @return list of store items
     * @throws FileNotFoundException no such file to read from
     */
    List<JsonObject> getValues() throws FileNotFoundException;

    /**
     * Convert all objects in JsonArray to its String representation
     * @param array array of JSON objects
     * @return array of string representations of JSON array
     */
    String[] jsonArrayToDataArray(JsonArray array);
}
