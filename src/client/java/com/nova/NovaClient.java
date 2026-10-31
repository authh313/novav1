package com.nova;

import com.mojang.blaze3d.platform.InputConstants;
import com.nova.gui.ClickGuiScreen;
import com.nova.gui.HudConfig;
import com.nova.gui.HudOverlay;
import com.nova.gui.WorldScanner;
import com.nova.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class NovaClient implements ClientModInitializer {
    public static final String MOD_ID = "nova";

    private static ModuleManager modules;

    public static ModuleManager modules() {
        return modules;
    }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        HudConfig.load();

        // Key bind: Right Shift opens the GUI (rebindable in Controls).
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "nova"));

        KeyMapping openGui = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.nova.open_gui",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RSHIFT,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            if (client.player != null) {
                modules.tick();
                WorldScanner.tick(client);
            }
        });

        // HUD: watermark, module list, status bar.
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                new HudOverlay());
    }
}
