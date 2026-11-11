package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AngerHud {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        float anger = ClientState.displayAnger;

        if (anger >= 99.5F || ClientState.shattered) {
            drawFullScreenRed(graphics, width, height);
        }
        if (mc.player.hasEffect(MobEffects.DARKNESS)) {
            drawVoidOverlay(graphics, width, height);
        }
        drawBar(graphics, mc, width, height, anger);
        drawShatter(graphics, width, height);
    }

    /** The only screen wide red effect: it shows up once the bar is completely full. */
    private static void drawFullScreenRed(GuiGraphics graphics, int width, int height) {
        long now = System.currentTimeMillis();
        float pulse = 0.5F + 0.5F * (float) Math.sin(now / 150.0D);
        int alpha = (int) (46 + 54 * pulse);
        graphics.fill(0, 0, width, height, (alpha << 24) | 0x780808);

        int band = Math.max(18, height / 9);
        for (int i = 0; i < band; i++) {
            int a = (int) ((1.0F - i / (float) band) * (120 + 80 * pulse));
            int color = (a << 24) | 0x9C0F04;
            graphics.fill(0, i, width, i + 1, color);
            graphics.fill(0, height - 1 - i, width, height - i, color);
        }
        int side = Math.max(12, width / 12);
        for (int i = 0; i < side; i++) {
            int a = (int) ((1.0F - i / (float) side) * (90 + 60 * pulse));
            int color = (a << 24) | 0x8A0D04;
            graphics.fill(i, 0, i + 1, height, color);
            graphics.fill(width - 1 - i, 0, width - i, height, color);
        }
        for (int y = 0; y < height; y += 4) {
            graphics.fill(0, y, width, y + 1, 0x1A000000);
        }
        int flash = (int) (26 * Math.max(0.0F, Math.sin(now / 90.0D)));
        graphics.fill(0, 0, width, height, (flash << 24) | 0xB01406);
    }

    private static void drawVoidOverlay(GuiGraphics graphics, int width, int height) {
        graphics.fill(0, 0, width, height, 0x5A000000);
        int band = Math.max(24, height / 6);
        for (int i = 0; i < band; i++) {
            int a = (int) ((1.0F - i / (float) band) * 210);
            graphics.fill(0, i, width, i + 1, (a << 24));
            graphics.fill(0, height - 1 - i, width, height - i, (a << 24));
        }
        RandomSource random = RandomSource.create(System.currentTimeMillis() / 120L);
        for (int i = 0; i < 40; i++) {
            int x = random.nextInt(Math.max(1, width));
            int y = random.nextInt(Math.max(1, height));
            graphics.fill(x, y, x + 1, y + 1, 0x60FFFFFF);
        }
    }

    private static void drawBar(GuiGraphics graphics, Minecraft mc, int width, int height, float anger) {
        long now = System.currentTimeMillis();
        int barWidth = 9;
        int barHeight = 84;
        int x = 10;
        int y = (height - barHeight) / 2;
        float pulse = 0.5F + 0.5F * (float) Math.sin(now / 210.0D);
        boolean full = anger >= 99.5F;

        // outer terror glow
        int glowAlpha = (int) (24 + 70 * (anger / 100.0F) * (0.6F + 0.4F * pulse));
        for (int i = 4; i >= 1; i--) {
            graphics.fill(x - 3 - i, y - 3 - i, x + barWidth + 3 + i, y + barHeight + 3 + i,
                    ((glowAlpha / i) << 24) | 0x8E1A06);
        }

        // metal frame with jagged notches
        graphics.fill(x - 4, y - 4, x + barWidth + 4, y + barHeight + 4, 0xEE030202);
        graphics.fill(x - 3, y - 3, x + barWidth + 3, y - 2, 0xFF2C1712);
        graphics.fill(x - 3, y + barHeight + 2, x + barWidth + 3, y + barHeight + 3, 0xFF2C1712);
        graphics.fill(x - 3, y - 3, x - 2, y + barHeight + 3, 0xFF2C1712);
        graphics.fill(x + barWidth + 2, y - 3, x + barWidth + 3, y + barHeight + 3, 0xFF2C1712);
        for (int i = 0; i < 4; i++) {
            int ny = y + 6 + i * (barHeight - 12) / 3;
            graphics.fill(x - 4, ny, x - 2, ny + 2, 0xFF4A2418);
            graphics.fill(x + barWidth + 2, ny, x + barWidth + 4, ny + 2, 0xFF4A2418);
        }

        // empty channel
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF120405);

        int filled = Math.round(barHeight * (anger / 100.0F));
        for (int i = 0; i < filled; i++) {
            float t = i / (float) barHeight;
            float wave = 0.5F + 0.5F * (float) Math.sin(now / 300.0D + i * 0.16F);
            int red = (int) (86 + 150 * t + 16 * wave);
            int green = (int) (10 + 62 * t + 22 * wave * t);
            int blue = (int) (8 + 16 * t);
            int color = 0xFF000000 | (Math.min(255, red) << 16) | (Math.min(255, green) << 8) | Math.min(255, blue);
            graphics.fill(x, y + barHeight - 1 - i, x + barWidth, y + barHeight - i, color);
        }

        // wobbling liquid surface
        if (filled > 0 && filled < barHeight) {
            int surface = y + barHeight - filled;
            for (int cx = 0; cx < barWidth; cx++) {
                int wobble = (int) (Math.sin(now / 170.0D + cx * 0.9D) * 1.6D);
                int sy = surface + wobble;
                graphics.fill(x + cx, sy, x + cx + 1, sy + 2, full ? 0xFFFFC46A : 0xFFC23A12);
            }
        }

        // veins
        for (int v = 0; v < 2; v++) {
            int vx = x + 2 + v * 4;
            for (int i = 0; i < filled; i += 3) {
                int vy = y + barHeight - 1 - i;
                graphics.fill(vx, vy, vx + 1, vy + 1, 0x66000000);
            }
        }

        // tenth marks
        for (int i = 1; i < 10; i++) {
            int lineY = y + (barHeight * i / 10);
            graphics.fill(x, lineY, x + barWidth, lineY + 1, 0x99000000);
            graphics.fill(x - 2, lineY, x, lineY + 1, 0xFF3D1D12);
        }

        // the watching eye
        int eyeY = y - 12;
        int eyeBlink = (int) ((now / 97L) % 14L);
        graphics.fill(x - 1, eyeY - 2, x + 10, eyeY + 2, 0xCC050303);
        if (eyeBlink != 0) {
            graphics.fill(x, eyeY - 1, x + 9, eyeY + 1, 0xFFEAE0D2);
            int pupil = (int) (x + 3 + Math.sin(now / 900.0D) * 1.5D);
            graphics.fill(pupil, eyeY - 1, pupil + 2, eyeY + 1, 0xFFE03008);
        } else {
            graphics.fill(x, eyeY, x + 9, eyeY + 1, 0xFF8C1A08);
        }

        graphics.drawString(mc.font, Component.translatable("arkandayim.hud.null"),
                x - 1, eyeY - 11, 0xFFCC4A22, true);
        graphics.drawString(mc.font, (int) anger + "%", x - 2, y + barHeight + 7,
                full ? 0xFFFF7A3C : 0xFFB0684A, true);
        if (full) {
            graphics.drawString(mc.font, Component.translatable("arkandayim.hud.broken"),
                    x - 4, y + barHeight + 17, 0xFFE23A12, true);
        }
    }

    private static void drawShatter(GuiGraphics graphics, int width, int height) {
        if (ClientState.shatterAnim > 0) {
            float progress = 1.0F - (ClientState.shatterAnim / (float) ClientState.SHATTER_LEN);
            RandomSource random = RandomSource.create(9931L);
            int centerX = 10 + 4;
            int centerY = height / 2;
            int fade = (int) (200 * (1.0F - progress));
            for (int crack = 0; crack < 11; crack++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double px = centerX;
                double py = centerY;
                int segments = 10 + random.nextInt(8);
                for (int segment = 0; segment < segments; segment++) {
                    angle += (random.nextDouble() - 0.5D) * 0.75D;
                    px += Math.cos(angle) * (6.0D + progress * 26.0D);
                    py += Math.sin(angle) * (6.0D + progress * 26.0D);
                    int size = random.nextInt(3) + 1;
                    graphics.fill((int) px, (int) py, (int) px + size, (int) py + 1, (fade << 24) | 0xFFFFFF);
                }
            }
            int shardCount = 26;
            for (int i = 0; i < shardCount; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = progress * (40.0D + random.nextDouble() * 190.0D);
                int px = (int) (centerX + Math.cos(angle) * distance);
                int py = (int) (centerY + Math.sin(angle) * distance);
                int size = random.nextInt(5) + 2;
                int alpha = (int) (170 * (1.0F - progress));
                int color = random.nextBoolean() ? 0xFFFFFF : 0xC03010;
                graphics.fill(px, py, px + size, py + size, (alpha << 24) | color);
            }
        }

        if (ClientState.flash > 0.01F) {
            int alpha = (int) (ClientState.flash * 150.0F);
            graphics.fill(0, 0, width, height, (alpha << 24) | 0xFFD9B0);
        }
    }
}
