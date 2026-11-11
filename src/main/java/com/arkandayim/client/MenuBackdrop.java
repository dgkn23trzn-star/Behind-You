package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The main menu background. Every picture in {@code config/arkandayim/menu/*.png} is
 * loaded at start up and shown one after another; the three bundled scenes are used
 * until the folder has pictures of its own. The rotation never stops.
 */
@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MenuBackdrop {

    private static final ResourceLocation[] BUNDLED = {
            new ResourceLocation(ArkandayimMod.MODID, "textures/gui/menu/bg_1.png"),
            new ResourceLocation(ArkandayimMod.MODID, "textures/gui/menu/bg_2.png"),
            new ResourceLocation(ArkandayimMod.MODID, "textures/gui/menu/bg_3.png")
    };
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final long SHOW_MS = 8000L;
    private static final long FADE_MS = 1200L;

    private static List<ResourceLocation> pictures;
    private static int index;
    private static long shownAt;

    private MenuBackdrop() {
    }

    @SubscribeEvent
    public static void onBackground(ScreenEvent.BackgroundRendered event) {
        if (event.getScreen() instanceof TitleScreen) {
            render(event.getGuiGraphics(), event.getScreen().width, event.getScreen().height, 0x55);
        }
    }

    private static void ensureLoaded() {
        if (pictures != null) {
            return;
        }
        pictures = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        Path folder = mc.gameDirectory.toPath().resolve("config").resolve("arkandayim").resolve("menu");
        try {
            if (!Files.isDirectory(folder)) {
                Files.createDirectories(folder);
            }
            TextureManager manager = mc.getTextureManager();
            int count = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.png")) {
                for (Path path : stream) {
                    try (InputStream in = Files.newInputStream(path)) {
                        var image = com.mojang.blaze3d.platform.NativeImage.read(in);
                        ResourceLocation id = new ResourceLocation(ArkandayimMod.MODID,
                                "menu_photo/" + count++);
                        manager.register(id, new DynamicTexture(image));
                        pictures.add(id);
                    } catch (Exception broken) {
                        LOGGER.warn("Could not read menu picture {}", path, broken);
                    }
                }
            }
        } catch (Exception error) {
            LOGGER.warn("Could not open the menu picture folder", error);
        }
        for (ResourceLocation bundled : BUNDLED) {
            pictures.add(bundled);
        }
        shownAt = Util.getMillis();
        index = 0;
    }

    /** Draws the current picture and fades the next one in. */
    public static void render(GuiGraphics graphics, int width, int height, int shade) {
        ensureLoaded();
        long now = Util.getMillis();
        if (now - shownAt >= SHOW_MS) {
            shownAt = now;
            index = (index + 1) % pictures.size();
        }
        int previous = (index + pictures.size() - 1) % pictures.size();
        float fade = Math.min(1.0F, (now - shownAt) / (float) FADE_MS);
        blit(graphics, pictures.get(previous), width, height, 1.0F);
        if (fade < 1.0F) {
            blit(graphics, pictures.get(index), width, height, fade);
        }
        graphics.fill(0, 0, width, height, (shade << 24));
        graphics.fillGradient(0, 0, width, height / 3, 0x66000000, 0x00000000);
        graphics.fillGradient(0, height * 2 / 3, width, height, 0x00000000, 0x99000000);
    }

    private static void blit(GuiGraphics graphics, ResourceLocation texture, int width, int height, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(texture, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
