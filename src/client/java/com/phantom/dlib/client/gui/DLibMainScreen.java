package com.phantom.dlib.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DLibMainScreen extends Screen {
    private final Screen parent;

    public DLibMainScreen(Screen parent) {
        super(Component.literal("Mod Configurations"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            // Returns to the pause screen when you press Escape
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}