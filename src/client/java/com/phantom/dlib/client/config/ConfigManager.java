package com.phantom.dlib.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDirectory, "config/dlib");
    
    // Core Registry: ModID -> Category -> OptionKey -> OptionObject
    private static final LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ConfigOption>>> REGISTRY = new LinkedHashMap<>();

    /**
     * PUBLIC API: Allows external mods to register their configuration layout dynamically.
     */
    public static void registerOption(String modId, String category, String key, String type, String defaultValue, List<String> choices) {
        REGISTRY.computeIfAbsent(modId, k -> new LinkedHashMap<>())
                .computeIfAbsent(category, k -> new LinkedHashMap<>())
                .put(key, new ConfigOption(type, defaultValue, defaultValue, choices));
    }

    /**
     * Reads and applies saved JSON configuration files from disk over the registered defaults.
     * External mods should call this AFTER registering all their settings.
     */
    public static void load() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();

        for (String mod : REGISTRY.keySet()) {
            File file = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json");
            if (!file.exists()) {
                saveMod(mod); // If no file exists, generate one filled with the registered defaults
                continue;
            }
            loadSingleMod(mod, file);
        }
    }

    private static void loadSingleMod(String mod, File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject rootJson = GSON.fromJson(reader, JsonObject.class);
            if (rootJson == null) return;

            LinkedHashMap<String, LinkedHashMap<String, ConfigOption>> categories = REGISTRY.get(mod);
            for (String catName : categories.keySet()) {
                if (rootJson.has(catName) && rootJson.get(catName).isJsonObject()) {
                    JsonObject catJson = rootJson.getAsJsonObject(catName);
                    LinkedHashMap<String, ConfigOption> options = categories.get(catName);

                    for (String key : options.keySet()) {
                        if (catJson.has(key)) {
                            JsonElement valueElem = catJson.get(key);
                            ConfigOption option = options.get(key);
                            if (option.type.equals("toggle") && valueElem.isJsonPrimitive()) {
                                option.value = String.valueOf(valueElem.getAsBoolean());
                            } else if (valueElem.isJsonPrimitive()) {
                                option.value = valueElem.getAsString();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DLib] Failed parsing config for: " + mod);
            saveMod(mod); 
        }
    }

    public static void saveMod(String mod) {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        File file = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json");

        try (FileWriter writer = new FileWriter(file)) {
            JsonObject rootJson = new JsonObject();
            LinkedHashMap<String, LinkedHashMap<String, ConfigOption>> categories = REGISTRY.get(mod);

            for (Map.Entry<String, LinkedHashMap<String, ConfigOption>> catEntry : categories.entrySet()) {
                JsonObject catJson = new JsonObject();
                for (Map.Entry<String, ConfigOption> optEntry : catEntry.getValue().entrySet()) {
                    ConfigOption opt = optEntry.getValue();
                    if (opt.type.equals("toggle")) {
                        catJson.addProperty(optEntry.getKey(), Boolean.parseBoolean(opt.value));
                    } else {
                        catJson.addProperty(optEntry.getKey(), opt.value);
                    }
                }
                rootJson.add(catEntry.getKey(), catJson);
            }
            GSON.toJson(rootJson, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- TYPE-SAFE RUNTIME GETTERS FOR OTHER MODS ---
    
    public static boolean getBoolean(String modId, String category, String key) {
        ConfigOption opt = getOption(modId, category, key);
        return opt != null && Boolean.parseBoolean(opt.value);
    }

    public static String getString(String modId, String category, String key) {
        ConfigOption opt = getOption(modId, category, key);
        return opt != null ? opt.value : "";
    }

    public static double getNumber(String modId, String category, String key) {
        ConfigOption opt = getOption(modId, category, key);
        if (opt == null) return 0.0;
        try {
            return Double.parseDouble(opt.value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static ConfigOption getOption(String modId, String category, String key) {
        LinkedHashMap<String, LinkedHashMap<String, ConfigOption>> modMap = REGISTRY.get(modId);
        if (modMap != null) {
            LinkedHashMap<String, ConfigOption> catMap = modMap.get(category);
            if (catMap != null) {
                return catMap.get(key);
            }
        }
        return null;
    }

    public static void discardChanges(String mod) {
        File file = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json");
        if (!file.exists()) {
            resetToDefaults(mod);
        } else {
            loadSingleMod(mod, file);
        }
    }

    public static void resetToDefaults(String mod) {
        LinkedHashMap<String, LinkedHashMap<String, ConfigOption>> categories = REGISTRY.get(mod);
        if (categories != null) {
            for (LinkedHashMap<String, ConfigOption> options : categories.values()) {
                for (ConfigOption opt : options.values()) {
                    opt.value = opt.defaultValue;
                }
            }
        }
    }

    public static List<String> getRegisteredMods() { return new ArrayList<>(REGISTRY.keySet()); }
    public static LinkedHashMap<String, LinkedHashMap<String, ConfigOption>> getStructureForMod(String mod) { return REGISTRY.getOrDefault(mod, new LinkedHashMap<>()); }

    public static class ConfigOption {
        public final String type; 
        public String value;
        public final String defaultValue;
        public final List<String> choices; 

        public ConfigOption(String type, String value, String defaultValue, List<String> choices) {
            this.type = type;
            this.value = value;
            this.defaultValue = defaultValue;
            this.choices = choices;
        }
    }
}