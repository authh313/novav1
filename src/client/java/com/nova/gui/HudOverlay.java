package com.nova.gui;

import com.nova.NovaClient;
import com.nova.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;
import java.util.List;

/** Clean, independently configurable HUD widgets. */
public final class HudOverlay implements HudElement {
    private static final int PANEL = 0xE90E131C;
    private static final int EDGE = 0xFF293242;
    private static final int ACCENT = 0xFF9D83FF;
    private static final int TEXT = 0xFFF1F3F8;
    private static final int SECONDARY = 0xFFC4CBD6;
    private static final int MUTED = 0xFF8994A4;
    private static final int SUCCESS = 0xFF77D6B2;
    private static final int PAD = 10;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        HudConfig.load();

        Font font = mc.font;
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();

        if (HudConfig.get("watermark")) drawWatermark(graphics, font);

        int moduleRows = HudConfig.get("moduleList") ? drawModuleList(graphics, font, screenW) : 0;
        int rightY = PAD + (moduleRows > 0 ? moduleRows * (font.lineHeight + 4) + 8 : 0);

        if (HudConfig.get("storageEsp") && isEnabled("Storage ESP")) {
            rightY = drawStoragePanel(graphics, font, screenW, rightY);
        }
        if (HudConfig.get("amethystFinder") && isEnabled("Amethyst Finder")) {
            drawAmethystPanel(graphics, font, screenW, rightY + 8);
        }

        int bottom = screenH - PAD;
        if (HudConfig.get("statusBar")) {
            bottom = drawStatusBar(graphics, font, mc, screenW, screenH) - 8;
        }
        if (HudConfig.get("coordinates")) {
            drawCoordinates(graphics, font, mc, PAD, bottom - 64);
        }
    }

    private void drawWatermark(GuiGraphics g, Font font) {
        String title = "NOVA";
        int w = font.width(title) + 36;
        int x = PAD;
        int y = PAD;
        card(g, x, y, w, 28);
        g.fill(x, y + 1, x + 3, y + 27, ACCENT);
        g.drawString(font, title, x + 12, y + 10, TEXT, true);
        g.fill(x + w - 19, y + 9, x + w - 11, y + 17, SUCCESS);
    }

    private int drawModuleList(GuiGraphics g, Font font, int screenW) {
        List<Module> enabled = NovaClient.modules().enabled();
        enabled.sort(Comparator.comparingInt((Module m) -> font.width(m.name)).reversed());
        int y = PAD;
        for (Module module : enabled) {
            int w = font.width(module.name) + 22;
            int x = screenW - PAD - w;
            card(g, x, y, w, font.lineHeight + 8);
            g.fill(x + w - 2, y + 2, x + w, y + font.lineHeight + 6, ACCENT);
            g.drawString(font, module.name, x + 9, y + 4, SECONDARY, false);
            y += font.lineHeight + 11;
        }
        return enabled.size();
    }

    private int drawStatusBar(GuiGraphics g, Font font, Minecraft mc, int screenW, int screenH) {
        int h = 28;
        int y = screenH - PAD - h;
        card(g, PAD, y, screenW - PAD * 2, h);
        String coords = String.format("%d  /  %d  /  %d", mc.player.blockPosition().getX(),
                mc.player.blockPosition().getY(), mc.player.blockPosition().getZ());
        String fps = "FPS  " + mc.getFps();
        String ping = "PING  " + pingOf(mc) + " ms";
        String modules = "MODULES  " + NovaClient.modules().enabled().size();
        g.drawString(font, "NOVA", PAD + 10, y + 10, ACCENT, false);
        g.drawString(font, coords, PAD + 60, y + 10, SECONDARY, false);
        int gap = 26;
        int right = screenW - PAD - font.width(ping) - font.width(fps) - font.width(modules) - gap * 2 - 10;
        g.drawString(font, fps, right, y + 10, MUTED, false);
        g.drawString(font, ping, right + font.width(fps) + gap, y + 10, MUTED, false);
        g.drawString(font, modules, right + font.width(fps) + font.width(ping) + gap * 2, y + 10, SECONDARY, false);
        return y;
    }

    private void drawCoordinates(GuiGraphics g, Font font, Minecraft mc, int x, int y) {
        y = Math.max(PAD + 32, y);
        int w = 222;
        card(g, x, y, w, 56);
        g.drawString(font, "POSITION", x + 10, y + 8, MUTED, false);
        String pos = mc.player.blockPosition().getX() + "  " + mc.player.blockPosition().getY()
                + "  " + mc.player.blockPosition().getZ();
        g.drawString(font, pos, x + 10, y + 22, TEXT, true);
        int chunkX = mc.player.blockPosition().getX() >> 4;
        int chunkZ = mc.player.blockPosition().getZ() >> 4;
        String detail = mc.level.dimension().location().getPath().toUpperCase() + "   ·   CHUNK " + chunkX + ", " + chunkZ;
        g.drawString(font, trim(font, detail, w - 20), x + 10, y + 40, MUTED, false);
    }

    private int drawStoragePanel(GuiGraphics g, Font font, int screenW, int y) {
        List<WorldScanner.StorageHit> hits = WorldScanner.storage();
        int w = 218;
        int rows = Math.min(5, hits.size());
        int h = 35 + Math.max(1, rows) * 17;
        int x = screenW - PAD - w;
        card(g, x, y, w, h);
        g.drawString(font, "STORAGE", x + 10, y + 8, TEXT, true);
        g.drawString(font, "LOADED AREA", x + w - font.width("LOADED AREA") - 10, y + 8, MUTED, false);
        if (hits.isEmpty()) {
            g.drawString(font, "No containers found nearby", x + 10, y + 25, MUTED, false);
        } else {
            for (int i = 0; i < rows; i++) {
                WorldScanner.StorageHit hit = hits.get(i);
                String row = hit.kind() + "   " + hit.pos().getX() + ", " + hit.pos().getY() + ", " + hit.pos().getZ();
                g.drawString(font, trim(font, row, w - 20), x + 10, y + 25 + i * 17, SECONDARY, false);
            }
        }
        return y + h;
    }

    private void drawAmethystPanel(GuiGraphics g, Font font, int screenW, int y) {
        List<WorldScanner.AmethystHit> hits = WorldScanner.amethyst();
        int w = 218;
        int rows = Math.min(4, hits.size());
        int h = 35 + Math.max(1, rows) * 17;
        int x = screenW - PAD - w;
        card(g, x, y, w, h);
        g.drawString(font, "AMETHYST CHUNKS", x + 10, y + 8, TEXT, true);
        if (hits.isEmpty()) {
            g.drawString(font, "No loaded hits nearby", x + 10, y + 25, MUTED, false);
        } else {
            for (int i = 0; i < rows; i++) {
                WorldScanner.AmethystHit hit = hits.get(i);
                String row = "CHUNK  " + hit.chunkX() + ", " + hit.chunkZ() + "   ·   " + Math.round(hit.distance()) + "m";
                g.drawString(font, row, x + 10, y + 25 + i * 17, SECONDARY, false);
            }
        }
    }

    private static boolean isEnabled(String name) {
        return NovaClient.modules().all().stream().anyMatch(m -> m.name.equals(name) && m.isEnabled());
    }

    private static int pingOf(Minecraft mc) {
        if (mc.getConnection() == null || mc.player == null) return 0;
        var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    private static void card(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, EDGE);
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, ACCENT);
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "…";
        int end = text.length();
        while (end > 0 && font.width(text.substring(0, end) + ellipsis) > maxWidth) end--;
        return text.substring(0, end) + ellipsis;
    }
}
