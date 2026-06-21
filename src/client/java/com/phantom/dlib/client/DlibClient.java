package com.phantom.dlib.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.phantom.dlib.client.config.ConfigManager;

public class DLibClient implements ClientModInitializer {
    public static final String MOD_ID = "dlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        LOGGER.info("DLib Initialized!.");
    }
}