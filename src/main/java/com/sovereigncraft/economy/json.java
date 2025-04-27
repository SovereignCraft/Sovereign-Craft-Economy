package com.sovereigncraft.economy;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
//import java.lang.reflect.Array; // isn't used.
import java.util.List;
import java.util.Map;
//import java.util.ArrayList; // isn't used.
//import java.util.Arrays; // isn't used.

public class json {
    public static Map<String, Object> JSON2Map (String JSONString) {
        JsonReader jsonReader = new JsonReader( new StringReader(JSONString) );
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(jsonReader, Map.class);
        return map;
    }
    public static List<String> JSON2List (String JSONString){
        JsonReader jsonReader = new JsonReader( new StringReader(JSONString));
        Gson gson = new Gson();
        List<String> mcArray = gson.fromJson(jsonReader, List.class);
        return mcArray;
    }
    // Added for LNBits 1.0.0 - parses JSON array of objects into List<Map<String, Object>>
    public static List<Map<String, Object>> JSON2ListOfMaps(String JSONString) {
        JsonReader jsonReader = new JsonReader(new StringReader(JSONString));
        Gson gson = new Gson();
        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType();
        return gson.fromJson(jsonReader, listType);
    }
}
