package com.phantom.dlib.client.api.entry;

import net.minecraft.network.chat.Component;

public abstract class ConfigEntry {
    private final String id;
    private final Component label;

    public ConfigEntry(String id, String label) {
        this.id = id;
        this.label = Component.literal(label);
    }

    public String getId() { return id; }
    public Component getLabel() { return label; }
}