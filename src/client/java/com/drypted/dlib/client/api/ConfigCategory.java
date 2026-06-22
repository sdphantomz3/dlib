package com.drypted.dlib.client.api;

import com.drypted.dlib.client.api.entry.ConfigEntry;
import java.util.ArrayList;
import java.util.List;

public class ConfigCategory {
    private final String title;
    private final List<ConfigEntry> entries = new ArrayList<>();

    public ConfigCategory(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public List<ConfigEntry> getEntries() { return entries; }

    public void addEntry(ConfigEntry entry) {
        this.entries.add(entry);
    }
}