package com.sovereigncraft.economy;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.Map;

public class json {
    public static Map JSON2Map (String JSONString) {
        JsonReader jsonReader = new JsonReader( new StringReader(JSONString) );
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonReader, Map.class);
        return map;
    }
}
