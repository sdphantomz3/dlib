package com.drypted.dlib.client.api;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private final String modName;
    private final List<ConfigCategory> categories = new ArrayList<>();

    public ModConfig(String modName) {
        this.modName = modName;
    }

    public String getModName() { return modName; }
    public List<ConfigCategory> getCategories() { return categories; }

    public ConfigCategory createCategory(String categoryTitle) {
        ConfigCategory category = new ConfigCategory(categoryTitle);
        this.categories.add(category);
        return category;
    }
}