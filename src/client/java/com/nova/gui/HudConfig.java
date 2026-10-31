package com.nova.gui;

import com.nova.NovaClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persisted HUD widget visibility, edited directly from the click GUI. */
public final class HudConfig {
    private static final String[] KEYS = {
            "watermark", "moduleList", "coordinates", "statusBar", "storageEsp", "amethystFinder"
    };
    private static final Properties VALUES = new Properties();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nova-hud.properties");
    private static boolean loaded;

    private HudConfig() {}

    public static boolean get(String key) {
        load();
        return Boolean.parseBoolean(VALUES.getProperty(key, "true"));
    }

    public static void toggle(String key) {
        set(key, !get(key));
    }

    public static void set(String key, boolean value) {
        load();
        if (!isKnown(key)) return;
        VALUES.setProperty(key, Boolean.toString(value));
        String moduleName = moduleFor(key);
        if (moduleName != null && NovaClient.modules() != null) {
            NovaClient.modules().all().stream().filter(module -> module.name.equals(moduleName))
                    .findFirst().ifPresent(module -> module.setEnabled(value));
        }
        save();
    }

    public static void load() {
        if (loaded) return;
        loaded = true;
        if (Files.isRegularFile(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                VALUES.load(in);
            } catch (IOException ignored) {
                // Keep defaults if the user's config cannot be read.
            }
        }
        for (String key : KEYS) VALUES.putIfAbsent(key, defaultValue(key));
        if (NovaClient.modules() != null) {
            syncModule("storageEsp", "Storage ESP");
            syncModule("amethystFinder", "Amethyst Finder");
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                VALUES.store(out, "Nova HUD widget visibility");
            }
        } catch (IOException ignored) {
            // The HUD remains usable for this session if saving is unavailable.
        }
    }

    private static boolean isKnown(String key) {
        for (String candidate : KEYS) if (candidate.equals(key)) return true;
        return false;
    }

    private static String defaultValue(String key) {
        return key.equals("storageEsp") || key.equals("amethystFinder") ? "false" : "true";
    }

    private static String moduleFor(String key) {
        return switch (key) {
            case "storageEsp" -> "Storage ESP";
            case "amethystFinder" -> "Amethyst Finder";
            default -> null;
        };
    }

    private static void syncModule(String key, String name) {
        boolean enabled = Boolean.parseBoolean(VALUES.getProperty(key));
        NovaClient.modules().all().stream().filter(module -> module.name.equals(name))
                .findFirst().ifPresent(module -> module.setEnabled(enabled));
    }
}
