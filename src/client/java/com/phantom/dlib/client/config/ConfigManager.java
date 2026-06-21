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
    
    // Structure: ModID -> Category -> OptionKey -> OptionObject
    private static final LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ConfigOption>>> REGISTRY = new LinkedHashMap<>();

    static {
        // --- PRE-POPULATING SPLIT INPUT TYPES & CATEGORIES ---
        defineOption("PvpEssentials", "Visuals", "Arrow HUD", "toggle", "true", null);
        defineOption("PvpEssentials", "Visuals", "HUD Theme", "cycle", "Dark", List.of("Dark", "Light", "Chroma"));
        defineOption("PvpEssentials", "Gameplay", "Alert Message", "text", "Watch Out!", null);
        defineOption("PvpEssentials", "Gameplay", "Max Alerts", "number", "5", null);

        defineOption("PhantomCore", "Optimization", "Fast Rendering", "toggle", "true", null);
        defineOption("PhantomCore", "Personalization", "Custom Prefix", "text", "[Phantom]", null);
        defineOption("PhantomCore", "Personalization", "Max Scale", "number", "1.5", null);
        defineOption("PhantomCore", "Personalization", "Cape Style", "cycle", "Classic", List.of("Classic", "Minimal", "None"));
    }

    private static void defineOption(String mod, String category, String key, String type, String defaultVal, List<String> choices) {
        REGISTRY.computeIfAbsent(mod, k -> new LinkedHashMap<>())
                .computeIfAbsent(category, k -> new LinkedHashMap<>())
                .put(key, new ConfigOption(type, defaultVal, defaultVal, choices));
    }

    public static void load() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();

        for (String mod : REGISTRY.keySet()) {
            File file = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json");
            if (!file.exists()) {
                saveMod(mod);
                continue;
            }
            loadSingleMod(mod, file);
        }
    }

    private static void loadSingleMod(String mod, File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject rootJson = GSON.fromJson(reader, JsonObject.class);
            if (rootJson == null) throw new Exception("Empty config layout");

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
            System.err.println("[DLib] Failed parsing config for: " + mod + ". Creating backup recovery...");
            try {
                File backup = new File(CONFIG_DIR, mod.toLowerCase(Locale.ROOT) + ".json.bak");
                Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}
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