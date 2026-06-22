package com.drypted.dlib.client.api.entry;

import java.util.List;

public class EntryCycle extends ConfigEntry {
    private final List<String> cycles;
    private int currentIndex;

    public EntryCycle(String id, String label, List<String> cycles, int defaultIndex) {
        super(id, label);
        this.cycles = cycles;
        this.currentIndex = defaultIndex;
    }

    public String getCurrentValue() {
        return cycles.get(currentIndex);
    }

    public void cycleForward() {
        this.currentIndex = (this.currentIndex + 1) % cycles.size();
    }

    public int getCurrentIndex() { return currentIndex; }
    public List<String> getCycles() { return cycles; }
}