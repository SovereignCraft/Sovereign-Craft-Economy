package com.sovereigncraft.economy;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

public class json {
    public static Map JSON2Map (String JSONString) {
        JsonReader jsonReader = new JsonReader( new StringReader(JSONString) );
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonReader, Map.class);
        return map;
    }
    public static List<String> JSON2List (String JSONString){
        JsonReader jsonReader = new JsonReader( new StringReader(JSONString));
        Gson gson = new Gson();
        List<String> mcArray = gson.fromJson(jsonReader, List.class);
        return mcArray;
    }
}
