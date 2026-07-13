package com.drypted.dlib.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDirectory, "config/dlib");
    
    // Core Registry: ModID -> Category -> OptionKey -> ConfigOption
    private static final LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ConfigOption>>> REGISTRY = new LinkedHashMap<>();
    
    // Display names for mods
    private static final Map<String, String> MOD_DISPLAY_NAMES = new LinkedHashMap<>();
    
    // Unique key -> ConfigOption (for fast lookup)
    private static final Map<String, ConfigOption> OPTION_MAP = new HashMap<>();

    /**
     * PUBLIC API: Register a configuration option.
     * @param modId           Internal mod identifier (used for file names)
     * @param modDisplayName  Human‑readable name shown in the GUI
     * @param category        Category within the mod
     * @param key             Option key within the category
     * @param uniqueKey       Globally unique key for this option (used with getOption)
     * @param type            "toggle", "cycle", "text", "number", "item_select", "item_select_multi", "action"
     *                        - "item_select": single item picker (choices = list of item IDs like "minecraft:diamond")
     *                        - "item_select_multi": multi-item picker (same choices format)
     *                        - "action": a button that executes a Runnable callback (use registerAction)
     * @param defaultValue    Default value as string.
     *                        For item_select types, use comma-separated item IDs (e.g. "minecraft:diamond,minecraft:iron_ingot")
     * @param choices         List of choices for "cycle" type, or list of available item IDs for "item_select" types (nullable)
     * @param tooltip         Optional hover tooltip text (nullable)
     */
    public static void registerOption(String modId, String modDisplayName, String category, String key,
                                      String uniqueKey, String type, String defaultValue,
                                      List<String> choices, String tooltip) {
        MOD_DISPLAY_NAMES.put(modId, modDisplayName);
        ConfigOption opt = new ConfigOption(type, defaultValue, defaultValue, choices, tooltip, null);
        String cat = category != null ? category : "";

        REGISTRY.computeIfAbsent(modId, k -> new LinkedHashMap<>())
                .computeIfAbsent(cat, k -> new LinkedHashMap<>())
                .put(key, opt);

        if (OPTION_MAP.containsKey(uniqueKey)) {
            System.err.println("[DLib] Duplicate uniqueKey: " + uniqueKey + " (overwriting)");
        }
        OPTION_MAP.put(uniqueKey, opt);
    }

    public static void registerOption(String modId, String modDisplayName, String category, String key,
                                      String uniqueKey, String type, String defaultValue,
                                      List<String> choices) {
        registerOption(modId, modDisplayName, category, key, uniqueKey, type, defaultValue, choices, null);
    }

    /**
     * Register an option at the top level (outside any category).
     * @see #registerOption(String, String, String, String, String, String, String, List, String)
     */
    public static void registerOption(String modId, String modDisplayName, String key,
                                      String uniqueKey, String type, String defaultValue,
                                      List<String> choices, String tooltip) {
        registerOption(modId, modDisplayName, null, key, uniqueKey, type, defaultValue, choices, tooltip);
    }

    /**
     * Register an option at the top level without a tooltip.
     */
    public static void registerOption(String modId, String modDisplayName, String key,
                                      String uniqueKey, String type, String defaultValue,
                                      List<String> choices) {
        registerOption(modId, modDisplayName, null, key, uniqueKey, type, defaultValue, choices, null);
    }

    /**
     * Register an "action" option — a button that executes a callback when clicked.
     * <p>
     * Action options are not persisted to disk (no value to save).
     * The button label is taken from {@code key}.
     *
     * @param modId           Internal mod identifier
     * @param modDisplayName  Human‑readable name shown in the GUI
     * @param category        Category within the mod
     * @param key             Button label (also used as the option key)
     * @param uniqueKey       Globally unique key for this option
     * @param buttonLabel     Text shown on the button in the GUI
     * @param callback        The Runnable to execute when the button is clicked
     * @param tooltip         Optional hover tooltip text (nullable)
     */
    public static void registerAction(String modId, String modDisplayName, String category, String key,
                                      String uniqueKey, String buttonLabel, Runnable callback, String tooltip) {
        MOD_DISPLAY_NAMES.put(modId, modDisplayName);
        ConfigOption opt = new ConfigOption("action", buttonLabel, buttonLabel, null, tooltip, callback);
        String cat = category != null ? category : "";

        REGISTRY.computeIfAbsent(modId, k -> new LinkedHashMap<>())
                .computeIfAbsent(cat, k -> new LinkedHashMap<>())
                .put(key, opt);

        if (OPTION_MAP.containsKey(uniqueKey)) {
            System.err.println("[DLib] Duplicate uniqueKey: " + uniqueKey + " (overwriting)");
        }
        OPTION_MAP.put(uniqueKey, opt);
    }

    /**
     * Register an "action" option without a tooltip.
     * @see #registerAction(String, String, String, String, String, String, Runnable, String)
     */
    public static void registerAction(String modId, String modDisplayName, String category, String key,
                                      String uniqueKey, String buttonLabel, Runnable callback) {
        registerAction(modId, modDisplayName, category, key, uniqueKey, buttonLabel, callback, null);
    }

    /**
     * Register an action at the top level (outside any category).
     * @see #registerAction(String, String, String, String, String, String, Runnable, String)
     */
    public static void registerAction(String modId, String modDisplayName, String key,
                                      String uniqueKey, String buttonLabel, Runnable callback, String tooltip) {
        registerAction(modId, modDisplayName, null, key, uniqueKey, buttonLabel, callback, tooltip);
    }

    /**
     * Register an action at the top level without a tooltip.
     */
    public static void registerAction(String modId, String modDisplayName, String key,
                                      String uniqueKey, String buttonLabel, Runnable callback) {
        registerAction(modId, modDisplayName, null, key, uniqueKey, buttonLabel, callback, null);
    }

    // ── Headings ─────────────────────────────────────────────────────────────

    /**
     * Register a heading / section label inside a category.
     * <p>
     * Headings are read‑only text labels that appear between options to visually
     * group related settings. They are not persisted to disk.
     *
     * @param modId           Internal mod identifier
     * @param modDisplayName  Human‑readable name shown in the GUI
     * @param category        Category within the mod (null for top‑level)
     * @param headingText     Text to display as the heading
     */
    public static void registerHeading(String modId, String modDisplayName, String category, String headingText) {
        MOD_DISPLAY_NAMES.put(modId, modDisplayName);
        ConfigOption opt = new ConfigOption("heading", headingText, headingText, null, null, null);
        String key = "__hdg_" + headingText.hashCode() + "_" + System.nanoTime();
        REGISTRY.computeIfAbsent(modId, k -> new LinkedHashMap<>())
                .computeIfAbsent(category != null ? category : "", k -> new LinkedHashMap<>())
                .put(key, opt);
    }

    /**
     * Register a heading at the top level (outside any category).
     * @see #registerHeading(String, String, String, String)
     */
    public static void registerHeading(String modId, String modDisplayName, String headingText) {
        registerHeading(modId, modDisplayName, null, headingText);
    }

    // ── Separators ───────────────────────────────────────────────────────────

    /**
     * Register a visual separator line inside a category.
     * <p>
     * Separators are horizontal yellow lines that visually divide settings
     * into logical sections. They are not persisted to disk.
     *
     * @param modId           Internal mod identifier
     * @param modDisplayName  Human‑readable name shown in the GUI
     * @param category        Category within the mod (null for top‑level)
     */
    public static void registerSeparator(String modId, String modDisplayName, String category) {
        MOD_DISPLAY_NAMES.put(modId, modDisplayName);
        ConfigOption opt = new ConfigOption("separator", "", "", null, null, null);
        String key = "__sep_" + System.nanoTime();
        REGISTRY.computeIfAbsent(modId, k -> new LinkedHashMap<>())
                .computeIfAbsent(category != null ? category : "", k -> new LinkedHashMap<>())
                .put(key, opt);
    }

    /**
     * Register a separator at the top level (outside any category).
     * @see #registerSeparator(String, String, String)
     */
    public static void registerSeparator(String modId, String modDisplayName) {
        registerSeparator(modId, modDisplayName, null);
    }

    /**
     * Retrieve a ConfigOption by its unique key.
     */
    public static ConfigOption getOption(String uniqueKey) {
        return OPTION_MAP.get(uniqueKey);
    }

    /**
     * Programmatically set the value of a registered option by its unique key.
     * Has no effect if the key is not found or the option is a non‑persisted type
     * (heading / separator / action).
     *
     * @param uniqueKey  The globally unique key used during registration.
     * @param value      The new value as a string (for toggles use "true" / "false").
     */
    public static void setOption(String uniqueKey, String value) {
        ConfigOption opt = OPTION_MAP.get(uniqueKey);
        if (opt == null) {
            System.err.println("[DLib] setOption: unknown key '" + uniqueKey + "'");
            return;
        }
        if (opt.type.equals("heading") || opt.type.equals("separator") || opt.type.equals("action")) {
            System.err.println("[DLib] setOption: cannot set value of '" + opt.type + "' type (key='" + uniqueKey + "')");
            return;
        }
        opt.value = value;
    }

    /**
     * Convenience overload for boolean (toggle) options.
     * @param uniqueKey  The globally unique key of a toggle option.
     * @param value      The new boolean state.
     */
    public static void setOption(String uniqueKey, boolean value) {
        setOption(uniqueKey, String.valueOf(value));
    }

    /**
     * Get the display name for a mod (falls back to modId if not set).
     */
    public static String getModDisplayName(String modId) {
        return MOD_DISPLAY_NAMES.getOrDefault(modId, modId);
    }

    // --- Loading & Saving (unchanged) ---

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
                            if (option.type.equals("action") || option.type.equals("heading") || option.type.equals("separator"))
                                continue; // not persisted
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
                    if (opt.type.equals("action") || opt.type.equals("heading") || opt.type.equals("separator"))
                        continue; // not persisted
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
        public final String tooltip;   // hover tooltip text
        public final Runnable action;  // callback for "action" type (nullable)

        public ConfigOption(String type, String value, String defaultValue, List<String> choices, String tooltip, Runnable action) {
            this.type = type;
            this.value = value;
            this.defaultValue = defaultValue;
            this.choices = choices;
            this.tooltip = tooltip;
            this.action = action;
        }
    }
}