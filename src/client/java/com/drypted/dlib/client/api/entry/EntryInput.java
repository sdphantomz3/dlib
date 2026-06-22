package com.drypted.dlib.client.api.entry;

public class EntryInput extends ConfigEntry {
    private String value;
    private final String defaultValue;
    private final boolean editable; // If false, the input will be rendered as disabled

    public EntryInput(String id, String label, String defaultValue, boolean editable) {
        super(id, label);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.editable = editable;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public boolean isEditable() { return editable; }
    public String getDefaultValue() { return defaultValue; }
}