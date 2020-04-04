package cz.muni.crocs.appletstore.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Parse JSON file
 */
public interface JsonParser {

    String TAG_TYPE = "type";
    String TAG_NAME = "name";
    String TAG_TITLE = "title";
    String TAG_ICON = "icon";
    String TAG_LATEST = "latest";
    String TAG_VERSION = "versions";
    String TAG_BUILD = "builds";
    String TAG_AUTHOR = "author";
    String TAG_DESC = "description";
    String TAG_URL = "url";
    String TAG_USAGE = "usage";
    String TAG_KEYS = "keys";
    String TAG_DEFAULT_SELECTED = "default_selected";
    String TAG_PGP_IDENTIFIER = "pgp"; //can be email or key ID
    String TAG_PGP_SIGNER = "signed_by";
    String TAG_APPLET_INSTANCE_NAMES = "applet_instance_names";

    /**
     * Get a list of JSON objects of the store items
     *
     * @return list of store items
     * @throws FileNotFoundException no such file to read from
     */
    List<JsonObject> getValues() throws FileNotFoundException;

    /**
     * Convert all objects in JsonArray to its String representation
     *
     * @param array array of JSON objects
     * @return array of string representations of JSON array
     */
    String[] jsonArrayToDataArray(JsonArray array);
}
