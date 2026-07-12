package com.drypted.dlib.client;

import com.drypted.dlib.client.config.ConfigManager;

import net.fabricmc.api.ClientModInitializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLibClient implements ClientModInitializer {
    public static final String MOD_ID = "dlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        
        
//         String mod1 = "CombatEssentials";
//         ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Trigger Bot",
//                 "combatessentials+aimbot+triggerbot", "toggle", "false", null, null);
//         ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Target Mode",
//                 "combatessentials+aimbot+targetmode", "cycle", "Closest",
//                 List.of("Closest", "Lowest HP", "Crosshair"), null);
//         ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Maximum Range FOV",
//                 "combatessentials+aimbot+maxfov", "number", "90.0", null, null);
//         ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Draw FOV Circle",
//                 "combatessentials+render+drawfov", "toggle", "true", null,
//                 "Shows the field‑of‑view circle on the HUD.");
//         ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Target Hex Color",
//                 "combatessentials+render+targetcolor", "text", "#FF5555", null,
//                 "Hex color code for the target highlight.");
//                 ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Silent Aim",
//         "combatessentials+aimbot+silentaim", "toggle", "false", null,
//         "Adjusts aim server-side without visibly moving the crosshair.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Aim Smoothness",
//         "combatessentials+aimbot+smoothness", "number", "6.5", null, null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Aim Priority",
//         "combatessentials+aimbot+priority", "cycle", "Distance",
//         List.of("Distance", "Health", "Angle", "Armor"), null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Ignore Invisible",
//         "combatessentials+aimbot+ignoreinvisible", "toggle", "true", null, null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Ignore Friends",
//         "combatessentials+aimbot+ignorefriends", "toggle", "true", null,
//         "Skips players marked as friends.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Require Line of Sight",
//         "combatessentials+aimbot+lineofsight", "toggle", "true", null, null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Target Lock",
//         "combatessentials+aimbot+targetlock", "toggle", "false", null,
//         "Keeps aiming at the same target until it is lost.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Lock Delay",
//         "combatessentials+aimbot+lockdelay", "number", "150", null,
//         "Delay in milliseconds before locking onto a target.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Show Target Name",
//         "combatessentials+render+targetname", "toggle", "true", null, null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "ESP Style",
//         "combatessentials+render+espstyle", "cycle", "Outline",
//         List.of("Outline", "Box", "Glow", "Filled"), null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "ESP Opacity",
//         "combatessentials+render+espopacity", "number", "0.75", null, null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Show Distance",
//         "combatessentials+render+distance", "toggle", "true", null,
//         "Displays the distance to the current target.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Friendly Color",
//         "combatessentials+render+friendlycolor", "text", "#55FF55", null,
//         "Hex color used for friendly player highlights.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Background Opacity",
//         "combatessentials+render+backgroundopacity", "number", "0.35", null,
//         "Opacity of rendered overlays.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Misc Settings", "Auto Disable",
//         "combatessentials+misc+autodisable", "toggle", "false", null,
//         "Automatically disables the module under certain conditions.");

// ConfigManager.registerOption(mod1, "Combat Essentials", "Misc Settings", "Debug Logging",
//         "combatessentials+misc+debug", "toggle", "false", null, null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Misc Settings", "Preset",
//         "combatessentials+misc+preset", "cycle", "Balanced",
//         List.of("Legit", "Balanced", "Aggressive", "Custom"), null);

// ConfigManager.registerOption(mod1, "Combat Essentials", "Misc Settings", "Configuration Name",
//         "combatessentials+misc+configname", "text", "Default", null,
//         "Name of the active configuration profile.");

// Single item select
ConfigManager.registerOption(
    "mymod", "My Mod", "Items", "Favorite Item",
    "mymod+items+favorite", "item_select",
    "minecraft:diamond",  // default value
    List.of("minecraft:diamond", "minecraft:iron_ingot", "minecraft:gold_ingot"),
    "Pick your favorite item"
);

// Multi item select
ConfigManager.registerOption(
    "mymod", "My Mod", "Items", "Allowed Blocks",
    "mymod+items+blocks", "item_select_multi",
    "minecraft:dirt,minecraft:stone",  // default (comma-separated)
    List.of("minecraft:dirt", "minecraft:stone", "minecraft:grass_block", "minecraft:sand"),
    "Select allowed blocks"
);
ConfigManager.registerOption(
    "mymod", "Test 2", "Items", "Allowed Blocks",
    "mymod+items+blocksa", "item_select_multi",
    "minecraft:dirt,minecraft:stone",null,
    "Select allowed blocks"
);
        // Commit modifications to build the dynamic JSON architecture
        ConfigManager.load();
        LOGGER.info("DLib Initialized!.");
    }
}