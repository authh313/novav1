package com.nova.module;

public class Module {
    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        PLAYER("Player");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    public final String name;
    public final Category category;
    public final String mode; // shown greyed next to the name in the module list, "" for none
    private boolean enabled;

    public Module(String name, Category category, String mode, boolean enabledByDefault) {
        this.name = name;
        this.category = category;
        this.mode = mode;
        this.enabled = enabledByDefault;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (this.enabled == value) return;
        this.enabled = value;
        if (value) onEnable(); else onDisable();
    }

    // Override these (or subclass Module) to hook behaviour in.
    protected void onEnable() {}
    protected void onDisable() {}

    // Called every client tick while enabled.
    public void onTick() {}
}
