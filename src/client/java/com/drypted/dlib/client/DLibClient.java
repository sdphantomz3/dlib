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
        
        
        String mod1 = "CombatEssentials";
        ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Trigger Bot",
                "combatessentials+aimbot+triggerbot", "toggle", "false", null, null);
        ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Target Mode",
                "combatessentials+aimbot+targetmode", "cycle", "Closest",
                List.of("Closest", "Lowest HP", "Crosshair"), null);
        ConfigManager.registerOption(mod1, "Combat Essentials", "Aimbot Settings", "Maximum Range FOV",
                "combatessentials+aimbot+maxfov", "number", "90.0", null, null);
        ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Draw FOV Circle",
                "combatessentials+render+drawfov", "toggle", "true", null,
                "Shows the field‑of‑view circle on the HUD.");
        ConfigManager.registerOption(mod1, "Combat Essentials", "Render Options", "Target Hex Color",
                "combatessentials+render+targetcolor", "text", "#FF5555", null,
                "Hex color code for the target highlight.");
                
        // Commit modifications to build the dynamic JSON architecture
        ConfigManager.load();
        LOGGER.info("DLib Initialized!.");
    }
}