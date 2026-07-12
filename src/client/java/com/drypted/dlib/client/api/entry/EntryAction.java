package com.drypted.dlib.client.api.entry;

/**
 * An action entry that renders as a button in the config GUI.
 * When clicked, the provided {@link Runnable} callback is executed.
 * <p>
 * Action entries are not persisted — they have no value to save/load.
 */
public class EntryAction extends ConfigEntry {
    private final Runnable callback;

    /**
     * @param id       Unique identifier for this entry
     * @param label    Text shown on the button
     * @param callback The Runnable to execute when the button is clicked
     */
    public EntryAction(String id, String label, Runnable callback) {
        super(id, label);
        this.callback = callback;
    }

    public Runnable getCallback() {
        return callback;
    }
}
