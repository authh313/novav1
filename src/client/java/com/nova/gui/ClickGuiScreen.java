package com.nova.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.nova.NovaClient;
import com.nova.module.Module;
import com.nova.module.Module.Category;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** A responsive, data-driven control center for Nova modules and HUD widgets. */
public final class ClickGuiScreen extends Screen {
    private static final int SCRIM = 0xB8070A10;
    private static final int SHELL = 0xFF10141D;
    private static final int SIDEBAR = 0xFF0C1018;
    private static final int SURFACE = 0xFF171D28;
    private static final int SURFACE_HOVER = 0xFF202938;
    private static final int BORDER = 0xFF293242;
    private static final int ACCENT = 0xFF9D83FF;
    private static final int ACCENT_DIM = 0xFF29233F;
    private static final int TEXT = 0xFFF1F3F8;
    private static final int MUTED = 0xFF919BAA;
    private static final int GREEN = 0xFF77D6B2;
    private static final int PANEL_W = 1020;
    private static final int PANEL_H = 640;
    private static final int SIDEBAR_W = 176;

    private Category selectedCategory = Category.RENDER;
    private boolean hudPage;
    private final List<Hitbox> hitboxes = new ArrayList<>();

    public ClickGuiScreen() {
        super(Component.literal("Nova Control Center"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, SCRIM);
        graphics.fill(0, 0, width, height / 2, 0x120E1322);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        hitboxes.clear();

        int panelW = Math.min(PANEL_W, width - 28);
        int panelH = Math.min(PANEL_H, height - 28);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int sidebarW = Math.min(SIDEBAR_W, panelW / 4);

        graphics.fill(x - 2, y - 2, x + panelW + 2, y + panelH + 2, 0x70000000);
        graphics.fill(x, y, x + panelW, y + panelH, SHELL);
        graphics.fill(x, y, x + sidebarW, y + panelH, SIDEBAR);
        graphics.fill(x + sidebarW, y, x + sidebarW + 1, y + panelH, BORDER);
        graphics.fill(x, y, x + panelW, y + 2, ACCENT);

        drawBrand(graphics, x, y, sidebarW);
        drawNavigation(graphics, x, y, sidebarW, mouseX, mouseY);
        drawHeader(graphics, x + sidebarW + 28, y + 24, panelW - sidebarW - 56);

        if (hudPage) {
            drawHudPage(graphics, x + sidebarW + 28, y + 116, panelW - sidebarW - 56, panelH - 152, mouseX, mouseY);
        } else {
            drawModulePage(graphics, x + sidebarW + 28, y + 116, panelW - sidebarW - 56, panelH - 152, mouseX, mouseY);
        }

        graphics.fill(x + sidebarW + 28, y + panelH - 34, x + panelW - 28, y + panelH - 33, BORDER);
        graphics.drawString(font, "RIGHT SHIFT  ·  CLOSE", x + sidebarW + 28, y + panelH - 23, MUTED, false);
        graphics.drawString(font, "NOVA  /  CLIENT CONTROL CENTER", x + panelW - 230, y + panelH - 23, 0xFF737E8E, false);
    }

    private void drawBrand(GuiGraphics g, int x, int y, int sidebarW) {
        g.fill(x + 18, y + 24, x + 48, y + 54, ACCENT_DIM);
        g.fill(x + 18, y + 24, x + 21, y + 54, ACCENT);
        g.drawString(font, "N", x + 28, y + 32, TEXT, true);
        g.drawString(font, "NOVA", x + 58, y + 27, TEXT, true);
        g.drawString(font, "CLIENT", x + 58, y + 43, MUTED, false);
        g.fill(x + 16, y + 72, x + sidebarW - 16, y + 73, BORDER);
    }

    private void drawNavigation(GuiGraphics g, int x, int y, int sidebarW, int mouseX, int mouseY) {
        int navX = x + 12;
        int navW = sidebarW - 24;
        int top = y + 91;
        g.drawString(font, "WORKSPACE", navX + 9, top, 0xFF697485, false);
        int rowY = top + 18;
        drawNavRow(g, "HUD & Display", navX, rowY, navW, hudPage, mouseX, mouseY, () -> hudPage = true);
        rowY += 35;
        g.drawString(font, "MODULES", navX + 9, rowY, 0xFF697485, false);
        rowY += 18;
        for (Category category : Category.values()) {
            Category captured = category;
            drawNavRow(g, category.label, navX, rowY, navW, !hudPage && selectedCategory == category,
                    mouseX, mouseY, () -> { selectedCategory = captured; hudPage = false; });
            rowY += 35;
        }
    }

    private void drawNavRow(GuiGraphics g, String label, int x, int y, int w, boolean selected,
                            int mouseX, int mouseY, Runnable action) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 28);
        if (selected || hover) {
            g.fill(x, y, x + w, y + 28, selected ? ACCENT_DIM : SURFACE_HOVER);
        }
        if (selected) g.fill(x, y + 5, x + 2, y + 23, ACCENT);
        g.drawString(font, label, x + 11, y + 9, selected ? TEXT : (hover ? TEXT : MUTED), false);
        hitboxes.add(new Hitbox(x, y, w, 28, action));
    }

    private void drawHeader(GuiGraphics g, int x, int y, int contentW) {
        String title = hudPage ? "HUD & Display" : selectedCategory.label;
        String subtitle = hudPage
                ? "Choose what stays visible while you play."
                : "Live module controls · changes apply immediately";
        g.drawString(font, title, x, y, TEXT, true);
        g.drawString(font, subtitle, x, y + 19, MUTED, false);
        int on = NovaClient.modules().enabled().size();
        String count = String.format("%02d  ACTIVE", on);
        int badgeW = font.width(count) + 20;
        int bx = x + contentW - badgeW;
        g.fill(bx, y - 1, bx + badgeW, y + 21, SURFACE);
        g.fill(bx, y - 1, bx + 2, y + 21, GREEN);
        g.drawString(font, count, bx + 10, y + 6, TEXT, false);
    }

    private void drawModulePage(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        List<Module> modules = NovaClient.modules().inCategory(selectedCategory);
        int gap = 12;
        int cols = w > 600 ? 2 : 1;
        int cardW = (w - gap * (cols - 1)) / cols;
        int cardH = 86;
        int rowGap = 12;
        int idx = 0;
        for (Module module : modules) {
            int col = idx % cols;
            int row = idx / cols;
            int cx = x + col * (cardW + gap);
            int cy = y + row * (cardH + rowGap);
            boolean hovered = inside(mouseX, mouseY, cx, cy, cardW, cardH);
            g.fill(cx, cy, cx + cardW, cy + cardH, hovered ? SURFACE_HOVER : SURFACE);
            g.fill(cx, cy, cx + cardW, cy + 1, BORDER);
            if (module.isEnabled()) g.fill(cx, cy, cx + 3, cy + cardH, ACCENT);

            g.drawString(font, module.name, cx + 16, cy + 16, TEXT, true);
            g.drawString(font, module.category.label.toUpperCase(), cx + 16, cy + 34, MUTED, false);

            int toggleW = 88;
            int toggleH = 28;
            int tx = cx + cardW - toggleW - 14;
            int ty = cy + (cardH - toggleH) / 2;
            drawToggle(g, tx, ty, toggleW, toggleH, module.isEnabled(), mouseX, mouseY);
            g.drawString(font, module.isEnabled() ? "ENABLED" : "DISABLED", tx + 12, ty + 10,
                    module.isEnabled() ? 0xFFDACFFF : MUTED, false);
            hitboxes.add(new Hitbox(tx, ty, toggleW, toggleH, () -> toggleModule(module)));
            idx++;
        }
        if (modules.isEmpty()) {
            drawEmptyState(g, x, y, w, h, "No modules in this category", "New modules will appear here automatically.");
        }
    }

    private void drawHudPage(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        String[][] rows = {
                {"Watermark", "Client label in the top left", "watermark"},
                {"Active modules", "Right aligned list of enabled modules", "moduleList"},
                {"Coordinates", "Position, dimension and chunk", "coordinates"},
                {"Performance strip", "FPS, latency and module count", "statusBar"},
                {"Storage ESP panel", "Loaded chests and containers nearby", "storageEsp"},
                {"Amethyst chunk finder", "Loaded chunks containing amethyst", "amethystFinder"}
        };
        int gap = 12;
        int cols = w > 600 ? 2 : 1;
        int cardW = (w - gap * (cols - 1)) / cols;
        int cardH = 74;
        for (int i = 0; i < rows.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int cx = x + col * (cardW + gap);
            int cy = y + row * (cardH + gap);
            boolean enabled = HudConfig.get(rows[i][2]);
            boolean hover = inside(mouseX, mouseY, cx, cy, cardW, cardH);
            g.fill(cx, cy, cx + cardW, cy + cardH, hover ? SURFACE_HOVER : SURFACE);
            g.fill(cx, cy, cx + cardW, cy + 1, BORDER);
            g.drawString(font, rows[i][0], cx + 14, cy + 14, TEXT, true);
            g.drawString(font, rows[i][1], cx + 14, cy + 34, MUTED, false);
            int tw = 78;
            int th = 26;
            int tx = cx + cardW - tw - 13;
            int ty = cy + (cardH - th) / 2;
            drawToggle(g, tx, ty, tw, th, enabled, mouseX, mouseY);
            g.drawString(font, enabled ? "ON" : "OFF", tx + 27, ty + 9, enabled ? 0xFFDACFFF : MUTED, false);
            String key = rows[i][2];
            hitboxes.add(new Hitbox(tx, ty, tw, th, () -> HudConfig.toggle(key)));
        }
        g.drawString(font, "World scanners read client-loaded chunks only.", x, y + 3 * (cardH + gap) + 2, MUTED, false);
    }

    private void drawToggle(GuiGraphics g, int x, int y, int w, int h, boolean enabled, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        g.fill(x, y, x + w, y + h, enabled ? ACCENT_DIM : (hover ? 0xFF303948 : 0xFF252D39));
        g.fill(x, y, x + w, y + 1, enabled ? ACCENT : BORDER);
        g.fill(x, y + h - 1, x + w, y + h, enabled ? ACCENT : BORDER);
        g.fill(x + w - 2, y, x + w, y + h, enabled ? ACCENT : BORDER);
        g.fill(x, y, x + 2, y + h, enabled ? ACCENT : BORDER);
    }

    private static void toggleModule(Module module) {
        module.toggle();
        if (module.name.equals("Storage ESP")) HudConfig.set("storageEsp", module.isEnabled());
        if (module.name.equals("Amethyst Finder")) HudConfig.set("amethystFinder", module.isEnabled());
    }

    private void drawEmptyState(GuiGraphics g, int x, int y, int w, int h, String title, String detail) {
        int boxW = Math.min(360, w - 24);
        int bx = x + (w - boxW) / 2;
        int by = y + Math.max(24, (h - 72) / 2);
        g.fill(bx, by, bx + boxW, by + 72, SURFACE);
        g.fill(bx, by, bx + 3, by + 72, ACCENT);
        g.drawString(font, title, bx + 18, by + 17, TEXT, true);
        g.drawString(font, detail, bx + 18, by + 39, MUTED, false);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (int i = hitboxes.size() - 1; i >= 0; i--) {
                Hitbox box = hitboxes.get(i);
                if (inside(event.x(), event.y(), box.x, box.y, box.w, box.h)) {
                    box.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RSHIFT || event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private record Hitbox(int x, int y, int w, int h, Runnable action) {}
}
