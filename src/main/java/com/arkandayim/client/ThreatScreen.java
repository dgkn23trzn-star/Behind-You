package com.arkandayim.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public class ThreatScreen extends Screen {

    public ThreatScreen() {
        super(Component.translatable("arkandayim.menu.threat"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("arkandayim.menu.continue"), button -> {
            MainMenuThreat.dismiss();
            Minecraft.getInstance().setScreen(new TitleScreen());
        }).bounds(center - 105, this.height - 46, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("arkandayim.menu.close"), button ->
                Minecraft.getInstance().stop()).bounds(center + 5, this.height - 46, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        int width = this.width;
        int height = this.height;
        RandomSource random = RandomSource.create(now / 80L);

        MenuBackdrop.render(graphics, width, height, 0x66);
        graphics.fillGradient(0, height / 3, width, height * 2 / 3, 0x30A81204, 0x00000000);

        for (int i = 0; i < 280; i++) {
            int x = random.nextInt(Math.max(1, width));
            int y = random.nextInt(Math.max(1, height));
            int alpha = 12 + random.nextInt(40);
            graphics.fill(x, y, x + 1 + random.nextInt(4), y + 1, (alpha << 24) | 0x9AA0A8);
        }

        int center = width / 2;
        int jitter = (int) (Math.sin(now / 140.0D) * 2.0D);
        graphics.drawCenteredString(this.font,
                Component.translatable("arkandayim.menu.threat").withStyle(ChatFormatting.DARK_RED),
                center + jitter, height / 2 - 62, 0xFFB01808);
        graphics.drawCenteredString(this.font,
                Component.translatable("arkandayim.menu.sub").withStyle(ChatFormatting.GRAY),
                center, height / 2 - 40, 0xFF6E5A56);

        int flicker = (int) (30 * Math.max(0.0F, Math.sin(now / 70.0D)));
        graphics.fill(0, 0, width, height, (flicker << 24) | 0x8A0C04);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
