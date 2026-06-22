package com.phantom.dlib.client.api.entry;

public class EntryToggle extends ConfigEntry {
    private boolean value;
    private final boolean defaultValue;

    public EntryToggle(String id, String label, boolean defaultValue) {
        super(id, label);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }
    public boolean getDefaultValue() { return defaultValue; }
}