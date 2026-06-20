package com.phantom.dlib.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLibClient implements ClientModInitializer {
    public static final String MOD_ID = "dlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("DLib Initialized!.");
    }
}