package com.drypted.dlib.client;

import net.fabricmc.api.ClientModInitializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drypted.dlib.client.config.ConfigManager;

public class DLibClient implements ClientModInitializer {
    public static final String MOD_ID = "dlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // String mod1 = "CombatEssentials";
        // ConfigManager.registerOption(mod1, "Aimbot Settings", "Trigger Bot", "toggle", "false", null);
        // ConfigManager.registerOption(mod1, "Aimbot Settings", "Target Mode", "cycle", "Closest", List.of("Closest", "Lowest HP", "Crosshair"));
        // ConfigManager.registerOption(mod1, "Aimbot Settings", "Maximum Range FOV", "number", "90.0", null);
        
        // ConfigManager.registerOption(mod1, "Render Options", "Draw FOV Circle", "toggle", "true", null);
        // ConfigManager.registerOption(mod1, "Render Options", "Target Hex Color", "text", "#FF5555", null);

        // // --- MOCK MOD 2: RENDERER PLUS ---
        // String mod2 = "RendererPlus";
        // ConfigManager.registerOption(mod2, "Performance Tweaks", "Optimize Entities", "toggle", "true", null);
        // ConfigManager.registerOption(mod2, "Performance Tweaks", "Chunk Render Distance Limit", "number", "16", null);
        
        // ConfigManager.registerOption(mod2, "Cosmetics Layout", "Custom Cape Styling", "cycle", "Minecon 2011", List.of("None", "Minecon 2011", "Optifine Style", "Neon Cloud"));
        // ConfigManager.registerOption(mod2, "Cosmetics Layout", "Rainbow Speed Multiplier", "number", "1.0", null);

        // // --- MOCK MOD 3: AUTOMATION UTILS ---
        // String mod3 = "AutomationUtils";
        // ConfigManager.registerOption(mod3, "Auto-Miner Settings", "Active Mining Module", "toggle", "false", null);
        // ConfigManager.registerOption(mod3, "Auto-Miner Settings", "Target Block Material", "cycle", "Diamond Ore", List.of("Coal Ore", "Iron Ore", "Gold Ore", "Diamond Ore"));
        // ConfigManager.registerOption(mod3, "Inventory Filters", "Item Filter Words List", "text", "cobblestone, dirt, gravel", null);
        // ConfigManager.registerOption(mod3, "Inventory Filters", "Auto Drop Threshold Percentage", "number", "90", null);

        // Commit modifications to build the dynamic JSON architecture
        ConfigManager.load();
        LOGGER.info("DLib Initialized!.");
    }
}