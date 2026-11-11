package com.arkandayim.client;

import com.arkandayim.network.ModNetwork;
import com.arkandayim.network.PanelActionPacket;
import com.arkandayim.util.PanelEntries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The Arkandayim panel. It grows into place when it opens, lists every mod item and
 * has a column of world actions.
 */
public class PanelScreen extends Screen {

    private static final int PANEL_W = 336;
    private static final int PANEL_H = 224;
    private static final int SLOT = 20;
    private static final int COLUMNS = 6;

    private final List<ItemStack> stacks = PanelEntries.stacks();
    private int left;
    private int top;
    private int anim;

    private PanelScreen() {
        super(Component.translatable("arkandayim.panel.title"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new PanelScreen());
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        int buttonX = this.left + 156;
        addAction(buttonX, this.top + 34, PanelActionPacket.START_HUNT, "arkandayim.panel.start_hunt");
        addAction(buttonX, this.top + 56, PanelActionPacket.BUILD_HOUSE, "arkandayim.panel.build_house");
        addAction(buttonX, this.top + 78, PanelActionPacket.DARK_BIOME, "arkandayim.panel.dark_biome");
        addAction(buttonX, this.top + 100, PanelActionPacket.KNOCK_BOX, "arkandayim.panel.knock");
        addAction(buttonX, this.top + 122, PanelActionPacket.TOGGLE_TIME, "arkandayim.panel.time");
        addAction(buttonX, this.top + 144, PanelActionPacket.TOGGLE_CHASE, "arkandayim.panel.chase");

        this.addRenderableWidget(Button.builder(Component.translatable("arkandayim.panel.close"),
                        b -> this.onClose())
                .bounds(buttonX, this.top + 176, 160, 18).build());
    }

    private void addAction(int x, int y, byte action, String key) {
        this.addRenderableWidget(Button.builder(Component.translatable(key), b -> send(action))
                .bounds(x, y, 160, 18).build());
    }

    private static boolean canSend() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getConnection() != null
                && ModNetwork.CHANNEL.isRemotePresent(mc.getConnection().getConnection());
    }

    private static void send(byte action) {
        if (canSend()) {
            ModNetwork.sendToServer(new PanelActionPacket(action));
        }
    }

    private static void sendGive(byte index) {
        if (canSend()) {
            ModNetwork.sendToServer(new PanelActionPacket(PanelActionPacket.GIVE_ITEM, index));
        }
    }

    private int slotX(int index) {
        return this.left + 16 + (index % COLUMNS) * SLOT;
    }

    private int slotY(int index) {
        return this.top + 36 + (index / COLUMNS) * SLOT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        if (this.anim < 12) {
            this.anim++;
        }
        float t = Mth.clamp(this.anim / 12.0F, 0.0F, 1.0F);
        float eased = 1.0F - (float) Math.pow(1.0F - t, 3.0F);
        float scale = 0.86F + 0.14F * eased;
        float offsetY = (1.0F - eased) * 20.0F;

        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2.0F, this.height / 2.0F + offsetY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-this.width / 2.0F, -this.height / 2.0F, 0.0F);

        int right = this.left + PANEL_W;
        int bottom = this.top + PANEL_H;
        graphics.fill(this.left, this.top, right, bottom, 0xF00C0A0F);
        graphics.fill(this.left, this.top, right, this.top + 2, 0xFFB0622A);
        graphics.fill(this.left, bottom - 2, right, bottom, 0xFF3A2A1C);
        graphics.fill(this.left, this.top, this.left + 2, bottom, 0xFF3A2A1C);
        graphics.fill(right - 2, this.top, right, bottom, 0xFF3A2A1C);
        graphics.fill(this.left + 146, this.top + 26, this.left + 148, bottom - 26, 0xFF2A2118);

        graphics.drawString(this.font, this.title, this.left + 16, this.top + 10, 0xFFE7D8C4, false);
        graphics.drawString(this.font, Component.translatable("arkandayim.panel.items"),
                this.left + 16, this.top + 24, 0xFF8E8070, false);
        graphics.drawString(this.font, Component.translatable("arkandayim.panel.actions"),
                this.left + 156, this.top + 24, 0xFF8E8070, false);

        for (int i = 0; i < this.stacks.size(); i++) {
            int x = slotX(i);
            int y = slotY(i);
            graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xFF1B1620);
            graphics.renderItem(this.stacks.get(i), x, y);
            if (this.stacks.get(i).getCount() > 1) {
                graphics.renderItemDecorations(this.font, this.stacks.get(i), x, y);
            }
        }

        graphics.drawString(this.font, Component.translatable("arkandayim.panel.creative_hint"),
                this.left + 16, this.top + 148,
                this.minecraft != null && this.minecraft.player != null && this.minecraft.player.isCreative()
                        ? 0xFF7FA86A : 0xFFA85C4A, false);
        graphics.drawString(this.font, Component.translatable("arkandayim.panel.escape"),
                this.left + 16, this.top + 162, 0xFF6E6256, false);

        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();

        for (int i = 0; i < this.stacks.size(); i++) {
            int x = slotX(i);
            int y = slotY(i);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.renderTooltip(this.font, this.stacks.get(i), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            for (int i = 0; i < this.stacks.size(); i++) {
                int x = slotX(i);
                int y = slotY(i);
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    sendGive((byte) i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
