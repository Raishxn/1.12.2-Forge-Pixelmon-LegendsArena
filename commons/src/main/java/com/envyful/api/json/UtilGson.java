package com.envyful.api.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * Static utility class for methods regarding Google's GSON API
 *
 */
public class UtilGson {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

}
