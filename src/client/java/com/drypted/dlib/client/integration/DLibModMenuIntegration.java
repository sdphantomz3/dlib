package com.drypted.dlib.client.integration;

import com.drypted.dlib.client.gui.DLibMainScreen;
import net.minecraft.client.gui.screens.Screen;

public final class DLibModMenuIntegration {
    private DLibModMenuIntegration() {}

    /**
     * Returns a config screen for the given mod ID, to be used as
     * ModMenu's ConfigScreenFactory.
     */
    public static Screen createConfigScreen(Screen parent, String modId) {
        return DLibMainScreen.openConfigScreen(parent, modId);
    }
}