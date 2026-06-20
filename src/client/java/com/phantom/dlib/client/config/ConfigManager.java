package com.phantom.dlib.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDirectory, "config/dlib");
    private static final LinkedHashMap<String, Map<String, Boolean>> CONFIG_REGISTRY = new LinkedHashMap<>();

    // --- REAL MOD OPTIONS DATA CONFIGURATION ---
    static {
        registerDefaultOptions("PvpEssentials", List.of("Arrow HUD Enabled", "Show Durability Warning", "Clean Crosshair"));
        registerDefaultOptions("PhantomCore", List.of("Enhanced Performance", "Show FPS Counter", "Custom Capes Enabled"));
        registerDefaultOptions("DLib-Engine", List.of("Debug Logging", "Optimize Atlas Textures", "Matrix Transform Cache"));
        registerDefaultOptions("ExampleMod", List.of("Toggle Box A", "Toggle Box B"));
    }

    private static void registerDefaultOptions(String modName, List<String> optionKeys) {
        Map<String, Boolean> optionsMap = new LinkedHashMap<>();
        for (String key : optionKeys) {
            optionsMap.put(key, true); // Default all options to true
        }
        CONFIG_REGISTRY.put(modName, optionsMap);
    }

    /**
     * Scans and loads mod configuration json files from the storage directory.
     */
    public static void load() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        for (String mod : CONFIG_REGISTRY.keySet()) {
            File file = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json");
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json != null) {
                        Map<String, Boolean> optionsMap = CONFIG_REGISTRY.get(mod);
                        for (String key : optionsMap.keySet()) {
                            if (json.has(key)) {
                                optionsMap.put(key, json.get(key).getAsBoolean());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                saveMod(mod); // Generate default file if it doesn't exist
            }
        }
    }

    /**
     * Serializes a specific mod's configuration layout back to its respective JSON file.
     */
    public static void saveMod(String mod) {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        File file = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject json = new JsonObject();
            Map<String, Boolean> optionsMap = CONFIG_REGISTRY.get(mod);
            for (Map.Entry<String, Boolean> entry : optionsMap.entrySet()) {
                json.addProperty(entry.getKey(), entry.getValue());
            }
            GSON.toJson(json, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getRegisteredMods() {
        return new ArrayList<>(CONFIG_REGISTRY.keySet());
    }

    public static Map<String, Boolean> getOptionsForMod(String mod) {
        return CONFIG_REGISTRY.getOrDefault(mod, Collections.emptyMap());
    }

    public static boolean getBoolean(String mod, String key) {
        return CONFIG_REGISTRY.getOrDefault(mod, Collections.emptyMap()).getOrDefault(key, false);
    }

    public static void setBoolean(String mod, String key, boolean value) {
        if (CONFIG_REGISTRY.containsKey(mod) && CONFIG_REGISTRY.get(mod).containsKey(key)) {
            CONFIG_REGISTRY.get(mod).put(key, value);
            saveMod(mod); // Save immediately upon configuration value updates
        }
    }
}