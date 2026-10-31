package com.nova.module;

import com.nova.module.Module.Category;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Combat
        add(new Module("KillAura", Category.COMBAT, "Single", true));
        add(new Module("Reach", Category.COMBAT, "", false));
        add(new Module("Criticals", Category.COMBAT, "", false));
        add(new Module("Velocity", Category.COMBAT, "", false));
        add(new Module("AutoClicker", Category.COMBAT, "", false));

        // Movement
        add(new Module("Fly", Category.MOVEMENT, "", false));
        add(new Module("Sprint", Category.MOVEMENT, "Legit", true));
        add(new Module("Speed", Category.MOVEMENT, "", false));
        add(new Module("NoFall", Category.MOVEMENT, "", false));
        add(new Module("Step", Category.MOVEMENT, "", false));

        // Render
        add(new Module("ESP", Category.RENDER, "Box", true));
        add(new Module("Tracers", Category.RENDER, "", false));
        add(new Module("Fullbright", Category.RENDER, "", true));
        add(new Module("Nametags", Category.RENDER, "", false));
        add(new Module("Chams", Category.RENDER, "", false));
        add(new Module("Storage ESP", Category.RENDER, "Loaded", false));
        add(new Module("Amethyst Finder", Category.RENDER, "Loaded chunks", false));

        // Player
        add(new Module("AutoTool", Category.PLAYER, "", true));
        add(new Module("Scaffold", Category.PLAYER, "", false));
        add(new Module("ChestStealer", Category.PLAYER, "", false));
        add(new Module("FastPlace", Category.PLAYER, "", false));
        add(new Module("InvCleaner", Category.PLAYER, "", false));
    }

    private void add(Module module) {
        modules.add(module);
    }

    public List<Module> all() {
        return modules;
    }

    public List<Module> inCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.category == category) result.add(m);
        }
        return result;
    }

    public List<Module> enabled() {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.isEnabled()) result.add(m);
        }
        return result;
    }

    public void tick() {
        for (Module m : modules) {
            if (m.isEnabled()) m.onTick();
        }
    }
}
