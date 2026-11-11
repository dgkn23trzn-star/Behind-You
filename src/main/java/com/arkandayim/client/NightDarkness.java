package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.entity.ArasEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Unlit places are pitch black: deep caves even in broad daylight, and the whole
 * night sky except for a faint glow and a few stars. The flashlight (H) cuts a cone
 * forward out of the black, about ten blocks deep.
 */
@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NightDarkness {

    private static final ResourceLocation EYES =
            new ResourceLocation(ArkandayimMod.MODID, "textures/entity/distant_eyes.png");
    private static final ResourceLocation EYE_DOT =
            new ResourceLocation(ArkandayimMod.MODID, "textures/entity/eye_dot.png");
    private static final int BANDS = 40;

    private static float darkness = 0.0F;
    private static float lamp = 0.0F;
    private static boolean hintShown = false;

    public static float darkness() {
        return darkness;
    }

    /** How far the flashlight is faded in, 0 when off and 1 when fully on. */
    public static float lampFade() {
        return lamp;
    }

    public static boolean isActive() {
        return darkness > 0.2F;
    }

    /** True when the world is in the dark half of an overworld night. */
    public static boolean isNight(Minecraft mc) {
        if (mc.level == null || mc.level.dimension() != Level.OVERWORLD) {
            return false;
        }
        long time = mc.level.getDayTime() % 24000L;
        return time >= 13000L && time <= 23300L;
    }

    /** Deep underground the light level stays this low even at noon. */
    public static boolean isUnlit(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return false;
        }
        if (mc.level.dimension() != Level.OVERWORLD || mc.player.isSpectator()) {
            return false;
        }
        BlockPos pos = mc.player.blockPosition();
        return mc.level.getMaxLocalRawBrightness(pos) <= 3;
    }

    /**
     * The strongest light anywhere around the player: a torch, a lantern, lava or a
     * glowing block all count, so the darkness opens up wherever you light it.
     */
    public static int localLight(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return 0;
        }
        BlockPos base = mc.player.blockPosition().above();
        int best = mc.level.getMaxLocalRawBrightness(base);
        for (int dx = -7; dx <= 7; dx += 3) {
            for (int dz = -7; dz <= 7; dz += 3) {
                for (int dy = -3; dy <= 3; dy += 2) {
                    int light = mc.level.getMaxLocalRawBrightness(base.offset(dx, dy, dz));
                    if (light > best) {
                        best = light;
                    }
                }
            }
        }
        return best;
    }

    /** Light where you are looking, so a lit room shows up from its dark corner too. */
    public static int viewLight(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return 0;
        }
        Vec3 eye = mc.player.getEyePosition(1.0F);
        Vec3 look = mc.player.getViewVector(1.0F);
        int best = 0;
        for (int step = 2; step <= 14; step += 3) {
            for (int side = -1; side <= 1; side++) {
                Vec3 point = eye.add(look.scale(step)).add(-look.z * side * 1.6D, 0.0D, look.x * side * 1.6D);
                BlockPos pos = BlockPos.containing(point);
                int light = mc.level.getMaxLocalRawBrightness(pos);
                if (light > best) {
                    best = light;
                }
            }
        }
        return best;
    }

    public static boolean lampOn(Minecraft mc) {
        return mc.player != null && mc.player.hasEffect(MobEffects.NIGHT_VISION);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            darkness = 0.0F;
            lamp = 0.0F;
            hintShown = false;
            return;
        }
        boolean dark = isNight(mc) || isUnlit(mc);
        boolean lit = lampOn(mc);
        // Light level 3 or below stays fully black, 12 and above is completely clear.
        int light = Math.max(localLight(mc), viewLight(mc));
        float litByLight = Mth.clamp((light - 3) / 9.0F, 0.0F, 1.0F);
        float target = dark ? 1.0F - litByLight : 0.0F;
        darkness = Mth.lerp(0.08F, darkness, target);
        if (darkness < 0.002F) {
            darkness = 0.0F;
        }
        lamp = Mth.lerp(0.08F, lamp, lit ? 1.0F : 0.0F);
        if (lamp < 0.002F) {
            lamp = 0.0F;
        }

        if (!dark || darkness < 0.5F) {
            hintShown = false;
        } else if (!hintShown && darkness > 0.85F) {
            hintShown = true;
            mc.player.displayClientMessage(Component.translatable("arkandayim.dark.hint"), true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (darkness <= 0.002F) {
            return;
        }
        float skyAmount = skyVisibility(mc);
        drawBlackout(graphics, mc, width, height, skyAmount);
        drawGlowingEyes(graphics, mc, width, height);
    }

    /** How much of the night sky is in view: 0 looking down, 1 looking straight up. */
    private static float skyVisibility(Minecraft mc) {
        if (!isNight(mc) || isUnlit(mc)) {
            return 0.0F;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        return Mth.clamp((30.0F - camera.getXRot()) / 60.0F, 0.0F, 1.0F);
    }

    /**
     * With the flashlight the screen only dims down to one flat tone, so nothing is left
     * dark at the bottom, and the fog event already cuts the view off at ten blocks.
     */
    private static void drawBlackout(GuiGraphics graphics, Minecraft mc, int width, int height, float skyAmount) {
        int solid = (int) Mth.lerp(lamp, 250.0F, 62.0F);
        int topAlpha = Mth.clamp((int) (solid - 120.0F * skyAmount), 30, solid);
        if (solid - topAlpha < 2) {
            topAlpha = solid;
        }
        int fadeHeight = Math.max(1, (int) (height * 0.62F));

        for (int i = 0; i < BANDS; i++) {
            int y0 = i * height / BANDS;
            int y1 = (i + 1) * height / BANDS;
            if (y1 <= y0) {
                continue;
            }
            float t = Mth.clamp(y0 / (float) fadeHeight, 0.0F, 1.0F);
            int alpha = (int) Mth.lerp(t, topAlpha, solid);
            graphics.fill(0, y0, width, y1, alpha << 24);
        }
    }

    /**
     * In pure blackness only the eyes exist. Everything drawn in the world is under the
     * blackout, so the eyes are projected onto the screen and drawn on top of it.
     */
    private static void drawGlowingEyes(GuiGraphics graphics, Minecraft mc, int width, int height) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.getPosition();
        float yaw = (float) Math.toRadians(camera.getYRot());
        float pitch = (float) Math.toRadians(camera.getXRot());
        double fx = -Math.sin(yaw) * Math.cos(pitch);
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double ux = -rz * fy;
        double uy = rz * fx - rx * fz;
        double uz = rx * fy;

        double fov = ClientState.locked ? ClientState.zoom : mc.options.fov().get();
        double tanHalf = Math.tan(Math.toRadians(fov) * 0.5D);
        double aspect = width / (double) height;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ArasEntity aras) || !aras.isAlive() || aras.isInvisible()) {
                continue;
            }
            Vec3 head = new Vec3(aras.getX(), aras.getY() + aras.getBbHeight() * 0.95D, aras.getZ());
            drawEyePair(graphics, EYE_DOT, cam, head, 0.45D, 1.0F, width, height,
                    fx, fy, fz, rx, 0.0D, rz, ux, uy, uz, tanHalf, aspect);
        }

        List<Vec3> distant = DistantEyesFx.activePositions();
        for (Vec3 pos : distant) {
            drawEyePair(graphics, EYES, cam, pos, 1.9D, 0.85F, width, height,
                    fx, fy, fz, rx, 0.0D, rz, ux, uy, uz, tanHalf, aspect);
        }
    }

    private static void drawEyePair(GuiGraphics graphics, ResourceLocation texture, Vec3 cam, Vec3 pos,
                                    double worldWidth, float brightness,
                                    int width, int height,
                                    double fx, double fy, double fz,
                                    double rx, double ry, double rz,
                                    double ux, double uy, double uz,
                                    double tanHalf, double aspect) {
        double dx = pos.x - cam.x;
        double dy = pos.y - cam.y;
        double dz = pos.z - cam.z;
        double depth = dx * fx + dy * fy + dz * fz;
        if (depth < 0.6D) {
            return;
        }
        double side = dx * rx + dy * ry + dz * rz;
        double up = dx * ux + dy * uy + dz * uz;
        double ndcX = side / (depth * tanHalf * aspect);
        double ndcY = up / (depth * tanHalf);
        if (Math.abs(ndcX) > 1.15D || Math.abs(ndcY) > 1.15D) {
            return;
        }
        double pxPerBlock = height / (2.0D * depth * tanHalf);
        int size = (int) Mth.clamp(worldWidth * pxPerBlock, 2.0D, Math.max(8.0D, height * 0.9D));
        int x = (int) ((ndcX * 0.5D + 0.5D) * width) - size / 2;
        int y = (int) ((0.5D - ndcY * 0.5D) * height) - size / 2;

        float distanceFade = (float) Mth.clamp(1.35D - depth / 90.0D, 0.2D, 1.0D);
        float alpha = Mth.clamp(darkness * brightness * distanceFade, 0.0F, 1.0F);
        if (alpha <= 0.02F) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(texture, x, y, size, size, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
