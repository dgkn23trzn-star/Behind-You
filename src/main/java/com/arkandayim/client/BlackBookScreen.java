package com.arkandayim.client;

import com.arkandayim.item.BlackBookItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Three dark pages. The last one shows the coordinates written into the book.
 */
public class BlackBookScreen extends Screen {

    private static final int PAGES = 3;

    private final ItemStack book;
    private int page;

    private BlackBookScreen(ItemStack book) {
        super(Component.translatable("item.arkandayim.black_book"));
        this.book = book;
    }

    public static void open(ItemStack book) {
        Minecraft.getInstance().setScreen(new BlackBookScreen(book));
    }

    @Override
    protected void init() {
        int y = this.height - 34;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-1))
                .bounds(this.width / 2 - 74, y, 22, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(1))
                .bounds(this.width / 2 + 52, y, 22, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("arkandayim.book.close"), b -> this.onClose())
                .bounds(this.width / 2 - 40, y, 80, 20).build());
    }

    private void turn(int delta) {
        this.page = Math.floorMod(this.page + delta, PAGES);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int panelWidth = Math.min(300, this.width - 40);
        int panelHeight = 170;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2 - 10;

        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF20B0A0D);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFF3A3438);
        graphics.fill(left, top + panelHeight - 2, left + panelWidth, top + panelHeight, 0xFF3A3438);
        graphics.fill(left, top, left + 2, top + panelHeight, 0xFF3A3438);
        graphics.fill(left + panelWidth - 2, top, left + panelWidth, top + panelHeight, 0xFF3A3438);

        int textLeft = left + 16;
        int textTop = top + 16;
        int textWidth = panelWidth - 32;

        if (this.page == 0) {
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p1.title"), textLeft, textTop, textWidth, 0xFFE8E2D6);
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p1.l1"), textLeft, textTop + 26, textWidth, 0xFFA9A29A);
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p1.l2"), textLeft, textTop + 60, textWidth, 0xFFA9A29A);
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p1.l3"), textLeft, textTop + 94, textWidth, 0xFF8C4A3C);
        } else if (this.page == 1) {
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p2.title"), textLeft, textTop, textWidth, 0xFFE8E2D6);
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p2.l1"), textLeft, textTop + 26, textWidth, 0xFFA9A29A);
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p2.l2"), textLeft, textTop + 62, textWidth, 0xFFA9A29A);
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p2.l3"), textLeft, textTop + 98, textWidth, 0xFFA9A29A);
        } else {
            graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p3.title"), textLeft, textTop, textWidth, 0xFFE8E2D6);
            int[] coords = BlackBookItem.readHouseCoords(this.book);
            if (coords == null) {
                graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p3.none"), textLeft, textTop + 30, textWidth, 0xFF8C4A3C);
            } else {
                graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p3.l1"), textLeft, textTop + 26, textWidth, 0xFFA9A29A);
                graphics.drawString(this.font, "X: " + coords[0] + "   Y: " + coords[1] + "   Z: " + coords[2],
                        textLeft, textTop + 62, 0xFFFFD9A0, true);
                graphics.drawWordWrap(this.font, Component.translatable("arkandayim.book.p3.l2"), textLeft, textTop + 88, textWidth, 0xFFA9A29A);
            }
        }

        graphics.drawString(this.font, (this.page + 1) + " / " + PAGES,
                left + panelWidth - 16 - this.font.width((this.page + 1) + " / " + PAGES),
                top + panelHeight - 16, 0xFF6E6560, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
