package com.drypted.dlib.client;

import com.drypted.dlib.client.config.ConfigManager;

import net.fabricmc.api.ClientModInitializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLibClient implements ClientModInitializer {
    public static final String MOD_ID = "dlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Set to {@code true} to register demo/test config entries that showcase
     * every feature of the DLib Config API.  Set to {@code false} in production.
     */
    private static final boolean TEST_FEATURES = true;

    @Override
    public void onInitializeClient() {
        if (TEST_FEATURES) {
            registerTestFeatures();
        }

        ConfigManager.load();
        LOGGER.info("DLib Initialized!.");
    }

    // ── Test / demo registrations ────────────────────────────────────────────

    private static void registerTestFeatures() {
        String mod = "dlib_demo";

        // ═══════════════════════════════════════════════════════════════════
        //  TOP‑LEVEL ITEMS  (outside any category — rendered first)
        // ═══════════════════════════════════════════════════════════════════

        ConfigManager.registerHeading(mod, "DLib Demo", "Quick Toggles");

        ConfigManager.registerOption(mod, "DLib Demo", "Demo Enabled",
                "dlib+demo+enabled", "toggle", "true", null,
                "Master switch — turn off to hide all demo entries.");

        ConfigManager.registerSeparator(mod, "DLib Demo");

        // ═══════════════════════════════════════════════════════════════════
        //  CATEGORY:  General  (toggle, cycle, text, number)
        // ═══════════════════════════════════════════════════════════════════

        ConfigManager.registerOption(mod, "DLib Demo", "General", "Feature Toggle",
                "dlib+demo+general+toggle", "toggle", "true", null,
                "Toggles a boolean feature on or off.");

        ConfigManager.registerOption(mod, "DLib Demo", "General", "Game Mode",
                "dlib+demo+general+mode", "cycle", "Survival",
                List.of("Survival", "Creative", "Hardcore", "Spectator"),
                "Cycle through available game modes.");

        ConfigManager.registerHeading(mod, "DLib Demo", "General", "─ Advanced Settings ─");

        ConfigManager.registerOption(mod, "DLib Demo", "General", "Max Distance",
                "dlib+demo+general+distance", "number", "64.0", null,
                "Maximum distance in blocks (supports decimals).");

        ConfigManager.registerOption(mod, "DLib Demo", "General", "Player Name",
                "dlib+demo+general+name", "text", "Steve", null,
                "Custom player display name.");

        ConfigManager.registerSeparator(mod, "DLib Demo", "General");

        ConfigManager.registerOption(mod, "DLib Demo", "General", "Render Quality",
                "dlib+demo+general+quality", "cycle", "High",
                List.of("Low", "Medium", "High", "Ultra"), null);

        // ═══════════════════════════════════════════════════════════════════
        //  CATEGORY:  Items  (item_select + item_select_multi)
        // ═══════════════════════════════════════════════════════════════════

        ConfigManager.registerHeading(mod, "DLib Demo", "Items", "─ Single Pickers ─");

        // Single item — restricted list
        ConfigManager.registerOption(mod, "DLib Demo", "Items", "Favorite Block",
                "dlib+demo+items+favblock", "item_select",
                "minecraft:diamond_block",
                List.of("minecraft:diamond_block", "minecraft:iron_block", "minecraft:gold_block",
                        "minecraft:emerald_block", "minecraft:netherite_block"),
                "Pick your favorite block from the list.");

        // Single item — ALL Minecraft items (null choices)
        ConfigManager.registerOption(mod, "DLib Demo", "Items", "Any Item",
                "dlib+demo+items+any", "item_select",
                "minecraft:stone", null,
                "Choose any item or block from the entire game.");

        ConfigManager.registerSeparator(mod, "DLib Demo", "Items");

        ConfigManager.registerHeading(mod, "DLib Demo", "Items", "─ Multi Pickers ─");

        // Multi item — restricted list
        ConfigManager.registerOption(mod, "DLib Demo", "Items", "Allowed Ores",
                "dlib+demo+items+ores", "item_select_multi",
                "minecraft:iron_ore,minecraft:gold_ore,minecraft:diamond_ore",
                List.of("minecraft:iron_ore", "minecraft:gold_ore", "minecraft:diamond_ore",
                        "minecraft:emerald_ore", "minecraft:netherite_scrap",
                        "minecraft:coal_ore", "minecraft:copper_ore"),
                "Select which ores to highlight.");

        // Multi item — ALL Minecraft items (null choices)
        ConfigManager.registerOption(mod, "DLib Demo", "Items", "Block Whitelist",
                "dlib+demo+items+whitelist", "item_select_multi",
                "minecraft:stone,minecraft:dirt,minecraft:grass_block",
                null,
                "Blocks that are allowed (all Minecraft items shown).");

        // ═══════════════════════════════════════════════════════════════════
        //  CATEGORY:  Actions  (action buttons)
        // ═══════════════════════════════════════════════════════════════════

        ConfigManager.registerAction(mod, "DLib Demo", "Actions", "Print Config",
                "dlib+demo+actions+print", "Print",
                () -> {
                    LOGGER.info("=== DLib Demo Config Values ===");
                    logOption("dlib+demo+enabled");
                    logOption("dlib+demo+general+toggle");
                    logOption("dlib+demo+general+mode");
                    logOption("dlib+demo+general+distance");
                    logOption("dlib+demo+general+name");
                    logOption("dlib+demo+general+quality");
                    logOption("dlib+demo+items+favblock");
                    logOption("dlib+demo+items+any");
                    logOption("dlib+demo+items+ores");
                    logOption("dlib+demo+items+whitelist");
                    LOGGER.info("===============================");
                },
                "Prints all demo config values to the game log.");

        ConfigManager.registerSeparator(mod, "DLib Demo", "Actions");

        ConfigManager.registerAction(mod, "DLib Demo", "Actions", "Reset All Demos",
                "dlib+demo+actions+reset", "Reset",
                () -> {
                    ConfigManager.setOption("dlib+demo+enabled", "true");
                    ConfigManager.setOption("dlib+demo+general+toggle", "true");
                    ConfigManager.setOption("dlib+demo+general+mode", "Survival");
                    ConfigManager.setOption("dlib+demo+general+distance", "64.0");
                    ConfigManager.setOption("dlib+demo+general+name", "Steve");
                    ConfigManager.setOption("dlib+demo+general+quality", "High");
                    ConfigManager.setOption("dlib+demo+items+favblock", "minecraft:diamond_block");
                    ConfigManager.setOption("dlib+demo+items+any", "minecraft:stone");
                    ConfigManager.setOption("dlib+demo+items+ores",
                            "minecraft:iron_ore,minecraft:gold_ore,minecraft:diamond_ore");
                    ConfigManager.setOption("dlib+demo+items+whitelist",
                            "minecraft:stone,minecraft:dirt,minecraft:grass_block");
                    ConfigManager.saveMod(mod);
                    LOGGER.info("All demo options reset to defaults and saved.");
                },
                "Resets every demo option to its default value and saves.");

        ConfigManager.registerAction(mod, "DLib Demo", "Actions", "Toggle Demo",
                "dlib+demo+actions+toggle", "Toggle",
                () -> {
                    ConfigManager.ConfigOption opt = ConfigManager.getOption("dlib+demo+enabled");
                    boolean current = opt != null && Boolean.parseBoolean(opt.value);
                    ConfigManager.setOption("dlib+demo+enabled", !current);
                    ConfigManager.saveMod(mod);
                    LOGGER.info("Demo enabled toggled to: {}", !current);
                },
                "Programmatically toggles the Demo Enabled option via setOption().");
    }

    private static void logOption(String uniqueKey) {
        ConfigManager.ConfigOption opt = ConfigManager.getOption(uniqueKey);
        if (opt != null) {
            LOGGER.info("  {} = {}", uniqueKey, opt.value);
        }
    }
}