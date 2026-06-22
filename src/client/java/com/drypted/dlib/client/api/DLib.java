package com.drypted.dlib.client.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class DLib {
    public static final Map<String, ModConfig> REGISTERED_MODS = new LinkedHashMap<>();

    public static ModConfig register(String modName) {
        ModConfig config = new ModConfig(modName);
        REGISTERED_MODS.put(modName, config);
        return config;
    }
}